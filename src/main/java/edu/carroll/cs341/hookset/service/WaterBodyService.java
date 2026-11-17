package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.WaterBodyInput;
import entities.jpa.entity.Fish;
import entities.jpa.entity.WaterBody;

import java.util.List;

/**
 * Service for looking up and adding water bodies and the fish found in them.
 *
 * <p>Water bodies can be looked up as a full list or individually by their
 * URL slug. The fish in a single water body are loaded here too, since that
 * lookup goes through the water body.</p>
 */
public interface WaterBodyService {

    /**
     * Returns every water body in the database.
     *
     * @return the list of water bodies, or an empty list if none exist
     */
    List<WaterBody> getAllWaterBodies();

    /**
     * Returns a single water body by its URL slug.
     *
     * @param waterBodySlug the slug used in the water body's URL
     * @return the matching water body, or {@code null} if the slug is blank or no water body matches
     */
    WaterBody getWaterBodyFromSlug(String waterBodySlug);

    /**
     * Returns every fish species found in a water body.
     *
     * @param waterBodyId the ID of the water body
     * @return the list of fish species in the water body, or an empty list if the ID is null,
     *         the water body does not exist, or it has no fish
     */
    List<Fish> getAllFishForWaterBody(Long waterBodyId);

    /**
     * Adds a new water body, linked to the fish found in it.
     *
     * <p>This method was added specifically for testing. Tests can only depend on
     * services, not repositories, so this gives them a way to create water body
     * test data. The app itself does not add water bodies yet, which is also why
     * the caller provides the slug instead of it being generated.</p>
     *
     * <p>The name, type, state, description, and slug are all required and can't be
     * blank. The name can be at most 250 characters, the state 100, the description
     * 1000, and the slug 100. The type must be exactly River, Lake, or Stream. The
     * slug is saved in lowercase and can't match an existing water body's slug.</p>
     *
     * <p>The fish IDs are optional, so a water body can be added with no fish. If any
     * fish ID is null or doesn't match a fish, the water body is not added.</p>
     *
     * @param waterBodyInput the details of the water body to add
     * @return {@code true} if the water body was saved, otherwise {@code false}
     */
    boolean addWaterBody(WaterBodyInput waterBodyInput);
}