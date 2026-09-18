package importers.waterbody.mapper;

import shared.jpa.entity.WaterBody;
import importers.waterbody.dto.WaterBodyDto;

/**
 * Defines the mapping operation used to convert water body data into a WaterBody entity.
 *
 * <p>Implementations of this interface are responsible for converting a
 * {@link WaterBodyDto} into a {@link WaterBody} entity for use within Hookset.</p>
 */
public interface WaterBodyMapper {

    /**
     * Maps the provided water body DTO to a WaterBody entity.
     *
     * @param waterBodyDto the water body data to map
     * @return the mapped WaterBody entity
     */
    public WaterBody mapToEntity(WaterBodyDto waterBodyDto);
}