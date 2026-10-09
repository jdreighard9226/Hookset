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
 *
 * Sources used:
 * https://www.baeldung.com/spring-request-param
 * Specifically section 5: Default value for request parameters.
 */
@Controller
public class FlyController {

    private final FlyService flyService;
    private static final Logger log = LoggerFactory.getLogger(FlyController.class);
    public FlyController(FlyService flyService) {
        this.flyService = flyService;
    }

    @GetMapping("/flies")
    public String getFlies(@RequestParam(defaultValue = "hookset") String filter, Model model) {
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

    @GetMapping("/flies/details/{slug}")
    public String getDetails(@PathVariable String slug, Model model) {
        log.info("Request received for flies details page, slug: {}", slug);
        FlyDto flyDto = flyService.getFlyFromSlug(slug);
        model.addAttribute("fly", flyDto);
        return "flies/details";
    }


    @GetMapping("/flies/add")
    public String getAddFly(Model model) {
        model.addAttribute("flyForm", new FlyForm());
        return "/flies/add";
    }

    @PostMapping("/flies/add")
    public String postAddFly(@Valid @ModelAttribute("flyForm") FlyForm flyForm,
                             BindingResult result) {

        // Run business logic validation
        flyService.validateFly(flyForm, result);

        // Check both Jakarta and business validation errors
        if (result.hasErrors()) {
            return "flies/add";
        }

        // Save the fly
        flyService.addFly(flyForm);

        return "redirect:/flies";
    }

    @PostMapping("/flies/delete/{flyId}")
    public String deleteFly(@PathVariable Long flyId) {
        flyService.deleteFly(flyId);
        return "redirect:/flies";
    }
}
