package importers.waterbody.mapper.impl;

import shared.jpa.entity.WaterBody;
import importers.waterbody.dto.WaterBodyDto;
import importers.waterbody.mapper.WaterBodyMapper;

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
