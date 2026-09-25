package importers.waterbody.controller;

import importers.waterbody.dto.WaterBodyAndFishDto;
import importers.waterbody.service.WaterBodyImporterService;
import org.springframework.stereotype.Component;

@Component
public class WaterBodyAndFishController {

    private final WaterBodyImporterService waterBodyImporterService;

    public WaterBodyAndFishController(WaterBodyImporterService waterBodyImporterService) {
        this.waterBodyImporterService = waterBodyImporterService;
    }

    public boolean importWaterBodiesAndFish() {

        WaterBodyAndFishDto waterBodyAndFishDto =
                waterBodyImporterService.getAllWaterBodies();

        return waterBodyImporterService.saveFishAndWaterBodies(
                waterBodyAndFishDto.getFishWaterBodyDtos(),
                waterBodyAndFishDto.getFishDtos(),
                waterBodyAndFishDto.getWaterBodyDtos()
        );
    }
}
