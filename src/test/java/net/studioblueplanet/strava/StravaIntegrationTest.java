package net.studioblueplanet.strava;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
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
 * Integration tests for Strava upload workflow
 * Tests the full stack: StravaUploadManager → StravaApiClient → WireMock (mock HTTP)
 */
public class StravaIntegrationTest {

    private static final String TEST_ENV = "test-integration.env";
    private static final int WIRE_MOCK_PORT = 8090;

    private WireMockServer wireMockServer;
    private StravaUploadManager uploadManager;
    private StravaApiClient apiClient;

    @Before
    public void setUp() throws IOException {
        createEnvFile();
        wireMockServer = new WireMockServer(WIRE_MOCK_PORT);
        wireMockServer.start();
        WireMock.configureFor("localhost", WIRE_MOCK_PORT);
        StravaConfig config = new StravaConfig(TEST_ENV);
        StravaAuthenticator authenticator = new StravaAuthenticator(config, "http://localhost:" + WIRE_MOCK_PORT);
        apiClient = new StravaApiClient(config, authenticator, "http://localhost:" + WIRE_MOCK_PORT);
        StravaSportMapper sportMapper = new StravaSportMapper();
        StravaDuplicateDetector duplicateDetector = new StravaDuplicateDetector(sportMapper);
        uploadManager = new StravaUploadManager(apiClient, duplicateDetector, sportMapper);
    }

    @After
    public void tearDown() {
        wireMockServer.stop();
        new File(TEST_ENV).delete();
    }

    private void createEnvFile() throws IOException {
        try (FileWriter w = new FileWriter(TEST_ENV)) {
            w.write("STRAVA_CLIENT_ID=12345\nSTRAVA_CLIENT_SECRET=secret\n" +
                    "STRAVA_ACCESS_TOKEN=token\nSTRAVA_REFRESH_TOKEN=refresh\n" +
                    "STRAVA_TOKEN_EXPIRES_AT=9999999999\n");
        }
    }

    private JsonObject activityMetadata(String startDate, double distanceMeters, String type) {
        JsonObject m = new JsonObject();
        m.addProperty("start_date", startDate);
        m.addProperty("distance", distanceMeters);
        m.addProperty("type", type);
        return m;
    }

    @Test
    public void testEndToEndUploadSuccess() throws IOException {
        stubFor(get(urlMatching("/api/v3/athlete/activities\\?.*"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                .withBody("[]")));
        stubFor(post(urlEqualTo("/api/v3/uploads"))
            .willReturn(aResponse().withStatus(201).withHeader("Content-Type", "application/json")
                .withBody("{\"id\":111,\"status\":\"Your activity is still being processed.\"}")));

        JsonObject activity = activityMetadata("2026-03-03T10:00:00Z", 5000.0, "running");
        UploadResult result = uploadManager.uploadActivity(activity, "<gpx/>", "Morning Run", "desc");

        assertTrue("Upload should succeed", result.isSuccess());
        assertFalse("Should not be duplicate", result.isDuplicate());
        assertEquals("Upload ID should match", 111L, result.getUploadId());
    }

    @Test
    public void testDuplicateDetectionIntegration() throws IOException {
        // Strava already has this activity
        stubFor(get(urlMatching("/api/v3/athlete/activities\\?.*"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                .withBody("[{\"id\":99,\"name\":\"Existing\",\"type\":\"Run\"," +
                    "\"distance\":5000.0,\"start_date\":\"2026-03-03T10:00:00Z\"}]")));

        JsonObject activity = activityMetadata("2026-03-03T10:00:00Z", 5000.0, "running");
        UploadResult result = uploadManager.uploadActivity(activity, "<gpx/>", "Morning Run", "desc");

        assertTrue("Result should be success (skipped counts as success)", result.isSuccess());
        assertTrue("Should be detected as duplicate", result.isDuplicate());
        verify(0, postRequestedFor(urlEqualTo("/api/v3/uploads")));
    }

    @Test
    public void testErrorRecoveryIntegration() throws IOException {
        stubFor(get(urlMatching("/api/v3/athlete/activities\\?.*"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                .withBody("[]")));
        // First attempt: server error; second attempt: success
        stubFor(post(urlEqualTo("/api/v3/uploads"))
            .inScenario("retry").whenScenarioStateIs("Started")
            .willReturn(aResponse().withStatus(503).withBody("Server Error"))
            .willSetStateTo("Retry"));
        stubFor(post(urlEqualTo("/api/v3/uploads"))
            .inScenario("retry").whenScenarioStateIs("Retry")
            .willReturn(aResponse().withStatus(201).withHeader("Content-Type", "application/json")
                .withBody("{\"id\":222,\"status\":\"processed\"}")));

        JsonObject activity = activityMetadata("2026-03-03T10:00:00Z", 5000.0, "running");
        UploadResult result = uploadManager.uploadActivity(activity, "<gpx/>", "Run", "desc");

        assertTrue("Upload should succeed after retry", result.isSuccess());
        assertFalse("Should not be duplicate", result.isDuplicate());
        assertTrue("Should have retried at least once", result.getRetryCount() >= 1);
    }

    @Test
    public void testMultipleActivitiesIntegration() throws IOException {
        // No existing activities — all uploads succeed
        stubFor(get(urlMatching("/api/v3/athlete/activities\\?.*"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                .withBody("[]")));
        stubFor(post(urlEqualTo("/api/v3/uploads"))
            .willReturn(aResponse().withStatus(201).withHeader("Content-Type", "application/json")
                .withBody("{\"id\":300,\"status\":\"processed\"}")));

        String[] dates = {"2026-03-01T08:00:00Z", "2026-03-02T09:00:00Z", "2026-03-03T10:00:00Z"};
        int uploaded = 0;
        for (String date : dates) {
            JsonObject activity = activityMetadata(date, 5000.0, "running");
            UploadResult result = uploadManager.uploadActivity(activity, "<gpx/>", "Run " + date, "");
            if (result.isSuccess() && !result.isDuplicate()) uploaded++;
        }

        assertEquals("All 3 activities should be uploaded", 3, uploaded);
        verify(3, postRequestedFor(urlEqualTo("/api/v3/uploads")));
    }
}
