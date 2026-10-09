package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import org.springframework.stereotype.Component;
import shared.jpa.entity.Fly;

@Component
public class FlyMapper {

    public FlyDto createFlyDto(Fly fly) {
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

    public Fly createFlyEntity(FlyForm flyForm) {
        Fly fly = new Fly();

        fly.setFlyName(flyForm.getFlyName().trim());
        fly.setFlyType(flyForm.getFlyType());
        fly.setColor(flyForm.getFlyColor());
        fly.setMinSize(flyForm.getMinSize());
        fly.setMaxSize(flyForm.getMaxSize());

        return fly;
    }
}