package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.input.PasswordChangeInput;
import edu.carroll.cs341.hookset.service.input.SignupInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shared.jpa.entity.User;

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
 * <p>The service never reads the login itself, so tests pass the user's ID directly.
 * Two users are saved before each test so their IDs exist.</p>
 *
 * <p>Tests run against the H2 test database and each test is rolled back by
 * {@link Transactional}. Count assertions are measured against a baseline so the tests
 * do not depend on the database starting empty.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    /** Service under test. */
    @Autowired
    private UserService userService;

    /** Repository used to set up and check user test data. */
    @Autowired
    private UserRepository userRepository;

    /** Password encoder used to hash test user passwords the same way signup does. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** The main user each test acts as. */
    private User testUser;

    /** A second user used to test ownership rules. */
    private User otherUser;

    /**
     * Saves two test users before each test.
     */
    @BeforeEach
    public void setUpUsers() {
        testUser = userRepository.save(createTestUser("testuser"));
        otherUser = userRepository.save(createTestUser("otheruser"));
    }

    // ---------- addUser ----------

    /**
     * Happy path: a valid signup creates one new user.
     */
    @Test
    public void testAddUserHappyPath() {
        String username = "testusername";
        String pw = "testPassword1";
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertTrue(result, "A valid signup should be saved");
        assertEquals(before + 1, after, "User count should grow by one");
        assertNotNull(savedUser, "The new user should be findable by username");
        assertEquals(username, savedUser.getUsername(), "Saved username should match the input");
    }

    /**
     * Happy path: a mixed case username is saved in lowercase.
     */
    @Test
    public void testAddUserHappyPathMixedCaseSavedLowercase() {
        String username = "NewUser";
        String pw = "testPassword1";
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, "differentPassword1");

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "A password under 8 characters should be rejected");
        assertEquals(before, after, "No user should be saved");
        assertNull(savedUser, "The rejected username should not exist");
    }

    /**
     * Crappy path: a password over the 18 character limit is rejected instead of crashing BCrypt.
     */
    @Test
    public void testAddUserCrappyPathPasswordTooLong() {
        // well over the 18 character cap, and over BCrypt's 72 byte limit too
        String username = "newuser";
        String pw = "a".repeat(73);
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "A password over 18 characters should be rejected");
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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result, "A password with spaces should be rejected");
        assertEquals(before, after, "No user should be saved");
        assertNull(savedUser, "The rejected username should not exist");
    }

    /**
     * Crazy path: a null input is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrazyPathNullSignupInput() {
        int before = userRepository.findAll().size();

        boolean result = userService.addUser(null);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result, "A null signup input should be rejected");
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crazy path: a null username is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrazyPathNullUsername() {
        String pw = "testPassword1";
        SignupInput signupInput = createSignupInput(null, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, null, null);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

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
        SignupInput signupInput = createSignupInput(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertTrue(result, "A password of exactly 8 characters should be allowed");
        assertEquals(before + 1, after, "User count should grow by one");
        assertNotNull(savedUser, "The new user should be findable by username");
    }

    // ---------- changePassword ----------

    /**
     * Happy path: the user's password is changed and the new one verifies against the saved hash.
     */
    @Test
    public void testChangePasswordHappyPath() {
        String newPw = "newPassword1";
        PasswordChangeInput passwordChangeInput = createPasswordChangeInput(newPw, newPw);

        boolean result = userService.changePassword(passwordChangeInput, testUser.getId());

        // reload from the repository so the check reads what was saved, not the object in memory
        User updatedUser = userRepository.findByUsername("testuser");

        assertTrue(result, "A valid password change should succeed");
        assertNotNull(updatedUser, "The user should still exist after a password change");

        // the hash is salted, so matches() is the only way to confirm the new password was saved
        assertTrue(passwordEncoder.matches(newPw, updatedUser.getHashPassword()),
                "The saved hash should match the new password");
        assertFalse(passwordEncoder.matches("test-password", updatedUser.getHashPassword()),
                "The old password should no longer match");
    }

    // ---------- isUsernameBlank ----------

    /**
     * Happy path: a normal username is not blank.
     */
    @Test
    public void testIsUsernameBlankHappyPath() {
        assertFalse(userService.isUsernameBlank("newuser"),
                "A normal username should not be blank");
    }

    // ---------- isUsernameTooLong ----------

    /**
     * Happy path: a normal length username is not too long.
     */
    @Test
    public void testIsUsernameTooLongHappyPath() {
        assertFalse(userService.isUsernameTooLong("newuser"),
                "A normal length username should not be too long");
    }

    // ---------- isUsernameSpaceFree ----------

    /**
     * Happy path: a username with no spaces is space free.
     */
    @Test
    public void testIsUsernameSpaceFreeHappyPath() {
        assertTrue(userService.isUsernameSpaceFree("newuser"),
                "A username with no spaces should be space free");
    }

    // ---------- isUsernameTaken ----------

    /**
     * Happy path: a username nobody has used is not taken.
     */
    @Test
    public void testIsUsernameTakenHappyPath() {
        assertFalse(userService.isUsernameTaken("brandnewuser"),
                "An unused username should not be taken");
    }

    // ---------- doPasswordsMatch ----------

    /**
     * Happy path: a password and an identical confirmation match.
     */
    @Test
    public void testDoPasswordsMatchHappyPath() {
        assertTrue(userService.doPasswordsMatch("testPassword1", "testPassword1"),
                "Identical passwords should match");
    }

    // ---------- isPasswordBlank ----------

    /**
     * Happy path: a normal password is not blank.
     */
    @Test
    public void testIsPasswordBlankHappyPath() {
        assertFalse(userService.isPasswordBlank("testPassword1"),
                "A normal password should not be blank");
    }

    // ---------- isPasswordTooShort ----------

    /**
     * Happy path: a normal length password is not too short.
     */
    @Test
    public void testIsPasswordTooShortHappyPath() {
        assertFalse(userService.isPasswordTooShort("testPassword1"),
                "A normal length password should not be too short");
    }

    // ---------- isPasswordTooLong ----------

    /**
     * Happy path: a normal length password is not too long.
     */
    @Test
    public void testIsPasswordTooLongHappyPath() {
        assertFalse(userService.isPasswordTooLong("testPassword1"),
                "A normal length password should not be too long");
    }

    // ---------- isPasswordSpaceFree ----------

    /**
     * Happy path: a password with no spaces is space free.
     */
    @Test
    public void testIsPasswordSpaceFreeHappyPath() {
        assertTrue(userService.isPasswordSpaceFree("testPassword1"),
                "A password with no spaces should be space free");
    }

    // ---------- Helpers ----------

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
     * Builds signup input with the given values.
     *
     * @param username the username for the new account
     * @param pw the password for the new account
     * @param confirmPw the password confirmation
     * @return new signup input
     */
    private SignupInput createSignupInput(String username, String pw, String confirmPw) {
        return new SignupInput(username, pw, confirmPw);
    }

    /**
     * Builds password change input with the given new password and confirmation.
     *
     * @param pw the new password
     * @param confirmPw the new password confirmation
     * @return new password change input
     */
    private PasswordChangeInput createPasswordChangeInput(String pw, String confirmPw) {
        return new PasswordChangeInput(pw, confirmPw);
    }
}