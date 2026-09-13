package edu.carroll.cs341.hookset.importers.waterbody.mapper.impl;

import edu.carroll.cs341.hookset.importers.jpa.entity.Fish;
import edu.carroll.cs341.hookset.importers.jpa.entity.WaterBody;
import edu.carroll.cs341.hookset.importers.waterbody.dto.FishDto;
import edu.carroll.cs341.hookset.importers.waterbody.dto.WaterBodyDto;
import edu.carroll.cs341.hookset.importers.waterbody.mapper.WaterBodyMapper;

public class WaterBodyMapperImpl implements WaterBodyMapper {

    @Override
    public WaterBody mapToEntity(WaterBodyDto waterBodyDto) {
        if (waterBodyDto == null) {
            return null;
        }

        WaterBody waterBody = new WaterBody();
        waterBody.setWaterBodyName(waterBodyDto.getWaterBodyName());
        waterBody.setFishWildLifeLLID(waterBodyDto.getFishWildLifeLLID());

        return waterBody;
    }
}
