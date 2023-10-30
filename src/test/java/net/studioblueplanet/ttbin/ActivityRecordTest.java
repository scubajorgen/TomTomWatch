/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import java.io.Writer;
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
public class ActivityRecordTest
{
    private ActivityRecord  instance;
    public ActivityRecordTest()
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
        instance = new ActivityRecord();
        instance.setUtcTime(1615652380);  //13-03-2021 16:19:40
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of compareTo method, of class ActivityRecord.
     */
    @Test
    public void testCompareTo()
    {
        System.out.println("compareTo");
        ActivityRecord other = new ActivityRecord();
        other.setUtcTime(0);
        assertTrue(instance.compareTo(other)>0);
        other.setUtcTime(1615652380);
        assertTrue(instance.compareTo(other)==0);
        other.setUtcTime(1615652381);
        assertTrue(instance.compareTo(other)<0);
    }

    /**
     * Test of setUtcTime method, of class ActivityRecord.
     */
    @Test
    public void testSetUtcTime()
    {
        System.out.println("setUtcTime");
        instance.setUtcTime(1615652380);  //13-03-2021 16:19:40
        assertEquals("2021-03-13 16:19:40", instance.getDateTime().format("YYYY-MM-DD hh:mm:ss"));
        instance.setUtcTime(0);  // Start of epoch
        assertEquals("1970-01-01 00:00:00", instance.getDateTime().format("YYYY-MM-DD hh:mm:ss"));
    }

    /**
     * Test of getDateTime method, of class ActivityRecord.
     */
    @Test
    public void testGetDateTime()
    {
        System.out.println("getDateTime");
        assertEquals("2021-03-13 16:19:40", instance.getDateTime().format("YYYY-MM-DD hh:mm:ss"));
    }

    /**
     * Test of setBatteryLevel, setBatteryLevel method, of class ActivityRecord.
     */
    @Test
    public void testSetGetBatteryLevel()
    {
        System.out.println("setBatteryLevel getBatteryLevel");
        int level = 32;
        instance.setBatteryLevel(level);
        assertEquals(level, instance.getBatteryLevel());
    }

    /**
     * Test of setCoordinate method, of class ActivityRecord.
     */
    @Test
    public void testSetCoordinate()
    {
        System.out.println("setCoordinate, getLatitude, getLongitude");
        double lat = -1.0;
        double lon = 1.0;
        instance.setCoordinate(lat, lon);
        assertEquals(lat, instance.getLatitude(), 0.00000001);
        assertEquals(lon, instance.getLongitude(), 0.00000001);
    }

    /**
     * Test of setRawCoordinate method, of class ActivityRecord.
     */
    @Test
    public void testSetRawCoordinate()
    {
        System.out.println("setRawCoordinate, getRawLatitude, getRawLonitude");
        double lat = -2.0;
        double lon = 2.0;
        instance.setRawCoordinate(lat, lon);
        assertEquals(lat, instance.getRawLatitude(), 0.00000001);
        assertEquals(lon, instance.getRawLongitude(), 0.00000001);
    }

    /**
     * Test of setSpeed, getSpeed method, of class ActivityRecord.
     */
    @Test
    public void testSetSpeed()
    {
        System.out.println("setSpeed, getSpeed");
        double speed = 3.0;
        instance.setSpeed(speed);
        assertEquals(speed, instance.getSpeed(), 0.0001);
    }

    /**
     * Test of setTemperature, getTemperature method, of class ActivityRecord.
     */
    @Test
    public void testSetTemperature()
    {
        System.out.println("setTemperature, getTemperature");
        int temperature = 12;
        instance.setTemperature(temperature);
        assertEquals(temperature, instance.getTemperature());
    }

    /**
     * Test of setInstantaneousSpeed method, of class ActivityRecord.
     */
    @Test
    public void testSetInstantaneousSpeed()
    {
        System.out.println("setInstantaneousSpeed, getInstantaneousSpeed");
        System.out.println("setSpeed, getSpeed");
        double speed = 3.23;
        instance.setInstantaneousSpeed(speed);
        assertEquals(speed, instance.getInstantaneousSpeed(), 0.0001);
    }

    /**
     * Test of setCalories method, of class ActivityRecord.
     */
    @Test
    public void testSetCalories()
    {
        System.out.println("setCalories, getCalories");
        double calories = 123.0;
        instance.setCalories(calories);
        assertEquals(calories, instance.getCalories(), 0.0001);
    }

    /**
     * Test of setHeading method, of class ActivityRecord.
     */
    @Test
    public void testSetHeading()
    {
        System.out.println("setHeading, getHeading");
        double heading = 179.1;
        instance.setHeading(heading);
        assertEquals(heading, instance.getHeading(), 0.0001);
    }



    /**
     * Test of setDistance method, of class ActivityRecord.
     */
    @Test
    public void testSetDistance()
    {
        System.out.println("setDistance");
        double distance=2134.6;
        instance.setDistance(distance);
        assertEquals(distance, instance.getDistance(), 0.0001);
    }

    /**
     * Test of setCycles method, of class ActivityRecord.
     */
    @Test
    public void testSetCycles()
    {
        System.out.println("setCycles, getCycles");
        int cycles=176;
        instance.setCycles(cycles);
        assertEquals(cycles, instance.getCycles());
    }

    /**
     * Test of hasHeightValue method, of class ActivityRecord.
     */
    @Test
    public void testHasHeightValue()
    {
        System.out.println("getElevation1, getElevation2, setElevation1, setElevation2, hasHeightValue");
        instance.setElevation1(ActivityRecord.INVALID);
        instance.setElevation2(ActivityRecord.INVALID);
        assertFalse(instance.hasHeightValue());

        double elevation=1234.5;
        instance.setElevation1(elevation);
        assertEquals(elevation, instance.getElevation1(), 0.0001);
        assertEquals(ActivityRecord.INVALID, instance.getElevation2(), 0.0001);
        assertTrue(instance.hasHeightValue());

        instance.setElevation1(ActivityRecord.INVALID);
        instance.setElevation2(elevation);
        assertEquals(ActivityRecord.INVALID, instance.getElevation1(), 0.0001);
        assertEquals(elevation, instance.getElevation2(), 0.0001);
        assertTrue(instance.hasHeightValue());
    }

    /**
     * Test of setDerivedElevation method, of class ActivityRecord.
     */
    @Test
    public void testSetDerivedElevation()
    {
        System.out.println("setDerivedElevation, getDerivedElevation");
        double elevation=176245.6;
        instance.setDerivedElevation(elevation);
        assertEquals(elevation, instance.getDerivedElevation(), 0.0001);
    }

    /**
     * Test of setCumulativeAscend method, of class ActivityRecord.
     */
    @Test
    public void testSetCumulativeAscend()
    {
        System.out.println("setCumulativeAscend, getCumulativeAscent");
        double ascent=1245.6;
        instance.setCumulativeAscend(ascent);
        assertEquals(ascent, instance.getCumulativeAscend(), 0.0001);
    }

    /**
     * Test of setCumulativeDecend method, of class ActivityRecord.
     */
    @Test
    public void testSetCumulativeDecend()
    {
        System.out.println("setCumulativeDecend, getCumulativeDescent");
        double decent=12345.6;
        instance.setCumulativeDecend(decent);
        assertEquals(decent, instance.getCumulativeDecend(), 0.0001);
    }

    /**
     * Test of setElevationStatus method, of class ActivityRecord.
     */
    @Test
    public void testSetElevationStatus()
    {
        System.out.println("setElevationStatus, getElevationStatus");
        int elevationStatus=654;
        instance.setElevationStatus(elevationStatus);
        assertEquals(elevationStatus, instance.getElevationStatus());
    }

    /**
     * Test of setHeartRate method, of class ActivityRecord.
     */
    @Test
    public void testSetHeartRate()
    {
        int time;
        int heartRate;
        System.out.println("setHeartRate, getHeartRate");
        // 3 seconds lag
        time = 1615652377;
        heartRate = 70;
        instance.setHeartRate(time, heartRate);
        assertEquals(ActivityRecord.INVALID, instance.getHeartRate());

        // 2 seconds lag
        time = 1615652378;
        heartRate = 71;
        instance.setHeartRate(time, heartRate);
        assertEquals(heartRate, instance.getHeartRate());

        // 1 second ahead
        time=1615652381;
        heartRate=72;
        instance.setHeartRate(time, heartRate);
        assertEquals(heartRate, instance.getHeartRate());
        
        // exactly right
        time=1615652380;
        heartRate=73;
        instance.setHeartRate(time, heartRate);
        assertEquals(heartRate, instance.getHeartRate());
    }


    /**
     * Test of setFitnessPoints method, of class ActivityRecord.
     */
    @Test
    public void testSetFitnessPoints()
    {
        int time;
        int points;
        System.out.println("setFitnessPoints, getFitnessPoints");
        // 3 seconds lag
        time = 1615652377;
        points = 123;
        instance.setFitnessPoints(time, points);
        assertEquals(0, instance.getFitnessPoints());

        // 2 seconds lag
        time = 1615652378;
        points = 1237;
        instance.setFitnessPoints(time, points);
        assertEquals(points, instance.getFitnessPoints());

        // 1 second ahead
        time=1615652381;
        points=124;
        instance.setFitnessPoints(time, points);
        assertEquals(points, instance.getFitnessPoints());
        
        // exactly right
        time=1615652380;
        points=125;
        instance.setFitnessPoints(time, points);
        assertEquals(points, instance.getFitnessPoints());
    }


    /**
     * Test of setPrecision method, of class ActivityRecord.
     */
    @Test
    public void testSetPrecision()
    {
        System.out.println("setPrecision, getEhpe, getEvpe, getHdop");
        int ehpe = 6;
        int evpe = 7;
        int hdop = 8;
        instance.setPrecision(ehpe, evpe, hdop);
        assertEquals(ehpe, instance.getEhpe());
        assertEquals(evpe, instance.getEvpe());
        assertEquals(hdop, instance.getHdop());
    }

    /**
     * Test of setMovementState method, of class ActivityRecord.
     */
    @Test
    public void testSetMovementState()
    {
        System.out.println("setMovementState, getMovementState");
        int state = 34;
        instance.setMovementState(state);
        assertEquals(state, instance.getMovementState(), 0.0001);
    }

    /**
     * Test of equals method, of class ActivityRecord.
     */
    @Test
    public void testEquals()
    {
        System.out.println("equals");
        ActivityRecord record=new ActivityRecord();
        record.setUtcTime(1615652380);
        assertTrue(instance.equals(record));
        record.setUtcTime(1615652381);
        assertFalse(instance.equals(record));
    }

    /**
     * Test of dumpRecord method, of class ActivityRecord.
     */
    @Test
    @Ignore
    public void testDumpRecord()
    {
        System.out.println("dumpRecord");
        ActivityRecord instance = new ActivityRecord();
        instance.dumpRecord();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of dumpRecordCsvHeader method, of class ActivityRecord.
     */
    @Test
    @Ignore
    public void testDumpRecordCsvHeader() throws Exception
    {
        System.out.println("dumpRecordCsvHeader");
        Writer writer = null;
        ActivityRecord instance = new ActivityRecord();
        instance.dumpRecordCsvHeader(writer);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of dumpRecordCsv method, of class ActivityRecord.
     */
    @Test
    @Ignore
    public void testDumpRecordCsv() throws Exception
    {
        System.out.println("dumpRecordCsv");
        Writer writer = null;
        ActivityRecord instance = new ActivityRecord();
        instance.dumpRecordCsv(writer);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
