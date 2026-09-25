package importers.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shared.jpa.entity.Fish;

@Repository
public interface FishRepo extends JpaRepository<Fish, Long> {
}
