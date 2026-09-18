package shared.jpa.entity;

import jakarta.persistence.*;

@Entity
public class WaterBody {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waterBodyId")
    private Integer waterBodyId;

    @Column(name = "fwpLlid")
    private Integer fishWildLifeLLID;

    @Column(name = "waterBodyName")
    private String waterBodyName;

    public Integer getWaterBodyId() {
        return this.waterBodyId;
    }

    public Integer getFishWildLifeLLID() {
        return this.fishWildLifeLLID;
    }

    public String getWaterBodyName() {
        return this.waterBodyName;
    }

    public void setFishWildLifeLLID(Integer fishWildLifeLLID) {
        this.fishWildLifeLLID = fishWildLifeLLID;
    }

    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }
}
