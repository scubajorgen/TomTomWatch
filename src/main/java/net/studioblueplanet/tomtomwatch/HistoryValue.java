/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.logger.DebugLogger;

/**
 *
 * @author Jorgen
 */
public class HistoryValue
{
    public enum ValueType
    {
        VALUETYPE_INT,
        VALUETYPE_FLOAT,
        VALUETYPE_STRING,
        VALUETYPE_UNKNOWN
    }
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_DURATION      = 0x01;  /* int_val   = seconds       */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_DISTANCE      = 0x02;  /* float_val = metres        */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_AVERAGEPACE   = 0x05;  /* float_val = metres/second */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_AVERAGESPEED  = 0x07;  /* float_val = metres/second */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_LENGTH        = 0x08;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_LAPS          = 0x09;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_SWOLF         = 0x0f;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_STROKES       = 0x13;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_CALORIES      = 0x14;  /* int_val                   */

    private static final int    TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_1    = 0x20;  /* int_val, 16 bit           */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_2    = 0x21;  /* int_val, 16 bit           */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_3    = 0x22;  /* int_val, 16 bit           */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_4    = 0x23;  /* int_val, 16 bit           */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_5    = 0x24;  /* int_val, 16 bit           */

    private static final int    TTWATCH_HISTORY_ENTRY_TAG_RACENAME      = 0x25;  /* LENGTH (8 bits) + STRING  */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_RACEPOSITION  = 0x26;  /* int_val (8 bits)          */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_RACEPACEAHEAD = 0x27;  /* float_val (m/s)           */    
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_RACEAHEAD     = 0x28;  /* int_val in seconds        */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_HEARTRATE     = 0x1f;  /* int_val - avg heartrate?  */


    private int                 tag;
    private double              floatValue;
    private int                 intValue;
    private String              stringValue;
    private ValueType           valueType;
    
    public HistoryValue()
    {
    }
    
    
    public int convertValue(int tag, byte[] data, int offset)
    {
        int nextOffset;
        int length;

        intValue    =0;
        floatValue  =0.0;
        stringValue =null;
        this.tag    =tag;
        
        DebugLogger.info(String.format("History value tag: 0x%02x", tag));
        
        switch (tag)
        {
            case TTWATCH_HISTORY_ENTRY_TAG_DURATION:
            case TTWATCH_HISTORY_ENTRY_TAG_LENGTH:
            case TTWATCH_HISTORY_ENTRY_TAG_LAPS:
            case TTWATCH_HISTORY_ENTRY_TAG_SWOLF:
            case TTWATCH_HISTORY_ENTRY_TAG_STROKES:
            case TTWATCH_HISTORY_ENTRY_TAG_CALORIES:
            case TTWATCH_HISTORY_ENTRY_TAG_HEARTRATE:
            case TTWATCH_HISTORY_ENTRY_TAG_RACEAHEAD:
                intValue    =ToolBox.readInt(data, offset, 4, true);
                valueType=ValueType.VALUETYPE_INT;
                nextOffset=offset+4;
                break;

            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_1:
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_2:
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_3:
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_4:
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_5:
                intValue    =ToolBox.readInt(data, offset, 2, true);
                valueType=ValueType.VALUETYPE_INT;
                nextOffset=offset+2;
                break;
                
                
            case TTWATCH_HISTORY_ENTRY_TAG_DISTANCE:
            case TTWATCH_HISTORY_ENTRY_TAG_AVERAGEPACE:
            case TTWATCH_HISTORY_ENTRY_TAG_AVERAGESPEED:
            case TTWATCH_HISTORY_ENTRY_TAG_RACEPACEAHEAD:
                floatValue   =ToolBox.readFloat(data, offset, true);
                valueType=ValueType.VALUETYPE_FLOAT;
                nextOffset=offset+4;
                break;
                
                
            case TTWATCH_HISTORY_ENTRY_TAG_RACEPOSITION:
                intValue    =ToolBox.readInt(data, offset, 1, true);
                valueType   =ValueType.VALUETYPE_INT;
                nextOffset  =offset+1;
                break;
                
            case TTWATCH_HISTORY_ENTRY_TAG_RACENAME:
                length    =ToolBox.readInt(data, offset, 1, true);
                stringValue=ToolBox.readString(data, offset+1, length);
                valueType=ValueType.VALUETYPE_STRING;
                nextOffset=offset+length+1;
                break;
                
                
            default:
                DebugLogger.info(String.format("Undefined value 0x%02x", tag));
                nextOffset  =offset+4;
                valueType   =ValueType.VALUETYPE_UNKNOWN;
                break;
        }
        
        
        return nextOffset;
    }
    
    
    /**
     * Returns the tag (variable)
     * @return Tag
     */
    public int getTag()
    {
        return tag;
    }
    
    /**
     * Return the meaning of the tag.
     * @return String describing the variable
     */
    public String getTagDescription()
    {
        String description;
        
        switch(tag)
        {
            case TTWATCH_HISTORY_ENTRY_TAG_DURATION:
                description="Duration";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_DISTANCE:
                description="Distance (m)";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_AVERAGEPACE:
                description="Average pace (m/s)";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_AVERAGESPEED:
                description="Average speed (m/s)";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_LENGTH:
                description="Lengths";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_LAPS:
                description="Laps";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_SWOLF:
                description="Swolf";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_STROKES:
                description="Strokes";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_CALORIES:
                description="Calories";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_RACEPOSITION:
                description="Race Position";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_RACENAME:
                description="Race";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_RACEPACEAHEAD:
                description="Race pace diff (m/s)";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_1:
                description="Unknown1";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_2:
                description="Unknown2";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_3:
                description="Unknown3";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_4:
                description="Unknown4";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2_5:
                description="Unknown5";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_HEARTRATE:
                description="Ave. heartrate (bpm)";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_RACEAHEAD:
                description="Seconds ahead";
                break;
            default:
                description="Unknown";
                break;
        }
        return description;
    }
    
    
    public int getValueAsInt()
    {
        int value;
        
        if (valueType==ValueType.VALUETYPE_INT)
        {
            value=this.intValue;
        }
        else
        {
            DebugLogger.error("Requesting integer value of a variable that is not an integer");
            value=-9999;
        }
        return value;
    }
    
    public double getValueAsFloat()
    {
        double value;
        
        if (valueType==ValueType.VALUETYPE_FLOAT)
        {
            value=this.floatValue;
        }
        else
        {
            DebugLogger.error("Requesting float value of a variable that is not an float");
            value=-9999.0;
        }
        return value;        
    }
    
    public String getValueAsString()
    {
        String value;
        
        if (valueType==ValueType.VALUETYPE_STRING)
        {
            value=this.stringValue;
        }
        else
        {
            DebugLogger.error("Requesting float value of a variable that is not an float");
            value="";
        }
        return value;        
    }


    public String getDescription()
    {
        String description;
        
        description=String.format("0x%02x %-20s ", tag, this.getTagDescription());
        
        if (valueType==ValueType.VALUETYPE_INT)
        {
            if (tag==TTWATCH_HISTORY_ENTRY_TAG_DURATION)
            {
                description+=ToolBox.secondsToHours(intValue);
            }
            else
            {   
                description+=String.format("%8d", intValue);
            }
        }
        else if (valueType==ValueType.VALUETYPE_FLOAT)
        {
            description+=String.format("%8.2f", floatValue);
        }
        else if (valueType==ValueType.VALUETYPE_STRING)
        {
            description+=stringValue;
        }
        description+="\n";
        return description;
    }
}
