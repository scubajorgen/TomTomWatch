/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.generics;

import net.studioblueplanet.logger.DebugLogger;

import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author jorgen.van.der.velde
 */
public class ToolBox
{
    /**
     * Read an unsigned integer value from the byte array
     * @param data Byte array
     * @param offset Offset where to start
     * @param bytes Number of bytes to be read in the int
     * @param isLittleEndian Indicates whether the byte array is LittleEndian or BigEndian
     * @return The integer value
     */
    public static int readUnsignedInt(byte[] data, int offset, int bytes, boolean isLittleEndian)
    {
        int i;
        int theInt;
        
        theInt=0;
        if (isLittleEndian)
        {
            i=0;
            while (i<bytes)
            {
                theInt|=((int)data[i+offset]&0xFF)<<(i*8);
                i++;
            }
        }
        else
        {
            i=0;
            while (i<bytes)
            {
                theInt|=((int)data[i+offset]&0xFF)<<((bytes-i-1)*8);
                i++;
            }
            
        }
        return theInt;
    }

    
    
    /**
     * Read an unsigned integer value from the byte array
     * @param data Byte array
     * @param offset Offset where to start
     * @param bytes Number of bytes to be read in the int
     * @param isLittleEndian Indicates whether the byte array is LittleEndian or BigEndian
     * @return The integer value
     */
    public static long readUnsignedLong(byte[] data, int offset, int bytes, boolean isLittleEndian)
    {
        int  i;
        long theInt;
        
        theInt=0;
        if (isLittleEndian)
        {
            i=0;
            while (i<bytes)
            {
                theInt|=((long)data[i+offset]&0xFF)<<(i*8);
                i++;
            }
        }
        else
        {
            i=0;
            while (i<bytes)
            {
                theInt|=((long)data[i+offset]&0xFF)<<((bytes-i-1)*8);
                i++;
            }
            
        }
        return theInt;
    }
    
    
    /**
     * Read an signed integer value from the byte array
     * @param data Byte array
     * @param offset Offset where to start
     * @param bytes Number of bytes to be read in the int
     * @param isLittleEndian Indicates whether the byte array is LittleEndian or BigEndian
     * @return The integer value
     */
    public static int readInt(byte[] data, int offset, int bytes, boolean isLittleEndian)
    {
        int         theInt;
        boolean     sign;
        
        theInt=readUnsignedInt(data, offset, bytes, isLittleEndian);
        
        sign=(theInt & (1<<(8*bytes-1)))>0;
        
        if (sign)
        {
            theInt |= (((int)-1)<<(8*bytes));
        }
        
        return theInt;
    }


    /**
     * Read a float value from the byte array. A float value is constructed
     * from 4 bytes
     * @param data Byte array
     * @param offset Offset where to start
     * @param isLittleEndian Indicates whether the byte array is LittleEndian or BigEndian
     * @return The float value
     */
    public static float readFloat(byte[] data, int offset, boolean isLittleEndian)
    {
        int     theInt;
        float   theFloat;
        
        theInt      =readInt(data, offset, 4, isLittleEndian);
        theFloat    =Float.intBitsToFloat(theInt);
        
        return theFloat;
    }
    
    /**
     * Writes a float to the byte array
     * @param data Byte array
     * @param value Float value to write
     * @param offset Offset in the byte array
     * @param isLittleEndian Indicates whether the byte array is LittleEndian or BigEndian
     */
    public static void writeFloat(byte[] data, float value, int offset, boolean isLittleEndian)
    {
        int intValue;
        
        intValue=Float.floatToIntBits(value);
        ToolBox.writeUnsignedInt(data, intValue, offset, 4, isLittleEndian);
    }
    
    /**
     * Reads a string from the input stream.
     * @param data Byte array containing the data
     * @param offset Offset in the array
     * @param chars Number of characters to read
     * @return The string read
     */
    public static String readString(byte[] data, int offset, int chars)
    {
        int     i;
        String  string;
        
        string="";
        i=0;
        while (i<chars)
        {
            if (data[offset+i]>0)
            {
                string+=(char)data[offset+i];
            }
            i++;
        }
        return string;
    }    
    
    /**
     * Writes the given value (value) in the byte array (data) at given location (offset).
     * The value is assumed to be (bytes) long. If isLittleEndian, the least significant 
     * byte (lsb) is written as the first byte in the byte array, followed by the 
     * higher significant bytes.
     * If not isLittleEndian the most significant byte (msb) is written first, followed
     * by the less significant bytes.
     * @param data      Array to write the value to
     * @param value     Value to write
     * @param offset    Offset in the byte array where the value is written
     * @param bytes     Number of bytes making up the integer value (1-4)
     * @param isLittleEndian True for little endian, false for big endian
     */
    public static void writeUnsignedInt(byte[] data, int value, int offset, int bytes, boolean isLittleEndian)
    {
        int i;
        
        i=0;
        while (i<bytes)
        {
            if (isLittleEndian)
            {
                data[i+offset]=(byte)((value>>(i*8))&0xff);
            }
            else
            {
                data[i+offset]=(byte)((value>>((bytes-i-1)*8))&0xff);
            }
            i++;
        }
    }
    
    /**
     * Writes the given value (value) in the byte array (data) at given location (offset).
     * The value is assumed to be (bytes) long. If isLittleEndian, the least significant 
     * byte (lsb) is written as the first byte in the byte array, followed by the 
     * higher significant bytes.
     * If not isLittleEndian the most significant byte (msb) is written first, followed
     * by the less significant bytes.
     * @param data      Array to write the value to
     * @param value     Value to write
     * @param offset    Offset in the byte array where the value is written
     * @param bytes     Number of bytes making up the integer value (1-4)
     * @param isLittleEndian True for little endian, false for big endian
     */
    public static void writeUnsignedLong(byte[] data, long value, int offset, int bytes, boolean isLittleEndian)
    {
        int i;
        
        i=0;
        while (i<bytes)
        {
            if (isLittleEndian)
            {
                data[i+offset]=(byte)((value>>(i*8))&0xff);
            }
            else
            {
                data[i+offset]=(byte)((value>>((bytes-i-1)*8))&0xff);
            }
            i++;
        }
    }
    
    /**
     * Returns the number of seconds as string in the format 5h03'02"
     * @param seconds The seconds to convert
     * @return The string with the duration
     */
    public static String secondsToHours(int seconds)
    {
        int     hours;
        int     minutes;
        int     remainingSeconds;
        
        hours=seconds/3600;
        
        minutes=(seconds-(hours*3600))/60;
        
        remainingSeconds=seconds-hours*3600-minutes*60;
        
        return String.format("%dh%02d'%02d\"", hours, minutes, remainingSeconds);
    }
 
    
    /**
     * Request page. Applies to text, json, xml, etc files
     * @param urlString URL of the page
     * @return The page as string
     */
    public static String readStringFromUrl(String urlString)
    {
        BufferedReader  in;
        String          fileString;
        String          inputLine;
        URL             url;
        boolean         exit;

        fileString      =null;
        
        if (urlString!=null)
        {
            // Read the config file from this url
            try
            {
                url=new URL(urlString);
                in = new BufferedReader(new InputStreamReader(url.openStream()));
                exit        =false;
                fileString  ="";
                while (!exit)
                {
                    inputLine = in.readLine();
                    if (inputLine!=null)
                    {
                        fileString+=inputLine;
                    }
                    else
                    {
                        exit=true;
                    }
                }
                in.close();    
            }
            catch (MalformedURLException e)
            {
                DebugLogger.error("Error requesting URL: "+e.getMessage());
            }
            catch (IOException e)
            {
                DebugLogger.error("Error requesting URL: "+e.getMessage());
            }
        }
        return fileString;
    }    

    /**
     * Reads bytes from indicated URL. Applies to binary files
     * @param urlString The URL to read from
     * @return The byte array or null if something went wrong.
     */
    public static byte[] readBytesFromUrl(String urlString)
    {
        URL                     url;
        ByteArrayOutputStream   baos;
        InputStream             is;
        byte[]                  byteChunk;
        int                     n;
        byte[]                  bytes;
        
        is                      =null;  
        bytes                   =null;      
        
        try 
        {
            url                 =new URL(urlString);
            baos                =new ByteArrayOutputStream();

            is                  = url.openStream ();
            byteChunk           = new byte[4096]; // Or whatever size you want to read in at a time.

            while ( (n = is.read(byteChunk)) > 0 ) 
            {
                baos.write(byteChunk, 0, n);
            }
            bytes=baos.toByteArray();
        }
        catch (IOException e) 
        {
          DebugLogger.error("Error reading bytes from "+urlString+": "+e.getMessage());
        }
        finally 
        {
            if (is != null) 
            { 
                try
                {
                    is.close();
                }
                catch (Exception e)
                {
                    DebugLogger.error("Error closing URL");
                }
            }
        }   
        return bytes;
    }
    
    
    
    /**
     * Reads byte array from file
     * @param fileName The file to read
     * @return The byte array or null if something went wrong
     */
    public static byte[] readBytesFromFile(String fileName)
    {
        byte[]              bytes;
        int                 fileSize;
        RandomAccessFile    file;
        
        bytes=null;
        
        try
        {
            file        = new RandomAccessFile(fileName, "r");
            fileSize    =(int)file.length();
            bytes       = new byte[fileSize];
            file.readFully(bytes);      
            file.close();
        }
        catch (FileNotFoundException e)
        {
            DebugLogger.error("Error reading file: "+e.getMessage());
            bytes       =null;
        }
        catch (IOException e)
        {
            DebugLogger.error("Error reading file: "+e.getMessage());
            bytes       =null;
        }        
        
        return bytes;
    }

    /**
     * This method writes the bytes to a binary file
     * @param fileName File name of the file to write to
     * @param bytes Bytes to write
     * @return False if all went ok, true if an error occurred
     */
    public static boolean writeBytesToFile(String fileName, byte[] bytes)
    {
        boolean             error;
        RandomAccessFile    diskFile;
        
        error=false;

        try
        {
            diskFile=new RandomAccessFile(fileName, "rw");
            diskFile.write(bytes);
            diskFile.close();
        }
        catch (FileNotFoundException e)
        {
            DebugLogger.error("File not found Exception during file write: "+e.getMessage());
        }
        catch (IOException e)
        {
            DebugLogger.error("IO Exception during file write: "+e.getMessage());
        }
        
        return error;
    }

    
    /**
     * Write string to file
     * @param fileName Filename to use
     * @param string String to write
     * @return False if all went ok, true if an error occurred
     */
    public static boolean writeStringToFile(String fileName, String string)
    {
        boolean             error;
        PrintWriter         out;

        error       =false;
        try
        {
            out     =new PrintWriter(fileName);  
            out.println(string);
            out.close();
        }
        catch (FileNotFoundException e)
        {
            error   =true;
        }
       
        return error;        
    }
    
    
    /**
     * Reads all bytes from an input stream
     * @param is Input stream
     * @return Byte array containing the bytes or null if not successful
     */
    public static byte[] getBytesFromInputStream(InputStream is)
    {
        byte[] theBytes;
        
        theBytes=null;
        
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();)
        {
            byte[] buffer = new byte[1024];

            for (int len; (len = is.read(buffer)) != -1;)
                os.write(buffer, 0, len);

            os.flush();

            theBytes=os.toByteArray();
        }
        catch (IOException e)
        {
            DebugLogger.error("Error reading bytes from input stream");
        }
        return theBytes;
    }    
    
    
    private static final double EARTH_RADIUS = 6372.795477598; // Approx Earth radius in KM

    /**
     * Calculates the distance between two coordinates
     * @param startLat First coordinate latitude
     * @param startLong First coordinate longitude
     * @param endLat Second coordinate latitude
     * @param endLong Second coordinate longitude
     * @return Distance in km
     */
    public static double distance(double startLat, double startLong,
                                  double endLat, double endLong) 
    {
        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // <-- d
    }

    private static double haversin(double val) 
    {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
