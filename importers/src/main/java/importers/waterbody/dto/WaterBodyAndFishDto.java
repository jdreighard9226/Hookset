package importers.waterbody.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all water body, fish, and fish to water body data collected
 * during a single import from the Montana FWP API.
 *
 * <p>This DTO groups the three lists together so they can be passed
 * from the retrieval step to the save step as one object.</p>
 */
public class WaterBodyAndFishDto {

    /** The unique water bodies collected from the API. */
    private List<WaterBodyDto> waterBodyDtos = new ArrayList<>();

    /** The unique fish species collected from the API. */
    private List<FishDto> fishDtos = new ArrayList<>();

    /** The unique fish to water body relationships collected from the API. */
    private List<FishWaterBodyDto> fishWaterBodyDtos = new ArrayList<>();

    /**
     * Creates the DTO with the collected import data.
     *
     * @param waterBodyDtos the unique water bodies
     * @param fishDtos the unique fish species
     * @param fishWaterBodyDtos the unique fish to water body relationships
     */
    public WaterBodyAndFishDto(
            List<WaterBodyDto> waterBodyDtos,
            List<FishDto> fishDtos,
            List<FishWaterBodyDto> fishWaterBodyDtos) {

        this.waterBodyDtos = waterBodyDtos;
        this.fishDtos = fishDtos;
        this.fishWaterBodyDtos = fishWaterBodyDtos;
    }

    /**
     * Returns the unique water bodies.
     *
     * @return the list of water body DTOs
     */
    public List<WaterBodyDto> getWaterBodyDtos() {
        return waterBodyDtos;
    }

    /**
     * Returns the unique fish species.
     *
     * @return the list of fish DTOs
     */
    public List<FishDto> getFishDtos() {
        return fishDtos;
    }

    /**
     * Returns the unique fish to water body relationships.
     *
     * @return the list of fish water body DTOs
     */
    public List<FishWaterBodyDto> getFishWaterBodyDtos() {
        return fishWaterBodyDtos;
    }

    /**
     * Adds a water body to the collected data.
     *
     * @param waterBodyDto the water body to add
     */
    public void addWaterBodyDto(WaterBodyDto waterBodyDto) {
        waterBodyDtos.add(waterBodyDto);
    }

    /**
     * Adds a fish species to the collected data.
     *
     * @param fishDto the fish to add
     */
    public void addFishDto(FishDto fishDto) {
        fishDtos.add(fishDto);
    }

    /**
     * Adds a fish to water body relationship to the collected data.
     *
     * @param fishWaterBodyDto the relationship to add
     */
    public void addFishWaterBodyDto(FishWaterBodyDto fishWaterBodyDto) {
        fishWaterBodyDtos.add(fishWaterBodyDto);
    }
}