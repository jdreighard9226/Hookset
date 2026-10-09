package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FlyRepository;
import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import edu.carroll.cs341.hookset.web.mapper.FlyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import shared.jpa.entity.Fly;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for looking up, creating, and deleting flies.
 *
 * <p>Hookset has two kinds of flies. Default flies have no owning user and
 * are shown to everyone. Custom flies belong to the user who created them
 * and are only shown to that user.</p>
 *
 * <p>This service also validates new custom flies, generates their URL slugs,
 * and assigns an image based on the fly type.</p>
 */
@Service
public class FlyServiceImpl implements FlyService {

    private static final Logger log = LoggerFactory.getLogger(FlyServiceImpl.class);

    /** The repository used to access fly records. */
    private final FlyRepository flyRepository;

    /** The mapper used to convert between fly entities, DTOs, and forms. */
    private final FlyMapper flyMapper;

    /**
     * Creates the fly service with the repository and mapper it depends on.
     *
     * @param flyRepository the repository used to access fly records
     * @param flyMapper the mapper used to convert between fly entities, DTOs, and forms
     */
    public FlyServiceImpl(FlyRepository flyRepository, FlyMapper flyMapper) {
        this.flyRepository = flyRepository;
        this.flyMapper = flyMapper;
    }

    /**
     * Returns all default flies along with the current user's custom flies.
     *
     * @return the combined list of flies as DTOs
     */
    @Override
    public List<FlyDto> getAllFlies() {
        Long userId = getCurrentUserId();

        log.info("Getting default and custom flies for user ID: {}", userId);

        List<Fly> flies = flyRepository.findByUserIdIsNullOrUserId(userId);
        return mapAllFlyDtos(flies);
    }

    /**
     * Returns all default Hookset flies.
     *
     * @return the list of default flies as DTOs
     */
    @Override
    public List<FlyDto> getAllHooksetFlies() {
        log.info("Getting default Hookset flies");

        List<Fly> flies = flyRepository.findByUserIdIsNull();
        return mapAllFlyDtos(flies);
    }

    /**
     * Returns all custom flies created by the current user.
     *
     * @return the list of the user's custom flies as DTOs
     */
    @Override
    public List<FlyDto> getAllUserFlies() {
        Long userId = getCurrentUserId();

        log.info("Getting custom flies for user ID: {}", userId);

        List<Fly> flies = flyRepository.findByUserId(userId);
        return mapAllFlyDtos(flies);
    }

    /**
     * Returns a single fly by its URL slug.
     *
     * @param flySlug the slug of the fly to look up
     * @return the matching fly as a DTO, or {@code null} if none exists
     */
    @Override
    public FlyDto getFlyFromSlug(String flySlug) {
        log.info("Looking up fly with slug: {}", flySlug);

        Fly fly = flyRepository.findByFlySlug(flySlug);

        if (fly == null) {
            log.warn("Fly not found with slug: {}", flySlug);
            return null;
        }

        return flyMapper.toDto(fly);
    }

    /**
     * Saves a new custom fly for the current user.
     *
     * <p>The slug is generated from the fly name, and the image is picked
     * from the fly type. Any type other than dry fly or streamer uses the
     * nymph image.</p>
     *
     * @param flyForm the form containing the custom fly details
     */
    @Override
    public void addFly(FlyForm flyForm) {
        Long userId = getCurrentUserId();

        Fly fly = flyMapper.toEntity(flyForm);
        fly.setUserId(userId);
        fly.setFlySlug(generateFlySlug(fly.getFlyName()));

        log.info("Adding custom fly: {} for user ID: {}", fly.getFlyName(), userId);

        String flyType = fly.getFlyType();

        if (flyType.equals("Dry Fly")) {
            fly.setFlyImage("dry.png");
        } else if (flyType.equals("Streamer")) {
            fly.setFlyImage("streamer.png");
        } else {
            fly.setFlyImage("nymph.png");
        }

        flyRepository.save(fly);

        log.info("Custom fly saved with slug: {}", fly.getFlySlug());
    }

    /**
     * Checks a submitted fly form for errors that annotations can't catch.
     *
     * <p>Rejects the form if the minimum size is larger than the maximum size,
     * or if the current user already has a fly with the same name. Errors are
     * added to the binding result so the form can show them.</p>
     *
     * @param flyForm the form to validate
     * @param result the binding result to add any errors to
     */
    @Override
    public void validateFly(FlyForm flyForm, BindingResult result) {
        if (flyForm.getMinSize() != null
                && flyForm.getMaxSize() != null) {

            if (flyForm.getMinSize() > flyForm.getMaxSize()) {
                log.warn("Fly validation failed. Min size {} exceeds max size {}",
                        flyForm.getMinSize(), flyForm.getMaxSize());

                result.rejectValue(
                        "minSize",
                        "invalid.size",
                        "Minimum size cannot exceed maximum size"
                );
            }
        }

        Long userId = getCurrentUserId();

        if (flyRepository.existsByUserIdAndFlyNameIgnoreCase(
                userId, flyForm.getFlyName().trim())) {
            log.warn("Fly validation failed. Duplicate fly name: {} for user ID: {}",
                    flyForm.getFlyName().trim(), userId);

            result.rejectValue(
                    "flyName",
                    "duplicate.flyName",
                    "You already have a fly with this name"
            );
        }
    }

    /**
     * Deletes a custom fly owned by the current user.
     *
     * @param flyId the ID of the fly to delete
     * @throws AccessDeniedException if the fly belongs to another user or is a default fly
     */
    public void deleteFly(Long flyId) {
        Long userId = getCurrentUserId();

        log.info("Deleting fly with ID: {} for user ID: {}", flyId, userId);

        Fly fly = flyRepository.findById(flyId).orElse(null);

        if (fly == null) {
            log.warn("Fly not found with ID: {}", flyId);
            return;
        }

        // default flies have a null user ID, so this also blocks deleting them
        if (!userId.equals(fly.getUserId())) {
            log.warn("Delete denied. User ID: {} does not own fly ID: {}", userId, flyId);
            throw new AccessDeniedException("You cannot delete this fly");
        }

        flyRepository.delete(fly);

        log.info("Fly deleted with ID: {}", flyId);
    }

    /**
     * Gets the ID of the currently logged-in user.
     *
     * @return the ID of the logged-in user, or {@code null} if no user is logged in
     */
    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.getPrincipal() instanceof HooksetUserDetails userDetails) {
            return userDetails.getUserId();
        }

        return null;
    }

    /**
     * Converts a list of fly entities into fly DTOs.
     *
     * @param flies the fly entities to convert
     * @return the list of fly DTOs
     */
    private List<FlyDto> mapAllFlyDtos(List<Fly> flies) {
        if (flies.isEmpty()) {
            log.info("No flies found");
        } else {
            log.info("Found {} flies", flies.size());
        }

        List<FlyDto> flyDtos = new ArrayList<>();

        for (Fly fly : flies) {
            flyDtos.add(flyMapper.toDto(fly));
        }

        return flyDtos;
    }

    /**
     * Generates a URL slug from a fly name.
     *
     * <p>Trims the name, lowercases it, and replaces runs of whitespace with
     * a single hyphen. For example, "Parachute Adams" becomes "parachute-adams".</p>
     *
     * @param flyName the fly name to build the slug from
     * @return the generated slug
     */
    private String generateFlySlug(String flyName) {
        return flyName.trim().toLowerCase().replaceAll("\\s+", "-");
    }
}