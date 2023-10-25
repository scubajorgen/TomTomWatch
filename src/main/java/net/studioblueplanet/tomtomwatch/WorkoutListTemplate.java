/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.generics.ToolBox;

import net.studioblueplanet.tomtomwatch.Workout.WorkoutType;
import net.studioblueplanet.tomtomwatch.Workout.IntensityLevel;
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
        public ExtentType                   extent;
        public Integer                      extentTime;             // in seconds
        public Integer                      extentDistance;         // in meters
        public HrZone                       extentReachHrZone;
        public IntensityType                intensity;
        public Integer                      intensityCadence;       // in RPM
        public Integer                      intensitySpeed;         // in m/hr
        public Integer                      intensityPace;          // in s/km
        public HrZone                       intensityHrZone;        
        
        public String validate()
        {
            String valid;
            
            valid="OK";
            
            if (name==null)
            {
                valid="Step must have a name";
            
            }
            else if (description==null)
            {
                valid="Step must have a description";
                 
            }
            else if (type==null)
            {
                valid="Step must have a type: WARMUP, REST, WORK, COOLDOWN";
            }
            else if (extent==null || extent==ExtentType.NONE)
            {
                valid="Step must have a length: MANUAL, DISTANCE, TIME, REACHHRZONE";
            }
            else 
            {
                if (extent==ExtentType.DISTANCE && extentDistance==null)
                {
                    valid="No distance specified for DISTANCE step";
                }
                else if (extent==ExtentType.TIME && extentTime==null)
                {
                    valid="No time specified for TIME step";
                }
                else if (extent==ExtentType.REACHHRZONE && extentReachHrZone==null)
                {
                    valid="No reachHrZone specified for REACHHRZONE step";
                }
            }
            if (valid.equals("OK") && intensity==null)
            {
                valid="No intensity defined. Must be NONE, PACE, SPEED, HRZONE, CADENCE";
            }
            else
            {
                if (intensity==IntensityType.HRZONE && intensityHrZone==null)
                {
                    valid="No hrZone specified for HRZONE intensity step";
                }
                else if (intensity==IntensityType.PACE && intensityPace==null)
                {
                    valid="No pace specified for PACE intensity step";
                }
                else if (intensity==IntensityType.SPEED && intensitySpeed==null)
                {
                    valid="No speed specified for SPEED intensity step";
                }
                else if (intensity==IntensityType.CADENCE && intensityCadence==null)
                {
                    valid="No cadence specified for CADENCE intensity step";
                }
            }
            if (valid.equals("OK") && extent==ExtentType.REACHHRZONE)
            {
                if (intensity!=IntensityType.NONE)
                {
                    valid="A step with extent REACHHRZONE must have intensity=NONE";
                } 
                else if (type!=StepType.REST)
                {
                   valid="A step with extent REACHHRZONE must have type=REST"; 
                }
            }
            return valid;
        }
    }
    
    /**
     * Represents the definition of a single workout
     */
    public static class WorkoutTemplate
    {
        public String                       fileId;         // ID of the workout file, "0x00BEnnnn"
        public String                       workoutId;      // OPTIONAL, UUID as 16 byte hex; if not passed, one is generated
        public String                       workoutMd5;     // OPTIONAL, MD5 hash of the workout file
        public String                       name;
        public String                       description;
        public ActivityType                 activity;
        public WorkoutType                  type;
        public IntensityLevel               intensityLevel;
        public final List<StepTemplate>     steps;
        
        /**
         * Constructor
         */
        public WorkoutTemplate()
        {
            steps=new ArrayList<>();
        }
        
        /**
         * Checks consistency of the workout
         * @return True if consistent, false if not
         */
        public String validate()
        {
            String                  valid;
            Iterator<StepTemplate>  it;
            StepTemplate            step;
            
            valid="OK";
            if (fileId==null || Integer.parseInt(fileId.substring(2), 16)<0x00BE0001 || Integer.parseInt(fileId.substring(2), 16)>0x00BEFFFF)
            {
                valid="Illegal fileID; must be 00BE0001-00BEFFFF";
            }
            else if (workoutId!=null && workoutId.length()!=32)
            {
                valid="Illegal workoutId length; must be 32";
            }
            else if (name==null)
            {
                valid="Workout has no name";
            }
            else if (description==null)
            {
                valid="Workout has no description";
            }
            else if (activity==null)
            {
                valid="Illegal workout activity; must be RUNNING or CYCLING";
            }
            else if (type==null)
            {
                valid="Illegal workout type; must be FATBURN, FITNESS, ENDURANCE, POWER, SPEED or CUSTOM";
            }
            else if (steps==null || steps.isEmpty())
            {
                valid="Workout has no steps";
            }
            else
            {
                it=steps.iterator();
                while (it.hasNext() && valid.equals("OK"))
                {
                    step=it.next();
                    valid=step.validate();
                }
            }
            
            return valid;
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
        // some defaults
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
        WorkoutListTemplate deserialized=null;
        try
        {
            deserialized = gson.fromJson(json, WorkoutListTemplate.class);
        }
        catch(JsonSyntaxException e)
        {
            DebugLogger.error("Error decoding JSON");
        }
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
            workoutTemplate.workoutId       =ToolBox.bytesToHexString(item.getWorkoutId(), 32); 
            workoutTemplate.workoutMd5      =ToolBox.bytesToHexString(item.getWorkoutMd5(), 32); 
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
                stepTemplate.extent     =step.getStepExtent();
                switch (step.getStepExtent())
                {
                    case DISTANCE:
                        stepTemplate.extentDistance=step.getExtentDistance()/1000;      // mm -> m
                        break;
                    case TIME:
                        stepTemplate.extentTime=step.getExtentTime();                   // sec
                        break;
                    case REACHHRZONE:
                        stepTemplate.extentReachHrZone=step.getExtentReachHrZone();
                        break;
                }
                stepTemplate.intensity  =step.getIntensity();
                switch (step.getIntensity())
                {
                    case CADENCE:
                        stepTemplate.intensityCadence=step.getIntensityCadence();
                        break;
                    case PACE:
                        stepTemplate.intensityPace=step.getIntensityPace()/1000;        // msec/km -> sec/km
                        break;
                    case SPEED:
                        stepTemplate.intensitySpeed=step.getIntensitySpeed()*3600/1000; // mm/sec -> m/hr
                        break;
                    case HRZONE:
                        stepTemplate.intensityHrZone=step.getIntensityHrZone();
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
        byte[]      calculatedMd5;
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
            workoutToAdd.setWorkoutUid(workoutId);
            workoutToAdd.setIntensityLevel(workout.intensityLevel);
            
            int stepCount=0;
            for (StepTemplate step : workout.steps)
            {
                WorkoutStep stepToAdd=new WorkoutStep(stepCount, step.name, step.description, step.type);
                switch (step.extent)
                {
                    case DISTANCE:
                        stepToAdd.setExtentDistance(step.extentDistance*1000);    // m to mm
                        break;
                    case TIME:
                        stepToAdd.setExtentDuration(step.extentTime);             // sec
                        break;
                    case REACHHRZONE:
                        stepToAdd.setExtentReachHrZone(step.extentReachHrZone);
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
                    case CADENCE:
                        stepToAdd.setIntensityCadence(step.intensityCadence);
                        break;
                    case HRZONE:
                        stepToAdd.setIntensityHrZone(step.intensityHrZone);
                        break;
                    case PACE:
                        stepToAdd.setIntensityPace(step.intensityPace*1000);
                        break;
                    case SPEED:
                        stepToAdd.setIntensitySpeed(step.intensitySpeed*1000/3600);
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
            itemToAdd.setFileSize(workoutToAdd.getWorkoutDataLength());
            itemToAdd.setIntensityLevel(workout.intensityLevel);
        
            itemToAdd.setWorkoutId(workoutId);
            calculatedMd5=workoutToAdd.getWorkoutMd5Hash();
            if (workout.workoutMd5!=null)
            {
                if (!workout.workoutMd5.toLowerCase().equals(ToolBox.bytesToHexString(calculatedMd5,32)))
                {
                    DebugLogger.info(String.format("%s - MD5 hash passed does not correspond to calculated MD5, using calculated MD5", workout.fileId));
                }
            }
            itemToAdd.setWorkoutMd5(calculatedMd5);

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
    
    /**
     * Counts the workouts of given activity and type
     * @param activity The activity of the workout to filter
     * @param type The type of the workout to filter
     * @return Number of workouts of given type and activity
     */
    private int countWorkouts(ActivityType activity, WorkoutType type)
    {
        int                         count;
        Iterator<WorkoutTemplate>   it;
        WorkoutTemplate             workout;
        count=0;
        it=workouts.iterator();
        while (it.hasNext())
        {
            workout=it.next();
            if (workout.activity==activity && workout.type==type)
            {
                count++;
            }
        }
        return count;
    }
    
    /**
     * This method performs consistency checks on the content in this template 
     * @return A string indicating 'OK' if valid, else the reason for invalidity
     */
    public String validate()
    {
        String                      valid;
        Iterator<String>            it;
        Iterator<WorkoutTemplate>   workoutIt;
        WorkoutTemplate             workout;
        HrZoneTemplate              previousHrZone;
        HrZoneTemplate              hrZone;
        
        valid="OK";
        if (hrZones.size()!=5)
        {
            valid="Illegal number of HR Zones\n";
        }
        else
        {
            it=hrZones.keySet().iterator();
            previousHrZone=null;
            while (it.hasNext() && valid.equals("OK"))
            {
                hrZone=hrZones.get(it.next());
                if (hrZone.hrMax<hrZone.hrMin)
                {
                    valid=String.format("Invalid HR Zone - hrMax(%d)<hrMin(%d)", hrZone.hrMax, hrZone.hrMin);
                }
                else if (previousHrZone!=null && hrZone.hrMin!=previousHrZone.hrMax+1)
                {
                    valid="Invalid HR Zones; no gap allowed between zones";
                }
                previousHrZone=hrZone;
            }
        }
        for (ActivityType activity : ActivityType.values())
        { 
            for (WorkoutType type : WorkoutType.values())
            { 
                if (countWorkouts(activity, type)>5)
                {
                    valid=String.format("More than 5 workouts are defined for %s, %s", activity.toString(), type.toString());
                }
            }
        }        
        workoutIt=workouts.iterator();
        while (workoutIt.hasNext() && valid.equals("OK"))
        {
            workout=workoutIt.next();
            valid=workout.validate();
        }
        
        return valid;
    }
    
}
