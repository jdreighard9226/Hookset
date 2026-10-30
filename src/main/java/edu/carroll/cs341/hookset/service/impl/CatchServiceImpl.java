package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.*;
import edu.carroll.cs341.hookset.service.CatchService;
import edu.carroll.cs341.hookset.service.input.CatchInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import shared.jpa.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that handles loading, adding, and editing catches.
 *
 * <p>Every catch belongs to the user who logged it. The user is always passed
 * in by the caller, so this service doesn't depend on how they logged in.
 * Before a catch is saved, the fly, fish, and water body are checked to make
 * sure they exist, the fly is checked to make sure the user is allowed to use
 * it, and the fish is checked to make sure it actually lives in the chosen
 * water body.</p>
 *
 * <p>Catches are returned as entities. The fly, fish, and water body are
 * mapped on the catch itself, so callers can read them straight off it.</p>
 *
 * <p>Sources Used:</p>
 * <ul>
 *     <li>How to store SQL DECIMAL values in Java:
 *     https://stackoverflow.com/questions/35702026/sql-decimal-to-java-number</li>
 * </ul>
 */
@Service
public class CatchServiceImpl implements CatchService {
    /** The logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CatchServiceImpl.class);

    /** The largest fish length the Catch table can store, from DECIMAL(5,2). */
    private static final BigDecimal MAX_FISH_LENGTH = new BigDecimal("999.99");

    /** The most characters the notes column can store. */
    private static final int MAX_NOTES_LENGTH = 1000;

    /** The repository used to look up and save catches. */
    private final CatchRepository catchRepository;

    /** The repository used to look up fish species. */
    private final FishRepository fishRepository;

    /** The repository used to look up flies. */
    private final FlyRepository flyRepository;

    /** The repository used to look up water bodies. */
    private final WaterBodyRepository waterBodyRepository;

    /**
     * Creates the catch service with the repositories it needs.
     *
     * @param catchRepository the repository used to look up and save catches
     * @param fishRepository the repository used to look up fish species
     * @param flyRepository the repository used to look up flies
     * @param waterBodyRepository the repository used to look up water bodies
     */
    public CatchServiceImpl(CatchRepository catchRepository,
                            FishRepository fishRepository,
                            FlyRepository flyRepository,
                            WaterBodyRepository waterBodyRepository) {

        this.catchRepository = catchRepository;
        this.fishRepository = fishRepository;
        this.flyRepository = flyRepository;
        this.waterBodyRepository = waterBodyRepository;
    }

    /**
     * Loads every catch from every user.
     *
     * @return the list of all catches, or an empty list if there are none
     */
    @Override
    public List<Catch> getAllCatches() {
        List<Catch> catches = catchRepository.findAll();

        log.info("Loaded {} catches", catches.size());

        return catches;
    }

    /**
     * Loads every catch that belongs to a user.
     *
     * @param userId the ID of the user whose catches to load
     * @return the list of the user's catches, or an empty list if the user ID is null
     */
    @Override
    public List<Catch> getAllUserCatches(Long userId) {
        // a null user ID would turn this into an IS NULL query, so there's nothing to show
        if (userId == null) {
            log.warn("Could not load user catches. User ID is null");
            return new ArrayList<>();
        }

        List<Catch> catches = catchRepository.findByUserId(userId);

        log.info("Loaded {} catches for user ID: {}", catches.size(), userId);

        return catches;
    }

    /**
     * Loads a single catch by its ID.
     *
     * @param catchId the ID of the catch to load
     * @return the catch, or {@code null} if the ID is null or no catch was found
     */
    @Override
    public Catch getCatchById(Long catchId) {
        if (catchId == null) {
            log.warn("Catch lookup failed. Catch ID is null");
            return null;
        }

        Catch catchRecord = catchRepository.findById(catchId).orElse(null);

        if (catchRecord == null) {
            log.warn("Catch not found for catch ID: {}", catchId);
            return null;
        }

        log.info("Loaded catch ID: {}", catchId);

        return catchRecord;
    }

    /**
     * Saves a new catch for a user.
     *
     * <p>The catch is only saved if a user ID is given and every check on
     * the input passes. The owner always comes from the user ID parameter,
     * never from the input.</p>
     *
     * @param catchInput the details of the new catch
     * @param userId the ID of the user logging the catch
     * @return {@code true} if the catch was saved, otherwise {@code false}
     */
    @Override
    public boolean addCatch(CatchInput catchInput, Long userId) {
        // a catch has to belong to someone
        if (userId == null) {
            log.warn("Catch not saved. User ID is null");
            return false;
        }

        if (!isCatchInputValid(catchInput, userId)) {
            return false;
        }

        Catch catchRecord = new Catch();

        // owner comes from the caller, not the input, so nobody can save a catch as someone else
        catchRecord.setUserId(userId);
        copyInputToCatch(catchInput, catchRecord);

        catchRepository.save(catchRecord);

        log.info("Catch saved for user ID: {}", userId);
        return true;
    }

    /**
     * Updates a catch owned by a user.
     *
     * <p>Ownership is checked before the input, so bad input can't reveal
     * whether another user's catch exists. The owner is never changed.</p>
     *
     * @param catchId the ID of the catch to update
     * @param catchInput the new catch details
     * @param userId the ID of the user editing the catch
     * @return {@code true} if the catch was updated, otherwise {@code false}
     */
    @Override
    public boolean updateCatch(Long catchId, CatchInput catchInput, Long userId) {
        // ownership first, so bad input can't tell someone whether another user's catch exists
        if (!isCatchOwner(catchId, userId)) {
            return false;
        }

        if (!isCatchInputValid(catchInput, userId)) {
            return false;
        }

        // the ownership check already confirmed this catch exists
        Catch catchRecord = catchRepository.findById(catchId).orElse(null);

        copyInputToCatch(catchInput, catchRecord);

        catchRepository.save(catchRecord);

        log.info("Catch ID {} updated", catchId);
        return true;
    }

    /**
     * Checks that a catch exists and belongs to a user.
     *
     * @param catchId the ID of the catch to check
     * @param userId the ID of the user to check ownership against
     * @return {@code true} if the catch exists and belongs to the user, otherwise {@code false}
     */
    @Override
    public boolean isCatchOwner(Long catchId, Long userId) {
        if (catchId == null) {
            log.warn("Catch ownership check failed. Catch ID is null");
            return false;
        }

        Catch catchRecord = catchRepository.findById(catchId).orElse(null);

        if (catchRecord == null) {
            log.warn("Catch not found for catch ID: {}", catchId);
            return false;
        }

        if (userId == null || !userId.equals(catchRecord.getUserId())) {
            log.warn("Catch ID {} does not belong to user ID: {}", catchId, userId);
            return false;
        }

        return true;
    }

    /**
     * Checks that a fly exists and that a user is allowed to use it.
     *
     * <p>Default flies have no owner and can be used by anyone. Custom flies
     * can only be used by the user who made them.</p>
     *
     * @param flyId the ID of the fly to check
     * @param userId the ID of the user who wants to use the fly
     * @return {@code true} if the fly exists and the user can use it, otherwise {@code false}
     */
    @Override
    public boolean isFlyValid(Long flyId, Long userId) {
        if (flyId == null) {
            log.warn("Catch validation failed. Fly ID is null");
            return false;
        }

        Fly fly = flyRepository.findById(flyId).orElse(null);

        if (fly == null) {
            log.warn("Catch validation failed. Fly not found for fly ID: {}", flyId);
            return false;
        }

        // default flies have no owner and anyone can use them
        if (fly.getUserId() == null) {
            return true;
        }

        // custom flies can only be used by the user who made them
        if (!fly.getUserId().equals(userId)) {
            log.warn("Catch validation failed. Fly ID {} belongs to another user", flyId);
            return false;
        }

        return true;
    }

    /**
     * Checks that a fish species exists.
     *
     * @param fishId the ID of the fish to check
     * @return {@code true} if the fish exists, otherwise {@code false}
     */
    @Override
    public boolean isFishValid(Long fishId) {
        if (fishId == null || !fishRepository.existsById(fishId)) {
            log.warn("Catch validation failed. Fish not found for fish ID: {}", fishId);
            return false;
        }

        return true;
    }

    /**
     * Checks that a water body exists.
     *
     * @param waterBodyId the ID of the water body to check
     * @return {@code true} if the water body exists, otherwise {@code false}
     */
    @Override
    public boolean isWaterBodyValid(Long waterBodyId) {
        if (waterBodyId == null || !waterBodyRepository.existsById(waterBodyId)) {
            log.warn("Catch validation failed. Water body not found for water body ID: {}", waterBodyId);
            return false;
        }

        return true;
    }

    /**
     * Checks that a fish species lives in a water body.
     *
     * <p>The fish are loaded through the water body's {@code @ManyToMany}
     * relationship, which reads the FishWaterBody join table.</p>
     *
     * @param fishId the ID of the fish to check
     * @param waterBodyId the ID of the water body to check
     * @return {@code true} if the fish lives in the water body, otherwise {@code false}
     */
    @Override
    public boolean isFishInWaterBody(Long fishId, Long waterBodyId) {
        if (fishId == null || waterBodyId == null) {
            log.warn("Catch validation failed. Fish ID or water body ID is null");
            return false;
        }

        WaterBody waterBody = waterBodyRepository.findById(waterBodyId).orElse(null);

        if (waterBody == null) {
            log.warn("Catch validation failed. Water body not found for water body ID: {}", waterBodyId);
            return false;
        }

        // look through every fish in the water body for a matching ID
        for (Fish fish : waterBody.getFishes()) {
            if (fishId.equals(fish.getId())) {
                return true;
            }
        }

        log.warn("Catch validation failed. Fish ID {} is not found in water body ID {}", fishId, waterBodyId);
        return false;
    }

    /**
     * Checks that a fish length fits in the Catch table.
     *
     * <p>Length is optional. If one is given it has to be greater than zero,
     * no more than {@link #MAX_FISH_LENGTH}, and have at most two decimal places.</p>
     *
     * @param fishLength the fish length to check
     * @return {@code true} if the length is empty or valid, otherwise {@code false}
     */
    @Override
    public boolean isFishLengthValid(BigDecimal fishLength) {
        // length is optional
        if (fishLength == null) {
            return true;
        }

        if (fishLength.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Catch validation failed. Fish length is not greater than zero: {}", fishLength);
            return false;
        }

        if (fishLength.compareTo(MAX_FISH_LENGTH) > 0) {
            log.warn("Catch validation failed. Fish length is over {}: {}", MAX_FISH_LENGTH, fishLength);
            return false;
        }

        // scale is the number of digits after the decimal point
        if (fishLength.scale() > 2) {
            log.warn("Catch validation failed. Fish length has more than two decimals: {}", fishLength);
            return false;
        }

        return true;
    }

    /**
     * Checks that a catch date is given and is not in the future.
     *
     * @param dateCaught the date and time the fish was caught
     * @return {@code true} if the date is valid, otherwise {@code false}
     */
    @Override
    public boolean isDateCaughtValid(LocalDateTime dateCaught) {
        if (dateCaught == null) {
            log.warn("Catch validation failed. Date caught is null");
            return false;
        }

        if (dateCaught.isAfter(LocalDateTime.now())) {
            log.warn("Catch validation failed. Date caught is in the future: {}", dateCaught);
            return false;
        }

        return true;
    }

    /**
     * Checks that catch notes fit in the notes column.
     *
     * @param notes the notes to check
     * @return {@code true} if the notes are empty or short enough, otherwise {@code false}
     */
    @Override
    public boolean isNotesValid(String notes) {
        // notes are optional
        if (notes == null) {
            return true;
        }

        if (notes.length() > MAX_NOTES_LENGTH) {
            log.warn("Catch validation failed. Notes exceed {} characters", MAX_NOTES_LENGTH);
            return false;
        }

        return true;
    }

    /**
     * Runs every check a catch must pass before it is saved, no matter who calls the service.
     *
     * @param catchInput the catch details to check
     * @param userId the ID of the user saving the catch, used to check fly ownership
     * @return {@code true} if every check passes, otherwise {@code false}
     */
    private boolean isCatchInputValid(CatchInput catchInput, Long userId) {
        if (catchInput == null || userId == null) {
            log.warn("Catch validation failed. Catch input or user ID is null");
            return false;
        }

        if (!isFlyValid(catchInput.flyId(), userId)) {
            return false;
        }

        if (!isFishValid(catchInput.fishId())) {
            return false;
        }

        if (!isWaterBodyValid(catchInput.waterBodyId())) {
            return false;
        }

        if (!isFishInWaterBody(catchInput.fishId(), catchInput.waterBodyId())) {
            return false;
        }

        if (!isFishLengthValid(catchInput.fishLength())) {
            return false;
        }

        if (!isDateCaughtValid(catchInput.dateCaught())) {
            return false;
        }

        if (!isNotesValid(catchInput.notes())) {
            return false;
        }

        return true;
    }

    /**
     * Copies the editable fields from the input onto a catch.
     *
     * <p>The user ID is not copied, so an edit can't move a catch to another user.
     * This only runs after {@link #isCatchInputValid(CatchInput, Long)} passes,
     * so the fly, fish, and water body are known to exist.</p>
     *
     * @param catchInput the catch details to copy from
     * @param catchRecord the catch to copy onto
     */
    private void copyInputToCatch(CatchInput catchInput, Catch catchRecord) {
        catchRecord.setFly(flyRepository.findById(catchInput.flyId()).orElse(null));
        catchRecord.setFish(fishRepository.findById(catchInput.fishId()).orElse(null));
        catchRecord.setWaterBody(waterBodyRepository.findById(catchInput.waterBodyId()).orElse(null));
        catchRecord.setFishLength(catchInput.fishLength());
        catchRecord.setDateCaught(catchInput.dateCaught());
        catchRecord.setNotes(catchInput.notes());
    }
}