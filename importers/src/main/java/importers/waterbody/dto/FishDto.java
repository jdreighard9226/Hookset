package importers.waterbody.dto;

public class FishDto {
    private String fwpSpeciesId;
    private String fishFamily;
    private String fishSpecies;

    public String getFwpSpeciesId() {
        return fwpSpeciesId;
    }
    public String getFishFamily() {
        return fishFamily;
    }

    public String getFishSpecies() {
        return fishSpecies;
    }

    public void setFwpSpeciesId(String fwpSpeciesId) {
        this.fwpSpeciesId = fwpSpeciesId;
    }

    public void setFishFamily(String fishFamily) {
        this.fishFamily = fishFamily;
    }

    public void setFishSpecies(String fishSpecies) {
        this.fishSpecies = fishSpecies;
    }
}
