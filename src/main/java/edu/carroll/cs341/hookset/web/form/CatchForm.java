package edu.carroll.cs341.hookset.web.form;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents the data submitted through the Hookset catch form.
 *
 * <p>This form object stores the selected fly, fish species, water body,
 * fish length, date caught, and additional notes entered by a user
 * when recording a catch.</p>
 */
public class CatchForm {

    /** The identifier of the fly used to catch the fish. */
    @NotNull(message = "Please select a fly.")
    private Long flyId;

    /** The identifier of the fish species caught. */
    @NotNull(message = "Please select a fish species.")
    private Long fishId;

    /** The identifier of the water body where the fish was caught. */
    @NotNull(message = "Please select a water body.")
    private Long waterBodyId;

    /** The length of the fish in inches. */
    @DecimalMin(value = "0.01", message = "Fish length must be greater than zero.")
    @Digits(integer = 3, fraction = 2, message = "Fish length must have at most two decimal places.")
    private BigDecimal fishLength;

    /** The date and time the fish was caught. */
    @NotNull(message = "Please enter the date and time of the catch.")
    @PastOrPresent(message = "Catch date cannot be in the future.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateCaught;

    /** Additional notes about the catch. */
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters.")
    private String notes;

    /**
     * Returns the identifier of the selected fly.
     *
     * @return the fly identifier
     */
    public Long getFlyId() {
        return flyId;
    }

    /**
     * Sets the identifier of the selected fly.
     *
     * @param flyId the fly identifier
     */
    public void setFlyId(Long flyId) {
        this.flyId = flyId;
    }

    /**
     * Returns the identifier of the fish species caught.
     *
     * @return the fish identifier
     */
    public Long getFishId() {
        return fishId;
    }

    /**
     * Sets the identifier of the fish species caught.
     *
     * @param fishId the fish identifier
     */
    public void setFishId(Long fishId) {
        this.fishId = fishId;
    }

    /**
     * Returns the identifier of the selected water body.
     *
     * @return the water body identifier
     */
    public Long getWaterBodyId() {
        return waterBodyId;
    }

    /**
     * Sets the identifier of the selected water body.
     *
     * @param waterBodyId the water body identifier
     */
    public void setWaterBodyId(Long waterBodyId) {
        this.waterBodyId = waterBodyId;
    }

    /**
     * Returns the length of the fish in inches.
     *
     * @return the fish length
     */
    public BigDecimal getFishLength() {
        return fishLength;
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
     * Returns any additional notes about the catch.
     *
     * @return the catch notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets any additional notes about the catch.
     *
     * @param notes the catch notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}