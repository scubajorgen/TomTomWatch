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
        FATBURN(1, "FAT BURN"),
        ENDURANCE(2, "ENDURANCE"),
        FITNESS(3, "FITNESS"),
        SPEED(4, "SPEED"),
        POWER(5, "POWER"),
        CUSTOM(6,"CUSTOM"); //?

        private final int       value;
        private final String    description;

        /**
         * Constructor
         * @param i Enum value
         */
        WorkoutClass(int i, String description)
        {
            this.value          = i;
            this.description    =description;
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
        
        @Override
        public String toString()
        {
            return description;
        }
    }

    private final HashMap<Integer, String>      workoutDescriptions;
    
    private WorkoutClass                        workoutClass;
    private String                              name;
    private String                              description;
    private final HashMap<Integer, WorkoutStep> workoutSteps;

    /**
     * Constructor, initializes the instance
     */
    public Workout()
    {
        workoutSteps        = new HashMap<>();
        workoutDescriptions = new HashMap<>();
    }

    /**
     * This method adds a description to the list of descriptions associated
     * with this workout
     * @param id ID of the description
     * @param descriptionText Description text
     */
    public void putDescription(int id, String descriptionText)
    {
        workoutDescriptions.put(id, descriptionText);
        if (id==0)
        {
            this.name=descriptionText;
        }
        else if (id==1)
        {
            this.description=descriptionText;
        }
    }
    
    /**
     * Returns a description text from the list of descriptions associated
     * with this workout
     * @param id ID of the descriptioni
     * @return The description text.
     */
    public String getDescription(int id)
    {
        return workoutDescriptions.get(id);
    }
    
    /**
     * Returns the name of the workout
     *
     * @return The name (like 'Warm up', 'Work', 'Rest')
     */
    public String getWorkoutName()
    {
        return name;
    }

    /**
     * Returns the description of the workout
     *
     * @return The description, like 'Get into the Fat Burn HR zone'
     */
    public String getWorkoutDescription()
    {
        return description;
    }
    
    /**
     * Sets the class/type of the workout
     * @param workoutClass The class/type
     */
    public void setWorkoutClass(WorkoutClass workoutClass)
    {
        this.workoutClass=workoutClass;
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
        
        outputString ="____________________________________________________________________________________________________\n";
        outputString+=String.format("%-10s - %s", workoutClass, name)+"\n"+description+"\n";
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
