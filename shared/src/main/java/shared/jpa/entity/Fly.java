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

    /** The date and time the fly was entered. */
    @Column(name = "dateEntered")
    private Instant dateEntered;

    /** The date and time the fly was last modified. May be null. */
    @Column(name = "LastDateModified")
    private Instant lastDateModified;

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
     * Returns the date and time the fly was entered.
     *
     * @return the date entered
     */
    public Instant getDateEntered() {
        return dateEntered;
    }

    /**
     * Returns the date and time the fly was last modified.
     *
     * @return the last modified date, or null if never modified
     */
    public Instant getLastDateModified() {
        return lastDateModified;
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

    /**
     * Sets the date and time the fly was entered.
     *
     * @param dateEntered the date entered
     */
    public void setDateEntered(Instant dateEntered) {
        this.dateEntered = dateEntered;
    }

    /**
     * Sets the date and time the fly was last modified.
     *
     * @param dateModified the last modified date
     */
    public void setLastDateModified(Instant dateModified) {
        this.lastDateModified = dateModified;
    }
}