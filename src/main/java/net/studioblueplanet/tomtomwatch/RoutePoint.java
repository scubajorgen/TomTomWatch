/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

/**
 *
 * @author Jorgen
 */
public class RoutePoint
{
    private double              latitude;
    private double              longitude;
    
    /**
     * Constructor. It takes the coordinate it represents
     * @param latitude Latitude of the coordinate in degrees
     * @param longitude Longitude of the coordinate in degrees
     */
    public RoutePoint(double latitude, double longitude)
    {
        this.latitude       =latitude;
        this.longitude     =longitude;
    }
    
    /**
     * Returns the latitude
     * @return The latitude*10^7 as integer
     */
    public int getLatitudeInt()
    {
        return (int)(latitude*1e7);
    }
    
    /**
     * Returns the longitude
     * @return The longitude*10^7 as integer
     */
    public int getLongitudeInt()
    {
        return (int)(longitude*1e7);
    }
    
    /**
     * Returns the latitude
     * @return The latitude in degrees [-180.0 .. 180.0]
     */
    public double getLatitude()
    {
        return this.latitude;
    }
    
    /**
     * Returns the longitude
     * @return The longitude in degrees [-180.0 .. 180.0]
     */
    public double getLongitude()
    {
        return this.longitude;
    }
  
}
