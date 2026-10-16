package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.form.FlyForm;
import shared.jpa.entity.Fly;

import java.util.List;

/**
 * Service for looking up, creating, editing, and deleting flies.
 *
 * <p>This interface defines the fly operations used by the web layer.
 * Default Hookset flies have no owning user and are shown to everyone,
 * while custom flies belong to the user who created them.</p>
 *
 * <p>The validation methods are public so the controller can call each one
 * and show a specific error message. {@link #addFly(FlyForm)} and
 * {@link #updateFly(Long, FlyForm)} run their checks again so bad data can't
 * be saved no matter who calls them.</p>
 */
public interface FlyService {

    /**
     * Returns all default flies along with the current user's custom flies.
     *
     * @return the combined list of flies, or an empty list if none exist
     */
    List<Fly> getAllFlies();

    /**
     * Returns all default Hookset flies.
     *
     * @return the list of default flies, or an empty list if none exist
     */
    List<Fly> getAllHooksetFlies();

    /**
     * Returns all custom flies created by the current user.
     *
     * @return the list of the user's custom flies, or an empty list if none exist
     */
    List<Fly> getAllUserFlies();

    /**
     * Returns a single fly by its URL slug.
     *
     * <p>Default flies are checked first, then the current user's custom flies.</p>
     *
     * @param flySlug the slug of the fly to look up
     * @return the matching fly, or {@code null} if none exists
     */
    Fly getFlyFromSlug(String flySlug);

    /**
     * Saves a new custom fly for the current user.
     *
     * @param flyForm the form containing the custom fly details
     * @return {@code true} if the fly was saved, otherwise {@code false}
     */
    boolean addFly(FlyForm flyForm);

    /**
     * Deletes a custom fly owned by the current user.
     *
     * @param flyId the ID of the fly to delete
     * @throws org.springframework.security.access.AccessDeniedException if the fly belongs to another user or is a default fly
     */
    void deleteFly(Long flyId);

    /**
     * Returns a single fly by its ID.
     *
     * <p>Default flies can be loaded by anyone. Custom flies can only be loaded
     * by the user who owns them.</p>
     *
     * @param flyId the ID of the fly to look up
     * @return the matching fly, or {@code null} if none exists
     * @throws org.springframework.security.access.AccessDeniedException if the fly belongs to another user
     */
    Fly getFlyById(Long flyId);

    /**
     * Checks that a fly's min size is not larger than its max size.
     *
     * @param flyForm the form containing the sizes to check
     * @return {@code true} if the size range is valid or either size is missing, otherwise {@code false}
     */
    boolean isSizeRangeValid(FlyForm flyForm);

    /**
     * Checks that a new fly name is not already used by a default fly or one of
     * the current user's custom flies.
     *
     * @param flyForm the form containing the fly name to check
     * @return {@code true} if the name is unique, otherwise {@code false}
     */
    boolean isFlyNameUnique(FlyForm flyForm);

    /**
     * Checks that an edited fly name is not already used by a default fly or
     * another of the current user's custom flies.
     *
     * <p>Keeping the fly's current name is always allowed.</p>
     *
     * @param flyId the ID of the fly being edited
     * @param flyForm the form containing the new fly name
     * @return {@code true} if the name is unique, otherwise {@code false}
     */
    boolean isFlyNameUniqueForEdit(Long flyId, FlyForm flyForm);

    /**
     * Checks whether a fly name is missing or only whitespace.
     *
     * @param flyName the fly name to check
     * @return {@code true} if the name is blank, otherwise {@code false}
     */
    boolean isFlyNameBlank(String flyName);

    /**
     * Updates a custom fly owned by the current user.
     *
     * @param flyId the ID of the fly to update
     * @param flyForm the form containing the new fly details
     * @return {@code true} if the fly was updated, otherwise {@code false}
     * @throws org.springframework.security.access.AccessDeniedException if the fly belongs to another user or is a default fly
     */
    boolean updateFly(Long flyId, FlyForm flyForm);
}