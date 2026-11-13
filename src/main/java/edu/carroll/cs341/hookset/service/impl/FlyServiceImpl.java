package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.CatchRepository;
import edu.carroll.cs341.hookset.jpa.repo.FlyRepository;
import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.service.input.FlyInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import entities.jpa.entity.Fly;

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

    /** The repository used to check whether catches use a fly. */
    private final CatchRepository catchRepository;

    /**
     * Creates the fly service with the repositories it depends on.
     *
     * @param flyRepository the repository used to access fly records
     * @param catchRepository the repository used to check whether catches use a fly
     */
    public FlyServiceImpl(FlyRepository flyRepository, CatchRepository catchRepository) {
        this.flyRepository = flyRepository;
        this.catchRepository = catchRepository;
    }

    /**
     * Returns all default flies along with a user's custom flies.
     *
     * <p>A {@code null} user ID returns only the default flies.</p>
     *
     * @param userId the ID of the user whose custom flies to include
     * @return the combined list of flies, or an empty list if none exist
     */
    @Override
    public List<Fly> getAllFlies(Long userId) {
        log.info("Loading default flies and custom flies for user ID {}", userId);

        // one query for both, default flies have a null user ID and custom flies have this user's ID
        List<Fly> flies = flyRepository.findByUserIdIsNullOrUserId(userId);

        log.info("Loaded {} default and custom flies for user ID {}", flies.size(), userId);
        return flies;
    }

    /**
     * Returns all default Hookset flies.
     *
     * @return the list of default flies, or an empty list if none exist
     */
    @Override
    public List<Fly> getAllHooksetFlies() {
        log.info("Loading default Hookset flies");

        // default flies are the only ones without an owner
        List<Fly> flies = flyRepository.findByUserIdIsNull();

        log.info("Loaded {} default Hookset flies", flies.size());
        return flies;
    }

    /**
     * Returns all custom flies created by a user.
     *
     * @param userId the ID of the user whose flies to return
     * @return the list of the user's custom flies, or an empty list if the user ID is null or has none
     */
    @Override
    public List<Fly> getAllUserFlies(Long userId) {
        // a null user ID would turn this into an IS NULL query and return the default flies
        if (userId == null) {
            log.warn("Could not load custom flies. User ID is null");
            return new ArrayList<>();
        }

        log.info("Loading custom flies for user ID {}", userId);

        List<Fly> flies = flyRepository.findByUserId(userId);

        log.info("Loaded {} custom flies for user ID {}", flies.size(), userId);
        return flies;
    }

    /**
     * Returns a single fly by its URL slug.
     *
     * <p>Default flies are checked first. If no default fly matches, the
     * user's custom flies are checked next. Other users' custom flies are
     * never searched.</p>
     *
     * @param slug the slug of the fly to look up
     * @param userId the ID of the user whose custom flies can also match
     * @return the matching fly, or {@code null} if no default fly or custom fly of this user matches
     */
    @Override
    public Fly getFlyFromSlug(String slug, Long userId) {
        log.info("Looking up fly slug {} for user ID {}", slug, userId);

        // check the default flies first since everyone can see them
        Fly fly = flyRepository.findBySlugAndUserIdIsNull(slug);

        if (fly != null) {
            log.info("Slug {} matched default fly ID {}", slug, fly.getId());
            return fly;
        }

        log.info("No default fly matches slug {}, checking custom flies for user ID {}", slug, userId);

        // a null user ID would turn this into an IS NULL query and match default flies again
        if (userId != null) {
            fly = flyRepository.findBySlugAndUserId(slug, userId);
        }

        if (fly == null) {
            log.warn("No default fly or custom fly for user ID {} matches slug {}", userId, slug);
            return null;
        }

        log.info("Slug {} matched custom fly ID {} for user ID {}", slug, fly.getId(), userId);
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
     * @return the matching fly, or {@code null} if the ID is null or no fly has that ID
     * @throws AccessDeniedException if the fly belongs to another user
     */
    @Override
    public Fly getFlyById(Long id, Long userId) {
        // findById throws on a null ID instead of returning empty, so this has to be checked first
        if (id == null) {
            log.warn("Could not load fly for user ID {}. Fly ID is null", userId);
            return null;
        }

        Fly fly = flyRepository.findById(id).orElse(null);

        if (fly == null) {
            log.warn("User ID {} requested fly ID {}, which does not exist", userId, id);
            return null;
        }

        // default flies have a null user ID and are visible to everyone
        if (fly.getUserId() != null && !fly.getUserId().equals(userId)) {
            log.warn("Access denied. User ID {} tried to load fly ID {}, which belongs to another user", userId, id);
            throw new AccessDeniedException("You cannot view this fly");
        }

        log.info("Loaded fly ID {} for user ID {}", id, userId);
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
     * @return {@code true} if the fly was saved, otherwise {@code false} if the user ID is null,
     *         the input fails validation, or the name matches a fly the user can already see
     */
    @Override
    public boolean addFly(FlyInput flyInput, Long userId) {
        // a custom fly has to belong to someone, and a null owner would turn it into a default fly
        if (userId == null) {
            log.warn("Fly not saved. User ID is null");
            return false;
        }

        // runs every check itself so no caller can save a bad fly
        if (!isFlyInputValid(flyInput)) {
            log.warn("Fly not saved for user ID {}. The input failed validation", userId);
            return false;
        }

        // checked separately from the other rules because editing uses a different version of this check
        if (!isFlyNameUnique(flyInput.name(), userId)) {
            log.warn("Fly not saved for user ID {}. The name {} matches a fly they can already see",
                    userId, flyInput.name());
            return false;
        }

        // owner, slug, and image are all set inside toFly, never taken from the input
        Fly fly = toFly(flyInput, userId);

        log.info("Saving custom fly {} for user ID {}", fly.getName(), userId);

        flyRepository.save(fly);

        // the database assigns the ID during save, so it's only available after this point
        log.info("Saved custom fly ID {} with slug {} for user ID {}", fly.getId(), fly.getSlug(), userId);
        return true;
    }

    /**
     * Updates a custom fly owned by a user.
     *
     * <p>Ownership is checked before the input, so input errors are never
     * reported for a fly the user doesn't own.</p>
     *
     * @param flyId the ID of the fly to update
     * @param flyInput the new fly details
     * @param userId the ID of the user editing the fly
     * @return {@code true} if the fly was updated, otherwise {@code false} if the fly ID is null,
     *         the fly does not exist, the input fails validation, or the new name matches another fly the user can see
     * @throws AccessDeniedException if the fly is a default fly or belongs to another user
     */
    @Override
    public boolean updateFly(Long flyId, FlyInput flyInput, Long userId) {
        // findById throws on a null ID instead of returning empty, so this has to be checked first
        if (flyId == null) {
            log.warn("Fly not updated for user ID {}. Fly ID is null", userId);
            return false;
        }

        log.info("User ID {} updating fly ID {}", userId, flyId);

        Fly fly = flyRepository.findById(flyId).orElse(null);

        if (fly == null) {
            log.warn("Fly not updated. User ID {} tried to update fly ID {}, which does not exist", userId, flyId);
            return false;
        }

        // default flies have a null user ID and can never be edited
        if (!isFlyOwner(flyId, userId)) {
            log.warn("Update denied. User ID {} tried to update fly ID {}, which is not theirs", userId, flyId);
            throw new AccessDeniedException("You cannot edit this fly");
        }

        // runs every check itself so no caller can save a bad edit
        if (!isFlyInputValid(flyInput)) {
            log.warn("Fly ID {} not updated for user ID {}. The input failed validation", flyId, userId);
            return false;
        }

        // the edit version lets the fly keep its own name without counting as a duplicate
        if (!isFlyNameUniqueForEdit(flyId, flyInput.name(), userId)) {
            log.warn("Fly ID {} not updated for user ID {}. The name {} matches another fly they can see",
                    flyId, userId, flyInput.name());
            return false;
        }

        // user ID is not copied, so an edit can't move a fly to another user
        fly.setName(flyInput.name());
        fly.setColor(flyInput.color());
        fly.setType(flyInput.type());
        fly.setMinSize(flyInput.minSize());
        fly.setMaxSize(flyInput.maxSize());

        // the name may have changed, so the slug has to be rebuilt
        // the type may have changed too, so the image is picked again
        fly.setSlug(generateFlySlug(fly.getName()));
        fly.setImage(getFlyImage(fly.getType()));

        flyRepository.save(fly);

        log.info("Updated fly ID {} for user ID {}, slug is now {}", flyId, userId, fly.getSlug());
        return true;
    }

    /**
     * Deletes a custom fly owned by a user.
     *
     * <p>A fly that is used by any catch can't be deleted, since removing it
     * would leave those catches pointing at a fly that no longer exists.</p>
     *
     * @param id the ID of the fly to delete
     * @param userId the ID of the user deleting the fly
     * @return {@code true} if the fly was deleted, otherwise {@code false} if the fly ID is null,
     *         the fly does not exist, or the fly is used by a catch
     * @throws AccessDeniedException if the fly belongs to another user or is a default fly
     */
    @Override
    public boolean deleteFly(Long id, Long userId) {
        // findById throws on a null ID instead of returning empty, so this has to be checked first
        if (id == null) {
            log.warn("Fly not deleted for user ID {}. Fly ID is null", userId);
            return false;
        }

        log.info("User ID {} deleting fly ID {}", userId, id);

        Fly fly = flyRepository.findById(id).orElse(null);

        if (fly == null) {
            log.warn("Fly not deleted. User ID {} tried to delete fly ID {}, which does not exist", userId, id);
            return false;
        }

        // default flies have a null user ID and can never be deleted
        if (!isFlyOwner(id, userId)) {
            log.warn("Delete denied. User ID {} tried to delete fly ID {}, which is not theirs", userId, id);
            throw new AccessDeniedException("You cannot delete this fly");
        }

        // catches point at their fly, so deleting a fly in use would break those catches
        if (isFlyUsedByCatch(id)) {
            log.warn("Fly ID {} not deleted for user ID {}. It is used by at least one catch", id, userId);
            return false;
        }

        flyRepository.delete(fly);

        log.info("Deleted fly ID {} for user ID {}", id, userId);
        return true;
    }

    /**
     * Checks whether any catch was logged with a fly.
     *
     * @param id the ID of the fly to check
     * @return {@code true} if at least one catch uses the fly, otherwise {@code false}
     */
    @Override
    public boolean isFlyUsedByCatch(Long id) {
        if (id == null) {
            log.warn("Could not check whether a fly is used by a catch. Fly ID is null");
            return false;
        }

        // only needs to know if one exists, so this doesn't load any catches
        boolean isUsed = catchRepository.existsByFlyId(id);

        log.info("Fly ID {} is used by a catch: {}", id, isUsed);
        return isUsed;
    }

    /**
     * Checks that a fly's min size is not larger than its max size.
     *
     * <p>If either size is missing there is nothing to compare, so this
     * returns {@code true}. Missing sizes are caught by {@code isFlyInputValid}
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

            // an equal min and max is fine, it just means the fly only comes in one size
            if (minSize > maxSize) {
                log.warn("Fly validation failed. Min size {} is larger than max size {}", minSize, maxSize);

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
     *
     * @param name the name of the new fly
     * @param userId the ID of the user adding the fly
     * @return {@code true} if no visible fly has the same slug, otherwise {@code false},
     *         including when the name is blank
     */
    @Override
    public boolean isFlyNameUnique(String name, Long userId) {
        // generateFlySlug can't handle a null name, so this has to be checked first
        if (name == null || name.isBlank()) {
            log.warn("Fly validation failed. Fly name is blank for user ID {}", userId);
            return false;
        }

        String newSlug = generateFlySlug(name);

        // only default flies and this user's flies, other users can have a fly with the same name
        List<Fly> visibleFlies = flyRepository.findByUserIdIsNullOrUserId(userId);

        // two names with the same slug would collide in the URL
        for (Fly fly : visibleFlies) {
            if (newSlug.equals(fly.getSlug())) {
                log.warn("Fly validation failed. Slug {} is already used by fly ID {}, which user ID {} can see",
                        newSlug, fly.getId(), userId);
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that an edited fly's name won't collide with another fly the user can see.
     *
     * <p>The fly keeping its own name is allowed. Otherwise this uses the same
     * slug rules as {@link #isFlyNameUnique(String, Long)}.</p>
     *
     * @param id the ID of the fly being edited
     * @param name the new name for the fly
     * @param userId the ID of the user editing the fly
     * @return {@code true} if the name is unchanged or no other visible fly has the same slug, otherwise {@code false},
     *         including when the ID is null, the fly does not exist, or the name is blank
     */
    @Override
    public boolean isFlyNameUniqueForEdit(Long id, String name, Long userId) {
        // findById throws on a null ID instead of returning empty, so this has to be checked first
        if (id == null) {
            log.warn("Fly edit validation failed. Fly ID is null for user ID {}", userId);
            return false;
        }

        // needed to compare against the fly's current slug below
        Fly flyBeingEdited = flyRepository.findById(id).orElse(null);

        if (flyBeingEdited == null) {
            log.warn("Fly edit validation failed. Fly ID {} does not exist", id);
            return false;
        }

        if (name == null || name.isBlank()) {
            log.warn("Fly edit validation failed. New name for fly ID {} is blank", id);
            return false;
        }

        // built after the blank check, since generateFlySlug can't handle a null name
        String newSlug = generateFlySlug(name);

        // the fly keeping its own name isn't a duplicate
        if (newSlug.equals(flyBeingEdited.getSlug())) {
            return true;
        }

        // only default flies and this user's flies, other users can have a fly with the same name
        List<Fly> visibleFlies = flyRepository.findByUserIdIsNullOrUserId(userId);

        // two names with the same slug would collide in the URL
        for (Fly fly : visibleFlies) {
            if (newSlug.equals(fly.getSlug())) {
                log.warn("Fly edit validation failed. Renaming fly ID {} to slug {} would match fly ID {}, which user ID {} can see",
                        id, newSlug, fly.getId(), userId);
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
            log.warn("Fly validation failed. Fly name is blank");
            return true;
        }

        return false;
    }

    /**
     * Checks whether a fly color is missing or only whitespace.
     *
     * @param flyColor the fly color to check
     * @return {@code true} if the color is null, empty, or only whitespace, otherwise {@code false}
     */
    @Override
    public boolean isFlyColorBlank(String flyColor) {
        if (flyColor == null || flyColor.isBlank()) {
            log.warn("Fly validation failed. Fly color is blank");
            return true;
        }

        return false;
    }

    /**
     * Checks whether a fly color is between 3 and 40 characters long.
     *
     * @param flyColor the fly color to check
     * @return {@code true} if the trimmed color is between 3 and 40 characters, otherwise {@code false}
     */
    @Override
    public boolean isFlyColorLengthValid(String flyColor) {
        if (flyColor == null) {
            log.warn("Fly validation failed. Fly color is null, so its length can't be checked");
            return false;
        }

        // trimmed so spaces around the color don't count toward its length
        int colorLength = flyColor.trim().length();

        if (colorLength < 3 || colorLength > 40) {
            log.warn("Fly validation failed. Fly color is {} characters, outside the 3 to 40 limit", colorLength);
            return false;
        }

        return true;
    }

    /**
     * Checks that a fly color isn't made up entirely of digits.
     *
     * <p>A color like "olive" or "black 2" passes. A color like "123" fails,
     * since it isn't a color at all.</p>
     *
     * @param flyColor the fly color to check
     * @return {@code true} if the color is not blank and has at least one character that isn't a digit, otherwise {@code false}
     */
    @Override
    public boolean isFlyColorValidString(String flyColor) {
        if (flyColor == null || flyColor.isBlank()) {
            log.warn("Fly validation failed. Fly color is blank, so it can't be checked for digits");
            return false;
        }

        String trimmedColor = flyColor.trim();

        // starts true and flips as soon as one character isn't a digit
        boolean isAllDigits = true;

        for (int i = 0; i < trimmedColor.length(); i++) {
            char currentCharacter = trimmedColor.charAt(i);

            if (!Character.isDigit(currentCharacter)) {
                isAllDigits = false;

                // one non-digit is enough, so there's no reason to check the rest
                break;
            }
        }

        if (isAllDigits) {
            log.warn("Fly validation failed. Fly color {} is only a number", flyColor);
            return false;
        }

        return true;
    }

    /**
     * Checks whether a minimum hook size is between 1 and 32.
     *
     * @param minSize the minimum hook size to check
     * @return {@code true} if the size is not null and between 1 and 32, otherwise {@code false}
     */
    @Override
    public boolean isMinSizeValidNumber(Integer minSize) {
        if (minSize == null) {
            log.warn("Fly validation failed. Min size is null");
            return false;
        }

        if (minSize < 1 || minSize > 32) {
            log.warn("Fly validation failed. Min size {} is outside the 1 to 32 limit", minSize);
            return false;
        }

        return true;
    }

    /**
     * Checks whether a maximum hook size is between 1 and 32.
     *
     * @param maxSize the maximum hook size to check
     * @return {@code true} if the size is not null and between 1 and 32, otherwise {@code false}
     */
    @Override
    public boolean isMaxSizeValidNumber(Integer maxSize) {
        if (maxSize == null) {
            log.warn("Fly validation failed. Max size is null");
            return false;
        }

        if (maxSize < 1 || maxSize > 32) {
            log.warn("Fly validation failed. Max size {} is outside the 1 to 32 limit", maxSize);
            return false;
        }

        return true;
    }

    /**
     * Checks whether a fly type is one of the allowed types.
     *
     * <p>The allowed types are "Dry", "Nymph", and "Streamer". The match is
     * exact and case-sensitive, so "dry" is not allowed.</p>
     *
     * @param flyType the fly type to check
     * @return {@code true} if the type exactly matches one of the allowed fly types, otherwise {@code false}
     */
    @Override
    public boolean isFlyTypeValid(String flyType) {
        if (flyType == null || flyType.isBlank()) {
            log.warn("Fly validation failed. Fly type is blank");
            return false;
        }

        // these match the option values on the fly forms
        List<String> validFlyTypes = List.of("Dry", "Nymph", "Streamer");

        for (String validType : validFlyTypes) {
            if (validType.equals(flyType)) {
                return true;
            }
        }

        log.warn("Fly validation failed. Fly type {} is not Dry, Nymph, or Streamer", flyType);
        return false;
    }

    /**
     * Checks whether a fly name is between 1 and 150 characters long.
     *
     * @param name the fly name to check
     * @return {@code true} if the trimmed name is between 1 and 150 characters, otherwise {@code false}
     */
    @Override
    public boolean isFlyNameLengthValid(String name) {
        if (name == null) {
            log.warn("Fly validation failed. Fly name is null, so its length can't be checked");
            return false;
        }

        // trimmed so a name of only spaces counts as 0 characters and fails
        int nameLength = name.trim().length();

        if (nameLength < 1 || nameLength > 150) {
            log.warn("Fly validation failed. Fly name is {} characters, outside the 1 to 150 limit", nameLength);
            return false;
        }

        return true;
    }

    /**
     * Checks whether a fly exists and is a custom fly that belongs to a user.
     *
     * <p>Default flies have no owner, so this is always {@code false} for them.</p>
     *
     * @param id the ID of the fly to check
     * @param userId the ID of the user to check ownership against
     * @return {@code true} if the fly exists and the user made it, otherwise {@code false}
     */
    @Override
    public boolean isFlyOwner(Long id, Long userId) {
        // findById throws on a null ID instead of returning empty, so this has to be checked first
        if (id == null) {
            log.warn("Fly ownership check failed for user ID {}. Fly ID is null", userId);
            return false;
        }

        Fly fly = flyRepository.findById(id).orElse(null);

        if (fly == null) {
            log.warn("Fly ownership check failed for user ID {}. Fly ID {} does not exist", userId, id);
            return false;
        }

        // default flies have a null user ID, so they never belong to anyone and can't be edited or deleted
        // userId goes first in equals so a null user ID can't match a default fly's null owner
        if (userId == null || !userId.equals(fly.getUserId())) {
            log.warn("Fly ownership check failed. Fly ID {} does not belong to user ID {}", id, userId);
            return false;
        }

        return true;
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

        // stops at the first failed check, each validator logs its own reason
        if (isFlyNameBlank(flyInput.name())) {
            return false;
        }

        if (!isFlyNameLengthValid(flyInput.name())) {
            return false;
        }

        if (!isFlyTypeValid(flyInput.type())) {
            return false;
        }

        if (isFlyColorBlank(flyInput.color())) {
            return false;
        }

        if (!isFlyColorLengthValid(flyInput.color())) {
            return false;
        }

        if (!isFlyColorValidString(flyInput.color())) {
            return false;
        }

        if (!isMinSizeValidNumber(flyInput.minSize())) {
            return false;
        }

        if (!isMaxSizeValidNumber(flyInput.maxSize())) {
            return false;
        }

        // last, since it only makes sense to check once both sizes are known to be valid numbers
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

        // these three come from the service, not the user
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
     * @param name the fly name to build the slug from, which must not be null
     * @return the generated slug
     */
    private String generateFlySlug(String name) {
        // \\s+ matches one or more whitespace characters, so double spaces still become one hyphen
        return name.trim().toLowerCase().replaceAll("\\s+", "-");
    }

    /**
     * Picks the image file for a fly type.
     *
     * <p>Any type other than "Dry" or "Streamer" uses the nymph image.</p>
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