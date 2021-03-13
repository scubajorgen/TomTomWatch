/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.generics;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import net.studioblueplanet.ttbin.ActivityRecord;

/**
 * Implementation of the Douglas-Peucker algorithm.
 * @author https://programmersought.com/article/54684929685/
 */
public class DPUtil
{

    /**
     * The default error threshold of the maximum distance between the point and
     * the track line (unit: meter)
     */
    private static final double defaultDMax = 3.0;

    /**
     * DP algorithm entry Pass in the set of track points before compression
     * Output the compressed result track point collection
     *
     * @param originPoints The collection of track points before compression
     * @param dMax Maximum distance error threshold from point to track
     * @return The compressed and sorted list of GPS coordinates
     */
    public static List<ActivityRecord> dpAlgorithm(List<ActivityRecord> originPoints, Double dMax)
    {
        List<ActivityRecord> resultPoints = new ArrayList<>();
        resultPoints.add(originPoints.get(0));//Get the coordinates of the first original latitude and longitude point and add it to the filtered array
        resultPoints.add(originPoints.get(originPoints.size() - 1));//Get the last original latitude and longitude point coordinates and add them to the filtered array
        //Maximum distance error threshold
        if (dMax == null)
        {
            dMax = defaultDMax;
        }
        int start = 0;
        int end   = originPoints.size() - 1;
        compression(originPoints, resultPoints, start, end, dMax);
        
        Collections.sort(resultPoints); // sort points on datetime
        
        return resultPoints;
    }

    /**
     * Function: According to the maximum distance limit, the original
     * trajectory is recursively sampled by the DP method to obtain the
     * compressed trajectory
     *
     * @param originPoints: the original latitude and longitude coordinate point
     * array
     * @param resultPoints: keep the filtered point coordinate array
     * @param start: starting subscript
     * @param end: end subscript
     * @param dMax: pre-specified maximum distance error calculation
     */
    public static void compression(List<ActivityRecord> originPoints, List<ActivityRecord> resultPoints,
            int start, int end, double dMax)
    {
        if (start < end)
        {//recursive conditions
            double maxDist = 0;//Maximum distance
            int cur_pt = 0;//Current subscript
            for (int i = start + 1; i < end; i++)
            {
                //The distance from the current point to the corresponding line segment
                double curDist = distToSegment(originPoints.get(start), originPoints.get(end), originPoints.get(i));
                if (curDist > maxDist)
                {
                    maxDist = curDist;
                    cur_pt = i;
                }//Find the maximum distance and the subscript of the corresponding point of the maximum distance
            }
            //If the current maximum distance is greater than the maximum distance error
            if (maxDist >= dMax)
            {
                resultPoints.add(originPoints.get(cur_pt));//Add the current point to the filter array
                //Divide the original line segment into two segments with the current point as the center, and perform recursive processing respectively
                compression(originPoints, resultPoints, start, cur_pt, dMax);
                compression(originPoints, resultPoints, cur_pt, end, dMax);
            }
        }
    }

    /**
     * Function: use the triangle area (calculated by Helen's formula) equal
     * method to calculate the distance from point pX to the straight line
     * determined by points pA and pB
     *
     * @param pA: starting point
     * @param pB: end point
     * @param pX: The third point
     * @return distance: the distance from point pX to the line where pA and pB
     * are located
     */
    public static double distToSegment(ActivityRecord pA, ActivityRecord pB, ActivityRecord pX)
    {
        double a = Math.abs(geoDist(pA, pB));
        double b = Math.abs(geoDist(pA, pX));
        double c = Math.abs(geoDist(pB, pX));
        double p = (a + b + c) / 2.0;
        double s = Math.sqrt(Math.abs(p * (p - a) * (p - b) * (p - c)));
        double d = s * 2.0 / a;
        return d;
    }

    /**
     * Function: Find the distance between two latitude and longitude points
     * according to mathematical formulas
     * @param pA: starting point
     * @param pB: end point
     * @return distance: distance in m
     */
    public static double geoDist(ActivityRecord pA, ActivityRecord pB)
    {
        double radLat1 = Rad((pA).getLatitude());
        double radLat2 = Rad((pB).getLatitude());
        double radLon1 = Rad((pA).getLongitude());
        double radLon2 = Rad((pB).getLongitude());
        double delta_lon = Rad(radLon2 - radLon1);
        double top_1 = Math.cos(radLat2) * Math.sin(delta_lon);
        double top_2 = Math.cos(radLat1) * Math.sin(radLat2) - Math.sin(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
        double top = Math.sqrt(top_1 * top_1 + top_2 * top_2);
        double bottom = Math.sin(radLat1) * Math.sin(radLat2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
        double delta_sigma = Math.atan2(top, bottom);
        double distance = delta_sigma * 6378137.0;
        return distance;
    }

    /**
     * Function: angle to radians
     *
     * @param d: angle
     * @return returns radians
     */
    public static double Rad(double d)
    {
        return d * Math.PI / 180.0;
    }
    
}
