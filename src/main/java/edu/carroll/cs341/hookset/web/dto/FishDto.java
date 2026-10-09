package edu.carroll.cs341.hookset.web.dto;

/**
 * Carries fish species data from the service layer to the views.
 *
 * <p>This DTO holds only the fields the web pages need to display a fish
 * species, so views never work with the JPA entity directly.</p>
 */
public class FishDto {

    /** The database identifier of the fish species. */
    private Long fishId;

    /** The family the fish species belongs to. */
    private String fishFamily;

    /** The name of the fish species. */
    private String fishSpecies;

    /** The image shown for the fish species. */
    private String fishImage;

    /**
     * Returns the database identifier of the fish species.
     *
     * @return the fish identifier
     */
    public Long getFishId() {
        return fishId;
    }

    /**
     * Sets the database identifier of the fish species.
     *
     * @param fishId the fish identifier
     */
    public void setFishId(Long fishId) {
        this.fishId = fishId;
    }

    /**
     * Returns the family the fish species belongs to.
     *
     * @return the fish family
     */
    public String getFishFamily() {
        return fishFamily;
    }

    /**
     * Sets the family the fish species belongs to.
     *
     * @param fishFamily the fish family
     */
    public void setFishFamily(String fishFamily) {
        this.fishFamily = fishFamily;
    }

    /**
     * Returns the name of the fish species.
     *
     * @return the fish species name
     */
    public String getFishSpecies() {
        return fishSpecies;
    }

    /**
     * Sets the name of the fish species.
     *
     * @param fishSpecies the fish species name
     */
    public void setFishSpecies(String fishSpecies) {
        this.fishSpecies = fishSpecies;
    }

    /**
     * Returns the image shown for the fish species.
     *
     * @return the fish image
     */
    public String getFishImage() {
        return fishImage;
    }

    /**
     * Sets the image shown for the fish species.
     *
     * @param fishImage the fish image
     */
    public void setFishImage(String fishImage) {
        this.fishImage = fishImage;
    }
}