package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.Fly;

import java.util.List;

public interface FlyRepository extends JpaRepository<Fly, Long> {

    // All default Hookset flies
    List<Fly> findByUserIdIsNull();

    // All custom flies belonging to a user
    List<Fly> findByUserId(Long userId);

    // Both default and user-created flies
    List<Fly> findByUserIdIsNullOrUserId(Long userId);
}
