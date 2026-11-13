package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.CatchRepository;
import edu.carroll.cs341.hookset.jpa.repo.FishRepository;
import edu.carroll.cs341.hookset.jpa.repo.FlyRepository;
import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import edu.carroll.cs341.hookset.service.input.FlyInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import entities.jpa.entity.Catch;
import entities.jpa.entity.Fish;
import entities.jpa.entity.Fly;
import entities.jpa.entity.User;
import entities.jpa.entity.WaterBody;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link edu.carroll.cs341.hookset.service.impl.FlyServiceImpl}.
 *
 * <p>Each service method is tested against a happy path, a crappy path, and one or more
 * crazy paths. Happy paths cover valid input with an expected result. Crappy paths cover
 * failures the service is designed to handle, such as missing flies or duplicate names.
 * Crazy paths cover nulls in every parameter and every input field, blank values, and
 * ownership edge cases, such as two users sharing a slug, one user trying to read or
 * change another user's fly, and attempts to change default Hookset flies.</p>
 *
 * <p>The service never reads the login itself, so each test passes the user's ID
 * directly. Two users are saved before each test so their IDs exist as real foreign keys.</p>
 *
 * <p>Tests run against the H2 test database and each test is rolled back by
 * {@link Transactional}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FlyServiceTest {

    /** Service under test. */
    @Autowired
    private FlyService flyService;

    /** Repository used to set up and check fly test data. */
    @Autowired
    private FlyRepository flyRepository;

    /** Repository used to set up user test data. */
    @Autowired
    private UserRepository userRepository;

    /** Repository used to save catches that use a fly. */
    @Autowired
    private CatchRepository catchRepository;

    /** Repository used to save the fish a test catch needs. */
    @Autowired
    private FishRepository fishRepository;

    /** Repository used to save the water body a test catch needs. */
    @Autowired
    private WaterBodyRepository waterBodyRepository;

    /** Password encoder used to hash test user passwords the same way signup does. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** The main user each test acts as. */
    private User testUser;

    /** A second user used to test ownership rules. */
    private User otherUser;

    /**
     * Saves two test users before each test.
     */
    @BeforeEach
    public void setUpUsers() {
        // real users so custom flies can point at real user IDs through the Fly table's foreign key
        testUser = userRepository.save(createTestUser("testuser"));
        otherUser = userRepository.save(createTestUser("otheruser"));
    }

    // ---------- getAllFlies ----------

    /**
     * Happy path: default flies and the user's custom flies are both returned.
     */
    @Test
    public void testGetAllFliesHappyPath() {
        // baseline so the count check only measures the two flies this test saves
        int before = flyService.getAllFlies(testUser.getId()).size();
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        List<Fly> flies = flyService.getAllFlies(testUser.getId());
        assertNotNull(flies, "getAllFlies should never return null");
        assertEquals(before + 2, flies.size(),
                "List size should grow by two after saving a default and a custom fly");

        // checks by name instead of position, since the list order isn't guaranteed
        List<String> returnedNames = getFlyNames(flies);
        assertTrue(returnedNames.contains("Default Fly"),
                "Default fly should be in the returned list");
        assertTrue(returnedNames.contains("Custom Fly"),
                "User's custom fly should be in the returned list");
    }

    /**
     * Crappy path: no flies are added, so the list does not grow and is never null.
     */
    @Test
    public void testGetAllFliesCrappyPath() {
        // read straight from the repository so the baseline doesn't go through the method under test
        int before = flyRepository.findByUserIdIsNullOrUserId(testUser.getId()).size();

        List<Fly> flies = flyService.getAllFlies(testUser.getId());
        assertNotNull(flies, "getAllFlies should never return null");
        assertEquals(before, flies.size(),
                "List size should not change when no flies are added");
    }

    /**
     * Crazy path: another user's custom fly is not returned.
     */
    @Test
    public void testGetAllFliesCrazyPathOtherUser() {
        // the other user's fly is the only one saved, so any leak between users shows up here
        flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

        List<Fly> flies = flyService.getAllFlies(testUser.getId());
        assertFalse(getFlyNames(flies).contains("Other Fly"),
                "Another user's custom fly should not be returned");
    }

    /**
     * Crazy path: a null user ID returns only default flies.
     */
    @Test
    public void testGetAllFliesCrazyPathNullUser() {
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // with a null user ID the query becomes userId IS NULL twice, so only default flies should come back
        List<Fly> flies = flyService.getAllFlies(null);
        assertNotNull(flies, "getAllFlies should never return null");

        List<String> returnedNames = getFlyNames(flies);
        assertTrue(returnedNames.contains("Default Fly"),
                "Default fly should be returned for a null user ID");
        assertFalse(returnedNames.contains("Custom Fly"),
                "Custom flies should not be returned for a null user ID");
    }

    /**
     * Crazy path: a user ID that does not exist returns only default flies.
     */
    @Test
    public void testGetAllFliesCrazyPathUnknownUser() {
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // far above any ID H2 will hand out during a test run
        List<Fly> flies = flyService.getAllFlies(999999L);

        List<String> returnedNames = getFlyNames(flies);
        assertTrue(returnedNames.contains("Default Fly"),
                "Default fly should be returned for an unknown user ID");
        assertFalse(returnedNames.contains("Custom Fly"),
                "Custom flies should not be returned for an unknown user ID");
    }

    // ---------- getAllHooksetFlies ----------

    /**
     * Happy path: a saved default fly is returned.
     */
    @Test
    public void testGetAllHooksetFliesHappyPath() {
        // baseline so the count check only measures the fly this test saves
        int before = flyService.getAllHooksetFlies().size();
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        List<Fly> flies = flyService.getAllHooksetFlies();
        assertNotNull(flies, "getAllHooksetFlies should never return null");
        assertEquals(before + 1, flies.size(),
                "List size should grow by one after saving a default fly");
        assertTrue(getFlyNames(flies).contains("Default Fly"),
                "Saved default fly should be in the returned list");
    }

    /**
     * Crappy path: no default flies are added, so the list does not grow and is never null.
     */
    @Test
    public void testGetAllHooksetFliesCrappyPath() {
        // read straight from the repository so the baseline doesn't go through the method under test
        int before = flyRepository.findByUserIdIsNull().size();

        List<Fly> flies = flyService.getAllHooksetFlies();
        assertNotNull(flies, "getAllHooksetFlies should never return null");
        assertEquals(before, flies.size(),
                "List size should not change when no default flies are added");
    }

    /**
     * Crazy path: custom flies from any user are not returned as Hookset flies.
     */
    @Test
    public void testGetAllHooksetFliesCrazyPathCustomFliesExcluded() {
        int before = flyService.getAllHooksetFlies().size();

        // one fly from each user, so the test catches a filter on the wrong user as well as a missing filter
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));
        flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

        List<Fly> flies = flyService.getAllHooksetFlies();
        assertEquals(before, flies.size(),
                "Saving custom flies should not change the Hookset fly count");

        List<String> returnedNames = getFlyNames(flies);
        assertFalse(returnedNames.contains("Custom Fly"),
                "Test user's custom fly should not be a Hookset fly");
        assertFalse(returnedNames.contains("Other Fly"),
                "Other user's custom fly should not be a Hookset fly");
    }

    // ---------- getAllUserFlies ----------

    /**
     * Happy path: the user's custom fly is returned.
     */
    @Test
    public void testGetAllUserFliesHappyPath() {
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        List<Fly> flies = flyService.getAllUserFlies(testUser.getId());
        // an exact count works here, since the test user is created fresh for every test and only has this one fly
        assertNotNull(flies, "getAllUserFlies should never return null");
        assertEquals(1, flies.size(),
                "User should have exactly one custom fly");
        assertEquals("Custom Fly", flies.getFirst().getName(),
                "Returned fly should be the user's custom fly");
    }

    /**
     * Crappy path: a user with no custom flies gets an empty list.
     */
    @Test
    public void testGetAllUserFliesCrappyPath() {
        // the test user is created fresh for every test, so they have no custom flies yet
        List<Fly> flies = flyService.getAllUserFlies(testUser.getId());
        assertNotNull(flies, "getAllUserFlies should never return null");
        assertTrue(flies.isEmpty(),
                "A user with no custom flies should get an empty list");
    }

    /**
     * Crazy path: default flies and another user's flies are not returned as the user's flies.
     */
    @Test
    public void testGetAllUserFliesCrazyPathOtherFliesExcluded() {
        // one default fly and one other user's fly, the two kinds that should never count as this user's
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));
        flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

        List<Fly> flies = flyService.getAllUserFlies(testUser.getId());
        assertNotNull(flies, "getAllUserFlies should never return null");
        assertTrue(flies.isEmpty(),
                "Default flies and other users' flies should not be returned as the user's flies");
    }

    /**
     * Crazy path: a null user ID returns an empty list, not the default flies.
     */
    @Test
    public void testGetAllUserFliesCrazyPathNullUser() {
        // a default fly exists so an IS NULL query would return it and fail
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        List<Fly> flies = flyService.getAllUserFlies(null);
        assertNotNull(flies, "getAllUserFlies should never return null");
        assertTrue(flies.isEmpty(),
                "A null user ID should return an empty list, not the default flies");
    }

    /**
     * Crazy path: a user ID that does not exist returns an empty list.
     */
    @Test
    public void testGetAllUserFliesCrazyPathUnknownUser() {
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // far above any ID H2 will hand out during a test run
        List<Fly> flies = flyService.getAllUserFlies(999999L);
        assertNotNull(flies, "getAllUserFlies should never return null");
        assertTrue(flies.isEmpty(),
                "A user ID that does not exist should return an empty list");
    }

    // ---------- getFlyFromSlug ----------

    /**
     * Happy path: the user's custom fly is found by its slug.
     */
    @Test
    public void testGetFlyFromSlugHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        Fly result = flyService.getFlyFromSlug("custom-fly", testUser.getId());
        // check every field so a lookup that returns the wrong row can't slip through
        assertNotNull(result, "Fly should be found for an existing slug");
        assertEquals(saved.getId(), result.getId(),
                "Returned fly ID should match the saved fly");
        assertEquals("Custom Fly", result.getName(),
                "Returned fly name should match the saved name");
        assertEquals("custom-fly", result.getSlug(),
                "Returned fly slug should match the requested slug");
        assertEquals("Dry", result.getType(),
                "Returned fly type should match the saved type");
        assertEquals("Olive", result.getColor(),
                "Returned fly color should match the saved color");
        assertEquals(12, result.getMinSize(),
                "Returned fly min size should match the saved min size");
        assertEquals(18, result.getMaxSize(),
                "Returned fly max size should match the saved max size");
        assertEquals(testUser.getId(), result.getUserId(),
                "Returned fly should belong to the user");
    }

    /**
     * Happy path: a default fly is found by its slug.
     */
    @Test
    public void testGetFlyFromSlugHappyPathDefaultFly() {
        Fly saved = flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        // default flies are checked first, so this is found before custom flies are even looked at
        Fly result = flyService.getFlyFromSlug("default-fly", testUser.getId());
        assertNotNull(result, "Default fly should be found by its slug");
        assertEquals(saved.getId(), result.getId(),
                "Returned fly ID should match the saved default fly");
    }

    /**
     * Crappy path: a slug that does not exist returns null even when other flies exist.
     */
    @Test
    public void testGetFlyFromSlugCrappyPath() {
        // a real fly exists so a lookup that ignores the slug would return it and fail
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        Fly result = flyService.getFlyFromSlug("does-not-exist", testUser.getId());
        assertNull(result, "No fly should be returned for a slug that does not exist");
    }

    /**
     * Crazy path: a null slug returns null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNullSlug() {
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // no fly has a null slug, so both lookups in the service come back empty
        Fly result = flyService.getFlyFromSlug(null, testUser.getId());
        assertNull(result, "No fly should be returned for a null slug");
    }

    /**
     * Crazy path: an empty slug returns null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathEmptySlug() {
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // an empty string isn't null, so this checks it doesn't match anything either
        Fly result = flyService.getFlyFromSlug("", testUser.getId());
        assertNull(result, "No fly should be returned for an empty slug");
    }

    /**
     * Crazy path: a null user ID still finds a default fly but not a custom fly.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNullUser() {
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // a null user ID must not turn into an IS NULL query that matches default flies
        Fly defaultResult = flyService.getFlyFromSlug("default-fly", null);
        assertNotNull(defaultResult, "A default fly should be found for a null user ID");

        Fly customResult = flyService.getFlyFromSlug("custom-fly", null);
        assertNull(customResult, "A custom fly should not be found for a null user ID");
    }

    /**
     * Crazy path: a null slug and a null user ID together return null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNullSlugAndUser() {
        // a default fly exists so a lookup that matched on nulls would return it and fail
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        Fly result = flyService.getFlyFromSlug(null, null);
        assertNull(result, "No fly should be returned when both the slug and user ID are null");
    }

    /**
     * Crazy path: a slug that only belongs to another user's custom fly returns null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathOtherUser() {
        flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

        // the service only searches default flies and this user's flies, never other users'
        Fly result = flyService.getFlyFromSlug("other-fly", testUser.getId());
        assertNull(result, "Another user's custom fly should not be found by slug");
    }

    /**
     * Crazy path: two users have a custom fly with the same slug, and each user gets their own.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathSharedSlug() {
        // saved straight through the repository, since addFly would never create a shared slug for one user
        Fly testUserFly = flyRepository.save(
                createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));
        Fly otherUserFly = flyRepository.save(
                createTestFly("Woolly Bugger", "woolly-bugger", otherUser.getId()));

        Fly testUserResult = flyService.getFlyFromSlug("woolly-bugger", testUser.getId());
        assertNotNull(testUserResult, "Test user should find their own fly by slug");
        assertEquals(testUserFly.getId(), testUserResult.getId(),
                "Test user should get their own fly, not the other user's");

        Fly otherUserResult = flyService.getFlyFromSlug("woolly-bugger", otherUser.getId());
        assertNotNull(otherUserResult, "Other user should find their own fly by slug");
        assertEquals(otherUserFly.getId(), otherUserResult.getId(),
                "Other user should get their own fly, not the test user's");
    }

    /**
     * Crazy path: when a default fly and a custom fly share a slug, the default fly is returned.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathDefaultAndCustomShareSlug() {
        Fly defaultFly = flyRepository.save(createTestFly("Parachute Adams", "parachute-adams", null));
        flyRepository.save(createTestFly("Parachute Adams", "parachute-adams", testUser.getId()));

        Fly result = flyService.getFlyFromSlug("parachute-adams", testUser.getId());
        // the service checks default flies first, so this pins down that order
        assertNotNull(result, "A fly should be found when a default and custom fly share a slug");
        assertEquals(defaultFly.getId(), result.getId(),
                "The default fly should be returned before the user's custom fly");
    }

    // ---------- getFlyById ----------

    /**
     * Happy path: the user's custom fly is found by its ID.
     */
    @Test
    public void testGetFlyByIdHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // the user owns the fly, so the ownership check passes
        Fly result = flyService.getFlyById(saved.getId(), testUser.getId());
        assertNotNull(result, "Fly should be found for an existing ID");
        assertEquals(saved.getId(), result.getId(),
                "Returned fly ID should match the saved fly");
        assertEquals("Custom Fly", result.getName(),
                "Returned fly name should match the saved fly");
    }

    /**
     * Happy path: a default fly is found by its ID.
     */
    @Test
    public void testGetFlyByIdHappyPathDefaultFly() {
        Fly saved = flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        // default flies have no owner, so any user can load them
        Fly result = flyService.getFlyById(saved.getId(), testUser.getId());
        assertNotNull(result, "Default fly should be found by its ID");
        assertEquals(saved.getId(), result.getId(),
                "Returned fly ID should match the saved default fly");
    }

    /**
     * Crappy path: an ID that does not exist returns null.
     */
    @Test
    public void testGetFlyByIdCrappyPath() {
        // far above any ID H2 will hand out during a test run
        Fly result = flyService.getFlyById(999999L, testUser.getId());
        assertNull(result, "No fly should be returned for an ID that does not exist");
    }

    /**
     * Crazy path: a null fly ID returns null.
     */
    @Test
    public void testGetFlyByIdCrazyPathNullId() {
        // findById would throw on a null ID, so this proves the service checks for null first
        Fly result = flyService.getFlyById(null, testUser.getId());
        assertNull(result, "No fly should be returned for a null ID");
    }

    /**
     * Crazy path: another user's custom fly cannot be loaded by ID.
     */
    @Test
    public void testGetFlyByIdCrazyPathOtherUser() {
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

        // throws instead of returning null, so a denied fly can't be mistaken for a missing one
        assertThrows(AccessDeniedException.class,
                () -> flyService.getFlyById(otherFly.getId(), testUser.getId()),
                "Loading another user's fly by ID should be denied");
    }

    /**
     * Crazy path: a null user ID cannot load a custom fly.
     */
    @Test
    public void testGetFlyByIdCrazyPathNullUserCustomFly() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // a null user ID can't equal the fly's owner, so this is denied like any other user
        assertThrows(AccessDeniedException.class,
                () -> flyService.getFlyById(saved.getId(), null),
                "A null user ID should not be able to load a custom fly");
    }

    /**
     * Crazy path: a null user ID can still load a default fly.
     */
    @Test
    public void testGetFlyByIdCrazyPathNullUserDefaultFly() {
        Fly saved = flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        // default flies skip the ownership check, so a null user ID still sees them
        Fly result = flyService.getFlyById(saved.getId(), null);
        assertNotNull(result, "A default fly should be visible even with a null user ID");
        assertEquals(saved.getId(), result.getId(),
                "Returned fly ID should match the saved default fly");
    }

    /**
     * Crazy path: a null fly ID and a null user ID together return null.
     */
    @Test
    public void testGetFlyByIdCrazyPathNullIdAndUser() {
        // the null fly ID check runs first, so the null user ID is never looked at
        Fly result = flyService.getFlyById(null, null);
        assertNull(result, "No fly should be returned when both the fly ID and user ID are null");
    }

    // ---------- addFly ----------

    /**
     * Happy path: a dry fly is saved for the user with a generated slug and the dry fly image.
     */
    @Test
    public void testAddFlyHappyPath() {
        // "Dry" is the form's option value, not the "Dry Fly" display text
        FlyInput flyInput = createTestFlyInput("Parachute Adams Test", "Dry");

        boolean result = flyService.addFly(flyInput, testUser.getId());
        assertTrue(result, "addFly should return true for a valid fly");

        // read back through the service, since the test user is fresh and this is their only fly
        List<Fly> userFlies = flyService.getAllUserFlies(testUser.getId());
        assertEquals(1, userFlies.size(),
                "User should have one custom fly after adding one");

        Fly saved = userFlies.getFirst();
        assertEquals(testUser.getId(), saved.getUserId(),
                "Added fly should belong to the user passed in");
        assertEquals("Parachute Adams Test", saved.getName(),
                "Added fly name should match the input");
        assertEquals("parachute-adams-test", saved.getSlug(),
                "Added fly slug should be generated from the name");
        assertEquals("Dry", saved.getType(),
                "Added fly type should match the input");
        assertEquals("dry.png", saved.getImage(),
                "A dry fly should use the dry fly image");
        assertEquals("Olive", saved.getColor(),
                "Added fly color should match the input");
        assertEquals(12, saved.getMinSize(),
                "Added fly min size should match the input");
        assertEquals(18, saved.getMaxSize(),
                "Added fly max size should match the input");
    }

    /**
     * Happy path: a streamer is saved with the streamer image.
     */
    @Test
    public void testAddFlyHappyPathStreamer() {
        // only the image is checked here, the happy path above already covers the other fields
        flyService.addFly(createTestFlyInput("Test Streamer", "Streamer"), testUser.getId());

        Fly saved = flyService.getAllUserFlies(testUser.getId()).getFirst();
        assertEquals("streamer.png", saved.getImage(),
                "A streamer should use the streamer image");
    }

    /**
     * Happy path: a nymph is saved with the nymph image.
     */
    @Test
    public void testAddFlyHappyPathNymph() {
        // nymph is the image getFlyImage falls back to, so this checks that fallback
        flyService.addFly(createTestFlyInput("Test Nymph", "Nymph"), testUser.getId());

        Fly saved = flyService.getAllUserFlies(testUser.getId()).getFirst();
        assertEquals("nymph.png", saved.getImage(),
                "A nymph should use the nymph image");
    }

    /**
     * Crappy path: adding a second fly with the same name for the same user is rejected.
     */
    @Test
    public void testAddFlyCrappyPathDuplicateName() {
        // the first add has to succeed, so the second add is the only thing being tested
        boolean firstResult = flyService.addFly(createTestFlyInput("Test Bugger", "Streamer"), testUser.getId());
        assertTrue(firstResult, "The first fly with a new name should be saved");

        // baseline taken after the first add, so the count only measures the second one
        int before = flyRepository.findAll().size();

        boolean secondResult = flyService.addFly(createTestFlyInput("Test Bugger", "Streamer"), testUser.getId());
        assertFalse(secondResult, "A second fly with the same name for the same user should be rejected");

        int after = flyRepository.findAll().size();
        assertEquals(before, after, "No fly should be saved for a duplicate name");
    }

    /**
     * Crappy path: a name that matches a default fly is rejected.
     */
    @Test
    public void testAddFlyCrappyPathDefaultFlyName() {
        // default flies are visible to everyone, so their names are taken for everyone
        flyRepository.save(createTestFly("Test Default", "test-default", null));

        int before = flyRepository.findAll().size();

        boolean result = flyService.addFly(createTestFlyInput("Test Default", "Dry"), testUser.getId());
        assertFalse(result, "A fly with a default fly's name should be rejected");

        int after = flyRepository.findAll().size();
        assertEquals(before, after, "No fly should be saved when the name matches a default fly");
    }

    /**
     * Crazy path: a name another user already used is allowed and saved for this user.
     */
    @Test
    public void testAddFlyCrazyPathOtherUserSameName() {
        // custom flies are private, so another user's names shouldn't block this user
        flyRepository.save(createTestFly("Shared Name", "shared-name", otherUser.getId()));

        boolean result = flyService.addFly(createTestFlyInput("Shared Name", "Dry"), testUser.getId());
        assertTrue(result, "A name used only by another user should be allowed");

        List<Fly> userFlies = flyService.getAllUserFlies(testUser.getId());
        assertEquals(1, userFlies.size(), "Fly should be saved for the test user");
        assertEquals(testUser.getId(), userFlies.getFirst().getUserId(),
                "Saved fly should belong to the test user, not the other user");
    }

    /**
     * Crazy path: a null input is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathNullInput() {
        int before = flyRepository.findAll().size();

        // the null check on the input runs before anything else, so nothing is ever built
        boolean result = flyService.addFly(null, testUser.getId());
        assertFalse(result, "A null fly input should not be saved");

        int after = flyRepository.findAll().size();
        assertEquals(before, after, "No fly should be saved for a null input");
    }

    /**
     * Crazy path: a null user ID is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathNullUser() {
        int before = flyRepository.findAll().size();

        // a valid fly so the only thing wrong is the missing user
        boolean result = flyService.addFly(createTestFlyInput("Test Fly", "Dry"), null);
        assertFalse(result, "A fly with no user should not be saved");

        int after = flyRepository.findAll().size();
        assertEquals(before, after, "No fly should be saved for a null user ID");
    }

    /**
     * Crazy path: a null name is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathNullName() {
        // assertAddFlyRejected checks both the false result and that the fly count didn't change
        assertAddFlyRejected(createTestFlyInput(null, "Dry", "Olive", 12, 18),
                "A fly with a null name should not be saved");
    }

    /**
     * Crazy path: a blank name is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathBlankName() {
        // only spaces, which isn't null but is still blank
        assertAddFlyRejected(createTestFlyInput("   ", "Dry", "Olive", 12, 18),
                "A fly with a blank name should not be saved");
    }

    /**
     * Crazy path: a null type is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathNullType() {
        assertAddFlyRejected(createTestFlyInput("Test Fly", null, "Olive", 12, 18),
                "A fly with a null type should not be saved");
    }

    /**
     * Crazy path: a blank type is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathBlankType() {
        // gets past a null check but fails the blank check
        assertAddFlyRejected(createTestFlyInput("Test Fly", "   ", "Olive", 12, 18),
                "A fly with a blank type should not be saved");
    }

    /**
     * Crazy path: a null color is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathNullColor() {
        // the service catches it before the database's NOT NULL constraint ever runs
        assertAddFlyRejected(createTestFlyInput("Test Fly", "Dry", null, 12, 18),
                "A fly with a null color should not be saved");
    }

    /**
     * Crazy path: a blank color is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathBlankColor() {
        assertAddFlyRejected(createTestFlyInput("Test Fly", "Dry", "   ", 12, 18),
                "A fly with a blank color should not be saved");
    }

    /**
     * Crazy path: a null min size is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathNullMinSize() {
        // sizes are Integer, not int, so a missing size can actually be null
        assertAddFlyRejected(createTestFlyInput("Test Fly", "Dry", "Olive", null, 18),
                "A fly with a null min size should not be saved");
    }

    /**
     * Crazy path: a null max size is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathNullMaxSize() {
        assertAddFlyRejected(createTestFlyInput("Test Fly", "Dry", "Olive", 12, null),
                "A fly with a null max size should not be saved");
    }

    /**
     * Crazy path: every field null is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathAllFieldsNull() {
        // the name check runs first and fails, so this mostly proves nothing crashes on all nulls
        assertAddFlyRejected(createTestFlyInput(null, null, null, null, null),
                "A fly with every field null should not be saved");
    }

    /**
     * Crazy path: a min size larger than the max size is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathMinGreaterThanMax() {
        // both sizes are valid on their own, only the range between them is wrong
        assertAddFlyRejected(createTestFlyInput("Test Fly", "Dry", "Olive", 18, 12),
                "A fly with a min size larger than its max size should not be saved");
    }

    /**
     * Crazy path: a fly type that isn't one of the allowed types is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathUnknownType() {
        // a type the form can't send, so this only reaches the service through a direct call
        assertAddFlyRejected(createTestFlyInput("Soft Hackle", "Wet Fly"),
                "A fly type that isn't Dry, Nymph, or Streamer should not be saved");
    }

    /**
     * Crazy path: extra whitespace in the name is collapsed when the slug is generated.
     */
    @Test
    public void testAddFlyCrazyPathWhitespaceName() {
        // leading, trailing, and repeated inner spaces all in one name
        flyService.addFly(createTestFlyInput("  Parachute    Adams Test  ", "Dry"), testUser.getId());

        Fly saved = flyService.getAllUserFlies(testUser.getId()).getFirst();
        assertEquals("parachute-adams-test", saved.getSlug(),
                "Slug should trim the name and collapse inner whitespace to one hyphen");
    }

    /**
     * Crazy path: addFly rejects a name over 150 characters.
     */
    @Test
    public void testAddFlyCrazyPathNameTooLong() {
        // one over the VARCHAR(150) name column
        assertAddFlyRejected(createTestFlyInput("a".repeat(151), "Dry", "Olive", 12, 18),
                "A fly name over 150 characters should not be saved");
    }

    /**
     * Crazy path: addFly rejects a color that is only a number.
     */
    @Test
    public void testAddFlyCrazyPathNumberColor() {
        assertAddFlyRejected(createTestFlyInput("Test Fly", "Dry", "123", 12, 18),
                "A fly color that is only a number should not be saved");
    }

    /**
     * Crazy path: addFly rejects a color that is too short.
     */
    @Test
    public void testAddFlyCrazyPathColorTooShort() {
        // one under the 3 character minimum
        assertAddFlyRejected(createTestFlyInput("Test Fly", "Dry", "Re", 12, 18),
                "A fly color under 3 characters should not be saved");
    }

    /**
     * Crazy path: addFly rejects a size outside 1-32.
     */
    @Test
    public void testAddFlyCrazyPathSizeOutOfRange() {
        // both sizes are out of range, 0 is under the minimum and 40 is over the maximum
        assertAddFlyRejected(createTestFlyInput("Test Fly", "Dry", "Olive", 0, 40),
                "A fly with sizes outside 1-32 should not be saved");
    }

    // ---------- updateFly ----------

    /**
     * Happy path: the user's fly is updated and the slug and image are regenerated.
     */
    @Test
    public void testUpdateFlyHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Old Name", "old-name", testUser.getId()));

        // every field changes from the saved values so a skipped setter would show up
        FlyInput flyInput = createTestFlyInput("New Name", "Streamer", "Black", 4, 8);

        boolean result = flyService.updateFly(saved.getId(), flyInput, testUser.getId());
        assertTrue(result, "updateFly should return true for a successful update");

        // looked up again by ID, which inside one test transaction is the same fly the service changed
        Fly updated = flyRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated, "Updated fly should still exist");
        assertEquals("New Name", updated.getName(), "Fly name should be updated");
        assertEquals("new-name", updated.getSlug(), "Fly slug should be regenerated from the new name");
        assertEquals("Streamer", updated.getType(), "Fly type should be updated");
        assertEquals("streamer.png", updated.getImage(), "Fly image should be updated to match the new type");
        assertEquals("Black", updated.getColor(), "Fly color should be updated");
        assertEquals(4, updated.getMinSize(), "Fly min size should be updated");
        assertEquals(8, updated.getMaxSize(), "Fly max size should be updated");
        assertEquals(testUser.getId(), updated.getUserId(), "Fly should still belong to the same user");
    }

    /**
     * Crappy path: updating a fly ID that does not exist returns false.
     */
    @Test
    public void testUpdateFlyCrappyPathMissingFly() {
        // far above any ID H2 will hand out during a test run
        boolean result = flyService.updateFly(999999L, createTestFlyInput("New Name", "Dry"), testUser.getId());
        assertFalse(result, "Updating a fly ID that does not exist should return false");
    }

    /**
     * Crappy path: renaming a fly to the name of another of the user's flies is rejected.
     */
    @Test
    public void testUpdateFlyCrappyPathDuplicateName() {
        Fly saved = flyRepository.save(createTestFly("First Fly", "first-fly", testUser.getId()));
        flyRepository.save(createTestFly("Second Fly", "second-fly", testUser.getId()));

        // the second fly's name is already used by the same user, so renaming the first fly to it is a duplicate
        boolean result = flyService.updateFly(saved.getId(), createTestFlyInput("Second Fly", "Dry"), testUser.getId());
        assertFalse(result, "Renaming to another of the user's fly names should be rejected");
        assertFlyNameUnchanged(saved.getId(), "First Fly");
    }

    /**
     * Crazy path: keeping the same name while changing other fields is allowed.
     */
    @Test
    public void testUpdateFlyCrazyPathKeepOwnName() {
        // without the keep-your-own-name rule, this would fail as a duplicate of itself
        Fly saved = flyRepository.save(createTestFly("Same Name", "same-name", testUser.getId()));

        boolean result = flyService.updateFly(saved.getId(),
                createTestFlyInput("Same Name", "Dry", "Black", 12, 18), testUser.getId());
        assertTrue(result, "Keeping the same name should be allowed");

        Fly updated = flyRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated, "Updated fly should still exist");
        assertEquals("Black", updated.getColor(), "Other fields should still be updated");
    }

    /**
     * Crazy path: another user's fly cannot be updated and is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathOtherUser() {
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

        // ownership is checked before the input, so this throws even though the input itself is valid
        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(otherFly.getId(), createTestFlyInput("Hijacked Name", "Dry"), testUser.getId()),
                "Updating another user's fly should be denied");
        // the exception alone doesn't prove nothing changed, so check the saved row too
        assertFlyNameUnchanged(otherFly.getId(), "Other Fly");
    }

    /**
     * Crazy path: a default fly cannot be updated and is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathDefaultFly() {
        Fly defaultFly = flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        // default flies have no owner, so nobody can edit them
        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(defaultFly.getId(), createTestFlyInput("Hijacked Name", "Dry"), testUser.getId()),
                "Updating a default fly should be denied");
        assertFlyNameUnchanged(defaultFly.getId(), "Default Fly");
    }

    /**
     * Crazy path: a null fly ID returns false.
     */
    @Test
    public void testUpdateFlyCrazyPathNullFlyId() {
        // findById would throw on a null ID, so this proves the service checks for null first
        boolean result = flyService.updateFly(null, createTestFlyInput("New Name", "Dry"), testUser.getId());
        assertFalse(result, "Updating a null fly ID should return false");
    }

    /**
     * Crazy path: a null input is rejected and the fly is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathNullInput() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // the user owns this fly, so the null input is the only reason for the failure
        boolean result = flyService.updateFly(saved.getId(), null, testUser.getId());
        assertFalse(result, "A null fly input should not update the fly");
        assertFlyNameUnchanged(saved.getId(), "Custom Fly");
    }

    /**
     * Crazy path: a null user ID cannot update a custom fly.
     */
    @Test
    public void testUpdateFlyCrazyPathNullUser() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // a null user ID can't equal the fly's owner, so this is denied like any other user
        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(saved.getId(), createTestFlyInput("Hijacked Name", "Dry"), null),
                "A null user ID should not be able to update a fly");
        assertFlyNameUnchanged(saved.getId(), "Custom Fly");
    }

    /**
     * Crazy path: a null user ID cannot update a default fly either.
     */
    @Test
    public void testUpdateFlyCrazyPathNullUserDefaultFly() {
        // a null user ID and a default fly's null owner must not count as a match
        Fly defaultFly = flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(defaultFly.getId(), createTestFlyInput("Hijacked Name", "Dry"), null),
                "A null user ID should not be able to update a default fly");
        assertFlyNameUnchanged(defaultFly.getId(), "Default Fly");
    }

    /**
     * Crazy path: a null name is rejected and the fly is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathNullName() {
        // assertUpdateFlyRejected checks both the false result and that the saved name didn't change
        assertUpdateFlyRejected(createTestFlyInput(null, "Dry", "Olive", 12, 18),
                "A null name should not update the fly");
    }

    /**
     * Crazy path: a null type is rejected and the fly is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathNullType() {
        assertUpdateFlyRejected(createTestFlyInput("New Name", null, "Olive", 12, 18),
                "A null type should not update the fly");
    }

    /**
     * Crazy path: a null color is rejected and the fly is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathNullColor() {
        assertUpdateFlyRejected(createTestFlyInput("New Name", "Dry", null, 12, 18),
                "A null color should not update the fly");
    }

    /**
     * Crazy path: a null min size is rejected and the fly is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathNullMinSize() {
        assertUpdateFlyRejected(createTestFlyInput("New Name", "Dry", "Olive", null, 18),
                "A null min size should not update the fly");
    }

    /**
     * Crazy path: a null max size is rejected and the fly is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathNullMaxSize() {
        assertUpdateFlyRejected(createTestFlyInput("New Name", "Dry", "Olive", 12, null),
                "A null max size should not update the fly");
    }

    /**
     * Crazy path: a min size larger than the max size is rejected and the fly is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathMinGreaterThanMax() {
        // both sizes are valid on their own, only the range between them is wrong
        assertUpdateFlyRejected(createTestFlyInput("New Name", "Dry", "Olive", 18, 12),
                "A min size larger than the max size should not update the fly");
    }

    /**
     * Crazy path: updateFly rejects a type that isn't on the list.
     */
    @Test
    public void testUpdateFlyCrazyPathUnknownType() {
        // a real fly type, just not one Hookset supports
        assertUpdateFlyRejected(createTestFlyInput("New Name", "Wet Fly", "Olive", 12, 18),
                "Updating to a type that isn't on the list should be rejected");
    }

    /**
     * Crazy path: updateFly rejects a color that is only a number.
     */
    @Test
    public void testUpdateFlyCrazyPathNumberColor() {
        assertUpdateFlyRejected(createTestFlyInput("New Name", "Dry", "123", 12, 18),
                "Updating to a color that is only a number should be rejected");
    }

    /**
     * Crazy path: updateFly rejects a size outside 1-32.
     */
    @Test
    public void testUpdateFlyCrazyPathSizeOutOfRange() {
        // one over the largest hook size
        assertUpdateFlyRejected(createTestFlyInput("New Name", "Dry", "Olive", 12, 33),
                "Updating to a size over 32 should be rejected");
    }

    // ---------- deleteFly ----------

    /**
     * Happy path: deleting an unused custom fly returns true and removes it.
     */
    @Test
    public void testDeleteFlyHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // no catches use this fly, so nothing blocks the delete
        boolean result = flyService.deleteFly(saved.getId(), testUser.getId());
        assertTrue(result, "Deleting an unused custom fly should return true");
        assertNull(flyRepository.findById(saved.getId()).orElse(null),
                "The fly should be gone after it is deleted");
    }

    /**
     * Crappy path: deleting a fly ID that does not exist returns false instead of throwing.
     */
    @Test
    public void testDeleteFlyCrappyPath() {
        // far above any ID H2 will hand out during a test run
        // a missing fly returns false instead of throwing, since there's nothing to protect
        boolean result = flyService.deleteFly(999999L, testUser.getId());
        assertFalse(result, "Deleting a fly ID that does not exist should return false");
    }

    /**
     * Crappy path: a fly used by a catch is not deleted.
     */
    @Test
    public void testDeleteFlyCrappyPathUsedByCatch() {
        Fly saved = flyRepository.save(createTestFly("Used Fly", "used-fly", testUser.getId()));
        catchRepository.save(createTestCatch(saved));

        // the user owns the fly, so the catch is the only thing blocking the delete
        boolean result = flyService.deleteFly(saved.getId(), testUser.getId());
        assertFalse(result, "A fly used by a catch should not be deleted");
        assertFlyNameUnchanged(saved.getId(), "Used Fly");
    }

    /**
     * Crazy path: another user's fly cannot be deleted and still exists afterward.
     */
    @Test
    public void testDeleteFlyCrazyPathOtherUser() {
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

        // the denied delete has to leave the fly in place, not just throw
        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(otherFly.getId(), testUser.getId()),
                "Deleting another user's fly should be denied");
        assertTrue(flyRepository.findById(otherFly.getId()).isPresent(),
                "Another user's fly should still exist after a denied delete");
    }

    /**
     * Crazy path: a default fly cannot be deleted and still exists afterward.
     */
    @Test
    public void testDeleteFlyCrazyPathDefaultFly() {
        Fly defaultFly = flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        // default flies have no owner, so nobody can delete them
        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(defaultFly.getId(), testUser.getId()),
                "Deleting a default fly should be denied");
        assertTrue(flyRepository.findById(defaultFly.getId()).isPresent(),
                "Default fly should still exist after a denied delete");
    }

    /**
     * Crazy path: a null fly ID returns false and deletes nothing.
     */
    @Test
    public void testDeleteFlyCrazyPathNullId() {
        // a real fly exists so a null that matched something would delete it
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // findById would throw on a null ID, so this proves the service checks for null first
        boolean result = flyService.deleteFly(null, testUser.getId());
        assertFalse(result, "Deleting with a null fly ID should return false");
        assertTrue(flyRepository.findById(saved.getId()).isPresent(),
                "No fly should be deleted for a null ID");
    }

    /**
     * Crazy path: a null user ID cannot delete a custom fly.
     */
    @Test
    public void testDeleteFlyCrazyPathNullUser() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // a null user ID can't equal the fly's owner, so this is denied like any other user
        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(saved.getId(), null),
                "A null user ID should not be able to delete a fly");
        assertTrue(flyRepository.findById(saved.getId()).isPresent(),
                "Fly should still exist after a denied delete");
    }

    /**
     * Crazy path: a null user ID cannot delete a default fly either.
     */
    @Test
    public void testDeleteFlyCrazyPathNullUserDefaultFly() {
        // a null user ID and a default fly's null owner must not count as a match
        Fly defaultFly = flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(defaultFly.getId(), null),
                "A null user ID should not be able to delete a default fly");
        assertTrue(flyRepository.findById(defaultFly.getId()).isPresent(),
                "Default fly should still exist after a denied delete");
    }

    /**
     * Crazy path: a null fly ID and a null user ID together do nothing and do not throw.
     */
    @Test
    public void testDeleteFlyCrazyPathNullIdAndUser() {
        // the null fly ID check runs first, so the null user ID is never looked at
        assertDoesNotThrow(() -> flyService.deleteFly(null, null),
                "Deleting with both IDs null should not throw");
    }

    /**
     * Crazy path: another user's fly that is used by a catch is still denied, not just rejected.
     */
    @Test
    public void testDeleteFlyCrazyPathOtherUserUsedFly() {
        // ownership is checked before the catch check, so this throws instead of returning false
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));
        catchRepository.save(createTestCatch(otherFly));

        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(otherFly.getId(), testUser.getId()),
                "Deleting another user's fly should be denied even when it is used by a catch");
        assertFlyNameUnchanged(otherFly.getId(), "Other Fly");
    }

    // ---------- isSizeRangeValid ----------

    /**
     * Happy path: a min size smaller than the max size is valid.
     */
    @Test
    public void testIsSizeRangeValidHappyPath() {
        assertTrue(flyService.isSizeRangeValid(12, 18),
                "A min size smaller than the max size should be valid");
    }

    /**
     * Crappy path: a min size larger than the max size is invalid.
     */
    @Test
    public void testIsSizeRangeValidCrappyPath() {
        // both sizes are valid hook sizes, only the order is wrong
        assertFalse(flyService.isSizeRangeValid(18, 12),
                "A min size larger than the max size should be invalid");
    }

    /**
     * Crazy path: a min size equal to the max size is valid.
     */
    @Test
    public void testIsSizeRangeValidCrazyPathEqualSizes() {
        // boundary case, catches a > that should have been >= or the other way around
        assertTrue(flyService.isSizeRangeValid(14, 14),
                "A min size equal to the max size should be valid");
    }

    /**
     * Crazy path: a null min size passes so the missing field is reported elsewhere.
     */
    @Test
    public void testIsSizeRangeValidCrazyPathNullMin() {
        // null is skipped on purpose so the user only gets the missing size message
        assertTrue(flyService.isSizeRangeValid(null, 12),
                "A null min size should pass so the missing field check reports it");
    }

    /**
     * Crazy path: a null max size passes so the missing field is reported elsewhere.
     */
    @Test
    public void testIsSizeRangeValidCrazyPathNullMax() {
        assertTrue(flyService.isSizeRangeValid(12, null),
                "A null max size should pass so the missing field check reports it");
    }

    /**
     * Crazy path: both sizes null passes so the missing fields are reported elsewhere.
     */
    @Test
    public void testIsSizeRangeValidCrazyPathBothNull() {
        assertTrue(flyService.isSizeRangeValid(null, null),
                "Both sizes null should pass so the missing field check reports them");
    }

    // ---------- isFlyNameUnique ----------

    /**
     * Happy path: a name the user has not used is unique.
     */
    @Test
    public void testIsFlyNameUniqueHappyPath() {
        // nothing is saved first, so no visible fly could have this name
        assertTrue(flyService.isFlyNameUnique("Brand New Fly", testUser.getId()),
                "A name the user has not used should be unique");
    }

    /**
     * Crappy path: a name the user already used is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrappyPath() {
        // saved directly with the slug generateFlySlug would make for this name
        flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));

        assertFalse(flyService.isFlyNameUnique("Woolly Bugger", testUser.getId()),
                "A name the user already used should not be unique");
    }

    /**
     * Crazy path: a duplicate name with different case and extra whitespace is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathCaseAndWhitespace() {
        flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));

        // both names turn into the slug woolly-bugger, which is how names are compared
        assertFalse(flyService.isFlyNameUnique("  WOOLLY   BUGGER  ", testUser.getId()),
                "A duplicate name should not be unique regardless of case or whitespace");
    }

    /**
     * Crazy path: a blank name is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathBlank() {
        // strict on bad input, blank is never treated as a usable name
        assertFalse(flyService.isFlyNameUnique("   ", testUser.getId()),
                "A blank name should not be unique");
    }

    /**
     * Crazy path: a null name is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathNullName() {
        // strict on bad input, same as the blank case
        assertFalse(flyService.isFlyNameUnique(null, testUser.getId()),
                "A null name should not be unique");
    }

    /**
     * Crazy path: a name another user already used is still unique for this user.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathOtherUserName() {
        // custom flies are private, so another user's names shouldn't block this user
        flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", otherUser.getId()));

        assertTrue(flyService.isFlyNameUnique("Woolly Bugger", testUser.getId()),
                "A name used only by another user should be unique for this user");
    }

    /**
     * Crazy path: a name that matches a default fly is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathDefaultFlyName() {
        // default flies are visible to everyone, so their names are taken for everyone
        flyRepository.save(createTestFly("Test Default", "test-default", null));

        assertFalse(flyService.isFlyNameUnique("Test Default", testUser.getId()),
                "A name that matches a default fly should not be unique");
    }

    /**
     * Crazy path: a null user ID still blocks default fly names.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathNullUserDefaultName() {
        flyRepository.save(createTestFly("Test Default", "test-default", null));

        assertFalse(flyService.isFlyNameUnique("Test Default", null),
                "A default fly name should not be unique even with a null user ID");
    }

    /**
     * Crazy path: a null user ID does not match any user's custom flies.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathNullUserCustomName() {
        // a null user ID must only see default flies, never every user's custom flies
        flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));

        assertTrue(flyService.isFlyNameUnique("Woolly Bugger", null),
                "A custom fly name should not count as taken for a null user ID");
    }

    /**
     * Crazy path: a null name and a null user ID together are not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathNullNameAndUser() {
        assertFalse(flyService.isFlyNameUnique(null, null),
                "A null name should not be unique even with a null user ID");
    }

    // ---------- isFlyNameUniqueForEdit ----------

    /**
     * Happy path: keeping a fly's current name is allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditHappyPath() {
        // without the keep-your-own-name rule, editing only the color would fail as a duplicate
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));

        assertTrue(flyService.isFlyNameUniqueForEdit(saved.getId(), "Woolly Bugger", testUser.getId()),
                "Keeping a fly's current name should be allowed");
    }

    /**
     * Happy path: renaming to a name nobody has used is allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditHappyPathNewName() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));

        // an unused name skips the keep-your-own-name shortcut and checks the full list
        assertTrue(flyService.isFlyNameUniqueForEdit(saved.getId(), "Brand New Fly", testUser.getId()),
                "Renaming to an unused name should be allowed");
    }

    /**
     * Crappy path: renaming a fly to the name of another of the user's flies is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrappyPath() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));
        // a second fly the user owns, whose name the first fly tries to take
        flyRepository.save(createTestFly("Second Fly", "second-fly", testUser.getId()));

        assertFalse(flyService.isFlyNameUniqueForEdit(saved.getId(), "Second Fly", testUser.getId()),
                "Renaming to another of the user's fly names should not be allowed");
    }

    /**
     * Crazy path: keeping the current name with different case and whitespace is allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathCaseAndWhitespace() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));

        // still turns into the slug woolly-bugger, so it counts as keeping the current name
        assertTrue(flyService.isFlyNameUniqueForEdit(saved.getId(), "  woolly bugger  ", testUser.getId()),
                "Keeping the current name with different case or whitespace should be allowed");
    }

    /**
     * Crazy path: renaming a fly to the name of a default fly is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathDefaultFlyName() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));
        // default flies are visible to everyone, so their names are taken for everyone
        flyRepository.save(createTestFly("Test Default", "test-default", null));

        assertFalse(flyService.isFlyNameUniqueForEdit(saved.getId(), "Test Default", testUser.getId()),
                "Renaming to a default fly's name should not be allowed");
    }

    /**
     * Crazy path: renaming to a name only another user has used is allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathOtherUserName() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));
        // custom flies are private, so another user's names shouldn't block this user
        flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

        assertTrue(flyService.isFlyNameUniqueForEdit(saved.getId(), "Other Fly", testUser.getId()),
                "A name used only by another user should be allowed");
    }

    /**
     * Crazy path: a blank name is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathBlankName() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));

        assertFalse(flyService.isFlyNameUniqueForEdit(saved.getId(), "   ", testUser.getId()),
                "A blank name should not be allowed");
    }

    /**
     * Crazy path: a null name is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathNullName() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));

        assertFalse(flyService.isFlyNameUniqueForEdit(saved.getId(), null, testUser.getId()),
                "A null name should not be allowed");
    }

    /**
     * Crazy path: a fly ID that does not exist is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathMissingFly() {
        // the fly being edited has to exist so its current slug can be compared
        assertFalse(flyService.isFlyNameUniqueForEdit(999999L, "Woolly Bugger", testUser.getId()),
                "Checking a fly ID that does not exist should return false");
    }

    /**
     * Crazy path: a null fly ID is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathNullFlyId() {
        // findById would throw on a null ID, so this proves the service checks for null first
        assertFalse(flyService.isFlyNameUniqueForEdit(null, "Woolly Bugger", testUser.getId()),
                "Checking a null fly ID should return false");
    }

    /**
     * Crazy path: a null user ID still blocks default fly names.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathNullUser() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));
        flyRepository.save(createTestFly("Test Default", "test-default", null));

        // a null user ID still sees default flies, so their names stay blocked
        assertFalse(flyService.isFlyNameUniqueForEdit(saved.getId(), "Test Default", null),
                "A default fly name should be blocked even with a null user ID");
    }

    /**
     * Crazy path: every parameter null is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathAllNull() {
        assertFalse(flyService.isFlyNameUniqueForEdit(null, null, null),
                "Checking with every parameter null should return false");
    }

    // ---------- isFlyNameBlank ----------

    /**
     * Happy path: a real name is not blank.
     */
    @Test
    public void testIsFlyNameBlankHappyPath() {
        assertFalse(flyService.isFlyNameBlank("Woolly Bugger"),
                "A real name should not be blank");
    }

    /**
     * Crappy path: a null name is blank.
     */
    @Test
    public void testIsFlyNameBlankCrappyPathNull() {
        // strict on bad input, null counts as blank
        assertTrue(flyService.isFlyNameBlank(null),
                "A null name should be blank");
    }

    /**
     * Crazy path: an empty name is blank.
     */
    @Test
    public void testIsFlyNameBlankCrazyPathEmpty() {
        assertTrue(flyService.isFlyNameBlank(""),
                "An empty name should be blank");
    }

    /**
     * Crazy path: a name of only spaces is blank.
     */
    @Test
    public void testIsFlyNameBlankCrazyPathSpaces() {
        assertTrue(flyService.isFlyNameBlank("   "),
                "A name of only spaces should be blank");
    }

    /**
     * Crazy path: a name of only tabs and newlines is blank.
     */
    @Test
    public void testIsFlyNameBlankCrazyPathTabsAndNewlines() {
        // isBlank covers all whitespace, not just spaces
        assertTrue(flyService.isFlyNameBlank("\t\n \r"),
                "A name of only tabs and newlines should be blank");
    }

    /**
     * Crazy path: a single character name is not blank.
     */
    @Test
    public void testIsFlyNameBlankCrazyPathSingleCharacter() {
        // boundary case, the smallest name that isn't blank
        assertFalse(flyService.isFlyNameBlank("A"),
                "A single character name should not be blank");
    }

    // ---------- isFlyNameLengthValid ----------

    /**
     * Happy path: a normal name is within the allowed length.
     */
    @Test
    public void testIsFlyNameLengthValidHappyPath() {
        assertTrue(flyService.isFlyNameLengthValid("Woolly Bugger"),
                "A normal name should be within the allowed length");
    }

    /**
     * Crappy path: a name over 150 characters is too long.
     */
    @Test
    public void testIsFlyNameLengthValidCrappyPath() {
        // one over the VARCHAR(150) name column
        String longName = "a".repeat(151);

        assertFalse(flyService.isFlyNameLengthValid(longName),
                "A name over 150 characters should be too long");
    }

    /**
     * Crazy path: a name of exactly 150 characters is allowed.
     */
    @Test
    public void testIsFlyNameLengthValidCrazyPathExactlyMax() {
        // boundary case, catches a > that should have been >= or the other way around
        String maxName = "a".repeat(150);

        assertTrue(flyService.isFlyNameLengthValid(maxName),
                "A name of exactly 150 characters should be allowed");
    }

    /**
     * Crazy path: a single character name is allowed.
     */
    @Test
    public void testIsFlyNameLengthValidCrazyPathSingleCharacter() {
        // boundary case, the shortest allowed name
        assertTrue(flyService.isFlyNameLengthValid("A"),
                "A single character name should be allowed");
    }

    /**
     * Crazy path: an empty name is too short.
     */
    @Test
    public void testIsFlyNameLengthValidCrazyPathEmpty() {
        assertFalse(flyService.isFlyNameLengthValid(""),
                "An empty name should be too short");
    }

    /**
     * Crazy path: a name of only spaces is too short once trimmed.
     */
    @Test
    public void testIsFlyNameLengthValidCrazyPathSpaces() {
        // trimmed down to an empty string, so this has 0 characters
        assertFalse(flyService.isFlyNameLengthValid("     "),
                "A name of only spaces should be too short once trimmed");
    }

    /**
     * Crazy path: spaces around a 150 character name don't count toward the length.
     */
    @Test
    public void testIsFlyNameLengthValidCrazyPathPaddedMax() {
        // 154 characters before trimming, 150 after
        String paddedName = "  " + "a".repeat(150) + "  ";

        assertTrue(flyService.isFlyNameLengthValid(paddedName),
                "Spaces around the name should be trimmed before checking the length");
    }

    /**
     * Crazy path: a null name is not valid.
     */
    @Test
    public void testIsFlyNameLengthValidCrazyPathNull() {
        assertFalse(flyService.isFlyNameLengthValid(null),
                "A null name should not be valid");
    }

    // ---------- isFlyTypeValid ----------

    /**
     * Happy path: every type the form can send is valid.
     */
    @Test
    public void testIsFlyTypeValidHappyPath() {
        // all three option values on the fly form's type dropdown
        assertTrue(flyService.isFlyTypeValid("Dry"), "Dry should be a valid type");
        assertTrue(flyService.isFlyTypeValid("Nymph"), "Nymph should be a valid type");
        assertTrue(flyService.isFlyTypeValid("Streamer"), "Streamer should be a valid type");
    }

    /**
     * Crappy path: a type that isn't on the list is not valid.
     */
    @Test
    public void testIsFlyTypeValidCrappyPath() {
        // a real fly type, just not one Hookset supports
        assertFalse(flyService.isFlyTypeValid("Wet Fly"),
                "A type that isn't on the list should not be valid");
    }

    /**
     * Crazy path: the dropdown's display text is not the value it sends, so it is not valid.
     */
    @Test
    public void testIsFlyTypeValidCrazyPathDisplayText() {
        // the form shows "Dry Fly" but sends "Dry"
        assertFalse(flyService.isFlyTypeValid("Dry Fly"),
                "The display text Dry Fly should not be valid, only the value Dry");
    }

    /**
     * Crazy path: a type with the wrong case is not valid.
     */
    @Test
    public void testIsFlyTypeValidCrazyPathWrongCase() {
        assertFalse(flyService.isFlyTypeValid("dry"),
                "A lowercase type should not be valid");
    }

    /**
     * Crazy path: a valid type with spaces around it is not valid.
     */
    @Test
    public void testIsFlyTypeValidCrazyPathPadded() {
        // types aren't trimmed, so the spaces make this not match
        assertFalse(flyService.isFlyTypeValid(" Dry "),
                "A type with spaces around it should not be valid");
    }

    /**
     * Crazy path: a blank type is not valid.
     */
    @Test
    public void testIsFlyTypeValidCrazyPathBlank() {
        assertFalse(flyService.isFlyTypeValid("   "),
                "A blank type should not be valid");
    }

    /**
     * Crazy path: a null type is not valid.
     */
    @Test
    public void testIsFlyTypeValidCrazyPathNull() {
        assertFalse(flyService.isFlyTypeValid(null),
                "A null type should not be valid");
    }

    // ---------- isFlyColorBlank ----------

    /**
     * Happy path: a real color is not blank.
     */
    @Test
    public void testIsFlyColorBlankHappyPath() {
        assertFalse(flyService.isFlyColorBlank("Olive"),
                "A real color should not be blank");
    }

    /**
     * Crappy path: a null color is blank.
     */
    @Test
    public void testIsFlyColorBlankCrappyPathNull() {
        assertTrue(flyService.isFlyColorBlank(null),
                "A null color should be blank");
    }

    /**
     * Crazy path: an empty color is blank.
     */
    @Test
    public void testIsFlyColorBlankCrazyPathEmpty() {
        assertTrue(flyService.isFlyColorBlank(""),
                "An empty color should be blank");
    }

    /**
     * Crazy path: a color of only spaces is blank.
     */
    @Test
    public void testIsFlyColorBlankCrazyPathSpaces() {
        assertTrue(flyService.isFlyColorBlank("   "),
                "A color of only spaces should be blank");
    }

    /**
     * Crazy path: a color of only tabs and newlines is blank.
     */
    @Test
    public void testIsFlyColorBlankCrazyPathTabsAndNewlines() {
        // isBlank covers all whitespace, not just spaces
        assertTrue(flyService.isFlyColorBlank("\t\n \r"),
                "A color of only tabs and newlines should be blank");
    }

    // ---------- isFlyColorLengthValid ----------

    /**
     * Happy path: a normal color is within the allowed length.
     */
    @Test
    public void testIsFlyColorLengthValidHappyPath() {
        assertTrue(flyService.isFlyColorLengthValid("Olive"),
                "A normal color should be within the allowed length");
    }

    /**
     * Crappy path: a color over 40 characters is too long.
     */
    @Test
    public void testIsFlyColorLengthValidCrappyPath() {
        // one over the VARCHAR(40) color column
        String longColor = "a".repeat(41);

        assertFalse(flyService.isFlyColorLengthValid(longColor),
                "A color over 40 characters should be too long");
    }

    /**
     * Crazy path: a color of exactly 40 characters is allowed.
     */
    @Test
    public void testIsFlyColorLengthValidCrazyPathExactlyMax() {
        // boundary case, catches a > that should have been >= or the other way around
        String maxColor = "a".repeat(40);

        assertTrue(flyService.isFlyColorLengthValid(maxColor),
                "A color of exactly 40 characters should be allowed");
    }

    /**
     * Crazy path: a color of exactly 3 characters is allowed.
     */
    @Test
    public void testIsFlyColorLengthValidCrazyPathExactlyMin() {
        // boundary case, three letters is the shortest allowed color
        assertTrue(flyService.isFlyColorLengthValid("Red"),
                "A color of exactly 3 characters should be allowed");
    }

    /**
     * Crazy path: a color of 2 characters is too short.
     */
    @Test
    public void testIsFlyColorLengthValidCrazyPathTooShort() {
        // one under the 3 character minimum
        assertFalse(flyService.isFlyColorLengthValid("Re"),
                "A color of 2 characters should be too short");
    }

    /**
     * Crazy path: spaces around a short color don't count toward the length.
     */
    @Test
    public void testIsFlyColorLengthValidCrazyPathPaddedShort() {
        // passes the form's @Size since it counts the spaces, but the service trims first
        assertFalse(flyService.isFlyColorLengthValid("  Re  "),
                "Spaces around the color should be trimmed before checking the length");
    }

    /**
     * Crazy path: an empty color is too short.
     */
    @Test
    public void testIsFlyColorLengthValidCrazyPathEmpty() {
        assertFalse(flyService.isFlyColorLengthValid(""),
                "An empty color should be too short");
    }

    /**
     * Crazy path: a null color is not valid.
     */
    @Test
    public void testIsFlyColorLengthValidCrazyPathNull() {
        assertFalse(flyService.isFlyColorLengthValid(null),
                "A null color should not be valid");
    }

    // ---------- isFlyColorValidString ----------

    /**
     * Happy path: a normal color is a valid string.
     */
    @Test
    public void testIsFlyColorValidStringHappyPath() {
        assertTrue(flyService.isFlyColorValidString("Olive"),
                "A normal color should be a valid string");
    }

    /**
     * Crappy path: a color that is only a number is not valid.
     */
    @Test
    public void testIsFlyColorValidStringCrappyPath() {
        assertFalse(flyService.isFlyColorValidString("123"),
                "A color that is only a number should not be valid");
    }

    /**
     * Crazy path: a number with spaces around it is still only a number.
     */
    @Test
    public void testIsFlyColorValidStringCrazyPathPaddedNumber() {
        // trimmed before the digit check, so the spaces don't count as non-digits
        assertFalse(flyService.isFlyColorValidString("  42  "),
                "A number with spaces around it should still not be valid");
    }

    /**
     * Crazy path: a color mixing letters and digits is valid.
     */
    @Test
    public void testIsFlyColorValidStringCrazyPathLettersAndDigits() {
        // one letter is enough to pass, since only all-digit colors are blocked
        assertTrue(flyService.isFlyColorValidString("Olive2"),
                "A color with letters and digits should be valid");
    }

    /**
     * Crazy path: a color with symbols is valid.
     */
    @Test
    public void testIsFlyColorValidStringCrazyPathSymbols() {
        assertTrue(flyService.isFlyColorValidString("Olive/Black"),
                "A color with symbols should be valid");
    }

    /**
     * Crazy path: a decimal number is not all digits, so it passes.
     */
    @Test
    public void testIsFlyColorValidStringCrazyPathDecimal() {
        // the check only blocks all-digit strings, the decimal point makes this pass
        assertTrue(flyService.isFlyColorValidString("1.5"),
                "A decimal number should pass since it isn't all digits");
    }

    /**
     * Crazy path: a blank color is not valid.
     */
    @Test
    public void testIsFlyColorValidStringCrazyPathBlank() {
        assertFalse(flyService.isFlyColorValidString("   "),
                "A blank color should not be valid");
    }

    /**
     * Crazy path: a null color is not valid.
     */
    @Test
    public void testIsFlyColorValidStringCrazyPathNull() {
        assertFalse(flyService.isFlyColorValidString(null),
                "A null color should not be valid");
    }

    // ---------- isMinSizeValidNumber ----------

    /**
     * Happy path: a normal hook size is valid.
     */
    @Test
    public void testIsMinSizeValidNumberHappyPath() {
        assertTrue(flyService.isMinSizeValidNumber(12),
                "A normal hook size should be valid");
    }

    /**
     * Crappy path: a size over 32 is not valid.
     */
    @Test
    public void testIsMinSizeValidNumberCrappyPath() {
        // one over the largest hook size
        assertFalse(flyService.isMinSizeValidNumber(33),
                "A size over 32 should not be valid");
    }

    /**
     * Crazy path: the smallest size number, 1, is valid.
     */
    @Test
    public void testIsMinSizeValidNumberCrazyPathExactlyMin() {
        // boundary case, the smallest hook size
        assertTrue(flyService.isMinSizeValidNumber(1),
                "A size of 1 should be valid");
    }

    /**
     * Crazy path: the largest size number, 32, is valid.
     */
    @Test
    public void testIsMinSizeValidNumberCrazyPathExactlyMax() {
        // boundary case, the largest hook size
        assertTrue(flyService.isMinSizeValidNumber(32),
                "A size of 32 should be valid");
    }

    /**
     * Crazy path: a size of 0 is not valid.
     */
    @Test
    public void testIsMinSizeValidNumberCrazyPathZero() {
        // one under the smallest hook size
        assertFalse(flyService.isMinSizeValidNumber(0),
                "A size of 0 should not be valid");
    }

    /**
     * Crazy path: a negative size is not valid.
     */
    @Test
    public void testIsMinSizeValidNumberCrazyPathNegative() {
        assertFalse(flyService.isMinSizeValidNumber(-5),
                "A negative size should not be valid");
    }

    /**
     * Crazy path: a null size is not valid.
     */
    @Test
    public void testIsMinSizeValidNumberCrazyPathNull() {
        assertFalse(flyService.isMinSizeValidNumber(null),
                "A null size should not be valid");
    }

    // ---------- isMaxSizeValidNumber ----------

    /**
     * Happy path: a normal hook size is valid.
     */
    @Test
    public void testIsMaxSizeValidNumberHappyPath() {
        assertTrue(flyService.isMaxSizeValidNumber(18),
                "A normal hook size should be valid");
    }

    /**
     * Crappy path: a size over 32 is not valid.
     */
    @Test
    public void testIsMaxSizeValidNumberCrappyPath() {
        // one over the largest hook size
        assertFalse(flyService.isMaxSizeValidNumber(33),
                "A size over 32 should not be valid");
    }

    /**
     * Crazy path: the smallest size number, 1, is valid.
     */
    @Test
    public void testIsMaxSizeValidNumberCrazyPathExactlyMin() {
        // boundary case, the smallest hook size
        assertTrue(flyService.isMaxSizeValidNumber(1),
                "A size of 1 should be valid");
    }

    /**
     * Crazy path: the largest size number, 32, is valid.
     */
    @Test
    public void testIsMaxSizeValidNumberCrazyPathExactlyMax() {
        // boundary case, the largest hook size
        assertTrue(flyService.isMaxSizeValidNumber(32),
                "A size of 32 should be valid");
    }

    /**
     * Crazy path: a size of 0 is not valid.
     */
    @Test
    public void testIsMaxSizeValidNumberCrazyPathZero() {
        // one under the smallest hook size
        assertFalse(flyService.isMaxSizeValidNumber(0),
                "A size of 0 should not be valid");
    }

    /**
     * Crazy path: a negative size is not valid.
     */
    @Test
    public void testIsMaxSizeValidNumberCrazyPathNegative() {
        assertFalse(flyService.isMaxSizeValidNumber(-5),
                "A negative size should not be valid");
    }

    /**
     * Crazy path: a null size is not valid.
     */
    @Test
    public void testIsMaxSizeValidNumberCrazyPathNull() {
        assertFalse(flyService.isMaxSizeValidNumber(null),
                "A null size should not be valid");
    }

    // ---------- isFlyUsedByCatch ----------

    /**
     * Happy path: a fly with a catch logged on it is in use.
     */
    @Test
    public void testIsFlyUsedByCatchHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Used Fly", "used-fly", testUser.getId()));
        // the catch is what makes the fly count as in use
        catchRepository.save(createTestCatch(saved));

        assertTrue(flyService.isFlyUsedByCatch(saved.getId()),
                "A fly with a catch logged on it should be in use");
    }

    /**
     * Crappy path: a fly with no catches is not in use.
     */
    @Test
    public void testIsFlyUsedByCatchCrappyPath() {
        Fly saved = flyRepository.save(createTestFly("Unused Fly", "unused-fly", testUser.getId()));

        assertFalse(flyService.isFlyUsedByCatch(saved.getId()),
                "A fly with no catches should not be in use");
    }

    /**
     * Crazy path: a catch on a different fly doesn't count.
     */
    @Test
    public void testIsFlyUsedByCatchCrazyPathOtherFlyHasCatch() {
        Fly usedFly = flyRepository.save(createTestFly("Used Fly", "used-fly", testUser.getId()));
        Fly unusedFly = flyRepository.save(createTestFly("Unused Fly", "unused-fly", testUser.getId()));
        catchRepository.save(createTestCatch(usedFly));

        assertFalse(flyService.isFlyUsedByCatch(unusedFly.getId()),
                "A catch on a different fly should not make this fly in use");
    }

    /**
     * Crazy path: a fly ID that doesn't exist is not in use.
     */
    @Test
    public void testIsFlyUsedByCatchCrazyPathMissingFly() {
        // far above any ID H2 will hand out during a test run
        assertFalse(flyService.isFlyUsedByCatch(999999L),
                "A fly ID that doesn't exist should not be in use");
    }

    /**
     * Crazy path: a null fly ID is not in use.
     */
    @Test
    public void testIsFlyUsedByCatchCrazyPathNull() {
        // the service returns false before existsByFlyId is ever called with null
        assertFalse(flyService.isFlyUsedByCatch(null),
                "A null fly ID should not be in use");
    }

    // ---------- isFlyOwner ----------

    /**
     * Happy path: a user owns the custom fly they made.
     */
    @Test
    public void testIsFlyOwnerHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        // the fly's user ID matches the user passed in, so this is the one case that returns true
        assertTrue(flyService.isFlyOwner(saved.getId(), testUser.getId()),
                "A user should own the custom fly they made");
    }

    /**
     * Crappy path: a user does not own another user's custom fly.
     */
    @Test
    public void testIsFlyOwnerCrappyPath() {
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

        // the fly exists, so a false here can only come from the owner check
        assertFalse(flyService.isFlyOwner(otherFly.getId(), testUser.getId()),
                "A user should not own another user's custom fly");
    }

    /**
     * Crazy path: nobody owns a default fly.
     */
    @Test
    public void testIsFlyOwnerCrazyPathDefaultFly() {
        Fly defaultFly = flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        // default flies have a null user ID, so they never belong to anyone
        assertFalse(flyService.isFlyOwner(defaultFly.getId(), testUser.getId()),
                "Nobody should own a default fly");
    }

    /**
     * Crazy path: a fly ID that does not exist is not owned by anyone.
     */
    @Test
    public void testIsFlyOwnerCrazyPathMissingFly() {
        // far above any ID H2 will hand out during a test run
        assertFalse(flyService.isFlyOwner(999999L, testUser.getId()),
                "A fly that does not exist should not be owned by anyone");
    }

    /**
     * Crazy path: a null fly ID returns false.
     */
    @Test
    public void testIsFlyOwnerCrazyPathNullFlyId() {
        // findById would throw on a null ID, so this proves the service checks for null first
        assertFalse(flyService.isFlyOwner(null, testUser.getId()),
                "A null fly ID should not be owned by anyone");
    }

    /**
     * Crazy path: a null user ID does not own a custom fly.
     */
    @Test
    public void testIsFlyOwnerCrazyPathNullUser() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        assertFalse(flyService.isFlyOwner(saved.getId(), null),
                "A null user ID should not own a custom fly");
    }

    /**
     * Crazy path: a null user ID does not own a default fly, even though both are null.
     */
    @Test
    public void testIsFlyOwnerCrazyPathNullUserDefaultFly() {
        // a null user ID and a default fly's null owner must not count as a match
        Fly defaultFly = flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        assertFalse(flyService.isFlyOwner(defaultFly.getId(), null),
                "A null user ID should not own a default fly");
    }

    /**
     * Crazy path: a null fly ID and a null user ID together return false.
     */
    @Test
    public void testIsFlyOwnerCrazyPathNullFlyIdAndUser() {
        assertFalse(flyService.isFlyOwner(null, null),
                "Both IDs null should not count as owning anything");
    }

    // ---------- Helpers ----------

    /**
     * Checks that addFly rejects the input and saves nothing.
     *
     * @param flyInput the invalid fly input to add
     * @param message the failure message for the result check
     */
    private void assertAddFlyRejected(FlyInput flyInput, String message) {
        // count before and after, so the test catches a fly being saved even when the result is false
        int before = flyRepository.findAll().size();

        boolean result = flyService.addFly(flyInput, testUser.getId());
        assertFalse(result, message);

        int after = flyRepository.findAll().size();
        assertEquals(before, after, "No fly should be saved when the input is rejected");
    }

    /**
     * Checks that updateFly rejects the input and leaves the saved fly unchanged.
     *
     * @param flyInput the invalid fly input to update with
     * @param message the failure message for the result check
     */
    private void assertUpdateFlyRejected(FlyInput flyInput, String message) {
        // a fly the test user owns, so ownership never causes the failure
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        boolean result = flyService.updateFly(saved.getId(), flyInput, testUser.getId());
        assertFalse(result, message);
        assertFlyNameUnchanged(saved.getId(), "Custom Fly");
    }

    /**
     * Reloads a fly and checks it still exists with its original name.
     *
     * @param flyId the ID of the fly to check
     * @param expectedName the name the fly should still have
     */
    private void assertFlyNameUnchanged(Long flyId, String expectedName) {
        // looked up again by ID, which inside one test transaction is the same fly the service would have changed
        Fly unchanged = flyRepository.findById(flyId).orElse(null);

        assertNotNull(unchanged, "Fly should still exist");
        assertEquals(expectedName, unchanged.getName(),
                "Fly name should not change when an update is rejected or denied");
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

        // slug is passed in rather than generated, so tests can set up collisions on purpose
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
     * Builds fly input with fixed test values and the given name and type.
     *
     * @param name the fly name
     * @param type the fly type
     * @return new fly input
     */
    private FlyInput createTestFlyInput(String name, String type) {
        return createTestFlyInput(name, type, "Olive", 12, 18);
    }

    /**
     * Builds fly input with every field given, so tests can put a null in any one of them.
     *
     * @param name the fly name
     * @param type the fly type
     * @param color the fly color
     * @param minSize the smallest hook size
     * @param maxSize the largest hook size
     * @return new fly input
     */
    private FlyInput createTestFlyInput(String name, String type, String color, Integer minSize, Integer maxSize) {
        return new FlyInput(name, type, color, minSize, maxSize);
    }

    /**
     * Builds a catch logged by the test user with the given fly.
     *
     * <p>The catch table requires a real fish and water body, so this saves one of each
     * first. The fly is passed in so tests can choose which fly the catch uses.</p>
     *
     * @param fly the fly the catch was made with
     * @return a new, unsaved catch
     */
    private Catch createTestCatch(Fly fly) {
        Fish fish = fishRepository.save(createTestFish());
        WaterBody waterBody = waterBodyRepository.save(createTestWaterBody());

        Catch testCatch = new Catch();

        // always the test user, since these tests only care which fly the catch uses
        testCatch.setUserId(testUser.getId());
        testCatch.setFly(fly);
        testCatch.setFish(fish);
        testCatch.setWaterBody(waterBody);
        testCatch.setDateCaught(LocalDateTime.now());

        return testCatch;
    }

    /**
     * Builds a fish with fixed test values.
     *
     * @return a new, unsaved fish
     */
    private Fish createTestFish() {
        Fish fish = new Fish();

        fish.setFamily("Trout");
        fish.setSpecies("Test Rainbow Trout");
        fish.setImage("rainbow-trout.png");

        return fish;
    }

    /**
     * Builds a water body with fixed test values.
     *
     * @return a new, unsaved water body
     */
    private WaterBody createTestWaterBody() {
        WaterBody waterBody = new WaterBody();

        // slug is unique in the table, so this test class gives its water body its own slug
        waterBody.setName("Fly Service Test River");
        waterBody.setType("River");
        waterBody.setState("Montana");
        waterBody.setDescription("A water body used by fly service tests.");
        waterBody.setSlug("fly-service-test-river");

        return waterBody;
    }

    /**
     * Collects the names of a list of flies.
     *
     * @param flies the flies to collect names from
     * @return the fly names in the same order
     */
    private List<String> getFlyNames(List<Fly> flies) {
        List<String> names = new ArrayList<>();

        for (Fly fly : flies) {
            names.add(fly.getName());
        }

        return names;
    }
}