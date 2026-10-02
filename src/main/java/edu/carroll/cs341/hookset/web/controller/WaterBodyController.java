package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.FishService;
import edu.carroll.cs341.hookset.service.WaterBodyService;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Handles requests for the water body pages.
 *
 * <p>This controller displays the list of every water body and the details
 * page for a single water body. The details page also shows the fish
 * species found in that water body.</p>
 */
@Controller
public class WaterBodyController {

    private static final Logger log = LoggerFactory.getLogger(WaterBodyController.class);

    /** The service used to look up water bodies. */
    private final WaterBodyService waterBodyService;

    /** The service used to look up the fish species in a water body. */
    private final FishService fishService;

    /**
     * Creates the water body controller with the services it depends on.
     *
     * @param waterBodyService the service used to look up water bodies
     * @param fishService the service used to look up the fish species in a water body
     */
    public WaterBodyController(WaterBodyService waterBodyService, FishService fishService) {
        this.waterBodyService = waterBodyService;
        this.fishService = fishService;
    }

    /**
     * Handles GET requests to the water body list page.
     *
     * @param model the model used to pass the water bodies to the view
     * @return the name of the water body list view to render
     */
    @GetMapping("/waterbodies")
    public String getWaterBodies(Model model) {
        log.info("Request received for water body list page");

        List<WaterBodyDto> waterBodies = waterBodyService.getAllWaterBodies();

        model.addAttribute("waterBodies", waterBodies);

        return "waterbodies/index";
    }

    /**
     * Handles GET requests to a water body's details page.
     *
     * @param slug the slug of the water body taken from the URL
     * @param model the model used to pass the water body and its fish species to the view
     * @return the name of the water body details view to render
     */
    @GetMapping("/waterbodies/details/{slug}")
    public String getDetails(@PathVariable String slug, Model model) {
        log.info("Request received for water body details page, slug: {}", slug);

        WaterBodyDto waterBody = waterBodyService.getWaterBodyFromSlug(slug);

        List<FishDto> fishDtos = fishService.getAllFishForWaterBody(waterBody.getWaterBodyId());
        model.addAttribute("waterBody", waterBody);
        model.addAttribute("fishes", fishDtos);

        return "waterbodies/details";
    }
}