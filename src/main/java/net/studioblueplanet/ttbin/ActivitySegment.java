/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.ttbin;


import java.util.ArrayList;
import hirondelle.date4j.DateTime;
import java.util.TimeZone;
import java.io.Writer;
import java.io.IOException;

/**
 *
 * @author Jorgen
 */
public class ActivitySegment
{
    private ArrayList<ActivityRecord>   records;
    private DateTime                    startTime;
    private DateTime                    endTime;
    private static TimeZone             localTimeZone;
    
    
    /**
     * Constructor
     */
    public ActivitySegment()
    {
        records=new ArrayList();
    }

    /**
     * Add a record to the segment
     * @param record The record to add
     */
    public void addRecord(ActivityRecord record)
    {
        records.add(record);
    }
    
    /** 
     * Return the number of records in this segment
     * @return The number of records
     */
    public int numberOfRecords()
    {
        return records.size();
    }
    
    /**
     * Get the record at given index
     * @param index The index of the record to retrieve
     * @return The record
     */
    public ActivityRecord getRecord(int index)
    {
        return records.get(index);
    }
    
    /**
     * Returns the array of records in this activity segments
     * @return ArrayList with records
     */
    public ArrayList<ActivityRecord> getRecords()
    {
        return this.records;
    }
    
    /**
     * Set the time zone of the local time to use
     * @param timeZone The time zone corresponding to the local time
     */
    public static void setLocalTimeZone(TimeZone timeZone)
    {
        localTimeZone=timeZone;
    }
    
    /**
     * Set start time of this semgent as local time
     * @param seconds Local Time seconds in the epoch era
     */
    public void setStartTime(int seconds)
    {
        startTime=DateTime.forInstant((long)seconds*1000, localTimeZone);
    }
    
    /**
     * Get the start time
     * @return The start time
     */
    public DateTime getStartTime()
    {
        return this.startTime;
    }
    
    /**
     * Set the end time of this segment as local time
     * @param seconds The epoch seconds
     */
    public void setEndTime(int seconds)
    {
        endTime=DateTime.forInstant((long)seconds*1000, localTimeZone);
    }
    
    /**
     * Get the end time
     * @return The end time
     */
    public DateTime getEndTime()
    {
        return this.endTime;
    }
    
    
    /**
     * Dump the records in this segment as CSV. Starts the segment with 
     * an empty line
     * @param writer Writer to dump to
     * @param withHeader Indicates whether to dump the CSV header or not
     * @throws IOException Thrown when an error occurs during writing
     */
    public void dumpSegmentCsv(Writer writer, boolean withHeader) throws IOException
    {
        int i;

        writer.write("\n");
        if (withHeader)
        {
            records.get(0).dumpRecordCsvHeader(writer);
        }
        i=0;
        while (i<this.records.size())
        {
            records.get(i).dumpRecordCsv(writer);
            i++;
        }
    }
    
    
    
}
