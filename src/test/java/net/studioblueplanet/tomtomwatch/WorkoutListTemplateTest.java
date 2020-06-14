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
import java.util.LinkedHashMap;
import java.io.IOException;
import net.studioblueplanet.generics.ToolBox;

import net.studioblueplanet.tomtomwatch.WorkoutListTemplate.StepTemplate;
import net.studioblueplanet.tomtomwatch.WorkoutListTemplate.WorkoutTemplate;
import net.studioblueplanet.tomtomwatch.WorkoutListTemplate.HrZoneTemplate;
import net.studioblueplanet.tomtomwatch.Workout.WorkoutType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.HrZone;
import net.studioblueplanet.tomtomwatch.WorkoutStep.ExtentType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.IntensityType;
import net.studioblueplanet.tomtomwatch.WorkoutStep.StepType;
import net.studioblueplanet.tomtomwatch.WorkoutListItem.ActivityType;
import net.studioblueplanet.tomtomwatch.WorkoutListItem.IntensityLevel;

import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    
    @Test
    public void testGetHrZones()
    {
        System.out.println("getHrZones");
        WorkoutListTemplate instance=new WorkoutListTemplate();
        
        LinkedHashMap<String, HrZoneTemplate> hrZones=instance.getHrZones();
        assertEquals(5, hrZones.size());
        
        HrZoneTemplate hrZone=hrZones.get("cardio");
        assertEquals(153, hrZone.hrMax);
        
        hrZone=hrZones.get("nonExisting");
        assertNull(hrZone);
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
        String expected = new String(Files.readAllBytes((new File("src/test/resources/compareworkouts.json")).toPath()),"UTF-8").replace("\r\n", "\n");
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
    public void testToWorkoutList() throws IOException
    {
        WorkoutListTemplate instance;
        WorkoutList         workoutList;
        byte[]              data;
        System.out.println("toWorkoutList");
        String json = new String(Files.readAllBytes((new File("src/test/resources/testworkouts.json")).toPath())).replace("\r\n", "\n");   
        instance=WorkoutListTemplate.fromJson(json);
        
        WorkoutList result=instance.toWorkoutList();
        
        List<WorkoutListItem> itemList=result.getWorkoutList();
        WorkoutListItem       item=itemList.get(1);
        Workout               workout=result.getWorkout(item);
        
        assertEquals("Test workout 1", item.getWorkoutName());
        assertEquals("Test description", item.getWorkoutDescription());
        assertArrayEquals(ToolBox.hexStringToBytes("12345678123456781234567812345680", 16), item.getId());
        assertEquals(WorkoutType.POWER, item.getWorkoutType());
        assertEquals(0x00BE0002, item.getFileId());
        assertEquals(ActivityType.CYCLING, item.getActivity());

        assertEquals("Test workout 1", workout.getWorkoutName());
        assertArrayEquals(ToolBox.hexStringToBytes("12345678123456781234567812345681", 16), workout.getId());
        assertEquals(WorkoutType.POWER, workout.getWorkoutType());
        assertEquals(2, workout.getUnknown11());
        
        item=itemList.get(0);
        workout=result.getWorkout(item);
        assertEquals(4, workout.getSteps().size());
        
        WorkoutStep step=workout.getSteps().get(2);
        assertEquals("Step 3", step.getName());
        assertEquals("", step.getDescription());
        assertEquals(StepType.WARMUP, step.getType());
        assertEquals(IntensityType.SPEED, step.getIntensity());
        assertEquals(27, step.getIntensitySpeed());
        assertEquals(-1, step.getIntensityPace());
        assertEquals(HrZone.NONE, step.getIntensityHrZone());
        assertEquals(ExtentType.REACHHRZONE, step.getStepExtent());
        assertEquals(HrZone.FATBURN, step.getExtentReachHrZone());
        assertEquals(-1, step.getExtentDistance());
        assertEquals(-1, step.getExtentTime());
    }

    @Test 
    public void testFromToJsonRoundtrip() throws IOException
    {
        WorkoutListTemplate instance;
        WorkoutList         workoutList;
        byte[]              data;
        System.out.println("fromToJson Roundtrip");
        String json = new String(Files.readAllBytes((new File("src/test/resources/testworkouts.json")).toPath())).replace("\r\n", "\n");   
        instance=WorkoutListTemplate.fromJson(json);
        String result=instance.toJson();
        assertEquals(json, result);
    }

    @Test
    public void testFromProtobufToProtobuf_TheFullMonty() throws IOException
    {
        System.out.println("From protobuf to WorkoutList to WorkoutListTemplate to JSON and back, the full monty");
        WorkoutList         workoutList;
        byte[]              data1, data2, data3;
        byte[]              result1, result2, result3;
        
        System.out.println("fromWorkoutList");

        // Create WorkoutList from protobuf data
        workoutList = new WorkoutList();
        boolean expResult = false;
        data1 = Files.readAllBytes((new File("src/test/resources/0x00be0000.bin")).toPath());
        workoutList.createWorkoutListFromData(data1);
        data2 = Files.readAllBytes((new File("src/test/resources/0x00be0001.bin")).toPath());
        workoutList.appendWorkoutFromData(0x00BE0001, data2);
        data3 = Files.readAllBytes((new File("src/test/resources/0x00be0002.bin")).toPath());
        workoutList.appendWorkoutFromData(0x00BE0002, data3);
        
        // Convert to WorkoutListTemplate
        WorkoutListTemplate template=WorkoutListTemplate.fromWorkoutList(workoutList);
        
        // Convert to JSON
        String json=template.toJson();
        
        // Convert JSON back to WorkoutListTemplate
        WorkoutListTemplate newTemplate=WorkoutListTemplate.fromJson(json);
        
        // Convert back to WorkoutList
        WorkoutList resultList=newTemplate.toWorkoutList();
        
        // Convert to protobufdata again and compare to the origintal protobuf. And Tadaaaaa!!! The same!!!
        result1=resultList.getWorkoutListData();
        assertArrayEquals(data1, result1);
        result2=resultList.getWorkoutData(0x00BE0001);
        assertArrayEquals(result2, data2);
        result3=resultList.getWorkoutData(0x00BE0002);
        assertArrayEquals(result3, data3);
    }

    @Test
    public void testSetHrZonesFromSettings()
    {
        System.out.println("Set the HR Zone settings from WatchSettings");
        
        WorkoutListTemplate instance=new WorkoutListTemplate();
        
        WatchSettings settings;
        
        settings     = mock(WatchSettings.class);
        when(settings.getSettingsValueInt("hrzone/easy/min")).thenReturn(87L);
        when(settings.getSettingsValueInt("hrzone/easy/max")).thenReturn(103L);
        
        when(settings.getSettingsValueInt("hrzone/fatburn/min")).thenReturn(104L);
        when(settings.getSettingsValueInt("hrzone/fatburn/max")).thenReturn(121L);
        
        when(settings.getSettingsValueInt("hrzone/cardio/min")).thenReturn(122L);
        when(settings.getSettingsValueInt("hrzone/cardio/max")).thenReturn(138L);
        
        when(settings.getSettingsValueInt("hrzone/perform/min")).thenReturn(139L);
        when(settings.getSettingsValueInt("hrzone/perform/max")).thenReturn(156L);
        
        when(settings.getSettingsValueInt("hrzone/peak/min")).thenReturn(157L);
        when(settings.getSettingsValueInt("hrzone/peak/max")).thenReturn(174L);
        
        instance.setHrZonesFromSettings(settings);
        verify(settings, times(10)).getSettingsValueInt(any());  
        
        LinkedHashMap<String, HrZoneTemplate> hrZones=instance.getHrZones();
        assertEquals(87, hrZones.get("easy").hrMin);
        assertEquals(156, hrZones.get("perform").hrMax);
        assertEquals(174, hrZones.get("peak").hrMax);
        
        // Check if the default values are not overwritten when the 
        // settings not found
        instance=new WorkoutListTemplate();
        when(settings.getSettingsValueInt(any())).thenReturn(-1L);
        instance.setHrZonesFromSettings(settings);
        hrZones=instance.getHrZones();
        assertEquals(96, hrZones.get("easy").hrMin);
        assertEquals(172, hrZones.get("perform").hrMax);
        assertEquals(192, hrZones.get("peak").hrMax);      
    }
    
    @Test
    public void testSetHrZonesToSettings()
    {
        System.out.println("Set the HR Zone settings to WatchSettings");
        ArgumentCaptor<String>  stringCaptor;
        ArgumentCaptor<Long>    longCaptor;
        
        WorkoutListTemplate instance=new WorkoutListTemplate();
        
        WatchSettings settings;
        
        settings     = mock(WatchSettings.class);
        
        stringCaptor=ArgumentCaptor.forClass(String.class);
        longCaptor=ArgumentCaptor.forClass(Long.class);
        instance.setHrZonesToSettings(settings);
        verify(settings, times(10)).setSettingsValueInt(stringCaptor.capture(), longCaptor.capture());  
        assertEquals("hrzone/easy/min", stringCaptor.getAllValues().get(0));
        assertEquals(96L, longCaptor.getAllValues().get(0).longValue());
        assertEquals("hrzone/cardio/max", stringCaptor.getAllValues().get(5));
        assertEquals(153L, longCaptor.getAllValues().get(5).longValue());
        assertEquals("hrzone/peak/max", stringCaptor.getAllValues().get(9));
        assertEquals(192L, longCaptor.getAllValues().get(9).longValue());
    }   
            
}
