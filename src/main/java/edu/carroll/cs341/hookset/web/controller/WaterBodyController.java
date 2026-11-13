package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.WaterBodyService;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import edu.carroll.cs341.hookset.web.mapper.FishMapper;
import edu.carroll.cs341.hookset.web.mapper.WaterBodyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import entities.jpa.entity.Fish;
import entities.jpa.entity.WaterBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles requests for the water body pages.
 *
 * <p>This controller maps requests under {@code /waterbodies} and displays
 * the list of every water body and the details page for a single water body.
 * The details page also shows the fish species found in that water body,
 * read straight off the water body's {@code @ManyToMany}.</p>
 */
@Controller
@RequestMapping("/waterbodies")
public class WaterBodyController {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(WaterBodyController.class);

    /** The service used to look up water bodies. */
    private final WaterBodyService waterBodyService;

    /** The mapper used to convert fish entities into DTOs. */
    private final FishMapper fishMapper;

    /** The mapper used to convert water body entities into DTOs. */
    private final WaterBodyMapper waterBodyMapper;

    /**
     * Creates the water body controller with the service and mappers it depends on.
     *
     * @param waterBodyService the service used to look up water bodies
     * @param fishMapper the mapper used to convert fish entities into DTOs
     * @param waterBodyMapper the mapper used to convert water body entities into DTOs
     */
    public WaterBodyController(WaterBodyService waterBodyService,
                               FishMapper fishMapper,
                               WaterBodyMapper waterBodyMapper) {
        this.waterBodyService = waterBodyService;
        this.fishMapper = fishMapper;
        this.waterBodyMapper = waterBodyMapper;
    }

    /**
     * Handles GET requests to the water body list page.
     *
     * @param user the logged-in user, used only to log who loaded the page
     * @param model the model used to pass the water bodies to the view
     * @return the name of the water body list view to render
     */
    @GetMapping
    public String index(@AuthenticationPrincipal HooksetUserDetails user, Model model) {
        log.info("User ID {} loading the water bodies page", user.getId());

        // every water body in the database, since this page lists them all
        List<WaterBody> waterBodyEntities = waterBodyService.getAllWaterBodies();

        // the view only gets DTOs, so entities stay out of the templates and they only see the fields meant for display
        List<WaterBodyDto> waterBodies = new ArrayList<>();

        for (WaterBody waterBody : waterBodyEntities) {
            waterBodies.add(waterBodyMapper.toDto(waterBody));
        }

        // each water body's slug is used by the view to build its link to the details page
        model.addAttribute("waterBodies", waterBodies);

        log.info("Showing {} water bodies to user ID {}", waterBodies.size(), user.getId());

        return "waterbodies/index";
    }

    /**
     * Handles GET requests to a water body's details page.
     *
     * @param slug the slug of the water body taken from the URL
     * @param user the logged-in user, used only to log who loaded the page
     * @param model the model used to pass the water body and its fish species to the view
     * @return the name of the water body details view, or a redirect to the list if no water body matches
     */
    @GetMapping("/details/{slug}")
    public String getDetails(@PathVariable String slug,
                             @AuthenticationPrincipal HooksetUserDetails user,
                             Model model) {
        log.info("User ID {} loading the details page for water body slug: {}", user.getId(), slug);

        // the service lowercases the slug, so a URL typed in the wrong case still finds the water body
        WaterBody waterBodyEntity = waterBodyService.getWaterBodyFromSlug(slug);

        // a mistyped or old URL would otherwise crash the mapper on null
        if (waterBodyEntity == null) {
            log.warn("User ID {} requested details for water body slug {}, which does not exist. Redirecting to the water bodies page",
                    user.getId(), slug);
            return "redirect:/waterbodies";
        }

        WaterBodyDto waterBody = waterBodyMapper.toDto(waterBodyEntity);

        // fish come from the FishWaterBody join table through the water body's @ManyToMany
        Set<Fish> fishEntities = waterBodyEntity.getFishes();

        List<FishDto> fishDtos = new ArrayList<>();

        for (Fish fish : fishEntities) {
            fishDtos.add(fishMapper.toDto(fish));
        }

        model.addAttribute("waterBody", waterBody);
        model.addAttribute("fishes", fishDtos);

        log.info("Showing water body ID {} with {} fish species to user ID {}",
                waterBodyEntity.getId(), fishDtos.size(), user.getId());

        return "waterbodies/details";
    }
}