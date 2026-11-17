/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.generics.ToolBox;

import hirondelle.date4j.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.json.JSONObject;

/**
 * TTBIN file writer. Writes the TTBIN file. Contains functionality
 * to generate the subdirectories and filename.
 * Layout: {basePath}/{deviceName}/{Type}_{YYYY-MM-DD}_{hh-mm-ss}.ttbin
 * @author jorgen.van.der.velde
 */
public class TtbinFileWriter
{
    private static TtbinFileWriter      theInstance;
    
    /**
     * Constructor
     */
    private TtbinFileWriter()
    {
        
    }
    
    /**
     * Ensures the path ends with a slash.
     * @param path Path to normalize
     * @return Path ending with /
     */
    private String normalizeBasePath(String path)
    {
        if ((!path.endsWith("/"))&& (!path.endsWith("\\")))
        {
            return path+"/";
        }
        return path;
    }
    
    /**
     * This method creates the full path where the ttbin/export file is going to be
     * stored: {basePath}/{deviceName}/
     * @param path The base path
     * @param deviceName Device name subdirectory
     * @return The created directory path, or null if creation failed
     */
    private String createPath(String path, String deviceName)
    {
        String  fullPath;
        File    dir;
        boolean ok;
        
        fullPath=normalizeBasePath(path);
        
        fullPath+=deviceName+"/";
        
        dir=new File(fullPath);
        
        if (!dir.exists())
        {
            ok=dir.mkdirs();
        
            if (!ok)
            {
                DebugLogger.error("TTBIN subdirectory not created. Illegal dir or it already exists");
                return null;
            }
        }        
        return fullPath;
    }
    
    
    
    
    /**
     * Returns the one and only instance of this class (Singleton)
     * @return The instance
     */
    public static TtbinFileWriter getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new TtbinFileWriter();
        }
        return theInstance;
    }

    /**
     * Builds the file stem without extension: {Type}_{YYYY-MM-DD}_{hh-mm-ss}
     * @param dateTime Date time of the track
     * @param type Activity type description (e.g. Gym, Running)
     * @return File stem
     */
    public String getActivityFileStem(DateTime dateTime, String type)
    {
        return type+"_"+dateTime.format("YYYY-MM-DD")+"_"+dateTime.format("hh-mm-ss");
    }
            
    
    /**
     * This method gets the full filename of the TTBIN file
     * @param path          Base path
     * @param deviceName    Device name
     * @param dateTime      Date time of the track
     * @param type          Type of the track
     * @return              The filename or null if not successful
     */
    public String getFullFileName(String path, String deviceName, DateTime dateTime, String type)
    {
        return getExportFullFileName(path, deviceName, dateTime, type, "ttbin");
    }

    /**
     * Builds a full export path under {basePath}/{deviceName}/ with the flattened
     * activity stem and given extension (without leading dot).
     * @param basePath   Base path (ttbin or gpx directory)
     * @param deviceName Device name subdirectory
     * @param dateTime   Date time of the track
     * @param type       Activity type description
     * @param extension  File extension without dot (ttbin, gpx, tcx)
     * @return Full path or null if directory could not be created
     */
    public String getExportFullFileName(String basePath, String deviceName, DateTime dateTime, String type, String extension)
    {
        String fullPath;
        
        fullPath=createPath(basePath, deviceName);
        
        if (fullPath!=null)
        {
            return fullPath+getActivityFileStem(dateTime, type)+"."+extension;
        }
        return null;
    }
        
    
    /**
     * Writes the UsbFile data to a ttbin file. A suitable directory 
     * is created under {base}/{deviceName}/
     * @param fileName Name of the file to write to. Use getFullFilename()
     * @param file The file containing the raw data
     * @return True if an error occurred, false if all went ok
     */
    public boolean writeTtbinFile(String fileName, UsbFile file)
    {
        FileOutputStream    ttbinFile;
        boolean             error;
        
        error           =false;
        ttbinFile       =null;
        
        
        try
        {
            ttbinFile   =new FileOutputStream(fileName);
        }
        catch(FileNotFoundException e)
        {
            error=true;
            DebugLogger.error("Error writing file "+fileName+". "+e.getMessage());
        }
        
        if (ttbinFile!=null)
        {
            try
            {
                ttbinFile.write(file.fileData);
                ttbinFile.close();
            }
            catch (IOException e)
            {
                DebugLogger.error("Error writing file"+fileName+". "+e.getMessage());
            }
        }
              
        return error;
    }

    /**
     * This method verifies the written file against the file data in file
     * @param fileName Name of the file to check
     * @param file File containing the data to check against
     * @return True if an error occurred, false if verification successful
     */
    public boolean verifyTtbinFile(String fileName, UsbFile file)
    {
        boolean             error;
        byte[]              inputBytes;
/*        
        RandomAccessFile    ttbinFile;
        int                 fileSize;
        
        error=false;

        try
        {
            ttbinFile   = new RandomAccessFile(fileName, "r");
            fileSize    =(int)ttbinFile.length();
            inputBytes  = new byte[fileSize];
            ttbinFile.readFully(inputBytes);      
            if (fileSize==file.length)
            {
                if (!Arrays.equals(file.fileData, inputBytes))
                {
                    error=true;
                }
            }
            if (error)
            {
                DebugLogger.error("Error verifying ttbin file. Files not identical");
            }
            
        }
        catch (FileNotFoundException e)
        {
            DebugLogger.error("Error verifying ttbin file: "+e.getMessage());
            error=true;
        }
        catch (IOException e)
        {
            DebugLogger.error("Error verifying ttbin file: "+e.getMessage());
            error=true;
        }
*/

        error           =false;
        inputBytes      =ToolBox.readBytesFromFile(fileName);
        if (inputBytes!=null)
        {
            if (inputBytes.length==file.length)
            {
                if (!Arrays.equals(file.fileData, inputBytes))
                {
                    error=true;
                }                
            }
            else
            {
                error=true;
            }
            if (error)
            {
                DebugLogger.error("Error verifying ttbin file. Files not identical");
            }
        }
        else
        {
            error=true;
        }
        return error;
    }
    
    
    public boolean writeTtbinMetadataFile(String ttbinFileName, ActivityData data)
    {
        boolean     error;
        JSONObject  jsonObject;
        error=false;
        
        jsonObject=new JSONObject();
        
        jsonObject.put("ttbinFile", new File(ttbinFileName).getName());
        jsonObject.put("deviceName", data.activity.getDeviceName());
        jsonObject.put("deviceSerial", data.activity.getDeviceSerialNumber());
        
        System.out.println(jsonObject.toString());
        
        
        return error;
    }
}
