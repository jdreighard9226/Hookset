package edu.carroll.cs341.hookset.web.dto;

public class FlyDto {
    private Long flyId;
    private Long userId;
    private String flyType;
    private String flyName;
    private String color;
    private Integer minSize;
    private Integer maxSize;
    private String flyImage;


    public Long getFlyId() {
        return flyId;
    }

    public void setFlyId(Long flyId) {
        this.flyId = flyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFlyType() {
        return flyType;
    }

    public void setFlyType(String flyType) {
        this.flyType = flyType;
    }

    public String getFlyName() {
        return flyName;
    }

    public void setFlyName(String flyName) {
        this.flyName = flyName;
    }

    public String getColor() {
        return color;
    }

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

    public boolean isCustomFly() {
        return userId != null;
    }

    public String getFlyImage() {
        return flyImage;
    }

    public void setFlyImage(String flyImage) {
        this.flyImage = flyImage;
    }
}