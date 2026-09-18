package edu.carroll.cs341.hookset.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles requests for the Hookset home page.
 *
 * <p>This controller maps requests to the root URL and returns the
 * index view used as the application's home page.</p>
 *
 */
@Controller
public class IndexController {

    /**
     * Handles GET requests to the Hookset home page.
     *
     * @return the name of the index view to render
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}