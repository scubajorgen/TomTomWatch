/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.usb;

import net.studioblueplanet.usb.UsbConnection;
import net.studioblueplanet.usb.UsbPacket;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;
import static org.mockito.Mockito.*;


import hirondelle.date4j.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;

/**
 *
 * @author jorgen
 */
public class UsbInterfaceTest
{
 
    UsbConnection mockConnection=mock(UsbConnection.class);

    byte    openFile[]          ={0x00, 0x00, 0x00, 0x00, 0x12, 0x34, 0x56, 0x78, 0, 0, 0, 0, 0, 0, 0, 0x00, 0, 0, 0, 0};
    byte    closeFile[]         ={0x00, 0x00, 0x00, 0x00, 0x12, 0x34, 0x56, 0x78, 0, 0, 0, 0, 0, 0, 0, 0x00, 0, 0, 0, 0};
    byte    fileOpOk[]          ={0x00, 0x00, 0x00, 0x00, 0x12, 0x34, 0x56, 0x78, 0, 0, 0, 0, 0, 0, 0, 0x00, 0, 0, 0, 0};
    byte    fileOpError[]       ={0x00, 0x00, 0x00, 0x00, 0x12, 0x34, 0x56, 0x78, 0, 0, 0, 0, 0, 0, 0, 0x00, 0, 0, 0, 1};
    byte    fileSize[]          ={0x00, 0x00, 0x00, 0x00, 0x12, 0x34, 0x56, 0x78, 0, 0, 0, 0, 0, 0, 0, 0x32, 0, 0, 0, 0};
    byte    fileSizeError[]     ={0x12, 0x34, 0x56, 0x78, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
    byte    writeOk[]           ={0x00, 0x00, 0x00, 0x00, 0x12, 0x34, 0x56, 0x78, 0, 0, 0, 0, 0, 0, 0, 0x00, 0, 0, 0, 0};
    byte    time[]              ={0x5D, 0x73, (byte)0xB9, 0x33, 0x12, 0x34, 0x56, 0x78, 0, 0, 0, 0, 0, 0, 0, 0x00, 0, 0, 0, 0};
    byte    bleVersion[]        ={0x78, 0x65, 0x43, 0x21};
    byte    productId[]         ={(byte)0xE0, 0x07, 0x00, 0x00};
    byte    firmwareVersion[]   ="1.7.53".getBytes();
    byte    resetGps[]          ="wait 1 minute before disconnecting USB".getBytes();
    byte    fileList[][]        =
                                {
                                    {0, 0, 0, 0, 0x00, (byte)0x91, 0x00, 0x01, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                                    {0, 0, 0, 0, 0x00, (byte)0x91, 0x00, 0x02, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0},
                                    {0, 0, 0, 0, 0x00, (byte)0x73, 0x00, 0x01, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0},
                                    {0, 0, 0, 0, 0x11, (byte)0x11, 0x11, 0x11, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1}
                                };
    byte    chunks[][]          =
            {
                {0x12, 0x34, 0x56, 0x78, 0, 0, 0, 20,  1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4},
                {0x12, 0x34, 0x56, 0x78, 0, 0, 0, 20,  2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5},
                {0x12, 0x34, 0x56, 0x78, 0, 0, 0, 10,  3, 0, 0, 0, 0, 0, 0, 0, 0, 6}
            };
    byte    fileData[]=
    {
        1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4,
        2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5,
        3, 0, 0, 0, 0, 0, 0, 0, 0, 6
    };
    
    
    public UsbInterfaceTest()
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
        Mockito.reset(mockConnection);
        doReturn(false).when(mockConnection).isError();
        doReturn(20).when(mockConnection).getFileReadChunkSize();
        doReturn(20).when(mockConnection).getFileWriteChunkSize();
        
        // File size
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=fileSize;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x05), anyObject(), anyObject(), anyByte(), anyInt());        

        // Open File
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=openFile;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x06), anyObject(), anyObject(), anyByte(), anyInt());        
        
        // Close File
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=closeFile;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x0c), anyObject(), anyObject(), anyByte(), anyInt());        
        
        // File read chunks
        doAnswer(new Answer()
        {
            private int count=0;
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=chunks[count];
                if (count<2)
                {
                    rxPacket.length=28;
                }
                else
                {
                    rxPacket.length=18;
                }
                count++;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x07), anyObject(), anyObject(), anyByte(), anyInt());     
        
        // Write File
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=writeOk;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x04), anyObject(), anyObject(), anyByte(), anyInt());  

        // Delete file
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=fileOpOk;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x03), anyObject(), anyObject(), anyByte(), anyInt());      
        
        // File size - 1st file
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=fileList[0];
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x11), anyObject(), anyObject(), anyByte(), anyInt());           

        // File size - subsequent files
        doAnswer(new Answer()
        {
            private int count=1;
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=fileList[count];
                rxPacket.length=20;
                count++;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x12), anyObject(), anyObject(), anyByte(), anyInt());          

        // Get watch time
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=time;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x14), anyObject(), anyObject(), anyByte(), anyInt());           
        
        // Get BLE version
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=bleVersion;
                rxPacket.length=4;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x28), anyObject(), anyObject(), anyByte(), anyInt());           
        
        // Get firmware version
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=firmwareVersion;
                rxPacket.length=6;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x21), anyObject(), anyObject(), anyByte(), anyInt());  
        
        // Get product ID
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=productId;
                rxPacket.length=4;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x20), anyObject(), anyObject(), anyByte(), anyInt());     
        
        // Reset GPS processor
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=resetGps;
                rxPacket.length=38;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x1D), anyObject(), anyObject(), anyByte(), anyInt());      
    
        // Format device
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=fileOpOk;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x0E), anyObject(), anyObject(), anyByte(), anyInt());      
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of openConnection method, of class UsbInterface.
     */
    @Test
    public void testOpenConnection()
    {
        boolean expResult;
        boolean result;
        System.out.println("openConnection");
        UsbInterface instance = new UsbInterface(mockConnection);
        
        // Success
        expResult = false;
        result = instance.openConnection();
        assertEquals(expResult, result);
        Mockito.verify(mockConnection, Mockito.times(1)).connect();

        // Fail
        Mockito.reset(mockConnection);
        doReturn(true).when(mockConnection).isError();
        expResult = true;
        result = instance.openConnection();
        assertEquals(expResult, result);
        Mockito.verify(mockConnection, Mockito.times(1)).connect();
    }

    /**
     * Test of closeConnection method, of class UsbInterface.
     */
    @Test
    public void testCloseConnection()
    {
        System.out.println("closeConnection");
        UsbInterface instance = new UsbInterface(mockConnection);

        // Success
        instance.closeConnection();
        Mockito.verify(mockConnection, Mockito.times(1)).disconnect();
    }

    /**
     * Test of sendStartupSequence method, of class UsbInterface.
     */
    @Test
    public void testSendStartupSequence()
    {
        boolean expResult;
        boolean result;
        
        System.out.println("sendStartupSequence");
        UsbInterface instance = new UsbInterface(mockConnection);
        
        // Success
        expResult = false;
        result = instance.sendStartupSequence();
        assertEquals(expResult, result);
        Mockito.verify(mockConnection, Mockito.times(18)).isError();
        Mockito.verify(mockConnection, Mockito.times(9)).sendRequest(anyByte(), anyObject(), anyObject(), anyByte(), anyInt());

        // Fail
        Mockito.reset(mockConnection);
        doReturn(true).when(mockConnection).isError();
        expResult = true;
        result = instance.sendStartupSequence();
        assertEquals(expResult, result);
        Mockito.verify(mockConnection, Mockito.times(1)).isError();
        Mockito.verify(mockConnection, Mockito.times(0)).sendRequest(anyByte(), anyObject(), anyObject(), anyByte(), anyInt());
    
    }

    /**
     * Test of sendMessageGroup1 method, of class UsbInterface.
     */
    @Test
    public void testSendMessageGroup1()
    {
        boolean expResult;
        boolean result;
        
        System.out.println("sendStartupSequence");
        UsbInterface instance = new UsbInterface(mockConnection);

        // Success
        expResult = false;
        result = instance.sendMessageGroup1();
        assertEquals(expResult, result);
        Mockito.verify(mockConnection, Mockito.times(8)).isError();
        Mockito.verify(mockConnection, Mockito.times(4)).sendRequest(anyByte(), anyObject(), anyObject(), anyByte(), anyInt());

        // Fail
        Mockito.reset(mockConnection);
        doReturn(true).when(mockConnection).isError();
        expResult = true;
        result = instance.sendMessageGroup1();
        assertEquals(expResult, result);
        Mockito.verify(mockConnection, Mockito.times(1)).isError();
        Mockito.verify(mockConnection, Mockito.times(0)).sendRequest(anyByte(), anyObject(), anyObject(), anyByte(), anyInt());
    }

    /**
     * Test of fileExists method, of class UsbInterface.
     */
    @Test
    public void testFileExists()
    {
        boolean expResult;
        boolean result;

        System.out.println("fileExists");
        int fileId = 0x12345678;
        UsbInterface instance = new UsbInterface(mockConnection);

        // Good scenario, file exists
        expResult = true;
        result = instance.fileExists(fileId);
        assertEquals(expResult, result);
        Mockito.verify(mockConnection, Mockito.times(2)).sendRequest(anyByte(), anyObject(), anyObject(), anyByte(), anyInt());

        // Good scenario, file does not exist
        Mockito.reset(mockConnection);
        doReturn(false).when(mockConnection).isError();
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=fileSizeError;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x06), anyObject(), anyObject(), anyByte(), anyInt());        
        expResult = false;
        result = instance.fileExists(fileId);
        assertEquals(expResult, result);
        Mockito.verify(mockConnection, Mockito.times(1)).sendRequest(anyByte(), anyObject(), anyObject(), anyByte(), anyInt());

        // Error scenario
        Mockito.reset(mockConnection);
        doReturn(true).when(mockConnection).isError();
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=fileSize;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x06), anyObject(), anyObject(), anyByte(), anyInt());        
        expResult = false;
        result = instance.fileExists(fileId);
        assertEquals(expResult, result);
        Mockito.verify(mockConnection, Mockito.times(0)).sendRequest(anyByte(), anyObject(), anyObject(), anyByte(), anyInt());
    }

    /**
     * Test of readFile method, of class UsbInterface.
     */
    @Test
    public void testReadFile()
    {
        boolean expResult;
        boolean result;
        
        System.out.println("readFile");
        UsbFile file = new UsbFile();
        UsbInterface instance = new UsbInterface(mockConnection);
        
        file.fileId=0x12345678;
        expResult = false;
        result = instance.readFile(file);

        assertEquals(expResult, result);
        assertEquals(0x32, file.length);
        assertEquals(1, file.fileData[0]);
        assertEquals(4, file.fileData[19]);
        assertEquals(2, file.fileData[20]);
        assertEquals(5, file.fileData[39]);
        assertEquals(3, file.fileData[40]);
        assertEquals(6, file.fileData[49]);
    }

    /**
     * Test of writeFile method, of class UsbInterface.
     */
    @Test
    public void testWriteFile()
    {
        boolean expResult;
        boolean result;
        
        System.out.println("writeFile");
        UsbFile file = new UsbFile();
        file.fileId=0x12345678;
        file.length=50;
        file.fileData=fileData;
        UsbInterface instance = new UsbInterface(mockConnection);
      
        expResult = false;
        result = instance.writeFile(file);
        assertEquals(expResult, result);

/*      // TO DO: validation of the rxPackets  
        // Write file
        ArgumentCaptor<UsbPacket> argument=ArgumentCaptor.forClass(UsbPacket.class);
        verify(mockConnection, times(3)).sendRequest(eq((byte)0x04), argument.capture(), anyObject(), anyByte(), anyInt());
        
        java.util.List<UsbPacket>list=argument.getAllValues();
        byte flut[]=argument.getAllValues().get(0).data;
        assertEquals(0x12, argument.getAllValues().get(0).data[0]);
        assertEquals(4, argument.getAllValues().get(0).data[23]);
*/    
    }

    /**
     * Test of deleteFile method, of class UsbInterface.
     */
    @Test
    public void testDeleteFile()
    {
        boolean expResult;
        boolean result;

        System.out.println("deleteFile");
        UsbFile file = new UsbFile();
        file.fileId  = 0x12345678;
        UsbInterface instance = new UsbInterface(mockConnection);
        expResult = false;

        // Good scenario
        expResult = false;
        result = instance.deleteFile(file);
        assertEquals(expResult, result);

        // Watch returns error
        Mockito.reset(mockConnection);
        doReturn(false).when(mockConnection).isError();
        doReturn(20).when(mockConnection).getFileWriteChunkSize();
        // Delete file - error
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=fileOpError;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x03), anyObject(), anyObject(), anyByte(), anyInt());           
        expResult = true;
        result = instance.deleteFile(file);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFileList method, of class UsbInterface.
     */
    
    @Test
    public void testGetFileList1()
    {
        WatchInterface.FileType fileType;
        ArrayList<UsbFile> result;
        
        System.out.println("getFileList");
        UsbInterface instance = new UsbInterface(mockConnection);
        
        // Good scenario
        fileType = WatchInterface.FileType.TTWATCH_FILE_ALL;
        result = instance.getFileList(fileType);
        
        verify(mockConnection, times(1)).sendRequest(eq((byte)0x11), anyObject(), anyObject(), anyByte(), anyInt());
        verify(mockConnection, times(3)).sendRequest(eq((byte)0x12), anyObject(), anyObject(), anyByte(), anyInt());
        assertEquals(3, result.size());
        assertEquals(0x00910001, result.get(0).fileId);
        assertEquals(256, result.get(0).length);
        assertEquals(0x00910002, result.get(1).fileId);
        assertEquals(257, result.get(1).length);
        assertEquals(0x00730001, result.get(2).fileId);
        assertEquals(258, result.get(2).length);
    }

    
    @Test
    public void testGetFileList2()
    {
        WatchInterface.FileType fileType;
        ArrayList<UsbFile> result;
        
        System.out.println("getFileList");
        UsbInterface instance = new UsbInterface(mockConnection);
        
        // Good scenario - filter
        fileType = WatchInterface.FileType.TTWATCH_FILE_TTBIN_DATA;
        result = instance.getFileList(fileType);
        
        verify(mockConnection, times(1)).sendRequest(eq((byte)0x11), anyObject(), anyObject(), anyByte(), anyInt());
        verify(mockConnection, times(3)).sendRequest(eq((byte)0x12), anyObject(), anyObject(), anyByte(), anyInt());
        assertEquals(2, result.size());
        assertEquals(0x00910001, result.get(0).fileId);
        assertEquals(256, result.get(0).length);
        assertEquals(0x00910002, result.get(1).fileId);
        assertEquals(257, result.get(1).length);
    }
    
    /**
     * Test of getWatchTime method, of class UsbInterface.
     */
    
    @Test
    public void testGetWatchTime()
    {
        System.out.println("getWatchTime");
        UsbInterface instance = new UsbInterface(mockConnection);
        
        String expResult = new DateTime("2019-09-07 14:05:39").format("yyyy-MM-dd hh:mm:ss");
        String result = instance.getWatchTime().format("yyyy-MM-dd hh:mm:ss");
        assertEquals(expResult, result);
    }

    
    /**
     * Test of readBleVersion method, of class UsbInterface.
     */
    @Test
    public void testReadBleVersion()
    {
        System.out.println("readBleVersion");
        UsbInterface instance = new UsbInterface(mockConnection);

        // Good scenario
        String expResult = "2019902241";
        String result = instance.readBleVersion();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of readFirmwareVersion method, of class UsbInterface.
     */

    @Test
    public void testReadFirmwareVersion()
    {
        System.out.println("readFirmwareVersion");
        UsbInterface instance = new UsbInterface(mockConnection);

        // Good scenario
        String expResult = "1.7.53";
        String result = instance.readFirmwareVersion();
        assertEquals(expResult, result);
    }

    
    /**
     * Test of getProductId method, of class UsbInterface.
     */
    @Test
    public void testGetProductId()
    {
        System.out.println("getProductId");
        UsbInterface instance = new UsbInterface(mockConnection);

        // Good scenario
        int expResult = 0xE0070000;
        int result = instance.getProductId();
        assertEquals(expResult, result);

    }
    
    /**
     * Test of getDeviceSerialNumber method, of class UsbInterface.
     */
    @Test
    public void testGetDeviceSerialNumber()
    {
        System.out.println("getDeviceSerialNumber");
        UsbInterface instance = new UsbInterface(mockConnection);

        // Good scenario
        Mockito.reset(mockConnection);
        doReturn("1234").when(mockConnection).getDeviceSerialNumber();

        String expResult = "1234";
        String result = instance.getDeviceSerialNumber();
        assertEquals(expResult, result);
        
        // Fail
        Mockito.reset(mockConnection);
        doReturn(null).when(mockConnection).getDeviceSerialNumber();

        expResult = "unknown";
        result = instance.getDeviceSerialNumber();
        assertEquals(expResult, result);        
    }

    /**
     * Test of resetDevice method, of class UsbInterface.
     */
   
    @Test
    public void testResetDevice()
    {
        boolean expResult;
        boolean result;
        
        System.out.println("resetDevice");
        UsbInterface instance = new UsbInterface(mockConnection);

        // Good scenario
        expResult = false;
        result = instance.resetDevice();
        assertEquals(expResult, result);
        verify(mockConnection, times(1)).sendRequest(eq((byte)0x10), anyObject(), anyObject(), anyByte(), anyInt()); 

        // Call returns error
        Mockito.reset(mockConnection);
        doReturn(true).when(mockConnection).isError();
        expResult = true;
        result = instance.resetDevice();
        assertEquals(expResult, result);
        verify(mockConnection, times(1)).sendRequest(eq((byte)0x10), anyObject(), anyObject(), anyByte(), anyInt()); 
    }

    /**
     * Test of resetGpsProcessor method, of class UsbInterface.
     */
   
    @Test
    public void testResetGpsProcessor()
    {
        System.out.println("resetGpsProcessor");
        UsbInterface instance = new UsbInterface(mockConnection);

        // Good scenario
        boolean expResult = false;
        boolean result = instance.resetGpsProcessor();
        assertEquals(expResult, result);
    }

    /**
     * Test of formatDevice method, of class UsbInterface.
     */
    
    @Test
    public void testFormatDevice()
    {
        boolean expResult;
        boolean result;
        
        System.out.println("formatDevice");
        UsbInterface instance = new UsbInterface(mockConnection);
        
        // Good scenario
        expResult = false;
        result = instance.formatDevice();
        assertEquals(expResult, result);
        
         // Format failed
        Mockito.reset(mockConnection);
        doReturn(false).when(mockConnection).isError();
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                Object[]    args = invocation.getArguments();
                UsbPacket   rxPacket;
                rxPacket=((UsbPacket)args[2]);
                rxPacket.data=fileOpError;
                rxPacket.length=20;
                return null; // void method, so return null
            }
        }).when(mockConnection).sendRequest(eq((byte)0x0E), anyObject(), anyObject(), anyByte(), anyInt());   
        expResult = true;
        result = instance.formatDevice();
        assertEquals(expResult, result);       
    }
   
}
