package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.Fish;

/**
 * Repository for reading and writing fish species records.
 *
 * <p>Fish records are loaded into the database by the importers
 * application. This repository gives the web application access to
 * them through the standard Spring Data JPA methods.</p>
 */
public interface FishRepository extends JpaRepository<Fish, Long> {
}