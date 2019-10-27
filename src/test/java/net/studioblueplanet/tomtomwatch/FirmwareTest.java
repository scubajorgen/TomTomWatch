/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.usb.WatchInterface;
import javax.swing.JOptionPane;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 *
 * @author jorgen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ToolBox.class,JOptionPane.class})
public class FirmwareTest
{
    private final WatchInterface                watchInterface;
    private final TomTomWatchView               theView;
    private Firmware                            instance;
    
    public FirmwareTest()
    {
        watchInterface  = mock(WatchInterface.class);
        theView         = mock(TomTomWatchView.class);
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
        instance=Firmware.getInstance();
        instance.setView(theView);
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getInstance method, of class Firmware.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("TEST getInstance");
        Firmware result = Firmware.getInstance();
        assertNotEquals(null, result);
        assertEquals(true, result instanceof Firmware);
    }

    /**
     * Test of updateFirmware method, of class Firmware.
     */
    @Test
    public void testUpdateFirmware()
    {
        int productId;
        long currentFirmware;
        ArgumentCaptor<String> stringCaptor;
        ArgumentCaptor<String> stringCaptor2;
        boolean result;
        boolean expResult;
        
        System.out.println("TEST updateFirmware forceUpdateFirmware");

        // Mock the ToolBox
        PowerMockito.mockStatic(ToolBox.class);  
        
        // Mock the JOptionsPane
        PowerMockito.mockStatic(JOptionPane.class);
        stringCaptor=ArgumentCaptor.forClass(String.class);
        stringCaptor2=ArgumentCaptor.forClass(String.class);
        
        productId = productId=0x123;
        currentFirmware = 0L;

        
        when(watchInterface.getPreference("ConfigURL")).thenReturn("testUrl");
        when(ToolBox.readStringFromUrl("testUrl")).thenReturn("{\"service:firmware\": \"https://serviceUrl/{PRODUCT_ID}/FirmwareVersionConfigV2.xml\"}");
        when(ToolBox.readStringFromUrl("https://serviceUrl/00000123/FirmwareVersionConfigV2.xml")).thenReturn(
                "<FirmwareVersion>\n" +
                "<latestVersion>\n" +
                "<Major>1</Major>\n" +
                "<Minor>7</Minor>\n" +
                "<Build>64</Build>\n" +
                "</latestVersion>\n" +
                "<isCritical>no</isCritical>\n" +
                "<URL>1_7_64/0x000000F0</URL>\n" +
                "</FirmwareVersion>");
        when(ToolBox.readBytesFromUrl("https://serviceUrl/00000123/1_7_64/0x000000F0")).thenReturn(new byte[] {0x00, 0x01, 0x02});
        when(watchInterface.readBleVersion()).thenReturn("1030828");
        when(watchInterface.writeVerifyFile(any())).thenReturn(false);
        when(JOptionPane.showConfirmDialog(null, 
                                           "New firmware found. Execute update?", 
                                           "Confirm",
                                           JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)).thenReturn(JOptionPane.YES_OPTION);
        
        // good flow
        expResult = false;
        result = instance.updateFirmware(watchInterface, productId, currentFirmware);
        verify(theView).setStatus(stringCaptor.capture());
        assertEquals(expResult, result);
        assertEquals("Current firmware: 0.0.0, Latest firmware: 1.7.64\n" +
                     "Current BLE firmware: 1030828, Latest BLE firmware: -\n", 
                     stringCaptor.getValue());
        
        // firmware up to date
        currentFirmware=(1L<<32)+(7L<<16)+64L;
        result = instance.updateFirmware(watchInterface, productId, currentFirmware);
        verify(theView, times(2)).setStatus(stringCaptor.capture());
        verify(theView, times(6)).appendStatus(stringCaptor2.capture());
        assertEquals(expResult, result);
        assertEquals("Current firmware: 1.7.64, Latest firmware: 1.7.64\n" +
                     "Current BLE firmware: 1030828, Latest BLE firmware: -\n", 
                     stringCaptor.getValue());
        assertEquals("Firmware up to date.", 
                     stringCaptor2.getValue());

    }
    
    /**
     * Test of prepareFirmware method, of class Firmware.
     */
    @Test
    public void testPrepareFirmware()
    {
        int                     productId;
        boolean                 expResult;
        boolean                 result;
        ArgumentCaptor<String>  stringCaptor;
        
        System.out.println("TEST prepareFirmware");
        // Mock the ToolBox
        PowerMockito.mockStatic(ToolBox.class);    
        
        stringCaptor=ArgumentCaptor.forClass(String.class);
        
        
        // No config URL
        productId=0x123;
        when(watchInterface.getPreference("ConfigURL")).thenReturn(null);
        when(ToolBox.readStringFromUrl("testUrl")).thenReturn(null);
        expResult=true;
        result = instance.prepareFirmware(watchInterface, productId);   
        assertEquals(expResult, result);
        verify(theView).showErrorDialog(stringCaptor.capture());
        assertEquals("Error downloading firmware: no config URL found in preferences", stringCaptor.getValue());
        
        // No page found on config url
        productId=0x123;
        when(watchInterface.getPreference("ConfigURL")).thenReturn("testUrl");
        when(ToolBox.readStringFromUrl("testUrl")).thenReturn(null);
        expResult=true;
        result = instance.prepareFirmware(watchInterface, productId);   
        assertEquals(expResult, result);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error downloading firmware: no information from testUrl", stringCaptor.getValue());
        
        // No 'service:firmware' tag
        when(ToolBox.readStringFromUrl("testUrl")).thenReturn("{\"noservice:nofirmware\": \"https://serviceUrl/{PRODUCT_ID}/FirmwareVersionConfigV2.xml\"}");
        expResult=true;
        result = instance.prepareFirmware(watchInterface, productId);   
        assertEquals(expResult, result);
        verify(theView, times(3)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error downloading firmware: no service URL from testUrl", stringCaptor.getValue());
        
        // No firmware info
        when(ToolBox.readStringFromUrl("testUrl")).thenReturn("{\"service:firmware\": \"https://serviceUrl/{PRODUCT_ID}/FirmwareVersionConfigV2.xml\"}");
        when(ToolBox.readStringFromUrl("https://serviceUrl/00000123/FirmwareVersionConfigV2.xml")).thenReturn(null);
        when(watchInterface.readBleVersion()).thenReturn("1030828");
        expResult=true;
        result = instance.prepareFirmware(watchInterface, productId);   
        assertEquals(expResult, result);
        verify(theView, times(4)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error downloading firmware: no information from https://serviceUrl/00000123/FirmwareVersionConfigV2.xml", stringCaptor.getValue());
        
        // error extracting firmware
        
        // no BLE info from watch
        
        // Good flow
        when(ToolBox.readStringFromUrl("https://serviceUrl/00000123/FirmwareVersionConfigV2.xml")).thenReturn(
                "<FirmwareVersion>\n" +
                "<latestVersion>\n" +
                "<Major>1</Major>\n" +
                "<Minor>7</Minor>\n" +
                "<Build>64</Build>\n" +
                "</latestVersion>\n" +
                "<isCritical>no</isCritical>\n" +
                "<URL>1_7_64/0x000000F0</URL>\n" +
                "</FirmwareVersion>");
        when(watchInterface.readBleVersion()).thenReturn("1030828");
        productId=0x123;
        expResult=false;
        result = instance.prepareFirmware(watchInterface, productId);
        assertEquals(expResult, result);
    }    


    
}
