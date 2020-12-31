/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.settings.ConfigSettings;
import net.studioblueplanet.generics.PolyLineEncoder;

import hirondelle.date4j.DateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.util.TimeZone;

import org.json.JSONObject;
import org.json.JSONArray;

/**
 * This class represents a TomTom activity, like a run or a cycle trip
 * @author Jorgen
 */
public class Activity
{
    class Elevation
    {
        int index;
        int originalIndex;
        double lat;
        double lon;
        double elevation;
    }
    
    // If acitivity is paused for less than this time, the pause/activate is marked as waypoint
    private static final    long                        WAYPOINTPAUSETIME           =10;  
    public  static final    int                         FITNESSPOINTS_UNDEFINED     =-1;
    private                 double                      minDistanceBetweenRecords   =0.000;
    
    protected               int                         summaryType;
    protected               double                      summaryDistance;
    protected               int                         summaryCalories;
    protected               int                         summaryDuration;
    protected               int                         summaryActivity;
    protected               TimeZone                    localTimeZone;
    protected               int                         timeZoneSeconds; // Difference with respect to UTC
    
    protected               int                         batteryLevel;
    
    protected               int                         fitnessPointsStart;
    protected               int                         fitnessPointsEnd;
    protected               String                      workout;
    protected               String                      workoutDescription;
    protected               String                      workoutSteps;
    
    protected               String                      route;
    
    protected               int                         secondsToFix;   // Time it took to find satellite fix
    
    private final           List<ActivitySegment>       segments;
    private final           List<ActivityRecord>        waypoints;
    private                 ActivityRecord              newRecord;
    private                 ActivitySegment             newActivitySegment;     
    
    private                 long                        timeOfPause;
    
    private                 String                      deviceName;
    private                 String                      deviceSerial;
    
    private final           long                        waypointPauseTimeout;
    
    private final           TtbinHeader                 header;
    
    private                 float                       trackSmoothingQFactor;
    private                 boolean                     isSmoothed;
    
    private                 ArrayList<Elevation>        elevations;
    
    private                 double                      prevLat=0.0;
    private                 double                      prevLon=0.0;
    private                 int                         skippedPoints=0;
    private                 int                         validPoints=0;
    
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
        this.segments               =new ArrayList<>();    
        this.waypoints              =new ArrayList<>();   
        this.newRecord              =null;
        this.newActivitySegment     =null;
        this.deviceName             ="TomTom";
        this.deviceSerial           ="Unknown";
        
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
     * Sets the serial number of the device
     * @param serial The name of the device
     */
    public void setDeviceSerialNumber(String serial)
    {
        this.deviceSerial=serial;
    }
    
    /**
     * Gets the device serial number. Default 'Unknown' unless replaced by something else
     * @return The serial number
     */
    public String getDeviceSerialNumber()
    {
        return this.deviceSerial;
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
     * Returns the date time of the first active record
     * @return Date time of first active record
     */
    public DateTime getFirstActiveRecordTime()
    {
        long                time;
        long                correction;
        TimeZone            zone;
        DateTime            dateTime;
        DateTime            localDateTime;
        ActivitySegment     segment;
        ActivityRecord      record;
        ActivityRecordGps   gpsRecord;
        
        localDateTime=null;

        if (segments.size()>0)
        {
            segment=segments.get(0);
            if (segment.numberOfRecords()>0)
            {
                record          =segment.getRecord(0);
                dateTime        =record.getDateTime();
                time            =dateTime.getMilliseconds(TimeZone.getTimeZone("UTC"));
                // Correct if the timeZoneSeconds is not a multiple of 1800 (=1/2 hour)
                correction      =this.timeZoneSeconds%1800;
                if (correction>900)
                {
                    correction=correction-1800;
                }
                time+=correction*1000;
                localDateTime   =DateTime.forInstant(time, localTimeZone);
               
            }
        }
        return localDateTime;
    }
    
    
    /**
     * Get the array list of records associated with indicated segment.
     * @param segmentId Index of the segment
     * @return The array list of reccords, or null if none available
     */
    public List<ActivityRecord> getRecords(int segmentId)
    {
        return this.segments.get(segmentId).getRecords();
    }
    
    
    public List<ActivityRecord> getWaypoints()
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
     * Get the workout that was performed
     * @return The workout or null if none
     */
    public String getWorkout()
    {
        return workout;
    }

    /**
     * Get the description of the workout that was performed
     * @return The description or null if none
     */
    public String getWorkoutDescription()
    {
        return workoutDescription;
    }

    /**
     * Get the list of workout steps 
     * @return The steps or null if none
     */
    public String getWorkoutSteps()
    {
        return workoutSteps;
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
        
        
        lat         =ToolBox.readInt(recordData,  1, 4, true);
        lon         =ToolBox.readInt(recordData,  5, 4, true);
        heading     =ToolBox.readUnsignedInt(recordData,  9, 2, true);
        speed       =ToolBox.readUnsignedInt(recordData, 11, 2, true);
        timestamp   =ToolBox.readUnsignedLong(recordData, 13, 4, true);
        calories    =ToolBox.readUnsignedInt(recordData, 17, 2, true);
        instantSpeed=ToolBox.readUnsignedInt(recordData, 19, 4, true); // float
        cumDistance =ToolBox.readUnsignedInt(recordData, 23, 4, true); // float
        cycles      =ToolBox.readUnsignedInt(recordData, 27, 1, true);

        // Sometimes a weird record is encountered. Skip it
        if (timestamp!=4294967295L)
        {        
            latF=(double)lat/1.0E7;
            lonF=(double)lon/1.0E7;
            validPoints++;
            
            if (ToolBox.distance(prevLat, prevLon, latF, lonF)>=minDistanceBetweenRecords)
            {
                // If a gps record is encountered, this marks the start of a new record
                newRecord   =new ActivityRecordGps();
                ((ActivityRecordGps)newRecord).setUtcTime(timestamp);
                ((ActivityRecordGps)newRecord).setCoordinate(latF, lonF);
                ((ActivityRecordGps)newRecord).setHeading((double)heading/100.0);
                ((ActivityRecordGps)newRecord).setSpeed((double)speed/100.0);
                ((ActivityRecordGps)newRecord).setCalories(calories);
                ((ActivityRecordGps)newRecord).setInstantaneousSpeed(Float.intBitsToFloat(instantSpeed));
                ((ActivityRecordGps)newRecord).setDistance(Float.intBitsToFloat(cumDistance));
                ((ActivityRecordGps)newRecord).setCycles(cycles);

                // Sets the current value of the batterylevel
                newRecord.setBatteryLevel(this.batteryLevel);
                this.newActivitySegment.addRecord(newRecord);
                prevLat=latF;
                prevLon=lonF;
            }
            else
            {
                skippedPoints++;
            }
        }
        else
        {
            DebugLogger.info("Invalid record");
        }
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
        int cumDescend;
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
        cumDescend       =ToolBox.readUnsignedInt(recordData,  8, 2, true);        

        if (cumAscend==65535)
        {
            cumAscend=ActivityRecord.INVALID;
        }

        if (cumDescend==65535)
        {
            cumDescend=ActivityRecord.INVALID;
        }

        
        
        unknown         =ToolBox.readInt(recordData, 10, 2, true);      
        
        ((ActivityRecordGps)newRecord).setElevation1(elevation1);
        ((ActivityRecordGps)newRecord).setElevation2(elevation2);
        ((ActivityRecordGps)newRecord).setCumulativeAscend(cumAscend);
        ((ActivityRecordGps)newRecord).setCumulativeDecend(cumDescend);
        ((ActivityRecordGps)newRecord).setElevationStatus(status);
        
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

        ((ActivityRecordGps)newRecord).unknownInt1=ToolBox.readUnsignedInt(recordData,  6, 1, true);
        ((ActivityRecordGps)newRecord).unknownInt2=ToolBox.readUnsignedInt(recordData,  7, 1, true);
        ((ActivityRecordGps)newRecord).unknownInt3=ToolBox.readUnsignedInt(recordData,  8, 1, true);
        ((ActivityRecordGps)newRecord).unknownInt4=ToolBox.readUnsignedInt(recordData,  9, 1, true);
        ((ActivityRecordGps)newRecord).unknownInt5=ToolBox.readUnsignedInt(recordData, 10, 1, true);
        ((ActivityRecordGps)newRecord).unknownInt6=ToolBox.readUnsignedInt(recordData, 11, 1, true);
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
     * Parse battery record
     * @param recordData The record data
     */
    private void parseRecordBattery(byte[] recordData)
    {
        int unknown1;   // 127
        int unknown2;   // 4, 5, 6,
        int unknown3;   // 0

        this.batteryLevel   =ToolBox.readUnsignedInt(recordData,  1, 1, true);
        unknown1            =ToolBox.readUnsignedInt(recordData,  2, 1, true);
        unknown2            =ToolBox.readUnsignedInt(recordData,  3, 1, true);
        unknown3            =ToolBox.readUnsignedInt(recordData,  4, 1, true);
    }   

    /**
     * Parse the record indicating the period to get ready
     * @param recordData 
     */
    private void parseRecordTimeToSatelliteFix(byte[] recordData)
    {
        secondsToFix=ToolBox.readUnsignedInt(recordData,  1, 2, true);
        DebugLogger.info("Time to get a fix: "+secondsToFix+" s");
    }
    
   
    /**
     * 
     * @param recordData 
     */
    private void parseRecordWorkout(byte[] recordData)
    {
        int     index;
        int     length;
        
        // TO DO: The implementation is not correct
        // the section from byte 24 contains an array of descriptions starting
        // with 6 bytes header:
        // 0x22 ? 0x08 ID 0x12 length
        // followed by the string of size length
        // 
        // 0-23: Workout?
        // last part: workout steps?
        //
        // Usually description 0 is the workout name, 1 the description
        
        try
        {
            index=24;
            index+=5;
            length=recordData[index];
            index++;
            workout=new String(recordData, index, length, "UTF-8");
            index+=length;

            index+=5;
            length=recordData[index];
            index++;
            workoutDescription=new String(recordData, index, length, "UTF-8");
            index+=length;
        }
        catch (UnsupportedEncodingException e)
        {
            DebugLogger.error("Error converting bytes to UTF-8 string");
        }
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
//System.out.println(String.format("tag 0x%02x: length %d", tag, this.header.getLength(tag)));        
//System.out.println(tag);     
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
                break;
            case TtbinFileDefinition.TAG_MOVEMENT:
//                parseRecordMovement(recordData);
                break;
            case TtbinFileDefinition.TAG_TIMETOFIX:  
                parseRecordTimeToSatelliteFix(recordData);
                break;
            case TtbinFileDefinition.TAG_37:
                break;
            case TtbinFileDefinition.TAG_48:      // 8 or 14 bytes after tag
                break;
            case TtbinFileDefinition.TAG_BATTERY:
                  parseRecordBattery(recordData);
                break;
            case TtbinFileDefinition.TAG_FITNESSPOINTS:
                parseRecordActivityPoints(recordData);
                break;
            case TtbinFileDefinition.TAG_4B:      // 27, 32, 33, 82, 88.... bytes incl tag
                break;
            case TtbinFileDefinition.TAG_WORKOUT:
                parseRecordWorkout(recordData);
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
    
    /* ******************************************************************************************* *\
     * DEBUGGING METHODS
    \* ******************************************************************************************* */
    /**
     * Dump the record data as hexadecimals
     * @param recordData The record data. 
     * @param asText False: print hex, True: print ascii
     * @param maxSize Truncate after this number of characters
     */
    public void dumpRecordData(byte[] recordData, boolean asText, int maxSize)
    {
        String data;
        int i;
        
        data="";
        i=0;
        while (i<Math.min(recordData.length, maxSize))
        {
            if (asText)
            {
                if (recordData[i]>32 && recordData[i]<128)
                {
                    data+=String.format("%c  ", recordData[i]);
                }
                else
                {
                    data+="-  ";
                }
            }
            else
            {
                data+=String.format("%02x ", recordData[i]);
            }
            i++;
        }
        DebugLogger.info(data);
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
     * HEIGHT FROM SERVICE
    \* ******************************************************************************************* */
    
    /**
     * This method checks if the activity has GPS reocord with height values.
     * Currently only GPS records of the Adventurer contain height values.
     * @return True if height values encountered, false if not.
     */
    public boolean hasHeightValues()
    {
        boolean found;
        
        found=false;
        
        Iterator<ActivitySegment>   itSegment;
        ActivitySegment             segment;
        List<ActivityRecord>        points;
        Iterator<ActivityRecord>    itPoint;
        ActivityRecord              point;
        ActivityRecordGps           pointGps;
        

        itSegment=segments.iterator();

        while (itSegment.hasNext() && !found)
        {
            segment             =itSegment.next();
            points              =segment.getRecords();
            
            itPoint             =points.iterator();
            while (itPoint.hasNext() && !found)
            {
                point=itPoint.next();
                
                if (point instanceof ActivityRecordGps)
                {
                    pointGps=(ActivityRecordGps)point;
                    if (pointGps.hasHeightValue())
                    {
                        found=true;
                    }
                }
            }
            
        }
        
        return found;
    }
    
    /**
     * Creates the URL for the Google elevation service. The URL takes
     * as URL parameter the polyline encoded array of locations. The number
     * of points that can be passed is limited by the maximum length of the 
     * URL which is about 8192 characters. The method simply limits the 
     * number of points to a fixed amount, for which the URL stays well 
     * within the size limit.
     * @param segment Segment for which the height is requested
     * @return The URL as String
     */
    private String buildGoogleHeightServiceUrl(ActivitySegment segment)
    {
        List<ActivityRecord>        points;
        ActivityRecordGps           point;
        int                         numberOfPoints;
        int                         numberOfRequestPoints;
        float                       indexIncrement;
        int                         maxPoints;
        int                         i;
        float                       indexF;
        int                         indexMin;
        int                         indexMax;
        int                         index;
        String                      url;
        PolyLineEncoder             encoder;
        Elevation                   elevationValue;

        encoder =PolyLineEncoder.getInstance();
        encoder.resetPointEncoding();
        
        url                 ="https://maps.googleapis.com/maps/api/elevation/json?locations=enc:";

        points              =segment.getRecords();
        numberOfPoints      =points.size();

        // Limit the number of points to request from the Google service
        // Calculate the increment, making sure to incorporate the first
        // and last point in the segment.
        if (numberOfPoints>500)
        {
            numberOfRequestPoints=500;
            indexIncrement=Math.max(((float)numberOfPoints-1.0f)/(500.0f-1.0f), 1);
        }
        else
        {
            numberOfRequestPoints=numberOfPoints;
            indexIncrement=1.0f;
        }


        // Now skip through the original data and add points
        // to the Google request at regular intervals
        elevations.clear();
        i        =0;
        indexF   =0.0f;
        while (i<numberOfRequestPoints)
        {
            index=Math.round(indexF);
            point=(ActivityRecordGps)points.get(index);

            url+=encoder.encodePoint(point.getLatitude(), point.getLongitude());

            elevationValue=new Elevation();
            elevationValue.index        =i;
            elevationValue.originalIndex=index;
            elevationValue.lat          =point.getLatitude();
            elevationValue.lon          =point.getLongitude();
            elevations.add(elevationValue);
            
            indexF+=indexIncrement;
            i++;
        }

        url+="&key="+ConfigSettings.getInstance().getStringValue("heightServiceKey");

        return url;
    }
    
    public boolean processGoogleHeightServiceResults(byte[] heights)
    {
        JSONObject                  jsonObj;
        JSONArray                   results;
        JSONObject                  heightValueJson;
        Elevation                   heightValue;
        boolean                     error;
        int                         size;
        int                         i;
        
        error=false;
        
        jsonObj                 =new JSONObject(ToolBox.readString(heights, 0, heights.length));
        if (jsonObj.getString("status").equals("OK"))
        {
            results=jsonObj.getJSONArray("results");

            size=results.length();
            if (size!=elevations.size())
            {
                DebugLogger.error("Error processing Google height service results: unexpected number of heights received");
                error=true;
            }
            i=0;
            while (i<size)
            {
                heightValueJson=results.getJSONObject(i);
                
                elevations.get(i).elevation=heightValueJson.getDouble("elevation");
               
                i++;
            }

        }
        else
        {
            DebugLogger.error("Error response from height service: "+jsonObj.getString("status"));
            error=true;
        }        
        return error;
    }
    
    /**
     * Generate the elevations in the segment from the downloaded values
     * @return 
     */
    private boolean convertServiceResultsToHeights(ActivitySegment segment)
    {
        boolean error;
        int     i;
        int     responseIndex;      // Index in the Google request/response
        int     dataIndex;          // Index in the segment data
        int     previousDataIndex;
        double  elevation1;
        double  elevation2;
        double  elevation;
        
        error=false;
     
        i                   =0;
        responseIndex       =0;
        dataIndex           =0;
        previousDataIndex   =0;
        while (i<segment.numberOfRecords())
        {
            if (i==elevations.get(responseIndex).originalIndex)
            {
                elevation=elevations.get(responseIndex).elevation;
                previousDataIndex=dataIndex;
                responseIndex++;
                if (responseIndex<elevations.size())
                {
                    dataIndex=elevations.get(responseIndex).originalIndex;
                }
            }
            else
            {
                elevation1=elevations.get(responseIndex-1).elevation;
                elevation2=elevations.get(responseIndex).elevation;
                elevation=elevation1+(elevation2-elevation1)*(i-previousDataIndex)/(dataIndex-previousDataIndex);
            }
            ((ActivityRecordGps)(segment.getRecord(i))).setDerivedElevation(elevation);
            i++;
        }
        
        return error;
    }
    
    
    /**
     * This method reads the height values for the coordinates from a 
     * service on internet.
     * @return False if all went ok, true if an error occurred 
     */
    public boolean readHeigthsFromService()
    {
        boolean error;
        Iterator<ActivitySegment>   itSegment;
        ActivitySegment             segment;
        String                      url;
        byte[]                      heights;

        
        error   =false;

        elevations=new ArrayList<>();
       
        itSegment=segments.iterator();

        while (itSegment.hasNext())
        {
            segment             =itSegment.next();

            url=buildGoogleHeightServiceUrl(segment);
            
            heights=ToolBox.readBytesFromUrl(url);
            
            if (heights!=null)
            {
                error=processGoogleHeightServiceResults(heights);

                if (!error)
                {
                    error=this.convertServiceResultsToHeights(segment);
                }
                        
            }
            else
            {
                DebugLogger.error("Error requesting height values from height service");
                error=true;
            }
            
        }
        
        
        return error;
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

    /**
     * Returns the total distance of this activity
     * @return Distance in km
     */
    public double getDistance()
    {
        int     i;
        double  distance;
        
        i       =0;
        distance=0.0;
        while (i<segments.size())
        {
            distance+=segments.get(i).getDistance();
            i++;
        }
        return distance;
    }

    /* ******************************************************************************************* *\
     * TRACK COMPRESSING - DOUGLASS-PEUCKER ALGORITHM
    \* ******************************************************************************************* */
    public void compressTrack(double maxError)
    {
        Iterator<ActivitySegment> it;
        it=segments.iterator();
        while (it.hasNext())
        {
            it.next().compress(maxError);
        }
    }
}
