/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Class represents a route segment (&lt;trkSeg&gt; in the GPX file)
 * @author Jorgen
 */
public class RouteSegment
{
    private ArrayList<RoutePoint>       points;


    /**
     * Constructor. Creates the segment.
     */
    public RouteSegment()
    {
        points=new ArrayList();
    }
    
    /**
     * Returns the number of points/coordinates in this segment
     * @return The number of points/coordinates
     */
    public int getNumberOfPoints()
    {
        return points.size();
    }
    
    /**
     * Returns the point/coordinate at given index
     * @param index The index
     * @return The point at the index, or null if a non existing point is requested
     */
    public RoutePoint getPoint(int index)
    {
        RoutePoint point;
        
        point=null;
        
        if ((index>=0) && (index<points.size()))
        {
            point=points.get(index);
        }
        return point;
    }
    
    /**
     * Append a RoutePoint to the segment
     * @param routePoint The point to append
     */
    public void appendRoutePoint(RoutePoint routePoint)
    {
        points.add(routePoint);
    }
    
    /**
     * Get the RoutePoints in an ArrayList
     * @return The array list containing the routepoints
     */
    public ArrayList<RoutePoint> getRoutePoints()
    {
        return this.points;
    }
    
    public RoutePoint getFirstPoint()
    {
        RoutePoint point;
        
        if (points.size()>0)
        {
            point=points.get(0);
        }
        else
        {
            point=null;
        }
        return point;
    }
    
    public RoutePoint getLastPoint()
    {
        RoutePoint point;
        
        if (points.size()>0)
        {
            point=points.get(points.size()-1);
        }
        else
        {
            point=null;
        }
        return point;
    }
    
    /**
     * This method returns the distance traveled in the segment
     * @return The distance in meters
     */
    public double getSegmentDistance()
    {
        double                  distance;
        double                  radius;
        double                  lat1, lat2, lon1, lon2;
        double                  dLat, dLon, c, a;
        Iterator<RoutePoint>    iterator;
        RoutePoint              point;
        RoutePoint              prevPoint;
        
        distance=0.0;
        radius=6371000;
        
        iterator=points.iterator();
        
        if (iterator.hasNext())
        {
            prevPoint=iterator.next();

            while (iterator.hasNext())
            {
                point=iterator.next();

                lat1=2*Math.PI*prevPoint.getLatitude()/360.0;
                lon1=2*Math.PI*prevPoint.getLongitude()/360.0;

                lat2=2*Math.PI*point.getLatitude()/360.0;
                lon2=2*Math.PI*point.getLongitude()/360.0;

/*
                // Spherical law of cosines
                distance+= Math.acos(Math.sin(lat1)*Math.sin(lat2) + 
                           Math.cos(lat1)*Math.cos(lat2) *
                           Math.cos(lon2-lon1)) * radius;
*/

                
                // Haversine method
                dLat = lat2-lat1;
                dLon = lon2-lon1;

                a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
                c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
                distance += radius * c;                
                
                prevPoint=point;
            }
        }
        
        return distance;
    }

}
