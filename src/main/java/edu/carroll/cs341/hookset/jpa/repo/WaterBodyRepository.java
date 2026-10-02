package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.WaterBody;

/**
 * Repository for reading and writing water body records.
 *
 * <p>Water bodies are loaded into the database by the importers
 * application. Besides the standard Spring Data JPA methods, this
 * repository provides a lookup by URL slug so pages can find a water
 * body from its address.</p>
 */
public interface WaterBodyRepository extends JpaRepository<WaterBody, Long> {

    /**
     * Finds a water body by its URL slug.
     *
     * @param waterBodySlug the slug used in the water body's URL
     * @return the matching water body, or {@code null} if none exists
     */
    WaterBody getWaterBodyByWaterBodySlug(String waterBodySlug);
}