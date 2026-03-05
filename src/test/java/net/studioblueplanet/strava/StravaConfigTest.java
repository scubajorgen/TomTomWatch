package net.studioblueplanet.strava;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Unit tests for StravaConfig class
 * Tests .env file parsing and configuration management
 */
public class StravaConfigTest {

    private static final String TEST_ENV_FILE = ".env.test";
    private File envFile;

    @Before
    public void setUp() {
        // Clean up any existing test file
        envFile = new File(TEST_ENV_FILE);
        if (envFile.exists()) {
            envFile.delete();
        }
    }

    @After
    public void tearDown() {
        // Clean up test file
        if (envFile != null && envFile.exists()) {
            envFile.delete();
        }
    }

    @Test
    public void testLoadValidConfiguration() throws IOException {
        // Arrange: Create valid .env file
        createEnvFile(
            "STRAVA_CLIENT_ID=12345\n" +
            "STRAVA_CLIENT_SECRET=secret123\n" +
            "STRAVA_ACCESS_TOKEN=access_token_123\n" +
            "STRAVA_REFRESH_TOKEN=refresh_token_123\n" +
            "STRAVA_TOKEN_EXPIRES_AT=1234567890"
        );

        // Act: Load configuration
        StravaConfig config = new StravaConfig(TEST_ENV_FILE);

        // Assert: All values loaded correctly
        assertEquals("12345", config.getClientId());
        assertEquals("secret123", config.getClientSecret());
        assertEquals("access_token_123", config.getAccessToken());
        assertEquals("refresh_token_123", config.getRefreshToken());
        assertEquals(1234567890L, config.getTokenExpiresAt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingEnvFile() {
        // Act & Assert: Should throw exception when file doesn't exist
        new StravaConfig("nonexistent.env");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingClientId() throws IOException {
        // Arrange: Create .env without CLIENT_ID
        createEnvFile(
            "STRAVA_CLIENT_SECRET=secret123\n"
        );

        // Act & Assert: Should throw exception
        new StravaConfig(TEST_ENV_FILE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingClientSecret() throws IOException {
        // Arrange: Create .env without CLIENT_SECRET
        createEnvFile(
            "STRAVA_CLIENT_ID=12345\n"
        );

        // Act & Assert: Should throw exception
        new StravaConfig(TEST_ENV_FILE);
    }

    @Test
    public void testGetAccessToken() throws IOException {
        // Arrange: Create .env with access token
        createEnvFile(
            "STRAVA_CLIENT_ID=12345\n" +
            "STRAVA_CLIENT_SECRET=secret123\n" +
            "STRAVA_ACCESS_TOKEN=my_access_token\n"
        );

        // Act
        StravaConfig config = new StravaConfig(TEST_ENV_FILE);

        // Assert
        assertEquals("my_access_token", config.getAccessToken());
    }

    @Test
    public void testGetAccessTokenWhenNotSet() throws IOException {
        // Arrange: Create .env without tokens
        createEnvFile(
            "STRAVA_CLIENT_ID=12345\n" +
            "STRAVA_CLIENT_SECRET=secret123\n"
        );

        // Act
        StravaConfig config = new StravaConfig(TEST_ENV_FILE);

        // Assert: Should return null or empty string
        String token = config.getAccessToken();
        assertTrue(token == null || token.isEmpty());
    }

    @Test
    public void testSaveTokens() throws IOException {
        // Arrange: Create minimal .env
        createEnvFile(
            "STRAVA_CLIENT_ID=12345\n" +
            "STRAVA_CLIENT_SECRET=secret123\n"
        );

        StravaConfig config = new StravaConfig(TEST_ENV_FILE);

        // Act: Save tokens
        config.saveTokens("new_access_token", "new_refresh_token", 9999999999L);

        // Assert: Reload and verify tokens were saved
        StravaConfig reloadedConfig = new StravaConfig(TEST_ENV_FILE);
        assertEquals("new_access_token", reloadedConfig.getAccessToken());
        assertEquals("new_refresh_token", reloadedConfig.getRefreshToken());
        assertEquals(9999999999L, reloadedConfig.getTokenExpiresAt());
    }

    // Helper method to create test .env file
    private void createEnvFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(TEST_ENV_FILE)) {
            writer.write(content);
        }
    }
}
