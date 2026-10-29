package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.CatchInput;
import edu.carroll.cs341.hookset.web.form.CatchForm;
import shared.jpa.entity.Catch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for logging and retrieving catches.
 *
 * <p>This interface defines the catch operations used by the web layer,
 * including looking up all catches, looking up the logged-in user's catches,
 * and saving or editing catches for the logged-in user.</p>
 *
 * <p>The validation methods are public so the controller can call each one
 * and show a specific error message. {@link #addCatch(CatchForm)} and
 * {@link #updateCatch(Long, CatchForm)} run them again so bad data can't be
 * saved no matter who calls them.</p>
 */
public interface CatchService {

    /**
     * Returns every catch logged in Hookset.
     *
     * @return the list of all catches, or an empty list if none exist
     */
    List<Catch> getAllCatches();

    /**
     * Returns every catch logged by the current user.
     *
     * @return the list of the user's catches, or an empty list if none exist
     */
    List<Catch> getAllUserCatches(Long userId);

    /**
     * Returns a single catch by its ID.
     *
     * @param catchId the ID of the catch to look up
     * @return the matching catch, or {@code null} if none exists
     */
    Catch getCatchById(Long catchId);

     boolean addCatch(CatchInput catchInput, Long userId);


    boolean updateCatch(Long catchId, CatchInput catchInput, Long userId);

    /**
     * Checks whether a catch exists and belongs to the current user.
     *
     * @param catchId the ID of the catch to check
     * @return {@code true} if the catch exists and the current user logged it, otherwise {@code false}
     */
     boolean isCatchOwner(Long catchId, Long userId);

    /**
     * Checks whether a fly exists and can be used by the current user.
     *
     * @param flyId the ID of the fly to check
     * @return {@code true} if the fly is a default fly or belongs to the current user, otherwise {@code false}
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