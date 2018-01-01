/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.generics;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jorgen
 */
public class PolyLineEncoderTest
{
    
    public PolyLineEncoderTest()
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
     * Test of getInstance method, of class PolyLineEncoder.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("getInstance");
        PolyLineEncoder expResult = null;
        PolyLineEncoder result = PolyLineEncoder.getInstance();
        assertNotEquals(expResult, result);
    }

    /**
     * Test of resetPointEncoding method, of class PolyLineEncoder.
     */
    @Test
    public void testResetPointEncoding()
    {
/*
        System.out.println("resetPointEncoding");
        PolyLineEncoder instance = new PolyLineEncoder();
        instance.resetPointEncoding();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
*/

        
        System.out.println("resetPointEncoding");
        double value;
        String expResult;
        String result;
        double lat;
        double lon;

        PolyLineEncoder instance = new PolyLineEncoder();
        
        instance.resetPointEncoding();
        
        lat         =38.5;
        lon         =-120.2;
        result      = instance.encodePoint(lat, lon);    
        expResult   ="_p~iF~ps|U";
        assertEquals(expResult, result);
        
        lat         =38.5;
        lon         =-120.2;
        result      = instance.encodePoint(lat, lon);    
        expResult   ="??";
        assertEquals(expResult, result);
        
        instance.resetPointEncoding();
        lat         =38.5;
        lon         =-120.2;
        result      = instance.encodePoint(lat, lon);    
        expResult   ="_p~iF~ps|U";
        assertEquals(expResult, result);

    }

    /**
     * Test of encodePoint method, of class PolyLineEncoder.
     */
    @Test
    public void testEncodePoint()
    {
        System.out.println("encodePoint");
        double lat = 0.0;
        double lon = 0.0;
        String result;
        
        PolyLineEncoder instance = new PolyLineEncoder();

        instance.resetPointEncoding();

        // Example on https://developers.google.com/maps/documentation/utilities/polylinealgorithm
        String expResult = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";


        result="";
        
        lat=38.5;
        lon=-120.2;
        result+= instance.encodePoint(lat, lon);

        lat=40.7;
        lon=-120.95;
        result+= instance.encodePoint(lat, lon);

        lat=43.252;
        lon=-126.453;
        result+= instance.encodePoint(lat, lon);


        assertEquals(expResult, result);

    }

    /**
     * Test of encodeValue method, of class PolyLineEncoder.
     */
    @Test
    public void testEncodeValue()
    {
        System.out.println("encodeValue");
        double value = 38.5;
        PolyLineEncoder instance = new PolyLineEncoder();
        
        instance.resetPointEncoding();
        String expResult = "_p~iF";
        String result = instance.encodeValue(value);
        assertEquals(expResult, result);
    }
    
}
