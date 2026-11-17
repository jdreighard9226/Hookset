package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import edu.carroll.cs341.hookset.service.WaterBodyService;
import edu.carroll.cs341.hookset.service.input.WaterBodyInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import entities.jpa.entity.Fish;
import entities.jpa.entity.WaterBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for looking up and adding water bodies and the fish found in them.
 *
 * <p>Water bodies can be looked up as a full list or individually by their
 * URL slug. The fish in a single water body are loaded here too, since that
 * lookup goes through the water body's {@code @ManyToMany} relationship.</p>
 *
 * <p>Water bodies own the link to their fish, so adding a water body is also
 * where it gets linked to the fish found in it.</p>
 */
@Service
public class WaterBodyServiceImpl implements WaterBodyService {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(WaterBodyServiceImpl.class);

    /** The longest water body name allowed, matching the database column. */
    private static final int MAX_NAME_LENGTH = 250;

    /** The longest state allowed, matching the database column. */
    private static final int MAX_STATE_LENGTH = 100;

    /** The longest description allowed, matching the database column. */
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    /** The longest slug allowed, matching the database column. */
    private static final int MAX_SLUG_LENGTH = 100;

    /** The only water body types the database allows. */
    private static final List<String> VALID_TYPES = List.of("River", "Lake", "Stream");

    /** The repository used to look up and save water bodies. */
    private final WaterBodyRepository waterBodyRepository;

    /** The repository used to load the fish a new water body is linked to. */
    private final FishRepository fishRepository;

    /**
     * Creates the water body service with the repositories it depends on.
     *
     * @param waterBodyRepository the repository used to look up and save water bodies
     * @param fishRepository the repository used to load the fish a new water body is linked to
     */
    public WaterBodyServiceImpl(WaterBodyRepository waterBodyRepository, FishRepository fishRepository) {
        this.waterBodyRepository = waterBodyRepository;
        this.fishRepository = fishRepository;
    }

    /**
     * Returns every water body in the database.
     *
     * @return the list of water bodies, or an empty list if none exist
     */
    @Override
    public List<WaterBody> getAllWaterBodies() {
        log.info("Loading every water body");

        // water bodies aren't owned by anyone, so every user gets the same full list
        List<WaterBody> waterBodies = waterBodyRepository.findAll();

        // an empty water body table usually means the seed script has not been run
        if (waterBodies.size() < 1) {
            log.warn("No water bodies found in the database. The seed script may not have been run");
        }

        log.info("Loaded {} water bodies", waterBodies.size());

        return waterBodies;
    }

    /**
     * Returns a single water body by its URL slug.
     *
     * <p>The slug is lowercased before the lookup, since every saved slug is
     * lowercase. That way a URL typed in the wrong case still finds the water body.</p>
     *
     * @param waterBodySlug the slug used in the water body's URL
     * @return the matching water body, or {@code null} if the slug is blank or no water body matches
     */
    @Override
    public WaterBody getWaterBodyFromSlug(String waterBodySlug) {
        // a blank slug can't match anything, and this avoids calling toLowerCase on null
        if (waterBodySlug == null || waterBodySlug.isBlank()) {
            log.warn("Water body lookup failed. Slug is blank");
            return null;
        }

        // lowercased so the result doesn't depend on the database's case sensitivity
        String lowercaseSlug = waterBodySlug.toLowerCase();

        log.info("Looking up water body slug {}", lowercaseSlug);

        WaterBody waterBody = waterBodyRepository.getWaterBodyBySlug(lowercaseSlug);

        if (waterBody == null) {
            log.warn("Water body lookup failed. No water body matches slug {} (requested as {})",
                    lowercaseSlug, waterBodySlug);
            return null;
        }

        log.info("Slug {} matched water body ID {}", lowercaseSlug, waterBody.getId());

        return waterBody;
    }

    /**
     * Returns every fish species found in a water body.
     *
     * <p>The fish are loaded through the water body's {@code @ManyToMany}
     * relationship, which reads the FishWaterBody join table.</p>
     *
     * @param waterBodyId the ID of the water body
     * @return the list of fish species in the water body, or an empty list if the ID is null,
     *         the water body does not exist, or it has no fish
     */
    @Override
    public List<Fish> getAllFishForWaterBody(Long waterBodyId) {
        // findById throws on a null ID instead of returning empty, so this has to be checked first
        if (waterBodyId == null) {
            log.warn("Fish lookup failed. Water body ID is null");
            return new ArrayList<>();
        }

        log.info("Loading fish for water body ID {}", waterBodyId);

        WaterBody waterBody = waterBodyRepository.findById(waterBodyId).orElse(null);

        if (waterBody == null) {
            log.warn("Fish lookup failed. Water body ID {} does not exist", waterBodyId);
            return new ArrayList<>();
        }

        // the @ManyToMany is a Set, so it's copied into a List to match what callers expect
        // a new list also means callers can't change the water body's fish by editing the result
        List<Fish> fish = new ArrayList<>(waterBody.getFishes());

        // every seeded water body should have at least one fish, so an empty list is worth flagging
        if (fish.size() < 1) {
            log.warn("No fish found for water body ID {}", waterBodyId);
        }

        log.info("Loaded {} fish for water body ID {}", fish.size(), waterBodyId);

        return fish;
    }

    /**
     * Adds a new water body, linked to the fish found in it.
     *
     * <p>This method was added specifically for testing. Tests can only depend on
     * services, not repositories, so this gives them a way to create water body
     * test data. The app itself does not add water bodies yet, which is also why
     * the caller provides the slug instead of it being generated.</p>
     *
     * <p>The name, type, state, description, and slug are all required and can't be
     * blank. The name can be at most 250 characters, the state 100, the description
     * 1000, and the slug 100. The type must be exactly River, Lake, or Stream. The
     * slug is saved in lowercase and can't match an existing water body's slug.</p>
     *
     * <p>The fish IDs are optional, so a water body can be added with no fish. If any
     * fish ID is null or doesn't match a fish, the water body is not added.</p>
     *
     * @param waterBodyInput the details of the water body to add
     * @return {@code true} if the water body was saved, otherwise {@code false}
     */
    @Override
    public boolean addWaterBody(WaterBodyInput waterBodyInput) {
        if (waterBodyInput == null) {
            log.warn("Water body not saved. The input was null");
            return false;
        }

        if (!isTextValid(waterBodyInput.name(), MAX_NAME_LENGTH)) {
            log.warn("Water body not saved. The name was missing, blank, or longer than {} characters",
                    MAX_NAME_LENGTH);
            return false;
        }

        // the database only allows these exact values, so anything else would fail on save
        if (!VALID_TYPES.contains(waterBodyInput.type())) {
            log.warn("Water body not saved. The type {} is not River, Lake, or Stream", waterBodyInput.type());
            return false;
        }

        if (!isTextValid(waterBodyInput.state(), MAX_STATE_LENGTH)) {
            log.warn("Water body not saved. The state was missing, blank, or longer than {} characters",
                    MAX_STATE_LENGTH);
            return false;
        }

        if (!isTextValid(waterBodyInput.description(), MAX_DESCRIPTION_LENGTH)) {
            log.warn("Water body not saved. The description was missing, blank, or longer than {} characters",
                    MAX_DESCRIPTION_LENGTH);
            return false;
        }

        if (!isTextValid(waterBodyInput.slug(), MAX_SLUG_LENGTH)) {
            log.warn("Water body not saved. The slug was missing, blank, or longer than {} characters",
                    MAX_SLUG_LENGTH);
            return false;
        }

        // lowercased because getWaterBodyFromSlug lowercases every lookup
        String lowercaseSlug = waterBodyInput.slug().toLowerCase();

        // the slug column is unique, so a duplicate would throw on save instead of failing cleanly
        if (waterBodyRepository.existsBySlug(lowercaseSlug)) {
            log.warn("Water body not saved. A water body with slug {} already exists", lowercaseSlug);
            return false;
        }

        // every fish is loaded before anything is saved, so one bad ID stops the whole add
        List<Fish> fishToLink = new ArrayList<>();
        if (waterBodyInput.fishIds() != null) {
            for (Long fishId : waterBodyInput.fishIds()) {
                // findById throws on a null ID instead of returning empty, so this has to be checked first
                if (fishId == null) {
                    log.warn("Water body {} not saved. One of the fish IDs was null", lowercaseSlug);
                    return false;
                }

                Fish fish = fishRepository.findById(fishId).orElse(null);

                if (fish == null) {
                    log.warn("Water body {} not saved. Fish ID {} does not exist", lowercaseSlug, fishId);
                    return false;
                }

                fishToLink.add(fish);
            }
        }

        WaterBody waterBody = toWaterBody(waterBodyInput, lowercaseSlug);

        // water body owns the @ManyToMany, so adding the fish here is what writes the FishWaterBody rows
        for (Fish fish : fishToLink) {
            waterBody.getFishes().add(fish);
        }

        log.info("Saving water body {} with slug {} and {} fish",
                waterBody.getName(), lowercaseSlug, waterBody.getFishes().size());

        waterBodyRepository.save(waterBody);

        log.info("Saved water body ID {} with slug {}", waterBody.getId(), waterBody.getSlug());
        return true;
    }

    /**
     * Checks that a required text is present, not blank, and short enough to save.
     *
     * @param text the text to check
     * @param maxLength the longest the text is allowed to be
     * @return {@code true} if the text can be saved, otherwise {@code false}
     */
    private boolean isTextValid(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return false;
        }

        if (text.length() > maxLength) {
            return false;
        }

        return true;
    }

    /**
     * Builds a new, unsaved water body from the given input, with no fish linked yet.
     *
     * @param waterBodyInput the details of the water body to build
     * @param lowercaseSlug the slug to save, already lowercased
     * @return a new water body with the name, type, state, description, and slug from the input
     */
    private WaterBody toWaterBody(WaterBodyInput waterBodyInput, String lowercaseSlug) {
        WaterBody waterBody = new WaterBody();

        waterBody.setName(waterBodyInput.name());
        waterBody.setType(waterBodyInput.type());
        waterBody.setState(waterBodyInput.state());
        waterBody.setDescription(waterBodyInput.description());
        waterBody.setSlug(lowercaseSlug);

        return waterBody;
    }
}