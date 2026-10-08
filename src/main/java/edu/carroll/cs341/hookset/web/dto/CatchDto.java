package edu.carroll.cs341.hookset.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a catch record used throughout the Hookset application.
 *
 * <p>Contains information about the catch, including the user identifier,
 * fly, fish species, water body, fish length, date caught, and notes.</p>
 */
public class CatchDto {

    private Long catchId;
    private Long userId;

    private FlyDto fly;
    private FishDto fish;
    private WaterBodyDto waterBody;

    private BigDecimal fishLength;
    private LocalDateTime dateCaught;
    private String notes;

    public Long getCatchId() {
        return catchId;
    }

    public void setCatchId(Long catchId) {
        this.catchId = catchId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public FlyDto getFly() {
        return fly;
    }

    public void setFly(FlyDto fly) {
        this.fly = fly;
    }

    public FishDto getFish() {
        return fish;
    }

    public void setFish(FishDto fish) {
        this.fish = fish;
    }

    public WaterBodyDto getWaterBody() {
        return waterBody;
    }

    public void setWaterBody(WaterBodyDto waterBody) {
        this.waterBody = waterBody;
    }

    public BigDecimal getFishLength() {
        return fishLength;
    }

    public void setFishLength(BigDecimal fishLength) {
        this.fishLength = fishLength;
    }

    public LocalDateTime getDateCaught() {
        return dateCaught;
    }

    public void setDateCaught(LocalDateTime dateCaught) {
        this.dateCaught = dateCaught;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}