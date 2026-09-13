package edu.carroll.cs341.hookset.importers.waterbody.service;

/**
 * https://docs.spring.io/spring-boot/reference/io/rest-client.html
 */
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WaterBodyImporterService {
    private final WebClient webClient;

    public WaterBodyImporterService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder .baseUrl("https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer")
                .build();
    }
}
