/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.ttbin;

import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.usb.UsbFile;

import hirondelle.date4j.DateTime;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.TimeZone;
import java.util.ArrayList;

/**
 * This class is responsible for reading a TomTom ttbin file. The file consists
 * of records. The first record is the header and defines the content of the 
 * file.
 * First byte of each record is the 'tag', defining the type of record. 
 * The header defines the tags with corresponding record length of the records
 * that are supported by the watch (can be encountered in the file)
 * @author Jorgen
 */
public class TomTomReader
{


    private static TomTomReader                     theInstance;
    private TtbinHeader                             header;
    private byte[]                                  bytes;
    private Activity                                activity;   
    private boolean                                 trackSmoothingEnabled;
    private float                                   trackSmoothingQFactor;
    
    
    /**
     * Constructor
     */
    private TomTomReader()
    {
        bytes                   =new byte[TtbinHeader.HEADERLENGTH];
        
        // Default: no track smoothing
        trackSmoothingEnabled   =false;
        trackSmoothingQFactor   =2.0f;
    }
    
    /**
     * Get the one and only instance of this class
     * @return The singleton instance
     */
    public static TomTomReader getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new TomTomReader();
        }
        return theInstance;
    }
    
    
    public void setTrackSmoothing(boolean enabled, float qFactor)
    {
        this.trackSmoothingEnabled   =enabled;
        this.trackSmoothingQFactor  =qFactor;
    }
    
    /**
     * This method reads the ttbin file header
     * @param in The file input stream
     * @throws IOException 
     */
    private void readHeader(InputStream in) throws IOException
    {
        int                 i;
        TtbinRecordLength   length;
        
        header=new TtbinHeader();
/*       
        in.read(bytes, 0, TtbinHeader.HEADERLENGTH);
                
        // fixed fields
        header.tag                  =ToolBox.readUnsignedInt(bytes, 0, 1, true);
        header.fileVersion          =ToolBox.readUnsignedInt(bytes, 1, 2, true);
        header.firmwareVersion[0]   =ToolBox.readUnsignedInt(bytes, 3, 1, true);
        header.firmwareVersion[1]   =ToolBox.readUnsignedInt(bytes, 4, 1, true);
        header.firmwareVersion[2]   =ToolBox.readUnsignedInt(bytes, 5, 1, true);
        header.productId            =ToolBox.readUnsignedInt(bytes, 6, 2, true);
        header.startTime            =ToolBox.readUnsignedInt(bytes, 8, 4, true);
        header.softwareVersion      =ToolBox.readString(bytes, 12, 16);
        header.gpsFirmwareVersion   =ToolBox.readString(bytes, 28, 80);
        header.watchTime            =ToolBox.readUnsignedInt(bytes, 108, 4, true);
        header.localTimeOffset      =ToolBox.readUnsignedInt(bytes, 112, 4, true);
        header.reserved             =ToolBox.readUnsignedInt(bytes, 116, 1, true);
        header.recordLengthCount    =ToolBox.readUnsignedInt(bytes, 117, 1, true);
*/


        // fixed fields
        in.read(bytes, 0, 1);
        header.tag                  =ToolBox.readUnsignedInt(bytes, 0, 1, true);
        in.read(bytes, 0, 2);
        header.fileVersion          =ToolBox.readUnsignedInt(bytes, 0, 2, true);

        // Version 0x09 and earlier (0x07 has been tested)
        if (header.fileVersion<=0x09)
        {
            in.read(bytes, 0, 3);
            header.firmwareVersion[0]   =ToolBox.readUnsignedInt(bytes, 0, 1, true);
            header.firmwareVersion[1]   =ToolBox.readUnsignedInt(bytes, 1, 1, true);
            header.firmwareVersion[2]   =ToolBox.readUnsignedInt(bytes, 2, 1, true);
        }
        // Version 0x0A
        else if (header.fileVersion==0x0A)
        {
            // in version 0x0A the version numbers seem to be two bytes
            in.read(bytes, 0, 6);
            header.firmwareVersion[0]   =ToolBox.readUnsignedInt(bytes, 0, 2, true);
            header.firmwareVersion[1]   =ToolBox.readUnsignedInt(bytes, 2, 2, true);
            header.firmwareVersion[2]   =ToolBox.readUnsignedInt(bytes, 4, 2, true);
        }

        in.read(bytes, 0, 2);
        header.productId            =ToolBox.readUnsignedInt(bytes, 0, 2, true);
        in.read(bytes, 0, 4);
        header.startTime            =ToolBox.readUnsignedInt(bytes, 0, 4, true);
        in.read(bytes, 0, 16);
        header.softwareVersion      =ToolBox.readString(bytes, 0, 16);
        in.read(bytes, 0, 80);
        header.gpsFirmwareVersion   =ToolBox.readString(bytes, 0, 80);
        in.read(bytes, 0, 4);
        header.watchTime            =ToolBox.readUnsignedInt(bytes, 0, 4, true);
        in.read(bytes, 0, 4);
        header.localTimeOffset      =ToolBox.readUnsignedInt(bytes, 0, 4, true);
        in.read(bytes, 0, 1);
        header.reserved             =ToolBox.readUnsignedInt(bytes, 0, 1, true);
        in.read(bytes, 0, 1);
        header.recordLengthCount    =ToolBox.readUnsignedInt(bytes, 0, 1, true);

        // The record tags
        i=0;
        while (i<header.recordLengthCount)
        {
            in.read(bytes, 0, TtbinHeader.LENGTHITEMLENGTH);
            length          =new TtbinRecordLength();
            length.tag      =ToolBox.readUnsignedInt(bytes, 0, 1, true);
            length.length   =ToolBox.readUnsignedInt(bytes, 1, 2, true);
            header.recordLengths.add(length);
            i++;
        }
    }
    
    /**
     * Dump header to DebugLogger, for debugging purposes
     */
    private void dumpHeader()
    {
        int i;
        DebugLogger.debug("TTBIN FILE HEADER");
        DebugLogger.debug("Tag                   "+String.format("0x%02x", header.tag));
        DebugLogger.debug("File version          "+header.fileVersion);
        DebugLogger.debug("Firmware version      "+header.firmwareVersion[0]+"."+header.firmwareVersion[1]+"."+header.firmwareVersion[2]);
        DebugLogger.debug("ProductId             "+header.productId);
        DebugLogger.debug("Start Time            "+DateTime.forInstant((long)header.startTime*1000, TimeZone.getDefault()).format("DD-MM-YYYY hh:mm:ss"));
        DebugLogger.debug("Software Version      "+header.softwareVersion);
        DebugLogger.debug("GPS Firmware Version  "+header.gpsFirmwareVersion);
        DebugLogger.debug("Watch Time            "+DateTime.forInstant((long)header.watchTime*1000, TimeZone.getDefault()).format("DD-MM-YYYY hh:mm:ss"));
        DebugLogger.debug("Local time offset (s) "+header.localTimeOffset);
        DebugLogger.debug("Reserved              "+header.reserved);
        DebugLogger.debug("Record types          "+header.recordLengthCount);
        i=0;
        while (i<header.recordLengthCount)
        {
            DebugLogger.debug(String.format("Record type tag 0x%02x, length %5d", header.recordLengths.get(i).tag, header.recordLengths.get(i).length));
            i++;
        }
    }
    
    /** 
     * Read the next record data
     * @param in Input stream to read from
     * @param header Header information
     * @return The byte array containing the record data
     * @throws IOException Error while reading from the input stream
     */
    public byte[] readRecord(InputStream in, TtbinHeader header) throws IOException
    {
        byte[]  recordData;
        byte[]  lengthData;
        int     tag;
        int     length;

        recordData=null;

        if (in.available()>0)
        {
            tag=in.read();
            length=header.getLength(tag);
            
            // Variable length, like the 4B record
            if (length==0xFFFF)
            {
                lengthData=new byte[2];
                in.read(lengthData, 0, 2); // read two next bytes
                length=ToolBox.readUnsignedInt(lengthData, 0, 2, true)+1;

                recordData=new byte[length];
                recordData[0]=(byte)tag;
                
                // Read the remainder of the record
                in.read(recordData, 1, length-1);
            }
            // Fixed length
            else if (length>0)
            {
                // Read first byte of the record, which is the tag
                recordData=new byte[length];
                recordData[0]=(byte)tag;
                
                // Read the remainder of the record
                in.read(recordData, 1, length-1);
              
            }
                
        }
        
        return recordData;
    }
    
    
    /**
     * Calculate the corrected elevation. Elevation 1 seems to be the absolute
     * elevation. Elevation 2 is the relative elevation with respect to 0 m.
     * Elevation 1 contains is not correct at the start of the segment.
     * @param activity Activity to correct
     */
    private void correctElevation(Activity activity)
    {
        double                      average1;
        double                      average2;
        double                      elevation1;
        double                      elevation2;
        double                      sum1;
        double                      sum2;
        int                         count1;
        int                         count2;
        int                         maxSegments;
        int                         segmentCount;
        int                         recordCount;
        int                         maxRecords;
        ArrayList<ActivityRecord>   records;
        ActivityRecordGps           record;
        
        maxSegments =activity.getNumberOfSegments();
        average1    =0.0;
        average2    =0.0;
        sum1        =0.0;
        count1      =0;
        sum2        =0.0;
        count2      =0;
        segmentCount=0;
        while (segmentCount<maxSegments)
        {
            records=activity.getRecords(segmentCount);
            
            if (records.size()>5)
            {
                // skip the first 5 records to prevent unreliable records 
                recordCount=5;
                while (recordCount<records.size())
                {
                    record=(ActivityRecordGps)records.get(recordCount);

                    elevation1=record.getElevation1();
                    elevation2=record.getElevation2();

                    if (elevation1!=ActivityRecord.INVALID)
                    {
                        sum1+=elevation1;
                        count1++;
                    }
                    if (elevation2!=ActivityRecord.INVALID)
                    {
                        sum2+=elevation2;
                        count2++;
                    }

                    recordCount++;
                }
            }
            segmentCount++;
        }

        // Check if there are any valid points to correct
        if (count1>0 && count2>0)
        {
            average1=sum1/count1;
            average2=sum2/count2;

            segmentCount=0;
            while (segmentCount<maxSegments)
            {
                records=activity.getRecords(segmentCount);

                recordCount=0;
                while (recordCount<records.size())
                {
                    record=(ActivityRecordGps)records.get(recordCount);

                    elevation2=record.getElevation2();

                    if (elevation2!=ActivityRecord.INVALID)
                    {
                        elevation1=elevation2+(average1-average2);
                        record.setDerivedElevation(elevation1);
                    }
                    else
                    {
                        record.setDerivedElevation(ActivityRecord.INVALID);
                    }

                    recordCount++;
                }

                segmentCount++;
            }
        }
        else
        {
            DebugLogger.info("Height not corrected. Insufficient track points in the track.");
        }
    
    
    }
    
    
    /**
     * This method reads the ttbin file from the input stream passed.
     * @param in Input stream to read from
     * @return The activity read or null if an error occurred
     * @throws IOException 
     */
    private Activity readTtbinFile(InputStream in) throws IOException
    {
        boolean             fileRead;
        byte[]              recordData;
        
        DebugLogger.info("Reading Header");
        readHeader(in);
        dumpHeader();

        activity=new Activity(this.header);

        // Set the timezone
        activity.setTimeZoneDifference(header.localTimeOffset);

        DebugLogger.info("Reading records");
        fileRead=false;
        while (!fileRead)
        {
            recordData=this.readRecord(in, header);
            if (recordData!=null)
            {
                activity.parseRecord(recordData);
            }
            else
            {
                fileRead=true;
            }
        }

        // Calculated the corrected elevation
        this.correctElevation(activity);

        // If required, smooth the track
        if (this.trackSmoothingEnabled)
        {
            activity.smoothTrack(trackSmoothingQFactor);
        }
        
        
        
//activity.dumpActivityCsv();
        
        DebugLogger.info("Done reading");

        return activity;
    }
    
    
    /**************************************************************************\
     * FILE READING
    \**************************************************************************/
    /**
     * Reads the ttbin file and stores it in the Activity 
     * @param fileName Name of the ttbin file
     * @return The activity corresponding to the file read
     */
    public Activity readTtbinFile(String fileName)
    {
        FileInputStream     in;
        Activity            activity;
        
        in      =null;
        activity=null;
        
        try
        {
            in = new FileInputStream(fileName);
            
            activity=this.readTtbinFile(in);
           
        }
        catch (IOException e)
        {
            DebugLogger.error(e.getMessage());
        }
        finally 
        {
            if (in != null) 
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    DebugLogger.error(e.getMessage());
                }
            }
        }        
        
        return activity;
    }
    
    /**
     * Reads the ttbin file as stored in the UsbFile passed
     * @param file The UsbFile containing the file data
     * @return The activity corresponding to the file data
     */
    public Activity readTtbinFile(UsbFile file)
    {
        ByteArrayInputStream in;
        Activity            activity;
        
        in      =null;
        activity=null;
        
        try
        {
            in = new ByteArrayInputStream(file.fileData);
            
            activity=this.readTtbinFile(in);
           
        }
        catch (IOException e)
        {
            DebugLogger.error(e.getMessage());
        }
        finally 
        {
            if (in != null) 
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    DebugLogger.error(e.getMessage());
                }
            }
        }        
        
        return activity;
    }
    
    
}
