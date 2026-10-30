package shared.jpa.entity;

import jakarta.persistence.*;

/**
 * Represents a fly stored in the Hookset database.
 *
 * <p>Each fly record stores the name, type, color, hook size range, image,
 * and URL slug of the fly, along with the user who created it.</p>
 *
 * <p>Hookset has two kinds of flies. Default flies have no owning user and
 * are shown to everyone. Custom flies belong to the user who created them.</p>
 */
@Entity
@Table(name = "Fly")
public class Fly {

    /** The unique database identifier for the fly. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** The identifier of the user who created the fly, or null for a default fly. */
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

    /** The smallest hook size the fly is commonly tied on. */
    @Column(name = "minSize")
    private Integer minSize;

    /** The largest hook size the fly is commonly tied on. */
    @Column(name = "maxSize")
    private Integer maxSize;

    /** The image file name shown for the fly. */
    @Column(name = "image")
    private String image;

    /** The URL slug used for the fly's details page. */
    @Column(name = "slug")
    private String slug;


    /**
     * Returns the unique identifier for the fly.
     *
     * @return the fly identifier
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the identifier of the user who created the fly.
     *
     * @return the user identifier, or {@code null} for a default fly
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * Sets the identifier of the user who created the fly.
     *
     * @param userId the user identifier, or {@code null} for a default fly
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the type of fly.
     *
     * @return the fly type
     */
    public String getType() {
        return this.type;
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
     * Returns the name of the fly.
     *
     * @return the fly name
     */
    public String getName() {
        return this.name;
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
     * Returns the color of the fly.
     *
     * @return the fly color
     */
    public String getColor() {
        return this.color;
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
     * Returns the smallest hook size the fly is commonly tied on.
     *
     * @return the minimum hook size
     */
    public Integer getMinSize() {
        return this.minSize;
    }

    /**
     * Sets the smallest hook size the fly is commonly tied on.
     *
     * @param minSize the minimum hook size
     */
    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    /**
     * Returns the largest hook size the fly is commonly tied on.
     *
     * @return the maximum hook size
     */
    public Integer getMaxSize() {
        return this.maxSize;
    }

    /**
     * Sets the largest hook size the fly is commonly tied on.
     *
     * @param maxSize the maximum hook size
     */
    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Returns the image file name shown for the fly.
     *
     * @return the image file name
     */
    public String getImage() {
        return this.image;
    }

    /**
     * Sets the image file name shown for the fly.
     *
     * @param image the image file name
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Returns the URL slug used for the fly's details page.
     *
     * @return the fly slug
     */
    public String getSlug() {
        return this.slug;
    }

    /**
     * Sets the URL slug used for the fly's details page.
     *
     * @param slug the fly slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Checks whether another object is the same fly record.
     *
     * <p>Two flies are equal only when both have been saved and share the same
     * database ID. A fly that hasn't been saved yet is only equal to itself.</p>
     *
     * @param other the object to compare against
     * @return {@code true} if both are the same saved fly, otherwise {@code false}
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Fly otherFly)) {
            return false;
        }

        // unsaved flies have no ID yet, so they can't be equal to anything but themselves
        if (this.getId() == null || otherFly.getId() == null) {
            return false;
        }

        return this.getId().equals(otherFly.getId());
    }

    /**
     * Returns a hash code that stays the same for the fly's whole life.
     *
     * <p>The ID can't be used here because it is null until the fly is saved,
     * and a hash code that changes after saving would lose the fly inside a
     * {@code HashSet}.</p>
     *
     * @return the hash code for the fly
     */
    @Override
    public int hashCode() {
        return Fly.class.hashCode();
    }

    /**
     * Returns a readable summary of the fly for logging.
     *
     * @return the fly's ID, owner, name, type, color, sizes, image, and slug
     */
    @Override
    public String toString() {
        return "Fly{id=" + this.id
                + ", userId=" + this.userId
                + ", name='" + this.name + "'"
                + ", type='" + this.type + "'"
                + ", color='" + this.color + "'"
                + ", minSize=" + this.minSize
                + ", maxSize=" + this.maxSize
                + ", image='" + this.image + "'"
                + ", slug='" + this.slug + "'}";
    }
}