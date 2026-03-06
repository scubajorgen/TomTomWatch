package net.studioblueplanet.strava;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for StravaSportMapper
 * Tests the mapping of TomTom sport types to Strava activity types
 */
public class StravaSportMapperTest {

    private final StravaSportMapper mapper = new StravaSportMapper();

    @Test
    public void testRunMapsToRun() {
        assertEquals("Run", mapper.mapToStravaActivityType("running"));
    }

    @Test
    public void testCycleMapsToRide() {
        assertEquals("Ride", mapper.mapToStravaActivityType("cycling"));
    }

    @Test
    public void testSwimMapsToSwim() {
        assertEquals("Swim", mapper.mapToStravaActivityType("swimming"));
    }

    @Test
    public void testHikeMapsToHike() {
        assertEquals("Hike", mapper.mapToStravaActivityType("hiking"));
    }

    @Test
    public void testGymMapsToWorkout() {
        assertEquals("Workout", mapper.mapToStravaActivityType("gym"));
    }

    @Test
    public void testFreestyleMapsToWorkout() {
        assertEquals("Workout", mapper.mapToStravaActivityType("freestyle"));
    }

    @Test
    public void testUnknownSportDefaultsToWorkout() {
        assertEquals("Workout", mapper.mapToStravaActivityType("unknown_sport"));
    }

    @Test
    public void testNullSportDefaultsToWorkout() {
        assertEquals("Workout", mapper.mapToStravaActivityType(null));
    }
}
