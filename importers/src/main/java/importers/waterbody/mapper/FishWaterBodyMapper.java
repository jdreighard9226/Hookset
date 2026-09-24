package importers.waterbody.mapper;

import importers.waterbody.dto.FishDto;
import importers.waterbody.dto.WaterBodyDto;
import shared.jpa.entity.FishWaterBody;

public interface FishWaterBodyMapper {
    FishWaterBody mapToEntity(FishDto fishDto, WaterBodyDto waterBodyDto);
}
