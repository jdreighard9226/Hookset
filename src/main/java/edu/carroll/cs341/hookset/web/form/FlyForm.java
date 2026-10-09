package edu.carroll.cs341.hookset.web.form;

import jakarta.validation.constraints.*;

public class FlyForm {
    @NotBlank(message = "Fly Name is required.")
    @Size(max = 150, message = "Fly Name must be shorter than 150 characters")
    private String flyName;

    @NotBlank(message = "Please select a fly type.")
    private String flyType;

    @NotBlank(message = "Please Select a fly color")
    @Size(max=40, message = "Fly Color must be shorter than 40 characters")
    private String flyColor;


    @NotNull(message = "Please select a minimum size.")
    @Min(value = 1, message = "Minimum size must be at least 1.")
    @Max(value = 32, message = "Minimum size cannot exceed 32.")
    private Integer minSize;


    @NotNull(message = "Please select a maximum size.")
    @Min(value = 1, message = "Maximum size must be at least 1.")
    @Max(value = 32, message = "Maximum size cannot exceed 32.")
    private Integer maxSize;

    @AssertTrue(message = "Minimum size cannot be greater than maximum size.")
    public boolean isSizeRangeValid() {
        if (minSize == null || maxSize == null) {
            return true;
        }

        return minSize <= maxSize;
    }

    public String getFlyName() {
        return flyName;
    }

    public void setFlyName(String flyName) {
        this.flyName = flyName;
    }

    public String getFlyType() {
        return flyType;
    }

    public void setFlyType(String flyType) {
        this.flyType = flyType;
    }

    public String getFlyColor() {
        return flyColor;
    }

    public void setFlyColor(String flyColor) {
        this.flyColor = flyColor;
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

}
