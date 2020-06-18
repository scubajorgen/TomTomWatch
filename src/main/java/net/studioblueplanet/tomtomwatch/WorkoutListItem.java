/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.tomtomwatch.Workout.WorkoutType;
/**
 * This class represents the metadata of a workout as present in the 
 * 0x00BE0000 file. Basically, ID, name, description and some type info.
 * It also contains a reference to the actual workout file and ID
 * @author jorgen
 */
public class WorkoutListItem implements Comparable<WorkoutListItem>
{
    public enum ActivityType
    {
        RUNNING(0x4181, "RUNNING"),
        CYCLING(0x0802, "CYCLING");

        private final long      value;
        private final String    description;

        /**
         * Constructor
         * @param value Enum value
         */
        ActivityType(long value, String description)
        {
            this.value          = value;
            this.description    =description;
        }

        /**
         * Get the enum based on the enum value passed
         * @param value Enum value
         * @return The enum or null if not found
         */
        public static ActivityType getActivityType(long value)
        {
            for (ActivityType activity : ActivityType.values())
            {
                if (activity.value == value)
                {
                    return activity;
                }
            }
            return null;
        }
        
        /**
         * Returns the value
         * @return The value of this enum entry
         */
        public long getValue()
        {
            return value;
        }
        
        @Override
        public String toString()
        {
            return description;
        }
    }

    public enum IntensityLevel
    {
        EASIEST  (19, "Easiest"),
        EASIER   ( 9, "Easier"),
        STANDARD ( 0, "Standard"),
        HARDER   (10, "Harder"),
        HARDEST  (20, "Hardest");

        private final int       value;
        private final String    description;

        /**
         * Constructor
         * @param value Enum value
         */
        IntensityLevel(int value, String description)
        {
            this.value          =value;
            this.description    =description;
        }

        /**
         * Returns the value of this enum entry
         * @return The value
         */
        public int getValue()
        {
            return value;
        }
        
        /**
         * Get the enum based on the enum value passed
         * @param value Enum value
         * @return The enum or null if not found
         */
        public static IntensityLevel getIntensityLevel(int value)
        {
            for (IntensityLevel level : IntensityLevel.values())
            {
                if (level.value == value)
                {
                    return level;
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
    
    private final int           fileId;
    private final String        workoutName;
    private final String        workoutDescription;
    private final ActivityType  activity;
    private final WorkoutType   workoutType;
    
    private byte[]              workoutMd5; // MD5 of the workout file
    private byte[]              workoutId;  // GUID identifying workout file; does not relate to content
    private IntensityLevel      intensityLevel;
    private int                 unknown8;
    private int                 fileSize;
    private int                 unknown12;
    private int                 unknown13;
    
    
    /**
     * Constructor
     * @param fileId File ID, like 0x00BE0001
     * @param name Name/title of the workout
     * @param description Description of the workout
     * @param activity The activity (i.e. RUNNING, CYCLING) of this item
     * @param workoutType Class/type of the workout
     */
    public WorkoutListItem(int fileId, String name, String description, ActivityType activity, WorkoutType workoutType)
    {
        this.fileId             =fileId;
        this.workoutName        =name;
        this.workoutDescription =description;
        this.activity           =activity;
        this.workoutType        =workoutType;
        
        this.intensityLevel     =IntensityLevel.STANDARD;
        this.unknown8           =0;
        this.unknown12          =2;
        this.unknown13          =1;
    }

    /**
     * Returns the file ID of this workout list item.
     * @return The file ID; will be 0x00BE001, 0x00BE002, etc.
     */
    public int getFileId()
    {
        return fileId;
    }

    /**
     * Returns the name of the workout
     * @return The name of the workout
     */
    public String getWorkoutName()
    {
        return workoutName;
    }

    /**
     * Returns the description of the workout
     * @return The description
     */
    public String getWorkoutDescription()
    {
        return workoutDescription;
    }

    /**
     * Returns the activity of the workout 
     * @return The activity (RUNNING, CYCLING)
     */
    public ActivityType getActivity()
    {
        return activity;
    }

    /**
     * Returns the class/type of workout 
     * @return The class (FAT BURN, ENDURANCE, etc)
     */
    public WorkoutType getWorkoutType()
    {
        return workoutType;
    }

    /**
     * Gets the MD5 hash of the workout protobuf file
     * @return The hash as an array of 16 bytes
     */
    public byte[] getWorkoutMd5()
    {
        return workoutMd5;
    }

    /**
     * Sets the MD5 hash of the workout protobuf file
     * @param md5 The Hash as an array of 16 bytes
     */
    public void setWorkoutMd5(byte[] md5)
    {
        this.workoutMd5 = md5;
    }

    /**
     * Gets the UID identifying the workout. This UID also is present in the
     * workout file
     * @return The unique ID as an array of 16 bytes
     */
    public byte[] getWorkoutId()
    {
        return workoutId;
    }

    /**
     * Sets the UID identifying the workout
     * @param workoutId The UID as an array of 16 bytes
     */
    public void setWorkoutId(byte[] workoutId)
    {
        this.workoutId = workoutId;
    }

    /**
     * Gets the intensity level (e.g. EASIEST, ... HARDEST) of the workout
     * @return The level
     */
    public IntensityLevel getIntensityLevel()
    {
        return intensityLevel;
    }

    /**
     * Sets the intensity level of the workout
     * @param level The level
     */
    public void setIntensityLevel(IntensityLevel level)
    {
        this.intensityLevel = level;
    }

    public int getUnknown8()
    {
        return unknown8;
    }

    public void setUnknown8(int unknown8)
    {
        this.unknown8 = unknown8;
    }

    public int fileSize()
    {
        return fileSize;
    }

    public void setFileSize(int unknown9)
    {
        this.fileSize = unknown9;
    }

    public int getUnknown12()
    {
        return unknown12;
    }

    public void setUnknown12(int unknown12)
    {
        this.unknown12 = unknown12;
    }

    public int getUnknown13()
    {
        return unknown13;
    }

    public void setUnknown13(int unknown13)
    {
        this.unknown13 = unknown13;
    }
    
    /**
     * Compares this item to another
     * @param item Item to compare to
     * @return 0 if equal, small 0 if comes before, larger 0 if comes after
     */
    @Override
    public int compareTo(WorkoutListItem item)
    {
        int compare;
        if (this.activity.getValue()==item.getActivity().getValue())
        {
            compare=this.workoutType.getValue()-item.workoutType.getValue();
        }
        else
        {
            // RUNNING before CYCLING
            compare=(int)(item.getActivity().getValue()-this.activity.getValue());
        }
        return compare;
    }
    
    /**
     * Returns a textual representation of the Workout List item
     * @return The string
     */
    @Override
    public String toString()
    {
        String outputString;
        outputString="____________________________________________________________________________________________________\n";
        outputString+=String.format("%s - %08x - %-10s - %s\n", activity, fileId, workoutType, workoutName);
        outputString+=workoutDescription+"\n";
        return outputString;
    }
}
