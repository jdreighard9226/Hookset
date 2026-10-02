package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shared.jpa.entity.WaterBody;

import java.util.List;

@Repository
public interface WaterBodyRepository extends JpaRepository<WaterBody, Long> {
    WaterBody getWaterBodyByWaterBodySlug(String waterBodySlug);
}
