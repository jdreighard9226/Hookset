package shared.jpa.entity;

import jakarta.persistence.*;

/**
 * Represents a fish species stored in the Hookset database.
 *
 * <p>Each fish record stores the fish family, species name, and the image
 * shown for it on the site.</p>
 *
 * <p>Fish are linked to the water bodies they live in through the
 * {@code @ManyToMany} on {@link WaterBody}, which owns that relationship.
 * Catches point to the fish that was caught through their own
 * {@code @ManyToOne}.</p>
 */
@Entity
@Table(name = "Fish")
public class Fish {

    /** The unique database identifier for the fish record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** The biological family of the fish. */
    @Column(name = "family")
    private String family;

    /** The species name of the fish. */
    @Column(name = "species")
    private String species;

    /** The image file name shown for the fish. */
    @Column(name = "image")
    private String image;


    /**
     * Returns the unique identifier for the fish record.
     *
     * @return the fish identifier
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the biological family of the fish.
     *
     * @return the fish family
     */
    public String getFamily() {
        return this.family;
    }

    /**
     * Sets the biological family of the fish.
     *
     * @param family the fish family
     */
    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * Returns the species name of the fish.
     *
     * @return the fish species
     */
    public String getSpecies() {
        return this.species;
    }

    /**
     * Sets the species name of the fish.
     *
     * @param species the fish species
     */
    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * Returns the image file name shown for the fish.
     *
     * @return the image file name
     */
    public String getImage() {
        return this.image;
    }

    /**
     * Sets the image file name shown for the fish.
     *
     * @param image the image file name
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Checks whether another object is the same fish record.
     *
     * <p>Two fish are equal only when both have been saved and share the same
     * database ID. A fish that hasn't been saved yet is only equal to itself.</p>
     *
     * @param other the object to compare against
     * @return {@code true} if both are the same saved fish, otherwise {@code false}
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Fish otherFish)) {
            return false;
        }

        // unsaved fish have no ID yet, so they can't be equal to anything but themselves
        if (this.getId() == null || otherFish.getId() == null) {
            return false;
        }

        return this.getId().equals(otherFish.getId());
    }

    /**
     * Returns a hash code that stays the same for the fish's whole life.
     *
     * <p>The ID can't be used here because it is null until the fish is saved,
     * and a hash code that changes after saving would lose the fish inside a
     * {@code HashSet}, like the one on {@link WaterBody}.</p>
     *
     * @return the hash code for the fish
     */
    @Override
    public int hashCode() {
        return Fish.class.hashCode();
    }

    /**
     * Returns a readable summary of the fish for logging.
     *
     * @return the fish's ID, family, species, and image
     */
    @Override
    public String toString() {
        return "Fish{id=" + this.id
                + ", family='" + this.family + "'"
                + ", species='" + this.species + "'"
                + ", image='" + this.image + "'}";
    }
}