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
 *
 * <p>Spring Data turns a {@code null} user ID into an {@code IS NULL} check,
 * so passing {@code null} to a method that filters by user ID matches the
 * default flies instead of nothing. Callers should check for {@code null}
 * first.</p>
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
     * <p>A {@code null} user ID returns the default flies, not an empty list.</p>
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
     * Finds one user's custom fly by its URL slug.
     *
     * <p>A {@code null} user ID matches default flies instead, so callers looking
     * for a custom fly should check for {@code null} first.</p>
     *
     * @param slug the slug of the fly to look up
     * @param userId the ID of the user who owns the fly
     * @return the matching custom fly, or {@code null} if that user has none with this slug
     */
    Fly findBySlugAndUserId(String slug, Long userId);

    /**
     * Finds a default Hookset fly by its URL slug.
     *
     * @param slug the slug of the fly to look up
     * @return the matching default fly, or {@code null} if none exists
     */
    Fly findBySlugAndUserIdIsNull(String slug);
}