/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import net.studioblueplanet.logger.DebugLogger;
import hirondelle.date4j.DateTime;
import java.util.TimeZone;
import net.studioblueplanet.generics.ToolBox;


/**
 * This class reads and stores the tracked activities of the watch. This data
 * is stored in the 0x00B1nnnn files and contains steps, calories, rest heartrate, etc
 * @author Jorgen
 */
public class Tracker
{
    /**
     * This class represents a record containing the heart rate info
     */
    public class HeartRateRecord
    {
        public DateTime             dateTime;
        public int                  interval;
        public int                  heartRate;
        public int                  unknown1;
        public int                  unknown2;
    }

    class Sleep
    {
        public DateTime             goToSleepTime;
        public DateTime             wakeUpTime;
        public int                  duration;
    }
    
    /**
     * This class represents a record containing the tracked data.
     */
    public class TrackedDataRecord
    {
        public int                  recordId;
        public DateTime             intervalStartDateTime;  // start of measurement interval (UTC)
        public int                  timeZoneSeconds;        // timezone offset in seconds
        public int                  measurementInterval;    // lenght of measuremnt interval in seconds
        public int                  steps;
        public int                  active;
        public int                  distance;
        public int                  kcal;
        public int                  kcalRest;
        public int                  unknown;
        public int                  sleepMode;              // 0-not known, 1-active, 2-??, 3-1st sleep hour, 4-sleeping
        public int                  sleep;                  // sleep seconds
        public int                  fitnessPoints;          // Fitness points collected during the interval
        
        
        public void add(TrackedDataRecord record)
        {
            TrackedDataRecord sum;
            
            sum=new TrackedDataRecord();
            
            this.measurementInterval+=record.measurementInterval;
            this.steps              +=record.steps;
            this.active             +=record.active;
            this.distance           +=record.distance;
            this.kcal               +=record.kcal;
            this.kcalRest           +=record.kcalRest;
        }

        public TrackedDataRecord clone()
        {
            TrackedDataRecord sum;
            
            sum=new TrackedDataRecord();
            
            sum.intervalStartDateTime   =this.intervalStartDateTime;
            sum.timeZoneSeconds         =this.timeZoneSeconds;
            sum.measurementInterval     =this.measurementInterval;
            sum.steps                   =this.steps;
            sum.active                  =this.active;
            sum.distance                =this.distance;
            sum.kcal                    =this.kcal;
            sum.kcalRest                =this.kcalRest;
            sum.unknown                 =this.unknown;
            
            return sum;
        }

        
    }

    class FitnessRecord
    {
        public DateTime                 dateTime;
        public int                      interval;
        public int                      fitnessPointCounter1;
        public int                      fitnessPointCounter2;
    }
    
    private static final int                        SLEEPMODE_UNDEFINED     =0;
    private static final int                        SLEEPMODE_AWAKE         =1;
    private static final int                        SLEEPMODE_UNKNOWN       =2;
    private static final int                        SLEEPMODE_FIRSTSLEEP    =3;
    private static final int                        SLEEPMODE_SLEEP         =4;
    
    
    private String                                  deviceName;
    private String                                  versionString;

    final private ArrayList<HeartRateRecord>        heartRates;
    final private ArrayList<TrackedDataRecord>      trackedData;
    final private ArrayList<TrackedDataRecord>      trackedDataPerHour;
    final private ArrayList<Sleep>                  sleeps;
    final private ArrayList<FitnessRecord>          fitnessRecords;
    
    
    /**
     * Returns a UTC DateTime based on the epoch time passed 
     * @param seconds Epoch time in seconds
     * @return DateTime in UTC.
     */
    private DateTime epochToUtcDate(int seconds)
    {
        DateTime dateTime;
        
        dateTime=DateTime.forInstant((long)seconds*1000L, TimeZone.getTimeZone("UTC"));
//        dateTime=DateTime.forInstant((long)seconds*1000L, TimeZone.getDefault());     // Returns DateTime in local time of PC
        
        return dateTime;
    }
    
    /**
     * Process Device info
     * @param info Device info
     * @return False if all went ok, true if an error occurred.
     */
    private boolean processDeviceInfo(TrackerProto.DeviceInfo info)
    {
        boolean                         error;
        TrackerProto.SoftwareVersion    version;
        String                          theVersion;
        
        error=false;
        
        version             =info.getSoftwareVersion();
        theVersion          =String.format("%d.%d.%d", version.getMajorVersion(), version.getMediumVersion(), version.getMinorVersion());
        
        if (deviceName==null)
        {
            deviceName      =info.getDeviceName();
            versionString   =theVersion;
        }
        else
        {
            if (!deviceName.equals(info.getDeviceName()))
            {
                DebugLogger.error("Error parsing tracker data: device ID not consistent");
            }
            if (!versionString.equals(theVersion))
            {
                DebugLogger.error("Error parsing tracker data: software version not consistent");
            }
        }
        DebugLogger.info("Parsing activity tracker file from device: "+info.getDeviceName()+", software "+theVersion);
        
        
        
        return error;        
    }
    
    /**
     * Process the data record
     * @return False if all went ok, true if an error occurred.
     */    
    private boolean processTrackedActivityRecord(TrackerProto.TrackRecord record)
    {
        boolean                     error;
        TrackedDataRecord           dataRecord;
        
        error=false;
        
        dataRecord=new TrackedDataRecord();
        
        dataRecord.intervalStartDateTime    =epochToUtcDate(record.getTime());
        dataRecord.measurementInterval      =record.getInterval();
        dataRecord.timeZoneSeconds          =record.getTimeZone();

        dataRecord.steps                    =record.getSteps();
        dataRecord.active                   =record.getActive();
        dataRecord.distance                 =record.getDistance();
        dataRecord.kcal                     =record.getKcal();
        dataRecord.kcalRest                 =record.getKcalRest();
        
        if (record.hasSleepMode())
        {
            dataRecord.sleepMode            =record.getSleepMode();
        }
        else
        {
            dataRecord.sleepMode            =0;
        }
        if (record.hasSleepTime())
        {
            dataRecord.sleep                =record.getSleepTime();
        }
        else
        {
            dataRecord.sleep                =0;
        }
        
        trackedData.add(dataRecord);
                
/*        
        System.out.print(record.getRecordId()+" "+epochToUtcDate(record.getTime())+" zone "+record.getTimeZone()+" steps "+record.getSteps()+" act "+record.getActive()+" dist "+record.getDistance()+" kcal "+
                                          record.getKcal()+" kcalrest "+record.getKcalRest()+" u1 "+record.getUnknown1());
        if (record.hasSleepMode())
        {
            System.out.print(" sleepmode "+record.getSleepMode());
        }
        if (record.hasSleepTime())
        {
            System.out.print(" sleeptime "+record.getSleepTime());
        }
        System.out.println();
*/        
        return error;
    }
    
    /**
     * Process the data record
     * @return False if all went ok, true if an error occurred.
     */    
    private boolean processRecord2(TrackerProto.Record2 record)
    {
        boolean                     error;
        
        error=false;
        
        return error;
    }
    
    
    /**
     * Process the data record
     * @return False if all went ok, true if an error occurred.
     */    
    private boolean processHeartRateRecord(TrackerProto.HeartRecord record)
    {
        boolean                     error;
        HeartRateRecord                   heartRate;
        
        error=false;

        heartRate=new HeartRateRecord();
        
        heartRate.dateTime  =epochToUtcDate(record.getTime());
        heartRate.interval  =record.getInterval();
        heartRate.heartRate =record.getHeartRate();
        heartRate.unknown1  =record.getValue01();
        heartRate.unknown2  =record.getValue02();
        
        this.heartRates.add(heartRate);
        
//        System.out.println(epochToUtcDate(record.getTime())+" "+record.getHeartRate()+" "+record.getValue02()+" "+record.getValue03());

        
        return error;
    }
    
    
    /**
     * Process the data record
     * @return False if all went ok, true if an error occurred.
     */    
    private boolean processRecord4(TrackerProto.Record4 record)
    {
        boolean                     error;
        
        error=false;
        
        
        return error;
    }


    /**
     * Return the fitness point counter value at given date time.
     * @param dateTime Date time 
     * @return Fitness point counter value. If the datetime is prior to 
     *         the values in the list, 0 is returned.
     */
    public int getFitnessPointCounter(DateTime dateTime)
    {
        int                     counter;
        Iterator<FitnessRecord> it;
        FitnessRecord           record;
        boolean                 exit;
        
        counter=0;
        exit=false;
        it=fitnessRecords.iterator();
        while(it.hasNext() && !exit)
        {
            record=it.next();
            if (record.dateTime.gt(dateTime))
            {
                exit=true;
            }
            else
            {
                counter=record.fitnessPointCounter1;
            }
        }

        return counter;
    }
    
    
    /**
     * Process the data record
     * @return False if all went ok, true if an error occurred.
     */  
    private boolean processFitnessRecord(TrackerProto.FitnessRecord record)
    {
        boolean                     error;
        FitnessRecord               fitnessRecord;
        
        error=false;
        
        fitnessRecord=new FitnessRecord();
        
        fitnessRecord.dateTime=this.epochToUtcDate(record.getTime());
        fitnessRecord.interval=record.getInterval();
        fitnessRecord.fitnessPointCounter1=record.getFitnessPoints1();
        fitnessRecord.fitnessPointCounter2=record.getFitnessPoints2();
        
        fitnessRecords.add(fitnessRecord);
        
        return error;        
    }
    

    
    
    /**
     * Constructor. Initialises the class variabeles.
     */
    public Tracker()
    {
        deviceName          =null;
        heartRates          =new ArrayList<>();
        trackedData         =new ArrayList<>();
        trackedDataPerHour  =new ArrayList<>();
        sleeps              =new ArrayList<>();
        fitnessRecords      =new ArrayList<>();
    }
    
    /**
     * Clears all the data stored. Should be called prior to parsing a new
     * set of activity track files.
     */
    public void clear()
    {
        heartRates.clear();
        trackedData.clear();
        sleeps.clear();
    }

    /**
     * Convert the data to hourly data. Deduce sleeping periods, if any
     * Sleep calculation: A sleep period consists of mode SLEEPMODE_FIRSTSLEEP 
     * followed by SLEEPMODE_SLEEP. Sum sleep duration during the period 
     */
    public void convertToHourly()
    {
        DateTime                        lastDateTime;
        TrackedDataRecord               hourlyRecord;
        TrackedDataRecord               record;
        Iterator<TrackedDataRecord>     it;
        int                             lastSleepMode;
        Sleep                           sleep;
        int                             previousFitnessPoints;
        int                             currentFitnessPoints;
       
        lastDateTime            =new DateTime("1970-01-01 00:00:00");
        hourlyRecord            =null;
        sleep                   =null;
        lastSleepMode           =SLEEPMODE_UNDEFINED;
        previousFitnessPoints   =-1;
        currentFitnessPoints    =0;
        
        it=trackedData.iterator();
        
        while (it.hasNext())
        {
            record=it.next();
            


            // End condition of sleep period
            // Sometimes following occurs: ...33344444333... (one sleep period immediately followed by another)
            // In that case end the first, start the next. Therefore, first check the end condition.
            if ((record.sleepMode!=SLEEPMODE_SLEEP) && (lastSleepMode==SLEEPMODE_SLEEP))
            {
                    sleep.wakeUpTime=record.intervalStartDateTime;
                    sleeps.add(sleep);
            }

            // start condition of sleep period  
            if ((record.sleepMode==SLEEPMODE_FIRSTSLEEP) && (lastSleepMode!=SLEEPMODE_FIRSTSLEEP))
            {
                sleep               =new Sleep();
                sleep.goToSleepTime =record.intervalStartDateTime;
            }
            
            // Sleeping
            if ((record.sleepMode==SLEEPMODE_SLEEP) || (record.sleepMode==SLEEPMODE_FIRSTSLEEP))
            {
                // Add the sleep seconds to the sleeping period
                sleep.duration+=record.sleep;
            }

            lastSleepMode=record.sleepMode;
            
            if ((record.intervalStartDateTime.getHour()!=lastDateTime.getHour()) ||
                (record.intervalStartDateTime.numSecondsFrom(lastDateTime)>3600)||
                (hourlyRecord==null))
            {
                if (hourlyRecord!=null)
                {
                    currentFitnessPoints=this.getFitnessPointCounter(record.intervalStartDateTime);
                    
                    if (previousFitnessPoints<0)
                    {
                        // If no previous value is known (1st hour), set the hourly value to 0. Best we can do...
                        hourlyRecord.fitnessPoints=0;
                    }
                    else
                    {
                        // The number of points earned during the hour is the difference of the counter value
                        // at the end of the hour and at the beginning of the hour.
                        // At 00:00 hours (local time) the fitness point counter is reset to 0: max (diff, 0)
                        // TO DO: If fitness points are collected around midnight, an error may occur. Check and repair.
                        hourlyRecord.fitnessPoints=Math.max(currentFitnessPoints-previousFitnessPoints, 0);
                    }
                    this.trackedDataPerHour.add(hourlyRecord);
                    previousFitnessPoints=currentFitnessPoints;
                }
                hourlyRecord=record.clone();
                lastDateTime=record.intervalStartDateTime;
            }
            else
            {
                hourlyRecord.add(record);
            }
        }
        
    }
    
    /**
     * Appends the tracker data from the data presented
     * @param data Protobuffer encoded data
     * @return False if all went ok, true if an error occurred
     */
    public boolean appendFromData(byte[] data)
    {
        boolean                                 error;
        boolean                                 exit;
        TrackerProto.Root                       root;
        TrackerProto.RootContainer              container;
        List<TrackerProto.RootContainer>        containers;
        Iterator<TrackerProto.RootContainer>    containerIt;  
        TrackerProto.DataContainer              dataContainer;
        TrackerProto.SubDataContainer           subDataContainer;
        
        error=false;
        
        try
        {
            root                =TrackerProto.Root.parseFrom(data);

            exit                =false;
                
            
            containers                 =root.getRootContainerList();
            containerIt                =containers.iterator();
            while (containerIt.hasNext() && !exit)
            {
                container=containerIt.next();
                
                if (container.hasDataContainer())
                {
                    dataContainer=container.getDataContainer();
                    subDataContainer=dataContainer.getSubDataContainer();
                    
                    // The subdata container contains a record. Get it and  process it
                    if (subDataContainer.hasDeviceInfo())
                    {
                        error=processDeviceInfo(subDataContainer.getDeviceInfo());
                    }
                    else if (subDataContainer.hasTrackRecord())
                    {
                        error=processTrackedActivityRecord(subDataContainer.getTrackRecord());
                    }
                    else if (subDataContainer.hasFitnessRecord())
                    {
                        error=processFitnessRecord(subDataContainer.getFitnessRecord());
                    }
                    else if (subDataContainer.hasRecord2())
                    {
                        error=processRecord2(subDataContainer.getRecord2());
                    }
                    else if (subDataContainer.hasHeartRecord())
                    {
                        error=processHeartRateRecord(subDataContainer.getHeartRecord());
                    }
                    else if (subDataContainer.hasRecord4())
                    {
                        error=processRecord4(subDataContainer.getRecord4());
                    }

                    
                }                
                else if (container.hasMetadata())
                {
                    // TO DO: meaning of metadata is unknown. So do nothing for the moment
                }
                
            }            
                    
        }
        catch (InvalidProtocolBufferException e)
        {
            DebugLogger.error("Error parsing tracker file: "+e.getMessage());
            error               =true;
        }
        
        
        return error;
    }
    
    /**
     * Returns the tracked activity as a table in a String. Values per hour
     * are shown.
     * @return String containing the tracked activity data
     */
    public String trackedActivityToString()
    {
        String                          string;
        Iterator<TrackedDataRecord>     it;
        TrackedDataRecord               record;
        
        string="";     
        
        string="Datetime            Interval(s) Steps Active(s)  Distance(m)  kcal  fitnesspoints\n";
        
        it=this.trackedDataPerHour.iterator();
        
        while (it.hasNext())
        {
            record=it.next();
            string+=record.intervalStartDateTime.format("YYYY-MM-DD hh:mm:ss ") + 
                    String.format("%11d ", record.measurementInterval)+
                    String.format("%5d ", record.steps)+
                    String.format("%9d ", record.active)+
                    String.format("%12d ", record.distance)+
//                    String.format("%9.2f ", (float)record.distance/1000.0)+
                    String.format("%5d ", record.kcal)+
                    String.format("%5d ", record.fitnessPoints)+
                    "\n";
        }
        
        return string;
    }
    
    /**
     * Returns the tracked heart rates as a table in a String. All values
     * are shown.
     * @return String containing the heart rate values
     */
    public String heartRatesToString()
    {
        String                          string;
        Iterator<HeartRateRecord>       it;
        HeartRateRecord                 record;
        
        string="";     
        
        string="Datetime            heartrate(bpm)  \n";
        
        it=this.heartRates.iterator();
        
        while (it.hasNext())
        {
            record=it.next();
            string+=record.dateTime.format("YYYY-MM-DD hh:mm:ss ") + 
                    String.format("%14d ", record.heartRate)+
                    "\n";
        }
        
        return string;
    }    


    /**
     * Returns the sleeping periods as a table in a String. All values
     * are shown.
     * @return String containing the sleeping periods
     */
    public String sleepingPeriodsToString()
    {
        String                          string;
        Iterator<Sleep>                 it;
        Sleep                           record;
        
        string="";     
        
        string="Start                  End             Sleeping  \n";
        
        it=this.sleeps.iterator();
        
        while (it.hasNext())
        {
            record=it.next();
            string+=record.goToSleepTime.format("YYYY-MM-DD hh:mm:ss ") +
                    record.wakeUpTime.format("YYYY-MM-DD hh:mm:ss ") +
                    String.format("%20s ", ToolBox.secondsToHours(record.duration))+
                    "\n";
        }
        
        return string;
    }    
    
}
