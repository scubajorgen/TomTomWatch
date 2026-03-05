package net.studioblueplanet.strava;

import java.io.*;
import java.util.Properties;

/**
 * Configuration manager for Strava integration
 * Handles loading and saving .env file with Strava API credentials and OAuth tokens
 *
 * @author TomTomWatch Strava Integration
 */
public class StravaConfig {

    private static final String KEY_CLIENT_ID = "STRAVA_CLIENT_ID";
    private static final String KEY_CLIENT_SECRET = "STRAVA_CLIENT_SECRET";
    private static final String KEY_ACCESS_TOKEN = "STRAVA_ACCESS_TOKEN";
    private static final String KEY_REFRESH_TOKEN = "STRAVA_REFRESH_TOKEN";
    private static final String KEY_TOKEN_EXPIRES_AT = "STRAVA_TOKEN_EXPIRES_AT";

    private final String envFilePath;
    private final Properties properties;

    /**
     * Constructor - loads configuration from .env file
     *
     * @param envFilePath Path to .env file
     * @throws IllegalArgumentException if file doesn't exist or required fields are missing
     */
    public StravaConfig(String envFilePath) {
        this.envFilePath = envFilePath;
        this.properties = new Properties();

        // Load .env file
        File envFile = new File(envFilePath);
        if (!envFile.exists()) {
            throw new IllegalArgumentException(".env file not found: " + envFilePath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse key=value
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    properties.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading .env file: " + e.getMessage(), e);
        }

        // Validate required fields
        validateRequiredFields();
    }

    /**
     * Validates that required configuration fields are present
     *
     * @throws IllegalArgumentException if required fields are missing
     */
    private void validateRequiredFields() {
        if (getClientId() == null || getClientId().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: " + KEY_CLIENT_ID);
        }
        if (getClientSecret() == null || getClientSecret().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: " + KEY_CLIENT_SECRET);
        }
    }

    /**
     * Get Strava API Client ID
     *
     * @return Client ID
     */
    public String getClientId() {
        return properties.getProperty(KEY_CLIENT_ID);
    }

    /**
     * Get Strava API Client Secret
     *
     * @return Client Secret
     */
    public String getClientSecret() {
        return properties.getProperty(KEY_CLIENT_SECRET);
    }

    /**
     * Get OAuth Access Token
     *
     * @return Access Token or null if not set
     */
    public String getAccessToken() {
        return properties.getProperty(KEY_ACCESS_TOKEN);
    }

    /**
     * Get OAuth Refresh Token
     *
     * @return Refresh Token or null if not set
     */
    public String getRefreshToken() {
        return properties.getProperty(KEY_REFRESH_TOKEN);
    }

    /**
     * Get token expiration time as Unix timestamp (seconds since epoch)
     *
     * @return Expiration timestamp or 0 if not set
     */
    public long getTokenExpiresAt() {
        String expiresAtStr = properties.getProperty(KEY_TOKEN_EXPIRES_AT);
        if (expiresAtStr == null || expiresAtStr.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(expiresAtStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Save OAuth tokens to .env file
     *
     * @param accessToken Access token
     * @param refreshToken Refresh token
     * @param expiresAt Token expiration time (Unix timestamp)
     * @throws IOException if error writing to file
     */
    public void saveTokens(String accessToken, String refreshToken, long expiresAt) throws IOException {
        // Update properties in memory
        properties.setProperty(KEY_ACCESS_TOKEN, accessToken);
        properties.setProperty(KEY_REFRESH_TOKEN, refreshToken);
        properties.setProperty(KEY_TOKEN_EXPIRES_AT, String.valueOf(expiresAt));

        // Write all properties back to file
        StringBuilder content = new StringBuilder();
        content.append("# Strava API Credentials\n");
        content.append(KEY_CLIENT_ID).append("=").append(getClientId()).append("\n");
        content.append(KEY_CLIENT_SECRET).append("=").append(getClientSecret()).append("\n");
        content.append("\n");
        content.append("# Auto-generated OAuth tokens\n");
        content.append(KEY_ACCESS_TOKEN).append("=").append(accessToken).append("\n");
        content.append(KEY_REFRESH_TOKEN).append("=").append(refreshToken).append("\n");
        content.append(KEY_TOKEN_EXPIRES_AT).append("=").append(expiresAt).append("\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(envFilePath))) {
            writer.write(content.toString());
        }
    }
}
