package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.FishInput;
import entities.jpa.entity.Fish;

import java.util.List;

/**
 * Service for looking up and adding fish species.
 *
 * <p>Fish aren't owned by any user, so every user sees the same list. The fish
 * found in a single water body are loaded through
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

    /**
     * Adds a new fish species.
     *
     * <p>This method was added specifically for testing. Tests can only depend on
     * services, not repositories, so this gives them a way to create fish test data.
     * The app itself does not add fish yet.</p>
     *
     * <p>The family, species, and image are all required and can't be blank.
     * The family and species can be at most 200 characters, and the image at
     * most 100. A fish with the same family and species as an existing fish
     * is not added.</p>
     *
     * @param fishInput the details of the fish to add
     * @return {@code true} if the fish was saved, otherwise {@code false}
     */
    boolean addFish(FishInput fishInput);
}