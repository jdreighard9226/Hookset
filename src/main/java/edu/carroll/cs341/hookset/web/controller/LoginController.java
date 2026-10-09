package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.web.form.LoginForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles requests for the Hookset login page.
 *
 * <p>This controller maps requests under {@code /login}, displays the login
 * page, and adds a new LoginForm object to the model for use by the login
 * form.</p>
 *
 * <p>There is no POST handler here. Spring Security's login filter handles
 * the form submission and authenticates the user.</p>
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    /**
     * Handles GET requests for the Hookset login page.
     *
     * <p>A new LoginForm is added to the model so the login view can
     * bind form fields to the object.</p>
     *
     * @param model the model used to provide data to the login view
     * @return the name of the login view to render
     */
    @GetMapping
    public String getLogin(Model model) {
        log.info("Loading login page");

        model.addAttribute("loginForm", new LoginForm());

        return "login";
    }
}