/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import net.studioblueplanet.generics.ToolBox;


import net.studioblueplanet.usb.UsbFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jorgen
 */
public class TomTomReaderTest
{
    private final byte[] ttbinFileData;
    private final byte[] ttbinFileData2;
    
    public TomTomReaderTest()
    {
        ttbinFileData   =ToolBox.readBytesFromFile("src/test/resources/test.ttbin");
        ttbinFileData2  =ToolBox.readBytesFromFile("src/test/resources/test_cycles.ttbin");
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

    private void testContentsNonSmoothed(Activity result)
    {
        assertEquals(79, result.batteryLevel);
        assertEquals(0, result.fitnessPointsStart);
        assertEquals(0, result.fitnessPointsEnd);
        assertEquals("GMT+02:00", result.localTimeZone.getDisplayName());
        assertEquals("", result.route);
        assertEquals(8, result.summaryActivity);
        assertEquals(0, result.summaryCalories);
        assertEquals(4243, result.summaryDuration);
        assertEquals(3773.17798, result.summaryDistance, 0.0001);
        assertEquals(8, result.summaryType);
        assertEquals(7200, result.timeZoneSeconds);
        
        assertEquals(TtbinFileDefinition.ACTIVITY_FREESTYLE, result.getActivityType());
        assertEquals("Freestyle", result.getActivityDescription());
        assertEquals("TomTom", result.getDeviceName());
        assertEquals("Unknown", result.getDeviceSerialNumber());
        assertEquals(3.80562, result.getDistance(), 0.0001);
        assertEquals("2019-09-20 15:16:39",  result.getFirstActiveRecordTime().format("YYYY-MM-DD hh:mm:ss"));
        assertEquals(0, result.getFitnessPoints());
        assertEquals(1, result.getNumberOfSegments());
        assertEquals("", result.getRouteName());
        assertEquals("2019-09-20", result.getStartDateTime().format("YYYY-MM-DD"));
        assertEquals(0.0, result.getTrackSmoothingQFactor(), 0.0001);
        assertEquals(true, result.hasHeightValues());
    }
    
    /**
     * Test of getInstance method, of class TomTomReader.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("TEST: getInstance");
        TomTomReader result = TomTomReader.getInstance();
        assertNotEquals(null, result);
    }

    /**
     * Test of getCycles and getPace method, of class TomTomReader.
     */
    @Test
    public void testSetCyclesAndPace()
    {
        System.out.println("TEST: getCycles and getPace");
        TomTomReader instance = TomTomReader.getInstance();
        Activity result=instance.readTtbinFile(new UsbFile(0xFFFFFFFF, this.ttbinFileData2.length, this.ttbinFileData2));
        assertEquals(177, result.getPace());
        assertEquals(2082, result.getCycles());
    }    
    
    /**
     * Test of setTrackSmoothing method, of class TomTomReader.
     */
    @Test
    public void testSetTrackSmoothing()
    {
        System.out.println("TEST: setTrackSmoothing");
        boolean enabled = true;
        float qFactor = 3.21F;
        TomTomReader instance = TomTomReader.getInstance();
        instance.setTrackSmoothing(enabled, qFactor);
        Activity result=instance.readTtbinFile(new UsbFile(0xFFFFFFFF, this.ttbinFileData.length, this.ttbinFileData));
        assertEquals(3.21, result.getTrackSmoothingQFactor(), 0.0001);
        // The disstance is affectuated by the smoothing
        assertEquals(3.79630, result.getDistance(), 0.0001);
        // The summary originates from teh watch and is not affected
        assertEquals(3773.17798, result.summaryDistance, 0.0001);
    }

    /**
     * Test of readTtbinFile method, of class TomTomReader.
     */
    @Test
    public void testReadTtbinFile_String()
    {
        System.out.println("TEST: readTtbinFile");
        String fileName = "src/test/resources/test.ttbin";
        TomTomReader instance = TomTomReader.getInstance();
        Activity result = instance.readTtbinFile(fileName);
        testContentsNonSmoothed(result);
    }

    /**
     * Test of readTtbinFile method, of class TomTomReader.
     */
    @Test
    public void testReadTtbinFile_UsbFile()
    {
        System.out.println("TEST: readTtbinFile");
        UsbFile file = new UsbFile(0xFFFFFFFF, ttbinFileData.length, ttbinFileData);
        TomTomReader instance = TomTomReader.getInstance();
        Activity result = instance.readTtbinFile(file);
        
        testContentsNonSmoothed(result);
    }
    
}
