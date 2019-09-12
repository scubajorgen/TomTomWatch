/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.*;


import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.IOException;
import java.util.Properties;
import org.mockito.Mockito;

/**
 *
 * @author jorgen
 */
public class ConfigSettingsTest
{
//    @Mock
//    FileInputStream     settingsStream=mock(FileInputStream.class);

    @Mock
    Properties properties=Mockito.mock(Properties.class);
    
    @InjectMocks 
    private ConfigSettings theConfigSettings=Mockito.mock(ConfigSettings.class);
    
    
//    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    public ConfigSettingsTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
       
        ConfigSettings.setPropertiesFile("src/test/resources/tomtomwatch.properties");
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
     * Test of getInstance method, of class ConfigSettings.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("getInstance");
        ConfigSettings expResult = null;

        ConfigSettings result = ConfigSettings.getInstance();
        assertNotEquals(expResult, result);
    }

    /**
     * Test of validateSetting method, of class ConfigSettings.
     */
/*    
    @Test
    public void testValidateSetting()
    {
        System.out.println("validateSetting");
        ConfigSettings.Setting setting = new ConfigSettings.Setting(("debugLevel", "Description", "error", "off,debug,info,error", ConfigSettings.SettingType.STRING);
        
        ConfigSettings instance = ConfigSettings.getInstance();
        boolean expResult = true;
        boolean result = instance.validateSetting(setting);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of dumpSettings method, of class ConfigSettings.
     */
/*
    @Test
    public void testDumpSettings()
    {
        System.out.println("dumpSettings");
        ConfigSettings instance = null;
        instance.dumpSettings();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
*/
    /**
     * Test of getIntValue method, of class ConfigSettings.
     */
    @Test
    public void testGetIntValue()
    {
        System.out.println("getIntValue");
        String name = "waypointLogTimeout";
        ConfigSettings instance = ConfigSettings.getInstance();
        int expResult = 5432;
        int result = instance.getIntValue(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of getDoubleValue method, of class ConfigSettings.
     */
    @Test
    public void testGetDoubleValue()
    {
        System.out.println("getDoubleValue");
        String name = "trackSmoothingQFactor";
        ConfigSettings instance = ConfigSettings.getInstance();
        double expResult = 1.9;
        double result = instance.getDoubleValue(name);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getStringValue method, of class ConfigSettings.
     */
    @Test
    public void testGetStringValue()
    {
        System.out.println("getStringValue");
        String name = "";
        ConfigSettings instance = ConfigSettings.getInstance();
        String expResult = "error";
        String result = instance.getStringValue("debugLevel");
        assertEquals(expResult, result);
    }

    /**
     * Test of getBooleanValue method, of class ConfigSettings.
     */
    @Test
    public void testGetBooleanValue()
    {
        System.out.println("getBooleanValue");
        String name = "simulation";
        ConfigSettings instance = ConfigSettings.getInstance();
        boolean expResult = false;
        boolean result = instance.getBooleanValue(name);
        assertEquals(expResult, result);
    }
    
}
