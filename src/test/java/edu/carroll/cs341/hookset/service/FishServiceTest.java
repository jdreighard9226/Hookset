package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shared.jpa.entity.Fish;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link edu.carroll.cs341.hookset.service.impl.FishServiceImpl}.
 *
 * <p>Each service method is tested against a happy path, a crappy path, and a crazy
 * path. Happy paths cover valid input with an expected result. Crappy paths cover
 * cases where nothing changes. Crazy paths cover unusual volume, such as many fish
 * saved at once.</p>
 *
 * <p>Looking up the fish in a single water body is tested in
 * {@link WaterBodyServiceImplTest}, since that lookup goes through the water body.</p>
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
}