package edu.carroll.cs341.hookset.importers.waterbody.dto;

public class WaterBodyDto {
    private String waterBodyName;

    private int fishWildLifeLLID;

    public int getFishWildLifeLLID() {
        return fishWildLifeLLID;
    }
    public String getWaterBodyName() {
        return waterBodyName;
    }
    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }

    public void setFishWildLifeLLID(int fishWildLifeLLID) {
        this.fishWildLifeLLID = fishWildLifeLLID;
    }
}
