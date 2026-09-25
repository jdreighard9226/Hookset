package importers.waterbody.mapper;

import org.springframework.stereotype.Component;
import shared.jpa.entity.Fish;
import importers.waterbody.dto.FishDto;

/**
 * Defines the mapping operation used to convert fish data into a Fish entity.
 *
 * <p>Implementations of this interface are responsible for converting a
 * {@link FishDto} into a {@link Fish} entity for use within Hookset.</p>
 */

public interface FishMapper {

    /**
     * Maps the provided fish DTO to a Fish entity.
     *
     * @param fishDto the fish data to map
     * @return the mapped Fish entity
     */
    public Fish mapToEntity(FishDto fishDto);
}