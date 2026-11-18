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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import entities.jpa.entity.Fish;
import entities.jpa.entity.Fly;
import entities.jpa.entity.WaterBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link FlyService}.
 *
 * <p>Each service method is tested against a happy path, a crappy path, and a crazy
 * path. Happy paths cover valid input with an expected result. Crappy paths cover
 * bad input, IDs that don't exist, and users who don't own the fly. Crazy paths
 * cover null input, alone and mixed with bad input.</p>
 *
 * <p>Length and size checks are tested right at their limits, since that's where an
 * off-by-one mistake would show up.</p>
 *
 * <p>Two test users are created before each test. Custom flies are created through
 * {@link FlyService#addFly(FlyInput, Long)}, default flies through
 * {@link FlyService#addDefaultHooksetFly(FlyInput)}, and catches through
 * {@link CatchService}, so no repositories are used.</p>
 *
 * <p>Tests run against the H2 test database, which starts empty, and each test is
 * rolled back by {@link Transactional}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FlyServiceTest {

    /** Service under test. */
    @Autowired
    private FlyService flyService;

    /** Service used to create the test users. */
    @Autowired
    private UserService userService;

    /** Service used to load the test users, so their IDs are known. */
    @Autowired
    private LoginService loginService;

    /** Service used to create the fish a test catch needs. */
    @Autowired
    private FishService fishService;

    /** Service used to create the water body a test catch needs. */
    @Autowired
    private WaterBodyService waterBodyService;

    /** Service used to create catches that use a fly. */
    @Autowired
    private CatchService catchService;

    /** The username of the main test user. */
    private static final String TEST_USERNAME = "testuser";

    /** The username of the second test user, used to check that flies stay with their owner. */
    private static final String OTHER_USERNAME = "otheruser";

    /** The plain text password every test user signs up with. */
    private static final String TEST_PASSWORD = "test-password";

    /** The name given to the main test fly. */
    private static final String TEST_FLY_NAME = "Test Fly";

    /** The name given to the main default fly. */
    private static final String DEFAULT_FLY_NAME = "Hookset Fly";

    /** The type given to every test fly. */
    private static final String TEST_FLY_TYPE = "Dry";

    /** The color given to every test fly. */
    private static final String TEST_FLY_COLOR = "olive";

    /** The min hook size given to every test fly. */
    private static final Integer TEST_MIN_SIZE = 12;

    /** The max hook size given to every test fly. */
    private static final Integer TEST_MAX_SIZE = 18;

    /** The ID of the main test user, set before each test. */
    private Long testUserId;

    /** The ID of the second test user, set before each test. */
    private Long otherUserId;

    /**
     * Creates the two test users before each test and saves their IDs.
     */
    @BeforeEach
    public void setUpUsers() {
        // users are added through the service, since tests can't use repositories
        boolean testUserAdded = userService.addUser(new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not FlyService
        assertTrue(testUserAdded, "The test user should be added");

        boolean otherUserAdded = userService.addUser(new SignupInput(OTHER_USERNAME, TEST_PASSWORD, TEST_PASSWORD));

        // setup check, if this fails then addUser or the test data is broken, not FlyService
        assertTrue(otherUserAdded, "The other test user should be added");

        // addUser doesn't return the user, so loading it is how the test gets its ID
        UserDetails testUserDetails = loginService.loadUserByUsername(TEST_USERNAME);
        UserDetails otherUserDetails = loginService.loadUserByUsername(OTHER_USERNAME);

        // the ID is only on Hookset's own user details, so the loaded users have to be that type
        assertInstanceOf(HooksetUserDetails.class, testUserDetails, "The test user should be a HooksetUserDetails");
        assertInstanceOf(HooksetUserDetails.class, otherUserDetails, "The other user should be a HooksetUserDetails");

        testUserId = ((HooksetUserDetails) testUserDetails).getId();
        otherUserId = ((HooksetUserDetails) otherUserDetails).getId();
    }

    // ---------- getAllFlies ----------

    /**
     * Happy path: with no flies added, an empty list is returned instead of null.
     */
    @Test
    public void testGetAllFliesHappyPathNoFlies() {
        List<Fly> flies = flyService.getAllFlies(testUserId);

        // callers loop over this list, so null would crash them where an empty list wouldn't
        assertNotNull(flies, "getAllFlies should never return null");
        assertTrue(flies.isEmpty(), "The list should be empty when no flies have been added");
    }

    /**
     * Happy path: one default fly and one of the user's custom flies are both returned.
     */
    @Test
    public void testGetAllFliesHappyPathOneOfEach() {
        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getAllFlies
        assertTrue(defaultAdded, "The default fly should be added");

        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getAllFlies
        assertTrue(customAdded, "The custom fly should be added");

        List<Fly> flies = flyService.getAllFlies(testUserId);
        assertNotNull(flies, "getAllFlies should never return null");

        // one fly with no owner and one owned by the user, so both should come back
        assertEquals(2, flies.size(), "The list should have the default fly and the user's custom fly");

        // checks by name instead of position, since the list order isn't guaranteed
        List<String> returnedNames = new ArrayList<>();
        for (Fly returnedFly : flies) {
            returnedNames.add(returnedFly.getName());
        }

        // the count alone could pass with two of the same kind, so check for one of each
        assertTrue(returnedNames.contains(DEFAULT_FLY_NAME), "The default fly should be in the list");
        assertTrue(returnedNames.contains(TEST_FLY_NAME), "The user's custom fly should be in the list");
    }

    /**
     * Happy path: many default flies and many of the user's custom flies are all returned,
     * and another user's custom flies are left out.
     */
    @Test
    public void testGetAllFliesHappyPathManyFlies() {
        final int defaultCount = 50;
        final int customCount = 50;
        final int otherUserCount = 10;

        // each fly gets its own name so they can be told apart, and so no add is rejected as a duplicate
        List<String> expectedNames = new ArrayList<>();
        for (int i = 1; i <= defaultCount; i++) {
            String name = DEFAULT_FLY_NAME + " " + i;
            boolean defaultAdded = flyService.addDefaultHooksetFly(
                    new FlyInput(name, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

            // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getAllFlies
            assertTrue(defaultAdded, "Default fly " + name + " should be added");
            expectedNames.add(name);
        }

        for (int i = 1; i <= customCount; i++) {
            String name = TEST_FLY_NAME + " " + i;
            boolean customAdded = flyService.addFly(
                    new FlyInput(name, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

            // setup check, if this fails then addFly or the test data is broken, not getAllFlies
            assertTrue(customAdded, "Custom fly " + name + " should be added");
            expectedNames.add(name);
        }

        // the other user's flies should never show up in the test user's list
        List<String> otherUserNames = new ArrayList<>();
        for (int i = 1; i <= otherUserCount; i++) {
            String name = "Other Fly " + i;
            boolean otherAdded = flyService.addFly(
                    new FlyInput(name, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), otherUserId);

            // setup check, if this fails then addFly or the test data is broken, not getAllFlies
            assertTrue(otherAdded, "Other user's fly " + name + " should be added");
            otherUserNames.add(name);
        }

        List<Fly> flies = flyService.getAllFlies(testUserId);
        assertNotNull(flies, "getAllFlies should never return null");

        // only the default flies and the test user's own flies count
        assertEquals(defaultCount + customCount, flies.size(),
                "The list should have every default fly and every one of the user's custom flies");

        // checks by name instead of position, since the list order isn't guaranteed
        List<String> returnedNames = new ArrayList<>();
        for (Fly returnedFly : flies) {
            returnedNames.add(returnedFly.getName());
        }

        // the count alone could pass with the wrong flies, so check each one is actually there
        for (String name : expectedNames) {
            assertTrue(returnedNames.contains(name), "Fly " + name + " should be in the list");
        }

        // another user's custom flies are private to them
        for (String name : otherUserNames) {
            assertFalse(returnedNames.contains(name), "Other user's fly " + name + " should not be in the list");
        }
    }

    /**
     * Crappy path: a user ID that doesn't exist only gets the default flies.
     */
    @Test
    public void testGetAllFliesCrappyPathMissingUser() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getAllFlies
        assertTrue(defaultAdded, "The default fly should be added");

        // a custom fly is saved so a lookup that ignored the user would return it and fail this test
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getAllFlies
        assertTrue(customAdded, "The custom fly should be added");

        List<Fly> flies = flyService.getAllFlies(missingUserId);
        assertNotNull(flies, "getAllFlies should never return null");

        // a user that doesn't exist owns nothing, so only the default fly is visible
        assertEquals(1, flies.size(), "A missing user should only get the default flies");
        assertEquals(DEFAULT_FLY_NAME, flies.get(0).getName(), "The only fly returned should be the default fly");
    }

    /**
     * Crazy path: a null user ID only gets the default flies.
     */
    @Test
    public void testGetAllFliesCrazyPathNullUser() {
        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getAllFlies
        assertTrue(defaultAdded, "The default fly should be added");

        // a custom fly is saved so a lookup that ignored the user would return it and fail this test
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getAllFlies
        assertTrue(customAdded, "The custom fly should be added");

        List<Fly> flies = flyService.getAllFlies(null);
        assertNotNull(flies, "getAllFlies should never return null");

        // the interface says a null user ID returns only the default flies
        assertEquals(1, flies.size(), "A null user should only get the default flies");
        assertEquals(DEFAULT_FLY_NAME, flies.get(0).getName(), "The only fly returned should be the default fly");
    }

    // ---------- getAllHooksetFlies ----------

    /**
     * Happy path: one default fly is returned.
     */
    @Test
    public void testGetAllHooksetFliesHappyPathOneFly() {
        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getAllHooksetFlies
        assertTrue(defaultAdded, "The default fly should be added");

        List<Fly> flies = flyService.getAllHooksetFlies();
        assertNotNull(flies, "getAllHooksetFlies should never return null");

        // the database starts empty, so the added fly should be the only one
        assertEquals(1, flies.size(), "The list should only have the one default fly");

        // only one fly exists, so position 0 is safe here
        Fly returnedFly = flies.get(0);

        // default flies are the ones with no owner
        assertNull(returnedFly.getUserId(), "A default fly should have no owner");
        assertEquals(DEFAULT_FLY_NAME, returnedFly.getName(), "The returned name should match the one added");
        assertEquals(TEST_FLY_TYPE, returnedFly.getType(), "The returned type should match the one added");
        assertEquals(TEST_FLY_COLOR, returnedFly.getColor(), "The returned color should match the one added");
        assertEquals(TEST_MIN_SIZE, returnedFly.getMinSize(), "The returned min size should match the one added");
        assertEquals(TEST_MAX_SIZE, returnedFly.getMaxSize(), "The returned max size should match the one added");
    }

    /**
     * Happy path: many default flies are all returned, and custom flies are left out.
     */
    @Test
    public void testGetAllHooksetFliesHappyPathManyFlies() {
        final int defaultCount = 100;

        // each fly gets its own name so they can be told apart, and so no add is rejected as a duplicate
        List<String> addedNames = new ArrayList<>();
        for (int i = 1; i <= defaultCount; i++) {
            String name = DEFAULT_FLY_NAME + " " + i;
            boolean defaultAdded = flyService.addDefaultHooksetFly(
                    new FlyInput(name, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

            // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getAllHooksetFlies
            assertTrue(defaultAdded, "Default fly " + name + " should be added");
            addedNames.add(name);
        }

        // a custom fly is saved so a lookup that returned every fly would fail this test
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getAllHooksetFlies
        assertTrue(customAdded, "The custom fly should be added");

        List<Fly> flies = flyService.getAllHooksetFlies();
        assertNotNull(flies, "getAllHooksetFlies should never return null");

        // only the default flies count, the custom fly belongs to a user
        assertEquals(defaultCount, flies.size(), "The list should have all " + defaultCount + " default flies");

        // checks by name instead of position, since the list order isn't guaranteed
        List<String> returnedNames = new ArrayList<>();
        for (Fly returnedFly : flies) {
            returnedNames.add(returnedFly.getName());
        }

        // the count alone could pass with the wrong flies, so check each one is actually there
        for (String name : addedNames) {
            assertTrue(returnedNames.contains(name), "Default fly " + name + " should be in the list");
        }

        // custom flies belong to a user, so they should never be in the default list
        assertFalse(returnedNames.contains(TEST_FLY_NAME), "A custom fly should not be in the default list");
    }

    /**
     * Crappy path: with no default flies added, an empty list is returned, even when custom flies exist.
     */
    @Test
    public void testGetAllHooksetFliesCrappyPathNoFlies() {
        // a custom fly is saved so a lookup that returned every fly would fail this test
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getAllHooksetFlies
        assertTrue(customAdded, "The custom fly should be added");

        List<Fly> flies = flyService.getAllHooksetFlies();

        // callers loop over this list, so null would crash them where an empty list wouldn't
        assertNotNull(flies, "getAllHooksetFlies should never return null");
        assertTrue(flies.isEmpty(), "The list should be empty when no default flies have been added");
    }

    /**
     * Crazy path: a very large number of default flies are all returned.
     */
    @Test
    public void testGetAllHooksetFliesCrazyPathThousandFlies() {
        final int defaultCount = 1000;

        for (int i = 1; i <= defaultCount; i++) {
            String name = DEFAULT_FLY_NAME + " " + i;
            boolean defaultAdded = flyService.addDefaultHooksetFly(
                    new FlyInput(name, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

            // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getAllHooksetFlies
            assertTrue(defaultAdded, "Default fly " + name + " should be added");
        }

        List<Fly> flies = flyService.getAllHooksetFlies();
        assertNotNull(flies, "getAllHooksetFlies should never return null");

        // a list this size shouldn't get cut off or lose any flies
        assertEquals(defaultCount, flies.size(), "The list should have all " + defaultCount + " default flies");
    }

    // ---------- getAllUserFlies ----------

    /**
     * Happy path: a user with no custom flies gets an empty list, even when default flies exist.
     */
    @Test
    public void testGetAllUserFliesHappyPathNoFlies() {
        // a default fly is saved so a lookup that returned default flies would fail this test
        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getAllUserFlies
        assertTrue(defaultAdded, "The default fly should be added");

        List<Fly> flies = flyService.getAllUserFlies(testUserId);

        // callers loop over this list, so null would crash them where an empty list wouldn't
        assertNotNull(flies, "getAllUserFlies should never return null");
        assertTrue(flies.isEmpty(), "A user with no custom flies should get an empty list");
    }

    /**
     * Happy path: a user's one custom fly is returned.
     */
    @Test
    public void testGetAllUserFliesHappyPathOneFly() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getAllUserFlies
        assertTrue(customAdded, "The custom fly should be added");

        List<Fly> flies = flyService.getAllUserFlies(testUserId);
        assertNotNull(flies, "getAllUserFlies should never return null");
        // only one custom fly was added for this user
        assertEquals(1, flies.size(), "The user should have exactly the one custom fly");

        // only one fly exists, so position 0 is safe here
        Fly returnedFly = flies.get(0);

        // every field is checked so a fly returned with the wrong details doesn't pass
        assertEquals(testUserId, returnedFly.getUserId(), "The returned fly should belong to the user");
        assertEquals(TEST_FLY_NAME, returnedFly.getName(), "The returned name should match the one added");
        assertEquals(TEST_FLY_TYPE, returnedFly.getType(), "The returned type should match the one added");
        assertEquals(TEST_FLY_COLOR, returnedFly.getColor(), "The returned color should match the one added");
        assertEquals(TEST_MIN_SIZE, returnedFly.getMinSize(), "The returned min size should match the one added");
        assertEquals(TEST_MAX_SIZE, returnedFly.getMaxSize(), "The returned max size should match the one added");
    }

    /**
     * Happy path: many of a user's custom flies are returned, and default flies and
     * another user's flies are left out.
     */
    @Test
    public void testGetAllUserFliesHappyPathManyFlies() {
        final int customCount = 100;

        List<String> addedNames = new ArrayList<>();
        for (int i = 1; i <= customCount; i++) {
            String name = TEST_FLY_NAME + " " + i;
            boolean customAdded = flyService.addFly(
                    new FlyInput(name, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

            // setup check, if this fails then addFly or the test data is broken, not getAllUserFlies
            assertTrue(customAdded, "Custom fly " + name + " should be added");
            addedNames.add(name);
        }

        // a default fly and another user's fly are saved so a lookup that ignored the owner would fail this test
        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getAllUserFlies
        assertTrue(defaultAdded, "The default fly should be added");

        boolean otherAdded = flyService.addFly(
                new FlyInput("Other Fly", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), otherUserId);

        // setup check, if this fails then addFly or the test data is broken, not getAllUserFlies
        assertTrue(otherAdded, "The other user's fly should be added");

        List<Fly> flies = flyService.getAllUserFlies(testUserId);
        assertNotNull(flies, "getAllUserFlies should never return null");
        // the default fly and the other user's fly shouldn't be counted
        assertEquals(customCount, flies.size(), "The user should have all " + customCount + " of their custom flies");

        // every returned fly has to belong to this user, not to nobody or someone else
        List<String> returnedNames = new ArrayList<>();
        for (Fly returnedFly : flies) {
            assertEquals(testUserId, returnedFly.getUserId(), "Fly " + returnedFly.getName() + " should belong to the user");
            returnedNames.add(returnedFly.getName());
        }

        // the count alone could pass with the wrong flies, so check each one is actually there
        for (String name : addedNames) {
            assertTrue(returnedNames.contains(name), "Custom fly " + name + " should be in the list");
        }
    }

    /**
     * Crappy path: a user ID that doesn't exist gets an empty list.
     */
    @Test
    public void testGetAllUserFliesCrappyPathMissingUser() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        // a custom fly is saved so a lookup that ignored the user would return it and fail this test
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getAllUserFlies
        assertTrue(customAdded, "The custom fly should be added");

        List<Fly> flies = flyService.getAllUserFlies(missingUserId);

        // a user that doesn't exist can't own any flies
        assertNotNull(flies, "getAllUserFlies should never return null");
        assertTrue(flies.isEmpty(), "A missing user should get an empty list");
    }

    /**
     * Crazy path: a null user ID gets an empty list, not the default flies.
     */
    @Test
    public void testGetAllUserFliesCrazyPathNullUser() {
        // default flies have no owner, so a null lookup that matched "no owner" would wrongly return this
        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getAllUserFlies
        assertTrue(defaultAdded, "The default fly should be added");

        List<Fly> flies = flyService.getAllUserFlies(null);

        // the interface says a null user ID returns an empty list
        assertNotNull(flies, "getAllUserFlies should never return null");
        assertTrue(flies.isEmpty(), "A null user should get an empty list, not the default flies");
    }

    // ---------- getFlyFromSlug ----------

    /**
     * Happy path: a user's custom fly and a default fly are both found by their slugs.
     */
    @Test
    public void testGetFlyFromSlugHappyPath() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyFromSlug
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getFlyFromSlug
        assertTrue(defaultAdded, "The default fly should be added");

        // the service makes the slug, so the saved flies are where the test gets them from
        Fly savedCustomFly = flyService.getAllUserFlies(testUserId).get(0);
        Fly savedDefaultFly = flyService.getAllHooksetFlies().get(0);

        // the owner looking up their own fly should find it
        Fly returnedCustomFly = flyService.getFlyFromSlug(savedCustomFly.getSlug(), testUserId);
        assertNotNull(returnedCustomFly, "The user should find their custom fly by its slug");
        assertEquals(savedCustomFly.getId(), returnedCustomFly.getId(), "The returned fly should be the user's custom fly");

        // default flies are visible to every user
        Fly returnedDefaultFly = flyService.getFlyFromSlug(savedDefaultFly.getSlug(), testUserId);
        assertNotNull(returnedDefaultFly, "The user should find a default fly by its slug");
        assertEquals(savedDefaultFly.getId(), returnedDefaultFly.getId(), "The returned fly should be the default fly");
    }

    /**
     * Crappy path: a slug no fly has returns null for a real user.
     */
    @Test
    public void testGetFlyFromSlugCrappyPathMissingSlugValidUser() {
        final String missingSlug = "not-a-real-fly";

        // a fly is saved so a lookup that ignored the slug would return it and fail this test
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyFromSlug
        assertTrue(customAdded, "The custom fly should be added");

        Fly returnedFly = flyService.getFlyFromSlug(missingSlug, testUserId);

        // no fly was saved with this slug, so nothing should come back
        assertNull(returnedFly, "A slug no fly has should return null");
    }

    /**
     * Crappy path: a real custom fly's slug returns null for anyone but its owner,
     * while a default fly's slug still works for them.
     */
    @Test
    public void testGetFlyFromSlugCrappyPathValidSlugInvalidUser() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyFromSlug
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getFlyFromSlug
        assertTrue(defaultAdded, "The default fly should be added");

        // the service makes the slug, so the saved flies are where the test gets them from
        final String customSlug = flyService.getAllUserFlies(testUserId).get(0).getSlug();
        final String defaultSlug = flyService.getAllHooksetFlies().get(0).getSlug();

        // other users' custom flies are never searched
        Fly otherUserResult = flyService.getFlyFromSlug(customSlug, otherUserId);
        assertNull(otherUserResult, "Another user should not find the custom fly by its slug");

        // a user that doesn't exist owns nothing, so they can't find a custom fly either
        Fly missingUserResult = flyService.getFlyFromSlug(customSlug, missingUserId);
        assertNull(missingUserResult, "A missing user should not find the custom fly by its slug");

        // default flies are checked first and visible to everyone, so the user doesn't matter
        Fly defaultResult = flyService.getFlyFromSlug(defaultSlug, missingUserId);
        assertNotNull(defaultResult, "A default fly should be found no matter who asks");
    }

    /**
     * Crappy path: a slug no fly has returns null for a user that doesn't exist.
     */
    @Test
    public void testGetFlyFromSlugCrappyPathInvalidBoth() {
        final String missingSlug = "not-a-real-fly";
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyFromSlug
        assertTrue(customAdded, "The custom fly should be added");

        Fly returnedFly = flyService.getFlyFromSlug(missingSlug, missingUserId);

        // neither the slug nor the user is real, so nothing can match
        assertNull(returnedFly, "A missing slug and a missing user should return null");
    }

    /**
     * Crazy path: a null slug returns null for a real user.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNullSlug() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyFromSlug
        assertTrue(customAdded, "The custom fly should be added");

        Fly returnedFly = flyService.getFlyFromSlug(null, testUserId);

        // every saved fly has a slug, so a null slug can't match any of them
        assertNull(returnedFly, "A null slug should return null");
    }

    /**
     * Crazy path: a null user can't find a custom fly by its slug, but can still find a default fly.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNullUser() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyFromSlug
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getFlyFromSlug
        assertTrue(defaultAdded, "The default fly should be added");

        // the service makes the slug, so the saved flies are where the test gets them from
        final String customSlug = flyService.getAllUserFlies(testUserId).get(0).getSlug();
        final String defaultSlug = flyService.getAllHooksetFlies().get(0).getSlug();

        // a null user owns nothing, so a custom fly can't match
        Fly customResult = flyService.getFlyFromSlug(customSlug, null);
        assertNull(customResult, "A null user should not find a custom fly");

        // default flies are visible to everyone, including a null user
        Fly defaultResult = flyService.getFlyFromSlug(defaultSlug, null);
        assertNotNull(defaultResult, "A null user should still find a default fly");
    }

    /**
     * Crazy path: a null slug and a null user returns null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNullBoth() {
        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getFlyFromSlug
        assertTrue(defaultAdded, "The default fly should be added");

        Fly returnedFly = flyService.getFlyFromSlug(null, null);

        // a default fly has a null owner, so this makes sure two nulls don't match it by accident
        assertNull(returnedFly, "A null slug and a null user should return null");
    }

    /**
     * Crazy path: a null slug with a user that doesn't exist returns null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNullSlugInvalidUser() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyFromSlug
        assertTrue(customAdded, "The custom fly should be added");

        Fly returnedFly = flyService.getFlyFromSlug(null, missingUserId);

        // nothing about this lookup is real, so nothing can match
        assertNull(returnedFly, "A null slug and a missing user should return null");
    }

    /**
     * Crazy path: a slug no fly has with a null user returns null.
     */
    @Test
    public void testGetFlyFromSlugCrazyPathNullUserInvalidSlug() {
        final String missingSlug = "not-a-real-fly";

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getFlyFromSlug
        assertTrue(defaultAdded, "The default fly should be added");

        Fly returnedFly = flyService.getFlyFromSlug(missingSlug, null);

        // the default fly is visible to a null user, but its slug doesn't match
        assertNull(returnedFly, "A missing slug and a null user should return null");
    }

    // ---------- getFlyById ----------

    /**
     * Happy path: a user loads their own custom fly and a default fly by ID.
     */
    @Test
    public void testGetFlyByIdHappyPath() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyById
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getFlyById
        assertTrue(defaultAdded, "The default fly should be added");

        // addFly doesn't return the fly, so the saved lists are where the test gets the IDs from
        final Long customFlyId = flyService.getAllUserFlies(testUserId).get(0).getId();
        final Long defaultFlyId = flyService.getAllHooksetFlies().get(0).getId();

        // the owner can always load their own fly
        Fly returnedCustomFly = flyService.getFlyById(customFlyId, testUserId);
        assertNotNull(returnedCustomFly, "The user should load their own custom fly");
        assertEquals(TEST_FLY_NAME, returnedCustomFly.getName(), "The returned fly should be the user's custom fly");

        // default flies can be loaded by anyone
        Fly returnedDefaultFly = flyService.getFlyById(defaultFlyId, testUserId);
        assertNotNull(returnedDefaultFly, "The user should load a default fly");
        assertEquals(DEFAULT_FLY_NAME, returnedDefaultFly.getName(), "The returned fly should be the default fly");
    }

    /**
     * Crappy path: an ID no fly has returns null for a real user.
     */
    @Test
    public void testGetFlyByIdCrappyPathMissingIdValidUser() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyById
        assertTrue(customAdded, "The custom fly should be added");

        // only one fly exists in this test, so the next ID can't belong to any fly
        final Long missingFlyId = flyService.getAllUserFlies(testUserId).get(0).getId() + 1;

        Fly returnedFly = flyService.getFlyById(missingFlyId, testUserId);

        // no fly has this ID, so nothing should come back
        assertNull(returnedFly, "An ID no fly has should return null");
    }

    /**
     * Crappy path: a custom fly can't be loaded by another user or a missing user,
     * but a default fly can.
     */
    @Test
    public void testGetFlyByIdCrappyPathValidIdInvalidUser() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyById
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getFlyById
        assertTrue(defaultAdded, "The default fly should be added");

        // the add methods don't return the fly, and there's only one of each kind, so position 0 is safe
        final Long customFlyId = flyService.getAllUserFlies(testUserId).get(0).getId();
        final Long defaultFlyId = flyService.getAllHooksetFlies().get(0).getId();

        // the interface says loading another user's fly throws instead of returning null
        assertThrows(AccessDeniedException.class,
                () -> flyService.getFlyById(customFlyId, otherUserId),
                "Another user loading the custom fly should be denied");

        // a user that doesn't exist doesn't own the fly either
        assertThrows(AccessDeniedException.class,
                () -> flyService.getFlyById(customFlyId, missingUserId),
                "A missing user loading the custom fly should be denied");

        // default flies can be loaded by anyone, so the user doesn't matter
        Fly returnedDefaultFly = flyService.getFlyById(defaultFlyId, missingUserId);
        assertNotNull(returnedDefaultFly, "A default fly should load no matter who asks");
    }

    /**
     * Crappy path: an ID no fly has returns null for a user that doesn't exist.
     */
    @Test
    public void testGetFlyByIdCrappyPathInvalidBoth() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyById
        assertTrue(customAdded, "The custom fly should be added");

        // only one fly exists in this test, so the next ID can't belong to any fly
        final Long missingFlyId = flyService.getAllUserFlies(testUserId).get(0).getId() + 1;

        Fly returnedFly = flyService.getFlyById(missingFlyId, missingUserId);

        // a missing fly is checked before ownership, so this is null instead of denied
        assertNull(returnedFly, "A missing fly and a missing user should return null");
    }

    /**
     * Crazy path: a null ID returns null for a real user.
     */
    @Test
    public void testGetFlyByIdCrazyPathNullIdValidUser() {
        Fly returnedFly = flyService.getFlyById(null, testUserId);

        // the interface says a null ID returns null instead of crashing
        assertNull(returnedFly, "A null fly ID should return null");
    }

    /**
     * Crazy path: a null user can't load a custom fly, but can load a default fly.
     */
    @Test
    public void testGetFlyByIdCrazyPathNullUserValidId() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyById
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not getFlyById
        assertTrue(defaultAdded, "The default fly should be added");

        // the add methods don't return the fly, and there's only one of each kind, so position 0 is safe
        final Long customFlyId = flyService.getAllUserFlies(testUserId).get(0).getId();
        final Long defaultFlyId = flyService.getAllHooksetFlies().get(0).getId();

        // a null user doesn't own the custom fly, so it's treated like another user
        assertThrows(AccessDeniedException.class,
                () -> flyService.getFlyById(customFlyId, null),
                "A null user loading a custom fly should be denied");

        // default flies can be loaded by anyone, including a null user
        Fly returnedDefaultFly = flyService.getFlyById(defaultFlyId, null);
        assertNotNull(returnedDefaultFly, "A null user should still load a default fly");
    }

    /**
     * Crazy path: a null ID and a null user returns null.
     */
    @Test
    public void testGetFlyByIdCrazyPathNullBoth() {
        Fly returnedFly = flyService.getFlyById(null, null);

        // a null ID is checked first, so this returns null without crashing
        assertNull(returnedFly, "A null ID and a null user should return null");
    }

    /**
     * Crazy path: a null ID with a user that doesn't exist returns null.
     */
    @Test
    public void testGetFlyByIdCrazyPathNullIdInvalidUser() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        Fly returnedFly = flyService.getFlyById(null, missingUserId);

        // a null ID is checked before the user, so this returns null without crashing
        assertNull(returnedFly, "A null ID and a missing user should return null");
    }

    /**
     * Crazy path: an ID no fly has with a null user returns null.
     */
    @Test
    public void testGetFlyByIdCrazyPathNullUserInvalidId() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not getFlyById
        assertTrue(customAdded, "The custom fly should be added");

        // only one fly exists in this test, so the next ID can't belong to any fly
        final Long missingFlyId = flyService.getAllUserFlies(testUserId).get(0).getId() + 1;

        Fly returnedFly = flyService.getFlyById(missingFlyId, null);

        // a missing fly is checked before ownership, so this is null instead of denied
        assertNull(returnedFly, "A missing fly and a null user should return null");
    }

    // ---------- addFly ----------

    /**
     * Happy path: a valid fly is saved for the user with every field it was given.
     */
    @Test
    public void testAddFlyHappyPath() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // valid input for a real user, so addFly should accept it
        assertTrue(flyAdded, "A valid fly should be added");

        // getAllUserFlies is the only way to see what was saved, since tests can't use repositories
        List<Fly> flies = flyService.getAllUserFlies(testUserId);
        // the database starts empty, so the added fly should be the only one
        assertEquals(1, flies.size(), "The added fly should be the user's only fly");

        // only one fly exists, so position 0 is safe here
        Fly savedFly = flies.get(0);

        // the database assigns the ID and the service sets the owner and slug
        assertNotNull(savedFly.getId(), "The saved fly should be given an ID");
        assertEquals(testUserId, savedFly.getUserId(), "The saved fly should belong to the user who added it");
        assertNotNull(savedFly.getSlug(), "The saved fly should be given a slug");

        // every field is checked so a fly saved with the wrong details doesn't pass
        assertEquals(TEST_FLY_NAME, savedFly.getName(), "The saved name should match the input");
        assertEquals(TEST_FLY_TYPE, savedFly.getType(), "The saved type should match the input");
        assertEquals(TEST_FLY_COLOR, savedFly.getColor(), "The saved color should match the input");
        assertEquals(TEST_MIN_SIZE, savedFly.getMinSize(), "The saved min size should match the input");
        assertEquals(TEST_MAX_SIZE, savedFly.getMaxSize(), "The saved max size should match the input");
    }

    /**
     * Happy path: two users can each have a fly with the same name.
     */
    @Test
    public void testAddFlyHappyPathSameNameAsOtherUser() {
        boolean otherUserFlyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), otherUserId);

        // setup check, if this fails then addFly or the test data is broken
        assertTrue(otherUserFlyAdded, "The other user's fly should be added");

        boolean testUserFlyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // other users' custom flies aren't checked for duplicates, so the same name is fine
        assertTrue(testUserFlyAdded, "A fly with the same name as another user's fly should be added");
        // each user should end up with their own separate fly, not one shared fly
        assertEquals(1, flyService.getAllUserFlies(testUserId).size(), "The test user should have their own copy");
        assertEquals(1, flyService.getAllUserFlies(otherUserId).size(), "The other user should keep their own fly");
    }

    /**
     * Happy path: values right at every limit are saved.
     */
    @Test
    public void testAddFlyHappyPathLimits() {
        // the longest name, the shortest and longest color, and the smallest and largest sizes allowed
        final String maxLengthName = "a".repeat(150);
        final String minLengthColor = "red";
        final String maxLengthColor = "b".repeat(40);
        final Integer smallestSize = 1;
        final Integer largestSize = 32;

        boolean maxNameAdded = flyService.addFly(
                new FlyInput(maxLengthName, TEST_FLY_TYPE, minLengthColor, smallestSize, largestSize), testUserId);
        // every value here is right at a limit, so an off-by-one mistake would reject it
        assertTrue(maxNameAdded, "A fly at every lower and upper limit should be added");

        // the same size for min and max just means the fly comes in one size
        boolean sameSizeAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, maxLengthColor, TEST_MIN_SIZE, TEST_MIN_SIZE), testUserId);
        assertTrue(sameSizeAdded, "A fly with the same min and max size should be added");

        // both adds returned true, so both flies should actually be saved
        assertEquals(2, flyService.getAllUserFlies(testUserId).size(), "Both flies at the limits should be saved");
    }

    /**
     * Crappy path: a blank or too long name is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrappyPathName() {
        final String blankName = "   ";

        // one character over the max name length
        final String tooLongName = "a".repeat(151);

        // each bad name is tried on its own so every check gets hit separately
        boolean blankAdded = flyService.addFly(
                new FlyInput(blankName, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(blankAdded, "A fly with a blank name should not be added");

        boolean tooLongAdded = flyService.addFly(
                new FlyInput(tooLongName, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(tooLongAdded, "A fly name over 150 characters should not be added");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(flyService.getAllUserFlies(testUserId).isEmpty(), "No rejected fly should be saved");
    }

    /**
     * Crappy path: a type that isn't exactly Dry, Nymph, or Streamer is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrappyPathType() {
        final String unknownType = "Wet";

        // the interface says the match is case-sensitive
        final String lowercaseType = "dry";
        final String blankType = "   ";

        // each bad type is tried on its own so every check gets hit separately
        boolean unknownAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, unknownType, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(unknownAdded, "A fly type that isn't allowed should not be added");

        boolean lowercaseAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, lowercaseType, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(lowercaseAdded, "A fly type in the wrong case should not be added");

        boolean blankAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, blankType, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(blankAdded, "A fly with a blank type should not be added");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(flyService.getAllUserFlies(testUserId).isEmpty(), "No rejected fly should be saved");
    }

    /**
     * Crappy path: a blank, too short, too long, or digits-only color is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrappyPathColor() {
        final String blankColor = "   ";

        // one character under the min and one over the max color length
        final String tooShortColor = "ab";
        final String tooLongColor = "a".repeat(41);
        final String digitsColor = "123";

        // each bad color is tried on its own so every check gets hit separately
        boolean blankAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, blankColor, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(blankAdded, "A fly with a blank color should not be added");

        boolean tooShortAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, tooShortColor, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(tooShortAdded, "A fly color under 3 characters should not be added");

        boolean tooLongAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, tooLongColor, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(tooLongAdded, "A fly color over 40 characters should not be added");

        boolean digitsAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, digitsColor, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(digitsAdded, "A fly color made of only digits should not be added");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(flyService.getAllUserFlies(testUserId).isEmpty(), "No rejected fly should be saved");
    }

    /**
     * Crappy path: sizes outside 1 to 32, or a min larger than the max, are rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrappyPathSizes() {
        // one under the smallest and one over the largest hook size
        final Integer tooSmallSize = 0;
        final Integer tooLargeSize = 33;

        // each bad size is tried on its own so every check gets hit separately
        boolean smallMinAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, tooSmallSize, TEST_MAX_SIZE), testUserId);
        assertFalse(smallMinAdded, "A min size under 1 should not be added");

        boolean largeMinAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, tooLargeSize, TEST_MAX_SIZE), testUserId);
        assertFalse(largeMinAdded, "A min size over 32 should not be added");

        boolean smallMaxAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, tooSmallSize), testUserId);
        assertFalse(smallMaxAdded, "A max size under 1 should not be added");

        boolean largeMaxAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, tooLargeSize), testUserId);
        assertFalse(largeMaxAdded, "A max size over 32 should not be added");

        // both sizes are valid on their own, so the backwards range is the only problem
        boolean backwardsAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MAX_SIZE, TEST_MIN_SIZE), testUserId);
        assertFalse(backwardsAdded, "A min size larger than the max size should not be added");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(flyService.getAllUserFlies(testUserId).isEmpty(), "No rejected fly should be saved");
    }

    /**
     * Crappy path: a name that matches one of the user's flies or a default fly is rejected,
     * even with different case or spacing.
     */
    @Test
    public void testAddFlyCrappyPathDuplicateName() {
        // same slug as "Test Fly", just typed differently
        final String differentCaseName = "TEST FLY";
        final String extraSpacesName = "  Test   Fly  ";

        boolean firstAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken
        assertTrue(firstAdded, "The first fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken
        assertTrue(defaultAdded, "The default fly should be added");

        // names are compared by slug, so the exact name, a different case, and extra spaces all collide
        boolean exactAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(exactAdded, "A fly with the same name as one of the user's flies should not be added");

        boolean caseAdded = flyService.addFly(
                new FlyInput(differentCaseName, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(caseAdded, "A name that only differs by case should not be added");

        boolean spacesAdded = flyService.addFly(
                new FlyInput(extraSpacesName, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(spacesAdded, "A name that only differs by spacing should not be added");

        // default flies are visible to every user, so their names are taken too
        boolean defaultNameAdded = flyService.addFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(defaultNameAdded, "A fly with the same name as a default fly should not be added");

        // every duplicate was rejected, so only the first fly should be saved
        assertEquals(1, flyService.getAllUserFlies(testUserId).size(), "Only the first fly should be saved");
    }

    /**
     * Crappy path: a fly for a user that doesn't exist is rejected and nothing is saved.
     */
    @Test
    public void testAddFlyCrappyPathMissingUser() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), missingUserId);

        // a fly has to belong to a real user
        assertFalse(flyAdded, "A fly for a user that doesn't exist should not be added");
        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(flyService.getAllUserFlies(missingUserId).isEmpty(), "No fly should be saved for the missing user");
    }

    /**
     * Crazy path: a null input, a null user, or both are rejected, and no default fly gets made by accident.
     */
    @Test
    public void testAddFlyCrazyPathNulls() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;
        final FlyInput validInput = new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE);

        // a blank name makes this input invalid
        final FlyInput invalidInput = new FlyInput("   ", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE);

        // the user is real, but there is nothing to save
        boolean nullInputAdded = flyService.addFly(null, testUserId);
        assertFalse(nullInputAdded, "A null input should not be added");

        // the input is valid, but a fly with no owner would turn into a default fly
        boolean nullUserAdded = flyService.addFly(validInput, null);
        assertFalse(nullUserAdded, "A fly with a null user should not be added");

        // nothing about this call is usable
        boolean nullBothAdded = flyService.addFly(null, null);
        assertFalse(nullBothAdded, "A null input and a null user should not be added");

        // two problems at once should still fail cleanly instead of crashing
        boolean nullInputMissingUserAdded = flyService.addFly(null, missingUserId);
        assertFalse(nullInputMissingUserAdded, "A null input for a missing user should not be added");

        // bad input and no owner together should still fail cleanly
        boolean nullUserInvalidInputAdded = flyService.addFly(invalidInput, null);
        assertFalse(nullUserInvalidInputAdded, "An invalid input with a null user should not be added");

        // nothing should be saved for the real user
        assertTrue(flyService.getAllUserFlies(testUserId).isEmpty(), "No rejected fly should be saved for the user");

        // a fly with no owner is a default fly, so a null user must never sneak one in
        assertTrue(flyService.getAllHooksetFlies().isEmpty(), "A null user should never create a default fly");
    }

    // ---------- addDefaultHooksetFly ----------

    /**
     * Happy path: a valid default fly is saved with no owner and every user can see it.
     */
    @Test
    public void testAddDefaultHooksetFlyHappyPath() {
        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // valid input, so it should be accepted
        assertTrue(defaultAdded, "A valid default fly should be added");

        // getAllHooksetFlies is the only way to see what was saved, since tests can't use repositories
        List<Fly> defaultFlies = flyService.getAllHooksetFlies();
        assertEquals(1, defaultFlies.size(), "The added fly should be the only default fly");

        // only one default fly exists, so position 0 is safe here
        Fly savedFly = defaultFlies.get(0);

        // a default fly is one with no owner
        assertNull(savedFly.getUserId(), "A default fly should have no owner");
        assertEquals(DEFAULT_FLY_NAME, savedFly.getName(), "The saved name should match the input");

        // default flies show up for every user
        assertEquals(1, flyService.getAllFlies(testUserId).size(), "The test user should see the default fly");
        assertEquals(1, flyService.getAllFlies(otherUserId).size(), "The other user should see the default fly");
    }

    /**
     * Crappy path: input that fails the fly checks is rejected and nothing is saved.
     */
    @Test
    public void testAddDefaultHooksetFlyCrappyPathInvalidInput() {

        // default flies run the same checks as custom flies, so each one is broken on its own
        boolean blankNameAdded = flyService.addDefaultHooksetFly(
                new FlyInput("   ", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));
        assertFalse(blankNameAdded, "A default fly with a blank name should not be added");

        boolean badTypeAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, "Wet", TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));
        assertFalse(badTypeAdded, "A default fly with a type that isn't allowed should not be added");

        boolean badColorAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, "123", TEST_MIN_SIZE, TEST_MAX_SIZE));
        assertFalse(badColorAdded, "A default fly with a digits-only color should not be added");

        boolean badSizesAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MAX_SIZE, TEST_MIN_SIZE));
        assertFalse(badSizesAdded, "A default fly with a min size larger than the max should not be added");

        // a rejected add returning false isn't enough, nothing should have been saved either
        assertTrue(flyService.getAllHooksetFlies().isEmpty(), "No rejected default fly should be saved");
    }

    /**
     * Crappy path: a name that matches an existing default fly or any user's custom fly is rejected.
     */
    @Test
    public void testAddDefaultHooksetFlyCrappyPathDuplicateName() {
        boolean firstDefaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken
        assertTrue(firstDefaultAdded, "The first default fly should be added");

        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), otherUserId);

        // setup check, if this fails then addFly or the test data is broken
        assertTrue(customAdded, "The other user's custom fly should be added");

        // names are compared by slug, so a different case still collides
        boolean duplicateDefaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput("HOOKSET FLY", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));
        assertFalse(duplicateDefaultAdded, "A default fly matching another default fly should not be added");

        // a default fly with a custom fly's slug would hide that user's fly, so it's blocked
        boolean matchesCustomAdded = flyService.addDefaultHooksetFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));
        assertFalse(matchesCustomAdded, "A default fly matching any user's custom fly should not be added");

        // both duplicates were rejected, so only the first default fly should be saved
        assertEquals(1, flyService.getAllHooksetFlies().size(), "Only the first default fly should be saved");
    }

    /**
     * Crazy path: a null input is rejected instead of crashing.
     */
    @Test
    public void testAddDefaultHooksetFlyCrazyPathNull() {
        boolean nullAdded = flyService.addDefaultHooksetFly(null);

        // there's nothing to save, so this should fail cleanly
        assertFalse(nullAdded, "A null input should not be added");
        assertTrue(flyService.getAllHooksetFlies().isEmpty(), "Nothing should be saved for a null input");
    }

    // ---------- updateFly ----------

    /**
     * Happy path: a user's fly is updated with new valid details and keeps its owner.
     */
    @Test
    public void testUpdateFlyHappyPath() {
        final String updatedName = "Updated Fly";
        final String updatedType = "Nymph";
        final String updatedColor = "black";
        final Integer updatedMinSize = 14;
        final Integer updatedMaxSize = 20;

        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateFly
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        boolean flyUpdated = flyService.updateFly(flyId,
                new FlyInput(updatedName, updatedType, updatedColor, updatedMinSize, updatedMaxSize), testUserId);

        // valid input on the user's own fly, so the update should be accepted
        assertTrue(flyUpdated, "A valid update to the user's own fly should be accepted");

        // the fly is loaded again so the test checks what was saved, not what was sent
        Fly updatedFly = flyService.getFlyById(flyId, testUserId);
        assertNotNull(updatedFly, "The updated fly should still exist");

        // every field is checked so an update that only saved some fields doesn't pass
        assertEquals(updatedName, updatedFly.getName(), "The name should be updated");
        assertEquals(updatedType, updatedFly.getType(), "The type should be updated");
        assertEquals(updatedColor, updatedFly.getColor(), "The color should be updated");
        assertEquals(updatedMinSize, updatedFly.getMinSize(), "The min size should be updated");
        assertEquals(updatedMaxSize, updatedFly.getMaxSize(), "The max size should be updated");

        // the interface says the owner is never changed by an edit
        assertEquals(testUserId, updatedFly.getUserId(), "The fly should still belong to the same user");
    }

    /**
     * Happy path: a fly can keep its own name while other details change.
     */
    @Test
    public void testUpdateFlyHappyPathSameName() {
        final String updatedColor = "black";

        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateFly
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        boolean flyUpdated = flyService.updateFly(flyId,
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, updatedColor, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // a fly keeping its own name isn't a duplicate of itself
        assertTrue(flyUpdated, "An update that keeps the fly's own name should be accepted");
        // the other change still has to be saved
        assertEquals(updatedColor, flyService.getFlyById(flyId, testUserId).getColor(), "The color should be updated");
    }

    /**
     * Crappy path: updating a fly ID that doesn't exist is rejected.
     */
    @Test
    public void testUpdateFlyCrappyPathMissingFly() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateFly
        assertTrue(flyAdded, "The test fly should be added");

        // only one fly exists in this test, so the next ID can't belong to any fly
        final Long missingFlyId = flyService.getAllUserFlies(testUserId).get(0).getId() + 1;

        boolean flyUpdated = flyService.updateFly(missingFlyId,
                new FlyInput("Updated Fly", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // there's no fly with this ID, so there's nothing to update
        assertFalse(flyUpdated, "Updating a fly that doesn't exist should be rejected");
    }

    /**
     * Crappy path: every kind of bad input is rejected and the fly is left unchanged.
     */
    @Test
    public void testUpdateFlyCrappyPathInvalidInput() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateFly
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        // each field is broken on its own so every check gets hit separately
        boolean blankNameUpdated = flyService.updateFly(flyId,
                new FlyInput("   ", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(blankNameUpdated, "An update with a blank name should be rejected");

        boolean longNameUpdated = flyService.updateFly(flyId,
                new FlyInput("a".repeat(151), TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(longNameUpdated, "An update with a name over 150 characters should be rejected");

        boolean badTypeUpdated = flyService.updateFly(flyId,
                new FlyInput(TEST_FLY_NAME, "dry", TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(badTypeUpdated, "An update with a type in the wrong case should be rejected");

        boolean shortColorUpdated = flyService.updateFly(flyId,
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, "ab", TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(shortColorUpdated, "An update with a color under 3 characters should be rejected");

        boolean longColorUpdated = flyService.updateFly(flyId,
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, "a".repeat(41), TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(longColorUpdated, "An update with a color over 40 characters should be rejected");

        boolean digitsColorUpdated = flyService.updateFly(flyId,
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, "123", TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(digitsColorUpdated, "An update with a digits-only color should be rejected");

        boolean smallSizeUpdated = flyService.updateFly(flyId,
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, 0, TEST_MAX_SIZE), testUserId);
        assertFalse(smallSizeUpdated, "An update with a min size under 1 should be rejected");

        boolean largeSizeUpdated = flyService.updateFly(flyId,
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, 33), testUserId);
        assertFalse(largeSizeUpdated, "An update with a max size over 32 should be rejected");

        boolean backwardsUpdated = flyService.updateFly(flyId,
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MAX_SIZE, TEST_MIN_SIZE), testUserId);
        assertFalse(backwardsUpdated, "An update with a min size larger than the max should be rejected");

        // a rejected update returning false isn't enough, the saved fly shouldn't have changed either
        Fly unchangedFly = flyService.getFlyById(flyId, testUserId);
        assertEquals(TEST_FLY_NAME, unchangedFly.getName(), "The name should not change");
        assertEquals(TEST_FLY_TYPE, unchangedFly.getType(), "The type should not change");
        assertEquals(TEST_FLY_COLOR, unchangedFly.getColor(), "The color should not change");
        assertEquals(TEST_MIN_SIZE, unchangedFly.getMinSize(), "The min size should not change");
        assertEquals(TEST_MAX_SIZE, unchangedFly.getMaxSize(), "The max size should not change");
    }

    /**
     * Crappy path: renaming a fly to match another of the user's flies or a default fly is rejected.
     */
    @Test
    public void testUpdateFlyCrappyPathDuplicateName() {
        final String secondFlyName = "Second Fly";

        boolean firstAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateFly
        assertTrue(firstAdded, "The first fly should be added");

        boolean secondAdded = flyService.addFly(
                new FlyInput(secondFlyName, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateFly
        assertTrue(secondAdded, "The second fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not updateFly
        assertTrue(defaultAdded, "The default fly should be added");

        // the first fly is found by name, since list order isn't guaranteed
        Long firstFlyId = null;
        for (Fly savedFly : flyService.getAllUserFlies(testUserId)) {
            if (savedFly.getName().equals(TEST_FLY_NAME)) {
                firstFlyId = savedFly.getId();
            }
        }
        assertNotNull(firstFlyId, "The first fly should be found in the user's flies");

        // names are compared by slug, so a different case still collides with the second fly
        boolean ownDuplicateUpdated = flyService.updateFly(firstFlyId,
                new FlyInput("SECOND FLY", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(ownDuplicateUpdated, "Renaming to match another of the user's flies should be rejected");

        boolean defaultDuplicateUpdated = flyService.updateFly(firstFlyId,
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);
        assertFalse(defaultDuplicateUpdated, "Renaming to match a default fly should be rejected");

        // both renames were rejected, so the fly should keep its original name
        assertEquals(TEST_FLY_NAME, flyService.getFlyById(firstFlyId, testUserId).getName(),
                "The fly's name should not change");
    }

    /**
     * Crappy path: another user, a missing user, or anyone editing a default fly is denied.
     */
    @Test
    public void testUpdateFlyCrappyPathNotOwner() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;
        final FlyInput validInput = new FlyInput("Updated Fly", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE);

        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateFly
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not updateFly
        assertTrue(defaultAdded, "The default fly should be added");

        // the add methods don't return the fly, and there's only one of each kind, so position 0 is safe
        final Long customFlyId = flyService.getAllUserFlies(testUserId).get(0).getId();
        final Long defaultFlyId = flyService.getAllHooksetFlies().get(0).getId();

        // the interface says editing someone else's fly throws instead of returning false
        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(customFlyId, validInput, otherUserId),
                "Another user editing the fly should be denied");

        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(customFlyId, validInput, missingUserId),
                "A missing user editing the fly should be denied");

        // default flies can never be edited, even though everyone can see them
        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(defaultFlyId, validInput, testUserId),
                "Editing a default fly should be denied");

        // a denied edit shouldn't change anything
        assertEquals(TEST_FLY_NAME, flyService.getFlyById(customFlyId, testUserId).getName(),
                "The custom fly's name should not change");
    }

    /**
     * Crappy path: a missing fly, bad input, and a missing user all at once is rejected.
     */
    @Test
    public void testUpdateFlyCrappyPathInvalidAll() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateFly
        assertTrue(flyAdded, "The test fly should be added");

        // only one fly exists in this test, so the next ID can't belong to any fly
        final Long missingFlyId = flyService.getAllUserFlies(testUserId).get(0).getId() + 1;

        boolean flyUpdated = flyService.updateFly(missingFlyId,
                new FlyInput("   ", "Wet", "123", 0, 33), missingUserId);

        // a missing fly is checked first, so this is a clean false instead of denied
        assertFalse(flyUpdated, "Updating a missing fly with bad input for a missing user should be rejected");
    }

    /**
     * Crazy path: a null fly ID is rejected, whether the input is valid, invalid, or null.
     */
    @Test
    public void testUpdateFlyCrazyPathNullFlyId() {
        final FlyInput validInput = new FlyInput("Updated Fly", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE);
        final FlyInput invalidInput = new FlyInput("   ", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE);

        // a null ID is checked first, so none of these crash or get as far as ownership
        assertFalse(flyService.updateFly(null, validInput, testUserId), "A null fly ID should be rejected");
        assertFalse(flyService.updateFly(null, invalidInput, testUserId),
                "A null fly ID with invalid input should be rejected");
        assertFalse(flyService.updateFly(null, null, null), "A null fly ID, input, and user should be rejected");
    }

    /**
     * Crazy path: a null input on the user's own fly is rejected and the fly is left unchanged.
     */
    @Test
    public void testUpdateFlyCrazyPathNullInput() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateFly
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        boolean flyUpdated = flyService.updateFly(flyId, null, testUserId);

        // the owner is right, but there's nothing to update the fly with
        assertFalse(flyUpdated, "A null input should be rejected");
        // a rejected update returning false isn't enough, the fly shouldn't have changed either
        assertEquals(TEST_FLY_NAME, flyService.getFlyById(flyId, testUserId).getName(), "The fly should not change");
    }

    /**
     * Crazy path: a null user editing a real fly is denied, even when the input is also null or missing.
     */
    @Test
    public void testUpdateFlyCrazyPathNullUser() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;
        final FlyInput validInput = new FlyInput("Updated Fly", TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE);

        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not updateFly
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        // a null user doesn't own the fly, so it's treated like another user
        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(flyId, validInput, null),
                "A null user editing the fly should be denied");

        // ownership is checked before the input, so a null input doesn't change the outcome
        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(flyId, null, null),
                "A null user with a null input should still be denied");

        assertThrows(AccessDeniedException.class,
                () -> flyService.updateFly(flyId, null, missingUserId),
                "A missing user with a null input should still be denied");

        // none of the denied edits should change the fly
        assertEquals(TEST_FLY_NAME, flyService.getFlyById(flyId, testUserId).getName(), "The fly should not change");
    }

    // ---------- deleteFly ----------

    /**
     * Happy path: a user's own fly that no catch uses is deleted.
     */
    @Test
    public void testDeleteFlyHappyPath() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not deleteFly
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        boolean flyDeleted = flyService.deleteFly(flyId, testUserId);

        // the user owns the fly and nothing uses it, so it should be deleted
        assertTrue(flyDeleted, "The user's unused fly should be deleted");

        // returning true isn't enough, the fly should actually be gone
        assertNull(flyService.getFlyById(flyId, testUserId), "The deleted fly should no longer be found");
        assertTrue(flyService.getAllUserFlies(testUserId).isEmpty(), "The user should have no flies left");
    }

    /**
     * Crappy path: deleting a fly ID that doesn't exist is rejected.
     */
    @Test
    public void testDeleteFlyCrappyPathMissingFly() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not deleteFly
        assertTrue(flyAdded, "The test fly should be added");

        // only one fly exists in this test, so the next ID can't belong to any fly
        final Long missingFlyId = flyService.getAllUserFlies(testUserId).get(0).getId() + 1;

        boolean flyDeleted = flyService.deleteFly(missingFlyId, testUserId);

        // there's no fly with this ID, so there's nothing to delete
        assertFalse(flyDeleted, "Deleting a fly that doesn't exist should be rejected");
        // a failed delete shouldn't remove some other fly by mistake
        assertEquals(1, flyService.getAllUserFlies(testUserId).size(), "The real fly should not be deleted");
    }

    /**
     * Crappy path: another user, a missing user, or anyone deleting a default fly is denied.
     */
    @Test
    public void testDeleteFlyCrappyPathNotOwner() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not deleteFly
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not deleteFly
        assertTrue(defaultAdded, "The default fly should be added");

        // the add methods don't return the fly, and there's only one of each kind, so position 0 is safe
        final Long customFlyId = flyService.getAllUserFlies(testUserId).get(0).getId();
        final Long defaultFlyId = flyService.getAllHooksetFlies().get(0).getId();

        // the interface says deleting someone else's fly throws instead of returning false
        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(customFlyId, otherUserId),
                "Another user deleting the fly should be denied");

        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(customFlyId, missingUserId),
                "A missing user deleting the fly should be denied");

        // default flies can never be deleted
        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(defaultFlyId, testUserId),
                "Deleting a default fly should be denied");

        // a denied delete shouldn't remove anything
        assertEquals(1, flyService.getAllUserFlies(testUserId).size(), "The custom fly should still exist");
        assertEquals(1, flyService.getAllHooksetFlies().size(), "The default fly should still exist");
    }

    /**
     * Crappy path: a fly that a catch uses can't be deleted.
     */
    @Test
    public void testDeleteFlyCrappyPathUsedByCatch() {
        boolean fishAdded = fishService.addFish(new FishInput("test-family", "test-species", "test-image"));

        // setup check, if this fails then addFish or the test data is broken, not deleteFly
        assertTrue(fishAdded, "The test fish should be added");

        Fish savedFish = fishService.getAllFish().get(0);

        // the fish has to be in the water body, or the catch won't be accepted
        boolean waterBodyAdded = waterBodyService.addWaterBody(new WaterBodyInput("Test River", "River", "Montana",
                "A river used for testing", "test-river", List.of(savedFish.getId())));

        // setup check, if this fails then addWaterBody or the test data is broken, not deleteFly
        assertTrue(waterBodyAdded, "The test water body should be added");

        WaterBody savedWaterBody = waterBodyService.getWaterBodyFromSlug("test-river");

        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not deleteFly
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        // caught yesterday, since a catch can't be in the future
        boolean catchAdded = catchService.addCatch(new CatchInput(flyId, savedFish.getId(), savedWaterBody.getId(),
                new BigDecimal("12.50"), LocalDateTime.now().minusDays(1), "Caught on the test fly"), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not deleteFly
        assertTrue(catchAdded, "The test catch should be added");

        boolean flyDeleted = flyService.deleteFly(flyId, testUserId);

        // deleting it would leave the catch pointing at a fly that doesn't exist
        assertFalse(flyDeleted, "A fly used by a catch should not be deleted");
        // the fly has to still be there so the catch keeps pointing at a real fly
        assertNotNull(flyService.getFlyById(flyId, testUserId), "The fly should still exist");
    }

    /**
     * Crappy path: a missing fly and a missing user together is rejected.
     */
    @Test
    public void testDeleteFlyCrappyPathInvalidBoth() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not deleteFly
        assertTrue(flyAdded, "The test fly should be added");

        // only one fly exists in this test, so the next ID can't belong to any fly
        final Long missingFlyId = flyService.getAllUserFlies(testUserId).get(0).getId() + 1;

        boolean flyDeleted = flyService.deleteFly(missingFlyId, missingUserId);

        // a missing fly is checked first, so this is a clean false instead of denied
        assertFalse(flyDeleted, "Deleting a missing fly for a missing user should be rejected");
        // the real fly should be untouched
        assertEquals(1, flyService.getAllUserFlies(testUserId).size(), "The real fly should not be deleted");
    }

    /**
     * Crazy path: a null fly ID is rejected, a null user is denied, and both null is rejected.
     */
    @Test
    public void testDeleteFlyCrazyPathNulls() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not deleteFly
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        // a null ID is checked first, so it's a clean false
        assertFalse(flyService.deleteFly(null, testUserId), "A null fly ID should be rejected");
        assertFalse(flyService.deleteFly(null, null), "A null fly ID and a null user should be rejected");

        // a null user doesn't own the fly, so it's treated like another user
        assertThrows(AccessDeniedException.class,
                () -> flyService.deleteFly(flyId, null),
                "A null user deleting the fly should be denied");

        // none of the rejected or denied deletes should remove the fly
        assertEquals(1, flyService.getAllUserFlies(testUserId).size(), "The fly should not be deleted");
    }

    // ---------- isFlyUsedByCatch ----------

    /**
     * Happy path: a fly that a catch was logged with is used.
     */
    @Test
    public void testIsFlyUsedByCatchHappyPath() {
        boolean fishAdded = fishService.addFish(new FishInput("test-family", "test-species", "test-image"));

        // setup check, if this fails then addFish or the test data is broken, not isFlyUsedByCatch
        assertTrue(fishAdded, "The test fish should be added");

        Fish savedFish = fishService.getAllFish().get(0);

        // the fish has to be in the water body, or the catch won't be accepted
        boolean waterBodyAdded = waterBodyService.addWaterBody(new WaterBodyInput("Test River", "River", "Montana",
                "A river used for testing", "test-river", List.of(savedFish.getId())));

        // setup check, if this fails then addWaterBody or the test data is broken, not isFlyUsedByCatch
        assertTrue(waterBodyAdded, "The test water body should be added");

        WaterBody savedWaterBody = waterBodyService.getWaterBodyFromSlug("test-river");

        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyUsedByCatch
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        // caught yesterday, since a catch can't be in the future
        boolean catchAdded = catchService.addCatch(new CatchInput(flyId, savedFish.getId(), savedWaterBody.getId(),
                new BigDecimal("12.50"), LocalDateTime.now().minusDays(1), "Caught on the test fly"), testUserId);

        // setup check, if this fails then addCatch or the test data is broken, not isFlyUsedByCatch
        assertTrue(catchAdded, "The test catch should be added");

        boolean flyUsed = flyService.isFlyUsedByCatch(flyId);

        // a catch was logged with this fly, so it's in use
        assertTrue(flyUsed, "A fly a catch was logged with should be used");
    }

    /**
     * Crappy path: a fly no catch uses, or a fly ID that doesn't exist, is not used.
     */
    @Test
    public void testIsFlyUsedByCatchCrappyPath() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyUsedByCatch
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();
        final Long missingFlyId = flyId + 1;

        // the fly exists but no catch was ever logged with it
        boolean unusedFlyUsed = flyService.isFlyUsedByCatch(flyId);
        assertFalse(unusedFlyUsed, "A fly no catch uses should not be used");

        // a fly that doesn't exist can't be used by anything
        boolean missingFlyUsed = flyService.isFlyUsedByCatch(missingFlyId);
        assertFalse(missingFlyUsed, "A fly ID that doesn't exist should not be used");
    }

    /**
     * Crazy path: a null fly ID is not used instead of crashing.
     */
    @Test
    public void testIsFlyUsedByCatchCrazyPathNull() {
        boolean flyUsed = flyService.isFlyUsedByCatch(null);

        // the interface says a null ID returns false
        assertFalse(flyUsed, "A null fly ID should not be used");
    }

    // ---------- isSizeRangeValid ----------

    /**
     * Happy path: a min smaller than or equal to the max is a valid range.
     */
    @Test
    public void testIsSizeRangeValidHappyPath() {
        // the test sizes are a normal range, smaller min and larger max
        boolean normalRangeValid = flyService.isSizeRangeValid(TEST_MIN_SIZE, TEST_MAX_SIZE);
        assertTrue(normalRangeValid, "A min smaller than the max should be valid");

        // equal sizes just mean the fly comes in one size
        boolean sameSizeValid = flyService.isSizeRangeValid(TEST_MIN_SIZE, TEST_MIN_SIZE);
        assertTrue(sameSizeValid, "The same min and max should be valid");
    }

    /**
     * Crappy path: a min larger than the max is not a valid range, even by one.
     */
    @Test
    public void testIsSizeRangeValidCrappyPath() {
        // the test sizes swapped around are the obvious backwards range
        boolean backwardsValid = flyService.isSizeRangeValid(TEST_MAX_SIZE, TEST_MIN_SIZE);
        assertFalse(backwardsValid, "A min larger than the max should not be valid");

        // one apart is where an off-by-one mistake would wrongly allow it
        boolean offByOneValid = flyService.isSizeRangeValid(TEST_MIN_SIZE + 1, TEST_MIN_SIZE);
        assertFalse(offByOneValid, "A min one larger than the max should not be valid");
    }

    /**
     * Crazy path: a missing size leaves nothing to compare, so the range counts as valid.
     */
    @Test
    public void testIsSizeRangeValidCrazyPathNulls() {
        // the interface says missing sizes are caught by the other size checks, not this one
        assertTrue(flyService.isSizeRangeValid(null, TEST_MAX_SIZE), "A null min should count as valid");
        assertTrue(flyService.isSizeRangeValid(TEST_MIN_SIZE, null), "A null max should count as valid");
        assertTrue(flyService.isSizeRangeValid(null, null), "Both null should count as valid");
    }

    // ---------- isFlyNameUnique ----------

    /**
     * Happy path: a new name, or a name only another user has, is unique.
     */
    @Test
    public void testIsFlyNameUniqueHappyPath() {
        final String otherUserFlyName = "Other Fly";

        boolean otherAdded = flyService.addFly(
                new FlyInput(otherUserFlyName, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), otherUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyNameUnique
        assertTrue(otherAdded, "The other user's fly should be added");

        // no fly with this name exists for the user or as a default
        boolean newNameUnique = flyService.isFlyNameUnique(TEST_FLY_NAME, testUserId);
        assertTrue(newNameUnique, "A name no visible fly has should be unique");

        // other users' custom flies aren't checked, so their names are still free
        boolean otherUserNameUnique = flyService.isFlyNameUnique(otherUserFlyName, testUserId);
        assertTrue(otherUserNameUnique, "A name only another user has should be unique");
    }

    /**
     * Crappy path: a name matching one of the user's flies or a default fly, or a blank name, isn't unique.
     */
    @Test
    public void testIsFlyNameUniqueCrappyPath() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyNameUnique
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not isFlyNameUnique
        assertTrue(defaultAdded, "The default fly should be added");

        // the exact name of one of the user's flies is the obvious duplicate
        assertFalse(flyService.isFlyNameUnique(TEST_FLY_NAME, testUserId),
                "A name matching one of the user's flies should not be unique");

        // names are compared by slug, so case and spacing don't make a name different
        assertFalse(flyService.isFlyNameUnique("test fly", testUserId),
                "A name that only differs by case should not be unique");
        assertFalse(flyService.isFlyNameUnique("  Test   Fly ", testUserId),
                "A name that only differs by spacing should not be unique");

        // default flies are visible to every user, so their names are taken
        assertFalse(flyService.isFlyNameUnique(DEFAULT_FLY_NAME, testUserId),
                "A name matching a default fly should not be unique");

        // the interface says a blank name returns false
        assertFalse(flyService.isFlyNameUnique("   ", testUserId), "A blank name should not be unique");
    }

    /**
     * Crazy path: a null name isn't unique, and a null user is only checked against default flies.
     */
    @Test
    public void testIsFlyNameUniqueCrazyPathNulls() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyNameUnique
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not isFlyNameUnique
        assertTrue(defaultAdded, "The default fly should be added");

        // a null name can't be checked, so it should fail cleanly instead of crashing
        assertFalse(flyService.isFlyNameUnique(null, testUserId), "A null name should not be unique");
        assertFalse(flyService.isFlyNameUnique(null, null), "A null name with a null user should not be unique");

        // a null user owns nothing, so only default flies count against it
        assertFalse(flyService.isFlyNameUnique(DEFAULT_FLY_NAME, null),
                "A default fly's name should not be unique for a null user");
        assertTrue(flyService.isFlyNameUnique(TEST_FLY_NAME, null),
                "A custom fly's name should still be unique for a null user");
    }

    // ---------- isFlyNameUniqueForEdit ----------

    /**
     * Happy path: a fly keeping its own name, in any case, or taking a brand new name is unique.
     */
    @Test
    public void testIsFlyNameUniqueForEditHappyPath() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyNameUniqueForEdit
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        // a fly keeping its own name isn't a duplicate of itself
        assertTrue(flyService.isFlyNameUniqueForEdit(flyId, TEST_FLY_NAME, testUserId),
                "A fly keeping its own name should be unique");

        // same slug as its own name, so it's still the fly keeping its name
        assertTrue(flyService.isFlyNameUniqueForEdit(flyId, "TEST FLY", testUserId),
                "A fly keeping its own name in a different case should be unique");

        // a name no other fly has is always fine
        assertTrue(flyService.isFlyNameUniqueForEdit(flyId, "Brand New Fly", testUserId),
                "A brand new name should be unique");
    }

    /**
     * Crappy path: a name taken by another of the user's flies or a default fly, a missing fly,
     * or a blank name isn't unique.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrappyPath() {
        final String secondFlyName = "Second Fly";

        boolean firstAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyNameUniqueForEdit
        assertTrue(firstAdded, "The first fly should be added");

        boolean secondAdded = flyService.addFly(
                new FlyInput(secondFlyName, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyNameUniqueForEdit
        assertTrue(secondAdded, "The second fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not isFlyNameUniqueForEdit
        assertTrue(defaultAdded, "The default fly should be added");

        // the first fly is found by name, and the largest ID tells the test which ID is free
        Long firstFlyId = null;
        Long largestFlyId = 0L;
        for (Fly savedFly : flyService.getAllFlies(testUserId)) {
            if (savedFly.getName().equals(TEST_FLY_NAME)) {
                firstFlyId = savedFly.getId();
            }
            if (savedFly.getId() > largestFlyId) {
                largestFlyId = savedFly.getId();
            }
        }
        assertNotNull(firstFlyId, "The first fly should be found");

        final Long missingFlyId = largestFlyId + 1;

        // the user's second fly already has this name
        assertFalse(flyService.isFlyNameUniqueForEdit(firstFlyId, secondFlyName, testUserId),
                "A name another of the user's flies has should not be unique");

        // default flies are visible to every user, so their names are taken
        assertFalse(flyService.isFlyNameUniqueForEdit(firstFlyId, DEFAULT_FLY_NAME, testUserId),
                "A name a default fly has should not be unique");

        // the interface says a fly that doesn't exist returns false
        assertFalse(flyService.isFlyNameUniqueForEdit(missingFlyId, "Brand New Fly", testUserId),
                "A fly that doesn't exist should not count as unique");

        // the interface says a blank name returns false
        assertFalse(flyService.isFlyNameUniqueForEdit(firstFlyId, "   ", testUserId),
                "A blank name should not be unique");
    }

    /**
     * Crazy path: a null fly ID or a null name isn't unique instead of crashing.
     */
    @Test
    public void testIsFlyNameUniqueForEditCrazyPathNulls() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyNameUniqueForEdit
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        // missing pieces should fail cleanly instead of crashing
        assertFalse(flyService.isFlyNameUniqueForEdit(null, "Brand New Fly", testUserId),
                "A null fly ID should not be unique");
        assertFalse(flyService.isFlyNameUniqueForEdit(flyId, null, testUserId), "A null name should not be unique");
        assertFalse(flyService.isFlyNameUniqueForEdit(null, null, null), "All null should not be unique");
    }

    // ---------- isFlyNameBlank ----------

    /**
     * Happy path: a normal fly name is not blank.
     */
    @Test
    public void testIsFlyNameBlankHappyPath() {
        boolean nameBlank = flyService.isFlyNameBlank(TEST_FLY_NAME);

        // the name has real characters in it, so it shouldn't count as blank
        assertFalse(nameBlank, "A normal fly name should not be blank");
    }

    /**
     * Crappy path: an empty or whitespace-only fly name is blank.
     */
    @Test
    public void testIsFlyNameBlankCrappyPath() {
        // nothing at all is the simplest blank
        assertTrue(flyService.isFlyNameBlank(""), "An empty fly name should be blank");

        // only spaces looks like something was typed, but there's still no name
        assertTrue(flyService.isFlyNameBlank("   "), "A fly name of only spaces should be blank");
    }

    /**
     * Crazy path: a null fly name is blank.
     */
    @Test
    public void testIsFlyNameBlankCrazyPathNull() {
        boolean nameBlank = flyService.isFlyNameBlank(null);

        // a missing name is the most blank a name can be
        assertTrue(nameBlank, "A null fly name should be blank");
    }

    // ---------- isFlyColorBlank ----------

    /**
     * Happy path: a normal fly color is not blank.
     */
    @Test
    public void testIsFlyColorBlankHappyPath() {
        boolean colorBlank = flyService.isFlyColorBlank(TEST_FLY_COLOR);

        // the color has real characters in it, so it shouldn't count as blank
        assertFalse(colorBlank, "A normal fly color should not be blank");
    }

    /**
     * Crappy path: an empty or whitespace-only fly color is blank.
     */
    @Test
    public void testIsFlyColorBlankCrappyPath() {
        // nothing at all is the simplest blank
        assertTrue(flyService.isFlyColorBlank(""), "An empty fly color should be blank");

        // only spaces looks like something was typed, but there's still no color
        assertTrue(flyService.isFlyColorBlank("   "), "A fly color of only spaces should be blank");
    }

    /**
     * Crazy path: a null fly color is blank.
     */
    @Test
    public void testIsFlyColorBlankCrazyPathNull() {
        boolean colorBlank = flyService.isFlyColorBlank(null);

        // the interface says null counts as blank
        assertTrue(colorBlank, "A null fly color should be blank");
    }

    // ---------- isFlyColorLengthValid ----------

    /**
     * Happy path: a color right at 3 or 40 characters is valid, and spaces around it don't count.
     */
    @Test
    public void testIsFlyColorLengthValidHappyPath() {
        // exactly at the limits, which are still allowed
        final String minLengthColor = "red";
        final String maxLengthColor = "a".repeat(40);

        // 3 characters once the spaces around it are trimmed
        final String paddedColor = "  red  ";

        // each of these is right at a limit, or only passes once its spaces are trimmed
        assertTrue(flyService.isFlyColorLengthValid(minLengthColor), "A color of exactly 3 characters should be valid");
        assertTrue(flyService.isFlyColorLengthValid(maxLengthColor), "A color of exactly 40 characters should be valid");
        assertTrue(flyService.isFlyColorLengthValid(paddedColor), "Spaces around a color should not count toward its length");
    }

    /**
     * Crappy path: a color one under 3 or one over 40 characters is not valid,
     * including when spaces make it look longer.
     */
    @Test
    public void testIsFlyColorLengthValidCrappyPath() {
        // one under and one over the limits
        final String tooShortColor = "ab";
        final String tooLongColor = "a".repeat(41);

        // looks like 6 characters, but only 2 once trimmed
        final String paddedShortColor = "  ab  ";

        // each of these misses a limit by one character once trimmed
        assertFalse(flyService.isFlyColorLengthValid(tooShortColor), "A color of 2 characters should not be valid");
        assertFalse(flyService.isFlyColorLengthValid(tooLongColor), "A color of 41 characters should not be valid");
        assertFalse(flyService.isFlyColorLengthValid(paddedShortColor),
                "Spaces around a short color should not make it long enough");
    }

    /**
     * Crazy path: a null color is not a valid length.
     */
    @Test
    public void testIsFlyColorLengthValidCrazyPathNull() {
        boolean lengthValid = flyService.isFlyColorLengthValid(null);

        // a missing color has no length to check
        assertFalse(lengthValid, "A null color should not be a valid length");
    }

    // ---------- isFlyColorValidString ----------

    /**
     * Happy path: a color with letters, or letters mixed with digits, is valid.
     */
    @Test
    public void testIsFlyColorValidStringHappyPath() {
        // a normal color made of letters should pass
        assertTrue(flyService.isFlyColorValidString(TEST_FLY_COLOR), "A color of letters should be valid");

        // the interface gives "black 2" as an example that passes
        assertTrue(flyService.isFlyColorValidString("black 2"), "A color with a digit in it should be valid");
    }

    /**
     * Crappy path: a color made of only digits is not valid, even with spaces around it.
     */
    @Test
    public void testIsFlyColorValidStringCrappyPath() {
        // the interface gives "123" as an example that fails
        assertFalse(flyService.isFlyColorValidString("123"), "A color of only digits should not be valid");

        // the spaces around it don't make it a real color
        assertFalse(flyService.isFlyColorValidString("  456  "), "A digits-only color with spaces should not be valid");
    }

    /**
     * Crazy path: a null or blank color is not valid.
     */
    @Test
    public void testIsFlyColorValidStringCrazyPathNullAndBlank() {
        // the interface says the color has to not be blank to pass
        assertFalse(flyService.isFlyColorValidString(null), "A null color should not be valid");
        assertFalse(flyService.isFlyColorValidString("   "), "A blank color should not be valid");
    }

    // ---------- isMinSizeValidNumber ----------

    /**
     * Happy path: a min size right at 1 or 32 is valid.
     */
    @Test
    public void testIsMinSizeValidNumberHappyPath() {
        // the limits themselves are where an off-by-one mistake would wrongly reject a size
        assertTrue(flyService.isMinSizeValidNumber(1), "A min size of 1 should be valid");
        assertTrue(flyService.isMinSizeValidNumber(32), "A min size of 32 should be valid");
    }

    /**
     * Crappy path: a min size one under 1, one over 32, or negative is not valid.
     */
    @Test
    public void testIsMinSizeValidNumberCrappyPath() {
        // one past each limit is where an off-by-one mistake would wrongly allow a size
        assertFalse(flyService.isMinSizeValidNumber(0), "A min size of 0 should not be valid");
        assertFalse(flyService.isMinSizeValidNumber(33), "A min size of 33 should not be valid");

        // a negative size isn't a real hook size
        assertFalse(flyService.isMinSizeValidNumber(-5), "A negative min size should not be valid");
    }

    /**
     * Crazy path: a null min size is not valid.
     */
    @Test
    public void testIsMinSizeValidNumberCrazyPathNull() {
        boolean sizeValid = flyService.isMinSizeValidNumber(null);

        // the interface says the size has to not be null to pass
        assertFalse(sizeValid, "A null min size should not be valid");
    }

    // ---------- isMaxSizeValidNumber ----------

    /**
     * Happy path: a max size right at 1 or 32 is valid.
     */
    @Test
    public void testIsMaxSizeValidNumberHappyPath() {
        // the limits themselves are where an off-by-one mistake would wrongly reject a size
        assertTrue(flyService.isMaxSizeValidNumber(1), "A max size of 1 should be valid");
        assertTrue(flyService.isMaxSizeValidNumber(32), "A max size of 32 should be valid");
    }

    /**
     * Crappy path: a max size one under 1, one over 32, or negative is not valid.
     */
    @Test
    public void testIsMaxSizeValidNumberCrappyPath() {
        // one past each limit is where an off-by-one mistake would wrongly allow a size
        assertFalse(flyService.isMaxSizeValidNumber(0), "A max size of 0 should not be valid");
        assertFalse(flyService.isMaxSizeValidNumber(33), "A max size of 33 should not be valid");

        // a negative size isn't a real hook size
        assertFalse(flyService.isMaxSizeValidNumber(-5), "A negative max size should not be valid");
    }

    /**
     * Crazy path: a null max size is not valid.
     */
    @Test
    public void testIsMaxSizeValidNumberCrazyPathNull() {
        boolean sizeValid = flyService.isMaxSizeValidNumber(null);

        // the interface says the size has to not be null to pass
        assertFalse(sizeValid, "A null max size should not be valid");
    }

    // ---------- isFlyTypeValid ----------

    /**
     * Happy path: Dry, Nymph, and Streamer are all valid fly types.
     */
    @Test
    public void testIsFlyTypeValidHappyPath() {
        // these are the only three types the interface allows
        assertTrue(flyService.isFlyTypeValid("Dry"), "Dry should be a valid fly type");
        assertTrue(flyService.isFlyTypeValid("Nymph"), "Nymph should be a valid fly type");
        assertTrue(flyService.isFlyTypeValid("Streamer"), "Streamer should be a valid fly type");
    }

    /**
     * Crappy path: an unknown type, the wrong case, or extra spaces is not valid.
     */
    @Test
    public void testIsFlyTypeValidCrappyPath() {
        // a real fly type that Hookset doesn't offer
        assertFalse(flyService.isFlyTypeValid("Wet"), "A type that isn't offered should not be valid");

        // the interface says the match is exact and case-sensitive
        assertFalse(flyService.isFlyTypeValid("dry"), "A type in the wrong case should not be valid");
        assertFalse(flyService.isFlyTypeValid("Dry "), "A type with a trailing space should not be valid");
    }

    /**
     * Crazy path: a null or blank type is not valid.
     */
    @Test
    public void testIsFlyTypeValidCrazyPathNullAndBlank() {
        // a null or blank type can't match any of the allowed types
        assertFalse(flyService.isFlyTypeValid(null), "A null type should not be valid");
        assertFalse(flyService.isFlyTypeValid("   "), "A blank type should not be valid");
    }

    // ---------- isFlyNameLengthValid ----------

    /**
     * Happy path: a name right at 1 or 150 characters is valid, and spaces around it don't count.
     */
    @Test
    public void testIsFlyNameLengthValidHappyPath() {
        // exactly at the limits, which are still allowed
        final String minLengthName = "a";
        final String maxLengthName = "a".repeat(150);

        // 1 character once the spaces around it are trimmed
        final String paddedName = "  a  ";

        // each of these is right at a limit, or only passes once its spaces are trimmed
        assertTrue(flyService.isFlyNameLengthValid(minLengthName), "A name of exactly 1 character should be valid");
        assertTrue(flyService.isFlyNameLengthValid(maxLengthName), "A name of exactly 150 characters should be valid");
        assertTrue(flyService.isFlyNameLengthValid(paddedName), "Spaces around a name should not count toward its length");
    }

    /**
     * Crappy path: a name one over 150 characters, or only spaces, is not valid.
     */
    @Test
    public void testIsFlyNameLengthValidCrappyPath() {
        // one character over the limit
        final String tooLongName = "a".repeat(151);

        assertFalse(flyService.isFlyNameLengthValid(tooLongName), "A name of 151 characters should not be valid");

        // the interface says a name of only spaces counts as 0 characters
        assertFalse(flyService.isFlyNameLengthValid("   "), "A name of only spaces should not be valid");
    }

    /**
     * Crazy path: a null name is not a valid length.
     */
    @Test
    public void testIsFlyNameLengthValidCrazyPathNull() {
        boolean lengthValid = flyService.isFlyNameLengthValid(null);

        // a missing name has no length to check
        assertFalse(lengthValid, "A null name should not be a valid length");
    }

    // ---------- isFlyOwner ----------

    /**
     * Happy path: a user owns the custom fly they added.
     */
    @Test
    public void testIsFlyOwnerHappyPath() {
        boolean flyAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyOwner
        assertTrue(flyAdded, "The test fly should be added");

        // addFly doesn't return the fly, and it's the user's only fly, so position 0 is safe
        final Long flyId = flyService.getAllUserFlies(testUserId).get(0).getId();

        boolean isOwner = flyService.isFlyOwner(flyId, testUserId);

        // the user added this fly, so they own it
        assertTrue(isOwner, "A user should own the fly they added");
    }

    /**
     * Crappy path: another user, a missing user, a default fly, or a missing fly is not owned.
     */
    @Test
    public void testIsFlyOwnerCrappyPath() {
        // the two users from setup are the only ones, so the next ID can't belong to anyone
        final Long missingUserId = Math.max(testUserId, otherUserId) + 1;

        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyOwner
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not isFlyOwner
        assertTrue(defaultAdded, "The default fly should be added");

        // the add methods don't return the fly, and there's only one of each kind, so position 0 is safe
        final Long customFlyId = flyService.getAllUserFlies(testUserId).get(0).getId();
        final Long defaultFlyId = flyService.getAllHooksetFlies().get(0).getId();
        final Long missingFlyId = Math.max(customFlyId, defaultFlyId) + 1;

        // the custom fly belongs to the test user, so nobody else owns it
        assertFalse(flyService.isFlyOwner(customFlyId, otherUserId), "Another user should not own the fly");
        assertFalse(flyService.isFlyOwner(customFlyId, missingUserId), "A missing user should not own the fly");

        // the interface says default flies never belong to anyone
        assertFalse(flyService.isFlyOwner(defaultFlyId, testUserId), "No user should own a default fly");

        // a fly that doesn't exist can't belong to anyone
        assertFalse(flyService.isFlyOwner(missingFlyId, testUserId), "A fly that doesn't exist should not be owned");
    }

    /**
     * Crazy path: a null fly ID or a null user is never an owner, including on a default fly.
     */
    @Test
    public void testIsFlyOwnerCrazyPathNulls() {
        boolean customAdded = flyService.addFly(
                new FlyInput(TEST_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE), testUserId);

        // setup check, if this fails then addFly or the test data is broken, not isFlyOwner
        assertTrue(customAdded, "The custom fly should be added");

        boolean defaultAdded = flyService.addDefaultHooksetFly(
                new FlyInput(DEFAULT_FLY_NAME, TEST_FLY_TYPE, TEST_FLY_COLOR, TEST_MIN_SIZE, TEST_MAX_SIZE));

        // setup check, if this fails then addDefaultHooksetFly or the test data is broken, not isFlyOwner
        assertTrue(defaultAdded, "The default fly should be added");

        // the add methods don't return the fly, and there's only one of each kind, so position 0 is safe
        final Long customFlyId = flyService.getAllUserFlies(testUserId).get(0).getId();
        final Long defaultFlyId = flyService.getAllHooksetFlies().get(0).getId();

        // missing pieces should fail cleanly instead of crashing
        assertFalse(flyService.isFlyOwner(null, testUserId), "A null fly ID should not be owned");
        assertFalse(flyService.isFlyOwner(customFlyId, null), "A null user should not own a custom fly");
        assertFalse(flyService.isFlyOwner(null, null), "A null fly ID and a null user should not be an owner");

        // a default fly's owner is null too, so this makes sure two nulls don't count as a match
        assertFalse(flyService.isFlyOwner(defaultFlyId, null), "A null user should not own a default fly");
    }
}