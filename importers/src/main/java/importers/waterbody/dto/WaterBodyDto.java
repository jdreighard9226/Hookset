package importers.waterbody.dto;

public class WaterBodyDto {
    private String waterBodyName;

    private String fishWildLifeLLID;

    public String getFishWildLifeLLID() {
        return fishWildLifeLLID;
    }
    public String getWaterBodyName() {
        return waterBodyName;
    }
    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }

    public void setFishWildLifeLLID(String fishWildLifeLLID) {
        this.fishWildLifeLLID = fishWildLifeLLID;
    }
}
