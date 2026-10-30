package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.CatchInput;
import shared.jpa.entity.Catch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for logging and retrieving catches.
 *
 * <p>This interface defines the catch operations, including looking up all
 * catches, looking up one user's catches, and saving or editing catches for
 * a user. The user is always passed in by the caller, so the service doesn't
 * depend on how they logged in.</p>
 *
 * <p>The validation methods are public so callers can run each one and show
 * a specific error message. {@link #addCatch(CatchInput, Long)} and
 * {@link #updateCatch(Long, CatchInput, Long)} run them again so bad data
 * can't be saved no matter who calls them.</p>
 */
public interface CatchService {

    /**
     * Returns every catch logged in Hookset.
     *
     * @return the list of all catches, or an empty list if none exist
     */
    List<Catch> getAllCatches();

    /**
     * Returns every catch logged by a user.
     *
     * @param userId the ID of the user whose catches to return
     * @return the list of the user's catches, or an empty list if the user ID is null or has none
     */
    List<Catch> getAllUserCatches(Long userId);

    /**
     * Returns a single catch by its ID.
     *
     * @param catchId the ID of the catch to look up
     * @return the matching catch, or {@code null} if none exists
     */
    Catch getCatchById(Long catchId);

    /**
     * Saves a new catch for a user.
     *
     * <p>The owner always comes from the user ID, never from the input.</p>
     *
     * @param catchInput the details of the new catch
     * @param userId the ID of the user logging the catch
     * @return {@code true} if the catch was saved, otherwise {@code false}
     */
    boolean addCatch(CatchInput catchInput, Long userId);

    /**
     * Updates a catch owned by a user.
     *
     * <p>Ownership is checked before the input, so bad input can't reveal
     * whether another user's catch exists.</p>
     *
     * @param catchId the ID of the catch to update
     * @param catchInput the new catch details
     * @param userId the ID of the user editing the catch
     * @return {@code true} if the catch was updated, otherwise {@code false}
     */
    boolean updateCatch(Long catchId, CatchInput catchInput, Long userId);

    /**
     * Checks whether a catch exists and belongs to a user.
     *
     * @param catchId the ID of the catch to check
     * @param userId the ID of the user to check ownership against
     * @return {@code true} if the catch exists and the user logged it, otherwise {@code false}
     */
    boolean isCatchOwner(Long catchId, Long userId);

    /**
     * Checks whether a fly exists and can be used by a user.
     *
     * @param flyId the ID of the fly to check
     * @param userId the ID of the user who wants to use the fly
     * @return {@code true} if the fly is a default fly or belongs to the user, otherwise {@code false}
     */
    boolean isFlyValid(Long flyId, Long userId);

    /**
     * Checks whether a fish species exists.
     *
     * @param fishId the ID of the fish to check
     * @return {@code true} if the fish exists, otherwise {@code false}
     */
    boolean isFishValid(Long fishId);

    /**
     * Checks whether a water body exists.
     *
     * @param waterBodyId the ID of the water body to check
     * @return {@code true} if the water body exists, otherwise {@code false}
     */
    boolean isWaterBodyValid(Long waterBodyId);

    /**
     * Checks whether a fish species is found in a water body.
     *
     * @param fishId the ID of the fish to check
     * @param waterBodyId the ID of the water body to check
     * @return {@code true} if the fish is found in the water body, otherwise {@code false}
     */
    boolean isFishInWaterBody(Long fishId, Long waterBodyId);

    /**
     * Checks whether a fish length is valid.
     *
     * <p>Length is optional, so {@code null} is valid.</p>
     *
     * @param fishLength the fish length in inches
     * @return {@code true} if the length is missing or between 0.01 and 999.99 with at most two decimals, otherwise {@code false}
     */
    boolean isFishLengthValid(BigDecimal fishLength);

    /**
     * Checks whether a catch date is present and not in the future.
     *
     * @param dateCaught the date and time of the catch
     * @return {@code true} if the date is present and not in the future, otherwise {@code false}
     */
    boolean isDateCaughtValid(LocalDateTime dateCaught);

    /**
     * Checks whether catch notes are within the length limit.
     *
     * <p>Notes are optional, so {@code null} is valid.</p>
     *
     * @param notes the catch notes
     * @return {@code true} if the notes are missing or 1000 characters or fewer, otherwise {@code false}
     */
    boolean isNotesValid(String notes);
}