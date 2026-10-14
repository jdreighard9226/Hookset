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
 * the signup service. After a successful signup the user is redirected to the
 * login page.</p>
 */
@Controller
@RequestMapping("/signup")
public class SignupController {

    private static final Logger log = LoggerFactory.getLogger(SignupController.class);

    /** The service used to create new user accounts. */
    private final UserService userService;

    /**
     * Creates the controller with the signup service it depends on.
     *
     * @param signupService the service used to create new users
     */
    public SignupController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles GET requests for the Hookset signup page.
     *
     * <p>A new SignupForm is added to the model so the signup view can
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
     * <p>If the form fails validation or the username is already taken, the
     * signup page is shown again with the errors. Otherwise the new user is
     * created and redirected to the login page.</p>
     *
     * @param signupForm the submitted signup form
     * @param result the validation results for the form
     * @return the view to render or the redirect to perform
     */
    @PostMapping
    public String postSignup(
            @Valid @ModelAttribute SignupForm signupForm,
            BindingResult result,
            Model model) {

        log.info("Signup form submitted");

        boolean hasCustomErrors = false;

        if (userService.isUsernameBlank(signupForm.getUsername())) {
            model.addAttribute("usernameBlank", true);
            hasCustomErrors = true;
        }

        if (userService.isUsernameTooLong(signupForm.getUsername())) {
            model.addAttribute("usernameTooLong", true);
            hasCustomErrors = true;
        }

        if (userService.isUsernameSpaceFree(signupForm.getUsername()) == false) {
            model.addAttribute("usernameHasSpaces", true);
            hasCustomErrors = true;
        }

        if (userService.isUsernameTaken(signupForm.getUsername())) {
            model.addAttribute("usernameTaken", true);
            hasCustomErrors = true;
        }

        if (userService.isPasswordBlank(signupForm.getPassword())) {
            model.addAttribute("passwordBlank", true);
            hasCustomErrors = true;
        }

        if (userService.isPasswordTooShort(signupForm.getPassword())) {
            model.addAttribute("passwordTooShort", true);
            hasCustomErrors = true;
        }

        if (userService.isPasswordSpaceFree(signupForm.getPassword()) == false) {
            model.addAttribute("passwordHasSpaces", true);
            hasCustomErrors = true;
        }

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Signup form failed validation");
            return "signup";
        }

        if (userService.addUser(signupForm) == false) {
            log.warn("Signup failed");
            model.addAttribute("signupFailed", true);
            return "signup";
        }

        return "redirect:/login";
    }
}