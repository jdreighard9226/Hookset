package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles requests for the Hookset home page.
 *
 * <p>This controller maps requests to the root URL and returns the
 * index view used as the application's home page.</p>
 */
@Controller
public class IndexController {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    /**
     * Handles GET requests to the Hookset home page.
     *
     * @param user the logged-in user, or {@code null} for an anonymous visitor, used only for logging
     * @return the name of the index view to render
     */
    @GetMapping("/")
    public String index(@AuthenticationPrincipal HooksetUserDetails user) {
        // the home page is open to everyone, so user is null for anyone who isn't logged in
        if (user != null) {
            log.info("User ID {} loading the home page", user.getId());
        } else {
            log.info("Anonymous visitor loading the home page");
        }

        return "index";
    }
}