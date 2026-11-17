package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.PasswordChangeInput;
import edu.carroll.cs341.hookset.service.input.SignupInput;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link UserService}.
 *
 * <p>Each service method is tested against a happy path and a crappy path. Happy
 * paths cover valid input that should be saved or pass a check. Crappy paths cover
 * bad input that should be rejected or fail a check without changing anything.</p>
 *
 * <p>Length checks are tested right at their limits, since that's where an
 * off-by-one mistake would show up.</p>
 *
 * <p>Test users are created through {@link UserService#addUser(SignupInput)}, and what
 * was saved is checked through {@link LoginService}, so no repositories are used.</p>
 *
 * <p>Tests run against the H2 test database, which starts empty, and each test is
 * rolled back by {@link Transactional}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    /** Service under test. */
    @Autowired
    private UserService userService;

    /** Service used to load saved users, since tests can't use repositories. */
    @Autowired
    private LoginService loginService;

    /** Password encoder used to check that a saved password is the hash of the expected password. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** The username given to the main test user. */
    private static final String TEST_USERNAME = "testuser";

    /** The plain text password the main test user signs up with. */
    private static final String TEST_PASSWORD = "test-password";

    /** The plain text password used when changing the test user's password. */
    private static final String NEW_PASSWORD = "new-password";

    // ---------- changePassword ----------

    /**
     * Happy path: a valid new password replaces the user's old one.
     */
    @Test
    public void testChangePasswordHappyPath() {
        // the user is added through the service, since tests can't use repositories
        boolean userAdded = userService.addUser(new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not changePassword
        assertTrue(userAdded, "The test user should be added");

        // addUser doesn't return the user, so loading it is how the test gets its ID
        UserDetails savedUser = loginService.loadUserByUsername(TEST_USERNAME);

        // the ID is only on Hookset's own user details, so the loaded user has to be that type
        assertInstanceOf(HooksetUserDetails.class, savedUser, "The loaded user should be a HooksetUserDetails");
        Long userId = ((HooksetUserDetails) savedUser).getId();

        boolean passwordChanged = userService.changePassword(new PasswordChangeInput(NEW_PASSWORD, NEW_PASSWORD), userId);

        // valid matching passwords for a real user, so the change should be accepted
        assertTrue(passwordChanged, "A valid password change should be accepted");

        UserDetails updatedUser = loginService.loadUserByUsername(TEST_USERNAME);

        // returning true isn't enough, the saved hash has to actually be the new password
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, updatedUser.getPassword()),
                "The saved password should be the hash of the new password");

        // the old password should stop working once it's been replaced
        assertFalse(passwordEncoder.matches(TEST_PASSWORD, updatedUser.getPassword()),
                "The old password should no longer match");
    }

    /**
     * Crappy path: changing the password of a user that doesn't exist is rejected.
     */
    @Test
    public void testChangePasswordCrappyPathMissingUser() {
        boolean userAdded = userService.addUser(new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not changePassword
        assertTrue(userAdded, "The test user should be added");

        UserDetails savedUser = loginService.loadUserByUsername(TEST_USERNAME);

        // the ID is only on Hookset's own user details, so the loaded user has to be that type
        assertInstanceOf(HooksetUserDetails.class, savedUser, "The loaded user should be a HooksetUserDetails");

        // only one user exists in this test, so the next ID can't belong to any user
        final Long missingUserId = ((HooksetUserDetails) savedUser).getId() + 1;

        boolean passwordChanged = userService.changePassword(
                new PasswordChangeInput(NEW_PASSWORD, NEW_PASSWORD), missingUserId);

        // there's no user with this ID, so there's no password to change
        assertFalse(passwordChanged, "Changing the password of a user that doesn't exist should be rejected");

        // a change aimed at the wrong ID shouldn't touch the real user's password
        UserDetails unchangedUser = loginService.loadUserByUsername(TEST_USERNAME);
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, unchangedUser.getPassword()),
                "The existing user's password should not change");
    }

    /**
     * Crappy path: a new password that doesn't match its confirmation is rejected.
     */
    @Test
    public void testChangePasswordCrappyPathPasswordsDontMatch() {
        final String otherPassword = "other-password";

        boolean userAdded = userService.addUser(new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not changePassword
        assertTrue(userAdded, "The test user should be added");

        UserDetails savedUser = loginService.loadUserByUsername(TEST_USERNAME);

        // the ID is only on Hookset's own user details, so the loaded user has to be that type
        assertInstanceOf(HooksetUserDetails.class, savedUser, "The loaded user should be a HooksetUserDetails");
        Long userId = ((HooksetUserDetails) savedUser).getId();

        boolean passwordChanged = userService.changePassword(
                new PasswordChangeInput(NEW_PASSWORD, otherPassword), userId);

        // both passwords are valid on their own, so the mismatch is the only reason to reject this
        assertFalse(passwordChanged, "A password that doesn't match its confirmation should be rejected");

        // a rejected change shouldn't touch the saved password
        UserDetails unchangedUser = loginService.loadUserByUsername(TEST_USERNAME);
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, unchangedUser.getPassword()),
                "The user's password should not change");
    }

    /**
     * Crappy path: a new password over the max length is rejected.
     */
    @Test
    public void testChangePasswordCrappyPathTooLong() {
        // one character over the max password length
        final String tooLongPassword = "a".repeat(19);

        boolean userAdded = userService.addUser(new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not changePassword
        assertTrue(userAdded, "The test user should be added");

        UserDetails savedUser = loginService.loadUserByUsername(TEST_USERNAME);

        // the ID is only on Hookset's own user details, so the loaded user has to be that type
        assertInstanceOf(HooksetUserDetails.class, savedUser, "The loaded user should be a HooksetUserDetails");
        Long userId = ((HooksetUserDetails) savedUser).getId();

        boolean passwordChanged = userService.changePassword(
                new PasswordChangeInput(tooLongPassword, tooLongPassword), userId);

        // the passwords match, so the length is the only reason to reject this
        assertFalse(passwordChanged, "A password over 18 characters should be rejected");

        // a rejected change shouldn't touch the saved password
        UserDetails unchangedUser = loginService.loadUserByUsername(TEST_USERNAME);
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, unchangedUser.getPassword()),
                "The user's password should not change");
    }

    /**
     * Crappy path: a new password under the min length is rejected.
     */
    @Test
    public void testChangePasswordCrappyPathTooShort() {
        // one character under the min password length
        final String tooShortPassword = "a".repeat(7);

        boolean userAdded = userService.addUser(new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not changePassword
        assertTrue(userAdded, "The test user should be added");

        UserDetails savedUser = loginService.loadUserByUsername(TEST_USERNAME);

        // the ID is only on Hookset's own user details, so the loaded user has to be that type
        assertInstanceOf(HooksetUserDetails.class, savedUser, "The loaded user should be a HooksetUserDetails");
        Long userId = ((HooksetUserDetails) savedUser).getId();

        boolean passwordChanged = userService.changePassword(
                new PasswordChangeInput(tooShortPassword, tooShortPassword), userId);

        // the passwords match, so the length is the only reason to reject this
        assertFalse(passwordChanged, "A password under 8 characters should be rejected");

        // a rejected change shouldn't touch the saved password
        UserDetails unchangedUser = loginService.loadUserByUsername(TEST_USERNAME);
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, unchangedUser.getPassword()),
                "The user's password should not change");
    }

    // ---------- addUser ----------

    /**
     * Happy path: a valid signup is saved and can be loaded with its password.
     */
    @Test
    public void testAddUserHappyPath() {
        boolean userAdded = userService.addUser(new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // valid input in every field, so addUser should accept it
        assertTrue(userAdded, "A valid user should be added");

        // loading the user is the only way to see what was saved, since tests can't use repositories
        UserDetails savedUser = loginService.loadUserByUsername(TEST_USERNAME);
        assertNotNull(savedUser, "The added user should be saved");
        assertEquals(TEST_USERNAME, savedUser.getUsername(), "The saved username should match the input");

        // the password is stored as a hash, so it's checked with the encoder instead of compared directly
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, savedUser.getPassword()),
                "The saved password should be the hash of the input password");
    }

    /**
     * Crappy path: a blank or too long username is rejected and no user is saved.
     */
    @Test
    public void testAddUserCrappyPathUsername() {
        final String blankUsername = "   ";

        // one character over the max username length
        final String tooLongUsername = "a".repeat(21);

        boolean blankUsernameAdded = userService.addUser(new SignupInput(blankUsername, TEST_PASSWORD, TEST_PASSWORD));
        assertFalse(blankUsernameAdded, "A user with a blank username should not be added");

        boolean tooLongUsernameAdded = userService.addUser(
                new SignupInput(tooLongUsername, TEST_PASSWORD, TEST_PASSWORD));
        assertFalse(tooLongUsernameAdded, "A username over 20 characters should not be added");

        // a rejected add returning false isn't enough, the user shouldn't have been saved either
        assertThrows(UsernameNotFoundException.class,
                () -> loginService.loadUserByUsername(tooLongUsername),
                "The too long username should not be saved");
    }

    /**
     * Crappy path: a blank, too short, or too long password is rejected and no user is saved.
     */
    @Test
    public void testAddUserCrappyPathPassword() {
        final String blankPassword = "   ";

        // one character under the min and one over the max password length
        final String tooShortPassword = "a".repeat(7);
        final String tooLongPassword = "a".repeat(19);

        // each password matches its confirmation, so the password itself is the only problem
        boolean blankPasswordAdded = userService.addUser(new SignupInput(TEST_USERNAME, blankPassword, blankPassword));
        assertFalse(blankPasswordAdded, "A user with a blank password should not be added");

        boolean tooShortPasswordAdded = userService.addUser(
                new SignupInput(TEST_USERNAME, tooShortPassword, tooShortPassword));
        assertFalse(tooShortPasswordAdded, "A password under 8 characters should not be added");

        boolean tooLongPasswordAdded = userService.addUser(
                new SignupInput(TEST_USERNAME, tooLongPassword, tooLongPassword));
        assertFalse(tooLongPasswordAdded, "A password over 18 characters should not be added");

        // a rejected add returning false isn't enough, the user shouldn't have been saved either
        assertThrows(UsernameNotFoundException.class,
                () -> loginService.loadUserByUsername(TEST_USERNAME),
                "No user should be saved when every password is rejected");
    }

    /**
     * Crappy path: a blank or mismatched password confirmation is rejected and no user is saved.
     */
    @Test
    public void testAddUserCrappyPathConfirmPassword() {
        final String blankConfirmPassword = "   ";
        final String otherPassword = "other-password";

        // the password itself is valid, so the confirmation is the only problem
        boolean blankConfirmAdded = userService.addUser(
                new SignupInput(TEST_USERNAME, TEST_PASSWORD, blankConfirmPassword));
        assertFalse(blankConfirmAdded, "A user with a blank password confirmation should not be added");

        boolean mismatchedConfirmAdded = userService.addUser(
                new SignupInput(TEST_USERNAME, TEST_PASSWORD, otherPassword));
        assertFalse(mismatchedConfirmAdded, "A password that doesn't match its confirmation should not be added");

        // a rejected add returning false isn't enough, the user shouldn't have been saved either
        assertThrows(UsernameNotFoundException.class,
                () -> loginService.loadUserByUsername(TEST_USERNAME),
                "No user should be saved when the confirmation is rejected");
    }

    // ---------- isUsernameBlank ----------

    /**
     * Happy path: a normal username is not blank.
     */
    @Test
    public void testIsUsernameBlankHappyPath() {
        boolean usernameBlank = userService.isUsernameBlank(TEST_USERNAME);

        // the username has real characters in it, so it shouldn't count as blank
        assertFalse(usernameBlank, "A normal username should not be blank");
    }

    /**
     * Crappy path: an empty or whitespace-only username is blank.
     */
    @Test
    public void testIsUsernameBlankCrappyPath() {
        final String emptyUsername = "";
        final String spacesUsername = "   ";

        // nothing at all is the simplest blank
        boolean emptyBlank = userService.isUsernameBlank(emptyUsername);
        assertTrue(emptyBlank, "An empty username should be blank");

        // only spaces looks like something was typed, but there's still no username
        boolean spacesBlank = userService.isUsernameBlank(spacesUsername);
        assertTrue(spacesBlank, "A username of only spaces should be blank");
    }

    // ---------- isUsernameTooLong ----------

    /**
     * Happy path: a username at or under 20 characters is not too long.
     */
    @Test
    public void testIsUsernameTooLongHappyPath() {
        // exactly at the limit, which is still allowed
        final String maxLengthUsername = "a".repeat(20);

        boolean normalTooLong = userService.isUsernameTooLong(TEST_USERNAME);
        assertFalse(normalTooLong, "A normal username should not be too long");

        // the limit itself is where an off-by-one mistake would wrongly reject a username
        boolean maxLengthTooLong = userService.isUsernameTooLong(maxLengthUsername);
        assertFalse(maxLengthTooLong, "A username of exactly 20 characters should not be too long");
    }

    /**
     * Crappy path: a username over 20 characters is too long.
     */
    @Test
    public void testIsUsernameTooLongCrappyPath() {
        // one character over the limit
        final String tooLongUsername = "a".repeat(21);

        boolean usernameTooLong = userService.isUsernameTooLong(tooLongUsername);

        // the first length past the limit is where an off-by-one mistake would wrongly allow a username
        assertTrue(usernameTooLong, "A username of 21 characters should be too long");
    }

    // ---------- isUsernameSpaceFree ----------

    /**
     * Happy path: a username with no spaces is space free.
     */
    @Test
    public void testIsUsernameSpaceFreeHappyPath() {
        boolean usernameSpaceFree = userService.isUsernameSpaceFree(TEST_USERNAME);

        // the test username has no spaces, so it should pass
        assertTrue(usernameSpaceFree, "A username with no spaces should be space free");
    }

    /**
     * Crappy path: a username with a space anywhere in it is not space free.
     */
    @Test
    public void testIsUsernameSpaceFreeCrappyPath() {
        final String middleSpaceUsername = "test user";
        final String leadingSpaceUsername = " testuser";
        final String trailingSpaceUsername = "testuser ";

        // a space in the middle is the obvious case
        boolean middleSpaceFree = userService.isUsernameSpaceFree(middleSpaceUsername);
        assertFalse(middleSpaceFree, "A username with a space in the middle should not be space free");

        // spaces at the ends are easy to miss when typing, so they need to be caught too
        boolean leadingSpaceFree = userService.isUsernameSpaceFree(leadingSpaceUsername);
        assertFalse(leadingSpaceFree, "A username starting with a space should not be space free");

        boolean trailingSpaceFree = userService.isUsernameSpaceFree(trailingSpaceUsername);
        assertFalse(trailingSpaceFree, "A username ending with a space should not be space free");
    }

    // ---------- isUsernameTaken ----------

    /**
     * Happy path: a username no account uses is not taken.
     */
    @Test
    public void testIsUsernameTakenHappyPath() {
        final String freeUsername = "freeuser";

        // a user is saved so a check that ignored the username and said taken would fail this test
        boolean userAdded = userService.addUser(new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not isUsernameTaken
        assertTrue(userAdded, "The test user should be added");

        boolean usernameTaken = userService.isUsernameTaken(freeUsername);

        // no account was created with this username, so it should be free to use
        assertFalse(usernameTaken, "A username no account uses should not be taken");
    }

    /**
     * Crappy path: a username an account already uses is taken, in any capitalization.
     */
    @Test
    public void testIsUsernameTakenCrappyPath() {
        // same username as the saved user with different capitalization
        final String mixedCaseUsername = "TestUser";

        boolean userAdded = userService.addUser(new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not isUsernameTaken
        assertTrue(userAdded, "The test user should be added");

        // the exact saved username is the obvious duplicate
        boolean exactTaken = userService.isUsernameTaken(TEST_USERNAME);
        assertTrue(exactTaken, "A username an account already uses should be taken");

        // the interface says the comparison ignores case, so a different capitalization is still a duplicate
        boolean mixedCaseTaken = userService.isUsernameTaken(mixedCaseUsername);
        assertTrue(mixedCaseTaken, "A taken username in a different case should still be taken");
    }

    // ---------- doPasswordsMatch ----------

    /**
     * Happy path: a password and an identical confirmation match.
     */
    @Test
    public void testDoPasswordsMatchHappyPath() {
        boolean passwordsMatch = userService.doPasswordsMatch(TEST_PASSWORD, TEST_PASSWORD);

        // both values are the same, so they should match
        assertTrue(passwordsMatch, "Identical passwords should match");
    }

    /**
     * Crappy path: a password and a different confirmation don't match, even if only the case differs.
     */
    @Test
    public void testDoPasswordsMatchCrappyPath() {
        final String otherPassword = "other-password";

        // same password as the test password with different capitalization
        final String mixedCasePassword = "Test-Password";

        // completely different passwords are the obvious mismatch
        boolean differentMatch = userService.doPasswordsMatch(TEST_PASSWORD, otherPassword);
        assertFalse(differentMatch, "Different passwords should not match");

        // passwords are case sensitive, so a different capitalization is a different password
        boolean caseMatch = userService.doPasswordsMatch(TEST_PASSWORD, mixedCasePassword);
        assertFalse(caseMatch, "Passwords that only differ by case should not match");
    }

    // ---------- isPasswordBlank ----------

    /**
     * Happy path: a normal password is not blank.
     */
    @Test
    public void testIsPasswordBlankHappyPath() {
        boolean passwordBlank = userService.isPasswordBlank(TEST_PASSWORD);

        // the password has real characters in it, so it shouldn't count as blank
        assertFalse(passwordBlank, "A normal password should not be blank");
    }

    /**
     * Crappy path: an empty or whitespace-only password is blank.
     */
    @Test
    public void testIsPasswordBlankCrappyPath() {
        final String emptyPassword = "";
        final String spacesPassword = "   ";

        // nothing at all is the simplest blank
        boolean emptyBlank = userService.isPasswordBlank(emptyPassword);
        assertTrue(emptyBlank, "An empty password should be blank");

        // only spaces looks like something was typed, but there's still no password
        boolean spacesBlank = userService.isPasswordBlank(spacesPassword);
        assertTrue(spacesBlank, "A password of only spaces should be blank");
    }

    // ---------- isPasswordTooShort ----------

    /**
     * Happy path: a password at or over 8 characters is not too short.
     */
    @Test
    public void testIsPasswordTooShortHappyPath() {
        // exactly at the limit, which is still allowed
        final String minLengthPassword = "a".repeat(8);

        boolean normalTooShort = userService.isPasswordTooShort(TEST_PASSWORD);
        assertFalse(normalTooShort, "A normal password should not be too short");

        // the limit itself is where an off-by-one mistake would wrongly reject a password
        boolean minLengthTooShort = userService.isPasswordTooShort(minLengthPassword);
        assertFalse(minLengthTooShort, "A password of exactly 8 characters should not be too short");
    }

    /**
     * Crappy path: a password under 8 characters is too short.
     */
    @Test
    public void testIsPasswordTooShortCrappyPath() {
        // one character under the limit
        final String tooShortPassword = "a".repeat(7);

        boolean passwordTooShort = userService.isPasswordTooShort(tooShortPassword);

        // the first length under the limit is where an off-by-one mistake would wrongly allow a password
        assertTrue(passwordTooShort, "A password of 7 characters should be too short");
    }

    // ---------- isPasswordTooLong ----------

    /**
     * Happy path: a password at or under 18 characters is not too long.
     */
    @Test
    public void testIsPasswordTooLongHappyPath() {
        // exactly at the limit, which is still allowed
        final String maxLengthPassword = "a".repeat(18);

        boolean normalTooLong = userService.isPasswordTooLong(TEST_PASSWORD);
        assertFalse(normalTooLong, "A normal password should not be too long");

        // the limit itself is where an off-by-one mistake would wrongly reject a password
        boolean maxLengthTooLong = userService.isPasswordTooLong(maxLengthPassword);
        assertFalse(maxLengthTooLong, "A password of exactly 18 characters should not be too long");
    }

    /**
     * Crappy path: a password over 18 characters is too long.
     */
    @Test
    public void testIsPasswordTooLongCrappyPath() {
        // one character over the limit
        final String tooLongPassword = "a".repeat(19);

        boolean passwordTooLong = userService.isPasswordTooLong(tooLongPassword);

        // the first length past the limit is where an off-by-one mistake would wrongly allow a password
        assertTrue(passwordTooLong, "A password of 19 characters should be too long");
    }

    // ---------- isPasswordSpaceFree ----------

    /**
     * Happy path: a password with no spaces is space free.
     */
    @Test
    public void testIsPasswordSpaceFreeHappyPath() {
        boolean passwordSpaceFree = userService.isPasswordSpaceFree(TEST_PASSWORD);

        // the test password has no spaces, so it should pass
        assertTrue(passwordSpaceFree, "A password with no spaces should be space free");
    }

    /**
     * Crappy path: a password with a space anywhere in it is not space free.
     */
    @Test
    public void testIsPasswordSpaceFreeCrappyPath() {
        final String middleSpacePassword = "test password";
        final String leadingSpacePassword = " test-password";
        final String trailingSpacePassword = "test-password ";

        // a space in the middle is the obvious case
        boolean middleSpaceFree = userService.isPasswordSpaceFree(middleSpacePassword);
        assertFalse(middleSpaceFree, "A password with a space in the middle should not be space free");

        // spaces at the ends are easy to miss when typing, so they need to be caught too
        boolean leadingSpaceFree = userService.isPasswordSpaceFree(leadingSpacePassword);
        assertFalse(leadingSpaceFree, "A password starting with a space should not be space free");

        boolean trailingSpaceFree = userService.isPasswordSpaceFree(trailingSpacePassword);
        assertFalse(trailingSpaceFree, "A password ending with a space should not be space free");
    }
}