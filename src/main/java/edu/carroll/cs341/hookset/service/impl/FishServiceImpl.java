package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.service.FishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import shared.jpa.entity.Fish;

import java.util.List;

/**
 * Service implementation for looking up fish species.
 *
 * <p>Fish are read-only here, since they come from the seed script rather
 * than from users. The fish found in a single water body are loaded through
 * {@link edu.carroll.cs341.hookset.service.WaterBodyService}, since that
 * lookup goes through the water body.</p>
 */
@Service
public class FishServiceImpl implements FishService {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(FishServiceImpl.class);

    /** The repository used to look up fish species. */
    private final FishRepository fishRepository;

    /**
     * Creates the fish service with the repository it depends on.
     *
     * @param fishRepository the repository used to look up fish species
     */
    public FishServiceImpl(FishRepository fishRepository) {
        this.fishRepository = fishRepository;
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

        // an empty fish table usually means the seed script has not been run
        if (fish.size() < 1) {
            log.warn("No fish found in the database");
        }

        log.info("Loaded {} fish", fish.size());

        return fish;
    }
}