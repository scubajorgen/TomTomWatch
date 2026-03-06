package net.studioblueplanet.strava;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for StravaDuplicateDetector
 * Tests duplicate detection algorithm with time, distance, and sport type matching
 */
public class StravaDuplicateDetectorTest {

    private StravaDuplicateDetector detector;
    private StravaSportMapper sportMapper;

    @Before
    public void setUp() {
        sportMapper = new StravaSportMapper();
        detector = new StravaDuplicateDetector(sportMapper);
    }

    @Test
    public void testExactMatch() {
        // Local activity: 5000m run at 2026-03-06 10:00:00
        JsonObject localActivity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");

        // Strava has exact same activity
        JsonArray stravaActivities = new JsonArray();
        stravaActivities.add(createActivity("2026-03-06T10:00:00Z", 5000.0, "Run"));

        assertTrue("Exact match should be duplicate",
            detector.isDuplicate(localActivity, stravaActivities));
    }

    @Test
    public void testTimeWithinTolerance() {
        // Local activity: 5000m run at 10:00:00
        JsonObject localActivity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");

        // Strava activity 1 minute 30 seconds later (within ±2 minutes)
        JsonArray stravaActivities = new JsonArray();
        stravaActivities.add(createActivity("2026-03-06T10:01:30Z", 5000.0, "Run"));

        assertTrue("Time within 2 minutes should be duplicate",
            detector.isDuplicate(localActivity, stravaActivities));
    }

    @Test
    public void testTimeOutsideTolerance() {
        // Local activity: 5000m run at 10:00:00
        JsonObject localActivity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");

        // Strava activity 3 minutes later (outside ±2 minutes)
        JsonArray stravaActivities = new JsonArray();
        stravaActivities.add(createActivity("2026-03-06T10:03:00Z", 5000.0, "Run"));

        assertFalse("Time difference >2 minutes should not be duplicate",
            detector.isDuplicate(localActivity, stravaActivities));
    }

    @Test
    public void testDistanceWithinTolerance() {
        // Local activity: 10000m run
        JsonObject localActivity = createActivity("2026-03-06T10:00:00Z", 10000.0, "running");

        // Strava activity with 10050m (0.5% difference, within ±1%)
        JsonArray stravaActivities = new JsonArray();
        stravaActivities.add(createActivity("2026-03-06T10:00:00Z", 10050.0, "Run"));

        assertTrue("Distance within 1% should be duplicate",
            detector.isDuplicate(localActivity, stravaActivities));
    }

    @Test
    public void testDistanceOutsideTolerance() {
        // Local activity: 10000m run
        JsonObject localActivity = createActivity("2026-03-06T10:00:00Z", 10000.0, "running");

        // Strava activity with 10200m (2% difference, outside ±1%)
        JsonArray stravaActivities = new JsonArray();
        stravaActivities.add(createActivity("2026-03-06T10:00:00Z", 10200.0, "Run"));

        assertFalse("Distance difference >1% should not be duplicate",
            detector.isDuplicate(localActivity, stravaActivities));
    }

    @Test
    public void testDifferentSportType() {
        // Local activity: running
        JsonObject localActivity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");

        // Strava activity: cycling (different sport)
        JsonArray stravaActivities = new JsonArray();
        stravaActivities.add(createActivity("2026-03-06T10:00:00Z", 5000.0, "Ride"));

        assertFalse("Different sport types should not be duplicate",
            detector.isDuplicate(localActivity, stravaActivities));
    }

    @Test
    public void testSmallDistanceTolerance() {
        // Local activity: 500m run (short distance)
        JsonObject localActivity = createActivity("2026-03-06T10:00:00Z", 500.0, "running");

        // Strava activity with 530m (30m difference, within minimum ±50m)
        JsonArray stravaActivities = new JsonArray();
        stravaActivities.add(createActivity("2026-03-06T10:00:00Z", 530.0, "Run"));

        assertTrue("Distance within ±50m minimum should be duplicate",
            detector.isDuplicate(localActivity, stravaActivities));
    }

    @Test
    public void testNoMatchInMultipleActivities() {
        // Local activity: 5000m run at 10:00:00
        JsonObject localActivity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");

        // Multiple Strava activities, none matching
        JsonArray stravaActivities = new JsonArray();
        stravaActivities.add(createActivity("2026-03-06T08:00:00Z", 5000.0, "Run")); // Different time
        stravaActivities.add(createActivity("2026-03-06T10:00:00Z", 10000.0, "Run")); // Different distance
        stravaActivities.add(createActivity("2026-03-06T10:00:00Z", 5000.0, "Ride")); // Different sport

        assertFalse("No matching activity should not be duplicate",
            detector.isDuplicate(localActivity, stravaActivities));
    }

    @Test
    public void testMatchInMultipleActivities() {
        // Local activity: 5000m run at 10:00:00
        JsonObject localActivity = createActivity("2026-03-06T10:00:00Z", 5000.0, "running");

        // Multiple Strava activities, one matching
        JsonArray stravaActivities = new JsonArray();
        stravaActivities.add(createActivity("2026-03-06T08:00:00Z", 5000.0, "Run")); // Different time
        stravaActivities.add(createActivity("2026-03-06T10:01:00Z", 5010.0, "Run")); // MATCH!
        stravaActivities.add(createActivity("2026-03-06T12:00:00Z", 10000.0, "Ride")); // Different

        assertTrue("Should find duplicate in list of activities",
            detector.isDuplicate(localActivity, stravaActivities));
    }

    /**
     * Helper method to create activity JSON object
     */
    private JsonObject createActivity(String startDate, double distance, String type) {
        JsonObject activity = new JsonObject();
        activity.addProperty("start_date", startDate);
        activity.addProperty("distance", distance);
        activity.addProperty("type", type);
        return activity;
    }
}
