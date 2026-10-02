package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.web.dto.FishDto;
import org.springframework.stereotype.Component;
import shared.jpa.entity.Fish;

@Component
public class FishMapper {

    public FishDto mapToDto(Fish fish) {
        FishDto fishDto = new FishDto();

        fishDto.setFishId(fish.getFishId());
        fishDto.setFishFamily(fish.getFishFamily());
        fishDto.setFishSpecies(fish.getFishSpecies());
        fishDto.setFishImage(fish.getFishImage());

        return fishDto;
    }
}