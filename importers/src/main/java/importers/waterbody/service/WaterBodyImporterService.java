package importers.waterbody.service;

import importers.waterbody.dto.WaterBodyAttributeDto;
import importers.waterbody.dto.WaterBodyFeatureDto;
import importers.waterbody.dto.WaterBodyResponseDto;
import importers.waterbody.mapper.WaterBodyMapper;
import importers.jpa.repo.WaterBodyRepo;
import importers.waterbody.dto.WaterBodyDto;
import importers.waterbody.mapper.impl.WaterBodyMapperImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import shared.jpa.entity.WaterBody;

import java.util.ArrayList;
import java.util.List;

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

    private final RestClient restClient;
    private final WaterBodyRepo waterBodyRepo;
    private final WaterBodyMapper waterBodyMapper;

    public WaterBodyImporterService(RestClient.Builder restClientBuilder, WaterBodyRepo waterBodyRepo, WaterBodyMapper waterBodyMapper) {
        this.restClient = restClientBuilder
                .baseUrl("https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer")
                .build();
        this.waterBodyRepo = waterBodyRepo;
        this.waterBodyMapper = waterBodyMapper;
    }

    /**
     * Retrieves all available water body data from the Montana FWP
     * Fish Distribution API.
     *
     * @return a list containing the retrieved water body DTOs
     */
    public List<WaterBodyDto> getAllWaterBodies() {

        List<WaterBodyDto> waterBodyDtos = new ArrayList<>();

        int offset = 0;
        int pageSize = 2000;

        while (true) {
            int currentOffset = offset;

            WaterBodyResponseDto response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/9/query")
                            .queryParam("where", "1=1")
                            .queryParam("outFields", "LLID,WATERNAME")
                            .queryParam("returnGeometry", false)
                            .queryParam("returnDistinctValues", true)
                            .queryParam("resultOffset", currentOffset)
                            .queryParam("resultRecordCount", pageSize)
                            .queryParam("f", "json")
                            .build())
                    .retrieve()
                    .body(WaterBodyResponseDto.class);

            if (response == null ||
                    response.getFeatures() == null ||
                    response.getFeatures().isEmpty()) {
                break;
            }

            for (WaterBodyFeatureDto feature : response.getFeatures()) {
                WaterBodyAttributeDto attributes = feature.getAttributes();

                WaterBodyDto waterBodyDto = new WaterBodyDto();

                waterBodyDto.setFishWildLifeLLID(
                        attributes.getFishWildLifeLLID()
                );

                waterBodyDto.setWaterBodyName(
                        attributes.getWaterBodyName()
                );

                waterBodyDtos.add(waterBodyDto);
            }

            System.out.println(
                    "Offset: " + currentOffset +
                            " | Returned: " + response.getFeatures().size() +
                            " | Total collected: " + waterBodyDtos.size()
            );

            if (response.getFeatures().size() < pageSize) {
                break;
            }

            offset += response.getFeatures().size();
        }

        return waterBodyDtos;
    }

    private List<WaterBody> mapToEntitiy(List<WaterBodyDto> waterBodyDtos) {
        List<WaterBody> waterBodies = new ArrayList<>();
        for (WaterBodyDto waterBodyDto : waterBodyDtos) {
            waterBodies.add(waterBodyMapper.mapToEntity(waterBodyDto));
        }

        return waterBodies;
    }

    public boolean saveWaterBodies(List<WaterBodyDto> WaterBodyDtos) {
       List<WaterBody> waterBodies = mapToEntitiy(WaterBodyDtos);
        try {
            waterBodyRepo.saveAll(waterBodies);
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }
}