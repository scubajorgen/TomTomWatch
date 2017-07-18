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
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;
/**
 * TTBIN file writer. Writes the TTBIN file. Contains functionality
 * to generate the subdirectories and filename conform TomTom Sports
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
     * This method creates the full path where the ttbin file is going to be
     * stored.
     * @param path The base path
     * @param dateTime The date time of the ttbin file
     * @return The created directory
     */
    private String createPath(String path, DateTime dateTime, String deviceName)
    {
        String  fullPath;
        File    dir;
        boolean ok;
        
        if ((!path.endsWith("/"))&& (!path.endsWith("\\")))
        {
            fullPath=path+"/";
        }
        else
        {
            fullPath=path;
        }
        
        fullPath+=deviceName+"/"+dateTime.format("YYYY-MM-DD")+"/";
        
        dir=new File(fullPath);
        
        if (!dir.exists())
        {
            ok=dir.mkdirs();
        
            if (!ok)
            {
                DebugLogger.error("TTBIN subdirectory not created. Illegal dir or it already exists");
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
     * This method gets the full filename of the TTBIN file
     * @param path          Base path
     * @param deviceName    Device name
     * @param dateTime      Date time of the track
     * @param type          Type of the track
     * @return              The filename or null if not successful
     */
    public String getFullFileName(String path, String deviceName, DateTime dateTime, String type)
    {
        String fileName;
        String fullPath;
        
        fullPath=createPath(path, dateTime, deviceName);
        
        if (fullPath!=null)
        {
            fileName=fullPath+type+"_"+dateTime.format("hh-mm-ss")+".ttbin";
        }
        else
        {
            fileName=null;
        }
        return fileName;
    }
        
    
    /**
     * Writes the UsbFile data to a ttbin file. A suitable directory 
     * is created in the same style as TomTom MySports
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
}
