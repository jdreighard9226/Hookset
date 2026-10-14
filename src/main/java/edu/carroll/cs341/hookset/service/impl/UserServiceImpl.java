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
import shared.jpa.entity.User;

public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** The repository used to look up and save users. */
    private final UserRepository userRepository;

    /** The encoder used to hash new passwords. */
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean changePassword(ProfileForm profileForm) {

        if (isPasswordBlank(profileForm.getPassword())) {
            return false;
        }

        if (isPasswordTooShort(profileForm.getPassword())) {
            return false;
        }

        if (isPasswordSpaceFree(profileForm.getPassword()) == false) {
            return false;
        }

        if (doPasswordsMatch(profileForm) == false) {
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

    @Override
    public boolean addUser(SignupForm signupForm) {
        log.info("Attempting signup for username: {}", signupForm.getUsername());

        if (isUsernameBlank(signupForm.getUsername())) {
            return false;
        }

        if (isUsernameTooLong(signupForm.getUsername())) {
            return false;
        }

        if (isUsernameSpaceFree(signupForm.getUsername()) == false) {
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

        if (isPasswordSpaceFree(signupForm.getPassword()) == false) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(signupForm.getUsername());
        newUser.setIsAdmin(false);
        newUser.setHashPassword(
                passwordEncoder.encode(signupForm.getPassword())
        );

        userRepository.save(newUser);

        log.info("User created: {}", signupForm.getUsername());
        return true;
    }

    @Override
    public boolean isUsernameBlank(String username) {
        if (username == null || username.isBlank()) {
            log.warn("Username validation failed. Username is blank");
            return true;
        }

        return false;
    }

    @Override
    public boolean isUsernameTooLong(String username) {
        if (username != null && username.length() > 20) {
            log.warn("Username validation failed. Username exceeds 20 characters: {}", username);
            return true;
        }

        return false;
    }

    @Override
    public boolean isUsernameTaken(String username) {
        User user = userRepository.findByUsername(username);

        if (user != null) {
            log.warn("Username already taken: {}", username);
            return true;
        }

        return false;
    }

    @Override
    public boolean isUsernameSpaceFree(String username) {
        if (username != null && username.contains(" ")) {
            log.warn("Username validation failed. Username contains spaces: {}", username);
            return false;
        }

        return true;
    }

    @Override
    public boolean isPasswordSpaceFree(String password) {
        if (password != null && password.contains(" ")) {
            log.warn("Password validation failed. Password contains spaces");
            return false;
        }

        return true;
    }

    @Override
    public boolean isPasswordTooShort(String password) {
        if (password != null && !password.isBlank() && password.length() < 8) {
            log.warn("Password validation failed. Password is less than 8 characters");
            return true;
        }

        return false;
    }

    @Override
    public boolean isPasswordBlank(String password) {
        if (password == null || password.isBlank()) {
            log.warn("Password validation failed. Password is blank");
            return true;
        }

        return false;
    }



    @Override
    public boolean doPasswordsMatch(ProfileForm profileForm) {
        if (profileForm.getPassword() == null ||
                !profileForm.getPassword().equals(profileForm.getConfirmPassword())) {

            log.warn("Password validation failed. Passwords do not match");
            return false;
        }

        return true;
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            log.warn("No authenticated user found");
            return null;
        }

        return userRepository.findByUsername(authentication.getName());
    }
}
