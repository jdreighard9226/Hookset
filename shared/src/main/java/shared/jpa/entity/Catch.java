package shared.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a fish catch recorded by a user in the Hookset database.
 *
 * <p>Each catch record stores the user, fly, fish species, water body,
 * fish length, date caught, and any additional notes. The fly, fish, and
 * water body are mapped as full entities so they can be read straight off
 * the catch without separate lookups.</p>
 */
@Entity
@Table(name = "Catch")
public class Catch {

    /** The unique database identifier for the catch record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** The identifier of the user who recorded the catch. */
    @Column(name = "userId")
    private Long userId;

    /** The fly used to catch the fish. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flyId")
    private Fly fly;

    /** The fish species caught. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fishId")
    private Fish fish;

    /** The water body where the catch occurred. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waterBodyId")
    private WaterBody waterBody;

    /** The length of the fish in inches. */
    @Column(name = "fishLength")
    private BigDecimal fishLength;

    /** The date and time the fish was caught. */
    @Column(name = "dateCaught")
    private LocalDateTime dateCaught;

    /** Additional notes about the catch. */
    @Column(name = "notes")
    private String notes;


    /**
     * Returns the unique identifier for the catch.
     *
     * @return the catch identifier
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the identifier of the user who recorded the catch.
     *
     * @return the user identifier
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * Sets the identifier of the user who recorded the catch.
     *
     * @param userId the user identifier
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the fly used to catch the fish.
     *
     * @return the fly
     */
    public Fly getFly() {
        return this.fly;
    }

    /**
     * Sets the fly used to catch the fish.
     *
     * @param fly the fly
     */
    public void setFly(Fly fly) {
        this.fly = fly;
    }

    /**
     * Returns the fish species caught.
     *
     * @return the fish
     */
    public Fish getFish() {
        return this.fish;
    }

    /**
     * Sets the fish species caught.
     *
     * @param fish the fish
     */
    public void setFish(Fish fish) {
        this.fish = fish;
    }

    /**
     * Returns the water body where the catch occurred.
     *
     * @return the water body
     */
    public WaterBody getWaterBody() {
        return this.waterBody;
    }

    /**
     * Sets the water body where the catch occurred.
     *
     * @param waterBody the water body
     */
    public void setWaterBody(WaterBody waterBody) {
        this.waterBody = waterBody;
    }

    /**
     * Returns the length of the fish in inches.
     *
     * @return the fish length, or {@code null} if none was recorded
     */
    public BigDecimal getFishLength() {
        return this.fishLength;
    }

    /**
     * Sets the length of the fish in inches.
     *
     * @param fishLength the fish length
     */
    public void setFishLength(BigDecimal fishLength) {
        this.fishLength = fishLength;
    }

    /**
     * Returns the date and time the fish was caught.
     *
     * @return the date caught
     */
    public LocalDateTime getDateCaught() {
        return this.dateCaught;
    }

    /**
     * Sets the date and time the fish was caught.
     *
     * @param dateCaught the date caught
     */
    public void setDateCaught(LocalDateTime dateCaught) {
        this.dateCaught = dateCaught;
    }

    /**
     * Returns the notes about the catch.
     *
     * @return the notes, or {@code null} if none were recorded
     */
    public String getNotes() {
        return this.notes;
    }

    /**
     * Sets the notes about the catch.
     *
     * @param notes the notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Checks whether another object is the same catch record.
     *
     * <p>Two catches are equal only when both have been saved and share the same
     * database ID. A catch that hasn't been saved yet is only equal to itself.</p>
     *
     * @param other the object to compare against
     * @return {@code true} if both are the same saved catch, otherwise {@code false}
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Catch otherCatch)) {
            return false;
        }

        // unsaved catches have no ID yet, so they can't be equal to anything but themselves
        if (this.getId() == null || otherCatch.getId() == null) {
            return false;
        }

        return this.getId().equals(otherCatch.getId());
    }

    /**
     * Returns a hash code that stays the same for the catch's whole life.
     *
     * <p>The ID can't be used here because it is null until the catch is saved,
     * and a hash code that changes after saving would lose the catch inside a
     * {@code HashSet}.</p>
     *
     * @return the hash code for the catch
     */
    @Override
    public int hashCode() {
        return Catch.class.hashCode();
    }

    /**
     * Returns a readable summary of the catch for logging.
     *
     * <p>The fly, fish, and water body are left out on purpose, since printing
     * any of them would trigger a lazy load from the database. Notes are left
     * out too, since they're free text that can run up to 1000 characters.</p>
     *
     * @return the catch's ID, user ID, fish length, and date caught
     */
    @Override
    public String toString() {
        return "Catch{id=" + this.id
                + ", userId=" + this.userId
                + ", fishLength=" + this.fishLength
                + ", dateCaught=" + this.dateCaught + "}";
    }
}