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
import java.util.TreeMap;
import java.util.Map;
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
        ENTRYTYPE_ENUM("ENUM"),
        ENTRYTYPE_INT("INT"),
        ENTRYTYPE_FLOAT("FLOAT");
        
        private final String value;
        
        SettingType(String value)
        {
            this.value=value;
        }
        
        @Override
        public String toString()
        {
            return value;
        }
    }
    
    /**
     * Represents a setting (entry in the Manifest file of the watch)
     */
    public class ManifestEntry implements Comparable<ManifestEntry>
    {
        public int                          index;
        public String                       settingName;
        public String                       unit;
        public Long                         minValue;
        public Long                         maxValue;
        public Long                         scaling;
        public SettingType                  type;
        public boolean                      writeable;
        public Map<Integer, String>         enumValues;
        
        public ManifestEntry()
        {
            enumValues=new TreeMap<>();
        }
        
        public String getDescriptionValue(int enumIndex)
        {
            return enumValues.get(enumIndex);
        }
        
        @Override
        public int compareTo(ManifestEntry entry)
        {
            return index-entry.index;
        }
        
    }
    
    /** List of settings */
    private ArrayList<WatchSetting>             settings;
    
    /** List of setting definitions */
    private ArrayList<ManifestEntry>            settingDefinitions;
    
    private byte[]                              settingsData;

    private String                              version;

    private boolean                             isChanged;
    
    /**
     * Indicates whether the settings as loaded have been changed since loading
     * @return True if changed, false if not
     */
    public boolean isChanged()
    {
        return isChanged;
    }

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
        int             i;
        int             count;
        InputStream     inStream;
        BufferedReader br;
        
        if (settingDefinitions==null)
        {
            settingDefinitions=new ArrayList<>();
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
                        i=0;
                        while (i<enumValues.length)
                        {
                            enumValueParts      =enumValues[i].split("=");
                            if (enumValueParts.length==2)
                            {
                                entry.enumValues.put(Integer.valueOf(enumValueParts[0].trim()), enumValueParts[1].trim());
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
     * Loads the setting definition file (csv) and stores the result 
     * in the settings definition list. 
     * TTWatch manifest format
     * 1st line is skipped
     * @param fileName Name of the file
     */
    private void loadSettingsManifest(String fileName)
    {
        String          line = "";
        String          cvsSplitBy = ",";
        String[]        fields;
        String[]        enumValues;
        ManifestEntry   entry;
        int             i;
        InputStream     inStream;
        BufferedReader  br;
        
        if (settingDefinitions==null)
        {
            settingDefinitions=new ArrayList<>();
        }
        else
        {
            settingDefinitions.clear();
        }
        
        
        try  
        {
            inStream = getClass().getResourceAsStream("/net/studioblueplanet/tomtomwatch/resources/"+fileName); 
            br = new BufferedReader(new InputStreamReader(inStream));
            while ((line = br.readLine()) != null) 
            {
                if (line.trim().startsWith("#VERSION"))
                {
                    version=line.substring(9, 17);
                }
                else if (!line.trim().startsWith("#"))
                {
                    // use comma as separator
                    fields = line.split(cvsSplitBy, -1);

                    if (fields.length>=5)
                    {
                        entry               =new ManifestEntry();
                        entry.index         =Integer.parseInt(fields[1]);
                        entry.settingName   =fields[0];
                        switch(fields[2].trim().charAt(0))
                        {
                            case 'i':
                                entry.type      =SettingType.ENTRYTYPE_INT;
                                entry.unit      =fields[4];
                                if (!fields[5].equals(""))
                                {
                                    entry.minValue=Long.valueOf(fields[5]);
                                }
                                if (!fields[6].equals(""))
                                {
                                    entry.maxValue=Long.valueOf(fields[6]);
                                }
                                break;
                            case 'f':
                                entry.type      =SettingType.ENTRYTYPE_FLOAT;
                                entry.unit      =fields[4];
                                if (!fields[5].equals(""))
                                {
                                    entry.scaling=Long.valueOf(fields[5]);
                                }
                                if (!fields[6].equals(""))
                                {
                                    entry.minValue=Long.valueOf(fields[6]);
                                }
                                if (!fields[7].equals(""))
                                {
                                    entry.maxValue=Long.valueOf(fields[7]);
                                }
                                break;
                            case 'e':
                                entry.type      =SettingType.ENTRYTYPE_ENUM;
                                i=4;
                                while (i<fields.length)
                                {
                                    enumValues=fields[i].split("=");
                                    if (enumValues.length==2)
                                    {
                                        entry.enumValues.put(Integer.valueOf(enumValues[0]), enumValues[1].trim());
                                    }
                                    else
                                    {
                                        DebugLogger.error("Error parsing settings definition file: invalid enum");
                                    }
                                    i++;
                                }
                                entry.unit="";
                                break;
                        }
                        
                        if (fields[3].trim().equals("1"))
                        {
                            entry.writeable=true;
                        }
                        else
                        {
                            entry.writeable=false;
                        }                        
                        settingDefinitions.add(entry);
                    }
                    else
                    {
                        DebugLogger.error("Error parsing settings definition file: invalid line length");
                    }
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
        
        // RUNNER, ADVENTURER
        if (firmwareVersion==0x000100010013L)       // 1.1.19
        {
            fileName="manifest_00010113.txt";
        }
        if (firmwareVersion==0x00010003001BL)       // 1.3.27
        {
            fileName="manifest_0001031b.txt";
        }
        if (firmwareVersion==0x0001000300FFL)       // 1.3.255
        {
            fileName="manifest_0001031b.txt";
        }
        else if (firmwareVersion==0x00010006001AL)  // 1.6.26
        {
            fileName="manifest_0001031b.txt";
        }
        else if (firmwareVersion==0x000100070035L)  // 1.7.53
        {
            fileName="manifest_0001031b.txt";
        }
        else if (firmwareVersion==0x00010007003EL)  // 1.7.62
        {
            fileName="manifest_0001073e.txt";
        }
        else if (firmwareVersion==0x000100070040L)  // 1.7.64
        {
            fileName="manifest_0001073e.txt";
        }
        
        // MULTISPORTS
        else if (firmwareVersion==0x000100080019L)  // 1.8.25
        {
            fileName="manifest_00010819.txt";
        }
        else if (firmwareVersion==0x00010008002EL)  // 1.8.46
        {
            fileName="manifest_0001082e.txt";
        }
        else if (firmwareVersion==0x000100080034L)  // 1.8.52
        {
            fileName="manifest_0001082e.txt";
        }

        // Support for TTWATCH manifest and CSV settings definition
        if (fileName!=null)
        {
            if (fileName.startsWith("manifest"))
            {
                this.loadSettingsManifest(fileName);
            }
            else
            {
                loadSettingsDefinition(fileName);
            }
        }
    }
    
    
    /**
     * Convert the byte data (from 0x0085000n file) to the settings array
     * @param settingsData Data to convert
     */
    private void convertDataToSettings(byte[] settingsData)
    {
        int             length;
        int             fileType;
        int             tag;
        int             value;
        int             i;
        WatchSetting    setting;
        
        this.settings=new ArrayList<>();
        
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

            DebugLogger.debug("Tag "+tag+" Value "+value);         
            
            setting =new WatchSetting(tag, value);
            
            settings.add(setting);
            i++;
        }    
        isChanged=false;    
    }

    /**
     * Convert settings array to the byte data (from 0x0085000n file)
     * @return Byte array containing the content 
     */
    public byte[] convertSettingsToData()
    {
        byte[]  data;
        int     i;
        
        data=null;
        
        if (settings.size()>0)
        {
            data=new byte[4+6*settings.size()];
            ToolBox.writeUnsignedInt(data,          0x0085, 0, 2, true); // File id/version
            ToolBox.writeUnsignedInt(data, settings.size(), 2, 2, true); // Length
            
            i=0;
            while (i<settings.size())
            {
                ToolBox.writeUnsignedInt (data, settings.get(i).getIndex(), 4+i*6, 2, true);
                ToolBox.writeUnsignedLong(data, settings.get(i).getValue(), 6+i*6, 4, true);
                i++;
            }
        }        
        return data;
    }
    
    
    /**
     * Constructor. Reads the settings from data and the settings definition from file.
     * @param settingsData Byte array representing the manifest file data
     * @param firmwareVersion Software version 0x0000hhhhmmmmllll, 
     *                        hhhh-major, mmmm-mid, llll-minor version
     */
    public WatchSettings(byte[] settingsData, long firmwareVersion)
    {
        this.settingsData   =settingsData;
        convertDataToSettings(settingsData);
        loadSettingsDefinition(firmwareVersion);
    
        convertSettingsToData();
        isChanged=false;
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
    
    /**
     * Find setting by name
     * @param settingName name of the setting
     * @return The setting value or null if not found
     */
    private WatchSetting findSetting(String settingName)
    {
        Iterator<ManifestEntry> itEntry;
        ManifestEntry           entry;
        Iterator<WatchSetting>  itSetting;
        WatchSetting            setting;
        WatchSetting            returnSetting;
        boolean                 found;
        
        itEntry         =settingDefinitions.iterator();
        found           =false;
        entry           =null;
        returnSetting   =null;
        while (itEntry.hasNext() && !found)
        {
            entry=itEntry.next();
            if (entry.settingName.equals(settingName))
            {
                found=true;
            }
        }
        if (found)
        {
            if (entry!=null)
            {
                found=false;
                itSetting=settings.iterator();
                while (itSetting.hasNext() && !found)
                {
                    setting=itSetting.next();
                    if (setting.getIndex()==entry.index)
                    {
                        returnSetting=setting;
                        found=true;
                    }
                }
                if (!found)
                {
                    DebugLogger.error("Value with setting index "+entry.index+" not found in setting array");
                }
            }
            else
            {
                DebugLogger.error("Setting "+settingName+" appears not to be a valid setting name");
            }
        }
        else
        {
            DebugLogger.error("Setting "+settingName+" not found");
        }     
        return returnSetting;
    }
    
    /**
     * Get setting value by name, assuming an integer type of setting
     * @param settingName Name of the setting
     * @return The setting value as long or -1 if not found.
     */
    public long getSettingsValueInt(String settingName)
    {
        WatchSetting            setting;
        long                    value;
        
        setting=this.findSetting(settingName);
        if (setting!=null)
        {
            value=setting.getValue();
        }
        else
        {
            value=-1L;
        }
        return value;
    }
    
    /**
     * This method modifies the value with given settings name
     * @param settingName Name of the setting to modify
     * @param value New value of the setting
     */
    public void setSettingsValueInt(String settingName, long value)
    {
        WatchSetting            setting;
        
        setting=this.findSetting(settingName);
        if (setting!=null)
        {
            long oldValue=setting.value;
            if (value!=oldValue)
            {
                isChanged=true;
            }
            setting.setValue(value);
        }
    }
    
    /**
     * Create a settings definition CSV string representing the manifest
     * @return String representing the csv
     */
    public String settingsManifestToSettingsCsv()
    {
        String output;
        output="Index,Name,Type,Writable,Unit,min,max,enum count,enum values\n";
        java.util.Collections.sort(settingDefinitions);
        for (ManifestEntry entry : settingDefinitions)
        {
            output+=String.format("%d,%s,%s,%d,%s,", entry.index, entry.settingName, entry.type, entry.writeable?1:0, entry.unit);
            switch (entry.type)
            {
                case ENTRYTYPE_ENUM:
                    output+=String.format(",,%d,", entry.enumValues.size());
                    Iterator<Integer> it=entry.enumValues.keySet().iterator();
                    while (it.hasNext())
                    {
                        Integer key=it.next();
                        String value=entry.enumValues.get(key);
                        output+=String.format("%d=%s", key, value);
                        if (it.hasNext())
                        {
                            output+="|";
                        }
                    }
                    break;
                case ENTRYTYPE_INT:
                    if (entry.minValue!=null)
                    {
                        output+=String.format("%d", entry.minValue);
                    }
                    output+=",";

                    if (entry.maxValue!=null)
                    {
                        output+=String.format("%d,,", entry.maxValue);
                    }
                    output+=",,";
                    break;
                case ENTRYTYPE_FLOAT:
                    if (entry.minValue!=null)
                    {
                        output+=String.format("%d", entry.minValue);
                    }
                    output+=",";

                    if (entry.maxValue!=null)
                    {
                        output+=String.format("%d,,", entry.maxValue);
                    }
                    output+=",,";
                    break;
            }
            output+="\n";
        }
        return output;
    }

    /**
     * Create a settings definition CSV string representing the manifest
     * @return String representing the csv
     */
    public String settingsManifestToManifestCsv()
    {
        String  output;
        
        output="# name,index,e,writable,0=first,1=second etc...\n" +
               "# name,index,i,writable,units,min,max\n" +
               "# name,index,f,writable,units,scaling factor,min,max\n"+
               String.format("#VERSION=%s\n", version);
        java.util.Collections.sort(settingDefinitions);
        for (ManifestEntry entry : settingDefinitions)
        {
            switch (entry.type)
            {
                case ENTRYTYPE_ENUM:
                    output+=String.format("%s,%d,e,%d,", entry.settingName, entry.index, entry.writeable?1:0);
                    Iterator<Integer> it=entry.enumValues.keySet().iterator();
                    while (it.hasNext())
                    {
                        Integer key=it.next();
                        String value=entry.enumValues.get(key);
                        output+=String.format("%d=%s", key, value);
                        if (it.hasNext())
                        {
                            output+=",";
                        }
                    }
                    break;
                case ENTRYTYPE_INT:
                    output+=String.format("%s,%d,i,%d,%s,", entry.settingName, entry.index, entry.writeable?1:0,entry.unit);
                    if (entry.minValue!=null)
                    {
                        output+=String.format("%d", entry.minValue);
                    }
                    output+=",";

                    if (entry.maxValue!=null)
                    {
                        output+=String.format("%d", entry.maxValue);
                    }
                    break;
                case ENTRYTYPE_FLOAT:
                    output+=String.format("%s,%d,f,%d,%s,", entry.settingName, entry.index, entry.writeable?1:0, entry.unit);
                    if (entry.scaling!=null)
                    {
                        output+=String.format("%d", entry.scaling);
                    }
                    output+=",";
                    if (entry.minValue!=null)
                    {
                        output+=String.format("%d", entry.minValue);
                    }
                    output+=",";

                    if (entry.maxValue!=null)
                    {
                        output+=String.format("%d", entry.maxValue);
                    }
                    break;
            }
            output+="\n";
        }
        return output;
    }

    
}
