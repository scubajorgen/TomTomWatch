/*
 * Exception thrown when Strava rejects an upload because the activity already exists.
 */
package net.studioblueplanet.strava;

/**
 * Signals that Strava refused the upload because the activity is a duplicate of
 * one already present on the athlete's account. This is a normal, expected
 * outcome (re-uploading the same activity), not a real error. When Strava
 * reports the id of the existing activity, it is available here.
 * @author Claude
 */
public class StravaDuplicateException extends StravaException
{
    /** Id of the already-existing Strava activity, or -1 when unknown */
    private final long existingActivityId;

    public StravaDuplicateException(String message, long existingActivityId)
    {
        super(message);
        this.existingActivityId=existingActivityId;
    }

    /**
     * @return The id of the existing Strava activity, or -1 if it could not be determined
     */
    public long getExistingActivityId()
    {
        return existingActivityId;
    }
}
