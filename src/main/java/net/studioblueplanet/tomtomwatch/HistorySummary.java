/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.generics.ToolBox;

import net.studioblueplanet.ttbin.Activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This class represents the history of one activity. The summary contains
 * up to 10 most recent entries. It is shown by the watch (at least on the Adventurer)
 * under 'Recent Activities'.
 * It is stored on the watch as a History Summary file per activity (0x0083nnnn).
 * File format
 *  0 uint32    unknown
 *  4 uint16    activity
 *  6 uint16    entry length
 *  8 uint16    number of entries
 * 10 uint16    unknown
 * 12   entries
 * 
 * @author Jorgen
 */
public class HistorySummary
{
    public static final int                 HEADERLENGTH=12;
    private UsbFile                         usbFile;
    private int                             activity;
    private int                             numberOfEntries;
    private int                             entryLength;
    private ArrayList<HistorySummaryEntry>  entries;
   
    
    /**
     * Constructor. The file as read is passed to the method and is parsed
     * @param usbFile History Summary File. The file consists of a 
     *                header and a number of entries. The entry format 
     *                varies per device
     */
    public HistorySummary(UsbFile usbFile)
    {
        int             i;
        byte[]          bytes;
        HistorySummaryEntry    entry;
    
        DebugLogger.info("Parsing history file "+String.format("0x%08x", usbFile.fileId));
        
        this.usbFile        =usbFile;
        this.activity       =ToolBox.readUnsignedInt(this.usbFile.fileData, 4, 2, true);
        this.entryLength    =ToolBox.readUnsignedInt(this.usbFile.fileData, 6, 2, true);
        this.numberOfEntries=ToolBox.readUnsignedInt(this.usbFile.fileData, 8, 2, true);

        
        DebugLogger.debug("History summary file size: "+usbFile.length+". Expecting "+(HEADERLENGTH+entryLength*numberOfEntries));
        
        // Consistency check: filesize can be calculated based on number of entries and entry size
        if (usbFile.length==HEADERLENGTH+entryLength*numberOfEntries)
        {
            this.entries    =new ArrayList();
            bytes=new byte[entryLength];

            i=0;
            while (i<numberOfEntries)
            {
                System.arraycopy(usbFile.fileData, i*entryLength+HEADERLENGTH, bytes, 0, entryLength);
                entry       =new HistorySummaryEntry(bytes);
                this.entries.add(entry);
                i++;
            }
        }
        else
        {
            DebugLogger.error("Error parsing History Summary file "+usbFile.fileId+": File size not as expected");
        }
    }
    
    /**
     * Return a description of this history entry. It consists of the
     * activity description followed by the list of entries.
     * @return A string containing the description
     */
    public String getDescription()
    {
        Iterator<HistorySummaryEntry>      it;
        String                      description;
        HistorySummaryEntry                entry;
        
        description=Activity.getActivityDescription(activity).toUpperCase()+
                    String.format(" (0x%08x)", usbFile.fileId)+"\n";
        it=entries.iterator();
        
        while (it.hasNext())
        {
            entry=it.next();
            description+=entry.getDescription();
        }
        
        return description;
    }
    
    /**
     * Clears the history summary
     */
    public void clearHistorySummary()
    {
        byte[]  newFileData;
        
        
        // The new file will just be the header, without entries
        newFileData             =new byte[HEADERLENGTH];
        
        // Copy the header
        System.arraycopy(usbFile.fileData, 0, newFileData, 0, HEADERLENGTH);
        
        // Set the number of entries in the history summary to zero
        ToolBox.writeUnsignedInt(newFileData, 0, 8, 2, true);
        this.numberOfEntries    =0;
        entries.clear();
        
        this.usbFile.fileData   =newFileData;
        this.usbFile.length     =HEADERLENGTH;
    }
    
    /**
     * Returns the UsbFile instance representing the HistorySummary.
     * When it is cleared, a file is returned that can be written to the
     * watch.
     * @return The UsbFile.
     */
    public UsbFile getHistorySummaryFile()
    {
        return this.usbFile;
    }
    
            
}
