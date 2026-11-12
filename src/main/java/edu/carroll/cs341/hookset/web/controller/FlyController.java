package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.service.input.FlyInput;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import edu.carroll.cs341.hookset.web.mapper.FlyMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import entities.jpa.entity.Fly;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles requests for the Hookset fly pages.
 *
 * <p>This controller maps requests under {@code /flies} and returns views for
 * browsing flies, viewing a single fly, and adding, editing, or deleting a
 * custom fly. The fly list can be filtered to default flies, the user's custom
 * flies, or both.</p>
 *
 * <p>The logged-in user is passed into each handler by Spring Security and
 * handed to the fly service as an ID, so the service never reads the login
 * itself. Form input is mapped to {@link FlyInput} before it reaches the
 * service.</p>
 *
 * <p>Sources Used:</p>
 * <ul>
 *     <li><a href="https://www.baeldung.com/spring-request-param">Baeldung: Spring @RequestParam Annotation</a>, section 5, default values for request parameters</li>
 * </ul>
 */
@Controller
@RequestMapping("/flies")
public class FlyController {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(FlyController.class);

    /** The service used to look up, create, edit, and delete flies. */
    private final FlyService flyService;

    /** The mapper used to convert flies to DTOs, forms, and service input. */
    private final FlyMapper flyMapper;

    /**
     * Creates the fly controller with the service and mapper it depends on.
     *
     * @param flyService the service used to look up, create, edit, and delete flies
     * @param flyMapper the mapper used to convert flies to DTOs, forms, and service input
     */
    public FlyController(FlyService flyService, FlyMapper flyMapper) {
        this.flyService = flyService;
        this.flyMapper = flyMapper;
    }

    /**
     * Handles GET requests for the fly list page.
     *
     * <p>The filter can be "all" for default and custom flies, "mine" for the
     * user's custom flies, or "hookset" for default flies. Any other value
     * falls back to "all".</p>
     *
     * @param filter which set of flies to show, defaulting to "all"
     * @param user the logged-in user
     * @param model the model used to pass the flies and active filter to the view
     * @return the name of the fly index view to render
     */
    @GetMapping
    public String index(@RequestParam(defaultValue = "all") String filter,
                        @AuthenticationPrincipal HooksetUserDetails user,
                        Model model) {
        log.info("Loading flies page with filter: {}", filter);

        List<Fly> flies;

        if (filter.equals("mine")) {
            flies = flyService.getAllUserFlies(user.getId());
        } else if (filter.equals("hookset")) {
            flies = flyService.getAllHooksetFlies();
        } else {
            flies = flyService.getAllFlies(user.getId());
            filter = "all";
        }

        List<FlyDto> flyDtos = mapAllFlyDtos(flies);

        model.addAttribute("flies", flyDtos);
        model.addAttribute("filter", filter);

        return "flies/index";
    }

    /**
     * Handles GET requests for a single fly's details page.
     *
     * @param slug the URL slug of the fly to show
     * @param user the logged-in user, whose custom flies can also match the slug
     * @param model the model used to pass the fly to the view
     * @return the name of the fly details view to render
     */
    @GetMapping("/details/{slug}")
    public String getDetails(@PathVariable String slug,
                             @AuthenticationPrincipal HooksetUserDetails user,
                             Model model) {
        log.info("Loading fly details page for slug: {}", slug);

        Fly fly = flyService.getFlyFromSlug(slug, user.getId());
        FlyDto flyDto = flyMapper.toDto(fly);
        model.addAttribute("fly", flyDto);

        return "flies/details";
    }

    /**
     * Handles GET requests for the add fly page.
     *
     * @param model the model used to pass an empty fly form to the view
     * @return the name of the add fly view to render
     */
    @GetMapping("/add")
    public String getAddFly(Model model) {
        log.info("Loading add fly page");

        model.addAttribute("flyForm", new FlyForm());

        return "flies/add";
    }

    /**
     * Handles POST requests to save a new custom fly.
     *
     * <p>The form is checked against its annotations and the service's
     * business rules. If either finds errors, the add page is shown again
     * with those errors.</p>
     *
     * @param flyForm the submitted fly form
     * @param result the binding result holding any validation errors
     * @param user the logged-in user adding the fly
     * @param model the model used to pass error flags to the view
     * @return a redirect to the fly list on success, or the add fly view if there are errors
     */
    @PostMapping("/add")
    public String postAddFly(@Valid @ModelAttribute FlyForm flyForm,
                             BindingResult result,
                             @AuthenticationPrincipal HooksetUserDetails user,
                             Model model) {
        log.info("Add fly form submitted");

        boolean hasCustomErrors = false;

        FlyInput flyInput = flyMapper.toInput(flyForm);

        // only display if both sizes are filled in. Missing sizes will be caught by form
        if (flyInput.minSize() != null && flyInput.maxSize() != null) {
            if (!flyService.isSizeRangeValid(flyInput.minSize(), flyInput.maxSize())) {
                model.addAttribute("invalidSizeRange", true);
                hasCustomErrors = true;
            }
        }

        // only display if the color isn't blank. Blank colors will be caught by form
        if (!flyService.isFlyColorBlank(flyInput.color())) {
            if (!flyService.isFlyColorValidString(flyInput.color())) {
                model.addAttribute("invalidFlyColor", true);
                hasCustomErrors = true;
            }
        }

        if (!flyService.isFlyNameUnique(flyInput.name(), user.getId())) {
            // only display to user if they didn't type a blank name. Blank names will be caught by form
            if (!flyService.isFlyNameBlank(flyInput.name())) {
                model.addAttribute("duplicateFlyName", true);
                hasCustomErrors = true;
            }
        }

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Add fly form failed validation");
            return "flies/add";
        }

        if (!flyService.addFly(flyInput, user.getId())) {
            log.warn("Add fly failed");
            model.addAttribute("addFlyFailed", true);
            return "flies/add";
        }

        return "redirect:/flies";
    }

    /**
     * Handles GET requests for the edit fly page.
     *
     * @param id the ID of the fly to edit
     * @param user the logged-in user
     * @param model the model used to pass the filled fly form to the view
     * @return the name of the edit fly view to render
     */
    @GetMapping("/edit/{id}")
    public String getEditFly(@PathVariable Long id,
                             @AuthenticationPrincipal HooksetUserDetails user,
                             Model model) {
        log.info("Loading edit fly page for fly ID: {}", id);

        Fly fly = flyService.getFlyById(id, user.getId());

        FlyForm flyForm = flyMapper.toForm(fly);

        model.addAttribute("flyForm", flyForm);

        return "flies/edit";
    }

    /**
     * Handles POST requests to save changes to a custom fly.
     *
     * <p>The form is checked against its annotations and the service's
     * business rules. If either finds errors, the edit page is shown again
     * with those errors.</p>
     *
     * @param id the ID of the fly being edited
     * @param flyForm the submitted fly form
     * @param result the binding result holding any validation errors
     * @param user the logged-in user editing the fly
     * @param model the model used to pass error flags to the view
     * @return a redirect to the fly list on success, or the edit fly view if there are errors
     */
    @PostMapping("/edit/{id}")
    public String postEditFly(@PathVariable Long id,
                              @Valid @ModelAttribute FlyForm flyForm,
                              BindingResult result,
                              @AuthenticationPrincipal HooksetUserDetails user,
                              Model model) {
        log.info("Edit fly form submitted for fly ID: {}", id);

        boolean hasCustomErrors = false;

        FlyInput flyInput = flyMapper.toInput(flyForm);

        // only display if both sizes are filled in. Missing sizes will be caught by form
        if (flyInput.minSize() != null && flyInput.maxSize() != null) {
            if (!flyService.isSizeRangeValid(flyInput.minSize(), flyInput.maxSize())) {
                model.addAttribute("invalidSizeRange", true);
                hasCustomErrors = true;
            }
        }

        // only display if the color isn't blank. Blank colors will be caught by form
        if (!flyService.isFlyColorBlank(flyInput.color())) {
            if (!flyService.isFlyColorValidString(flyInput.color())) {
                model.addAttribute("invalidFlyColor", true);
                hasCustomErrors = true;
            }
        }

        if (!flyService.isFlyNameUniqueForEdit(id, flyInput.name(), user.getId())) {
            // only display to user if they didn't type a blank name. Blank names will be caught by form
            if (!flyService.isFlyNameBlank(flyInput.name())) {
                model.addAttribute("duplicateFlyName", true);
                hasCustomErrors = true;
            }
        }

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Edit fly form failed validation for fly ID: {}", id);
            model.addAttribute("id", id);
            return "flies/edit";
        }

        if (!flyService.updateFly(id, flyInput, user.getId())) {
            log.warn("Edit fly failed for fly ID: {}", id);
            model.addAttribute("updateFlyFailed", true);
            model.addAttribute("id", id);
            return "flies/edit";
        }

        return "redirect:/flies";
    }

    /**
     * Handles POST requests to delete a custom fly.
     *
     * <p>If the fly is used by any catches it isn't deleted, and a message is
     * passed to the fly list to tell the user why.</p>
     *
     * @param id the ID of the fly to delete
     * @param user the logged-in user deleting the fly
     * @param redirectAttributes used to pass a one-time message to the fly list after the redirect
     * @return a redirect to the fly list
     */
    @PostMapping("/delete/{id}")
    public String postDeleteFly(@PathVariable Long id,
                                @AuthenticationPrincipal HooksetUserDetails user,
                                RedirectAttributes redirectAttributes) {
        log.info("Delete fly request for fly ID: {}", id);

        if (!flyService.deleteFly(id, user.getId())) {
            if (flyService.isFlyUsedByCatch(id)) {
                log.warn("Delete fly blocked. Fly ID: {} is used by catches", id);
                redirectAttributes.addFlashAttribute("flyInUse", true);
            } else {
                log.warn("Delete fly failed for fly ID: {}", id);
                redirectAttributes.addFlashAttribute("deleteFlyFailed", true);
            }
        }

        return "redirect:/flies";
    }

    /**
     * Converts a list of fly entities into fly DTOs.
     *
     * @param flies the fly entities to convert
     * @return the list of fly DTOs
     */
    private List<FlyDto> mapAllFlyDtos(List<Fly> flies) {
        if (flies.isEmpty()) {
            log.info("No flies found");
        } else {
            log.info("Found {} flies", flies.size());
        }

        List<FlyDto> flyDtos = new ArrayList<>();

        for (Fly fly : flies) {
            flyDtos.add(flyMapper.toDto(fly));
        }

        return flyDtos;
    }
}