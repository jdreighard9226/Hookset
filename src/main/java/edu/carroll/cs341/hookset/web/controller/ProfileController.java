package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.UserService;
import edu.carroll.cs341.hookset.service.input.PasswordChangeInput;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.form.ProfileForm;
import edu.carroll.cs341.hookset.web.mapper.UserMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 *
 * <p>The logged-in user is passed in by Spring Security and handed to the
 * user service as an ID. The profile form is mapped to
 * {@link PasswordChangeInput} before it reaches the service.</p>
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    /** The service used to update the logged-in user's profile. */
    private final UserService userService;

    /** The mapper used to convert the profile form into service input. */
    private final UserMapper userMapper;

    /**
     * Creates the profile controller with the service and mapper it depends on.
     *
     * @param userService the service used to update the logged-in user's profile
     * @param userMapper the mapper used to convert the profile form into service input
     */
    public ProfileController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Handles GET requests for the profile page.
     *
     * <p>A new {@link ProfileForm} is filled with the logged-in user's username
     * and added to the model so the profile view can display it.</p>
     *
     * @param user the logged-in user
     * @param model the model used to pass the profile form to the view
     * @return the name of the profile view to render
     */
    @GetMapping
    public String getProfile(@AuthenticationPrincipal HooksetUserDetails user, Model model) {
        log.info("Loading profile page for username: {}", user.getUsername());

        ProfileForm profileForm = new ProfileForm();

        profileForm.setUsername(user.getUsername());

        model.addAttribute("profileForm", profileForm);

        return "profile";
    }

    /**
     * Handles POST requests to change the logged-in user's password.
     *
     * <p>Blank, length, and matching errors come from the form annotations.
     * Spaces are checked here because no annotation covers them. If anything
     * fails, the profile page is shown again with the errors. Otherwise the
     * user is redirected to the home page.</p>
     *
     * @param profileForm the submitted profile form
     * @param result the validation results for the form
     * @param user the logged-in user changing their password
     * @param model the model used to pass error flags to the view
     * @return a redirect to the home page on success, or the profile view if there are errors
     */
    @PostMapping
    public String postProfile(@Valid @ModelAttribute ProfileForm profileForm,
                              BindingResult result,
                              @AuthenticationPrincipal HooksetUserDetails user,
                              Model model) {
        log.info("Profile form submitted");

        boolean hasCustomErrors = false;

        // no annotation checks for spaces, so this is the only place that message comes from
        if (!userService.isPasswordSpaceFree(profileForm.getPassword())) {
            model.addAttribute("passwordHasSpaces", true);
            hasCustomErrors = true;
        }

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Profile form failed validation");
            return "profile";
        }

        PasswordChangeInput passwordChangeInput = userMapper.toPasswordChangeInput(profileForm);

        if (!userService.changePassword(passwordChangeInput, user.getId())) {
            log.warn("Password change failed");
            model.addAttribute("passwordChangeFailed", true);
            return "profile";
        }

        return "redirect:/";
    }
}