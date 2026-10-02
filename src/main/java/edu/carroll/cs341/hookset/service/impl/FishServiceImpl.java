package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.jpa.repo.FishWaterBodyRepository;
import edu.carroll.cs341.hookset.service.FishService;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.mapper.FishMapper;
import org.springframework.stereotype.Service;
import shared.jpa.entity.Fish;

import java.util.ArrayList;
import java.util.List;

@Service
public class FishServiceImpl implements FishService {

    private final FishRepository fishRepository;
    private final FishWaterBodyRepository fishWaterBodyRepository;
    private final FishMapper fishMapper;

    public FishServiceImpl(FishRepository fishRepository, FishWaterBodyRepository fishWaterBodyRepository, FishMapper fishMapper) {
        this.fishRepository = fishRepository;
        this.fishWaterBodyRepository = fishWaterBodyRepository;
        this.fishMapper = fishMapper;
    }

    @Override
    public List<FishDto> getAllFish() {
        List<Fish> fishEntities = fishRepository.findAll();
        if (fishEntities.size() < 1) {
            // log warning
        }

        List<FishDto> fishDtos = new ArrayList<>();
        for (Fish fish : fishEntities) {
            fishDtos.add(fishMapper.mapToDto(fish));
        }

        return fishDtos;
    }

    @Override
    public List<FishDto> getAllFishForWaterBody(Long waterBodyId) {
        List<Fish> fishEntities = fishWaterBodyRepository.findFishByWaterBodyId(waterBodyId);
        if (fishEntities.size() < 1) {
            // log warning
        }
        List<FishDto> fishDtos = new ArrayList<>();
        for (Fish fish : fishEntities) {
            fishDtos.add(fishMapper.mapToDto(fish));
        }

        return fishDtos;
    }

}
