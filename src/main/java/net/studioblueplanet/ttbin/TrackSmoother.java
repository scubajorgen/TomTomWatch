/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import java.util.List;
import java.util.Iterator;
import java.util.TimeZone;

/**
 *
 * @author Jorgen
 */
public class TrackSmoother
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
    
    
    private static          TrackSmoother              theInstance;
    
    /**
     * Private constructor
     */
    private TrackSmoother()
    {
    }


    /**
     * Returns the one and only instance of this class.
     * @return The instance of this class
     */
    public static TrackSmoother getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new TrackSmoother();
        }
        return theInstance;
    }
    
    /**
     * This method smoothes the track, by applying a Kalman filter
     * @param trackSmoothingQFactor The factor influencing the smoothing.
     *                              1.0 - high amount to 10.0 - low
     * @param segments Track segments making up the track
     */
    public void smoothTrack(List<ActivitySegment> segments, float trackSmoothingQFactor)
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
    }


    /**
     * This method resets the smoothing
     * @param segments List of segments making up the activity/track
     */
    public void resetSmoothing(List <ActivitySegment> segments)
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

    }
    
}
