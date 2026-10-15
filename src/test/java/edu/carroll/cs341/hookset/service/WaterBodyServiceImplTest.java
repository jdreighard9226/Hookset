
package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.jpa.repo.WaterBodyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shared.jpa.entity.WaterBody;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WaterBodyServiceImplTest {

    @Autowired
    private WaterBodyService waterBodyService;

    @Autowired
    private WaterBodyRepository waterBodyRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void checkDatabase() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.getMetaData().getURL().startsWith("jdbc:h2:"),
                    "Tests must use the H2 database");
        }
    }

    // Happy Path - Get all water bodies
    @Test
    void testGetAllWaterBodiesHappyPath() {
        WaterBody waterBody = createTestWaterBody();
        waterBodyRepository.save(waterBody);

        List<WaterBody> waterBodies = waterBodyService.getAllWaterBodies();

        assertNotNull(waterBodies);
        assertEquals(1,waterBodies.size());
        assertEquals("Test River", waterBodies.getFirst().getWaterBodyName());
    }

    // Crappy Path - No water bodies exist
    @Test
    void testGetAllWaterBodiesCrappyPath() {
        List<WaterBody> waterBodies = waterBodyService.getAllWaterBodies();

        assertNotNull(waterBodies);
        assertTrue(waterBodies.isEmpty());
    }

    // Crazy Path - Many water bodies exist
    @Test
    void testGetAllWaterBodiesCrazyPath() {
        for (int i = 1; i <= 25; i++) {
            WaterBody waterBody = createTestWaterBody();
            waterBody.setWaterBodySlug("test-river-" + i);
            waterBodyRepository.save(waterBody);
        }

        waterBodyRepository.flush();

        List<WaterBody> waterBodies = waterBodyService.getAllWaterBodies();

        long count = waterBodies.stream()
                .filter(body -> body.getWaterBodySlug().startsWith("test-river-"))
                .count();

        assertEquals(25, count);
    }

    // Happy Path - Find a water body by slug
    @Test
    void testGetWaterBodyFromSlugHappyPath() {
        WaterBody waterBody = createTestWaterBody();
        waterBodyRepository.saveAndFlush(waterBody);

        WaterBody result = waterBodyService.getWaterBodyFromSlug("test-river");

        assertNotNull(result);
        assertEquals("Test River", result.getWaterBodyName());
        assertEquals("test-river", result.getWaterBodySlug());
        assertEquals("Montana", result.getWaterBodyState());
        assertEquals("River", result.getWaterBodyType());
    }

    // Crappy Path - Slug does not exist
    @Test
    void testGetWaterBodyFromSlugCrappyPath() {
        WaterBody result = waterBodyService.getWaterBodyFromSlug("does-not-exist");

        assertNull(result);
    }

    // Crazy Path - Null slug
    @Test
    void testGetWaterBodyFromSlugCrazyPath() {
        WaterBody result = waterBodyService.getWaterBodyFromSlug(null);

        assertNull(result);
    }

    private WaterBody createTestWaterBody() {
        WaterBody waterBody = new WaterBody();

        waterBody.setWaterBodyName("Test River");
        waterBody.setWaterBodyType("River");
        waterBody.setWaterBodyState("Montana");
        waterBody.setWaterBodyDescription("A river used for testing.");
        waterBody.setWaterBodySlug("test-river");

        return waterBody;
    }
}
