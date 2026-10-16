package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.CatchService;
import edu.carroll.cs341.hookset.service.FishService;
import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.service.WaterBodyService;
import edu.carroll.cs341.hookset.web.dto.CatchDto;
import edu.carroll.cs341.hookset.web.form.CatchForm;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shared.jpa.entity.Fish;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles requests for the Hookset catch pages.
 *
 * <p>This controller maps requests under {@code /catches} and returns views
 * for browsing every logged catch, viewing the logged-in user's own catches,
 * viewing a single catch, and logging or editing a catch.</p>
 *
 * <p>It also returns the fish dropdown options fragment that htmx swaps into
 * the catch forms when a water body is picked.</p>
 */
@Controller
@RequestMapping("/catches")
public class CatchController {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CatchController.class);

    /** The format the datetime-local input expects for its max attribute. */
    private static final DateTimeFormatter DATE_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /** The service used to look up, save, and validate catches. */
    private final CatchService catchService;

    /** The service used to load the fly dropdown options. */
    private final FlyService flyService;

    /** The service used to load the fish dropdown options. */
    private final FishService fishService;

    /** The service used to load the water body dropdown options. */
    private final WaterBodyService waterBodyService;

    /**
     * Creates the catch controller with the services it depends on.
     *
     * @param catchService the service used to look up, save, and validate catches
     * @param flyService the service used to load the fly dropdown options
     * @param fishService the service used to load the fish dropdown options
     * @param waterBodyService the service used to load the water body dropdown options
     */
    public CatchController(CatchService catchService,
                           FlyService flyService,
                           FishService fishService,
                           WaterBodyService waterBodyService) {
        this.catchService = catchService;
        this.flyService = flyService;
        this.fishService = fishService;
        this.waterBodyService = waterBodyService;
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

    /**
     * Handles GET requests for a single catch's details page.
     *
     * <p>Any logged-in user can view any catch. The edit button only shows
     * for the user who logged it.</p>
     *
     * @param catchId the ID of the catch to show
     * @param model the model used to pass the catch and ownership to the view
     * @return the name of the catch details view, or a redirect to the catch list if the catch does not exist
     */
    @GetMapping("/details/{catchId}")
    public String getCatchDetails(@PathVariable Long catchId, Model model) {
        log.info("Loading catch details page for catch ID: {}", catchId);

        CatchDto catchDto = catchService.getCatchByCatchId(catchId);

        if (catchDto == null) {
            log.warn("Catch details not found for catch ID: {}", catchId);
            return "redirect:/catches";
        }

        model.addAttribute("catchRecord", catchDto);
        model.addAttribute("isOwner", catchService.isCatchOwner(catchId));

        return "catches/details";
    }

    /**
     * Handles GET requests for the add catch page.
     *
     * @param model the model used to pass the form and dropdown options to the view
     * @return the name of the add catch view to render
     */
    @GetMapping("/add")
    public String getAddCatch(Model model) {
        log.info("Loading add catch page");

        CatchForm catchForm = new CatchForm();

        // most catches are logged right after they happen, so start at now
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        catchForm.setDateCaught(now);

        model.addAttribute("catchForm", catchForm);
        addFormOptions(model, catchForm);

        return "catches/add";
    }

    /**
     * Handles POST requests from the add catch form.
     *
     * <p>If the form fails validation or any of the selected fly, fish, or water
     * body are invalid, the add page is shown again with the errors. Otherwise
     * the catch is saved and the user is redirected to their catches.</p>
     *
     * @param catchForm the submitted catch form
     * @param result the validation results for the form
     * @param model the model used to pass errors and dropdown options to the view
     * @return the view to render or the redirect to perform
     */
    @PostMapping("/add")
    public String postAddCatch(
            @Valid @ModelAttribute CatchForm catchForm,
            BindingResult result,
            Model model) {

        log.info("Add catch form submitted");

        boolean hasCustomErrors = addCustomErrors(catchForm, model);

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Add catch form failed validation");
            addFormOptions(model, catchForm);
            return "catches/add";
        }

        if (!catchService.addNewCatch(catchForm)) {
            log.warn("Add catch failed");
            model.addAttribute("addCatchFailed", true);
            addFormOptions(model, catchForm);
            return "catches/add";
        }

        return "redirect:/catches/mine";
    }

    /**
     * Handles GET requests for the edit catch page.
     *
     * <p>Users can only edit their own catches. Anyone else is sent back to
     * their own catch list.</p>
     *
     * @param catchId the ID of the catch to edit
     * @param model the model used to pass the form and dropdown options to the view
     * @return the name of the edit catch view, or a redirect if the catch can't be edited
     */
    @GetMapping("/edit/{catchId}")
    public String getEditCatch(@PathVariable Long catchId, Model model) {
        log.info("Loading edit catch page for catch ID: {}", catchId);

        // returns null for catches that don't exist or belong to someone else
        CatchForm catchForm = catchService.getCatchFormByCatchId(catchId);

        if (catchForm == null) {
            log.warn("Catch ID {} not found or not owned by the current user", catchId);
            return "redirect:/catches/mine";
        }

        model.addAttribute("catchId", catchId);
        model.addAttribute("catchForm", catchForm);
        addFormOptions(model, catchForm);

        return "catches/edit";
    }

    /**
     * Handles POST requests from the edit catch form.
     *
     * <p>Ownership is checked first. If the form fails validation or any of the
     * selected fly, fish, or water body are invalid, the edit page is shown
     * again with the errors. Otherwise the catch is updated and the user is
     * redirected to their catches.</p>
     *
     * @param catchId the ID of the catch being edited
     * @param catchForm the submitted catch form
     * @param result the validation results for the form
     * @param model the model used to pass errors and dropdown options to the view
     * @return the view to render or the redirect to perform
     */
    @PostMapping("/edit/{catchId}")
    public String postEditCatch(
            @PathVariable Long catchId,
            @Valid @ModelAttribute CatchForm catchForm,
            BindingResult result,
            Model model) {

        log.info("Edit catch form submitted for catch ID: {}", catchId);

        // checked before validation so someone can't probe other users' catches through error messages
        if (!catchService.isCatchOwner(catchId)) {
            log.warn("Catch ID {} not found or not owned by the current user", catchId);
            return "redirect:/catches/mine";
        }

        boolean hasCustomErrors = addCustomErrors(catchForm, model);

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Edit catch form failed validation for catch ID: {}", catchId);
            model.addAttribute("catchId", catchId);
            addFormOptions(model, catchForm);
            return "catches/edit";
        }

        if (!catchService.updateCatch(catchId, catchForm)) {
            log.warn("Edit catch failed for catch ID: {}", catchId);
            model.addAttribute("catchId", catchId);
            model.addAttribute("editCatchFailed", true);
            addFormOptions(model, catchForm);
            return "catches/edit";
        }

        return "redirect:/catches/mine";
    }

    /**
     * Handles GET requests for the fish dropdown options on the catch forms.
     *
     * <p>Called by htmx when a water body is picked. Returns only the option
     * tags, not a full page.</p>
     *
     * @param waterBodyId the ID of the picked water body
     * @param model the model used to pass the fish to the fragment
     * @return the fish options fragment
     */
    @GetMapping("/fish-options")
    public String getFishOptions(@RequestParam(required = false) Long waterBodyId, Model model) {
        log.info("Loading fish options for water body ID: {}", waterBodyId);

        List<Fish> fish = new ArrayList<>();

        if (waterBodyId != null) {
            fish = fishService.getAllFishForWaterBody(waterBodyId);
        }

        model.addAttribute("fish", fish);

        // only the fragment is rendered, htmx drops it straight into the fish dropdown
        return "catches/fish-options :: options";
    }

    /**
     * Runs the catch checks the form annotations can't do and adds a flag to
     * the model for each one that fails.
     *
     * <p>Empty fields are skipped because {@code @NotNull} on the form already
     * reports them. Length, date, and notes are left out entirely because the
     * form annotations show messages for them.</p>
     *
     * @param catchForm the submitted catch form
     * @param model the model to add the error flags to
     * @return {@code true} if any check failed, otherwise {@code false}
     */
    private boolean addCustomErrors(CatchForm catchForm, Model model) {
        boolean hasCustomErrors = false;

        // null IDs are already reported by @NotNull on the form, so only check IDs that were sent
        if (catchForm.getFlyId() != null
                && !catchService.isFlyValid(catchForm.getFlyId())) {
            model.addAttribute("flyInvalid", true);
            hasCustomErrors = true;
        }

        if (catchForm.getFishId() != null
                && !catchService.isFishValid(catchForm.getFishId())) {
            model.addAttribute("fishInvalid", true);
            hasCustomErrors = true;
        }

        if (catchForm.getWaterBodyId() != null
                && !catchService.isWaterBodyValid(catchForm.getWaterBodyId())) {
            model.addAttribute("waterBodyInvalid", true);
            hasCustomErrors = true;
        }

        // only worth checking once both were picked and exist, otherwise the user gets two errors for one bad pick
        if (!hasCustomErrors
                && catchForm.getFishId() != null
                && catchForm.getWaterBodyId() != null
                && !catchService.isFishInWaterBody(catchForm.getFishId(), catchForm.getWaterBodyId())) {
            model.addAttribute("fishNotInWaterBody", true);
            hasCustomErrors = true;
        }

        return hasCustomErrors;
    }

    /**
     * Adds the fly, fish, and water body dropdown options and the latest
     * allowed catch date to the model.
     *
     * <p>The fish list only includes fish found in the picked water body, or
     * is empty if no water body is picked yet.</p>
     *
     * @param model the model to add the options to
     * @param catchForm the form whose water body decides which fish to show
     */
    private void addFormOptions(Model model, CatchForm catchForm) {
        model.addAttribute("flies", flyService.getAllFlies());
        model.addAttribute("waterBodies", waterBodyService.getAllWaterBodies());

        List<Fish> fish = new ArrayList<>();

        if (catchForm.getWaterBodyId() != null) {
            fish = fishService.getAllFishForWaterBody(catchForm.getWaterBodyId());
        }

        model.addAttribute("fish", fish);

        // seconds are dropped because the picker only works in minutes
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        model.addAttribute("maxDateCaught", now.format(DATE_INPUT_FORMAT));
    }
}