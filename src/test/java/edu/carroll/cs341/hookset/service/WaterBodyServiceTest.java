package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.FishInput;
import edu.carroll.cs341.hookset.service.input.WaterBodyInput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import entities.jpa.entity.Fish;
import entities.jpa.entity.WaterBody;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link WaterBodyService}.
 *
 * <p>Each service method is tested against a happy path and a crappy path. Happy
 * paths cover valid input with an expected result. Crappy paths cover bad input
 * that should find nothing or be rejected, or an empty database.</p>
 *
 * <p>Test water bodies are created through
 * {@link WaterBodyService#addWaterBody(WaterBodyInput)} and test fish through
 * {@link FishService#addFish(FishInput)}, so no repositories are used.</p>
 *
 * <p>Tests run against the H2 test database, which starts empty, and each test is
 * rolled back by {@link Transactional}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WaterBodyServiceTest {

    /** Service under test. */
    @Autowired
    private WaterBodyService waterBodyService;

    /** Service used to create the fish that test water bodies are linked to. */
    @Autowired
    private FishService fishService;

    /** The name given to every test water body. */
    private static final String TEST_NAME = "Test River";

    /** The type given to every test water body. */
    private static final String TEST_TYPE = "River";

    /** The state given to every test water body. */
    private static final String TEST_STATE = "Montana";

    /** The description given to every test water body. */
    private static final String TEST_DESCRIPTION = "A river used for testing";

    /** The slug given to the main test water body. */
    private static final String TEST_SLUG = "test-river";

    /** The family given to every test fish. */
    private static final String TEST_FAMILY = "test-family";

    /** The species given to the main test fish. */
    private static final String TEST_SPECIES = "test-species";

    /** The image given to every test fish. */
    private static final String TEST_IMAGE = "test-image";

    // ---------- getAllWaterBodies ----------

    /**
     * Happy path: one added water body is returned in the list.
     */
    @Test
    public void testGetAllWaterBodiesHappyPathOneWaterBody() {
        // the water body is added through the service, since tests can't use repositories
        boolean waterBodyAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of()));

        // setup check, if this fails then addWaterBody or the test data is broken, not getAllWaterBodies
        assertTrue(waterBodyAdded, "The test water body should be added");

        List<WaterBody> waterBodies = waterBodyService.getAllWaterBodies();
        assertNotNull(waterBodies, "getAllWaterBodies should never return null");

        // the database starts empty, so the added water body should be the only one
        assertEquals(1, waterBodies.size(), "The list should only have the one added water body");

        // only one water body exists, so position 0 is safe here
        WaterBody returnedWaterBody = waterBodies.get(0);

        // every field is checked so a water body returned with the wrong details doesn't pass
        assertEquals(TEST_NAME, returnedWaterBody.getName(), "The returned name should match the one added");
        assertEquals(TEST_TYPE, returnedWaterBody.getType(), "The returned type should match the one added");
        assertEquals(TEST_STATE, returnedWaterBody.getState(), "The returned state should match the one added");
        assertEquals(TEST_DESCRIPTION, returnedWaterBody.getDescription(),
                "The returned description should match the one added");
        assertEquals(TEST_SLUG, returnedWaterBody.getSlug(), "The returned slug should match the one added");
    }

    /**
     * Happy path: many water bodies are added and every one of them is returned.
     */
    @Test
    public void testGetAllWaterBodiesHappyPathManyWaterBodies() {
        // each water body gets its own slug so they can be told apart in the results,
        // and so addWaterBody doesn't reject them for sharing a slug
        List<String> addedSlugs = new ArrayList<>();

        final int waterBodyCount = 100;
        for (int i = 1; i <= waterBodyCount; i++) {
            String slug = TEST_SLUG + "-" + i;
            boolean waterBodyAdded = waterBodyService.addWaterBody(
                    new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, slug, List.of()));

            // setup check, if this fails then addWaterBody or the test data is broken, not getAllWaterBodies
            assertTrue(waterBodyAdded, "Water body with slug " + slug + " should be added");
            addedSlugs.add(slug);
        }

        List<WaterBody> waterBodies = waterBodyService.getAllWaterBodies();
        assertNotNull(waterBodies, "getAllWaterBodies should never return null");

        // the database starts empty, so the list should only have the water bodies added above
        assertEquals(waterBodyCount, waterBodies.size(),
                "The list should have all " + waterBodyCount + " added water bodies");

        // checks by slug instead of position, since the list order isn't guaranteed
        List<String> returnedSlugs = new ArrayList<>();
        for (WaterBody returnedWaterBody : waterBodies) {
            returnedSlugs.add(returnedWaterBody.getSlug());
        }

        // the count alone could pass with the wrong water bodies, so check each one is actually there
        for (String slug : addedSlugs) {
            assertTrue(returnedSlugs.contains(slug), "Water body with slug " + slug + " should be in the list");
        }
    }

    /**
     * Crappy path: with no water bodies added, an empty list is returned instead of null.
     */
    @Test
    public void testGetAllWaterBodiesCrappyPathNoWaterBodies() {
        // nothing is added, so this checks the database's starting state
        List<WaterBody> waterBodies = waterBodyService.getAllWaterBodies();

        // callers loop over this list, so null would crash them where an empty list wouldn't
        assertNotNull(waterBodies, "getAllWaterBodies should never return null");
        assertTrue(waterBodies.isEmpty(), "The list should be empty when no water bodies have been added");
    }

    // ---------- getWaterBodyFromSlug ----------

    /**
     * Happy path: a valid slug returns the water body saved with it.
     */
    @Test
    public void testGetWaterBodyFromSlugHappyPath() {
        boolean waterBodyAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of()));

        // setup check, if this fails then addWaterBody or the test data is broken, not getWaterBodyFromSlug
        assertTrue(waterBodyAdded, "The test water body should be added");

        WaterBody returnedWaterBody = waterBodyService.getWaterBodyFromSlug(TEST_SLUG);

        // the water body was just added with this slug, so it should always be found
        assertNotNull(returnedWaterBody, "A water body should be found for a saved slug");

        // the database assigns the ID, so a saved water body should always have one
        assertNotNull(returnedWaterBody.getId(), "The returned water body should have an ID");

        // every field is checked so the wrong water body or wrong details don't pass
        assertEquals(TEST_NAME, returnedWaterBody.getName(), "The returned name should match the one added");
        assertEquals(TEST_TYPE, returnedWaterBody.getType(), "The returned type should match the one added");
        assertEquals(TEST_STATE, returnedWaterBody.getState(), "The returned state should match the one added");
        assertEquals(TEST_DESCRIPTION, returnedWaterBody.getDescription(),
                "The returned description should match the one added");
        assertEquals(TEST_SLUG, returnedWaterBody.getSlug(), "The returned slug should match the one looked up");
    }

    /**
     * Crappy path: a slug that no water body has returns null.
     */
    @Test
    public void testGetWaterBodyFromSlugCrappyPathMissingSlug() {
        final String missingSlug = "missing-river";

        // a water body is saved so a lookup that ignored the slug would return it and fail this test
        boolean waterBodyAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of()));

        // setup check, if this fails then addWaterBody or the test data is broken, not getWaterBodyFromSlug
        assertTrue(waterBodyAdded, "The test water body should be added");

        WaterBody returnedWaterBody = waterBodyService.getWaterBodyFromSlug(missingSlug);

        // no water body was saved with this slug, so nothing should come back
        assertNull(returnedWaterBody, "A slug that no water body has should return null");
    }

    /**
     * Crappy path: a slug with a space in it returns null.
     */
    @Test
    public void testGetWaterBodyFromSlugCrappyPathSpaces() {
        // same as the saved slug but with a space instead of the hyphen
        final String spacesSlug = "test river";

        // a water body is saved so a lookup that ignored the slug would return it and fail this test
        boolean waterBodyAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of()));

        // setup check, if this fails then addWaterBody or the test data is broken, not getWaterBodyFromSlug
        assertTrue(waterBodyAdded, "The test water body should be added");

        WaterBody returnedWaterBody = waterBodyService.getWaterBodyFromSlug(spacesSlug);

        // slugs have to match exactly, so a space doesn't get treated like the hyphen in the saved slug
        assertNull(returnedWaterBody, "A slug with a space in it should not match a saved slug");
    }

    /**
     * Crappy path: an empty slug returns null.
     */
    @Test
    public void testGetWaterBodyFromSlugCrappyPathBlank() {
        final String blankSlug = "";

        // a water body is saved so a lookup that ignored the slug would return it and fail this test
        boolean waterBodyAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of()));

        // setup check, if this fails then addWaterBody or the test data is broken, not getWaterBodyFromSlug
        assertTrue(waterBodyAdded, "The test water body should be added");

        WaterBody returnedWaterBody = waterBodyService.getWaterBodyFromSlug(blankSlug);

        // the interface says a blank slug returns null, since it can't match anything
        assertNull(returnedWaterBody, "An empty slug should return null");
    }

    // ---------- getAllFishForWaterBody ----------

    /**
     * Happy path: a water body with no fish linked returns an empty list instead of null.
     */
    @Test
    public void testGetAllFishForWaterBodyHappyPathNoFish() {
        // added with no fish IDs, since fish are optional on a water body
        boolean waterBodyAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of()));

        // setup check, if this fails then addWaterBody or the test data is broken, not getAllFishForWaterBody
        assertTrue(waterBodyAdded, "The test water body should be added");

        // addWaterBody doesn't return the water body, so the slug lookup is how the test gets its ID
        WaterBody savedWaterBody = waterBodyService.getWaterBodyFromSlug(TEST_SLUG);
        assertNotNull(savedWaterBody, "The test water body should be found by its slug");

        List<Fish> fish = waterBodyService.getAllFishForWaterBody(savedWaterBody.getId());

        // callers loop over this list, so null would crash them where an empty list wouldn't
        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(), "A water body with no fish linked should return an empty list");
    }

    /**
     * Happy path: a water body with one fish linked returns that fish.
     */
    @Test
    public void testGetAllFishForWaterBodyHappyPathOneFish() {
        boolean fishAdded = fishService.addFish(new FishInput(TEST_FAMILY, TEST_SPECIES, TEST_IMAGE));

        // setup check, if this fails then addFish or the test data is broken, not getAllFishForWaterBody
        assertTrue(fishAdded, "The test fish should be added");

        // addFish doesn't return the fish, and the database starts empty, so it's the only fish in the list
        Fish savedFish = fishService.getAllFish().get(0);

        boolean waterBodyAdded = waterBodyService.addWaterBody(new WaterBodyInput(
                TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of(savedFish.getId())));

        // setup check, if this fails then addWaterBody or the test data is broken, not getAllFishForWaterBody
        assertTrue(waterBodyAdded, "The test water body should be added with the fish linked");

        // addWaterBody doesn't return the water body, so the slug lookup is how the test gets its ID
        WaterBody savedWaterBody = waterBodyService.getWaterBodyFromSlug(TEST_SLUG);
        assertNotNull(savedWaterBody, "The test water body should be found by its slug");

        List<Fish> fish = waterBodyService.getAllFishForWaterBody(savedWaterBody.getId());
        assertNotNull(fish, "getAllFishForWaterBody should never return null");

        // only one fish was linked, so it should be the only one returned
        assertEquals(1, fish.size(), "The water body should have exactly the one linked fish");

        // only one fish exists, so position 0 is safe here
        Fish returnedFish = fish.get(0);

        // every field is checked so a link that pointed at the wrong fish doesn't pass
        assertEquals(savedFish.getId(), returnedFish.getId(), "The returned fish ID should match the linked fish");
        assertEquals(TEST_FAMILY, returnedFish.getFamily(), "The returned family should match the linked fish");
        assertEquals(TEST_SPECIES, returnedFish.getSpecies(), "The returned species should match the linked fish");
        assertEquals(TEST_IMAGE, returnedFish.getImage(), "The returned image should match the linked fish");
    }

    /**
     * Happy path: a water body with many fish linked returns every one of them.
     */
    @Test
    public void testGetAllFishForWaterBodyHappyPathManyFish() {
        // each fish gets its own species so they can be told apart in the results,
        // and so addFish doesn't reject them as duplicates of the same family and species
        List<String> addedSpecies = new ArrayList<>();

        final int fishCount = 100;
        for (int i = 1; i <= fishCount; i++) {
            String species = TEST_SPECIES + "-" + i;
            boolean fishAdded = fishService.addFish(new FishInput(TEST_FAMILY, species, TEST_IMAGE));

            // setup check, if this fails then addFish or the test data is broken, not getAllFishForWaterBody
            assertTrue(fishAdded, "Fish with species " + species + " should be added");
            addedSpecies.add(species);
        }

        // addFish doesn't return IDs, and the database starts empty, so every fish here is one added above
        List<Long> fishIds = new ArrayList<>();
        for (Fish savedFish : fishService.getAllFish()) {
            fishIds.add(savedFish.getId());
        }

        boolean waterBodyAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, fishIds));

        // setup check, if this fails then addWaterBody or the test data is broken, not getAllFishForWaterBody
        assertTrue(waterBodyAdded, "The test water body should be added with every fish linked");

        // addWaterBody doesn't return the water body, so the slug lookup is how the test gets its ID
        WaterBody savedWaterBody = waterBodyService.getWaterBodyFromSlug(TEST_SLUG);
        assertNotNull(savedWaterBody, "The test water body should be found by its slug");

        List<Fish> fish = waterBodyService.getAllFishForWaterBody(savedWaterBody.getId());
        assertNotNull(fish, "getAllFishForWaterBody should never return null");

        // every added fish was linked, so every one should come back
        assertEquals(fishCount, fish.size(), "The water body should have all " + fishCount + " linked fish");

        // checks by species instead of position, since the list order isn't guaranteed
        List<String> returnedSpecies = new ArrayList<>();
        for (Fish returnedFish : fish) {
            returnedSpecies.add(returnedFish.getSpecies());
        }

        // the count alone could pass with the wrong fish, so check each one is actually there
        for (String species : addedSpecies) {
            assertTrue(returnedSpecies.contains(species), "Fish with species " + species + " should be in the list");
        }
    }

    /**
     * Crappy path: an ID that no water body has returns an empty list instead of null.
     */
    @Test
    public void testGetAllFishForWaterBodyCrappyPathMissingWaterBody() {
        boolean fishAdded = fishService.addFish(new FishInput(TEST_FAMILY, TEST_SPECIES, TEST_IMAGE));

        // setup check, if this fails then addFish or the test data is broken, not getAllFishForWaterBody
        assertTrue(fishAdded, "The test fish should be added");

        // addFish doesn't return the fish, and the database starts empty, so it's the only fish in the list
        Fish savedFish = fishService.getAllFish().get(0);

        // a water body with a fish is saved so a lookup that ignored the ID would return that fish
        boolean waterBodyAdded = waterBodyService.addWaterBody(new WaterBodyInput(
                TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of(savedFish.getId())));

        // setup check, if this fails then addWaterBody or the test data is broken, not getAllFishForWaterBody
        assertTrue(waterBodyAdded, "The test water body should be added with the fish linked");

        WaterBody savedWaterBody = waterBodyService.getWaterBodyFromSlug(TEST_SLUG);
        assertNotNull(savedWaterBody, "The test water body should be found by its slug");

        // only one water body exists in this test, so the next ID can't belong to any water body
        final Long missingWaterBodyId = savedWaterBody.getId() + 1;

        List<Fish> fish = waterBodyService.getAllFishForWaterBody(missingWaterBodyId);

        // callers loop over this list, so null would crash them where an empty list wouldn't
        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(), "An ID that no water body has should return an empty list");
    }

    /**
     * Crappy path: a negative ID returns an empty list instead of null.
     */
    @Test
    public void testGetAllFishForWaterBodyCrappyPathNegativeId() {
        final Long negativeWaterBodyId = -100L;

        boolean fishAdded = fishService.addFish(new FishInput(TEST_FAMILY, TEST_SPECIES, TEST_IMAGE));

        // setup check, if this fails then addFish or the test data is broken, not getAllFishForWaterBody
        assertTrue(fishAdded, "The test fish should be added");

        // addFish doesn't return the fish, and the database starts empty, so it's the only fish in the list
        Fish savedFish = fishService.getAllFish().get(0);

        // a water body with a fish is saved so a lookup that ignored the ID would return that fish
        boolean waterBodyAdded = waterBodyService.addWaterBody(new WaterBodyInput(
                TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of(savedFish.getId())));

        // setup check, if this fails then addWaterBody or the test data is broken, not getAllFishForWaterBody
        assertTrue(waterBodyAdded, "The test water body should be added with the fish linked");

        List<Fish> fish = waterBodyService.getAllFishForWaterBody(negativeWaterBodyId);

        // the database never assigns negative IDs, so this can't match any water body
        assertNotNull(fish, "getAllFishForWaterBody should never return null");
        assertTrue(fish.isEmpty(), "A negative ID should return an empty list");
    }

    // ---------- addWaterBody ----------

    /**
     * Happy path: a valid water body is saved with every field it was given and its fish linked.
     */
    @Test
    public void testAddWaterBodyHappyPath() {
        boolean fishAdded = fishService.addFish(new FishInput(TEST_FAMILY, TEST_SPECIES, TEST_IMAGE));

        // setup check, if this fails then addFish or the test data is broken, not addWaterBody
        assertTrue(fishAdded, "The test fish should be added");

        // addFish doesn't return the fish, and the database starts empty, so it's the only fish in the list
        Fish savedFish = fishService.getAllFish().get(0);

        boolean waterBodyAdded = waterBodyService.addWaterBody(new WaterBodyInput(
                TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of(savedFish.getId())));

        // valid input in every field, so addWaterBody should accept it
        assertTrue(waterBodyAdded, "A valid water body should be added");

        // getWaterBodyFromSlug is the only way to see what was saved, since tests can't use repositories
        WaterBody savedWaterBody = waterBodyService.getWaterBodyFromSlug(TEST_SLUG);
        assertNotNull(savedWaterBody, "The added water body should be saved under its slug");

        // the database assigns the ID, so a saved water body should always have one
        assertNotNull(savedWaterBody.getId(), "The saved water body should be given an ID");

        // every field is checked so a water body saved with the wrong details doesn't pass
        assertEquals(TEST_NAME, savedWaterBody.getName(), "The saved name should match the input");
        assertEquals(TEST_TYPE, savedWaterBody.getType(), "The saved type should match the input");
        assertEquals(TEST_STATE, savedWaterBody.getState(), "The saved state should match the input");
        assertEquals(TEST_DESCRIPTION, savedWaterBody.getDescription(), "The saved description should match the input");
        assertEquals(TEST_SLUG, savedWaterBody.getSlug(), "The saved slug should match the input");

        // the fish link is part of the add, so it has to be saved too
        List<Fish> linkedFish = waterBodyService.getAllFishForWaterBody(savedWaterBody.getId());
        assertEquals(1, linkedFish.size(), "The water body should be saved with the one linked fish");
        assertEquals(savedFish.getId(), linkedFish.get(0).getId(), "The linked fish should be the one given");
    }

    /**
     * Crappy path: a blank name, type, state, description, or slug is rejected and nothing is saved.
     */
    @Test
    public void testAddWaterBodyCrappyPathBlank() {
        final String blank = "   ";

        // each field is blanked on its own so every check gets hit separately
        boolean blankNameAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(blank, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of()));
        assertFalse(blankNameAdded, "A water body with a blank name should not be added");

        boolean blankTypeAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, blank, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of()));
        assertFalse(blankTypeAdded, "A water body with a blank type should not be added");

        boolean blankStateAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, blank, TEST_DESCRIPTION, TEST_SLUG, List.of()));
        assertFalse(blankStateAdded, "A water body with a blank state should not be added");

        boolean blankDescriptionAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, blank, TEST_SLUG, List.of()));
        assertFalse(blankDescriptionAdded, "A water body with a blank description should not be added");

        boolean blankSlugAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, blank, List.of()));
        assertFalse(blankSlugAdded, "A water body with a blank slug should not be added");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(waterBodyService.getAllWaterBodies().isEmpty(), "No rejected water body should be saved");
    }

    /**
     * Crappy path: a name, type, state, description, or slug over its limit is rejected and nothing is saved.
     */
    @Test
    public void testAddWaterBodyCrappyPathTooLong() {
        // one character over the limits the WaterBodyService interface gives for each field
        final String tooLongName = "a".repeat(251);
        final String tooLongState = "b".repeat(101);
        final String tooLongDescription = "c".repeat(1001);
        final String tooLongSlug = "d".repeat(101);

        // type has no length limit of its own, but anything other than River, Lake, or Stream is rejected
        final String tooLongType = "River".repeat(21);

        // each field is too long on its own so every check gets hit separately
        boolean longNameAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(tooLongName, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of()));
        assertFalse(longNameAdded, "A name over 250 characters should not be added");

        boolean longTypeAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, tooLongType, TEST_STATE, TEST_DESCRIPTION, TEST_SLUG, List.of()));
        assertFalse(longTypeAdded, "A type that is not River, Lake, or Stream should not be added");

        boolean longStateAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, tooLongState, TEST_DESCRIPTION, TEST_SLUG, List.of()));
        assertFalse(longStateAdded, "A state over 100 characters should not be added");

        boolean longDescriptionAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, tooLongDescription, TEST_SLUG, List.of()));
        assertFalse(longDescriptionAdded, "A description over 1000 characters should not be added");

        boolean longSlugAdded = waterBodyService.addWaterBody(
                new WaterBodyInput(TEST_NAME, TEST_TYPE, TEST_STATE, TEST_DESCRIPTION, tooLongSlug, List.of()));
        assertFalse(longSlugAdded, "A slug over 100 characters should not be added");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(waterBodyService.getAllWaterBodies().isEmpty(), "No rejected water body should be saved");
    }
}