package net.studioblueplanet.strava;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Handles OAuth 2.0 authentication flow with Strava API
 * Manages authorization, token exchange, token refresh, and expiry checking
 */
public class StravaAuthenticator {

    private static final String DEFAULT_AUTH_URL = "https://www.strava.com/oauth/authorize";
    private static final String DEFAULT_TOKEN_URL = "https://www.strava.com/oauth/token";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String SCOPE = "activity:read_all,activity:write";

    private final StravaConfig config;
    private final String authUrl;
    private final String tokenUrl;

    /**
     * Creates a new StravaAuthenticator with production Strava endpoints
     *
     * @param config the Strava configuration with client credentials
     */
    public StravaAuthenticator(StravaConfig config) {
        this(config, null);
    }

    /**
     * Creates a new StravaAuthenticator with custom base URL (for testing)
     *
     * @param config the Strava configuration with client credentials
     * @param baseUrl custom base URL for API endpoints (null for production)
     */
    public StravaAuthenticator(StravaConfig config, String baseUrl) {
        this.config = config;
        if (baseUrl != null) {
            this.authUrl = baseUrl + "/oauth/authorize";
            this.tokenUrl = baseUrl + "/oauth/token";
        } else {
            this.authUrl = DEFAULT_AUTH_URL;
            this.tokenUrl = DEFAULT_TOKEN_URL;
        }
    }

    /**
     * Generates the Strava OAuth authorization URL
     *
     * @return the authorization URL for user to visit and authorize the app
     */
    public String generateAuthorizationUrl() {
        return authUrl +
            "?client_id=" + config.getClientId() +
            "&redirect_uri=" + REDIRECT_URI +
            "&response_type=code" +
            "&scope=" + SCOPE;
    }

    /**
     * Exchanges an authorization code for access and refresh tokens
     *
     * @param authCode the authorization code received from OAuth callback
     * @return true if token exchange was successful, false otherwise
     * @throws IOException if network error occurs
     */
    public boolean exchangeCodeForTokens(String authCode) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(tokenUrl);

        String jsonBody = "{" +
            "\"client_id\":\"" + config.getClientId() + "\"," +
            "\"client_secret\":\"" + config.getClientSecret() + "\"," +
            "\"code\":\"" + authCode + "\"," +
            "\"grant_type\":\"authorization_code\"" +
            "}";

        post.setEntity(new StringEntity(jsonBody));
        post.setHeader("Content-Type", "application/json");

        HttpResponse response = client.execute(post);
        String responseBody = EntityUtils.toString(response.getEntity());

        if (response.getStatusLine().getStatusCode() != 200) {
            return false;
        }

        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        if (!json.has("access_token")) {
            return false;
        }

        String accessToken = json.get("access_token").getAsString();
        String refreshToken = json.get("refresh_token").getAsString();
        long expiresAt = json.get("expires_at").getAsLong();

        config.saveTokens(accessToken, refreshToken, expiresAt);
        return true;
    }

    /**
     * Checks if the current access token is expired
     *
     * @return true if token is expired or missing, false if still valid
     */
    public boolean isTokenExpired() {
        long expiresAt = config.getTokenExpiresAt();
        if (expiresAt == 0) {
            return true; // No token set
        }

        long currentTime = System.currentTimeMillis() / 1000;
        return currentTime >= expiresAt;
    }

    /**
     * Refreshes the access token using the refresh token
     *
     * @return true if token refresh was successful, false otherwise
     * @throws IOException if network error occurs
     */
    public boolean refreshToken() throws IOException {
        String refreshToken = config.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            return false;
        }

        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(tokenUrl);

        String jsonBody = "{" +
            "\"client_id\":\"" + config.getClientId() + "\"," +
            "\"client_secret\":\"" + config.getClientSecret() + "\"," +
            "\"refresh_token\":\"" + refreshToken + "\"," +
            "\"grant_type\":\"refresh_token\"" +
            "}";

        post.setEntity(new StringEntity(jsonBody));
        post.setHeader("Content-Type", "application/json");

        HttpResponse response = client.execute(post);
        String responseBody = EntityUtils.toString(response.getEntity());

        if (response.getStatusLine().getStatusCode() != 200) {
            return false;
        }

        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        if (!json.has("access_token")) {
            return false;
        }

        String newAccessToken = json.get("access_token").getAsString();
        String newRefreshToken = json.get("refresh_token").getAsString();
        long expiresAt = json.get("expires_at").getAsLong();

        config.saveTokens(newAccessToken, newRefreshToken, expiresAt);
        return true;
    }
}
