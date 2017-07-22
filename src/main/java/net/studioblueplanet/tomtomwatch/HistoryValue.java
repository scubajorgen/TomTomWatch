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
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN1     = 0x00;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_DURATION     = 0x01;  /* int_val   = seconds       */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_DISTANCE     = 0x02;  /* float_val = metres        */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_AVERAGEPACE  = 0x05;  /* float_val = metres/second */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_AVERAGESPEED = 0x07;  /* float_val = metres/second */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_LENGTH       = 0x08;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_LAPS         = 0x09;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_SWOLF        = 0x0f;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_STROKES      = 0x13;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_CALORIES     = 0x14;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2     = 0x20;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_RACEPOSITION = 0x25;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_RACESPEED    = 0x26;  /* float_val = metres/second */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_RACETIME     = 0x27;  /* int_val   = seconds       */    
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN3     = 0x28;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN4     = 0x1f;  /* int_val                   */
    private static final int    TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN5     = 0x30;  /* int_val                   */


    private final int           tag;
    private final double        floatValue;
    private final int           intValue;
    
    public HistoryValue(int tag, byte[] data, int offset)
    {
        this.tag=tag;
        
        if (isInteger(tag))
        {
            intValue    =ToolBox.readInt(data, offset, 4, true);
            floatValue  =0.0;
        }
        else
        {
            floatValue  =ToolBox.readFloat(data, offset, true);
            intValue    =0;
        }
    }
    
    /**
     * Indicates whether the value at given tag is integer or float
     * @param tag The tag
     * @return True if integer, false if float
     */
    private boolean isInteger(int tag)
    {
        boolean isInt;
        
        switch(tag)
        {
            case TTWATCH_HISTORY_ENTRY_TAG_DURATION:
            case TTWATCH_HISTORY_ENTRY_TAG_LENGTH:
            case TTWATCH_HISTORY_ENTRY_TAG_LAPS:
            case TTWATCH_HISTORY_ENTRY_TAG_SWOLF:
            case TTWATCH_HISTORY_ENTRY_TAG_STROKES:
            case TTWATCH_HISTORY_ENTRY_TAG_CALORIES:
            case TTWATCH_HISTORY_ENTRY_TAG_RACEPOSITION:
            case TTWATCH_HISTORY_ENTRY_TAG_RACETIME:
//            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN1:
//            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2:
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN3:
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN5:
                isInt=true;
                break;
            default:
                isInt=false;
        }
        return isInt;
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
                description="Average pace";
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
            case TTWATCH_HISTORY_ENTRY_TAG_RACESPEED:
                description="Race Speed";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_RACETIME:
                description="Race Time";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN1:
                description="Unknown1";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN2:
                description="Unknown2";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN3:
                description="Unknown3";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN4:
                description="Unknown4";
                break;
            case TTWATCH_HISTORY_ENTRY_TAG_UNKNOWN5:
                description="Unknown5";
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
        
        if (isInteger(tag))
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
        
        if (!isInteger(tag))
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
    
    public String getDescription()
    {
        String description;
        
        description=String.format("0x%02x %-20s ", tag, this.getTagDescription());
        
        if (this.isInteger(tag))
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
        else
        {
            description+=String.format("%8.2f", floatValue);
        }
        description+="\n";
        return description;
    }
}
