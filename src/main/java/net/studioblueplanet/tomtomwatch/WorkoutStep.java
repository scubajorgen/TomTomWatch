/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

/**
 *
 * @author jorgen
 */
public class WorkoutStep
{
    /**
     * Type of the workout step
     */
    public enum WorkoutStepType 
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
        WorkoutStepType(int i, String description)
        {
            this.value      = i;
            this.description=description;
        }

        /**
         * Get the enum based on the enum value passed
         * @param i Enum value
         * @return The enum or null if not found
         */
        public static WorkoutStepType getWorkoutStepType(int i)
        {
            for (WorkoutStepType w : WorkoutStepType.values())
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
     * Extent of the workout step
     */
    public enum WorkoutStepExtentType
    {
        NONE       (0, ""),
        DURATION   (1, "Duration"),
        DISTANCE   (2, "Distance"),
        REACHHRZONE(4, "Reach HR zone"),
        MANUAL     (5, "Manual");
        
        private final int    value;
        private final String description;
        
        WorkoutStepExtentType(int value, String description)
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
    public enum WorkoutStepIntensityType
    {
        NONE  (0, ""),
        HRZONE(1, "HR Zone"),
        PACE  (2, "Pace"),
        SPEED (3, "Speed");
        
        private final String description;
        private final int    value;
        
        WorkoutStepIntensityType(int value, String description)
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
    
    
    private final int                 stepNo;
    private final String              name;
    private final String              description;
    private final WorkoutStepType     type;
    private WorkoutStepExtentType     stepExtent;
    private WorkoutStepIntensityType  stepIntensity;
    
    // extents
    private HrZone                    extentReachHrZone;  
    private int                       extentDistance;     // In mm
    private int                       extentDuration;     // In sec
    
    // intensities
    private int                       intensityPace;
    private int                       intensitySpeed;
    private HrZone                    intensityHrZone;
    
    /**
     * Constructor. Defines the type of step
     * @param no Step number
     * @param name Name of the workout step
     * @param description Description of the workout step
     * @param type Type of the workout
     */
    public WorkoutStep(int no, String name, String description, WorkoutStepType type)
    {
        this.stepNo         =no;
        this.name           =name;
        this.description    =description;
        this.type           =type;
        this.stepExtent     =WorkoutStepExtentType.NONE;
        this.stepIntensity  =WorkoutStepIntensityType.NONE;
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
    public WorkoutStepType getType()
    {
        return type;
    }


    
    /**
     * Set the workout step extent to distance
     * @param distance Distance in mm
     */
    public void setExtentDistance(int distance)
    {
        this.stepExtent            =WorkoutStepExtentType.DISTANCE;
        this.extentDistance         =distance;       
    }

    /**
     * Set the workout step extent to duration
     * @param duration Duration in seconds
     */
    public void setExtentDuration(int duration)
    {
        this.stepExtent            =WorkoutStepExtentType.DURATION;
        this.extentDuration         =duration;       
    }
    
    /**
     * Set the workout step extent to reach HR Zone
     * @param zone HR Zone to reach
     */
    public void setExtentReachHrZone(HrZone zone)
    {
        this.stepExtent            =WorkoutStepExtentType.REACHHRZONE;
        this.extentReachHrZone      =zone;       
    }
 
    /**
     * Set the workout step extent to manual
     */
    public void setExtentManual()
    {
        this.stepExtent            =WorkoutStepExtentType.MANUAL;
    }
    
    /**
     * Returns the extent of the workout step. The extent is basically
     * the goal to reach in terms of duration, distance or HR zone to reach
     * @return The extent of the workout step
     */
    public WorkoutStepExtentType getStepExtent()
    {
        return this.stepExtent;
    }

    /**
     * Get the extent in terms of HR Zone to reach
     * @return The HR Zone to reach, or NONE if the extent type is not REACH HR ZONE
     */
    public HrZone getExtentReachHrZone()
    {
        if (stepExtent==WorkoutStepExtentType.REACHHRZONE)
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
        if (stepExtent==WorkoutStepExtentType.DISTANCE)
        {
            return extentDistance;
        }
        return -1;
    }

    /**
     * Get the extent in terms of duration
     * @return The duration in s, or -1 if the extent type is not DURATION
     */
    public int getExtentDuration()
    {
        if (stepExtent==WorkoutStepExtentType.DURATION)
        {
            return extentDuration;
        }
        return -1;
    }



    
    /**
     * Sets the intensity of the step to pace
     * @param pace Pace in milliseconds per km
     */
    public void setIntensityPace(int pace)
    {
        this.stepIntensity  =WorkoutStepIntensityType.PACE;
        this.intensityPace  =pace;
    }
    
    /**
     * Sets the intensity of the step to speed
     * @param speed Pace in millimeters per per second
     */
    public void setIntensitySpeed(int speed)
    {
        this.stepIntensity  =WorkoutStepIntensityType.SPEED;
        this.intensitySpeed =speed;
    }

    /**
     * Sets the intensity of the step to HR Zone at which the step must be 
     * performed
     * @param zone HR Zone
     */
    public void setIntensityHrZone(HrZone zone)
    {
        this.stepIntensity  =WorkoutStepIntensityType.HRZONE;
        this.intensityHrZone=zone;
    }

    /**
     * Get the step intensity pace
     * @return The pace in msec/km or -1 if the step intensity is not of the PACE type
     */
    public int getIntensityPace()
    {
        if (this.stepIntensity==WorkoutStepIntensityType.PACE)
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
        if (this.stepIntensity==WorkoutStepIntensityType.SPEED)
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
        if (this.stepIntensity==WorkoutStepIntensityType.HRZONE)
        {
            return intensityHrZone;
        }
        return HrZone.NONE;
    }

    
    
    /**
     * Returns the intensity goal of the workout step
     * @return The intensity goal; the goal is optional
     */
    public WorkoutStepIntensityType getStepIntensity()
    {
        return this.stepIntensity;
    }
    
    /**
     * Returns the pace at which the workout step must be performed
     * @return The pace in milliseconds per km if the intensity goal is pace, -1 if otherwise
     */
    public int getStepIntensityPace()
    {
        int pace;
        if (stepIntensity==WorkoutStepIntensityType.PACE)
        {
            pace=intensityPace;
        }
        else
        {
            pace=-1;
        }
        return pace;
    }

    /**
     * Returns the speed at which the workout step must be performed
     * @return The speed in msec per m if the intensity goal is speed, -1 if otherwise
     */
    public int getStepIntensitySpeed()
    {
        int speed;
        if (stepIntensity==WorkoutStepIntensityType.SPEED)
        {
            speed=intensitySpeed;
        }
        else
        {
            speed=-1;
        }
        return speed;
    }

    /**
     * Returns the heartrate zone at which the workout step must be performed
     * @return The hr zone if the intensity goal is hr zone, null if otherwise
     */
    public HrZone getStepIntensityHrZone()
    {
        HrZone zone;
        if (stepIntensity==WorkoutStepIntensityType.HRZONE)
        {
            zone=intensityHrZone;
        }
        else
        {
            zone=null;
        }
        return zone;
    }
   
    
    @Override
    public String toString()
    {
        String outputString;
        
        outputString=String.format("    %02d: %-9s - %-9s - %-13s ", stepNo, name, type, stepExtent);
        switch (stepExtent)
        {
            case DURATION:
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
