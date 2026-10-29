package shared.jpa.entity;

import jakarta.persistence.*;

/**
 * Represents a water body stored in the Hookset database.
 *
 * <p>Each water body record stores the name of the water body.</p>
 */
@Entity
@Table(name = "WaterBody")
public class WaterBody {

    /** The unique database identifier for the water body. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    /** The name of the water body. */
    @Column(name = "name")
    private String name;

    @Column(name = "state")
    private String state;

    @Column(name = "type")
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "slug")
    private String slug;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Returns the unique identifier for the water body.
     *
     * @return the water body identifier
     */
    public long getId() {
        return this.id;
    }


    /**
     * Returns the name of the water body.
     *
     * @return the water body name
     */
    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    /**
     * Sets the name of the water body.
     *
     * @param name the water body namea
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}