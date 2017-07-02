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


    
    /**
     * Constructor. Initialises all on 'not initialised' (INVALID)
     */
    public ActivityRecord()
    {
        this.dateTime           =null;
        this.utcTimeZone        =TimeZone.getTimeZone("UTC");
    }

    /**
     * Set the local time zone
     * @param timeZoneSeconds Seconds with respect to GMT
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

    /*############################################################################################*\
     * DEBUGGING
    \*############################################################################################*/    
    
    /**
     * Dump the header containing all record fields
     * @param writer Writer to dump to
     * @throws IOException 
     */
    public void dumpRecordCsvHeader(Writer writer) throws IOException
    {
        writer.write("dateTime");        
    }
    
   /**
     * Dump all the record fields of this record
     * @param writer Writer to dump to
     * @throws IOException 
     */
    public void dumpRecordCsv(Writer writer) throws IOException
    {
        writer.write(this.dateTime.format("YYYY-MM-DD hh:mm:ss"));
    }
}
