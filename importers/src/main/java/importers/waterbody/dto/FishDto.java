package importers.waterbody.dto;

/**
 * Represents fish data transferred between the Montana FWP API
 * and the Hookset water body importer.
 *
 * <p>This DTO stores the Montana FWP species identifier, fish family,
 * and fish species before the data is mapped to a database entity.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>Montana Fish, Wildlife & Parks Fish Distribution - Streams:
 *   https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/9</li>
 *   <li>Montana Fish, Wildlife & Parks Fish Distribution - Lakes:
 *   https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/8</li>
 * </ul>
 */
public class FishDto {

    /** The species identifier provided by Montana FWP. */
    private String fwpSpeciesId;

    /** The biological family of the fish. */
    private String fishFamily;

    /** The species name of the fish. */
    private String fishSpecies;

    /**
     * Returns the Montana FWP species identifier.
     *
     * @return the FWP species identifier
     */
    public String getFwpSpeciesId() {
        return fwpSpeciesId;
    }

    /**
     * Returns the biological family of the fish.
     *
     * @return the fish family
     */
    public String getFishFamily() {
        return fishFamily;
    }

    /**
     * Returns the species name of the fish.
     *
     * @return the fish species
     */
    public String getFishSpecies() {
        return fishSpecies;
    }

    /**
     * Sets the Montana FWP species identifier.
     *
     * @param fwpSpeciesId the FWP species identifier
     */
    public void setFwpSpeciesId(String fwpSpeciesId) {
        this.fwpSpeciesId = fwpSpeciesId;
    }

    /**
     * Sets the biological family of the fish.
     *
     * @param fishFamily the fish family
     */
    public void setFishFamily(String fishFamily) {
        this.fishFamily = fishFamily;
    }

    /**
     * Sets the species name of the fish.
     *
     * @param fishSpecies the fish species
     */
    public void setFishSpecies(String fishSpecies) {
        this.fishSpecies = fishSpecies;
    }
}