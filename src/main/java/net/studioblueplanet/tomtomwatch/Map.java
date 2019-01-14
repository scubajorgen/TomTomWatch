/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.ActivityRecord;
import net.studioblueplanet.ttbin.ActivityRecordGps;
import net.studioblueplanet.ttbin.ActivitySegment;

/**
 * This class represents the map function, showing a map with a route.
 * @author jorgen
 */
public abstract class Map 
{
    protected class MapRoute
    {
        Route           route;
        BufferedImage   image;
        String          imageRemark;
    }

    
    protected final JPanel              panel;
    protected final JLabel              label;
    protected final int                 panelWidth;
    protected final int                 panelHeight;
    
    
    
    public Map(JPanel panel)
    {
        this.panel  =panel;
        label       = new JLabel();
        panel.add(label);     
        panelWidth  =panel.getWidth();
        panelHeight =panel.getHeight();
        
    }
    
    /**
     * Shows a previously cached map image
     * @param image The image to show
     */
    protected void showTrackImage(BufferedImage image)
    {
        ImageIcon       imageIcon;
        
        imageIcon=new ImageIcon(image);     
        label.setIcon(imageIcon);
        panel.setVisible(true);
    }
    
    

    /**
     * This method generates the map image showing the route. The image is 
     * added to the mapRoute structure.
     * @param mapRoute Route to show
     */
    protected abstract void generateMapImage(MapRoute mapRoute);


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
     * @param route The route to show
     * @return A string indicating the result of the showing (ToDo: remove or make sensible value).
     */
    public String showTrack(Route route)
    {
        BufferedImage   image;      
        MapRoute        mapRoute;

        mapRoute    =new MapRoute();
        mapRoute.route=route;
        generateMapImage(mapRoute);
        
        if (mapRoute.image!=null)
        {
            showTrackImage(mapRoute.image);
        }
        return "Ok";
    }
    
    
    /**
     * Hides the track
     */
    public void hideTrack()
    {
        this.label.setIcon(null);
    }    
    
}
