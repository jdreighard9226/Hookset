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
 *
 * <p>Each service method is tested against a happy path, a crappy path, and one or more
 * crazy paths. Happy paths cover valid input with an expected result. Crappy paths cover
 * failures the service is designed to handle, such as a water body with no fish. Crazy
 * paths cover unusual input such as null and negative IDs, many fish across several
 * water bodies, and a fish linked to more than one water body.</p>
 *
 * <p>Tests run against the H2 test database and each test is rolled back by
 * {@link Transactional}. Count assertions are measured against a baseline so the tests
 * do not depend on the database starting empty.</p>
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
        // guards against a wrong profile pointing these tests at the real MySQL database
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
        // baseline instead of a hardcoded count, so seed data doesn't break the test
        int before = fishService.getAllFish().size();
        Fish testFish = createTestFish("test-species");
        fishRepository.save(testFish);

        List<Fish> fish = fishService.getAllFish();

        assertNotNull(fish, "getAllFish should never return null");
        assertEquals(before + 1, fish.size(),
                "List size should grow by one after saving a fish");

        // compare by species since the returned objects may not be the same instances that were saved
        List<String> returnedSpecies = new ArrayList<>();
        for (Fish returnedFish : fish) {
            returnedSpecies.add(returnedFish.getSpecies());
        }

        assertTrue(returnedSpecies.contains(testFish.getSpecies()),
                "Saved fish should be in the returned list");
    }

    /**
     * Crappy path: no fish are added, so the list does not grow and is never null.
     */
    @Test
    public void testGetAllFishCrappyPath() {
        // read straight from the repository so the baseline doesn't go through the method under test
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
            returnedSpecies.add(returnedFish.getSpecies());
        }

        // the count alone could pass with the wrong fish, so check each one is actually there
        for (Fish savedFish : testFish) {
            assertTrue(returnedSpecies.contains(savedFish.getSpecies()),
                    "Missing fish with species: " + savedFish.getSpecies());
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

        List<Fish> fish = fishService.getAllFishForWaterBody(savedWaterBody.getId());

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertEquals(1, fish.size(),
                "Water body should have exactly one linked fish");

        // check every field so a query that joins the wrong columns can't slip through
        assertEquals(savedFish.getFishId(), fish.getFirst().getFishId(),
                "Returned fish ID should match the linked fish");
        assertEquals("test-species", fish.getFirst().getSpecies(),
                "Returned fish species should match the linked fish");
        assertEquals("test-family", fish.getFirst().getFamily(),
                "Returned fish family should match the linked fish");
        assertEquals("test-image", fish.getFirst().getImage(),
                "Returned fish image should match the linked fish");
    }

    /**
     * Crappy path: a water body with no linked fish returns an empty list.
     */
    @Test
    public void testGetAllFishForWaterBodyCrappyPathNoFish() {
        WaterBody savedWaterBody = waterBodyRepository.save(createTestWaterBody("test-river"));

        List<Fish> fish = fishService.getAllFishForWaterBody(savedWaterBody.getId());

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

        // asks about the empty water body, so a query that ignores the ID would fail here
        List<Fish> fish = fishService.getAllFishForWaterBody(emptyWaterBody.getId());

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(),
                "A fish linked to another water body should not be returned");
    }

    /**
     * Crappy path: a water body ID that does not exist returns an empty list.
     */
    @Test
    public void testGetAllFishForWaterBodyCrappyPathMissingWaterBody() {
        // far above any ID H2 will hand out during a test run
        List<Fish> fish = fishService.getAllFishForWaterBody(999999L);

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(),
                "A water body ID that does not exist should return an empty list");
    }

    /**
     * Crazy path: a null water body ID returns an empty list.
     */
    @Test
    public void testGetAllFishForWaterBodyCrazyPathNull() {
        // a real link exists so a null that matched everything would return it and fail
        Fish savedFish = fishRepository.save(createTestFish("test-species"));
        WaterBody savedWaterBody = waterBodyRepository.save(createTestWaterBody("test-river"));
        linkFishToWaterBody(savedFish, savedWaterBody);

        List<Fish> fish = fishService.getAllFishForWaterBody(null);

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(),
                "A null water body ID should return an empty list");
    }

    /**
     * Crazy path: a negative water body ID returns an empty list.
     */
    @Test
    public void testGetAllFishForWaterBodyCrazyPathNegativeId() {
        List<Fish> fish = fishService.getAllFishForWaterBody(-1L);

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(),
                "A negative water body ID should return an empty list");
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

        // different counts on each side so a mixed-up result can't accidentally have the right size
        List<Fish> otherFish = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Fish temp = fishRepository.save(createTestFish("other-species-" + i));
            linkFishToWaterBody(temp, otherWaterBody);
            otherFish.add(temp);
        }

        List<Fish> fish = fishService.getAllFishForWaterBody(targetWaterBody.getId());

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertEquals(25, fish.size(),
                "Only the 25 fish linked to the target water body should be returned");

        List<String> returnedSpecies = new ArrayList<>();
        for (Fish returnedFish : fish) {
            returnedSpecies.add(returnedFish.getSpecies());
        }

        for (Fish linkedFish : targetFish) {
            assertTrue(returnedSpecies.contains(linkedFish.getSpecies()),
                    "Missing fish with species: " + linkedFish.getSpecies());
        }

        for (Fish unlinkedFish : otherFish) {
            assertFalse(returnedSpecies.contains(unlinkedFish.getSpecies()),
                    "Fish from another water body was returned: " + unlinkedFish.getSpecies());
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

        List<Fish> firstFish = fishService.getAllFishForWaterBody(firstWaterBody.getId());
        List<Fish> secondFish = fishService.getAllFishForWaterBody(secondWaterBody.getId());

        // size 1 on each side catches a bad join that returns the shared fish twice
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

        fish.setFamily("test-family");
        fish.setImage("test-image");
        fish.setSpecies(species);

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

        waterBody.setName("Test River");
        waterBody.setType("River");
        waterBody.setState("Montana");
        waterBody.setDescription("A river used for testing.");

        // slug is the unique value, so callers pass a different one per water body
        waterBody.setSlug(slug);

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

        // both must already be saved so their generated IDs exist
        fishWaterBody.setFishId(fish.getFishId());
        fishWaterBody.setWaterBodyId(waterBody.getId());

        fishWaterBodyRepository.save(fishWaterBody);
    }
}