package edu.carroll.cs341.hookset.web.dto;

/**
 * Carries fly data from the service layer to the views.
 *
 * <p>This DTO holds only the fields the web pages need to display a fly,
 * so views never work with the JPA entity directly. Default Hookset flies
 * have no user ID, while custom flies carry the ID of the user who created
 * them.</p>
 */
public class FlyDto {

    /** The database identifier of the fly. */
    private Long flyId;

    /** The ID of the user who created the fly, or null for default flies. */
    private Long userId;

    /** The type of fly, such as dry fly, nymph, or streamer. */
    private String flyType;

    /** The name of the fly. */
    private String flyName;

    /** The color of the fly. */
    private String color;

    /** The minimum hook size the fly is tied in. */
    private Integer minSize;

    /** The maximum hook size the fly is tied in. */
    private Integer maxSize;

    /** The image shown for the fly. */
    private String flyImage;

    /** The URL slug used to look up the fly. */
    private String flySlug;

    /**
     * Returns the database identifier of the fly.
     *
     * @return the fly identifier
     */
    public Long getFlyId() {
        return flyId;
    }

    /**
     * Sets the database identifier of the fly.
     *
     * @param flyId the fly identifier
     */
    public void setFlyId(Long flyId) {
        this.flyId = flyId;
    }

    /**
     * Returns the ID of the user who created the fly.
     *
     * @return the user ID, or {@code null} for default flies
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who created the fly.
     *
     * @param userId the user ID, or {@code null} for default flies
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the type of fly.
     *
     * @return the fly type
     */
    public String getFlyType() {
        return flyType;
    }

    /**
     * Sets the type of fly.
     *
     * @param flyType the fly type
     */
    public void setFlyType(String flyType) {
        this.flyType = flyType;
    }

    /**
     * Returns the name of the fly.
     *
     * @return the fly name
     */
    public String getFlyName() {
        return flyName;
    }

    /**
     * Sets the name of the fly.
     *
     * @param flyName the fly name
     */
    public void setFlyName(String flyName) {
        this.flyName = flyName;
    }

    /**
     * Returns the color of the fly.
     *
     * @return the fly color
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the color of the fly.
     *
     * @param color the fly color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Returns the minimum hook size the fly is tied in.
     *
     * @return the minimum size
     */
    public Integer getMinSize() {
        return minSize;
    }

    /**
     * Sets the minimum hook size the fly is tied in.
     *
     * @param minSize the minimum size
     */
    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    /**
     * Returns the maximum hook size the fly is tied in.
     *
     * @return the maximum size
     */
    public Integer getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maximum hook size the fly is tied in.
     *
     * @param maxSize the maximum size
     */
    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Returns the image shown for the fly.
     *
     * @return the fly image
     */
    public String getFlyImage() {
        return flyImage;
    }

    /**
     * Sets the image shown for the fly.
     *
     * @param flyImage the fly image
     */
    public void setFlyImage(String flyImage) {
        this.flyImage = flyImage;
    }

    /**
     * Returns the URL slug used to look up the fly.
     *
     * @return the fly slug
     */
    public String getFlySlug() {
        return flySlug;
    }

    /**
     * Sets the URL slug used to look up the fly.
     *
     * @param flySlug the fly slug
     */
    public void setFlySlug(String flySlug) {
        this.flySlug = flySlug;
    }

    /**
     * Checks whether this is a custom fly created by a user.
     *
     * @return {@code true} if the fly belongs to a user, {@code false} if it is a default Hookset fly
     */
    public boolean isCustomFly() {
        return userId != null;
    }
}