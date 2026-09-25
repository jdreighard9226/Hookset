package edu.carroll.cs341.hookset.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.User;

/**
 * Provides database access for User entities.
 *
 * <p>Spring Data JPA generates the implementation for this repository,
 * including standard save and lookup operations. Additional queries are
 * derived from the method names declared here.</p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return the matching user, or null if no user has that username
     */
    User findByUsername(String username);
}