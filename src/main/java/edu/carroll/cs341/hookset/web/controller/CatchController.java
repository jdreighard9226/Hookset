package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.CatchService;
import edu.carroll.cs341.hookset.web.dto.CatchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Handles requests for the Hookset catch pages.
 *
 * <p>This controller maps requests under {@code /catches} and returns views
 * for browsing every logged catch and for viewing the logged-in user's own
 * catches.</p>
 */
@Controller
@RequestMapping("/catches")
public class CatchController {

    private static final Logger log = LoggerFactory.getLogger(CatchController.class);

    /** The service used to look up catches. */
    private final CatchService catchService;

    /**
     * Creates the catch controller with the service it depends on.
     *
     * @param catchService the service used to look up catches
     */
    public CatchController(CatchService catchService) {
        this.catchService = catchService;
    }

    /**
     * Handles GET requests for the page listing every logged catch.
     *
     * @param model the model used to pass the catches to the view
     * @return the name of the catch index view to render
     */
    @GetMapping
    public String index(Model model) {
        log.info("Loading all catches page");

        List<CatchDto> catchDtos = catchService.getAllCatches();
        model.addAttribute("catches", catchDtos);

        return "catches/index";
    }

    /**
     * Handles GET requests for the page listing the logged-in user's catches.
     *
     * @param model the model used to pass the user's catches to the view
     * @return the name of the user catches view to render
     */
    @GetMapping("/mine")
    public String getUserCatches(Model model) {
        log.info("Loading user catches page");

        List<CatchDto> catchDtos = catchService.getAllUserCatches();
        model.addAttribute("catches", catchDtos);

        return "catches/user-catches";
    }
}