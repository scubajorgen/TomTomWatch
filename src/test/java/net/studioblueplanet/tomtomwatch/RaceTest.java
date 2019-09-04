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

import net.studioblueplanet.usb.UsbFile;

import javax.xml.bind.DatatypeConverter;
/**
 *
 * @author jorgen
 */
public class RaceTest
{
    private static byte    data[]=DatatypeConverter.parseHexBinary(
        "334D492032354D000000000000000000"
        + "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
        + "00000000"
        + "02FA"
        + "06000000"
        + "DC050000"
        + "DC120000"
        + "FFFFFF27"
        + "FFFFFF28"
        + "FFFFFF28"
        + "FFFFFF27"
        + "FFFFFF28"
        + "FFFFFF28"    
    );

    private static UsbFile testFile;
    
    public RaceTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
        testFile            =new UsbFile();
        testFile.fileId     =0x00710000; // swimming (0x02), no 0x01
        testFile.length     =74;
        testFile.fileData   =data;
        testFile.endOfList  =true;
        testFile.stored     =true;
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
     * Test of getInfo method, of class Race.
     */
    @Test
    public void testGetInfo()
    {
        System.out.println("getInfo");
        Race instance = new Race(testFile);
        String expResult = "0x00710000 Running        3MI 25M           4828 m   1500 s [804 805 805 804 805 805]";

        String result = instance.getInfo();
        
        System.out.println(result);
        assertEquals(expResult, result);
    }
    
}
