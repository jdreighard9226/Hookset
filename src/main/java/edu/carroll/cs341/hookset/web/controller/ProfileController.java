package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.ProfileService;
import edu.carroll.cs341.hookset.web.form.ProfileForm;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles requests for the Hookset profile page.
 *
 * <p>This controller maps requests under {@code /profile}, displays the
 * logged-in user's profile, and handles password changes submitted from
 * the profile form.</p>
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    /** The service used to update the logged-in user's profile. */
    private final ProfileService profileService;

    /**
     * Creates the profile controller with the service it depends on.
     *
     * @param profileService the service used to update the logged-in user's profile
     */
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Handles GET requests for the profile page.
     *
     * <p>A new ProfileForm is filled with the logged-in user's username and
     * added to the model so the profile view can display it.</p>
     *
     * @param model the model used to pass the profile form to the view
     * @param authentication the logged-in user's authentication details
     * @return the name of the profile view to render
     */
    @GetMapping
    public String getProfile(Model model, Authentication authentication) {
        log.info("Loading profile page for username: {}", authentication.getName());

        ProfileForm profileForm = new ProfileForm();

        profileForm.setUsername(authentication.getName());

        model.addAttribute("profileForm", profileForm);

        return "profile";
    }

    /**
     * Handles POST requests to change the logged-in user's password.
     *
     * <p>If the form fails validation or the password change fails, the
     * profile page is shown again with the errors.</p>
     *
     * @param profileForm the submitted profile form
     * @param result the binding result holding any validation errors
     * @return a redirect to the home page on success, or the profile view if there are errors
     */
    @PostMapping
    public String postProfile(@Valid @ModelAttribute ProfileForm profileForm, BindingResult result) {
        log.info("Profile form submitted");

        if (result.hasErrors()) {
            log.warn("Profile form failed validation with {} error(s)", result.getErrorCount());
            return "profile";
        }

        boolean changePassword = profileService.changePassword(profileForm);

        if (!changePassword) {
            log.warn("Password change failed");

            result.reject("password.change.failed",
                    "Unable to change password. Please try again.");
            return "profile";
        }

        return "redirect:/";
    }
}