/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.usb;

import java.util.ArrayList;

import net.studioblueplanet.logger.DebugLogger;

import hirondelle.date4j.DateTime;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.studioblueplanet.generics.ToolBox;
/**
 * This abstract class defines the interface to the watch. It defines the 
 * file handling methods as well as the file definitions. A number of generic
 * methods have been filled in, a number of specific methods need to be 
 * overridden in child classes
 * @author Jorgen
 */
public abstract class WatchInterface
{

    /**
     * This enum defines the file type. The type is defined by the
     * first two bytes (MSBs) of the file ID.
     */
    public enum FileType
    {
        TTWATCH_FILE_ALL                    (0x00000000),
        TTWATCH_FILE_RACE_DATA              (0x00710000),
        TTWATCH_FILE_RACE_HISTORY_DATA      (0x00720000),
        TTWATCH_FILE_HISTORY_DATA           (0x00730000),
        TTWATCH_FILE_LANGUAGE               (0x00810000),
        TTWATCH_FILE_HISTORY_SUMMARY        (0x00830000),
        TTWATCH_FILE_TTBIN_DATA             (0x00910000),    // The ttbin file
        TTWATCH_FILE_TRACKEDACTIVITY        (0x00B10000),    // Tracked activities (steps, calories, distance, heartrate, etc)
        TTWATCH_FILE_TRACKEDACTIVITYTEMP    (0x00B20000),    // Temp tracked activities, used when watch is connected
        TTWATCH_FILE_TRACKEDACTIVITYDAILY   (0x00B30000),    // Tracked activity, per weekday 0x00b3000a - 0x00b3000f
        TTWATCH_FILE_TRACKPLANNING          (0x00B80000),    // Uploaded track
        TTWATCH_FILE_UNKNOWN                (0x00B90000),    // Watch generated protobuf (?) file, one for each track...
        TTWATCH_FILE_WORKOUTS               (0x00BE0000);    // Workouts
        
        
        private final int 	value;
        
        FileType(int value)
        {
            this.value=value;
        }
        
        public int getValue()
        {
            return this.value;
        }
                
    }
    
    
    // Particular file IDs
    public static final int                     FILEID_BLE_FIRMWARE     =0x00000012;
    public static final int                     FILEID_SYSTEM_FIRMWARE  =0x000000f0;
    public static final int                     FILEID_GPSQUICKFIX_DATA =0x00010100;
    public static final int                     FILEID_GPS_FIRMWARE     =0x00010200;
    public static final int                     FILEID_BLE_UPDATE_LOG   =0x00013001;
    public static final int                     FILEID_UPDATE_LOG       =0x00013002;  
    public static final int                     FILEID_LOG              =0x00013100;  
    public static final int                     FILEID_MANIFEST1        =0x00850000;
    public static final int                     FILEID_MANIFEST2        =0x00850001;
    public static final int                     FILEID_PLAYLIST         =0x00880000;
    public static final int                     FILEID_PREFERENCES_XML  =0x00f20000;
    
    public static final int                     FILEID_INVALID          =0xFFFFFFFF;

    public  static final int                    PRODUCTID_UNKNOWN       =-1;

    // The file type mask to be used to mask the file type in the file ID
    protected static final int                  TTWATCH_FILE_TYPE_MASK  =0xFFFF0000;

    
    protected final ArrayList<UsbFile>          fileList;
    
    protected ProgressListener                  listener;
    
    /** The preference file of the watch */
    protected UsbFile                           preferenceFile;
    
    
    /**
     * Constructor
     */
    protected WatchInterface()
    {
        fileList=new ArrayList();
    }
    
    
    /**
     * Opens the USB connection
     * @return True if an error occurred, false if the connection successfully opened
     */
    public boolean openConnection()
    {
        return false;
    }
    
    /**
     * Closes the USB connection
     */
    public void closeConnection()
    {
    }

       
    /**
     * According to ttwatch (Ryan Binns) this is the sequence that should be 
     * called after opening the watch connection
     * @return True if an error occurred, false if successful
     */
    public boolean sendStartupSequence()
    {
        return false;
    }
    
    /**
     * Another message sequence reported by ttwatch
     * @return True if an error occurred, false if successful
     */
    public boolean sendMessageGroup1()
    {
        return false;
    }
    
    /**
     * This method sets the progress listener for file reading and writing. 
     * Set to null if not used. Progress is reported to the listerner.
     * @param listener The listener
     */
    public void setProgressListener(ProgressListener listener)
    {
        this.listener=listener;
    }
    
    
    /**
     * This method checks if a file exists
     * @param fileId Id of the file to check
     * @return True if the file exists or false if not or something
     */
    public abstract boolean fileExists(int fileId);
    
    
    /**
     * This method reads the file with file ID defined in the file
     * instance passed from the watch
     * @param file File defining the file ID. The file data is read into
     *             this file instance
     * @return The file or null if an error occurred
     */
    public abstract boolean readFile(UsbFile file);    
    
    /**
     * Write file to the watch. The file (ID and data) is defined in the 
     * UsbFile passed
     * @param file The file to write
     * @return True if succeeded, false if not
     */
    public abstract boolean writeFile(UsbFile file);
    
    /**
     * This method writes the file, reads it back and verifies it
     * @param file The file to write
     * @return False if the file was correctly written, false if an error 
     *         occurred or the verification failed.
     */
    public boolean writeVerifyFile(UsbFile file)
    {
        boolean error;
        UsbFile readBackFile;
        int     i;
        
        error           =false;
        
        error           =writeFile(file);
        
        if (!error)
        {
            readBackFile=new UsbFile();
            readBackFile.fileId=file.fileId;
            error       =readFile(readBackFile);

        
            if (!error)
            {
                if (file.length==readBackFile.length)
                {
                    i=0;
                    while ((i<file.length) && !error)
                    {
                        if (file.fileData[i]!=readBackFile.fileData[i])
                        {
                            error=true;
                            DebugLogger.error("File verification failed!");
                        }
                        i++;
                    }
                }
                
            }
            else
            {
                DebugLogger.error("Error writing file!");
            }
        }
        else
        {
            DebugLogger.error("Error writing file!");
        }

        return error;
    }

    /**
     * Write the GPS Quickfix file
     * @param quickFixFile The quick fix file data
     * @return True if an error occurred, false if succeeded.
     */
    public boolean writeGpxQuickFixFile(byte[] quickFixFile)
    {
        UsbFile file;
        boolean error;
        
        
        file            =new UsbFile();
        file.fileData   =quickFixFile;
        file.length     =quickFixFile.length;
        file.fileId     =FILEID_GPSQUICKFIX_DATA;
        
//        error=this.deleteFile(file);
        
        // Write the file
        error=this.writeVerifyFile(file);

        // If succeeded, reboot watch
        if (!error)
        {
            error=this.resetGpsProcessor();
        }
        
        return error;
        
    }

    
    
    /**
     * Deletes the file on the watch indicated by the ID in the file 
     * passed. En passant the file data in the file is also deleted
     * @param file File defining the file ID to delete
     * @return True if an error cccurred, false if not
     */
    public abstract boolean deleteFile(UsbFile file);
    
    
    /**
     * This method requests the list of file of given type
     * @param fileType The type of files to request
     * @return The file list or null if something went wrong
     */
    public abstract ArrayList<UsbFile> getFileList(FileType fileType);
            
    /**
     * This method retrieves the time from the watch. 
     * @return The DateTime that represents the watch time in UTC. Or null 
     *         if an error occurred.
     */
    public abstract DateTime getWatchTime();

    
    /**
     * This method reads and returns the preferences from the watch.
     * The XML data is encoded in the UsbFile.fileData;
     * @return The preference file or null if an error occurred
     */
    public UsbFile readPreferences()
    {
        boolean         error;
        InputStream     inStream;
        
 

        // If there is no preference file on the watch, 
        // which occurs when the watch is factory reset,
        // write the default
        if (!this.fileExists(FILEID_PREFERENCES_XML))
        {
            inStream = getClass().getResourceAsStream("/net/studioblueplanet/tomtomwatch/resources/watchpreferences_default.bin"); 
            preferenceFile                  =new UsbFile();
            preferenceFile.fileId           =FILEID_PREFERENCES_XML;
            preferenceFile.fileData         =ToolBox.getBytesFromInputStream(inStream);
            preferenceFile.length           =preferenceFile.fileData.length;
            
            error=this.writeFile(preferenceFile);
        }
        
        preferenceFile                  =new UsbFile();
        preferenceFile.fileId           =FILEID_PREFERENCES_XML;
        error=this.readFile(preferenceFile);

        if (error)
        {
            preferenceFile=null;
        }
        return preferenceFile;
    }
    
    /**
     * Write the preference file to the watch.
     * @return True if an error occurred, false if ok
     */
    public boolean writePreferences()
    {
        boolean error;
        
        error=false;
        
        if (preferenceFile!=null)
        {
            error=this.writeFile(preferenceFile);
        }
        return error;
    } 
    
    
    /**
     * This method returns the preference from the watch XML preference file
     * indicated by the tag. Quick and dirty XML parsing :-)
     * @param tag Tag in the XML file, without the braces
     * @return The preference value as string
     */
    public String getPreference(String tag)
    {
        String                  preference;
        String                  xml;
        Pattern                 pattern;
        Matcher                 matcher;

        
        preference=null;
        
        readPreferences();
        
        if (preferenceFile!=null)
        {
            xml=null;
            try
            {
                xml = new String(preferenceFile.fileData, "ISO-8859-1");
            }
            catch (Exception e)
            {
                DebugLogger.error("Error parsing preferences");
            }

            // No bulky xml decoding, just a regexp to find the watch name
            pattern = Pattern.compile("<"+tag+">(.+?)</"+tag+">");
            matcher = pattern.matcher(xml);
            if (matcher.find())
            {
                preference=matcher.group(1);
                DebugLogger.info("Preference retrieved: "+preference);
            }

            
        }
        return preference;
        
    }
     
    /**
     * Write a preference in the preference file to the watch
     * @param tag Tag of the preference
     * @param value Value of the preference
     * @return False if all went ok, true if an error occurred.
     */
    public boolean setPreference(String tag, String value)
    {
        boolean                 error;
        String                  preferenceFileString;
        String                  xml;
        Pattern                 pattern;
        Matcher                 matcher;
        String                  patternString;
        
        preferenceFileString    =null;        
        error                   =false;
        
        readPreferences();

        if (preferenceFile!=null)
        {
            xml=null;
            try
            {
                xml = new String(preferenceFile.fileData, "ISO-8859-1");
            }
            catch (Exception e)
            {
                DebugLogger.error("Error parsing preferences");
            }

            // No bulky xml decoding, just a regexp to find the watch name
            patternString="(<"+tag+">)(.+?)(</"+tag+">)";
            
            pattern = Pattern.compile(patternString);
            matcher = pattern.matcher(xml);
            if (matcher.find())
            {
                preferenceFileString=matcher.replaceFirst("$1"+value+"$3");
                preferenceFile.fileData =preferenceFileString.getBytes(StandardCharsets.ISO_8859_1);
                preferenceFile.length   =preferenceFileString.length();
            }
            else
            {
                // Serious error: tags not found. Probably file corrupted!!
                error=true;
            }

        }
        else
        {
            error=true;
        }
        
        if (!error)
        {
            writePreferences();
        }
        return error;
    }
    

    /**
     * This method returns the BLE (Bluetooth Low Energy) version 
     * @return The version as string 
     */
    public abstract String readBleVersion();
    
    /**
     * This method returns the firmware version 
     * @return The version as string, or null if an error occurred
     */
    public abstract String readFirmwareVersion();


    /**
     * This method returns the firmware version 
     * @return The version as string, or null if an error occurred
     */
    public abstract int getProductId();    
    
    
    /**
     * Returns the serial number of the device.
     * BECAUSE OF AN ERROR IN USB4JAVA THIS METHOD DOES NOT WORK
     * @return String containing the serial number
     */
    public abstract String getDeviceSerialNumber();

    /**
     * THis method evaluates the file and indicates whether it is a file
     * of indicated file type
     * @param file File to evaluate
     * @param type Type to check against
     * @return True if the file is of indicated type
     */
    public boolean isFileType(UsbFile file, FileType type)
    {
        boolean isFileType;
        
        if((file.fileId&TTWATCH_FILE_TYPE_MASK)==type.getValue())
        {
            isFileType=true;
        }
        else
        {
            isFileType=false;
        }
        return isFileType;
    }
    
    /**
     * Execute device reset
     * @return False if all went OK, true if an error occurred.
     */
    public boolean resetDevice()
    {
        return false;
    }
    
    /**
     * Resets the GPS processor
     * @return False if all went OK, true if an error occurred.
     */
    public boolean resetGpsProcessor()
    {
        return false;
    }
    
    /**
     * This method formats the device
     * @return False if all went OK, true if an error occurred.
     */
    public boolean formatDevice()
    {
        return false;
    }

    
}
