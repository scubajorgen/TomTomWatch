/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import net.studioblueplanet.logger.DebugLogger;

import net.studioblueplanet.tomtomwatch.Workout.WorkoutClass;
import net.studioblueplanet.tomtomwatch.WorkoutStep.HrZone;
import net.studioblueplanet.tomtomwatch.WorkoutStep.WorkoutStepType;

/**
 * This class represents the list of workouts. On the TomTom watch, this list is 
 * represented by the file 0x00BE0000. This class also contains all the workouts
 * defined in subsequent files: 0x00BE0001, 0x00BE0002, etc
 * @author jorgen
 */
public class WorkoutList
{
    class Description
    {
        int     id;
        String  description;
    };
    
    private static final int MANUFACTURER_ID   =0x1234DAEB;
    private static final int FILEID_WORKOUTLIST=0x00080100;
    private static final int FILEID_WORKOUT    =0x00090100;
    

    private final HashMap<Integer, String>    workoutListDescriptions;
    private final HashMap<Integer, Workout>   workouts;
    private final ArrayList<WorkoutListItem>  workoutListItems;
    
    /**
     * Constructor. Initializes the instance
     */
    public WorkoutList()
    {
        workoutListDescriptions =new HashMap<>();
        workouts                =new HashMap<>();
        workoutListItems        =new ArrayList<>();
    }
    
    
    /**
     * Process the list of descriptions associated with 
     * the workout into a HashMap. The descriptions are 
     * referred to from the Workout and Program
     * @param data Data sub container
     */
    private void processDescriptions(Workout workout, WorkoutProto.SubDataContainer data)
    {
        List<WorkoutProto.Description> descs=data.getWorkoutDescriptionList();
        for(WorkoutProto.Description desc : descs)
        {
            workout.putDescription(desc.getId(), desc.getDescription());
        }
    }

    /**
     * Process the list of descriptions into a HashMap. The descriptions are 
     * referred to from the Workout and Program
     * @param data Data sub container
     */
    private void processItemDescriptions(WorkoutProto.SubDataContainer data)
    {
        List<WorkoutProto.Description> descs=data.getItemDescriptionList();
        for(WorkoutProto.Description desc : descs)
        {
            workoutListDescriptions.put(desc.getId(), desc.getDescription());
        }
    }
    
    /**
     * Process the Workout. Get the data and Workout steps
     * @param data Data to process
     * @return True if something went wrong
     */
    private boolean processWorkout(Workout workout, WorkoutProto.SubDataContainer data)
    {
        WorkoutProto.Workout            protoWorkout;
        WorkoutProto.WorkoutStepSub     step;
        WorkoutProto.Size               size;
        WorkoutProto.Intensity          intensity;
        List<WorkoutProto.WorkoutStep>  workoutSteps;
        WorkoutStep                     workoutStep;
        boolean                         error;
        
        error   =false;
        if (data.hasWorkout())
        {
            protoWorkout=data.getWorkout();
            workout.setWorkoutClass(WorkoutClass.getWorkoutClass(protoWorkout.getType()));
            workoutSteps=protoWorkout.getStepList();
            for (WorkoutProto.WorkoutStep stepContainer : workoutSteps)
            {
                step                =stepContainer.getStepSub();
                int id              =step.getStepNumber();
                WorkoutStepType type=WorkoutStepType.getWorkoutStepType(step.getStepType());
                workoutStep         =new WorkoutStep(workout.getDescription(step.getStepName()), workout.getDescription(step.getStepDescription()), type);
                
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
        
        return error;
    }

    /**
     * Process the Workout list. The list contains only meta data from 
     * the workouts
     * @param data Data to process
     * @return The workout, or null if something went wrong
     */
    private void processWorkoutList(WorkoutProto.SubDataContainer data)
    {
        List<WorkoutProto.WorkoutListItem>  items;
        WorkoutListItem                     listItem;
        String                              name;
        String                              description;
        WorkoutClass                        workoutClass;
        int                                 fileId;
        
        items=data.getWorkoutListItemList();
        for(WorkoutProto.WorkoutListItem item : items)
        {
            fileId      =item.getFileId();
            name        =workoutListDescriptions.get(item.getItemName());
            description =workoutListDescriptions.get(item.getItemDescription());
            workoutClass=WorkoutClass.getWorkoutClass(item.getType());
            listItem=new WorkoutListItem(fileId, name, description, workoutClass);
            
            workoutListItems.add(listItem);
        }
        
    }
    
    /**
     * Appends the workout data from the 0x00BEnnnn files, where
     * nnnn larger than zero
     * @param fileId ID of the file representing the workout
     * @param data Protobuffer encoded data
     * @return False if all went ok, true if an error occurred
     */
    public boolean appendWorkoutFromData(int fileId, byte[] data)
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
                        if (manufacturer!=MANUFACTURER_ID)
                        {
                            DebugLogger.error("Invalid workout file. Manufacturer code.");
                            error=true;
                        }                        int fileType=metadata.getFileType();
                        if (fileType!=FILEID_WORKOUT)
                        {
                            DebugLogger.error("Invalid workout file. File type not correct");
                            error=true;
                        }
                    }
                }
                
                if (container.hasDataContainer() && !error)
                {
                    dataContainer   =container.getDataContainer().getSubDataContainer();
                    
                    // The subdata container contains descriptions and the workout record.
                    if (dataContainer.hasWorkout())
                    {
                        workout=new Workout();
                        processDescriptions(workout, dataContainer);
                        processWorkout(workout, dataContainer);
                        workouts.put(fileId, workout);
                    }
                }                
            }            
                    
        }
        catch (InvalidProtocolBufferException e)
        {
            DebugLogger.error("Error parsing workout file: "+e.getMessage());
            error               =true;
        }
        return error;
    }    
    
    /**
     * 
     * @param data
     * @return 
     */
    public boolean createWorkoutListFromData(byte[] data)
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
        workoutListDescriptions.clear();
        workoutListItems.clear();
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
                        if (manufacturer!=MANUFACTURER_ID)
                        {
                            DebugLogger.error("Invalid workout file. Manufacturer code.");
                            error=true;
                        }                        int fileType=metadata.getFileType();
                        if (fileType!=FILEID_WORKOUTLIST)
                        {
                            DebugLogger.error("Invalid workout file. File type not correct");
                            error=true;
                        }
                    }
                }
                
                if (container.hasDataContainer())
                {
                    dataContainer   =container.getDataContainer().getSubDataContainer();
                        processItemDescriptions(dataContainer);
                        processWorkoutList(dataContainer);
                }                
            }            
                    
        }
        catch (InvalidProtocolBufferException e)
        {
            DebugLogger.error("Error parsing workout list file: "+e.getMessage());
            error               =true;
        }
        
        return error;
    }
    
    @Override
    public String toString()
    {
        String outputString;
        
        outputString="";

        for(WorkoutListItem item : workoutListItems)
        {
            int fileId=item.getFileId();
            Workout workout=workouts.get(fileId);
            // If the workout has been added, print it; otherwise just print the metadata
            if (workout!=null)
            {
                outputString+=workout;
            }
            else
            {
                outputString+="____________________________________________________________________________________________________\n";
                outputString+=String.format("%-10s - %s", item.getWorkoutClass(), item.getWorkoutName());
                outputString+=item.getWorkoutDescription()+"\n";
            }
        }
        return outputString;
    }
}
