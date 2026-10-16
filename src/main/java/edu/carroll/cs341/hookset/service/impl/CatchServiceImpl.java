package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.*;
import edu.carroll.cs341.hookset.service.CatchService;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.dto.*;
import edu.carroll.cs341.hookset.web.form.CatchForm;
import edu.carroll.cs341.hookset.web.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import shared.jpa.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that handles loading, adding, and editing catches.
 *
 * <p>Every catch belongs to the logged-in user. Before a catch is saved, the
 * fly, fish, and water body are checked to make sure they exist, the fly is
 * checked to make sure the user is allowed to use it, and the fish is checked
 * to make sure it actually lives in the chosen water body.</p>
 *
 * <p>Catches are returned as DTOs that include the full fly, fish, and water
 * body instead of just their IDs, so the views can show names without doing
 * their own lookups.</p>
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

    /** The largest fish length the Catches table can store, from DECIMAL(5,2). */
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

    /** The repository used to look up which fish live in which water bodies. */
    private final FishWaterBodyRepository fishWaterBodyRepository;

    /** The mapper used to build catch DTOs. */
    private final CatchMapper catchMapper;

    /** The mapper used to build fish DTOs. */
    private final FishMapper fishMapper;

    /** The mapper used to build fly DTOs. */
    private final FlyMapper flyMapper;

    /** The mapper used to build water body DTOs. */
    private final WaterBodyMapper waterBodyMapper;

    /**
     * Creates the catch service with the repositories and mappers it needs.
     *
     * @param catchRepository the repository used to look up and save catches
     * @param fishRepository the repository used to look up fish species
     * @param flyRepository the repository used to look up flies
     * @param waterBodyRepository the repository used to look up water bodies
     * @param fishWaterBodyRepository the repository used to look up which fish live in which water bodies
     * @param catchMapper the mapper used to build catch DTOs
     * @param fishMapper the mapper used to build fish DTOs
     * @param flyMapper the mapper used to build fly DTOs
     * @param waterBodyMapper the mapper used to build water body DTOs
     */
    public CatchServiceImpl(CatchRepository catchRepository,
                            FishRepository fishRepository,
                            FlyRepository flyRepository,
                            WaterBodyRepository waterBodyRepository,
                            FishWaterBodyRepository fishWaterBodyRepository,
                            CatchMapper catchMapper,
                            FishMapper fishMapper,
                            FlyMapper flyMapper,
                            WaterBodyMapper waterBodyMapper) {

        this.catchRepository = catchRepository;
        this.fishRepository = fishRepository;
        this.flyRepository = flyRepository;
        this.waterBodyRepository = waterBodyRepository;
        this.fishWaterBodyRepository = fishWaterBodyRepository;

        this.catchMapper = catchMapper;
        this.fishMapper = fishMapper;
        this.flyMapper = flyMapper;
        this.waterBodyMapper = waterBodyMapper;
    }

    /**
     * Loads every catch from every user.
     *
     * @return the list of all catch DTOs, or an empty list if there are none
     */
    @Override
    public List<CatchDto> getAllCatches() {
        List<Catch> catches = catchRepository.findAll();

        log.info("Loaded {} catches", catches.size());

        return mapAllCatchDtos(catches);
    }

    /**
     * Loads every catch that belongs to the logged-in user.
     *
     * @return the list of the user's catch DTOs, or an empty list if no user is logged in
     */
    @Override
    public List<CatchDto> getAllUserCatches() {
        Long userId = getCurrentUserId();

        // nobody logged in means there are no catches to show
        if (userId == null) {
            log.warn("Could not load user catches. No user logged in");
            return new ArrayList<>();
        }

        List<Catch> catches = catchRepository.findByUserId(userId);

        log.info("Loaded {} catches for user ID: {}", catches.size(), userId);

        return mapAllCatchDtos(catches);
    }

    /**
     * Loads a single catch by its ID.
     *
     * @param catchId the ID of the catch to load
     * @return the catch DTO, or {@code null} if the ID is null or no catch was found
     */
    @Override
    public CatchDto getCatchByCatchId(Long catchId) {
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

        return mapCatchDto(catchRecord);
    }

    /**
     * Saves a new catch for the logged-in user.
     *
     * <p>The catch is only saved if a user is logged in and every check on
     * the form passes.</p>
     *
     * @param catchForm the form holding the new catch
     * @return {@code true} if the catch was saved, otherwise {@code false}
     */
    @Override
    public boolean addNewCatch(CatchForm catchForm) {
        Long userId = getCurrentUserId();

        // a catch has to belong to someone
        if (userId == null) {
            log.warn("Catch not saved. No user logged in");
            return false;
        }

        if (!isCatchFormValid(catchForm)) {
            return false;
        }

        Catch catchRecord = new Catch();

        // user ID comes from the login, not the form, so nobody can save a catch as someone else
        catchRecord.setUserId(userId);
        catchRecord.setFlyId(catchForm.getFlyId());
        catchRecord.setFishId(catchForm.getFishId());
        catchRecord.setWaterBodyId(catchForm.getWaterBodyId());
        catchRecord.setFishLength(catchForm.getFishLength());
        catchRecord.setDateCaught(catchForm.getDateCaught());
        catchRecord.setNotes(catchForm.getNotes());

        catchRepository.save(catchRecord);

        log.info("Catch saved for user ID: {}", userId);
        return true;
    }

    /**
     * Checks that a fly exists and that the logged-in user is allowed to use it.
     *
     * <p>Default flies have no owner and can be used by anyone. Custom flies
     * can only be used by the user who made them.</p>
     *
     * @param flyId the ID of the fly to check
     * @return {@code true} if the fly exists and the user can use it, otherwise {@code false}
     */
    @Override
    public boolean isFlyValid(Long flyId) {
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
        if (!fly.getUserId().equals(getCurrentUserId())) {
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

        List<Fish> fishInWaterBody = fishWaterBodyRepository.findFishByWaterBodyId(waterBodyId);

        // look through every fish in the water body for a matching ID
        for (Fish fish : fishInWaterBody) {
            if (fish.getFishId().equals(fishId)) {
                return true;
            }
        }

        log.warn("Catch validation failed. Fish ID {} is not found in water body ID {}", fishId, waterBodyId);
        return false;
    }

    /**
     * Checks that a fish length fits in the Catches table.
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
     * Converts a list of catches into catch DTOs.
     *
     * @param catches the catches to convert
     * @return the list of catch DTOs
     */
    private List<CatchDto> mapAllCatchDtos(List<Catch> catches) {
        List<CatchDto> catchDtos = new ArrayList<>();

        for (Catch catchRecord : catches) {
            catchDtos.add(mapCatchDto(catchRecord));
        }

        return catchDtos;
    }

    /**
     * Converts a catch into a catch DTO with its fly, fish, and water body.
     *
     * @param catchRecord the catch to convert
     * @return the catch DTO
     */
    private CatchDto mapCatchDto(Catch catchRecord) {
        // the foreign keys on Catches guarantee these rows exist
        Fly fly = flyRepository.findById(catchRecord.getFlyId()).orElseThrow();
        Fish fish = fishRepository.findById(catchRecord.getFishId()).orElseThrow();
        WaterBody waterBody = waterBodyRepository.findById(catchRecord.getWaterBodyId()).orElseThrow();

        FlyDto flyDto = flyMapper.toDto(fly);
        FishDto fishDto = fishMapper.toDto(fish);
        WaterBodyDto waterBodyDto = waterBodyMapper.toDto(waterBody);

        return catchMapper.toDto(
                catchRecord,
                flyDto,
                fishDto,
                waterBodyDto
        );
    }

    /**
     * Returns the ID of the logged-in user.
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
     * Checks that a catch exists and belongs to the logged-in user.
     *
     * @param catchId the ID of the catch to check
     * @return {@code true} if the catch belongs to the logged-in user, otherwise {@code false}
     */
    @Override
    public boolean isCatchOwner(Long catchId) {
        if (catchId == null) {
            log.warn("Catch ownership check failed. Catch ID is null");
            return false;
        }

        Catch catchRecord = catchRepository.findById(catchId).orElse(null);

        if (catchRecord == null) {
            log.warn("Catch not found for catch ID: {}", catchId);
            return false;
        }

        Long userId = getCurrentUserId();

        if (userId == null || !userId.equals(catchRecord.getUserId())) {
            log.warn("Catch ID {} does not belong to the current user", catchId);
            return false;
        }

        return true;
    }

    /**
     * Loads a catch as a form so it can be edited.
     *
     * @param catchId the ID of the catch to load
     * @return the filled catch form, or {@code null} if the catch doesn't belong to the logged-in user
     */
    @Override
    public CatchForm getCatchFormByCatchId(Long catchId) {
        if (!isCatchOwner(catchId)) {
            return null;
        }

        // the ownership check already confirmed this catch exists
        Catch catchRecord = catchRepository.findById(catchId).orElse(null);

        log.info("Loaded catch ID {} for editing", catchId);

        return catchMapper.toForm(catchRecord);
    }

    /**
     * Updates a catch that belongs to the logged-in user.
     *
     * @param catchId the ID of the catch to update
     * @param catchForm the form holding the new values
     * @return {@code true} if the catch was updated, otherwise {@code false}
     */
    @Override
    public boolean updateCatch(Long catchId, CatchForm catchForm) {
        // ownership first, so a bad form can't tell someone whether another user's catch exists
        if (!isCatchOwner(catchId)) {
            return false;
        }

        if (!isCatchFormValid(catchForm)) {
            return false;
        }

        // the ownership check already confirmed this catch exists
        Catch catchRecord = catchRepository.findById(catchId).orElse(null);

        copyFormToCatch(catchForm, catchRecord);

        catchRepository.save(catchRecord);

        log.info("Catch ID {} updated", catchId);
        return true;
    }

    /**
     * Runs every catch check against a form.
     *
     * @param catchForm the form to check
     * @return {@code true} if every check passes, otherwise {@code false}
     */
    private boolean isCatchFormValid(CatchForm catchForm) {
        if (!isFlyValid(catchForm.getFlyId())) {
            return false;
        }

        if (!isFishValid(catchForm.getFishId())) {
            return false;
        }

        if (!isWaterBodyValid(catchForm.getWaterBodyId())) {
            return false;
        }

        if (!isFishInWaterBody(catchForm.getFishId(), catchForm.getWaterBodyId())) {
            return false;
        }

        if (!isFishLengthValid(catchForm.getFishLength())) {
            return false;
        }

        if (!isDateCaughtValid(catchForm.getDateCaught())) {
            return false;
        }

        if (!isNotesValid(catchForm.getNotes())) {
            return false;
        }

        return true;
    }

    /**
     * Copies the editable fields from a form onto a catch.
     *
     * <p>The user ID is not copied, so an edit can't move a catch to another user.</p>
     *
     * @param catchForm the form to copy from
     * @param catchRecord the catch to copy onto
     */
    private void copyFormToCatch(CatchForm catchForm, Catch catchRecord) {
        catchRecord.setFlyId(catchForm.getFlyId());
        catchRecord.setFishId(catchForm.getFishId());
        catchRecord.setWaterBodyId(catchForm.getWaterBodyId());
        catchRecord.setFishLength(catchForm.getFishLength());
        catchRecord.setDateCaught(catchForm.getDateCaught());
        catchRecord.setNotes(catchForm.getNotes());
    }
}