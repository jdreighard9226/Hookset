package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.Catch;

import java.util.List;

/**
 * Repository for accessing catch records.
 *
 * <p>This interface provides standard CRUD operations for {@link Catch}
 * entities through Spring Data JPA, along with a lookup for all catches
 * logged by a specific user.</p>
 */
public interface CatchRepository extends JpaRepository<Catch, Long> {

    /**
     * Finds all catches logged by the given user.
     *
     * @param userId the ID of the user whose catches should be returned
     * @return the list of catches for that user, or an empty list if none exist
     */
    List<Catch> findByUserId(Long userId);
}