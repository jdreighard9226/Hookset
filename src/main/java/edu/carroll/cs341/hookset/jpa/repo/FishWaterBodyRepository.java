package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shared.jpa.entity.Fish;
import shared.jpa.entity.FishWaterBody;

import java.util.List;

/**
 * Repository for the links between fish species and water bodies.
 *
 * <p>Each record ties one fish species to one water body it is found in.
 * Besides the standard Spring Data JPA methods, this repository provides
 * a query for looking up every fish species in a given water body.</p>
 */
public interface FishWaterBodyRepository extends JpaRepository<FishWaterBody, Long> {

    /**
     * Finds every fish species found in a water body.
     *
     * @param waterBodyId the identifier of the water body
     * @return the fish species found in the water body, or an empty list if none
     */
    // FishWaterBody stores raw ids instead of entity relationships, so the join is done on the id fields
    @Query("""
        SELECT f
        FROM Fish f, FishWaterBody fwb
        WHERE f.fishId = fwb.fishId
        AND fwb.waterBodyId = :waterBodyId
    """)
    List<Fish> findFishByWaterBodyId(Long waterBodyId);
}