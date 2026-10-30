package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.LoginService;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import shared.jpa.entity.User;

/**
 * Loads Hookset users for Spring Security during login.
 *
 * <p>Spring Security calls this service when a user submits the login form.
 * The user is looked up in the database and wrapped in a
 * {@link HooksetUserDetails} object so Spring Security can check the
 * password and create the authenticated session.</p>
 */
@Service
public class LoginServiceImpl implements LoginService {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(LoginServiceImpl.class);

    /** The repository used to look up users. */
    private final UserRepository userRepository;

    /**
     * Creates the login service with the repository it depends on.
     *
     * @param userRepository the repository used to look up users
     */
    public LoginServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username for authentication.
     *
     * <p>Usernames are saved in lowercase, so the lookup lowercases the
     * entered username first. That way login works the same no matter how
     * the user capitalizes it.</p>
     *
     * @param username the username entered on the login form
     * @return the user details used by Spring Security
     * @throws UsernameNotFoundException if the username is null or no user has that username
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("Attempting login for username: {}", username);

        // Spring Security expects this exception for any username that can't log in
        if (username == null) {
            log.warn("Login failed. Username is null");
            throw new UsernameNotFoundException("User not found");
        }

        // usernames are saved in lowercase, so the lookup has to match
        User user = userRepository.findByUsername(username.toLowerCase());

        // Spring Security expects this exception when the user does not exist
        if (user == null) {
            log.warn("Login failed. User not found: {}", username);
            throw new UsernameNotFoundException("User not found");
        }

        log.info("User found for login: {}", username);

        return new HooksetUserDetails(user);
    }
}