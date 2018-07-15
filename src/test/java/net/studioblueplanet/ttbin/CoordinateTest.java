/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author scuba
 */
public class CoordinateTest
{
    
    public CoordinateTest()
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
     * Test of getLatitude method, of class Coordinate.
     */
    @Test
    public void testGetLatitude()
    {
        double expResult;
        double result;

        System.out.println("getLatitude");
        Coordinate instance = new Coordinate(0, -1231234567, 0);
        expResult = -123.1234567;
        result = instance.getLatitude();
        assertEquals(expResult, result, 0.0000001);
    }

    /**
     * Test of getLongitude method, of class Coordinate.
     */
    @Test
    public void testGetLongitude()
    {
        double expResult;
        double result;

        System.out.println("getLongitude");
        Coordinate instance = new Coordinate(1231234567, 0, 0);
        expResult = 123.1234567;
        result = instance.getLongitude();
        assertEquals(expResult, result, 0.0000001);
    }

    /**
     * Test of getElevation method, of class Coordinate.
     */
    @Test
    public void testGetElevation()
    {
        double expResult;
        double result;

        System.out.println("getElevation");
        Coordinate instance = new Coordinate(0, 0, 12345);
        expResult = 123.45;
        result = instance.getElevation();
        assertEquals(expResult, result, 0.01);
    }
    
}
