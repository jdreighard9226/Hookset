package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.jpa.repo.FishWaterBodyRepository;
import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shared.jpa.entity.Fish;
import shared.jpa.entity.FishWaterBody;
import shared.jpa.entity.WaterBody;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link edu.carroll.cs341.hookset.service.impl.FishServiceImpl}.
 * <p>
 * Each service method is tested against a happy path, a crappy path, and one or more
 * crazy paths. Happy paths cover valid input with an expected result. Crappy paths cover
 * failures the service is designed to handle, such as a water body with no fish. Crazy
 * paths cover unusual input such as null and negative ids, many fish across several
 * water bodies, and a fish linked to more than one water body.
 * <p>
 * Tests run against the H2 test database and each test is rolled back by
 * {@link Transactional}. Count assertions are measured against a baseline so the tests
 * do not depend on the database starting empty.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FishServiceImplTest {

    /** Service under test. */
    @Autowired
    private FishService fishService;

    /** Repository used to set up fish test data. */
    @Autowired
    private FishRepository fishRepository;

    /** Repository used to link fish to water bodies in test data. */
    @Autowired
    private FishWaterBodyRepository fishWaterBodyRepository;

    /** Repository used to set up water body test data. */
    @Autowired
    private WaterBodyRepository waterBodyRepository;

    /** Data source used to confirm the tests are running against H2. */
    @Autowired
    private DataSource dataSource;

    /**
     * Confirms the tests are running against the H2 database before each test.
     *
     * @throws SQLException if the connection metadata cannot be read
     */
    @BeforeEach
    public void checkDatabase() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.getMetaData().getURL().startsWith("jdbc:h2:"),
                    "Tests must use the H2 database");
        }
    }

    // ---------- getAllFish ----------

    /**
     * Happy path: a saved fish is returned in the full list.
     */
    @Test
    public void testGetAllFishHappyPath() {
        int before = fishService.getAllFish().size();
        Fish testFish = createTestFish("test-species");
        fishRepository.save(testFish);

        List<Fish> fish = fishService.getAllFish();

        assertNotNull(fish, "getAllFish should never return null");
        assertEquals(before + 1, fish.size(),
                "List size should grow by one after saving a fish");

        List<String> returnedSpecies = new ArrayList<>();
        for (Fish returnedFish : fish) {
            returnedSpecies.add(returnedFish.getFishSpecies());
        }

        assertTrue(returnedSpecies.contains(testFish.getFishSpecies()),
                "Saved fish should be in the returned list");
    }

    /**
     * Crappy path: no fish are added, so the list does not grow and is never null.
     */
    @Test
    public void testGetAllFishCrappyPath() {
        int before = fishRepository.findAll().size();

        List<Fish> fish = fishService.getAllFish();

        assertNotNull(fish, "getAllFish should never return null");
        assertEquals(before, fish.size(),
                "List size should not change when no fish are added");
    }

    /**
     * Crazy path: many fish are saved and every one of them is returned.
     */
    @Test
    public void testGetAllFishCrazyPath() {
        int before = fishService.getAllFish().size();

        List<Fish> testFish = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Fish temp = createTestFish("test-species-" + i);
            testFish.add(temp);
        }
        fishRepository.saveAll(testFish);

        List<Fish> fish = fishService.getAllFish();

        assertNotNull(fish, "getAllFish should never return null");
        assertEquals(before + 100, fish.size(),
                "List size should grow by 100 after saving 100 fish");

        List<String> returnedSpecies = new ArrayList<>();
        for (Fish returnedFish : fish) {
            returnedSpecies.add(returnedFish.getFishSpecies());
        }

        for (Fish savedFish : testFish) {
            assertTrue(returnedSpecies.contains(savedFish.getFishSpecies()),
                    "Missing fish with species: " + savedFish.getFishSpecies());
        }
    }

    // ---------- getAllFishForWaterBody ----------

    /**
     * Happy path: a fish linked to a water body is returned for that water body.
     */
    @Test
    public void testGetAllFishForWaterBodyHappyPath() {
        Fish savedFish = fishRepository.save(createTestFish("test-species"));
        WaterBody savedWaterBody = waterBodyRepository.save(createTestWaterBody("test-river"));
        linkFishToWaterBody(savedFish, savedWaterBody);

        List<Fish> fish = fishService.getAllFishForWaterBody(savedWaterBody.getWaterBodyId());

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertEquals(1, fish.size(),
                "Water body should have exactly one linked fish");
        assertEquals(savedFish.getFishId(), fish.getFirst().getFishId(),
                "Returned fish ID should match the linked fish");
        assertEquals("test-species", fish.getFirst().getFishSpecies(),
                "Returned fish species should match the linked fish");
        assertEquals("test-family", fish.getFirst().getFishFamily(),
                "Returned fish family should match the linked fish");
        assertEquals("test-image", fish.getFirst().getFishImage(),
                "Returned fish image should match the linked fish");
    }

    /**
     * Crappy path: a water body with no linked fish returns an empty list.
     */
    @Test
    public void testGetAllFishForWaterBodyCrappyPathNoFish() {
        WaterBody savedWaterBody = waterBodyRepository.save(createTestWaterBody("test-river"));

        List<Fish> fish = fishService.getAllFishForWaterBody(savedWaterBody.getWaterBodyId());

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(),
                "A water body with no linked fish should return an empty list");
    }

    /**
     * Crappy path: a fish linked to a different water body is not returned.
     */
    @Test
    public void testGetAllFishForWaterBodyCrappyPathOtherWaterBody() {
        Fish savedFish = fishRepository.save(createTestFish("test-species"));
        WaterBody linkedWaterBody = waterBodyRepository.save(createTestWaterBody("linked-river"));
        WaterBody emptyWaterBody = waterBodyRepository.save(createTestWaterBody("empty-river"));
        linkFishToWaterBody(savedFish, linkedWaterBody);

        List<Fish> fish = fishService.getAllFishForWaterBody(emptyWaterBody.getWaterBodyId());

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(),
                "A fish linked to another water body should not be returned");
    }

    /**
     * Crappy path: a water body id that does not exist returns an empty list.
     */
    @Test
    public void testGetAllFishForWaterBodyCrappyPathMissingWaterBody() {
        List<Fish> fish = fishService.getAllFishForWaterBody(999999L);

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(),
                "A water body id that does not exist should return an empty list");
    }

    /**
     * Crazy path: a null water body id returns an empty list.
     */
    @Test
    public void testGetAllFishForWaterBodyCrazyPathNull() {
        Fish savedFish = fishRepository.save(createTestFish("test-species"));
        WaterBody savedWaterBody = waterBodyRepository.save(createTestWaterBody("test-river"));
        linkFishToWaterBody(savedFish, savedWaterBody);

        List<Fish> fish = fishService.getAllFishForWaterBody(null);

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(),
                "A null water body id should return an empty list");
    }

    /**
     * Crazy path: a negative water body id returns an empty list.
     */
    @Test
    public void testGetAllFishForWaterBodyCrazyPathNegativeId() {
        List<Fish> fish = fishService.getAllFishForWaterBody(-1L);

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(),
                "A negative water body id should return an empty list");
    }

    /**
     * Crazy path: many fish across two water bodies, and only the requested water body's fish are returned.
     */
    @Test
    public void testGetAllFishForWaterBodyCrazyPathManyFish() {
        WaterBody targetWaterBody = waterBodyRepository.save(createTestWaterBody("target-river"));
        WaterBody otherWaterBody = waterBodyRepository.save(createTestWaterBody("other-river"));

        List<Fish> targetFish = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            Fish temp = fishRepository.save(createTestFish("target-species-" + i));
            linkFishToWaterBody(temp, targetWaterBody);
            targetFish.add(temp);
        }

        List<Fish> otherFish = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Fish temp = fishRepository.save(createTestFish("other-species-" + i));
            linkFishToWaterBody(temp, otherWaterBody);
            otherFish.add(temp);
        }

        List<Fish> fish = fishService.getAllFishForWaterBody(targetWaterBody.getWaterBodyId());

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertEquals(25, fish.size(),
                "Only the 25 fish linked to the target water body should be returned");

        List<String> returnedSpecies = new ArrayList<>();
        for (Fish returnedFish : fish) {
            returnedSpecies.add(returnedFish.getFishSpecies());
        }

        for (Fish linkedFish : targetFish) {
            assertTrue(returnedSpecies.contains(linkedFish.getFishSpecies()),
                    "Missing fish with species: " + linkedFish.getFishSpecies());
        }

        for (Fish unlinkedFish : otherFish) {
            assertFalse(returnedSpecies.contains(unlinkedFish.getFishSpecies()),
                    "Fish from another water body was returned: " + unlinkedFish.getFishSpecies());
        }
    }

    /**
     * Crazy path: a fish linked to two water bodies is returned for each of them.
     */
    @Test
    public void testGetAllFishForWaterBodyCrazyPathSharedFish() {
        Fish sharedFish = fishRepository.save(createTestFish("shared-species"));
        WaterBody firstWaterBody = waterBodyRepository.save(createTestWaterBody("first-river"));
        WaterBody secondWaterBody = waterBodyRepository.save(createTestWaterBody("second-river"));
        linkFishToWaterBody(sharedFish, firstWaterBody);
        linkFishToWaterBody(sharedFish, secondWaterBody);

        List<Fish> firstFish = fishService.getAllFishForWaterBody(firstWaterBody.getWaterBodyId());
        List<Fish> secondFish = fishService.getAllFishForWaterBody(secondWaterBody.getWaterBodyId());

        assertEquals(1, firstFish.size(),
                "Shared fish should be returned once for the first water body");
        assertEquals(sharedFish.getFishId(), firstFish.getFirst().getFishId(),
                "First water body should return the shared fish");
        assertEquals(1, secondFish.size(),
                "Shared fish should be returned once for the second water body");
        assertEquals(sharedFish.getFishId(), secondFish.getFirst().getFishId(),
                "Second water body should return the shared fish");
    }

    // ---------- Helpers ----------

    /**
     * Builds a fish with fixed test values and the given species.
     *
     * @param species the species to assign to the fish
     * @return a new, unsaved fish
     */
    private Fish createTestFish(String species) {
        Fish fish = new Fish();

        fish.setFishFamily("test-family");
        fish.setFishImage("test-image");
        fish.setFishSpecies(species);

        return fish;
    }

    /**
     * Builds a water body with fixed test values and the given slug.
     *
     * @param slug the slug to assign to the water body
     * @return a new, unsaved water body
     */
    private WaterBody createTestWaterBody(String slug) {
        WaterBody waterBody = new WaterBody();

        waterBody.setWaterBodyName("Test River");
        waterBody.setWaterBodyType("River");
        waterBody.setWaterBodyState("Montana");
        waterBody.setWaterBodyDescription("A river used for testing.");
        waterBody.setWaterBodySlug(slug);

        return waterBody;
    }

    /**
     * Saves a link between a fish and a water body.
     *
     * @param fish the saved fish to link
     * @param waterBody the saved water body to link
     */
    private void linkFishToWaterBody(Fish fish, WaterBody waterBody) {
        FishWaterBody fishWaterBody = new FishWaterBody();

        fishWaterBody.setFishId(fish.getFishId());
        fishWaterBody.setWaterBodyId(waterBody.getWaterBodyId());

        fishWaterBodyRepository.save(fishWaterBody);
    }
}