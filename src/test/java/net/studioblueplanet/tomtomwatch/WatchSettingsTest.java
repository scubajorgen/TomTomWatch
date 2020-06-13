/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    private WatchSettings   theInstance;
    private byte[]          bytes;
    
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
        bytes=new byte[] {(byte)0x85, 0,                                    // file type
                          2, 0,                                             // length
                          2, 0,                                             // tag 01
                          1, 0, 0, 0,                                       // value 01
                          (byte)169, 0,                                     // tag 02
                          (byte)0x17, (byte)0x1c, (byte)0x00, (byte)0x00};  // value 02
        theInstance    =new WatchSettings(bytes, 0x000100070040L);
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
        String expResult;
        String result;
        result      =theInstance.getSettingDescriptions();
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
        byte[] bytes;
        long   expResult;
        long   result;
        result      =theInstance.getSettingsValueInt("options/utc_offset");
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
         byte[] bytes;
        long   expResult;
        long   result;
        expResult   =0x1234;
        theInstance.setSettingsValueInt("options/utc_offset", expResult);
        result      =theInstance.getSettingsValueInt("options/utc_offset");
        assertEquals(expResult, result);

        expResult   =-1L;
        result      =theInstance.getSettingsValueInt("some/non/existing/name");
        assertEquals(expResult, result);
    
    }    
    
    /**
     * Test of convertSettingsToData()
     */
    @Test
    public void testConvertSettingsToData() 
    {
        System.out.println("getSettingsValueInt");
        byte[] expResult;
        byte[] result;
        int i;

        // Convert back the settings to data
        result      =theInstance.convertSettingsToData();
        expResult   =bytes;
        assertArrayEquals(expResult, result);

        // Second test, test convertSettingsToData icm setSettingsValueInt
        theInstance.setSettingsValueInt("options/utc_offset", 0xff345678);
        expResult   =new byte[] {(byte)0x85, 0, 2, 0, 2, 0, 1, 0, 0, 0, (byte)169, 0, (byte)0x78, (byte)0x56, (byte)0x34, (byte)0xff};
        result      =theInstance.convertSettingsToData();
        assertArrayEquals(expResult, result);
    }   
    
    @Test
    public void testSettingsManifestToSettingsCsv() throws IOException
    {
        System.out.println("settingsManifestToSettingsCsv");
        String csv=theInstance.settingsManifestToSettingsCsv();
        String expected=new String(Files.readAllBytes((new File("src/main/java/net/studioblueplanet/tomtomwatch/resources/settings_00010007003e.csv")).toPath()));
        expected=expected.replace("\r\n", "\n");
        assertEquals(expected, csv);
    }

     @Test
    public void testSettingsManifestToManifestCsv() throws IOException
    {
        System.out.println("settingsManifestToManifestCsv");
        String expected=new String(Files.readAllBytes((new File("src/main/java/net/studioblueplanet/tomtomwatch/resources/manifest_0001073e.txt")).toPath()));
        expected=expected.replace("\r\n", "\n");
        String csv  =theInstance.settingsManifestToManifestCsv();
        assertEquals(expected, csv);
    }   
    
    @Test
    public void testIsChanged()
    {
        System.out.println("isChanged");
        assertEquals(false, theInstance.isChanged());
        theInstance.setSettingsValueInt("options/demo", 1);
        assertEquals(false, theInstance.isChanged());
        theInstance.setSettingsValueInt("options/demo", 0);
        assertEquals(true, theInstance.isChanged());
    }
}
