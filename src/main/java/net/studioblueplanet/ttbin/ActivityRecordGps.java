/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import net.studioblueplanet.logger.DebugLogger;

import java.io.IOException;
import java.io.Writer;
import hirondelle.date4j.DateTime;

/**
 * This record represents an GPS logged record. It applies to hiking, running,
 * cycling, etc
 * @author jorgen.van.der.velde
 */
public class ActivityRecordGps extends ActivityRecord
{
    private         double      lat;                // Latitude
    private         double      lon;                // Longitude
    private         double      rawLat;             // When the track is smoothed: the original latitude
    private         double      rawLon;             // When the track is smoothed: the original longitude
    private         double      speed;              // Average speed in m/s
    private         double      heading;            // Heading in degrees
    private         double      calories;           // calories
    private         double      distance;           // Cumulative distance since beginning
    private         double      cycles;             // running = steps/sec, cycling = crank rpm
    private         double      instantSpeed;       // Unfiltered speed (?) in m/s
    private         double      elevation1;         // Elevation 1 from GPS record - absolute elevation
    private         double      elevation2;         // Elevation 2 from GPS record - calibrated around 0
    private         double      derivedElevation;   // Combined elevation. Elevation 2 seems the best, but is shifted towards 0
    private         double      ascend; // Cummulative Elevation Gain in m
    private         double      descend; // Cummulative Elevation Gain in m
    private         int         elevationStatus;    // Elevation status byte??
    private         DateTime    heartRateDateTime;  // The timestamp associated with the heart rate
    private         int         heartRate;          // Heartrate in bpm
    private         int         temperature;        // Temperature in degC
    private         int         evpe;               // Estimated vertical percision error, in cm
    private         int         ehpe;               // Estamated horizontal precision error, in cm
    private         int         hdop;               // HDOP
    private         DateTime    fitnPointsDateTime; // DateTime stamp of cumm. activity points
    private         int         fitnessPoints;      // Cumulative Activity points
    private         int         movementState;      // Some derived movement state: 0 - standing still, 1 - reduced speed, 2 - moving
    
    
    public          int         unknownInt1;
    public          int         unknownInt2;
    public          int         unknownInt3;
    public          int         unknownInt4;
    public          int         unknownInt5;
    public          int         unknownInt6;
    public          float       unknownFloat1;
    public          float       unknownFloat2;

    public ActivityRecordGps()
    {
        super();
        this.lat                =INVALID;
        this.lon                =INVALID;
        this.rawLat             =INVALID;
        this.rawLon             =INVALID;
        this.speed              =INVALID;
        this.heading            =INVALID;
        this.calories           =INVALID;
        this.distance           =INVALID;
        this.cycles             =INVALID;
        this.instantSpeed       =INVALID;
        this.elevation1         =INVALID;
        this.elevation2         =INVALID;
        this.derivedElevation   =INVALID;
        this.ascend             =INVALID;
        this.descend            =INVALID;
        this.elevationStatus    =INVALID;
        this.heartRate          =INVALID;
        this.temperature        =INVALID;
        this.ehpe               =INVALID;
        this.evpe               =INVALID;
        this.hdop               =INVALID;
        this.movementState      =INVALID;
    }
    
    /**
     * This method sets the coordinate
     * @param lat Latitude
     * @param lon Longitude
     */
    public void setCoordinate(double lat, double lon)
    {
       this.lat=lat;
       this.lon=lon;
    }
   
    /**
     * This method returns the latitude
     * @return The latitude
     */
    public double getLatitude()
    {
       return this.lat;
    }
   
    /**
     * This method returns the latitude
     * @return The latitude
     */
    public double getLongitude()
    {
       return this.lon;
    }

    /**
     * If the lat and lon as set with setCoordinate() have been processed,
     * for example smoothed, this raw lat and raw lon can be set to the 
     * original, non-processed value
     * @param lat Non-processed latitude value
     * @param lon Non-processed longitude value
     */
    public void setRawCoordinate(double lat, double lon)
    {
        this.rawLat=lat;
        this.rawLon=lon;
    }
    
    /**
     * Returns the unprocessed latitude value
     * @return The unprocessed latitude value or INVALID if not available
     */
    public double getRawLatitude()
    {
        return this.rawLat;
    }
    
    /**
     * Returns the unprocessed longitude value
     * @return The unprocessed longitude value or INVALID if not available
     */
    public double getRawLongitude()
    {
        return this.rawLon;
    }
    
    
    /**
     * Sets the speed averaged over the sample period
     * @param speed The speed in m/s
     */
    public void setSpeed(double speed)
    {
        this.speed=speed;
    }
    
    /**
     * Returns the speed
     * @return The speed in m/s
     */
     
    public double getSpeed()
    {
        return this.speed;
    }
    
    /**
     * Sets the value of the temperature
     * @param temperature The temperature in deg. C
     */
    public void setTemperature(int temperature)
    {
        this.temperature=temperature;
    }
    
    /**
     * Gets the value of the temperature
     * @return The temperature in deg. C
     */
    public int getTemperature()
    {
        return this.temperature;
    }
    
    /**
     * Sets the instantaneous speed at the sample time
     * @param speed The speed in m/s
     */
    public void setInstantaneousSpeed(double speed)
    {
        this.instantSpeed=speed;
    }
    
    /**
     * Returns the instantaneous speed
     * @return The speed in m/s
     */
    public double getInstantaneousSpeed()
    {
        return this.instantSpeed;
    }
    
    /**
     * This method sets the calories
     * @param calories Calories in kCal;
     */
    public void setCalories(double calories)
    {
        this.calories=calories;
    }
    
    /**
     * This method returns the energy burned
     * @return The energy in kCal
     */
    public double getCalories()
    {
        return this.calories;
    }
    
    /**
     * This method sets the heading
     * @param heading The heading in degrees (0-360);
     */
    public void setHeading(double heading)
    {
        this.heading=heading;
    }
    
    /**
     * This method returns the heading
     * @return The heading in degrees
     */
    public double getHeading()
    {
        return this.heading;
    }
    
    /**
     * This method sets the cumulative distance since the start
     * of the activity
     * @param distance The distance in m;
     */
    public void setDistance(double distance)
    {
        this.distance=distance;
    }
    
    /**
     * This method returns the cumulative distance
     * @return The distance in m
     */
    public double getDistance()
    {
        return this.distance;
    }

    
    /**
     * This method sets number of cycles
     * @param cycles Cycles. For running:, for cycling:
     */
    public void setCycles(double cycles)
    {
        this.cycles=cycles;
    }
    
    /**
     * This method returns the cycles
     * @return Cycles. For running: for cycling:
     */
    public double getCycles()
    {
        return this.cycles;
    }
    
    /**
     * Set barometric elevation 
     * @param elevation Elevation in m 
     */
    public void setElevation1(double elevation)
    {
        this.elevation1=elevation;
    }
    
    /**
     * Get elevation
     * @return The elevation in m
     */
    public double getElevation1()
    {
        return this.elevation1;
    }
    
    /**
     * Set gps(?) elevation 
     * @param elevation Elevation in m 
     */
    public void setElevation2(double elevation)
    {
        this.elevation2=elevation;
    }
    
    /**
     * Get elevation
     * @return The elevation in m
     */
    public double getElevation2()
    {
        return this.elevation2;
    }
    
    /**
     * Returns whether this record contains height values
     * @return True if it contains height value or false if not.
     */
    public boolean hasHeightValue()
    {
        return (this.elevation1!=INVALID || this.elevation2!=INVALID);
    }
    

    public void setDerivedElevation(double elevation)
    {
        this.derivedElevation=elevation;
    }
    
    
    public double getDerivedElevation()
    {
        return this.derivedElevation;
    }
    
    
    /**
     * Set barometric cumulative ascend 
     * @param ascend Elevation gain in m 
     */
    public void setCumulativeAscend(double ascend)
    {
        this.ascend=ascend;
    }
    
    /**
     * Get cumulative ascend
     * @return The elevation gain in m
     */
    public double getCumulativeAscend()
    {
        return this.ascend;
    }
    
    /**
     * Set cumulative decend
     * @param descend Elevation gain in m 
     */
    public void setCumulativeDecend(double descend)
    {
        this.descend=descend;
    }
    
    /**
     * Get cumulative decend
     * @return The elevation gain in m
     */
    public double getCumulativeDecend()
    {
        return this.descend;
    }
    
    /**
     * Set the elevation status. No idea what it represents
     * @param status The status byte
     */
    public void setElevationStatus(int status)
    {
        this.elevationStatus=status;
    }
    
    /**
     * Get the elevation status
     * @return The status byte
     */
    public int getElevationStatus()
    {
        return this.elevationStatus;
    }
    
    /**
     * Sets the heart rate 
     * @param time Time stamp of heart rate (epoch), UTC
     * @param heartRate The heart rate in bpm
     */
    public void setHeartRate(int time, int heartRate)
    {
        long recordTime;

        // If a GPS date time stamp is available, check if the 
        // heartrate timestamp correspnods to it
        if (dateTime!=null)
        {
            recordTime                  =this.dateTime.getMilliseconds(utcTimeZone);

            // Check if the timestamps match. The heartrate timestamp
            // usually is equal to the gps timestamp. However sometimes it lags
            // the gps timestamp by one or two second. We allow for that.
            if (recordTime-((long)time*1000)<=2000)
            {
                this.heartRate          =heartRate;
                this.heartRateDateTime  =DateTime.forInstant((time)*1000, utcTimeZone);
            }
            else
            {
                DebugLogger.error("HR Time difference. Expected "+recordTime/1000+" Got: "+time);
            }
        }
        else
        {
            // If no GPS timestamp/coordinates recorded, just record the 
            // heartrate value
            this.heartRate              =heartRate;
            this.heartRateDateTime=DateTime.forInstant((time)*1000, utcTimeZone);
        }
            
    }
    
    /**
     * Returns the heart rate value
     * @return Heart rate in bpm
     */
    public int getHeartRate()
    {
        return this.heartRate;
    }
    
    


    /**
     * Sets the Fitness points. Note: it is the cummulative fitness points
     * for the day. Not defined for each record.
     * @param time Time stamp of heart rate (epoch), UTC
     * @param points The fitness points
     */
    public void setFitnessPoints(int time, int points)
    {
        long recordTime;

        // If a GPS date time stamp is available, check if the 
        // heartrate timestamp correspnods to it
        if (dateTime!=null)
        {
            recordTime                  =this.dateTime.getMilliseconds(utcTimeZone);

            // Check if the timestamps match. The heartrate timestamp
            // usually is equal to the gps timestamp. However sometimes it lags
            // the gps timestamp by one, sometimes 2 second. We allow for that.
            if (recordTime-((long)time*1000)<=2000)
            {
                this.fitnessPoints             =points;
                this.fitnPointsDateTime     =DateTime.forInstant((time)*1000, utcTimeZone);
            }
            else
            {
                DebugLogger.error("FP Time difference. Expected "+recordTime/1000+" Got: "+time);
            }
        }
        else
        {
            // If no GPS timestamp/coordinates recorded, just record the 
            // heartrate value
            this.fitnessPoints         =points;
            this.fitnPointsDateTime =DateTime.forInstant((time)*1000, utcTimeZone);
        }
            
    }
    
    /**
     * Returns the fitness point counter value
     * @return The points (daily counter value)
     */
    public int getFitnessPoints()
    {
        return this.fitnessPoints;
    }

    
    
    
    
    /**
     * Sets the precision of the measurement
     * @param ehpe Estimated horizontal precision error in cm
     * @param evpe Estimated vertical precision error in cm
     * @param hdop Horizontal Dilution of Precision (https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation)#Meaning_of_DOP_Values))
     */
    public void setPrecision(int ehpe, int evpe, int hdop)
    {
        this.ehpe=ehpe;
        this.evpe=evpe;
        this.hdop=hdop;
    }
    
    /**
     * Returns the estimated horizontal precision error
     * @return The precision error in cm
     */
    public int getEhpe()
    {
        return this.ehpe;
    }
    
    /**
     * Returns the estimated horizontal vertical error
     * @return The precision error in cm
     */
    public int getEvpe()
    {
        return this.evpe;
    }
    
    /**
     * Returns the horizontal dilution of precision
     * @return HDOP value in m
     */
    public int getHdop()
    {
        return this.hdop;
    }
    
    /**
     * Sets the value of the movement state, some state derived from 
     * speed
     * @param state 0-standing still, 1-reduced speed, 2-moving 
     */
    public void setMovementState(int state)
    {
        this.movementState=state;
    }
    
    /**
     * Gets the movement state
     * @return The movement state (0, 1, 2)
     */
    public int getMovementState()
    {
        return this.movementState;
    }
    
    
    /*############################################################################################*\
     * DEBUGGING
    \*############################################################################################*/    

    /**
     * Dump the record for debugging purposes
     */
    public void dumpRecord()
    {
        DebugLogger.info("Date time              ");
        if (dateTime!=null)
        {
            DebugLogger.info("Date time              "+this.dateTime.format("YYYY-MM-DD hh:mm:ss"));
        }
        else
        {
            DebugLogger.info("Date time              ----");
        }
        
        DebugLogger.info("Coordinate              ("+this.lat+", "+this.lon+")");
        DebugLogger.info("Speed (m/s)             "+this.speed);
        DebugLogger.info("Instant speed (m/s)     "+this.instantSpeed);
        DebugLogger.info("Distance (m)            "+this.distance);
        DebugLogger.info("Heading (deg)           "+this.heading);
        DebugLogger.info("Calories (kCal)         "+this.calories);
        DebugLogger.info("Elevation 1 (m)         "+this.elevation1);
        DebugLogger.info("Cumm. Elevation gain (m)"+this.ascend);
        DebugLogger.info("Elevation 2 (m)         "+this.elevation2);
        DebugLogger.info("Cumm. Elevation gain (m)"+this.ascend);
        DebugLogger.info("Cycles                  "+this.cycles);
    }
    
    /**
     * Dump the header containing all record fields
     * @param writer Writer to dump to
     * @throws IOException Thrown when an error occurs during writing
     */
    @Override
    public void dumpRecordCsvHeader(Writer writer) throws IOException
    {
        writer.write("dateTime, lat, lon, dist, cycles, heading, speed, intSpeed, movement, calories,"); 
        writer.write("status, ele1, ele2, eleCorr, cumEle1, cumEle2,");
        writer.write("heartrate,");
        writer.write("ehpe, evpe,hdop,");
        writer.write("battery,");
        writer.write("unknownint1, unknownint2,unknownint3,unknownint4,unknownint5,unknownint6,unknwonfloat1,unknownfloat2");
        writer.write("\n");
    }
    
    /**
     * Dump all the record fields of this record
     * @param writer Writer to dump to
     * @throws IOException Thrown when an error occurs during writing
     */
    @Override
    public void dumpRecordCsv(Writer writer) throws IOException
    {
        if (dateTime!=null)
        {
            writer.write(this.dateTime.format("YYYY-MM-DD hh:mm:ss")+",");
        }
        else
        {
            writer.write("----,");
        }
        writer.write(this.lat+",");
        writer.write(this.lon+",");
        writer.write(this.distance+",");
        writer.write(this.cycles+",");
        writer.write(this.heading+",");
        writer.write(this.speed+",");
        writer.write(this.instantSpeed+",");
        writer.write(this.movementState+",");
        writer.write(this.calories+",");
        
        writer.write(this.elevationStatus+",");
        writer.write(this.elevation1+",");
        writer.write(this.elevation2+",");
        writer.write(this.derivedElevation+",");
        writer.write(this.ascend+",");
        writer.write(this.descend+",");
        
        writer.write(""+this.heartRate+",");

        writer.write(""+this.ehpe+",");
        writer.write(""+this.evpe+",");
        writer.write(""+this.hdop+",");
        writer.write(""+this.batteryLevel+",");

        
        writer.write(""+this.unknownInt1+",");
        writer.write(""+this.unknownInt2+",");
        writer.write(""+this.unknownInt3+",");
        writer.write(""+this.unknownInt4+",");
        writer.write(""+this.unknownInt5+",");
        writer.write(""+this.unknownInt6+",");
        writer.write(""+this.unknownFloat1+",");
        writer.write(""+this.unknownFloat2+",");
        writer.write("\n");
        
    }
    
    
}
