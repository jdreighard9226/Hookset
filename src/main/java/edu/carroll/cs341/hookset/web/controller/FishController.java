package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.FishService;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.mapper.FishMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import shared.jpa.entity.Fish;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles requests for the fish species pages.
 *
 * <p>This controller maps requests under {@code /fish}, loads fish species
 * from the fish service, and passes them to the view that lists every
 * species in the database.</p>
 */
@Controller
@RequestMapping("/fish")
public class FishController {

    private static final Logger log = LoggerFactory.getLogger(FishController.class);

    /** The service used to look up fish species. */
    private final FishService fishService;

    private final FishMapper fishMapper;

    /**
     * Creates the fish controller with the service it depends on.
     *
     * @param fishService the service used to look up fish species
     */
    public FishController(FishService fishService, FishMapper fishMapper) {
        this.fishService = fishService; this.fishMapper = fishMapper;
    }

    /**
     * Handles GET requests to the fish list page.
     *
     * @param model the model used to pass the fish species to the view
     * @return the name of the fish list view to render
     */
    @GetMapping
    public String index(Model model) {
        log.info("Loading fish page");

        List<Fish> fishEntities = fishService.getAllFish();

        List<FishDto> fishDtos = new ArrayList<>();
        for (Fish fish : fishEntities) {
            fishDtos.add(fishMapper.toDto(fish));
        }
        model.addAttribute("fishes", fishDtos);

        return "fish/index";
    }
}