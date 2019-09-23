/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.usb.WatchInterface;
import net.studioblueplanet.generics.DirectExecutor;
import hirondelle.date4j.DateTime;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
/**
 *
 * @author jorgen
 */
public class CommunicationProcessTest
{
    private final WatchInterface      watchInterface;
    private final DirectExecutor      executor;
    private final TomTomWatchView     theView;
    private CommunicationProcess      theInstance;
    private ArgumentCaptor<String>    stringCaptor=ArgumentCaptor.forClass(String.class);
    private ArgumentCaptor<Integer>   intCaptor=ArgumentCaptor.forClass(Integer.class);
    private ArgumentCaptor<DateTime>  datetimeCaptor=ArgumentCaptor.forClass(DateTime.class);


    
    public CommunicationProcessTest()
    {
        watchInterface  = mock(WatchInterface.class);
        executor        = new DirectExecutor();
        theView         = mock(TomTomWatchView.class);

        when(watchInterface.openConnection()).thenReturn(false);
        when(watchInterface.getWatchTime()).thenReturn(new DateTime("2019-09-22 07:36:00"));
        when(watchInterface.getPreference("watchName")).thenReturn("Test Watch");
        when(watchInterface.readFirmwareVersion()).thenReturn("01.02.03");
        when(watchInterface.getProductId()).thenReturn(0x00E70000);
        when(watchInterface.getDeviceSerialNumber()).thenReturn("SerialNumber");    
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
        // When started the process will connect

        Mockito.reset(theView);
        Mockito.reset(watchInterface);

        when(watchInterface.openConnection()).thenReturn(false);
        when(watchInterface.getWatchTime()).thenReturn(new DateTime("2019-09-22 07:36:00"));
        when(watchInterface.getPreference("watchName")).thenReturn("Test Watch");
        when(watchInterface.readFirmwareVersion()).thenReturn("01.02.03");
        when(watchInterface.getProductId()).thenReturn(0x00E70000);
        when(watchInterface.getDeviceSerialNumber()).thenReturn("SerialNumber");    

        theInstance=new CommunicationProcess(this.watchInterface, this.executor);
        theInstance.startProcess(theView);
    }
    
    @After
    public void tearDown()
    {        
        theInstance.requestStop();
    }

    /**
     * Just a simple sleep
     * @param millis Milliseconds to sleep
     */
    private void wait(int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch(Exception e)
        {
        }        
    }
    
    /**
     * Test of startProcess method, of class CommunicationProcess.
     */
    @Test
    public void testStartProcess()
    {
        System.out.println("startProcess");

        // The startProcess is called in the setup() method
        
        verify(watchInterface).getPreference("watchName");
        verify(theView).setDeviceName(stringCaptor.capture());
        assertEquals("Test Watch", stringCaptor.getValue());
        
        verify(watchInterface).readFirmwareVersion();
        verify(theView).setFirmwareVersion(stringCaptor.capture());
        assertEquals("01.02.03", stringCaptor.getValue());
        
        verify(watchInterface).getProductId();
        verify(theView).setProductId(intCaptor.capture());
        assertEquals(0x00E70000, intCaptor.getValue().intValue());
        
        verify(watchInterface).getDeviceSerialNumber();
        verify(theView).setSerial(stringCaptor.capture());
        assertEquals("SerialNumber", stringCaptor.getValue());

        // The startProcess() method makes the process request the watch time every second
        // It immediately starts with a call, so in 2.5 sec 3 calls are to be expected
        wait(2500);
        verify(watchInterface, times(3)).getWatchTime();        
        verify(watchInterface).openConnection();
    }

    /**
     * Test of requestStop method, of class CommunicationProcess.
     */
    @Test
    public void testRequestStop()
    {
        System.out.println("requestStop");

        wait(500);
        theInstance.requestStop();
              
        // The watch timer will timeout just once after stopping the timer
        wait(1500);
        verify(watchInterface, times(1)).getWatchTime();
    }

    /**
     * Test of pushCommand method, of class CommunicationProcess.
     */
    @Test
    public void testPushCommandGetDeviceSerial()
    {
        System.out.println("pushCommand - THREADCOMMAND_GETDEVICESERIAL");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_GETDEVICESERIAL;
        theInstance.pushCommand(command);
        // Once called during startup hence 2 times expected
        verify(watchInterface, times(2)).getDeviceSerialNumber();
        verify(theView, times(2)).setSerial(stringCaptor.capture());
        assertEquals("SerialNumber", stringCaptor.getValue());
    }
    
    /**
     * Test of pushCommand method, of class CommunicationProcess.
     */
    @Test
    public void testPushCommandGetTime()
    {
        System.out.println("pushCommand - THREADCOMMAND_GETTIME");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_GETTIME;
        theInstance.pushCommand(command);
        // TO DO: sometimes called twice!
        verify(watchInterface, times(1)).getWatchTime();
        verify(theView, times(1)).showTime(datetimeCaptor.capture());
        assertEquals("07:36:00", datetimeCaptor.getValue().format("hh:mm:ss"));
    }
    

    /**
     * Test of requestSetNewDeviceName method, of class CommunicationProcess.
     */
    @Test
    public void testRequestSetNewDeviceName()
    {
        ArgumentCaptor<String>    prefCaptor=ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String>    nameCaptor=ArgumentCaptor.forClass(String.class);

        System.out.println("requestSetNewDeviceName");
        String name = "NewName";
        theInstance.requestSetNewDeviceName(name);
        verify(watchInterface).setPreference(prefCaptor.capture(), nameCaptor.capture());
        assertEquals("watchName", prefCaptor.getValue());
        assertEquals("NewName", nameCaptor.getValue());
    }

    /**
     * Test of requestLoadActivityFromTtbinFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testRequestLoadActivityFromTtbinFile()
    {
        System.out.println("requestLoadActivityFromTtbinFile");
        String fileName = "";
        CommunicationProcess instance = null;
        instance.requestLoadActivityFromTtbinFile(fileName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of requestWriteDeviceFileToDisk method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testRequestWriteDeviceFileToDisk()
    {
        System.out.println("requestWriteDeviceFileToDisk");
        int fileId = 0;
        CommunicationProcess instance = null;
        instance.requestWriteDeviceFileToDisk(fileId);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of requestUploadFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testRequestUploadFile()
    {
        System.out.println("requestUploadFile");
        String fileName = "";
        CommunicationProcess instance = null;
        instance.requestUploadFile(fileName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of requestDeleteDeviceFileFromWatch method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testRequestDeleteDeviceFileFromWatch()
    {
        System.out.println("requestDeleteDeviceFileFromWatch");
        int fileId = 0;
        CommunicationProcess instance = null;
        instance.requestDeleteDeviceFileFromWatch(fileId);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of requestUploadGpxFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testRequestUploadGpxFile()
    {
        System.out.println("requestUploadGpxFile");
        String file = "";
        String name = "";
        CommunicationProcess instance = null;
        instance.requestUploadGpxFile(file, name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of requestShowFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testRequestShowFile()
    {
        System.out.println("requestShowFile");
        int fileId = 0;
        CommunicationProcess instance = null;
        instance.requestShowFile(fileId);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getActivityData method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testGetActivityData()
    {
        System.out.println("getActivityData");
        int index = 0;
        CommunicationProcess instance = null;
        ActivityData expResult = null;
        ActivityData result = instance.getActivityData(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isConnected method, of class CommunicationProcess.
     */
    @Test
    public void testIsConnected()
    {
        boolean expResult;
        boolean result;

        System.out.println("isConnected");

        // When started, the process is connected
        expResult = true;
        result = theInstance.isConnected();
        assertEquals(expResult, result);
        verify(watchInterface).openConnection();

        CommunicationProcess instance = new CommunicationProcess(this.watchInterface, this.executor);
        Mockito.reset(watchInterface);
        // When created, the process is not connected
        expResult = false;
        result = instance.isConnected();
        assertEquals(expResult, result);
        verify(watchInterface, times(0)).openConnection();
    }

    /**
     * Test of getDeviceName method, of class CommunicationProcess.
     */
    @Test
    public void testGetDeviceName()
    {
        System.out.println("getDeviceName");

        String expResult = "Test Watch";
        String result = theInstance.getDeviceName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTrackSmoothing method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testSetTrackSmoothing()
    {
        System.out.println("setTrackSmoothing");
        boolean enabled = false;
        float qFactor = 0.0F;
        CommunicationProcess instance = null;
        instance.setTrackSmoothing(enabled, qFactor);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addRouteFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testAddRouteFile()
    {
        System.out.println("addRouteFile");
        String name = "";
        String file = "";
        int index = 0;
        CommunicationProcess instance = null;
        instance.addRouteFile(name, file, index);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteAllRouteFiles method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testDeleteAllRouteFiles()
    {
        System.out.println("deleteAllRouteFiles");
        CommunicationProcess instance = null;
        instance.deleteAllRouteFiles();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteRouteFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testDeleteRouteFile()
    {
        System.out.println("deleteRouteFile");
        int index = 0;
        CommunicationProcess instance = null;
        instance.deleteRouteFile(index);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRouteFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testGetRouteFile()
    {
        System.out.println("getRouteFile");
        int index = 0;
        CommunicationProcess instance = null;
        UsbFile expResult = null;
        UsbFile result = instance.getRouteFile(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clear method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testClear()
    {
        System.out.println("clear");
        CommunicationProcess instance = null;
        instance.clear();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of reportReadProgress method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testReportReadProgress()
    {
        System.out.println("reportReadProgress");
        int bytesRead = 0;
        CommunicationProcess instance = null;
        instance.reportReadProgress(bytesRead);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of reportWriteProgress method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testReportWriteProgress()
    {
        System.out.println("reportWriteProgress");
        int bytesWritten = 0;
        CommunicationProcess instance = null;
        instance.reportWriteProgress(bytesWritten);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadActivityFromTtbinFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testLoadActivityFromTtbinFile()
    {
        System.out.println("loadActivityFromTtbinFile");
        CommunicationProcess instance = null;
        boolean expResult = false;
        boolean result = instance.loadActivityFromTtbinFile();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of listHistory method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testListHistory()
    {
        System.out.println("listHistory");
        WatchInterface watchInterface = null;
        CommunicationProcess instance = null;
        instance.listHistory(watchInterface);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveSimulationSet method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testSaveSimulationSet()
    {
        System.out.println("saveSimulationSet");
        WatchInterface watchInterface = null;
        CommunicationProcess instance = null;
        instance.saveSimulationSet(watchInterface);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
