package net.studioblueplanet.strava;

import com.google.gson.JsonObject;
import net.studioblueplanet.strava.ui.DateRangeDialog;
import net.studioblueplanet.strava.ui.ProgressDialog;
import net.studioblueplanet.strava.ui.SummaryDialog;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.GpxWriter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Orchestrates the complete Strava sync workflow
 * Manages UI dialogs, OAuth authorization, and activity uploads
 */
public class StravaSyncManager {

    private static final String ENV_FILE = ".env";

    private final Frame parentFrame;
    private StravaConfig config;
    private StravaAuthenticator authenticator;
    private StravaApiClient apiClient;
    private StravaSportMapper sportMapper;
    private StravaDuplicateDetector duplicateDetector;
    private StravaUploadManager uploadManager;

    /**
     * Creates a new sync manager
     *
     * @param parentFrame the parent frame for dialogs
     */
    public StravaSyncManager(Frame parentFrame) {
        this.parentFrame = parentFrame;
        initializeComponents();
    }

    /**
     * Initializes Strava components
     */
    private void initializeComponents() {
        config = new StravaConfig(ENV_FILE);
        authenticator = new StravaAuthenticator(config);
        apiClient = new StravaApiClient(config, authenticator);
        sportMapper = new StravaSportMapper();
        duplicateDetector = new StravaDuplicateDetector(sportMapper);
        uploadManager = new StravaUploadManager(apiClient, duplicateDetector, sportMapper);
    }

    /**
     * Starts the sync workflow
     * Checks authorization, shows dialogs, and uploads activities
     *
     * @param activities list of activities to sync
     */
    public void startSync(List<Activity> activities) {
        // Run in background thread
        new Thread(() -> {
            try {
                // Step 1: Check/perform OAuth authorization
                if (!ensureAuthorized()) {
                    return;
                }

                // Step 2: Show date range dialog
                DateRangeDialog dateDialog = new DateRangeDialog(parentFrame);
                if (!dateDialog.showDialog()) {
                    return; // User cancelled
                }

                Date startDate = dateDialog.getStartDate();
                Date endDate = dateDialog.getEndDate();

                // Step 3: Filter activities by date range
                List<Activity> filteredActivities = filterActivitiesByDateRange(activities, startDate, endDate);

                if (filteredActivities.isEmpty()) {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(parentFrame,
                            "No activities found in selected date range",
                            "No Activities",
                            JOptionPane.INFORMATION_MESSAGE));
                    return;
                }

                // Step 4: Show progress dialog and upload
                uploadActivities(filteredActivities);

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(parentFrame,
                        "Error during sync: " + e.getMessage(),
                        "Sync Error",
                        JOptionPane.ERROR_MESSAGE));
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Ensures OAuth authorization is complete
     *
     * @return true if authorized
     */
    private boolean ensureAuthorized() {
        String accessToken = config.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            // Need to authorize
            String authUrl = authenticator.generateAuthorizationUrl();

            SwingUtilities.invokeLater(() -> {
                JTextArea textArea = new JTextArea(
                    "Strava authorization required.\n\n" +
                    "1. Click OK to open authorization page in browser\n" +
                    "2. Authorize the application\n" +
                    "3. Copy the 'code' parameter from the redirect URL\n" +
                    "4. Paste it in the next dialog\n\n" +
                    "URL: " + authUrl);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);

                int result = JOptionPane.showConfirmDialog(parentFrame,
                    new JScrollPane(textArea),
                    "Authorization Required",
                    JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    // Try to open browser
                    try {
                        Desktop.getDesktop().browse(new java.net.URI(authUrl));
                    } catch (Exception e) {
                        // Browser open failed, user can copy URL manually
                    }

                    // Prompt for authorization code
                    String code = JOptionPane.showInputDialog(parentFrame,
                        "Enter the authorization code from the redirect URL:",
                        "Authorization Code",
                        JOptionPane.QUESTION_MESSAGE);

                    if (code != null && !code.trim().isEmpty()) {
                        try {
                            if (authenticator.exchangeCodeForTokens(code.trim())) {
                                config = new StravaConfig(ENV_FILE); // Reload config
                                JOptionPane.showMessageDialog(parentFrame,
                                    "Authorization successful!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(parentFrame,
                                    "Authorization failed. Please try again.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(parentFrame,
                                "Error during authorization: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            return false;
        }

        // Check if token expired
        if (authenticator.isTokenExpired()) {
            try {
                if (authenticator.refreshToken()) {
                    config = new StravaConfig(ENV_FILE); // Reload config
                    return true;
                } else {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(parentFrame,
                            "Failed to refresh access token. Please re-authorize.",
                            "Token Expired",
                            JOptionPane.WARNING_MESSAGE));
                    return false;
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(parentFrame,
                        "Error refreshing token: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE));
                return false;
            }
        }

        return true;
    }

    /**
     * Filters activities by date range
     */
    private List<Activity> filterActivitiesByDateRange(List<Activity> activities, Date startDate, Date endDate) {
        List<Activity> filtered = new ArrayList<>();

        for (Activity activity : activities) {
            Date activityDate = new Date(activity.getStartDateTime().getMilliseconds(TimeZone.getDefault()));
            if (!activityDate.before(startDate) && !activityDate.after(endDate)) {
                filtered.add(activity);
            }
        }

        return filtered;
    }

    /**
     * Uploads activities with progress dialog
     */
    private void uploadActivities(List<Activity> activities) {
        ProgressDialog progressDialog = new ProgressDialog(parentFrame);

        // Set up progress listener
        uploadManager.setProgressListener((status, activityName) ->
            progressDialog.addLogMessage(status + ": " + activityName));

        // Show dialog
        SwingUtilities.invokeLater(() -> progressDialog.showDialog());

        // Statistics
        int total = activities.size();
        int uploaded = 0;
        int skipped = 0;
        int failed = 0;
        List<String> failedActivities = new ArrayList<>();

        // Upload each activity
        for (int i = 0; i < activities.size(); i++) {
            if (progressDialog.isCancelled()) {
                progressDialog.addLogMessage("Sync cancelled by user");
                break;
            }

            Activity activity = activities.get(i);
            String activityName = "Activity " + activity.getStartDateTime().format("YYYY-MM-DD hh:mm");

            progressDialog.updateProgress(i + 1, total, activityName);
            progressDialog.addLogMessage("Processing: " + activityName);

            try {
                // Convert to GPX
                StringWriter gpxWriter = new StringWriter();
                GpxWriter.getInstance().writeTrackToFile(gpxWriter, activity, "TomTomWatch");
                String gpxData = gpxWriter.toString();

                // Create activity metadata
                JsonObject metadata = new JsonObject();
                metadata.addProperty("start_date", activity.getStartDateTime().format("YYYY-MM-DDThh:mm:ssZ"));
                metadata.addProperty("distance", activity.getDistance());
                metadata.addProperty("type", mapActivityType(activity.getActivityType()));

                // Upload
                UploadResult result = uploadManager.uploadActivity(metadata, gpxData, activityName,
                    "Uploaded from TomTom Watch");

                if (result.isSuccess()) {
                    if (result.isDuplicate()) {
                        skipped++;
                    } else {
                        uploaded++;
                    }
                } else {
                    failed++;
                    failedActivities.add(activityName + ": " + result.getErrorMessage());
                }

            } catch (Exception e) {
                failed++;
                failedActivities.add(activityName + ": " + e.getMessage());
                progressDialog.addLogMessage("Error: " + e.getMessage());
            }
        }

        // Update final progress
        progressDialog.setCompleted(failed == 0);
        progressDialog.addLogMessage(String.format("Sync complete: %d uploaded, %d skipped, %d failed",
            uploaded, skipped, failed));

        // Show summary
        final int finalUploaded = uploaded;
        final int finalSkipped = skipped;
        final int finalFailed = failed;
        SwingUtilities.invokeLater(() -> {
            progressDialog.closeDialog();
            SummaryDialog summaryDialog = new SummaryDialog(parentFrame,
                total, finalUploaded, finalSkipped, finalFailed, failedActivities);
            summaryDialog.showDialog();
        });
    }

    /**
     * Maps activity type code to sport type string
     */
    private String mapActivityType(int typeCode) {
        switch (typeCode) {
            case 0: return "running";
            case 1: return "cycling";
            case 2: return "swimming";
            case 3: return "running"; // treadmill
            case 4: return "freestyle";
            case 5: return "gym";
            case 6: return "hiking";
            default: return "unknown";
        }
    }
}
