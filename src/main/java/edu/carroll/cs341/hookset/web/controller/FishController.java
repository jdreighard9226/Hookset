package edu.carroll.cs341.hookset.web.controller;
import edu.carroll.cs341.hookset.service.FishService;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class FishController {
    private final FishService fishService;

    public FishController(FishService fishService) {
        this.fishService = fishService;
    }

    @GetMapping("/fish")
    public String getFish(Model model) {
        List<FishDto> fishDtos = fishService.getAllFish();
        model.addAttribute("fishes", fishDtos);

        return "/fish/index";
    }

}
