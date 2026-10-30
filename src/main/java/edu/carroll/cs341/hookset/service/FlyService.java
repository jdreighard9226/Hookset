package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.FlyInput;
import shared.jpa.entity.Fly;

import java.util.List;

/**
 * Service for looking up, creating, editing, and deleting flies.
 *
 * <p>Default Hookset flies have no owning user and are shown to everyone,
 * while custom flies belong to the user who created them. The user is always
 * passed in by the caller, so this service doesn't depend on how they logged in.</p>
 *
 * <p>The validation methods are public so callers can run each one and show
 * a specific error message. {@link #addFly(FlyInput, Long)} and
 * {@link #updateFly(Long, FlyInput, Long)} run their checks again so bad data
 * can't be saved no matter who calls them.</p>
 */
public interface FlyService {

    /**
     * Returns all default flies along with a user's custom flies.
     *
     * <p>A {@code null} user ID returns only the default flies.</p>
     *
     * @param userId the ID of the user whose custom flies to include
     * @return the combined list of flies, or an empty list if none exist
     */
    List<Fly> getAllFlies(Long userId);

    /**
     * Returns all default Hookset flies.
     *
     * @return the list of default flies, or an empty list if none exist
     */
    List<Fly> getAllHooksetFlies();

    /**
     * Returns all custom flies created by a user.
     *
     * @param userId the ID of the user whose flies to return
     * @return the list of the user's custom flies, or an empty list if the user ID is null or has none
     */
    List<Fly> getAllUserFlies(Long userId);

    /**
     * Returns a single fly by its URL slug.
     *
     * <p>Default flies are checked first, then the user's custom flies.</p>
     *
     * @param slug the slug of the fly to look up
     * @param userId the ID of the user whose custom flies can also match
     * @return the matching fly, or {@code null} if none exists
     */
    Fly getFlyFromSlug(String slug, Long userId);

    /**
     * Returns a single fly by its ID.
     *
     * <p>Default flies can be loaded by anyone. Custom flies can only be loaded
     * by the user who owns them.</p>
     *
     * @param id the ID of the fly to look up
     * @param userId the ID of the user asking for the fly
     * @return the matching fly, or {@code null} if none exists
     * @throws org.springframework.security.access.AccessDeniedException if the fly belongs to another user
     */
    Fly getFlyById(Long id, Long userId);

    /**
     * Saves a new custom fly for a user.
     *
     * @param flyInput the details of the new fly
     * @param userId the ID of the user adding the fly
     * @return {@code true} if the fly was saved, otherwise {@code false}
     */
    boolean addFly(FlyInput flyInput, Long userId);

    /**
     * Updates a custom fly owned by a user.
     *
     * @param flyId the ID of the fly to update
     * @param flyInput the new fly details
     * @param userId the ID of the user editing the fly
     * @return {@code true} if the fly was updated, otherwise {@code false}
     * @throws org.springframework.security.access.AccessDeniedException if the fly is a default fly or belongs to another user
     */
    boolean updateFly(Long flyId, FlyInput flyInput, Long userId);

    /**
     * Deletes a custom fly owned by a user.
     *
     * @param id the ID of the fly to delete
     * @param userId the ID of the user deleting the fly
     * @throws org.springframework.security.access.AccessDeniedException if the fly belongs to another user or is a default fly
     */
    void deleteFly(Long id, Long userId);

    /**
     * Checks that a fly's min size is not larger than its max size.
     *
     * @param minSize the smallest hook size for the fly
     * @param maxSize the largest hook size for the fly
     * @return {@code true} if the size range is valid or incomplete, otherwise {@code false}
     */
    boolean isSizeRangeValid(Integer minSize, Integer maxSize);

    /**
     * Checks that a new fly's name won't collide with a default fly or one of
     * the user's custom flies.
     *
     * @param name the name of the new fly
     * @param userId the ID of the user adding the fly
     * @return {@code true} if the name is unique, otherwise {@code false}
     */
    boolean isFlyNameUnique(String name, Long userId);

    /**
     * Checks that an edited fly's name won't collide with another fly the user can see.
     *
     * <p>The fly keeping its own name is allowed.</p>
     *
     * @param flyId the ID of the fly being edited
     * @param name the new name for the fly
     * @param userId the ID of the user editing the fly
     * @return {@code true} if the name is unchanged or unique, otherwise {@code false}
     */
    boolean isFlyNameUniqueForEdit(Long flyId, String name, Long userId);

    /**
     * Checks whether a fly name is missing or only whitespace.
     *
     * @param name the fly name to check
     * @return {@code true} if the name is blank, otherwise {@code false}
     */
    boolean isFlyNameBlank(String name);
}