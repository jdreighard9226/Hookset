package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}
