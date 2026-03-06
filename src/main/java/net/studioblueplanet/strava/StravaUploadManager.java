package net.studioblueplanet.strava;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages Strava activity uploads with retry logic and duplicate detection
 * Implements exponential backoff and error classification for robust uploads
 */
public class StravaUploadManager {

    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 2000; // 2 seconds

    private final StravaApiClient apiClient;
    private final StravaDuplicateDetector duplicateDetector;
    private final StravaSportMapper sportMapper;
    private ProgressListener progressListener;

    /**
     * Progress listener interface for upload status updates
     */
    public interface ProgressListener {
        void onProgress(String status, String activityName);
    }

    /**
     * Creates a new upload manager
     *
     * @param apiClient the Strava API client
     * @param duplicateDetector the duplicate detector
     * @param sportMapper the sport type mapper
     */
    public StravaUploadManager(StravaApiClient apiClient, StravaDuplicateDetector duplicateDetector,
                               StravaSportMapper sportMapper) {
        this.apiClient = apiClient;
        this.duplicateDetector = duplicateDetector;
        this.sportMapper = sportMapper;
    }

    /**
     * Sets the progress listener for upload callbacks
     *
     * @param progressListener the listener to receive progress updates
     */
    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    /**
     * Uploads an activity to Strava with retry logic and duplicate detection
     *
     * @param activity the activity metadata (start_date, distance, type)
     * @param gpxData the GPX file content
     * @param name the activity name
     * @param description the activity description
     * @return the upload result
     */
    public UploadResult uploadActivity(JsonObject activity, String gpxData, String name, String description) {
        notifyProgress("Checking for duplicates", name);

        // Check for duplicates first
        try {
            String startDate = activity.get("start_date").getAsString();
            String endDate = startDate; // For single activity, start = end
            JsonArray existingActivities = apiClient.getAthleteActivities(startDate, endDate, 10);

            if (duplicateDetector.isDuplicate(activity, existingActivities)) {
                notifyProgress("Duplicate detected, skipping", name);
                return UploadResult.duplicate();
            }
        } catch (IOException e) {
            // If duplicate check fails, log but continue with upload
            notifyProgress("Warning: Duplicate check failed, proceeding with upload", name);
        }

        // Map sport type
        String localType = activity.get("type").getAsString();
        String stravaActivityType = sportMapper.mapToStravaActivityType(localType);

        // Attempt upload with retry logic
        notifyProgress("Uploading", name);

        int retryCount = 0;
        IOException lastException = null;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                JsonObject uploadResponse = apiClient.uploadActivity(gpxData, name, description, stravaActivityType);
                long uploadId = uploadResponse.get("id").getAsLong();

                notifyProgress("Upload successful", name);
                return UploadResult.success(uploadId, retryCount);

            } catch (IOException e) {
                lastException = e;

                // Check if error is retryable
                if (!isRetryableError(e)) {
                    notifyProgress("Upload failed: " + e.getMessage(), name);
                    return UploadResult.failure(e.getMessage(), retryCount);
                }

                // If not last attempt, retry with exponential backoff
                if (attempt < MAX_RETRIES) {
                    retryCount++;
                    long backoffMs = calculateBackoff(retryCount);

                    notifyProgress("Upload failed, retrying in " + (backoffMs / 1000) + "s (attempt " +
                        (attempt + 2) + "/" + (MAX_RETRIES + 1) + ")", name);

                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return UploadResult.failure("Upload interrupted", retryCount);
                    }
                }
            }
        }

        // All retries exhausted
        String errorMsg = "Upload failed after " + MAX_RETRIES + " retries: " +
            (lastException != null ? lastException.getMessage() : "Unknown error");
        notifyProgress(errorMsg, name);

        return UploadResult.failure(errorMsg, retryCount);
    }

    /**
     * Calculates exponential backoff delay
     * Formula: 2s * 2^(retryCount-1)
     * Results: 2s, 4s, 8s for retries 1, 2, 3
     *
     * @param retryCount the retry attempt number (1-based)
     * @return delay in milliseconds
     */
    private long calculateBackoff(int retryCount) {
        return (long) (BASE_BACKOFF_MS * Math.pow(2, retryCount - 1));
    }

    /**
     * Checks if an error is retryable (network/server errors)
     * Don't retry on client errors (400, 401, etc.)
     *
     * @param e the exception
     * @return true if error is retryable
     */
    private boolean isRetryableError(IOException e) {
        String message = e.getMessage().toLowerCase();

        // Don't retry on 4xx client errors
        if (message.contains("status 400") || message.contains("status 401") ||
            message.contains("status 403") || message.contains("status 404")) {
            return false;
        }

        // Retry on network errors, timeouts, and 5xx server errors
        return message.contains("timeout") || message.contains("connection") ||
               message.contains("network") || message.contains("status 5");
    }

    /**
     * Notifies progress listener if set
     *
     * @param status the status message
     * @param activityName the activity name
     */
    private void notifyProgress(String status, String activityName) {
        if (progressListener != null) {
            progressListener.onProgress(status, activityName);
        }
    }
}
