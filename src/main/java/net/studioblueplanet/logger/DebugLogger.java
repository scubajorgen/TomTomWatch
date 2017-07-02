/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.logger;

import hirondelle.date4j.DateTime;
import java.util.TimeZone;

/**
 *
 * @author jorgen
 */
public class DebugLogger 
{
    public static final int     DEBUGLEVEL_DEBUG=0;
    public static final int     DEBUGLEVEL_INFO =1;
    public static final int     DEBUGLEVEL_ERROR=2;
    public static final int     DEBUGLEVEL_OFF  =3;

    private static int          debugLevel=DEBUGLEVEL_INFO;
    
    /**
     *  Private constructor: this class cannot be instantiated
     */
    private DebugLogger()
    {
        
    } 
           
    /**
     * Set debugging level
     * @param newDebugLevel The new debug level
     */
    public static void setDebugLevel(int newDebugLevel)
    {
        if ((newDebugLevel>=DEBUGLEVEL_DEBUG) && (newDebugLevel<=DEBUGLEVEL_OFF))
        {
            debugLevel=newDebugLevel;
        }
    }
    
    /**
     * Set the debug level. Allowed values: 'off', 'debug', 'info', 'error'
     * @param level The level as string value
     */
    public static void setDebugLevel(String level)
    {
        if (level.trim().toLowerCase().equals("debug"))
        {
            debugLevel=DEBUGLEVEL_DEBUG;
        }
        else if (level.trim().toLowerCase().equals("info"))
        {
            debugLevel=DEBUGLEVEL_INFO;
        }
        else if (level.trim().toLowerCase().equals("error"))
        {
            debugLevel=DEBUGLEVEL_ERROR;
        }
        else
        {
            debugLevel=DEBUGLEVEL_OFF;
        }
    }
    
    /**
     * Returns the debug level
     * @return 
     */
    public static int getDebugLevel()
    {
        return debugLevel;
    }
    
    /**
     * Write info. Info is written to System.out when debugging is on.
     * @param info The info
     */
    public static void debug(String info)
    {
        DateTime time;
        if (debugLevel<=DEBUGLEVEL_DEBUG)
        {
            time=DateTime.now(TimeZone.getDefault());
            System.out.println("d " + time.format("YYYY-MM-DD hh:mm:ss  ")+info);
        }
    }

    
    /**
     * Write info. Info is written to System.out when debugging is on.
     * @param info The info
     */
    public static void info(String info)
    {
        DateTime time;
        if (debugLevel<=DEBUGLEVEL_INFO)
        {
            time=DateTime.now(TimeZone.getDefault());
            System.out.println("i "+time.format("YYYY-MM-DD hh:mm:ss  ")+info);
        }
    }

    /**
     * Write error message. Error messages are written to System.err if debugging
     * is on
     * @param info The message 
     */
    public static void error(String info)
    {
        DateTime time;
        if (debugLevel<=DEBUGLEVEL_ERROR)
        {
            time=DateTime.now(TimeZone.getDefault());
            System.err.println("e "+time.format("YYYY-MM-DD hh:mm:ss  ERROR: ")+info);
        }
    }
    
    
    /**
     * This method returns the debug level defined by the parameter as a string
     * @param debugLevel The debug level to convert
     * @return The string represenging the debug level or 'unknown' if 
     *         it could not be translated
     */
    public static String debugLevelToString(int debugLevel)
    {
        String returnString;
        
        switch (debugLevel)
        {
            case DEBUGLEVEL_OFF:
                returnString="off";
                break;
            case DEBUGLEVEL_DEBUG:
                returnString="debug";
                break;
            case DEBUGLEVEL_INFO:
                returnString="info";
                break;
            case DEBUGLEVEL_ERROR:
                returnString="error";
                break;
            default:
                returnString="unknown";
                break;
        }

        
        return returnString;
    }
    
    /**
     * This method returns current debug level as a string
     * @return The string representing the debug level or 'unknown' if 
     *         it could not be translated
     */
    public static String debugLevelToString()
    {
        return debugLevelToString(debugLevel);
    }    
}
