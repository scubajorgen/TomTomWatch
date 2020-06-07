/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

import net.studioblueplanet.tomtomwatch.Workout.WorkoutType;
import net.studioblueplanet.tomtomwatch.WorkoutListItem.IntensityLevel;
import net.studioblueplanet.tomtomwatch.WorkoutListItem.ActivityType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.StepType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.HrZone;
import net.studioblueplanet.tomtomwatch.WorkoutStep.IntensityType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.ExtentType;

/**
 * This class represents the definition of a list of workouts. It basically
 * represents the data needed to create the workout list and workout files
 * on the tomtom watch
 * @author jorgen
 */
public class WorkoutListTemplate
{
    /**
     * Represents the definition of a step in a workout
     */
    public static class StepTemplate
    {
        public String                       name;
        public String                       description;
        public StepType                     type;
        public ExtentType                   length;
        public Integer                      time;        // in seconds
        public Integer                      distance;    // in meters
        public HrZone                       reachHrZone;
        public IntensityType                intensity;
        public Integer                      speed;                   
        public Integer                      pace;
        public HrZone                       hrZone;        
    }
    
    /**
     * Represents the definition of a single workout
     */
    public static class WorkoutTemplate
    {
        public String                       fileId;         // "0x00BEnnnn"
        public String                       listId;         // optional, UUID as 16 byte hex
        public String                       workoutId;      // optional, UUID as 16 byte hex
        public String                       name;
        public String                       description;
        public ActivityType                 activity;
        public WorkoutType                  type;
        public IntensityLevel               intensityLevel;
        public final List<StepTemplate>     steps;
        
        public WorkoutTemplate()
        {
            steps=new ArrayList<>();
        }
    }
    
    private final List<WorkoutTemplate>     workouts;
    
    /**
     * Constructor. Initializes the list of workouts
     */
    public WorkoutListTemplate()
    {
        workouts=new ArrayList<>();
    }
    
    /**
     * Return the list of workouts
     * @return The list of workouts
     */
    public List<WorkoutTemplate> getWorkouts()
    {
        return workouts;
    }
    
    /**
     * Serializes the list of workouts defintion to a UTF-8 JSON string
     * @return The string
     */
    public String toJson()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this); 
        return json;
    }
    
    /**
     * Creates a new, fully populated list definition from JSON by deserializing
     * the JSON to a new instance.
     * @param json The definition in JSON format.
     * @return The new instance
     */
    public static WorkoutListTemplate fromJson(String json)
    {
        Gson gson = new Gson();
        WorkoutListTemplate deserialized = gson.fromJson(json, WorkoutListTemplate.class);
        return deserialized;
    }
    
    /**
     * Build an WorkoutListTemplate instance from a WorkoutList instance
     * @param list The workout list
     * @return A new WorkoutListTemplate
     */
    public static WorkoutListTemplate fromWorkoutList(WorkoutList list)
    {
        WorkoutListTemplate             template;
        List<WorkoutListItem>           listItems;
        Workout                         workout;
        WorkoutTemplate                 workoutTemplate;
        HashMap<Integer, WorkoutStep>   steps;
        WorkoutStep                     step;
        StepTemplate                    stepTemplate;
        
        template=new WorkoutListTemplate();
        
        listItems=list.getWorkoutList();
        
        for(WorkoutListItem item: listItems)
        {
            workoutTemplate                 =new WorkoutTemplate();
            workoutTemplate.name            =item.getWorkoutName();
            workoutTemplate.description     =item.getWorkoutDescription();
            workoutTemplate.activity        =item.getActivity();
            workoutTemplate.type            =item.getWorkoutClass();
            workoutTemplate.intensityLevel  =item.getIntensityLevel();
            workoutTemplate.listId          =String.format("%032x", new BigInteger(1, item.getId()));
            workoutTemplate.workoutId       =String.format("%032x", new BigInteger(1, item.getWorkoutId()));
            workoutTemplate.fileId          =String.format("0x%08x", item.getFileId());
            
            workout=list.getWorkout(item);
            
            steps=workout.getSteps();
            for (Integer stepNo : steps.keySet())
            {
                step                    =steps.get(stepNo);
                stepTemplate            =new StepTemplate();
                stepTemplate.name       =step.getName();
                stepTemplate.description=step.getDescription();
                stepTemplate.type       =step.getType();
                stepTemplate.length     =step.getStepExtent();
                switch (step.getStepExtent())
                {
                    case DISTANCE:
                        stepTemplate.distance=step.getExtentDistance()/1000;    // mm -> m
                        break;
                    case TIME:
                        stepTemplate.time=step.getExtentTime();
                        break;
                    case REACHHRZONE:
                        stepTemplate.reachHrZone=step.getExtentReachHrZone();
                        break;
                }
                stepTemplate.intensity  =step.getIntensity();
                switch (step.getIntensity())
                {
                    case PACE:
                        stepTemplate.pace=step.getIntensityPace()/1000;         // msec/km -> sec/km
                        break;
                    case SPEED:
                        stepTemplate.speed=step.getIntensitySpeed()*3600/1000;            // mm/sec -> m/hr
                        break;
                    case HRZONE:
                        stepTemplate.hrZone=step.getIntensityHrZone();
                }
                workoutTemplate.steps.add(stepTemplate);
            }
            
            template.workouts.add(workoutTemplate);
        }
        
        return template;
    }
    
    /**
     * This method creates a fully populated WorkoutList instance containing
     * the information required to write to the workout files 0x00BEnnnn
     * @return The WorkoutList instance
     */
    public WorkoutList toWorkoutList()
    {
        WorkoutList list;
        
        list=new WorkoutList();
        
        // TO DO: convert
        
        return list;
    }
}
