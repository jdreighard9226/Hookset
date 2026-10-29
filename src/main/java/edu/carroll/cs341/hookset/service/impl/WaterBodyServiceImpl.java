package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import edu.carroll.cs341.hookset.service.WaterBodyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import shared.jpa.entity.Fish;
import shared.jpa.entity.WaterBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for looking up water bodies.
 *
 * <p>Water bodies can be looked up as a full list or individually by their
 * URL slug. Water bodies are read-only here, since they come from the
 * importer rather than from users.</p>
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
        log.info("Loading all water bodies");

        List<WaterBody> waterBodies = waterBodyRepository.findAll();

        // an empty water body table usually means the importer has not been run
        if (waterBodies.size() < 1) {
            log.warn("No water bodies found in the database");
        }

        log.info("Loaded {} water bodies", waterBodies.size());

        return waterBodies;
    }

    /**
     * Returns a single water body by its URL slug.
     *
     * @param waterBodySlug the slug used in the water body's URL
     * @return the matching water body, or {@code null} if the slug is blank or no water body matches
     */
    @Override
    public WaterBody getWaterBodyFromSlug(String waterBodySlug) {
        if (waterBodySlug == null || waterBodySlug.isBlank()) {
            log.warn("Water body lookup failed. Slug is blank");
            return null;
        }

        log.info("Loading water body for slug: {}", waterBodySlug);

        WaterBody waterBody = waterBodyRepository.getWaterBodyBySlug(waterBodySlug);

        if (waterBody == null) {
            log.warn("No water body found for slug: {}", waterBodySlug);
        }

        return waterBody;
    }

    /**
     * Returns every fish species found in a water body.
     *
     * <p>The fish are loaded through the water body's {@code @ManyToMany}
     * relationship, which reads the FishWaterBody join table.</p>
     *
     * @param waterBodyId the ID of the water body
     * @return the list of fish species in the water body, or an empty list if the water body does not exist or has no fish
     */
    /**
     * Returns every fish species found in a water body.
     *
     * <p>The fish are loaded through the water body's {@code @ManyToMany}
     * relationship, which reads the FishWaterBody join table.</p>
     *
     * @param waterBodyId the ID of the water body
     * @return the list of fish species in the water body, or an empty list if the water body does not exist or has no fish
     */
    @Override
    public List<Fish> getAllFishForWaterBody(Long waterBodyId) {
        log.info("Loading fish for water body ID: {}", waterBodyId);

        WaterBody waterBody = waterBodyRepository.findById(waterBodyId).orElse(null);

        if (waterBody == null) {
            log.warn("Water body not found for ID: {}", waterBodyId);
            return new ArrayList<>();
        }

        List<Fish> fish = new ArrayList<>(waterBody.getFishes());

        if (fish.size() < 1) {
            log.warn("No fish found for water body ID: {}", waterBodyId);
        }

        log.info("Loaded {} fish for water body ID: {}", fish.size(), waterBodyId);

        return fish;
    }
}