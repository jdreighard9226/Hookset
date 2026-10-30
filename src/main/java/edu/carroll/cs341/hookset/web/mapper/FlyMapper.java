package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.service.input.FlyInput;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import org.springframework.stereotype.Component;
import shared.jpa.entity.Fly;

/**
 * Converts flies between entities, DTOs, forms, and service input for the web layer.
 *
 * <p>This mapper copies the fields the views need from a fly entity into a new
 * {@link FlyDto}, fills a {@link FlyForm} for the edit page, and turns a
 * submitted form into {@link FlyInput} so the fly service never depends on
 * the web layer. It never builds fly entities itself. The service does that.</p>
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

    /**
     * Converts a submitted fly form into service input.
     *
     * @param flyForm the submitted fly form
     * @return the fly input built from the form
     */
    public FlyInput toInput(FlyForm flyForm) {
        return new FlyInput(
                flyForm.getName(),
                flyForm.getType(),
                flyForm.getColor(),
                flyForm.getMinSize(),
                flyForm.getMaxSize()
        );
    }
}