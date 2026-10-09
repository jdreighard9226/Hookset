package edu.carroll.cs341.hookset.web.form;

import jakarta.validation.constraints.*;

/**
 * Holds the data submitted from the add fly form.
 *
 * <p>This form backs the page where users create custom flies. Each field is
 * validated with Jakarta annotations before the fly service runs its own
 * business rule checks.</p>
 */
public class FlyForm {

    /** The name of the fly. */
    @NotBlank(message = "Fly name is required.")
    @Size(max = 150, message = "Fly name cannot exceed 150 characters.")
    private String flyName;

    /** The type of fly, such as dry fly, nymph, or streamer. */
    @NotBlank(message = "Please select a fly type.")
    private String flyType;

    /** The color of the fly. */
    @NotBlank(message = "Please select a fly color.")
    @Size(max = 40, message = "Fly color cannot exceed 40 characters.")
    private String flyColor;

    /** The minimum hook size the fly is tied in. */
    @NotNull(message = "Please select a minimum size.")
    @Min(value = 1, message = "Minimum size must be at least 1.")
    @Max(value = 32, message = "Minimum size cannot exceed 32.")
    private Integer minSize;

    /** The maximum hook size the fly is tied in. */
    @NotNull(message = "Please select a maximum size.")
    @Min(value = 1, message = "Maximum size must be at least 1.")
    @Max(value = 32, message = "Maximum size cannot exceed 32.")
    private Integer maxSize;

    /**
     * Returns the name of the fly.
     *
     * @return the fly name
     */
    public String getFlyName() {
        return flyName;
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
     * Returns the type of fly.
     *
     * @return the fly type
     */
    public String getFlyType() {
        return flyType;
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
     * Returns the color of the fly.
     *
     * @return the fly color
     */
    public String getFlyColor() {
        return flyColor;
    }

    /**
     * Sets the color of the fly.
     *
     * @param flyColor the fly color
     */
    public void setFlyColor(String flyColor) {
        this.flyColor = flyColor;
    }

    /**
     * Returns the minimum hook size the fly is tied in.
     *
     * @return the minimum size
     */
    public Integer getMinSize() {
        return minSize;
    }

    /**
     * Sets the minimum hook size the fly is tied in.
     *
     * @param minSize the minimum size
     */
    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    /**
     * Returns the maximum hook size the fly is tied in.
     *
     * @return the maximum size
     */
    public Integer getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maximum hook size the fly is tied in.
     *
     * @param maxSize the maximum size
     */
    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Checks that the minimum size is not larger than the maximum size.
     *
     * <p>Returns true when either size is missing so the {@code @NotNull}
     * checks report that error instead.</p>
     *
     * @return {@code true} if the size range is valid or incomplete, otherwise {@code false}
     */
    @AssertTrue(message = "Minimum size cannot be greater than maximum size.")
    public boolean isSizeRangeValid() {
        if (minSize == null || maxSize == null) {
            return true;
        }

        return minSize <= maxSize;
    }
}