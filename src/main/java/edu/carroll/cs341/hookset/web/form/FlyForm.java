package edu.carroll.cs341.hookset.web.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Holds the data submitted from the add and edit fly forms.
 *
 * <p>This form backs the pages where users create and edit custom flies. Each
 * field is validated with Jakarta annotations before the fly service runs its
 * own business rule checks.</p>
 */
public class FlyForm {

    /** The ID of the fly being edited, used by the edit page to build its POST URL. Empty when adding a new fly. */
    private Long id;

    /** The name of the fly. */
    @NotBlank(message = "Fly name is required.")
    @Size(max = 150, message = "Fly name cannot exceed 150 characters.")
    private String name;

    /** The type of fly, which must be "Dry", "Nymph", or "Streamer". */
    @NotBlank(message = "Please select a fly type.")
    private String type;

    /** The color of the fly, typed in by the user. */
    @NotBlank(message = "Please enter a fly color.")
    @Size(max = 40, message = "Fly color cannot exceed 40 characters.")
    @Size(min = 3, message = "Fly color must be at least 3 characters.")
    private String color;

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
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the fly.
     *
     * @param name the fly name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of fly.
     *
     * @return the fly type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of fly.
     *
     * @param type the fly type
     */
    public void setType(String type) {
        this.type = type;
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
     * Sets the color of the fly.
     *
     * @param color the fly color
     */
    public void setColor(String color) {
        this.color = color;
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
     * Returns the ID of the fly being edited.
     *
     * @return the fly ID, or {@code null} when adding a new fly
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the ID of the fly being edited.
     *
     * @param id the fly ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Checks that the minimum size is not larger than the maximum size.
     *
     * <p>Returns {@code true} when either size is missing so the {@code @NotNull}
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