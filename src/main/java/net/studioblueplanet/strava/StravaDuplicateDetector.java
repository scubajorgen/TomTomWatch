package net.studioblueplanet.strava;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Detects duplicate activities between local and Strava activities
 * Uses time (±2 min), distance (±1% or ±50m), and sport type matching
 */
public class StravaDuplicateDetector {

    private static final long TIME_TOLERANCE_SECONDS = 120; // ±2 minutes
    private static final double DISTANCE_TOLERANCE_PERCENT = 0.01; // ±1%
    private static final double DISTANCE_TOLERANCE_MIN_METERS = 50.0; // ±50m minimum

    private final StravaSportMapper sportMapper;

    /**
     * Creates a new duplicate detector
     *
     * @param sportMapper mapper for converting sport types
     */
    public StravaDuplicateDetector(StravaSportMapper sportMapper) {
        this.sportMapper = sportMapper;
    }

    /**
     * Checks if a local activity is a duplicate of any Strava activity
     *
     * @param localActivity the local activity to check
     * @param stravaActivities array of Strava activities to compare against
     * @return true if duplicate found, false otherwise
     */
    public boolean isDuplicate(JsonObject localActivity, JsonArray stravaActivities) {
        // Extract local activity properties
        String localStartDate = localActivity.get("start_date").getAsString();
        double localDistance = localActivity.get("distance").getAsDouble();
        String localType = localActivity.get("type").getAsString();

        // Map local sport type to Strava activity type
        String localStravaType = sportMapper.mapToStravaActivityType(localType);

        Instant localTime = Instant.parse(localStartDate);

        // Check each Strava activity for match
        for (int i = 0; i < stravaActivities.size(); i++) {
            JsonObject stravaActivity = stravaActivities.get(i).getAsJsonObject();

            String stravaStartDate = stravaActivity.get("start_date").getAsString();
            double stravaDistance = stravaActivity.get("distance").getAsDouble();
            String stravaType = stravaActivity.get("type").getAsString();

            Instant stravaTime = Instant.parse(stravaStartDate);

            // Check if this activity matches all criteria
            if (isTimeMatch(localTime, stravaTime) &&
                isDistanceMatch(localDistance, stravaDistance) &&
                isSportTypeMatch(localStravaType, stravaType)) {
                return true; // Found a duplicate
            }
        }

        return false; // No duplicate found
    }

    /**
     * Checks if two times are within tolerance (±2 minutes)
     *
     * @param time1 first time
     * @param time2 second time
     * @return true if times match within tolerance
     */
    private boolean isTimeMatch(Instant time1, Instant time2) {
        long secondsDiff = Math.abs(ChronoUnit.SECONDS.between(time1, time2));
        return secondsDiff <= TIME_TOLERANCE_SECONDS;
    }

    /**
     * Checks if two distances are within tolerance
     * Uses max(±1% of distance, ±50m) as tolerance
     *
     * @param distance1 first distance in meters
     * @param distance2 second distance in meters
     * @return true if distances match within tolerance
     */
    private boolean isDistanceMatch(double distance1, double distance2) {
        double diff = Math.abs(distance1 - distance2);

        // Calculate tolerance: max of 1% of distance or 50m minimum
        double percentTolerance = Math.max(distance1, distance2) * DISTANCE_TOLERANCE_PERCENT;
        double tolerance = Math.max(percentTolerance, DISTANCE_TOLERANCE_MIN_METERS);

        return diff <= tolerance;
    }

    /**
     * Checks if two sport types match
     *
     * @param type1 first sport type (Strava format)
     * @param type2 second sport type (Strava format)
     * @return true if types match
     */
    private boolean isSportTypeMatch(String type1, String type2) {
        return type1.equals(type2);
    }
}
