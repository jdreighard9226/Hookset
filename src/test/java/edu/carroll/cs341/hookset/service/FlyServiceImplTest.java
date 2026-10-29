package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.FlyRepository;
import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
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
 * Crazy paths cover unusual input and ownership edge cases, such as two users sharing a
 * slug, one user trying to read or change another user's fly, and attempts to change
 * default Hookset flies.</p>
 *
 * <p>Two users are saved before each test, and the first one is logged in by loading their
 * real {@link HooksetUserDetails} through the application's {@link UserDetailsService}.
 * Tests that need the other user, or no user, change the security context directly.</p>
 *
 * <p>Tests run against the H2 test database and each test is rolled back by
 * {@link Transactional}. Count assertions are measured against a baseline so the tests
 * do not depend on the database starting empty.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FlyServiceImplTest {

    /** Service under test. */
    @Autowired
    private FlyService flyService;

    /** Repository used to set up and check fly test data. */
    @Autowired
    private FlyRepository flyRepository;

    /** Repository used to set up user test data. */
    @Autowired
    private UserRepository userRepository;

    /** User details service used to log in test users the same way the app does. */
    @Autowired
    private UserDetailsService userDetailsService;

    /** Data source used to confirm the tests are running against H2. */
    @Autowired
    private DataSource dataSource;

    /** Password encoder used to hash test user passwords the same way signup does. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** The user logged in for each test. */
    private User testUser;

    /** A second user used to test ownership rules. */
    private User otherUser;

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

    /**
     * Saves two test users and logs in the first one before each test.
     */
    @BeforeEach
    public void setUpUsers() {
        testUser = userRepository.save(createTestUser("testuser"));
        otherUser = userRepository.save(createTestUser("otheruser"));

        loginAs(testUser);
    }

    /**
     * Clears the security context after each test so a login does not leak into the next test.
     */
    @AfterEach
    public void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    // ---------- getAllFlies ----------

    /**
     * Happy path: default flies and the current user's custom flies are both returned.
     */
    @Test
    public void testGetAllFliesHappyPath() {
        // baseline instead of a hardcoded count, so seed data doesn't break the test
        int before = flyService.getAllFlies().size();
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        List<Fly> flies = flyService.getAllFlies();

        assertNotNull(flies, "getAllFlies should never return null");
        assertEquals(before + 2, flies.size(),
                "List size should grow by two after saving a default and a custom fly");

        List<String> returnedNames = getFlyNames(flies);

        assertTrue(returnedNames.contains("Default Fly"),
                "Default fly should be in the returned list");
        assertTrue(returnedNames.contains("Custom Fly"),
                "Current user's custom fly should be in the returned list");
    }

    /**
     * Crappy path: no flies are added, so the list does not grow and is never null.
     */
    @Test
    public void testGetAllFliesCrappyPath() {
        // read straight from the repository so the baseline doesn't go through the method under test
        int before = flyRepository.findByUserIdIsNullOrUserId(testUser.getUserId()).size();

        List<Fly> flies = flyService.getAllFlies();

        assertNotNull(flies, "getAllFlies should never return null");
        assertEquals(before, flies.size(),
                "List size should not change when no flies are added");
    }

    /**
     * Crazy path: another user's custom fly is not returned.
     */
    @Test
    public void testGetAllFliesCrazyPathOtherUser() {
        flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getUserId()));

        List<Fly> flies = flyService.getAllFlies();

        List<String> returnedNames = getFlyNames(flies);

        assertFalse(returnedNames.contains("Other Fly"),
                "Another user's custom fly should not be returned");
    }

    /**
     * Crazy path: with no user logged in, only default flies are returned.
     */
    @Test
    public void testGetAllFliesCrazyPathNoUser() {
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        // log out after saving, so the custom fly exists but nobody owns the session
        SecurityContextHolder.clearContext();

        List<Fly> flies = flyService.getAllFlies();

        List<String> returnedNames = getFlyNames(flies);

        assertTrue(returnedNames.contains("Default Fly"),
                "Default fly should be returned when no user is logged in");
        assertFalse(returnedNames.contains("Custom Fly"),
                "Custom flies should not be returned when no user is logged in");
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
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));
        flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getUserId()));

        List<Fly> flies = flyService.getAllHooksetFlies();

        List<String> returnedNames = getFlyNames(flies);

        assertEquals(before, flies.size(),
                "Saving custom flies should not change the Hookset fly count");
        assertFalse(returnedNames.contains("Custom Fly"),
                "Current user's custom fly should not be a Hookset fly");
        assertFalse(returnedNames.contains("Other Fly"),
                "Another user's custom fly should not be a Hookset fly");
    }

    // ---------- getAllUserFlies ----------

    /**
     * Happy path: the current user's custom fly is returned.
     */
    @Test
    public void testGetAllUserFliesHappyPath() {
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        List<Fly> flies = flyService.getAllUserFlies();

        // an exact count is safe here, since seed data only contains default flies
        assertNotNull(flies, "getAllUserFlies should never return null");
        assertEquals(1, flies.size(),
                "Current user should have exactly one custom fly");
        assertEquals("Custom Fly", flies.getFirst().getName(),
                "Returned fly should be the current user's custom fly");
    }

    /**
     * Crappy path: a user with no custom flies gets an empty list.
     */
    @Test
    public void testGetAllUserFliesCrappyPath() {
        List<Fly> flies = flyService.getAllUserFlies();

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
        flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getUserId()));

        List<Fly> flies = flyService.getAllUserFlies();

        assertNotNull(flies, "getAllUserFlies should never return null");
        assertTrue(flies.isEmpty(),
                "Default flies and other users' flies should not be returned as the user's flies");
    }

    // ---------- getFlyFromSlug ----------

    /**
     * Happy path: the current user's custom fly is found by its slug.
     */
    @Test
    public void testGetFlyFromSlugHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        Fly result = flyService.getFlyFromSlug("custom-fly");

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
        assertEquals(testUser.getUserId(), result.getUserId(),
                "Returned fly should belong to the current user");
    }

    /**
     * Happy path: a default fly is found by its slug while a user is logged in.
     */
    @Test
    public void testGetFlyFromSlugHappyPathDefaultFly() {
        Fly saved = flyRepository.save(createTestFly("Default Fly", "default-fly", null));

        Fly result = flyService.getFlyFromSlug("default-fly");

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
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        Fly result = flyService.getFlyFromSlug("does-not-exist");

        assertNull(result, "No fly should be returned for a slug that does not exist");
    }

    /**
     * Crazy path: a null slug returns null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNull() {
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        Fly result = flyService.getFlyFromSlug(null);

        assertNull(result, "No fly should be returned for a null slug");
    }

    /**
     * Crazy path: an empty slug returns null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathEmpty() {
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        Fly result = flyService.getFlyFromSlug("");

        assertNull(result, "No fly should be returned for an empty slug");
    }

    /**
     * Crazy path: a slug that only belongs to another user's custom fly returns null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathOtherUser() {
        flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getUserId()));

        Fly result = flyService.getFlyFromSlug("other-fly");

        assertNull(result, "Another user's custom fly should not be found by slug");
    }

    /**
     * Crazy path: two users have a custom fly with the same slug, and each user gets their own.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathSharedSlug() {
        // saved straight through the repository, since addFly would never create a shared slug for one user
        Fly testUserFly = flyRepository.save(
                createTestFly("Woolly Bugger", "woolly-bugger", testUser.getUserId()));
        Fly otherUserFly = flyRepository.save(
                createTestFly("Woolly Bugger", "woolly-bugger", otherUser.getUserId()));

        Fly testUserResult = flyService.getFlyFromSlug("woolly-bugger");

        // switch users mid-test to check the same slug resolves differently per login
        loginAs(otherUser);
        Fly otherUserResult = flyService.getFlyFromSlug("woolly-bugger");

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
        flyRepository.save(createTestFly("Parachute Adams", "parachute-adams", testUser.getUserId()));

        Fly result = flyService.getFlyFromSlug("parachute-adams");

        // the service checks default flies first, so this pins down that order
        assertNotNull(result, "A fly should be found when a default and custom fly share a slug");
        assertEquals(defaultFly.getId(), result.getId(),
                "The default fly should be returned before the user's custom fly");
    }

    /**
     * Crazy path: with no user logged in, a default fly is found but a custom fly is not.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNoUser() {
        flyRepository.save(createTestFly("Default Fly", "default-fly", null));
        flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        // with no user, a null user ID must not turn into an IS NULL query that matches default flies
        SecurityContextHolder.clearContext();

        Fly defaultResult = flyService.getFlyFromSlug("default-fly");
        Fly customResult = flyService.getFlyFromSlug("custom-fly");

        assertNotNull(defaultResult, "A default fly should be found when no user is logged in");
        assertNull(customResult, "A custom fly should not be found when no user is logged in");
    }

    // ---------- getFlyById ----------

    /**
     * Happy path: the current user's custom fly is found by its ID.
     */
    @Test
    public void testGetFlyByIdHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        Fly result = flyService.getFlyById(saved.getId());

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

        Fly result = flyService.getFlyById(saved.getId());

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
        Fly result = flyService.getFlyById(999999L);

        assertNull(result, "No fly should be returned for an ID that does not exist");
    }

    /**
     * Crazy path: a null ID returns null.
     */
    @Test
    public void testGetFlyByIdCrazyPathNull() {
        Fly result = flyService.getFlyById(null);

        assertNull(result, "No fly should be returned for a null ID");
    }
    /**
     * Crazy path: another user's custom fly cannot be loaded by ID.
     */
    @Test
    public void testGetFlyByIdCrazyPathOtherUser() {
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getUserId()));

        assertThrows(AccessDeniedException.class,
                () -> flyService.getFlyById(otherFly.getId()),
                "Loading another user's fly by ID should be denied");
    }

    // ---------- addFly ----------

    /**
     * Happy path: a dry fly is saved for the current user with a generated slug and the dry fly image.
     */
    @Test
    public void testAddFlyHappyPath() {
        // "Dry" is the form's option value, not the "Dry Fly" display text
        FlyForm flyForm = createTestFlyForm("Parachute Adams", "Dry");

        flyService.addFly(flyForm);

        List<Fly> userFlies = flyService.getAllUserFlies();

        assertEquals(1, userFlies.size(),
                "Current user should have one custom fly after adding one");

        Fly saved = userFlies.getFirst();

        assertEquals(testUser.getUserId(), saved.getUserId(),
                "Added fly should belong to the current user");
        assertEquals("Parachute Adams", saved.getName(),
                "Added fly name should match the form");
        assertEquals("parachute-adams", saved.getSlug(),
                "Added fly slug should be generated from the name");
        assertEquals("Dry", saved.getType(),
                "Added fly type should match the form");
        assertEquals("dry.png", saved.getImage(),
                "A dry fly should use the dry fly image");
        assertEquals("Olive", saved.getColor(),
                "Added fly color should match the form");
        assertEquals(12, saved.getMinSize(),
                "Added fly min size should match the form");
        assertEquals(18, saved.getMaxSize(),
                "Added fly max size should match the form");
    }

    /**
     * Happy path: a streamer is saved with the streamer image.
     */
    @Test
    public void testAddFlyHappyPathStreamer() {
        FlyForm flyForm = createTestFlyForm("Woolly Bugger", "Streamer");

        flyService.addFly(flyForm);

        Fly saved = flyService.getAllUserFlies().getFirst();

        assertEquals("streamer.png", saved.getImage(),
                "A streamer should use the streamer image");
    }

    /**
     * Crappy path: a form missing a color is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrappyPathMissingColor() {
        FlyForm flyForm = createTestFlyForm("Test Fly", "Dry");
        flyForm.setColor(null);

        int before = flyRepository.findAll().size();

        boolean result = flyService.addFly(flyForm);

        List<Fly> flyList = flyRepository.findAll();
        int after = flyList.size();

        // the service catches it before the database's NOT NULL constraint ever runs
        assertFalse(result, "A fly with no color should not be saved");
        assertEquals(before, after);
    }

    /**
     * Crappy path: adding a second fly with the same name for the same user is rejected.
     */
    @Test
    public void testAddFlyCrappyPathDuplicateName() {
        // a name that can't collide with a default fly, so the first add is guaranteed to work
        boolean firstResult = flyService.addFly(createTestFlyForm("Test Bugger", "Streamer"));

        int before = flyRepository.findAll().size();

        boolean secondResult = flyService.addFly(createTestFlyForm("Test Bugger", "Streamer"));

        List<Fly> flyList = flyRepository.findAll();
        int after = flyList.size();

        assertTrue(firstResult, "The first fly with a new name should be saved");
        assertFalse(secondResult, "A second fly with the same name for the same user should be rejected");
        assertEquals(before, after);
    }

    /**
     * Crazy path: a fly type that is not dry fly or streamer falls back to the nymph image.
     */
    @Test
    public void testAddFlyCrazyPathUnknownType() {
        // a type the form can't send, so this only reaches the service through a direct call
        FlyForm flyForm = createTestFlyForm("Soft Hackle", "Wet Fly");

        flyService.addFly(flyForm);

        Fly saved = flyService.getAllUserFlies().getFirst();

        assertEquals("nymph.png", saved.getImage(),
                "An unknown fly type should fall back to the nymph image");
    }

    /**
     * Crazy path: extra whitespace in the name is collapsed when the slug is generated.
     */
    @Test
    public void testAddFlyCrazyPathWhitespaceName() {
        FlyForm flyForm = createTestFlyForm("  Parachute    Adams  ", "Dry");

        flyService.addFly(flyForm);

        Fly saved = flyService.getAllUserFlies().getFirst();

        assertEquals("parachute-adams", saved.getSlug(),
                "Slug should trim the name and collapse inner whitespace to one hyphen");
    }

    // ---------- updateFly ----------

    /**
     * Happy path: the current user's fly is updated and the slug and image are regenerated.
     */
    @Test
    public void testUpdateFlyHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Old Name", "old-name", testUser.getUserId()));

        // every field changes from the saved values so a skipped setter would show up
        FlyForm flyForm = createTestFlyForm("New Name", "Streamer");
        flyForm.setColor("Black");
        flyForm.setMinSize(4);
        flyForm.setMaxSize(8);

        boolean result = flyService.updateFly(saved.getId(), flyForm);

        // reload from the repository so the asserts check what was saved, not the object in memory
        Fly updated = flyRepository.findById(saved.getId()).orElse(null);

        assertTrue(result, "updateFly should return true for a successful update");
        assertNotNull(updated, "Updated fly should still exist");
        assertEquals("New Name", updated.getName(),
                "Fly name should be updated");
        assertEquals("new-name", updated.getSlug(),
                "Fly slug should be regenerated from the new name");
        assertEquals("Streamer", updated.getType(),
                "Fly type should be updated");
        assertEquals("streamer.png", updated.getImage(),
                "Fly image should be updated to match the new type");
        assertEquals("Black", updated.getColor(),
                "Fly color should be updated");
        assertEquals(4, updated.getMinSize(),
                "Fly min size should be updated");
        assertEquals(8, updated.getMaxSize(),
                "Fly max size should be updated");
    }

    /**
     * Crappy path: updating a fly ID that does not exist returns false.
     */
    @Test
    public void testUpdateFlyCrappyPath() {
        FlyForm flyForm = createTestFlyForm("New Name", "Dry");

        boolean result = flyService.updateFly(999999L, flyForm);

        assertFalse(result, "Updating a fly ID that does not exist should return false");
    }

    /**
     * Crazy path: another user's fly cannot be updated and is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathOtherUser() {
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getUserId()));
        FlyForm flyForm = createTestFlyForm("Hijacked Name", "Dry");

        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(otherFly.getId(), flyForm),
                "Updating another user's fly should be denied");

        // the exception alone doesn't prove nothing changed, so check the saved row too
        Fly unchanged = flyRepository.findById(otherFly.getId()).orElse(null);

        assertNotNull(unchanged, "Another user's fly should still exist after a denied update");
        assertEquals("Other Fly", unchanged.getName(),
                "Another user's fly should not be changed after a denied update");
    }

    /**
     * Crazy path: a default fly cannot be updated and is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathDefaultFly() {
        Fly defaultFly = flyRepository.save(createTestFly("Default Fly", "default-fly", null));
        FlyForm flyForm = createTestFlyForm("Hijacked Name", "Dry");

        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(defaultFly.getId(), flyForm),
                "Updating a default fly should be denied");

        Fly unchanged = flyRepository.findById(defaultFly.getId()).orElse(null);

        assertNotNull(unchanged, "Default fly should still exist after a denied update");
        assertEquals("Default Fly", unchanged.getName(),
                "Default fly should not be changed after a denied update");
    }

    // ---------- deleteFly ----------

    /**
     * Happy path: the current user's fly is deleted.
     */
    @Test
    public void testDeleteFlyHappyPath() {
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        flyService.deleteFly(saved.getId());

        assertTrue(flyRepository.findById(saved.getId()).isEmpty(),
                "Deleted fly should no longer exist");
    }

    /**
     * Crappy path: deleting a fly ID that does not exist does nothing and does not throw.
     */
    @Test
    public void testDeleteFlyCrappyPath() {
        assertDoesNotThrow(() -> flyService.deleteFly(999999L),
                "Deleting a fly ID that does not exist should not throw");
    }

    /**
     * Crazy path: another user's fly cannot be deleted and still exists afterward.
     */
    @Test
    public void testDeleteFlyCrazyPathOtherUser() {
        Fly otherFly = flyRepository.save(createTestFly("Other Fly", "other-fly", otherUser.getUserId()));

        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(otherFly.getId()),
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
                () -> flyService.deleteFly(defaultFly.getId()),
                "Deleting a default fly should be denied");
        assertTrue(flyRepository.findById(defaultFly.getId()).isPresent(),
                "Default fly should still exist after a denied delete");
    }

    /**
     * Crazy path: a null ID does nothing and does not throw.
     */
    @Test
    public void testDeleteFlyCrazyPathNull() {
        // a real fly exists so a null that matched something would delete it
        Fly saved = flyRepository.save(createTestFly("Custom Fly", "custom-fly", testUser.getUserId()));

        assertDoesNotThrow(() -> flyService.deleteFly(null),
                "Deleting a null fly ID should not throw");
        assertTrue(flyRepository.findById(saved.getId()).isPresent(),
                "No fly should be deleted for a null ID");
    }

    // ---------- isSizeRangeValid ----------

    /**
     * Happy path: a min size smaller than the max size is valid.
     */
    @Test
    public void testIsSizeRangeValidHappyPath() {
        FlyForm flyForm = createTestFlyForm("Test Fly", "Dry");
        flyForm.setMinSize(12);
        flyForm.setMaxSize(18);

        boolean result = flyService.isSizeRangeValid(flyForm);

        assertTrue(result, "A min size smaller than the max size should be valid");
    }

    /**
     * Crappy path: a min size larger than the max size is invalid.
     */
    @Test
    public void testIsSizeRangeValidCrappyPath() {
        FlyForm flyForm = createTestFlyForm("Test Fly", "Dry");
        flyForm.setMinSize(18);
        flyForm.setMaxSize(12);

        boolean result = flyService.isSizeRangeValid(flyForm);

        assertFalse(result, "A min size larger than the max size should be invalid");
    }

    /**
     * Crazy path: a min size equal to the max size is valid.
     */
    @Test
    public void testIsSizeRangeValidCrazyPathEqualSizes() {
        // boundary case, catches a > that should have been >= or the other way around
        FlyForm flyForm = createTestFlyForm("Test Fly", "Dry");
        flyForm.setMinSize(14);
        flyForm.setMaxSize(14);

        boolean result = flyService.isSizeRangeValid(flyForm);

        assertTrue(result, "A min size equal to the max size should be valid");
    }

    /**
     * Crazy path: a missing size passes so the form's not null check can report it instead.
     */
    @Test
    public void testIsSizeRangeValidCrazyPathNullSize() {
        FlyForm flyForm = createTestFlyForm("Test Fly", "Dry");
        flyForm.setMinSize(null);
        flyForm.setMaxSize(12);

        boolean result = flyService.isSizeRangeValid(flyForm);

        assertTrue(result, "A missing size should pass so the not null check reports it");
    }

    // ---------- isFlyNameUnique ----------

    /**
     * Happy path: a name the user has not used is unique.
     */
    @Test
    public void testIsFlyNameUniqueHappyPath() {
        FlyForm flyForm = createTestFlyForm("Brand New Fly", "Dry");

        boolean result = flyService.isFlyNameUnique(flyForm);

        assertTrue(result, "A name the user has not used should be unique");
    }

    /**
     * Crappy path: a name the user already used is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrappyPath() {
        flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getUserId()));
        FlyForm flyForm = createTestFlyForm("Woolly Bugger", "Streamer");

        boolean result = flyService.isFlyNameUnique(flyForm);

        assertFalse(result, "A name the user already used should not be unique");
    }

    /**
     * Crazy path: a duplicate name with different case and extra whitespace is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathCaseAndWhitespace() {
        flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getUserId()));
        FlyForm flyForm = createTestFlyForm("  WOOLLY BUGGER  ", "Streamer");

        boolean result = flyService.isFlyNameUnique(flyForm);

        assertFalse(result, "A duplicate name should not be unique regardless of case or whitespace");
    }

    /**
     * Crazy path: a blank name is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathBlank() {
        // strict on bad input, blank is never treated as a usable name
        FlyForm flyForm = createTestFlyForm("   ", "Dry");

        boolean result = flyService.isFlyNameUnique(flyForm);

        assertFalse(result, "A blank name should not be unique");
    }

    /**
     * Crazy path: a null name is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathNull() {
        FlyForm flyForm = createTestFlyForm(null, "Dry");

        boolean result = flyService.isFlyNameUnique(flyForm);

        assertFalse(result, "A null name should not be unique");
    }

    /**
     * Crazy path: a name another user already used is still unique for the current user.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathOtherUserName() {
        // custom flies are private, so another user's names shouldn't block this user
        flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", otherUser.getUserId()));
        FlyForm flyForm = createTestFlyForm("Woolly Bugger", "Streamer");

        boolean result = flyService.isFlyNameUnique(flyForm);

        assertTrue(result, "A name used only by another user should be unique for the current user");
    }

    /**
     * Crazy path: a name that matches a default fly is not unique.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathDefaultFlyName() {
        // default flies are visible to everyone, so their names are taken for everyone
        flyRepository.save(createTestFly("Parachute Adams", "parachute-adams", null));
        FlyForm flyForm = createTestFlyForm("Parachute Adams", "Dry");

        boolean result = flyService.isFlyNameUnique(flyForm);

        assertFalse(result, "A name that matches a default fly should not be unique");
    }

    // ---------- isFlyNameUniqueForEdit ----------

    /**
     * Happy path: keeping a fly's current name is allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditHappyPath() {
        // without the keep-your-own-name rule, editing only the color would fail as a duplicate
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getUserId()));
        FlyForm flyForm = createTestFlyForm("Woolly Bugger", "Streamer");

        boolean result = flyService.isFlyNameUniqueForEdit(saved.getId(), flyForm);

        assertTrue(result, "Keeping a fly's current name should be allowed");
    }

    /**
     * Crappy path: renaming a fly to the name of another of the user's flies is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrappyPath() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getUserId()));
        flyRepository.save(createTestFly("Parachute Adams", "parachute-adams", testUser.getUserId()));
        FlyForm flyForm = createTestFlyForm("Parachute Adams", "Dry");

        boolean result = flyService.isFlyNameUniqueForEdit(saved.getId(), flyForm);

        assertFalse(result, "Renaming to another of the user's fly names should not be allowed");
    }

    /**
     * Crazy path: keeping the current name with different case and whitespace is allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathCaseAndWhitespace() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getUserId()));
        FlyForm flyForm = createTestFlyForm("  woolly bugger  ", "Streamer");

        boolean result = flyService.isFlyNameUniqueForEdit(saved.getId(), flyForm);

        assertTrue(result, "Keeping the current name with different case or whitespace should be allowed");
    }

    /**
     * Crazy path: renaming a fly to the name of a default fly is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathDefaultFlyName() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getUserId()));
        flyRepository.save(createTestFly("Parachute Adams", "parachute-adams", null));
        FlyForm flyForm = createTestFlyForm("Parachute Adams", "Dry");

        boolean result = flyService.isFlyNameUniqueForEdit(saved.getId(), flyForm);

        assertFalse(result, "Renaming to a default fly's name should not be allowed");
    }

    /**
     * Crazy path: a blank name is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathBlank() {
        Fly saved = flyRepository.save(createTestFly("Woolly Bugger", "woolly-bugger", testUser.getUserId()));
        FlyForm flyForm = createTestFlyForm("   ", "Streamer");

        boolean result = flyService.isFlyNameUniqueForEdit(saved.getId(), flyForm);

        assertFalse(result, "A blank name should not be allowed");
    }

    /**
     * Crazy path: a fly ID that does not exist is not allowed.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathMissingFly() {
        FlyForm flyForm = createTestFlyForm("Woolly Bugger", "Streamer");

        boolean result = flyService.isFlyNameUniqueForEdit(999999L, flyForm);

        assertFalse(result, "Checking a fly ID that does not exist should return false");
    }

    // ---------- Helpers ----------

    /**
     * Logs in a user by loading their real {@link HooksetUserDetails} and placing it in the security context.
     *
     * @param user the saved user to log in
     */
    private void loginAs(User user) {
        // loads through the real UserDetailsService so the test login matches what the app does
        HooksetUserDetails userDetails =
                (HooksetUserDetails) userDetailsService.loadUserByUsername(user.getUsername());

        // skips the password check, since these tests are about what a logged-in user can do
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
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
     * Builds a fly form with fixed test values and the given name and type.
     *
     * @param name the fly name to put on the form
     * @param type the fly type to put on the form
     * @return a new fly form
     */
    private FlyForm createTestFlyForm(String name, String type) {
        FlyForm flyForm = new FlyForm();

        flyForm.setName(name);
        flyForm.setType(type);
        flyForm.setColor("Olive");
        flyForm.setMinSize(12);
        flyForm.setMaxSize(18);

        return flyForm;
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