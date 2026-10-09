package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.web.dto.CatchDto;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import org.springframework.stereotype.Component;
import shared.jpa.entity.Catch;

@Component
public class CatchMapper {
    public CatchDto createCatchDto(Catch catchRecord, FlyDto flyDto, FishDto fishDto, WaterBodyDto waterBodyDto) {
        CatchDto catchDto = new CatchDto();

        catchDto.setCatchId(catchRecord.getCatchId());
        catchDto.setUserId(catchRecord.getUserId());
        catchDto.setFish(fishDto);
        catchDto.setWaterBody(waterBodyDto);
        catchDto.setFishLength(catchRecord.getFishLength());
        catchDto.setDateCaught(catchRecord.getDateCaught());
        catchDto.setNotes(catchRecord.getNotes());

        return catchDto;
    }
}
