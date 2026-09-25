package importers.waterbody.dto;

/**
 * Represents a single feature returned by the Montana FWP Fish
 * Distribution API.
 *
 * <p>ArcGIS wraps the requested fields for each record inside an
 * attributes object. This DTO holds that object.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>Montana Fish, Wildlife & Parks Fish Distribution - Streams:
 *   https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/9</li>
 * </ul>
 */
public class WaterBodyAndFishFeatureDto {

    /** The attributes returned for this feature. */
    private WaterBodyAndFishAttributeDto attributes;

    /**
     * Returns the attributes for this feature.
     *
     * @return the feature attributes
     */
    public WaterBodyAndFishAttributeDto getAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes for this feature.
     *
     * @param attributes the feature attributes
     */
    public void setAttributes(WaterBodyAndFishAttributeDto attributes) {
        this.attributes = attributes;
    }
}