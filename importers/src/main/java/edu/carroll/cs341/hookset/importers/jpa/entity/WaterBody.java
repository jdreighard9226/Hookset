package edu.carroll.cs341.hookset.importers.jpa.entity;

import jakarta.persistence.*;

@Entity
public class WaterBody {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Water_Body_ID")
    private int waterBodyId;

    @Column(name = "FWP_LLID")
    private int fishWildLifeLLID;

    @Column(name = "Water_Body_Name")
    private String waterBodyName;

    public int getWaterBodyId() {
        return this.waterBodyId;
    }

    public int getFishWildLifeLLID() {
        return this.fishWildLifeLLID;
    }

    public String getWaterBodyName() {
        return this.waterBodyName;
    }

    public void setFishWildLifeLLID(int fishWildLifeLLID) {
        this.fishWildLifeLLID = fishWildLifeLLID;
    }

    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }
}
