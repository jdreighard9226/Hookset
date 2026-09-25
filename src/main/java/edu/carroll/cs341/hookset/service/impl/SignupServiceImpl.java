package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.SignupService;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shared.jpa.entity.User;

/**
 * Creates new Hookset user accounts.
 *
 * <p>This service checks that the requested username is not already taken,
 * hashes the password using the configured password encoder, and saves the
 * new user to the database.</p>
 */
@Service
public class SignupServiceImpl implements SignupService {

    /** The repository used to look up and save users. */
    private final UserRepository userRepository;

    /** The encoder used to hash user passwords before saving. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the signup service with the dependencies it needs.
     *
     * @param userRepository the repository used to look up and save users
     * @param passwordEncoder the encoder used to hash passwords
     */
    public SignupServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user account from the submitted signup form.
     *
     * <p>New users are never given administrator privileges.</p>
     *
     * @param signupForm the form containing the username and password
     * @return true if the user was created, false if the username is taken
     */
    @Override
    public boolean addUser(SignupForm signupForm) {

        // stop if the username is already in use
        User user = userRepository.findByUsername(signupForm.getUsername());
        if (user != null) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(signupForm.getUsername());
        newUser.setIsAdmin(false);

        // only the BCrypt hash is stored, never the plain password
        newUser.setHashPassword(
                passwordEncoder.encode(signupForm.getPassword())
        );

        userRepository.save(newUser);
        return true;
    }
}