package edu.carroll.cs341.hookset.service;

import shared.jpa.entity.Fish;

import java.util.List;

/**
 * Service for looking up fish species.
 *
 * <p>Fish are read-only, since they come from the seed script rather than
 * from users. The fish found in a single water body are loaded through
 * {@link WaterBodyService#getAllFishForWaterBody(Long)}, since that lookup
 * goes through the water body.</p>
 */
public interface FishService {

    /**
     * Returns every fish species in the database.
     *
     * @return the list of fish species, or an empty list if none exist
     */
    List<Fish> getAllFish();
}