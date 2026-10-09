package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.CatchService;
import edu.carroll.cs341.hookset.web.dto.CatchDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Controller
@RequestMapping("/catches")
public class CatchController {

    private final CatchService catchService;

    public CatchController(CatchService catchService) {
        this.catchService = catchService;
    }

    @GetMapping
    public String index(Model model) {
        List<CatchDto> catchDtos = catchService.getAllCatches();
        model.addAttribute("catches", catchDtos);

        return "catches/index";
    }

    @GetMapping("/mine")
    public String userCatches(Model model) {
        List<CatchDto> catchDtos = catchService.getAllUserCatches();
        model.addAttribute("catches", catchDtos);

        return "catches/user-catches";
    }
}