package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import org.springframework.stereotype.Component;
import shared.jpa.entity.WaterBody;

/**
 * Converts water body entities into DTOs for the web layer.
 *
 * <p>This mapper copies the fields the views need from a water body entity
 * into a new WaterBodyDto, so the entity itself never reaches the views.</p>
 */
@Component
public class WaterBodyMapper {

    /**
     * Converts a water body entity into a DTO.
     *
     * @param waterBody the water body entity to convert
     * @return the water body as a DTO
     */
    public WaterBodyDto toDto(WaterBody waterBody) {
        WaterBodyDto waterBodyDto = new WaterBodyDto();

        waterBodyDto.setWaterBodyId(waterBody.getWaterBodyId());
        waterBodyDto.setWaterBodyName(waterBody.getWaterBodyName());
        waterBodyDto.setWaterBodyType(waterBody.getWaterBodyType());
        waterBodyDto.setWaterBodyState(waterBody.getWaterBodyState());
        waterBodyDto.setWaterBodyDescription(waterBody.getWaterBodyDescription());
        waterBodyDto.setWaterBodySlug(waterBody.getWaterBodySlug());

        return waterBodyDto;
    }
}