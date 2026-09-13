package edu.carroll.cs341.hookset.importers.waterbody.mapper;

import edu.carroll.cs341.hookset.importers.jpa.entity.Fish;
import edu.carroll.cs341.hookset.importers.waterbody.dto.FishDto;

public interface FishMapper {
    public Fish mapToEntity(FishDto fishDto);
}
