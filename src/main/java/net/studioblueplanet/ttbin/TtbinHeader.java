/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;


import java.util.ArrayList;

/**
 * This class represents the TTBIN file header. It contains information
 * on the ttbin file, a.o. the list of record tags with corresponding length
 * @author jorgen.van.der.velde
 */
    
public class TtbinHeader
{
    public static final int                 HEADERLENGTH=118;   // header bytes, without the variable recordLengths array
    public static final int                 LENGTHITEMLENGTH=3; // bytes in one length item
    public int                              tag;
    public int                              fileVersion;
    public int[]                            firmwareVersion=new int[3];
    public int                              productId;
    public int                              startTime;          // local time
    public String                           softwareVersion;
    public String                           gpsFirmwareVersion;
    public int                              watchTime;          // local time    
    public int                              localTimeOffset;    // seconds from UTC
    public int                              reserved;
    public int                              recordLengthCount;
    public ArrayList<TtbinRecordLength>     recordLengths=new ArrayList();

    /**
     * This method returns the record length of a record identified by the tag
     * @param tag Type of record
     * @return The length of the record
     */
    public int getLength(int tag)
    {
        int     i;
        int     length;

        length=-1;
        i=0;
        while (i<recordLengths.size() && length<0)
        {
            if (recordLengths.get(i).tag==tag)
            {
                length=recordLengths.get(i).length;
            }
            i++;
        }
        return length;
    }
/*        
uint16_t file_version;
uint8_t  firmware_version[3];
uint16_t product_id;
uint32_t start_time;    
uint8_t  software_version[16];
uint8_t  gps_firmware_version[80];
uint32_t watch_time;    
int32_t local_time_offset;  
uint8_t  _reserved;
uint8_t  length_count;  
RECORD_LENGTH lengths[1];        
*/    
}