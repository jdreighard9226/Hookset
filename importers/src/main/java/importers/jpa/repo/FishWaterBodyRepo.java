package importers.jpa.repo;

import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shared.jpa.entity.FishWaterBody;

@Repository
public interface FishWaterBodyRepo extends JpaRepository<FishWaterBody, Long> {
}
