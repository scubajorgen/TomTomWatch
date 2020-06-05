/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.tomtomwatch.Workout.WorkoutClass;
/**
 *
 * @author jorgen
 */
public class WorkoutListItem implements Comparable<WorkoutListItem>
{
    enum ActivityType
    {
        RUNNING(0x4181, "RUNNING"),
        CYCLING(0x0802, "CYCLING");

        private final long      value;
        private final String    description;

        /**
         * Constructor
         * @param i Enum value
         */
        ActivityType(long i, String description)
        {
            this.value          = i;
            this.description    =description;
        }

        /**
         * Get the enum based on the enum value passed
         * @param i Enum value
         * @return The enum or null if not found
         */
        public static ActivityType getActivityType(long i)
        {
            for (ActivityType activity : ActivityType.values())
            {
                if (activity.value == i)
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
    
    private final int           fileId;
    private final String        workoutName;
    private final String        workoutDescription;
    private final ActivityType  activity;
    private final WorkoutClass  workoutClass;
    
    private byte[]              id;
    private byte[]              workoutId;
    private int                 unknown7;
    private int                 unknown8;
    private int                 unknown9;
    private int                 unknown12;
    private int                 unknown13;
    
    
    /**
     * Constructor
     * @param fileId File ID, like 0x00BE0001
     * @param name Name/title of the workout
     * @param description Description of the workout
     * @param workoutClass Class/type of the workout
     */
    public WorkoutListItem(int fileId, String name, String description, ActivityType activity, WorkoutClass workoutClass)
    {
        this.fileId             =fileId;
        this.workoutName        =name;
        this.workoutDescription =description;
        this.activity           =activity;
        this.workoutClass       =workoutClass;
        
        this.unknown7           =0;
        this.unknown8           =0;
        this.unknown12          =6;
        this.unknown13          =2;
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
    public WorkoutClass getWorkoutClass()
    {
        return workoutClass;
    }

    /**
     * Get the ID of this list item
     * @return The unique ID as an array of 16 bytes
     */
    public byte[] getId()
    {
        return id;
    }

    /**
     * Sets the ID of this list item
     * @param id The ID as an array of 16 bytes
     */
    public void setId(byte[] id)
    {
        this.id = id;
    }

    /**
     * Get the ID of the workout this list item refers to
     * @return The unique ID as an array of 16 bytes
     */
    public byte[] getWorkoutId()
    {
        return workoutId;
    }

    /**
     * Sets the ID of the workout this list item refers to
     * @param workoutId The ID as an array of 16 bytes
     */
    public void setWorkoutId(byte[] workoutId)
    {
        this.workoutId = workoutId;
    }

    
    public int getUnknown7()
    {
        return unknown7;
    }

    public void setUnknown7(int unknown7)
    {
        this.unknown7 = unknown7;
    }

    public int getUnknown8()
    {
        return unknown8;
    }

    public void setUnknown8(int unknown8)
    {
        this.unknown8 = unknown8;
    }

    public int getUnknown9()
    {
        return unknown9;
    }

    public void setUnknown9(int unknown9)
    {
        this.unknown9 = unknown9;
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
    
    @Override
    public int compareTo(WorkoutListItem item)
    {
        int compare;
        if (this.activity.getValue()==item.getActivity().getValue())
        {
            compare=this.workoutClass.getValue()-item.workoutClass.getValue();
        }
        else
        {
            compare=(int)(this.activity.getValue()-item.getActivity().getValue());
        }
        return compare;
    }
    
    @Override
    public String toString()
    {
        String outputString;
        outputString="____________________________________________________________________________________________________\n";
        outputString+=String.format("%s - %08x - %-10s - %s\n", activity, fileId, workoutClass, workoutName);
        outputString+=workoutDescription+"\n";
        return outputString;
    }
}
