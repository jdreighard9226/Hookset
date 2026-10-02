package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;

import java.util.List;

/**
 * Defines the operations for looking up water bodies.
 *
 * <p>Implementations read water body records from the database and return
 * them as DTOs for the web layer.</p>
 */
public interface WaterBodyService {

    /**
     * Gets every water body in the database.
     *
     * @return the water bodies as DTOs, or an empty list if none exist
     */
    List<WaterBodyDto> getAllWaterBodies();

    /**
     * Gets a water body by its URL slug.
     *
     * @param waterBodySlug the slug used in the water body's URL
     * @return the matching water body as a DTO
     */
    WaterBodyDto getWaterBodyFromSlug(String waterBodySlug);
}