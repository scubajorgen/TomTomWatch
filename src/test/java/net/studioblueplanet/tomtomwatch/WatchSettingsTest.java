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

        bytes=new byte[] {(byte)0x85, 0, 1, 0, 2, 0, 1, 0, 0, 0};

        
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

        bytes=new byte[] {(byte)0x85, 0, 2, 0, 2, 0, 1, 0, 0, 0, (byte)169, 0, (byte)0x17, (byte)0x1c, (byte)0x00, (byte)0x00};

        
        instance    =new WatchSettings(bytes, 0x00010006001AL);
        result      =instance.getSettingDescriptions();
        expResult   = "   2 options/demo                                       on\n";
        expResult   +=" 169 options/utc_offset                                 7191\n";
        assertEquals(expResult, result);
    }

    /**
     * Test of getSettingsValueInt()
     */
    @Test
    public void testGetSettingsValueInt() 
    {
        System.out.println("getSettingsValueInt");
        WatchSettings instance;
        byte[] bytes;
        long   expResult;
        long   result;

        bytes=new byte[] {(byte)0x85, 0, 2, 0, 2, 0, 1, 0, 0, 0, (byte)169, 0, (byte)0x17, (byte)0x1c, (byte)0x00, (byte)0x00};

        
        instance    =new WatchSettings(bytes, 0x00010006001AL);
        result      =instance.getSettingsValueInt("options/utc_offset");
        expResult   =0x1c17L;
        assertEquals(expResult, result);
    
    }    

    /**
     * Test of setSettingsValueInt()
     */
    @Test
    public void testSetSettingsValueInt() 
    {
        System.out.println("setSettingsValueInt");
        WatchSettings instance;
        byte[] bytes;
        long   expResult;
        long   result;

        bytes=new byte[] {(byte)0x85, 0, 2, 0, 2, 0, 1, 0, 0, 0, (byte)169, 0, (byte)0x17, (byte)0x1c, (byte)0x00, (byte)0x00};

        
        instance    =new WatchSettings(bytes, 0x00010006001AL);
        expResult   =0x1234;
        instance.setSettingsValueInt("options/utc_offset", expResult);
        result      =instance.getSettingsValueInt("options/utc_offset");
        assertEquals(expResult, result);

        
        expResult   =-1L;
        result      =instance.getSettingsValueInt("some/non/existing/name");
        assertEquals(expResult, result);
    
    }    
    
    /**
     * Test of convertSettingsToData()
     */
    @Test
    public void testConvertSettingsToData() 
    {
        System.out.println("getSettingsValueInt");
        WatchSettings instance;
        byte[] bytes;
        byte[] expResult;
        byte[] result;
        int i;

        bytes=new byte[] {(byte)0x85, 0, 2, 0, 2, 0, 1, 0, 0, 0, (byte)169, 0, (byte)0x17, (byte)0x1c, (byte)0x00, (byte)0x00};

        // Convert data to settings
        instance    =new WatchSettings(bytes, 0x00010006001AL);
        
        // Convert back the settings to data
        result      =instance.convertSettingsToData();
        expResult   =bytes;
        
        assertEquals(result.length, expResult.length);
        i=0;
        while (i<expResult.length)
        {
            assertEquals(expResult[i], result[i]);
            i++;
        }
        
        // Second test, test convertSettingsToData icm convertSettingsToData
        instance.setSettingsValueInt("options/utc_offset", 0xff345678);
        expResult   =new byte[] {(byte)0x85, 0, 2, 0, 2, 0, 1, 0, 0, 0, (byte)169, 0, (byte)0x78, (byte)0x56, (byte)0x34, (byte)0xff};
        result      =instance.convertSettingsToData();
        
        assertEquals(result.length, expResult.length);
        i=0;
        while (i<expResult.length)
        {
            assertEquals(expResult[i], result[i]);
            i++;
        }
        
        
    }        

}
