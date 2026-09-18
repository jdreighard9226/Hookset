package importers.waterbody.controller;

import importers.waterbody.service.WaterBodyImporterService;

public class WaterBodyController {

    private final WaterBodyImporterService waterBodyImporterService;

    public WaterBodyController(WaterBodyImporterService waterBodyImporterService) {
        this.waterBodyImporterService = waterBodyImporterService;
    }
}
