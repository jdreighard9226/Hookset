package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles requests for the Hookset fly pages.
 *
 * <p>This controller maps requests under {@code /flies} and returns views for
 * browsing flies, viewing a single fly, adding a custom fly, and deleting a
 * custom fly. The fly list can be filtered to default flies, the user's custom
 * flies, or both.</p>
 *
 * <p>Sources Used:</p>
 * <ul>
 *     <li><a href="https://www.baeldung.com/spring-request-param">Baeldung: Spring @RequestParam Annotation</a>, section 5, default values for request parameters</li>
 * </ul>
 */
@Controller
@RequestMapping("/flies")
public class FlyController {

    private static final Logger log = LoggerFactory.getLogger(FlyController.class);

    /** The service used to look up, create, and delete flies. */
    private final FlyService flyService;

    /**
     * Creates the fly controller with the service it depends on.
     *
     * @param flyService the service used to look up, create, and delete flies
     */
    public FlyController(FlyService flyService) {
        this.flyService = flyService;
    }

    /**
     * Handles GET requests for the fly list page.
     *
     * <p>The filter can be "all" for default and custom flies, "mine" for the
     * user's custom flies, or "hookset" for default flies. Any other value
     * falls back to "hookset".</p>
     *
     * @param filter which set of flies to show, defaulting to "hookset"
     * @param model the model used to pass the flies and active filter to the view
     * @return the name of the fly index view to render
     */
    @GetMapping
    public String index(@RequestParam(defaultValue = "hookset") String filter, Model model) {
        log.info("Loading flies page with filter: {}", filter);

        List<FlyDto> flyDtos;

        if (filter.equals("all")) {
            flyDtos = flyService.getAllFlies();
        } else if (filter.equals("mine")) {
            flyDtos = flyService.getAllUserFlies();
        } else {
            flyDtos = flyService.getAllHooksetFlies();
            filter = "hookset";
        }

        model.addAttribute("flies", flyDtos);
        model.addAttribute("filter", filter);

        return "flies/index";
    }

    /**
     * Handles GET requests for a single fly's details page.
     *
     * @param slug the URL slug of the fly to show
     * @param model the model used to pass the fly to the view
     * @return the name of the fly details view to render
     */
    @GetMapping("/details/{slug}")
    public String getDetails(@PathVariable String slug, Model model) {
        log.info("Loading fly details page for slug: {}", slug);

        FlyDto flyDto = flyService.getFlyFromSlug(slug);
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
     * @return a redirect to the fly list on success, or the add fly view if there are errors
     */
    @PostMapping("/add")
    public String postAddFly(@Valid @ModelAttribute("flyForm") FlyForm flyForm,
                             BindingResult result) {
        log.info("Add fly form submitted");

        // business rules run after annotation validation so both sets of errors show together
        flyService.validateFly(flyForm, result);

        if (result.hasErrors()) {
            log.warn("Add fly failed with {} validation errors", result.getErrorCount());
            return "flies/add";
        }

        flyService.addFly(flyForm);

        return "redirect:/flies";
    }

    /**
     * Handles POST requests to delete a custom fly.
     *
     * @param flyId the ID of the fly to delete
     * @return a redirect to the fly list
     */
    @PostMapping("/delete/{flyId}")
    public String postDeleteFly(@PathVariable Long flyId) {
        log.info("Delete fly request for fly ID: {}", flyId);

        flyService.deleteFly(flyId);

        return "redirect:/flies";
    }
}