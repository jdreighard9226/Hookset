package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.dto.FlyDto;

import java.util.List;

public interface FlyService {
    List<FlyDto> getAllFlies();
    List<FlyDto> getAllHooksetFlies();
    List<FlyDto> getAllUserFlies();
    FlyDto getFlyFromSlug(String flySlug);
}
