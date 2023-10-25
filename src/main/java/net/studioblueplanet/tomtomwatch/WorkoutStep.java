/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

/**
 * This class represents a step in the workout. It is defined by an extent 
 * (or length) and an intensity
 * @author jorgen
 */
public class WorkoutStep
{
    /**
     * Type of the workout step
     */
    public enum StepType 
    {
        WARMUP  (1, "Warm Up"),
        WORK    (2, "Work"),
        REST    (3, "Rest"),
        COOLDOWN(5, "Cool Down");
        
        private final int    value;
        private final String description;
        
        /**
         * Constructor
         * @param i Enum value
         * @param description Human readable text representation
         */
        StepType(int i, String description)
        {
            this.value      = i;
            this.description=description;
        }

        /**
         * Get the enum based on the enum value passed
         * @param i Enum value
         * @return The enum or null if not found
         */
        public static StepType getWorkoutStepType(int i)
        {
            for (StepType w : StepType.values())
            {
                if (w.value == i)
                {
                    return w;
                }
            }
            return null;
        }    
        
        public int getValue()
        {
            return value;
        }
        
        @Override
        public String toString()
        {
            return description;
        }
    }
    
    /**
     * Extent of the workout step. In the TomTom MySports portal this is called
     * 'length'
     */
    public enum ExtentType
    {
        NONE       (0, ""),
        TIME       (1, "Time"),
        DISTANCE   (2, "Distance"),
        REACHHRZONE(4, "Reach HR zone"),
        MANUAL     (5, "Manual");
        
        private final int    value;
        private final String description;
        
        ExtentType(int value, String description)
        {
            this.value      =value;
            this.description=description;
        }
        
        public int getValue()
        {
            return value;
        }
        
        @Override
        public String toString()
        {
            return this.description;
        }
    }
    
    /**
     * Type or goal of the workout step intensity
     */
    public enum IntensityType
    {
        NONE   (0, ""),
        HRZONE (1, "HR Zone"),
        PACE   (2, "Pace"),
        SPEED  (3, "Speed"),
        CADENCE(4, "Cadence");
        
        private final String description;
        private final int    value;
        
        IntensityType(int value, String description)
        {
            this.value      =value;
            this.description=description;
        }
        
        public int getValue()
        {
            return value;
        }
        
        @Override
        public String toString()
        {
            return this.description;
        }        
    }
    
    /**
     * Heart rate zone
     */
    public enum HrZone
    {
        NONE   (0, "-"),
        EASY   (1, "Easy"),
        FATBURN(2, "Fat Burn"),
        CARDIO (3, "Cardio"),
        PERFORM(4, "Perform"),
        PEAK   (5, "Peak");
        
        private final int       value;
        private final String    description;
        
        /**
         * Constructor
         * @param i Enum value
         */
        HrZone(int i, String description)
        {
            this.value = i;
            this.description=description;
        }    
        
        /**
         * Get the enum based on the enum value passed
         * @param i Enum value
         * @return The enum or null if not found
         */
        public static HrZone getHrZoneType(int i)
        {
            for (HrZone zone : HrZone.values())
            {
                if (zone.value == i)
                {
                    return zone;
                }
            }
            return null;
        } 
        
        public int getValue()
        {
            return value;
        }
        
        @Override
        public String toString()
        {
            return description;
        }
    }
    
    
    private final int                   stepNo;
    private final String                name;
    private final String                description;
    private final StepType              type;
    private ExtentType                  stepExtent;
    private IntensityType               stepIntensity;
    
    // extents
    private HrZone                      extentReachHrZone;  
    private int                         extentDistance;     // In mm
    private int                         extentDuration;     // In sec
    
    // intensities
    private int                         intensityCadence;     // RPM
    private int                         intensityPace;        // ms/km
    private int                         intensitySpeed;       // mm/sec
    private HrZone                      intensityHrZone;      
    
    /**
     * Constructor. Defines the type of step
     * @param no Step number
     * @param name Name of the workout step
     * @param description Description of the workout step
     * @param type Type of the workout
     */
    public WorkoutStep(int no, String name, String description, StepType type)
    {
        this.stepNo         =no;
        this.name           =name;
        this.description    =description;
        this.type           =type;
        this.stepExtent     =ExtentType.NONE;
        this.stepIntensity  =IntensityType.NONE;
    }

    /**
     * Get the title of the step
     * @return The title
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the description of the step
     * @return The description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Get the type of the step
     * @return The type/phase in the workout, like WARM UP, WORK
     */
    public StepType getType()
    {
        return type;
    }
    
    /**
     * Set the workout step extent to distance
     * @param distance Distance in mm
     */
    public void setExtentDistance(int distance)
    {
        this.stepExtent            =ExtentType.DISTANCE;
        this.extentDistance         =distance;       
    }

    /**
     * Set the workout step extent to duration
     * @param duration Duration in seconds
     */
    public void setExtentDuration(int duration)
    {
        this.stepExtent            =ExtentType.TIME;
        this.extentDuration         =duration;       
    }
    
    /**
     * Set the workout step extent to reach HR Zone
     * @param zone HR Zone to reach
     */
    public void setExtentReachHrZone(HrZone zone)
    {
        this.stepExtent            =ExtentType.REACHHRZONE;
        this.extentReachHrZone      =zone;       
    }
 
    /**
     * Set the workout step extent to manual
     */
    public void setExtentManual()
    {
        this.stepExtent            =ExtentType.MANUAL;
    }
    
    /**
     * Returns the extent of the workout step. The extent is basically
     * the goal to reach in terms of duration, distance or HR zone to reach
     * @return The extent of the workout step
     */
    public ExtentType getStepExtent()
    {
        return this.stepExtent;
    }

    /**
     * Get the extent in terms of HR Zone to reach
     * @return The HR Zone to reach, or NONE if the extent type is not REACH HR ZONE
     */
    public HrZone getExtentReachHrZone()
    {
        if (stepExtent==ExtentType.REACHHRZONE)
        {
            return extentReachHrZone;
        }
        return HrZone.NONE;
    }

    /**
     * Get the extent in terms of distance to cover
     * @return The distance in mm, or -1 if the extent type is not DISTANCE
     */
    public int getExtentDistance()
    {
        if (stepExtent==ExtentType.DISTANCE)
        {
            return extentDistance;
        }
        return -1;
    }

    /**
     * Get the extent in terms of duration
     * @return The duration in s, or -1 if the extent type is not DURATION
     */
    public int getExtentTime()
    {
        if (stepExtent==ExtentType.TIME)
        {
            return extentDuration;
        }
        return -1;
    }

    /**
     * Sets the intensity of the step to cadence
     * @param cadence Cadence in RPM
     */
    public void setIntensityCadence(int cadence)
    {
        this.stepIntensity    =IntensityType.CADENCE;
        this.intensityCadence =cadence;
    }

    /**
     * Sets the intensity of the step to pace
     * @param pace Pace in milliseconds per km
     */
    public void setIntensityPace(int pace)
    {
        this.stepIntensity  =IntensityType.PACE;
        this.intensityPace  =pace;
    }

    /**
     * Sets the intensity of the step to speed
     * @param speed Pace in millimeters per per second
     */
    public void setIntensitySpeed(int speed)
    {
        this.stepIntensity  =IntensityType.SPEED;
        this.intensitySpeed =speed;
    }

    /**
     * Sets the intensity of the step to HR Zone at which the step must be 
     * performed
     * @param zone HR Zone
     */
    public void setIntensityHrZone(HrZone zone)
    {
        this.stepIntensity  =IntensityType.HRZONE;
        this.intensityHrZone=zone;
    }

    /**
     * Get the step intensity cadence
     * @return The pace in RPM or -1 if the step intensity is not of the PACE type
     */
    public int getIntensityCadence()
    {
        if (this.stepIntensity==IntensityType.CADENCE)
        {
            return intensityCadence;
        }
        return -1;
    }

    /**
     * Get the step intensity pace
     * @return The pace in msec/km or -1 if the step intensity is not of the PACE type
     */
    public int getIntensityPace()
    {
        if (this.stepIntensity==IntensityType.PACE)
        {
            return intensityPace;
        }
        return -1;
    }

    /**
     * Get the step intensity speed
     * @return The speed in mm/s or -1 if the step intensity is not of the SPEED type
     */
    public int getIntensitySpeed()
    {
        if (this.stepIntensity==IntensityType.SPEED)
        {
            return intensitySpeed;
        }
        return -1;
    }

    /**
     * Get the step intensity HR Zone
     * @return The HR Zone or NONE if the step intensity is not of the HRZONE type
     */
     public HrZone getIntensityHrZone()
    {
        if (this.stepIntensity==IntensityType.HRZONE)
        {
            return intensityHrZone;
        }
        return HrZone.NONE;
    }
    
    /**
     * Returns the intensity goal of the workout step
     * @return The intensity goal; the goal is optional
     */
    public IntensityType getIntensity()
    {
        return this.stepIntensity;
    }
    
    @Override
    public String toString()
    {
        String outputString;
        
        outputString=String.format("    %02d: %-9s - %-9s - %-13s ", stepNo, name, type, stepExtent);
        switch (stepExtent)
        {
            case TIME:
                outputString+=String.format("%4d sec", extentDuration);
                break;
            case DISTANCE:
                outputString+=String.format("%4d m  ", extentDistance/1000);
                break;
            case MANUAL:
                outputString+=String.format("        ");
                break;
            case REACHHRZONE:
                outputString+=String.format("%8s", this.extentReachHrZone);
                break;
        }
        switch (stepIntensity)
        {
            case CADENCE:
                outputString+=String.format(" @ %d RPM", intensityCadence);
                break;
            case SPEED:
                outputString+=String.format(" @ %5.1f km/h", (float)intensitySpeed/3600.0);
                break;
            case PACE:
                int min=intensityPace/60000;
                int sec=(intensityPace-60000*min)/1000;
                outputString+=String.format(" @ %d:%02d min/km", min, sec);
                break;
            case HRZONE:
                outputString+=String.format(" @ %s", intensityHrZone);
                break;
        }
        if (description!=null)
        {
            outputString+=String.format("\n        %s", description);
        }
        else
        {
            outputString+="\n";
        }
        outputString+="\n";
        return outputString;
    }
}
