package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.form.ProfileForm;
import edu.carroll.cs341.hookset.web.form.SignupForm;

/**
 * Service for creating users and changing passwords.
 *
 * <p>The validation methods are public so the controllers can call each one
 * and show a specific error message. {@link #addUser(SignupForm)} and
 * {@link #changePassword(ProfileForm)} run them again so bad data can't be
 * saved no matter who calls the service.</p>
 */
public interface UserService {

    /**
     * Changes the password for the logged-in user.
     *
     * @param profileForm the form containing the new password and its confirmation
     * @return {@code true} if the password was changed, otherwise {@code false}
     */
    boolean changePassword(ProfileForm profileForm);

    /**
     * Creates a new user account from the submitted signup form.
     *
     * @param signupForm the form containing the username and password
     * @return {@code true} if the user was created, otherwise {@code false}
     */
    boolean addUser(SignupForm signupForm);

    /**
     * Checks whether a username is missing or only whitespace.
     *
     * @param username the username to check
     * @return {@code true} if the username is blank, otherwise {@code false}
     */
    boolean isUsernameBlank(String username);

    /**
     * Checks whether a username is longer than 20 characters.
     *
     * @param username the username to check
     * @return {@code true} if the username is over 20 characters, otherwise {@code false}
     */
    boolean isUsernameTooLong(String username);

    /**
     * Checks that a username has no spaces.
     *
     * @param username the username to check
     * @return {@code true} if the username has no spaces, otherwise {@code false}
     */
    boolean isUsernameSpaceFree(String username);

    /**
     * Checks whether a username is already used by another account.
     *
     * <p>The comparison ignores case, since usernames are saved in lowercase.</p>
     *
     * @param username the username to check
     * @return {@code true} if the username is taken, otherwise {@code false}
     */
    boolean isUsernameTaken(String username);

    /**
     * Checks that a password and its confirmation match.
     *
     * @param password the password
     * @param confirmPassword the password confirmation
     * @return {@code true} if the passwords match, otherwise {@code false}
     */
    boolean doPasswordsMatch(String password, String confirmPassword);

    /**
     * Checks whether a password is missing or only whitespace.
     *
     * @param password the password to check
     * @return {@code true} if the password is blank, otherwise {@code false}
     */
    boolean isPasswordBlank(String password);

    /**
     * Checks whether a password is shorter than 8 characters.
     *
     * @param password the password to check
     * @return {@code true} if the password is under 8 characters, otherwise {@code false}
     */
    boolean isPasswordTooShort(String password);

    /**
     * Checks that a password has no spaces.
     *
     * @param password the password to check
     * @return {@code true} if the password has no spaces, otherwise {@code false}
     */
    boolean isPasswordSpaceFree(String password);
}