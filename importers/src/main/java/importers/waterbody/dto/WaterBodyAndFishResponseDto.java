package importers.waterbody.dto;

import java.util.List;

/**
 * Represents one response returned by the Montana FWP Fish
 * Distribution API.
 *
 * <p>Each response contains a list of features. ArcGIS also reports whether
 * more records are available beyond the current page.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>Montana Fish, Wildlife & Parks Fish Distribution - Streams:
 *   https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/9</li>
 * </ul>
 */
public class WaterBodyAndFishResponseDto {

    /** The features returned in this page of results. */
    private List<WaterBodyAndFishFeatureDto> features;

    /** Indicates whether more records are available past this page. */
    private boolean exceededTransferLimit;

    /**
     * Returns the features in this page of results.
     *
     * @return the list of features
     */
    public List<WaterBodyAndFishFeatureDto> getFeatures() {
        return features;
    }

    /**
     * Sets the features in this page of results.
     *
     * @param features the list of features
     */
    public void setFeatures(List<WaterBodyAndFishFeatureDto> features) {
        this.features = features;
    }

    /**
     * Returns whether more records are available past this page.
     *
     * @return true if more records are available, false otherwise
     */
    public boolean isExceededTransferLimit() {
        return exceededTransferLimit;
    }

    /**
     * Sets whether more records are available past this page.
     *
     * @param exceededTransferLimit true if more records are available
     */
    public void setExceededTransferLimit(boolean exceededTransferLimit) {
        this.exceededTransferLimit = exceededTransferLimit;
    }
}