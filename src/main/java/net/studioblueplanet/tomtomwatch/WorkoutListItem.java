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
public class WorkoutListItem
{
    private final int           fileId;
    private final String        workoutName;
    private final String        workoutDescription;
    private final WorkoutClass  workoutClass;
    /**
     * Constructor
     * @param fileId 
     */
    public WorkoutListItem(int fileId, String name, String description, WorkoutClass workoutClass)
    {
        this.fileId             =fileId;
        this.workoutName        =name;
        this.workoutDescription =description;
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
     * Returns the class/type of workout 
     * @return The class (FAT BURN, ENDURANCE, etc)
     */
    public WorkoutClass getWorkoutClass()
    {
        return workoutClass;
    }
    
    
}
