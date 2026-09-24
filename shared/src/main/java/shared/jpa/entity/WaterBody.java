package shared.jpa.entity;

import jakarta.persistence.*;

/**
 * Represents a water body stored in the Hookset database.
 *
 * <p>Each water body record stores the Montana FWP LLID along with
 * the name of the water body.</p>
 */
@Entity
@Table(name = "WaterBody")
public class WaterBody {

    /** The unique database identifier for the water body. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waterBodyId")
    private Float waterBodyId;

    /** The LLID used by Montana FWP to identify the water body. */
    @Column(name = "fwpLlid")
    private String fishWildLifeLLID;

    /** The name of the water body. */
    @Column(name = "waterBodyName")
    private String waterBodyName;

    /**
     * Returns the unique identifier for the water body.
     *
     * @return the water body identifier
     */
    public Float getWaterBodyId() {
        return this.waterBodyId;
    }

    /**
     * Returns the Montana FWP LLID for the water body.
     *
     * @return the Montana FWP LLID
     */
    public String getFishWildLifeLLID() {
        return this.fishWildLifeLLID;
    }

    /**
     * Returns the name of the water body.
     *
     * @return the water body name
     */
    public String getWaterBodyName() {
        return this.waterBodyName;
    }

    /**
     * Sets the Montana FWP LLID for the water body.
     *
     * @param fishWildLifeLLID the Montana FWP LLID
     */
    public void setFishWildLifeLLID(String fishWildLifeLLID) {
        this.fishWildLifeLLID = fishWildLifeLLID;
    }

    /**
     * Sets the name of the water body.
     *
     * @param waterBodyName the water body name
     */
    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }
}