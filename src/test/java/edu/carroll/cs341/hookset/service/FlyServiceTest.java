package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.FlyRepository;
import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.service.input.FlyInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shared.jpa.entity.Fly;
import shared.jpa.entity.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
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
 * {@link Transactional}. Count assertions are measured against a baseline so the tests
 * do not depend on the database starting empty.</p>
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
        testUser = userRepository.save(createTestUser("testuser"));
        otherUser = userRepository.save(createTestUser("otheruser"));
    }

    // ---------- getAllFlies ----------

    /**
     * Happy path: default flies and the user's custom flies are both returned.
     */
    @Test
    public void testGetAllFliesHappyPath() {
        // baseline instead of a hardcoded count, so seed data doesn't break the test
        int before = flyService.getAllFlies(testUser.getId()).size();
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        List<Fly> flies = flyService.getAllFlies(testUser.getId());

        assertNotNull(flies, "getAllFlies should never return null");
        assertEquals(before + 2, flies.size(),
                "List size should grow by two after saving a default and a custom fly");

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

        List<Fly> flies = flyService.getAllFlies(null);

        List<String> returnedNames = getFlyNames(flies);

        assertNotNull(flies, "getAllFlies should never return null");
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

        List<String> returnedNames = getFlyNames(flies);

        assertEquals(before, flies.size(),
                "Saving custom flies should not change the Hookset fly count");
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

        // an exact count is safe here, since seed data only contains default flies
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

        Fly result = flyService.getFlyFromSlug(null, testUser.getId());

        assertNull(result, "No fly should be returned for a null slug");
    }

    /**
     * Crazy path: an empty slug returns null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathEmptySlug() {
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

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
        Fly customResult = flyService.getFlyFromSlug("custom-fly", null);

        assertNotNull(defaultResult, "A default fly should be found for a null user ID");
        assertNull(customResult, "A custom fly should not be found for a null user ID");
    }

    /**
     * Crazy path: a null slug and a null user ID together return null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNullSlugAndUser() {
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
        Fly otherUserResult = flyService.getFlyFromSlug("woolly-bugger", otherUser.getId());

        assertNotNull(testUserResult, "Test user should find their own fly by slug");
        assertEquals(testUserFly.getId(), testUserResult.getId(),
                "Test user should get their own fly, not the other user's");
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
        Fly result = flyService.getFlyById(999999L, testUser.getId());

        assertNull(result, "No fly should be returned for an ID that does not exist");
    }

    /**
     * Crazy path: a null fly ID returns null.
     */
    @Test
    public void testGetFlyByIdCrazyPathNullId() {
        Fly result = flyService.getFlyById(null, testUser.getId());

        assertNull(result, "No fly should be returned for a null ID");
    }

    /**
     * Crazy path: another user's custom fly cannot be loaded by ID.
     */
    @Test
    public void testGetFlyByIdCrazyPathOtherUser() {
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

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

        List<Fly> userFlies = flyService.getAllUserFlies(testUser.getId());

        assertTrue(result, "addFly should return true for a valid fly");
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
        // a name that can't collide with a default fly, so the first add is guaranteed to work
        boolean firstResult = flyService.addFly(createTestFlyInput("Test Bugger", "Streamer"), testUser.getId());

        int before = flyRepository.findAll().size();

        boolean secondResult = flyService.addFly(createTestFlyInput("Test Bugger", "Streamer"), testUser.getId());

        int after = flyRepository.findAll().size();

        assertTrue(firstResult, "The first fly with a new name should be saved");
        assertFalse(secondResult, "A second fly with the same name for the same user should be rejected");
        assertEquals(before, after, "No fly should be saved for a duplicate name");
    }

    /**
     * Crappy path: a name that matches a default fly is rejected.
     */
    @Test
    public void testAddFlyCrappyPathDefaultFlyName() {
        flyRepository.save(createTestFly("Test Default", "test-default", null));

        int before = flyRepository.findAll().size();

        boolean result = flyService.addFly(createTestFlyInput("Test Default", "Dry"), testUser.getId());

        int after = flyRepository.findAll().size();

        assertFalse(result, "A fly with a default fly's name should be rejected");
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

        List<Fly> userFlies = flyService.getAllUserFlies(testUser.getId());

        assertTrue(result, "A name used only by another user should be allowed");
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

        boolean result = flyService.addFly(null, testUser.getId());

        int after = flyRepository.findAll().size();

        assertFalse(result, "A null fly input should not be saved");
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

        int after = flyRepository.findAll().size();

        assertFalse(result, "A fly with no user should not be saved");
        assertEquals(before, after, "No fly should be saved for a null user ID");
    }

    /**
     * Crazy path: a null name is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathNullName() {
        assertAddFlyRejected(createTestFlyInput(null, "Dry", "Olive", 12, 18),
                "A fly with a null name should not be saved");
    }

    /**
     * Crazy path: a blank name is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathBlankName() {
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
        assertAddFlyRejected(createTestFlyInput(null, null, null, null, null),
                "A fly with every field null should not be saved");
    }

    /**
     * Crazy path: a min size larger than the max size is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrazyPathMinGreaterThanMax() {
        assertAddFlyRejected(createTestFlyInput("Test Fly", "Dry", "Olive", 18, 12),
                "A fly with a min size larger than its max size should not be saved");
    }

    /**
     * Crazy path: a fly type that is not dry fly or streamer falls back to the nymph image.
     */
    @Test
    public void testAddFlyCrazyPathUnknownType() {
        // a type the form can't send, so this only reaches the service through a direct call
        flyService.addFly(createTestFlyInput("Soft Hackle", "Wet Fly"), testUser.getId());

        Fly saved = flyService.getAllUserFlies(testUser.getId()).getFirst();

        assertEquals("nymph.png", saved.getImage(),
                "An unknown fly type should fall back to the nymph image");
    }

    /**
     * Crazy path: extra whitespace in the name is collapsed when the slug is generated.
     */
    @Test
    public void testAddFlyCrazyPathWhitespaceName() {
        flyService.addFly(createTestFlyInput("  Parachute    Adams Test  ", "Dry"), testUser.getId());

        Fly saved = flyService.getAllUserFlies(testUser.getId()).getFirst();

        assertEquals("parachute-adams-test", saved.getSlug(),
                "Slug should trim the name and collapse inner whitespace to one hyphen");
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

        // reload from the repository so the asserts check what was saved, not the object in memory
        Fly updated = flyRepository.findById(saved.getId()).orElse(null);

        assertTrue(result, "updateFly should return true for a successful update");
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

        Fly updated = flyRepository.findById(saved.getId()).orElse(null);

        assertTrue(result, "Keeping the same name should be allowed");
        assertNotNull(updated, "Updated fly should still exist");
        assertEquals("Black", updated.getColor(), "Other fields should still be updated");
    }

    /**
     * Crazy path: another user's fly cannot be updated and is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathOtherUser() {
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

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
        boolean result = flyService.updateFly(null, createTestFlyInput("New Name", "Dry"), testUser.getId());

        assertFalse(result, "Updating a null fly ID should return false");
    }

    /**
     * Crazy path: a null input is rejected and the fly is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathNullInput() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

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
        assertUpdateFlyRejected(createTestFlyInput("New Name", "Dry", "Olive", 18, 12),
                "A min size larger than the max size should not update the fly");
    }

    // ---------- deleteFly ----------

    /**
     * Happy path: the user's fly is deleted.
     */
    @Test
    public void testDeleteFlyHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        flyService.deleteFly(saved.getId(), testUser.getId());

        assertTrue(flyRepository.findById(saved.getId()).isEmpty(),
                "Deleted fly should no longer exist");
    }

    /**
     * Crappy path: deleting a fly ID that does not exist does nothing and does not throw.
     */
    @Test
    public void testDeleteFlyCrappyPath() {
        assertDoesNotThrow(() -> flyService.deleteFly(999999L, testUser.getId()),
                "Deleting a fly ID that does not exist should not throw");
    }

    /**
     * Crazy path: another user's fly cannot be deleted and still exists afterward.
     */
    @Test
    public void testDeleteFlyCrazyPathOtherUser() {
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getId()));

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

        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(defaultFly.getId(), testUser.getId()),
                "Deleting a default fly should be denied");
        assertTrue(flyRepository.findById(defaultFly.getId()).isPresent(),
                "Default fly should still exist after a denied delete");
    }

    /**
     * Crazy path: a null fly ID does nothing and does not throw.
     */
    @Test
    public void testDeleteFlyCrazyPathNullId() {
        // a real fly exists so a null that matched something would delete it
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

        assertDoesNotThrow(() -> flyService.deleteFly(null, testUser.getId()),
                "Deleting a null fly ID should not throw");
        assertTrue(flyRepository.findById(saved.getId()).isPresent(),
                "No fly should be deleted for a null ID");
    }

    /**
     * Crazy path: a null user ID cannot delete a custom fly.
     */
    @Test
    public void testDeleteFlyCrazyPathNullUser() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getId()));

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
        assertDoesNotThrow(() -> flyService.deleteFly(null, null),
                "Deleting with both IDs null should not throw");
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
        assertTrue(flyService.isFlyNameUnique("Brand New Fly", testUser.getId()),
                "A name the user has not used should be unique");
    }

    /**
     * Crappy path: a name the user already used is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrappyPath() {
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

        assertTrue(flyService.isFlyNameUniqueForEdit(saved.getId(), "Brand New Fly", testUser.getId()),
                "Renaming to an unused name should be allowed");
    }

    /**
     * Crappy path: renaming a fly to the name of another of the user's flies is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrappyPath() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));
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

        assertTrue(flyService.isFlyNameUniqueForEdit(saved.getId(), "  woolly bugger  ", testUser.getId()),
                "Keeping the current name with different case or whitespace should be allowed");
    }

    /**
     * Crazy path: renaming a fly to the name of a default fly is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathDefaultFlyName() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getId()));
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
        assertFalse(flyService.isFlyNameUniqueForEdit(999999L, "Woolly Bugger", testUser.getId()),
                "Checking a fly ID that does not exist should return false");
    }

    /**
     * Crazy path: a null fly ID is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathNullFlyId() {
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
        assertFalse(flyService.isFlyNameBlank("A"),
                "A single character name should not be blank");
    }

    // ---------- Helpers ----------

    /**
     * Checks that addFly rejects the input and saves nothing.
     *
     * @param flyInput the invalid fly input to add
     * @param message the failure message for the result check
     */
    private void assertAddFlyRejected(FlyInput flyInput, String message) {
        int before = flyRepository.findAll().size();

        boolean result = flyService.addFly(flyInput, testUser.getId());

        int after = flyRepository.findAll().size();

        assertFalse(result, message);
        assertEquals(before, after, "No fly should be saved when the input is rejected");
    }

    /**
     * Checks that updateFly rejects the input and leaves the saved fly unchanged.
     *
     * @param flyInput the invalid fly input to update with
     * @param message the failure message for the result check
     */
    private void assertUpdateFlyRejected(FlyInput flyInput, String message) {
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
        // reload from the repository so the check reads what was saved, not the object in memory
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