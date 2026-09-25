package importers.waterbody.service;

import importers.jpa.repo.FishRepo;
import importers.jpa.repo.FishWaterBodyRepo;
import importers.jpa.repo.WaterBodyRepo;
import importers.waterbody.dto.*;
import importers.waterbody.mapper.FishMapper;
import importers.waterbody.mapper.WaterBodyMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import shared.jpa.entity.Fish;
import shared.jpa.entity.FishWaterBody;
import shared.jpa.entity.WaterBody;

import java.util.*;

/**
 * Provides operations for importing Montana water body and fish data into Hookset.
 *
 * <p>This service uses Spring RestClient to communicate with the Montana Fish,
 * Wildlife & Parks Fish Distribution API. Water body and fish data retrieved
 * from the API is converted into DTOs before being processed and stored in the
 * Hookset database.</p>
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
    private final FishRepo fishRepo;
    private final FishWaterBodyRepo fishWaterBodyRepo;
    private final WaterBodyMapper waterBodyMapper;
    private final FishMapper fishMapper;

    public WaterBodyImporterService(
            RestClient.Builder restClientBuilder,
            WaterBodyRepo waterBodyRepo,
            FishRepo fishRepo,
            FishWaterBodyRepo fishWaterBodyRepo,
            WaterBodyMapper waterBodyMapper,
            FishMapper fishMapper) {

        this.restClient = restClientBuilder
                .baseUrl("https://services1.arcgis.com/754BERmVIq3RqSf8/ArcGIS/rest/services/MT_FWP_Fish_Distribution/FeatureServer")
                .build();

        this.waterBodyRepo = waterBodyRepo;
        this.fishRepo = fishRepo;
        this.fishWaterBodyRepo = fishWaterBodyRepo;
        this.waterBodyMapper = waterBodyMapper;
        this.fishMapper = fishMapper;
    }

    /**
     * Retrieves all available water body and fish data from the Montana FWP
     * Fish Distribution API.
     *
     * @return DTO containing water bodies, fish, and their relationships
     */
    public WaterBodyAndFishDto getAllWaterBodies() {

        int offset = 0;
        int pageSize = 2000;

        Map<String, WaterBodyDto> waterBodyDtos = new HashMap<>();
        Map<Integer, FishDto> fishDtos = new HashMap<>();
        Map<String, FishWaterBodyDto> fishWaterBodyDtos = new HashMap<>();

        while (true) {

            int currentOffset = offset;

            WaterBodyAndFishResponseDto response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/9/query")
                            .queryParam("where", "1=1")
                            .queryParam(
                                    "outFields",
                                    "WATERNAME,SPECIESID,SPECIES,FAMILY"
                            )
                            .queryParam("returnGeometry", false)
                            .queryParam("orderByFields", "FID")
                            .queryParam("resultOffset", currentOffset)
                            .queryParam("resultRecordCount", pageSize)
                            .queryParam("f", "json")
                            .build())
                    .retrieve()
                    .body(WaterBodyAndFishResponseDto.class);

            if (response == null ||
                    response.getFeatures() == null ||
                    response.getFeatures().isEmpty()) {

                break;
            }

            for (WaterBodyAndFishFeatureDto feature : response.getFeatures()) {

                WaterBodyAndFishAttributeDto attributes =
                        feature.getAttributes();

                if (attributes == null ||
                        attributes.getWaterBodyName() == null ||
                        attributes.getSpeciesId() == null) {

                    continue;
                }

                String waterBodyName = attributes.getWaterBodyName();
                String waterBodyKey = waterBodyName.toLowerCase();

                // add unique water bodies
                if (!waterBodyDtos.containsKey(waterBodyKey)) {

                    WaterBodyDto waterBodyDto = new WaterBodyDto();

                    waterBodyDto.setWaterBodyName(
                            waterBodyName
                    );

                    waterBodyDtos.put(
                            waterBodyKey,
                            waterBodyDto
                    );
                }

                // add unique fish
                if (!fishDtos.containsKey(attributes.getSpeciesId())) {

                    FishDto fishDto = new FishDto();

                    fishDto.setFishFamily(
                            attributes.getFamily()
                    );

                    fishDto.setFishSpecies(
                            attributes.getSpecies()
                    );

                    fishDto.setFwpSpeciesId(
                            attributes.getSpeciesId()
                    );

                    fishDtos.put(
                            attributes.getSpeciesId(),
                            fishDto
                    );
                }

                // add unique fish / water body relationship
                String fishWaterBodyKey =
                        waterBodyKey
                                + "|"
                                + attributes.getSpeciesId();

                if (!fishWaterBodyDtos.containsKey(fishWaterBodyKey)) {

                    FishWaterBodyDto fishWaterBodyDto =
                            new FishWaterBodyDto();

                    fishWaterBodyDto.setSpeciesId(
                            attributes.getSpeciesId()
                    );

                    fishWaterBodyDto.setWaterBodyName(
                            waterBodyDtos.get(waterBodyKey).getWaterBodyName()
                    );

                    fishWaterBodyDtos.put(
                            fishWaterBodyKey,
                            fishWaterBodyDto
                    );
                }
            }

            if (response.getFeatures().size() < pageSize) {
                break;
            }

            offset += response.getFeatures().size();
        }

        List<WaterBodyDto> waterBodyDtoList =
                new ArrayList<>(waterBodyDtos.values());

        List<FishDto> fishDtoList =
                new ArrayList<>(fishDtos.values());

        List<FishWaterBodyDto> fishWaterBodyDtoList =
                new ArrayList<>(fishWaterBodyDtos.values());

        return new WaterBodyAndFishDto(
                waterBodyDtoList,
                fishDtoList,
                fishWaterBodyDtoList
        );
    }

    private List<WaterBody> mapWaterBodiesToEntitiy(
            List<WaterBodyDto> waterBodyDtos) {

        List<WaterBody> waterBodies = new ArrayList<>();

        for (WaterBodyDto waterBodyDto : waterBodyDtos) {

            waterBodies.add(
                    waterBodyMapper.mapToEntity(waterBodyDto)
            );
        }

        return waterBodies;
    }

    private List<Fish> mapFishesToEntity(
            List<FishDto> fishDtos) {

        List<Fish> fishes = new ArrayList<>();

        for (FishDto fishDto : fishDtos) {

            fishes.add(
                    fishMapper.mapToEntity(fishDto)
            );
        }

        return fishes;
    }

    private List<FishWaterBody> mapFishWaterBodiesToEntity(
            List<FishWaterBodyDto> fishWaterBodyDtos,
            List<Fish> fishes,
            List<WaterBody> waterBodies) {

        Map<Integer, Long> fishIds = new HashMap<>();
        Map<String, Long> waterBodyIds = new HashMap<>();

        for (Fish fish : fishes) {
            fishIds.put(
                    fish.getFwpSpeciesId(),
                    fish.getFishId()
            );
        }

        for (WaterBody waterBody : waterBodies) {
            waterBodyIds.put(
                    waterBody.getWaterBodyName(),
                    waterBody.getWaterBodyId()
            );
        }

        List<FishWaterBody> fishWaterBodies = new ArrayList<>();

        for (FishWaterBodyDto fishWaterBodyDto : fishWaterBodyDtos) {

            Long fishId = fishIds.get(
                    fishWaterBodyDto.getSpeciesId()
            );

            Long waterBodyId = waterBodyIds.get(
                    fishWaterBodyDto.getWaterBodyName()
            );

            if (fishId == null || waterBodyId == null) {
                continue;
            }

            FishWaterBody fishWaterBody = new FishWaterBody();

            fishWaterBody.setFishId(fishId);
            fishWaterBody.setWaterBodyId(waterBodyId);

            fishWaterBodies.add(fishWaterBody);
        }

        return fishWaterBodies;
    }

    private boolean saveWaterBodies(
            List<WaterBodyDto> waterBodyDtos) {

        List<WaterBody> waterBodies =
                mapWaterBodiesToEntitiy(waterBodyDtos);

        try {

            waterBodyRepo.saveAll(waterBodies);

            return true;

        } catch (DataAccessException e) {

            return false;
        }
    }

    private boolean saveFishes(
            List<FishDto> fishDtos) {

        List<Fish> fishes =
                mapFishesToEntity(fishDtos);

        try {

            fishRepo.saveAll(fishes);

            return true;

        } catch (DataAccessException e) {

            return false;
        }
    }

    private boolean saveFishWaterBodies(
            List<FishWaterBody> fishWaterBodies) {

        try {

            fishWaterBodyRepo.saveAll(fishWaterBodies);

            return true;

        } catch (DataAccessException e) {

            return false;
        }
    }

    public boolean saveFishAndWaterBodies(
            List<FishWaterBodyDto> fishWaterBodyDtos,
            List<FishDto> fishDtos,
            List<WaterBodyDto> waterBodyDtos) {

        boolean isValid =
                saveFishes(fishDtos) &&
                        saveWaterBodies(waterBodyDtos);

        if (!isValid) {
            return false;
        }

        // now we need database ids to properly map FishWaterBodies table
        List<Fish> fishes = fishRepo.findAll();
        List<WaterBody> waterBodies = waterBodyRepo.findAll();

        List<FishWaterBody> fishWaterBodies =
                mapFishWaterBodiesToEntity(
                        fishWaterBodyDtos,
                        fishes,
                        waterBodies
                );

        // if something failed to map, do not save partial relationships
        if (fishWaterBodies.size() != fishWaterBodyDtos.size()) {
            return false;
        }

        return saveFishWaterBodies(fishWaterBodies);
    }
}