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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

import net.studioblueplanet.tomtomwatch.Workout.IntensityLevel;
import net.studioblueplanet.tomtomwatch.Workout.WorkoutType;
import net.studioblueplanet.tomtomwatch.WorkoutListItem.ActivityType;

/**
 *
 * @author jorgen
 */
public class WorkoutListTest
{
    
    public WorkoutListTest()
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
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of appendWorkoutFromData method, of class WorkoutList.
     */
    @Test
    public void testAppendWorkoutFromData() throws IOException
    {
        System.out.println("appendWorkoutFromData");
        int fileId = 0x00be0023;
        byte[] data = Files.readAllBytes((new File("src/test/resources/0x00be0023.bin")).toPath());
        WorkoutList instance = new WorkoutList();
        boolean expResult = false;
        boolean result = instance.appendWorkoutFromData(fileId, data);
        assertEquals(expResult, result);
        
        Workout workout=instance.getWorkouts().get(0x00be0024);
        assertNull(workout);
        workout=instance.getWorkouts().get(fileId);
        assertNotNull(workout);
        assertEquals("★★★60 min", workout.getWorkoutName());
        assertEquals("Workout alternating between comfortable and medium and hard efforts", workout.getWorkoutDescription());
        assertEquals(Workout.WorkoutType.ENDURANCE, workout.getWorkoutType());
        HashMap<Integer, WorkoutStep> steps=workout.getSteps();
        assertEquals(14, steps.size());
        WorkoutStep step=steps.get(1);
        assertEquals(WorkoutStep.ExtentType.TIME, step.getStepExtent());
        assertEquals("Warm up", step.getName());
        assertEquals("Get into the Cardio HR zone", step.getDescription());
        assertEquals(60, step.getExtentTime());
        assertEquals(-1, step.getExtentDistance());
        assertEquals(WorkoutStep.HrZone.NONE, step.getExtentReachHrZone());
    }

    /**
     * Test of createWorkoutListFromData method, of class WorkoutList.
     */
    @Test
    public void testCreateWorkoutListFromData() throws IOException
    {
        System.out.println("createWorkoutListFromData");
        byte[] data = Files.readAllBytes((new File("src/test/resources/0x00be0000-2.bin")).toPath());
        WorkoutList instance = new WorkoutList();
        boolean expResult = false;
        boolean result = instance.createWorkoutListFromData(data);
        instance.sort();
        assertEquals(expResult, result);
        
        List<WorkoutListItem> list=instance.getWorkoutList();
        assertEquals(54, list.size());
        WorkoutListItem item=list.get(37);
        assertEquals(0x00be0023, item.getFileId());
        assertEquals("★★★60 min", item.getWorkoutName());
        assertEquals("Workout alternating between comfortable and medium and hard efforts", item.getWorkoutDescription());
        assertEquals(ActivityType.CYCLING, item.getActivity());
        assertEquals(WorkoutType.ENDURANCE, item.getWorkoutType());
        assertEquals(IntensityLevel.STANDARD, item.getIntensityLevel());
        assertEquals(0, item.getUnknown8());
        assertEquals(617, item.fileSize());
        assertEquals(2, item.getUnknown12());
        assertEquals(1, item.getUnknown13());
        assertArrayEquals(javax.xml.bind.DatatypeConverter.parseHexBinary("E1E198719FF62F4000D051B02F8AE1F1"), item.getWorkoutMd5());
        assertArrayEquals(javax.xml.bind.DatatypeConverter.parseHexBinary("A5051A5B46A75986809F34D542FBA63F"), item.getWorkoutId());
    }

    /**
     * Test of getWorkoutListData method, of class WorkoutList.
     */
    @Test
    public void testGetWorkoutListData() throws IOException
    {
        System.out.println("getWorkoutListData");
        // read protobuf data
        byte[] data = Files.readAllBytes((new File("src/test/resources/0x00be0000-2.bin")).toPath());
        WorkoutList instance = new WorkoutList();
        
        // Decode to object
        instance.createWorkoutListFromData(data);
        
        // Encode object to protbuf data again
        byte[] result = instance.getWorkoutListData();

        // Compare
        assertArrayEquals(data, result);
    }

    /**
     * Test of getWorkoutData method, of class WorkoutList.
     */
    @Test
    public void testGetWorkoutData() throws IOException
    {
        System.out.println("getWorkoutData");
        int fileId = 0x00BE0023;
        byte[] data = Files.readAllBytes((new File("src/test/resources/0x00be0023.bin")).toPath());
        WorkoutList instance = new WorkoutList();
        
        // Decode to object
        instance.appendWorkoutFromData(fileId, data);       

        // Encode again to protobuf data
        byte[] result = instance.getWorkoutData(fileId);
        assertArrayEquals(data, result);
    }

    /**
     * Test of toString method, of class WorkoutList.
     */
    @Test
    public void testToString() throws IOException
    {
        System.out.println("toString");
        WorkoutList instance = new WorkoutList();
        byte[] data = Files.readAllBytes((new File("src/test/resources/0x00be0000.bin")).toPath());
        instance.createWorkoutListFromData(data);
        String expResult = "____________________________________________________________________________________________________\n";
        expResult +=String.format("%s - %08x - %-10s - %s\n", "RUNNING", 0x00be0001, "FAT BURN", "★☆☆30 min");
        expResult +="Keep going and stay in the Fat Burn HR zone for the entire time\n";
        expResult += "____________________________________________________________________________________________________\n";
        expResult +=String.format("%s - %08x - %-10s - %s\n", "CYCLING", 0x00be0002, "FAT BURN", "★☆☆30 min");
        expResult +="Introductory workout keeping within the Fat Burn HR zone\n";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
    
}
