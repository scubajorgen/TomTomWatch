/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.logger;

import hirondelle.date4j.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

import java.io.PrintStream;
import java.util.TimeZone;

/**
 *
 * @author jorgen
 */
public class DebugLoggerTest
{
    PrintStream mockInfoStream=mock(PrintStream.class);
    PrintStream mockErrorStream=mock(PrintStream.class);
    
    
    public DebugLoggerTest()
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
        DebugLogger.setErrorPrintStream(mockErrorStream);
        DebugLogger.setInfoPrintStream(mockInfoStream);
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of setDebugLevel method, of class DebugLogger.
     */
    @Test
    public void testSetDebugLevel_int()
    {
        int newDebugLevel;
        System.out.println("setDebugLevel");
        
        newDebugLevel = DebugLogger.DEBUGLEVEL_DEBUG;
        DebugLogger.setDebugLevel(newDebugLevel);
        assertEquals(DebugLogger.DEBUGLEVEL_DEBUG, DebugLogger.getDebugLevel());
        
        newDebugLevel = DebugLogger.DEBUGLEVEL_OFF;
        DebugLogger.setDebugLevel(newDebugLevel);
        assertEquals(DebugLogger.DEBUGLEVEL_OFF, DebugLogger.getDebugLevel());
        
        DebugLogger.setDebugLevel(35);
        assertEquals(DebugLogger.DEBUGLEVEL_OFF, DebugLogger.getDebugLevel());
    }

    /**
     * Test of setDebugLevel method, of class DebugLogger.
     */
    @Test
    public void testSetDebugLevel_String()
    {
        String level;
        
        System.out.println("setDebugLevel");
        level = "debug";
        DebugLogger.setDebugLevel(level);
        assertEquals(DebugLogger.DEBUGLEVEL_DEBUG, DebugLogger.getDebugLevel());
        level = "info";
        DebugLogger.setDebugLevel(level);
        assertEquals(DebugLogger.DEBUGLEVEL_INFO, DebugLogger.getDebugLevel());
        level = "error";
        DebugLogger.setDebugLevel(level);
        assertEquals(DebugLogger.DEBUGLEVEL_ERROR, DebugLogger.getDebugLevel());
        level = "pietje puk";
        DebugLogger.setDebugLevel(level);
        assertEquals(DebugLogger.DEBUGLEVEL_OFF, DebugLogger.getDebugLevel());
    }

    /**
     * Test of getDebugLevel method, of class DebugLogger.
     */
    @Test
    public void testGetDebugLevel()
    {
        System.out.println("getDebugLevel");
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_ERROR);
        int expResult = DebugLogger.DEBUGLEVEL_ERROR;
        int result = DebugLogger.getDebugLevel();
        assertEquals(expResult, result);
    }

    /**
     * Test of debug method, of class DebugLogger.
     */
    @Test
    public void testDebug()
    {
        String info;
        
        System.out.println("debug");
        info = "test";
        
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        
        Mockito.reset(mockInfoStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_OFF);
        DebugLogger.debug(info);
        verify(mockInfoStream, times(0)).println(anyString());
        
        Mockito.reset(mockInfoStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_ERROR);
        DebugLogger.debug(info);
        verify(mockInfoStream, times(0)).println(anyString());

        Mockito.reset(mockInfoStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_INFO);
        DebugLogger.debug(info);
        verify(mockInfoStream, times(0)).println(anyString());

        Mockito.reset(mockInfoStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_DEBUG);
        DebugLogger.debug(info);
        verify(mockInfoStream, times(1)).println(argument.capture());
        assertEquals("d " + DateTime.now(TimeZone.getDefault()).format("YYYY-MM-DD hh:mm:ss  ")+"test", argument.getValue());
    }

    /**
     * Test of info method, of class DebugLogger.
     */
    @Test
    public void testInfo()
    {
        String info;
        
        System.out.println("info");
        info = "caramba! info";
        
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        
        Mockito.reset(mockInfoStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_OFF);
        DebugLogger.info(info);
        verify(mockInfoStream, times(0)).println(anyString());
        
        Mockito.reset(mockInfoStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_ERROR);
        DebugLogger.info(info);
        verify(mockInfoStream, times(0)).println(anyString());

        Mockito.reset(mockInfoStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_INFO);
        DebugLogger.info(info);
        verify(mockInfoStream, times(1)).println(anyString());

        Mockito.reset(mockInfoStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_DEBUG);
        DebugLogger.info(info);
        verify(mockInfoStream, times(1)).println(argument.capture());
        assertEquals("i " + DateTime.now(TimeZone.getDefault()).format("YYYY-MM-DD hh:mm:ss  ")+"caramba! info", argument.getValue());    
    }

    /**
     * Test of error method, of class DebugLogger.
     */
    @Test
    public void testError()
    {
        String info;
        
        System.out.println("error");
        info = "mdjrk! info";
        
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        
        Mockito.reset(mockInfoStream);
        Mockito.reset(mockErrorStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_OFF);
        DebugLogger.error(info);
        verify(mockInfoStream, times(0)).println(anyString());
        verify(mockErrorStream, times(0)).println(anyString());
        
        Mockito.reset(mockInfoStream);
        Mockito.reset(mockErrorStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_ERROR);
        DebugLogger.error(info);
        verify(mockInfoStream, times(0)).println(anyString());
        verify(mockErrorStream, times(1)).println(anyString());

        Mockito.reset(mockInfoStream);
        Mockito.reset(mockErrorStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_INFO);
        DebugLogger.error(info);
        verify(mockInfoStream, times(0)).println(anyString());
        verify(mockErrorStream, times(1)).println(anyString());

        Mockito.reset(mockInfoStream);
        Mockito.reset(mockErrorStream);
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_DEBUG);
        DebugLogger.error(info);
        verify(mockInfoStream, times(0)).println(anyString());
        verify(mockErrorStream, times(1)).println(argument.capture());
        assertEquals("e " + DateTime.now(TimeZone.getDefault()).format("YYYY-MM-DD hh:mm:ss  ERROR: ")+"mdjrk! info", argument.getValue());   
        
    }

    /**
     * Test of debugLevelToString method, of class DebugLogger.
     */
    @Test
    public void testDebugLevelToString_int()
    {
        int debugLevel;
        String expResult;
        String result;
        System.out.println("debugLevelToString");
        debugLevel = DebugLogger.DEBUGLEVEL_ERROR;
        expResult = "error";
        result = DebugLogger.debugLevelToString(debugLevel);
        assertEquals(expResult, result);

        debugLevel = DebugLogger.DEBUGLEVEL_INFO;
        expResult = "info";
        result = DebugLogger.debugLevelToString(debugLevel);
        assertEquals(expResult, result);
    
    }

    /**
     * Test of debugLevelToString method, of class DebugLogger.
     */
    @Test
    public void testDebugLevelToString_0args()
    {
        String expResult;
        String result;
        
        System.out.println("debugLevelToString");
        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_ERROR);
        expResult = "error";
        result = DebugLogger.debugLevelToString();
        assertEquals(expResult, result);

        DebugLogger.setDebugLevel(DebugLogger.DEBUGLEVEL_OFF);
        expResult = "off";
        result = DebugLogger.debugLevelToString();
        assertEquals(expResult, result);
    }
    
}
