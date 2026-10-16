package edu.carroll.cs341.hookset.service;

import shared.jpa.entity.WaterBody;

import java.util.List;

/**
 * Service for looking up water bodies.
 *
 * <p>Water bodies can be looked up as a full list or individually by their
 * URL slug.</p>
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
}