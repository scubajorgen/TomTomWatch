/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
/**
 *
 * @author jorgen
 */
public class WorkoutListTemplate
{
    private class StepTemplate
    {
        public String   name;
        public String   description;
        public int      intensityDuration=-1;
        public int      intensityDistance=-1;
        public String   intensityHrZone;
    }
    
    private class WorkoutTemplate
    {
        
    }
    
    private String                          listTitle;
    private String                          listDescription;
    
    private final List<WorkoutTemplate>     workouts;
    
    public WorkoutListTemplate()
    {
        workouts=new ArrayList<>();
    }
}
