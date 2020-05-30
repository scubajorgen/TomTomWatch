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
     * Process the list of descriptions into a HashMap
     * @param data Data sub container
     */
    private void processDescriptions(WorkoutProto.SubDataContainer data)
    {
        List<WorkoutProto.Description> descs=data.getDescriptionList();
        for(WorkoutProto.Description desc : descs)
        {
            descriptions.put(desc.getId(), desc.getDescription());
        }
    }
    
    private Workout processWorkout(WorkoutProto.SubDataContainer data)
    {
        Workout workout;
        String  name;
        String  description;
        
        
        
        workout=new Workout(descriptions.get(0), descriptions.get(1), null);
        
        return workout;
    }


    
    /**
     * Appends the tracker data from the data presented
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
        WorkoutProto.DataContainer              dataContainer;
        WorkoutProto.SubDataContainer           subDataContainer;
        
        error=false;
        
        try
        {
            root                =WorkoutProto.Root.parseFrom(data);
            exit                =false;
                
            containers                 =root.getSubDataContainerList();
            containerIt                =containers.iterator();
            while (containerIt.hasNext() && !exit)
            {
                container=containerIt.next();
                
                if (container.hasMetadata())
                {
                    metadata=container.getMetadata();
                    if (metadata.hasFileType())
                    {
                        int fileType=metadata.getFileType();
                        if (fileType!=0x00090100)
                        {
                            DebugLogger.error("Invalid workout file");
                            error=true;
                        }
                    }
                }
                
                if (container.hasDataContainer())
                {
                    dataContainer=container.getDataContainer();
                    subDataContainer=dataContainer.getSubDataContainer();
                    
                    // The subdata container contains a record. Get it and  process it
                    if (subDataContainer.hasProgram())
                    {
                        workout=processWorkout(subDataContainer);
                    }
                }                
                else if (container.hasMetadata())
                {
                    // TO DO: meaning of metadata is unknown. So do nothing for the moment
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
}
