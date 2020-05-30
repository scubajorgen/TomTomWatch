/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

/**
 *
 * @author jorgen
 */
public class WorkoutStep
{
    enum WorkoutStepType 
    {
        DISTANCE,
        DURATION,
        REACHHR
    }
    private WorkoutStepType     type;
    private int                 distance;   // In mm
    private int                 duration;   // In sec
    
}
