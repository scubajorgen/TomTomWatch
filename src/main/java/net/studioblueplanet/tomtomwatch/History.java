/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.usb.UsbFile;

import java.util.ArrayList;
import java.util.Iterator;
import net.studioblueplanet.logger.DebugLogger;

/**
 * This class represents the history as present on the device in history 
 * data files (a short description in a small file)
 * @author Jorgen
 */
public class History
{
    private ArrayList<HistoryItem>      historyEntries;

    
    /**
     * Constructor. Creates the list
     */
    public History()
    {
        historyEntries=new ArrayList();
    }
    
    
    /**
     * Adds a history item as encoded in the file
     * @param file File containing the history item data
     * @return True if an error occurred, false if all went ok
     */
    public boolean addHistoryItemFromFile(UsbFile file)
    {
        HistoryItem item;
        boolean     error;
        
        DebugLogger.info(String.format("Parsing history file 0x%08x", file.fileId));
        error=false;
        item=new HistoryItem(file);
        
        if (item!=null)
        {
            historyEntries.add(item);
        }
        else
        {
            error=true;
        }
        return error;
    }
    
    /**
     * Get a list of history item descriptions
     * @return The list as String.
     */
    public String getDescription()
    {
        Iterator<HistoryItem>   it;
        HistoryItem             entry;
        String                  description;
        int                     fileId;
        
        description="";
        it=historyEntries.iterator();
        while (it.hasNext())
        {
            entry=it.next();
            fileId=entry.getFileId();
//            description+=String.format("\nHISTORY ITEM %s - %s\n", entry.getDescription(), entry.getIndex());
            description+="\nHISTORY ITEM - " + entry.getAcitivity()+" - " + entry.getIndex()+ String.format(" (0x%08x)", entry.getFileId())+"\n";
            description+=entry.getDescription();
        }
        return description;
    }
            
}
