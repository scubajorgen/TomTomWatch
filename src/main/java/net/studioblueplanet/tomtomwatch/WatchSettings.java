/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.logger.DebugLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Represents the list of settings as present in the Manifest File on the watch.
 * Manifest file: 0x0085000n
 * The setting definitions are read from a csv file, corresponding to the 
 * firmware version. Csv files are stored in the resources directory
 * @author Jorgen
 */
public class WatchSettings
{
    public class WatchSetting
    {
        private int index;
        private long value;
        
        /**
         * Constructor
         * @param index Tag/Index of the setting
         * @param value Value of the setting
         */
        public WatchSetting(int index, int value)
        {
            this.index      =index;
            this.value      =value;
        }
        
        /**
         * Sets the tag value
         * @param index The tag value
         */
        public void setIndex(int index)
        {
            this.index = index;
        }

        /**
         * Sets the value. It is set as long to support unsigned and signed
         * 32 bit int values
         * @param value The value
         */
        public void setValue(long value)
        {
            this.value = value;
        }

        /**
         * Returns the index value
         * @return The index value
         */
        public int getIndex()
        {
            return index;
        }

        /**
         * Returns the 32 bit value
         * @return The value. Represents a int, float or enum
         */
        public long getValue()
        {
            return value;
        }

    }
    
    
    /**
     * Represents the type of the setting
     */
    public enum SettingType
    {
        ENTRYTYPE_ENUM,
        ENTRYTYPE_INT,
        ENTRYTYPE_FLOAT
    }
    
    /**
     * Represents an enum value. Couples the integer value to a description 
     * string
     */
    public class EnumValue
    {
        public int      value;
        public String   description;
    }
    
    /**
     * Represents a setting (entry in the Manifest file of the watch)
     */
    public class ManifestEntry
    {
        public int         index;
        public String      settingName;
        public String      unit;
        public long        minValue;
        public long        maxValue;
        public SettingType   type;
        public boolean     writeable;
        public EnumValue[] enumValues;
        
        public String getDescriptionValue(int enumIndex)
        {
            int         i;
            boolean     found;
            String      enumDescription;
            
            enumDescription="unknown";
            found=false;
            i=0;
            while ((i<enumValues.length) && !found)
            {
                if (enumValues[i].value==enumIndex)
                {
                    enumDescription   =enumValues[i].description;
                    found       =true;
                }
                i++;
            }
            return enumDescription;
        }
        
    }
    
    /** List of settings */
    private ArrayList<WatchSetting>     settings;
    
    /** List of setting definitions */
    private ArrayList<ManifestEntry>    settingDefinitions;
    
    /**
     * Loads the setting definition file (csv) and stores the result 
     * in the settings definition list. Csv format:
     * {Index,Name,Type,Writable,Unit,min,max,enum value count,enum values}
     * 1st line is skipped
     * @param fileName Name of the file
     */
    private void loadSettingsDefinition(String fileName)
    {
        String          line = "";
        String          cvsSplitBy = ",";
        String[]        fields;
        String[]        enumValues;
        String[]        enumValueParts;
        ManifestEntry   entry;
        EnumValue       value;
        int             count;
        int             i;
        InputStream     inStream;
        BufferedReader br;
        
        if (settingDefinitions==null)
        {
            settingDefinitions=new ArrayList();
        }
        else
        {
            settingDefinitions.clear();
        }
        
        
        try  
        {
            inStream = getClass().getResourceAsStream("/net/studioblueplanet/tomtomwatch/resources/"+fileName); 
            br = new BufferedReader(new InputStreamReader(inStream));
            // header
            br.readLine();
            while ((line = br.readLine()) != null) 
            {
                // use comma as separator
                fields = line.split(cvsSplitBy);

                if (fields.length>=7)
                {
                    entry               =new ManifestEntry();
                    entry.index         =Integer.parseInt(fields[0]);
                    entry.settingName   =fields[1];
                    if (fields[2].trim().equals("INT"))
                    {
                        entry.type      =SettingType.ENTRYTYPE_INT;
                    }
                    else if (fields[2].trim().equals("FLOAT"))
                    {
                        entry.type      =SettingType.ENTRYTYPE_FLOAT;
                    }
                    else if (fields[2].trim().equals("ENUM"))
                    {
                        entry.type      =SettingType.ENTRYTYPE_ENUM;
                    }
                    if (fields[3].trim().equals("1"))
                    {
                        entry.writeable=true;
                    }
                    else
                    {
                        entry.writeable=false;
                    }
                    entry.unit          =fields[4].trim();
                    if ((entry.type==SettingType.ENTRYTYPE_INT) || (entry.type==SettingType.ENTRYTYPE_FLOAT))
                    {
                        entry.minValue=Long.parseLong(fields[5]);
                        entry.maxValue=Long.parseLong(fields[6]);
                        settingDefinitions.add(entry);
                    }
                    else if (entry.type==SettingType.ENTRYTYPE_ENUM)
                    {
                        count=Integer.parseInt(fields[7]);
                        enumValues=fields[8].split("\\|");
                        entry.enumValues=new EnumValue[enumValues.length];
                        i=0;
                        while (i<enumValues.length)
                        {
                            enumValueParts      =enumValues[i].split("=");
                            if (enumValueParts.length==2)
                            {
                                entry.enumValues[i]=new EnumValue();
                                entry.enumValues[i].value         =Integer.parseInt(enumValueParts[0].trim());
                                entry.enumValues[i].description   =enumValueParts[1].trim();
                            }
                            else
                            {
                                DebugLogger.error("Error decoding setting enum values");
                            }
                            i++;
                        }
                        settingDefinitions.add(entry);
                    }
                    else
                    {
                        DebugLogger.error("Error parsing setting definition: undefined setting type");
                    }
                    
                        
                }
                else
                {
                    DebugLogger.error("Error parsing settings definition file: invalid line length");
                }

            }

        } 
        catch (IOException e) 
        {
            DebugLogger.error("Error reading settings definition file");
        }   
    }
    
    /**
     * Loads the settings definitions, based on the firmware version
     * @param firmwareVersion Firmware version
     */
    private void loadSettingsDefinition(long firmwareVersion)
    {
        String fileName;
        
        fileName=null;
        
        if (firmwareVersion==0x0001000300FFL)
        {
            fileName="settings_00010003001b.csv";
        }
        else if (firmwareVersion==0x00010006001AL)
        {
            fileName="settings_00010003001b.csv";
        }
        if (fileName!=null)
        {
            loadSettingsDefinition(fileName);
        }
    }
    
    
    
    /**
     * Constructor
     * @param settingsData Byte array representing the manifest file data
     * @param firmwareVersion Software version 0x00hhmmll, hh-major, mm-mid, ll-minor version
     */
    public WatchSettings(byte[] settingsData, long firmwareVersion)
    {
        int             length;
        int             fileType;
        int             tag;
        int             value;
        int             i;
        WatchSetting    setting;
        
        this.settings=new ArrayList();
        
        fileType=ToolBox.readInt(settingsData, 0, 2, true);
        length=ToolBox.readInt(settingsData  , 2, 2, true);
        
        if (settingsData.length!= length*6+4)
        {
            DebugLogger.error("Settings file length inconstent");
        }
        
        DebugLogger.info("Decoding settings file");
        
        i=0;
        while (i<length)
        {
            tag     =ToolBox.readInt(settingsData, 4+i*6, 2, true);
            value   =ToolBox.readInt(settingsData, 6+i*6, 4, true);

            DebugLogger.info("Tag "+tag+" Value "+value);         
            
            setting =new WatchSetting(tag, value);
            
            settings.add(setting);
            i++;
        }
        loadSettingsDefinition(firmwareVersion);
        
        
    }
    
    /**
     * Get the setting definition corresponding to the given index.
     * @param index Index of the definition to search for
     * @return The setting definition or null if not found.
     */
    private ManifestEntry getManifestEntry(int index)
    {
        Iterator<ManifestEntry> it;
        ManifestEntry           entry;
        boolean                 found;
        
        found   =false;
        entry   =null;
        
        it=settingDefinitions.iterator();
        while (it.hasNext() && !found)
        {
            entry=it.next();
            if (entry.index==index)
            {
                found=true;
            }
        }
        if (!found)
        {
            entry=null;
        }
        return entry;
    }
    
    /**
     * Returns the list of settings as name-value pairs.
     * @return String containing the list
     */
    public String getSettingDescriptions()
    {
        Iterator<WatchSetting>  it;
        WatchSetting            setting;
        int                     index;
        ManifestEntry           entry;
        String                  valueString;
        String                  description;
        
        
        if (this.settingDefinitions!=null)
        {
            description="";
            it=settings.iterator();

            while (it.hasNext())
            {
                setting=it.next();

                index=setting.getIndex();
                entry=this.getManifestEntry(index);

                if (entry!=null)
                {
                    if (entry.type==SettingType.ENTRYTYPE_INT)
                    {
                        valueString=Integer.toString((int)setting.getValue());
                    }
                    else if (entry.type==SettingType.ENTRYTYPE_FLOAT)
                    {
                        valueString="";
                    }
                    else if (entry.type==SettingType.ENTRYTYPE_ENUM)
                    {
                        valueString=entry.getDescriptionValue((int)setting.getValue());
                    }
                    else
                    {
                        valueString="";
                    }
                    description+=String.format("%4d %-50s %s\n", index, entry.settingName, valueString);
                }
                else
                {
                    description+=String.format("%4d %-50s %s\n", index, "unknown", (int)setting.getValue()); 
                }

            }
        }
        else
        {
            description="No settings definition found for firmware version";
        }
        return description;
    }
    
    
    
    
}
