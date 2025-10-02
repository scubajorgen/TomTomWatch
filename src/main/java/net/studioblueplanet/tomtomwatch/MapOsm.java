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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.ActivityRecord;
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
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.WaypointRenderer;

/**
 *
 * @author jorgen
 */
public class MapOsm extends Map
{
    /**
     * Inner class representing a waypoint on the map
     */
    private class OsmWaypoint implements Waypoint 
    {
        private GeoPosition position;
        private String      name;
        private int         symbol;
        private boolean     focussed;

        /** 
         * Creates a new instance of Waypoint 
         */
        public OsmWaypoint()
        {
            this(new GeoPosition(0, 0));
            focussed=false;
        }

        /**
         * @param latitude the latitude
         * @param longitude the longitude
         */
        public OsmWaypoint(double latitude, double longitude, String name, int symbol)
        {
            this(new GeoPosition(latitude, longitude));
            this.name   =name;
            this.symbol =symbol;
        }

        /**
         * @param coord the geo coordinate
         */
        public OsmWaypoint(GeoPosition coord)
        {
            this.position = coord;
        }

        @Override
        public GeoPosition getPosition()
        {
            return position;
        }
        
        public String getName()
        {
            return name;                  
        }
        
        public int getSymbol()
        {
            return symbol;
        }

        public boolean isFocussed()
        {
            return focussed;
        }

        public void setFocussed(boolean focussed)
        {
            this.focussed = focussed;
        }
    }    

    /**
     * Inner class representing a track segment to display
     */
    private class OsmTrackSegment
    {
        private     List<GeoPosition> segment;
        private     double leftBound;
        private     double rightBound;
        private     double upperBound;
        private     double lowerBound;
        
        public OsmTrackSegment()
        {
            segment=new ArrayList<GeoPosition>();
            leftBound   =180.0;
            rightBound  =-180.0;
            lowerBound  =180.0;
            upperBound  =-180.0;
        }
        
        public void add(GeoPosition position)
        {
            double lat;
            double lon;
            
            segment.add(position);
            
            lat=position.getLatitude();
            lon=position.getLongitude();
            if (lat<lowerBound)
            {
                lowerBound=lat;
            }
            if (lat>upperBound)
            {
                upperBound=lat;
            }
            if (lon>rightBound)
            {
                rightBound=lon;
            }
            if (lon<leftBound)
            {
                leftBound=lon;
            }
        }
        
        public List<GeoPosition> getPositions()
        {
            return segment;
        }
        
        public int size()
        {
            return segment.size();
        }
        
        public double getLeftBound()
        {
            return leftBound;
        }
        
        public double getRightBound()
        {
            return rightBound;
        }
        
        public double getUpperBound()
        {
            return upperBound;
        }

        public double getLowerBound()
        {
            return lowerBound;
        }
    }

    
    /**
     * Inner class representing a track to display
     */
    private class OsmTrack
    {
        private final List<OsmTrackSegment>    track;
        private final Set<OsmWaypoint>  waypoints;
        
        public OsmTrack()
        {
            track=new ArrayList<OsmTrackSegment>();
            // Create waypoints from the geo-positions
            waypoints      = new HashSet<>();
        }
        
        public void add(OsmTrackSegment segment)
        {
            track.add(segment);
        }
        
        public void add(OsmWaypoint waypoint)
        {
            waypoints.add(waypoint);
        }
        
        public List<OsmTrackSegment> getSegments()
        {
            return track;
        }

        public Set<OsmWaypoint> getWaypoints()
        {
            return waypoints;
        }

        public int size()
        {
            return track.size();
        }

        public int waypointSize()
        {
            return waypoints.size();
        }
        
        /**
         * Return the bounds of the track as a list of two coordinates: 
         * upper left and lower right
         * @return The bounds 
         */
        public List<GeoPosition> getBounds()
        {
            double leftBound;
            double rightBound;
            double upperBound;
            double lowerBound;      
            
            leftBound   =180.0;
            rightBound  =-180.0;
            lowerBound  =180.0;
            upperBound  =-180.0;
            
            ArrayList<GeoPosition> bounds;
            
            bounds=new ArrayList<GeoPosition>();
            
            for (OsmTrackSegment segment : track)
            {
                if (segment.getLeftBound()<leftBound)
                {
                    leftBound=segment.getLeftBound();
                }
                if (segment.getRightBound()>rightBound)
                {
                    rightBound=segment.getRightBound();
                }
                if (segment.getUpperBound()>upperBound)
                {
                    upperBound=segment.getUpperBound();
                }
                if (segment.getLowerBound()<lowerBound)
                {
                    lowerBound=segment.getLowerBound();
                }
                bounds.add(new GeoPosition(upperBound, leftBound));
                bounds.add(new GeoPosition(lowerBound, rightBound));
            }

            for (Waypoint waypoint : waypoints)
            {
                // Add a small bounding box around the waypoint, to compensate for
                // the situation in which there is 1 waypoint
                GeoPosition wpt=waypoint.getPosition();
                GeoPosition wptNW=new GeoPosition(wpt.getLatitude()-0.0001,wpt.getLongitude()-0.0001);
                GeoPosition wptSE=new GeoPosition(wpt.getLatitude()+0.0001,wpt.getLongitude()+0.0001);
                bounds.add(wptNW);
                bounds.add(wptSE);
            }
            return bounds;
        }
    }
    
    /**
     * Paints a track
     * @author Martin Steiger, Jörgen 
     */
    private class RoutePainter implements Painter<JXMapViewer>
    {
        private final Color     color = Color.RED;
        private final boolean   antiAlias = true;

        private final OsmTrack  track;

        /**
         * @param track the track
         */
        public RoutePainter(OsmTrack track)
        {
            this.track = track;
        }

        @Override
        public void paint(Graphics2D g, JXMapViewer map, int w, int h)
        {
            g = (Graphics2D) g.create();

            // convert from viewport to world bitmap
            Rectangle rect = map.getViewportBounds();
            g.translate(-rect.x, -rect.y);

            if (antiAlias)
            {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            
            // do the drawing
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(4));

            drawRoute(g, map, false);

            // do the drawing again
            g.setStroke(new BasicStroke(2));

            drawRoute(g, map, true);

            g.dispose();
        }

        /**
         * @param g the graphics object
         * @param map the map
         */
        private void drawRoute(Graphics2D g, JXMapViewer map, boolean useColor)
        {
            int     lastX;
            int     lastY;
            int     segmentCount;

            boolean first;
            
            segmentCount=0;
            for (OsmTrackSegment segment : track.getSegments())
            {
                first   =true;
                lastX   =0;
                lastY   =0;

                // Choose color per segment
                if (useColor)
                {
                    if (segmentCount%2>0)
                    {
                        g.setColor(Color.BLUE);
                    }
                    else
                    {
                        g.setColor(Color.RED);
                    }
                }
                for (GeoPosition gp : segment.getPositions())
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
                segmentCount++;
            }
        }
    }

    /**
     * Inner class that renders waypoints
     */
    private class OsmWaypointRenderer implements WaypointRenderer<OsmWaypoint>
    {
        private BufferedImage imgWpt    = null;
        private BufferedImage imgStart  = null;
        private BufferedImage imgFinish = null;

        /**
         * Uses a default waypoint image
         */
        public OsmWaypointRenderer()
        {
            try
            {
                imgStart    = ImageIO.read(OsmWaypointRenderer.class.getResource("/images/x1.png"));
                imgFinish   = ImageIO.read(OsmWaypointRenderer.class.getResource("/images/x2.png"));
                imgWpt      = ImageIO.read(OsmWaypointRenderer.class.getResource("/images/PurpleDot.png"));
            }
            catch (Exception ex)
            {
            }
        }

        @Override
        public void paintWaypoint(Graphics2D g, JXMapViewer map, OsmWaypoint w)
        {
            BufferedImage img;
            
            int symbol=((OsmWaypoint)w).getSymbol();
            
            if (symbol==254)
            {
                img=imgStart;
            }
            else if (symbol==255)
            {
                img=imgFinish;
            }
            else
            {
                img=imgWpt;
            }
            Point2D point = map.getTileFactory().geoToPixel(w.getPosition(), map.getZoom());

            if(img!=null)
            {
                int x = (int)point.getX() -img.getWidth()/2;
                int y = (int)point.getY() -img.getHeight()/2;
                g.drawImage(img, x, y, null);
                if (((OsmWaypoint)w).isFocussed())
                {
                    g.drawString(((OsmWaypoint)w).getName(), x, y);
                }
            }
        }        
    }
    
    /**
     * Inner class that listens to mouse movement. It focusses the waypoint 
     * the mouse is moving over
     */
    private class MouseMovedListener extends MouseAdapter 
    {
        private static final int    AREA=5;
        private final OsmTrack      track;
        
        public MouseMovedListener(OsmTrack track)
        {
            this.track=track;
        }
        /**
         * Gets called on mouseClicked events, calculates the GeoPosition and fires
         * the mapClicked method that the extending class needs to implement.
         * 
         * @param evt the mouse event
         */
        @Override
        public void mouseMoved(MouseEvent evt) 
        {
            Rectangle bounds = mapViewer.getViewportBounds();
            int x = bounds.x + evt.getX();
            int y = bounds.y + evt.getY();

            Set<OsmWaypoint> points=track.getWaypoints();
            
            boolean found       =false;
            for(OsmWaypoint point : points)
            {
                GeoPosition position=point.getPosition();
                Point2D xy          =mapViewer.getTileFactory().geoToPixel(position, mapViewer.getZoom());
                if (xy.getX()-x<AREA && xy.getX()-x>-AREA && 
                    xy.getY()-y<AREA && xy.getY()-y>-AREA)
                {
                    if (!found)
                    {
                        // Only focus on the 1st in the list when overlapping waypoints
                        point.setFocussed(true);
                        found=true;
                    }
                    else
                    {
                        point.setFocussed(false);
                    }
                    
                }
                else
                {
                    point.setFocussed(false);
                }
            }
            evt.getComponent().repaint();
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
        TileFactoryInfo info = new OSMTileFactoryInfo("OpenStreetMap", "https://tile.openstreetmap.org");
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        
        
        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));        
        panel.add(mapViewer);
        hideTrack();
    }
 
    
    private void initializeOverlayPainter(OsmTrack track, boolean fitTrack)
    {
        // Create a waypoint painter that takes all the waypoints
        WaypointPainter<OsmWaypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(track.getWaypoints());
        waypointPainter.setRenderer(new OsmWaypointRenderer());

        RoutePainter routePainter    = new RoutePainter(track);
        // Set the focus
        if (fitTrack)
        {
            List<GeoPosition> bounds=track.getBounds();
            if (bounds.size()>1)
            {
                mapViewer.zoomToBestFit(new HashSet<>(bounds), 0.9);
            }
        }
        
        // Set the focus
        mapViewer.zoomToBestFit(new HashSet<GeoPosition>(track.getBounds()), 0.9);
        
        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);
        mapViewer.addMouseMotionListener(new MouseMovedListener(track));        
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
        ActivityRecord          gpsRecord;
        int                     numOfSegments;
        int                     segment;
        int                     numOfRecords;
        int                     record;
        boolean                 firstWaypointShown;
        OsmTrack                track;
        OsmTrackSegment         trackSegment;
        int                     points;
        
        // Create a track from the geo-positions
        track                   =new OsmTrack();

        firstWaypointShown      =false;
        lat                     =0.0;
        lon                     =0.0;
        points                  =0;

        activity=activityData.activity;
        numOfSegments=activity.getNumberOfSegments();
        segment=0;
        firstWaypointShown  =false;
        while (segment<numOfSegments)
        {
            records=activity.getRecords(segment);
            numOfRecords        =records.size();
            trackSegment        =new OsmTrackSegment();
            record=0;
            while (record<numOfRecords)
            {
                gpsRecord=records.get(record);
                lat=gpsRecord.getLatitude();
                lon=gpsRecord.getLongitude();
                
                // Sometime the tomtom registers a (0,0) coordinate
                // Only show normal, non zero, coordinates
                if ((lat!=0.0) && (lon!=0.0))
                {
                    trackSegment.add(new GeoPosition(lat, lon));
                    // Show first point of each segment
                    if (!firstWaypointShown)
                    {
                        track.add(new OsmWaypoint(lat, lon, "Start", 254));
                        firstWaypointShown=true;
                    }
                    points++;
                }
                record++;
            }
            
            track.add(trackSegment);
            segment++;
        }     
        
        // Show last waypoint
        if (firstWaypointShown)
        {
//            waypoints.add(new DefaultWaypoint(lat, lon));
            track.add(new OsmWaypoint(lat, lon, "Finish", 255));
        }

        for (ActivityRecord point : activityData.activity.getWaypoints())
        {
            track.add(new OsmWaypoint(point.getLatitude(), point.getLongitude(), "waypoint", 1));
        }
        
        // No points = show default image
        if (points==0)
        {
            this.hideTrack();
        }
        else
        {
            this.initializeOverlayPainter(track, false);
        }
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
        OsmTrack            track;
        OsmTrackSegment     trackSegment;        
        Set<Waypoint>       waypoints;
        boolean             firstWaypointShown;
        int                 points;
        
        // Create a track from the geo-positions
        track               = new OsmTrack();
        // Create waypoints from the geo-positions
        waypoints           = new HashSet<Waypoint>();

        
        lat                 =0.0;
        lon                 =0.0;
        firstWaypointShown  =false;
        points              =0;
        
        numberOfSegments    =route.getNumberOfSegments();
        segmentCount        =0;
        while (segmentCount<numberOfSegments)
        {
            segment         =route.getSegment(segmentCount);
            trackSegment    =new OsmTrackSegment();
            
            numberOfPoints=segment.getNumberOfPoints();
            pointCount=0;
            while (pointCount<numberOfPoints)
            {
                point   =segment.getPoint(pointCount);
                lat     =point.getLatitude();
                lon     =point.getLongitude();
                trackSegment.add(new GeoPosition(lat, lon));
                points++;
                
                if (!firstWaypointShown)
                {
                    waypoints.add(new DefaultWaypoint(lat, lon));
                    firstWaypointShown=true;
                }
                pointCount++;
            }
            track.add(trackSegment);
            segmentCount++;
        }

        // last point of route
        waypoints.add(new DefaultWaypoint(lat, lon));

        // No points = show default image
        if (points==0)
        {
            this.hideTrack();
        }
        else
        {
            this.initializeOverlayPainter(track, false);
        }
        
        return "";
    }

    /**
     * Hides the track
     */
    public void hideTrack()
    {
        mapViewer.setZoom(15);
        mapViewer.setAddressLocation(new GeoPosition(53.252, 6.588));          
        mapViewer.setOverlayPainter(null);
    }
}
