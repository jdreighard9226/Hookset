package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.dto.CatchDto;
import edu.carroll.cs341.hookset.web.form.CatchForm;

import java.util.List;

/**
 * Service for logging and retrieving catches.
 *
 * <p>This interface defines the catch operations used by the web layer,
 * including looking up all catches, looking up the logged-in user's catches,
 * and saving new catches for the logged-in user.</p>
 */
public interface CatchService {

    /**
     * Returns every catch logged in Hookset.
     *
     * @return the list of all catches as DTOs, or an empty list if none exist
     */
    List<CatchDto> getAllCatches();

    /**
     * Returns every catch logged by the current user.
     *
     * @return the list of the user's catches as DTOs, or an empty list if none exist
     */
    List<CatchDto> getAllUserCatches();

    /**
     * Returns a single catch by its ID.
     *
     * @param catchId the ID of the catch to look up
     * @return the matching catch as a DTO, or {@code null} if none exists
     */
    CatchDto getCatchByCatchId(int catchId);

    /**
     * Saves a new catch for the current user.
     *
     * @param catchForm the form containing the catch details
     * @return {@code true} if the catch was saved, otherwise {@code false}
     */
    boolean addNewCatch(CatchForm catchForm);
}