package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.service.input.FlyInput;
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

        flyDto.setId(fly.getId());
        flyDto.setUserId(fly.getUserId());
        flyDto.setType(fly.getType());
        flyDto.setName(fly.getName());
        flyDto.setColor(fly.getColor());
        flyDto.setMinSize(fly.getMinSize());
        flyDto.setMaxSize(fly.getMaxSize());
        flyDto.setImage(fly.getImage());
        flyDto.setSlug(fly.getSlug());

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
        fly.setName(flyForm.getName().trim());
        fly.setType(flyForm.getType());
        fly.setColor(flyForm.getColor());
        fly.setMinSize(flyForm.getMinSize());
        fly.setMaxSize(flyForm.getMaxSize());

        return fly;
    }

    /**
     * Converts a fly into a form so it can be edited.
     *
     * @param flyDto the fly to convert
     * @return a fly form filled with the fly's values
     */
    public FlyForm toForm(FlyDto flyDto) {
        FlyForm flyForm = new FlyForm();

        flyForm.setId(flyDto.getId());
        flyForm.setName(flyDto.getName());
        flyForm.setType(flyDto.getType());
        flyForm.setColor(flyDto.getColor());
        flyForm.setMinSize(flyDto.getMinSize());
        flyForm.setMaxSize(flyDto.getMaxSize());

        return flyForm;
    }

    public FlyInput toInput(FlyForm flyForm) {
        return new FlyInput(flyForm.getName(), flyForm.getType(), flyForm.getColor(), flyForm.getMinSize(), flyForm.getMaxSize());
    }

    /**
     * Converts a fly into a form so it can be edited.
     *
     * @param fly the fly to convert
     * @return a fly form filled with the fly's values
     */
    public FlyForm toForm(Fly fly) {
        FlyForm flyForm = new FlyForm();

        flyForm.setId(fly.getId());
        flyForm.setName(fly.getName());
        flyForm.setType(fly.getType());
        flyForm.setColor(fly.getColor());
        flyForm.setMinSize(fly.getMinSize());
        flyForm.setMaxSize(fly.getMaxSize());

        return flyForm;
    }
}