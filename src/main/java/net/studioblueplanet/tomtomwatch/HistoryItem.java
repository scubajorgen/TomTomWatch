/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;


import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.ttbin.Activity;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents one activity summary as stored on the watch
 * as a file with file id 0x0072aaii. aa is the activity code, ii the index
 * (0x00 - 0x0a)
 * @author Jorgen
 */
public class HistoryItem
{
    private int                             numberOfValues;
    private final ArrayList<HistoryValue>   values;
    private int                             fileId;
    
    /**
     * Cosntructor. Constructs the item from the file
     * @param file 
     */
    public HistoryItem(UsbFile file)
    {
        byte[]          bytes;
        int             i;
        int             tag;
        HistoryValue    value;
        
        values=new ArrayList();

        fileId=file.fileId;

        
        bytes=file.fileData;
        
        numberOfValues=ToolBox.readInt(bytes, 1, 4, true);
        
        if (numberOfValues!=(bytes.length-5)/5)
        {
            numberOfValues=(bytes.length-5)/5;
            DebugLogger.error("Error parsing history item: number of entries not correct");
        }

        
        i=0;
        while (i<numberOfValues)
        {
            tag=ToolBox.readInt(bytes, 5+5*i, 1, true);
            value=new HistoryValue(tag, bytes, 6+5*i);
            values.add(value);
            i++;
        }
        
    }
    
    /**
     * Returns the fileId
     * @return The file ID
     */
    public int getFileId()
    {
        return this.fileId;
    }
    
    /**
     * Returns the activity description
     * @return The description
     */
    public String getAcitivity()
    {
        String description;
        
        description = Activity.getActivityDescription((fileId>>8)&0xff);
        
        return description;
    }
    
    /**
     * Returns the index
     * @return The index (0-10)
     */
    public String getIndex()
    {
        return String.format("%s", fileId&0xff);
    }
    
    /**
     * Returns the description. A list of tag-values
     * @return String with the tag-value on each line
     */
    public String getDescription()
    {
        String                      description;
        Iterator<HistoryValue>      it;
        HistoryValue                value;
        
        description ="";
        
        it=values.iterator();
        
        while (it.hasNext())
        {
            value   =it.next();
            description+=value.getDescription();
        }

        return description;
    }
}
