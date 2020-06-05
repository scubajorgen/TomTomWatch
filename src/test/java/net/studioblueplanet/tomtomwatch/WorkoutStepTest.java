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
        theInstance=new WorkoutStep(5, "Name", "Description", WorkoutStep.WorkoutStepType.REST);
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
        WorkoutStep.WorkoutStepType expResult = WorkoutStep.WorkoutStepType.REST;
        WorkoutStep.WorkoutStepType result = theInstance.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of setGerExtentDistance method, of class WorkoutStep.
     */
    @Test
    public void testSetGetExtentDistance()
    {
        System.out.println("setGetExtentDistance");
  
        WorkoutStep.WorkoutStepExtentType expType=WorkoutStep.WorkoutStepExtentType.NONE;
        WorkoutStep.WorkoutStepExtentType type=theInstance.getStepExtent();
        assertEquals(expType, type);
        
        int expResult = 500;
        theInstance.setExtentDistance(expResult);
        int distance=theInstance.getExtentDistance();
        assertEquals(expResult, distance);
        expType=WorkoutStep.WorkoutStepExtentType.DISTANCE;
        type=theInstance.getStepExtent();
        assertEquals(expType, type);
    }

    /**
     * Test of setGetExtentReachHrZone method, of class WorkoutStep.
     */
    @Test
    public void testSetGetExtentReachHrZone()
    {
        System.out.println("setGetExtentReachHrZone");
  
        WorkoutStep.WorkoutStepExtentType expType=WorkoutStep.WorkoutStepExtentType.NONE;
        WorkoutStep.WorkoutStepExtentType type=theInstance.getStepExtent();
        assertEquals(expType, type);
        
        WorkoutStep.HrZone expResult = WorkoutStep.HrZone.FATBURN;
        theInstance.setExtentReachHrZone(expResult);
        WorkoutStep.HrZone zone=theInstance.getExtentReachHrZone();
        assertEquals(expResult, zone);
        expType=WorkoutStep.WorkoutStepExtentType.REACHHRZONE;
        type=theInstance.getStepExtent();
        assertEquals(expType, type);
    }

    /**
     * Test of setExtentManual method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testSetExtentManual()
    {
        System.out.println("setExtentManual");
        WorkoutStep instance = null;
        instance.setExtentManual();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExtentDuration method, of class WorkoutStep.
     */
    @Test
    public void testSetGetExtentDuration()
    {
        System.out.println("setGetExtentDuration");
  
        WorkoutStep.WorkoutStepExtentType expType=WorkoutStep.WorkoutStepExtentType.NONE;
        WorkoutStep.WorkoutStepExtentType type=theInstance.getStepExtent();
        assertEquals(expType, type);
        
        int expResult = 213;
        theInstance.setExtentDuration(expResult);
        int duration=theInstance.getExtentDuration();
        assertEquals(expResult, duration);
        expType=WorkoutStep.WorkoutStepExtentType.DURATION;
        type=theInstance.getStepExtent();
        assertEquals(expType, type);
    }

    /**
     * Test of setIntensityPace method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testSetIntensityPace()
    {
        System.out.println("setIntensityPace");
        int pace = 0;
        WorkoutStep instance = null;
        instance.setIntensityPace(pace);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntensitySpeed method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testSetIntensitySpeed()
    {
        System.out.println("setIntensitySpeed");
        int speed = 0;
        WorkoutStep instance = null;
        instance.setIntensitySpeed(speed);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntensityHrZone method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testSetIntensityHrZone()
    {
        System.out.println("setIntensityHrZone");
        WorkoutStep.HrZone zone = null;
        WorkoutStep instance = null;
        instance.setIntensityHrZone(zone);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIntensityPace method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testGetIntensityPace()
    {
        System.out.println("getIntensityPace");
        WorkoutStep instance = null;
        int expResult = 0;
        int result = instance.getIntensityPace();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIntensitySpeed method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testGetIntensitySpeed()
    {
        System.out.println("getIntensitySpeed");
        WorkoutStep instance = null;
        int expResult = 0;
        int result = instance.getIntensitySpeed();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIntensityHrZone method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testGetIntensityHrZone()
    {
        System.out.println("getIntensityHrZone");
        WorkoutStep instance = null;
        WorkoutStep.HrZone expResult = null;
        WorkoutStep.HrZone result = instance.getIntensityHrZone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStepIntensity method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testGetStepIntensity()
    {
        System.out.println("getStepIntensity");
        WorkoutStep instance = null;
        WorkoutStep.WorkoutStepIntensityType expResult = null;
        WorkoutStep.WorkoutStepIntensityType result = instance.getStepIntensity();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStepIntensityPace method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testGetStepIntensityPace()
    {
        System.out.println("getStepIntensityPace");
        WorkoutStep instance = null;
        int expResult = 0;
        int result = instance.getStepIntensityPace();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStepIntensitySpeed method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testGetStepIntensitySpeed()
    {
        System.out.println("getStepIntensitySpeed");
        WorkoutStep instance = null;
        int expResult = 0;
        int result = instance.getStepIntensitySpeed();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStepIntensityHrZone method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testGetStepIntensityHrZone()
    {
        System.out.println("getStepIntensityHrZone");
        WorkoutStep instance = null;
        WorkoutStep.HrZone expResult = null;
        WorkoutStep.HrZone result = instance.getStepIntensityHrZone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class WorkoutStep.
     */
    @Test
    @Ignore
    public void testToString()
    {
        System.out.println("toString");
        WorkoutStep instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
