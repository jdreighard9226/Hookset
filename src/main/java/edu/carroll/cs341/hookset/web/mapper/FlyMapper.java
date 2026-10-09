package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import org.springframework.stereotype.Component;
import shared.jpa.entity.Fly;

/**
 * Converts between fly entities, DTOs, and forms for the web layer.
 *
 * <p>This mapper copies the fields the views need from a fly entity into
 * a new FlyDto, and builds new fly entities from submitted fly forms so
 * custom flies can be saved.</p>
 */
@Component
public class FlyMapper {

    /**
     * Converts a fly entity into a DTO.
     *
     * @param fly the fly entity to convert
     * @return the fly as a DTO
     */
    public FlyDto toDto(Fly fly) {
        FlyDto flyDto = new FlyDto();

        flyDto.setFlyId(fly.getFlyId());
        flyDto.setUserId(fly.getUserId());
        flyDto.setFlyType(fly.getFlyType());
        flyDto.setFlyName(fly.getFlyName());
        flyDto.setColor(fly.getColor());
        flyDto.setMinSize(fly.getMinSize());
        flyDto.setMaxSize(fly.getMaxSize());
        flyDto.setFlyImage(fly.getFlyImage());
        flyDto.setFlySlug(fly.getFlySlug());

        return flyDto;
    }

    /**
     * Converts a submitted fly form into a new fly entity.
     *
     * <p>The owning user and slug are not set here and must be added before
     * the fly is saved.</p>
     *
     * @param flyForm the form containing the custom fly details
     * @return a new fly entity built from the form
     */
    public Fly toEntity(FlyForm flyForm) {
        Fly fly = new Fly();

        // trim so "Adams " and "Adams" don't save as different flies
        fly.setFlyName(flyForm.getFlyName().trim());
        fly.setFlyType(flyForm.getFlyType());
        fly.setColor(flyForm.getFlyColor());
        fly.setMinSize(flyForm.getMinSize());
        fly.setMaxSize(flyForm.getMaxSize());

        return fly;
    }
}