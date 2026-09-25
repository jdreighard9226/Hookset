package importers.waterbody.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WaterBodyAndFishAttributeDto {

    @JsonProperty("WATERNAME")
    private String waterBodyName;

    @JsonProperty("SPECIESID")
    private Integer speciesId;

    @JsonProperty("SPECIES")
    private String species;

    @JsonProperty("FAMILY")
    private String family;

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

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }
}