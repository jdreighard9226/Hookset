package importers.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shared.jpa.entity.WaterBody;

public interface WaterBodyRepo extends JpaRepository<WaterBody, Integer> {

}