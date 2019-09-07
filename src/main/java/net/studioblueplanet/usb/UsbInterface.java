/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.usb;

import net.studioblueplanet.logger.DebugLogger;

import hirondelle.date4j.DateTime;
import java.io.UnsupportedEncodingException;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Iterator;
import net.studioblueplanet.generics.ToolBox;


/**
 * This class represents the communication interface to the TomTom Watch.
 * It offers user level functionality. It depends on UsbConnection for the
 * actual communication to the watch.
 * It contains the main functions concern the handling of files on the watch.
 * @author Jorgen
 */
public class UsbInterface extends WatchInterface
{
    // This enum defines the file access mode: for reading or for writing
    public enum FileMode
    {
        FILEMODE_READ, 
        FILEMODE_WRITE
    }

    
    
    
    // List of commands that can be sent to the watch
    private static final byte               MSG_OPEN_FILE_WRITE         =0x02;
    private static final byte               MSG_DELETE_FILE             =0x03;
    private static final byte               MSG_WRITE_FILE_DATA         =0x04;
    private static final byte               MSG_GET_FILE_SIZE           =0x05;
    private static final byte               MSG_OPEN_FILE_READ          =0x06;
    private static final byte               MSG_READ_FILE_DATA_REQUEST  =0x07;
    private static final byte               MSG_READ_FILE_DATA_RESPONSE =0x09;
    private static final byte               MSG_FIND_CLOSE              =0x0a;
    private static final byte               MSG_CLOSE_FILE              =0x0c;
    private static final byte               MSG_UNKNOWN_0D              =0x0d;
    private static final byte               MSG_FORMAT_WATCH            =0x0e;
    private static final byte               MSG_RESET_DEVICE            =0x10;  /* issued after updating firmware,
                                                                                   causes USB disconnect and reconnect
                                                                                   after approximately 90 seconds */
    private static final byte               MSG_FIND_FIRST_FILE         =0x11;
    private static final byte               MSG_FIND_NEXT_FILE          =0x12;
    private static final byte               MSG_GET_CURRENT_TIME        =0x14;
    private static final byte               MSG_UNKNOWN_1A              =0x1a;
    private static final byte               MSG_RESET_GPS_PROCESSOR     =0x1d;
    private static final byte               MSG_UNKNOWN_1F              =0x1f;
    private static final byte               MSG_GET_PRODUCT_ID          =0x20;
    private static final byte               MSG_GET_FIRMWARE_VERSION    =0x21;
    private static final byte               MSG_UNKNOWN_22              =0x22;
    private static final byte               MSG_UNKNOWN_23              =0x23;
    private static final byte               MSG_GET_BLE_VERSION         =0x28;      
    
    
    
    // Private variables 
    private static UsbInterface             theInstance=null;
    private final UsbConnection             connection;
    private final UsbPacket                 txPacket;
    private final UsbPacket                 rxPacket;
    private boolean                         connected;
    private int                             openedFile;
    private final byte[]                    filePacketBuffer;
    
    
    /* ########################################################################################### *\
     # CONSTRUCTOR
    \* ########################################################################################## */
    
    /**
     * Constructor.
     * @param usbConnection The USB connection to use
     */
    public UsbInterface(UsbConnection usbConnection)
    {
        super();
        
        txPacket            =new UsbPacket();
        rxPacket            =new UsbPacket();        
        
        this.connection     =usbConnection;
        
        connected           =false;
        openedFile          =-1;
        
        filePacketBuffer    =new byte[246]; // 242 for reading, 246 for writing 
        
    }
    
    /* ########################################################################################### *\
     # HELPER
    \* ########################################################################################## */
    
    /**
     * This method splits the int value in bytes and adds them to the packet.
     * Big endian.
     * 
     *        0   1   2   3   4   5   6   7   8   9   10 ...
     * Packet #   #   #   #   #   #   #   #   #   #   #  ...
     *                        MSB ... LSB
     *        | offset        |
     *                        | bytes |
     * 
     * @param value  Integer value to encode in the packet
     * @param packet Byte array representing the packet
     * @param offset Offset in the byte array
     * @param bytes  Number of bytes to encode (1-4)
     */
    private void intToPacket(int value, byte[] packet, int offset, int bytes)
    {
        int i;
        i=0;
        while (i<bytes)
        {
            packet[offset+i]=(byte)((value>>(8*(bytes-i-1)))& 0xff);
            i++;
        }
    }
 
    /**
     * This method retrieves bytes from the packet and puts them into an int
     * @param packet The packet bytes
     * @param offset Offset in the packet
     * @param bytes  Number of bytes to encode into an int
     * @return The integer value
     */
    private int packetToInt(byte[] packet, int offset, int bytes)
    {
        int i;
        int value;
        
        value=0;
        i=0;
        while (i<bytes)
        {
            value|=(((int)(packet[bytes-i+offset-1])&0xff)<<(8*i));
            i++;
        }
        return value;
    }
    
    
    /* ########################################################################################### *\
     # USB ACCESS
    \* ########################################################################################## */
    
    /**
     * This method presents a communication sequence that is used by a lot of 
     * file operations to the watch. It sends the command and receives the response.
     * The response is validated:
     * - File ID must match the ID in the request
     * - The watch must not report an error
     * @param command The command is validated 
     * @param fileId  ID of the file to manipulate
     * @return True if an error occurred, false if not
     */
    private boolean executeFileOperation(byte command, int fileId)
    {
        int         id;
        int         errorInt;
        boolean     error;

        error=false;
        
        // Copy the file id in the tx packet, length is 4 bytes
        this.intToPacket(fileId, txPacket.data, 0, 4);
        txPacket.length=4;

        // Only proceed when there is no connection error
        if (!connection.isError())
        {
            connection.sendRequest(command, txPacket, rxPacket, command, 20);
            id      =this.packetToInt(rxPacket.data,  4, 4);        
            errorInt=this.packetToInt(rxPacket.data, 16, 4);

            // The id appears to be 0x00000000...
            if (/*(id!=fileId) ||*/ (errorInt!=0) || connection.isError())
            {
                error=true;
            }
        } 
        else
        {
            error=true;
        }
        return error;   
    }
    

    /**
     * Sends a simple packet without data and doesn't check the response
     * content.
     * @param command Command to send
     * @param responseLength Expected response length
     * @return 
     */
    private boolean executeSendZeroPacket(byte command, int responseLength)
    {
        boolean     error;

        error=false;
        
        // Copy the file id in the tx packet, length is 4 bytes

        txPacket.length=0;

        // Only proceed when there is no connection error
        if (!connection.isError())
        {
            connection.sendRequest(command, txPacket, rxPacket, command, responseLength);
            if (connection.isError())
            {
                error=true;
            }
        } 
        else
        {
            error=true;
        }
        return error;           
    }
    
    
    
    /**
     * Opens the file for reading or writing
     * @param fileId Id of the file
     * @param mode FILEMODE_READ for reading, FILEMODE_WRITE for writing
     */
    private boolean openFile(int fileId, FileMode mode)
    {
        byte        command;
        boolean     error;
        
        if (mode==FileMode.FILEMODE_WRITE)
        {
            command=MSG_OPEN_FILE_WRITE;
        }
        else
        {
            command=MSG_OPEN_FILE_READ;
        }
        
        error=this.executeFileOperation(command, fileId);
        
        if (!error)
        {
            openedFile=fileId;
        }
        else
        {
            openedFile=-1;
            DebugLogger.error("Error opening file");
        }
        
        return error;
    }
    
    /**
     * Closes the opened file, if any
     */
    private boolean closeFile()
    {
        boolean     error;
        
        if (openedFile!=-1)
        {
            error=this.executeFileOperation(MSG_CLOSE_FILE, openedFile);

            if (error)
            {
                DebugLogger.error("Error closing watch file");
            }
            openedFile=-1;
        }        
        else
        {
            error=true;
            DebugLogger.error("Trying to close non opened file");
            
        }
        return error;
    }
    
    
    /**
     * This method returns the size of indicated file on the watch
     * @param fileId Id of the file
     * @return The size of the file or -1 if something went wrong
     */
    private int getFileSize(int fileId)
    {
        int fileSize;
        int id;

        fileSize=-1;
        
        // Encode the fileID in the tx packet
        this.intToPacket(fileId, txPacket.data, 0, 4);
        txPacket.length=4;

        if (!connection.isError())
        {
            connection.sendRequest(MSG_GET_FILE_SIZE, txPacket, rxPacket, MSG_GET_FILE_SIZE, 20);
            fileSize=this.packetToInt(rxPacket.data, 12, 4);
            id      =this.packetToInt(rxPacket.data,  4, 4);
            if ((id==fileId) && !connection.isError())
            {
                DebugLogger.info("USB File size retrieved: "+fileSize);
            }
            else
            {
                DebugLogger.error("Retrieving USB file size: invalid response "+String.format("%08x", id));
            }
        }
        
        return fileSize;
    }
    
    /**
     * Reads a block of data from the file
     * @param length Nmumber of bytes to read [1-242]
     */
    private int readFileData(int length)
    {
        int id;
        int bytesRead;

        
        bytesRead=-1;
        // Check if the file has been opened
        if (openedFile>=0)
        {
            // Copy the file id and length in the tx packet
            this.intToPacket(openedFile, txPacket.data, 0, 4);
            this.intToPacket(length    , txPacket.data, 4, 4);
            txPacket.length=8;

            if (!connection.isError())
            {
                connection.sendRequest(MSG_READ_FILE_DATA_REQUEST, txPacket, rxPacket, MSG_READ_FILE_DATA_RESPONSE, length+8);
                id          =this.packetToInt(rxPacket.data,  0, 4);        
                bytesRead   =this.packetToInt(rxPacket.data,  4, 4);
                
                DebugLogger.debug("USB File chunk read: "+bytesRead);

                if ((id==openedFile) && (bytesRead==length) && (!connection.isError()))
                {
                    System.arraycopy(rxPacket.data, 8, filePacketBuffer, 0, bytesRead);
                }
                else
                {
                    DebugLogger.error("Error reading from the file");
                }

            }
            else
            {
                DebugLogger.error("Error: reading while USB is in error");
                bytesRead=-1;
            }
        }
        else
        {
            
        }
        return bytesRead;
    }
    
    /**
     * Writes a chunk of data to the opened file
     * @param bytes   Bytes to write
     * @param length  Number of bytes to write
     * @return False if succeeded, true if not.
     */
    private boolean writeFileData(byte[] bytes, int length)
    {
        int     id;
        boolean error;
        
        error=false;
        // Check if the file has been opened
        if (openedFile>=0)
        {
            // Copy the file id and length in the tx packet
            this.intToPacket(openedFile, txPacket.data, 0, 4);
            System.arraycopy(bytes, 0, txPacket.data, 4, length);

            
            txPacket.length=(byte)(length+4);

            if (!connection.isError())
            {
                connection.sendRequest(MSG_WRITE_FILE_DATA, txPacket, rxPacket, MSG_WRITE_FILE_DATA, 20);
                id          =this.packetToInt(rxPacket.data,  4, 4);        
                

                if ((!connection.isError() && id==openedFile))
                {
                    DebugLogger.debug("USB File chunk written: "+length);
                }
                else
                {
                    error=true;
                    DebugLogger.error("Error writing to the file");
                }

            }
            else
            {
                DebugLogger.error("Error: writing while USB is in error");
                error=true;
            }
        }
        else
        {
            DebugLogger.error("Error: writing to non-opened file");
            error=true;
        }
        return error;
        
    }
    
    
    /**
     * Find the first or next file on the device
     * @return The file without the file data. Or null if an error occurred.
     */
    private UsbFile findNextFile(boolean isFirst)
    {
        byte    command;
        int     id;
        int     fileSize;
        int     endOfList;
        UsbFile file;

        
        file=null;
        
        // Encode some zero's for the first file
        if (isFirst)
        {
            this.intToPacket(0, txPacket.data, 0, 4);
            this.intToPacket(0, txPacket.data, 4, 4);
            txPacket.length=8;
            command=MSG_FIND_FIRST_FILE;
            
        }
        else
        {
            txPacket.length=0;
            command=MSG_FIND_NEXT_FILE;
        }
        
        if (!connection.isError())
        {
            connection.sendRequest(command, txPacket, rxPacket, command, 20);
            
            if (!connection.isError())
            {
                file            =new UsbFile();
                file.fileId     =this.packetToInt(rxPacket.data,  4, 4);
                file.length     =this.packetToInt(rxPacket.data, 12, 4);
                if (this.packetToInt(rxPacket.data, 16, 4)!=0)
                {
                    file.endOfList=true;
                }
                else
                {
                    file.endOfList=false;
                }
            }
        }
        else
        {
            DebugLogger.error("Error finding file on device");
        }
          
        
        return file;
    }
    
    
    /* ########################################################################################### *\
     # PUBLIC METHODS
    \* ########################################################################################## */
    
    /**
     * Opens the USB connection
     * @return True if not successful, false if successful
     */
    @Override
    public boolean openConnection()
    {
        boolean error;
        
        error   =false;
        connection.connect();
        if (!connection.isError())
        {
            connected=true;
        }
        else
        {
            error=true;
        }
        
        return error;
    }
    
    /**
     * Closes the USB connection
     */
    @Override
    public void closeConnection()
    {
        // Disconnect
        connection.disconnect();
        connected       =false;
        
        // Scratch the cached preference file
        preferenceFile  =null;
    }

       
    /**
     * According to ttwatch (Ryan Binns) this is the sequence that should be 
     * called after opening the watch connection
     * @return True if an error occurred, false if successful
     */
    @Override
    public boolean sendStartupSequence()
    {
        boolean error;
        
        error=false;
        if (!error)
        {
            error=executeSendZeroPacket(MSG_UNKNOWN_0D, 20);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_FIND_CLOSE, 0);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_UNKNOWN_22, 1);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_UNKNOWN_22, 1);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_GET_PRODUCT_ID, 4);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_FIND_CLOSE, 0);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_GET_BLE_VERSION, 4);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_GET_BLE_VERSION, 4);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_UNKNOWN_1F, 4);
        }
        return error;
    }
    
    /**
     * Another message sequence reported by ttwatch
     * @return True if an error occurred, false if successful
     */
    @Override
    public boolean sendMessageGroup1()
    {
        boolean error;
        
        error=false;
        if (!error)
        {
            error=executeSendZeroPacket(MSG_UNKNOWN_1A, 4);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_FIND_CLOSE, 0);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_UNKNOWN_23, 3);
        }
        if (!error)
        {
            error=executeSendZeroPacket(MSG_UNKNOWN_23, 3);
        }
        return error;
    }
    
    
    public boolean fileExists(int fileId)
    {
/*   
        // Next implementation uses the getFileList function. 
        // This function however is slow (takes ~8 seconds)
        // Note: on previous versions like 1.3.255 the function was faster.
        
        int                 id;
        boolean             exists;
        ArrayList<UsbFile>  files;
        Iterator<UsbFile>   it;
        UsbFile             file;

        exists=false;
        
        files=this.getFileList(FileType.TTWATCH_FILE_ALL);
        if (files!=null)
        {
            it=files.iterator();
            while (it.hasNext() && !exists)
            {
                if (it.next().fileId==fileId)
                {
                    exists=true;
                }
            }
        }
*/
        boolean             exists;
        boolean             error;

        // Next implementation is faster: it abuses the file open command
        // to check if the file exists.
        DebugLogger.info(String.format("Trying if file 0x%08x exists...", fileId));
        exists=false;
        error=openFile(fileId, UsbInterface.FileMode.FILEMODE_READ);
        if (!error)
        {
            exists=true;
            DebugLogger.info(String.format("File 0x%08x exists.", fileId));
            error=closeFile();
            if (error)
            {
                DebugLogger.error("Error closing file.");
            }
        }
        else
        {
            DebugLogger.info(String.format("Error opening file: File 0x%08x does not exist.", fileId));
        }

        return exists;
    }
        
    
    
    /**
     * This method reads the file with file ID defined in the file
     * instance passed from the watch
     * @param file File defining the file ID. The file data is read into
     *             this file instance
     * @return The file or null if an error occurred
     */
    @Override
    public boolean readFile(UsbFile file)
    {
        int         fileSize;
        int         bytesToRead;
        int         remainingBytes;
        int         bytesRead;
        int         chunkSize;
        boolean     error;
        
        
        // Point of starting
        remainingBytes  =0;
        chunkSize       =0;
        fileSize        =0;
        error           =false;
        
        
        
        // Request the chunk size for reading. It varies based on watch type
        if (!error)
        {
            chunkSize=connection.getFileReadChunkSize();
            if (connection.isError())
            {
                error=true;
            }
        }
        
        // Open the file
        if (!error)
        {
            error=openFile(file.fileId, UsbInterface.FileMode.FILEMODE_READ);
        }
        
        // Get the file size
        if (!error)
        {
            fileSize        =getFileSize(file.fileId);
            if (fileSize<0)
            {
                error=true;
            }
        }
        
        // Read the file in chunks
        if (!error)
        {
            remainingBytes  =fileSize;
            file.length     =fileSize;
            file.fileData   =new byte[fileSize];
            while (remainingBytes>0 && !error)
            {
                bytesToRead=Math.min(chunkSize, remainingBytes);
                bytesRead=readFileData(bytesToRead);
                if (bytesRead<0)
                {
                    error=true;
                }
                System.arraycopy(this.filePacketBuffer, 0, file.fileData, fileSize-remainingBytes, bytesToRead);
                
                // Report progress
                if ((listener!=null) && (bytesRead>=0))
                {
                    listener.reportReadProgress(bytesRead);
                }
                
                remainingBytes-=bytesToRead;
            }
        }
        
        // Close the file
        if (!error)
        {
            error=closeFile();
        }        

        // Close the USB connection
        if (!error)
        {
            DebugLogger.info("USB File read. ID: "+String.format("0x%08x", file.fileId));
        }
        
        return error;
    }
    
    
    /**
     * Write file to the watch. The file (ID and data) is defined in the 
     * UsbFile passed
     * @param file The file to write
     * @return True if succeeded, false if not
     */
    @Override
    public boolean writeFile(UsbFile file)
    {
        int         bytesToWrite;
        int         remainingBytes;
        int         chunkSize;
        int         fileSize;
        boolean     error;
        
        
        // Point of starting
        chunkSize       =0;
        error           =false;
        
        // Request the chunk size for writing. It varies based on watch type
        if (!error)
        {
            chunkSize=connection.getFileWriteChunkSize();
            if (connection.isError() || (chunkSize<0))
            {
                error=true;
            }
        }

        
        // delete file
/*        
        if (!error)
        {
            error=this.deleteFile(file);
        }
*/        
        // Open the file
        if (!error)
        {
            error=openFile(file.fileId, UsbInterface.FileMode.FILEMODE_WRITE);
        }

        
        // Read the file in chunks
        if (!error)
        {
            fileSize        =file.length;
            remainingBytes  =fileSize;
            

            while (remainingBytes>0 && !error)
            {
                bytesToWrite=Math.min(chunkSize, remainingBytes);
                
                // Copy the
                System.arraycopy(file.fileData, fileSize-remainingBytes, this.filePacketBuffer, 0, bytesToWrite);
                
                error=writeFileData(filePacketBuffer, bytesToWrite);
                
                // Report progress
                if ((listener!=null) && (bytesToWrite>=0) && !error)
                {
                    listener.reportWriteProgress(bytesToWrite);
                }
                
                remainingBytes-=bytesToWrite;
            }
        }
        
        // Close the file
        if (!error)
        {
            error=closeFile();
        }        

        // Close the USB connection
        if (!error)
        {
            DebugLogger.info("USB File written. ID: "+String.format("0x%08x", file.fileId));
        }
        
        return error;
        
    }
    
    /**
     * Deletes the file on the watch indicated by the ID in the file 
     * passed. En passant the file data in the file is also deleted
     * @param file File defining the file ID to delete
     * @return True if an error cccurred, false if not
     */
    @Override
    public boolean deleteFile(UsbFile file)
    {
        boolean     error;

        error=this.executeFileOperation(MSG_DELETE_FILE, file.fileId);
        
        if (!error)
        {
            file.fileData=null;
            DebugLogger.info("USB File deleted. ID: "+String.format("0x%08x", file.fileId));
        }
        else
        {
            DebugLogger.error("Error deleting file "+String.format("0x%08x", file.fileId));
        }
        return error;
    }
    
    
    /**
     * This method requests the list of file of given type
     * @param fileType The type of files to request
     * @return The file list or null if something went wrong
     */
    @Override
    public ArrayList<UsbFile> getFileList(FileType fileType)
    {
        boolean isFirst;
        boolean ready;
        boolean error;
        UsbFile file;
        
        isFirst     =true;
        ready       =false;
        error       =false;
        
        // Remove any existing files from previous download
        this.fileList.clear();
        
        if (!error)
        {
            do 
            {
                file=this.findNextFile(isFirst);
                isFirst=false;

                if (file!=null)
                {
                    // Filter the files based on file type: ALL or indicated file type
                    if (((fileType==FileType.TTWATCH_FILE_ALL) || ((file.fileId & TTWATCH_FILE_TYPE_MASK) ==fileType.getValue())) && 
                        !file.endOfList)
                    {
                        this.fileList.add(file);
                    }
                    ready=file.endOfList;
                }
                else
                {
                    error=true;
                    ready=true;
                }
            }
            while (!ready);
        }

        // Close the USB connection
        if (!error)
        {
            DebugLogger.info("File list retrieved: number "+fileList.size());
        }
        else
        {
            DebugLogger.error("Error retrieving file list");
        }
        
        return this.fileList;
    }
    
    /**
     * This method retrieves the time from the watch. 
     * @return The DateTime that represents the watch time in UTC. Or null 
     *         if an error occurred.
     */
    @Override
    public DateTime getWatchTime()
    {
        DateTime dateTime;
        long     time;
        boolean  error;
        
        dateTime=null;
        error   =false;
        

        if (!error)
        {
            txPacket.length=0;
            connection.sendRequest( MSG_GET_CURRENT_TIME, txPacket, rxPacket,  MSG_GET_CURRENT_TIME, 20);
            
            if (!connection.isError())
            {
                time    =(long)this.packetToInt(rxPacket.data,  0, 4)*1000L;
                dateTime=DateTime.forInstant(time, TimeZone.getTimeZone("UTC"));
            }
            else
            {
                error=true;
                DebugLogger.error("Error retrieving time from the watch");
            }
        }
        
        // Close the USB connection
        if (!error)
        {
//            DebugLogger.info("Time Retrieved (UTC): "+dateTime.format("YYYY-MM-DD hh:mm:ss"));
        }
        
        return dateTime;
    }

    /**
     * This method returns the BLE version 
     * @return The version as string 
     */
    @Override
    public String readBleVersion()
    {
        boolean  error;
        int      version;
        
        version =-1;
        error   =false;
        

        if (!error)
        {
            txPacket.length=0;
            connection.sendRequest( MSG_GET_BLE_VERSION, txPacket, rxPacket,  MSG_GET_BLE_VERSION, 4);
            
            if (!connection.isError())
            {
                version    =this.packetToInt(rxPacket.data,  0, 4);
            }
            else
            {
                error=true;
                DebugLogger.error("Error retrieving ble version");
            }
        }
        
        // Close the USB connection
        if (!error)
        {
            DebugLogger.info("BLE version: "+version);
        }
        
        return Integer.toString(version);
    }    

    
    /**
     * This method returns the firmware version 
     * @return The version as string, or null if an error occurred
     */
    @Override
    public String readFirmwareVersion()
    {
        boolean  error;
        int      major;
        int      minor;
        int      build;
        String   version;
        byte[]   stringBytes;
        
        version =null;
        minor   =0;
        major   =0;
        build   =0;
        error   =false;
        

        if (!error)
        {
            txPacket.length=0;
            connection.sendRequest( MSG_GET_FIRMWARE_VERSION, txPacket, rxPacket,  MSG_GET_FIRMWARE_VERSION, UsbConnection.VARIABLE_LENGTH);
            
            if (!connection.isError())
            {
                try
                {
                    stringBytes=new byte[rxPacket.length];
                    System.arraycopy(rxPacket.data, 0, stringBytes, 0, rxPacket.length);
                    version=new String(stringBytes, "UTF-8");
                    DebugLogger.info("Firmware version: "+version);
                }
                catch(UnsupportedEncodingException e)
                {
                    version="error";
                    DebugLogger.error("Error encoding firmware version");
                }
            }
            else
            {
                error=true;
                DebugLogger.error("Error retrieving firmware version");
            }
        }
        
        
        return version;
    }    


    /**
     * This method returns the firmware version 
     * @return The version as string, or null if an error occurred
     */
    @Override
    public int getProductId()
    {
        boolean  error;
        int      major;
        int      minor;
        int      build;
        byte[]   stringBytes;
        int      productId;
        
        productId   =PRODUCTID_UNKNOWN;
        minor       =0;
        major       =0;
        build       =0;
        error       =false;
        

        if (!error)
        {
            txPacket.length=0;
            connection.sendRequest( MSG_GET_PRODUCT_ID, txPacket, rxPacket,  MSG_GET_PRODUCT_ID, 4);
            
            if (!connection.isError())
            {
                productId=ToolBox.readUnsignedInt(rxPacket.data, 0, 4, false);
                DebugLogger.info(String.format("Product ID retrieved: 0x%08x", productId));
            }
            else
            {
                error=true;
                DebugLogger.error("Error retrieving product ID");
            }
        }
        
        
        return productId;
    }    
    
    
    
    /**
     * Returns the serial number of the device.
     * BECAUSE OF AN ERROR IN USB4JAVA THIS METHOD DOES NOT WORK
     * @return String containing the serial number
     */
    @Override
    public String getDeviceSerialNumber()
    {
        String serial;
        
        serial=null;
        
        if (this.connection!=null)
        {
            serial=connection.getDeviceSerialNumber();
        }

        // WORKAROUND. Due to a bug in the usb4j lib requesting serial number fails.
        // Under windows
        if (serial==null)
        {
            serial="unknown";
        }
        return serial;
    }



    /**
     * Execute device reset
     * @return False if all went OK, true if an error occurred.
     */
    @Override
    public boolean resetDevice()
    {
        boolean  error;
       

        error       =false;
        

        if (!error)
        {
            txPacket.length=0;
            connection.sendRequest( MSG_RESET_DEVICE, txPacket, rxPacket,  MSG_RESET_DEVICE, 0);
            
            if (!connection.isError())
            {

            }
            else
            {
                // This is to be expected: the reboot results in error while receiving the response
                error=true;
                DebugLogger.error("Error resetting device");
            }
        } 
        return error;
    }

    /**
     * Resets the GPS processor
     * @return False if all went OK, true if an error occurred.
     */
    @Override
    public boolean resetGpsProcessor()
    {
        boolean error;
        int     id;
        byte[]  stringBytes;

        error=false;
        
        // Encode the fileID in the tx packet
        txPacket.length=0;

        if (!connection.isError())
        {
            // Expected lenght is max 60, but it seems to vary?
            connection.sendRequest(MSG_RESET_GPS_PROCESSOR, txPacket, rxPacket, MSG_RESET_GPS_PROCESSOR, UsbConnection.VARIABLE_LENGTH);

            if (!connection.isError())
            {
                try
                {
                    stringBytes=new byte[rxPacket.length];
                    System.arraycopy(rxPacket.data, 0, stringBytes, 0, rxPacket.length);
                    // Show the string
                    DebugLogger.info("Reboot Message: "+new String(stringBytes, "ISO-8859-1"));
                }
                catch (Exception e)
                {
                    DebugLogger.error("Error resetting GPS processor");
                }
            }
            else
            {
                error=true;
            }
                 
        }
        else
        {
            error=true;
        }
        return error;        
    }

    
    /**
     * This method formats the device
     * @return False if all went OK, true if an error occurred.
     */
    @Override
    public boolean formatDevice()
    {
        boolean     error;
        int         errorInt;
       

        error       =false;
        

        if (!error)
        {
            txPacket.length=0;
            connection.sendRequest( MSG_FORMAT_WATCH, txPacket, rxPacket,  MSG_FORMAT_WATCH, 20);
            
            if (!connection.isError())
            {
                errorInt=this.packetToInt(rxPacket.data,  16, 4);
                if (errorInt>0)
                {
                    error=true;
                    DebugLogger.error("Error formatting device");
                }
            }
            else
            {
                error=true;
                DebugLogger.error("Error formatting device");
            }
        }  
        return error;
    }
    
}
