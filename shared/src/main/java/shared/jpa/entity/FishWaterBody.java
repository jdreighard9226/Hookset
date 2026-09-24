package shared.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fishWaterBodies")
public class FishWaterBody {
    @Id
    @Column(name = "fishId")
    private long fishId;

    @Id
    @Column(name = "waterBodyId")
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
