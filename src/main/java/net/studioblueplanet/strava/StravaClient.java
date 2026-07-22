/*
 * Client for uploading activities to Strava using the Strava V3 API.
 */
package net.studioblueplanet.strava;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpServer;

import org.json.JSONException;
import org.json.JSONObject;

import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.settings.ConfigSettings;

/**
 * Uploads an activity (GPX or TCX) to Strava. Handles the one-time OAuth2
 * authorization (via a temporary loopback web server) and transparently
 * refreshes the access token when it has expired.
 * @author Claude
 */
public class StravaClient
{
    /**
     * Callback used to report progress to the caller (e.g. the status window).
     */
    public interface ProgressListener
    {
        void progress(String message);
    }

    private static final String     AUTH_URL        ="https://www.strava.com/oauth/authorize";
    private static final String     TOKEN_URL       ="https://www.strava.com/oauth/token";
    private static final String     UPLOAD_URL      ="https://www.strava.com/api/v3/uploads";
    private static final String     SCOPE           ="activity:write";

    // Refresh the access token this many seconds before it actually expires
    private static final long       EXPIRY_MARGIN   =120L;

    // Authorization (browser consent) timeout
    private static final int        AUTH_TIMEOUT_S  =180;

    // Upload status polling
    private static final int        POLL_MAX_TRIES  =30;
    private static final long       POLL_INTERVAL_MS=2000L;

    private final ConfigSettings    settings;
    private final StravaTokenStore  tokenStore;

    // Filled in by the loopback callback during authorization
    private volatile String         authCode;
    private volatile String         authError;

    public StravaClient()
    {
        this.settings   =ConfigSettings.getInstance();
        this.tokenStore =new StravaTokenStore();
    }

    /**
     * Uploads an activity to Strava. Performs authorization if required and
     * refreshes the access token when it has expired.
     * @param data        The activity file content (GPX or TCX)
     * @param dataType    "gpx" or "tcx"
     * @param name        Name of the activity in Strava
     * @param description Description of the activity in Strava
     * @param listener    Progress listener, may be null
     * @return The Strava activity id of the uploaded activity
     * @throws StravaException When the upload could not be completed
     */
    public long uploadActivity(byte[] data, String dataType, String name, String description, ProgressListener listener) throws StravaException
    {
        checkCredentials();
        ensureAccessToken(listener);
        long uploadId   =postUpload(data, dataType, name, description, listener);
        long activityId =pollUpload(uploadId, listener);
        return activityId;
    }

    /**
     * Sets the sport type of an existing activity (PUT /activities/{id}). Uses
     * the current access token; call this right after uploadActivity(), while
     * the token is still valid.
     * @param activityId The Strava activity id
     * @param sportType  A Strava sport_type value (e.g. "Ride", "VirtualRide")
     * @throws StravaException When the update failed
     */
    public void updateSportType(long activityId, String sportType) throws StravaException
    {
        String  urlString="https://www.strava.com/api/v3/activities/"+activityId;
        byte[]  bytes    =("sport_type="+urlEncode(sportType)).getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn=null;
        try
        {
            conn=openConnection(urlString);
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Authorization", "Bearer "+tokenStore.getAccessToken());
            conn.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream os=conn.getOutputStream())
            {
                os.write(bytes);
            }

            int        code=conn.getResponseCode();
            JSONObject json=readJson(conn);
            if (code<200 || code>=300)
            {
                throw new StravaException("Setting Strava sport type failed ("+code+"): "+errorMessage(json));
            }
        }
        catch (IOException e)
        {
            throw new StravaException("Error setting Strava sport type: "+e.getMessage(), e);
        }
        finally
        {
            if (conn!=null)
            {
                conn.disconnect();
            }
        }
    }

    /**
     * Verifies that the Strava API credentials have been configured. Two modes
     * are supported: via a token proxy (stravaProxyUrl set, recommended - keeps
     * the client secret off this machine) or direct (stravaClientSecret set).
     * The client id is required in both modes (it is used to build the browser
     * authorization URL and is not secret).
     * @throws StravaException When the configuration is incomplete
     */
    private void checkCredentials() throws StravaException
    {
        if (getClientId().isEmpty())
        {
            throw new StravaException("Strava is not configured: stravaClientId is missing in tomtomwatch.properties "
                                    + "(register an app at https://www.strava.com/settings/api, callback domain 'localhost').");
        }
        if (!isProxyMode() && getClientSecret().isEmpty())
        {
            throw new StravaException("Strava is not configured. Set either stravaProxyUrl (recommended) or "
                                    + "stravaClientSecret in tomtomwatch.properties.");
        }
    }

    /**
     * Ensures a valid access token is available: refreshes it when expired or
     * starts the interactive authorization when no refresh token is present.
     * @param listener Progress listener, may be null
     * @throws StravaException When a valid token could not be obtained
     */
    private void ensureAccessToken(ProgressListener listener) throws StravaException
    {
        if (tokenStore.hasRefreshToken())
        {
            long now=System.currentTimeMillis()/1000L;
            if (now>=tokenStore.getExpiresAt()-EXPIRY_MARGIN)
            {
                progress(listener, "Refreshing Strava access token\n");
                refreshAccessToken();
            }
        }
        else
        {
            authorize(listener);
        }
    }

    /**
     * Runs the interactive OAuth2 authorization code flow. Starts a temporary
     * web server on localhost, opens the browser to the Strava consent page and
     * waits for Strava to redirect back with the authorization code.
     * @param listener Progress listener, may be null
     * @throws StravaException When authorization failed or timed out
     */
    private void authorize(ProgressListener listener) throws StravaException
    {
        authCode    =null;
        authError   =null;

        HttpServer      server;
        CountDownLatch  latch=new CountDownLatch(1);

        try
        {
            server=HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        }
        catch (IOException e)
        {
            throw new StravaException("Could not start local web server for Strava authorization: "+e.getMessage(), e);
        }

        int    port         =server.getAddress().getPort();
        String redirectUri  ="http://localhost:"+port+"/";

        server.createContext("/", (exchange) ->
        {
            Map<String, String> params=parseQuery(exchange.getRequestURI().getRawQuery());
            String responseText;
            // Ignore unrelated requests (e.g. favicon) that carry no code/error
            if (params.containsKey("code") || params.containsKey("error"))
            {
                authCode    =params.get("code");
                authError   =params.get("error");
                responseText="<html><body><h2>TomTomWatch</h2><p>Strava authorization "
                           + (authError==null ? "succeeded" : "failed")
                           + ". You can close this tab and return to TomTomWatch.</p></body></html>";
            }
            else
            {
                responseText="<html><body>TomTomWatch</body></html>";
            }
            byte[] body=responseText.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os=exchange.getResponseBody())
            {
                os.write(body);
            }
            if (params.containsKey("code") || params.containsKey("error"))
            {
                latch.countDown();
            }
        });
        server.start();

        try
        {
            String authorizeUrl=AUTH_URL
                    + "?client_id="     + urlEncode(getClientId())
                    + "&response_type=" + "code"
                    + "&redirect_uri="  + urlEncode(redirectUri)
                    + "&approval_prompt=auto"
                    + "&scope="         + urlEncode(SCOPE);

            progress(listener, "Opening browser for Strava authorization...\n");
            if (!openBrowser(authorizeUrl))
            {
                progress(listener, "Could not open a browser automatically. Please open this URL manually:\n"
                                 + authorizeUrl + "\n");
            }

            boolean received=latch.await(AUTH_TIMEOUT_S, TimeUnit.SECONDS);
            if (!received)
            {
                throw new StravaException("Strava authorization timed out. Please try again.");
            }
            if (authError!=null)
            {
                throw new StravaException("Strava authorization was denied: "+authError);
            }
            if (authCode==null || authCode.isEmpty())
            {
                throw new StravaException("Strava authorization did not return an authorization code.");
            }

            progress(listener, "Authorization received, obtaining access token\n");
            exchangeCodeForToken(authCode);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new StravaException("Strava authorization was interrupted.", e);
        }
        finally
        {
            server.stop(0);
        }
    }

    /**
     * Exchanges the authorization code for an access and refresh token.
     * @param code The authorization code returned by Strava
     * @throws StravaException When the token could not be obtained
     */
    private void exchangeCodeForToken(String code) throws StravaException
    {
        JSONObject response;
        if (isProxyMode())
        {
            JSONObject request=new JSONObject();
            request.put("code", code);
            response=postToProxy("/token", request);
        }
        else
        {
            Map<String, String> form=new HashMap<>();
            form.put("client_id",       getClientId());
            form.put("client_secret",   getClientSecret());
            form.put("code",            code);
            form.put("grant_type",      "authorization_code");
            response=postForm(TOKEN_URL, form, null);
        }
        storeTokens(response);
    }

    /**
     * Refreshes the access token using the stored refresh token.
     * @throws StravaException When the token could not be refreshed
     */
    private void refreshAccessToken() throws StravaException
    {
        JSONObject response;
        if (isProxyMode())
        {
            JSONObject request=new JSONObject();
            request.put("refresh_token", tokenStore.getRefreshToken());
            response=postToProxy("/refresh", request);
        }
        else
        {
            Map<String, String> form=new HashMap<>();
            form.put("client_id",       getClientId());
            form.put("client_secret",   getClientSecret());
            form.put("grant_type",      "refresh_token");
            form.put("refresh_token",   tokenStore.getRefreshToken());
            response=postForm(TOKEN_URL, form, null);
        }
        storeTokens(response);
    }

    /**
     * Extracts the tokens from a Strava token endpoint response and stores them.
     * @param response The parsed JSON response
     * @throws StravaException When the response does not contain the tokens
     */
    private void storeTokens(JSONObject response) throws StravaException
    {
        try
        {
            String accessToken  =response.getString("access_token");
            String refreshToken =response.getString("refresh_token");
            long   expiresAt;
            if (response.has("expires_at"))
            {
                expiresAt=response.getLong("expires_at");
            }
            else
            {
                expiresAt=System.currentTimeMillis()/1000L+response.optLong("expires_in", 21600L);
            }
            tokenStore.store(accessToken, refreshToken, expiresAt);
        }
        catch (JSONException e)
        {
            throw new StravaException("Unexpected response from Strava token endpoint: "+e.getMessage(), e);
        }
    }

    /**
     * Posts the activity file to the Strava upload endpoint.
     * @return The upload id
     * @throws StravaException When the upload request failed
     */
    private long postUpload(byte[] data, String dataType, String name, String description, ProgressListener listener) throws StravaException
    {
        progress(listener, "Uploading activity to Strava ("+data.length+" bytes, "+dataType+")\n");

        String boundary="----TomTomWatchBoundary"+Long.toHexString(System.currentTimeMillis());
        byte[] payload;
        try
        {
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            appendFormField(baos, boundary, "data_type",   dataType);
            // Omit the name entirely when empty, so Strava auto-generates a title
            if (name!=null && !name.trim().isEmpty())
            {
                appendFormField(baos, boundary, "name",    name);
            }
            appendFormField(baos, boundary, "description", description);
            appendFileField(baos, boundary, "file", "activity."+dataType, data);
            baos.write(("--"+boundary+"--\r\n").getBytes(StandardCharsets.UTF_8));
            payload=baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new StravaException("Error preparing Strava upload: "+e.getMessage(), e);
        }

        HttpURLConnection conn=null;
        try
        {
            conn=openConnection(UPLOAD_URL);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer "+tokenStore.getAccessToken());
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
            conn.setFixedLengthStreamingMode(payload.length);
            try (OutputStream os=conn.getOutputStream())
            {
                os.write(payload);
            }

            int        code=conn.getResponseCode();
            JSONObject json=readJson(conn);
            if (code<200 || code>=300)
            {
                throw new StravaException("Strava upload failed ("+code+"): "+errorMessage(json));
            }
            return json.getLong("id");
        }
        catch (IOException e)
        {
            throw new StravaException("Error uploading to Strava: "+e.getMessage(), e);
        }
        catch (JSONException e)
        {
            throw new StravaException("Unexpected response from Strava upload endpoint: "+e.getMessage(), e);
        }
        finally
        {
            if (conn!=null)
            {
                conn.disconnect();
            }
        }
    }

    /**
     * Polls the Strava upload status until the activity has been processed.
     * @return The resulting Strava activity id
     * @throws StravaException When processing failed or timed out
     */
    private long pollUpload(long uploadId, ProgressListener listener) throws StravaException
    {
        progress(listener, "Waiting for Strava to process the activity\n");
        String statusUrl=UPLOAD_URL+"/"+uploadId;

        int tries=0;
        while (tries<POLL_MAX_TRIES)
        {
            HttpURLConnection conn=null;
            try
            {
                conn=openConnection(statusUrl);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer "+tokenStore.getAccessToken());

                int        code=conn.getResponseCode();
                JSONObject json=readJson(conn);
                if (code<200 || code>=300)
                {
                    throw new StravaException("Strava upload status failed ("+code+"): "+errorMessage(json));
                }

                String error=json.optString("error", null);
                if (error!=null && !error.isEmpty() && !"null".equals(error))
                {
                    // A duplicate is a normal outcome, not a real error. Strava's
                    // message typically ends with the existing activity id.
                    if (error.toLowerCase().contains("duplicate"))
                    {
                        throw new StravaDuplicateException(error, extractLastNumber(error));
                    }
                    throw new StravaException("Strava could not process the activity: "+error);
                }
                if (!json.isNull("activity_id"))
                {
                    return json.getLong("activity_id");
                }
                // Still processing; report the status message from Strava
                progress(listener, "  "+json.optString("status", "processing...")+"\n");
            }
            catch (IOException e)
            {
                throw new StravaException("Error reading Strava upload status: "+e.getMessage(), e);
            }
            catch (JSONException e)
            {
                throw new StravaException("Unexpected response from Strava upload status endpoint: "+e.getMessage(), e);
            }
            finally
            {
                if (conn!=null)
                {
                    conn.disconnect();
                }
            }

            try
            {
                Thread.sleep(POLL_INTERVAL_MS);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                throw new StravaException("Strava upload wait was interrupted.", e);
            }
            tries++;
        }
        throw new StravaException("Timed out waiting for Strava to process the activity.");
    }

    /* ******************************************************************************************** *\
     * HTTP helpers
    \* ******************************************************************************************** */

    /**
     * Posts an application/x-www-form-urlencoded form and returns the JSON response.
     * @throws StravaException When the request failed
     */
    private JSONObject postForm(String urlString, Map<String, String> form, String bearerToken) throws StravaException
    {
        StringBuilder body=new StringBuilder();
        for (Map.Entry<String, String> entry : form.entrySet())
        {
            if (body.length()>0)
            {
                body.append('&');
            }
            body.append(urlEncode(entry.getKey())).append('=').append(urlEncode(entry.getValue()));
        }
        byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn=null;
        try
        {
            conn=openConnection(urlString);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (bearerToken!=null)
            {
                conn.setRequestProperty("Authorization", "Bearer "+bearerToken);
            }
            conn.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream os=conn.getOutputStream())
            {
                os.write(bytes);
            }

            int        code=conn.getResponseCode();
            JSONObject json=readJson(conn);
            if (code<200 || code>=300)
            {
                throw new StravaException("Strava request failed ("+code+"): "+errorMessage(json));
            }
            return json;
        }
        catch (IOException e)
        {
            throw new StravaException("Error contacting Strava: "+e.getMessage(), e);
        }
    }

    /**
     * Posts a JSON body to the Strava token proxy (Cloudflare Worker) and
     * returns the JSON response. The proxy performs the actual token exchange
     * with Strava using the client secret, so the secret never reaches this app.
     * @param path Endpoint path ("/token" or "/refresh")
     * @param body The JSON request body
     * @return The parsed JSON response (Strava's token response, passed through)
     * @throws StravaException When the request failed
     */
    private JSONObject postToProxy(String path, JSONObject body) throws StravaException
    {
        String base=getProxyUrl();
        while (base.endsWith("/"))
        {
            base=base.substring(0, base.length()-1);
        }
        String urlString=base+path;
        byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn=null;
        try
        {
            conn=openConnection(urlString);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            String appKey=getProxyKey();
            if (!appKey.isEmpty())
            {
                conn.setRequestProperty("X-App-Key", appKey);
            }
            conn.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream os=conn.getOutputStream())
            {
                os.write(bytes);
            }

            int        code=conn.getResponseCode();
            JSONObject json=readJson(conn);
            if (code<200 || code>=300)
            {
                throw new StravaException("Strava proxy request failed ("+code+"): "+errorMessage(json));
            }
            return json;
        }
        catch (IOException e)
        {
            throw new StravaException("Error contacting Strava proxy: "+e.getMessage(), e);
        }
        finally
        {
            if (conn!=null)
            {
                conn.disconnect();
            }
        }
    }

    /**
     * Opens a connection to the given URL, honoring the configured proxy settings.
     */
    private HttpURLConnection openConnection(String urlString) throws IOException
    {
        URL url=new URL(urlString);
        HttpURLConnection conn;
        if (settings.getBooleanValue("proxyEnable"))
        {
            Proxy proxy=new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(settings.getStringValue("proxyHost"), settings.getIntValue("proxyPort")));
            conn=(HttpURLConnection)url.openConnection(proxy);
        }
        else
        {
            conn=(HttpURLConnection)url.openConnection();
        }
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        return conn;
    }

    /**
     * Reads the response body (from the input or error stream) and parses it as JSON.
     * @throws StravaException When the body could not be parsed as JSON
     */
    private JSONObject readJson(HttpURLConnection conn) throws StravaException
    {
        String body=readBody(conn);
        try
        {
            return new JSONObject(body);
        }
        catch (JSONException e)
        {
            throw new StravaException("Unexpected (non-JSON) response from Strava: "+body, e);
        }
    }

    /**
     * Reads the response body from the connection, using the error stream when
     * the request failed.
     */
    private String readBody(HttpURLConnection conn)
    {
        InputStream is=conn.getErrorStream();
        if (is==null)
        {
            try
            {
                is=conn.getInputStream();
            }
            catch (IOException e)
            {
                return "";
            }
        }
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        byte[] chunk=new byte[4096];
        int    n;
        try
        {
            while ((n=is.read(chunk))>0)
            {
                baos.write(chunk, 0, n);
            }
        }
        catch (IOException e)
        {
            DebugLogger.error("Error reading Strava response: "+e.getMessage());
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                // ignore
            }
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Builds a human readable error message from a Strava error response.
     */
    private String errorMessage(JSONObject json)
    {
        // Strava uses "message"/"errors"; the proxy uses "error"
        String message=json.optString("message", "");
        if (message.isEmpty())
        {
            message=json.optString("error", "");
        }
        if (json.has("errors"))
        {
            message+=" "+json.get("errors").toString();
        }
        if (message.trim().isEmpty())
        {
            message=json.toString();
        }
        return message.trim();
    }

    /**
     * Returns the last run of digits in the text as a long, or -1 if none.
     * Used to pull the existing activity id out of a Strava duplicate message
     * (e.g. "activity.gpx duplicate of activity 1234567890").
     */
    private static long extractLastNumber(String text)
    {
        Matcher matcher=Pattern.compile("\\d+").matcher(text);
        long    value  =-1;
        while (matcher.find())
        {
            try
            {
                value=Long.parseLong(matcher.group());
            }
            catch (NumberFormatException e)
            {
                // number too large to be an id; ignore
            }
        }
        return value;
    }

    /* ******************************************************************************************** *\
     * Utilities
    \* ******************************************************************************************** */

    private void appendFormField(ByteArrayOutputStream baos, String boundary, String name, String value) throws IOException
    {
        baos.write(("--"+boundary+"\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Disposition: form-data; name=\""+name+"\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write((value==null ? "" : value).getBytes(StandardCharsets.UTF_8));
        baos.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void appendFileField(ByteArrayOutputStream baos, String boundary, String name, String fileName, byte[] content) throws IOException
    {
        baos.write(("--"+boundary+"\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Disposition: form-data; name=\""+name+"\"; filename=\""+fileName+"\"\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write("Content-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        baos.write(content);
        baos.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parses a URL query string into a map of parameters.
     */
    private Map<String, String> parseQuery(String query)
    {
        Map<String, String> params=new HashMap<>();
        if (query==null || query.isEmpty())
        {
            return params;
        }
        for (String pair : query.split("&"))
        {
            int idx=pair.indexOf('=');
            if (idx>0)
            {
                String key  =urlDecode(pair.substring(0, idx));
                String value=urlDecode(pair.substring(idx+1));
                params.put(key, value);
            }
            else
            {
                params.put(urlDecode(pair), "");
            }
        }
        return params;
    }

    /**
     * Opens the given URL in the default browser.
     * @return True when the browser was launched, false otherwise
     */
    private boolean openBrowser(String url)
    {
        try
        {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
        }
        catch (Exception e)
        {
            DebugLogger.error("Could not open browser for Strava authorization: "+e.getMessage());
        }
        return false;
    }

    private void progress(ProgressListener listener, String message)
    {
        if (listener!=null)
        {
            listener.progress(message);
        }
    }

    private String getClientId()
    {
        return settings.getStringValue("stravaClientId").trim();
    }

    private String getClientSecret()
    {
        return settings.getStringValue("stravaClientSecret").trim();
    }

    private String getProxyUrl()
    {
        return settings.getStringValue("stravaProxyUrl").trim();
    }

    private String getProxyKey()
    {
        return settings.getStringValue("stravaProxyKey").trim();
    }

    /**
     * Indicates whether the token proxy is configured. When true, the code
     * exchange and token refresh go through the proxy and no client secret is
     * needed on this machine.
     * @return True if a proxy URL is configured
     */
    private boolean isProxyMode()
    {
        return !getProxyUrl().isEmpty();
    }

    private static String urlEncode(String value)
    {
        try
        {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (Exception e)
        {
            return value;
        }
    }

    private static String urlDecode(String value)
    {
        try
        {
            return java.net.URLDecoder.decode(value, "UTF-8");
        }
        catch (Exception e)
        {
            return value;
        }
    }
}
