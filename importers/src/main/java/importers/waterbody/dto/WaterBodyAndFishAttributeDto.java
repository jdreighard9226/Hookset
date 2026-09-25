package importers.waterbody.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the attributes of a single feature returned by the
 * Montana FWP Fish Distribution API.
 *
 * <p>Each feature describes one fish species found in one water body.
 * The JSON field names from the API are mapped to Java fields using
 * {@link JsonProperty}.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>Montana Fish, Wildlife & Parks Fish Distribution - Streams:
 *   https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/9</li>
 * </ul>
 */
public class WaterBodyAndFishAttributeDto {

    /** The name of the water body. */
    @JsonProperty("WATERNAME")
    private String waterBodyName;

    /** The species identifier provided by Montana FWP. */
    @JsonProperty("SPECIESID")
    private Integer speciesId;

    /** The species name of the fish. */
    @JsonProperty("SPECIES")
    private String species;

    /** The biological family of the fish. */
    @JsonProperty("FAMILY")
    private String family;

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

    /**
     * Returns the species name of the fish.
     *
     * @return the fish species
     */
    public String getSpecies() {
        return species;
    }

    /**
     * Sets the species name of the fish.
     *
     * @param species the fish species
     */
    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * Returns the biological family of the fish.
     *
     * @return the fish family
     */
    public String getFamily() {
        return family;
    }

    /**
     * Sets the biological family of the fish.
     *
     * @param family the fish family
     */
    public void setFamily(String family) {
        this.family = family;
    }
}