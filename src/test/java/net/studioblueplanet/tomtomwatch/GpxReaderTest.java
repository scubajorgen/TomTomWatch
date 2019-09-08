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

/**
 *
 * @author jorgen
 */
public class GpxReaderTest
{
    
    public GpxReaderTest()
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
     * Test of getInstance method, of class GpxReader.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("getInstance");
        GpxReader notExpResult = null;
        GpxReader result = GpxReader.getInstance();
        assertNotEquals(notExpResult, result);
    }

    /**
     * Test of readRouteFromFile method, of class GpxReader.
     */
    @Test
    public void testReadRouteFromFile_route()
    {
        System.out.println("readRouteFromFile");
        String fileName = "src/test/resources/test1.gpx";
        Route route = new Route();
        GpxReader instance = GpxReader.getInstance();
        boolean expResult = false;
        boolean result = instance.readRouteFromFile(fileName, route);
        assertEquals(expResult, result);
        assertEquals(1, route.segments.size());
        assertEquals(2, route.segments.get(0).getNumberOfPoints());
        assertEquals(52.22182, route.segments.get(0).getPoint(0).getLatitude() , 0.00001);
        assertEquals( 6.89512, route.segments.get(0).getPoint(0).getLongitude(), 0.00001);
        assertEquals(52.21952, route.segments.get(0).getPoint(1).getLatitude() , 0.00001);
        assertEquals( 6.94597, route.segments.get(0).getPoint(1).getLongitude(), 0.00001);
    }
    
    /**
     * Test of readRouteFromFile method, of class GpxReader.
     */
    @Test
    public void testReadRouteFromFile_track()
    {
        System.out.println("readRouteFromFile");
        String fileName = "src/test/resources/test2.gpx";
        Route route = new Route();
        GpxReader instance = GpxReader.getInstance();
        boolean expResult = false;
        boolean result = instance.readRouteFromFile(fileName, route);
        assertEquals(expResult, result);
        assertEquals(2, route.segments.size());
        assertEquals(2, route.segments.get(0).getNumberOfPoints());
        assertEquals(3, route.segments.get(1).getNumberOfPoints());
        assertEquals(53.23657, route.segments.get(0).getPoint(0).getLatitude() , 0.00001);
        assertEquals( 6.57871, route.segments.get(0).getPoint(0).getLongitude(), 0.00001);
        assertEquals(53.23644, route.segments.get(0).getPoint(1).getLatitude() , 0.00001);
        assertEquals( 6.57862, route.segments.get(0).getPoint(1).getLongitude(), 0.00001);
        assertEquals(53.23416, route.segments.get(1).getPoint(2).getLatitude() , 0.00001);
        assertEquals( 6.57688, route.segments.get(1).getPoint(2).getLongitude(), 0.00001);
    }

    /**
     * Test of readRouteFromFile method, of class GpxReader.
     */
    @Test
    public void testReadRouteFromFile_routeAndTrack()
    {
        System.out.println("readRouteFromFile");
        String fileName = "src/test/resources/test3.gpx";
        Route route = new Route();
        GpxReader instance = GpxReader.getInstance();
        boolean expResult = false;
        boolean result = instance.readRouteFromFile(fileName, route);
        assertEquals(expResult, result);
        assertEquals(1, route.segments.size());
        assertEquals(2, route.segments.get(0).getNumberOfPoints());
        assertEquals(52.22182, route.segments.get(0).getPoint(0).getLatitude() , 0.00001);
        assertEquals( 6.89512, route.segments.get(0).getPoint(0).getLongitude(), 0.00001);
        assertEquals(52.21952, route.segments.get(0).getPoint(1).getLatitude() , 0.00001);
        assertEquals( 6.94597, route.segments.get(0).getPoint(1).getLongitude(), 0.00001);
    }   
    
    /**
     * Test of readRouteFromFile method, of class GpxReader.
     */
    @Test
    public void testReadRouteFromFile_noGpx()
    {
        System.out.println("readRouteFromFile");
        String fileName = "src/test/resources/test4.gpx";
        Route route = new Route();
        GpxReader instance = GpxReader.getInstance();
        boolean expResult = true;
        boolean result = instance.readRouteFromFile(fileName, route);
        assertEquals(expResult, result);
    }      

    /**
     * Test of readRouteFromFile method, of class GpxReader.
     */
    @Test
    public void testReadRouteFromFile_noTrackNoRoute()
    {
        System.out.println("readRouteFromFile");
        String fileName = "src/test/resources/test5.gpx";
        Route route = new Route();
        GpxReader instance = GpxReader.getInstance();
        boolean expResult = true;
        boolean result = instance.readRouteFromFile(fileName, route);
        assertEquals(expResult, result);
    }      

    /**
     * Test of readRouteFromFile method, of class GpxReader.
     */
    @Test
    public void testReadRouteFromFile_noFile()
    {
        System.out.println("readRouteFromFile");
        String fileName = "src/test/resources/test6.gpx";
        Route route = new Route();
        GpxReader instance = GpxReader.getInstance();
        boolean expResult = true;
        boolean result = instance.readRouteFromFile(fileName, route);
        assertEquals(expResult, result);
    }      
}
