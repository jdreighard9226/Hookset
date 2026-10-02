package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.jpa.repo.FishWaterBodyRepository;
import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import edu.carroll.cs341.hookset.service.WaterBodyService;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import edu.carroll.cs341.hookset.web.mapper.FishMapper;
import edu.carroll.cs341.hookset.web.mapper.WaterBodyMapper;
import org.springframework.stereotype.Service;
import shared.jpa.entity.Fish;
import shared.jpa.entity.WaterBody;

import java.util.ArrayList;
import java.util.List;

@Service
public class WaterBodyServiceImpl implements WaterBodyService {

    private final WaterBodyRepository waterBodyRepository;
    private final FishWaterBodyRepository fishWaterBodyRepository;
    private final WaterBodyMapper waterBodyMapper;
    private final FishMapper fishMapper;

    public WaterBodyServiceImpl(WaterBodyRepository waterBodyRepository, FishWaterBodyRepository fishWaterBodyRepository, WaterBodyMapper waterBodyMapper, FishMapper fishMapper) {
        this.waterBodyRepository = waterBodyRepository;
        this.fishWaterBodyRepository = fishWaterBodyRepository;
        this.waterBodyMapper = waterBodyMapper;
        this.fishMapper = fishMapper;
    }
    @Override
    public List<WaterBodyDto> getAllWaterBodies() {
        List<WaterBody> waterBodyEntities = waterBodyRepository.findAll();
        if (waterBodyEntities.size() < 1) {
            /// log warning + size
        }

        List<WaterBodyDto> waterBodyDtos = new ArrayList<>();
        for (WaterBody waterBody : waterBodyEntities) {
            waterBodyDtos.add(waterBodyMapper.toDto(waterBody));
        }

        return waterBodyDtos;
    }


    @Override
    public WaterBodyDto getWaterBodyFromSlug(String waterBodySlug) {
        return waterBodyMapper.toDto(waterBodyRepository.getWaterBodyByWaterBodySlug(waterBodySlug));
    }
}
