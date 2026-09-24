package importers.waterbody.dto;

public class FishWaterBodyDto {
    private long fishId;
    private long waterBodyId;

    public long getFishId() {
        return fishId;
    }

    public long getWaterBodyId() {
        return waterBodyId;
    }

    public void setFishId(long fishId) {
        this.fishId = fishId;
    }

    public void setWaterBodyId(long waterBodyId) {
        this.waterBodyId = waterBodyId;
    }
}
