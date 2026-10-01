package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import edu.carroll.cs341.hookset.service.WaterBodyService;
import edu.carroll.cs341.hookset.web.form.WaterBodyForm;
import shared.jpa.entity.WaterBody;

import java.util.List;

public class WaterBodyImpl implements WaterBodyService {

    private final WaterBodyRepository waterBodyRepository;
    public WaterBodyImpl(WaterBodyRepository waterBodyRepository) {
        this.waterBodyRepository = waterBodyRepository;
    }
    @Override
    public List<WaterBody> getAllWaterBodies() {
        return waterBodyRepository.findAll();
    }
}
