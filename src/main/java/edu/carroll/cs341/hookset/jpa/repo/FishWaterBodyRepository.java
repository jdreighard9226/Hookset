package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shared.jpa.entity.Fish;
import shared.jpa.entity.FishWaterBody;
import shared.jpa.entity.WaterBody;

import java.util.List;

public interface FishWaterBodyRepository extends JpaRepository<FishWaterBody, Long> {


    @Query("""
        SELECT f
        FROM Fish f, FishWaterBody fwb
        WHERE f.fishId = fwb.fishId
        AND fwb.waterBodyId = :waterBodyId
    """)
    List<Fish> findFishByWaterBodyId(Long waterBodyId);

}
