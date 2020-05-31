/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import net.studioblueplanet.logger.DebugLogger;

import net.studioblueplanet.tomtomwatch.Workout.WorkoutClass;
import net.studioblueplanet.tomtomwatch.WorkoutStep.HrZone;
import net.studioblueplanet.tomtomwatch.WorkoutStep.WorkoutStepType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.WorkoutStepExtentType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.WorkoutStepIntensityType;

/**
 *
 * @author jorgen
 */
public class Workouts
{
    class Description
    {
        int     id;
        String  description;
    };
    
    private final HashMap<Integer, String>    descriptions;
    private final List<Workout>               workouts;
    
    /**
     * Constructor. Initializes the instance
     */
    public Workouts()
    {
        workouts        =new ArrayList<>();
        descriptions    =new HashMap<>();
    }
    
    
    /**
     * Process the list of descriptions into a HashMap. The descriptions are 
     * referred to from the Workout and Program
     * @param data Data sub container
     */
    private void processDescriptions(WorkoutProto.SubDataContainer data)
    {
        List<WorkoutProto.Description> descs=data.getWorkoutDescriptionList();
        for(WorkoutProto.Description desc : descs)
        {
            descriptions.put(desc.getId(), desc.getDescription());
        }
    }
    
    /**
     * Process the Workout. Get the data and Workout steps
     * @param data Data to process
     * @return The workout, or null if something went wrong
     */
    private Workout processWorkout(WorkoutProto.SubDataContainer data)
    {
        Workout                         workout;
        WorkoutProto.Workout            protoWorkout;
        WorkoutProto.WorkoutStepSub     step;
        WorkoutProto.Size               size;
        WorkoutProto.Intensity          intensity;
        List<WorkoutProto.WorkoutStep>  workoutSteps;
        WorkoutStep                     workoutStep;
        boolean                         error;
        
        error   =false;
        workout =null;
        if (data.hasWorkout())
        {
            protoWorkout=data.getWorkout();
            workout=new Workout(descriptions.get(0), descriptions.get(1), WorkoutClass.getWorkoutClass(protoWorkout.getType()));
            workoutSteps=protoWorkout.getStepList();
            for (WorkoutProto.WorkoutStep stepContainer : workoutSteps)
            {
                step                =stepContainer.getStepSub();
                int id              =step.getStepNumber();
                WorkoutStepType type=WorkoutStepType.getWorkoutStepType(step.getStepType());
                workoutStep         =new WorkoutStep(descriptions.get(step.getStepName()), descriptions.get(step.getStepDescription()), type);
                
                // Set the extent of the workout step
                if (step.hasStepSize())
                {
                    size=step.getStepSize();
                    if (size.hasDistance())
                    {
                        workoutStep.setStepExtentDistance(size.getDistance());
                    } else if (size.hasDuration())
                    {
                        workoutStep.setStepExtentDuration(size.getDuration());
                    } else if (size.hasManual())
                    {
                        workoutStep.setStepExtentManual();
                    } else if (size.hasReachZone())
                    {
                        workoutStep.setStepExtentReachHrZone(HrZone.getHrZoneType(size.getReachZone()));
                    }
                }
                else
                {
                    DebugLogger.error("No extent of the workout step found");
                    error=true;
                }
                
                // Set the intensity at which the workout step must be performed; is optional
                if (step.hasIntensity())
                {
                    intensity=step.getIntensity();
                    if (intensity.hasPace())
                    {
                        workoutStep.setStepIntensityPace(intensity.getPace());
                    }
                    else if (intensity.hasHeartratezone())
                    {
                        workoutStep.setStepIntensityHrZone(HrZone.getHrZoneType(intensity.getHeartratezone()));
                    }
                    else if (intensity.hasSpeed())
                    {
                        workoutStep.setStepIntensitySpeed(intensity.getSpeed());
                    }
                }
                
                workout.addWorkoutStep(id, workoutStep);
            }
        }
        else
        {
            DebugLogger.error("No workout found");
            error=true;
        }
        
        if (error)
        {
            workout=null;
        }
        return workout;
    }


    
    /**
     * Appends the workout data from the 0x00BEnnnn files, where
     * nnnn larger than zero
     * @param data Protobuffer encoded data
     * @return False if all went ok, true if an error occurred
     */
    public boolean appendFromData(byte[] data)
    {
        boolean                                 error;
        Workout                                 workout;
        boolean                                 exit;
        WorkoutProto.Root                       root;
        WorkoutProto.RootContainer              container;
        WorkoutProto.Metadata                   metadata;
        List<WorkoutProto.RootContainer>        containers;
        Iterator<WorkoutProto.RootContainer>    containerIt;  
        WorkoutProto.SubDataContainer           dataContainer;
        
        error=false;
        
        try
        {
            root                        =WorkoutProto.Root.parseFrom(data);
            exit                        =false;
            containers                  =root.getSubDataContainerList();
            containerIt                 =containers.iterator();
            while (containerIt.hasNext() && !exit)
            {
                container=containerIt.next();
                
                if (container.hasMetadata())
                {
                    metadata=container.getMetadata();
                    if (metadata.hasFileType())
                    {
                        int manufacturer=metadata.getManufacturer();
                        if (manufacturer!=0x1234DAEB)
                        {
                            DebugLogger.error("Invalid workout file. Manufacturer code.");
                            error=true;
                        }                        int fileType=metadata.getFileType();
                        if (fileType!=0x00090100)
                        {
                            DebugLogger.error("Invalid workout file. File type not correct");
                            error=true;
                        }
                    }
                }
                
                if (container.hasDataContainer())
                {
                    dataContainer   =container.getDataContainer().getSubDataContainer();
                    
                    // The subdata container contains descriptions and the workout record.
                    if (dataContainer.hasWorkout())
                    {
                        processDescriptions(dataContainer);
                        workout=processWorkout(dataContainer);
                        if (workout!=null)
                        {
                            workouts.add(workout);
                        }
                    }
                }                
            }            
                    
        }
        catch (InvalidProtocolBufferException e)
        {
            DebugLogger.error("Error parsing tracker file: "+e.getMessage());
            error               =true;
        }
        return error;
    }    
    
    @Override
    public String toString()
    {
        String outputString;
        
        outputString="--- WORKOUTS ---\n";
        for(Workout workout : workouts)
        {
            outputString+=workout;
        }
        
        return outputString;
    }
}
