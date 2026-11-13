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
import entities.jpa.entity.User;

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
 * <p>Checks go through the service wherever it can answer them. The repository is only
 * used for what the service can't tell us, such as user counts, saved hashes, and the
 * exact saved username.</p>
 *
 * <p>Tests run against the H2 test database and each test is rolled back by
 * {@link Transactional}.</p>
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

    /** A second user used to check that a password change only affects its own user. */
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
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertTrue(result, "A valid signup should be saved");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before + 1, after, "User count should grow by one");

        assertTrue(userService.isUsernameTaken(username), "The new username should now be taken");
    }

    /**
     * Happy path: a mixed case username is saved in lowercase.
     */
    @Test
    public void testAddUserHappyPathMixedCaseSavedLowercase() {
        String username = "NewUser";
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertTrue(result, "A valid mixed case signup should be saved");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before + 1, after, "User count should grow by one");

        // repository instead of isUsernameTaken, since that lowercases its input and can't prove how the name was saved
        User savedUser = userRepository.findByUsername("newuser");
        assertNotNull(savedUser, "The new user should be saved under the lowercase name");
        assertEquals("newuser", savedUser.getUsername(), "Saved username should be lowercase");
    }

    /**
     * Crappy path: a blank username is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathUsernameBlank() {
        String username = "";
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A blank username should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crappy path: a 21 character username is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathUsernameTooLong() {
        // one over the VARCHAR(20) username column
        String username = "abcdefghijklmnopqrstu";
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A username over 20 characters should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");

        assertFalse(userService.isUsernameTaken(username), "The rejected username should not be taken");
    }

    /**
     * Crappy path: a username with a space is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathUsernameHasSpaces() {
        String username = "test user";
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A username with spaces should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");

        assertFalse(userService.isUsernameTaken(username), "The rejected username should not be taken");
    }

    /**
     * Crappy path: a username that already exists is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathUsernameTaken() {
        // "testuser" is saved in setUpUsers before every test
        String username = "testuser";
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A taken username should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crappy path: a taken username in different case is still rejected.
     */
    @Test
    public void testAddUserCrappyPathUsernameTakenDifferentCase() {
        String username = "TESTUSER";
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A taken username should be rejected regardless of case");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crappy path: a password that doesn't match its confirmation is rejected.
     */
    @Test
    public void testAddUserCrappyPathPasswordsDontMatch() {
        String username = "newuser";
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(username, password, "differentPassword1");

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "Mismatched passwords should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");

        assertFalse(userService.isUsernameTaken(username), "The rejected username should not be taken");
    }

    /**
     * Crappy path: a blank password is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathPasswordBlank() {
        String username = "newuser";
        String password = "";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A blank password should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");

        assertFalse(userService.isUsernameTaken(username), "The rejected username should not be taken");
    }

    /**
     * Crappy path: a 7 character password is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathPasswordTooShort() {
        // one under the 8 character minimum
        String username = "newuser";
        String password = "passwor";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A password under 8 characters should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");

        assertFalse(userService.isUsernameTaken(username), "The rejected username should not be taken");
    }

    /**
     * Crappy path: a password over the 18 character limit is rejected instead of crashing BCrypt.
     */
    @Test
    public void testAddUserCrappyPathPasswordTooLong() {
        // well over the 18 character cap, and over BCrypt's 72 byte limit too
        String username = "newuser";
        String password = "a".repeat(73);
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A password over 18 characters should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");

        assertFalse(userService.isUsernameTaken(username), "The rejected username should not be taken");
    }

    /**
     * Crappy path: a password with a space is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrappyPathPasswordHasSpaces() {
        String username = "newuser";
        String password = "test Password1";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A password with spaces should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");

        assertFalse(userService.isUsernameTaken(username), "The rejected username should not be taken");
    }

    /**
     * Crazy path: a null input is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrazyPathNullSignupInput() {
        int before = userRepository.findAll().size();

        boolean result = userService.addUser(null);
        assertFalse(result, "A null signup input should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crazy path: a null username is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrazyPathNullUsername() {
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(null, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A null username should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
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
        assertFalse(result, "A null password should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");

        assertFalse(userService.isUsernameTaken(username), "The rejected username should not be taken");
    }

    /**
     * Crazy path: a username of only spaces is rejected and nothing is saved.
     */
    @Test
    public void testAddUserCrazyPathUsernameAllSpaces() {
        // blank and contains spaces at once, either check alone should reject it
        String username = "   ";
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertFalse(result, "A username of only spaces should be rejected");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before, after, "No user should be saved");
    }

    /**
     * Crazy path: a username of exactly 20 characters is allowed.
     */
    @Test
    public void testAddUserCrazyPathUsernameExactly20Characters() {
        // boundary case, catches a > that should have been >= in isUsernameTooLong
        String username = "abcdefghijklmnopqrst";
        String password = "testPassword1";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertTrue(result, "A username of exactly 20 characters should be allowed");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before + 1, after, "User count should grow by one");

        assertTrue(userService.isUsernameTaken(username), "The new username should now be taken");
    }

    /**
     * Crazy path: a password of exactly 8 characters is allowed.
     */
    @Test
    public void testAddUserCrazyPathPasswordExactly8Characters() {
        // boundary case, catches a < that should have been <= in isPasswordTooShort
        String username = "newuser";
        String password = "password";
        SignupInput signupInput = createSignupInput(username, password, password);

        int before = userRepository.findAll().size();

        boolean result = userService.addUser(signupInput);
        assertTrue(result, "A password of exactly 8 characters should be allowed");

        List<User> userList = userRepository.findAll();
        int after = userList.size();
        assertEquals(before + 1, after, "User count should grow by one");

        assertTrue(userService.isUsernameTaken(username), "The new username should now be taken");
    }

    // ---------- changePassword ----------

    /**
     * Happy path: the user's password is changed and the new one verifies against the saved hash.
     */
    @Test
    public void testChangePasswordHappyPath() {
        String newPassword = "newPassword1";
        PasswordChangeInput passwordChangeInput = createPasswordChangeInput(newPassword, newPassword);

        boolean result = userService.changePassword(passwordChangeInput, testUser.getId());
        assertTrue(result, "A valid password change should succeed");

        User updatedUser = userRepository.findByUsername("testuser");
        assertNotNull(updatedUser, "The user should still exist after a password change");

        // the hash is salted, so matches() is the only way to confirm the new password was set
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getHashPassword()),
                "The saved hash should match the new password");
        assertFalse(passwordEncoder.matches("test-password", updatedUser.getHashPassword()),
                "The old password should no longer match");

        // the other user's password should be untouched, so a change that hit the wrong row or every row would fail here
        User untouchedUser = userRepository.findByUsername(otherUser.getUsername());
        assertNotNull(untouchedUser, "The other user should still exist after a password change");
        assertTrue(passwordEncoder.matches("test-password", untouchedUser.getHashPassword()),
                "The other user's password should not change");
    }

    /**
     * Crappy path: a new password that doesn't match its confirmation is rejected and the old password stays.
     */
    @Test
    public void testChangePasswordCrappyPath() {
        String newPassword = "newPassword1";
        PasswordChangeInput passwordChangeInput = createPasswordChangeInput(newPassword, "differentPassword1");

        boolean result = userService.changePassword(passwordChangeInput, testUser.getId());
        assertFalse(result, "A password change with a mismatched confirmation should be rejected");

        User unchangedUser = userRepository.findByUsername("testuser");
        assertNotNull(unchangedUser, "The user should still exist after a rejected password change");

        assertTrue(passwordEncoder.matches("test-password", unchangedUser.getHashPassword()),
                "The old password should still match");
        assertFalse(passwordEncoder.matches(newPassword, unchangedUser.getHashPassword()),
                "The rejected new password should not match");
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

    /**
     * Crappy path: an empty username is blank.
     */
    @Test
    public void testIsUsernameBlankCrappyPath() {
        assertTrue(userService.isUsernameBlank(""),
                "An empty username should be blank");
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

    /**
     * Crappy path: a 21 character username is too long.
     */
    @Test
    public void testIsUsernameTooLongCrappyPath() {
        // one over the 20 character limit
        assertTrue(userService.isUsernameTooLong("abcdefghijklmnopqrstu"),
                "A username over 20 characters should be too long");
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

    /**
     * Crappy path: a username with a space is not space free.
     */
    @Test
    public void testIsUsernameSpaceFreeCrappyPath() {
        assertFalse(userService.isUsernameSpaceFree("test user"),
                "A username with a space should not be space free");
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

    /**
     * Crappy path: a username that already exists is taken.
     */
    @Test
    public void testIsUsernameTakenCrappyPath() {
        // "testuser" is saved in setUpUsers before every test
        assertTrue(userService.isUsernameTaken("testuser"),
                "An existing username should be taken");
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

    /**
     * Crappy path: a password and a different confirmation don't match.
     */
    @Test
    public void testDoPasswordsMatchCrappyPath() {
        assertFalse(userService.doPasswordsMatch("testPassword1", "differentPassword1"),
                "Different passwords should not match");
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

    /**
     * Crappy path: an empty password is blank.
     */
    @Test
    public void testIsPasswordBlankCrappyPath() {
        assertTrue(userService.isPasswordBlank(""),
                "An empty password should be blank");
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

    /**
     * Crappy path: a 7 character password is too short.
     */
    @Test
    public void testIsPasswordTooShortCrappyPath() {
        // one under the 8 character minimum
        assertTrue(userService.isPasswordTooShort("passwor"),
                "A password under 8 characters should be too short");
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

    /**
     * Crappy path: a 19 character password is too long.
     */
    @Test
    public void testIsPasswordTooLongCrappyPath() {
        // one over the 18 character limit
        assertTrue(userService.isPasswordTooLong("a".repeat(19)),
                "A password over 18 characters should be too long");
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

    /**
     * Crappy path: a password with a space is not space free.
     */
    @Test
    public void testIsPasswordSpaceFreeCrappyPath() {
        assertFalse(userService.isPasswordSpaceFree("test Password1"),
                "A password with a space should not be space free");
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
     * @param password the password for the new account
     * @param confirmPassword the password confirmation
     * @return new signup input
     */
    private SignupInput createSignupInput(String username, String password, String confirmPassword) {
        return new SignupInput(username, password, confirmPassword);
    }

    /**
     * Builds password change input with the given new password and confirmation.
     *
     * @param password the new password
     * @param confirmPassword the new password confirmation
     * @return new password change input
     */
    private PasswordChangeInput createPasswordChangeInput(String password, String confirmPassword) {
        return new PasswordChangeInput(password, confirmPassword);
    }
}