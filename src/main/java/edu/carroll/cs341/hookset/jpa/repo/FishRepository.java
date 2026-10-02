package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shared.jpa.entity.Fish;

import java.util.List;

@Repository
public interface FishRepository extends JpaRepository<Fish, Long> {
}
