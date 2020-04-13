/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.ttbin;


import hirondelle.date4j.DateTime;
import java.util.TimeZone;
import java.io.Writer;
import java.io.IOException;

/**
 * This record represents a record in an activity
 * @author Jorgen
 */
public class ActivityRecord
{
    public static   final   int         INVALID=-9999;
    
    protected static        TimeZone    localTimeZone;      // Local Time time zone (where recorded)
    protected static        TimeZone    utcTimeZone;
    protected               DateTime    dateTime;           // Date time stamp

    protected               int         batteryLevel;       // Battery level in %
    
    /**
     * Constructor. Initialises all on 'not initialised' (INVALID)
     */
    public ActivityRecord()
    {
        this.dateTime           =null;
        this.utcTimeZone        =TimeZone.getTimeZone("UTC");
        this.batteryLevel       =INVALID;
    }

    /**
     * Set the local time zone
     * @param newLocalTimeZone The new local timezone to use
     */
    public static void setLocalTimeZone(TimeZone newLocalTimeZone)
    {
        localTimeZone=newLocalTimeZone;
    }
    
    /**
     * Set the UTC date time stamp 
     * @param utcTime The local time, in seconds (epoch)
     */
    public void setUtcTime(long utcTime)
    {
        // If the device could not record a GPS coordintate, the utcTime=0xffffffff
        if (utcTime!=0xffffffffL)
        {
            dateTime=DateTime.forInstant((utcTime)*1000, utcTimeZone);
        }
        else
        {
            dateTime=null;
        }
    }
   
    /**
     * This mehthod returns the datetime stamp
     * @return The DateTime stamp as UTC date Time Stamp
     */
    DateTime getDateTime()
    {
        return this.dateTime;
    }

    /**
     * Sets the current battery level
     * @param level Level in %
     */
    public void setBatteryLevel(int level)
    {
        this.batteryLevel=level;
    }
    
    /**
     * Returns the current battery level
     * @return The level in % or INVALID if not defined
     */
    public int getBatteryLevel()
    {
        return this.batteryLevel;
    }
    
    /**
     * Checks if this record equals the record passed
     * @param record
     * @return 
     */
    public boolean equals(ActivityRecord record)
    {
        boolean isEqual;
        
        isEqual=true;
        
        if (this.dateTime==null)
        {
            System.err.println();
        }
        
        if (!this.dateTime.equals(record.dateTime))
        {
            isEqual=false;
        }
        return isEqual;
    }
    
    /*############################################################################################*\
     * DEBUGGING
    \*############################################################################################*/    
    
    /**
     * Dump the header containing all record fields
     * @param writer Writer to dump to
     * @throws IOException Thrown when an error occurs during writing
     */
    public void dumpRecordCsvHeader(Writer writer) throws IOException
    {
        writer.write("dateTime");        
    }
    
   /**
     * Dump all the record fields of this record
     * @param writer Writer to dump to
     * @throws IOException Thrown when an error occurs during writing
     */
    public void dumpRecordCsv(Writer writer) throws IOException
    {
        writer.write(this.dateTime.format("YYYY-MM-DD hh:mm:ss"));
    }
}
