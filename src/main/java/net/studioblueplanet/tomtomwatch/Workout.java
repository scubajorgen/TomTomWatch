/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class represents the workout as defined by a TomTom workout file 
 * (0x00BEnnnn, where nnnn larger than 0). It contains name, ID, description,
 * type and the steps defining the  workout
 * @author jorgen
 */
public class Workout
{
    /**
     * Class of the workout
     */
    public enum WorkoutType
    {
        FATBURN  (1, "FAT BURN"),
        ENDURANCE(2, "ENDURANCE"),
        FITNESS  (3, "FITNESS"),
        SPEED    (4, "SPEED"),
        POWER    (5, "POWER"),
        CUSTOM   (6,"CUSTOM"); //?

        private final int       value;
        private final String    description;

        /**
         * Constructor
         * @param i Enum value
         */
        WorkoutType(int i, String description)
        {
            this.value          =i;
            this.description    =description;
        }

        /**
         * Returns the value of this enum entry
         * @return The value
         */
        public int getValue()
        {
            return value;
        }
        
        /**
         * Get the enum based on the enum value passed
         * @param i Enum value
         * @return The enum or null if not found
         */
        public static WorkoutType getWorkoutClass(int i)
        {
            for (WorkoutType w : WorkoutType.values())
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
    
    private static final int MANUFACTURER_ID   =0x1234DAEB;
    private static final int FILEID_WORKOUT    =0x00090100;
    
    private final WorkoutDescriptions           workoutDescriptions;
    
    private byte[]                              id;
    private WorkoutType                         workoutType;
    private int                                 workoutNameId=0;
    private int                                 workoutDescriptionId=1;
    private int                                 unknown11=2;
    private final HashMap<Integer, WorkoutStep> workoutSteps;

    /**
     * Constructor, initializes the instance
     */
    public Workout()
    {
        workoutSteps        = new HashMap<>();
        workoutDescriptions = new WorkoutDescriptions();
    }

    /**
     * This method adds a description to the list of descriptions associated
     * with this workout
     * @param id ID of the description
     * @param descriptionText Description text
     */
    public void putDescription(int id, String descriptionText)
    {
        workoutDescriptions.addDescription(id, descriptionText);
    }
    
    /**
     * Returns a description text from the list of descriptions associated
     * with this workout
     * @param id ID of the description
     * @return The description text.
     */
    public String getDescription(int id)
    {
        return workoutDescriptions.findDescription(id);
    }

    /** 
     * Find the index for a given description
     * @param description Description to find
     * @return The index, or -1 if not found
     */
    private int findDescriptionIndex(String description)
    {
        return workoutDescriptions.findDescriptionIndex(description);
    }

    /**
     * Sets the workout name, as ID in the descriptions list
     * @param workoutNameId The ID in the description list
     */
    public void setWorkoutName(int workoutNameId)
    {
        this.workoutNameId=workoutNameId;
    }
    
    /**
     * Sets the workout name as String. If the string does not exist in the 
     * descriptions list, it will be added.
     * @param name Name to add
     */
    public void setWorkoutName(String name)
    {
        int index;
        index=workoutDescriptions.findDescriptionIndex(name);
        if (index<0)
        {
            index=workoutDescriptions.addDescription(name);
        }
        this.workoutNameId=index;
    }
    
    /**
     * Sets the workout description as String. If the string does not exist in the 
     * descriptions list, it will be added.
     * @param description Description to add
     */
    public void setWorkoutDescription(String description)
    {
        int index;
        index=workoutDescriptions.findDescriptionIndex(description);
        if (index<0)
        {
            index=workoutDescriptions.addDescription(description);
        }
        this.workoutDescriptionId=index;
    }
    /**
     * Returns the name of the workout
     * @return The name (like 'Warm up', 'Work', 'Rest')
     */
    public String getWorkoutName()
    {
        return workoutDescriptions.findDescription(workoutNameId);
    }

    /**
     * Sets the workout description, as ID in the descriptions list
     * @param workoutDescriptionId The ID in the description list
     */
    public void setWorkoutDescription(int workoutDescriptionId)
    {
        this.workoutDescriptionId=workoutDescriptionId;
    }
    
    /**
     * Returns the description of the workout
     *
     * @return The description, like 'Get into the Fat Burn HR zone'
     */
    public String getWorkoutDescription()
    {
        return workoutDescriptions.findDescription(workoutDescriptionId);
    }
    /**
     * Get the UUID
     * @return The UUID as 16 bytes
     */
    public byte[] getId()
    {
        return id;
    }

    /**
     * Set the UUID
     * @param id The UUID as 16 byte value
     */
    public void setId(byte[] id)
    {
        this.id = id;
    }

    /**
     * Get the unknown 11 value
     * @return The value
     */
    public int getUnknown11()
    {
        return unknown11;
    }

    /**
     * Set the unknown11 value
     * @param unknown11 The unknown11 value
     */
    public void setUnknown11(int unknown11)
    {
        this.unknown11 = unknown11;
    }
    
    /**
     * Sets the class/type of the workout
     * @param workoutType The class/type
     */
    public void setWorkoutType(WorkoutType workoutType)
    {
        this.workoutType=workoutType;
    }

    /**
     * Returns the class of workout
     *
     * @return The class, like ENDURANCE, FATBURN
     */
    public WorkoutType getWorkoutType()
    {
        return workoutType;
    }

    /**
     * Returns the list of steps that make up the workout
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
        workoutDescriptions.addDescription(workoutStep.getName());
        workoutDescriptions.addDescription(workoutStep.getDescription());
        workoutSteps.put(id, workoutStep);
    }
    /*############################################################################################*\
     * The Protobuf methods     
    \*############################################################################################*/    

    /**
     * Level 7: get the step intensity
     * @param step The step to encode
     * @return A fully 
     */
    private WorkoutProto.Intensity buildIntensity(WorkoutStep step)
    {
        WorkoutProto.Intensity.Builder builder;
        
        builder=WorkoutProto.Intensity.newBuilder();
        
        switch (step.getIntensity())
        {
            case HRZONE:
                builder.setHeartratezone(step.getIntensityHrZone().getValue());
                break;
            case PACE:
                builder.setPace(step.getIntensityPace());
                break;
            case SPEED:
                builder.setPace(step.getIntensitySpeed());
                break;
        }
        
        return builder.build();
    }
    
    /**
     * Level 7: build the step extent
     * @param step The step to encode
     * @return Fully populated extent
     */
    private WorkoutProto.Length buildExtent(WorkoutStep step)
    {
        WorkoutProto.Length.Builder builder;
        
        builder=WorkoutProto.Length.newBuilder();
        
        switch (step.getStepExtent())
        {
            case TIME:
                builder.setTime(step.getExtentTime());
                break;
            case DISTANCE:
                builder.setDistance(step.getExtentDistance());
                break;
            case REACHHRZONE:
                builder.setReachZone(step.getExtentReachHrZone().getValue());
                break;
        }
        
        return builder.build();        
    }
    
    /**
     * Level 5 and 6: build a workout step
     * @param id ID/number of the step
     * @param step The step
     * @return A fully populated step
     */
    private WorkoutProto.WorkoutStep buildWorkoutStep(int id, WorkoutStep step)
    {
        WorkoutProto.WorkoutStep.Builder subBuilder;
        WorkoutProto.WorkoutStepSub.Builder stepBuilder;
        
        subBuilder=WorkoutProto.WorkoutStep.newBuilder();
        
        stepBuilder=WorkoutProto.WorkoutStepSub.newBuilder();
        
        stepBuilder.setStepName(findDescriptionIndex(step.getName()));
        stepBuilder.setStepDescription(findDescriptionIndex(step.getDescription()));
        stepBuilder.setStepNumber(id);
        stepBuilder.setStepType(step.getType().getValue());
        stepBuilder.setStepLength(buildExtent(step));
        stepBuilder.setIntensity(buildIntensity(step));
        
        subBuilder.setStepSub(stepBuilder.build());
        return subBuilder.build();
    }
    
    /**
     * Level 4: Build single item description
     * @param id Id of the description
     * @param description Text of the description
     * @return A fully populated item
     */
    private WorkoutProto.Description buildWorkoutDescription(int id, String description)
    {
        WorkoutProto.Description.Builder builder;
        
        builder=WorkoutProto.Description.newBuilder();
        builder.setId(id);
        builder.setDescription(description);
        return builder.build();
    }
    
    /**
     * Level 4: Build the workout
     * @return The fully populated workout
     */
    private WorkoutProto.Workout buildWorkout()
    {
        WorkoutProto.Workout.Builder builder;
        
        builder=WorkoutProto.Workout.newBuilder();
        builder.setName(workoutNameId);
        builder.setDescription(workoutDescriptionId);
        builder.setId(ByteString.copyFrom(id));
        builder.setType(workoutType.getValue());
        builder.setUnknown11(unknown11);
        for (Integer key   : workoutSteps.keySet()) 
        {
             WorkoutStep step = workoutSteps.get(key);  //get() is less efficient 
             builder.addStep(buildWorkoutStep(key, step));
        }
        return builder.build();
    }
    
    /**
     * Level 3: build the sub-data container
     * @return 
     */
    private WorkoutProto.SubDataContainer buildSubDataContainer()
    {
        Iterator<Integer>   it;
        int                 index;
        
        WorkoutProto.SubDataContainer.Builder   builder;
        builder=WorkoutProto.SubDataContainer.newBuilder();
        it=workoutDescriptions.iterator();
        while (it.hasNext()) 
        {
            index=it.next();
            String value = workoutDescriptions.findDescription(index);  
            builder.addWorkoutDescription(buildWorkoutDescription(index, value));
        }
        
        builder.setWorkout(buildWorkout());
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
        metadataBuilder.setFileType(FILEID_WORKOUT);
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
     * Returns the workout as protobuf data
     * @return The protobuf data as bytes
     */
    public byte[] getWorkoutData()
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
     * Return the length of the protobuf data
     * @return The length of the protobuf data
     */
    public int getWorkoutDataLength()
    {
        byte[] temp;
        temp=getWorkoutData();
        return temp.length;
    }
    
    /**
     * Returns a textual representation of the workout
     * @return String, multiple lines
     */
    @Override
    public String toString()
    {
        String  outputString;
        int     i;
        
        outputString ="____________________________________________________________________________________________________\n";
        outputString+=String.format("    %-10s - %s", workoutType, workoutDescriptions.findDescription(workoutNameId))+"\n    "+
                      workoutDescriptions.findDescription(workoutDescriptionId)+"\n";
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
