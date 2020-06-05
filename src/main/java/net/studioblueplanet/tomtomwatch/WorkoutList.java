/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import net.studioblueplanet.logger.DebugLogger;

import net.studioblueplanet.tomtomwatch.Workout.WorkoutClass;
import net.studioblueplanet.tomtomwatch.WorkoutListItem.ActivityType;
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
     * Returns the list with workout metadata
     * @return The list
     */
    public List<WorkoutListItem> getWorkoutList()
    {
        return workoutListItems;
    }
    
    /**
     * Return the hashmap with workouts. Workouts are identified by 
     * the fileID (as key)
     * @return The workouts
     */
    public HashMap<Integer, Workout> getWorkouts()
    {
        return workouts;
    }
    
    /** 
     * Find the index for a given description
     * @param description Description to find
     * @return The index, or -1 if not found
     */
    private int findDescriptionIndex(String description)
    {
        for (Integer key   : workoutListDescriptions.keySet()) 
        {
             String value = workoutListDescriptions.get(key);  //get() is less efficient 
             if (value.equals(description))
             {
                 return key;
             }
        }        
        return -1;
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
        WorkoutProto.Extent             extent;
        WorkoutProto.Intensity          intensity;
        List<WorkoutProto.WorkoutStep>  workoutSteps;
        WorkoutStep                     workoutStep;
        boolean                         error;
        
        error   =false;
        if (data.hasWorkout())
        {
            protoWorkout=data.getWorkout();
            workout.setWorkoutName(protoWorkout.getName());
            workout.setWorkoutDescription(protoWorkout.getDescription());
            workout.setId(protoWorkout.getId().toByteArray());
            workout.setWorkoutClass(WorkoutClass.getWorkoutClass(protoWorkout.getType()));
            workout.setUnknown11(protoWorkout.getUnknown11());
            workoutSteps=protoWorkout.getStepList();
            for (WorkoutProto.WorkoutStep stepContainer : workoutSteps)
            {
                step                =stepContainer.getStepSub();
                int stepNumber      =step.getStepNumber();
                WorkoutStepType type=WorkoutStepType.getWorkoutStepType(step.getStepType());
                workoutStep         =new WorkoutStep(stepNumber,
                                                     workout.getDescription(step.getStepName()), 
                                                     workout.getDescription(step.getStepDescription()), 
                                                     type);
                
                // Set the extent of the workout step
                if (step.hasStepExtent())
                {
                    extent=step.getStepExtent();
                    if (extent.hasDistance())
                    {
                        workoutStep.setExtentDistance(extent.getDistance());
                    } else if (extent.hasDuration())
                    {
                        workoutStep.setExtentDuration(extent.getDuration());
                    } else if (extent.hasManual())
                    {
                        workoutStep.setExtentManual();
                    } else if (extent.hasReachZone())
                    {
                        workoutStep.setExtentReachHrZone(HrZone.getHrZoneType(extent.getReachZone()));
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
                        workoutStep.setIntensityPace(intensity.getPace());
                    }
                    else if (intensity.hasHeartratezone())
                    {
                        workoutStep.setIntensityHrZone(HrZone.getHrZoneType(intensity.getHeartratezone()));
                    }
                    else if (intensity.hasSpeed())
                    {
                        workoutStep.setIntensitySpeed(intensity.getSpeed());
                    }
                }
                
                workout.addWorkoutStep(stepNumber, workoutStep);
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
        ActivityType                        activity;
        int                                 fileId;
        
        items=data.getWorkoutListItemList();
        for(WorkoutProto.WorkoutListItem item : items)
        {
            fileId      =item.getFileId();
            name        =workoutListDescriptions.get(item.getItemName());
            description =workoutListDescriptions.get(item.getItemDescription());
            activity    =ActivityType.getActivityType(item.getActivity());
            workoutClass=WorkoutClass.getWorkoutClass(item.getType());
            listItem=new WorkoutListItem(fileId, name, description, activity, workoutClass);
            listItem.setId(item.getId().toByteArray());
            listItem.setWorkoutId(item.getWorkoutId().toByteArray());
            listItem.setUnknown7(item.getUnknown7());
            listItem.setUnknown8(item.getUnknown8());
            listItem.setUnknown9(item.getUnknown9());
            listItem.setUnknown12(item.getUnknown12());
            listItem.setUnknown13(item.getUnknown13());
            
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
            containers                  =root.getRootContainerList();
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
     * Creates the workout list from the data from file 0x00BE0000
     * @param data The data
     * @return True if something went wrong
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
            containers                  =root.getRootContainerList();
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
    
    /**
     * Sort the list
     */
    public void sort()
    {
        Collections.sort(this.workoutListItems);
    }
    
    /*############################################################################################*\
     * The Protobuf methods     
    \*############################################################################################*/    
    /**
     * Level 4: Build single item description
     * @param id Id of the description
     * @param description Text of the description
     * @return A fully populated item
     */
    private WorkoutProto.Description buildItemDescription(int id, String description)
    {
        WorkoutProto.Description.Builder builder;
        
        builder=WorkoutProto.Description.newBuilder();
        builder.setId(id);
        builder.setDescription(description);
        return builder.build();
    }
    
    /**
     * Level 4: build the workout list item for the item passed
     * @param item Item to convert
     * @return Fully populated list item
     */
    private WorkoutProto.WorkoutListItem buildWorkoutListItem(WorkoutListItem item)
    {
        WorkoutProto.WorkoutListItem.Builder builder;
        
        builder=WorkoutProto.WorkoutListItem.newBuilder();
        builder.setId(ByteString.copyFrom(item.getId()));
        builder.setWorkoutId(ByteString.copyFrom(item.getWorkoutId()));
        builder.setActivity(item.getActivity().getValue());
        builder.setItemName(findDescriptionIndex(item.getWorkoutName()));
        builder.setItemDescription(findDescriptionIndex(item.getWorkoutDescription()));
        builder.setFileId(item.getFileId());
        builder.setType(item.getWorkoutClass().getValue());
        builder.setUnknown7(item.getUnknown7());
        builder.setUnknown8(item.getUnknown8());
        builder.setUnknown9(item.getUnknown9());
        builder.setUnknown12(item.getUnknown12());
        builder.setUnknown13(item.getUnknown13());
        return builder.build();
    }
    
    /**
     * Level 3: build the sub-data container
     * @return 
     */
    private WorkoutProto.SubDataContainer buildSubDataContainer()
    {
        WorkoutProto.SubDataContainer.Builder   builder;

        builder=WorkoutProto.SubDataContainer.newBuilder();

        for (WorkoutListItem item : this.workoutListItems)
        {
            builder.addWorkoutListItem(buildWorkoutListItem(item));
        }

        for (Integer key   : workoutListDescriptions.keySet()) 
        {
             String value = workoutListDescriptions.get(key);  //get() is less efficient 
             builder.addItemDescription(buildItemDescription(key, value));
        }
        return builder.build();
    }
    
    /**
     * Level 2: build the metadata
     * @return The metadata, fully populated
     */
    private WorkoutProto.Metadata buildMetadata()
    {
        WorkoutProto.Metadata.Builder        metadataBuilder;

        metadataBuilder     =WorkoutProto.Metadata.newBuilder();
        // Set the values: appear to be the same always...
        metadataBuilder.setManufacturer(MANUFACTURER_ID);
        metadataBuilder.setFileType(FILEID_WORKOUTLIST);
        return metadataBuilder.build();
    }
    
    /**
     * Level 2: Build the data container
     * @return The data container, fully populated
     */
    private WorkoutProto.DataContainer buildDataContainer()
    {
        WorkoutProto.DataContainer.Builder      builder;
        
        builder=WorkoutProto.DataContainer.newBuilder();
        builder.setSubDataContainer(buildSubDataContainer());
        return builder.build();
    }
    
    /**
     * Level 1: Build the root container with metadata
     * @return The container recursively filled
     */
    private WorkoutProto.RootContainer buildRootContainerForMetadata()
    {
        WorkoutProto.RootContainer.Builder   rootContainerBuilder;
        WorkoutProto.Metadata.Builder        metadataBuilder;

        rootContainerBuilder=WorkoutProto.RootContainer.newBuilder();
        rootContainerBuilder.setMetadata(buildMetadata());
        return rootContainerBuilder.build();
    }
    
     /**
     * Level 1: Build the root container with data
     * @return The container recursively filled
     */
    private WorkoutProto.RootContainer buildRootContainerForData()
    {
        WorkoutProto.RootContainer.Builder   rootContainerBuilder;
        WorkoutProto.DataContainer.Builder   builder;

        rootContainerBuilder=WorkoutProto.RootContainer.newBuilder();
        rootContainerBuilder.setDataContainer(buildDataContainer());
        return rootContainerBuilder.build();
    }
    
   
    /**
     * Level 0: Returns the workout list as protobuf data
     * @return The protobud data
     */
    public byte[] getWorkoutListData()
    {
        WorkoutProto.Root           root;
        WorkoutProto.Root.Builder   rootBuilder;
        
        rootBuilder         =WorkoutProto.Root.newBuilder();
        
        rootBuilder.addRootContainer(buildRootContainerForMetadata());

        rootBuilder.addRootContainer(buildRootContainerForData());
        

        root                =rootBuilder.build();
        return root.toByteArray();        
    }
    
    /**
     * Returns the workout as protobuf data
     * @return 
     */
    public byte[] getWorkoutData(int fileId)
    {
        Workout workout;
        
        workout=workouts.get(fileId);
        
        return workout.getWorkoutData();
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
                outputString+=item;
                outputString+=workout;
            }
            else
            {
                outputString+=item;
            }
        }
        return outputString;
    }
}
