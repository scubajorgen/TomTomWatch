/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;

import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.ActivitySegment;
import net.studioblueplanet.ttbin.ActivityRecord;
import net.studioblueplanet.ttbin.ActivityRecordGps;


/**
 *
 * @author Jorgen
 */
public class Map
{
    // should be 2048 according to google. However, in practice the URL 
    // google processes must be smaller
    private static final int        MAXSTRINGLENGTH=1700;

    private static final int        MAXCOMPRESSIONPARAMS=7;
    private static final int[]      compressionParamPoints     ={  0,     25,    500,   1000,   1500,   2000,   2500,  3000};    
    private static final double[]   compressionParamMaxSlopeDev={0.0,    0.2,    0.3,    0.6,    0.7,    0.8,   0.85,   0.9};    
//    private static final double[]   compressionParamMaxSlopeDev={0.0, 0.0001, 0.00025, 0.0005, 0.0008, 0.0012, 0.0016, 0.002};    
    private int                     compressionParamIndex      =MAXCOMPRESSIONPARAMS-1;

    
    private JPanel                      panel;
    private JLabel                      label;
    private int                         panelWidth;
    private int                         panelHeight;
    
   
    private ActivityRecordGps           firstPoint;
    private ActivityRecordGps           lastPoint;
    private int                         totalPointCount;
    private int                         compressedPointCount;
    
    
//    private ActivityRecordGps           previousEncodedRecord=null;
    private RoutePoint                  previousEncodedPoint=null;

    private double                      maxSlopeDeviation=0.0005;
    
    private String                      resultString;
    
    public static final String          MAPTYPE_ROAD        ="roadmap";
    public static final String          MAPTYPE_SATELLITE   ="satellite";
    public static final String          MAPTYPE_TERRAIN     ="terrain";
    public static final String          MAPTYPE_HYBRID      ="hybrid";

    private static String               mapType=MAPTYPE_ROAD;
    
    /**
     * Constuctor
     */
    public Map(JPanel panel)
    {
        this.panel  =panel;
        label       = new JLabel();
        panel.add(label);     
        panelWidth  =panel.getWidth();
        panelHeight =panel.getHeight();
    }

    /**
     * Returns the map type currently set
     * @return The map type
     */
    public static String getMapType()
    {
        return mapType;
    }
    
    /**
     * Sets the map type
     * @param newMapType The new map type 
     */
    public static void setMapType(String newMapType)
    {
        if (newMapType.equals(MAPTYPE_ROAD) ||
            newMapType.equals(MAPTYPE_SATELLITE) ||
            newMapType.equals(MAPTYPE_TERRAIN) ||
            newMapType.equals(MAPTYPE_HYBRID))
        {
            mapType=newMapType;
            DebugLogger.info("Map type set to "+mapType);
        }
    }
    
    /**
     * Returns the URL for retreiving the map
     * @return The URL
     */
    private String getMapString()
    {
        String mapString;
        
        mapString="http://maps.googleapis.com/maps/api/staticmap?size="+panelWidth+"x"+panelHeight+"&sensor=false&maptype=" + 
                  mapType;
        

        return mapString;
        
    }
    
    
    
    
    /**
     *  This method resets the Google polyline encoding. Basically it
     *  sets the 1st point to null, indicating that the first point must be
     *  encoded fully (for subsequent points only the delta will be encoded)
     */
    private void resetPointEncoding()
    {
//        this.previousEncodedRecord=null;
        this.previousEncodedPoint=null;
    }
    
    /**
     * This method encodes a point according the Google encoded polyline
     * method.
     * @param point The point to encode
     * @return String representing the encoded point (or delta with respect
     *                to previous point if it is not the 1st point)
     */
    private String encodePoint(RoutePoint record)
    {
        String              encodedPointString;
        double              deltaLat, deltaLon;
        
        encodedPointString="";
        
        
        if (previousEncodedPoint==null)
        {
            encodedPointString+=encodeValue(record.getLatitude());
            encodedPointString+=encodeValue(record.getLongitude());
        }
        else
        {
            deltaLat=record.getLatitude()-previousEncodedPoint.getLatitude();
            deltaLon=record.getLongitude()-previousEncodedPoint.getLongitude();
            encodedPointString+=encodeValue(deltaLat);
            encodedPointString+=encodeValue(deltaLon);
        }
        previousEncodedPoint=record;
        
        return encodedPointString;
        
    }
    
        
    
    
    /**
     * This method converts doubles to the Google encoded string format
     * @param value Value to convert
     * @return Encoded string part
     */
    private String encodeValue(double value)
    {
        String  conversion;
        int     binValue;
        int     charCode;
        int     nextCharCode;
        char    theChar;
        boolean isNegative;
        int     i;
        boolean finished;
        
        conversion="";
        finished=false;
        
        if (value<0)
        {
            value=-value;
            isNegative=true;
        }
        else
        {
            isNegative=false;
        }
        

        value=value*1e5;
        binValue=(int)Math.round(value);
        if (binValue==0)
        {
            isNegative=false;
        }
        
        if (isNegative)
        {
            binValue=~binValue;
            binValue+=1;
        }
        binValue<<=1;
        
        if (isNegative)
        {
            binValue=~binValue;
        }
        
        i=0;
        while (i<6 && !finished)
        {
            charCode=binValue & 0x1f;
            binValue>>=5;
            
            if (i<5)
            {
                nextCharCode=binValue>>((i+1)*5) & 0x1f;
                if (binValue>0)
                {
                    charCode |= 0x20;
                }
                else
                {
                    finished=true;
                }
            }
            charCode+=63;
            theChar=(char)charCode;
            conversion+=theChar;


            i++;
        }
        
        return conversion;
    }
    
    
    
    
    
    
    /**
     * Convert the track to a Google map string, while compressing it so that
     * the string length does not exceed given maximum. Compression is implemented
     * by skipping route points.
     * @param route Route to encode
     * @param maxStringLength Maximum length of the encoeded string
     * @return 
     */
    String compressAndConvertTrack(Route route, int maxStringLength)
    {
        int                         segment;
        int                         numberOfSegments;
        String                      trackString;
        String                      pointString;
        String                      pathString;
        RouteSegment                points;
        RoutePoint                  record;
        boolean                     bailOut;
        boolean                     found;
        boolean                     maxCompressionReached;
        int                         numberOfTrackPoints;
        int                         segmentLength;
        
        double                      charsPerPoint;
        double                      increment;
        int                         index;
        int                         indexCount;
        
        double                      latitude;
        double                      longitude;


        trackString         ="";
        compressedPointCount=0;


        numberOfSegments    =route.getNumberOfSegments();

        // Calibrate the compression based on the number of trackpoints in
        // the track
        numberOfTrackPoints =0;
        segment             =0;
        while (segment<numberOfSegments)
        {
            points=route.getSegment(segment);
            numberOfTrackPoints+=points.getNumberOfPoints();
            segment++;
        }
        
        // The average encoding length for a point is about 6.0 chars per lat/lon
        // Assume 6.5 for starters, in order to increase the change the string limit
        // is not exceeded
        charsPerPoint   =3.0;
        increment       =(double)numberOfTrackPoints/(maxStringLength/charsPerPoint);
        increment       =Math.max(increment,1.0);
        DebugLogger.info("Map encoding: starting with compression "+charsPerPoint+
                         " points to encode "+numberOfTrackPoints+
                         " max length "+maxStringLength+
                         " increment "+increment);
        
        

        // This loop tries higher compression levels, if the compression
        // results in truncated strings.
        maxCompressionReached=false;
        found=false;
        while (!found && !maxCompressionReached)
        {
            compressedPointCount=0;
            bailOut         =false;
            trackString     ="";

            // Process the segments
            segment=0;
            while (segment<numberOfSegments && !bailOut)
            {
                this.resetPointEncoding();            

                if ((segment%2)>0)
                {
                    pathString="path=color:blue|enc:";
                }
                else
                {
                    pathString="path=color:red|enc:";

                }
                // If adding the path string makes the track string exceed
                // the max length, bail out
                if (trackString.length()+pathString.length()<maxStringLength)
                {
                    trackString+=pathString;
                }
                else
                {
                    bailOut=true;
                }   

                // Retrieve the list of points that make up the segment
                points          =route.getSegment(segment);
                segmentLength   =points.getNumberOfPoints();

                
                indexCount  =0;
                index       =0;
                while (index<segmentLength && !bailOut)
                {
                    index=(int)(increment*indexCount);

                    if (index<segmentLength)
                    {
                        record=points.getPoint(index);

                        latitude    =record.getLatitude();
                        longitude   =record.getLongitude();

                        // Encode the lat/lon 
                        pointString      =this.encodePoint(record);

                        // If adding the point string makes the track string exceed
                        // the max length, bail out
                        if (trackString.length()+pointString.length()<maxStringLength)
                        {
                            trackString+=pointString;
                            compressedPointCount++;
                        }
                        else
                        {
                            bailOut=true;
                        }

                    }                    
                    indexCount++;
                }
                
                // Make sure the last point in the array is always encoded
                if ((index<segmentLength-1) && !bailOut)
                {
                    record=points.getLastPoint();

                    pointString=this.encodePoint(record);

                    // If adding the point string makes the track string exceed
                    // the max length, bail out
                    if (trackString.length()+pointString.length()<maxStringLength)
                    {
                        trackString+=pointString;
                        compressedPointCount++;
                    }
                    else
                    {
                        bailOut=true;
                    }                    
                }
                



                // More segments to follow? Add a pipe symbol as separator

                if (segment<numberOfSegments-1 && !bailOut)
                {
                    trackString+='&';
                }

                segment++;
            }

            
            // If not bailed out, the points have been encoded and the 
            // resulting string is within limits
            if (!bailOut)
            {
                found=true;
            }
            else
            {
                // If bailed out, try again with a higher increment
                charsPerPoint   *=1.1;
                if (charsPerPoint<=10.0)
                {
                    increment       =(double)numberOfTrackPoints/(maxStringLength/charsPerPoint);
                    DebugLogger.info("Map encoding: string exceeded "+trackString.length()+
                                     "@"+compressedPointCount+" points, trying higher compression "+charsPerPoint);
                }
                else
                {
                    maxCompressionReached=true;
                    DebugLogger.info("Map encoding: max compression reached ");
                }
            }
            
        }        
        
        
        
        if (!found)
        {
            resultString="Track truncated for printing. Too much points";
            DebugLogger.info(resultString);            
        }

        DebugLogger.info("Points "+compressedPointCount+" Length "+trackString.length()+" cpp "+(double)trackString.length()/(double)compressedPointCount);

        return trackString;        
    }
    
    
    /**
     * This method show the track in this frame on a google map
     * @param trackNo The track to show
     */
    public String showTrack(ActivityData activityData)
    {
        Activity                    activity;
        Route                    route;
        int                         numberOfSegments;
        int                         numberOfPoints;
        int                         segmentCount;
        int                         pointCount;
        RouteSegment                routeSegment;
        RoutePoint                  point;
        ActivityRecord              record;
        ActivitySegment             segment;
        ActivityRecordGps           recordGps;
        ArrayList<ActivityRecord>   points;
        Iterator<ActivityRecord>    it;
        double                      latitude;
        double                      longitude;
        BufferedImage               image;
        
        // The map image is cached with each activity in the ActivityData
        // First, check if the cached map image exists. If so, display
        // this image. Otherwise (mapImage==null) generate the map image
        if (activityData.mapImage==null)
        {
            // Convert Activity to Route
            activity=activityData.activity;

            route=new Route();

            numberOfSegments=activity.getNumberOfSegments();
            segmentCount=0;
            while (segmentCount<numberOfSegments)
            {
                routeSegment=route.appendRouteSegment();

                points=activity.getRecords(segmentCount);
                it=points.iterator();
                while (it.hasNext())
                {
                    recordGps=(ActivityRecordGps)it.next();

                    latitude    =recordGps.getLatitude();
                    longitude   =recordGps.getLongitude();

                    // Filter out invalid lat/lon
                    if ((latitude!=ActivityRecord.INVALID) && (longitude!=ActivityRecord.INVALID) && 
                        (latitude!=0.0) && (longitude!=0.0))
                    {     
                        point=new RoutePoint(latitude, longitude);
                        routeSegment.appendRoutePoint(point);
                    }
                }


                segmentCount++;
            }
            image                   =this.generateMapImage(route);
            activityData.mapImage   =image;
        }
        else
        {
            image                   =activityData.mapImage;
        }
        this.showTrackImage(image);
        

        return "Ok";
    }

    /**
     * This method show the track in this frame on a google map
     * @param trackNo The track to show
     */
    public String showTrack(Route route)
    {
        BufferedImage   image;        

        image=generateMapImage(route);
        
        if (image!=null)
        {
            showTrackImage(image);
        }
        return "Ok";
    }    
    
    /**
     * This method generates the map image showing the route
     * @param route Route to show
     * @return The map image or null if not succeeded
     */
    private BufferedImage generateMapImage(Route route)
    {
        String          trackString;
        BufferedImage   image;        
        ImageIcon       imageIcon;
        
        resultString    ="Track shown";
        imageIcon       =null;
        image           =null;
        
        try
        {

            trackString=this.getMapString()+"&"; 
            trackString+=compressAndConvertTrack(route, MAXSTRINGLENGTH-trackString.length());
            DebugLogger.debug("Google URL: "+trackString+" Length: "+ trackString.length());        
            image                   = ImageIO.read(new URL(trackString));

            
        }
        catch (Exception e)
        {
            resultString="Unable to get Google map"; 
            DebugLogger.error(resultString+": "+e.getMessage()); 
        }

        return image;
    }
    
    
    
    /**
     * Shows a previously cached map image
     * @param image The image to show
     */
    public void showTrackImage(BufferedImage image)
    {
        ImageIcon       imageIcon;
        
        imageIcon=new ImageIcon(image);     
        label.setIcon(imageIcon);
        panel.setVisible(true);
    }
    
    
    /**
     * Hides the track
     */
    public void hideTrack()
    {
        this.label.setIcon(null);
    }
    
}
