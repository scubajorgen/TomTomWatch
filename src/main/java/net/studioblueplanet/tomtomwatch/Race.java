/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.usb.UsbFile;

/**
 *
 * @author Jorgen
 */
public class Race
{
    /** ID of the file */
    private int         fileId;
    
    /** Activity */
    private int         activity;
    
    /** Name of the race */
    private String      name;
    
    /** Distance of the race in m */
    private int         distance;
    
    /** Duration of the race in seconds */
    private int         duration;
    
    /** Number of checkpoints in the race */
    private int         numberOfCheckPoints;
    
    /** The checkpoint distances in m */
    private int[]       checkPoints;
    
    /**
     * Constructor. Constructs the race out of the downloaded file.
     * @param file UsbFile downloaded from the watch containing the race data
     */
    public Race(UsbFile file)
    {
        int checkPointBytes;
        int checkPointAreaSize;
        int i;
        int j;
        int checkPointDistance;
        
        fileId              =file.fileId;
        name                =ToolBox.readString(file.fileData,  0, 15);
        duration            =ToolBox.readInt   (file.fileData, 42,  4, true);
        distance            =ToolBox.readInt   (file.fileData, 46,  4, true);
        numberOfCheckPoints =ToolBox.readInt   (file.fileData, 38,  4, true);
        activity            =(file.fileId>>8)&0xff;
        
        checkPointAreaSize=(file.length-50);
        if(checkPointAreaSize%numberOfCheckPoints==0) 
        {
            checkPointBytes=(file.length-50)/numberOfCheckPoints;
            checkPoints=new int[numberOfCheckPoints];
            i=0;
            while (i<numberOfCheckPoints)
            {
                checkPointDistance=0;
                
                j=0;
                while (j<checkPointBytes)
                {
                    checkPointDistance+=file.fileData[50+i*checkPointBytes+j]&0xff;
                    
                    
                    j++;
                }
                checkPoints[i]=checkPointDistance;
                i++;
            }
        
        }
        else
        {
            DebugLogger.error("Error parsing race data: checkpoints not equal size");
        }
    }
    
    
    /**
     * Returns a string describing the reace
     * @return The race description
     */
    public String getInfo()
    {
        String  info;
        int     i;
        int     points;
        
        info="";
        info+=String.format("0x%08x %-14s %-15s %6d m %6d s", fileId, 
                                                              Activity.getActivityDescription(activity), 
                                                              name, distance, duration);
        
        info+=" [";
        i=0;
        // Print at most 15 checkpoint distances
        points=Math.min(numberOfCheckPoints, 15);
        while (i<points)
        {
            info+=checkPoints[i];
            if (i<numberOfCheckPoints-1)
            {
                info+=" ";
            }
            i++;
        }
        if (numberOfCheckPoints>15)
        {
            info+="...";
        }
        info+="]";
        
        
        return info;
        
    }
}
