package importers.waterbody.mapper.impl;

import org.springframework.stereotype.Component;
import shared.jpa.entity.Fish;
import importers.waterbody.dto.FishDto;
import importers.waterbody.mapper.FishMapper;

/**
 * Maps fish data from a {@link FishDto} to a {@link Fish} entity.
 *
 * <p>This mapper converts fish data retrieved by the water body importer
 * into a Fish entity that can be stored in the Hookset database.</p>
 */
@Component
public class FishMapperImpl implements FishMapper {

    /**
     * Maps the provided fish DTO to a Fish entity.
     *
     * <p>If the provided DTO is null, no entity can be created and
     * the method returns null.</p>
     *
     * @param fishDto the fish data to map
     * @return the mapped Fish entity, or null if fishDto is null
     */
    @Override
    public Fish mapToEntity(FishDto fishDto) {
        if (fishDto == null) {
            // Error logging will be added here.
            return null;
        }

        Fish fish = new Fish();
        fish.setFwpSpeciesId(fishDto.getFwpSpeciesId());
        fish.setFishFamily(fishDto.getFishFamily());
        fish.setFishSpecies(fishDto.getFishSpecies());

        return fish;
    }
}