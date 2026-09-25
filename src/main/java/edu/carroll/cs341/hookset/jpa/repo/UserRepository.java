package edu.carroll.cs341.hookset.jpa.repo;

import org.hibernate.internal.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}
