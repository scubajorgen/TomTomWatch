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
import static org.junit.Assert.*;

import net.studioblueplanet.tomtomwatch.WorkoutListItem.ActivityType;
import net.studioblueplanet.tomtomwatch.WorkoutListItem.IntensityLevel;

/**
 *
 * @author jorgen
 */
public class WorkoutListItemTest
{
    private WorkoutListItem theInstance;
    
    public WorkoutListItemTest()
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
        theInstance = new WorkoutListItem(0x00BE0012, "Name", "Description", WorkoutListItem.ActivityType.CYCLING, Workout.WorkoutType.ENDURANCE);
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getFileId method, of class WorkoutListItem.
     */
    @Test
    public void testGetFileId()
    {
        System.out.println("getFileId");
        int expResult = 0x00BE0012;
        int result = theInstance.getFileId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getWorkoutName method, of class WorkoutListItem.
     */
    @Test
    public void testGetWorkoutName()
    {
        System.out.println("getWorkoutName");
        String expResult = "Name";
        String result = theInstance.getWorkoutName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getWorkoutDescription method, of class WorkoutListItem.
     */
    @Test
    public void testGetWorkoutDescription()
    {
        System.out.println("getWorkoutDescription");
        String expResult = "Description";
        String result = theInstance.getWorkoutDescription();
        assertEquals(expResult, result);
    }

    /**
     * Test of getActivity method, of class WorkoutListItem.
     */
    @Test
    public void testGetActivity()
    {
        System.out.println("getActivity");
        WorkoutListItem.ActivityType expResult = WorkoutListItem.ActivityType.CYCLING;
        WorkoutListItem.ActivityType result = theInstance.getActivity();
        assertEquals(expResult, result);
    }

    /**
     * Test of getWorkoutClass method, of class WorkoutListItem.
     */
    @Test
    public void testGetWorkoutClass()
    {
        System.out.println("getWorkoutClass");
        Workout.WorkoutType expResult = Workout.WorkoutType.ENDURANCE;
        Workout.WorkoutType result = theInstance.getWorkoutType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getId method, of class WorkoutListItem.
     */
    @Test
    public void testSetGetId()
    {
        System.out.println("set/getId");
        byte[] expResult = javax.xml.bind.DatatypeConverter.parseHexBinary("E1E198719FF62F4000D051B02F8AE1F1");
        theInstance.setWorkoutMd5(expResult);
        byte[] result = theInstance.getWorkoutMd5();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getWorkoutId method, of class WorkoutListItem.
     */
    @Test
    public void testSetGetWorkoutId()
    {
        System.out.println("set/getWorkoutId");
        byte[] expResult = javax.xml.bind.DatatypeConverter.parseHexBinary("F0CCAB1EDEADBEAF4000D051B02F8AE1");
        theInstance.setWorkoutId(expResult);
        byte[] result = theInstance.getWorkoutId();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of setIntensityLevel and getIntensityLevel method, of class WorkoutListItem.
     */
    @Test
    public void testSetGetIntensityLevel()
    {
        System.out.println("set/getIntensityLevel");
        
        // Default value
        assertEquals(IntensityLevel.STANDARD, theInstance.getIntensityLevel());
        
        IntensityLevel expResult = IntensityLevel.HARDER;
        theInstance.setIntensityLevel(expResult);
        assertEquals(expResult, theInstance.getIntensityLevel());
    }

    /**
     * Test of getUnknown8 method, of class WorkoutListItem.
     */
    @Test
    public void testSetGetUnknown8()
    {
        System.out.println("set/getUnknown8");
        int expResult = 512;
        theInstance.setUnknown8(expResult);
        int result = theInstance.getUnknown8();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUnknown9 method, of class WorkoutListItem.
     */
    @Test
    public void testSetGetUnknown9()
    {
        System.out.println("set/getUnknown9");
        int expResult = 324;
        theInstance.setFileSize(expResult);
        int result = theInstance.fileSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUnknown12 method, of class WorkoutListItem.
     */
    @Test
    public void testSetGetUnknown12()
    {
        System.out.println("set/getUnknown12");
        int expResult = 8456;
        theInstance.setUnknown12(expResult);
        int result = theInstance.getUnknown12();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUnknown13 method, of class WorkoutListItem.
     */
    @Test
    public void testSetGetUnknown13()
    {
        System.out.println("set/getUnknown13");
        int expResult = 9876;
        theInstance.setUnknown13(expResult);
        int result = theInstance.getUnknown13();
        assertEquals(expResult, result);
    }

    /**
     * Test of compareTo method, of class WorkoutListItem.
     */
    @Test
    public void testCompareTo()
    {
        WorkoutListItem item;
        System.out.println("compareTo");
        item = new WorkoutListItem(0x00be1234, "Name", "Description", WorkoutListItem.ActivityType.CYCLING, Workout.WorkoutType.ENDURANCE);
        int expResult = 0;
        int result = theInstance.compareTo(item);
        assertEquals(expResult, result);
 
        item = new WorkoutListItem(0x00be1234, "Name", "Description", WorkoutListItem.ActivityType.CYCLING, Workout.WorkoutType.FATBURN);
        expResult = 1;
        result = theInstance.compareTo(item);
        assertEquals(expResult, result);

        item = new WorkoutListItem(0x00be1234, "Name", "Description", WorkoutListItem.ActivityType.CYCLING, Workout.WorkoutType.POWER);
        expResult = -3;
        result = theInstance.compareTo(item);
        assertEquals(expResult, result);

        item = new WorkoutListItem(0x00be1234, "Name", "Description", WorkoutListItem.ActivityType.RUNNING, Workout.WorkoutType.POWER);
        expResult = 0x4181-0x0802;
        result = theInstance.compareTo(item);
        assertEquals(expResult, result);

    }

    /**
     * Test of toString method, of class WorkoutListItem.
     */
    @Test
    public void testToString()
    {
        System.out.println("toString");
        String expResult = "____________________________________________________________________________________________________\n";
        expResult+=String.format("%s - %08x - %-10s - %s\n", WorkoutListItem.ActivityType.CYCLING, 0x00BE0012, Workout.WorkoutType.ENDURANCE, "Name");
        expResult+="Description\n";
        String result = theInstance.toString();
        assertEquals(expResult, result);
    }
    
}
