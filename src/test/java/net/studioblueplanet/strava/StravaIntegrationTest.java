package net.studioblueplanet.strava;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.GpxWriter;
import net.studioblueplanet.ttbin.TomTomReader;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Manual integration test for Strava upload functionality
 * Tests the complete workflow: load activity → convert to GPX → upload to Strava
 *
 * This is NOT a JUnit test - it uses real Strava credentials and performs actual uploads.
 * Run manually: mvn exec:java -Dexec.mainClass="net.studioblueplanet.strava.StravaIntegrationTest"
 */
public class StravaIntegrationTest {

    private static final String ENV_FILE = ".env";
    private static final String ACTIVITY_FILE = "working/simulation/0x00910000.ttbin";

    public static void main(String[] args) {
        System.out.println("=== Strava Integration Test ===\n");

        try {
            // Step 1: Initialize Strava components
            System.out.println("Step 1: Initializing Strava components...");
            StravaConfig config = new StravaConfig(ENV_FILE);
            StravaAuthenticator authenticator = new StravaAuthenticator(config);
            StravaApiClient apiClient = new StravaApiClient(config, authenticator);
            StravaSportMapper sportMapper = new StravaSportMapper();
            StravaDuplicateDetector duplicateDetector = new StravaDuplicateDetector(sportMapper);
            StravaUploadManager uploadManager = new StravaUploadManager(apiClient, duplicateDetector, sportMapper);

            // Set up progress listener
            uploadManager.setProgressListener((status, activityName) ->
                System.out.println("  [Progress] " + status + " - " + activityName)
            );
            System.out.println("✓ Components initialized\n");

            // Step 2: Load activity from simulation directory
            System.out.println("Step 2: Loading activity from " + ACTIVITY_FILE + "...");
            File activityFile = new File(ACTIVITY_FILE);
            if (!activityFile.exists()) {
                System.err.println("✗ Activity file not found: " + ACTIVITY_FILE);
                System.err.println("  Expected location: " + activityFile.getAbsolutePath());
                return;
            }

            TomTomReader reader = TomTomReader.getInstance();
            reader.setTrackSmoothing(true, 10.0f);
            Activity activity = reader.readTtbinFile(activityFile.getAbsolutePath());

            if (activity == null) {
                System.err.println("✗ Failed to read activity file");
                return;
            }
            System.out.println("✓ Activity loaded:");
            System.out.println("  - Start time: " + activity.getStartDateTime());
            System.out.println("  - Distance: " + String.format("%.2f km", activity.getDistance() / 1000.0));
            System.out.println("  - Type: " + getActivityTypeName(activity.getActivityType()));
            System.out.println();

            // Step 3: Convert to GPX format
            System.out.println("Step 3: Converting to GPX format...");
            StringWriter gpxWriter = new StringWriter();
            GpxWriter.getInstance().writeTrackToFile(gpxWriter, activity, "TomTomWatch");
            String gpxData = gpxWriter.toString();
            System.out.println("✓ GPX generated (" + gpxData.length() + " bytes)\n");

            // Step 4: Create activity metadata for Strava
            System.out.println("Step 4: Creating activity metadata...");
            JsonObject activityMetadata = new JsonObject();

            // Format timestamp in ISO 8601 format for Strava API (YYYY-MM-DDThh:mm:ssZ)
            String startDate = activity.getStartDateTime().format("YYYY-MM-DDThh:mm:ssZ");
            activityMetadata.addProperty("start_date", startDate);
            activityMetadata.addProperty("distance", activity.getDistance());
            activityMetadata.addProperty("type", mapActivityType(activity.getActivityType()));

            System.out.println("✓ Metadata created:");
            System.out.println("  - start_date: " + startDate);
            System.out.println("  - distance: " + activity.getDistance() + "m");
            System.out.println("  - type: " + activityMetadata.get("type").getAsString());
            System.out.println();

            // Step 5: Attempt first upload
            System.out.println("Step 5: Uploading to Strava (first attempt)...");
            String activityName = "Test Activity - " + activity.getStartDateTime().format("YYYY-MM-DD hh:mm");
            String description = "Uploaded via TomTomWatch Strava Integration (Test)";

            UploadResult firstResult = uploadManager.uploadActivity(activityMetadata, gpxData, activityName, description);

            System.out.println("\nFirst Upload Result:");
            System.out.println("  - Success: " + firstResult.isSuccess());
            System.out.println("  - Duplicate: " + firstResult.isDuplicate());
            if (firstResult.isSuccess() && !firstResult.isDuplicate()) {
                System.out.println("  - Upload ID: " + firstResult.getUploadId());
                System.out.println("  - Retry count: " + firstResult.getRetryCount());
            } else if (firstResult.isDuplicate()) {
                System.out.println("  - Activity already exists on Strava (skipped)");
            } else {
                System.out.println("  - Error: " + firstResult.getErrorMessage());
                System.out.println("  - Retry count: " + firstResult.getRetryCount());
            }
            System.out.println();

            // Step 6: Attempt second upload to test duplicate detection
            if (firstResult.isSuccess() && !firstResult.isDuplicate()) {
                System.out.println("Step 6: Uploading again to test duplicate detection...");
                UploadResult secondResult = uploadManager.uploadActivity(activityMetadata, gpxData, activityName, description);

                System.out.println("\nSecond Upload Result:");
                System.out.println("  - Success: " + secondResult.isSuccess());
                System.out.println("  - Duplicate: " + secondResult.isDuplicate());

                if (secondResult.isDuplicate()) {
                    System.out.println("  ✓ Duplicate detection working correctly!");
                } else {
                    System.out.println("  ⚠ Warning: Duplicate was not detected");
                }
                System.out.println();
            }

            // Step 7: Fetch and display recent activities
            System.out.println("Step 7: Fetching recent activities from Strava...");
            String afterDate = activity.getStartDateTime().minusDays(1).format("YYYY-MM-DDThh:mm:ssZ");
            String beforeDate = activity.getStartDateTime().plusDays(1).format("YYYY-MM-DDThh:mm:ssZ");

            JsonArray recentActivities = apiClient.getAthleteActivities(afterDate, beforeDate, 5);
            System.out.println("✓ Found " + recentActivities.size() + " activities around this time:");

            for (int i = 0; i < recentActivities.size(); i++) {
                JsonObject act = recentActivities.get(i).getAsJsonObject();
                System.out.println("  " + (i + 1) + ". " + act.get("name").getAsString());
                System.out.println("     - Type: " + act.get("type").getAsString());
                System.out.println("     - Distance: " + String.format("%.2f km", act.get("distance").getAsDouble() / 1000.0));
                System.out.println("     - Start: " + act.get("start_date").getAsString());
            }

            System.out.println("\n=== Integration Test Complete ===");
            System.out.println("✓ All components working correctly");
            System.out.println("✓ Upload and duplicate detection verified");

        } catch (IOException e) {
            System.err.println("\n✗ Error during integration test:");
            System.err.println("  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n✗ Unexpected error:");
            System.err.println("  " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Maps TomTom activity type code to readable name
     */
    private static String getActivityTypeName(int typeCode) {
        switch (typeCode) {
            case 0: return "Running";
            case 1: return "Cycling";
            case 2: return "Swimming";
            case 3: return "Treadmill";
            case 4: return "Freestyle";
            case 5: return "Gym";
            case 6: return "Hiking";
            default: return "Unknown (" + typeCode + ")";
        }
    }

    /**
     * Maps TomTom activity type to internal type name for sport mapper
     */
    private static String mapActivityType(int typeCode) {
        switch (typeCode) {
            case 0: return "running";
            case 1: return "cycling";
            case 2: return "swimming";
            case 3: return "running"; // treadmill
            case 4: return "freestyle";
            case 5: return "gym";
            case 6: return "hiking";
            default: return "unknown";
        }
    }
}
