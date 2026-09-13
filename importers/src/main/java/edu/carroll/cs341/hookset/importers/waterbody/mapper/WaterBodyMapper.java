package edu.carroll.cs341.hookset.importers.waterbody.mapper;

import edu.carroll.cs341.hookset.importers.jpa.entity.Fish;
import edu.carroll.cs341.hookset.importers.jpa.entity.WaterBody;
import edu.carroll.cs341.hookset.importers.waterbody.dto.FishDto;
import edu.carroll.cs341.hookset.importers.waterbody.dto.WaterBodyDto;

public interface WaterBodyMapper {
    public WaterBody mapToEntity(WaterBodyDto waterBodyDto);
}
