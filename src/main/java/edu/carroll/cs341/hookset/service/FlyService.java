package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import org.springframework.validation.BindingResult;
import shared.jpa.entity.Fly;

import java.util.List;

/**
 * Service for looking up, creating, and deleting flies.
 *
 * <p>This interface defines the fly operations used by the web layer.
 * Default Hookset flies have no owning user and are shown to everyone,
 * while custom flies belong to the user who created them.</p>
 */
public interface FlyService {

    /**
     * Returns all default flies along with the current user's custom flies.
     *
     * @return the combined list of flies as DTOs
     */
    List<Fly> getAllFlies();

    /**
     * Returns all default Hookset flies.
     *
     * @return the list of default flies as DTOs
     */
    List<Fly> getAllHooksetFlies();

    /**
     * Returns all custom flies created by the current user.
     *
     * @return the list of the user's custom flies as DTOs
     */
    List<Fly> getAllUserFlies();

    /**
     * Returns a single fly by its URL slug.
     *
     * @param flySlug the slug of the fly to look up
     * @return the matching fly as a DTO, or {@code null} if none exists
     */
    Fly getFlyFromSlug(String flySlug);

    /**
     * Saves a new custom fly for the current user.
     *
     * @param flyForm the form containing the custom fly details
     */
    void addFly(FlyForm flyForm);


    /**
     * Deletes a custom fly owned by the current user.
     *
     * @param flyId the ID of the fly to delete
     */
    void deleteFly(Long flyId);

    FlyDto getFlyById(Long flyId);

    boolean isSizeRangeValid(FlyForm flyForm);

    boolean isFlyNameUnique(FlyForm flyForm);
    boolean isFlyNameUniqueForEdit(Long flyId, FlyForm flyForm);
    boolean updateFly(Long flyId, FlyForm flyForm);

}