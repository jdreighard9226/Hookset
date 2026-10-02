package edu.carroll.cs341.hookset.web.controller;

import edu.carroll.cs341.hookset.service.FishService;
import edu.carroll.cs341.hookset.service.WaterBodyService;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class WaterBodyController {

    private final WaterBodyService waterBodyService;
    private final FishService fishService;
    public WaterBodyController(WaterBodyService waterBodyService, FishService fishService) {
        this.waterBodyService = waterBodyService;
        this.fishService = fishService;
    }


    @GetMapping("/waterbodies")
    public String getWaterBodies(Model model) {

        List<WaterBodyDto> waterBodies = waterBodyService.getAllWaterBodies();

        model.addAttribute("waterBodies", waterBodies);

        return "waterbodies/index";
    }

    @GetMapping("/waterbodies/details/{slug}")
    public String getDetails(@PathVariable String slug, Model model) {

        WaterBodyDto waterBody = waterBodyService.getWaterBodyFromSlug(slug);

        List<FishDto> fishDtos = fishService.getAllFishForWaterBody(waterBody.getWaterBodyId());
        model.addAttribute("waterBody", waterBody);
        model.addAttribute("fishes", fishDtos);

        return "waterbodies/details";
    }
}
