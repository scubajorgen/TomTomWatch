/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;

import net.studioblueplanet.tomtomwatch.WorkoutStep.HrZone;
import net.studioblueplanet.tomtomwatch.WorkoutStep.ExtentType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.IntensityType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.StepType;

/**
 *
 * @author jorgen
 */
public class WorkoutStepTest
{
    private WorkoutStep theInstance;
    
    public WorkoutStepTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
        theInstance=new WorkoutStep(5, "Name", "Description", WorkoutStep.StepType.REST);
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getName method, of class WorkoutStep.
     */
    @Test
    public void testGetName()
    {
        System.out.println("getName");
        String expResult = "Name";
        String result = theInstance.getName();
        assertEquals(expResult, result);

    }

    /**
     * Test of getDescription method, of class WorkoutStep.
     */
    @Test
    public void testGetDescription()
    {
        System.out.println("getDescription");
        String expResult = "Description";
        String result = theInstance.getDescription();
        assertEquals(expResult, result);
    }

    /**
     * Test of getType method, of class WorkoutStep.
     */
    @Test
    public void testGetType()
    {
        System.out.println("getType");
        StepType expResult = StepType.REST;
        StepType result = theInstance.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of set and getExtentDistance method, of class WorkoutStep.
     */
    @Test
    public void testSetGetExtentDistance()
    {
        System.out.println("setGetExtentDistance");
  
        ExtentType expType=ExtentType.NONE;
        ExtentType type=theInstance.getStepExtent();
        assertEquals(expType, type);
        
        int expResult = 500;
        theInstance.setExtentDistance(expResult);
        int distance=theInstance.getExtentDistance();
        assertEquals(expResult, distance);
        expType=ExtentType.DISTANCE;
        type=theInstance.getStepExtent();
        assertEquals(expType, type);
    }

    /**
     * Test of setExtentReachHrZone and getExtentReachHrZone method, of class WorkoutStep.
     */
    @Test
    public void testSetGetExtentReachHrZone()
    {
        System.out.println("setGetExtentReachHrZone");
  
        ExtentType expType=ExtentType.NONE;
        ExtentType type=theInstance.getStepExtent();
        assertEquals(expType, type);
        
        HrZone expResult = HrZone.FATBURN;
        theInstance.setExtentReachHrZone(expResult);
        HrZone zone=theInstance.getExtentReachHrZone();
        assertEquals(expResult, zone);
        expType=ExtentType.REACHHRZONE;
        type=theInstance.getStepExtent();
        assertEquals(expType, type);
    }

    /**
     * Test of setExtentManual and getExtentManual method, of class WorkoutStep.
     */
    @Test
    public void testSetGetExtentManual()
    {
        System.out.println("setGetExtentManual");

        ExtentType expType=ExtentType.NONE;
        ExtentType type=theInstance.getStepExtent();
        assertEquals(expType, type);

        theInstance.setExtentManual();
        HrZone zone=theInstance.getExtentReachHrZone();
        assertEquals(HrZone.NONE, theInstance.getExtentReachHrZone());
        assertEquals(-1, theInstance.getExtentDistance());
        assertEquals(-1, theInstance.getExtentTime());
        expType=ExtentType.MANUAL;
        type=theInstance.getStepExtent();
        assertEquals(expType, type);
    }

    /**
     * Test of setExtentTime and getExtentTime method, of class WorkoutStep.
     */
    @Test
    public void testSetGetExtentTime()
    {
        System.out.println("setGetExtentTime");
  
        ExtentType expType=ExtentType.NONE;
        ExtentType type=theInstance.getStepExtent();
        assertEquals(expType, type);
        
        int expResult = 213;
        theInstance.setExtentDuration(expResult);
        int duration=theInstance.getExtentTime();
        assertEquals(expResult, duration);
        expType=ExtentType.TIME;
        type=theInstance.getStepExtent();
        assertEquals(expType, type);
    }

    /**
     * Test of getIntensity, setIntensityPace and setIntensityPace  method, 
     * of class WorkoutStep.
     */
    @Test
    public void testSetGetIntensityPace()
    {
        System.out.println("setIntensityPace, setIntensityPace, getIntensity");
        int pace = 123;

        theInstance.setIntensityPace(pace);
        
        IntensityType expected=IntensityType.PACE;
        IntensityType result=theInstance.getIntensity();
        assertEquals(expected, result);
        
        assertEquals(pace, theInstance.getIntensityPace());
        assertEquals(-1, theInstance.getIntensitySpeed());
        assertEquals(HrZone.NONE, theInstance.getIntensityHrZone());
    }

    /**
     * Test of getIntensity, getIntensitySpeed, setIntensitySpeed method, of class WorkoutStep.
     */
    @Test
    public void testSetGetIntensitySpeed()
    {
        System.out.println("setIntensitySpeed, setIntensitySpeed, getIntensity");
        int speed = 10000;

        theInstance.setIntensitySpeed(speed);
        
        IntensityType expected=IntensityType.SPEED;
        IntensityType result=theInstance.getIntensity();
        assertEquals(expected, result);
        
        assertEquals(speed, theInstance.getIntensitySpeed());
        assertEquals(-1, theInstance.getIntensityPace());
        assertEquals(HrZone.NONE, theInstance.getIntensityHrZone());
    }

    /**
     * Test of getIntensity, setIntensityHrZone and getIntensityHrZone method, 
     * of class WorkoutStep.
     */
    @Test
    public void testSetGetIntensityHrZone()
    {
        System.out.println("setIntensityHrZone, setIntensityHrZone, getIntensity");
        HrZone zone = HrZone.CARDIO;

        theInstance.setIntensityHrZone(zone);
        
        IntensityType expected=IntensityType.HRZONE;
        IntensityType result=theInstance.getIntensity();
        assertEquals(expected, result);
        
        assertEquals(zone, theInstance.getIntensityHrZone());
        assertEquals(-1, theInstance.getIntensityPace());
        assertEquals(-1, theInstance.getIntensitySpeed());
    }
    /**
     * Test of toString method, of class WorkoutStep.
     */
    @Test
    public void testToString()
    {
        System.out.println("toString");
        theInstance.setExtentDistance(100000);
        theInstance.setIntensityHrZone(HrZone.CARDIO);
        
        String expResult = String.format("    %02d: %-9s - %-9s - %-13s  100 m   @ Cardio\n        Description\n", 5, "Name", "Rest", "Distance");
        String result = theInstance.toString();
        assertEquals(expResult, result);
    }
    
}
