package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shared.jpa.entity.Fish;
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
 * failures the service is designed to handle, such as a missing slug or a water body with
 * no fish. Crazy paths cover unusual input such as null, empty, whitespace, and wrong-case
 * slugs, null and negative IDs, many fish across several water bodies, and a fish linked
 * to more than one water body.</p>
 *
 * <p>Tests run against the H2 test database and each test is rolled back by
 * {@link Transactional}. Count assertions are measured against a baseline so the tests
 * do not depend on the database starting empty.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WaterBodyServiceTest {

    /** Service under test. */
    @Autowired
    private WaterBodyService waterBodyService;

    /** Repository used to set up water body test data. */
    @Autowired
    private WaterBodyRepository waterBodyRepository;

    /** Repository used to set up fish test data. */
    @Autowired
    private FishRepository fishRepository;


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

        assertTrue(returnedSlugs.contains(testWaterBody.getSlug()),
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
            assertTrue(returnedSlugs.contains(testWaterBody.getSlug()),
                    "Missing water body with slug: " + testWaterBody.getSlug());
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
        assertEquals(saved.getId(), result.getId(),
                "Returned water body ID should match the saved water body");
        assertEquals("Test River", result.getName(),
                "Returned water body name should match the saved name");
        assertEquals("test-river", result.getSlug(),
                "Returned water body slug should match the requested slug");
        assertEquals("Montana", result.getState(),
                "Returned water body state should match the saved state");
        assertEquals("River", result.getType(),
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

    // ---------- getAllFishForWaterBody ----------

    /**
     * Happy path: a fish linked to a water body is returned for that water body.
     */
    @Test
    public void testGetAllFishForWaterBodyHappyPath() {
        Fish savedFish = fishRepository.save(createTestFish("test-species"));
        WaterBody savedWaterBody = waterBodyRepository.save(createTestWaterBody("test-river"));
        linkFishToWaterBody(savedFish, savedWaterBody);

        List<Fish> fish = waterBodyService.getAllFishForWaterBody(savedWaterBody.getId());

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertEquals(1, fish.size(),
                "Water body should have exactly one linked fish");

        // check every field so a mapping that joins the wrong columns can't slip through
        assertEquals(savedFish.getId(), fish.getFirst().getId(),
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

        List<Fish> fish = waterBodyService.getAllFishForWaterBody(savedWaterBody.getId());

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

        // asks about the empty water body, so a lookup that ignores the ID would fail here
        List<Fish> fish = waterBodyService.getAllFishForWaterBody(emptyWaterBody.getId());

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
        List<Fish> fish = waterBodyService.getAllFishForWaterBody(999999L);

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

        List<Fish> fish = waterBodyService.getAllFishForWaterBody(null);

        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(),
                "A null water body ID should return an empty list");
    }

    /**
     * Crazy path: a negative water body ID returns an empty list.
     */
    @Test
    public void testGetAllFishForWaterBodyCrazyPathNegativeId() {
        List<Fish> fish = waterBodyService.getAllFishForWaterBody(-1L);

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

        List<Fish> fish = waterBodyService.getAllFishForWaterBody(targetWaterBody.getId());

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

        List<Fish> firstFish = waterBodyService.getAllFishForWaterBody(firstWaterBody.getId());
        List<Fish> secondFish = waterBodyService.getAllFishForWaterBody(secondWaterBody.getId());

        // size 1 on each side catches a bad join that returns the shared fish twice
        assertEquals(1, firstFish.size(),
                "Shared fish should be returned once for the first water body");
        assertEquals(sharedFish.getId(), firstFish.getFirst().getId(),
                "First water body should return the shared fish");
        assertEquals(1, secondFish.size(),
                "Shared fish should be returned once for the second water body");
        assertEquals(sharedFish.getId(), secondFish.getFirst().getId(),
                "Second water body should return the shared fish");
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

        waterBody.setName("Test River");
        waterBody.setType("River");
        waterBody.setState("Montana");
        waterBody.setDescription("A river used for testing.");

        // slug is the value lookups use, so callers pass a different one per water body
        waterBody.setSlug(slug);

        return waterBody;
    }

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
     * Links a fish to a water body through the water body's {@code @ManyToMany}.
     *
     * <p>Water body owns the relationship, so the link is added on its side.</p>
     *
     * @param fish the saved fish to link
     * @param waterBody the saved water body to link
     */
    private void linkFishToWaterBody(Fish fish, WaterBody waterBody) {
        waterBody.getFishes().add(fish);
        waterBodyRepository.save(waterBody);
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
            slugs.add(waterBody.getSlug());
        }

        return slugs;
    }
}