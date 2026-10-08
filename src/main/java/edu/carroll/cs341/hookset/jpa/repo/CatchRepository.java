package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.Catch;

public interface CatchRepository extends JpaRepository<Catch, Long> {
}
