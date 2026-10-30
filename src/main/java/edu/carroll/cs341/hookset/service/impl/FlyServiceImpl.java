package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FlyRepository;
import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.service.input.FlyInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import shared.jpa.entity.Fly;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for looking up, creating, editing, and deleting flies.
 *
 * <p>Hookset has two kinds of flies. Default flies have no owning user and
 * are shown to everyone. Custom flies belong to the user who created them
 * and are only shown to that user.</p>
 *
 * <p>This service also validates custom flies, generates their URL slugs,
 * and assigns an image based on the fly type. The user is always passed in
 * by the caller, so the service doesn't depend on how they logged in.</p>
 */
@Service
public class FlyServiceImpl implements FlyService {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(FlyServiceImpl.class);

    /** The repository used to access fly records. */
    private final FlyRepository flyRepository;

    /**
     * Creates the fly service with the repository it depends on.
     *
     * @param flyRepository the repository used to access fly records
     */
    public FlyServiceImpl(FlyRepository flyRepository) {
        this.flyRepository = flyRepository;
    }

    /**
     * Returns all default flies along with a user's custom flies.
     *
     * @param userId the ID of the user whose custom flies to include
     * @return the combined list of flies
     */
    @Override
    public List<Fly> getAllFlies(Long userId) {
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
     * Returns all custom flies created by a user.
     *
     * @param userId the ID of the user whose flies to return
     * @return the list of the user's custom flies
     */
    @Override
    public List<Fly> getAllUserFlies(Long userId) {
        // a null user ID would turn this into an IS NULL query and return the default flies
        if (userId == null) {
            log.warn("Could not load user flies. User ID is null");
            return new ArrayList<>();
        }
        log.info("Getting custom flies for user ID: {}", userId);

        List<Fly> flies = flyRepository.findByUserId(userId);
        return flies;
    }

    /**
     * Returns a single fly by its URL slug.
     *
     * <p>Default flies are checked first. If no default fly matches, the
     * user's custom flies are checked next.</p>
     *
     * @param slug the slug of the fly to look up
     * @param userId the ID of the user whose custom flies can also match
     * @return the matching fly, or {@code null} if none exists
     */
    @Override
    public Fly getFlyFromSlug(String slug, Long userId) {
        log.info("Looking up fly with slug: {}", slug);

        // check the default flies first since everyone can see them
        Fly fly = flyRepository.findBySlugAndUserIdIsNull(slug);

        if (fly != null) {
            return fly;
        }

        log.info("No Hookset fly exists for slug: {}", slug);

        // a null user ID would turn this into an IS NULL query and match default flies again
        if (userId != null) {
            fly = flyRepository.findBySlugAndUserId(slug, userId);
        }

        if (fly == null) {
            log.warn("Fly not found with slug: {} in user flies or Hookset flies", slug);
            return null;
        }

        return fly;
    }

    /**
     * Returns a single fly by its ID.
     *
     * <p>Default flies can be loaded by anyone. Custom flies can only be loaded
     * by the user who owns them.</p>
     *
     * @param id the ID of the fly to look up
     * @param userId the ID of the user asking for the fly
     * @return the matching fly, or {@code null} if none exists
     * @throws AccessDeniedException if the fly belongs to another user
     */
    @Override
    public Fly getFlyById(Long id, Long userId) {
        if (id == null) {
            log.warn("Fly lookup failed. Fly ID is null");
            return null;
        }

        Fly fly = flyRepository.findById(id).orElse(null);

        if (fly == null) {
            log.warn("Fly not found with ID: {}", id);
            return null;
        }

        // default flies have a null user ID and are visible to everyone
        if (fly.getUserId() != null && !fly.getUserId().equals(userId)) {
            log.warn("Access denied. User ID: {} does not own fly ID: {}", userId, id);
            throw new AccessDeniedException("You cannot view this fly");
        }

        return fly;
    }

    /**
     * Saves a new custom fly for a user.
     *
     * <p>The slug is generated from the fly name, and the image is picked
     * from the fly type. Any type other than dry fly or streamer uses the
     * nymph image.</p>
     *
     * @param flyInput the details of the new fly
     * @param userId the ID of the user adding the fly
     * @return {@code true} if the fly was saved, otherwise {@code false}
     */
    @Override
    public boolean addFly(FlyInput flyInput, Long userId) {
        if (userId == null) {
            log.warn("Fly not saved. No user given");
            return false;
        }

        // runs every check itself so no caller can save a bad fly
        if (!isFlyInputValid(flyInput)) {
            return false;
        }

        if (!isFlyNameUnique(flyInput.name(), userId)) {
            return false;
        }

        // owner, slug, and image are all set inside toFly, never taken from the input
        Fly fly = toFly(flyInput, userId);

        log.info("Adding custom fly: {} for user ID: {}", fly.getName(), userId);

        flyRepository.save(fly);

        log.info("Custom fly saved with slug: {}", fly.getSlug());
        return true;
    }

    /**
     * Updates a custom fly owned by a user.
     *
     * <p>Ownership is checked before the input, so a bad edit can't reveal
     * whether another user's fly exists.</p>
     *
     * @param flyId the ID of the fly to update
     * @param flyInput the new fly details
     * @param userId the ID of the user editing the fly
     * @return {@code true} if the fly was updated, otherwise {@code false}
     * @throws AccessDeniedException if the fly is a default fly or belongs to another user
     */
    @Override
    public boolean updateFly(Long flyId, FlyInput flyInput, Long userId) {
        if (flyId == null) {
            log.warn("Fly update failed. Fly ID is null");
            return false;
        }

        log.info("Updating fly with ID: {} for user ID: {}", flyId, userId);

        Fly fly = flyRepository.findById(flyId).orElse(null);

        if (fly == null) {
            log.warn("Fly update failed. Fly not found with ID: {}", flyId);
            return false;
        }

        // default flies have a null user ID and can never be edited
        if (fly.getUserId() == null || !fly.getUserId().equals(userId)) {
            log.warn("Update denied. User ID: {} does not own fly ID: {}", userId, flyId);
            throw new AccessDeniedException("You cannot edit this fly");
        }

        if (!isFlyInputValid(flyInput)) {
            return false;
        }

        if (!isFlyNameUniqueForEdit(flyId, flyInput.name(), userId)) {
            return false;
        }

        // user ID is not copied, so an edit can't move a fly to another user
        fly.setName(flyInput.name());
        fly.setColor(flyInput.color());
        fly.setType(flyInput.type());
        fly.setMinSize(flyInput.minSize());
        fly.setMaxSize(flyInput.maxSize());

        // the name may have changed, so the slug has to be rebuilt
        fly.setSlug(generateFlySlug(fly.getName()));
        fly.setImage(getFlyImage(fly.getType()));

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
     * Deletes a custom fly owned by a user.
     *
     * @param id the ID of the fly to delete
     * @param userId the ID of the user deleting the fly
     * @throws AccessDeniedException if the fly belongs to another user or is a default fly
     */
    @Override
    public void deleteFly(Long id, Long userId) {
        if (id == null) {
            log.warn("Fly delete failed. Fly ID is null");
            return;
        }

        log.info("Deleting fly with ID: {} for user ID: {}", id, userId);

        Fly fly = flyRepository.findById(id).orElse(null);

        if (fly == null) {
            log.warn("Fly not found with ID: {}", id);
            return;
        }

        // default flies have a null user ID, so this also blocks deleting them
        if (fly.getUserId() == null || !fly.getUserId().equals(userId)) {
            log.warn("Delete denied. User ID: {} does not own fly ID: {}", userId, id);
            throw new AccessDeniedException("You cannot delete this fly");
        }

        flyRepository.delete(fly);

        log.info("Fly deleted with ID: {}", id);
    }

    /**
     * Checks that a fly's min size is not larger than its max size.
     *
     * <p>If either size is missing there is nothing to compare, so this
     * returns true. Missing sizes are caught by {@code isFlyInputValid}
     * before a fly is saved.</p>
     *
     * @param minSize the smallest hook size for the fly
     * @param maxSize the largest hook size for the fly
     * @return {@code true} if the size range is valid or incomplete, otherwise {@code false}
     */
    @Override
    public boolean isSizeRangeValid(Integer minSize, Integer maxSize) {
        // only compare when both sizes are filled in
        if (minSize != null && maxSize != null) {

            if (minSize > maxSize) {
                log.warn("Fly validation failed. Min size {} exceeds max size {}", minSize, maxSize);

                return false;
            }
        }

        return true;
    }

    /**
     * Checks that a new fly's name won't collide with one the user can already see.
     *
     * <p>Names are compared by slug, so names that only differ by case or
     * spacing count as duplicates. Default flies are included, so a user
     * can't make a fly whose URL matches a Hookset fly.</p>
     * @param userId the ID of the user adding the fly
     * @return {@code true} if no visible fly has the same slug, otherwise {@code false}
     */
    @Override
    public boolean isFlyNameUnique(String name, Long userId) {
        if (name == null || name.isBlank()) {
            log.warn("Fly validation failed. Fly name is blank");
            return false;
        }

        String newSlug = generateFlySlug(name);

        List<Fly> visibleFlies = flyRepository.findByUserIdIsNullOrUserId(userId);

        // two names with the same slug would collide in the URL
        for (Fly fly : visibleFlies) {
            if (newSlug.equals(fly.getSlug())) {
                log.warn("Fly validation failed. Duplicate fly slug: {} for user ID: {}", newSlug, userId);
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that an edited fly's name won't collide with another fly the user can see.
     *
     * <p>The fly keeping its own name is allowed. Otherwise this uses the same
     *
     * @param flyId the ID of the fly being edited
     * @param flyInput the new fly details
     * @param userId the ID of the user editing the fly
     * @return {@code true} if the name is unchanged or no other visible fly has the same slug, otherwise {@code false}
     */
    @Override
    public boolean isFlyNameUniqueForEdit(Long flyId, String name, Long userId) {
        if (flyId == null) {
            return false;
        }
        Fly flyBeingEdited = flyRepository.findById(flyId).orElse(null);

        if (flyBeingEdited == null) {
            log.warn("Fly edit validation failed. Fly not found with ID: {}", flyId);
            return false;
        }

        if (name == null || name.isBlank()) {
            log.warn("Fly edit validation failed. Fly name is blank for fly ID: {}", flyId);
            return false;
        }

        // built after the blank check, since generateFlySlug can't handle a null name
        String newSlug = generateFlySlug(name);

        // the fly keeping its own name isn't a duplicate
        if (newSlug.equals(flyBeingEdited.getSlug())) {
            return true;
        }

        List<Fly> visibleFlies = flyRepository.findByUserIdIsNullOrUserId(userId);

        // two names with the same slug would collide in the URL
        for (Fly fly : visibleFlies) {
            if (newSlug.equals(fly.getSlug())) {
                log.warn("Fly edit validation failed. Duplicate fly slug: {} for user ID: {}", newSlug, userId);
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether a fly name is missing or only whitespace.
     *
     * @param name the fly name to check
     * @return {@code true} if the name is blank, otherwise {@code false}
     */
    @Override
    public boolean isFlyNameBlank(String name) {
        if (name == null || name.isBlank()) {
            log.warn("Fly name validation failed. Fly name is blank");
            return true;
        }

        return false;
    }

    /**
     * Runs the checks every saved fly must pass, no matter who calls the service.
     *
     * <p>Name uniqueness is checked separately because adding and editing use
     * different rules for it.</p>
     *
     * @param flyInput the fly input to check
     * @return {@code true} if every check passes, otherwise {@code false}
     */
    private boolean isFlyInputValid(FlyInput flyInput) {
        if (flyInput == null) {
            log.warn("Fly validation failed. Fly input is null");
            return false;
        }
        if (flyInput.name() == null || flyInput.name().isBlank()) {
            log.warn("Fly validation failed. Fly name is blank");
            return false;
        }

        if (flyInput.type() == null || flyInput.type().isBlank()) {
            log.warn("Fly validation failed. Fly type is blank");
            return false;
        }

        if (flyInput.minSize() == null || flyInput.maxSize() == null) {
            log.warn("Fly validation failed. Min or max size is missing");
            return false;
        }

        if (flyInput.color() == null || flyInput.color().isBlank()) {
            log.warn("Fly validation failed. Fly color is blank");
            return false;
        }

        if (!isSizeRangeValid(flyInput.minSize(), flyInput.maxSize())) {
            return false;
        }

        return true;
    }

    /**
     * Builds a new fly entity from the submitted input.
     *
     * <p>The slug, image, and owner are set here instead of coming from the
     * input, so the caller can't choose them.</p>
     *
     * @param flyInput the submitted fly details
     * @param userId the ID of the user creating the fly
     * @return the new fly entity, not yet saved
     */
    private Fly toFly(FlyInput flyInput, Long userId) {
        Fly fly = new Fly();

        fly.setName(flyInput.name());
        fly.setColor(flyInput.color());
        fly.setType(flyInput.type());
        fly.setMinSize(flyInput.minSize());
        fly.setMaxSize(flyInput.maxSize());

        fly.setUserId(userId);
        fly.setSlug(generateFlySlug(flyInput.name()));
        fly.setImage(getFlyImage(flyInput.type()));

        return fly;
    }

    /**
     * Generates a URL slug from a fly name.
     *
     * <p>Trims the name, lowercases it, and replaces runs of whitespace with
     * a single hyphen. For example, "Parachute Adams" becomes "parachute-adams".</p>
     *
     * @param name the fly name to build the slug from
     * @return the generated slug
     */
    private String generateFlySlug(String name) {
        return name.trim().toLowerCase().replaceAll("\\s+", "-");
    }

    /**
     * Picks the image file for a fly type.
     *
     * <p>Any type other than dry fly or streamer uses the nymph image.</p>
     *
     * @param type the fly type
     * @return the image file name
     */
    private String getFlyImage(String type) {
        // these match the option values on the fly forms
        if (type.equals("Dry")) {
            return "dry.png";
        }

        if (type.equals("Streamer")) {
            return "streamer.png";
        }

        return "nymph.png";
    }
}