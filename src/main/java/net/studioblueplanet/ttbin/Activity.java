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
    public class KalmanLatLong 
    {
        private final float MinAccuracy = 1;

        private float       Q_metres_per_second;    
        private long        TimeStamp_milliseconds;
        private double      lat;
        private double      lng;
        private float       variance; // P matrix.  Negative means object uninitialised.  NB: units irrelevant, as long as same units used throughout

        public KalmanLatLong(float Q_metres_per_second) 
        { 
            this.Q_metres_per_second = Q_metres_per_second; 
            variance = -1; 
        }
        
        public float getQFactor()
        {
            return this.Q_metres_per_second;
        }

        public long getTimeStamp() 
        { 
            return TimeStamp_milliseconds; 
        }

        public double geLat() 
        { 
            return lat; 
        }

        public double getLng() 
        { 
            return lng; 
        }

        public float getAccuracy() 
        { 
            return (float)Math.sqrt(variance); 
        }

        public void setState(double lat, double lng, float accuracy, long TimeStamp_milliseconds) 
        {
            this.lat                    =lat; 
            this.lng                    =lng; 
            variance                    = accuracy * accuracy; 
            this.TimeStamp_milliseconds =TimeStamp_milliseconds;
        }

        /// <summary>
        /// Kalman filter processing for lattitude and longitude
        /// </summary>
        /// <param name="lat_measurement_degrees">new measurement of lattidude</param>
        /// <param name="lng_measurement">new measurement of longitude</param>
        /// <param name="accuracy">measurement of 1 standard deviation error in metres</param>
        /// <param name="TimeStamp_milliseconds">time of measurement</param>
        /// <returns>new state</returns>
        public void process(double lat_measurement, double lng_measurement, float accuracy, long TimeStamp_milliseconds) 
        {
            long TimeInc_milliseconds;
            
            if (accuracy < MinAccuracy) 
            {
                accuracy = MinAccuracy;
            }

            if (variance < 0) 
            {
                // if variance < 0, object is unitialised, so initialise with current values
                this.TimeStamp_milliseconds = TimeStamp_milliseconds;
                lat                         =lat_measurement; 
                lng                         = lng_measurement; 
                variance                    = accuracy*accuracy; 
            } 
            else 
            {
                // else apply Kalman filter methodology

                TimeInc_milliseconds = TimeStamp_milliseconds - this.TimeStamp_milliseconds;
                if (TimeInc_milliseconds > 0) 
                {
                    // time has moved on, so the uncertainty in the current position increases
                    variance += (float)TimeInc_milliseconds * Q_metres_per_second * Q_metres_per_second / 1000.0f;
                    this.TimeStamp_milliseconds = TimeStamp_milliseconds;
                    // TO DO: USE VELOCITY INFORMATION HERE TO GET A BETTER ESTIMATE OF CURRENT POSITION
                }

                // Kalman gain matrix K = Covarariance * Inverse(Covariance + MeasurementVariance)
                // NB: because K is dimensionless, it doesn't matter that variance has different units to lat and lng
                float K = variance / (variance + accuracy * accuracy);
                // apply K
                lat += K * (lat_measurement - lat);
                lng += K * (lng_measurement - lng);
                // new Covarariance  matrix is (IdentityMatrix - K) * Covarariance 
                variance = (1 - K) * variance;
            }
        }
    }    
    
    
    // If acitivity is paused for less than this time, the pause/activate is marked as waypoint
    private static final    long                        WAYPOINTPAUSETIME=10;  
    
    protected               int                         summaryType;
    protected               double                      summaryDistance;
    protected               int                         summaryCalories;
    protected               int                         summaryDuration;
    protected               int                         summaryActivity;
    protected               TimeZone                    localTimeZone;
    protected               int                         timeZoneSeconds; // Difference with respect to UTC
    
    protected               int                         fitnessPointsStart;
    protected               int                         fitnessPointsEnd;
    
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
     * @return The fitness points or 0 if not defined
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
            points=0;
        }
        return points;
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
        reserved =ToolBox.readUnsignedInt(recordData,  2, 1, true);
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
        int cumElevation1;
        int cumElevation2;
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

        cumElevation1   =ToolBox.readUnsignedInt(recordData,  6, 2, true);        
        cumElevation2   =ToolBox.readUnsignedInt(recordData,  8, 2, true);        
        unknown         =ToolBox.readUnsignedInt(recordData, 10, 2, true);      
        
        ((ActivityRecordGps)newRecord).setElevation1(elevation1);
        ((ActivityRecordGps)newRecord).setElevation2(elevation2);
        ((ActivityRecordGps)newRecord).setCumulativeElevationGain1(cumElevation1);
        ((ActivityRecordGps)newRecord).setCumulativeElevationGain2(cumElevation2);
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
        
        // Sensor data? GPS Erro signal? Kalman matrices?
        //  0 1 byte
        //  1 uint16   EVPE in cm? sensor 1 or GPS error in cm??
        //  3 uint16   EHPE in cm? filtered (?) sensor 1. GPS signal strength?
        //  5 uint8    HDOP (https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation)#Meaning_of_DOP_Values)
        //  6 uint8[4] sensors 2-5
        // 10 uint8[4] sensors 6-10
        // 14 uint8[4] sensors 2-5 normalized [0, 1]
        // 18 uint8    ? =4
        // 19 uint8    ? =2

        evpe=ToolBox.readUnsignedInt(recordData,  1, 2, true);
        ehpe=ToolBox.readUnsignedInt(recordData,  3, 2, true);
        ((ActivityRecordGps)newRecord).setPrecision(ehpe, evpe);
/*
//        ((ActivityRecordGps)newRecord).unknownInt3=ToolBox.readUnsignedInt(recordData,  5, 1, true);
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
     * Parse file summary record
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
     * Parse the record data and create a record or add to a record.
     * Based on the tag defining the record, the adequate sub function is called
     * @param recordData Data representing the record
     */
    public void parseRecord(byte[] recordData)
    {
        byte tag;
        
        tag=recordData[0];
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
            case TtbinFileDefinition.TAG_PRECISION:      // tag 4 bytes 1 byte
                parseRecordPrecision(recordData);
//                dumpRecordData(recordData);
                break;
            case TtbinFileDefinition.TAG_42:      // 1 byte Cycles?
//                dumpRecordData(recordData);
                break;
            case TtbinFileDefinition.TAG_49:      // 4 bytes after tag
//                dumpRecordData(recordData);
                break;
            case TtbinFileDefinition.TAG_FITNESSPOINTS:      // 4 bytes after tag
                parseRecordActivityPoints(recordData);
                break;
            case TtbinFileDefinition.TAG_4B:      // 4 bytes after tag
//                dumpRecordData(recordData);
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
        KalmanLatLong               filter;
        ActivitySegment             segment;
        Iterator<ActivitySegment>   itSegment;
        ActivityRecordGps           gpsRecord;
        ActivityRecord              record;
        Iterator<ActivityRecord>    itRecord;
        double                      lat;
        double                      lon;
        int                         ehpe;
        long                        timeInMs;
        
        // Process the segments in the track
        itSegment=segments.iterator();
        this.trackSmoothingQFactor=trackSmoothingQFactor;
        
        while (itSegment.hasNext())
        {
            segment=itSegment.next();
            filter=new KalmanLatLong(trackSmoothingQFactor);
    
            itRecord=segment.getRecords().iterator();
            
            while (itRecord.hasNext())
            {
                record=itRecord.next();
                
                if (record instanceof ActivityRecordGps)
                {
                    gpsRecord=(ActivityRecordGps)record;

                    lat=gpsRecord.getLatitude();
                    lon=gpsRecord.getLongitude();
                    ehpe=gpsRecord.getEhpe();


                    if ((lat!=0.0) && (lon!=0.0) && (ehpe!=ActivityRecord.INVALID))
                    {
                        // The timestamp in ms. The timezone doesn't matter actually...
                        timeInMs           =record.getDateTime().getMilliseconds(TimeZone.getTimeZone("UTC"));

                        // Store the original coordinate to the raw lat/lon
                        gpsRecord.setRawCoordinate(lat, lon);
                        
                        // Do the filter.
                        filter.process((float)lat, (float)lon, (float)ehpe/100.0f, timeInMs);

                        // Get the filtered value and replace the original coordinate with it
                        lat=filter.geLat();
                        lon=filter.getLng();
                        gpsRecord.setCoordinate(lat, lon);

                    }
                }                
            }
        }
        isSmoothed=true;
        
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
        ActivitySegment             segment;
        Iterator<ActivitySegment>   itSegment;
        ActivityRecordGps           gpsRecord;
        ActivityRecord              record;
        Iterator<ActivityRecord>    itRecord;
        double                      lat;
        double                      lon;
        int                         ehpe;
        long                        timeInMs;
        
        // Process the segments in the track
        itSegment=segments.iterator();
        
        while (itSegment.hasNext())
        {
            segment=itSegment.next();
    
            itRecord=segment.getRecords().iterator();
            
            while (itRecord.hasNext())
            {
                record=itRecord.next();
                
                if (record instanceof ActivityRecordGps)
                {
                    gpsRecord=(ActivityRecordGps)record;

                    lat=gpsRecord.getRawLatitude();
                    lon=gpsRecord.getRawLongitude();
                    
                    if (lat!=ActivityRecord.INVALID && lon!=ActivityRecord.INVALID)
                    {
                        gpsRecord.setCoordinate(lat, lon);
                        gpsRecord.setRawCoordinate(ActivityRecord.INVALID, ActivityRecord.INVALID);
                    }
                }                
            }
        }
        this.trackSmoothingQFactor  =0.0f;
        isSmoothed                  =false;
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
