package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.UserService;
import edu.carroll.cs341.hookset.service.input.SignupInput;
import edu.carroll.cs341.hookset.web.form.SignupForm;
import edu.carroll.cs341.hookset.web.mapper.UserMapper;
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
 *
 * <p>The signup form is mapped to {@link SignupInput} before it reaches the
 * user service, so the service never depends on the web layer.</p>
 */
@Controller
@RequestMapping("/signup")
public class SignupController {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(SignupController.class);

    /** The service used to create new user accounts. */
    private final UserService userService;

    /** The mapper used to convert the signup form into service input. */
    private final UserMapper userMapper;

    /**
     * Creates the signup controller with the service and mapper it depends on.
     *
     * @param userService the service used to create new user accounts
     * @param userMapper the mapper used to convert the signup form into service input
     */
    public SignupController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Handles GET requests for the Hookset signup page.
     *
     * <p>A new {@link SignupForm} is added to the model so the signup view can
     * bind form fields to the object.</p>
     *
     * @param model the model used to provide data to the signup view
     * @return the name of the signup view to render
     */
    @GetMapping
    public String getSignup(Model model) {
        // signup is open to everyone in the security config, so there's no user to log here
        log.info("Loading the signup page");

        // empty form for the view to bind to
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
    public String postSignup(@Valid @ModelAttribute SignupForm signupForm,
                             BindingResult result,
                             Model model) {
        // nobody is logged in yet, so the submitted username is the only way to tell signups apart
        log.info("Signup form submitted for username: {}", signupForm.getUsername());

        // the service takes a record, not the web form, so it doesn't depend on where input came from
        SignupInput signupInput = userMapper.toSignupInput(signupForm);

        boolean hasCustomErrors = false;

        // no annotation checks for spaces, so this is the only place that message comes from
        if (!userService.isUsernameSpaceFree(signupInput.username())) {
            log.warn("Signup for username {} failed. Username has spaces", signupInput.username());
            model.addAttribute("usernameHasSpaces", true);
            hasCustomErrors = true;
        }

        // needs the database, so no annotation can check it
        // ignores case, so "JD" is reported as taken if "jd" already exists
        if (userService.isUsernameTaken(signupInput.username())) {
            log.warn("Signup for username {} failed. Username is already taken", signupInput.username());
            model.addAttribute("usernameTaken", true);
            hasCustomErrors = true;
        }

        // the password itself is never logged, only that it had spaces
        if (!userService.isPasswordSpaceFree(signupInput.password())) {
            log.warn("Signup for username {} failed. Password has spaces", signupInput.username());
            model.addAttribute("passwordHasSpaces", true);
            hasCustomErrors = true;
        }

        // annotation errors and custom errors are both checked here, so the user sees every error at once
        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Signup form for username {} failed validation with {} field errors",
                    signupInput.username(), result.getErrorCount());
            return "signup";
        }

        // the service runs every check again and saves the username in lowercase
        if (!userService.addUser(signupInput)) {
            log.warn("Signup for username {} passed form validation but the user service rejected it",
                    signupInput.username());
            model.addAttribute("signupFailed", true);
            return "signup";
        }

        log.info("New account created for username: {}", signupInput.username());

        // signup doesn't log the user in, so they go to the login page next
        // redirecting after a successful POST also stops a refresh from submitting the signup twice
        return "redirect:/login";
    }
}