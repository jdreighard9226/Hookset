package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.CatchService;
import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.service.WaterBodyService;
import edu.carroll.cs341.hookset.service.input.CatchInput;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.dto.CatchDto;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import edu.carroll.cs341.hookset.web.form.CatchForm;
import edu.carroll.cs341.hookset.web.mapper.CatchMapper;
import edu.carroll.cs341.hookset.web.mapper.FishMapper;
import edu.carroll.cs341.hookset.web.mapper.FlyMapper;
import edu.carroll.cs341.hookset.web.mapper.WaterBodyMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shared.jpa.entity.Catch;
import shared.jpa.entity.Fish;
import shared.jpa.entity.Fly;
import shared.jpa.entity.WaterBody;

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
 * <p>The logged-in user is passed into each handler by Spring Security and
 * handed to the services as an ID, so the services never read the login
 * themselves. Form input is mapped to {@link CatchInput} before it reaches
 * the catch service.</p>
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

    /** The service used to load the water body and fish dropdown options. */
    private final WaterBodyService waterBodyService;

    /** The mapper used to convert catches to DTOs, forms, and service input. */
    private final CatchMapper catchMapper;

    /** The mapper used to convert fish for the fish dropdown. */
    private final FishMapper fishMapper;

    /** The mapper used to convert water bodies for the water body dropdown. */
    private final WaterBodyMapper waterBodyMapper;

    /** The mapper used to convert flies for the fly dropdown. */
    private final FlyMapper flyMapper;

    /**
     * Creates the catch controller with the services and mappers it depends on.
     *
     * @param catchService the service used to look up, save, and validate catches
     * @param flyService the service used to load the fly dropdown options
     * @param waterBodyService the service used to load the water body and fish dropdown options
     * @param catchMapper the mapper used to convert catches to DTOs, forms, and service input
     * @param fishMapper the mapper used to convert fish for the fish dropdown
     * @param waterBodyMapper the mapper used to convert water bodies for the water body dropdown
     * @param flyMapper the mapper used to convert flies for the fly dropdown
     */
    public CatchController(CatchService catchService,
                           FlyService flyService,
                           WaterBodyService waterBodyService,
                           CatchMapper catchMapper,
                           FishMapper fishMapper,
                           WaterBodyMapper waterBodyMapper,
                           FlyMapper flyMapper) {
        this.catchService = catchService;
        this.flyService = flyService;
        this.waterBodyService = waterBodyService;
        this.catchMapper = catchMapper;
        this.fishMapper = fishMapper;
        this.waterBodyMapper = waterBodyMapper;
        this.flyMapper = flyMapper;
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

        List<Catch> catches = catchService.getAllCatches();
        List<CatchDto> catchDtos = mapAllCatchDtos(catches);
        model.addAttribute("catches", catchDtos);

        return "catches/index";
    }

    /**
     * Handles GET requests for the page listing the logged-in user's catches.
     *
     * @param user the logged-in user
     * @param model the model used to pass the user's catches to the view
     * @return the name of the user catches view to render
     */
    @GetMapping("/mine")
    public String getUserCatches(@AuthenticationPrincipal HooksetUserDetails user, Model model) {
        log.info("Loading user catches page");

        List<Catch> catches = catchService.getAllUserCatches(user.getId());
        List<CatchDto> catchDtos = mapAllCatchDtos(catches);
        model.addAttribute("catches", catchDtos);

        return "catches/user-catches";
    }

    /**
     * Handles GET requests for a single catch's details page.
     *
     * <p>Any logged-in user can view any catch. The edit button only shows
     * for the user who logged it.</p>
     *
     * @param user the logged-in user
     * @param catchId the ID of the catch to show
     * @param model the model used to pass the catch and ownership to the view
     * @return the name of the catch details view, or a redirect to the catch list if the catch does not exist
     */
    @GetMapping("/details/{catchId}")
    public String getCatchDetails(@AuthenticationPrincipal HooksetUserDetails user,
                                  @PathVariable Long catchId,
                                  Model model) {
        log.info("Loading catch details page for catch ID: {}", catchId);

        Catch catchRecord = catchService.getCatchById(catchId);

        if (catchRecord == null) {
            log.warn("Catch details not found for catch ID: {}", catchId);
            return "redirect:/catches";
        }

        CatchDto catchDto = catchMapper.toDto(catchRecord);

        model.addAttribute("catchRecord", catchDto);
        model.addAttribute("isOwner", catchService.isCatchOwner(catchId, user.getId()));

        return "catches/details";
    }

    /**
     * Handles GET requests for the add catch page.
     *
     * @param model the model used to pass the form and dropdown options to the view
     * @param user the logged-in user, used to load their custom flies into the dropdown
     * @return the name of the add catch view to render
     */
    @GetMapping("/add")
    public String getAddCatch(Model model, @AuthenticationPrincipal HooksetUserDetails user) {
        log.info("Loading add catch page");

        CatchForm catchForm = new CatchForm();

        // most catches are logged right after they happen, so start at now
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        catchForm.setDateCaught(now);

        model.addAttribute("catchForm", catchForm);
        addFormOptions(model, catchForm, user.getId());

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
     * @param user the logged-in user saving the catch
     * @param model the model used to pass errors and dropdown options to the view
     * @return the view to render or the redirect to perform
     */
    @PostMapping("/add")
    public String postAddCatch(@Valid @ModelAttribute CatchForm catchForm,
                               BindingResult result,
                               @AuthenticationPrincipal HooksetUserDetails user,
                               Model model) {
        log.info("Add catch form submitted");

        CatchInput catchInput = catchMapper.toInput(catchForm);
        boolean hasCustomErrors = addCustomErrors(catchInput, model, user.getId());

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Add catch form failed validation");
            addFormOptions(model, catchForm, user.getId());
            return "catches/add";
        }

        if (!catchService.addCatch(catchInput, user.getId())) {
            log.warn("Add catch failed");
            model.addAttribute("addCatchFailed", true);
            addFormOptions(model, catchForm, user.getId());
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
     * @param user the logged-in user
     * @param model the model used to pass the form and dropdown options to the view
     * @return the name of the edit catch view, or a redirect if the catch can't be edited
     */
    @GetMapping("/edit/{catchId}")
    public String getEditCatch(@PathVariable Long catchId,
                               @AuthenticationPrincipal HooksetUserDetails user,
                               Model model) {
        log.info("Loading edit catch page for catch ID: {}", catchId);

        // false for catches that don't exist or belong to someone else
        if (!catchService.isCatchOwner(catchId, user.getId())) {
            log.warn("Catch ID {} not found or not owned by the current user", catchId);
            return "redirect:/catches/mine";
        }

        // the ownership check already confirmed this catch exists
        Catch catchRecord = catchService.getCatchById(catchId);

        CatchForm catchForm = catchMapper.toForm(catchRecord);

        model.addAttribute("catchId", catchId);
        model.addAttribute("catchForm", catchForm);
        addFormOptions(model, catchForm, user.getId());

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
     * @param user the logged-in user editing the catch
     * @param model the model used to pass errors and dropdown options to the view
     * @return the view to render or the redirect to perform
     */
    @PostMapping("/edit/{catchId}")
    public String postEditCatch(@PathVariable Long catchId,
                                @Valid @ModelAttribute CatchForm catchForm,
                                BindingResult result,
                                @AuthenticationPrincipal HooksetUserDetails user,
                                Model model) {
        log.info("Edit catch form submitted for catch ID: {}", catchId);

        // checked before validation so someone can't probe other users' catches through error messages
        if (!catchService.isCatchOwner(catchId, user.getId())) {
            log.warn("Catch ID {} not found or not owned by the current user", catchId);
            return "redirect:/catches/mine";
        }

        CatchInput catchInput = catchMapper.toInput(catchForm);
        boolean hasCustomErrors = addCustomErrors(catchInput, model, user.getId());

        if (result.hasErrors() || hasCustomErrors) {
            log.warn("Edit catch form failed validation for catch ID: {}", catchId);
            model.addAttribute("catchId", catchId);
            addFormOptions(model, catchForm, user.getId());
            return "catches/edit";
        }

        if (!catchService.updateCatch(catchId, catchInput, user.getId())) {
            log.warn("Edit catch failed for catch ID: {}", catchId);
            model.addAttribute("catchId", catchId);
            model.addAttribute("editCatchFailed", true);
            addFormOptions(model, catchForm, user.getId());
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
            fish = waterBodyService.getAllFishForWaterBody(waterBodyId);
        }

        List<FishDto> fishDtos = new ArrayList<>();

        for (Fish fishEntity : fish) {
            fishDtos.add(fishMapper.toDto(fishEntity));
        }

        model.addAttribute("fish", fishDtos);

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
     * @param catchInput the submitted catch details
     * @param model the model to add the error flags to
     * @param userId the ID of the logged-in user, used to check fly ownership
     * @return {@code true} if any check failed, otherwise {@code false}
     */
    private boolean addCustomErrors(CatchInput catchInput, Model model, Long userId) {
        boolean hasCustomErrors = false;

        // null IDs are already reported by @NotNull on the form, so only check IDs that were sent
        if (catchInput.flyId() != null
                && !catchService.isFlyValid(catchInput.flyId(), userId)) {
            model.addAttribute("flyInvalid", true);
            hasCustomErrors = true;
        }

        if (catchInput.fishId() != null
                && !catchService.isFishValid(catchInput.fishId())) {
            model.addAttribute("fishInvalid", true);
            hasCustomErrors = true;
        }

        if (catchInput.waterBodyId() != null
                && !catchService.isWaterBodyValid(catchInput.waterBodyId())) {
            model.addAttribute("waterBodyInvalid", true);
            hasCustomErrors = true;
        }

        // only worth checking once both were picked and exist, otherwise the user gets two errors for one bad pick
        if (!hasCustomErrors
                && catchInput.fishId() != null
                && catchInput.waterBodyId() != null
                && !catchService.isFishInWaterBody(catchInput.fishId(), catchInput.waterBodyId())) {
            model.addAttribute("fishNotInWaterBody", true);
            hasCustomErrors = true;
        }

        return hasCustomErrors;
    }

    /**
     * Adds the fly, fish, and water body dropdown options and the latest
     * allowed catch date to the model.
     *
     * <p>The fly list includes the default flies plus the user's custom flies.
     * The fish list only includes fish found in the picked water body, or is
     * empty if no water body is picked yet.</p>
     *
     * @param model the model to add the options to
     * @param catchForm the form whose water body decides which fish to show
     * @param userId the ID of the logged-in user, used to include their custom flies
     */
    private void addFormOptions(Model model, CatchForm catchForm, Long userId) {
        List<Fly> flies = flyService.getAllFlies(userId);
        List<FlyDto> flyDtos = new ArrayList<>();

        for (Fly flyEntity : flies) {
            flyDtos.add(flyMapper.toDto(flyEntity));
        }

        List<WaterBody> waterBodies = waterBodyService.getAllWaterBodies();
        List<WaterBodyDto> waterBodyDtos = new ArrayList<>();

        for (WaterBody waterBodyEntity : waterBodies) {
            waterBodyDtos.add(waterBodyMapper.toDto(waterBodyEntity));
        }

        List<Fish> fish = new ArrayList<>();

        if (catchForm.getWaterBodyId() != null) {
            fish = waterBodyService.getAllFishForWaterBody(catchForm.getWaterBodyId());
        }

        List<FishDto> fishDtos = new ArrayList<>();

        for (Fish fishEntity : fish) {
            fishDtos.add(fishMapper.toDto(fishEntity));
        }

        model.addAttribute("flies", flyDtos);
        model.addAttribute("waterBodies", waterBodyDtos);
        model.addAttribute("fish", fishDtos);

        // seconds are dropped because the picker only works in minutes
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        model.addAttribute("maxDateCaught", now.format(DATE_INPUT_FORMAT));
    }

    /**
     * Converts a list of catch entities into catch DTOs.
     *
     * @param catches the catch entities to convert
     * @return the list of catch DTOs
     */
    private List<CatchDto> mapAllCatchDtos(List<Catch> catches) {
        if (catches.isEmpty()) {
            log.info("No catches found");
        } else {
            log.info("Found {} catches", catches.size());
        }

        List<CatchDto> catchDtos = new ArrayList<>();

        for (Catch catchEntity : catches) {
            catchDtos.add(catchMapper.toDto(catchEntity));
        }

        return catchDtos;
    }
}