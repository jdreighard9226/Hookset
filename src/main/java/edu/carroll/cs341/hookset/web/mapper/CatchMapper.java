package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.web.dto.CatchDto;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import org.springframework.stereotype.Component;
import shared.jpa.entity.Catch;

/**
 * Maps catch entities to catch DTOs.
 *
 * <p>A catch entity only stores the IDs of its fly, fish, and water body.
 * This mapper takes those related records as already-built DTOs and combines
 * them with the catch details into a single {@link CatchDto}.</p>
 */
@Component
public class CatchMapper {

    /**
     * Converts a catch entity and its related DTOs into a catch DTO.
     *
     * @param catchRecord the catch entity to convert
     * @param flyDto the fly used for the catch
     * @param fishDto the fish that was caught
     * @param waterBodyDto the water body the catch was made on
     * @return the catch DTO built from the entity and related DTOs
     */
    public CatchDto toDto(Catch catchRecord, FlyDto flyDto, FishDto fishDto, WaterBodyDto waterBodyDto) {
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