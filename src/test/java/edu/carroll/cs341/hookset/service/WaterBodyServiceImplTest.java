package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shared.jpa.entity.WaterBody;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link edu.carroll.cs341.hookset.service.impl.WaterBodyServiceImpl}.
 *
 * <p>Each service method is tested against a happy path, a crappy path, and one or more
 * crazy paths. Happy paths cover valid input with an expected result. Crappy paths cover
 * failures the service is designed to handle. Crazy paths cover unusual input such as
 * null, empty, whitespace, and wrong-case slugs.</p>
 *
 * <p>Tests run against the H2 test database and each test is rolled back by
 * {@link Transactional}. Count assertions are measured against a baseline so the tests
 * do not depend on the database starting empty.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WaterBodyServiceImplTest {

    /** Service under test. */
    @Autowired
    private WaterBodyService waterBodyService;

    /** Repository used to set up test data. */
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

    // ---------- getAllWaterBodies ----------

    /**
     * Happy path: a saved water body is returned in the full list.
     */
    @Test
    public void testGetAllWaterBodiesHappyPath() {
        // baseline instead of a hardcoded count, so seed data doesn't break the test
        int before = waterBodyService.getAllWaterBodies().size();
        WaterBody testWaterBody = createTestWaterBody("test-river");
        waterBodyRepository.save(testWaterBody);

        List<WaterBody> waterBodies = waterBodyService.getAllWaterBodies();

        assertNotNull(waterBodies, "getAllWaterBodies should never return null");
        assertEquals(before + 1, waterBodies.size(),
                "List size should grow by one after saving a water body");

        // checks by slug instead of position, since the list order isn't guaranteed
        List<String> returnedSlugs = getWaterBodySlugs(waterBodies);

        assertTrue(returnedSlugs.contains(testWaterBody.getWaterBodySlug()),
                "Saved water body should be in the returned list");
    }

    /**
     * Crappy path: no water bodies are added, so the list does not grow and is never null.
     */
    @Test
    public void testGetAllWaterBodiesCrappyPath() {
        // read straight from the repository so the baseline doesn't go through the method under test
        int before = waterBodyRepository.findAll().size();

        List<WaterBody> waterBodies = waterBodyService.getAllWaterBodies();

        assertNotNull(waterBodies, "getAllWaterBodies should never return null");
        assertEquals(before, waterBodies.size(),
                "List size should not change when no water bodies are added");
    }

    /**
     * Crazy path: many water bodies are saved and every one of them is returned.
     */
    @Test
    public void testGetAllWaterBodiesCrazyPath() {
        int before = waterBodyService.getAllWaterBodies().size();

        List<WaterBody> testWaterBodies = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            WaterBody temp = createTestWaterBody("test-river-" + i);
            testWaterBodies.add(temp);
        }
        waterBodyRepository.saveAll(testWaterBodies);

        List<WaterBody> waterBodies = waterBodyService.getAllWaterBodies();

        assertNotNull(waterBodies, "getAllWaterBodies should never return null");
        assertEquals(before + 25, waterBodies.size(),
                "List size should grow by 25 after saving 25 water bodies");

        List<String> returnedSlugs = getWaterBodySlugs(waterBodies);

        // the count alone could pass with the wrong water bodies, so check each one is actually there
        for (WaterBody testWaterBody : testWaterBodies) {
            assertTrue(returnedSlugs.contains(testWaterBody.getWaterBodySlug()),
                    "Missing water body with slug: " + testWaterBody.getWaterBodySlug());
        }
    }

    // ---------- getWaterBodyFromSlug ----------

    /**
     * Happy path: a saved water body is found by its slug.
     */
    @Test
    public void testGetWaterBodyFromSlugHappyPath() {
        WaterBody saved = waterBodyRepository.save(createTestWaterBody("test-river"));

        WaterBody result = waterBodyService.getWaterBodyFromSlug("test-river");

        // check every field so a lookup that returns the wrong row can't slip through
        assertNotNull(result, "Water body should be found for an existing slug");
        assertEquals(saved.getWaterBodyId(), result.getWaterBodyId(),
                "Returned water body ID should match the saved water body");
        assertEquals("Test River", result.getWaterBodyName(),
                "Returned water body name should match the saved name");
        assertEquals("test-river", result.getWaterBodySlug(),
                "Returned water body slug should match the requested slug");
        assertEquals("Montana", result.getWaterBodyState(),
                "Returned water body state should match the saved state");
        assertEquals("River", result.getWaterBodyType(),
                "Returned water body type should match the saved type");
    }

    /**
     * Crappy path: a slug that does not exist returns null even when other water bodies exist.
     */
    @Test
    public void testGetWaterBodyFromSlugCrappyPath() {
        // a real water body exists so a lookup that ignores the slug would return it and fail
        waterBodyRepository.save(createTestWaterBody("test-river"));

        WaterBody result = waterBodyService.getWaterBodyFromSlug("does-not-exist");

        assertNull(result, "No water body should be returned for a slug that does not exist");
    }

    /**
     * Crazy path: a null slug returns null.
     */
    @Test
    public void testGetWaterBodyFromSlugCrazyPathNull() {
        waterBodyRepository.save(createTestWaterBody("test-river"));

        WaterBody result = waterBodyService.getWaterBodyFromSlug(null);

        assertNull(result, "No water body should be returned for a null slug");
    }

    /**
     * Crazy path: an empty slug returns null.
     */
    @Test
    public void testGetWaterBodyFromSlugCrazyPathEmpty() {
        waterBodyRepository.save(createTestWaterBody("test-river"));

        WaterBody result = waterBodyService.getWaterBodyFromSlug("");

        assertNull(result, "No water body should be returned for an empty slug");
    }

    /**
     * Crazy path: a whitespace-only slug returns null.
     */
    @Test
    public void testGetWaterBodyFromSlugCrazyPathWhitespace() {
        waterBodyRepository.save(createTestWaterBody("test-river"));

        WaterBody result = waterBodyService.getWaterBodyFromSlug("   ");

        assertNull(result, "No water body should be returned for a whitespace-only slug");
    }

    /**
     * Crazy path: a slug with the wrong case does not match.
     */
    @Test
    public void testGetWaterBodyFromSlugCrazyPathWrongCase() {
        waterBodyRepository.save(createTestWaterBody("test-river"));

        // H2 compares case-sensitively, but MySQL's default collation doesn't, so prod would match this
        WaterBody result = waterBodyService.getWaterBodyFromSlug("TEST-RIVER");

        assertNull(result, "Slug lookup should be case-sensitive and not match the wrong case");
    }

    // ---------- Helpers ----------

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

        // slug is the value lookups use, so callers pass a different one per water body
        waterBody.setWaterBodySlug(slug);

        return waterBody;
    }

    /**
     * Collects the slugs of a list of water bodies.
     *
     * @param waterBodies the water bodies to collect slugs from
     * @return the slugs in the same order
     */
    private List<String> getWaterBodySlugs(List<WaterBody> waterBodies) {
        List<String> slugs = new ArrayList<>();

        for (WaterBody waterBody : waterBodies) {
            slugs.add(waterBody.getWaterBodySlug());
        }

        return slugs;
    }
}