package edu.carroll.cs341.hookset.web.dto;

public class WaterBodyDto {
    private Long waterBodyId;
    private String waterbodyName;

    public Long getWaterBodyId() {
        return waterBodyId;
    }

    public String getWaterbodyName() {
        return waterbodyName;
    }

    public void setWaterBodyName(String waterBodyName) {
        this.waterbodyName = waterBodyName;
    }
}