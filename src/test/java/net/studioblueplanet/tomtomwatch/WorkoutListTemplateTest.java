/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.io.File;
import java.nio.file.Files;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.io.IOException;

import net.studioblueplanet.tomtomwatch.WorkoutListTemplate.StepTemplate;
import net.studioblueplanet.tomtomwatch.WorkoutListTemplate.WorkoutTemplate;
import net.studioblueplanet.tomtomwatch.Workout.WorkoutType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.HrZone;
import net.studioblueplanet.tomtomwatch.WorkoutStep.ExtentType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.IntensityType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.StepType;
import net.studioblueplanet.tomtomwatch.WorkoutListItem.ActivityType;
import net.studioblueplanet.tomtomwatch.WorkoutListItem.IntensityLevel;

/**
 *
 * @author jorgen
 */
public class WorkoutListTemplateTest
{
    
    public WorkoutListTemplateTest()
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
     * Handy piece of code to write JSON as UTF-8 to file
     * @param fileName Filename to write to
     * @param utf8String String (JSON) to write
     * @throws IOException When writing goes problematic
     */
    private void writeUTF8File(String fileName, String utf8String) throws IOException
    {
        java.io.BufferedWriter writer = Files.newBufferedWriter((new File(fileName)).toPath(), java.nio.charset.StandardCharsets.UTF_8);
        writer.append(utf8String);
        writer.flush();
        writer.close();        
    }

    @Test
    public void testFromJson() throws IOException
    {
        WorkoutListTemplate instance;
        
        System.out.println("fromJson");
        String json = new String(Files.readAllBytes((new File("src/test/resources/testworkouts.json")).toPath()));   
        instance=WorkoutListTemplate.fromJson(json);
        List<WorkoutListTemplate.WorkoutTemplate> workouts=instance.getWorkouts();
        assertEquals(2, workouts.size());
        
        WorkoutTemplate workout=workouts.get(0);
        assertEquals("0x00be0001", workout.fileId);
        assertEquals("Test workout", workout.name);
        assertEquals("Test description", workout.description);
        assertEquals("12345678123456781234567812345678", workout.listId.toUpperCase());
        assertEquals("12345678123456781234567812345679", workout.workoutId.toUpperCase());
        assertEquals(ActivityType.RUNNING, workout.activity);
        assertEquals(WorkoutType.FITNESS, workout.type);
        assertEquals(IntensityLevel.STANDARD, workout.intensityLevel);
        
        assertEquals(4, workout.steps.size());
        StepTemplate step=workout.steps.get(1);
        assertEquals("Step 2", step.name);
        assertEquals("Description", step.description);
        assertEquals(StepType.WORK, step.type);
        assertEquals(ExtentType.DISTANCE, step.length);
        assertNull(step.time);
        assertEquals(5000, (int)step.distance);
        assertNull(step.reachHrZone);
        assertEquals(IntensityType.PACE, step.intensity);
        assertNull(step.hrZone);
        assertEquals(240, (int)step.pace);
        assertNull(step.speed);

    }
    
    @Test
    public void testToJson() throws IOException
    {
        WorkoutListTemplate instance;
        WorkoutList         workoutList;
        byte[]              data;
        System.out.println("toJson");

        // Populate a workout list with workout items and workouts
        workoutList = new WorkoutList();
        boolean expResult = false;
        data = Files.readAllBytes((new File("src/test/resources/0x00be0000.bin")).toPath());
        workoutList.createWorkoutListFromData(data);
        data = Files.readAllBytes((new File("src/test/resources/0x00be0001.bin")).toPath());
        workoutList.appendWorkoutFromData(0x00BE0001, data);
        data = Files.readAllBytes((new File("src/test/resources/0x00be0002.bin")).toPath());
        workoutList.appendWorkoutFromData(0x00BE0002, data);
        workoutList.sort();

        // Convert to JSON
        instance=WorkoutListTemplate.fromWorkoutList(workoutList);
        String result=instance.toJson();
        
        // Read expected result
        String expected = new String(Files.readAllBytes((new File("src/test/resources/compareworkouts.json")).toPath()),"UTF-8").replace("\n\r", "\n");
        
        // Compare!
        assertEquals(expected, result);
    }
    
    @Test 
    public void testFromWorkoutList() throws IOException
    {
        WorkoutListTemplate instance;
        WorkoutList         workoutList;
        byte[]              data;
        System.out.println("fromWorkoutList");

        workoutList = new WorkoutList();
        boolean expResult = false;
        data = Files.readAllBytes((new File("src/test/resources/0x00be0000.bin")).toPath());
        workoutList.createWorkoutListFromData(data);
        data = Files.readAllBytes((new File("src/test/resources/0x00be0001.bin")).toPath());
        workoutList.appendWorkoutFromData(0x00BE0001, data);
        data = Files.readAllBytes((new File("src/test/resources/0x00be0002.bin")).toPath());
        workoutList.appendWorkoutFromData(0x00BE0002, data);
        workoutList.sort();
        
        instance=WorkoutListTemplate.fromWorkoutList(workoutList);
        
        assertNotNull(instance);
        
        List<WorkoutTemplate> workouts=instance.getWorkouts();
        
        assertEquals(2, workouts.size());
        
        WorkoutTemplate workout=workouts.get(0);
        assertEquals("0x00be0001", workout.fileId);
        assertEquals("★☆☆30 min", workout.name);
        assertEquals("Keep going and stay in the Fat Burn HR zone for the entire time", workout.description);
        assertEquals("E05B453335EC210A59130562CC3E5190", workout.listId.toUpperCase());
        assertEquals("885785DF42E95378A4CA3F848280C12D", workout.workoutId.toUpperCase());
        assertEquals(ActivityType.RUNNING, workout.activity);
        assertEquals(WorkoutType.FATBURN, workout.type);

        workout=workouts.get(1);
        assertEquals(1, workout.steps.size());
        StepTemplate step=workout.steps.get(0);
        assertEquals("Work", step.name);
        assertEquals("Get into the Fat Burn HR zone", step.description);
        assertEquals(ExtentType.TIME, step.length);
        assertEquals(1800, (int)step.time);
        assertNull(step.distance);
        assertNull(step.reachHrZone);
        assertEquals(IntensityType.HRZONE, step.intensity);
        assertEquals(HrZone.FATBURN, step.hrZone);
        assertNull(step.pace);
        assertNull(step.speed);
    }

    @Test 
    public void testFromToJsonRoundtrip() throws IOException
    {
        WorkoutListTemplate instance;
        WorkoutList         workoutList;
        byte[]              data;
        System.out.println("fromToJson Roundtrip");
        String json = new String(Files.readAllBytes((new File("src/test/resources/testworkouts.json")).toPath())).replace("\n\r", "\n");   
        instance=WorkoutListTemplate.fromJson(json);
        String result=instance.toJson();
        assertEquals(json, result);
    }
            
    
}
