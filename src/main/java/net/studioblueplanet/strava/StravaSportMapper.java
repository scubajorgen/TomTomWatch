package net.studioblueplanet.strava;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps TomTom sport types to Strava activity types
 * Provides a default mapping of "Workout" for unknown or unmapped sport types
 */
public class StravaSportMapper {

    private final Map<String, String> sportTypeMap;

    /**
     * Constructs a new StravaSportMapper with default TomTom-to-Strava mappings
     */
    public StravaSportMapper() {
        this.sportTypeMap = new HashMap<>();
        initializeMappings();
    }

    /**
     * Initializes the TomTom to Strava sport type mappings
     */
    private void initializeMappings() {
        sportTypeMap.put("running", "Run");
        sportTypeMap.put("cycling", "Ride");
        sportTypeMap.put("swimming", "Swim");
        sportTypeMap.put("hiking", "Hike");
        sportTypeMap.put("gym", "Workout");
        sportTypeMap.put("freestyle", "Workout");
    }

    /**
     * Maps a TomTom sport type to a Strava activity type
     *
     * @param tomtomSportType the TomTom sport type (e.g., "running", "cycling")
     * @return the corresponding Strava activity type (e.g., "Run", "Ride"), or "Workout" for unknown types
     */
    public String mapToStravaActivityType(String tomtomSportType) {
        if (tomtomSportType == null) {
            return "Workout";
        }
        return sportTypeMap.getOrDefault(tomtomSportType.toLowerCase(), "Workout");
    }
}
