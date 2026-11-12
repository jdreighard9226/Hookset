package entities.jpa.entity;

import jakarta.persistence.*;

import java.util.Objects;

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
     * Checks whether this fish is the same as another object.
     *
     * <p>Two fish are equal when they have the same family and species. The ID is not
     * compared, so an unsaved fish can still be equal to a saved one with the same details.</p>
     *
     * @param o the object to compare against
     * @return true if the other object is a fish with the same family and species
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Fish fish)) return false;
        return Objects.equals(getFamily(), fish.getFamily()) && Objects.equals(getSpecies(), fish.getSpecies());
    }

    /**
     * Returns a hash code built from the fish's family and species instead of its ID.
     *
     * <p>The ID can't be used here because it is null until the fish is saved,
     * and a hash code that changes after saving would lose the fish inside a
     * {@code HashSet}.</p>
     *
     * @return the hash code for the fish
     */
    @Override
    public int hashCode() {
        return Objects.hash(getFamily(), getSpecies());
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