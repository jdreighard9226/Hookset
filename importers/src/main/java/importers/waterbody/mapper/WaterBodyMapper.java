package importers.waterbody.mapper;

import shared.jpa.entity.WaterBody;
import importers.waterbody.dto.WaterBodyDto;

public interface WaterBodyMapper {
    public WaterBody mapToEntity(WaterBodyDto waterBodyDto);
}
