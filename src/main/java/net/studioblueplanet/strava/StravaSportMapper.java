/*
 * Maps TomTom activity types to Strava sport_type values.
 */
package net.studioblueplanet.strava;

import java.util.HashMap;
import java.util.Map;

import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.settings.ConfigSettings;

/**
 * Maps a TomTom activity description (e.g. "Cycling", "IndoorCycling") to a
 * Strava sport_type (e.g. "Ride", "VirtualRide"). A set of sensible defaults is
 * built in; individual mappings can be overridden through the "stravaSportMap"
 * setting, a comma separated list of "TomTomActivity:StravaSportType" pairs,
 * for example: "IndoorCycling:VirtualRide, Skiing:NordicSki".
 * @author Claude
 */
public class StravaSportMapper
{
    /** Fallback sport_type for unknown/unmapped activities */
    private static final String         DEFAULT_SPORT="Workout";

    private static StravaSportMapper    theInstance;

    // Keyed on the lower-cased TomTom activity description
    private final Map<String, String>   map;

    private StravaSportMapper()
    {
        map=new HashMap<>();
        // Built-in defaults (TomTom activity -> Strava sport_type)
        put("running",       "Run");
        put("trailrunning",  "TrailRun");
        put("treadmill",     "Run");
        put("cycling",       "Ride");
        put("indoorcycling", "VirtualRide");
        put("swimming",      "Swim");
        put("hiking",        "Hike");
        put("gym",           "WeightTraining");
        put("freestyle",     "Workout");
        put("skiing",        "AlpineSki");
        put("snowboarding",  "Snowboard");

        // Apply overrides from the configuration
        parseOverrides(ConfigSettings.getInstance().getStringValue("stravaSportMap"));
    }

    /**
     * @return The one and only instance of this class
     */
    public static StravaSportMapper getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new StravaSportMapper();
        }
        return theInstance;
    }

    private void put(String tomtomActivity, String sportType)
    {
        map.put(tomtomActivity.toLowerCase(), sportType);
    }

    /**
     * Parses the "stravaSportMap" override string and applies it on top of the
     * built-in defaults. Malformed entries are ignored (with a log message).
     * @param overrides Comma separated "activity:sport" pairs, may be empty
     */
    private void parseOverrides(String overrides)
    {
        if (overrides==null || overrides.trim().isEmpty())
        {
            return;
        }
        for (String pair : overrides.split(","))
        {
            int colon=pair.indexOf(':');
            if (colon>0 && colon<pair.length()-1)
            {
                String activity =pair.substring(0, colon).trim();
                String sport    =pair.substring(colon+1).trim();
                if (!activity.isEmpty() && !sport.isEmpty())
                {
                    put(activity, sport);
                    DebugLogger.info("Strava sport override: "+activity+" -> "+sport);
                    continue;
                }
            }
            DebugLogger.error("Ignoring malformed stravaSportMap entry: '"+pair.trim()+"'");
        }
    }

    /**
     * Returns the Strava sport_type for the given TomTom activity description.
     * @param activityDescription TomTom activity, e.g. "Cycling" (may be null)
     * @return The mapped Strava sport_type, or the default when unmapped
     */
    public String toSportType(String activityDescription)
    {
        if (activityDescription!=null)
        {
            String sport=map.get(activityDescription.toLowerCase());
            if (sport!=null)
            {
                return sport;
            }
        }
        return DEFAULT_SPORT;
    }
}
