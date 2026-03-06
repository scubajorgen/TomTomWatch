package net.studioblueplanet.strava;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import com.sun.net.httpserver.HttpServer;

/**
 * Test utility to verify Strava credentials and fetch most recent activity
 * This is a manual test tool - run main() method to test
 */
public class StravaCredentialsTester {

    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String AUTH_URL = "https://www.strava.com/oauth/authorize";
    private static final String TOKEN_URL = "https://www.strava.com/oauth/token";
    private static final String API_BASE = "https://www.strava.com/api/v3";

    private static String authCode = null;

    public static void main(String[] args) {
        try {
            System.out.println("=== Strava Credentials Tester ===\n");

            // Step 1: Load configuration
            System.out.println("Step 1: Loading .env configuration...");
            StravaConfig config = new StravaConfig(".env");
            System.out.println("✓ Client ID: " + config.getClientId());
            System.out.println("✓ Client Secret: " + maskSecret(config.getClientSecret()));

            // Step 2: Check if we already have a valid access token
            String accessToken = config.getAccessToken();
            if (accessToken != null && !accessToken.isEmpty()) {
                System.out.println("✓ Access token found, testing...");
                if (testToken(accessToken)) {
                    System.out.println("✓ Token is valid!");
                    fetchAndDisplayActivity(accessToken);
                    return;
                } else {
                    System.out.println("✗ Token is invalid or expired, need to re-authorize");
                }
            }

            // Step 3: Start OAuth flow
            System.out.println("\nStep 2: Starting OAuth authorization flow...");
            System.out.println("Starting callback server on port 8080...");

            // Start local server for OAuth callback
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/callback", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("code=")) {
                    authCode = query.substring(query.indexOf("code=") + 5);
                    if (authCode.contains("&")) {
                        authCode = authCode.substring(0, authCode.indexOf("&"));
                    }
                }

                // Send response to browser
                String response = "<html><body><h1>Authorization Successful!</h1><p>You can close this window and return to the terminal.</p></body></html>";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            server.start();

            // Build authorization URL
            String authUrl = AUTH_URL +
                "?client_id=" + config.getClientId() +
                "&redirect_uri=" + REDIRECT_URI +
                "&response_type=code" +
                "&scope=activity:read_all";

            System.out.println("\nOpening browser for Strava authorization...");
            System.out.println("If browser doesn't open, visit this URL:");
            System.out.println(authUrl);

            // Open browser
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            }

            // Wait for callback
            System.out.println("\nWaiting for authorization...");
            int attempts = 0;
            while (authCode == null && attempts < 60) {
                Thread.sleep(1000);
                attempts++;
            }

            server.stop(0);

            if (authCode == null) {
                System.out.println("✗ Authorization timed out or failed");
                return;
            }

            System.out.println("✓ Authorization code received!");

            // Step 4: Exchange code for tokens
            System.out.println("\nStep 3: Exchanging authorization code for access token...");
            accessToken = exchangeCodeForToken(config.getClientId(), config.getClientSecret(), authCode);

            if (accessToken == null) {
                System.out.println("✗ Failed to get access token");
                return;
            }

            System.out.println("✓ Access token received!");
            System.out.println("Token: " + maskSecret(accessToken));

            // Step 5: Fetch and display most recent activity
            System.out.println("\nStep 4: Fetching your most recent activity...");
            fetchAndDisplayActivity(accessToken);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean testToken(String accessToken) {
        try {
            HttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(API_BASE + "/athlete");
            request.addHeader("Authorization", "Bearer " + accessToken);

            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static String exchangeCodeForToken(String clientId, String clientSecret, String code) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(TOKEN_URL);

        String jsonBody = "{" +
            "\"client_id\":\"" + clientId + "\"," +
            "\"client_secret\":\"" + clientSecret + "\"," +
            "\"code\":\"" + code + "\"," +
            "\"grant_type\":\"authorization_code\"" +
            "}";

        post.setEntity(new StringEntity(jsonBody));
        post.setHeader("Content-Type", "application/json");

        HttpResponse response = client.execute(post);
        String responseBody = EntityUtils.toString(response.getEntity());

        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        if (json.has("access_token")) {
            return json.get("access_token").getAsString();
        }

        return null;
    }

    private static void fetchAndDisplayActivity(String accessToken) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(API_BASE + "/athlete/activities?per_page=1");
        request.addHeader("Authorization", "Bearer " + accessToken);

        HttpResponse response = client.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        JsonArray activities = JsonParser.parseString(responseBody).getAsJsonArray();

        if (activities.size() == 0) {
            System.out.println("No activities found in your Strava account.");
            return;
        }

        JsonObject activity = activities.get(0).getAsJsonObject();

        System.out.println("\n=== Most Recent Activity ===");
        System.out.println("Name: " + activity.get("name").getAsString());
        System.out.println("Type: " + activity.get("type").getAsString());
        System.out.println("Date: " + activity.get("start_date").getAsString());
        System.out.println("Distance: " + String.format("%.2f km", activity.get("distance").getAsDouble() / 1000));
        System.out.println("Moving Time: " + formatDuration(activity.get("moving_time").getAsInt()));
        System.out.println("Total Elevation Gain: " + String.format("%.0f m", activity.get("total_elevation_gain").getAsDouble()));

        if (activity.has("average_heartrate")) {
            System.out.println("Average Heart Rate: " + String.format("%.0f bpm", activity.get("average_heartrate").getAsDouble()));
        }

        System.out.println("\n✓ Successfully connected to Strava!");
        System.out.println("✓ Your credentials are working correctly!");
    }

    private static String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }

    private static String maskSecret(String secret) {
        if (secret == null || secret.length() < 8) {
            return "***";
        }
        return secret.substring(0, 4) + "..." + secret.substring(secret.length() - 4);
    }
}
