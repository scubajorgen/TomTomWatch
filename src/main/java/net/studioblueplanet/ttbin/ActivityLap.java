/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import hirondelle.date4j.DateTime;

/**
 * This class represents a manual lap mark as recorded by the watch (tag 0x2f).
 * The values are cumulative since the start of the activity.
 * @author jorgen
 */
public class ActivityLap
{
    private final int           elapsedSeconds;     // Total elapsed time in s
    private final double        distance;           // Total distance in m
    private final int           calories;           // Total calories
    private final DateTime      dateTime;           // UTC time of the last record before the lap mark

    /**
     * Constructor
     * @param elapsedSeconds Total elapsed time since start of the activity in s
     * @param distance Total distance since start of the activity in m
     * @param calories Total calories since start of the activity
     * @param dateTime UTC date time of the last record preceding the lap mark,
     *                 or null if no record preceded the lap mark
     */
    public ActivityLap(int elapsedSeconds, double distance, int calories, DateTime dateTime)
    {
        this.elapsedSeconds =elapsedSeconds;
        this.distance       =distance;
        this.calories       =calories;
        this.dateTime       =dateTime;
    }

    /**
     * Returns the total elapsed time at the lap mark
     * @return The time in seconds, cumulative since start of the activity
     */
    public int getElapsedSeconds()
    {
        return elapsedSeconds;
    }

    /**
     * Returns the total distance at the lap mark
     * @return The distance in m, cumulative since start of the activity
     */
    public double getDistance()
    {
        return distance;
    }

    /**
     * Returns the total calories at the lap mark
     * @return The calories, cumulative since start of the activity
     */
    public int getCalories()
    {
        return calories;
    }

    /**
     * Returns the date time of the lap mark. It is the date time of the last
     * record (GPS or heart rate) preceding the lap mark in the ttbin file.
     * @return The date time as UTC or null if not available
     */
    public DateTime getDateTime()
    {
        return dateTime;
    }
}
