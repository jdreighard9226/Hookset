package edu.carroll.cs341.hookset.importers.waterbody.mapper.impl;

import edu.carroll.cs341.hookset.importers.jpa.entity.Fish;
import edu.carroll.cs341.hookset.importers.waterbody.dto.FishDto;
import edu.carroll.cs341.hookset.importers.waterbody.mapper.FishMapper;

public class FishMapperImpl implements FishMapper {

    @Override
    public Fish mapToEntity(FishDto fishDto) {
       if (fishDto == null) {
           // error logging
           return null;
       }
       Fish fish = new Fish();
       return null;
    }
}
