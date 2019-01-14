/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.generics.PolyLineEncoder;
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
import net.studioblueplanet.settings.ConfigSettings;


/**
 *
 * @author Jorgen
 */
public class Map
{
    private class MapRoute
    {
        Route           route;
        String          mapString;
        BufferedImage   image;
        String          imageRemark;
    }
    
    // should be 2048 according to google. However, in practice the URL 
    // google processes must be smaller
    private static final int            MAXSTRINGLENGTH=1700;

    private static final int            MAXCOMPRESSIONPARAMS=7;
    
    private final JPanel                panel;
    private final JLabel                label;
    private final int                   panelWidth;
    private final int                   panelHeight;
    
   
    private ActivityRecordGps           firstPoint;
    private ActivityRecordGps           lastPoint;
    private int                         totalPointCount;
    private int                         compressedPointCount;
    
    private final PolyLineEncoder       encoder;
    
    public static final String          MAPTYPE_ROAD        ="roadmap";
    public static final String          MAPTYPE_SATELLITE   ="satellite";
    public static final String          MAPTYPE_TERRAIN     ="terrain";
    public static final String          MAPTYPE_HYBRID      ="hybrid";

    private static String               mapType=MAPTYPE_ROAD;
    
    private static String               mapKey="";
    
    /**
     * Constuctor
     * @param panel Panel to use for showing the map
     */
    public Map(JPanel panel)
    {
        ConfigSettings  settings;
        
        this.panel  =panel;
        label       = new JLabel();
        panel.add(label);     
        panelWidth  =panel.getWidth();
        panelHeight =panel.getHeight();
        
        encoder     =PolyLineEncoder.getInstance();
        
        settings    =ConfigSettings.getInstance();
        mapKey      =settings.getStringValue("mapServiceKey");
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
     * Returns the URL for retreiving the map. It returs the URL in mapRoute
     * or a mapRoute.imageRemark if no Google map API key defined.
     * @param mapRoute Structure defining the map route. 
     * @return The URL
     */
    private void getMapString(MapRoute mapRoute)
    {
        mapRoute.mapString=null;
        
        if (!mapKey.equals("") && !mapKey.equals("none"))
        {
            mapRoute.mapString="http://maps.googleapis.com/maps/api/staticmap?key="+mapKey+
                                                                   "&size="+panelWidth+"x"+panelHeight+
                                                                   "&sensor=false&maptype=" + mapType;
            mapRoute.imageRemark="";
        }
        else
        {
            mapRoute.imageRemark="No map image available. No Google map API key defined";
        }

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
                encoder.resetPointEncoding();            

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
                        pointString      =encoder.encodePoint(latitude, longitude);

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

                    latitude    =record.getLatitude();
                    longitude   =record.getLongitude();

                    pointString=encoder.encodePoint(latitude, longitude);

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
            DebugLogger.info("Track truncated for printing. Too much points");
        }

        DebugLogger.info("Points "+compressedPointCount+" Length "+trackString.length()+" cpp "+(double)trackString.length()/(double)compressedPointCount);

        return trackString;        
    }
    
    
    /**
     * This method show the track in this frame on a google map
     * @param activityData The activity data structure containing the track (Activity) to show
     * @return A string indicating the result of the showing (ToDo: remove or make sensible value).
     */
    public String showTrack(ActivityData activityData)
    {
        Activity                    activity;
        Route                       route;
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
        MapRoute                    mapRoute;
        
        // The map image is cached with each activity in the ActivityData
        // First, check if the cached map image exists. If so, display
        // this image. Otherwise (mapImage==null) generate the map image
        if (activityData.mapImage==null)
        {
            // Convert Activity to Route
            activity=activityData.activity;

            mapRoute    =new MapRoute();
            
            mapRoute.route=new Route();

            numberOfSegments=activity.getNumberOfSegments();
            segmentCount=0;
            while (segmentCount<numberOfSegments)
            {
                routeSegment=mapRoute.route.appendRouteSegment();

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
            generateMapImage(mapRoute);
            activityData.mapImage       =mapRoute.image;
            activityData.mapImageRemark =mapRoute.imageRemark;
        }

     
        if (activityData.mapImage!=null)
        {
            this.showTrackImage(activityData.mapImage);
        }
        else
        {
            // Remove the icon image and display some error text
            label.setIcon(null);
            label.setText(activityData.mapImageRemark);
            DebugLogger.error("No map image available: "+activityData.mapImageRemark);
        }

        return "Ok";
    }

    /**
     * This method show the route track in this frame on a google map
     * @param mapRoute The route to show as structure.
     * @return A string indicating the result of the showing (ToDo: remove or make sensible value).
     */
    public String showTrack(MapRoute mapRoute)
    {
        BufferedImage   image;        

        generateMapImage(mapRoute);
        
        if (mapRoute.image!=null)
        {
            showTrackImage(mapRoute.image);
        }
        return "Ok";
    }    
    
    /**
     * This method show the route track in this frame on a google map
     * @param route The route to show
     * @return A string indicating the result of the showing (ToDo: remove or make sensible value).
     */
    public String showTrack(Route route)
    {
        MapRoute    mapRoute;
        String      returnString;
        
        mapRoute=new MapRoute();
        mapRoute.route=route;
        returnString=showTrack(mapRoute);
        return returnString;
    }
    
    
    /**
     * This method generates the map image showing the route
     * @param mapRoute Route to show
     * @return The map image or null if not succeeded
     */
    private void generateMapImage(MapRoute mapRoute)
    {
        String          trackString;
        String          mapString;
        
        try
        {
            getMapString(mapRoute);
            
            if (mapRoute.mapString!=null)
            {
                trackString=mapRoute.mapString+"&"; 
                trackString+=compressAndConvertTrack(mapRoute.route, MAXSTRINGLENGTH-trackString.length());
                DebugLogger.debug("Google URL: "+trackString+" Length: "+ trackString.length());        
                mapRoute.image                   = ImageIO.read(new URL(trackString));
                if (mapRoute.image==null)
                {
                    mapRoute.mapString="No map obtained from Google maps service";
                }
            }
        }
        catch (Exception e)
        {
            mapRoute.imageRemark="Unable to get Google map: "+e.getMessage(); 
            DebugLogger.error(mapRoute.imageRemark); 
        }
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
