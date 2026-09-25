package importers.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.Fish;

/**
 * Provides database access for Fish entities used by the importer.
 *
 * <p>Spring Data JPA generates the implementation for this repository,
 * including standard save and lookup operations.</p>
 */
public interface FishRepo extends JpaRepository<Fish, Long> {
}