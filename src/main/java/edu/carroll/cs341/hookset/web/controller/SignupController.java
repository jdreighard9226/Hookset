package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.UserService;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles requests for the Hookset signup page.
 *
 * <p>This controller maps requests under {@code /signup}, displays the signup
 * form, validates submitted form data, and creates new user accounts through
 * the user service. After a successful signup the user is redirected to the
 * login page.</p>
 */
@Controller
@RequestMapping("/signup")
public class SignupController {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(SignupController.class);

    /** The service used to create new user accounts. */
    private final UserService userService;

    /**
     * Creates the signup controller with the service it depends on.
     *
     * @param userService the service used to create new user accounts
     */
    public SignupController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles GET requests for the Hookset signup page.
     *
     * <p>A new {@code SignupForm} is added to the model so the signup view can
     * bind form fields to the object.</p>
     *
     * @param model the model used to provide data to the signup view
     * @return the name of the signup view to render
     */
    @GetMapping
    public String getSignup(Model model) {
        log.info("Loading signup page");

        model.addAttribute("signupForm", new SignupForm());

        return "signup";
    }

    /**
     * Handles POST requests from the Hookset signup form.
     *
     * <p>Blank, length, and matching errors come from the form annotations.
     * Spaces and taken usernames are checked here because no annotation covers
     * them. If anything fails, the signup page is shown again with the errors.
     * Otherwise the new user is created and redirected to the login page.</p>
     *
     * @param signupForm the submitted signup form
     * @param result the validation results for the form
     * @param model the model used to pass error flags to the view
     * @return the view to render or the redirect to perform
     */
    @PostMapping
    public String postSignup(
            @Valid @ModelAttribute SignupForm signupForm,
            BindingResult result,
            Model model) {

        log.info("Signup form submitted");

        boolean hasCustomErrors = false;

        // no annotation checks for spaces, so this is the only place that message comes from
        if (!userService.isUsernameSpaceFree(signupForm.getUsername())) {
            model.addAttribute("usernameHasSpaces", true);
            hasCustomErrors = true;
        }

        // needs the database, so no annotation can check it
        if (userService.isUsernameTaken(signupForm.getUsername())) {
            model.addAttribute("usernameTaken", true);
            hasCustomErrors = true;
        }

        if (!userService.isPasswordSpaceFree(signupForm.getPassword())) {
            model.addAttribute("passwordHasSpaces", true);
            hasCustomErrors = true;
        }

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Signup form failed validation");
            return "signup";
        }

        if (!userService.addUser(signupForm)) {
            log.warn("Signup failed");
            model.addAttribute("signupFailed", true);
            return "signup";
        }

        return "redirect:/login";
    }
}