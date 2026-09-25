package importers.waterbody.controller;

import importers.waterbody.dto.WaterBodyAndFishDto;
import importers.waterbody.service.WaterBodyImporterService;
import org.springframework.stereotype.Component;

/**
 * Coordinates the import of water body and fish data into Hookset.
 *
 * <p>This controller retrieves water body and fish data through the
 * importer service and then passes that data back to the service to be
 * saved in the Hookset database.</p>
 */
@Component
public class WaterBodyAndFishController {

    /** The service used to retrieve and save water body and fish data. */
    private final WaterBodyImporterService waterBodyImporterService;

    /**
     * Creates the controller with the importer service it depends on.
     *
     * @param waterBodyImporterService the service used to run the import
     */
    public WaterBodyAndFishController(WaterBodyImporterService waterBodyImporterService) {
        this.waterBodyImporterService = waterBodyImporterService;
    }

    /**
     * Imports water bodies, fish, and the relationships between them.
     *
     * @return true if all data was saved successfully, false otherwise
     */
    public boolean importWaterBodiesAndFish() {

        // pull everything from the FWP API first
        WaterBodyAndFishDto waterBodyAndFishDto =
                waterBodyImporterService.getAllWaterBodies();

        // then save fish, water bodies, and their relationships
        return waterBodyImporterService.saveFishAndWaterBodies(
                waterBodyAndFishDto.getFishWaterBodyDtos(),
                waterBodyAndFishDto.getFishDtos(),
                waterBodyAndFishDto.getWaterBodyDtos()
        );
    }
}