package importers.waterbody.mapper;

import importers.waterbody.dto.FishDto;
import importers.waterbody.dto.FishWaterBodyDto;
import importers.waterbody.dto.WaterBodyDto;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import shared.jpa.entity.FishWaterBody;


public interface FishWaterBodyMapper {
    FishWaterBody mapToEntity(FishWaterBodyDto fishWaterBodyDto);
}
