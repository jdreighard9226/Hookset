package importers.waterbody.dto;

import java.util.List;

public class WaterBodyResponseDto {

    private List<WaterBodyFeatureDto> features;
    private boolean exceededTransferLimit;

    public List<WaterBodyFeatureDto> getFeatures() {
        return features;
    }

    public void setFeatures(List<WaterBodyFeatureDto> features) {
        this.features = features;
    }

    public boolean isExceededTransferLimit() {
        return exceededTransferLimit;
    }

    public void setExceededTransferLimit(boolean exceededTransferLimit) {
        this.exceededTransferLimit = exceededTransferLimit;
    }
}