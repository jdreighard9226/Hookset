package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.jpa.repo.FishWaterBodyRepository;
import edu.carroll.cs341.hookset.service.FishService;
import edu.carroll.cs341.hookset.web.dto.FishDto;
import edu.carroll.cs341.hookset.web.mapper.FishMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import shared.jpa.entity.Fish;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for looking up fish species.
 *
 * <p>This service reads fish records from the database and converts them
 * into DTOs for the web layer. Fish can be looked up as a full list or
 * filtered to the species found in a single water body.</p>
 */
@Service
public class FishServiceImpl implements FishService {

    private static final Logger log = LoggerFactory.getLogger(FishServiceImpl.class);

    /** The repository used to look up fish species. */
    private final FishRepository fishRepository;

    /** The repository used to look up which fish are in which water bodies. */
    private final FishWaterBodyRepository fishWaterBodyRepository;

    /** The mapper used to convert fish entities into DTOs. */
    private final FishMapper fishMapper;

    /**
     * Creates the fish service with the repositories and mapper it depends on.
     *
     * @param fishRepository the repository used to look up fish species
     * @param fishWaterBodyRepository the repository used to look up which fish are in which water bodies
     * @param fishMapper the mapper used to convert fish entities into DTOs
     */
    public FishServiceImpl(FishRepository fishRepository, FishWaterBodyRepository fishWaterBodyRepository, FishMapper fishMapper) {
        this.fishRepository = fishRepository;
        this.fishWaterBodyRepository = fishWaterBodyRepository;
        this.fishMapper = fishMapper;
    }

    /**
     * Gets every fish species in the database.
     *
     * @return the fish species as DTOs, or an empty list if none exist
     */
    @Override
    public List<FishDto> getAllFish() {
        log.info("Loading all fish");

        List<Fish> fishEntities = fishRepository.findAll();

        // An empty fish table usually means the importer has not been run
        if (fishEntities.size() < 1) {
            log.warn("No fish found in the database");
        }

        List<FishDto> fishDtos = new ArrayList<>();
        for (Fish fish : fishEntities) {
            fishDtos.add(fishMapper.toDto(fish));
        }

        log.info("Loaded {} fish", fishDtos.size());

        return fishDtos;
    }

    /**
     * Gets every fish species found in a water body.
     *
     * @param waterBodyId the identifier of the water body
     * @return the fish species in the water body as DTOs, or an empty list if none exist
     */
    @Override
    public List<FishDto> getAllFishForWaterBody(Long waterBodyId) {
        log.info("Loading fish for water body id: {}", waterBodyId);

        List<Fish> fishEntities = fishWaterBodyRepository.findFishByWaterBodyId(waterBodyId);

        if (fishEntities.size() < 1) {
            log.warn("No fish found for water body id: {}", waterBodyId);
        }

        List<FishDto> fishDtos = new ArrayList<>();
        for (Fish fish : fishEntities) {
            fishDtos.add(fishMapper.toDto(fish));
        }

        log.info("Loaded {} fish for water body id: {}", fishDtos.size(), waterBodyId);

        return fishDtos;
    }
}