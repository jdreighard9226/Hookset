package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.UserRepository;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shared.jpa.entity.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link edu.carroll.cs341.hookset.service.impl.LoginServiceImpl}.
 *
 * <p>Each service method is tested against a happy path, a crappy path, and a crazy
 * path. The happy path loads a real saved user. The crappy path asks for a username
 * that does not exist. The crazy path passes a null username.</p>
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

    /** Repository used to set up user test data. */
    @Autowired
    private UserRepository userRepository;

    /** Password encoder used to hash test user passwords the same way signup does. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** The saved user each test logs in as. */
    private User testUser;

    /**
     * Saves a test user before each test.
     */
    @BeforeEach
    public void setUpUser() {
        testUser = userRepository.save(createTestUser("testuser"));
    }

    // ---------- loadUserByUsername ----------

    /**
     * Happy path: an existing user is loaded and wrapped in HooksetUserDetails.
     */
    @Test
    public void testLoadUserByUsernameHappyPath() {
        UserDetails userDetails = loginService.loadUserByUsername("testuser");

        assertNotNull(userDetails, "An existing user should be loaded");

        // the rest of the app casts to HooksetUserDetails to get the ID, so it has to be that type
        assertInstanceOf(HooksetUserDetails.class, userDetails,
                "Loaded user should be a HooksetUserDetails");

        HooksetUserDetails hooksetUserDetails = (HooksetUserDetails) userDetails;

        assertEquals("testuser", hooksetUserDetails.getUsername(),
                "Loaded username should match the saved user");
        assertEquals(testUser.getId(), hooksetUserDetails.getId(),
                "Loaded user ID should match the saved user");
        assertEquals(testUser.getHashPassword(), hooksetUserDetails.getPassword(),
                "Loaded password should be the saved hash, so Spring Security can check it");
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
     * Crazy path: a null username throws UsernameNotFoundException instead of crashing.
     */
    @Test
    public void testLoadUserByUsernameCrazyPathNull() {
        assertThrows(UsernameNotFoundException.class,
                () -> loginService.loadUserByUsername(null),
                "A null username should throw UsernameNotFoundException");
    }

    // ---------- Helpers ----------

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
}