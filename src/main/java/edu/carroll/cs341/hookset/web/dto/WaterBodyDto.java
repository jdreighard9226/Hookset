package edu.carroll.cs341.hookset.web.dto;

/**
 * Carries water body data from the service layer to the views.
 *
 * <p>This DTO holds only the fields the web pages need to display a water
 * body, so views never work with the JPA entity directly.</p>
 */
public class WaterBodyDto {

    /** The database identifier of the water body. */
    private Long id;

    /** The name of the water body. */
    private String name;

    /** The type of water body, such as a stream or lake. */
    private String type;

    /** The state the water body is located in. */
    private String state;

    /** The description of the water body. */
    private String description;

    /** The slug used in the water body's URL. */
    private String slug;

    /**
     * Returns the database identifier of the water body.
     *
     * @return the water body identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the name of the water body.
     *
     * @return the water body name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of water body.
     *
     * @return the water body type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the state the water body is located in.
     *
     * @return the water body state
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the description of the water body.
     *
     * @return the water body description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the slug used in the water body's URL.
     *
     * @return the water body slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Sets the name of the water body.
     *
     * @param name the water body name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the database identifier of the water body.
     *
     * @param id the water body identifier
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the type of water body.
     *
     * @param type the water body type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets the state the water body is located in.
     *
     * @param state the water body state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Sets the description of the water body.
     *
     * @param description the water body description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the slug used in the water body's URL.
     *
     * @param slug the water body slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }
}