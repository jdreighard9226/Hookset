package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.FishInput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import entities.jpa.entity.Fish;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link FishService}.
 *
 * <p>Each service method is tested against a happy path, a crappy path, and a crazy
 * path. Happy paths cover valid input with an expected result. Crappy paths cover
 * bad input that should be rejected, or an empty database. Crazy paths cover null
 * input and data that is close to a duplicate without being one.</p>
 *
 * <p>Test fish are created through {@link FishService#addFish(FishInput)}, so no
 * repositories are used. Looking up the fish in a single water body is tested in
 * {@link WaterBodyServiceTest}, since that lookup goes through the water body.</p>
 *
 * <p>Tests run against the H2 test database, which starts empty, and each test is
 * rolled back by {@link Transactional}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FishServiceTest {

    /** Service under test. */
    @Autowired
    private FishService fishService;

    /** The family given to every test fish. */
    private static final String TEST_FAMILY = "test-family";

    /** The species given to the main test fish. */
    private static final String TEST_SPECIES = "test-species";

    /** The image given to every test fish. */
    private static final String TEST_IMAGE = "test-image";

    // ---------- getAllFish ----------

    /**
     * Happy path: one added fish is returned in the list.
     */
    @Test
    public void testGetAllFishHappyPathOneFish() {
        // the fish is added through the service, since tests can't use repositories
        boolean fishAdded = fishService.addFish(new FishInput(TEST_FAMILY, TEST_SPECIES, TEST_IMAGE));
        assertTrue(fishAdded, "The test fish should be added");

        List<Fish> fish = fishService.getAllFish();
        assertNotNull(fish, "getAllFish should never return null");

        // the database starts empty, so the added fish should be the only one
        assertEquals(1, fish.size(), "The list should only have the one added fish");

        // only one fish exists, so position 0 is safe here
        Fish returnedFish = fish.get(0);

        // every field is checked so a fish returned with the wrong details doesn't pass
        assertEquals(TEST_FAMILY, returnedFish.getFamily(), "The returned family should match the one added");
        assertEquals(TEST_SPECIES, returnedFish.getSpecies(), "The returned species should match the one added");
        assertEquals(TEST_IMAGE, returnedFish.getImage(), "The returned image should match the one added");
    }

    /**
     * Happy path: many fish are added and every one of them is returned.
     */
    @Test
    public void testGetAllFishHappyPathManyFish() {
        // each fish gets its own species so they can be told apart in the results,
        // and so addFish doesn't reject them as duplicates of the same family and species
        List<String> addedSpecies = new ArrayList<>();

        final int fishCount = 100;
        for (int i = 1; i <= fishCount; i++) {
            String species = TEST_SPECIES + "-" + i;
            boolean fishAdded = fishService.addFish(new FishInput(TEST_FAMILY, species, TEST_IMAGE));

            // setup check, if this fails then addFish or the test data is broken, not getAllFish
            assertTrue(fishAdded, "Fish with species " + species + " should be added");
            addedSpecies.add(species);
        }

        List<Fish> fish = fishService.getAllFish();
        assertNotNull(fish, "getAllFish should never return null");

        // the database starts empty, so the list should only have the fish added above
        assertEquals(fishCount, fish.size(), "The list should have all " + fishCount + " added fish");

        // checks by species instead of position, since the list order isn't guaranteed
        List<String> returnedSpecies = new ArrayList<>();
        for (Fish returnedFish : fish) {
            returnedSpecies.add(returnedFish.getSpecies());
        }

        // the count alone could pass with the wrong fish, so check each one is actually there
        for (String species : addedSpecies) {
            assertTrue(returnedSpecies.contains(species), "Missing fish with species: " + species);
        }
    }

    /**
     * Crappy path: with no fish added, an empty list is returned instead of null.
     */
    @Test
    public void testGetAllFishCrappyPathNoFish() {
        // nothing is added, so this checks the database's starting state
        List<Fish> fish = fishService.getAllFish();

        // callers loop over this list, so null would crash them where an empty list wouldn't
        assertNotNull(fish, "getAllFish should never return null");
        assertTrue(fish.isEmpty(), "The list should be empty when no fish have been added");
    }

    /**
     * Crazy path: fish that share a family or a species, but not both, are all returned separately.
     */
    @Test
    public void testGetAllFishCrazyPathNearDuplicates() {
        final String otherFamily = "other-family";
        final String otherSpecies = "other-species";

        boolean originalAdded = fishService.addFish(new FishInput(TEST_FAMILY, TEST_SPECIES, TEST_IMAGE));
        assertTrue(originalAdded, "The original fish should be added");

        // only an exact family and species match is a duplicate, so these are different fish
        boolean sameSpeciesAdded = fishService.addFish(new FishInput(otherFamily, TEST_SPECIES, TEST_IMAGE));
        assertTrue(sameSpeciesAdded, "A fish with the same species but a different family should be added");

        boolean sameFamilyAdded = fishService.addFish(new FishInput(TEST_FAMILY, otherSpecies, TEST_IMAGE));
        assertTrue(sameFamilyAdded, "A fish with the same family but a different species should be added");

        List<Fish> fish = fishService.getAllFish();
        assertNotNull(fish, "getAllFish should never return null");

        // a list that merged similar fish together would come back with fewer than 3
        assertEquals(3, fish.size(), "All three fish should be returned separately");

        // stay null unless the loop finds a matching fish, so a missing fish fails the assertNotNull below
        Fish originalFish = null;
        Fish sameSpeciesFish = null;
        Fish sameFamilyFish = null;

        // order isn't guaranteed, so find each fish by its family and species instead of its position
        for (Fish returnedFish : fish) {
            if (returnedFish.getFamily().equals(TEST_FAMILY) && returnedFish.getSpecies().equals(TEST_SPECIES)) {
                originalFish = returnedFish;
            }

            if (returnedFish.getFamily().equals(otherFamily) && returnedFish.getSpecies().equals(TEST_SPECIES)) {
                sameSpeciesFish = returnedFish;
            }

            if (returnedFish.getFamily().equals(TEST_FAMILY) && returnedFish.getSpecies().equals(otherSpecies)) {
                sameFamilyFish = returnedFish;
            }
        }

        // checked before the assertEquals so a missing fish fails with a clear message instead of a NullPointerException
        assertNotNull(originalFish, "The original fish should be in the list");
        assertEquals(TEST_FAMILY, originalFish.getFamily(), "The original fish should have the test family");
        assertEquals(TEST_SPECIES, originalFish.getSpecies(), "The original fish should have the test species");

        assertNotNull(sameSpeciesFish, "The same species, different family fish should be in the list");
        assertEquals(otherFamily, sameSpeciesFish.getFamily(), "The same species fish should have the other family");
        assertEquals(TEST_SPECIES, sameSpeciesFish.getSpecies(), "The same species fish should have the test species");

        assertNotNull(sameFamilyFish, "The same family, different species fish should be in the list");
        assertEquals(TEST_FAMILY, sameFamilyFish.getFamily(), "The same family fish should have the test family");
        assertEquals(otherSpecies, sameFamilyFish.getSpecies(), "The same family fish should have the other species");
    }

    // ---------- addFish ----------

    /**
     * Happy path: one valid fish is added and saved with every field it was given.
     */
    @Test
    public void testAddFishHappyPathOneFish() {
        boolean fishAdded = fishService.addFish(new FishInput(TEST_FAMILY, TEST_SPECIES, TEST_IMAGE));

        // valid input in every field, so addFish should accept it
        assertTrue(fishAdded, "A valid fish should be added");

        // getAllFish is the only way to see what was saved,
        // since tests shouldn't have intimate knowledge of the data layer
        List<Fish> fish = fishService.getAllFish();
        assertNotNull(fish, "getAllFish should never return null");

        // the database starts empty, so the added fish should be the only one
        assertEquals(1, fish.size(), "The added fish should be the only fish saved");

        // only one fish exists, so position 0 is safe here
        Fish savedFish = fish.get(0);

        // the database assigns the ID, so a saved fish should always have one
        assertNotNull(savedFish.getId(), "The saved fish should be given an ID");

        // every field is checked so a fish saved with the wrong details doesn't pass
        assertEquals(TEST_FAMILY, savedFish.getFamily(), "The saved family should match the input");
        assertEquals(TEST_SPECIES, savedFish.getSpecies(), "The saved species should match the input");
        assertEquals(TEST_IMAGE, savedFish.getImage(), "The saved image should match the input");
    }

    /**
     * Happy path: many valid fish are added and every one of them is saved.
     */
    @Test
    public void testAddFishHappyPathManyFish() {
        // each fish gets its own species so addFish doesn't reject them as duplicates
        List<String> addedSpecies = new ArrayList<>();

        final int fishCount = 100;
        for (int i = 1; i <= fishCount; i++) {
            String species = TEST_SPECIES + "-" + i;
            boolean fishAdded = fishService.addFish(new FishInput(TEST_FAMILY, species, TEST_IMAGE));

            // checked inside the loop so a rejected add points to the exact fish that failed
            assertTrue(fishAdded, "Fish with species " + species + " should be added");
            addedSpecies.add(species);
        }

        List<Fish> fish = fishService.getAllFish();
        assertNotNull(fish, "getAllFish should never return null");

        // every add returned true, so every fish should actually be saved
        assertEquals(fishCount, fish.size(), "All " + fishCount + " added fish should be saved");

        // checks by species instead of position, since the list order isn't guaranteed
        List<String> savedSpecies = new ArrayList<>();
        for (Fish savedFish : fish) {
            savedSpecies.add(savedFish.getSpecies());
        }

        // the count alone could pass with the wrong fish, so check each one is actually there
        for (String species : addedSpecies) {
            assertTrue(savedSpecies.contains(species), "Fish with species " + species + " should be saved");
        }
    }

    /**
     * Crappy path: a blank family, species, or image is rejected and nothing is saved.
     */
    @Test
    public void testAddFishCrappyPathBlank() {
        final String blank = "   ";

        // each field is blanked on its own so every check gets hit separately
        boolean blankFamilyAdded = fishService.addFish(new FishInput(blank, TEST_SPECIES, TEST_IMAGE));
        assertFalse(blankFamilyAdded, "A fish with a blank family should not be added");

        boolean blankSpeciesAdded = fishService.addFish(new FishInput(TEST_FAMILY, blank, TEST_IMAGE));
        assertFalse(blankSpeciesAdded, "A fish with a blank species should not be added");

        boolean blankImageAdded = fishService.addFish(new FishInput(TEST_FAMILY, TEST_SPECIES, blank));
        assertFalse(blankImageAdded, "A fish with a blank image should not be added");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(fishService.getAllFish().isEmpty(), "No rejected fish should be saved");
    }

    /**
     * Crappy path: a family, species, or image one character over its limit is rejected and nothing is saved.
     */
    @Test
    public void testAddFishCrappyPathTooLong() {
        // one character over the limits the FishService interface gives for each field
        final String tooLongName = "a".repeat(201);
        final String tooLongImage = "b".repeat(101);

        // each field is too long on its own so every check gets hit separately
        boolean longFamilyAdded = fishService.addFish(new FishInput(tooLongName, TEST_SPECIES, TEST_IMAGE));
        assertFalse(longFamilyAdded, "A family over 200 characters should not be added");

        boolean longSpeciesAdded = fishService.addFish(new FishInput(TEST_FAMILY, tooLongName, TEST_IMAGE));
        assertFalse(longSpeciesAdded, "A species over 200 characters should not be added");

        boolean longImageAdded = fishService.addFish(new FishInput(TEST_FAMILY, TEST_SPECIES, tooLongImage));
        assertFalse(longImageAdded, "An image over 100 characters should not be added");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(fishService.getAllFish().isEmpty(), "No rejected fish should be saved");
    }
}