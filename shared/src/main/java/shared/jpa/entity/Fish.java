package shared.jpa.entity;

import jakarta.persistence.*;

/**
 * Represents a fish species stored in the Hookset database.
 *
 * <p>Each fish record stores the fish family and species name.
 * Fish records can be associated with
 * water bodies and catch records throughout Hookset.</p>
 */
@Entity
@Table(name = "Fishes")
public class Fish {

    /** The unique database identifier for the fish record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fishId")
    private Long fishId;

    /** The biological family of the fish. */
    @Column(name = "fishFamily")
    private String fishFamily;

    /** The species name of the fish. */
    @Column(name = "fishSpecies")
    private String fishSpecies;

    @Column(name = "fishImage")
    private String fishImage;

    /**
     * Returns the unique identifier for the fish record.
     *
     * @return the fish identifier
     */
    public Long getFishId() {
        return fishId;
    }


    /**
     * Returns the biological family of the fish.
     *
     * @return the fish family
     */
    public String getFishFamily() {
        return fishFamily;
    }

    /**
     * Returns the species name of the fish.
     *
     * @return the fish species
     */
    public String getFishSpecies() {
        return fishSpecies;
    }

    /**
     * Sets the biological family of the fish.
     *
     * @param fishFamily the fish family
     */
    public void setFishFamily(String fishFamily) {
        this.fishFamily = fishFamily;
    }

    /**
     * Sets the species name of the fish.
     *
     * @param fishSpecies the fish species
     */
    public void setFishSpecies(String fishSpecies) {
        this.fishSpecies = fishSpecies;
    }

    public String getFishImage() {
        return fishImage;
    }

    public void setFishImage(String fishImage) {
        this.fishImage = fishImage;
    }
}