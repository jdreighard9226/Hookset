package importers.waterbody.mapper.impl;

import shared.jpa.entity.Fish;
import importers.waterbody.dto.FishDto;
import importers.waterbody.mapper.FishMapper;

public class FishMapperImpl implements FishMapper {

    @Override
    public Fish mapToEntity(FishDto fishDto) {
       if (fishDto == null) {
           // error logging
           return null;
       }
       Fish fish = new Fish();
       fish.setFwpSpeciesId(fishDto.getFwpSpeciesId());
       fish.setFishFamily(fishDto.getFishFamily());
       fish.setFishSpecies(fishDto.getFishSpecies());

       return fish;
    }
}
