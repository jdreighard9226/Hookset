package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import org.springframework.stereotype.Component;
import shared.jpa.entity.WaterBody;

@Component
public class WaterBodyMapper {

    public WaterBodyDto toDto(WaterBody waterBody) {
        WaterBodyDto waterBodyDto = new WaterBodyDto();

        waterBodyDto.setWaterBodyId(waterBody.getWaterBodyId());
        waterBodyDto.setWaterBodyName(waterBody.getWaterBodyName());
        waterBodyDto.setWaterBodyType(waterBody.getWaterBodyType());
        waterBodyDto.setWaterBodyDescription(waterBody.getWaterBodyDescription());
        waterBodyDto.setWaterBodySlug(waterBody.getWaterBodySlug());

        return waterBodyDto;
    }
}