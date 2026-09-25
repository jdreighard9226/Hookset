package importers.waterbody.dto;

import java.util.List;

public class WaterBodyAndFishResponseDto {

    private List<WaterBodyAndFishFeatureDto> features;
    private boolean exceededTransferLimit;

    public List<WaterBodyAndFishFeatureDto> getFeatures() {
        return features;
    }

    public void setFeatures(List<WaterBodyAndFishFeatureDto> features) {
        this.features = features;
    }

    public boolean isExceededTransferLimit() {
        return exceededTransferLimit;
    }

    public void setExceededTransferLimit(boolean exceededTransferLimit) {
        this.exceededTransferLimit = exceededTransferLimit;
    }
}