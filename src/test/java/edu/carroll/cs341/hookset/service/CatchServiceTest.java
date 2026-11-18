package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.CatchInput;
import edu.carroll.cs341.hookset.service.input.FishInput;
import edu.carroll.cs341.hookset.service.input.FlyInput;
import edu.carroll.cs341.hookset.service.input.SignupInput;
import edu.carroll.cs341.hookset.service.input.WaterBodyInput;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import entities.jpa.entity.Catch;
import entities.jpa.entity.Fish;
import entities.jpa.entity.WaterBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link CatchService}.
 *
 * <p>Each service method is tested against a happy path and a crappy path. Happy paths
 * cover valid input with an expected result, including values right at each limit and
 * the optional fields left empty. Crappy paths cover input the service is designed to
 * reject, such as another user's catch, a fish that isn't in the water body, or a date
 * in the future.</p>
 *
 * <p>The service never reads the login itself, so each test passes the user's ID
 * directly. Two users, a default fly, a fish, and a water body holding that fish are
 * created before each test, since every catch needs all of them as real foreign keys.
 * Everything is created through the services, so no repositories are used.</p>
 *
 * <p>Tests run against the H2 test database, which only has the tables and no data,
 * and each test is rolled back by {@link Transactional}. That means the only rows a
 * test sees are the ones made in setup and in the test itself.</p>
 *
 * <p>Sources Used:</p>
 * <ul>
 *     <li>Claude (Anthropic) was used to build this test code for CatchService, based on how
 *     I wrote all my previous tests. All code has been looked through and verified by me.</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CatchServiceTest {

    /** Service under test. */
    @Autowired
    private CatchService catchService;

    /** Service used to create the test users. */
    @Autowired
    private UserService userService;

    /** Service used to load the test users, so their IDs are known. */
    @Autowired
    private LoginService loginService;

    /** Service used to create the flies a catch is logged with. */
    @Autowired
    private FlyService flyService;

    /** Service used to create the fish a catch is logged as. */
    @Autowired
    private FishService fishService;

    /** Service used to create the water bodies a catch is logged in. */
    @Autowired
    private WaterBodyService waterBodyService;

    /** The username of the main test user. */
    private static final String TEST_USERNAME = "testuser";

    /** The username of the second test user, used to check catches stay with their owner. */
    private static final String OTHER_USERNAME = "otheruser";

    /** The plain text password every test user signs up with. */
    private static final String TEST_PASSWORD = "test-password";

    /** The slug of the water body made in setup. */
    private static final String TEST_WATER_BODY_SLUG = "catch-service-test-river";

    /** The fish length given to every test catch. */
    private static final BigDecimal TEST_FISH_LENGTH = new BigDecimal("14.00");

    /** The date given to every test catch, fixed in the past so it's always valid. */
    private static final LocalDateTime TEST_DATE_CAUGHT = LocalDateTime.of(2026, 6, 15, 8, 30);

    /** The notes given to the main test catch. */
    private static final String TEST_NOTES = "Caught on the first cast";

    /** The ID of the main test user, set before each test. */
    private Long testUserId;

    /** The ID of the second test user, set before each test. */
    private Long otherUserId;

    /** The ID of the default fly any user can log a catch with, set before each test. */
    private Long testFlyId;

    /** The ID of the fish each test catch is logged as, set before each test. */
    private Long testFishId;

    /** The ID of the water body each test catch is logged in, set before each test. */
    private Long testWaterBodyId;

    /**
     * Creates two users, a default fly, a fish, and a water body holding that fish before each test.
     */
    @BeforeEach
    public void setUpCatchData() {
        // everything is created through the services, since tests can't use repositories
        boolean testUserAdded = userService.addUser(new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not CatchService
        assertTrue(testUserAdded, "The test user should be added");

        boolean otherUserAdded = userService.addUser(new SignupInput(OTHER_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not CatchService
        assertTrue(otherUserAdded, "The other test user should be added");

        // addUser doesn't return the user, so loading it is how the test gets its ID
        UserDetails testUserDetails = loginService.loadUserByUsername(TEST_USERNAME);
        UserDetails otherUserDetails = loginService.loadUserByUsername(OTHER_USERNAME);

        // the ID is only on Hookset's own user details, so the loaded users have to be that type
        assertInstanceOf(HooksetUserDetails.class, testUserDetails, "The test user should be a HooksetUserDetails");
        assertInstanceOf(HooksetUserDetails.class, otherUserDetails, "The other user should be a HooksetUserDetails");

        testUserId = ((HooksetUserDetails) testUserDetails).getId();
        otherUserId = ((HooksetUserDetails) otherUserDetails).getId();

        // a default fly, so any user can log a catch with it
        boolean flyAdded = flyService.addDefaultHooksetFly(new FlyInput("Test Fly", "Dry", "olive", 12, 18));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not CatchService
        assertTrue(flyAdded, "The test fly should be added");

        // the database starts empty, so the only default fly is the one just added
        testFlyId = flyService.getAllHooksetFlies().get(0).getId();

        boolean fishAdded = fishService.addFish(new FishInput("test-family", "Test Trout", "test-image"));

        // setup check, if this fails then addFish or the test data is broken, not CatchService
        assertTrue(fishAdded, "The test fish should be added");

        // the database starts empty, so the only fish is the one just added
        testFishId = fishService.getAllFish().get(0).getId();

        // the fish is linked here, since a catch is only allowed when the fish lives in the water body
        boolean waterBodyAdded = waterBodyService.addWaterBody(new WaterBodyInput("Catch Service Test River",
                "River", "Montana", "A river used for catch service testing.", TEST_WATER_BODY_SLUG,
                List.of(testFishId)));

        // setup check, if this fails then addWaterBody or the test data is broken, not CatchService
        assertTrue(waterBodyAdded, "The test water body should be added");

        // addWaterBody doesn't return the water body, so the slug lookup is how the test gets its ID
        WaterBody savedWaterBody = waterBodyService.getWaterBodyFromSlug(TEST_WATER_BODY_SLUG);
        assertNotNull(savedWaterBody, "The test water body should be found by its slug");
        testWaterBodyId = savedWaterBody.getId();
    }

    // ---------- getAllCatches ----------

    /**
     * Happy path: one added catch is returned in the full list.
     */
    @Test
    public void testGetAllCatchesHappyPathOneCatch() {
        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not getAllCatches
        assertTrue(catchAdded, "The test catch should be added");

        List<Catch> catches = catchService.getAllCatches();
        assertNotNull(catches, "getAllCatches should never return null");

        // the database starts empty, so the added catch should be the only one
        assertEquals(1, catches.size(), "The one added catch should be the only catch returned");

        // only one catch exists, so position 0 is safe here
        assertEquals(TEST_NOTES, catches.get(0).getNotes(), "The returned catch should be the one that was added");
    }

    /**
     * Happy path: many catches from different users are all returned.
     */
    @Test
    public void testGetAllCatchesHappyPathManyCatches() {
        final int catchCount = 20;

        // half the catches belong to each user, since getAllCatches isn't limited to one user
        List<String> addedNotes = new ArrayList<>();
        for (int i = 1; i <= catchCount; i++) {
            String notes = "Catch " + i;
            Long ownerId = testUserId;
            if (i % 2 == 0) {
                ownerId = otherUserId;
            }

            boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                    TEST_FISH_LENGTH, TEST_DATE_CAUGHT, notes), ownerId);

            // setup check, if this fails then addCatch or the test data is broken, not getAllCatches
            assertTrue(catchAdded, "Catch with notes " + notes + " should be added");
            addedNotes.add(notes);
        }

        List<Catch> catches = catchService.getAllCatches();
        assertNotNull(catches, "getAllCatches should never return null");

        // every catch counts here, no matter who logged it
        assertEquals(catchCount, catches.size(), "All " + catchCount + " catches should be returned");

        // checks by notes instead of position, since the list order isn't guaranteed
        List<String> returnedNotes = new ArrayList<>();
        for (Catch returnedCatch : catches) {
            returnedNotes.add(returnedCatch.getNotes());
        }

        // the count alone could pass with the wrong catches, so check each one is actually there
        for (String notes : addedNotes) {
            assertTrue(returnedNotes.contains(notes), "Catch with notes " + notes + " should be in the list");
        }
    }

    /**
     * Crappy path: with no catches added, an empty list is returned instead of null.
     */
    @Test
    public void testGetAllCatchesCrappyPathNoCatches() {
        // setup adds no catches and the test database starts empty, so there's nothing to return
        List<Catch> catches = catchService.getAllCatches();

        // callers loop over this list, so null would crash them where an empty list wouldn't
        assertNotNull(catches, "getAllCatches should never return null");
        assertTrue(catches.isEmpty(), "No catches should be returned when none have been added");
    }

    // ---------- getAllUserCatches ----------

    /**
     * Happy path: every catch the user logged is returned, and another user's catches are left out.
     */
    @Test
    public void testGetAllUserCatchesHappyPath() {
        final String firstNotes = "Morning catch";
        final String secondNotes = "Evening catch";
        final String otherUserNotes = "Someone else's catch";

        boolean firstAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, firstNotes), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not getAllUserCatches
        assertTrue(firstAdded, "The first catch should be added");

        boolean secondAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, secondNotes), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not getAllUserCatches
        assertTrue(secondAdded, "The second catch should be added");

        // another user's catch is added so a lookup that ignored the user would return it and fail this test
        boolean otherAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, otherUserNotes), otherUserId);

        // setup check, if this fails then addCatch or the test data is broken, not getAllUserCatches
        assertTrue(otherAdded, "The other user's catch should be added");

        List<Catch> catches = catchService.getAllUserCatches(testUserId);
        assertNotNull(catches, "getAllUserCatches should never return null");

        // only the test user's two catches count
        assertEquals(2, catches.size(), "Both of the user's catches should be returned");

        // checks by notes instead of position, and every returned catch has to belong to this user
        List<String> returnedNotes = new ArrayList<>();
        for (Catch returnedCatch : catches) {
            assertEquals(testUserId, returnedCatch.getUserId(), "Every returned catch should belong to the user");
            returnedNotes.add(returnedCatch.getNotes());
        }

        assertTrue(returnedNotes.contains(firstNotes), "The user's first catch should be in the list");
        assertTrue(returnedNotes.contains(secondNotes), "The user's second catch should be in the list");
        assertFalse(returnedNotes.contains(otherUserNotes), "Another user's catch should not be in the list");
    }

    /**
     * Crappy path: a user with no catches gets an empty list, even when another user has catches.
     */
    @Test
    public void testGetAllUserCatchesCrappyPathNoCatches() {
        // another user's catch exists so a lookup that ignored the user ID would return it and fail
        boolean otherAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, "Someone else's catch"), otherUserId);

        // setup check, if this fails then addCatch or the test data is broken, not getAllUserCatches
        assertTrue(otherAdded, "The other user's catch should be added");

        List<Catch> catches = catchService.getAllUserCatches(testUserId);

        // callers loop over this list, so null would crash them where an empty list wouldn't
        assertNotNull(catches, "getAllUserCatches should never return null");
        assertTrue(catches.isEmpty(), "A user with no catches should get an empty list");
    }

    /**
     * Crappy path: a user ID that doesn't exist gets an empty list.
     */
    @Test
    public void testGetAllUserCatchesCrappyPathMissingUser() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        // a catch is added so a lookup that ignored the user would return it and fail this test
        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not getAllUserCatches
        assertTrue(catchAdded, "The test catch should be added");

        List<Catch> catches = catchService.getAllUserCatches(missingUserId);

        // a user that doesn't exist can't have logged any catches
        assertNotNull(catches, "getAllUserCatches should never return null");
        assertTrue(catches.isEmpty(), "A missing user should get an empty list");
    }

    // ---------- getCatchById ----------

    /**
     * Happy path: an added catch is found by its ID with every detail intact.
     */
    @Test
    public void testGetCatchByIdHappyPath() {
        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not getCatchById
        assertTrue(catchAdded, "The test catch should be added");

        // addCatch doesn't return the catch, and it's the user's only catch, so position 0 is safe
        final Long catchId = catchService.getAllUserCatches(testUserId).get(0).getId();

        Catch returnedCatch = catchService.getCatchById(catchId);

        // the catch was just added with this ID, so it should always be found
        assertNotNull(returnedCatch, "An added catch should be found by its ID");
        assertEquals(catchId, returnedCatch.getId(), "The returned catch should have the ID that was looked up");

        // every field is checked so the wrong catch or wrong details don't pass
        assertEquals(testUserId, returnedCatch.getUserId(), "The returned catch should belong to the user who logged it");
        assertEquals(testFlyId, returnedCatch.getFly().getId(), "The returned catch should have the fly it was logged with");
        assertEquals(testFishId, returnedCatch.getFish().getId(), "The returned catch should have the fish it was logged as");
        assertEquals(testWaterBodyId, returnedCatch.getWaterBody().getId(),
                "The returned catch should have the water body it was logged in");

        // compareTo instead of equals, since BigDecimal equals also compares the number of decimals
        assertEquals(0, TEST_FISH_LENGTH.compareTo(returnedCatch.getFishLength()),
                "The returned catch should have the fish length it was logged with");
        assertEquals(TEST_DATE_CAUGHT, returnedCatch.getDateCaught(), "The returned catch should have the date it was caught");
        assertEquals(TEST_NOTES, returnedCatch.getNotes(), "The returned catch should have the notes it was logged with");
    }

    /**
     * Crappy path: an ID no catch has, or a negative ID, returns null, even when other catches exist.
     */
    @Test
    public void testGetCatchByIdCrappyPath() {
        final Long negativeCatchId = -100L;

        // a real catch exists so a lookup that ignored the ID would return it and fail this test
        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not getCatchById
        assertTrue(catchAdded, "The test catch should be added");

        // only one catch exists in this test, so the next ID can't belong to any catch
        final Long missingCatchId = catchService.getAllUserCatches(testUserId).get(0).getId() + 1;

        Catch missingResult = catchService.getCatchById(missingCatchId);
        assertNull(missingResult, "No catch should be returned for an ID that doesn't exist");

        // the database never hands out negative IDs, so this can't match anything
        Catch negativeResult = catchService.getCatchById(negativeCatchId);
        assertNull(negativeResult, "No catch should be returned for a negative ID");
    }

    // ---------- addCatch ----------

    /**
     * Happy path: valid input is saved with every field and the right owner.
     */
    @Test
    public void testAddCatchHappyPath() {
        final BigDecimal fishLength = new BigDecimal("18.50");
        final String notes = "Caught below the riffle";

        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                fishLength, TEST_DATE_CAUGHT, notes), testUserId);

        // valid input for a real user, so addCatch should accept it
        assertTrue(catchAdded, "Valid catch input should be saved");

        // getAllUserCatches is the only way to see what was saved, since tests can't use repositories
        List<Catch> userCatches = catchService.getAllUserCatches(testUserId);
        assertEquals(1, userCatches.size(), "The user should have exactly one catch after adding one");

        // only one catch exists, so position 0 is safe here
        Catch savedCatch = userCatches.get(0);

        // the database assigns the ID, and the interface says the owner comes from the user ID passed in
        assertNotNull(savedCatch.getId(), "The saved catch should be given an ID");
        assertEquals(testUserId, savedCatch.getUserId(), "The catch owner should come from the user ID passed in");

        // every field is checked so a catch saved with the wrong details doesn't pass
        assertEquals(testFlyId, savedCatch.getFly().getId(), "The catch should be linked to the fly from the input");
        assertEquals(testFishId, savedCatch.getFish().getId(), "The catch should be linked to the fish from the input");
        assertEquals(testWaterBodyId, savedCatch.getWaterBody().getId(),
                "The catch should be linked to the water body from the input");

        // compareTo instead of equals, since BigDecimal equals also compares the number of decimals
        assertEquals(0, fishLength.compareTo(savedCatch.getFishLength()),
                "The catch should have the fish length from the input");
        assertEquals(TEST_DATE_CAUGHT, savedCatch.getDateCaught(), "The catch should have the date caught from the input");
        assertEquals(notes, savedCatch.getNotes(), "The catch should have the notes from the input");
    }

    /**
     * Happy path: a catch can be saved with the optional length and notes left out.
     */
    @Test
    public void testAddCatchHappyPathOptionalFieldsEmpty() {
        // the interface says length and notes are optional, so leaving them out is valid input
        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                null, TEST_DATE_CAUGHT, null), testUserId);
        assertTrue(catchAdded, "A catch with no length and no notes should be saved");

        Catch savedCatch = catchService.getAllUserCatches(testUserId).get(0);

        // the empty fields should stay empty instead of getting a made up value
        assertNull(savedCatch.getFishLength(), "The saved catch should have no fish length");
        assertNull(savedCatch.getNotes(), "The saved catch should have no notes");
    }

    /**
     * Happy path: a user can log a catch with their own custom fly.
     */
    @Test
    public void testAddCatchHappyPathOwnCustomFly() {
        boolean flyAdded = flyService.addFly(new FlyInput("Custom Fly", "Nymph", "black", 14, 20), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not addCatch
        assertTrue(flyAdded, "The custom fly should be added");

        // addFly doesn't return the fly, and it's the user's only custom fly, so position 0 is safe
        final Long customFlyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        boolean catchAdded = catchService.addCatch(new CatchInput(customFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);

        // the interface says a fly is valid if it's a default fly or belongs to the user
        assertTrue(catchAdded, "A catch with the user's own custom fly should be saved");
        assertEquals(customFlyId, catchService.getAllUserCatches(testUserId).get(0).getFly().getId(),
                "The catch should be linked to the user's custom fly");
    }

    /**
     * Happy path: values right at every limit are saved.
     */
    @Test
    public void testAddCatchHappyPathLimits() {
        // the smallest and largest lengths, and the longest notes allowed
        final BigDecimal smallestLength = new BigDecimal("0.01");
        final BigDecimal largestLength = new BigDecimal("999.99");
        final String longestNotes = "a".repeat(1000);

        // a minute ago is about as close to now as a test can safely get without risking the future
        final LocalDateTime recentDate = LocalDateTime.now().minusMinutes(1);

        // the limits themselves are where an off-by-one mistake would wrongly reject a catch
        boolean smallestAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                smallestLength, recentDate, longestNotes), testUserId);
        assertTrue(smallestAdded, "A catch at the smallest length with the longest notes should be saved");

        boolean largestAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                largestLength, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertTrue(largestAdded, "A catch at the largest length should be saved");

        // both adds returned true, so both catches should actually be saved
        assertEquals(2, catchService.getAllUserCatches(testUserId).size(), "Both catches at the limits should be saved");
    }

    /**
     * Crappy path: a fly, fish, or water body the user can't use is rejected and nothing is saved.
     */
    @Test
    public void testAddCatchCrappyPathInvalidReferences() {
        boolean otherFlyAdded = flyService.addFly(new FlyInput("Other Fly", "Dry", "olive", 12, 18), otherUserId);

        // setup check, if this fails then addFly or the test data is broken, not addCatch
        assertTrue(otherFlyAdded, "The other user's custom fly should be added");

        // addFly doesn't return the fly, and it's the other user's only custom fly, so position 0 is safe
        final Long otherUserFlyId = flyService.getAllUserFlies(otherUserId).get(0).getId();

        // the setup records are the only ones of each kind, so one past the largest ID can't belong to anything
        final Long missingFlyId = Math.max(testFlyId, otherUserFlyId) + 1;
        final Long missingFishId = testFishId + 1;
        final Long missingWaterBodyId = testWaterBodyId + 1;

        // each reference is broken on its own, so every check gets hit separately
        boolean otherFlyCatchAdded = catchService.addCatch(new CatchInput(otherUserFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertFalse(otherFlyCatchAdded, "A catch with another user's custom fly should not be saved");

        boolean missingFlyCatchAdded = catchService.addCatch(new CatchInput(missingFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertFalse(missingFlyCatchAdded, "A catch with a fly that doesn't exist should not be saved");

        boolean missingFishCatchAdded = catchService.addCatch(new CatchInput(testFlyId, missingFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertFalse(missingFishCatchAdded, "A catch with a fish that doesn't exist should not be saved");

        boolean missingWaterBodyCatchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId,
                missingWaterBodyId, TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertFalse(missingWaterBodyCatchAdded, "A catch in a water body that doesn't exist should not be saved");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(catchService.getAllUserCatches(testUserId).isEmpty(), "No rejected catch should be saved");
    }

    /**
     * Crappy path: a fish that doesn't live in the chosen water body is rejected and nothing is saved.
     */
    @Test
    public void testAddCatchCrappyPathFishNotInWaterBody() {
        boolean otherFishAdded = fishService.addFish(new FishInput("test-family", "Lake Only Fish", "test-image"));

        // setup check, if this fails then addFish or the test data is broken, not addCatch
        assertTrue(otherFishAdded, "The second fish should be added");

        // the second fish is found by species, since list order isn't guaranteed
        Long otherFishId = null;
        for (Fish savedFish : fishService.getAllFish()) {
            if (savedFish.getSpecies().equals("Lake Only Fish")) {
                otherFishId = savedFish.getId();
            }
        }
        assertNotNull(otherFishId, "The second fish should be found");

        // a real fish that just isn't linked to the test water body, so every other check passes
        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, otherFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, "Wrong water"), testUserId);
        assertFalse(catchAdded, "A fish that isn't in the water body should not be saved");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(catchService.getAllUserCatches(testUserId).isEmpty(), "No catch should be saved when the input is rejected");
    }

    /**
     * Crappy path: a bad length, a future date, or notes that are too long are rejected and nothing is saved.
     */
    @Test
    public void testAddCatchCrappyPathInvalidValues() {
        // zero and negative lengths aren't real fish, and 1000.00 is one cent past the largest length
        final BigDecimal zeroLength = BigDecimal.ZERO;
        final BigDecimal negativeLength = new BigDecimal("-5.00");
        final BigDecimal tooLongLength = new BigDecimal("1000.00");

        // the column only stores two decimals
        final BigDecimal tooManyDecimalsLength = new BigDecimal("12.345");

        // built from now, so it's always in the future no matter when the test runs
        final LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        // one over the 1000 character notes limit
        final String tooLongNotes = "a".repeat(1001);

        // each value is broken on its own, so every check gets hit separately
        boolean zeroAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                zeroLength, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertFalse(zeroAdded, "A catch with a length of zero should not be saved");

        boolean negativeAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                negativeLength, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertFalse(negativeAdded, "A catch with a negative length should not be saved");

        boolean tooLongAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                tooLongLength, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertFalse(tooLongAdded, "A catch longer than 999.99 should not be saved");

        boolean decimalsAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                tooManyDecimalsLength, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertFalse(decimalsAdded, "A catch length with three decimals should not be saved");

        boolean futureAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, futureDate, TEST_NOTES), testUserId);
        assertFalse(futureAdded, "A catch dated in the future should not be saved");

        boolean notesAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, tooLongNotes), testUserId);
        assertFalse(notesAdded, "A catch with notes over 1000 characters should not be saved");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(catchService.getAllUserCatches(testUserId).isEmpty(), "No rejected catch should be saved");
    }

    // ---------- updateCatch ----------

    /**
     * Happy path: the owner updates their catch and every new detail is saved.
     */
    @Test
    public void testUpdateCatchHappyPath() {
        final BigDecimal updatedLength = new BigDecimal("22.25");
        final LocalDateTime updatedDate = LocalDateTime.of(2026, 7, 4, 18, 0);
        final String updatedNotes = "New notes";

        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, "Old notes"), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not updateCatch
        assertTrue(catchAdded, "The test catch should be added");

        // addCatch doesn't return the catch, and it's the user's only catch, so position 0 is safe
        final Long catchId = catchService.getAllUserCatches(testUserId).get(0).getId();

        boolean catchUpdated = catchService.updateCatch(catchId, new CatchInput(testFlyId, testFishId,
                testWaterBodyId, updatedLength, updatedDate, updatedNotes), testUserId);

        // valid input on the user's own catch, so the update should be accepted
        assertTrue(catchUpdated, "The owner should be able to update their catch");

        // the catch is loaded again so the test checks what was saved, not what was sent
        Catch updatedCatch = catchService.getCatchById(catchId);
        assertNotNull(updatedCatch, "The catch should still exist after it's updated");

        // every changed field is checked so an update that only saved some fields doesn't pass
        assertEquals(updatedNotes, updatedCatch.getNotes(), "The catch notes should change to the new value");
        assertEquals(0, updatedLength.compareTo(updatedCatch.getFishLength()),
                "The catch fish length should change to the new value");
        assertEquals(updatedDate, updatedCatch.getDateCaught(), "The catch date should change to the new value");

        // the owner never comes from the input, so an edit can't hand the catch to someone else
        assertEquals(testUserId, updatedCatch.getUserId(), "Updating a catch should not change who owns it");
    }

    /**
     * Happy path: the owner moves their catch to a different fly, fish, and water body.
     */
    @Test
    public void testUpdateCatchHappyPathNewReferences() {
        final String newSpecies = "Lake Trout";
        final String newWaterBodySlug = "catch-service-test-lake";

        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not updateCatch
        assertTrue(catchAdded, "The test catch should be added");

        final Long catchId = catchService.getAllUserCatches(testUserId).get(0).getId();

        boolean customFlyAdded = flyService.addFly(new FlyInput("Custom Fly", "Streamer", "white", 4, 8), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateCatch
        assertTrue(customFlyAdded, "The custom fly should be added");

        final Long customFlyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        boolean newFishAdded = fishService.addFish(new FishInput("test-family", newSpecies, "test-image"));

        // setup check, if this fails then addFish or the test data is broken, not updateCatch
        assertTrue(newFishAdded, "The new fish should be added");

        // the new fish is found by species, since list order isn't guaranteed
        Long newFishId = null;
        for (Fish savedFish : fishService.getAllFish()) {
            if (savedFish.getSpecies().equals(newSpecies)) {
                newFishId = savedFish.getId();
            }
        }
        assertNotNull(newFishId, "The new fish should be found");

        // the new fish lives in the new water body, so the move is a valid catch
        boolean newWaterBodyAdded = waterBodyService.addWaterBody(new WaterBodyInput("Catch Service Test Lake",
                "Lake", "Montana", "A lake used for catch service testing.", newWaterBodySlug, List.of(newFishId)));

        // setup check, if this fails then addWaterBody or the test data is broken, not updateCatch
        assertTrue(newWaterBodyAdded, "The new water body should be added");

        final Long newWaterBodyId = waterBodyService.getWaterBodyFromSlug(newWaterBodySlug).getId();

        boolean catchUpdated = catchService.updateCatch(catchId, new CatchInput(customFlyId, newFishId,
                newWaterBodyId, TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertTrue(catchUpdated, "Moving the catch to a valid fly, fish, and water body should be accepted");

        // each link is checked so an update that only changed some of them doesn't pass
        Catch updatedCatch = catchService.getCatchById(catchId);
        assertEquals(customFlyId, updatedCatch.getFly().getId(), "The catch should now use the custom fly");
        assertEquals(newFishId, updatedCatch.getFish().getId(), "The catch should now be the new fish");
        assertEquals(newWaterBodyId, updatedCatch.getWaterBody().getId(), "The catch should now be in the new water body");
    }

    /**
     * Crappy path: a user can't update another user's catch or a catch that doesn't exist.
     */
    @Test
    public void testUpdateCatchCrappyPathNotOwner() {
        final String otherUserNotes = "Other user's notes";

        boolean otherAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, otherUserNotes), otherUserId);

        // setup check, if this fails then addCatch or the test data is broken, not updateCatch
        assertTrue(otherAdded, "The other user's catch should be added");

        // addCatch doesn't return the catch, and it's the other user's only catch, so position 0 is safe
        final Long otherCatchId = catchService.getAllUserCatches(otherUserId).get(0).getId();

        // only one catch exists in this test, so the next ID can't belong to any catch
        final Long missingCatchId = otherCatchId + 1;

        // valid input, so ownership is the only reason these can fail
        final CatchInput validInput = new CatchInput(testFlyId, testFishId, testWaterBodyId,
                new BigDecimal("22.25"), TEST_DATE_CAUGHT, "Hijacked notes");

        boolean otherCatchUpdated = catchService.updateCatch(otherCatchId, validInput, testUserId);
        assertFalse(otherCatchUpdated, "A user should not be able to update another user's catch");

        // the interface says a missing catch and someone else's catch look the same
        boolean missingCatchUpdated = catchService.updateCatch(missingCatchId, validInput, testUserId);
        assertFalse(missingCatchUpdated, "Updating a catch that doesn't exist should be rejected");

        // a rejected update returning false isn't enough, the other user's catch shouldn't have changed either
        Catch unchangedCatch = catchService.getCatchById(otherCatchId);
        assertEquals(otherUserNotes, unchangedCatch.getNotes(), "The other user's catch should not change");
        assertEquals(otherUserId, unchangedCatch.getUserId(), "The other user should still own their catch");
    }

    /**
     * Crappy path: bad input on the user's own catch is rejected and the catch is left unchanged.
     */
    @Test
    public void testUpdateCatchCrappyPathInvalidInput() {
        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not updateCatch
        assertTrue(catchAdded, "The test catch should be added");

        final Long catchId = catchService.getAllUserCatches(testUserId).get(0).getId();

        // the setup records are the only ones of each kind, so the next ID can't belong to anything
        final Long missingFishId = testFishId + 1;

        // each value is broken on its own, so every check gets hit separately
        boolean missingFishUpdated = catchService.updateCatch(catchId, new CatchInput(testFlyId, missingFishId,
                testWaterBodyId, TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertFalse(missingFishUpdated, "An update to a fish that doesn't exist should be rejected");

        boolean zeroLengthUpdated = catchService.updateCatch(catchId, new CatchInput(testFlyId, testFishId,
                testWaterBodyId, BigDecimal.ZERO, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);
        assertFalse(zeroLengthUpdated, "An update to a length of zero should be rejected");

        boolean futureUpdated = catchService.updateCatch(catchId, new CatchInput(testFlyId, testFishId,
                testWaterBodyId, TEST_FISH_LENGTH, LocalDateTime.now().plusDays(1), TEST_NOTES), testUserId);
        assertFalse(futureUpdated, "An update to a future date should be rejected");

        boolean longNotesUpdated = catchService.updateCatch(catchId, new CatchInput(testFlyId, testFishId,
                testWaterBodyId, TEST_FISH_LENGTH, TEST_DATE_CAUGHT, "a".repeat(1001)), testUserId);
        assertFalse(longNotesUpdated, "An update to notes over 1000 characters should be rejected");

        // a rejected update returning false isn't enough, the saved catch shouldn't have changed either
        Catch unchangedCatch = catchService.getCatchById(catchId);
        assertEquals(testFishId, unchangedCatch.getFish().getId(), "The fish should not change");
        assertEquals(0, TEST_FISH_LENGTH.compareTo(unchangedCatch.getFishLength()), "The length should not change");
        assertEquals(TEST_DATE_CAUGHT, unchangedCatch.getDateCaught(), "The date should not change");
        assertEquals(TEST_NOTES, unchangedCatch.getNotes(), "The notes should not change");
    }

    // ---------- isCatchOwner ----------

    /**
     * Happy path: the user who logged a catch is its owner.
     */
    @Test
    public void testIsCatchOwnerHappyPath() {
        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not isCatchOwner
        assertTrue(catchAdded, "The test catch should be added");

        final Long catchId = catchService.getAllUserCatches(testUserId).get(0).getId();

        boolean isOwner = catchService.isCatchOwner(catchId, testUserId);

        // the user logged this catch, so they own it
        assertTrue(isOwner, "The user who logged the catch should be its owner");
    }

    /**
     * Crappy path: another user, a missing user, or a missing catch is not an owner.
     */
    @Test
    public void testIsCatchOwnerCrappyPath() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean catchAdded = catchService.addCatch(new CatchInput(testFlyId, testFishId, testWaterBodyId,
                TEST_FISH_LENGTH, TEST_DATE_CAUGHT, TEST_NOTES), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not isCatchOwner
        assertTrue(catchAdded, "The test catch should be added");

        final Long catchId = catchService.getAllUserCatches(testUserId).get(0).getId();
        final Long missingCatchId = catchId + 1;

        // the catch exists, so a false here can only come from the owner check
        assertFalse(catchService.isCatchOwner(catchId, otherUserId), "A user should not own another user's catch");
        assertFalse(catchService.isCatchOwner(catchId, missingUserId), "A missing user should not own the catch");

        // the interface says the catch has to exist to be owned
        assertFalse(catchService.isCatchOwner(missingCatchId, testUserId), "A catch that doesn't exist should not be owned");
    }

    // ---------- isFlyValid ----------

    /**
     * Happy path: a default fly is valid for any user, and a custom fly is valid for its owner.
     */
    @Test
    public void testIsFlyValidHappyPath() {
        boolean customFlyAdded = flyService.addFly(new FlyInput("Custom Fly", "Nymph", "black", 14, 20), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyValid
        assertTrue(customFlyAdded, "The custom fly should be added");

        final Long customFlyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        // default flies are for everyone, so both users can use the setup fly
        assertTrue(catchService.isFlyValid(testFlyId, testUserId), "A default fly should be valid for the test user");
        assertTrue(catchService.isFlyValid(testFlyId, otherUserId), "A default fly should be valid for the other user");

        // the user made this fly, so they can log catches with it
        assertTrue(catchService.isFlyValid(customFlyId, testUserId), "A user's own custom fly should be valid for them");
    }

    /**
     * Crappy path: another user's custom fly or a missing fly is not valid.
     */
    @Test
    public void testIsFlyValidCrappyPath() {
        boolean otherFlyAdded = flyService.addFly(new FlyInput("Other Fly", "Dry", "olive", 12, 18), otherUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyValid
        assertTrue(otherFlyAdded, "The other user's custom fly should be added");

        final Long otherUserFlyId = flyService.getAllUserFlies(otherUserId).get(0).getId();

        // the only two flies are the setup fly and this one, so one past the largest can't belong to any fly
        final Long missingFlyId = Math.max(testFlyId, otherUserFlyId) + 1;

        // the fly exists, so a false here can only come from the custom fly owner check
        assertFalse(catchService.isFlyValid(otherUserFlyId, testUserId),
                "Another user's custom fly should not be valid for this user");

        // a fly that doesn't exist can't be used by anyone
        assertFalse(catchService.isFlyValid(missingFlyId, testUserId), "A fly that doesn't exist should not be valid");
    }

    // ---------- isFishValid ----------

    /**
     * Happy path: an added fish is valid.
     */
    @Test
    public void testIsFishValidHappyPath() {
        boolean fishValid = catchService.isFishValid(testFishId);

        // the fish was added in setup, so it exists
        assertTrue(fishValid, "An added fish should be valid");
    }

    /**
     * Crappy path: a fish ID that doesn't exist, or a negative ID, is not valid.
     */
    @Test
    public void testIsFishValidCrappyPath() {
        // the setup fish is the only fish, so the next ID can't belong to any fish
        final Long missingFishId = testFishId + 1;
        final Long negativeFishId = -100L;

        assertFalse(catchService.isFishValid(missingFishId), "A fish ID that doesn't exist should not be valid");

        // the database never hands out negative IDs, so this can't match anything
        assertFalse(catchService.isFishValid(negativeFishId), "A negative fish ID should not be valid");
    }

    // ---------- isWaterBodyValid ----------

    /**
     * Happy path: an added water body is valid.
     */
    @Test
    public void testIsWaterBodyValidHappyPath() {
        boolean waterBodyValid = catchService.isWaterBodyValid(testWaterBodyId);

        // the water body was added in setup, so it exists
        assertTrue(waterBodyValid, "An added water body should be valid");
    }

    /**
     * Crappy path: a water body ID that doesn't exist, or a negative ID, is not valid.
     */
    @Test
    public void testIsWaterBodyValidCrappyPath() {
        // the setup water body is the only one, so the next ID can't belong to any water body
        final Long missingWaterBodyId = testWaterBodyId + 1;
        final Long negativeWaterBodyId = -100L;

        assertFalse(catchService.isWaterBodyValid(missingWaterBodyId),
                "A water body ID that doesn't exist should not be valid");

        // the database never hands out negative IDs, so this can't match anything
        assertFalse(catchService.isWaterBodyValid(negativeWaterBodyId), "A negative water body ID should not be valid");
    }

    // ---------- isFishInWaterBody ----------

    /**
     * Happy path: a fish linked to a water body is found in it, even when the water body has other fish.
     */
    @Test
    public void testIsFishInWaterBodyHappyPath() {
        final String secondSpecies = "Brown Trout";
        final String sharedWaterBodySlug = "catch-service-test-creek";

        // the setup water body was made with the test fish linked
        assertTrue(catchService.isFishInWaterBody(testFishId, testWaterBodyId),
                "A fish linked to the water body should be found in it");

        boolean secondFishAdded = fishService.addFish(new FishInput("test-family", secondSpecies, "test-image"));

        // setup check, if this fails then addFish or the test data is broken, not isFishInWaterBody
        assertTrue(secondFishAdded, "The second fish should be added");

        // the second fish is found by species, since list order isn't guaranteed
        Long secondFishId = null;
        for (Fish savedFish : fishService.getAllFish()) {
            if (savedFish.getSpecies().equals(secondSpecies)) {
                secondFishId = savedFish.getId();
            }
        }
        assertNotNull(secondFishId, "The second fish should be found");

        // a water body with two fish, so a check that only looked at the first fish would fail
        boolean sharedAdded = waterBodyService.addWaterBody(new WaterBodyInput("Catch Service Test Creek", "Stream",
                "Montana", "A creek used for catch service testing.", sharedWaterBodySlug,
                List.of(testFishId, secondFishId)));

        // setup check, if this fails then addWaterBody or the test data is broken, not isFishInWaterBody
        assertTrue(sharedAdded, "The shared water body should be added");

        final Long sharedWaterBodyId = waterBodyService.getWaterBodyFromSlug(sharedWaterBodySlug).getId();

        assertTrue(catchService.isFishInWaterBody(testFishId, sharedWaterBodyId),
                "The first fish should be found in the shared water body");
        assertTrue(catchService.isFishInWaterBody(secondFishId, sharedWaterBodyId),
                "The second fish should be found in the shared water body");
    }

    /**
     * Crappy path: a fish that isn't linked, or a fish or water body that doesn't exist, is not found.
     */
    @Test
    public void testIsFishInWaterBodyCrappyPath() {
        final String otherSpecies = "Lake Only Fish";
        final String otherWaterBodySlug = "catch-service-test-lake";

        boolean otherFishAdded = fishService.addFish(new FishInput("test-family", otherSpecies, "test-image"));

        // setup check, if this fails then addFish or the test data is broken, not isFishInWaterBody
        assertTrue(otherFishAdded, "The second fish should be added");

        // the second fish is found by species, since list order isn't guaranteed
        Long otherFishId = null;
        for (Fish savedFish : fishService.getAllFish()) {
            if (savedFish.getSpecies().equals(otherSpecies)) {
                otherFishId = savedFish.getId();
            }
        }
        assertNotNull(otherFishId, "The second fish should be found");

        // the second fish lives in a different water body, so it's linked somewhere, just not in the test river
        boolean otherWaterBodyAdded = waterBodyService.addWaterBody(new WaterBodyInput("Catch Service Test Lake",
                "Lake", "Montana", "A lake used for catch service testing.", otherWaterBodySlug, List.of(otherFishId)));

        // setup check, if this fails then addWaterBody or the test data is broken, not isFishInWaterBody
        assertTrue(otherWaterBodyAdded, "The other water body should be added");

        final Long otherWaterBodyId = waterBodyService.getWaterBodyFromSlug(otherWaterBodySlug).getId();

        // one past the largest ID of each kind can't belong to anything
        final Long missingFishId = Math.max(testFishId, otherFishId) + 1;
        final Long missingWaterBodyId = Math.max(testWaterBodyId, otherWaterBodyId) + 1;

        // both exist, so a false here can only come from the missing link
        assertFalse(catchService.isFishInWaterBody(otherFishId, testWaterBodyId),
                "A fish from a different water body should not be found in the test river");

        // the test fish was never linked to the lake, so it shouldn't be found there
        assertFalse(catchService.isFishInWaterBody(testFishId, otherWaterBodyId),
                "The test fish should not be found in a water body it isn't linked to");

        assertFalse(catchService.isFishInWaterBody(missingFishId, testWaterBodyId),
                "A fish that doesn't exist should not be found in any water body");
        assertFalse(catchService.isFishInWaterBody(testFishId, missingWaterBodyId),
                "No fish should be found in a water body that doesn't exist");
    }

    // ---------- isFishLengthValid ----------

    /**
     * Happy path: normal lengths, lengths right at the limits, whole numbers, and no length at all are valid.
     */
    @Test
    public void testIsFishLengthValidHappyPath() {
        // the smallest and largest lengths the interface allows
        final BigDecimal smallestLength = new BigDecimal("0.01");
        final BigDecimal largestLength = new BigDecimal("999.99");

        // no decimals at all is still at most two decimals
        final BigDecimal wholeLength = new BigDecimal("16");

        assertTrue(catchService.isFishLengthValid(new BigDecimal("16.75")), "A normal fish length should be valid");

        // the limits themselves are where an off-by-one mistake would wrongly reject a length
        assertTrue(catchService.isFishLengthValid(smallestLength), "A length of 0.01 should be valid");
        assertTrue(catchService.isFishLengthValid(largestLength), "A length of 999.99 should be valid");

        assertTrue(catchService.isFishLengthValid(wholeLength), "A whole number length should be valid");

        // the interface says length is optional, so leaving it out is valid
        assertTrue(catchService.isFishLengthValid(null), "No length at all should be valid");
    }

    /**
     * Crappy path: zero, negative, too long, or too many decimals is not valid.
     */
    @Test
    public void testIsFishLengthValidCrappyPath() {
        final BigDecimal negativeLength = new BigDecimal("-5.00");

        // one cent past the largest length allowed
        final BigDecimal tooLongLength = new BigDecimal("1000.00");

        // the column only stores two decimals
        final BigDecimal tooManyDecimalsLength = new BigDecimal("12.345");

        // a length has to be greater than zero, so zero itself is the first value that fails
        assertFalse(catchService.isFishLengthValid(BigDecimal.ZERO), "A fish length of zero should not be valid");

        // a negative length isn't a real fish
        assertFalse(catchService.isFishLengthValid(negativeLength), "A negative fish length should not be valid");

        assertFalse(catchService.isFishLengthValid(tooLongLength), "A fish length over 999.99 should not be valid");
        assertFalse(catchService.isFishLengthValid(tooManyDecimalsLength),
                "A fish length with three decimals should not be valid");
    }

    // ---------- isDateCaughtValid ----------

    /**
     * Happy path: a date in the past, or a moment ago, is valid.
     */
    @Test
    public void testIsDateCaughtValidHappyPath() {
        // a minute ago is about as close to now as a test can safely get without risking the future
        final LocalDateTime recentDate = LocalDateTime.now().minusMinutes(1);

        assertTrue(catchService.isDateCaughtValid(TEST_DATE_CAUGHT), "A date in the past should be valid");

        // a catch logged right after it happened still has to be accepted
        assertTrue(catchService.isDateCaughtValid(recentDate), "A date from a minute ago should be valid");
    }

    /**
     * Crappy path: a date in the future, even a little, or no date at all is not valid.
     */
    @Test
    public void testIsDateCaughtValidCrappyPath() {
        // built from now, so they're always in the future no matter when the test runs
        final LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        final LocalDateTime inAnHour = LocalDateTime.now().plusHours(1);

        assertFalse(catchService.isDateCaughtValid(tomorrow), "A date tomorrow should not be valid");

        // close to now is still the future, so a check that only looked at the day would wrongly allow this
        assertFalse(catchService.isDateCaughtValid(inAnHour), "A date an hour from now should not be valid");

        // the interface says the date has to be present, unlike length and notes
        assertFalse(catchService.isDateCaughtValid(null), "No date at all should not be valid");
    }

    // ---------- isNotesValid ----------

    /**
     * Happy path: short notes, notes right at 1000 characters, and no notes at all are valid.
     */
    @Test
    public void testIsNotesValidHappyPath() {
        // exactly at the limit, which is still allowed
        final String longestNotes = "a".repeat(1000);

        assertTrue(catchService.isNotesValid("Rose to a hopper on the far bank"), "Short notes should be valid");

        // the limit itself is where an off-by-one mistake would wrongly reject notes
        assertTrue(catchService.isNotesValid(longestNotes), "Notes of exactly 1000 characters should be valid");

        // the interface says notes are optional, so leaving them out is valid
        assertTrue(catchService.isNotesValid(null), "No notes at all should be valid");
    }

    /**
     * Crappy path: notes over 1000 characters are not valid.
     */
    @Test
    public void testIsNotesValidCrappyPath() {
        // one over the 1000 character limit, and well past it
        final String tooLongNotes = "a".repeat(1001);
        final String wayTooLongNotes = "a".repeat(5000);

        // the first length past the limit is where an off-by-one mistake would wrongly allow notes
        assertFalse(catchService.isNotesValid(tooLongNotes), "Notes of 1001 characters should not be valid");

        // far past the limit should obviously fail too
        assertFalse(catchService.isNotesValid(wayTooLongNotes), "Notes of 5000 characters should not be valid");
    }
}