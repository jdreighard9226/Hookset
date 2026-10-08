package edu.carroll.cs341.hookset.web.mapper;

import edu.carroll.cs341.hookset.web.dto.FlyDto;
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

        return flyDto;
    }
}