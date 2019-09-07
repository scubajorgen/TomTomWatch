/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.studioblueplanet.logger.DebugLogger;

/**
 *
 * @author jorgen.van.der.velde
 */
public class ConfigSettings
{
    public enum SettingType
    {
        STRING, INT, DOUBLE, BOOLEAN
    }
        
    class Setting
    {

        // Name of the setting in the property file
        public String       name;               
        
        // Type of the value
        public SettingType  type;
        
        // Short description
        public String       description;       
        
        // Default value
        public String       defaultValue;       
        
        // Current value
        public String       value;              
        
        // Allowed value: INT, DOUBLE: "min,max", STRING: "value1,value2,..." BOOLEAN: no meaning; Leave empty "" for no check
        public String       allowed;            
        
        
        public Setting(String name, String description, String defaultValue, String allowed, SettingType type)
        {
            this.name           =name;
            this.description    =description;
            this.defaultValue   =defaultValue;
            this.value          =defaultValue;
            this.allowed        =allowed;
            this.type           =type;
        }
    }
    

    private static String               propertyFileName="tomtomwatch.properties";


    private final Setting[]             settings=
    {
        new Setting("debugLevel"            , "DebugLogger log level            " , "error", "off,debug,info,error"   , SettingType.STRING ),
        new Setting("debuggingMenu"         , "Debugging functions menue        " , "false", ""                       , SettingType.BOOLEAN),
        new Setting("debugFilePath"         , "Path where watch files are stored" , "//"   , ""                       , SettingType.STRING ),
        new Setting("gpxFilePath"           , "Path where GPX files are stored  " , "//"   , ""                       , SettingType.STRING ),
        new Setting("routeFilePath"         , "Path where to find GPX route files", "//"   , ""                       , SettingType.STRING ),
        new Setting("ttbinFilePath"         , "Path where TTBIN files are stored" , "//"   , ""                       , SettingType.STRING ),
        new Setting("autoSaveTtbin"         , "Autosave TTBIN enabled           " , "true" , ""                       , SettingType.BOOLEAN),
        new Setting("waypointLogTimeout"    , "Limit in ms for pause to be wp   " , "5000" , "1,30000"                , SettingType.INT    ),
        new Setting("simulation"            , "Simulation mode                  " , "false", ""                       , SettingType.BOOLEAN),
        new Setting("simulationPath"        , "Path where sim files are stored  " , "//"   , ""                       , SettingType.STRING ),
        new Setting("trackSmoothingEnabled" , "Track smoothing enabled          " , "false", ""                       , SettingType.BOOLEAN),
        new Setting("trackSmoothingQFactor" , "Defines the amount of smoothing  " , "2.0"  , "1.0,10.0"               , SettingType.DOUBLE ),
        new Setting("heightService"         , "Defines the elevation service    " , "none" , "none,google"            , SettingType.STRING ),
        new Setting("heightServiceKey"      , "Google Elevation service API key " , ""     , ""                       , SettingType.STRING ),
        new Setting("mapService"            , "Map service                      " , "osm"  , "none,osm,google"        , SettingType.STRING ),
        new Setting("mapServiceKey"         , "Google Maps service API key      " , ""     , ""                       , SettingType.STRING ),
        new Setting("quickFixDays"          , "GPS Quick Fix days ahead         " , "3"    , "3,7"                    , SettingType.INT    ),
        new Setting("downloadAll"           , "Download all activities          " , "false", ""                       , SettingType.BOOLEAN),
        new Setting("ugotmeGpxExtensions"   , "Enable gpx extensions            " , "false", ""                       , SettingType.BOOLEAN),
        new Setting("garminGpxExtensions"   , "Enable garmin gpx extensions     " , "true" , ""                       , SettingType.BOOLEAN)
    };
    
    private static ConfigSettings       theInstance=null;        
    
    /**
     * Constructor, reads the setting values
     */
    private ConfigSettings()
    {
        this.readSettings();
    }
    
    /**
     * Returns the one and only instance of this class
     * @return The instance
     */
    public static ConfigSettings getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new ConfigSettings();
        }
        return theInstance;
    }
    
    public static void setPropertiesFile(String fileName)
    {
        propertyFileName=fileName;
    }
    
    /**
     * Reads the settings from the property file. If no file found, 
     * the default values are used
     */
    private void readSettings()
    {
        Properties      properties;
        int             i;
        String          settingValue;
        boolean         error;
        Setting         setting;
        
        

        // Read properties file.
        properties = new Properties();
        try
        {
            properties.load(new FileInputStream(propertyFileName));

            // Get each setting from the array
            i=0;
            while (i<settings.length)
            {
                setting=settings[i];
                settingValue=properties.getProperty(setting.name);
                if (settingValue!=null)
                {
                    setting.value=settingValue;
                    error=validateSetting(setting);
                    if (error)
                    {
                        setting.value=setting.defaultValue;
                        DebugLogger.error("Error validating setting '"+setting.name+"'. Using default value '"+setting.value+"'");
                    }
                }
                else
                {
                    DebugLogger.error("Setting "+setting.name+" not found, assuming default value");
                    setting.value=setting.defaultValue;
                    error=true;
                }
                i++;
            }

            DebugLogger.info("Settings read");
            dumpSettings();
        }    
        catch (IOException e)
        {
            DebugLogger.error("Error reading settings, using defaults. Error: "+e.getMessage());
        }
    }
    
    private boolean validateSetting(Setting setting)
    {
        boolean     error;
        int         intValue;
        int         intMin;
        int         intMax;
        double      doubleValue;
        double      doubleMin;
        double      doubleMax;
        String[]    subStrings;
        int         i;
        
        error=false;
        
        // Remove leading and trailing spaces
        setting.value=setting.value.trim();
        switch (setting.type)
        {
            case INT:
                try
                {
                    intValue    =Integer.parseInt(setting.value);
                    subStrings  =setting.allowed.split("[,]");
                    if (subStrings.length==2)
                    {
                        intMin=Integer.parseInt(subStrings[0]);
                        intMax=Integer.parseInt(subStrings[1]);
                        if ((intValue>intMax) || (intValue<intMin))
                        {
                            DebugLogger.error("Setting value '"+intValue+"' of setting '"+setting.name+"' out of bounds");
                            error=true;
                        }
                    }
                }
                catch (Exception e)
                {
                    DebugLogger.error("Illegal value '"+setting.value+"' of setting '"+setting.name+"'");
                    error=true;
                } 
                break;
            case DOUBLE:
                try
                {
                    doubleValue =Double.parseDouble(setting.value);
                    subStrings  =setting.allowed.split("[,]");
                    if (subStrings.length==2)
                    {
                        doubleMin=Double.parseDouble(subStrings[0]);
                        doubleMax=Double.parseDouble(subStrings[1]);
                        if ((doubleValue>doubleMax) || (doubleValue<doubleMin))
                        {
                            DebugLogger.error("Setting value '"+doubleValue+"' of setting '"+setting.name+"' out of bounds");
                            error=true;
                        }
                    }
                }
                catch (Exception e)
                {
                    DebugLogger.error("Illegal value '"+setting.value+"' of setting '"+setting.name+"'");
                    error=true;
                } 
                break;
            case BOOLEAN:
                if (!setting.value.equals("true") && !setting.value.equals("false") &&
                    !setting.value.equals("1") && !setting.value.equals("0") &&
                    !setting.value.equals("yes") && !setting.value.equals("no"))
                {
                    DebugLogger.error("Setting value '"+setting.value+"' of setting '"+setting.name+"' not allowed (use: yes, no, true, false, 1, 0)");
                    error=true;                    
                }
                break;
            case STRING:
                // Check if the value equals one of the allowed values,
                // unless no allowed values defined
                if (!setting.allowed.equals(""))
                {
                    subStrings  =setting.allowed.split("[,]");
                    i=0;
                    error=true;
                    while (i<subStrings.length && error)
                    {
                        if (setting.value.equals(subStrings[i]))
                        {
                            error=false;
                        }
                        i++;
                    }
                    if (error)
                    {
                        DebugLogger.error("Setting value '"+setting.value+"' of setting "+setting.name+" not allowed");
                    }
                }
                break;
                
        }
        
        return error;
    }
    
    public void dumpSettings()
    {
        int i;
        i=0;
        while (i<settings.length)
        {
            DebugLogger.info(settings[i].name+"="+settings[i].value);
            i++;
        }
    }
    
    /**
     * Returns the setting value as int value
     * @param name Setting name
     * @return The integer value or -1 if the setting is not the INT type or
     *         if the setting does not exist.
     */
    public int getIntValue(String name)
    {
        int         i;
        int         value;
        boolean     found;
        
        i       =0;
        found   =false;
        value   =-1;
        while (i<settings.length)
        {
            if (settings[i].name.equals(name))
            {
                if (settings[i].type==SettingType.INT)
                {
                    value=Integer.parseInt(settings[i].value);
                }
                else
                {
                    DebugLogger.error("Requesting int value of non-int setting '"+settings[i].name+"'");
                }
                found=true;
            }
            i++;
        }
        if (!found)
        {
            DebugLogger.error("Requesting value of non-existing setting '"+name+"'");
        }
        return value;
    }
    
    /**
     * Returns the setting value as double value
     * @param name Setting name
     * @return The double value or -1 if the setting is not the DOUBLE type or
     *         if the setting does not exist.
     */
    public double getDoubleValue(String name)
    {
        int         i;
        double      value;
        boolean     found;
        
        i       =0;
        found   =false;
        value   =-1;
        while (i<settings.length)
        {
            if (settings[i].name.equals(name))
            {
                if (settings[i].type==SettingType.DOUBLE)
                {
                    value=Double.parseDouble(settings[i].value);
                }
                else
                {
                    DebugLogger.error("Requesting double value of non-double setting '"+settings[i].name+"'");
                }
                found=true;
            }
            i++;
        }
        if (!found)
        {
            DebugLogger.error("Requesting value of non-existing setting '"+name+"'");
        }
        return value;
    }    

    /**
     * Returns the setting value as String value
     * @param name Setting name
     * @return The string value or "" if the setting is not the STRING type or
     *         if the setting does not exist.
     */
    public String getStringValue(String name)
    {
        int         i;
        String      value;
        boolean     found;
        
        i       =0;
        found   =false;
        value   ="";
        while (i<settings.length)
        {
            if (settings[i].name.equals(name))
            {
                if (settings[i].type==SettingType.STRING)
                {
                    value=settings[i].value;
                }
                else
                {
                    DebugLogger.error("Requesting string value of non-string setting '"+settings[i].name+"'");
                }
                found=true;
            }
            i++;
        }
        if (!found)
        {
            DebugLogger.error("Requesting value of non-existing setting '"+name+"'");
        }
        return value;
    }
    
    /**
     * Returns the setting value as boolean value
     * @param name Setting name
     * @return The boolean value or false if the setting is not the BOOLEAN type or
     *         if the setting does not exist.
     */
    public boolean getBooleanValue(String name)
    {
        int         i;
        boolean     value;
        boolean     found;
        
        i       =0;
        found   =false;
        value   =false;
        while (i<settings.length)
        {
            if (settings[i].name.equals(name))
            {
                if (settings[i].type==SettingType.BOOLEAN)
                {
                    if ((settings[i].value.equals("true")) || 
                        (settings[i].value.equals("yes") ) ||
                        (settings[i].value.equals("1")   ))
                    {
                        value=true;
                    }
                }
                else
                {
                    DebugLogger.error("Requesting boolean value of non-boolean setting '"+settings[i].name+"'");
                }
                found=true;
            }
            i++;
        }
        if (!found)
        {
            DebugLogger.error("Requesting value of non-existing setting '"+name+"'");
        }
        return value;
    }     
    
            
}
