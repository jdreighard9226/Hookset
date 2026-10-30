package shared.jpa.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a water body stored in the Hookset database.
 *
 * <p>Each water body record stores the name, type, state, description, and
 * URL slug of the water body, along with the fish species found in it.</p>
 *
 * <p>Water body owns the {@code @ManyToMany} link to {@link Fish}, so fish are
 * added to or removed from a water body through {@link #getFishes()}. The links
 * are stored in the FishWaterBody join table.</p>
 */
@Entity
@Table(name = "WaterBody")
public class WaterBody {

    /** The unique database identifier for the water body. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** The name of the water body. */
    @Column(name = "name")
    private String name;

    /** The state the water body is located in. */
    @Column(name = "state")
    private String state;

    /** The type of water body, either River, Lake, or Stream. */
    @Column(name = "type")
    private String type;

    /** A short description of the water body. */
    @Column(name = "description")
    private String description;

    /** The URL slug used for the water body's details page. */
    @Column(name = "slug")
    private String slug;

    /** The fish species found in the water body, sorted by species name. */
    @ManyToMany
    @JoinTable(
            name = "FishWaterBody",
            joinColumns = @JoinColumn(name = "waterBodyId"),
            inverseJoinColumns = @JoinColumn(name = "fishId")
    )
    @OrderBy("species")
    private Set<Fish> fishes = new HashSet<>();


    /**
     * Returns the unique identifier for the water body.
     *
     * @return the water body identifier, or {@code null} if it hasn't been saved yet
     */
    public Long getId() {
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

    /**
     * Sets the name of the water body.
     *
     * @param name the water body name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the state the water body is located in.
     *
     * @return the water body state
     */
    public String getState() {
        return this.state;
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
     * Returns the type of the water body.
     *
     * @return the water body type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the type of the water body.
     *
     * @param type the water body type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the description of the water body.
     *
     * @return the water body description
     */
    public String getDescription() {
        return this.description;
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
     * Returns the URL slug of the water body.
     *
     * @return the water body slug
     */
    public String getSlug() {
        return this.slug;
    }

    /**
     * Sets the URL slug of the water body.
     *
     * @param slug the water body slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Returns the fish species found in the water body.
     *
     * <p>Adding to or removing from this set and saving the water body updates
     * the FishWaterBody join table.</p>
     *
     * @return the set of fish in the water body
     */
    public Set<Fish> getFishes() {
        return this.fishes;
    }

    /**
     * Replaces the fish species found in the water body.
     *
     * @param fishes the new set of fish in the water body
     */
    public void setFishes(Set<Fish> fishes) {
        this.fishes = fishes;
    }

    /**
     * Checks whether another object is the same water body record.
     *
     * <p>Two water bodies are equal only when both have been saved and share the
     * same database ID. A water body that hasn't been saved yet is only equal to itself.</p>
     *
     * @param other the object to compare against
     * @return {@code true} if both are the same saved water body, otherwise {@code false}
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (!(other instanceof WaterBody otherWaterBody)) {
            return false;
        }

        if (this.getId() == null || otherWaterBody.getId() == null) {
            return false;
        }

        return this.getId().equals(otherWaterBody.getId());
    }

    /**
     * Returns a hash code that stays the same for the water body's whole life.
     *
     * <p>The ID can't be used here because it is null until the water body is saved,
     * and a hash code that changes after saving would lose the water body inside a
     * {@code HashSet}.</p>
     *
     * @return the hash code for the water body
     */
    @Override
    public int hashCode() {
        return WaterBody.class.hashCode();
    }

    /**
     * Returns a readable summary of the water body for logging.
     *
     * <p>The fish are left out on purpose, since printing them would load the whole
     * collection from the database. The description is left out since it's too long
     * for a log line.</p>
     *
     * @return the water body's ID, name, type, state, and slug
     */
    @Override
    public String toString() {
        return "WaterBody{id=" + this.id
                + ", name='" + this.name + "'"
                + ", type='" + this.type + "'"
                + ", state='" + this.state + "'"
                + ", slug='" + this.slug + "'}";
    }
}