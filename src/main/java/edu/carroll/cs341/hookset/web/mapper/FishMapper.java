package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.web.dto.FishDto;
import org.springframework.stereotype.Component;
import shared.jpa.entity.Fish;

/**
 * Converts fish entities into DTOs for the web layer.
 *
 * <p>This mapper copies the fields the views need from a fish entity into
 * a new FishDto, so the entity itself never reaches the views.</p>
 */
@Component
public class FishMapper {

    /**
     * Converts a fish entity into a DTO.
     *
     * @param fish the fish entity to convert
     * @return the fish as a DTO
     */
    public FishDto mapToDto(Fish fish) {
        FishDto fishDto = new FishDto();

        fishDto.setFishId(fish.getFishId());
        fishDto.setFishFamily(fish.getFishFamily());
        fishDto.setFishSpecies(fish.getFishSpecies());
        fishDto.setFishImage(fish.getFishImage());

        return fishDto;
    }
}