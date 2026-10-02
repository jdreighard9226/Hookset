package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.dto.FishDto;

import java.util.List;

/**
 * Defines the operations for looking up fish species.
 *
 * <p>Implementations read fish records from the database and return them
 * as DTOs for the web layer.</p>
 */
public interface FishService {

    /**
     * Gets every fish species in the database.
     *
     * @return the fish species as DTOs, or an empty list if none exist
     */
    List<FishDto> getAllFish();

    /**
     * Gets every fish species found in a water body.
     *
     * @param waterBodyId the identifier of the water body
     * @return the fish species in the water body as DTOs, or an empty list if none exist
     */
    List<FishDto> getAllFishForWaterBody(Long waterBodyId);
}