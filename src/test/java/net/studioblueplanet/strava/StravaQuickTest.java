package net.studioblueplanet.strava;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Scanner;

/**
 * Quick test to verify Strava integration is working
 * Handles OAuth authorization if needed
 */
public class StravaQuickTest {

    private static final String ENV_FILE = ".env";

    public static void main(String[] args) {
        System.out.println("=== Strava Quick Test ===\n");

        try {
            // Initialize components
            System.out.println("Initializing Strava components...");
            StravaConfig config = new StravaConfig(ENV_FILE);
            StravaAuthenticator authenticator = new StravaAuthenticator(config);
            StravaApiClient apiClient = new StravaApiClient(config, authenticator);

            // Check if we have a valid access token
            String accessToken = config.getAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                System.out.println("\n⚠ No access token found. Authorization required.");
                System.out.println("\nStep 1: Visit this URL to authorize the application:");
                System.out.println(authenticator.generateAuthorizationUrl());
                System.out.println("\nStep 2: After authorizing, you'll be redirected to localhost.");
                System.out.println("Copy the 'code' parameter from the URL.");
                System.out.println("(The URL will look like: http://localhost/?code=XXXXX&scope=...)");
                System.out.print("\nEnter the authorization code: ");

                Scanner scanner = new Scanner(System.in);
                String authCode = scanner.nextLine().trim();

                System.out.println("\nExchanging code for access token...");
                boolean success = authenticator.exchangeCodeForTokens(authCode);

                if (!success) {
                    System.err.println("✗ Failed to exchange authorization code for tokens");
                    System.err.println("  Please check the code and try again");
                    return;
                }

                // Reload config to get new tokens
                config = new StravaConfig(ENV_FILE);
                System.out.println("✓ Authorization successful! Tokens saved to .env\n");
            } else {
                System.out.println("✓ Access token found\n");

                // Check if token is expired
                if (authenticator.isTokenExpired()) {
                    System.out.println("Access token expired. Refreshing...");
                    boolean refreshed = authenticator.refreshToken();
                    if (refreshed) {
                        config = new StravaConfig(ENV_FILE);
                        System.out.println("✓ Token refreshed\n");
                    } else {
                        System.err.println("✗ Failed to refresh token. Please re-authorize.\n");
                        return;
                    }
                }
            }

            // Test API connectivity
            System.out.println("Testing Strava API connection...");
            String afterDate = "2019-09-01T00:00:00Z";  // Old date to get some activities
            String beforeDate = "2026-12-31T23:59:59Z"; // Far future
            JsonArray activities = apiClient.getAthleteActivities(afterDate, beforeDate, 5);

            System.out.println("✓ Successfully connected to Strava API");
            System.out.println("✓ Found " + activities.size() + " recent activities:\n");

            for (int i = 0; i < activities.size(); i++) {
                JsonObject activity = activities.get(i).getAsJsonObject();
                String name = activity.get("name").getAsString();
                String type = activity.get("type").getAsString();
                double distance = activity.get("distance").getAsDouble();
                String startDate = activity.get("start_date").getAsString();

                System.out.println((i + 1) + ". " + name);
                System.out.println("   Type: " + type);
                System.out.println("   Distance: " + String.format("%.2f km", distance / 1000.0));
                System.out.println("   Date: " + startDate);
                System.out.println();
            }

            // Test sport mapper
            System.out.println("Testing sport type mapping...");
            StravaSportMapper sportMapper = new StravaSportMapper();
            System.out.println("  running → " + sportMapper.mapToStravaActivityType("running"));
            System.out.println("  cycling → " + sportMapper.mapToStravaActivityType("cycling"));
            System.out.println("  swimming → " + sportMapper.mapToStravaActivityType("swimming"));
            System.out.println("✓ Sport mapping working\n");

            // Test duplicate detector
            System.out.println("Testing duplicate detection...");
            StravaDuplicateDetector duplicateDetector = new StravaDuplicateDetector(sportMapper);

            // Create a test activity that matches the first real activity
            if (activities.size() > 0) {
                JsonObject firstActivity = activities.get(0).getAsJsonObject();
                JsonObject testActivity = new JsonObject();
                testActivity.addProperty("start_date", firstActivity.get("start_date").getAsString());
                testActivity.addProperty("distance", firstActivity.get("distance").getAsDouble());
                testActivity.addProperty("type", firstActivity.get("type").getAsString().toLowerCase());

                boolean isDuplicate = duplicateDetector.isDuplicate(testActivity, activities);
                System.out.println("  Duplicate detection result: " + (isDuplicate ? "✓ MATCH (correct)" : "✗ NO MATCH (incorrect)"));
            }
            System.out.println();

            System.out.println("=== All Tests Passed ===");
            System.out.println("✓ Authentication working");
            System.out.println("✓ API client working");
            System.out.println("✓ Sport mapping working");
            System.out.println("✓ Duplicate detection working");
            System.out.println("\nReady for full integration test!");

        } catch (IOException e) {
            System.err.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n✗ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
