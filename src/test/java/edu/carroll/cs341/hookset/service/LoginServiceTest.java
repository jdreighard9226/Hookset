package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.service.input.SignupInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link LoginService}.
 *
 * <p>Each service method is tested against a happy path, a crappy path, and a crazy
 * path. The happy paths load a real saved user, including when more than one user
 * exists. The crappy paths ask for usernames that do not exist or are blank. The
 * crazy path passes a null username.</p>
 *
 * <p>Tests only rely on what {@link LoginService} promises through
 * {@link UserDetailsService}. Test users are created through
 * {@link UserService#addUser(SignupInput)}, the same way signup creates them, so no
 * repositories are used.</p>
 *
 * <p>Tests run against the H2 test database and each test is rolled back by
 * {@link Transactional}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LoginServiceTest {

    /** Service under test. */
    @Autowired
    private LoginService loginService;

    /** Service used to create test users the same way signup does. */
    @Autowired
    private UserService userService;

    /** Password encoder used to check that the loaded password is the hash of the test password. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** The username of the user saved before each test. */
    private static final String TEST_USERNAME = "testuser";

    /** The plain text password every test user signs up with. */
    private static final String TEST_PASSWORD = "test-password";

    /**
     * Creates a test user through signup before each test.
     */
    @BeforeEach
    public void setUpUser() {
        SignupInput signupInput = new SignupInput(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD);
        boolean userAdded = userService.addUser(signupInput);

        // every test depends on this user existing, so fail here instead of later
        assertTrue(userAdded, "The test user should be created by signup");
    }

    // ---------- loadUserByUsername ----------

    /**
     * Happy path: an existing user is loaded with their username and password hash.
     */
    @Test
    public void testLoadUserByUsernameHappyPath() {
        UserDetails userDetails = loginService.loadUserByUsername(TEST_USERNAME);
        assertNotNull(userDetails, "An existing user should be loaded");

        assertEquals(TEST_USERNAME, userDetails.getUsername(),
                "Loaded username should match the saved user");

        // Spring Security checks the login against this hash, so it has to match the real password
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, userDetails.getPassword()),
                "Loaded password should be the saved hash of the test password");
    }

    /**
     * Happy path: with more than one user saved, each username loads its own user.
     */
    @Test
    public void testLoadUserByUsernameHappyPathMultipleUsers() {
        SignupInput otherSignupInput = new SignupInput("otheruser", TEST_PASSWORD, TEST_PASSWORD);
        boolean otherUserAdded = userService.addUser(otherSignupInput);
        assertTrue(otherUserAdded, "The second test user should be created by signup");

        // a lookup that ignored the username would hand back the same account both times
        UserDetails testUserDetails = loginService.loadUserByUsername(TEST_USERNAME);
        assertEquals(TEST_USERNAME, testUserDetails.getUsername(),
                "The first username should load the first user");

        UserDetails otherUserDetails = loginService.loadUserByUsername("otheruser");
        assertEquals("otheruser", otherUserDetails.getUsername(),
                "The second username should load the second user");
    }

    /**
     * Crappy path: a username that does not exist throws UsernameNotFoundException.
     */
    @Test
    public void testLoadUserByUsernameCrappyPath() {
        // Spring Security relies on this exact exception to fail the login cleanly
        assertThrows(UsernameNotFoundException.class,
                () -> loginService.loadUserByUsername("doesnotexist"),
                "A username that does not exist should throw UsernameNotFoundException");
    }

    /**
     * Crappy path: an empty or whitespace-only username throws UsernameNotFoundException.
     */
    @Test
    public void testLoadUserByUsernameCrappyPathBlank() {
        assertThrows(UsernameNotFoundException.class,
                () -> loginService.loadUserByUsername(""),
                "An empty username should throw UsernameNotFoundException");

        assertThrows(UsernameNotFoundException.class,
                () -> loginService.loadUserByUsername("   "),
                "A whitespace-only username should throw UsernameNotFoundException");
    }

    /**
     * Crazy path: a null username throws UsernameNotFoundException instead of crashing.
     */
    @Test
    public void testLoadUserByUsernameCrazyPathNull() {
        assertThrows(UsernameNotFoundException.class,
                () -> loginService.loadUserByUsername(null),
                "A null username should throw UsernameNotFoundException");
    }
}