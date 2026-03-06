package net.studioblueplanet.strava;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Unit tests for StravaAuthenticator
 * Tests OAuth 2.0 authentication flow with mocked Strava API
 */
public class StravaAuthenticatorTest {

    private static final String TEST_ENV_FILE = "test-oauth.env";
    private WireMockServer wireMockServer;
    private StravaAuthenticator authenticator;
    private StravaConfig config;

    @Before
    public void setUp() throws IOException {
        // Create test .env file
        createEnvFile(
            "STRAVA_CLIENT_ID=12345\n" +
            "STRAVA_CLIENT_SECRET=secret123\n" +
            "STRAVA_ACCESS_TOKEN=\n" +
            "STRAVA_REFRESH_TOKEN=\n" +
            "STRAVA_TOKEN_EXPIRES_AT=\n"
        );

        config = new StravaConfig(TEST_ENV_FILE);

        // Start WireMock server for mocking Strava API
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        authenticator = new StravaAuthenticator(config, "http://localhost:8089");
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
    public void testGenerateAuthorizationUrl() {
        String url = authenticator.generateAuthorizationUrl();

        assertTrue("URL should contain client_id", url.contains("client_id=12345"));
        assertTrue("URL should contain redirect_uri", url.contains("redirect_uri="));
        assertTrue("URL should contain response_type=code", url.contains("response_type=code"));
        assertTrue("URL should contain scope", url.contains("scope=activity"));
    }

    @Test
    public void testExchangeCodeForTokens() throws IOException {
        // Mock Strava token exchange endpoint
        stubFor(post(urlEqualTo("/oauth/token"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{"
                    + "\"access_token\":\"test_access_token\","
                    + "\"refresh_token\":\"test_refresh_token\","
                    + "\"expires_at\":9999999999"
                    + "}")));

        boolean success = authenticator.exchangeCodeForTokens("test_auth_code");

        assertTrue("Token exchange should succeed", success);
        assertEquals("test_access_token", config.getAccessToken());
        assertEquals("test_refresh_token", config.getRefreshToken());
        assertEquals(9999999999L, config.getTokenExpiresAt());
    }

    @Test
    public void testRefreshExpiredToken() throws IOException {
        // Create new config file with expired token
        long expiredTime = System.currentTimeMillis() / 1000 - 3600;
        createEnvFile(
            "STRAVA_CLIENT_ID=12345\n" +
            "STRAVA_CLIENT_SECRET=secret123\n" +
            "STRAVA_ACCESS_TOKEN=old_token\n" +
            "STRAVA_REFRESH_TOKEN=refresh_token_123\n" +
            "STRAVA_TOKEN_EXPIRES_AT=" + expiredTime + "\n"
        );

        // Reload config and authenticator with expired token
        config = new StravaConfig(TEST_ENV_FILE);
        authenticator = new StravaAuthenticator(config, "http://localhost:8089");

        // Mock Strava token refresh endpoint
        stubFor(post(urlEqualTo("/oauth/token"))
            .withRequestBody(containing("\"grant_type\":\"refresh_token\""))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{"
                    + "\"access_token\":\"new_access_token\","
                    + "\"refresh_token\":\"new_refresh_token\","
                    + "\"expires_at\":9999999999"
                    + "}")));

        boolean success = authenticator.refreshToken();

        assertTrue("Token refresh should succeed", success);

        // Reload config to get updated tokens from file
        config = new StravaConfig(TEST_ENV_FILE);
        assertEquals("new_access_token", config.getAccessToken());
        assertEquals("new_refresh_token", config.getRefreshToken());
    }

    @Test
    public void testTokenNotExpired() throws IOException {
        // Set token that expires in the future (1 hour from now)
        long futureExpiry = (System.currentTimeMillis() / 1000) + 3600;
        config.saveTokens("valid_token", "refresh_token", futureExpiry);

        assertFalse("Token should not be expired", authenticator.isTokenExpired());
    }

    @Test
    public void testTokenExpired() throws IOException {
        // Set token that expired 1 hour ago
        long pastExpiry = (System.currentTimeMillis() / 1000) - 3600;
        config.saveTokens("expired_token", "refresh_token", pastExpiry);

        assertTrue("Token should be expired", authenticator.isTokenExpired());
    }
}
