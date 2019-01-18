/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.JPanel;

import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.ActivitySegment;
import net.studioblueplanet.ttbin.ActivityRecord;
import net.studioblueplanet.ttbin.ActivityRecordGps;

/**
 *
 * @author jorgen
 */
public class MapOsm extends Map
{
    /**
     * Paints a route
     * @author Martin Steiger
     */
    public class RoutePainter implements Painter<JXMapViewer>
    {
        private Color color = Color.RED;
        private boolean antiAlias = true;

        private List<GeoPosition> track;

        /**
         * @param track the track
         */
        public RoutePainter(List<GeoPosition> track)
        {
            // copy the list so that changes in the 
            // original list do not have an effect here
            this.track = new ArrayList<GeoPosition>(track);
        }

        @Override
        public void paint(Graphics2D g, JXMapViewer map, int w, int h)
        {
            g = (Graphics2D) g.create();

            // convert from viewport to world bitmap
            Rectangle rect = map.getViewportBounds();
            g.translate(-rect.x, -rect.y);

            if (antiAlias)
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // do the drawing
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(4));

            drawRoute(g, map);

            // do the drawing again
            g.setColor(color);
            g.setStroke(new BasicStroke(2));

            drawRoute(g, map);

            g.dispose();
        }

        /**
         * @param g the graphics object
         * @param map the map
         */
        private void drawRoute(Graphics2D g, JXMapViewer map)
        {
            int lastX = 0;
            int lastY = 0;

            boolean first = true;

            for (GeoPosition gp : track)
            {
                // convert geo-coordinate to world bitmap pixel
                Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

                if (first)
                {
                    first = false;
                }
                else
                {
                    g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
                }

                lastX = (int) pt.getX();
                lastY = (int) pt.getY();
            }
        }
    }

    
    private JXMapViewer             mapViewer;    
    
    
    /**
     * Constructor
     * @param panel Panel to show the map on 
     */
    public MapOsm(JPanel panel)
    {
        super(panel);
        mapViewer = new JXMapViewer();

        // Create a TileFactoryInfo for OpenStreetMap
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        
        panel.add(mapViewer);
        
        hideTrack();
    }
            
    
    
    
    
    private void initializeOverlayPainter(List<GeoPosition> track, Set<Waypoint> waypoints)
    {
        RoutePainter                routePainter;
        WaypointPainter<Waypoint>   waypointPainter;
        
        // Create a waypoint painter that takes all the waypoints
        waypointPainter = new WaypointPainter<Waypoint>();
        waypointPainter.setWaypoints(waypoints);         

        
        routePainter    = new RoutePainter(track);        
        
        // Set the focus
        mapViewer.zoomToBestFit(new HashSet<GeoPosition>(track), 0.7);

        
        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);         
    }
    
    
   /**
     * This method show the track in this frame on a google map
     * @param activityData The activity data structure containing the track (Activity) to show
     * @return A string indicating the result of the showing (ToDo: remove or make sensible value).
     */
    public String showTrack(ActivityData activityData)
    {
        double                  lat;
        double                  lon;
        Activity                activity;
        List<ActivityRecord>    records;
        ActivityRecordGps       gpsRecord;
        int                     numOfSegments;
        int                     segment;
        int                     numOfRecords;
        int                     record;
        boolean                 firstWaypointShown;
        
        // Create a track from the geo-positions
        List<GeoPosition> track = new ArrayList<GeoPosition>();
        // Create waypoints from the geo-positions
        Set<Waypoint> waypoints = new HashSet<Waypoint>();

        firstWaypointShown      =false;
        lat                     =0.0;
        lon                     =0.0;

        activity=activityData.activity;
        numOfSegments=activity.getNumberOfSegments();
        segment=0;
        while (segment<numOfSegments)
        {
            firstWaypointShown=false;
            records=activity.getRecords(segment);
            numOfRecords=records.size();
            
            record=0;
            while (record<numOfRecords)
            {
                gpsRecord=(ActivityRecordGps)records.get(record);
                lat=gpsRecord.getLatitude();
                lon=gpsRecord.getLongitude();
                
                
                if ((lat!=0.0) && (lon!=0.0))
                {
                    track.add(new GeoPosition(lat, lon));
                    // Show first point of each segment
                    if (!firstWaypointShown)
                    {
                        waypoints.add(new DefaultWaypoint(lat, lon));
                        firstWaypointShown=true;
                    }
                }
                record++;
            }
            
            segment++;
        }     
        
        // Show last waypoint
        if (firstWaypointShown)
        {
            waypoints.add(new DefaultWaypoint(lat, lon));
        }
        
        this.initializeOverlayPainter(track, waypoints);
        
        return "";
    }

    /**
     * This method show the route track in this frame on a google map
     * @param route The route to show
     * @return A string indicating the result of the showing (ToDo: remove or make sensible value).
     */
    public String showTrack(Route route)
    {
        RouteSegment        segment;
        RoutePoint          point;
        int                 numberOfSegments;
        int                 segmentCount;
        int                 numberOfPoints;
        int                 pointCount;
        double              lat;
        double              lon;

        List<GeoPosition>   track;
        Set<Waypoint>       waypoints;
        
        // Create a track from the geo-positions
        track               = new ArrayList<GeoPosition>();
        // Create waypoints from the geo-positions
        waypoints           = new HashSet<Waypoint>();

        
        lat=0.0;
        lon=0.0;
        
        
        numberOfSegments    =route.getNumberOfSegments();
        segmentCount        =0;
        while (segmentCount<numberOfSegments)
        {
            segment=route.getSegment(segmentCount);
            
            numberOfPoints=segment.getNumberOfPoints();
            pointCount=0;
            while (pointCount<numberOfPoints)
            {
                point   =segment.getPoint(pointCount);
                lat     =point.getLatitude();
                lon     =point.getLongitude();
                track.add(new GeoPosition(lat, lon));
                
                if (pointCount==0)
                {
                    waypoints.add(new DefaultWaypoint(lat, lon));
                }
                pointCount++;
            }
            segmentCount++;
        }

        // last point of route
        waypoints.add(new DefaultWaypoint(lat, lon));
        
        this.initializeOverlayPainter(track, waypoints);
        
        return "";
    }

    /**
     * Hides the track
     */
    public void hideTrack()
    {
        mapViewer.setZoom(12);
        mapViewer.setAddressLocation(new GeoPosition(53.252, 6.588));          
        mapViewer.setOverlayPainter(null);
    }
}
