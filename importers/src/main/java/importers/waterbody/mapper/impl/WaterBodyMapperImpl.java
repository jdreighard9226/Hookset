package importers.waterbody.mapper.impl;

import org.springframework.stereotype.Component;
import shared.jpa.entity.WaterBody;
import importers.waterbody.dto.WaterBodyDto;
import importers.waterbody.mapper.WaterBodyMapper;

/**
 * Maps water body data from a {@link WaterBodyDto} to a {@link WaterBody} entity.
 *
 * <p>This mapper converts water body data retrieved by the importer into a
 * WaterBody entity that can be stored in the Hookset database.</p>
 */
@Component
public class WaterBodyMapperImpl implements WaterBodyMapper {

    /**
     * Maps the provided water body DTO to a WaterBody entity.
     *
     * <p>If the provided DTO is null, no entity can be created and the method
     * returns null.</p>
     *
     * @param waterBodyDto the water body data to map
     * @return the mapped WaterBody entity, or null if waterBodyDto is null
     */
    @Override
    public WaterBody mapToEntity(WaterBodyDto waterBodyDto) {
        if (waterBodyDto == null) {
            return null;
        }

        WaterBody waterBody = new WaterBody();
        waterBody.setWaterBodyName(waterBodyDto.getWaterBodyName());
        return waterBody;
    }
}