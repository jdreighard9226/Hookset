package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import entities.jpa.entity.Fish;

/**
 * Repository for reading and writing fish species records.
 *
 * <p>This repository gives the web application access to fish records through
 * the standard Spring Data JPA methods, plus a check used to stop the same
 * species from being added twice.</p>
 */
public interface FishRepository extends JpaRepository<Fish, Long> {

    /**
     * Checks whether a fish with the given family and species already exists.
     *
     * @param family the biological family to match
     * @param species the species name to match
     * @return {@code true} if a fish with that family and species exists, otherwise {@code false}
     */
    boolean existsByFamilyAndSpecies(String family, String species);
}