package edu.carroll.cs341.hookset.web.dto;

/**
 * Carries fish species data from the service layer to the views.
 *
 * <p>This DTO holds only the fields the web pages need to display a fish
 * species, so views never work with the JPA entity directly.</p>
 */
public class FishDto {

    /** The database identifier of the fish species. */
    private Long id;

    /** The family the fish species belongs to. */
    private String family;

    /** The name of the fish species. */
    private String species;

    /** The image shown for the fish species. */
    private String image;

    /**
     * Returns the database identifier of the fish species.
     *
     * @return the fish identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database identifier of the fish species.
     *
     * @param id the fish identifier
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the family the fish species belongs to.
     *
     * @return the fish family
     */
    public String getFamily() {
        return family;
    }

    /**
     * Sets the family the fish species belongs to.
     *
     * @param family the fish family
     */
    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * Returns the name of the fish species.
     *
     * @return the fish species name
     */
    public String getSpecies() {
        return species;
    }

    /**
     * Sets the name of the fish species.
     *
     * @param species the fish species name
     */
    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * Returns the image shown for the fish species.
     *
     * @return the fish image
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the image shown for the fish species.
     *
     * @param image the fish image
     */
    public void setImage(String image) {
        this.image = image;
    }
}