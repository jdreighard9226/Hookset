package edu.carroll.cs341.hookset.web.dto;

/**
 * Carries water body data from the service layer to the views.
 *
 * <p>This DTO holds only the fields the web pages need to display a water
 * body, so views never work with the JPA entity directly.</p>
 */
public class WaterBodyDto {

    /** The database identifier of the water body. */
    private Long waterBodyId;

    /** The name of the water body. */
    private String waterBodyName;

    /** The type of water body, such as a stream or lake. */
    private String waterBodyType;

    /** The state the water body is located in. */
    private String waterBodyState;

    /** The description of the water body. */
    private String waterBodyDescription;

    /** The slug used in the water body's URL. */
    private String waterBodySlug;

    /**
     * Returns the database identifier of the water body.
     *
     * @return the water body identifier
     */
    public Long getWaterBodyId() {
        return waterBodyId;
    }

    /**
     * Returns the name of the water body.
     *
     * @return the water body name
     */
    public String getWaterBodyName() {
        return waterBodyName;
    }

    /**
     * Returns the type of water body.
     *
     * @return the water body type
     */
    public String getWaterBodyType() {
        return waterBodyType;
    }

    /**
     * Returns the state the water body is located in.
     *
     * @return the water body state
     */
    public String getWaterBodyState() {
        return waterBodyState;
    }

    /**
     * Returns the description of the water body.
     *
     * @return the water body description
     */
    public String getWaterBodyDescription() {
        return waterBodyDescription;
    }

    /**
     * Returns the slug used in the water body's URL.
     *
     * @return the water body slug
     */
    public String getWaterBodySlug() {
        return waterBodySlug;
    }

    /**
     * Sets the name of the water body.
     *
     * @param waterBodyName the water body name
     */
    public void setWaterBodyName(String waterBodyName) {
        this.waterBodyName = waterBodyName;
    }

    /**
     * Sets the database identifier of the water body.
     *
     * @param waterBodyId the water body identifier
     */
    public void setWaterBodyId(Long waterBodyId) {
        this.waterBodyId = waterBodyId;
    }

    /**
     * Sets the type of water body.
     *
     * @param waterBodyType the water body type
     */
    public void setWaterBodyType(String waterBodyType) {
        this.waterBodyType = waterBodyType;
    }

    /**
     * Sets the state the water body is located in.
     *
     * @param waterBodyState the water body state
     */
    public void setWaterBodyState(String waterBodyState) {
        this.waterBodyState = waterBodyState;
    }

    /**
     * Sets the description of the water body.
     *
     * @param waterBodyDescription the water body description
     */
    public void setWaterBodyDescription(String waterBodyDescription) {
        this.waterBodyDescription = waterBodyDescription;
    }

    /**
     * Sets the slug used in the water body's URL.
     *
     * @param waterBodySlug the water body slug
     */
    public void setWaterBodySlug(String waterBodySlug) {
        this.waterBodySlug = waterBodySlug;
    }
}