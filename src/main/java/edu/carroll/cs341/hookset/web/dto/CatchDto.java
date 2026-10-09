package edu.carroll.cs341.hookset.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a catch record used throughout the Hookset application.
 *
 * <p>Contains information about the catch, including the user identifier,
 * fly, fish species, water body, fish length, date caught, and notes. The
 * fly, fish, and water body are stored as their own DTOs so views can show
 * their details without extra lookups.</p>
 */
public class CatchDto {

    /** The unique ID of the catch. */
    private Long catchId;

    /** The ID of the user who logged the catch. */
    private Long userId;

    /** The fly used to make the catch. */
    private FlyDto fly;

    /** The fish species that was caught. */
    private FishDto fish;

    /** The water body the catch was made on. */
    private WaterBodyDto waterBody;

    /** The length of the fish that was caught. */
    private BigDecimal fishLength;

    /** The date and time the fish was caught. */
    private LocalDateTime dateCaught;

    /** Any notes the user added about the catch. */
    private String notes;

    /**
     * Returns the unique ID of the catch.
     *
     * @return the catch ID
     */
    public Long getCatchId() {
        return catchId;
    }

    /**
     * Sets the unique ID of the catch.
     *
     * @param catchId the catch ID
     */
    public void setCatchId(Long catchId) {
        this.catchId = catchId;
    }

    /**
     * Returns the ID of the user who logged the catch.
     *
     * @return the user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who logged the catch.
     *
     * @param userId the user ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the fly used to make the catch.
     *
     * @return the fly
     */
    public FlyDto getFly() {
        return fly;
    }

    /**
     * Sets the fly used to make the catch.
     *
     * @param fly the fly
     */
    public void setFly(FlyDto fly) {
        this.fly = fly;
    }

    /**
     * Returns the fish species that was caught.
     *
     * @return the fish
     */
    public FishDto getFish() {
        return fish;
    }

    /**
     * Sets the fish species that was caught.
     *
     * @param fish the fish
     */
    public void setFish(FishDto fish) {
        this.fish = fish;
    }

    /**
     * Returns the water body the catch was made on.
     *
     * @return the water body
     */
    public WaterBodyDto getWaterBody() {
        return waterBody;
    }

    /**
     * Sets the water body the catch was made on.
     *
     * @param waterBody the water body
     */
    public void setWaterBody(WaterBodyDto waterBody) {
        this.waterBody = waterBody;
    }

    /**
     * Returns the length of the fish that was caught.
     *
     * @return the fish length
     */
    public BigDecimal getFishLength() {
        return fishLength;
    }

    /**
     * Sets the length of the fish that was caught.
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
        return dateCaught;
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
     * Returns any notes the user added about the catch.
     *
     * @return the catch notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets any notes the user added about the catch.
     *
     * @param notes the catch notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}