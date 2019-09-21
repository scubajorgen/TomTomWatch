/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.usb;

import java.util.List;

import net.studioblueplanet.logger.DebugLogger;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;
import javax.usb.UsbPort;
import javax.usb.UsbConfiguration;
import javax.usb.UsbInterface;
import javax.usb.UsbEndpoint;
import javax.usb.UsbClaimException;
import javax.usb.UsbPipe;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbInterfacePolicy;

/**
 * This class represents the connection to the USB watch. It contains functionality
 * to open en close the connection and to exchange request/response by means
 * of blocks of data
 * @author Jorgen
 */
public class UsbConnection
{
    /**
     * Enum defining the device type
     */
    public enum DeviceType 
    {
        DEVICETYPE_UNKNOWN,
        DEVICETYPE_MULTISPORTS,
        DEVICETYPE_SPARK_MUSIC,
        DEVICETYPE_SPARK_CARDIO
    };    

    public static final int                 VARIABLE_LENGTH=-1;
    
    private UsbDevice                       device;
    private UsbInterface                    iface;
    private DeviceType                      deviceType;
    private byte                            txMessageCounter;
    private byte                            txPrevMessageCounter;
    
    private boolean                         isError;
    private String                          lastError;
    
    private byte[]                          rxBuffer;
    private int                             rxLength;
    
    /**
     * The constructor. Initializes the instance.
     */
    public UsbConnection()
    {
        device              =null;
        iface               =null;
        deviceType          =DeviceType.DEVICETYPE_UNKNOWN;
        txMessageCounter    =0;
        txPrevMessageCounter=-1;
        isError             =false;
        lastError           ="";
    }
    
    /**
     * Try to connect to a TomTom Watch. If successful the variable device
     * will hold the UsbDevice instance, the variable deviceType the type found.
     * If not successful, isError=true and lastError will hold a description.
     */
    public void connect()
    {
        UsbServices         services;
        UsbConfiguration    configuration;

        isError     =false;
        lastError   ="Ok";
        try
        {
            services = UsbHostManager.getUsbServices();

            if (device==null)
            {
                device          =findDevice(services.getRootUsbHub(), (short)0x1390, (short)0x7474);
                if (device!=null)
                {
                    deviceType  =DeviceType.DEVICETYPE_MULTISPORTS;
                    DebugLogger.info("Connected to TomTom Multisport");
                }
            }
            if (device==null)
            {
                device          =findDevice(services.getRootUsbHub(), (short)0x1390, (short)0x7475);
                if (device!=null)
                {
                    deviceType  =DeviceType.DEVICETYPE_SPARK_MUSIC;
                    DebugLogger.info("Connected to TomTom Spark Music/Runner Music/Adventurer");

                }
            }
            if (device==null)
            {
                device          =findDevice(services.getRootUsbHub(), (short)0x1390, (short)0x7477);
                if (device!=null)
                {
                    deviceType  =DeviceType.DEVICETYPE_SPARK_CARDIO;
                    DebugLogger.info("Connected to TomTom Spark Cardio/Runner Cardio");
                }
            }
        }
        catch (UsbException e)
        {
            isError     =true;
            lastError   ="USB Error: "+e.getMessage();
            DebugLogger.error(lastError);
        }

        if (device!=null)
        {
            configuration       = device.getActiveUsbConfiguration();
            iface               = configuration.getUsbInterface((byte) 0);

            try
            {
                // For Linux: force claim if the interface has been claimed by the kernel
                iface.claim(new ForcePolicy());
                DebugLogger.info("USB interface claimed");
            }
            catch(UsbClaimException e)
            {
                isError     =true;
                lastError   ="USB Error: "+e.getMessage();
            }
            catch (UsbException e)
            {
                isError     =true;
                lastError   ="USB Error: "+e.getMessage();
            }
        } 
        else
        {
            isError     =true;
            lastError   ="No valid USB device found";            
        }
        if (isError)
        {
            DebugLogger.error(lastError);
        }
    }
    
    /**
     * Disconnects from the TTWatch
     */
    public void disconnect()
    {
        
        if (iface!=null)
        {
            try
            {
                iface.release();
            }
            catch(UsbClaimException e)
            {
                isError=true;
                lastError="USB claim exception during release: "+e.getMessage();
            }
            catch (UsbException e)
            {
                isError=true;
                lastError="USB exception during release: "+e.getMessage();
            }
            catch (UsbDisconnectedException e)
            {
                isError=true;
                lastError="USB disconnect exception: "+e.getMessage();
            }
      }
      if (isError)
      {
          DebugLogger.error(lastError);
      }
      deviceType    =DeviceType.DEVICETYPE_UNKNOWN;
      device        =null;
    }
    
    
    /* ********************************************************************************************\
    * 
    * USB METHODS - DEBUGGING
    * 
    \* *******************************************************************************************/
    /**
     * Find the device identified by vendorId and productId
     * @param hub
     * @param vendorId
     * @param productId
     * @return 
     */
    @SuppressWarnings("unchecked")
    private UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) 
            {
                return device;
            }
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null)
                {
                    return device;
                }
            }
        }
        return null;
    }    
    
    /**
     * Dumps the specified USB device to stdout.
     * @param device The USB device to dump.
     */
    @SuppressWarnings("unchecked")
    private static void dumpDevice(final UsbDevice device)
    {
        // Dump information about the device itself
        System.out.println(device);
        final UsbPort port = device.getParentUsbPort();
        if (port != null)
        {
            System.out.println("Connected to port: " + port.getPortNumber());
            System.out.println("Parent: " + port.getUsbHub());
        }

        // Dump device descriptor
        System.out.println(device.getUsbDeviceDescriptor());

        // Process all configurations
        for (UsbConfiguration configuration: (List<UsbConfiguration>) device
            .getUsbConfigurations())
        {
            // Dump configuration descriptor
            System.out.println(configuration.getUsbConfigurationDescriptor());

            // Process all interfaces
            for (UsbInterface iface: (List<UsbInterface>) configuration
                .getUsbInterfaces())
            {
                // Dump the interface descriptor
                System.out.println(iface.getUsbInterfaceDescriptor());

                // Process all endpoints
                for (UsbEndpoint endpoint: (List<UsbEndpoint>) iface
                    .getUsbEndpoints())
                {
                    // Dump the endpoint descriptor
                    System.out.println(endpoint.getUsbEndpointDescriptor());
                }
            }
        }

        System.out.println();

        // Dump child devices if device is a hub
        if (device.isUsbHub())
        {
            final UsbHub hub = (UsbHub) device;
            for (UsbDevice child: (List<UsbDevice>) hub.getAttachedUsbDevices())
            {
                dumpDevice(child);
            }
        }
    }    
    
    
    /* ********************************************************************************************\
    * 
    * USB METHODS - Request/Response
    * 
    \* *******************************************************************************************/
    /**
     * This method executes a number of validity checks on the response
     * @param response Response data
     * @param expectedRxMsg Expected message type
     * @param expectedRxLength Expected message length
     * @return True if valid, false if not
     */
    private boolean validateResponse(byte msg, byte[] response, byte expectedRxMsg, int expectedRxLength)
    {
        boolean valid;
        int     length;
        
        valid=true;
        
        length=response[1]&0xff;
        DebugLogger.debug("Validation: response "+response[0]+
                          " length: "            +length+
                          " msgcount: "          +response[2]+"/"+(this.txPrevMessageCounter)+
                          " type:"               +String.format("0x%02x", response[3])+"/"+String.format("0x%02x", expectedRxMsg));
        
        // Byte 0: must be 0x01
        if (response[0]!=0x01)
        {
            valid  =false;
            isError=true;
            lastError="USB: Invalid response";
        }
        
        // Byte 1: remaining length, is including msgcount and type, hence +2.
        if ((expectedRxLength!=VARIABLE_LENGTH) && (length!=expectedRxLength+2))
        {
            valid  =false;
            isError=true;
            lastError="USB: Invalid response length";
        }
        
        // Byte 2: counter value, duplicate from the value in the request 
        if (response[2]!=this.txPrevMessageCounter)
        {
            valid  =false;
            isError=true;
            lastError="USB: Response not synchonized with the request";
        }
     
        // Byte 3: message type
        if (response[3]!=expectedRxMsg)
        {
            valid  =false;
            isError=true;
            lastError="USB: Wrong response type";                
        }
            
        
        return valid;
    }
    
    
    
    /**
     * This message synchronously sends a requests and waits for the response. The method
     * performs a number of checks.
     * @param msg               The request message type
     * @param txData            The data to send
     * @param rxData            The response data
     * @param expectedRxMsg     Expected response ID, usually equal to the request ID. Exception is msg=MSG_READ_FILE_DATA_REQUEST,
     *                          in this case the response is MSG_READ_FILE_DATA_RESPONSE
     * @param expectedRxLength  Expected response length.
     */
    public void sendRequest(byte msg, UsbPacket txData, UsbPacket rxData, byte expectedRxMsg, int expectedRxLength)
    {
        byte[]          txPacket;
        int             i;
        int             packetSize;
        byte            readEndpoint;
        byte            writeEndpoint;
        int             length;
        UsbEndpoint     endpoint;
        UsbPipe         pipe;
        
        // Initialize
        readEndpoint    =(byte)0x00;
        writeEndpoint   =(byte)0x00;
        packetSize      =0;
        isError         =false;
        
        // Length as integeer
        length=(txData.length)&0xff;
        
        // Check if the length of the data in the packet is within limits
        // Packet size is 256, 4 overhead bytes
        if (length>=256-4)
        {
            isError=true;
            lastError="USB Error: tx data to long";
        }
        
        if (!isError)
        {
            // USB Connection parameters
            switch (deviceType)
            {
                case DEVICETYPE_MULTISPORTS:
                    packetSize      =64;
                    writeEndpoint   =(byte)0x05;
                    readEndpoint    =(byte)0x84;
                    break;
                case DEVICETYPE_SPARK_MUSIC:
                case DEVICETYPE_SPARK_CARDIO:
                    packetSize      =256;
                    writeEndpoint   =(byte)0x02;
                    readEndpoint    =(byte)0x81;
                    break;
                default:
                    isError=true;
                    lastError="No valid device found";
            }
 
            if (!isError)
            {
                // Construct the overhead bytes
                txPacket            =new byte[packetSize];
                txPacket[0]         =0x09;                          // request
                txPacket[1]         =(byte)(length+2);              // Length+2
                txPacket[2]         =txMessageCounter;              // Message counter
                txPacket[3]         =msg;                           // Request type
                txPrevMessageCounter=txMessageCounter;
                txMessageCounter++;
                
                // Construct the RX buffer size
                rxBuffer            =new byte[packetSize];
                
                
                // Copy the data 
/*
                i=0;
                while (i<length)
                {
                    txPacket[4+i]=txData.data[i];
                    i++;
                }
*/
                System.arraycopy(txData.data, 0, txPacket, 4, length);

                // Put the request on the line :-)
                endpoint = iface.getUsbEndpoint(writeEndpoint);
                try
                {
                    pipe = endpoint.getUsbPipe();
                    pipe.open();
                    try
                    {
                        int sent = pipe.syncSubmit(txPacket);
                    }
                    finally
                    {
                        pipe.close();
                    }
                }
                catch(UsbException e)
                {
                    isError=true;
                    lastError="USB Error while sending packet: "+e.getMessage();
                }
                catch(UsbDisconnectedException e)
                {
                    isError=true;
                    lastError="USB Error while sending packet: "+e.getMessage();
                }
                catch(UsbNotClaimedException e)
                {
                    isError=true;
                    lastError="USB Error while sending packet: "+e.getMessage();
                }
            }            
        }
        if (!isError)
        {
            // Wait for the response.
            endpoint = iface.getUsbEndpoint(readEndpoint);
            try
            {
                pipe = endpoint.getUsbPipe();
                pipe.open();
                try
                {
                    // Funny enough rxLengh=0 after the call...
                    rxLength = (byte)pipe.syncSubmit(rxBuffer);
                    
                    // Validate the response
                    // 
                    if (validateResponse(msg, rxBuffer, expectedRxMsg, expectedRxLength))
                    {
                        // If valid, copy the response 
                        length=(rxBuffer[1]&0xff)-2;
                        /*
                        i=0;
                        while (i<length)
                        {
                            rxData.data[i]=rxBuffer[i+4];
                            i++;
                        }
                        */
                        System.arraycopy(rxBuffer, 4, rxData.data, 0, length);
                        rxData.length=(byte)length;
                    }
                    else
                    {
                        // isError and lastError are already set
                    }
                    
                }
                finally
                {
                    pipe.close();
                }            
            }
            catch (UsbException e)
            {
                isError=true;
                lastError="USB Error: error while receiving. "+e.getMessage();
            }
        }
        
       
        if (isError)
        {
            DebugLogger.error(lastError);
        }
    }
    
    /**
     * Indicates the result of last method
     * @return True if an error occurred, false if not
     */
    public boolean isError()
    {
        return isError;
    }
    
    /**
     * Get a description of the last error.
     * @return The error description as String
     */
    public String getLastError()
    {
        return lastError;
    }
    
    /**
     * Returns the maximum chunk size when reading files.
     * The chunksize apparently is the buffer size minus 14 bytes.
     * @return The chunk size or -1 if not known
     */
    public int getFileReadChunkSize()
    {
        int chunkSize;
        
        chunkSize=-1;
        switch (deviceType)
        {
            case DEVICETYPE_MULTISPORTS:
                chunkSize=50;
                break;
            case DEVICETYPE_SPARK_MUSIC:
            case DEVICETYPE_SPARK_CARDIO:
                chunkSize=242;
                break;
            default:
                isError=true;
                lastError="No valid device known";
        }
        if (isError)
        {
            DebugLogger.error(lastError);
        }
        return chunkSize;
    }

    /**
     * Returns the maximum chunk size when writing files
     * @return The chunk size or -1 if not known
     */
    public int getFileWriteChunkSize()
    {
        int chunkSize;
        
        chunkSize=-1;
        switch (deviceType)
        {
            case DEVICETYPE_MULTISPORTS:
                chunkSize=54;
                break;
            case DEVICETYPE_SPARK_MUSIC:
            case DEVICETYPE_SPARK_CARDIO:
                chunkSize=246;
                break;
            default:
                isError=true;
                lastError="No valid device known";
        }
        if (isError)
        {
            DebugLogger.error(lastError);
        }
        return chunkSize;
    }    
    
    
    /**
     * Returns the serial number of the device as string.
     * @return The string or null if an error occurred
     */
    public String getDeviceSerialNumber()
    {
        String serial;

        serial=null;
        
        if (device!=null)
        {
            try
            {
                final UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
                serial=device.getString(desc.iSerialNumber());

//                serial=device.getSerialNumberString();
            }
            catch (Exception e)
            {
                //isError=true;
                //lastError="Error getting device serial number";
                DebugLogger.error("Error getting device serial number: "+e.getMessage());
            }
                
        }
        return serial;
    }

    
}
