/*
 * Exception thrown when a Strava operation (authorization or upload) fails.
 */
package net.studioblueplanet.strava;

/**
 * Signals a failure during Strava authorization or activity upload. The message
 * is suitable for presenting to the user.
 * @author Claude
 */
public class StravaException extends Exception
{
    public StravaException(String message)
    {
        super(message);
    }

    public StravaException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
