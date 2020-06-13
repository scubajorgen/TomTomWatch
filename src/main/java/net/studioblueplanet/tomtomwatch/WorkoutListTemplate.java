/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.logger.DebugLogger;

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
    public static class HrZoneTemplate
    {
        public int                          hrMin;
        public int                          hrMax;
        
        /**
         * Constructor
         * @param minRate Mininum heart rate in this zone
         * @param maxRate Maximum heart rate in this zone
         */
        public HrZoneTemplate(int minRate, int maxRate)
        {
            hrMin=minRate;
            hrMax=maxRate;
        }
    }
    /**
     * Represents the definition of a step in a workout
     */
    public static class StepTemplate
    {
        public String                       name;
        public String                       description;
        public StepType                     type;
        public ExtentType                   length;
        public Integer                      time;           // in seconds
        public Integer                      distance;       // in meters
        public HrZone                       reachHrZone;
        public IntensityType                intensity;
        public Integer                      speed;          // in m/hr                
        public Integer                      pace;           // in s/km
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
    
    private final LinkedHashMap<String, HrZoneTemplate>     hrZones;
    private final List<WorkoutTemplate>                     workouts;
    
    /**
     * Constructor. Initializes the list of workouts
     */
    public WorkoutListTemplate()
    {
        hrZones=new LinkedHashMap<>();
        // defaults
        hrZones.put("easy"   , new HrZoneTemplate( 96, 114));
        hrZones.put("fatburn", new HrZoneTemplate(115, 133));
        hrZones.put("cardio" , new HrZoneTemplate(134, 153));
        hrZones.put("perform", new HrZoneTemplate(154, 172));
        hrZones.put("peak"   , new HrZoneTemplate(173, 192));
        
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
     * Return the list with HR zone settings 
     * @return The list with the 5 HR Zone min/max bpms
     */
    public LinkedHashMap<String, HrZoneTemplate> getHrZones()
    {
        return this.hrZones;
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
            workoutTemplate.type            =item.getWorkoutType();
            workoutTemplate.intensityLevel  =item.getIntensityLevel();
            workoutTemplate.listId          =ToolBox.bytesToHexString(item.getId(), 32);        
            workoutTemplate.workoutId       =ToolBox.bytesToHexString(item.getWorkoutId(), 32); 
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
     * @return The WorkoutList instance or null if an error occurred
     */
    public WorkoutList toWorkoutList()
    {
        WorkoutList list;
        byte[]      workoutId;
        byte[]      itemId;
        int         fileId;
        boolean     error;
        
        list=new WorkoutList();
        
        for(WorkoutTemplate workout : workouts)
        {
            Workout workoutToAdd=new Workout();
            workoutToAdd.setWorkoutName(workout.name);
            workoutToAdd.setWorkoutDescription(workout.description);
            workoutToAdd.setWorkoutType(workout.type);
            workoutToAdd.setUnknown11(2);
            if (workout.workoutId!=null)
            {
                workoutId=ToolBox.hexStringToBytes(workout.workoutId, 16);
            }
            else
            {
                workoutId=ToolBox.getUUID();
            }
            workoutToAdd.setId(workoutId);
            
            int stepCount=0;
            for (StepTemplate step : workout.steps)
            {
                WorkoutStep stepToAdd=new WorkoutStep(stepCount, step.name, step.description, step.type);
                switch (step.length)
                {
                    case DISTANCE:
                        stepToAdd.setExtentDistance(step.distance);
                        break;
                    case TIME:
                        stepToAdd.setExtentDuration(step.time);
                        break;
                    case REACHHRZONE:
                        stepToAdd.setExtentReachHrZone(step.reachHrZone);
                        break;
                    case MANUAL:
                        stepToAdd.setExtentManual();
                        break;
                    default:
                        DebugLogger.error("Error creating workout step: step needs a length");
                        break;
                }
                switch (step.intensity)
                {
                    case HRZONE:
                        stepToAdd.setIntensityHrZone(step.hrZone);
                        break;
                    case PACE:
                        stepToAdd.setIntensityPace(step.pace*1000);
                        break;
                    case SPEED:
                        stepToAdd.setIntensitySpeed(step.speed*1000/3600);
                        break;
                    case NONE:
                        break;
                }
                workoutToAdd.addWorkoutStep(stepCount, stepToAdd);
                stepCount++;
            }
            
            fileId=Integer.parseInt(workout.fileId.substring(2), 16);
            list.addWorkout(fileId, workoutToAdd);
            
            WorkoutListItem itemToAdd=new WorkoutListItem(fileId, workout.name, workout.description, workout.activity, workout.type);
            if (workout.listId!=null)
            {
                itemId=ToolBox.hexStringToBytes(workout.listId, 16);
            }
            else
            {
                itemId=ToolBox.getUUID();
            }
            itemToAdd.setFileSize(workoutToAdd.getWorkoutDataLength());
            
            itemToAdd.setId(itemId);
            itemToAdd.setWorkoutId(workoutId);

            list.addListItem(itemToAdd);
        }
        
        return list;
    }
    
    /**
     * Overwrite the default HR Zones with the values from the WatchSettings
     * @param settings The WatchSettings
     */
    public void setHrZonesFromSettings(WatchSettings settings)
    {
        HrZoneTemplate hrZoneValue;
        int            bpm;
        
        for(String zone : hrZones.keySet())
        {
            hrZoneValue=hrZones.get(zone);
            bpm=(int)settings.getSettingsValueInt("hrzone/"+zone.toLowerCase()+"/min");
            if (bpm>0)
            {
                hrZoneValue.hrMin=bpm;
            }
            else
            {
                DebugLogger.error("HR setting not found in WatchSettings. Setting not modified");
            }
            bpm=(int)settings.getSettingsValueInt("hrzone/"+zone.toLowerCase()+"/max");
            if (bpm>0)
            {
                hrZoneValue.hrMax=bpm;
            }
            else
            {
                DebugLogger.error("HR setting not found in WatchSettings. Setting not modified");
            }
        }
    }
    
    /**
     * Set the HR Zones in this template to the WatchSettings
     * @param settings The WatchSettings
     */
    public void setHrZonesToSettings(WatchSettings settings)
    {
        HrZoneTemplate hrZoneValue;
        for(String zone : hrZones.keySet())
        {
            hrZoneValue=hrZones.get(zone);
            settings.setSettingsValueInt("hrzone/"+zone.toLowerCase()+"/min", hrZoneValue.hrMin);
            settings.setSettingsValueInt("hrzone/"+zone.toLowerCase()+"/max", hrZoneValue.hrMax);
        }
    }
    
}
