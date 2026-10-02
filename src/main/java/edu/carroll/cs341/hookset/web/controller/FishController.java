package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.FishService;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Handles requests for the fish species pages.
 *
 * <p>This controller loads fish species from the fish service and passes
 * them to the view that lists every species in the database.</p>
 */
@Controller
public class FishController {

    private static final Logger log = LoggerFactory.getLogger(FishController.class);

    /** The service used to look up fish species. */
    private final FishService fishService;

    /**
     * Creates the fish controller with the service it depends on.
     *
     * @param fishService the service used to look up fish species
     */
    public FishController(FishService fishService) {
        this.fishService = fishService;
    }

    /**
     * Handles GET requests to the fish list page.
     *
     * @param model the model used to pass the fish species to the view
     * @return the name of the fish list view to render
     */
    @GetMapping("/fish")
    public String getFish(Model model) {
        log.info("Request received for fish list page");

        List<FishDto> fishDtos = fishService.getAllFish();
        model.addAttribute("fishes", fishDtos);

        return "/fish/index";
    }
}