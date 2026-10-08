package shared.jpa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a fish catch recorded by a user in the Hookset database.
 *
 * <p>Each catch record stores the user, fly, fish species, water body,
 * fish length, date caught, and any additional notes.</p>
 */
@Entity
@Table(name = "Catches")
public class Catch {

    /** The unique database identifier for the catch record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "catchId")
    private Long catchId;

    /** The identifier of the user who recorded the catch. */
    @Column(name = "userId", nullable = false)
    private Long userId;

    /** The identifier of the fly used to catch the fish. */
    @Column(name = "flyId", nullable = false)
    private Long flyId;

    /** The identifier of the fish species caught. */
    @Column(name = "fishId", nullable = false)
    private Long fishId;

    /** The identifier of the water body where the catch occurred. */
    @Column(name = "waterBodyId", nullable = false)
    private Long waterBodyId;

    /** The length of the fish in inches. */
    @Column(name = "fishLength")
    private BigDecimal fishLength;

    /** The date and time the fish was caught. */
    @Column(name = "dateCaught", nullable = false)
    private LocalDateTime dateCaught;

    /** Additional notes about the catch. */
    @Column(name = "notes")
    private String notes;

    public Long getCatchId() {
        return catchId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getFlyId() {
        return flyId;
    }

    public Long getFishId() {
        return fishId;
    }

    public Long getWaterBodyId() {
        return waterBodyId;
    }

    public BigDecimal getFishLength() {
        return fishLength;
    }

    public LocalDateTime getDateCaught() {
        return dateCaught;
    }

    public String getNotes() {
        return notes;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFlyId(Long flyId) {
        this.flyId = flyId;
    }

    public void setFishId(Long fishId) {
        this.fishId = fishId;
    }

    public void setWaterBodyId(Long waterBodyId) {
        this.waterBodyId = waterBodyId;
    }

    public void setFishLength(BigDecimal fishLength) {
        this.fishLength = fishLength;
    }

    public void setDateCaught(LocalDateTime dateCaught) {
        this.dateCaught = dateCaught;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
