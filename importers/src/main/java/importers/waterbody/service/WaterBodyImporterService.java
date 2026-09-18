package importers.waterbody.service;

/**
 * https://docs.spring.io/spring-boot/reference/io/rest-client.html
 */
import jpa.repo.WaterBodyRepo;
import importers.waterbody.dto.WaterBodyDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

@Service
public class WaterBodyImporterService {

    @Autowired
    private ObjectMapper objectMapper;

    private final WebClient webClient;
    private final WaterBodyRepo waterBodyRepo;

    public WaterBodyImporterService(WebClient.Builder webClientBuilder, WaterBodyRepo waterBodyRepo) {
        this.webClient = webClientBuilder .baseUrl("https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer")
                .build();
        this.waterBodyRepo = waterBodyRepo;
    }

    public Flux<WaterBodyDto> getAllWaterBodies() {
        Flux<WaterBodyDto> waterBodyDtos;

        webClient.get().uri("");

        return null;
    }

}
