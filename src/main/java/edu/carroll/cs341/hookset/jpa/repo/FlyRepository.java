package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.Fly;

import java.util.List;

/**
 * Repository for accessing fly records.
 *
 * <p>This interface provides standard CRUD operations for {@link Fly}
 * entities through Spring Data JPA. Default Hookset flies have no owning
 * user, while custom flies are tied to the user who created them.</p>
 */
public interface FlyRepository extends JpaRepository<Fly, Long> {

    /**
     * Finds all default Hookset flies.
     *
     * @return the list of flies with no owning user
     */
    List<Fly> findByUserIdIsNull();

    /**
     * Finds all custom flies created by the given user.
     *
     * @param userId the ID of the user whose custom flies should be returned
     * @return the list of that user's custom flies, or an empty list if none exist
     */
    List<Fly> findByUserId(Long userId);

    /**
     * Finds all default flies along with the given user's custom flies.
     *
     * @param userId the ID of the user whose custom flies should be included
     * @return the combined list of default and user-created flies
     */
    List<Fly> findByUserIdIsNullOrUserId(Long userId);

    /**
     * Finds a fly by its URL slug.
     *
     * @param flySlug the slug of the fly to look up
     * @return the matching fly, or {@code null} if none exists
     */
    Fly findByFlySlug(String flySlug);

    /**
     * Checks whether the given user already has a custom fly with the given name.
     *
     * <p>The name comparison ignores case so duplicates like "Adams" and
     * "adams" are caught.</p>
     *
     * @param userId the ID of the user who owns the fly
     * @param flyName the fly name to check
     * @return {@code true} if a matching fly exists, otherwise {@code false}
     */
    boolean existsByUserIdAndFlyNameIgnoreCase(Long userId, String flyName);
}