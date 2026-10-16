package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shared.jpa.entity.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link edu.carroll.cs341.hookset.service.impl.UserServiceImpl}.
 *
 * <p>Each service method is tested against a happy path, a crappy path, and one or more
 * crazy paths. Happy paths cover valid input with an expected result. Crappy paths cover
 * input the service is designed to reject, such as blank, too long, or taken usernames.
 * Crazy paths cover unusual input such as null values, names made only of spaces, and
 * values sitting exactly on a length limit.</p>
 *
 * <p>Two users are saved before each test, and the first one is logged in by loading their
 * real {@link HooksetUserDetails} through the application's {@link UserDetailsService}.</p>
 *
 * <p>Tests run against the H2 test database and each test is rolled back by
 * {@link Transactional}. Count assertions are measured against a baseline so the tests
 * do not depend on the database starting empty.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceImplTest {

    /** Service under test. */
    @Autowired
    private UserService userService;

    /** Repository used to set up and check user test data. */
    @Autowired
    private UserRepository userRepository;

    /** User details service used to log in test users the same way the app does. */
    @Autowired
    private UserDetailsService userDetailsService;

    /** Password encoder used to hash test user passwords the same way signup does. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Data source used to confirm the tests are running against H2. */
    @Autowired
    private DataSource dataSource;

    /** The user logged in for each test. */
    private User testUser;

    /** A second user used to test ownership rules. */
    private User otherUser;

    /**
     * Confirms the tests are running against the H2 database before each test.
     *
     * @throws SQLException if the connection metadata cannot be read
     */
    @BeforeEach
    public void checkDatabase() throws SQLException {
        // guards against a wrong profile pointing these tests at the real MySQL database
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.getMetaData().getURL().startsWith("jdbc:h2:"),
                    "Tests must use the H2 database");
        }
    }

    /**
     * Saves two test users and logs in the first one before each test.
     */
    @BeforeEach
    public void setUpUsers() {
        testUser = userRepository.save(createTestUser("testuser"));
        otherUser = userRepository.save(createTestUser("otheruser"));

        loginAs(testUser);
    }

    /**
     * Clears the security context after each test so a login does not leak into the next test.
     */
    @AfterEach
    public void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    // ---------- addUser ----------

    /**
     * Happy path: a valid signup creates one new user.
     */
    @Test
    public void testAddUserHappyPath() {
        String username = "testusername";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertTrue(result, "A valid signup should be saved");
        assertEquals(before + 1, after, "User count should grow by one");
        assertNotNull(savedUser, "The new user should be findable by username");
        assertEquals(username, savedUser.getUsername(), "Saved username should match the form");
    }

    /**
     * Happy path: a mixed case username is saved in lowercase.
     */
    @Test
    public void testAddUserHappyPathMixedCaseSavedLowercase() {
        String username = "NewUser";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        // looked up in lowercase, since H2 compares case-sensitively unlike MySQL
        User savedUser = userRepository.findByUsername("newuser");

        assertTrue(result, "A valid mixed case signup should be saved");
        assertEquals(before + 1, after, "User count should grow by one");
        assertNotNull(savedUser, "The new user should be saved under the lowercase name");
        assertEquals("newuser", savedUser.getUsername(), "Saved username should be lowercase");
    }

    /**
     * Crappy path: a blank username is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathUsernameBlank() {
        String username = "";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result, "A blank username should be rejected");
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crappy path: a 21 character username is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathUsernameTooLong() {
        // one over the VARCHAR(20) username column
        String username = "abcdefghijklmnopqrstu";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "A username over 20 characters should be rejected");
        assertEquals(before, after, "No user should be saved");
        assertNull(savedUser, "The rejected username should not exist");
    }

    /**
     * Crappy path: a username with a space is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathUsernameHasSpaces() {
        String username = "test user";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "A username with spaces should be rejected");
        assertEquals(before, after, "No user should be saved");
        assertNull(savedUser, "The rejected username should not exist");
    }

    /**
     * Crappy path: a username that already exists is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathUsernameTaken() {
        // "testuser" is saved in setUpUsers before every test
        String username = "testuser";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result, "A taken username should be rejected");
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crappy path: a taken username in different case is still rejected.
     */
    @Test
    public void testAddUserCrappyPathUsernameTakenDifferentCase() {
        String username = "TESTUSER";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result, "A taken username should be rejected regardless of case");
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crappy path: a password that doesn't match its confirmation is rejected.
     */
    @Test
    public void testAddUserCrappyPathPasswordsDontMatch() {
        String username = "newuser";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, "differentPassword1");

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "Mismatched passwords should be rejected");
        assertEquals(before, after, "No user should be saved");
        assertNull(savedUser, "The rejected username should not exist");
    }

    /**
     * Crappy path: a blank password is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathPasswordBlank() {
        String username = "newuser";
        String pw = "";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "A blank password should be rejected");
        assertEquals(before, after, "No user should be saved");
        assertNull(savedUser, "The rejected username should not exist");
    }

    /**
     * Crappy path: a 7 character password is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathPasswordTooShort() {
        // one under the 8 character minimum
        String username = "newuser";
        String pw = "passwor";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "A password under 8 characters should be rejected");
        assertEquals(before, after, "No user should be saved");
        assertNull(savedUser, "The rejected username should not exist");
    }

    /**
     * Crappy path: a password over BCrypt's 72 byte limit is rejected instead of crashing.
     */
    @Test
    public void testAddUserCrappyPathPasswordTooLong() {
        // BCrypt throws past 72 bytes, so this fails with an exception until isPasswordTooLong exists
        String username = "newuser";
        String pw = "a".repeat(73);
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "A password over 72 bytes should be rejected");
        assertEquals(before, after, "No user should be saved");
        assertNull(savedUser, "The rejected username should not exist");
    }

    /**
     * Crappy path: a password with a space is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathPasswordHasSpaces() {
        String username = "newuser";
        String pw = "test Password1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "A password with spaces should be rejected");
        assertEquals(before, after, "No user should be saved");
        assertNull(savedUser, "The rejected username should not exist");
    }

    /**
     * Crazy path: a null form is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrazyPathNullSignupForm() {
        int before = userRepository.findAll().size();

        boolean result = userService.addUser(null);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result, "A null signup form should be rejected");
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crazy path: a null username is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrazyPathNullUsername() {
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(null, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result, "A null username should be rejected");
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crazy path: a null password is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrazyPathNullPassword() {
        String username = "newuser";
        SignupForm signupForm = createSignupForm(username, null, null);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "A null password should be rejected");
        assertEquals(before, after, "No user should be saved");
        assertNull(savedUser, "The rejected username should not exist");
    }

    /**
     * Crazy path: a username of only spaces is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrazyPathUsernameAllSpaces() {
        // blank and contains spaces at once, either check alone should reject it
        String username = "   ";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result, "A username of only spaces should be rejected");
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crazy path: a username of exactly 20 characters is allowed.
     */
    @Test
    public void testAddUserCrazyPathUsernameExactly20Characters() {
        // boundary case, catches a > that should have been >= in isUsernameTooLong
        String username = "abcdefghijklmnopqrst";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertTrue(result, "A username of exactly 20 characters should be allowed");
        assertEquals(before + 1, after, "User count should grow by one");
        assertNotNull(savedUser, "The new user should be findable by username");
    }

    /**
     * Crazy path: a password of exactly 8 characters is allowed.
     */
    @Test
    public void testAddUserCrazyPathPasswordExactly8Characters() {
        // boundary case, catches a < that should have been <= in isPasswordTooShort
        String username = "newuser";
        String pw = "password";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertTrue(result, "A password of exactly 8 characters should be allowed");
        assertEquals(before + 1, after, "User count should grow by one");
        assertNotNull(savedUser, "The new user should be findable by username");
    }

    // ---------- Helpers ----------

    /**
     * Logs in a user by loading their real {@link HooksetUserDetails} and placing it in the security context.
     *
     * @param user the saved user to log in
     */
    private void loginAs(User user) {
        // loads through the real UserDetailsService so the test login matches what the app does
        HooksetUserDetails userDetails =
                (HooksetUserDetails) userDetailsService.loadUserByUsername(user.getUsername());

        // skips the password check, since these tests are about what a logged-in user can do
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Builds a user with fixed test values and the given username.
     *
     * <p>The password is hashed with the app's password encoder, the same way signup stores it.</p>
     *
     * @param username the username to assign to the user
     * @return a new, unsaved user
     */
    private User createTestUser(String username) {
        User user = new User();

        user.setUsername(username);
        user.setIsAdmin(false);
        user.setHashPassword(passwordEncoder.encode("test-password"));

        return user;
    }

    /**
     * Builds a signup form with the given values.
     *
     * @param username the username to put on the form
     * @param pw the password to put on the form
     * @param confirmPw the password confirmation to put on the form
     * @return a new signup form
     */
    private SignupForm createSignupForm(String username, String pw, String confirmPw) {
        SignupForm signupForm = new SignupForm();

        signupForm.setUsername(username);
        signupForm.setPassword(pw);
        signupForm.setConfirmPassword(confirmPw);

        return signupForm;
    }
}