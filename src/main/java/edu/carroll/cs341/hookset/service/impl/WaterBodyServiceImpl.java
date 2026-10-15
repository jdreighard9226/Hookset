package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import edu.carroll.cs341.hookset.service.WaterBodyService;
import edu.carroll.cs341.hookset.web.dto.WaterBodyDto;
import edu.carroll.cs341.hookset.web.mapper.WaterBodyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import shared.jpa.entity.WaterBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for looking up water bodies.
 *
 * <p>This service reads water body records from the database and converts
 * them into DTOs for the web layer. Water bodies can be looked up as a full
 * list or individually by their URL slug.</p>
 */
@Service
public class WaterBodyServiceImpl implements WaterBodyService {

    private static final Logger log = LoggerFactory.getLogger(WaterBodyServiceImpl.class);

    /** The repository used to look up water bodies. */
    private final WaterBodyRepository waterBodyRepository;


    /**
     * Creates the water body service with the repository and mapper it depends on.
     *
     * @param waterBodyRepository the repository used to look up water bodies
     */
    public WaterBodyServiceImpl(WaterBodyRepository waterBodyRepository) {
        this.waterBodyRepository = waterBodyRepository;
    }

    /**
     * Gets every water body in the database.
     *
     * @return the water bodies as DTOs, or an empty list if none exist
     */
    @Override
    public List<WaterBody> getAllWaterBodies() {
        log.info("Loading all water bodies");

        List<WaterBody> waterBodyEntities = waterBodyRepository.findAll();

        // an empty water body table usually means the importer has not been run
        if (waterBodyEntities.size() < 1) {
            log.warn("No water bodies found in the database. Size: {}", waterBodyEntities.size());
        }

        log.info("Loaded {} water bodies", waterBodyEntities.size());

        return waterBodyEntities;
    }

    /**
     * Gets a water body by its URL slug.
     *
     * @param waterBodySlug the slug used in the water body's URL
     * @return the matching water body as a DTO
     */
    @Override
    public WaterBody getWaterBodyFromSlug(String waterBodySlug) {

        if (waterBodySlug == null || waterBodySlug.isBlank()) {
            log.warn("getWaterBodyFromSlug called with null or blank slug");
            return null;
        }

        log.info("Loading water body for slug: {}", waterBodySlug);

        WaterBody waterBody = waterBodyRepository.getWaterBodyByWaterBodySlug(waterBodySlug);

        if (waterBody == null) {
            log.warn("No water body found for slug: {}", waterBodySlug);
        }

        return waterBody;
    }
}