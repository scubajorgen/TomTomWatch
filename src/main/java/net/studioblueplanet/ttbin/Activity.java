/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.ttbin;

import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.settings.ConfigSettings;

import hirondelle.date4j.DateTime;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.TimeZone;


/**
 *
 * @author Jorgen
 */
public class Activity
{
    // If acitivity is paused for less than this time, the pause/activate is marked as waypoint
    private static final    long                        WAYPOINTPAUSETIME       =10;  
    public  static final    int                         FITNESSPOINTS_UNDEFINED =-1;
    
    protected               int                         summaryType;
    protected               double                      summaryDistance;
    protected               int                         summaryCalories;
    protected               int                         summaryDuration;
    protected               int                         summaryActivity;
    protected               TimeZone                    localTimeZone;
    protected               int                         timeZoneSeconds; // Difference with respect to UTC
    
    protected               int                         fitnessPointsStart;
    protected               int                         fitnessPointsEnd;
     
    protected               String                      route;
    
    private final           ArrayList<ActivitySegment>  segments;
    private final           ArrayList<ActivityRecord>   waypoints;
    private                 ActivityRecord              newRecord;
    private                 ActivitySegment             newActivitySegment;     
    
    private                 long                        timeOfPause;
    
    private                 String                      deviceName;
    
    private final           long                        waypointPauseTimeout;
    
    private final           TtbinHeader                 header;
    
    private                 float                       trackSmoothingQFactor;
    private                 boolean                     isSmoothed;
    
    /* ******************************************************************************************* *\
     * CONSTRUCTOR
    \* ******************************************************************************************* */


    /**
     * Constructor
     * @param header Header defining the ttbin file
     */
    public Activity(TtbinHeader header)
    {
        // Set fitness points to undefined
        this.fitnessPointsStart    =-1;
        this.fitnessPointsEnd      =-1;
        
        this.header                 =header;
        this.timeOfPause            =0;
        this.localTimeZone          =null;
        this.segments               =new ArrayList();    
        this.waypoints              =new ArrayList();   
        this.newRecord              =null;
        this.newActivitySegment     =null;
        this.deviceName             ="TomTom";
        
        this.route                  ="";
        
        this.isSmoothed             =false;
        this.trackSmoothingQFactor  =0.0f;
        
        this.waypointPauseTimeout=ConfigSettings.getInstance().getIntValue("waypointLogTimeout");    
    }
    
    /* ******************************************************************************************* *\
     * GETTERS & SETTERS
    \* ******************************************************************************************* */

    
    
    /**
     * Sets the name or id of the device
     * @param name The name of the device
     */
    public void setDeviceName(String name)
    {
        this.deviceName=name;
    }
    
    /**
     * Gets the device name. Default 'TomTom' unless replaced by something else
     * @return The name
     */
    public String getDeviceName()
    {
        return this.deviceName;
    }
    
    /**
     * Set the local time zone
     * @param seconds Seconds with respect to GMT
     */
    public void setTimeZoneDifference(int seconds)
    {
        String timeZoneString;
        
        this.timeZoneSeconds=seconds;
        
        if (seconds<0)
        {
            timeZoneString="GMT-"+(seconds+1800)/3600;
        }
        else
        {
            timeZoneString="GMT+"+(seconds+1800)/3600;
        }
        
        DebugLogger.info("Time Zone: "+timeZoneString+ " Difference in seconds "+seconds);
        
        localTimeZone=TimeZone.getTimeZone(timeZoneString);        

        ActivitySegment.setLocalTimeZone(localTimeZone);
        ActivityRecord.setLocalTimeZone(localTimeZone);
    }    
    
    /**
     * Return the activity name as string
     * @param activityType The activity as integer
     * @return The activity name
     */
    public static String getActivityDescription(int activityType)
    {
        String description;
        
        switch (activityType)
        {
            case TtbinFileDefinition.ACTIVITY_RUNNING:
                description="Running";
                break;
            case TtbinFileDefinition.ACTIVITY_CYCLING:
                description="Cycling";
                break;
            case TtbinFileDefinition.ACTIVITY_SWIMMING:
                description="Swimming";
                break;
            case TtbinFileDefinition.ACTIVITY_TREADMILL:
                description="Treadmill";
                break;
            case TtbinFileDefinition.ACTIVITY_FREESTYLE:
                description="Freestyle";
                break;
            case TtbinFileDefinition.ACTIVITY_GYM:
                description="Gym";
                break;
            case TtbinFileDefinition.ACTIVITY_HIKING:
                description="Hiking";
                break;
            case TtbinFileDefinition.ACTIVITY_INDOORCYCLING:
                description="IndoorCycling";
                break;
            case TtbinFileDefinition.ACTIVITY_TRAILRUNNING:
                description="TrailRunning";
                break;
            case TtbinFileDefinition.ACTIVITY_SKIING:
                description="Skiing";
                break;
            case TtbinFileDefinition.ACTIVITY_SNOWBOARDING:
                description="Snowboarding";
                break;
            default:
                description="Unknown";
                break;
        }
        return description;
    }
        
    /**
     * Return the activity description of this Activity instance
     * @return The activity description as string
     */
    public String getActivityDescription()
    {
        return getActivityDescription(this.summaryActivity);
    }
    
    
    
    
    /**
     * Returns the activity type. Use TtbinFileDefinition 
     * ACTIVITY_... definitions for identification
     * @return The activity type
     */
    public int getActivityType()
    {
        return this.summaryActivity;
    }
    
    
    
   /**
     * Returns the number of segments in this Acitivity. Segments are 
     * discerned by pressing pause/continue on the TomTom
     * @return The number of segments.
     */
    public int getNumberOfSegments()
    {
        return this.segments.size();
    }


    /**
     * Return the indicated segment number
     * @param segmentNumber The index of the segment number
     * @return The segment or null if not found.
     */
    public ActivitySegment getSegment(int segmentNumber)
    {
        ActivitySegment segment;
        
        segment=null;
        
        if (segmentNumber>=0 && segmentNumber<this.segments.size())
        {
            segment=segments.get(segmentNumber);
        }
        
        return segment;
    }
    
    /**
     * Returns the start datetime
     * @return The datetime as local time
     */
    public DateTime getStartDateTime()
    {
        int         time;
        DateTime    dateTime;
        
        // Time in seconds. Calculate UTC time
        time=header.startTime-this.timeZoneSeconds;
        dateTime=DateTime.forInstant((long)(time)*1000L, localTimeZone);
               
        return dateTime;
    }
    
    
    /**
     * Get the array list of records associated with indicated segment.
     * @param segmentId Index of the segment
     * @return The array list of reccords, or null if none available
     */
    public ArrayList<ActivityRecord> getRecords(int segmentId)
    {
        return this.segments.get(segmentId).getRecords();
    }
    
    
    public ArrayList<ActivityRecord> getWaypoints()
    {
        return this.waypoints;
    }

    /**
     * Get the fitness points earned with this activity
     * @return The fitness points or FITNESSPOINTS_UNDEFINED=-1 if not defined
     */
    public int getFitnessPoints()
    {
        int points;
        
        if ((fitnessPointsEnd>=0) && (fitnessPointsStart>=0))
        {
            points=fitnessPointsEnd-fitnessPointsStart;
        }
        else
        {
            points=FITNESSPOINTS_UNDEFINED;
        }
        return points;
    }
            
    /**
     * Returns the name of the planned route that was followed
     * @return The route name as String, or "" if no route followed
     */
    public String getRouteName()
    {
        return this.route;
    }
    
    
    /* ******************************************************************************************* *\
     * TTBIN FILE RECORD PARSING
    \* ******************************************************************************************* */

    /**
     * Parse the GPS record
     * @param recordData The record data
     */
    private void parseRecordGps(byte[] recordData)
    {
        int         lat;            // lat *1e7
        int         lon;            // lon *1e7
        int         heading;        // heading degrees * 100, N = 0, E = 9000
        int         speed;          // speed in cm/s
        int         calories;       //
        int         instantSpeed;   // m/s
        int         cumDistance;    // m
        int         cycles;         // running = steps/sec, cycling = crank rpm
        long        timestamp;      // Sattelite timestamp UTC
        double[]    filtered;
        double      latF;
        double      lonF;
        
        // If a gps record is encountered, this marks the start of a new record
        newRecord   =new ActivityRecordGps();
        this.newActivitySegment.addRecord(newRecord);
        
        lat         =ToolBox.readInt(recordData,  1, 4, true);
        lon         =ToolBox.readInt(recordData,  5, 4, true);
        heading     =ToolBox.readUnsignedInt(recordData,  9, 2, true);
        speed       =ToolBox.readUnsignedInt(recordData, 11, 2, true);
        timestamp   =ToolBox.readUnsignedLong(recordData, 13, 4, true);
        calories    =ToolBox.readUnsignedInt(recordData, 17, 2, true);
        instantSpeed=ToolBox.readUnsignedInt(recordData, 19, 4, true); // float
        cumDistance =ToolBox.readUnsignedInt(recordData, 23, 4, true); // float
        cycles      =ToolBox.readUnsignedInt(recordData, 27, 1, true);

        latF=(double)lat/1.0E7;
        lonF=(double)lon/1.0E7;

        ((ActivityRecordGps)newRecord).setUtcTime(timestamp);
        ((ActivityRecordGps)newRecord).setCoordinate(latF, lonF);
        ((ActivityRecordGps)newRecord).setHeading((double)heading/100.0);
        ((ActivityRecordGps)newRecord).setSpeed((double)speed/100.0);
        ((ActivityRecordGps)newRecord).setCalories(calories);
        ((ActivityRecordGps)newRecord).setInstantaneousSpeed(Float.intBitsToFloat(instantSpeed));
        ((ActivityRecordGps)newRecord).setDistance(Float.intBitsToFloat(cumDistance));
        ((ActivityRecordGps)newRecord).setCycles(cycles);

    }
    
    /**
     * Parse the heart rate data
     * @param recordData 
     */
    private void parseRecordHeartRate(byte[] recordData)
    {
        int         timeStamp;      // Timestamp (local time)
        int         heartRate;      // Heart rate in bpm
        int         reserved;       // Unknown

        heartRate=ToolBox.readUnsignedInt(recordData,  1, 1, true);
        reserved =ToolBox.readUnsignedInt(recordData,  2, 1, true); // 255 for external sensor, other value for internal
        timeStamp=ToolBox.readUnsignedInt(recordData,  3, 4, true);

        if (newRecord!=null)
        {
            // Funny enough the heart rate record uses the local time as displayed on the watch
            // So subtract the timeZoneSeconds to obtain the UTC time
            ((ActivityRecordGps)newRecord).setHeartRate(timeStamp-this.timeZoneSeconds, heartRate);
        }
        else
        {
            DebugLogger.info("Skipping heart rate record");
        }
    }
    
    /**
     * Parse the elevation data
     * @param recordData The data representing the record, starting with the tag
     */
    private void parseRecordElevation(byte[] recordData)
    {
        int status;
        int elevation1;
        int elevation2;
        int cumAscend;
        int cumDecend;
        int unknown;
        
        status          =ToolBox.readUnsignedInt(recordData,  1, 1, true);
        elevation1      =ToolBox.readInt(recordData,  2, 2, true);
        if (elevation1==32767)
        {
            elevation1=ActivityRecord.INVALID;
        }
        
        elevation2      =ToolBox.readInt(recordData,  4, 2, true);        
        if (elevation2==32767)
        {
            elevation2=ActivityRecord.INVALID;
        }

        cumAscend       =ToolBox.readUnsignedInt(recordData,  6, 2, true);        
        cumDecend       =ToolBox.readUnsignedInt(recordData,  8, 2, true);        
        unknown         =ToolBox.readInt(recordData, 10, 2, true);      
        
        ((ActivityRecordGps)newRecord).setElevation1(elevation1);
        ((ActivityRecordGps)newRecord).setElevation2(elevation2);
        ((ActivityRecordGps)newRecord).setCumulativeAscend(cumAscend);
        ((ActivityRecordGps)newRecord).setCumulativeDecend(cumDecend);
        ((ActivityRecordGps)newRecord).setElevationStatus(status);
((ActivityRecordGps)newRecord).setElevationStatus(unknown);
        
        // TO DO
        
    }
    
    /**
     * Parse file summary record
     * @param recordData Record data
     */
    private void parseRecordSummary(byte[] recordData)
    {
        int     activity;
        float   distance;
        int     duration;
        int     calories;
        
        activity    =ToolBox.readUnsignedInt(recordData,  1, 1, true);
        distance    =Float.intBitsToFloat(ToolBox.readUnsignedInt(recordData,  2, 4, true));
        duration    =ToolBox.readUnsignedInt(recordData,  6, 4, true);
        calories    =ToolBox.readUnsignedInt(recordData,10, 2, true);
        
        this.summaryType        =activity;
        this.summaryDistance    =distance;
        this.summaryDuration    =duration;
        this.summaryCalories    =calories;
    }
    
    /**
     * Parse the status record
     * @param recordData Data representing the status
     */
    private void parseRecordStatus(byte[] recordData)
    {
        int         status;
        int         activity;
        int         timeStamp;
        boolean     startNewSegment;
        DateTime    dateTime;
    
        status      =ToolBox.readUnsignedInt(recordData,  1, 1, true);
        activity    =ToolBox.readUnsignedInt(recordData,  2, 1, true);
        timeStamp   =ToolBox.readUnsignedInt(recordData,  3, 4, true); // local time

        this.summaryActivity=activity;
     
        // NOTE: the TomTom Sports ttbin file contains the local time stamp.
        //       the ttwatch ttbin file contains the UTC timestamp!!
        
        // Pass the epoch timestamp as UTC and create a Local Time Zone datetime
        dateTime=DateTime.forInstant((long)(timeStamp-this.timeZoneSeconds)*1000, localTimeZone);
        
        switch(status)
        {
            case TtbinFileDefinition.STATUS_READY:
                DebugLogger.info("Status: Ready   "+dateTime.format("YYYY-MM-DD hh:mm:ss"));
                break;
            case TtbinFileDefinition.STATUS_ACTIVE:
                DebugLogger.info("Status: Active  "+dateTime.format("YYYY-MM-DD hh:mm:ss"));
                break;
            case TtbinFileDefinition.STATUS_PAUSED:
                DebugLogger.info("Status: Paused  "+dateTime.format("YYYY-MM-DD hh:mm:ss"));
                break;
            case TtbinFileDefinition.STATUS_STOPPED:
                DebugLogger.info("Status: Stopped "+dateTime.format("YYYY-MM-DD hh:mm:ss"));
                break;
            default:
                DebugLogger.info("Status: unknown "+dateTime.format("YYYY-MM-DD hh:mm:ss"));
                break;
        }
        DebugLogger.info("Activity:"+this.getActivityDescription());
        
        // When the device goes to status STATUS_ACTIVE, start a new segment
        if (status==TtbinFileDefinition.STATUS_ACTIVE)
        {
            startNewSegment=false;
            if (timeOfPause!=0)
            {
                // when pause/resume is pressed in succession, it is used to mark a waypoint
                if ((timeStamp-timeOfPause)*1000<=this.waypointPauseTimeout)
                {
                    // add the most recent record as waypoint
                    DebugLogger.info("Waypoint added");
                    waypoints.add(newRecord);
                }
                else
                {
                    startNewSegment=true;
                }
            }
            else
            {
                startNewSegment=true;
            }
            if (startNewSegment)
            {
               newActivitySegment=new ActivitySegment();
               segments.add(newActivitySegment);
               newActivitySegment.setStartTime(timeStamp);                    
            }

        }
        
        // When STATUS_PAUSED, set the end time of the segment
        if (status==TtbinFileDefinition.STATUS_PAUSED)
        {
            newActivitySegment.setEndTime(timeStamp);
            timeOfPause=timeStamp;        
        }
        
    }
    
    /**
     * Parse record with tag 23. Apparently it contains extended GPS information
     * like EHPE and EVPE, and probably smoothing matrix values???
     * This record always follows a GPS record on the TomTom Adventurer
     * @param recordData The record data 
     */
    private void parseRecordPrecision(byte[] recordData)
    {
        int                 evpe;
        int                 ehpe;
        int                 hdop;
        
        // Sensor data? GPS Erro signal? Kalman matrices?
        //  0 1 byte
        //  1 uint16   EVPE in cm? sensor 1 or GPS error in cm??
        //  3 uint16   EHPE in cm? filtered (?) sensor 1. GPS signal strength?
        //  5 uint8    HDOP (https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation)#Meaning_of_DOP_Values)
        //  6 uint8[4] sensors 2-5?? 
        // 10 uint8[4] sensors 6-10??
        // 14 uint8[4] sensors 2-5 normalized [0, 1]??
        // 18 uint8    ? =4??
        // 19 uint8    ? =2??

        evpe=ToolBox.readUnsignedInt(recordData,  1, 2, true);
        ehpe=ToolBox.readUnsignedInt(recordData,  3, 2, true);
        hdop=ToolBox.readUnsignedInt(recordData,  5, 1, true);
        ((ActivityRecordGps)newRecord).setPrecision(ehpe, evpe, hdop);
/*
        ((ActivityRecordGps)newRecord).unknownInt3=ToolBox.readUnsignedInt(recordData,  6, 1, true);
        ((ActivityRecordGps)newRecord).unknownInt4=ToolBox.readUnsignedInt(recordData,  7, 1, true);
        ((ActivityRecordGps)newRecord).unknownInt5=ToolBox.readUnsignedInt(recordData,  8, 1, true);
        ((ActivityRecordGps)newRecord).unknownInt6=ToolBox.readUnsignedInt(recordData,  9, 1, true);
        
        int i;
        i=5;
        while (i<20)
        {
            System.out.print(ToolBox.readUnsignedInt(recordData,  i, 1, true)+",");
            i++;
        }
        System.out.println("");
*/        
    }

    
    /**
     * Parse activity point record
     * @param recordData Record data
     */
    private void parseRecordActivityPoints(byte[] recordData)
    {
        int         timeStamp;
        int         points1;
        int         points2;
        
        timeStamp   =ToolBox.readUnsignedInt(recordData,  1, 4, true);
        
        // Both values seem to be the same...
        points1     =ToolBox.readUnsignedInt(recordData,  5, 2, true);
        points2     =ToolBox.readUnsignedInt(recordData,  7, 2, true);


        // Get the first value
        if (fitnessPointsStart<0)
        {
            fitnessPointsStart=points1;
        }

        // Remember the latest value
        fitnessPointsEnd=points1;


        // If the activity has been Ready and Active, log the points
        // to the record
        if (newRecord!=null)
        {
            // Timestamp is UTC, use points1 (points1 and points2 seem to be identical)
            ((ActivityRecordGps)newRecord).setFitnessPoints(timeStamp, points1);
        }
        else
        {
            DebugLogger.info("Skipping fitness points record");
        }  
        
//        DebugLogger.info(DateTime.forInstant((long)timeStamp*1000L, TimeZone.getDefault()).format("YYYY-MM-DD hh:mm:ss")+" "+points1+" "+points2);
    }

    /**
     * Parse heart rate recovery record. The record contains the 
     * heart reate recovery (reduction in bpm/min) and associated score
     * @param recordData Record data
     */
    private void parseRecordHeartRateRecovery(byte[] recordData)
    {
        int         timeStamp;
        int         score;
        int         hrRecovery;
        
        
        // Recovery score: no=0, poor=1, good=2, fair=3, excellent=4
        score       =ToolBox.readInt(recordData,  1, 4, true);

        // Recovery of heartrate, as reduction in bpm during the 1st minute
        // after the excercise
        hrRecovery  =ToolBox.readInt(recordData,  5, 4, true);

        newActivitySegment.setHeartRateRecovery(hrRecovery, score);

        DebugLogger.info("Heart rate recovery: "+hrRecovery+" bpm/min, score: "+score);
    }
    
    
    /**
     * Parse movement state record. This record contains 1 byte that seems
     * to coincide with the momevement: 0 - standing still, 1 - reduced speed,
     * 2 - moving
     * @param recordData Record data
     */
    private void parseRecordMovement(byte[] recordData)
    {
        int         movementState;
        
        // Movement state.
        movementState       =ToolBox.readInt(recordData,  1, 1, true);
        ((ActivityRecordGps)newRecord).setMovementState(movementState);
        
    }   
    
    /**
     * Parse movement state record. This record contains 1 byte that seems
     * to coincide with the momevement: 0 - standing still, 1 - reduced speed,
     * 2 - moving
     * @param recordData Record data
     */
    private void parseRouteDescription(byte[] recordData)
    {
        
        // The route name
        this.route=ToolBox.readString(recordData,  21, 80);
      
    }   
    
    

    /**
     * Parse the record data and create a record or add to a record.
     * Based on the tag defining the record, the adequate sub function is called
     * @param recordData Data representing the record
     */
    public void parseRecord(byte[] recordData)
    {
        byte tag;
        
        tag=recordData[0];
//System.out.println(String.format("tag 0x%02x: unknown, length %d", tag, this.header.getLength(tag)));        
      
        switch(tag)
        {
            case TtbinFileDefinition.TAG_SUMMARY:
                parseRecordSummary(recordData);
                break;
            case TtbinFileDefinition.TAG_STATUS:
                parseRecordStatus(recordData);
                break;
            
            case TtbinFileDefinition.TAG_GPS:
                parseRecordGps(recordData);
                break;
            case TtbinFileDefinition.TAG_ELEVATION:
                parseRecordElevation(recordData);
                break;
            case TtbinFileDefinition.TAG_HEART_RATE:
                parseRecordHeartRate(recordData);
                break;
            case TtbinFileDefinition.TAG_PRECISION:
                parseRecordPrecision(recordData);
//                dumpRecordData(recordData);
                break;
            case TtbinFileDefinition.TAG_MOVEMENT:
                parseRecordMovement(recordData);
                break;
            case TtbinFileDefinition.TAG_30:
//                dumpRecordData(recordData);
                break;
            case TtbinFileDefinition.TAG_48:      // 8 or 14 bytes after tag
//                dumpRecordData(recordData);
                break;
            case TtbinFileDefinition.TAG_49:      // 4 bytes after tag
//                dumpRecordData(recordData);
                break;
            case TtbinFileDefinition.TAG_FITNESSPOINTS:
                parseRecordActivityPoints(recordData);
                break;
            case TtbinFileDefinition.TAG_4B:      // 4 bytes after tag
//                dumpRecordData(recordData);
                break;
            case TtbinFileDefinition.TAG_HEART_RATE_RECOVERY:
                parseRecordHeartRateRecovery(recordData);
                break;
            case TtbinFileDefinition.TAG_ROUTEDESCRIPTION:
                parseRouteDescription(recordData);
                break;
            default:
//DebugLogger.info(String.format("tag 0x%02x: unknown, length %d", tag, this.header.getLength(tag)));        
                break;
                    
        }
    }
    
    
    /**
     * Dump the record data as hexadecimals
     * @param recordData The record data. 
     */
    public void dumpRecordData(byte[] recordData)
    {
        String data;
        int i;
        
        data="";
        i=0;
        while (i<recordData.length)
        {
            data+=String.format("%02x ", recordData[i]);
//            data+=String.format("%03d, ", recordData[i]);
            i++;
        }
        DebugLogger.info(data);
//        System.out.println(data);
    }  
    
    
    /**
     * Dump the activity to csv file for analysis purposes
     */
    public void dumpActivityCsv()
    {
        File            csvFile;
        FileWriter      fileWriter;
        BufferedWriter  writer;
        int             i;
        
        try
        {
            csvFile=new File ("dump.csv");
            fileWriter=new FileWriter(csvFile);
            writer=new BufferedWriter(fileWriter);
            
            writer.write("Summary");
            writer.write("Type, "    +this.summaryType    +"\n");
            writer.write("Distance, "+this.summaryDistance+"\n");
            writer.write("Duration, "+this.summaryDuration+"\n");
            writer.write("Calories, "+this.summaryCalories+"\n");
            
            i=0;
            while (i<this.segments.size())
            {
                // Dump csv header for first record only
                if (i==0)
                {
                    segments.get(i).dumpSegmentCsv(writer, true);
                }
                else
                {
                    segments.get(i).dumpSegmentCsv(writer, false);
                }
                i++;
            }

            writer.close();
            fileWriter.close();
        }
        catch (Exception e)
        {
            DebugLogger.error("Error writing to file");
        }
        
    }
    
    
    /* ******************************************************************************************* *\
     * TRACK SMOOTHING
    \* ******************************************************************************************* */


    /**
     * This method smoothes the track, by applying a Kalman filter
     * @param trackSmoothingQFactor The factor influencing the smoothing.
     *                              1.0 - high amount to 10.0 - low
     */
    public void smoothTrack(float trackSmoothingQFactor)
    {
        TrackSmoother              smoother;
        
        smoother=TrackSmoother.getInstance();
        smoother.smoothTrack(segments, trackSmoothingQFactor);
        isSmoothed=true;
        this.trackSmoothingQFactor=trackSmoothingQFactor;
    }


    /**
     * Returns the track smoothing Q factor that is used for the Kalman 
     * filter
     * @return The factor in m/s, or 0.0 if no smoothing applied
     */
    public float getTrackSmoothingQFactor()
    {
        return this.trackSmoothingQFactor;
    }
    
    /**
     * This method resets the smoothing
     */
    public void resetSmoothing()
    {
        TrackSmoother              smoother;
        
        smoother=TrackSmoother.getInstance();
        smoother.resetSmoothing(segments);
        isSmoothed=false;
        this.trackSmoothingQFactor=0.0f;
    }
    
    
    
    /**
     * Returns whether the track has been smoothed
     * @return True if smoothed, false if not.
     */
    public boolean isSmoothed()
    {
        return this.isSmoothed;
    }
    
    
}
