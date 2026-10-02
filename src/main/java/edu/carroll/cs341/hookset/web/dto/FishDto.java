package edu.carroll.cs341.hookset.web.dto;

public class FishDto {
    private Long fishId;
    private String fishFamily;
    private String fishSpecies;
    private String fishImage;


    public Long getFishId() {
        return fishId;
    }

    public String getFishFamily() {
        return fishFamily;
    }

    public String getFishSpecies() {
        return fishSpecies;
    }

    public void setFishFamily(String fishFamily) {
        this.fishFamily = fishFamily;
    }

    public void setFishSpecies(String fishSpecies) {
        this.fishSpecies = fishSpecies;
    }

    public void setFishId(Long fishId) {
        this.fishId = fishId;
    }

    public String getFishImage() {
        return fishImage;
    }

    public void setFishImage(String fishImage) {
        this.fishImage = fishImage;
    }
}
