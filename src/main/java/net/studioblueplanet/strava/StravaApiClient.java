package net.studioblueplanet.strava;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Client for interacting with the Strava API v3
 * Handles activity fetching, uploading, and status checking
 */
public class StravaApiClient {

    private static final String DEFAULT_API_BASE = "https://www.strava.com/api/v3";

    private final StravaConfig config;
    private final StravaAuthenticator authenticator;
    private final String apiBase;
    private final HttpClient httpClient;

    /**
     * Creates a new StravaApiClient with production endpoints
     *
     * @param config the Strava configuration
     * @param authenticator the OAuth authenticator
     */
    public StravaApiClient(StravaConfig config, StravaAuthenticator authenticator) {
        this(config, authenticator, null);
    }

    /**
     * Creates a new StravaApiClient with custom base URL (for testing)
     *
     * @param config the Strava configuration
     * @param authenticator the OAuth authenticator
     * @param apiBase custom API base URL (null for production)
     */
    public StravaApiClient(StravaConfig config, StravaAuthenticator authenticator, String apiBase) {
        this.config = config;
        this.authenticator = authenticator;
        this.apiBase = (apiBase != null) ? apiBase + "/api/v3" : DEFAULT_API_BASE;
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Fetches athlete activities within a date range
     *
     * @param startDate start date in ISO format (YYYY-MM-DD)
     * @param endDate end date in ISO format (YYYY-MM-DD)
     * @param perPage number of activities per page (max 200)
     * @return JSON array of activities
     * @throws IOException if request fails
     */
    public JsonArray getAthleteActivities(String startDate, String endDate, int perPage) throws IOException {
        String url = apiBase + "/athlete/activities?" +
            "after=" + startDate + "&" +
            "before=" + endDate + "&" +
            "per_page=" + perPage;

        String responseBody = executeGet(url);
        return JsonParser.parseString(responseBody).getAsJsonArray();
    }

    /**
     * Uploads an activity to Strava
     *
     * @param gpxData GPX file content
     * @param name activity name
     * @param description activity description
     * @param activityType Strava activity type (Run, Ride, etc.)
     * @return JSON object with upload ID and status
     * @throws IOException if upload fails
     */
    public JsonObject uploadActivity(String gpxData, String name, String description,
                                     String activityType) throws IOException {
        String url = apiBase + "/uploads";

        HttpPost post = new HttpPost(url);
        post.addHeader("Authorization", "Bearer " + config.getAccessToken());

        // Build multipart form data
        HttpEntity entity = MultipartEntityBuilder.create()
            .addBinaryBody("file", gpxData.getBytes(StandardCharsets.UTF_8),
                ContentType.APPLICATION_OCTET_STREAM, "activity.gpx")
            .addTextBody("name", name)
            .addTextBody("description", description)
            .addTextBody("activity_type", activityType)
            .addTextBody("data_type", "gpx")
            .build();

        post.setEntity(entity);

        HttpResponse response = httpClient.execute(post);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 401) {
            // Token expired, refresh and retry
            if (authenticator.refreshToken()) {
                post = new HttpPost(url);
                post.addHeader("Authorization", "Bearer " + config.getAccessToken());
                post.setEntity(entity);
                response = httpClient.execute(post);
                statusCode = response.getStatusLine().getStatusCode();
            }
        }

        handleRateLimitAndErrors(response, statusCode);

        String responseBody = EntityUtils.toString(response.getEntity());
        return JsonParser.parseString(responseBody).getAsJsonObject();
    }

    /**
     * Checks the status of an uploaded activity
     *
     * @param uploadId the upload ID returned from uploadActivity
     * @return JSON object with upload status
     * @throws IOException if request fails
     */
    public JsonObject getUploadStatus(long uploadId) throws IOException {
        String url = apiBase + "/uploads/" + uploadId;
        String responseBody = executeGet(url);
        return JsonParser.parseString(responseBody).getAsJsonObject();
    }

    /**
     * Executes a GET request with authorization and token refresh handling
     *
     * @param url the URL to request
     * @return response body as string
     * @throws IOException if request fails
     */
    private String executeGet(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        get.addHeader("Authorization", "Bearer " + config.getAccessToken());

        HttpResponse response = httpClient.execute(get);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 401) {
            // Token expired, refresh and retry
            if (authenticator.refreshToken()) {
                get = new HttpGet(url);
                get.addHeader("Authorization", "Bearer " + config.getAccessToken());
                response = httpClient.execute(get);
                statusCode = response.getStatusLine().getStatusCode();
            }
        }

        handleRateLimitAndErrors(response, statusCode);

        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Handles rate limit and error responses
     *
     * @param response the HTTP response
     * @param statusCode the status code
     * @throws IOException if rate limit exceeded or other error
     */
    private void handleRateLimitAndErrors(HttpResponse response, int statusCode) throws IOException {
        if (statusCode == 429) {
            String retryAfter = response.getFirstHeader("Retry-After") != null
                ? response.getFirstHeader("Retry-After").getValue()
                : "unknown";
            throw new IOException("Rate limit exceeded. Retry after " + retryAfter + " seconds.");
        }

        if (statusCode >= 400) {
            String errorBody = EntityUtils.toString(response.getEntity());
            throw new IOException("API request failed with status " + statusCode + ": " + errorBody);
        }
    }
}
