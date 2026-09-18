package importers.waterbody.dto;

/**
 * Represents water body data transferred between the Montana FWP API
 * and the Hookset water body importer.
 *
 * <p>This DTO stores the water body name and Montana FWP LLID before
 * the data is mapped to a database entity.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>Montana Fish, Wildlife & Parks Fish Distribution - Streams:
 *   https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/9</li>
 *   <li>Montana Fish, Wildlife & Parks Fish Distribution - Lakes:
 *   https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/8</li>
 * </ul>
 */
public class WaterBodyDto {

    /** The name of the water body. */
    private String waterBodyName;

    /** The LLID used by Montana FWP to identify the water body. */
    private String fishWildLifeLLID;

    /**
     * Returns the Montana FWP LLID for the water body.
     *
     * @return the Montana FWP LLID
     */
    public String getFishWildLifeLLID() {
        return fishWildLifeLLID;
    }

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
     * Sets the Montana FWP LLID for the water body.
     *
     * @param fishWildLifeLLID the Montana FWP LLID
     */
    public void setFishWildLifeLLID(String fishWildLifeLLID) {
        this.fishWildLifeLLID = fishWildLifeLLID;
    }
}