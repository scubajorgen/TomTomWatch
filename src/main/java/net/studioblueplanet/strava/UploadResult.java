package net.studioblueplanet.strava;

/**
 * Result of a Strava upload operation
 * Contains success status, upload ID, retry count, and error information
 */
public class UploadResult {

    private final boolean success;
    private final boolean duplicate;
    private final long uploadId;
    private final int retryCount;
    private final String errorMessage;

    private UploadResult(boolean success, boolean duplicate, long uploadId, int retryCount, String errorMessage) {
        this.success = success;
        this.duplicate = duplicate;
        this.uploadId = uploadId;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful upload result
     */
    public static UploadResult success(long uploadId, int retryCount) {
        return new UploadResult(true, false, uploadId, retryCount, null);
    }

    /**
     * Creates a duplicate result (successful but skipped)
     */
    public static UploadResult duplicate() {
        return new UploadResult(true, true, 0, 0, null);
    }

    /**
     * Creates a failed upload result
     */
    public static UploadResult failure(String errorMessage, int retryCount) {
        return new UploadResult(false, false, 0, retryCount, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public long getUploadId() {
        return uploadId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
