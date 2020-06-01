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
        WARMUP(1, "Warm Up"),
        WORK(2, "Work"),
        REST(3, "Rest"),
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
        NONE(""),
        DISTANCE("Distance"),
        DURATION("Duration"),
        REACHHRZONE("Reach HR zone"),
        MANUAL("Manual");
        
        private final String description;
        
        WorkoutStepExtentType(String description)
        {
            this.description=description;
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
        NONE(""),
        HRZONE("HR Zone"),
        PACE("Pace"),
        SPEED("Speed");
        
        private final String description;
        
        WorkoutStepIntensityType(String description)
        {
            this.description=description;
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
        EASY(1, "Easy"),
        FATBURN(2, "Fat Burn"),
        CARDIO(3, "Cardio"),
        PERFORM(4, "Perform"),
        PEAK(5, "Peak");
        
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
        this.stepExtent    =WorkoutStepExtentType.NONE;
        this.stepIntensity  =WorkoutStepIntensityType.NONE;
    }
    
    /**
     * Set the workout step extent to distance
     * @param distance Distance in mm
     */
    public void setStepExtentDistance(int distance)
    {
        this.stepExtent            =WorkoutStepExtentType.DISTANCE;
        this.extentDistance         =distance;       
    }

    /**
     * Set the workout step extent to duration
     * @param duration Duration in seconds
     */
    public void setStepExtentDuration(int duration)
    {
        this.stepExtent            =WorkoutStepExtentType.DURATION;
        this.extentDuration         =duration;       
    }
    
    /**
     * Set the workout step extent to reach HR Zone
     * @param zone HR Zone to reach
     */
    public void setStepExtentReachHrZone(HrZone zone)
    {
        this.stepExtent            =WorkoutStepExtentType.REACHHRZONE;
        this.extentReachHrZone      =zone;       
    }
 
    /**
     * Set the workout step extent to manual
     */
    public void setStepExtentManual()
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
     * Sets the intensity of the step to pace
     * @param pace Pace in milliseconds per km
     */
    public void setStepIntensityPace(int pace)
    {
        this.stepIntensity  =WorkoutStepIntensityType.PACE;
        this.intensityPace  =pace;
    }
    
    /**
     * Sets the intensity of the step to speed
     * @param speed Pace in millimeters per per second
     */
    public void setStepIntensitySpeed(int speed)
    {
        this.stepIntensity  =WorkoutStepIntensityType.SPEED;
        this.intensitySpeed =speed;
    }

    /**
     * Sets the intensity of the step to HR Zone at which the step must be 
     * performed
     * @param zone HR Zone
     */
    public void setStepIntensityHrZone(HrZone zone)
    {
        this.stepIntensity  =WorkoutStepIntensityType.HRZONE;
        this.intensityHrZone=zone;
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
        
        outputString=String.format("%02d: %-9s - %-9s - %-13s ", stepNo, name, type, stepExtent);
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
            outputString+=String.format("\n    %s", description);
        }
        else
        {
            outputString+="\n";
        }
        outputString+="\n";
        return outputString;
    }
}
