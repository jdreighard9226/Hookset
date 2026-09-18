package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.web.form.LoginForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles requests for the Hookset login page.
 *
 * <p>This controller displays the login page and adds a new LoginForm
 * object to the model for use by the login form.</p>
 *
 */
@Controller
public class LoginController {

    /**
     * Handles GET requests for the Hookset login page.
     *
     * <p>A new LoginForm is added to the model so the login view can
     * bind form fields to the object.</p>
     *
     * @param model the model used to provide data to the login view
     * @return the name of the login view to render
     */
    @GetMapping("/login")
    public String loginGet(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }
}