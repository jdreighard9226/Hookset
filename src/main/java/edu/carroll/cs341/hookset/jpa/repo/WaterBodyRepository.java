package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import entities.jpa.entity.WaterBody;

/**
 * Repository for reading and writing water body records.
 *
 * <p>Besides the standard Spring Data JPA methods, this repository provides a
 * lookup by URL slug so pages can find a water body from its address, and a
 * check used to stop two water bodies from sharing a slug.</p>
 */
public interface WaterBodyRepository extends JpaRepository<WaterBody, Long> {

    /**
     * Finds a water body by its URL slug.
     *
     * @param slug the slug used in the water body's URL
     * @return the matching water body, or {@code null} if none exists
     */
    WaterBody getWaterBodyBySlug(String slug);

    /**
     * Checks whether a water body with the given slug already exists.
     *
     * @param slug the slug to match
     * @return {@code true} if a water body with that slug exists, otherwise {@code false}
     */
    boolean existsBySlug(String slug);
}