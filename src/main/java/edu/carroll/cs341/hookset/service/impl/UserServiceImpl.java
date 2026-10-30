package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.UserService;
import edu.carroll.cs341.hookset.service.input.PasswordChangeInput;
import edu.carroll.cs341.hookset.service.input.SignupInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shared.jpa.entity.User;

/**
 * Service implementation for creating users and changing passwords.
 *
 * <p>Usernames are saved in lowercase so the same name can't be registered
 * twice with different capitalization. Passwords are hashed with the app's
 * password encoder before they are saved. The user is always passed in by
 * the caller, so this service doesn't depend on how they logged in.</p>
 *
 * <p>The validation methods are public so the controllers can call each one
 * and show a specific error message. {@link #addUser(SignupInput)} and
 * {@link #changePassword(PasswordChangeInput, Long)} run them again so bad
 * data can't be saved no matter who calls the service.</p>
 */
@Service
public class UserServiceImpl implements UserService {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** The longest username the Users table can store, from VARCHAR(20). */
    private static final int MAX_USERNAME_LENGTH = 20;

    /** The shortest password allowed. */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * The longest password allowed, in characters.
     *
     * <p>BCrypt can only hash 72 bytes. A character takes at most 4 bytes in
     * UTF-8, so capping at 18 characters guarantees a password can never go
     * over that limit no matter what characters it uses.</p>
     */
    private static final int MAX_PASSWORD_LENGTH = 18;

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
     * Changes the password for a user.
     *
     * <p>The new password is validated before the user is looked up, so a bad
     * password never touches the database.</p>
     *
     * @param passwordChangeInput the new password and its confirmation
     * @param userId the ID of the user whose password to change
     * @return {@code true} if the password was changed, otherwise {@code false}
     */
    @Override
    public boolean changePassword(PasswordChangeInput passwordChangeInput, Long userId) {
        if (passwordChangeInput == null) {
            log.warn("Password change failed. Password change input is null");
            return false;
        }

        if (userId == null) {
            log.warn("Password change failed. User ID is null");
            return false;
        }

        // runs every check itself so no caller can save a bad password
        if (isPasswordBlank(passwordChangeInput.password())) {
            return false;
        }

        if (isPasswordTooShort(passwordChangeInput.password())) {
            return false;
        }

        if (isPasswordTooLong(passwordChangeInput.password())) {
            return false;
        }

        if (!isPasswordSpaceFree(passwordChangeInput.password())) {
            return false;
        }

        if (!doPasswordsMatch(passwordChangeInput.password(), passwordChangeInput.confirmPassword())) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            log.warn("Password change failed. User not found with ID: {}", userId);
            return false;
        }

        user.setHashPassword(passwordEncoder.encode(passwordChangeInput.password()));
        userRepository.save(user);

        log.info("Password changed for username: {}", user.getUsername());

        return true;
    }

    /**
     * Creates a new user account.
     *
     * <p>New users are never admins. The username is saved in lowercase and
     * the password is hashed before saving.</p>
     *
     * @param signupInput the username, password, and password confirmation for the new account
     * @return {@code true} if the user was created, otherwise {@code false}
     */
    @Override
    public boolean addUser(SignupInput signupInput) {
        if (signupInput == null) {
            log.warn("Signup failed. Signup input is null");
            return false;
        }

        log.info("Attempting signup for username: {}", signupInput.username());

        // runs every check itself so no caller can save a bad user
        if (isUsernameBlank(signupInput.username())) {
            return false;
        }

        if (isUsernameTooLong(signupInput.username())) {
            return false;
        }

        if (!isUsernameSpaceFree(signupInput.username())) {
            return false;
        }

        if (isUsernameTaken(signupInput.username())) {
            return false;
        }

        if (isPasswordBlank(signupInput.password())) {
            return false;
        }

        if (isPasswordTooShort(signupInput.password())) {
            return false;
        }

        if (isPasswordTooLong(signupInput.password())) {
            return false;
        }

        if (!isPasswordSpaceFree(signupInput.password())) {
            return false;
        }

        if (!doPasswordsMatch(signupInput.password(), signupInput.confirmPassword())) {
            return false;
        }

        User newUser = new User();

        // lowercase so "JD" and "jd" can't both be registered
        newUser.setUsername(signupInput.username().toLowerCase());
        newUser.setIsAdmin(false);
        newUser.setHashPassword(passwordEncoder.encode(signupInput.password()));

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
     * Checks whether a username is longer than {@link #MAX_USERNAME_LENGTH} characters.
     *
     * @param username the username to check
     * @return {@code true} if the username is too long, otherwise {@code false}
     */
    @Override
    public boolean isUsernameTooLong(String username) {
        if (username != null && username.length() > MAX_USERNAME_LENGTH) {
            log.warn("Username validation failed. Username exceeds {} characters: {}",
                    MAX_USERNAME_LENGTH, username);
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
     * Checks whether a password is shorter than {@link #MIN_PASSWORD_LENGTH} characters.
     *
     * <p>A blank password returns false here because it is reported by
     * {@link #isPasswordBlank(String)} instead.</p>
     *
     * @param password the password to check
     * @return {@code true} if the password is too short, otherwise {@code false}
     */
    @Override
    public boolean isPasswordTooShort(String password) {
        if (password != null && !password.isBlank() && password.length() < MIN_PASSWORD_LENGTH) {
            log.warn("Password validation failed. Password is less than {} characters", MIN_PASSWORD_LENGTH);
            return true;
        }

        return false;
    }

    /**
     * Checks whether a password is longer than {@link #MAX_PASSWORD_LENGTH} characters.
     *
     * <p>The cap keeps every password under BCrypt's 72 byte limit, so hashing
     * can never fail. A null password returns false here because it is reported
     * by {@link #isPasswordBlank(String)} instead.</p>
     *
     * @param password the password to check
     * @return {@code true} if the password is too long, otherwise {@code false}
     */
    @Override
    public boolean isPasswordTooLong(String password) {
        if (password == null) {
            return false;
        }

        // the password itself is never logged
        if (password.length() > MAX_PASSWORD_LENGTH) {
            log.warn("Password validation failed. Password exceeds {} characters", MAX_PASSWORD_LENGTH);
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
}