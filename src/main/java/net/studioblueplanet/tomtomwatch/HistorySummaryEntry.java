/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.tomtomwatch;

import hirondelle.date4j.DateTime;
import net.studioblueplanet.generics.ToolBox;

/**
 * This class represents one record in the activity history. Whereas a ttbin file
 * represents all data of an activity, this represents just the summary.
 * @author Jorgen
 */
public class HistorySummaryEntry
{
    // The index is maximised on 10. All subsequent entries have index 10
    private int             index;
    
    // Byte defining the activity. Same code as used in the TTBIN files
    private int             activityType;
    
    // Date time of the activity. Local time as defined/displayed by the watch
    private DateTime        dateTime;
    
    // duration in seconds
    private int             duration;
    
    // Distance in m
    private float           distance;
    
    /**
     * Constructor. 
     * @param entryData The raw data describing the history entry. The format
     *                  varies per watch type.
     *                  Multisport  : 77 bytes, 
     *                  Runner/spark: 80 bytes (?), 
     *                  Adventurer  : 72 bytes
     */
    public HistorySummaryEntry(byte[] entryData)
    {
        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;
        
        // Generic data, the same for all watch types
        index           =ToolBox.readUnsignedInt(entryData,  0, 4, true);
        activityType    =ToolBox.readUnsignedInt(entryData,  4, 1, true);
        year            =ToolBox.readUnsignedInt(entryData, 13, 4, true);
        month           =ToolBox.readUnsignedInt(entryData, 17, 4, true);
        day             =ToolBox.readUnsignedInt(entryData, 21, 4, true);
        hour            =ToolBox.readUnsignedInt(entryData, 33, 4, true);
        minute          =ToolBox.readUnsignedInt(entryData, 37, 4, true);
        second          =ToolBox.readUnsignedInt(entryData, 41, 4, true);
        
        // Quick and dirty device identification:
        // spark, runner, adventurer
        if ((entryData.length==72) ||(entryData.length==80))
        {
            duration        =ToolBox.readUnsignedInt(entryData, 56, 4, true);
            distance        =ToolBox.readFloat(entryData, 60, true);
        }
        // multisport
        else if (entryData.length==77)
        {
            // Not tested
            duration        =ToolBox.readUnsignedInt(entryData, 53, 4, true);
            distance        =ToolBox.readFloat(entryData, 57, true);
        }
        else
        {
            duration        =0;
            distance        =0.0f;
        }
        
        dateTime=new DateTime(year, month, day, hour, minute, second, 0);
    }
    
    /**
     * Returns a single line describing the history entry.
     * @return The description, followed by a new-line
     */
    public String getDescription()
    {
        int hours;
        int minutes;
        int seconds;
        
        hours       =duration/3600;
        minutes     =duration/60-(hours*60);
        seconds     =duration-hours*3600-minutes*60;
        return String.format("% 3d. ", index)+dateTime.format("YYYY-MM-DD hh:mm:ss ")+
               String.format("%02dh%02d'%02d\" ", hours, minutes, seconds)+
               String.format(" %6.2f km", distance/1000.0)+"\n";
    }
}
