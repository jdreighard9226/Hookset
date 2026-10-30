package edu.carroll.cs341.hookset.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * @return the name of the index view to render
     */
    @GetMapping("/")
    public String index() {
        log.info("Loading home page");

        return "index";
    }
}