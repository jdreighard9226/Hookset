package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.web.dto.CatchDto;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import edu.carroll.cs341.hookset.web.form.CatchForm;
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
        catchDto.setFly(flyDto);
        catchDto.setWaterBody(waterBodyDto);
        catchDto.setFishLength(catchRecord.getFishLength());
        catchDto.setDateCaught(catchRecord.getDateCaught());
        catchDto.setNotes(catchRecord.getNotes());

        return catchDto;
    }

    /**
     * Converts a catch into a form so it can be edited.
     *
     * @param catchRecord the catch to convert
     * @return a catch form filled with the catch's values
     */
    public CatchForm toForm(Catch catchRecord) {
        CatchForm catchForm = new CatchForm();

        catchForm.setFlyId(catchRecord.getFlyId());
        catchForm.setFishId(catchRecord.getFishId());
        catchForm.setWaterBodyId(catchRecord.getWaterBodyId());
        catchForm.setFishLength(catchRecord.getFishLength());
        catchForm.setDateCaught(catchRecord.getDateCaught());
        catchForm.setNotes(catchRecord.getNotes());

        return catchForm;
    }
}