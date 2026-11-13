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
        log.info("User ID {} loading the flies page with filter: {}", user.getId(), filter);

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

        log.info("Showing {} flies to user ID {} with filter: {}", flyDtos.size(), user.getId(), filter);

        return "flies/index";
    }

    /**
     * Handles GET requests for a single fly's details page.
     *
     * @param slug the URL slug of the fly to show
     * @param user the logged-in user, whose custom flies can also match the slug
     * @param model the model used to pass the fly to the view
     * @return the name of the fly details view, or a redirect to the fly list if no fly matches the slug
     */
    @GetMapping("/details/{slug}")
    public String getDetails(@PathVariable String slug,
                             @AuthenticationPrincipal HooksetUserDetails user,
                             Model model) {
        log.info("User ID {} loading the details page for fly slug: {}", user.getId(), slug);

        // only finds default flies and this user's custom flies, so another user's custom fly comes back null too
        Fly fly = flyService.getFlyFromSlug(slug, user.getId());

        // a bad or old link lands back on the fly list instead of crashing in the mapper
        if (fly == null) {
            log.warn("User ID {} requested details for fly slug {}, which does not exist or is not visible to them. Redirecting to the fly list",
                    user.getId(), slug);
            return "redirect:/flies";
        }

        FlyDto flyDto = flyMapper.toDto(fly);
        model.addAttribute("fly", flyDto);

        log.info("Showing fly ID {} to user ID {}", flyDto.getId(), user.getId());

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
        log.info("Loading the add fly page");

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
        log.info("User ID {} submitted the add fly form for fly name: {}", user.getId(), flyForm.getName());

        FlyInput flyInput = flyMapper.toInput(flyForm);

        boolean hasCustomErrors = false;

        // only display if both sizes are filled in. Missing sizes will be caught by form
        if (flyInput.minSize() != null && flyInput.maxSize() != null) {
            if (!flyService.isSizeRangeValid(flyInput.minSize(), flyInput.maxSize())) {
                log.warn("User ID {} entered an invalid size range for a new fly: min {}, max {}",
                        user.getId(), flyInput.minSize(), flyInput.maxSize());
                model.addAttribute("invalidSizeRange", true);
                hasCustomErrors = true;
            }
        }

        // only display if the color isn't blank. Blank colors will be caught by form
        if (!flyService.isFlyColorBlank(flyInput.color())) {
            if (!flyService.isFlyColorValidString(flyInput.color())) {
                log.warn("User ID {} entered an invalid color for a new fly: {}", user.getId(), flyInput.color());
                model.addAttribute("invalidFlyColor", true);
                hasCustomErrors = true;
            }
        }

        if (!flyService.isFlyNameUnique(flyInput.name(), user.getId())) {
            // only display to user if they didn't type a blank name. Blank names will be caught by form
            if (!flyService.isFlyNameBlank(flyInput.name())) {
                log.warn("User ID {} tried to add a fly named {}, which matches a fly they can already see",
                        user.getId(), flyInput.name());
                model.addAttribute("duplicateFlyName", true);
                hasCustomErrors = true;
            }
        }

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Add fly form from user ID {} failed validation with {} field errors",
                    user.getId(), result.getErrorCount());
            return "flies/add";
        }

        if (!flyService.addFly(flyInput, user.getId())) {
            log.warn("Add fly passed form validation but the fly service rejected it for user ID {}", user.getId());
            model.addAttribute("addFlyFailed", true);
            return "flies/add";
        }

        log.info("User ID {} added a custom fly named {}", user.getId(), flyInput.name());

        return "redirect:/flies";
    }

    /**
     * Handles GET requests for the edit fly page.
     *
     * <p>Users can only edit their own custom flies. Anyone else, and anyone
     * trying to edit a default fly, is sent back to the fly list.</p>
     *
     * @param id the ID of the fly to edit
     * @param user the logged-in user
     * @param model the model used to pass the filled fly form to the view
     * @return the name of the edit fly view, or a redirect if the fly can't be edited
     */
    @GetMapping("/edit/{id}")
    public String getEditFly(@PathVariable Long id,
                             @AuthenticationPrincipal HooksetUserDetails user,
                             Model model) {
        log.info("User ID {} loading the edit page for fly ID {}", user.getId(), id);

        // false for flies that don't exist, belong to someone else, or are default flies
        if (!flyService.isFlyOwner(id, user.getId())) {
            log.warn("User ID {} tried to open the edit page for fly ID {}, which does not exist or is not theirs. Redirecting to the fly list",
                    user.getId(), id);
            return "redirect:/flies";
        }

        // the ownership check already confirmed this fly exists and belongs to this user
        Fly fly = flyService.getFlyById(id, user.getId());

        FlyForm flyForm = flyMapper.toForm(fly);

        model.addAttribute("flyForm", flyForm);

        return "flies/edit";
    }

    /**
     * Handles POST requests to save changes to a custom fly.
     *
     * <p>Ownership is checked first. The form is then checked against its
     * annotations and the service's business rules. If either finds errors,
     * the edit page is shown again with those errors.</p>
     *
     * @param id the ID of the fly being edited
     * @param flyForm the submitted fly form
     * @param result the binding result holding any validation errors
     * @param user the logged-in user editing the fly
     * @param model the model used to pass error flags to the view
     * @return a redirect to the fly list on success or if the fly can't be edited, or the edit fly view if there are errors
     */
    @PostMapping("/edit/{id}")
    public String postEditFly(@PathVariable Long id,
                              @Valid @ModelAttribute FlyForm flyForm,
                              BindingResult result,
                              @AuthenticationPrincipal HooksetUserDetails user,
                              Model model) {
        log.info("User ID {} submitted the edit form for fly ID {} with fly name: {}",
                user.getId(), id, flyForm.getName());

        // checked before validation so someone can't probe other users' flies through error messages
        if (!flyService.isFlyOwner(id, user.getId())) {
            log.warn("User ID {} tried to edit fly ID {}, which does not exist or is not theirs. Redirecting to the fly list",
                    user.getId(), id);
            return "redirect:/flies";
        }

        FlyInput flyInput = flyMapper.toInput(flyForm);

        boolean hasCustomErrors = false;

        // only display if both sizes are filled in. Missing sizes will be caught by form
        if (flyInput.minSize() != null && flyInput.maxSize() != null) {
            if (!flyService.isSizeRangeValid(flyInput.minSize(), flyInput.maxSize())) {
                log.warn("User ID {} entered an invalid size range for fly ID {}: min {}, max {}",
                        user.getId(), id, flyInput.minSize(), flyInput.maxSize());
                model.addAttribute("invalidSizeRange", true);
                hasCustomErrors = true;
            }
        }

        // only display if the color isn't blank. Blank colors will be caught by form
        if (!flyService.isFlyColorBlank(flyInput.color())) {
            if (!flyService.isFlyColorValidString(flyInput.color())) {
                log.warn("User ID {} entered an invalid color for fly ID {}: {}", user.getId(), id, flyInput.color());
                model.addAttribute("invalidFlyColor", true);
                hasCustomErrors = true;
            }
        }

        if (!flyService.isFlyNameUniqueForEdit(id, flyInput.name(), user.getId())) {
            // only display to user if they didn't type a blank name. Blank names will be caught by form
            if (!flyService.isFlyNameBlank(flyInput.name())) {
                log.warn("User ID {} tried to rename fly ID {} to {}, which matches another fly they can see",
                        user.getId(), id, flyInput.name());
                model.addAttribute("duplicateFlyName", true);
                hasCustomErrors = true;
            }
        }

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Edit form for fly ID {} from user ID {} failed validation with {} field errors",
                    id, user.getId(), result.getErrorCount());
            model.addAttribute("id", id);
            return "flies/edit";
        }

        if (!flyService.updateFly(id, flyInput, user.getId())) {
            log.warn("Edit for fly ID {} passed form validation but the fly service rejected it for user ID {}",
                    id, user.getId());
            model.addAttribute("updateFlyFailed", true);
            model.addAttribute("id", id);
            return "flies/edit";
        }

        log.info("User ID {} updated fly ID {}", user.getId(), id);

        return "redirect:/flies";
    }

    /**
     * Handles POST requests to delete a custom fly.
     *
     * <p>Ownership is checked first. If the fly is used by any catches it isn't
     * deleted, and a message is passed to the fly list to tell the user why.</p>
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
        log.info("User ID {} requested to delete fly ID {}", user.getId(), id);

        // checked first so deleting someone else's fly looks the same as deleting one that doesn't exist
        if (!flyService.isFlyOwner(id, user.getId())) {
            log.warn("User ID {} tried to delete fly ID {}, which does not exist or is not theirs", user.getId(), id);
            redirectAttributes.addFlashAttribute("deleteFlyFailed", true);
            return "redirect:/flies";
        }

        // ownership is confirmed, so the only reason left for this to fail is the fly being used by a catch
        if (!flyService.deleteFly(id, user.getId())) {
            log.warn("User ID {} tried to delete fly ID {}, but it is used by at least one catch", user.getId(), id);
            redirectAttributes.addFlashAttribute("flyInUse", true);
        } else {
            log.info("User ID {} deleted fly ID {}", user.getId(), id);
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
        List<FlyDto> flyDtos = new ArrayList<>();

        for (Fly fly : flies) {
            flyDtos.add(flyMapper.toDto(fly));
        }

        return flyDtos;
    }
}