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
        outputString+=String.format("%s - %-10s - %s\n", activity, workoutClass, workoutName);
        outputString+=workoutDescription+"\n";
        return outputString;
    }
}
