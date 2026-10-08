package shared.jpa.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Represents a fly stored in the Hookset database.
 *
 * <p>Each fly record stores the name, type, and color of the fly along with
 * the user who entered it and when it was created or last modified.</p>
 */
@Entity
@Table (name = "Flies")
public class Fly {

    /** The unique database identifier for the fly. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flyId")
    private Long flyId;

    /** The identifier of the user who entered the fly. May be null. */
    @Column(name = "userId")
    private Long userId;

    /** The type of fly, such as dry, nymph, or streamer. */
    @Column(name = "flyType")
    private String flyType;

    /** The name of the fly. */
    @Column(name = "flyName")
    private String flyName;

    /** The color of the fly. */
    @Column(name = "color")
    private String color;

    @Column(name = "minSize")
    private Integer minSize;

    @Column(name = "maxSize")
    private Integer maxSize;


    @Column(name = "flyImage")
    private String flyImage;

    @Column(name ="flySlug")
    private String flySlug;

    /**
     * Returns the unique identifier for the fly.
     *
     * @return the fly identifier
     */
    public Long getFlyId() {
        return flyId;
    }

    /**
     * Returns the identifier of the user who entered the fly.
     *
     * @return the user identifier, or null if not tied to a user
     */
    public Long getUserId() {
        return userId;
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
     * Returns the name of the fly.
     *
     * @return the fly name
     */
    public String getFlyName() {
        return flyName;
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
     * Sets the identifier of the user who entered the fly.
     *
     * @param userId the user identifier
     */
    public void setUserId(Long userId) {
        this.userId = userId;
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
     * Sets the name of the fly.
     *
     * @param flyName the fly name
     */
    public void setFlyName(String flyName) {
        this.flyName = flyName;
    }

    /**
     * Sets the color of the fly.
     *
     * @param color the fly color
     */
    public void setColor(String color) {
        this.color = color;
    }


    public Integer getMinSize() {
        return minSize;
    }

    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public String getFlyImage() {
        return flyImage;
    }

    public void setFlyImage(String flyImage) {
        this.flyImage = flyImage;
    }

    public String getFlySlug() {
        return flySlug;
    }

    public void setFlySlug(String flySlug) {
        this.flySlug = flySlug;
    }
}