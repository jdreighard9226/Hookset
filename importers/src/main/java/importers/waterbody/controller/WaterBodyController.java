package importers.waterbody.controller;

import importers.waterbody.service.WaterBodyImporterService;
import org.springframework.stereotype.Component;

@Component
public class WaterBodyController {

    private final WaterBodyImporterService waterBodyImporterService;

    public WaterBodyController(WaterBodyImporterService waterBodyImporterService) {
        this.waterBodyImporterService = waterBodyImporterService;
    }

    public void importWaterBodies() {
        waterBodyImporterService.saveWaterBodies(waterBodyImporterService.getAllWaterBodies());
    }
}
