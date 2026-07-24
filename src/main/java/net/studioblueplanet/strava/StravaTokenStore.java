/*
 * Persistent storage for the Strava OAuth2 tokens.
 */
package net.studioblueplanet.strava;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.studioblueplanet.logger.DebugLogger;

/**
 * Stores the Strava OAuth2 tokens (access token, refresh token and the moment
 * the access token expires) in a plain text properties file in the working
 * directory. The ConfigSettings class is read-only, hence the tokens - which
 * must survive across sessions - are kept in their own little store.
 * @author Claude
 */
public class StravaTokenStore
{
    private static final String     FILE_NAME       ="strava_tokens.properties";

    private static final String     KEY_ACCESS      ="accessToken";
    private static final String     KEY_REFRESH     ="refreshToken";
    private static final String     KEY_EXPIRES_AT  ="expiresAt";

    private final String            fileName;

    private String                  accessToken;
    private String                  refreshToken;
    // Unix time (seconds) at which the access token expires
    private long                    expiresAt;

    /**
     * Constructor. Reads the tokens from the store file (if present).
     */
    public StravaTokenStore()
    {
        this.fileName       =FILE_NAME;
        this.accessToken    ="";
        this.refreshToken   ="";
        this.expiresAt      =0L;
        this.load();
    }

    /**
     * Reads the tokens from the properties file. If the file does not exist
     * (first run, not authorized yet), the tokens remain empty.
     */
    private void load()
    {
        File file=new File(fileName);
        if (!file.exists())
        {
            DebugLogger.info("No Strava token file found ("+fileName+"), authorization required");
            return;
        }
        Properties properties=new Properties();
        try (FileInputStream in=new FileInputStream(file))
        {
            properties.load(in);
            this.accessToken    =properties.getProperty(KEY_ACCESS,     "");
            this.refreshToken   =properties.getProperty(KEY_REFRESH,    "");
            try
            {
                this.expiresAt  =Long.parseLong(properties.getProperty(KEY_EXPIRES_AT, "0"));
            }
            catch (NumberFormatException e)
            {
                this.expiresAt  =0L;
            }
            DebugLogger.info("Strava tokens loaded from "+fileName);
        }
        catch (IOException e)
        {
            DebugLogger.error("Error reading Strava token file "+fileName+": "+e.getMessage());
        }
    }

    /**
     * Stores the tokens and writes them to the properties file.
     * @param accessToken  The access token
     * @param refreshToken The refresh token
     * @param expiresAt    Unix time (seconds) at which the access token expires
     */
    public void store(String accessToken, String refreshToken, long expiresAt)
    {
        this.accessToken    =accessToken;
        this.refreshToken   =refreshToken;
        this.expiresAt      =expiresAt;
        this.save();
    }

    /**
     * Writes the current tokens to the properties file.
     */
    private void save()
    {
        Properties properties=new Properties();
        properties.setProperty(KEY_ACCESS,      this.accessToken);
        properties.setProperty(KEY_REFRESH,     this.refreshToken);
        properties.setProperty(KEY_EXPIRES_AT,  Long.toString(this.expiresAt));
        try (FileOutputStream out=new FileOutputStream(fileName))
        {
            properties.store(out, "TomTomWatch Strava OAuth2 tokens - do not share");
            DebugLogger.info("Strava tokens saved to "+fileName);
        }
        catch (IOException e)
        {
            DebugLogger.error("Error writing Strava token file "+fileName+": "+e.getMessage());
        }
    }

    /**
     * Indicates whether a refresh token is available. If so, the application
     * can obtain a valid access token without user interaction.
     * @return True if a refresh token is stored
     */
    public boolean hasRefreshToken()
    {
        return refreshToken!=null && !refreshToken.isEmpty();
    }

    public String getAccessToken()
    {
        return accessToken;
    }

    public String getRefreshToken()
    {
        return refreshToken;
    }

    public long getExpiresAt()
    {
        return expiresAt;
    }
}
