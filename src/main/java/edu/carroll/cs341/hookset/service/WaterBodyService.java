package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.form.WaterBodyForm;
import shared.jpa.entity.WaterBody;

import java.util.List;

public interface WaterBodyService {
    List<WaterBody> getAllWaterBodies();
}
