package shared.jpa.entity;

import jakarta.persistence.*;

/**
 * Represents a water body stored in the Hookset database.
 *
 * <p>Each water body record stores the Montana FWP LLID along with
 * the name of the water body.</p>
 */
@Entity
@Table(name = "WaterBodies")
public class WaterBody {

    /** The unique database identifier for the water body. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waterBodyId")
    private long waterBodyId;

    /** The name of the water body. */
    @Column(name = "waterBodyName")
    private String waterBodyName;

    /**
     * Returns the unique identifier for the water body.
     *
     * @return the water body identifier
     */
    public long getWaterBodyId() {
        return this.waterBodyId;
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
     * Sets the name of the water body.
     *
     * @param waterBodyName the water body name
     */
    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }
}