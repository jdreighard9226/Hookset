package edu.carroll.cs341.hookset.service;

import shared.jpa.entity.Fish;

import java.util.List;

/**
 * Service for looking up fish species.
 *
 * <p>Fish can be looked up as a full list or filtered to the species found
 * in a single water body.</p>
 */
public interface FishService {

    /**
     * Returns every fish species in the database.
     *
     * @return the list of fish species, or an empty list if none exist
     */
    List<Fish> getAllFish();

    /**
     * Returns every fish species found in a water body.
     *
     * @param waterBodyId the ID of the water body
     * @return the list of fish species in the water body, or an empty list if none exist
     */
    List<Fish> getAllFishForWaterBody(Long waterBodyId);
}