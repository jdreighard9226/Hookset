package shared.jpa.entity;

import jakarta.persistence.*;

/**
 * Represents a water body stored in the Hookset database.
 *
 * <p>Each water body record stores the name of the water body.</p>
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

    @Column(name = "waterBodyState")
    private String waterBodyState;

    @Column(name = "waterBodyType")
    private String waterBodyType;

    @Column(name = "waterBodyDescription")
    private String waterBodyDescription;

    public String getWaterBodyDescription() {
        return waterBodyDescription;
    }

    public void setWaterBodyDescription(String waterBodyDescription) {
        this.waterBodyDescription = waterBodyDescription;
    }

    public String getWaterBodySlug() {
        return waterBodySlug;
    }

    public void setWaterBodySlug(String waterBodySlug) {
        this.waterBodySlug = waterBodySlug;
    }

    @Column(name = "waterBodySlug")
    private String waterBodySlug;


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

    public String getWaterBodyType() {
        return this.waterBodyType;
    }

    /**
     * Sets the name of the water body.
     *
     * @param waterBodyName the water body name
     */
    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }

    public void setWaterBodyType(String waterBodyType) {
        this.waterBodyType = waterBodyType;
    }

    public String getWaterBodyState() {
        return waterBodyState;
    }

    public void setWaterBodyState(String waterBodyState) {
        this.waterBodyState = waterBodyState;
    }
}