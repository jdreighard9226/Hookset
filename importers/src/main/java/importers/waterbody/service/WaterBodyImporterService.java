package importers.waterbody.service;

import jpa.repo.WaterBodyRepo;
import importers.waterbody.dto.WaterBodyDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

/**
 * Provides operations for importing Montana water body data into Hookset.
 *
 * <p>This service uses Spring WebClient to communicate with the Montana Fish,
 * Wildlife & Parks Fish Distribution API. Water body data retrieved from the
 * API is converted into DTOs before being processed and stored in the Hookset
 * database.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>Spring Boot REST Client documentation:
 *   https://docs.spring.io/spring-boot/reference/io/rest-client.html</li>
 *   <li>Montana Fish, Wildlife & Parks Fish Distribution API:
 *   https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer</li>
 *   <li>Montana Fish, Wildlife & Parks Fish Distribution - Streams:
 *   https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/9</li>
 *   <li>Montana Fish, Wildlife & Parks Fish Distribution - Lakes:
 *   https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer/8</li>
 * </ul>
 */
@Service
public class WaterBodyImporterService {

    /** Object mapper used to convert API response data into Java objects. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Web client used to communicate with the Montana FWP Fish Distribution API. */
    private final WebClient webClient;

    /** Repository used to access and store water body records. */
    private final WaterBodyRepo waterBodyRepo;

    /**
     * Constructs the water body importer service with the required WebClient
     * builder and water body repository.
     *
     * <p>The WebClient is configured with the base URL for the Montana FWP
     * Fish Distribution API.</p>
     *
     * @param webClientBuilder builder used to configure the WebClient
     * @param waterBodyRepo repository used to access and store water body records
     */
    public WaterBodyImporterService(WebClient.Builder webClientBuilder, WaterBodyRepo waterBodyRepo) {
        this.webClient = webClientBuilder
                .baseUrl("https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer")
                .build();
        this.waterBodyRepo = waterBodyRepo;
    }

    /**
     * Retrieves all available water body data from the Montana FWP
     * Fish Distribution API.
     *
     * @return a Flux containing the retrieved water body DTOs
     */
    public Flux<WaterBodyDto> getAllWaterBodies() {
        Flux<WaterBodyDto> waterBodyDtos;

        webClient.get().uri("");

        return null;
    }
}