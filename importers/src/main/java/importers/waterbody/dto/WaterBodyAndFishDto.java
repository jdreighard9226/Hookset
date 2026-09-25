package importers.waterbody.dto;

import java.util.ArrayList;
import java.util.List;

public class WaterBodyAndFishDto {

    private List<WaterBodyDto> waterBodyDtos = new ArrayList<>();
    private List<FishDto> fishDtos = new ArrayList<>();
    private List<FishWaterBodyDto> fishWaterBodyDtos = new ArrayList<>();

    public WaterBodyAndFishDto(
            List<WaterBodyDto> waterBodyDtos,
            List<FishDto> fishDtos,
            List<FishWaterBodyDto> fishWaterBodyDtos) {

        this.waterBodyDtos = waterBodyDtos;
        this.fishDtos = fishDtos;
        this.fishWaterBodyDtos = fishWaterBodyDtos;
    }

    public List<WaterBodyDto> getWaterBodyDtos() {
        return waterBodyDtos;
    }

    public List<FishDto> getFishDtos() {
        return fishDtos;
    }

    public List<FishWaterBodyDto> getFishWaterBodyDtos() {
        return fishWaterBodyDtos;
    }

    public void addWaterBodyDto(WaterBodyDto waterBodyDto) {
        waterBodyDtos.add(waterBodyDto);
    }

    public void addFishDto(FishDto fishDto) {
        fishDtos.add(fishDto);
    }

    public void addFishWaterBodyDto(FishWaterBodyDto fishWaterBodyDto) {
        fishWaterBodyDtos.add(fishWaterBodyDto);
    }
}