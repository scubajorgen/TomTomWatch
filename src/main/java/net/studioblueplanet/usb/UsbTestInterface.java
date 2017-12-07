/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.usb;


import com.google.gson.Gson;
import net.studioblueplanet.generics.ToolBox;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hirondelle.date4j.DateTime;
import java.util.ArrayList;
import java.util.TimeZone;
import net.studioblueplanet.logger.DebugLogger;

/**
 * This class implements a Usb simulation. All files are read/written/deleted 
 * from a directory on disk instead of the TomTom Watch. It assumes a directory
 * containing the watch files and a versions.json file containing the version
 * information
 * @author Jorgen
 */
public class UsbTestInterface extends WatchInterface
{
    public static class Versions
    {
        public String serialNumber;
        public int    productId;
        public String firmwareVersion;
        public String bleVersion;
        
        public String serialize() 
        {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
        
        public void deserialize(String string)
        {
            Gson gson=new Gson();
        }
    }


    private static UsbTestInterface     theInstance;
    
    private static String               simulationPath;
    
    private static Versions             versions=null;
    
    /**
     * Private constructor
     */
    private UsbTestInterface()
    {
        super();
        
    }
    
   
    /**
     * This method returns the one and only instance of this Singleton class
     * @param path The path where to find the simulation files
     * @return The instance
     */
    public static UsbTestInterface getInstance(String path)
    {
        Gson    gson;
        String  fileName;
        
        if (theInstance==null)
        {
            theInstance=new UsbTestInterface();
        }
        simulationPath  =path;
        fileName        =path+"versions.json";
        
        gson=new Gson();
        try
        {
            versions=gson.fromJson(new FileReader(fileName), Versions.class);
        }
        catch (FileNotFoundException e)
        {
            DebugLogger.error("Error reading json file ");
        }
        return theInstance;
    }
    
    
    /**
     * This method checks if a file exists
     * @param fileId Id of the file to check
     * @return True if the file exists or false if not or something
     */
    public boolean fileExists(int fileId)
    {
        String fileName;
        File   file;
        
        fileName        =simulationPath+String.format("0x%08x.bin", fileId);

        file=new File(fileName);
        
        return file.exists();
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
        boolean error;
        String  fileName;
        
        error           =false;

        fileName        =simulationPath+String.format("0x%08x.bin", file.fileId);
        
        file.fileData   =ToolBox.readBytesFromFile(fileName);
        file.length     =file.fileData.length; 
        
        // Report progress
        if ((listener!=null) && (file.length>=0))
        {
            listener.reportReadProgress(file.length);
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
        boolean error;
        String  fileName;
        File    diskFile;
        
        error           =false;

        fileName        =simulationPath+String.format("0x%08x.bin", file.fileId);

        // If the file exists, delete it. 
        diskFile=new File(fileName);
        if (diskFile.exists())
        {
            deleteFile(file);
        }
        
        ToolBox.writeBytesToFile(fileName, file.fileData);
        
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
        ArrayList<UsbFile>  usbFiles;
        UsbFile             usbFile;
        File                directory;
        File[]              fileList; 
        int                 i;
        String              fileName;
        Pattern             pattern;
        Matcher             matcher;
        String              idString;
        int                 id;
        
        usbFiles            =new ArrayList();
        directory           =new File(simulationPath);
        fileList            = directory.listFiles();


        i=0;
        while (i<fileList.length)
        {
            if (fileList[i].isFile())
            {
                fileName    =fileList[i].getName();

                // Create a Pattern object
                pattern     = Pattern.compile("^(0x)([0-9a-f]{8})([.]bin)$");

                // Now create matcher object.
                matcher     = pattern.matcher(fileName.toLowerCase());
                if (matcher.find()) 
                {
                    idString=matcher.group(2);
                    id      =Integer.parseInt(idString, 16);
                    
                    if ((fileType==FileType.TTWATCH_FILE_ALL) || ((id & WatchInterface.TTWATCH_FILE_TYPE_MASK)==fileType.getValue()))
                    {
                        usbFile             =new UsbFile();
                        usbFile.fileId      =id;
                        usbFile.length      =(int)fileList[i].length();
                        usbFiles.add(usbFile);
                    }
                }
            }        
            i++;
        }
        
        
        
        return usbFiles;
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
        boolean error;
        String  fileName;
        File    diskFile;
        
        error           =false;

        fileName        =simulationPath+String.format("0x%08x.bin", file.fileId);
        
        diskFile        =new File(fileName);
        
        try
        {
            DebugLogger.info("Deleting file "+fileName);
            if (!diskFile.delete())
            {
                DebugLogger.error("Error deleting file");
                error=true;        
            }
        }
        catch (Exception e)
        {
            DebugLogger.error("Error deleting file: "+e.getMessage());
            error=true;
        }
        return error;
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
        
        dateTime=DateTime.now(TimeZone.getTimeZone("UTC"));
        
        return dateTime;
    }

    
    /**
     * This method returns the BLE version 
     * @return The version as string 
     */
    @Override
    public String readBleVersion()
    {
        String version;
        
        if (versions!=null)
        {
            version=versions.bleVersion;
        }
        else
        {
            version="unknown";
        }
        return version;
    }
    
    /**
     * This method returns the firmware version 
     * @return The version as string, or null if an error occurred
     */
    @Override
    public String readFirmwareVersion()
    {
        String version;
        
        if (versions!=null)
        {
            version=versions.firmwareVersion;
        }
        else
        {
            version="unknown";
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
        int id;
        
        if (versions!=null)
        {
            id=versions.productId;
        }
        else
        {
            id=WatchInterface.PRODUCTID_UNKNOWN;
        }
        return id;
    }
    
    
    /**
     * Returns the serial number of the device.
     * BECAUSE OF AN ERROR IN USB4JAVA THIS METHOD DOES NOT WORK
     * @return String containing the serial number
     */
    @Override
    public  String getDeviceSerialNumber()
    {
        String serial;
        
        if (versions!=null)
        {
            serial=versions.serialNumber;
        }
        else
        {
            serial="unknown";
        }
        return serial;
    }
    
    
}


