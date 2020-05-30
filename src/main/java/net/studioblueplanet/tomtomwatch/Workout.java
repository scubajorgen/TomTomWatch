/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author jorgen
 */
public class Workout
{
    enum WorkoutClass
    {
        FATBURN,
        ENDURANCE,
        FITNESS,
        SPEED,
        POWER,
        CUSTOM //?
    }
    
    private final WorkoutClass          workoutClass;
    private final String                name;
    private final String                description;
    private final List<WorkoutStep>     steps;
    
    /**
     * Constructor, initializes the instance
     * @param name Name of the workout
     * @param description Description of the workout;
     */
    public Workout(String name, String description, WorkoutClass workoutClass)
    {
        this.name           =name;
        this.description    =description;
        this.workoutClass   =workoutClass;
        steps               =new ArrayList<>();
    }

    /**
     * Returns the name of the workout
     * @return The name (like 'Warm up', 'Work', 'Rest')
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the description of the step
     * @return The description, like 'Get into the Fat Burn HR zone'
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the class of workout
     * @return The class, like ENDURANCE, FATBURN
     */
    public WorkoutClass getWorkoutClass()
    {
        return workoutClass;
    }

    /**
     * Returns the list of steps that make up the workout
     * @return List of steps
     */
    public List<WorkoutStep> getSteps()
    {
        return steps;
    }
    

}
