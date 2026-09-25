package importers.waterbody.dto;

public class FishWaterBodyDto {

    private String waterBodyName;
    private Integer speciesId;

    public String getWaterBodyName() {
        return waterBodyName;
    }

    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }

    public Integer getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(Integer speciesId) {
        this.speciesId = speciesId;
    }
}
