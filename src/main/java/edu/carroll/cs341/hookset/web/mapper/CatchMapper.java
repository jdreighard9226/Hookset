package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.service.input.CatchInput;
import edu.carroll.cs341.hookset.web.dto.CatchDto;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import edu.carroll.cs341.hookset.web.form.CatchForm;
import org.springframework.stereotype.Component;
import shared.jpa.entity.Catch;

/**
 * Maps catches between entities, DTOs, forms, and service input.
 *
 * <p>A catch entity holds its fly, fish, and water body as full entities.
 * This mapper converts each of those with its own mapper and combines them
 * with the catch details into a single {@link CatchDto}.</p>
 *
 * <p>It also turns a catch into a {@link CatchForm} for the edit page, and
 * turns a submitted form into {@link CatchInput} so the catch service never
 * depends on the web layer.</p>
 */
@Component
public class CatchMapper {

    /** The mapper used to build the fly DTO for a catch. */
    private final FlyMapper flyMapper;

    /** The mapper used to build the fish DTO for a catch. */
    private final FishMapper fishMapper;

    /** The mapper used to build the water body DTO for a catch. */
    private final WaterBodyMapper waterBodyMapper;

    /**
     * Creates the catch mapper with the mappers it uses for related records.
     *
     * @param flyMapper the mapper used to build the fly DTO for a catch
     * @param fishMapper the mapper used to build the fish DTO for a catch
     * @param waterBodyMapper the mapper used to build the water body DTO for a catch
     */
    public CatchMapper(FlyMapper flyMapper, FishMapper fishMapper, WaterBodyMapper waterBodyMapper) {
        this.flyMapper = flyMapper;
        this.fishMapper = fishMapper;
        this.waterBodyMapper = waterBodyMapper;
    }

    /**
     * Converts a catch entity into a catch DTO with its fly, fish, and water body.
     *
     * @param catchRecord the catch entity to convert
     * @return the catch DTO built from the entity
     */
    public CatchDto toDto(Catch catchRecord) {
        FlyDto flyDto = flyMapper.toDto(catchRecord.getFly());
        FishDto fishDto = fishMapper.toDto(catchRecord.getFish());
        WaterBodyDto waterBodyDto = waterBodyMapper.toDto(catchRecord.getWaterBody());

        CatchDto catchDto = new CatchDto();

        catchDto.setId(catchRecord.getId());
        catchDto.setUserId(catchRecord.getUserId());
        catchDto.setFly(flyDto);
        catchDto.setFish(fishDto);
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

        catchForm.setFlyId(catchRecord.getFly().getId());
        catchForm.setFishId(catchRecord.getFish().getId());
        catchForm.setWaterBodyId(catchRecord.getWaterBody().getId());
        catchForm.setFishLength(catchRecord.getFishLength());
        catchForm.setDateCaught(catchRecord.getDateCaught());
        catchForm.setNotes(catchRecord.getNotes());

        return catchForm;
    }

    /**
     * Converts a submitted catch form into service input.
     *
     * @param catchForm the submitted catch form
     * @return the catch input built from the form
     */
    public CatchInput toInput(CatchForm catchForm) {
        return new CatchInput(
                catchForm.getFlyId(),
                catchForm.getFishId(),
                catchForm.getWaterBodyId(),
                catchForm.getFishLength(),
                catchForm.getDateCaught(),
                catchForm.getNotes()
        );
    }
}