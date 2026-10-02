package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import shared.jpa.entity.Fish;
import shared.jpa.entity.WaterBody;

import java.util.List;

public interface WaterBodyService {
    List<WaterBodyDto> getAllWaterBodies();
    WaterBodyDto getWaterBodyFromSlug(String waterBodySlug);
}
