package importers.waterbody.dto;

/**
 * Represents a relationship between a fish species and a water body
 * found in the Montana FWP API.
 *
 * <p>Database identifiers do not exist yet when this DTO is created, so the
 * relationship is stored using the water body name and the FWP species
 * identifier. These values are later used to look up the database ids.</p>
 */
public class FishWaterBodyDto {

    /** The name of the water body the fish is found in. */
    // will be replaced by the FWP LLID once water bodies are keyed on it
    private String waterBodyName;

    /** The species identifier provided by Montana FWP. */
    private Integer speciesId;

    /**
     * Returns the name of the water body.
     *
     * @return the water body name
     */
    public String getWaterBodyName() {
        return waterBodyName;
    }

    /**
     * Sets the name of the water body.
     *
     * @param waterBodyName the water body name
     */
    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }

    /**
     * Returns the Montana FWP species identifier.
     *
     * @return the FWP species identifier
     */
    public Integer getSpeciesId() {
        return speciesId;
    }

    /**
     * Sets the Montana FWP species identifier.
     *
     * @param speciesId the FWP species identifier
     */
    public void setSpeciesId(Integer speciesId) {
        this.speciesId = speciesId;
    }
}