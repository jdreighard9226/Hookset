package importers.waterbody.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WaterBodyAttributeDto {

    @JsonProperty("LLID")
    private String fishWildLifeLLID;

    @JsonProperty("WATERNAME")
    private String waterBodyName;

    public String getFishWildLifeLLID() {
        return fishWildLifeLLID;
    }

    public void setFishWildLifeLLID(String fishWildLifeLLID) {
        this.fishWildLifeLLID = fishWildLifeLLID;
    }

    public String getWaterBodyName() {
        return waterBodyName;
    }

    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }
}