package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.service.FishService;
import edu.carroll.cs341.hookset.service.input.FishInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import entities.jpa.entity.Fish;

import java.util.List;

/**
 * Service implementation for looking up and adding fish species.
 *
 * <p>Fish aren't owned by any user, so every user sees the same list. The fish
 * found in a single water body are loaded through
 * {@link edu.carroll.cs341.hookset.service.WaterBodyService}, since that
 * lookup goes through the water body.</p>
 */
@Service
public class FishServiceImpl implements FishService {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(FishServiceImpl.class);

    /** The longest family or species name allowed, matching the database column. */
    private static final int MAX_NAME_LENGTH = 200;

    /** The longest image file name allowed, matching the database column. */
    private static final int MAX_IMAGE_LENGTH = 100;

    /** The repository used to look up and save fish species. */
    private final FishRepository fishRepository;

    /**
     * Creates the fish service with the repository it depends on.
     *
     * @param fishRepository the repository used to look up and save fish species
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
        log.info("Loading every fish species");

        // fish aren't owned by anyone, so every user gets the same full list
        List<Fish> fish = fishRepository.findAll();

        // an empty fish table usually means the seed script has not been run
        if (fish.size() < 1) {
            log.warn("No fish species found in the database. The seed script may not have been run");
        }

        log.info("Loaded {} fish species", fish.size());

        return fish;
    }

    /**
     * Adds a new fish species.
     *
     * <p>This method was added specifically for testing. Tests can only depend on
     * services, not repositories, so this gives them a way to create fish test data.
     * The app itself does not add fish yet.</p>
     *
     * <p>The family, species, and image are all required and can't be blank.
     * The family and species can be at most 200 characters, and the image at
     * most 100. A fish with the same family and species as an existing fish
     * is not added.</p>
     *
     * @param fishInput the details of the fish to add
     * @return {@code true} if the fish was saved, otherwise {@code false}
     */
    @Override
    public boolean addFish(FishInput fishInput) {
        if (fishInput == null) {
            log.warn("Fish not saved. The input was null");
            return false;
        }

        // ensure all text fields are valid before saving to repo
        if (!isTextValid(fishInput.family(), MAX_NAME_LENGTH)) {
            log.warn("Fish not saved. The family was missing, blank, or longer than {} characters",
                    MAX_NAME_LENGTH);
            return false;
        }

        if (!isTextValid(fishInput.species(), MAX_NAME_LENGTH)) {
            log.warn("Fish not saved. The species was missing, blank, or longer than {} characters",
                    MAX_NAME_LENGTH);
            return false;
        }

        if (!isTextValid(fishInput.image(), MAX_IMAGE_LENGTH)) {
            log.warn("Fish not saved. The image was missing, blank, or longer than {} characters",
                    MAX_IMAGE_LENGTH);
            return false;
        }

        // the same family and species is the same fish, matching Fish.equals
        if (fishRepository.existsByFamilyAndSpecies(fishInput.family(), fishInput.species())) {
            log.warn("Fish not saved. A fish with family {} and species {} already exists",
                    fishInput.family(), fishInput.species());
            return false;
        }

        Fish fish = new Fish();
        fish.setFamily(fishInput.family());
        fish.setSpecies(fishInput.species());
        fish.setImage(fishInput.image());

        log.info("Saving fish with family {} and species {}", fish.getFamily(), fish.getSpecies());

        fishRepository.save(fish);

        log.info("Saved fish ID {} with species {}", fish.getId(), fish.getSpecies());
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
}