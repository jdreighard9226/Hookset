package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.ProfileService;
import edu.carroll.cs341.hookset.web.form.ProfileForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shared.jpa.entity.User;

/**
 * Service implementation for managing the logged-in user's profile.
 *
 * <p>This service currently handles password changes. The new password is
 * checked against its confirmation, the logged-in user is looked up from the
 * security context, and the password is hashed before it is saved.</p>
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    /** The repository used to look up and save users. */
    private final UserRepository userRepository;

    /** The encoder used to hash new passwords. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the profile service with the repository and encoder it depends on.
     *
     * @param userRepository the repository used to look up and save users
     * @param passwordEncoder the encoder used to hash new passwords
     */
    public ProfileServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Changes the password for the logged-in user.
     *
     * @param profileForm the form containing the new password and its confirmation
     * @return {@code true} if the password was changed, otherwise {@code false}
     */
    @Override
    public boolean changePassword(ProfileForm profileForm) {
        if (!profileForm.getPassword().equals(profileForm.getConfirmPassword())) {
            log.warn("Password change failed. Passwords do not match");
            return false;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // anonymous users still get an authentication object, so check the type too
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            log.warn("Password change failed. No authenticated user");
            return false;
        }

        String username = authentication.getName();

        log.info("Changing password for username: {}", username);

        User user = userRepository.findByUsername(username);

        if (user == null) {
            log.warn("Password change failed. User not found: {}", username);
            return false;
        }

        user.setHashPassword(passwordEncoder.encode(profileForm.getPassword()));
        userRepository.save(user);

        log.info("Password changed for username: {}", username);
        return true;
    }
}