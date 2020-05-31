/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.usb.WatchInterface.FileType;
import net.studioblueplanet.usb.WatchInterface;
import net.studioblueplanet.generics.DirectExecutor;
import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.ttbin.TomTomReader;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.TtbinHeader;
import net.studioblueplanet.settings.ConfigSettings;
import hirondelle.date4j.DateTime;
import java.util.ArrayList;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;


/**
 *
 * @author jorgen
 */


@RunWith(PowerMockRunner.class)
@PrepareForTest(ToolBox.class)
public class CommunicationProcessTest
{
    private final WatchInterface                watchInterface;
    private final DirectExecutor                executor;
    private final TomTomWatchView               theView;
    private CommunicationProcess                theInstance;
    private final TomTomReader                  ttbinReader;
    private final GpxReader                     gpxReader;

    private final ArgumentCaptor<UsbFile>       fileCaptor;
    private final ArgumentCaptor<String>        stringCaptor;
    private final ArgumentCaptor<Integer>       intCaptor;
    private final ArgumentCaptor<DateTime>      datetimeCaptor;
    private final ArgumentCaptor<FileType>      fileTypeCaptor;
    private final ArgumentCaptor<ActivityData>  dataCaptor;
    private final ArgumentCaptor<byte[]>        bytesCaptor;

    
    public CommunicationProcessTest()
    {
        watchInterface  = mock(WatchInterface.class);
        executor        = new DirectExecutor();
        theView         = mock(TomTomWatchView.class);
        ttbinReader     = mock(TomTomReader.class);
        gpxReader       = mock(GpxReader.class);
        // Set test values for the settings
        ConfigSettings.setPropertiesFile("src/test/resources/tomtomwatch.properties");
        ConfigSettings.getInstance();
        
        intCaptor       =ArgumentCaptor.forClass(Integer.class);
        stringCaptor    =ArgumentCaptor.forClass(String.class);
        fileCaptor      =ArgumentCaptor.forClass(UsbFile.class);
        datetimeCaptor  =ArgumentCaptor.forClass(DateTime.class);
        fileTypeCaptor  =ArgumentCaptor.forClass(FileType.class);
        dataCaptor      =ArgumentCaptor.forClass(ActivityData.class);
        bytesCaptor     =ArgumentCaptor.forClass(byte[].class);
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    private void returnFileData(UsbFile usbFile)
    {
        if (usbFile.fileId==0x00000123)
        {
            usbFile.length=3;
            usbFile.fileData=new byte[3];
            usbFile.fileData[0]=0;
            usbFile.fileData[1]=1;
            usbFile.fileData[2]=2;
        }
        else if (usbFile.fileId==0x00004321)
        {
            usbFile.length=4;
            usbFile.fileData=new byte[4];
            usbFile.fileData[0]=4;
            usbFile.fileData[1]=3;
            usbFile.fileData[2]=2;
            usbFile.fileData[3]=1;
        }
        else if (usbFile.fileId==0x00B80000)
        {
            usbFile.length=4;
            usbFile.fileData=new byte[4];
            usbFile.fileData[0]=4;
            usbFile.fileData[1]=3;
            usbFile.fileData[2]=2;
            usbFile.fileData[3]=1;
        }
        else if (usbFile.fileId==0x00B80001)
        {
            usbFile.length=4;
            usbFile.fileData=new byte[4];
            usbFile.fileData[0]=4;
            usbFile.fileData[1]=3;
            usbFile.fileData[2]=2;
            usbFile.fileData[3]=1;
        }
        else if (usbFile.fileId==0x00910000)
        {
            usbFile.length=4;
            usbFile.fileData=new byte[3];
            usbFile.fileData[0]=10;
            usbFile.fileData[1]=30;
            usbFile.fileData[2]=20;
        }
        else if (usbFile.fileId==0x00910001)
        {
            usbFile.length=4;
            usbFile.fileData=new byte[3];
            usbFile.fileData[0]=40;
            usbFile.fileData[1]=30;
            usbFile.fileData[2]=22;
        }
        else if (usbFile.fileId==0x00910002)
        {
            usbFile.length=4;
            usbFile.fileData=new byte[3];
            usbFile.fileData[0]=40;
            usbFile.fileData[1]=30;
            usbFile.fileData[2]=22;
        }
        else if (usbFile.fileId==0x00910003)
        {
            usbFile.length=4;
            usbFile.fileData=new byte[3];
            usbFile.fileData[0]=40;
            usbFile.fileData[1]=30;
            usbFile.fileData[2]=22;
        }
        
    }
    
    @Before
    public void setUp()
    {
        ArrayList<UsbFile>  watchFiles=new ArrayList<>();
        ArrayList<UsbFile>  routeFiles=new ArrayList<>();
        ArrayList<UsbFile>  activityFiles=new ArrayList<>();

        UsbFile file1=new UsbFile(0x00000123, 3, null);
        UsbFile file2=new UsbFile(0x00004321, 3, null);
        UsbFile file3=new UsbFile(0x00B80000, 4, null);
        UsbFile file4=new UsbFile(0x00B80001, 4, null);
        UsbFile file5=new UsbFile(0x00910000, 3, null);
        UsbFile file6=new UsbFile(0x00910001, 3, null);
        UsbFile file7=new UsbFile(0x00910002, 3, null);
        UsbFile file8=new UsbFile(0x00910003, 3, null);

        watchFiles.add(file1);
        watchFiles.add(file2);
        watchFiles.add(file3);
        watchFiles.add(file4);
        watchFiles.add(file5);
        watchFiles.add(file6);
        watchFiles.add(file7);
        watchFiles.add(file8);
        
        routeFiles.add(file3);
        routeFiles.add(file4);

        activityFiles.add(file5);
        activityFiles.add(file6);
        activityFiles.add(file7);
        activityFiles.add(file8);
        
        Mockito.reset(theView);
        Mockito.reset(watchInterface);

        when(watchInterface.openConnection()).thenReturn(false);
        when(watchInterface.getWatchTime()).thenReturn(new DateTime("2019-09-22 07:36:00"));
        when(watchInterface.getPreference("watchName")).thenReturn("Test Watch");
        when(watchInterface.getPreference("ConfigURL")).thenReturn("preferenceValue");
        when(watchInterface.readFirmwareVersion()).thenReturn("01.02.03");
        when(watchInterface.getProductId()).thenReturn(0x00E70000);
        when(watchInterface.getDeviceSerialNumber()).thenReturn("SerialNumber"); 
        
        when(watchInterface.getFileList(FileType.TTWATCH_FILE_ALL)).thenReturn(watchFiles);
        when(watchInterface.getFileList(FileType.TTWATCH_FILE_TTBIN_DATA)).thenReturn(activityFiles);
        when(watchInterface.getFileList(FileType.TTWATCH_FILE_TRACKPLANNING)).thenReturn(routeFiles);
        
        // Read File; prepare for two subsequent calls

        when(watchInterface.readFile(any()))
        .thenAnswer
        ((InvocationOnMock invocation) ->
        {
            Object[]    args = invocation.getArguments();
            UsbFile usbFile =(UsbFile)args[0];
            returnFileData(usbFile);
            return false; 
        });

        when(watchInterface.writeVerifyFile(any())).thenReturn(false);
        when(watchInterface.deleteFile(any())).thenReturn(false);
        when(watchInterface.fileExists(anyInt())).thenReturn(true);
        when(watchInterface.writeGpxQuickFixFile(any())).thenReturn(false);

        theInstance=new CommunicationProcess(watchInterface, executor, ttbinReader, gpxReader);  
        // When started the process will connect
        theInstance.startProcess(theView);

        
        when(gpxReader.readRouteFromFile(any(), any()))
        .thenAnswer
        ((InvocationOnMock invocation) ->
        {
            Object[]    args = invocation.getArguments();
            Route route =(Route)args[1];
            RouteSegment segment;
            RoutePoint   point;
            segment=route.appendRouteSegment();
            point=new RoutePoint(53.2523616, 6.5884867);
            point=new RoutePoint(52.2221127, 6.9107758);
            segment.appendRoutePoint(point);
            return false; 
        });        
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
        System.out.println("TEST: startProcess");

        // The startProcess is called in the setup() method
        
        verify(watchInterface).getPreference("watchName");
        verify(theView).setDeviceName(stringCaptor.capture());
        assertEquals("Test Watch", stringCaptor.getValue());
        
        verify(watchInterface).readFirmwareVersion();
        verify(theView).setFirmwareVersion(stringCaptor.capture());
        assertEquals("01.02.03", stringCaptor.getValue());
        
        verify(watchInterface).getProductId();
        verify(theView).setProductId(intCaptor.capture());
        assertEquals(0x00E70000, (int)intCaptor.getValue());
        
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
        System.out.println("TEST: requestStop");

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
        System.out.println("TEST: pushCommand - THREADCOMMAND_GETDEVICESERIAL");
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
        System.out.println("TEST: pushCommand - THREADCOMMAND_GETTIME");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_GETTIME;

        // TO DO: When started, the first call is made
        wait(500);
        verify(watchInterface, times(1)).getWatchTime();
        verify(theView, times(1)).showTime(datetimeCaptor.capture());
        
        // Second call
        theInstance.pushCommand(command);
        verify(watchInterface, times(2)).getWatchTime();
        verify(theView, times(2)).showTime(datetimeCaptor.capture());
        assertEquals("07:36:00", datetimeCaptor.getValue().format("hh:mm:ss"));
    }

    /**
     * Test of pushCommand method, of class CommunicationProcess.
     */
    @Test
    public void testPushCommandListActivityFiles()
    {
        System.out.println("TEST: pushCommand - THREADCOMMAND_DOWNLOADACTIVITIES");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_DOWNLOADACTIVITIES;

        // Good flow - download all
        when(theView.isDownloadMostRecent()).thenReturn(false);
        when(ttbinReader.readTtbinFile((UsbFile)any())).thenReturn(new Activity(new TtbinHeader()));

        theInstance.pushCommand(command);
        verify(watchInterface, times(4)).readFile(fileCaptor.capture());
        assertEquals(0x00910003, fileCaptor.getAllValues().get(0).fileId);
        assertEquals(0x00910002, fileCaptor.getAllValues().get(1).fileId);
        assertEquals(0x00910001, fileCaptor.getAllValues().get(2).fileId);
        assertEquals(0x00910000, fileCaptor.getAllValues().get(3).fileId);
        verify(theView, times(4)).addListItem(any(), any());

        // Good flow - download all
        when(theView.isDownloadMostRecent()).thenReturn(true);
        theInstance.pushCommand(command);
        verify(watchInterface, times(7)).readFile(fileCaptor.capture());
        assertEquals(0x00910003, fileCaptor.getAllValues().get(0).fileId);
        assertEquals(0x00910002, fileCaptor.getAllValues().get(1).fileId);
        assertEquals(0x00910001, fileCaptor.getAllValues().get(2).fileId);
        verify(theView, times(7)).addListItem(any(), any());
        
        
        // File read error
        doReturn(true).when(watchInterface).readFile(any());
        theInstance.pushCommand(command);
        verify(theView, times(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error reading file 0x00910003", stringCaptor.getValue());

        // No file info
        when(watchInterface.getFileList(any())).thenReturn(null);
        theInstance.pushCommand(command);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error retrieving file info from watch", stringCaptor.getValue());

    }

    
    /**
     * Test of pushCommand method, of class CommunicationProcess.
     */
    @Test
    public void testPushCommandListRouteFiles()
    {
        System.out.println("TEST: pushCommand - THREADCOMMAND_DOWNLOADROUTES");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_DOWNLOADROUTES;

        // Good flow
        theInstance.pushCommand(command);
        verify(watchInterface, times(2)).readFile(fileCaptor.capture());
        assertEquals(0x00b80000, fileCaptor.getAllValues().get(0).fileId);
        assertEquals(0x00b80001, fileCaptor.getAllValues().get(1).fileId);
        assertNotEquals(null, fileCaptor.getAllValues().get(0).fileData);
        assertNotEquals(null, fileCaptor.getAllValues().get(1).fileData);
        verify(theView).addRoutesToListBoxLater(any(), anyInt());
        
        // File read error
        doReturn(true).when(watchInterface).readFile(any());
        theInstance.pushCommand(command);
        verify(theView, times(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error reading file 0x00b80000", stringCaptor.getValue());

        // No file info
        when(watchInterface.getFileList(any())).thenReturn(null);
        theInstance.pushCommand(command);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error retrieving file info from watch", stringCaptor.getValue());
    }

    /**
     * Test of pushCommand method, of class CommunicationProcess.
     */
    @Test
    public void testPushCommandUploadRouteFiles()
    {
        ArrayList<UsbFile>  watchFiles=new ArrayList<>();
        UsbFile file1=new UsbFile(0x00B80000, 4, null);
        watchFiles.add(file1);
        UsbFile file2=new UsbFile(0x00B80001, 4, null);
        watchFiles.add(file2);
        when(watchInterface.getFileList(any())).thenReturn(watchFiles);
        
        System.out.println("TEST: pushCommand - THREADCOMMAND_UPLOADROUTES");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_UPLOADROUTES;

        // Good flow: download and upload
        // Prepare: download the routes
        theInstance.deleteAllRouteFiles();
        theInstance.pushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADROUTES);
        theInstance.pushCommand(command);
        verify(watchInterface, never()).deleteFile(any());
        verify(watchInterface, never()).writeVerifyFile(any());

        // Good flow: download and upload
        // Prepare: download, delete the last route
        theInstance.deleteAllRouteFiles();
        theInstance.pushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADROUTES);
        theInstance.deleteRouteFile(1);
        theInstance.pushCommand(command);
        verify(watchInterface, times(1)).deleteFile(fileCaptor.capture());
        assertEquals(0x00b80001, fileCaptor.getValue().fileId);
        verify(watchInterface, never()).writeVerifyFile(any());
        
        // Good flow: download, append new file and upload
        // Prepare: download the routes
        theInstance.deleteAllRouteFiles();
        theInstance.pushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADROUTES);
        theInstance.addRouteFile("testRoute", "fileName", 2);
        theInstance.pushCommand(command);
        verify(watchInterface, times(1)).deleteFile(any());
        verify(watchInterface, times(1)).writeVerifyFile(fileCaptor.capture());
        assertEquals(0x00b80002, fileCaptor.getValue().fileId);

        // Good flow: download, add new file and upload
        // Prepare: download the routes
        theInstance.deleteAllRouteFiles();
        theInstance.pushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADROUTES);
        theInstance.addRouteFile("testRoute", "fileName", 1);
        theInstance.pushCommand(command);
        verify(watchInterface, times(2)).deleteFile(fileCaptor.capture());
        assertEquals(0x00b80001, fileCaptor.getValue().fileId);
        verify(watchInterface, times(3)).writeVerifyFile(fileCaptor.capture());
        assertEquals(0x00b80001, fileCaptor.getAllValues().get(0).fileId);
        assertEquals(0x00b80002, fileCaptor.getAllValues().get(1).fileId);

        // Bad flow: to many routes
        // Prepare: download the routes
        theInstance.deleteAllRouteFiles();
        int i=0;
        while (i<16)
        {
            theInstance.addRouteFile("testRoute", "fileName", 0);
            i++;
        }
        theInstance.pushCommand(command);
        verify(theView).showErrorDialog(stringCaptor.capture());
        assertEquals("To many routes to upload. Reduce to 15", stringCaptor.getValue());
        
        // Bad flow: error while deleting
        // Good flow: download and upload
        // Prepare: download, delete the last route
        when(watchInterface.deleteFile(any())).thenReturn(true);
        theInstance.deleteAllRouteFiles();
        theInstance.pushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADROUTES);
        theInstance.deleteRouteFile(1);
        theInstance.pushCommand(command);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error updating files to watch", stringCaptor.getValue());
        
        // Bad flow: error while deleting
        // Prepare: download, delete the last route
        when(watchInterface.deleteFile(any())).thenReturn(false);
        when(watchInterface.writeVerifyFile(any())).thenReturn(true);
        theInstance.deleteAllRouteFiles();
        theInstance.addRouteFile("testRoute", "fileName", 0);
        theInstance.pushCommand(command);
        verify(theView, times(3)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error updating files to watch", stringCaptor.getValue());
    }    
    
    
    /**
     * Test of pushCommand method, of class CommunicationProcess.
     */
    @Test
    public void testPushCommandSaveSimulationSet()
    {
        System.out.println("TEST: pushCommand - THREADCOMMAND_SAVESIMULATIONSET");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_SAVESIMULATIONSET;

        // Good flow
        // Mock the ToolBox
        PowerMockito.mockStatic(ToolBox.class);
        when(ToolBox.writeBytesToFile(any(), any())).thenReturn(false);
        theInstance.pushCommand(command);
        verify(watchInterface).getFileList(fileTypeCaptor.capture());
        assertEquals(FileType.TTWATCH_FILE_ALL, fileTypeCaptor.getValue());
        verify(watchInterface, times(8)).readFile(fileCaptor.capture());
        PowerMockito.verifyStatic(Mockito.times(8));
        ToolBox.writeBytesToFile(stringCaptor.capture(), any());

        // Error writing file
        when(ToolBox.writeBytesToFile(any(), any())).thenReturn(true);
        theInstance.pushCommand(command);
        verify(theView, times(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error writing file 0x00000123 to disk as .\\working\\simulation\\0x00000123.bin", stringCaptor.getValue());        
        
        // File read error
        doReturn(true).when(watchInterface).readFile(any());
        theInstance.pushCommand(command);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error reading file 0x00000123", stringCaptor.getValue());

        // No file info
        when(watchInterface.getFileList(any())).thenReturn(null);
        theInstance.pushCommand(command);
        verify(theView, times(3)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error retrieving file info from watch", stringCaptor.getValue());
    }
    
    /**
     * Test of pushCommand method, of class CommunicationProcess.
     */
    @Test
    public void testPushCommandUploadGpsQuickfix()
    {
        System.out.println("TEST: pushCommand - THREADCOMMAND_UPLOADGPSDATAS");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_UPLOADGPSDATA;

        // Mock the ToolBox
        PowerMockito.mockStatic(ToolBox.class);        
        
        // Good flow
        when(ToolBox.readStringFromUrl("preferenceValue")).thenReturn("{\"service:ephemeris\":\"TEST{DAYS}\"}");
        when(ToolBox.readBytesFromUrl("TEST7")).thenReturn(new byte[]{0, 1, 2});
        theInstance.pushCommand(command);
        verify(watchInterface).writeGpxQuickFixFile(any());
        verify(theView).setStatus(stringCaptor.capture());
        verify(theView, times(4)).appendStatus(stringCaptor.capture());
        assertEquals("Uploading GPS Quickfix data\n", stringCaptor.getAllValues().get(0)); 
        assertEquals("Configuration URL: preferenceValue\n", stringCaptor.getAllValues().get(1));
        assertEquals("Quickfix data URL: TEST7\n", stringCaptor.getAllValues().get(2));
        assertEquals("GPS Quickfix data sent to watch\n", stringCaptor.getAllValues().get(3));        
        assertEquals("Done\n", stringCaptor.getAllValues().get(4));

        // Cannot write to watch
        when(watchInterface.writeGpxQuickFixFile(any())).thenReturn(true);
        theInstance.pushCommand(command);
        verify(theView).showErrorDialog(stringCaptor.capture());
        assertEquals("Unable to send quickfix file to the watch", stringCaptor.getValue());

        // Cannot read quick fix file
        when(ToolBox.readBytesFromUrl(any())).thenReturn(null);
        theInstance.pushCommand(command);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Unable to read quickfix file from TomTom", stringCaptor.getValue());
        
        // No preference found
        doReturn(null).when(watchInterface).getPreference("ConfigURL");
        theInstance.pushCommand(command);
        verify(theView, times(3)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error reading preference from the Watch", stringCaptor.getValue());
    }    
    

    /**
     * Test of requestSetNewDeviceName method, of class CommunicationProcess.
     */
    @Test
    public void testRequestSetNewDeviceName()
    {
        ArgumentCaptor<String>    prefCaptor=ArgumentCaptor.forClass(String.class);
        String                    name;
        
        // Bad device name
        System.out.println("TEST: requestSetNewDeviceName");
        name = "New_Device_Name#$%";
        theInstance.requestSetNewDeviceName(name);
        verify(watchInterface, never()).setPreference(any(), any());
        verify(theView).showErrorDialog(stringCaptor.capture());
        assertEquals("Illegel Watch Name "+name, stringCaptor.getValue());

        // Good flow
        name = "New_Device_Name";
        theInstance.requestSetNewDeviceName(name);
        verify(watchInterface, times(1)).setPreference(prefCaptor.capture(), stringCaptor.capture());
        assertEquals("watchName", prefCaptor.getValue());
        assertEquals(name, stringCaptor.getValue());
    }

    /**
     * Test of requestLoadActivityFromTtbinFile method, of class CommunicationProcess.
     */
    @Test
    public void testRequestLoadActivityFromTtbinFile()
    {
        String                          fileName;
        System.out.println("TEST: requestLoadActivityFromTtbinFile");

        // Mock the ToolBox
        PowerMockito.mockStatic(ToolBox.class);
        
        // Non existing file
        fileName="PietjePuk";
        when(ToolBox.readBytesFromFile(fileName)).thenReturn(null);
        theInstance.requestLoadActivityFromTtbinFile(fileName);
        verify(theView).showErrorDialog(stringCaptor.capture());
        verify(theView, never()).addListItem(any(), any());
        assertEquals("Error loading file: ", stringCaptor.getValue().substring(0, 20));

        // Good flow
        fileName = "src/test/resources/test.ttbin";
        when(ToolBox.readBytesFromFile(fileName)).thenReturn(new byte[]{1, 2, 3});
        when(ttbinReader.readTtbinFile((UsbFile)any())).thenReturn(null);
        theInstance.requestLoadActivityFromTtbinFile(fileName);
        verify(theView).addListItem(dataCaptor.capture(), stringCaptor.capture());
    }

    /**
     * Test of requestWriteDeviceFileToDisk method, of class CommunicationProcess.
     */
    @Test
    public void testRequestWriteDeviceFileToDisk()
    {
        byte[]                  bytes;
        int                     fileId;

        // Mock the ToolBox
        PowerMockito.mockStatic(ToolBox.class);
        
        System.out.println("TEST: requestWriteDeviceFileToDisk");

        // Happy flow
        fileId = 0x00000123;
        when(ToolBox.writeBytesToFile(stringCaptor.capture(), bytesCaptor.capture())).thenReturn(false);
        theInstance.requestWriteDeviceFileToDisk(fileId);
        assertEquals(".\\working\\files\\0x00000123.bin", stringCaptor.getValue());
        bytes=bytesCaptor.getValue();
        assertEquals(3, bytes.length);
        assertEquals(0x00, bytes[0]);
        assertEquals(0x01, bytes[1]);
        assertEquals(0x02, bytes[2]);

        // Error writing file
        fileId = 0x00000123;
        when(ToolBox.writeBytesToFile(stringCaptor.capture(), bytesCaptor.capture())).thenReturn(true);
        theInstance.requestWriteDeviceFileToDisk(fileId);
        verify(theView, times(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error writing file .\\working\\files\\0x00000123.bin", stringCaptor.getValue());

        // Error fetching file from watch
        fileId = 0x00000123;
        // we have to use doReturn.when here iso. when.thenReturn, otherwise null pointer exception...
        doReturn(true).when(watchInterface).readFile(any());
        theInstance.requestWriteDeviceFileToDisk(fileId);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error reading file with ID 0x00000123", stringCaptor.getValue());

        // Non existing file on watch
        fileId = 0x00000001;
        theInstance.requestWriteDeviceFileToDisk(fileId);
        verify(theView, times(3)).showErrorDialog(stringCaptor.capture());
        assertEquals("File with ID 0x00000001 does not exist on watch", stringCaptor.getValue());

        // Error fetching file list
        fileId = 0x00000123;
        when(ToolBox.writeBytesToFile(stringCaptor.capture(), bytesCaptor.capture())).thenReturn(false);
        when(watchInterface.getFileList(any())).thenReturn(null);
        theInstance.requestWriteDeviceFileToDisk(fileId);
        verify(theView, times(4)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error: file list could not be retrieved from watch", stringCaptor.getValue());
    }

    /**
     * Test of requestUploadFile method, of class CommunicationProcess.
     */
    @Test
    public void testRequestUploadFile()
    {
        String                      fileName;
        UsbFile                     file;
        
        System.out.println("TEST: requestUploadFile");
        
        // Good flow
        fileName = "src/test/resources/0x00000123.bin";
        PowerMockito.mockStatic(ToolBox.class);
        when(ToolBox.readBytesFromFile(stringCaptor.capture())).thenReturn(new byte[]{'a', 'b', 'c', 'd'});
        theInstance.requestUploadFile(fileName);
        verify(watchInterface).writeVerifyFile(fileCaptor.capture());
        file=fileCaptor.getValue();
        assertEquals(0x00000123, file.fileId);
        assertEquals(4, file.length);
        assertEquals(fileName, stringCaptor.getValue());
        
        // Illegal file name
        fileName = "src/test/resources/PietjePuk.bin";
        theInstance.requestUploadFile(fileName);
        verify(theView, times(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("The filename 'PietjePuk.bin' does not fit the required format: 0xnnnnnnnn.bin", stringCaptor.getValue());
        
        // Writing to watch fails
        fileName = "src/test/resources/0x00000123.bin";
        when(watchInterface.writeVerifyFile(any())).thenReturn(true);
        theInstance.requestUploadFile(fileName);
        verify(theView, times(2)).appendStatus(stringCaptor.capture());
        assertEquals("Failed!!\n", stringCaptor.getValue());

        // Reading file fails
        fileName = "src/test/resources/0x00000123.bin";
        when(watchInterface.writeVerifyFile(any())).thenReturn(false);
        when(ToolBox.readBytesFromFile(stringCaptor.capture())).thenReturn(null);
        theInstance.requestUploadFile(fileName);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("The filename '0x00000123.bin' could not be read", stringCaptor.getValue());

}

    /**
     * Test of requestDeleteDeviceFileFromWatch method, of class CommunicationProcess.
     */
    @Test
    public void testRequestDeleteDeviceFileFromWatch()
    {
        int fileId;
        
        System.out.println("TEST: requestDeleteDeviceFileFromWatch");

        // Good flow
        fileId = 0x00000123;
        theInstance.requestDeleteDeviceFileFromWatch(fileId);
        verify(watchInterface).deleteFile(fileCaptor.capture());
        verify(theView).appendStatus(stringCaptor.capture());
        assertEquals(fileId, fileCaptor.getValue().fileId);
        assertEquals("File 0x00000123 deleted!", stringCaptor.getValue());
        
        // Non existing file
        fileId = 0x00000001;
        theInstance.requestDeleteDeviceFileFromWatch(fileId);
        verify(theView, atLeast(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("File with ID 0x00000001 does not exist on watch", stringCaptor.getValue());
        
        // Error deleting file
        fileId = 0x00000123;
        when(watchInterface.deleteFile(any())).thenReturn(true);
        theInstance.requestDeleteDeviceFileFromWatch(fileId);
        verify(theView, atLeast(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("File with ID 0x00000123 could not be deleted", stringCaptor.getValue());
        
        // Error reading file info
        fileId = 0x12300123;
        when(watchInterface.getFileList(any())).thenReturn(null);
        theInstance.requestDeleteDeviceFileFromWatch(fileId);
        verify(theView, atLeast(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error retrieving file info from watch on file while deleting 0x12300123", stringCaptor.getValue());
    }

    /**
     * Test of requestShowFile method, of class CommunicationProcess.
     */
    @Test
    public void testRequestShowFile()
    {
        int fileId;
        
        System.out.println("TEST: requestShowFile");

        // Good flow
        fileId = 0x00000123;
        theInstance.requestShowFile(fileId);
        verify(watchInterface).readFile(fileCaptor.capture());
        assertEquals(fileId, fileCaptor.getValue().fileId);
        
        // Illegal file ID
        fileId = WatchInterface.FILEID_INVALID;
        theInstance.requestShowFile(fileId);
        verify(theView).showErrorDialog(stringCaptor.capture());
        assertEquals("Illegal file ID", stringCaptor.getValue());
        
        // Non existing file
        fileId = 0x00000123;
        when(watchInterface.fileExists(fileId)).thenReturn(false);
        theInstance.requestShowFile(fileId);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Requested file does not exist on the watch.", stringCaptor.getValue());
    }

    /**
     * Test of getActivityData method, of class CommunicationProcess.
     */
    @Test
    public void testGetActivityData()
    {
        int             index;
        ActivityData    result;
        
        System.out.println("TEST: getActivityData");

        // Prepare: donwload some activitities
        when(theView.isDownloadMostRecent()).thenReturn(false);
        when(ttbinReader.readTtbinFile((UsbFile)any())).thenReturn(new Activity(new TtbinHeader()));
        theInstance.pushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADACTIVITIES);

        index = 0;
        result = theInstance.getActivityData(index);
        assertNotEquals(null, result);
        index = 3;
        result = theInstance.getActivityData(index);
        assertNotEquals(null, result);
        index = 4;
        result = theInstance.getActivityData(index);
        assertEquals(null, result);
    }

    /**
     * Test of isConnected method, of class CommunicationProcess.
     */
    @Test
    public void testIsConnected()
    {
        boolean expResult;
        boolean result;

        System.out.println("TEST: isConnected");

        // When started, the process is connected
        expResult = true;
        result = theInstance.isConnected();
        assertEquals(expResult, result);
        verify(watchInterface).openConnection();

        Mockito.reset(watchInterface);
        // When created, the process is not connected
        CommunicationProcess instance=new CommunicationProcess(watchInterface, executor, ttbinReader, gpxReader);
        expResult = false;
        result = instance.isConnected();
        assertEquals(expResult, result);
        verify(watchInterface, never()).openConnection();
    }

    /**
     * Test of getDeviceName method, of class CommunicationProcess.
     */
    @Test
    public void testGetDeviceName()
    {
        System.out.println("TEST: getDeviceName");

        String expResult = "Test Watch";
        String result = theInstance.getDeviceName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTrackSmoothing method, of class CommunicationProcess.
     */
    @Test
    public void testSetTrackSmoothing()
    {
        System.out.println("TEST: setTrackSmoothing");
        boolean enabled = true;
        float qFactor = 0.0F;
        theInstance.setTrackSmoothing(enabled, qFactor);
        verify(ttbinReader).setTrackSmoothing(enabled, qFactor);

    }

    /**
     * Test of addRouteFile method, of class CommunicationProcess.
     */
    @Test
    public void testAddRouteFile()
    {
        // Good flow
        System.out.println("TEST: addRouteFile");
        theInstance.deleteAllRouteFiles();
        String name = "testRoute";
        String file = "testFile";
        int index   = 0;
        theInstance.addRouteFile(name, file, index);
        verify(gpxReader).readRouteFromFile(stringCaptor.capture(), any());
        assertEquals(file, stringCaptor.getValue());
        UsbFile usbFile=theInstance.getRouteFile(0);
        assertNotEquals(null, usbFile);
        assertNotEquals(0, usbFile.length);
        
        // File read error
        doReturn(true).when(gpxReader).readRouteFromFile(any(), any());
        theInstance.addRouteFile(name, file, index);
        verify(theView).showErrorDialog(stringCaptor.capture());
        assertEquals("Error reading route file", stringCaptor.getValue());
    }

    /**
     * Test of deleteAllRouteFiles method, of class CommunicationProcess.
     */
    @Test
    public void testDeleteAllRouteFiles()
    {
        System.out.println("TEST: deleteAllRouteFiles");
        theInstance.deleteAllRouteFiles();
        verify(theView).addRoutesToListBox(any(), anyInt());
    }

    /**
     * Test of deleteRouteFile method, of class CommunicationProcess.
     */
    @Test
    public void testDeleteRouteFile()
    {
        System.out.println("TEST: deleteRouteFile");
        theInstance.deleteAllRouteFiles();
        verify(theView, times(1)).addRoutesToListBox(any(), anyInt());
        String name = "testRoute";
        String file = "testFile";
        int index   = 0;
        theInstance.addRouteFile(name, file, index);
        verify(gpxReader).readRouteFromFile(stringCaptor.capture(), any());
        assertEquals(file, stringCaptor.getValue());
        UsbFile usbFile=theInstance.getRouteFile(0);
        verify(theView, times(2)).addRoutesToListBox(any(), anyInt());
        
        // Illegal index
        theInstance.deleteRouteFile(1);
        verify(theView).showErrorDialog(stringCaptor.capture());
        assertEquals("Error while deleting route file", stringCaptor.getValue());

        // Illegal index
        theInstance.deleteRouteFile(-1);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error while deleting route file", stringCaptor.getValue());

        // Good flow
        theInstance.deleteRouteFile(0);
        verify(theView, times(3)).addRoutesToListBox(any(), anyInt());
    }
    

    /**
     * Test of getRouteFile method, of class CommunicationProcess.
     */
    @Test
    public void testGetRouteFile()
    {
        UsbFile result;
        
        System.out.println("TEST: getRouteFile");

        // Fail: no routes
        int index = 0;
        UsbFile expResult = null;
        result = theInstance.getRouteFile(index);
        assertEquals(expResult, result);
        
        // good flow: request added file
        theInstance.deleteAllRouteFiles();
        String name = "testRoute";
        String file = "testFile";
        index   = 0;
        theInstance.addRouteFile(name, file, index);        
        result = theInstance.getRouteFile(index);
        assertNotEquals(null, result);
        assertEquals(0xffffffff, result.fileId);
        assertNotEquals(0, result.length);
        assertNotEquals(null, result.fileData);
    }

    /**
     * Test of clear method, of class CommunicationProcess.
     */
    @Test
    public void testClear()
    {
        System.out.println("TEST: clear");
        theInstance.clear();
        verify(theView).clear();
    }

    /**
     * Test of reportReadProgress method, of class CommunicationProcess.
     */
    @Test
    public void testReportReadProgress()
    {
        System.out.println("TEST: reportReadProgress");
        int bytesRead = 0;

        theInstance.setReadExpectedBytes(100);
        theInstance.reportReadProgress(0);
        verify(theView).setProgress(0);
        theInstance.reportReadProgress(50);
        verify(theView).setProgress(500);
        theInstance.reportReadProgress(10);
        verify(theView).setProgress(600);
        theInstance.reportReadProgress(50);
        verify(theView).setProgress(1100);
    }
}
