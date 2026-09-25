package shared.jpa.entity;

import jakarta.persistence.*;

/**
 * Represents a fish species found in a water body in the Hookset database.
 *
 * <p>Each record links one fish to one water body using their database
 * identifiers. This connects fish species to the waters where Montana FWP
 * reports them.</p>
 */
@Entity
@Table(name = "FishWaterBodies")
public class FishWaterBody {

    /** The unique database identifier for the fish water body record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fishWaterBodyId")
    private Long fishWaterBodyId;

    /** The database identifier of the fish. */
    @Column(name = "fishId")
    private Long fishId;

    /** The database identifier of the water body. */
    @Column(name = "waterBodyId")
    private Long waterBodyId;

    /**
     * Returns the unique identifier for the fish water body record.
     *
     * @return the fish water body identifier
     */
    public Long getFishWaterBodyId() {
        return fishWaterBodyId;
    }

    /**
     * Returns the database identifier of the fish.
     *
     * @return the fish identifier
     */
    public Long getFishId() {
        return fishId;
    }

    /**
     * Sets the database identifier of the fish.
     *
     * @param fishId the fish identifier
     */
    public void setFishId(Long fishId) {
        this.fishId = fishId;
    }

    /**
     * Returns the database identifier of the water body.
     *
     * @return the water body identifier
     */
    public Long getWaterBodyId() {
        return waterBodyId;
    }

    /**
     * Sets the database identifier of the water body.
     *
     * @param waterBodyId the water body identifier
     */
    public void setWaterBodyId(Long waterBodyId) {
        this.waterBodyId = waterBodyId;
    }
}