/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.ttbin;

import java.util.List;
import java.util.ArrayList;
import hirondelle.date4j.DateTime;
import java.util.TimeZone;
import java.io.Writer;
import java.io.IOException;

import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.generics.DPUtil;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.settings.ConfigSettings;

/**
 *
 * @author Jorgen
 */
public class ActivitySegment
{
    public static final int                 HRRECOVERY_UNDEFINED=-9999;
    private List<ActivityRecord>            records;
    private DateTime                        startTime;
    private DateTime                        endTime;
    private static TimeZone                 localTimeZone;
    private int                             heartRateRecovery;
    private int                             heartRateRecoveryScore;
    
    /**
     * Constructor
     */
    public ActivitySegment()
    {
        records                 =new ArrayList<>();
        heartRateRecovery       =HRRECOVERY_UNDEFINED;
        heartRateRecoveryScore  =-1;
    }

    /**
     * Add a record to the segment. It is only added if it differs from
     * previous record.
     * @param record The record to add
     */
    public void addRecord(ActivityRecord record)
    {
        if (records.size()>0)
        {
            if (!record.equals(records.get(records.size()-1)))
            {
                records.add(record);
            }
        }
        else
        {
            records.add(record);
        }
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
    public List<ActivityRecord> getRecords()
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
     * Sets the heart rate recovery
     * @param recovery Recovery after one minute as decrease in bpm
     * @param score Score (1-5?)
     */
    public void setHeartRateRecovery(int recovery, int score)
    {
        this.heartRateRecovery      =recovery;
        this.heartRateRecoveryScore =score;
    }
    
    /**
     * Returns the heart rate recovery at the end of the segment
     * @return The heart rate recovery as decrease of heartrate in one minute in bpm
     */
    public int getHeartRateRecovery()
    {
        return this.heartRateRecovery;
    }
    
    /**
     * Returns the score associated with the heart rate recovery at the end of the segment
     * @return 0 (no recovery) - 4 (excellent recovery)
     */
    public int getHeartRateRecoveryScore()
    {
        return this.heartRateRecoveryScore;
    }
    
    /**
     * Returns the heart reate recovery score associated with the recovery
     * @return String indicating the recovery
     */
    public String getHeartRateRecoveryScoreString()
    {
        String score;
        
        score="";
        switch (heartRateRecoveryScore)
        {
            case 0:
                score="No recovery";  
                break;
            case 1:
                score="Poor recovery";
                break;
            case 2:
                score="Fair recovery";
                break;
            case 3:
                score="Good recovery";
                break;
            case 4:
                score="Excellent recovery";
                break;
        }
        return score;
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
    
    /**
     * Returns the distance of this activity segment
     * @return Distance in km
     */
    public double getDistance()
    {
        double              distance;
        ActivityRecord      record;
        ActivityRecordGps   gpsRecord;
        ActivityRecordGps   prevGpsRecord;
        int                 i;
        
        distance        =0.0;
        gpsRecord       =null;
        prevGpsRecord   =null;
        
        i=0;
        while (i<records.size() && prevGpsRecord==null)
        {
            record=records.get(i);
            if (record instanceof ActivityRecordGps)
            {
                prevGpsRecord=(ActivityRecordGps)record;
            }
            i++;
        }
        while (i<records.size())
        {
            record=records.get(i);
            if (record instanceof ActivityRecordGps)
            {
                gpsRecord=(ActivityRecordGps)record;
                if ((gpsRecord.getLatitude()!=0.0) && (gpsRecord.getLongitude()!=0.0) &&
                     (prevGpsRecord.getLatitude()!=0.0) && (prevGpsRecord.getLongitude()!=0.0))
                {
                    distance+=ToolBox.distance(prevGpsRecord.getLatitude(), prevGpsRecord.getLongitude(),
                                               gpsRecord.getLatitude()    , gpsRecord.getLongitude());
                    prevGpsRecord=gpsRecord;
                }
            }                
            i++;
        }
        return distance;
    }
    
    /**
     * Compress segment using the Douglas-Peucker method
     */
    public void compress(double maxError)
    {
        int before;
        int after;
        if (maxError>0.0)
        {
            before=records.size();
            records=DPUtil.dpAlgorithm(records, maxError);
            after=records.size();
            DebugLogger.info("DP Compression applied. Size before "+before+", after "+after+" ("+(100*after/before)+"%)");
        }
        else
        {
            DebugLogger.error("Compression maximum error value must be larger than 0.0");
        }
    }
    
}
