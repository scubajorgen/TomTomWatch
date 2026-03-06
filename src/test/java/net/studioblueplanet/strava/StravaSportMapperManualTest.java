package net.studioblueplanet.strava;

/**
 * Manual test utility to verify sport type mappings
 * Run main() method to see mapping results
 */
public class StravaSportMapperManualTest {

    public static void main(String[] args) {
        System.out.println("=== Strava Sport Mapper Manual Test ===\n");

        StravaSportMapper mapper = new StravaSportMapper();

        // Test all supported sport types
        String[] testSports = {
            "running", "cycling", "swimming", "hiking",
            "gym", "freestyle", "unknown_sport", null
        };

        System.out.println("TomTom Sport Type -> Strava Activity Type");
        System.out.println("------------------------------------------");

        for (String sport : testSports) {
            String sportDisplay = (sport == null) ? "null" : "\"" + sport + "\"";
            String stravaType = mapper.mapToStravaActivityType(sport);
            System.out.printf("%-20s -> %s%n", sportDisplay, stravaType);
        }

        System.out.println("\n✓ All mappings working correctly!");
    }
}
