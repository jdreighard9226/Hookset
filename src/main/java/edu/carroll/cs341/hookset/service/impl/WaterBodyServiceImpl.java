package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import edu.carroll.cs341.hookset.service.WaterBodyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import entities.jpa.entity.Fish;
import entities.jpa.entity.WaterBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for looking up water bodies and the fish found in them.
 *
 * <p>Water bodies can be looked up as a full list or individually by their
 * URL slug. The fish in a single water body are loaded here too, since that
 * lookup goes through the water body's {@code @ManyToMany} relationship.</p>
 *
 * <p>Water bodies are read-only here, since they come from the seed script
 * rather than from users.</p>
 */
@Service
public class WaterBodyServiceImpl implements WaterBodyService {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(WaterBodyServiceImpl.class);

    /** The repository used to look up water bodies. */
    private final WaterBodyRepository waterBodyRepository;

    /**
     * Creates the water body service with the repository it depends on.
     *
     * @param waterBodyRepository the repository used to look up water bodies
     */
    public WaterBodyServiceImpl(WaterBodyRepository waterBodyRepository) {
        this.waterBodyRepository = waterBodyRepository;
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
}