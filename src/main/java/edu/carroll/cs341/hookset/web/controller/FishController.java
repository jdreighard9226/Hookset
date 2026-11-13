package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.FishService;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.mapper.FishMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import entities.jpa.entity.Fish;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles requests for the fish species pages.
 *
 * <p>This controller maps requests under {@code /fish}, loads fish species
 * from the fish service, maps them to DTOs, and passes them to the view that
 * lists every species in the database.</p>
 */
@Controller
@RequestMapping("/fish")
public class FishController {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(FishController.class);

    /** The service used to look up fish species. */
    private final FishService fishService;

    /** The mapper used to convert fish entities into DTOs for the view. */
    private final FishMapper fishMapper;

    /**
     * Creates the fish controller with the service and mapper it depends on.
     *
     * @param fishService the service used to look up fish species
     * @param fishMapper the mapper used to convert fish entities into DTOs for the view
     */
    public FishController(FishService fishService, FishMapper fishMapper) {
        this.fishService = fishService;
        this.fishMapper = fishMapper;
    }

    /**
     * Handles GET requests to the fish list page.
     *
     * @param user the logged-in user, or {@code null} for an anonymous visitor, used only for logging
     * @param model the model used to pass the fish species to the view
     * @return the name of the fish list view to render
     */
    @GetMapping
    public String index(@AuthenticationPrincipal HooksetUserDetails user, Model model) {
        // user is null for anonymous visitors, so calling getId() without this check would crash
        if (user != null) {
            log.info("User ID {} loading the fish species page", user.getId());
        } else {
            log.info("Anonymous visitor loading the fish species page");
        }

        // every species in the database, since this page lists them all
        List<Fish> fishEntities = fishService.getAllFish();

        // the view only gets DTOs, so entities stay out of the templates and they only see the fields meant for display
        List<FishDto> fishDtos = new ArrayList<>();

        for (Fish fish : fishEntities) {
            fishDtos.add(fishMapper.toDto(fish));
        }

        model.addAttribute("fishes", fishDtos);

        if (user != null) {
            log.info("Showing {} fish species to user ID {}", fishDtos.size(), user.getId());
        } else {
            log.info("Showing {} fish species to an anonymous visitor", fishDtos.size());
        }

        return "fish/index";
    }
}