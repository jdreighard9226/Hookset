package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.LoginService;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import entities.jpa.entity.User;

/**
 * Loads Hookset users for Spring Security during login.
 *
 * <p>Spring Security calls this service when a user submits the login form.
 * The user is looked up in the database and wrapped in a
 * {@link HooksetUserDetails} object so Spring Security can check the
 * password and create the authenticated session.</p>
 *
 * <p>This service only finds the user. The password is checked by Spring
 * Security after this returns, so a user being found does not mean the
 * login succeeded.</p>
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
     * @return the user details used by Spring Security to check the password
     * @throws UsernameNotFoundException if the username is null or no user has that username
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("Login attempt for username: {}", username);

        // Spring Security expects this exception for any username that can't log in
        if (username == null) {
            log.warn("Login failed. Username is null");
            throw new UsernameNotFoundException("User not found");
        }

        // usernames are saved in lowercase, so the lookup has to match
        User user = userRepository.findByUsername(username.toLowerCase());

        // Spring Security turns this into the same "bad credentials" error as a wrong password,
        // so the login page never tells someone whether a username exists
        if (user == null) {
            log.warn("Login failed. No account exists for username: {}", username);
            throw new UsernameNotFoundException("User not found");
        }

        // the password hasn't been checked yet, Spring Security does that with the details returned here
        log.info("Found user ID {} for username {}, handing off to Spring Security for the password check",
                user.getId(), username);

        return new HooksetUserDetails(user);
    }
}