/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.ttbin;


/**
 * This class represents a coordinate
 * @author UserXP
 */
public class Coordinate
{
    /** Latitude in 10E-7 degrees */
    protected int latitude;

    /** Longitude in 10E-7 degrees */
    protected int longitude;

    /** Elevation in cm */
    protected int elevation;

    /**
     * Constructor. Constructs the coordinate and intialises it
     * @param longitude Longitude in 10-7 degrees
     * @param latitude Latitude in 10-7 degrees
     * @param elevation Elevation in cm
     */
    public Coordinate(int longitude, int latitude, int elevation)
    {
        this.latitude=latitude;
        this.longitude=longitude;
        this.elevation=elevation;
    }

    public Coordinate()
    {
        this.latitude   =0;
        this.longitude  =0;
        this.elevation  =0;
    }

    /**
     * This method returns the latitude in degrees.
     * @return The latitude
     */
    public double getLatitude()
    {
        return (double)latitude/1e7;
    }

    /**
     * This method returns the longitude in degrees.
     * @return The longitude
     */
    public double getLongitude()
    {
        return (double)longitude/1e7;
    }

    /**
     * This method returns the elvation in m
     * @return The elevation in m
     */
    public double getElevation()
    {
        return (double)elevation/100.0;
    }


}
