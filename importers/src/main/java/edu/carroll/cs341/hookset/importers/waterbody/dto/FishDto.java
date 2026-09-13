package edu.carroll.cs341.hookset.importers.waterbody.dto;

public class FishDto {
    private int fwpSpeciesId;
    private String fishFamily;
    private String fishSpecies;

    public int getFwpSpeciesId() {
        return fwpSpeciesId;
    }
    public String getFishFamily() {
        return fishFamily;
    }

    public String getFishSpecies() {
        return fishSpecies;
    }

    public void setFwpSpeciesId(int fwpSpeciesId) {
        this.fwpSpeciesId = fwpSpeciesId;
    }

    public void setFishFamily(String fishFamily) {
        this.fishFamily = fishFamily;
    }

    public void setFishSpecies(String fishSpecies) {
        this.fishSpecies = fishSpecies;
    }
}
