package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.UserService;
import edu.carroll.cs341.hookset.web.form.ProfileForm;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shared.jpa.entity.User;

/**
 * Service implementation for creating users and changing passwords.
 *
 * <p>Usernames are saved in lowercase so the same name can't be registered
 * twice with different capitalization. Passwords are hashed with the app's
 * password encoder before they are saved.</p>
 *
 * <p>The validation methods are public so the controllers can call each one
 * and show a specific error message. {@link #addUser(SignupForm)} and
 * {@link #changePassword(ProfileForm)} run them again so bad data can't be
 * saved no matter who calls the service.</p>
 */
@Service
public class UserServiceImpl implements UserService {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** The repository used to look up and save users. */
    private final UserRepository userRepository;

    /** The encoder used to hash new passwords. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the user service with the repository and encoder it depends on.
     *
     * @param userRepository the repository used to look up and save users
     * @param passwordEncoder the encoder used to hash new passwords
     */
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Changes the password for the logged-in user.
     *
     * <p>The new password is validated before the current user is looked up,
     * so a bad password never touches the database.</p>
     *
     * @param profileForm the form containing the new password and its confirmation
     * @return {@code true} if the password was changed, otherwise {@code false}
     */
    @Override
    public boolean changePassword(ProfileForm profileForm) {
        // runs every check itself so no caller can save a bad password
        if (isPasswordBlank(profileForm.getPassword())) {
            return false;
        }

        if (isPasswordTooShort(profileForm.getPassword())) {
            return false;
        }

        if (isPasswordTooLong(profileForm.getPassword())) {
            return false;
        }

        if (!isPasswordSpaceFree(profileForm.getPassword())) {
            return false;
        }

        if (!doPasswordsMatch(profileForm.getPassword(), profileForm.getConfirmPassword())) {
            return false;
        }

        User user = getCurrentUser();

        if (user == null) {
            log.warn("Password change failed. User not found");
            return false;
        }

        user.setHashPassword(passwordEncoder.encode(profileForm.getPassword()));
        userRepository.save(user);

        log.info("Password changed for username: {}", user.getUsername());

        return true;
    }

    /**
     * Creates a new user account from the submitted signup form.
     *
     * <p>New users are never admins. The username is saved in lowercase and
     * the password is hashed before saving.</p>
     *
     * @param signupForm the form containing the username and password
     * @return {@code true} if the user was created, otherwise {@code false}
     */
    @Override
    public boolean addUser(SignupForm signupForm) {
        if (signupForm == null) {
            log.warn("Signup failed. Signup form is null");
            return false;
        }

        log.info("Attempting signup for username: {}", signupForm.getUsername());

        // runs every check itself so no caller can save a bad user
        if (isUsernameBlank(signupForm.getUsername())) {
            return false;
        }

        if (isUsernameTooLong(signupForm.getUsername())) {
            return false;
        }

        if (!isUsernameSpaceFree(signupForm.getUsername())) {
            return false;
        }

        if (isUsernameTaken(signupForm.getUsername())) {
            return false;
        }

        if (isPasswordBlank(signupForm.getPassword())) {
            return false;
        }

        if (isPasswordTooShort(signupForm.getPassword())) {
            return false;
        }

        if (isPasswordTooLong(signupForm.getPassword())) {
            return false;
        }

        if (!isPasswordSpaceFree(signupForm.getPassword())) {
            return false;
        }

        if (!doPasswordsMatch(signupForm.getPassword(), signupForm.getConfirmPassword())) {
            return false;
        }

        User newUser = new User();

        // lowercase so "JD" and "jd" can't both be registered
        newUser.setUsername(signupForm.getUsername().toLowerCase());
        newUser.setIsAdmin(false);
        newUser.setHashPassword(passwordEncoder.encode(signupForm.getPassword()));

        userRepository.save(newUser);

        log.info("User created: {}", newUser.getUsername());
        return true;
    }

    /**
     * Checks whether a username is missing or only whitespace.
     *
     * @param username the username to check
     * @return {@code true} if the username is blank, otherwise {@code false}
     */
    @Override
    public boolean isUsernameBlank(String username) {
        if (username == null || username.isBlank()) {
            log.warn("Username validation failed. Username is blank");
            return true;
        }

        return false;
    }

    /**
     * Checks whether a username is longer than 20 characters.
     *
     * @param username the username to check
     * @return {@code true} if the username is over 20 characters, otherwise {@code false}
     */
    @Override
    public boolean isUsernameTooLong(String username) {
        // 20 matches the VARCHAR(20) username column
        if (username != null && username.length() > 20) {
            log.warn("Username validation failed. Username exceeds 20 characters: {}", username);
            return true;
        }

        return false;
    }

    /**
     * Checks whether a username is already used by another account.
     *
     * <p>The comparison ignores case, since usernames are saved in lowercase.
     * A blank username returns false because it can't match a saved user.</p>
     *
     * @param username the username to check
     * @return {@code true} if the username is taken, otherwise {@code false}
     */
    @Override
    public boolean isUsernameTaken(String username) {
        // a blank name can't belong to anyone, and this avoids calling toLowerCase on null
        if (isUsernameBlank(username)) {
            return false;
        }

        User user = userRepository.findByUsername(username.toLowerCase());

        if (user != null) {
            log.warn("Username already taken: {}", username);
            return true;
        }

        return false;
    }

    /**
     * Checks that a username has no spaces.
     *
     * @param username the username to check
     * @return {@code true} if the username has no spaces, otherwise {@code false}
     */
    @Override
    public boolean isUsernameSpaceFree(String username) {
        if (username != null && username.contains(" ")) {
            log.warn("Username validation failed. Username contains spaces: {}", username);
            return false;
        }

        return true;
    }

    /**
     * Checks that a password has no spaces.
     *
     * @param password the password to check
     * @return {@code true} if the password has no spaces, otherwise {@code false}
     */
    @Override
    public boolean isPasswordSpaceFree(String password) {
        // the password itself is never logged
        if (password != null && password.contains(" ")) {
            log.warn("Password validation failed. Password contains spaces");
            return false;
        }

        return true;
    }

    /**
     * Checks whether a password is shorter than 8 characters.
     *
     * <p>A blank password returns false here because it is reported by
     * {@link #isPasswordBlank(String)} instead.</p>
     *
     * @param password the password to check
     * @return {@code true} if the password is under 8 characters, otherwise {@code false}
     */
    @Override
    public boolean isPasswordTooShort(String password) {
        if (password != null && !password.isBlank() && password.length() < 8) {
            log.warn("Password validation failed. Password is less than 8 characters");
            return true;
        }

        return false;
    }

    /**
     * Checks whether a password is missing or only whitespace.
     *
     * @param password the password to check
     * @return {@code true} if the password is blank, otherwise {@code false}
     */
    @Override
    public boolean isPasswordBlank(String password) {
        if (password == null || password.isBlank()) {
            log.warn("Password validation failed. Password is blank");
            return true;
        }

        return false;
    }

    /**
     * Checks that a password and its confirmation match.
     *
     * @param password the password
     * @param confirmPassword the password confirmation
     * @return {@code true} if the passwords match, otherwise {@code false}
     */
    @Override
    public boolean doPasswordsMatch(String password, String confirmPassword) {
        // only password needs a null check, equals(null) just returns false
        if (password == null || !password.equals(confirmPassword)) {
            log.warn("Password validation failed. Passwords do not match");
            return false;
        }

        return true;
    }

    @Override
    public boolean isPasswordTooLong(String passsword) {
        return passsword.length() > 18;
    }

    /**
     * Gets the logged-in user from the security context.
     *
     * @return the logged-in user, or {@code null} if no user is logged in
     */
    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // visitors who aren't logged in still get an anonymous authentication
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            log.warn("No authenticated user found");
            return null;
        }

        return userRepository.findByUsername(authentication.getName());
    }
}