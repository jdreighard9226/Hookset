package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;
import shared.jpa.entity.Catch;

import java.util.List;

@Repository
public interface CatchRepository extends JpaRepository<Catch, Long> {
    List<Catch> findByUserId(Long userId);
}
