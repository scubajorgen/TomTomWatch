/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author jorgen
 */
public class Workout
{
    /**
     * Class of the workout
     */
    public enum WorkoutClass
    {
        FATBURN(1),
        ENDURANCE(2),
        FITNESS(3),
        SPEED(4),
        POWER(5),
        CUSTOM(6); //?

        private final int value;

        /**
         * Constructor
         * @param i Enum value
         */
        WorkoutClass(int i)
        {
            this.value = i;
        }

        /**
         * Get the enum based on the enum value passed
         * @param i Enum value
         * @return The enum or null if not found
         */
        public static WorkoutClass getWorkoutClass(int i)
        {
            for (WorkoutClass w : WorkoutClass.values())
            {
                if (w.value == i)
                {
                    return w;
                }
            }
            return null;
        }
    }

    private final WorkoutClass                  workoutClass;
    private final String                        name;
    private final String                        description;
    private final HashMap<Integer, WorkoutStep> workoutSteps;

    /**
     * Constructor, initializes the instance
     *
     * @param name Name of the workout
     * @param description Description of the workout;
     * @param workoutClass Class of the workout
     */
    public Workout(String name, String description, WorkoutClass workoutClass)
    {
        this.name           = name;
        this.description    = description;
        this.workoutClass   = workoutClass;
        workoutSteps        = new HashMap<>();
    }

    /**
     * Returns the name of the workout
     *
     * @return The name (like 'Warm up', 'Work', 'Rest')
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the description of the step
     *
     * @return The description, like 'Get into the Fat Burn HR zone'
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the class of workout
     *
     * @return The class, like ENDURANCE, FATBURN
     */
    public WorkoutClass getWorkoutClass()
    {
        return workoutClass;
    }

    /**
     * Returns the list of steps that make up the workout
     *
     * @return List of steps
     */
    public HashMap<Integer, WorkoutStep> getSteps()
    {
        return workoutSteps;
    }
    
    /**
     * Add given workout step to the workout
     * @param id Sequence number of the step
     * @param workoutStep Step to add
     */
    public void addWorkoutStep(int id, WorkoutStep workoutStep)
    {
        workoutSteps.put(id, workoutStep);
    }

    @Override
    public String toString()
    {
        String  outputString;
        int     i;
        
        outputString=name+" "+description+"\n";
        i=0;
        while (i<workoutSteps.size())
        {
            WorkoutStep step=workoutSteps.get(i);
            outputString+=step;
            i++;
        }
        outputString+="\n";
        return outputString;
    }

}
