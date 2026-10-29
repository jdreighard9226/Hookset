package shared.jpa.entity;

import jakarta.persistence.*;

/**
 * Represents a fly stored in the Hookset database.
 *
 * <p>Each fly record stores the name, type, and color of the fly along with
 * the user who entered it and when it was created or last modified.</p>
 */
@Entity
@Table (name = "Fly")
public class Fly {

    /** The unique database identifier for the fly. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** The identifier of the user who entered the fly. May be null. */
    @Column(name = "userId")
    private Long userId;

    /** The type of fly, such as dry, nymph, or streamer. */
    @Column(name = "type")
    private String type;

    /** The name of the fly. */
    @Column(name = "name")
    private String name;

    /** The color of the fly. */
    @Column(name = "color")
    private String color;

    @Column(name = "minSize")
    private Integer minSize;

    @Column(name = "maxSize")
    private Integer maxSize;


    @Column(name = "image")
    private String image;

    @Column(name ="slug")
    private String slug;

    /**
     * Returns the unique identifier for the fly.
     *
     * @return the fly identifier
     */
    public Long getId() {
        return id;
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
    public String getType() {
        return type;
    }

    /**
     * Returns the name of the fly.
     *
     * @return the fly name
     */
    public String getName() {
        return name;
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
     * @param type the fly type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets the name of the fly.
     *
     * @param name the fly name
     */
    public void setName(String name) {
        this.name = name;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}