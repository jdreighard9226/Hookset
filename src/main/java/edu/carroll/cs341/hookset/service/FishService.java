package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.dto.FishDto;

import java.util.List;

public interface FishService {
    List<FishDto> getAllFish();
    List<FishDto> getAllFishForWaterBody(Long waterBodyId);
}
