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
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Unit tests for StravaUploadManager
 * Tests upload orchestration with retry logic, duplicate detection, and progress tracking
 */
public class StravaUploadManagerTest {

    private static final String TEST_ENV_FILE = "test_strava_upload.env";

    private WireMockServer wireMockServer;
    private StravaConfig config;
    private StravaAuthenticator authenticator;
    private StravaApiClient apiClient;
    private StravaDuplicateDetector duplicateDetector;
    private StravaSportMapper sportMapper;
    private StravaUploadManager uploadManager;
    private TestProgressListener testProgressListener;

    /**
     * Test implementation of ProgressListener that captures progress updates
     */
    private static class TestProgressListener implements StravaUploadManager.ProgressListener {
        private final List<String> statuses = new ArrayList<>();
        private final List<String> activityNames = new ArrayList<>();

        @Override
        public void onProgress(String status, String activityName) {
            statuses.add(status);
            activityNames.add(activityName);
        }

        public boolean hasProgress(String status, String activityName) {
            for (int i = 0; i < statuses.size(); i++) {
                if (statuses.get(i).equals(status) && activityNames.get(i).equals(activityName)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Before
    public void setUp() throws IOException {
        // Start WireMock server for mocking Strava API
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        // Create test .env file with valid credentials
        createTestEnvFile("test_client_id", "test_client_secret",
                         "test_access_token", "test_refresh_token",
                         System.currentTimeMillis() / 1000 + 3600); // Expires in 1 hour

        // Initialize components with test configuration
        config = new StravaConfig(TEST_ENV_FILE);
        authenticator = new StravaAuthenticator(config, "http://localhost:8089");
        apiClient = new StravaApiClient(config, authenticator, "http://localhost:8089");
        sportMapper = new StravaSportMapper();
        duplicateDetector = new StravaDuplicateDetector(sportMapper);
        testProgressListener = new TestProgressListener();

        uploadManager = new StravaUploadManager(apiClient, duplicateDetector, sportMapper);
        uploadManager.setProgressListener(testProgressListener);
    }

    @After
    public void tearDown() {
        wireMockServer.stop();
        new File(TEST_ENV_FILE).delete();
    }

    @Test
    public void testSuccessfulUpload() throws IOException {
        // Mock get activities endpoint (no duplicates)
        stubFor(get(urlPathEqualTo("/api/v3/athlete/activities"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        // Mock upload endpoint
        stubFor(post(urlPathEqualTo("/api/v3/uploads"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 12345, \"status\": \"Your activity is ready.\"}")));

        JsonObject activity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");
        String gpxData = "<gpx>test data</gpx>";

        UploadResult result = uploadManager.uploadActivity(activity, gpxData, "Test Run", "Test description");

        assertTrue("Upload should succeed", result.isSuccess());
        assertEquals("Upload ID should match", 12345, result.getUploadId());
        assertTrue("Should have progress: Uploading", testProgressListener.hasProgress("Uploading", "Test Run"));
        assertTrue("Should have progress: Upload successful", testProgressListener.hasProgress("Upload successful", "Test Run"));
    }

    @Test
    public void testRetryOnNetworkError() throws IOException {
        // Mock get activities endpoint
        stubFor(get(urlPathEqualTo("/api/v3/athlete/activities"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        // Mock upload endpoint - fail twice, then succeed
        stubFor(post(urlPathEqualTo("/api/v3/uploads"))
            .inScenario("Retry")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse()
                .withStatus(500)
                .withBody("Network timeout"))
            .willSetStateTo("FirstRetry"));

        stubFor(post(urlPathEqualTo("/api/v3/uploads"))
            .inScenario("Retry")
            .whenScenarioStateIs("FirstRetry")
            .willReturn(aResponse()
                .withStatus(503)
                .withBody("Connection reset"))
            .willSetStateTo("SecondRetry"));

        stubFor(post(urlPathEqualTo("/api/v3/uploads"))
            .inScenario("Retry")
            .whenScenarioStateIs("SecondRetry")
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 12345, \"status\": \"Your activity is ready.\"}")));

        JsonObject activity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");
        String gpxData = "<gpx>test data</gpx>";

        UploadResult result = uploadManager.uploadActivity(activity, gpxData, "Test Run", "Test");

        assertTrue("Should succeed after retries", result.isSuccess());
        assertEquals("Should have retried 2 times", 2, result.getRetryCount());
    }

    @Test
    public void testMaxRetriesExceeded() throws IOException {
        // Mock get activities endpoint
        stubFor(get(urlPathEqualTo("/api/v3/athlete/activities"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        // Mock upload endpoint - always fail
        stubFor(post(urlPathEqualTo("/api/v3/uploads"))
            .willReturn(aResponse()
                .withStatus(500)
                .withBody("Network error")));

        JsonObject activity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");
        String gpxData = "<gpx>test data</gpx>";

        UploadResult result = uploadManager.uploadActivity(activity, gpxData, "Test", "Test");

        assertFalse("Upload should fail after max retries", result.isSuccess());
        assertEquals("Should have retried max times", 3, result.getRetryCount());
        assertTrue("Error message should mention retries", result.getErrorMessage().contains("retries"));
    }

    @Test
    public void testNoRetryOn400Error() throws IOException {
        // Mock get activities endpoint
        stubFor(get(urlPathEqualTo("/api/v3/athlete/activities"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        // Mock upload endpoint - return 400 error
        stubFor(post(urlPathEqualTo("/api/v3/uploads"))
            .willReturn(aResponse()
                .withStatus(400)
                .withBody("API request failed with status 400")));

        JsonObject activity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");
        String gpxData = "<gpx>test data</gpx>";

        UploadResult result = uploadManager.uploadActivity(activity, gpxData, "Test", "Test");

        assertFalse("Upload should fail", result.isSuccess());
        assertEquals("Should not have retried", 0, result.getRetryCount());
    }

    @Test
    public void testDuplicateSkipped() throws IOException {
        // Mock get activities endpoint with a matching activity
        String existingActivity = "[{\"start_date\": \"2026-03-06T10:01:00Z\", " +
                                 "\"distance\": 5010.0, " +
                                 "\"type\": \"Run\"}]";

        stubFor(get(urlPathEqualTo("/api/v3/athlete/activities"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(existingActivity)));

        JsonObject activity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");
        String gpxData = "<gpx>test data</gpx>";

        UploadResult result = uploadManager.uploadActivity(activity, gpxData, "Test Run", "Test");

        assertTrue("Should succeed with duplicate", result.isSuccess());
        assertTrue("Should be marked as duplicate", result.isDuplicate());
        assertTrue("Should have progress: Duplicate detected",
            testProgressListener.hasProgress("Duplicate detected, skipping", "Test Run"));
    }

    @Test
    public void testProgressCallback() throws IOException {
        // Mock get activities endpoint
        stubFor(get(urlPathEqualTo("/api/v3/athlete/activities"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("[]")));

        // Mock upload endpoint
        stubFor(post(urlPathEqualTo("/api/v3/uploads"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 12345, \"status\": \"Your activity is ready.\"}")));

        JsonObject activity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");
        String gpxData = "<gpx>test data</gpx>";

        uploadManager.uploadActivity(activity, gpxData, "Test Run", "Description");

        // Verify progress callbacks
        assertTrue("Should have progress: Checking for duplicates",
            testProgressListener.hasProgress("Checking for duplicates", "Test Run"));
        assertTrue("Should have progress: Uploading",
            testProgressListener.hasProgress("Uploading", "Test Run"));
        assertTrue("Should have progress: Upload successful",
            testProgressListener.hasProgress("Upload successful", "Test Run"));
    }

    /**
     * Helper to create activity JSON
     */
    private JsonObject createActivity(String startDate, double distance, String type) {
        JsonObject activity = new JsonObject();
        activity.addProperty("start_date", startDate);
        activity.addProperty("distance", distance);
        activity.addProperty("type", type);
        return activity;
    }

    /**
     * Helper to create test .env file
     */
    private void createTestEnvFile(String clientId, String clientSecret, String accessToken,
                                   String refreshToken, long expiresAt) throws IOException {
        try (FileWriter writer = new FileWriter(TEST_ENV_FILE)) {
            writer.write("STRAVA_CLIENT_ID=" + clientId + "\n");
            writer.write("STRAVA_CLIENT_SECRET=" + clientSecret + "\n");
            writer.write("STRAVA_ACCESS_TOKEN=" + accessToken + "\n");
            writer.write("STRAVA_REFRESH_TOKEN=" + refreshToken + "\n");
            writer.write("STRAVA_TOKEN_EXPIRES_AT=" + expiresAt + "\n");
        }
    }
}
