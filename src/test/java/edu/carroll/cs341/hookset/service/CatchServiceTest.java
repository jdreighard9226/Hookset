package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.CatchRepository;
import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.jpa.repo.FlyRepository;
import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import edu.carroll.cs341.hookset.service.input.CatchInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import entities.jpa.entity.Catch;
import entities.jpa.entity.Fish;
import entities.jpa.entity.Fly;
import entities.jpa.entity.User;
import entities.jpa.entity.WaterBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link edu.carroll.cs341.hookset.service.impl.CatchServiceImpl}.
 *
 * <p>Each service method is tested against a happy path, which covers valid input with
 * an expected result. Crappy and crazy paths will be added separately.</p>
 *
 * <p>The service never reads the login itself, so each test passes the user's ID
 * directly. A user, a default fly, a fish, and a water body holding that fish are saved
 * before each test, since every catch needs all of them as real foreign keys.</p>
 *
 * <p>Tests run against the H2 test database, which only has the tables and no data,
 * and each test is rolled back by {@link Transactional}. That means the only rows a
 * test sees are the ones saved in setup and in the test itself.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CatchServiceTest {

    /** Service under test. */
    @Autowired
    private CatchService catchService;

    /** Repository used to set up and check catch test data. */
    @Autowired
    private CatchRepository catchRepository;

    /** Repository used to set up fly test data. */
    @Autowired
    private FlyRepository flyRepository;

    /** Repository used to set up fish test data. */
    @Autowired
    private FishRepository fishRepository;

    /** Repository used to set up water body test data. */
    @Autowired
    private WaterBodyRepository waterBodyRepository;

    /** Repository used to set up user test data. */
    @Autowired
    private UserRepository userRepository;

    /** Password encoder used to hash test user passwords the same way signup does. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** The main user each test acts as. */
    private User testUser;

    /** A default fly any user can log a catch with. */
    private Fly testFly;

    /** The fish each test catch is logged as. */
    private Fish testFish;

    /** The water body each test catch is logged in, with the test fish linked to it. */
    private WaterBody testWaterBody;


    /**
     * Saves a user, a default fly, a fish, and a water body holding that fish before each test.
     */
    @BeforeEach
    public void setUpCatchData() {
        testUser = userRepository.save(createTestUser("testuser"));
        testFly = flyRepository.save(createTestFly("Test Fly", "test-fly", null));
        testFish = fishRepository.save(createTestFish("Test Trout"));

        // water body owns the @ManyToMany, so the fish is linked through it
        WaterBody waterBody = createTestWaterBody("catch-service-test-river");
        waterBody.getFishes().add(testFish);
        testWaterBody = waterBodyRepository.save(waterBody);
    }

    // ---------- getAllCatches ----------

    /**
     * Happy path: a saved catch is returned in the full list.
     */
    @Test
    public void testGetAllCatchesHappyPath() {
        Catch saved = catchRepository.save(createTestCatch("Caught on the first cast"));

        List<Catch> catches = catchService.getAllCatches();

        assertNotNull(catches, "getAllCatches should never return null");
        assertEquals(1, catches.size(),
                "The one saved catch should be the only catch returned");

        // checks by ID instead of position, since the list order isn't guaranteed
        assertTrue(getCatchIds(catches).contains(saved.getId()),
                "Saved catch should be in the returned list");
    }

    // ---------- getAllUserCatches ----------

    /**
     * Happy path: every catch the user logged is returned.
     */
    @Test
    public void testGetAllUserCatchesHappyPath() {
        Catch firstCatch = catchRepository.save(createTestCatch("Morning catch"));
        Catch secondCatch = catchRepository.save(createTestCatch("Evening catch"));

        List<Catch> catches = catchService.getAllUserCatches(testUser.getId());

        assertNotNull(catches, "getAllUserCatches should never return null");
        assertEquals(2, catches.size(),
                "Both of the user's catches should be returned");

        List<Long> returnedIds = getCatchIds(catches);

        assertTrue(returnedIds.contains(firstCatch.getId()),
                "User's first catch should be in the returned list");
        assertTrue(returnedIds.contains(secondCatch.getId()),
                "User's second catch should be in the returned list");
    }

    // ---------- getCatchById ----------

    /**
     * Happy path: a saved catch is found by its ID with its details intact.
     */
    @Test
    public void testGetCatchByIdHappyPath() {
        Catch saved = catchRepository.save(createTestCatch("Big one by the bank"));

        Catch result = catchService.getCatchById(saved.getId());

        assertNotNull(result, "A saved catch should be found by its ID");
        assertEquals(saved.getId(), result.getId(),
                "Returned catch should have the ID that was looked up");
        assertEquals("Big one by the bank", result.getNotes(),
                "Returned catch should have the notes it was saved with");
        assertEquals(testUser.getId(), result.getUserId(),
                "Returned catch should belong to the user who logged it");
    }

    // ---------- addCatch ----------

    /**
     * Happy path: valid input is saved with every field and the right owner.
     */
    @Test
    public void testAddCatchHappyPath() {
        CatchInput catchInput = createTestCatchInput(new BigDecimal("18.50"), "Caught below the riffle");

        boolean result = catchService.addCatch(catchInput, testUser.getId());

        assertTrue(result, "Valid catch input should be saved");

        // read straight from the repository so the check doesn't go through the service
        List<Catch> userCatches = catchRepository.findByUserId(testUser.getId());

        assertEquals(1, userCatches.size(),
                "User should have exactly one catch after adding one");

        Catch saved = userCatches.getFirst();

        assertEquals(testUser.getId(), saved.getUserId(),
                "Catch owner should come from the user ID passed in");
        assertEquals(testFly.getId(), saved.getFly().getId(),
                "Catch should be linked to the fly from the input");
        assertEquals(testFish.getId(), saved.getFish().getId(),
                "Catch should be linked to the fish from the input");
        assertEquals(testWaterBody.getId(), saved.getWaterBody().getId(),
                "Catch should be linked to the water body from the input");

        // compareTo instead of equals, since BigDecimal equals also compares the number of decimals
        assertEquals(0, new BigDecimal("18.50").compareTo(saved.getFishLength()),
                "Catch should have the fish length from the input");
        assertEquals(catchInput.dateCaught(), saved.getDateCaught(),
                "Catch should have the date caught from the input");
        assertEquals("Caught below the riffle", saved.getNotes(),
                "Catch should have the notes from the input");
    }

    // ---------- updateCatch ----------

    /**
     * Happy path: the owner updates their catch and the new details are saved.
     */
    @Test
    public void testUpdateCatchHappyPath() {
        Catch saved = catchRepository.save(createTestCatch("Old notes"));
        CatchInput catchInput = createTestCatchInput(new BigDecimal("22.25"), "New notes");

        boolean result = catchService.updateCatch(saved.getId(), catchInput, testUser.getId());

        assertTrue(result, "The owner should be able to update their catch");

        // reload from the repository so the check reads what was saved, not the object in memory
        Catch updated = catchRepository.findById(saved.getId()).orElse(null);

        assertNotNull(updated, "Catch should still exist after it is updated");
        assertEquals("New notes", updated.getNotes(),
                "Catch notes should change to the new value");
        assertEquals(0, new BigDecimal("22.25").compareTo(updated.getFishLength()),
                "Catch fish length should change to the new value");
        assertEquals(testUser.getId(), updated.getUserId(),
                "Updating a catch should not change who owns it");
    }

    // ---------- isCatchOwner ----------

    /**
     * Happy path: the user who logged a catch is its owner.
     */
    @Test
    public void testIsCatchOwnerHappyPath() {
        Catch saved = catchRepository.save(createTestCatch("Mine"));

        assertTrue(catchService.isCatchOwner(saved.getId(), testUser.getId()),
                "The user who logged the catch should be its owner");
    }

    // ---------- isFlyValid ----------

    /**
     * Happy path: a default fly can be used by any user.
     */
    @Test
    public void testIsFlyValidHappyPath() {
        assertTrue(catchService.isFlyValid(testFly.getId(), testUser.getId()),
                "A default fly should be valid for any user");
    }

    /**
     * Happy path: a user's own custom fly can be used by that user.
     */
    @Test
    public void testIsFlyValidHappyPathOwnCustomFly() {
        Fly customFly = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        assertTrue(catchService.isFlyValid(customFly.getId(), testUser.getId()),
                "A user's own custom fly should be valid for that user");
    }

    // ---------- isFishValid ----------

    /**
     * Happy path: a saved fish is valid.
     */
    @Test
    public void testIsFishValidHappyPath() {
        assertTrue(catchService.isFishValid(testFish.getId()),
                "A saved fish should be valid");
    }

    // ---------- isWaterBodyValid ----------

    /**
     * Happy path: a saved water body is valid.
     */
    @Test
    public void testIsWaterBodyValidHappyPath() {
        assertTrue(catchService.isWaterBodyValid(testWaterBody.getId()),
                "A saved water body should be valid");
    }

    // ---------- isFishInWaterBody ----------

    /**
     * Happy path: a fish linked to a water body is found in it.
     */
    @Test
    public void testIsFishInWaterBodyHappyPath() {
        assertTrue(catchService.isFishInWaterBody(testFish.getId(), testWaterBody.getId()),
                "A fish linked to the water body should be found in it");
    }

    // ---------- isFishLengthValid ----------

    /**
     * Happy path: a normal fish length with two decimals is valid.
     */
    @Test
    public void testIsFishLengthValidHappyPath() {
        assertTrue(catchService.isFishLengthValid(new BigDecimal("16.75")),
                "A normal fish length should be valid");
    }

    // ---------- isDateCaughtValid ----------

    /**
     * Happy path: a date in the past is valid.
     */
    @Test
    public void testIsDateCaughtValidHappyPath() {
        assertTrue(catchService.isDateCaughtValid(LocalDateTime.of(2026, 6, 15, 8, 30)),
                "A date in the past should be valid");
    }

    // ---------- isNotesValid ----------

    /**
     * Happy path: short notes are valid.
     */
    @Test
    public void testIsNotesValidHappyPath() {
        assertTrue(catchService.isNotesValid("Rose to a hopper on the far bank"),
                "Short notes should be valid");
    }

    // ---------- Helpers ----------

    /**
     * Builds a catch logged by the test user with the test fly, fish, and water body.
     *
     * @param notes the notes to assign to the catch
     * @return a new, unsaved catch
     */
    private Catch createTestCatch(String notes) {
        Catch testCatch = new Catch();

        testCatch.setUserId(testUser.getId());
        testCatch.setFly(testFly);
        testCatch.setFish(testFish);
        testCatch.setWaterBody(testWaterBody);
        testCatch.setFishLength(new BigDecimal("14.00"));
        testCatch.setDateCaught(LocalDateTime.of(2026, 6, 15, 8, 30));
        testCatch.setNotes(notes);

        return testCatch;
    }

    /**
     * Builds catch input that uses the test fly, fish, and water body.
     *
     * <p>The date is fixed in the past so the input is always valid no matter when the test runs.</p>
     *
     * @param fishLength the fish length in inches
     * @param notes the catch notes
     * @return new catch input
     */
    private CatchInput createTestCatchInput(BigDecimal fishLength, String notes) {
        LocalDateTime dateCaught = LocalDateTime.of(2026, 6, 15, 8, 30);

        return new CatchInput(testFly.getId(), testFish.getId(), testWaterBody.getId(),
                fishLength, dateCaught, notes);
    }

    /**
     * Builds a user with fixed test values and the given username.
     *
     * <p>The password is hashed with the app's password encoder, the same way signup stores it.</p>
     *
     * @param username the username to assign to the user
     * @return a new, unsaved user
     */
    private User createTestUser(String username) {
        User user = new User();

        user.setUsername(username);
        user.setIsAdmin(false);
        user.setHashPassword(passwordEncoder.encode("test-password"));

        return user;
    }

    /**
     * Builds a fly with fixed test values.
     *
     * @param name the name to assign to the fly
     * @param slug the slug to assign to the fly
     * @param userId the owning user's ID, or {@code null} for a default fly
     * @return a new, unsaved fly
     */
    private Fly createTestFly(String name, String slug, Long userId) {
        Fly fly = new Fly();

        fly.setName(name);
        fly.setSlug(slug);
        fly.setUserId(userId);
        fly.setType("Dry");
        fly.setColor("Olive");
        fly.setMinSize(12);
        fly.setMaxSize(18);
        fly.setImage("dry.png");

        return fly;
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
     * Builds a water body with fixed test values and the given slug.
     *
     * @param slug the slug to assign to the water body
     * @return a new, unsaved water body
     */
    private WaterBody createTestWaterBody(String slug) {
        WaterBody waterBody = new WaterBody();

        waterBody.setName("Catch Service Test River");
        waterBody.setType("River");
        waterBody.setState("Montana");
        waterBody.setDescription("A river used for catch service testing.");
        waterBody.setSlug(slug);

        return waterBody;
    }

    /**
     * Collects the IDs of a list of catches.
     *
     * @param catches the catches to collect IDs from
     * @return the catch IDs in the same order
     */
    private List<Long> getCatchIds(List<Catch> catches) {
        List<Long> ids = new ArrayList<>();

        for (Catch catchRecord : catches) {
            ids.add(catchRecord.getId());
        }

        return ids;
    }
}