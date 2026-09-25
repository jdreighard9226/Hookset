package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.LoginService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
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
     * @param username the username entered on the login form
     * @return the user details used by Spring Security
     * @throws UsernameNotFoundException if no user has that username
     */
    @Override
    public UserDetails loadUserByUsername(String username) {

        User user = userRepository.findByUsername(username);

        // Spring Security expects this exception when the user does not exist
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new HooksetUserDetails(user);
    }
}