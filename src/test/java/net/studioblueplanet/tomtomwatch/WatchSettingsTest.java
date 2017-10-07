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
 * @author Jorgen
 */
public class WatchSettingsTest
{
    
    public WatchSettingsTest()
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
     * Test of getSettingDescriptions method, of class WatchSettings.
     */
    @Test
    public void testGetSettingDescriptionsUnsupportedFirmware()
    {
        System.out.println("getSettingDescriptions - unsupported firmeware");
        WatchSettings instance;
        byte[] bytes;
        String expResult;
        String result;

        bytes=new byte[] {0, 0, 1, 0, 2, 0, 1, 0, 0, 0};

        
        instance    =new WatchSettings(bytes, 1);
        result      =instance.getSettingDescriptions();
        expResult   ="No settings definition found for firmware version";
        assertEquals(expResult, result);
    }


    /**
     * Test of getSettingDescriptions method, of class WatchSettings.
     */
    @Test
    public void testGetSettingDescriptions()
    {
        System.out.println("getSettingDescriptions");
        WatchSettings instance;
        byte[] bytes;
        String expResult;
        String result;

        bytes=new byte[] {0, 0, 2, 0, 2, 0, 1, 0, 0, 0, (byte)169, 0, (byte)0x17, (byte)0x1c, (byte)0x00, (byte)0x00};

        
        instance    =new WatchSettings(bytes, 0x01061A);
        result      =instance.getSettingDescriptions();
        expResult   = "   2 options/demo                                       on\n";
        expResult   +=" 169 options/utc_offset                                 7191\n";
        assertEquals(expResult, result);
    }
    
}
