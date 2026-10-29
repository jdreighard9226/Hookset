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

    @Column(name = "image")
    private String image;

    /**
     * Returns the unique identifier for the fish record.
     *
     * @return the fish identifier
     */
    public Long getId() {
        return id;
    }


    /**
     * Returns the biological family of the fish.
     *
     * @return the fish family
     */
    public String getFamily() {
        return family;
    }

    /**
     * Returns the species name of the fish.
     *
     * @return the fish species
     */
    public String getSpecies() {
        return species;
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
     * Sets the species name of the fish.
     *
     * @param species the fish species
     */
    public void setSpecies(String species) {
        this.species = species;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}