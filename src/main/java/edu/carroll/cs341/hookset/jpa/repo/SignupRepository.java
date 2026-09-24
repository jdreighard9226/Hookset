package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.User;

public interface SignupRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}
