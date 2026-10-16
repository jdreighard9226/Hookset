package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.impl.UserServiceImpl;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import jakarta.transaction.Transactional;
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
import shared.jpa.entity.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceImplTest {

    /** User details service used to log in test users the same way the app does. */
    @Autowired
    private UserDetailsService userDetailsService;

    /** Password encoder used to hash test user passwords the same way signup does. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;


    /** The user logged in for each test. */
    private User testUser;

    /** A second user used to test ownership rules. */
    private User otherUser;

    /**
     * Saves two test users and logs in the first one before each test.
     */
    @BeforeEach
    public void setUpUsers() {
        testUser = userRepository.save(createTestUser("testuser"));
        otherUser = userRepository.save(createTestUser("otheruser"));

        loginAs(testUser);
    }


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

        assertTrue(result);
        assertEquals(before + 1, after);
        assertNotNull(savedUser);
        assertEquals(username, savedUser.getUsername());
    }

    @Test
    public void testAddUserHappyPathMixedCaseSavedLowercase() {
        String username = "NewUser";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername("newuser");

        assertTrue(result);
        assertEquals(before + 1, after);
        assertNotNull(savedUser);
        assertEquals("newuser", savedUser.getUsername());
    }

    @Test
    public void testAddUserCrappyPathUsernameBlank() {
        String username = "";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result);
        assertEquals(before, after);
    }

    @Test
    public void testAddUserCrappyPathUsernameTooLong() {
        String username = "abcdefghijklmnopqrstu";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result);
        assertEquals(before, after);
        assertNull(savedUser);
    }

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

        assertFalse(result);
        assertEquals(before, after);
        assertNull(savedUser);
    }

    @Test
    public void testAddUserCrappyPathUsernameTaken() {
        String username = "testuser";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result);
        assertEquals(before, after);
    }

    @Test
    public void testAddUserCrappyPathUsernameTakenDifferentCase() {
        String username = "TESTUSER";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result);
        assertEquals(before, after);
    }

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

        assertFalse(result);
        assertEquals(before, after);
        assertNull(savedUser);
    }

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

        assertFalse(result);
        assertEquals(before, after);
        assertNull(savedUser);
    }

    @Test
    public void testAddUserCrappyPathPasswordTooShort() {
        String username = "newuser";
        String pw = "passwor";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result);
        assertEquals(before, after);
        assertNull(savedUser);
    }

    @Test
    public void testAddUserCrappyPathPasswordTooLong() {
        String username = "newuser";
        String pw = "a".repeat(73);
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result);
        assertEquals(before, after);
        assertNull(savedUser);
    }

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

        assertFalse(result);
        assertEquals(before, after);
        assertNull(savedUser);
    }

    @Test
    public void testAddUserCrazyPathNullSignupForm() {
        int before = userRepository.findAll().size();

        assertThrows(NullPointerException.class, () -> userService.addUser(null));

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertEquals(before, after);
    }

    @Test
    public void testAddUserCrazyPathNullUsername() {
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(null, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result);
        assertEquals(before, after);
    }

    @Test
    public void testAddUserCrazyPathNullPassword() {
        String username = "newuser";
        SignupForm signupForm = createSignupForm(username, null, null);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertFalse(result);
        assertEquals(before, after);
        assertNull(savedUser);
    }

    @Test
    public void testAddUserCrazyPathUsernameAllSpaces() {
        String username = "   ";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();

        assertFalse(result);
        assertEquals(before, after);
    }

    @Test
    public void testAddUserCrazyPathUsernameExactly20Characters() {
        String username = "abcdefghijklmnopqrst";
        String pw = "testPassword1";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertTrue(result);
        assertEquals(before + 1, after);
        assertNotNull(savedUser);
    }

    @Test
    public void testAddUserCrazyPathPasswordExactly8Characters() {
        String username = "newuser";
        String pw = "password";
        SignupForm signupForm = createSignupForm(username, pw, pw);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupForm);

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        User savedUser = userRepository.findByUsername(username);

        assertTrue(result);
        assertEquals(before + 1, after);
        assertNotNull(savedUser);
    }

    /**
     * Clears the security context after each test so a login does not leak into the next test.
     */
    @AfterEach
    public void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Logs in a user by loading their real {@link HooksetUserDetails} and placing it in the security context.
     *
     * @param user the saved user to log in
     */
    private void loginAs(User user) {
        HooksetUserDetails userDetails =
                (HooksetUserDetails) userDetailsService.loadUserByUsername(user.getUsername());

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