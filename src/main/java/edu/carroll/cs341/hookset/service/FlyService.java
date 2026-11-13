package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.FlyInput;
import entities.jpa.entity.Fly;

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
     * <p>Default flies are checked first, then the user's custom flies. Other
     * users' custom flies are never searched.</p>
     *
     * @param slug the slug of the fly to look up
     * @param userId the ID of the user whose custom flies can also match
     * @return the matching fly, or {@code null} if no default fly or custom fly of this user matches
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
     * @return the matching fly, or {@code null} if the ID is null or no fly has that ID
     * @throws org.springframework.security.access.AccessDeniedException if the fly belongs to another user
     */
    Fly getFlyById(Long id, Long userId);

    /**
     * Saves a new custom fly for a user.
     *
     * <p>The owner, slug, and image are set by the service, never taken from the input.</p>
     *
     * @param flyInput the details of the new fly
     * @param userId the ID of the user adding the fly
     * @return {@code true} if the fly was saved, otherwise {@code false} if the user ID is null,
     *         the input fails validation, or the name matches a fly the user can already see
     */
    boolean addFly(FlyInput flyInput, Long userId);

    /**
     * Updates a custom fly owned by a user.
     *
     * <p>Ownership is checked before the input, so input errors are never
     * reported for a fly the user doesn't own. The owner is never changed.</p>
     *
     * @param flyId the ID of the fly to update
     * @param flyInput the new fly details
     * @param userId the ID of the user editing the fly
     * @return {@code true} if the fly was updated, otherwise {@code false} if the fly ID is null,
     *         the fly does not exist, the input fails validation, or the new name matches another fly the user can see
     * @throws org.springframework.security.access.AccessDeniedException if the fly is a default fly or belongs to another user
     */
    boolean updateFly(Long flyId, FlyInput flyInput, Long userId);

    /**
     * Deletes a custom fly owned by a user.
     *
     * <p>A fly that is used by any catch can't be deleted, since removing it
     * would leave those catches pointing at a fly that no longer exists.</p>
     *
     * @param id the ID of the fly to delete
     * @param userId the ID of the user deleting the fly
     * @return {@code true} if the fly was deleted, otherwise {@code false} if the fly ID is null,
     *         the fly does not exist, or the fly is used by a catch
     * @throws org.springframework.security.access.AccessDeniedException if the fly belongs to another user or is a default fly
     */
    boolean deleteFly(Long id, Long userId);

    /**
     * Checks whether any catch was logged with a fly.
     *
     * @param flyId the ID of the fly to check
     * @return {@code true} if at least one catch uses the fly, otherwise {@code false}, including when the ID is null
     */
    boolean isFlyUsedByCatch(Long flyId);

    /**
     * Checks that a fly's min size is not larger than its max size.
     *
     * <p>If either size is missing there is nothing to compare, so this
     * returns {@code true}. Missing sizes are caught by the other size checks.</p>
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
     * <p>Names are compared by slug, so names that only differ by case or
     * spacing count as duplicates. Other users' custom flies are not checked,
     * so two users can each have a fly with the same name.</p>
     *
     * @param name the name of the new fly
     * @param userId the ID of the user adding the fly
     * @return {@code true} if no visible fly has the same slug, otherwise {@code false},
     *         including when the name is blank
     */
    boolean isFlyNameUnique(String name, Long userId);

    /**
     * Checks that an edited fly's name won't collide with another fly the user can see.
     *
     * <p>The fly keeping its own name is allowed. Otherwise this uses the same
     * slug rules as {@link #isFlyNameUnique(String, Long)}.</p>
     *
     * @param flyId the ID of the fly being edited
     * @param name the new name for the fly
     * @param userId the ID of the user editing the fly
     * @return {@code true} if the name is unchanged or no other visible fly has the same slug, otherwise {@code false},
     *         including when the ID is null, the fly does not exist, or the name is blank
     */
    boolean isFlyNameUniqueForEdit(Long flyId, String name, Long userId);

    /**
     * Checks whether a fly name is missing or only whitespace.
     *
     * @param name the fly name to check
     * @return {@code true} if the name is blank, otherwise {@code false}
     */
    boolean isFlyNameBlank(String name);

    /**
     * Checks whether a fly color is missing or only whitespace.
     *
     * @param flyColor the fly color to check
     * @return {@code true} if the color is null, empty, or only whitespace, otherwise {@code false}
     */
    boolean isFlyColorBlank(String flyColor);

    /**
     * Checks whether a fly color is between 3 and 40 characters long.
     *
     * <p>The color is trimmed first, so spaces around it don't count.</p>
     *
     * @param flyColor the fly color to check
     * @return {@code true} if the trimmed color is between 3 and 40 characters, otherwise {@code false}
     */
    boolean isFlyColorLengthValid(String flyColor);

    /**
     * Checks that a fly color isn't made up entirely of digits.
     *
     * <p>A color like "olive" or "black 2" passes. A color like "123" fails.</p>
     *
     * @param flyColor the fly color to check
     * @return {@code true} if the color is not blank and has at least one character that isn't a digit, otherwise {@code false}
     */
    boolean isFlyColorValidString(String flyColor);

    /**
     * Checks whether a minimum hook size is between 1 and 32.
     *
     * @param minSize the minimum hook size to check
     * @return {@code true} if the size is not null and between 1 and 32, otherwise {@code false}
     */
    boolean isMinSizeValidNumber(Integer minSize);

    /**
     * Checks whether a maximum hook size is between 1 and 32.
     *
     * @param maxSize the maximum hook size to check
     * @return {@code true} if the size is not null and between 1 and 32, otherwise {@code false}
     */
    boolean isMaxSizeValidNumber(Integer maxSize);

    /**
     * Checks whether a fly type is "Dry", "Nymph", or "Streamer".
     *
     * <p>The match is exact and case-sensitive, so "dry" is not allowed.</p>
     *
     * @param flyType the fly type to check
     * @return {@code true} if the type exactly matches one of the allowed fly types, otherwise {@code false}
     */
    boolean isFlyTypeValid(String flyType);

    /**
     * Checks whether a fly name is between 1 and 150 characters long.
     *
     * <p>The name is trimmed first, so a name of only spaces counts as 0 characters.</p>
     *
     * @param name the fly name to check
     * @return {@code true} if the trimmed name is between 1 and 150 characters, otherwise {@code false}
     */
    boolean isFlyNameLengthValid(String name);

    /**
     * Checks whether a fly exists and is a custom fly that belongs to a user.
     *
     * <p>Default flies have no owner, so this is always {@code false} for them.</p>
     *
     * @param flyId the ID of the fly to check
     * @param userId the ID of the user to check ownership against
     * @return {@code true} if the fly exists and the user made it, otherwise {@code false}
     */
    boolean isFlyOwner(Long flyId, Long userId);
}