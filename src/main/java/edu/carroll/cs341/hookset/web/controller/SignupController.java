package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.SignupService;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Handles requests for the Hookset signup page.
 *
 * <p>This controller displays the signup form, validates submitted form
 * data, and creates new user accounts through the signup service. After a
 * successful signup the user is redirected to the login page.</p>
 */
@Controller
public class SignupController {

    /** The service used to create new user accounts. */
    private final SignupService signupService;

    /**
     * Creates the controller with the signup service it depends on.
     *
     * @param signupService the service used to create new users
     */
    public SignupController(SignupService signupService) {
        this.signupService = signupService;
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
    @GetMapping("/signup")
    public String signupGet(Model model) {
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
    @PostMapping("/signup")
    public String signupPost(@Valid @ModelAttribute SignupForm signupForm, BindingResult result) {

        // show the form again if any validation rules failed
        if (result.hasErrors()) {
            return "signup";
        }

        if (signupService.addUser(signupForm)) {
            return "redirect:/login";
        }

        // username is taken, shown as a global error on the form
        result.reject(
                "usernameExists",
                "That username is already taken."
        );

        return "signup";
    }
}