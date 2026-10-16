package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FlyRepository;
import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import edu.carroll.cs341.hookset.web.mapper.FlyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import shared.jpa.entity.Fly;

import java.util.List;

/**
 * Service implementation for looking up, creating, editing, and deleting flies.
 *
 * <p>Hookset has two kinds of flies. Default flies have no owning user and
 * are shown to everyone. Custom flies belong to the user who created them
 * and are only shown to that user.</p>
 *
 * <p>This service also validates custom flies, generates their URL slugs,
 * and assigns an image based on the fly type.</p>
 */
@Service
public class FlyServiceImpl implements FlyService {
    /** The logger for this class. */
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
     * @return the combined list of flies
     */
    @Override
    public List<Fly> getAllFlies() {
        Long userId = getCurrentUserId();

        log.info("Getting default and custom flies for user ID: {}", userId);

        List<Fly> flies = flyRepository.findByUserIdIsNullOrUserId(userId);
        return flies;
    }

    /**
     * Returns all default Hookset flies.
     *
     * @return the list of default flies
     */
    @Override
    public List<Fly> getAllHooksetFlies() {
        log.info("Getting default Hookset flies");

        List<Fly> flies = flyRepository.findByUserIdIsNull();
        return flies;
    }

    /**
     * Returns all custom flies created by the current user.
     *
     * @return the list of the user's custom flies
     */
    @Override
    public List<Fly> getAllUserFlies() {
        Long userId = getCurrentUserId();

        log.info("Getting custom flies for user ID: {}", userId);

        List<Fly> flies = flyRepository.findByUserId(userId);
        return flies;
    }

    /**
     * Returns a single fly by its URL slug.
     *
     * <p>Default flies are checked first. If no default fly matches, the
     * current user's custom flies are checked next.</p>
     *
     * @param flySlug the slug of the fly to look up
     * @return the matching fly, or {@code null} if none exists
     */
    @Override
    public Fly getFlyFromSlug(String flySlug) {
        log.info("Looking up fly with slug: {}", flySlug);
        Long userId = getCurrentUserId();

        // check the default flies first since everyone can see them
        Fly fly = flyRepository.findByFlySlugAndUserIdIsNull(flySlug);

        if (fly != null) {
            return fly;
        }

        log.info("No Hookset fly exists for slug: {}", flySlug);

        // a null user ID would turn this into an IS NULL query and match default flies again
        if (userId != null) {
            fly = flyRepository.findByFlySlugAndUserId(flySlug, userId);
        }

        if (fly == null) {
            log.warn("Fly not found with slug: {} in user flies or Hookset flies", flySlug);
            return null;
        }

        return fly;
    }

    /**
     * Saves a new custom fly for the current user.
     *
     * <p>The slug is generated from the fly name, and the image is picked
     * from the fly type. Any type other than dry fly or streamer uses the
     * nymph image.</p>
     *
     * @param flyForm the form containing the custom fly details
     * @return {@code true} if the fly was saved, otherwise {@code false}
     */
    @Override
    public boolean addFly(FlyForm flyForm) {
        Long userId = getCurrentUserId();

        if (userId == null) {
            log.warn("Fly not saved. No user logged in");
            return false;
        }

        // runs every check itself so no caller can save a bad fly
        if (!isFlyFormValid(flyForm)) {
            return false;
        }

        if (!isFlyNameUnique(flyForm)) {
            return false;
        }

        Fly fly = flyMapper.toEntity(flyForm);

        // owner comes from the login, not the form, so nobody can add a fly as someone else
        fly.setUserId(userId);
        fly.setFlySlug(generateFlySlug(fly.getFlyName()));
        fly.setFlyImage(getFlyImage(fly.getFlyType()));

        log.info("Adding custom fly: {} for user ID: {}", fly.getFlyName(), userId);

        flyRepository.save(fly);

        log.info("Custom fly saved with slug: {}", fly.getFlySlug());
        return true;
    }

    /**
     * Checks that a fly's min size is not larger than its max size.
     *
     * <p>If either size is missing there is nothing to compare, so this
     * returns true. Missing sizes are caught by {@code @NotNull} on the form
     * and by {@code isFlyFormValid} before a fly is saved.</p>
     *
     * @param flyForm the form containing the sizes to check
     * @return {@code true} if the size range is valid or incomplete, otherwise {@code false}
     */
    @Override
    public boolean isSizeRangeValid(FlyForm flyForm) {
        // only compare when both sizes are filled in
        if (flyForm.getMinSize() != null
                && flyForm.getMaxSize() != null) {

            if (flyForm.getMinSize() > flyForm.getMaxSize()) {
                log.warn("Fly validation failed. Min size {} exceeds max size {}",
                        flyForm.getMinSize(), flyForm.getMaxSize());

                return false;
            }
        }

        return true;
    }

    /**
     * Checks that a new fly name is not already used by a default fly or one of
     * the current user's custom flies.
     *
     * <p>The comparison ignores case and surrounding whitespace. A blank name
     * returns false so nothing treats it as a valid name.</p>
     *
     * @param flyForm the form containing the fly name to check
     * @return {@code true} if the name is unique, otherwise {@code false}
     */
    @Override
    public boolean isFlyNameUnique(FlyForm flyForm) {
        if (flyForm.getFlyName() == null || flyForm.getFlyName().isBlank()) {
            log.warn("Fly validation failed. Fly name is blank");
            return false;
        }

        Long userId = getCurrentUserId();
        String newName = flyForm.getFlyName().trim();

        List<Fly> visibleFlies = flyRepository.findByUserIdIsNullOrUserId(userId);

        // compare against every fly this user can see
        for (Fly fly : visibleFlies) {
            if (fly.getFlyName().trim().equalsIgnoreCase(newName)) {
                log.warn("Fly validation failed. Duplicate fly name: {} for user ID: {}", newName, userId);
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that an edited fly name is not already used by a default fly or
     * another of the current user's custom flies.
     *
     * <p>Keeping the fly's current name is always allowed. The comparison
     * ignores case and surrounding whitespace.</p>
     *
     * @param flyId the ID of the fly being edited
     * @param flyForm the form containing the new fly name
     * @return {@code true} if the name is unique, otherwise {@code false}
     */
    @Override
    public boolean isFlyNameUniqueForEdit(Long flyId, FlyForm flyForm) {
        Fly flyBeingEdited = flyRepository.findById(flyId).orElse(null);

        if (flyBeingEdited == null) {
            log.warn("Fly edit validation failed. Fly not found with ID: {}", flyId);
            return false;
        }

        if (flyForm.getFlyName() == null || flyForm.getFlyName().isBlank()) {
            log.warn("Fly edit validation failed. Fly name is blank for fly ID: {}", flyId);
            return false;
        }

        String newName = flyForm.getFlyName().trim();

        // the fly keeping its own name isn't a duplicate
        if (flyBeingEdited.getFlyName().trim().equalsIgnoreCase(newName)) {
            return true;
        }

        Long userId = getCurrentUserId();

        List<Fly> userAndDefaultFlies = flyRepository.findByUserIdIsNullOrUserId(userId);

        // compare against every fly this user can see
        for (Fly fly : userAndDefaultFlies) {
            if (fly.getFlyName().trim().equalsIgnoreCase(newName)) {
                log.warn("Fly edit validation failed. Duplicate fly name: {} for user ID: {}", newName, userId);
                return false;
            }
        }

        return true;
    }

    /**
     * Updates a custom fly owned by the current user.
     *
     * <p>Ownership is checked before the form is validated. The slug and image
     * are rebuilt from the new name and type, the same way they are when a fly
     * is added.</p>
     *
     * @param flyId the ID of the fly to update
     * @param flyForm the form containing the new fly details
     * @return {@code true} if the fly was updated, otherwise {@code false}
     * @throws AccessDeniedException if the fly belongs to another user or is a default fly
     */
    @Override
    public boolean updateFly(Long flyId, FlyForm flyForm) {
        log.info("Updating fly with ID: {} for user ID: {}", flyId, getCurrentUserId());

        Fly fly = flyRepository.findById(flyId).orElse(null);

        if (fly == null) {
            log.warn("Fly update failed. Fly not found with ID: {}", flyId);
            return false;
        }

        // default flies have a null user ID and can never be edited
        if (fly.getUserId() == null || !fly.getUserId().equals(getCurrentUserId())) {
            log.warn("Update denied. User ID: {} does not own fly ID: {}", getCurrentUserId(), flyId);
            throw new AccessDeniedException("You cannot edit this fly");
        }

        // ownership is checked first so a bad form can't tell someone whether another user's fly exists
        if (!isFlyFormValid(flyForm)) {
            return false;
        }

        if (!isFlyNameUniqueForEdit(flyId, flyForm)) {
            return false;
        }

        // user ID is not copied, so an edit can't move a fly to another user
        fly.setFlyName(flyForm.getFlyName());
        fly.setColor(flyForm.getFlyColor());
        fly.setFlyType(flyForm.getFlyType());
        fly.setMinSize(flyForm.getMinSize());
        fly.setMaxSize(flyForm.getMaxSize());

        // the name may have changed, so the slug has to be rebuilt
        fly.setFlySlug(generateFlySlug(fly.getFlyName()));
        fly.setFlyImage(getFlyImage(fly.getFlyType()));

        try {
            flyRepository.save(fly);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to save fly due to database constraints", e);
            return false;
        }

        log.info("Fly updated with ID: {}", flyId);
        return true;
    }

    /**
     * Deletes a custom fly owned by the current user.
     *
     * @param flyId the ID of the fly to delete
     * @throws AccessDeniedException if the fly belongs to another user or is a default fly
     */
    @Override
    public void deleteFly(Long flyId) {

        if (flyId == null) {
            log.warn("Fly delete failed. Fly ID is null");
            return;
        }

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
     * Returns a single fly by its ID.
     *
     * <p>Default flies can be loaded by anyone. Custom flies can only be loaded
     * by the user who owns them.</p>
     *
     * @param flyId the ID of the fly to look up
     * @return the matching fly, or {@code null} if none exists
     * @throws AccessDeniedException if the fly belongs to another user
     */
    @Override
    public Fly getFlyById(Long flyId) {
        if (flyId == null) {
            log.warn("Fly lookup failed. Fly ID is null");
            return null;
        }

        Fly fly = flyRepository.findById(flyId).orElse(null);

        if (fly == null) {
            log.warn("Fly not found with ID: {}", flyId);
            return null;
        }

        // default flies have a null user ID and are visible to everyone
        if (fly.getUserId() != null && !fly.getUserId().equals(getCurrentUserId())) {
            log.warn("Access denied. User ID: {} does not own fly ID: {}", getCurrentUserId(), flyId);
            throw new AccessDeniedException("You cannot view this fly");
        }

        return fly;
    }

    /**
     * Gets the ID of the currently logged-in user.
     *
     * @return the ID of the logged-in user, or {@code null} if no user is logged in
     */
    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // anonymous users have a String principal, not HooksetUserDetails
        if (authentication != null
                && authentication.getPrincipal() instanceof HooksetUserDetails userDetails) {
            return userDetails.getUserId();
        }

        return null;
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

    /**
     * Runs the checks every saved fly must pass, no matter who calls the service.
     *
     * <p>Name uniqueness is checked separately because adding and editing use
     * different rules for it.</p>
     *
     * @param flyForm the form to check
     * @return {@code true} if every check passes, otherwise {@code false}
     */
    private boolean isFlyFormValid(FlyForm flyForm) {
        if (flyForm.getFlyName() == null || flyForm.getFlyName().isBlank()) {
            log.warn("Fly validation failed. Fly name is blank");
            return false;
        }

        if (flyForm.getFlyType() == null || flyForm.getFlyType().isBlank()) {
            log.warn("Fly validation failed. Fly type is blank");
            return false;
        }

        if (flyForm.getMinSize() == null || flyForm.getMaxSize() == null) {
            log.warn("Fly validation failed. Min or max size is missing");
            return false;
        }

        if (flyForm.getFlyColor() == null || flyForm.getFlyColor().isBlank()) {
            log.warn("Fly validation failed. Fly color is blank");
            return false;
        }

        if (!isSizeRangeValid(flyForm)) {
            return false;
        }

        return true;
    }

    /**
     * Picks the image file for a fly type.
     *
     * <p>Any type other than dry fly or streamer uses the nymph image.</p>
     *
     * @param flyType the fly type from the form
     * @return the image file name
     */
    private String getFlyImage(String flyType) {
        // these match the option values on the fly forms
        if (flyType.equals("Dry")) {
            return "dry.png";
        }

        if (flyType.equals("Streamer")) {
            return "streamer.png";
        }

        return "nymph.png";
    }
}