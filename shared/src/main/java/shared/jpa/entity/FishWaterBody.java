package shared.jpa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "FishWaterBodies")
public class FishWaterBody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fishWaterBodyId")
    private Long fishWaterBodyId;

    @Column(name = "fishId")
    private Long fishId;

    @Column(name = "waterBodyId")
    private Long waterBodyId;

    public Long getFishWaterBodyId() {
        return fishWaterBodyId;
    }

    public Long getFishId() {
        return fishId;
    }

    public void setFishId(Long fishId) {
        this.fishId = fishId;
    }

    public Long getWaterBodyId() {
        return waterBodyId;
    }

    public void setWaterBodyId(Long waterBodyId) {
        this.waterBodyId = waterBodyId;
    }
}