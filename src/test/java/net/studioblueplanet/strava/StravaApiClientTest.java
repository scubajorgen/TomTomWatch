package net.studioblueplanet.strava;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Unit tests for StravaApiClient
 * Tests Strava API interactions with mocked responses using WireMock
 */
public class StravaApiClientTest {

    private static final String TEST_ENV_FILE = "test-api.env";
    private WireMockServer wireMockServer;
    private StravaApiClient apiClient;
    private StravaConfig config;
    private StravaAuthenticator authenticator;

    @Before
    public void setUp() throws IOException {
        // Create test .env file with valid token
        createEnvFile(
            "STRAVA_CLIENT_ID=12345\n" +
            "STRAVA_CLIENT_SECRET=secret123\n" +
            "STRAVA_ACCESS_TOKEN=valid_token\n" +
            "STRAVA_REFRESH_TOKEN=refresh_token\n" +
            "STRAVA_TOKEN_EXPIRES_AT=9999999999\n"
        );

        config = new StravaConfig(TEST_ENV_FILE);

        // Start WireMock server for mocking Strava API
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        authenticator = new StravaAuthenticator(config, "http://localhost:8089");
        apiClient = new StravaApiClient(config, authenticator, "http://localhost:8089");
    }

    @After
    public void tearDown() {
        wireMockServer.stop();
        new File(TEST_ENV_FILE).delete();
    }

    private void createEnvFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(TEST_ENV_FILE)) {
            writer.write(content);
        }
    }

    @Test
    public void testGetAthleteActivities() throws IOException {
        // Mock GET /api/v3/athlete/activities
        stubFor(get(urlMatching("/api/v3/athlete/activities\\?.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[" +
                    "{\"id\":123,\"name\":\"Morning Run\",\"type\":\"Run\",\"distance\":5000," +
                    "\"start_date\":\"2026-03-01T07:00:00Z\"}," +
                    "{\"id\":124,\"name\":\"Evening Ride\",\"type\":\"Ride\",\"distance\":20000," +
                    "\"start_date\":\"2026-03-02T18:00:00Z\"}" +
                    "]")));

        JsonArray activities = apiClient.getAthleteActivities("2026-03-01", "2026-03-05", 10);

        assertNotNull("Activities should not be null", activities);
        assertEquals("Should return 2 activities", 2, activities.size());

        JsonObject firstActivity = activities.get(0).getAsJsonObject();
        assertEquals("First activity name", "Morning Run", firstActivity.get("name").getAsString());
        assertEquals("First activity type", "Run", firstActivity.get("type").getAsString());
    }

    @Test
    public void testUploadActivity() throws IOException {
        // Mock POST /api/v3/uploads
        stubFor(post(urlEqualTo("/api/v3/uploads"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":987654,\"status\":\"Your activity is still being processed.\"}")));

        String gpxData = "<gpx>test data</gpx>";
        JsonObject uploadResponse = apiClient.uploadActivity(gpxData, "Test Activity", "Test description", "Run");

        assertNotNull("Upload response should not be null", uploadResponse);
        assertEquals("Upload ID", 987654, uploadResponse.get("id").getAsInt());
        assertTrue("Status contains processed message",
            uploadResponse.get("status").getAsString().contains("processed"));
    }

    @Test
    public void testGetUploadStatus() throws IOException {
        // Mock GET /api/v3/uploads/:uploadId
        stubFor(get(urlEqualTo("/api/v3/uploads/987654"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":987654,\"activity_id\":123456,\"status\":\"Your activity is ready.\"}")));

        JsonObject status = apiClient.getUploadStatus(987654);

        assertNotNull("Status should not be null", status);
        assertEquals("Upload ID", 987654, status.get("id").getAsInt());
        assertEquals("Activity ID", 123456, status.get("activity_id").getAsInt());
        assertTrue("Status is ready", status.get("status").getAsString().contains("ready"));
    }

    @Test
    public void testRefreshTokenOn401() throws IOException {
        // Mock initial request with 401
        stubFor(get(urlMatching("/api/v3/athlete/activities\\?.*"))
            .inScenario("Token Refresh")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse()
                .withStatus(401))
            .willSetStateTo("Token Expired"));

        // Mock token refresh
        stubFor(post(urlEqualTo("/oauth/token"))
            .withRequestBody(containing("\"grant_type\":\"refresh_token\""))
            .inScenario("Token Refresh")
            .whenScenarioStateIs("Token Expired")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"access_token\":\"new_token\",\"refresh_token\":\"new_refresh\"," +
                    "\"expires_at\":9999999999}"))
            .willSetStateTo("Token Refreshed"));

        // Mock retry request with new token
        stubFor(get(urlMatching("/api/v3/athlete/activities\\?.*"))
            .inScenario("Token Refresh")
            .whenScenarioStateIs("Token Refreshed")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        JsonArray activities = apiClient.getAthleteActivities("2026-03-01", "2026-03-05", 10);

        assertNotNull("Activities should not be null after token refresh", activities);

        // Reload config to verify token was refreshed
        config = new StravaConfig(TEST_ENV_FILE);
        assertEquals("Access token should be updated", "new_token", config.getAccessToken());
    }

    @Test
    public void testRateLimitHandling() throws IOException {
        // Mock 429 rate limit response
        stubFor(get(urlMatching("/api/v3/athlete/activities\\?.*"))
            .willReturn(aResponse()
                .withStatus(429)
                .withHeader("Retry-After", "300")
                .withBody("{\"message\":\"Rate Limit Exceeded\"}")));

        try {
            apiClient.getAthleteActivities("2026-03-01", "2026-03-05", 10);
            fail("Should throw exception on rate limit");
        } catch (IOException e) {
            assertTrue("Error message should mention rate limit",
                e.getMessage().contains("Rate limit"));
        }
    }
}
