/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;


import java.util.ArrayList;
import java.util.Iterator;

import net.studioblueplanet.logger.DebugLogger;

/**
 * This class is a simple representation of route (track planning). 
 * A route consists of RouteSegments (GPX &lt;trkseg&gt;), 
 * which consists of RoutePoints (&lt;trkpt&gt;). In this representation a RoutePoint
 * only contains a lat/lon coordinate.
 * @author Jorgen
 */
public class Route
{
    protected ArrayList<RouteSegment>   segments;
    protected String                    routeName;
    
    /**
     * Private constructor
     */
    public Route()
    {
        segments=new ArrayList<>();
        clear();
    }
    
    
    /**
     * Clears the route log. Sets the route name to default
     */
    public void clear()
    {
        segments.clear();
        routeName="route";
    }

    /**
     * Sets the name of the route. The name will be visible on the watch.
     * It better be short
     * @param name The new name
     */
    public void setRouteName(String name)
    {
        this.routeName=name;
    }
    
    /**
     * This method returns the route name
     * @return The route name
     */
    public String getRouteName()
    {
        return this.routeName;
    }

    /**
     * Returns the number of segments
     * @return The number of segments
     */
    public int getNumberOfSegments()
    {
        return this.segments.size();
    }
    
    /**
     * Returns the array list with segments
     * @return List containing the route segment
     */
    public ArrayList<RouteSegment> getSegments()
    {
        return this.segments;
    }
    
    /**
     * Returns segment at given index
     * @param index Index
     * @return The segment or null if a non existing segment is requested
     */
    public RouteSegment getSegment(int index)
    {
        RouteSegment segment;
        
        segment=null;
        
        if ((index>=0) && (index<segments.size()))
        {
            segment=segments.get(index);
        }
            
        return segment;
    }
    
    /**
     * Returns the first segment of the route
     * @return The fist segment, or null if no segments present
     */
    public RouteSegment getFirstSegment()
    {
        RouteSegment segment;
        
        if (segments.size()>0)
        {
            segment=segments.get(0);
        }
        else
        {
            segment=null;
        }
        return segment;
    }
    
    /**
     * Returns the last segment of the route
     * @return The last segment, or null if no segments present
     */
    public RouteSegment getLastSegment()
    {
        RouteSegment segment;
        
        if (segments.size()>0)
        {
            segment=segments.get(segments.size()-1);
        }
        else
        {
            segment=null;
        }
        return segment;
    }

    /**
     * Creates and appends a new route segment
     * @return The newly created route segment 
     */
    
    public RouteSegment appendRouteSegment()
    {
        RouteSegment routeSegment;
        
        routeSegment=new RouteSegment();
        segments.add(routeSegment);
        
        return routeSegment;
    }

    
    /**
     * Returns the minimum value of the latitude in the log
     * @return THe minimum value
     */
    public int getMinLatitude()
    {
        Iterator<RouteSegment>  itSegment;
        Iterator<RoutePoint>    itPoint;
        int                     min;
        RoutePoint              point;
        RouteSegment            segment;
        
        min=1800000000;
        itSegment=segments.iterator();
        while (itSegment.hasNext())
        {
            segment=itSegment.next();
            itPoint=segment.getRoutePoints().iterator();
            while (itPoint.hasNext())
            {
                point=itPoint.next();
                if (point.getLatitudeInt()<min)
                {
                    min=point.getLatitudeInt();
                }
            }                   
        }
        return min;
    }
    
    /**
     * Returns the maximum value of the latitude in the log
     * @return THe maximum value
     */
    public int getMaxLatitude()
    {
        Iterator<RouteSegment>  itSegment;
        Iterator<RoutePoint>    itPoint;
        int                     max;
        RoutePoint              point;
        RouteSegment            segment;
        
        max=-1800000000;
        itSegment=segments.iterator();
        while (itSegment.hasNext())
        {
            segment=itSegment.next();
            itPoint=segment.getRoutePoints().iterator();
            while (itPoint.hasNext())
            {
                point=itPoint.next();
                if (point.getLatitudeInt()>max)
                {
                    max=point.getLatitudeInt();
                }
            }                   
        }
        return max;
    }
    
    /**
     * Returns the minimum value of the longitude in the log
     * @return THe minimum value
     */
    public int getMinLongitude()
    {
        Iterator<RouteSegment>  itSegment;
        Iterator<RoutePoint>    itPoint;
        int                     min;
        RoutePoint              point;
        RouteSegment            segment;
        
        min=1800000000;
        itSegment=segments.iterator();
        while (itSegment.hasNext())
        {
            segment=itSegment.next();
            itPoint=segment.getRoutePoints().iterator();
            while (itPoint.hasNext())
            {
                point=itPoint.next();
                if (point.getLongitudeInt()<min)
                {
                    min=point.getLongitudeInt();
                }
            }                   
        }
        return min;
    }
    
    /**
     * Returns the maximum value of the longitude in the log
     * @return THe maximum value
     */
    public int getMaxLongitude()
    {
        Iterator<RouteSegment>  itSegment;
        Iterator<RoutePoint>    itPoint;
        int                     max;
        RoutePoint              point;
        RouteSegment            segment;
        
        max=-1800000000;
        itSegment=segments.iterator();
        while (itSegment.hasNext())
        {
            segment=itSegment.next();
            itPoint=segment.getRoutePoints().iterator();
            while (itPoint.hasNext())
            {
                point=itPoint.next();
                if (point.getLongitudeInt()>max)
                {
                    max=point.getLongitudeInt();
                }
            }                   
        }
        return max;
    }
    
    /**
     * This method returns the distance of the route as the sum of segment
     * distances. This implicates the distance from one segment to another
     * is not incorporated (it is assumed that segments are contiguous).
     * @return The distance in m.
     */
    public double getDistance()
    {
        Iterator<RouteSegment>      it;
        RouteSegment                segment;
        double                      distance;
        
        it              =segments.iterator();
        distance        =0.0;
        while (it.hasNext())
        {
            segment=it.next();
            distance+=segment.getSegmentDistance();
        }
        return distance;
    }
    
    /**
     * Dumps the route to the debuglogger
     */
    public void dumpLog()
    {
        Iterator<RouteSegment>      itSegment;
        Iterator<RoutePoint>        itPoint;
        RouteSegment                segment;
        ArrayList<RoutePoint>       points;
        RoutePoint                  point;
        


        DebugLogger.debug("Route");
        
        itSegment=segments.iterator();
        
        while (itSegment.hasNext())
        {
            segment=itSegment.next();
            
            DebugLogger.debug("  Segment");
            points=segment.getRoutePoints();
            
            itPoint=points.iterator();
            while (itPoint.hasNext())
            {
                point=itPoint.next();
                DebugLogger.debug("    Lat: "+point.getLatitudeInt()+"Lon: "+point.getLongitudeInt());
            }
        }
    }


    /**
     * Debugging function. Writes bytes to bin file and stdout
     * @param bytes Bytes to print
     */
    private void dumpBytes(byte[] bytes, String fileName)
    {

        int i;
        i=0;
        while (i<bytes.length)
        {
            System.out.print(String.format("%02x ", bytes[i]));
            i++;
        }
        System.out.println();

        try
        {
          java.io.RandomAccessFile file=new java.io.RandomAccessFile(fileName, "rw");
          file.write(bytes);
          file.close();
        }
        catch (Exception e)
        {

        }
    }
    
    /**
     * Returns the number of points in the route
     * @return The number of points
     */
    protected int getNumberOfPoints()
    {
        Iterator<RouteSegment>  it;
        RouteSegment            segment;
        int                     numberOfPoints;
        
        numberOfPoints      =0;
        it=segments.iterator();
        while (it.hasNext())
        {
            segment         =it.next();
            numberOfPoints  +=segment.getNumberOfPoints();
        }
        return numberOfPoints;
    }
    
    
/*    
    public void test()
    {
        byte[] bytes;
        GpxReader reader=GpxReader.getInstance();

        clear();
        reader.readRouteFromFile("j:/files/java/TomTomWatch/development/reverse engineering/0x00b80001_test3punten-20170508.gpx");
        setRouteName("3-20170508");
        bytes=this.getTomTomRouteData();
        dumpBytes(bytes, "j:/files/java/TomTomWatch/development/reverse engineering/0x00b80001-c.bin");

        clear();
        reader.readRouteFromFile("j:/files/java/TomTomWatch/development/reverse engineering/0x00b80007_trail02_coastal.gpx");
        setRouteName("trail02_coastal");
        bytes=this.getTomTomRouteData();
        dumpBytes(bytes, "j:/files/java/TomTomWatch/development/reverse engineering/0x00b80007-c.bin");

        clear();
        reader.readRouteFromFile("j:/files/java/TomTomWatch/development/reverse engineering/0x00b8000d_4segmenten.gpx");
        setRouteName("0x00b8000d_4segmenten");
        bytes=this.getTomTomRouteData();
        dumpBytes(bytes, "j:/files/java/TomTomWatch/development/reverse engineering/0x00b8000d-c.bin");
        
        clear();
        reader.readRouteFromFile("j:/files/java/TomTomWatch/development/reverse engineering/0x00b80008_walk05_montanacuervo_4.0km.gpx");
        setRouteName("walk05_montanacuervo_4.0km");
        bytes=this.getTomTomRouteData();
        dumpBytes(bytes, "j:/files/java/TomTomWatch/development/reverse engineering/0x00b80008-c.bin");
        
        clear();
        reader.readRouteFromFile("j:/files/java/TomTomWatch/development/reverse engineering/0x00b80003_test3punten-20170508-invalidbounds.gpx");
        setRouteName("test3punten-20170508-invalidbounds");
        bytes=this.getTomTomRouteData();
        dumpBytes(bytes, "j:/files/java/TomTomWatch/development/reverse engineering/0x00b80003-c.bin");

    }
*/    
    
}
