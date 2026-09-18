package importers.waterbody.mapper;

import shared.jpa.entity.Fish;
import importers.waterbody.dto.FishDto;

public interface FishMapper {
    public Fish mapToEntity(FishDto fishDto);
}
