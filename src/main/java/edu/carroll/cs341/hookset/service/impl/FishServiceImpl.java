package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.jpa.repo.FishWaterBodyRepository;
import edu.carroll.cs341.hookset.service.FishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import shared.jpa.entity.Fish;

import java.util.List;

/**
 * Service implementation for looking up fish species.
 *
 * <p>Fish can be looked up as a full list or filtered to the species found
 * in a single water body. Fish are read-only here, since they come from
 * the importer rather than from users.</p>
 */
@Service
public class FishServiceImpl implements FishService {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(FishServiceImpl.class);

    /** The repository used to look up fish species. */
    private final FishRepository fishRepository;

    /** The repository used to look up which fish are in which water bodies. */
    private final FishWaterBodyRepository fishWaterBodyRepository;

    /**
     * Creates the fish service with the repositories it depends on.
     *
     * @param fishRepository the repository used to look up fish species
     * @param fishWaterBodyRepository the repository used to look up which fish are in which water bodies
     */
    public FishServiceImpl(FishRepository fishRepository, FishWaterBodyRepository fishWaterBodyRepository) {
        this.fishRepository = fishRepository;
        this.fishWaterBodyRepository = fishWaterBodyRepository;
    }

    /**
     * Returns every fish species in the database.
     *
     * @return the list of fish species, or an empty list if none exist
     */
    @Override
    public List<Fish> getAllFish() {
        log.info("Loading all fish");

        List<Fish> fish = fishRepository.findAll();

        // an empty fish table usually means the importer has not been run
        if (fish.size() < 1) {
            log.warn("No fish found in the database");
        }

        log.info("Loaded {} fish", fish.size());

        return fish;
    }

    /**
     * Returns every fish species found in a water body.
     *
     * @param waterBodyId the ID of the water body
     * @return the list of fish species in the water body, or an empty list if none exist
     */
    @Override
    public List<Fish> getAllFishForWaterBody(Long waterBodyId) {
        log.info("Loading fish for water body ID: {}", waterBodyId);

        List<Fish> fish = fishWaterBodyRepository.findFishByWaterBodyId(waterBodyId);

        if (fish.size() < 1) {
            log.warn("No fish found for water body ID: {}", waterBodyId);
        }

        log.info("Loaded {} fish for water body ID: {}", fish.size(), waterBodyId);

        return fish;
    }
}