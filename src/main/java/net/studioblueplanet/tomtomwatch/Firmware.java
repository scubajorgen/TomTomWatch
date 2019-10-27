/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;


import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.usb.WatchInterface;
import net.studioblueplanet.generics.ToolBox;

import org.json.JSONObject;
import org.json.JSONException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import javax.swing.JOptionPane;


import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


/**
 *
 * @author jorgen.van.der.velde
 */
public class Firmware
{
    private static Firmware             theInstance=null;
    
    private long                        latestVersionAvailable;
    private String                      latestBleVersionAvailable;
    private String[]                    updatedFileNames;
    private final ArrayList<UsbFile>    backupFiles;
    private final ArrayList<UsbFile>    updatedFiles;
    private TomTomWatchView             theView;
    private WatchInterface              watchInterface;
    private String                      firmwareBaseUrl;
    private String                      bleFirmwareVersion;
    
    /**
     * Private constructor
     */
    private Firmware()
    {
        backupFiles      =new ArrayList<>();
        updatedFiles     =new ArrayList<>();
    }
    
    /**
     * Extract the latest firmware version out of the XML firmware definition
     * file
     * @param xmlDefinition Definition
     * @return False if no errors occurred
     */
    private boolean getLatestFirmwareVersion(String xmlDefinition)
    {
        boolean                 error;

        DocumentBuilderFactory  dbFactory;
        DocumentBuilder         dBuilder;
        InputSource             is;
        Document                doc;
        Element                 versionElement;    
        Element                 latestVersionElement;
        Element                 majorElement;
        Element                 minorElement;
        Element                 buildElement;
        int                     major;
        int                     minor;
        int                     build;
        NodeList                nodeList;
        NodeList                bleNodeList;
        Node                    node;
        int                     i;
        String                  bleUrl;
        int                     numberOfFiles;
        

        error                       =false;
        latestVersionAvailable      =0;
        bleUrl                      =null;
        
        try
        {
            major                               =-1;
            minor                               =-1;
            build                               =-1;
            dbFactory                           =DocumentBuilderFactory.newInstance();
            dBuilder                            =dbFactory.newDocumentBuilder();
            is                                  =new InputSource(new StringReader(xmlDefinition));
            doc                                 =dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            
            // Get the main element
            versionElement                      =doc.getDocumentElement();
            if (versionElement.getNodeName().equals("FirmwareVersion"))
            {
                nodeList                        =versionElement.getElementsByTagName("latestVersion");
                node                            =nodeList.item(0);
                if (node.getNodeType()==Node.ELEMENT_NODE)
                {
                    latestVersionElement=(Element)node;
                
                    nodeList                    =versionElement.getElementsByTagName("Major");
                    node                        =nodeList.item(0);
                    if (node.getNodeType()==Node.ELEMENT_NODE)
                    {
                        majorElement            =(Element)node;
                        major                   =Integer.parseInt(majorElement.getTextContent());
                    }
                    nodeList                    =versionElement.getElementsByTagName("Minor");
                    node                        =nodeList.item(0);
                    if (node.getNodeType()==Node.ELEMENT_NODE)
                    {
                        minorElement            =(Element)node;
                        minor                   =Integer.parseInt(minorElement.getTextContent());
                    }
                    nodeList                    =versionElement.getElementsByTagName("Build");
                    node                        =nodeList.item(0);
                    if (node.getNodeType()==Node.ELEMENT_NODE)
                    {
                        buildElement            =(Element)node;
                        build                   =Integer.parseInt(buildElement.getTextContent());
                    }

                    if ((major>=0) && (minor>=0) && (build>=0))
                    {
                        latestVersionAvailable  =(((long)major<<32)|
                                                  ((long)minor<<16)|
                                                  ((long)build));
                    }
                    else
                    {
                        error                   =true;
                        DebugLogger.error("Error parsing firmware version XML: invalid major, minor and/or build element");

                    }
                    
                    numberOfFiles=0;
                    
                    // Add BLE firmware update file: <BLE version="28" URL="BLE/28/0x00000012"/>
                    bleNodeList            =versionElement.getElementsByTagName("BLE");
                    if (bleNodeList.getLength()==1)
                    {
                        latestBleVersionAvailable=bleNodeList.item(0).getAttributes().getNamedItem("version").getNodeValue();
                        bleUrl=bleNodeList.item(0).getAttributes().getNamedItem("URL").getNodeValue();
                        numberOfFiles+=1;
                    }
                    else if (bleNodeList.getLength()==0)
                    {
                        latestBleVersionAvailable="-";
                        // No BLE firmware can occur. Nothing to be done
                    }
                    else if (bleNodeList.getLength()>1)
                    {
                        DebugLogger.error("Error parsing firmware version XML: multiple BLE lines");
                    }

                    
                    // Add watch firmware update files: <URL>1_8_52/0x000000F0</URL>
                    nodeList            =versionElement.getElementsByTagName("URL");
                    if (nodeList.getLength()>0)
                    {
                        numberOfFiles+=nodeList.getLength();
                        if (bleUrl!=null)
                        {
                            numberOfFiles++;
                        }
                        updatedFileNames=new String[numberOfFiles];
                        i=0;
                        while (i<nodeList.getLength())
                        {
                            node=nodeList.item(i);
                            if (node.getNodeType()==Node.ELEMENT_NODE)
                            {
                                updatedFileNames[i]=((Element)node).getTextContent();
                            }
                            else
                            {
                                error           =true;
                                DebugLogger.error("Error parsing firmware version XML: Error parsing file list");
                            }
                            i++;
                        }
                        // Add the BLE firmeware file
                        if (bleUrl!=null)
                        {
                            updatedFileNames[i]=bleUrl;
                        }
                    }
                    else
                    {
                        error                   =true;
                        DebugLogger.error("Error parsing firmware version XML: Error parsing file list");
                    }

                    
                }
                else
                {
                    error                       =true;
                    DebugLogger.error("Error parsing firmware version XML: latestVersion element invalid");
                }
            }
            else
            {
                DebugLogger.error("Error parsing firmware version XML: no FirmwareVersion element");
            }
        }
        catch (Exception e)
        {
            error                               =true;
            DebugLogger.error("Error parsing firmware version XML: "+e.getMessage());
        }
        
        return error;
    }        

    
   
    private boolean downloadFiles()
    {
        boolean             error;
        String              fileUrl;
        String              idString;
        int                 id; 
        UsbFile             file;
        ArrayList<UsbFile>  files;
        Iterator<UsbFile>   it;
        UsbFile             watchFile;
        boolean             found;
        
        int     i;
        
        error=false;
        
        // Get the list of files on the watch
        files=watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_ALL);
        if (files==null)
        {
            error       =true;
        }
        
        i=0;
        while ((i<updatedFileNames.length) && !error)
        {
            idString    =updatedFileNames[i].substring(updatedFileNames[i].indexOf("0x")+2, updatedFileNames[i].length());
            id          =Integer.parseInt(idString, 16);
            
            // backup the file, if it exists on the watch
            // First find in in the list of files on the watch
            it          =files.iterator();
            found       =false;
            while (!found && it.hasNext())
            {
                watchFile=it.next();
                if (watchFile.fileId==id)
                {
                    found       =true;
                    error       =watchInterface.readFile(watchFile);
                    backupFiles.add(watchFile);
                }
            }
            
            // Download the file from the tomtom site
            fileUrl     =this.firmwareBaseUrl+updatedFileNames[i];
            file        =new UsbFile();
            file.fileId =id;

            theView.appendStatus(String.format("Downloading 0x%08x from %s\n", file.fileId, fileUrl));
            file.fileData=ToolBox.readBytesFromUrl(fileUrl);
            if ((file.fileData==null) || (file.fileData.length==0))
            {
                error=true;
            }
            else
            {
                file.length=file.fileData.length;
                this.updatedFiles.add(file);
            }
            
            
            i++;
        }
        
        return error;
    }
    
    /**
     * This method sorts the files to write in the right order
     */
    private void sortFiles()
    {
        int     i;
        int     j;
        int     k;
        int     files;
        int[]   fileIDs={WatchInterface.FILEID_BLE_FIRMWARE, 
                         WatchInterface.FILEID_SYSTEM_FIRMWARE,
                         WatchInterface.FILEID_GPS_FIRMWARE,
                         WatchInterface.FILEID_MANIFEST1,
                         WatchInterface.FILEID_MANIFEST2};
        UsbFile file;
        boolean done;
        
        
        
        files=this.updatedFiles.size();
        i=0;
        k=0;
        done=false;
        while ((i<files-1) && !done)
        {
            j=i;
            while ((j<files) && !done)
            {
                if (updatedFiles.get(j).fileId==fileIDs[k])
                {
                    // Swap files
                    if (i!=j)
                    {
                        file=updatedFiles.get(j);
                        updatedFiles.set(j, updatedFiles.get(i));
                        updatedFiles.set(i, file);
                    }
                    i++;
                    
                }
                j++;
            }
            if (k<fileIDs.length-1)
            {
                k++;
            }
            else
            {
                done=true;
            }        
        }
    }
    
    /**
     * Writes the new files
     * @return True if an error occurred, false if not
     */
    private boolean writeFiles()
    {
        boolean             error;
        Iterator<UsbFile>   it;
        UsbFile             file;
                
        error=false;
        
        it=updatedFiles.iterator();
        while (it.hasNext() && !error)
        {
            file=it.next();
            
            error=watchInterface.writeVerifyFile(file);
        }
        
        return error;
    }
    

    
    
    /** 
     * Execute the firmware update
     * @return True if an error occurred, false if not
     */
    private boolean doUpdateFirmware()
    {
        boolean error;
        
        theView.appendStatus("Updating firmware: downloading files\n");

        // Backup files from the device and donwload the new files
        error               =downloadFiles();

        if (!error)
        {
            theView.appendStatus("Files downloaded, write firmware to watch...\n");

            // Write the files to the watch in predefined order
            sortFiles();

            error=writeFiles();

            if (!error)
            {
    //                        error=watchInterface.sendMessageGroup1();
                theView.appendStatus("Rebooting\n");
                error=watchInterface.resetDevice();
                if (!error)
                {
                    theView.appendStatus("Done! Disconnect and reconnect watch\n");
                }
                else
                {
                    // Error while rebooting. is to be expected
                    theView.appendStatus("Done! Disconnect and reconnect watch\n");
                }
            }
            else
            {
                theView.appendStatus("Error writing files!! No reboot executed\n");
            }
        }
        else
        {
            theView.appendStatus("Error downloading files. Not executing update.");
        }
        return error;
    }
    
    
    
    
    
    /*############################################################################################*\
     * PUBLIC FUNCTIONS     
    \*############################################################################################*/    
    /**
     * This method returns the one and only instance of this class
     * @return The instance
     */
    public static Firmware getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new Firmware();
        }
        return theInstance;
    }    
    
    /**
     * Defines the view to use for feedback
     * @param view The TomTomWatch view
     */
    public void setView(TomTomWatchView view)
    {
        this.theView=view;
    }
    
    /**
     * Check the latest firmware and update the firmware if it is newer than the 
     * version in the watch
     * @param watchInterface The WatchInterface to use 
     * @param productId Product ID of the watch
     * @param currentFirmware Current firmware int the watch
     * @return False if an error occurred, true if successful
     */
    public boolean updateFirmware(WatchInterface watchInterface, int productId, long currentFirmware)
    {
        
        boolean     error;
        String      configUrl;
        String      firmwareDefinition;
        String      firmwareDefinitionUrl;
        String      page;
        JSONObject  jsonObj;
        Pattern     pattern;
        Matcher     matcher;
        int         latest;
        String      feedback;
        int         response;
        
        error                   =false;

        this.watchInterface     =watchInterface;
        
        error=prepareFirmware(watchInterface, productId);
        
        feedback = "Current firmware: "+((currentFirmware>>32)&0xff)+"."+
                                        ((currentFirmware>>16)&0xff)+"."+
                                        ((currentFirmware    )&0xff)+
                   ", Latest firmware: "+((latestVersionAvailable>>32)&0xff)+"."+
                                        ((latestVersionAvailable>>16)&0xff)+"."+
                                        ((latestVersionAvailable    )&0xff)+"\n";
        feedback+="Current BLE firmware: "+this.bleFirmwareVersion+
                  ", Latest BLE firmware: "+this.latestBleVersionAvailable+"\n";
        theView.setStatus(feedback);
        

        if (!error && latestVersionAvailable>currentFirmware)
        {
            response = JOptionPane.showConfirmDialog(null, "New firmware found. Execute update?", "Confirm",
                                                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) 
            {
                error=doUpdateFirmware();
            } 
        }
        else
        {
            theView.appendStatus("Firmware up to date.");            
        }

        return error;
    }
    

    
    
    /**
     * Get the firmware version and download the firmware files
     * @param watchInterface Watch interface to use to communicate to the watch
     * @param productId Product ID of the watch
     * @return False if an error occurred, true if successful
     */
    public boolean prepareFirmware(WatchInterface watchInterface, int productId)
    {
        boolean     error;
        String      firmwareDefinition;
        String      firmwareDefinitionUrl;
        String      page;
        JSONObject  jsonObj;
        String      configUrl;
        
        // The configuration URL
        configUrl               =watchInterface.getPreference("ConfigURL");
        firmwareDefinitionUrl   =null;
        error                   =false;
        
        if (configUrl!=null)
        {
            // Read the page at the config URL
            page                    =ToolBox.readStringFromUrl(configUrl);

            if (page!=null)
            {
                // Get the firmware definition URL. Fill in the approriate product ID
                jsonObj                 =new JSONObject(page);
                try
                {
                    firmwareDefinitionUrl   =jsonObj.getString("service:firmware");
                }
                catch (JSONException e)
                {
                    theView.showErrorDialog("Error downloading firmware: no service URL from "+configUrl);
                    error=true;                       
                }
                if (firmwareDefinitionUrl!=null)
                {
                    firmwareDefinitionUrl   =firmwareDefinitionUrl.replace("{PRODUCT_ID}", String.format("%08x", productId).toUpperCase());

                    DebugLogger.info("Firmware definition URL: "+firmwareDefinitionUrl);

                    // Derive the base url
                    firmwareBaseUrl         =firmwareDefinitionUrl.substring(0, firmwareDefinitionUrl.lastIndexOf('/')+1);

                    // Read the XML defining the last firmware version avaialble
                    firmwareDefinition      =ToolBox.readStringFromUrl(firmwareDefinitionUrl);

                    if (firmwareDefinition!=null)
                    {
                        // Get the latest firmware version and download the firmware files
                        error               =getLatestFirmwareVersion(firmwareDefinition);                     
                    }
                    else
                    {
                        theView.showErrorDialog("Error downloading firmware: no information from "+firmwareDefinitionUrl);
                        error=true;
                    }
                    // Get BLE firmware version
                    bleFirmwareVersion=watchInterface.readBleVersion();

                    if (bleFirmwareVersion==null)
                    {
                        theView.showErrorDialog("Error downloading firmware: no BLE info from watch");
                        error=true;
                    }                     
                }
            }
            else
            {
                theView.showErrorDialog("Error downloading firmware: no information from "+configUrl);
                error=true;
            }
        }
        else
        {
            theView.showErrorDialog("Error downloading firmware: no config URL found in preferences");
            error=true;            
        }
        return error;
    }
    

    /**
     * Thist method executes a firmware update. The method requires 
     * prepareFirmware to have been executed.
     * @param watchInterface The WatchInterface to use 
     * @return False if an error occurred, true if succesfull
     */
    public boolean forceUpdateFirmware(WatchInterface watchInterface)
    {
        
        boolean     error;
        String      firmwareDefinition;
        String      firmwareDefinitionUrl;
        String      page;
        JSONObject  jsonObj;
        Pattern     pattern;
        Matcher     matcher;
        int         latest;
        String      feedback;
        int         response;
        
        error                   =false;

        this.watchInterface     =watchInterface;
        
        feedback = "Downloading firmware: "+((latestVersionAvailable>>32)&0xff)+"."+
                                          ((latestVersionAvailable>>16)&0xff)+"."+
                                          ((latestVersionAvailable    )&0xff);
        theView.appendStatus(feedback+"\n");
        
        if (!error)
        {
            error=doUpdateFirmware();
        }
        return error;
    }



    
}
