package edu.carroll.cs341.hookset.web.dto;

public class WaterBodyDto {
    private Long waterBodyId;
    private String waterBodyName;
    private String waterBodyType;
    private String waterBodyState;
    private String waterBodyDescription;
    private String waterBodySlug;

    public Long getWaterBodyId() {
        return waterBodyId;
    }

    public String getWaterBodyName() {
        return waterBodyName;
    }

    public String getWaterBodyType() {
        return  waterBodyType;
    }

    public String getWaterBodyState() {
        return waterBodyState;
    }

    public String getWaterBodyDescription() {
        return waterBodyDescription;
    }

    public String getWaterBodySlug() {
        return waterBodySlug;
    }

    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }

    public void setWaterBodyId(Long waterBodyId) {
        this.waterBodyId = waterBodyId;
    }

    public void setWaterBodyType(String waterBodyType) {
        this.waterBodyType = waterBodyType;
    }

    public void setWaterBodyState(String waterBodyState) {
        this.waterBodyState = waterBodyState;
    }

    public void setWaterBodyDescription(String waterBodyDescription) {
        this.waterBodyDescription = waterBodyDescription;
    }

    public void setWaterBodySlug(String waterBodySlug) {
        this.waterBodySlug = waterBodySlug;
    }
}