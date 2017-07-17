/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.usb.ProgressListener;
import net.studioblueplanet.usb.WatchInterface;
import net.studioblueplanet.usb.UsbInterface;
import net.studioblueplanet.usb.UsbTestInterface;
import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.TomTomReader;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.settings.ConfigSettings;


import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import hirondelle.date4j.DateTime;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;


import com.google.gson.Gson;
import org.json.JSONObject;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import net.studioblueplanet.generics.ToolBox;

/**
 * This class executes the commands involving communication to the device.
 * The commands are processed in a separate thread
 * @author jorgen.van.der.velde
 */
public class CommunicationProcess implements Runnable, ProgressListener
{
    /** Parent view */
    private static TomTomWatchView              theView;
    
    /** The instance */
    private static CommunicationProcess         theInstance=null;
    

    /** Processing thread */
    private final Thread                        thread;

    // Guarded data
    private final LinkedList<ThreadCommand>     commandQueue;
    private final ArrayList<ActivityData>       activities;
    private String                              newDeviceName;
    private int                                 fileIdToWrite;
    private int                                 fileIdToDelete;
    private int                                 fileIdToShow;
    private String                              deviceName;
    private boolean                             isConnected;
    private boolean                             threadExit;
    
    private String                              uploadFile;
    private String                              uploadName;
    private final String                        ttbinFilePath;
    private final String                        debugFilePath;
    private int                                 productId;
    private int                                 currentFirmwareVersion;
    private boolean                             simulationMode;
    private String                              simulationFilePath;
    // End of guarded data

    // Progress listener data
    private long                                bytesToDownload;
    private long                                bytesDownloaded;
    
    
    /**
     * Constructor. Initializes the instance 
     */
    private CommunicationProcess()
    {
        ConfigSettings  settings;
        
        activities          =new ArrayList();
        isConnected         =false;
        threadExit          =false;
        commandQueue        =new LinkedList();
        productId           =WatchInterface.PRODUCTID_UNKNOWN;
        
        fileIdToWrite       =WatchInterface.FILEID_INVALID;
        fileIdToDelete      =WatchInterface.FILEID_INVALID;
        
        settings            =ConfigSettings.getInstance();
        ttbinFilePath       =settings.getStringValue("ttbinFilePath");
        debugFilePath       =settings.getStringValue("debugFilePath");
        simulationMode      =settings.getBooleanValue("simulation");
        simulationFilePath  =settings.getStringValue("simulationPath");

        
        if (!simulationFilePath.endsWith("/") && !simulationFilePath.endsWith("\\"))
        {
            simulationFilePath+="/";
        }

        
        // Start the processing thread
        thread          = new Thread(this);
        thread.start();
    }
    
    /*############################################################################################*\
     * PUBLIC METHODS
    \*############################################################################################*/    
    /**
     * Returns the one and only instance of this class.
     * @return 
     */
    public static CommunicationProcess getInstance(TomTomWatchView view)
    {
        theView         =view;
        if (theInstance==null)
        {
            theInstance =new CommunicationProcess();
        }
        return theInstance;
    }
    
    /**
     * Push a command for executing on the command queue
     * @param command Command to push on the queue
     */
    public void pushCommand(ThreadCommand command)
    {
        synchronized(this)
        {
            this.commandQueue.addLast(command);
        }
    }
    

    /**
     * Writes a new device name to the watch.
     * The request is processed asynchronously by the thread.
     * @param name The new name
     */
    public void requestSetNewDeviceName(String name)
    {
        Pattern         pattern;
        Matcher         matcher;
        
        // Arbitrary device name check... No funny chars, max 30 chars
        pattern = Pattern.compile("(^[a-zA-Z0-9_\\s]{1,30}$)");
        matcher = pattern.matcher(name);
        if (matcher.find())
        {
            synchronized(this)
            {
                this.newDeviceName=name;
            }
            this.pushCommand(ThreadCommand.THREADCOMMAND_SETNAME);
        }
        else
        {
            JOptionPane.showMessageDialog(theView, "Illegel Watch Name", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Writes a file from the watch as binary file to disk.
     * The request is processed asynchronously by the thread.
     * @param fileId Id of the file
     */
    public void requestWriteDeviceFileToDisk(int fileId)
    {
        synchronized(this)
        {
            this.fileIdToWrite=fileId;
            this.pushCommand(ThreadCommand.THREADCOMMAND_SAVEFILE);
        }
    }
    
    /**
     * Deletes the file from the watch.
     * The request is processed asynchronously by the thread.
     * @param fileId Id of the file
     */
    public void requestDeleteDeviceFileFromWatch(int fileId)
    {
        synchronized(this)
        {
            this.fileIdToDelete=fileId;
            this.pushCommand(ThreadCommand.THREADCOMMAND_DELETEFILE);
        }
    }


    /**
     * Converts and upload a GPX route file. 
     * The request is processed asynchronously by the thread.
     * @param file Filename of the file to upload
     * @param name Name of the route by which it becomes visible on the 
     *             watch
     */
    public void requestUploadGpxFile(String file, String name)
    {
        synchronized(this)
        {
            this.uploadFile=file;
            this.uploadName=name;
        }
        this.pushCommand(ThreadCommand.THREADCOMMAND_UPLOADROUTE);
        this.pushCommand(ThreadCommand.THREADCOMMAND_LISTROUTES);
    }


    /**
     * This method requests to display the contents of indicated file.
     * The request is processed asynchronously by the thread.
     * @param fileId ID of file to show
     */
    public void requestShowFile(int fileId)
    {
        synchronized(this)
        {
            fileIdToShow    =fileId;
        }
        this.pushCommand(ThreadCommand.THREADCOMMAND_SHOWFILE);
    }

    /**
     * Stop the process.
     * The request is processed asynchronously by the thread.
     */
    public void requestStop()
    {
        synchronized(this)
        {
            this.threadExit=true;
        }
    }

    /**
     * Get the ActivityData based on the index in the array
     * @param index Array index
     * @return The data or null if not found
     */
    public ActivityData getActivityData(int index)
    {
        ActivityData data;
        
        data = null;
        synchronized (this)
        {
            if ((index >= 0) && (index < activities.size()))
            {
                data = activities.get(index);
            }
        }
        return data;        
    }

    
    
    
    /**
     * Indicates whether a watch is connected
     * @return 
     */
    public boolean isConnected()
    {
        boolean localIsConnected;
        
        synchronized(this)
        {
            localIsConnected=this.isConnected;
        }
        return localIsConnected;
    }
    
    /**
     * Returns the device name
     * @return The device name or null if not connected
     */
    public String getDeviceName()
    {
        String localDeviceName;
        
        synchronized(this)
        {
            localDeviceName=deviceName;
        }
        return localDeviceName;
    }
    
    
    /**
     * This method sets the track smoothing. Call before downloading TTBINs
     * @param enabled Indicates whether smoothing is enabled
     * @param qFactor The Q Factor for smoothing
     */
    public void setTrackSmoothing(boolean enabled, float qFactor)
    {
        TomTomReader    reader;
        
        synchronized(this)
        {
            reader                  =TomTomReader.getInstance();
            reader.setTrackSmoothing(enabled, qFactor);            
        }
    }
    
    /*############################################################################################*\
     * THE THREAD METHOD     
    \*############################################################################################*/    
    /**
     * Process executing the communication commands
     */
    @Override
    public void run()
    {
        boolean         connected;
        boolean         error;
        boolean         exit;
        WatchInterface  watchInterface;
        ThreadCommand   localCommand;
        DateTime        time;

        // Get the interface to the USB
        synchronized(this)
        {
            if (simulationMode)
            {
                watchInterface  = UsbTestInterface.getInstance(this.simulationFilePath);
            }
            else
            {
                watchInterface  = UsbInterface.getInstance();
            }
        }        
        
        exit            =false;
        error           =false;
        localCommand    =ThreadCommand.THREADCOMMAND_NONE;
        
        // Do until the thread is stopped
        while (!exit)
        {
            // Try to connect to the device, and keep trying till succeeds
            connected       = false;
            while (!connected && !exit)
            {
                error = watchInterface.openConnection();
                if (!error)
                {
                    connected = true;
                    synchronized (this)
                    {
                        isConnected=true;
                        // First thing to do: request and display device name and firmware version
                        this.pushCommand(ThreadCommand.THREADCOMMAND_GETNAME);
                        this.pushCommand(ThreadCommand.THREADCOMMAND_GETFIRMWAREVERSION);
                        this.pushCommand(ThreadCommand.THREADCOMMAND_GETPRODUCTID);
                    }
                } 
                else
                {
                    // Not succeeded: wait and try again
                    try
                    {
                        Thread.sleep(1000);
                    } 
                    catch (InterruptedException e)
                    {

                    }
                    // Process command
                    synchronized (this)
                    {
                        exit=this.threadExit;
                    }

                }
            }
        
//            error=watchInterface.sendStartupSequence();
            
            // Get device serial. Does not seem to work :-(
            watchInterface.getDeviceSerialNumber();
            
            
            while (connected && !exit && !error)
            {
                // Poll the command buffer
                synchronized (this)
                {
                    exit=this.threadExit;
                    localCommand        = commandQueue.pollFirst();
                }

                // Default when nothing polled: get the time from the watch
                if (localCommand == null)
                {
                    localCommand        = ThreadCommand.THREADCOMMAND_GETTIME;
                }

                // Execute command
                switch (localCommand)
                {
                    case THREADCOMMAND_GETTIME:
                        time = watchInterface.getWatchTime();
                        if (time != null)
                        {
                            theView.showTime(time);
                        } 
                        else
                        {
                            error = true;
                        }
                        break;
                    case THREADCOMMAND_DOWNLOAD:
                        error = downloadActivityFiles(watchInterface);
                        break;
                    case THREADCOMMAND_ERASE:
                        error=deleteActivityFiles(watchInterface);
                        break;
                    case THREADCOMMAND_UPLOADGPSDATA:
                        error=uploadGpsData(watchInterface);
                        break;
                    case THREADCOMMAND_PREFERENCES:
                        error=getXmlPreferences(watchInterface);
                        break;
                    case THREADCOMMAND_LISTFILES:
                        error=listFiles(watchInterface);
                        break;
                    case THREADCOMMAND_GETNAME:
                        error=getDeviceName(watchInterface);
                        break;
                    case THREADCOMMAND_SETNAME:
                        error=setDeviceName(watchInterface);
                        break;
                    case THREADCOMMAND_SAVEFILE:
                        error=saveDeviceFile(watchInterface);
                        break;
                    case THREADCOMMAND_DELETEFILE:
                        error=deleteDeviceFile(watchInterface);
                        break;
                    case THREADCOMMAND_LISTHISTORY:
                        error=listHistory(watchInterface);
                        break;
                    case THREADCOMMAND_LISTHISTORYSUMMARY:
                        error=listHistorySummary(watchInterface);
                        break;
                    case TRHEADCOMMAND_CLEARDATA:
                        error=clearData(watchInterface);
                        break;
                    case THREADCOMMAND_UPLOADROUTE:
                        error=uploadRouteFile(watchInterface);
                        break;
                    case THREADCOMMAND_LISTROUTES:
                        error=listRouteFiles(watchInterface);
                        break;
                    case THREADCOMMAND_CLEARROUTES:
                        error=this.clearRouteFiles(watchInterface);
                        break;
                    case THREADCOMMAND_GETPRODUCTID:
                        error=getProductId(watchInterface);
                        break;
                    case THREADCOMMAND_GETFIRMWAREVERSION:
                        error=getFirmwareVersion(watchInterface);
                        break;
                    case THREADCOMMAND_UPDATEFIRMWARE:
                        error=updateFirmware(watchInterface);
                        break;
                    case THREADCOMMAND_SAVESIMULATIONSET:
                        error=saveSimulationSet(watchInterface);
                        break;
                    case THREADCOMMAND_SHOWFILE:
                        error=showFile(watchInterface);
                        break;
                }

                // Sleep for a while
                try
                {
                    Thread.sleep(1000);
                } 
                catch (InterruptedException e)
                {

                }
            }
            // If an error occurred, it means something went wrong
            // at USB level. Therefore reset the connection
            if (error)
            {
                watchInterface.closeConnection();
                connected=false;
                synchronized(this)
                {
                    // Forget all about the watch...
                    this.productId  =WatchInterface.PRODUCTID_UNKNOWN;
                    this.deviceName ="Unknown";
                    isConnected=false;
                }
                this.clear();
            }
        }
        
    }

    /*############################################################################################*\
     * HELPERS
    \*############################################################################################*/    
    /**
     * Removes any track data. Erase acitivities, clear the list
     */
    private void clear()
    {
        // Remove any existing activities
        synchronized (this)
        {
            activities.clear();
        }
        theView.clear();
    }

    
    /**
     * Sorts the arraylist with USB files.
     * @param files ArrayList with USB files.
     */
    private void sort(ArrayList<UsbFile> files)
    {
        int         i;
        int         j;
        UsbFile     file;
        UsbFile     temp;
        int         minId;
        int         minIdIndex;
        

        // Process array elements
        j=0;
        while (j<files.size())
        {
            // Find miminum in [j, size-1]
            i           =j;
            minId       =0x7fffffff;
            minIdIndex  =0;
            while (i<files.size())
            {
                file=files.get(i);
                if (file.fileId<minId)
                {
                    minId       =file.fileId;
                    minIdIndex  =i;
                }

                i++;
            }
            
            // Swap j, minIdIndex
            temp=files.get(j);
            files.set(j, files.get(minIdIndex));
            files.set(minIdIndex, temp);
            
            j++;
        }
    }
    
    /**
     * This method reports the progress file reading
     * @param bytesRead Number of bytes that have been written
     */
    @Override
    public void reportReadProgress(int bytesRead)
    {
        bytesDownloaded+=bytesRead;
        theView.setProgress((int)(1000L*bytesDownloaded/bytesToDownload));
    }

    /**
     * This method reports the progress file writing
     * @param bytesWritten Number of bytes that have been written
     */
    @Override
    public void reportWriteProgress(int bytesWritten)
    {
        
    }
    
    
    /**
     * Load the activity from a ttbin file. It results in an
     * ActivityData item appended to the lists.
     * @param fileName Name of the ttbin file.
     */
    public void loadActivityFromTtbinFile(String fileName)
    {
        RandomAccessFile    file;
        ActivityData        data;
        TomTomReader        reader;
        
        try
        {
            file                = new RandomAccessFile(fileName, "r");
            data                =new ActivityData();

            // Read the TTBIN file data
            data.file           =new UsbFile();
            data.file.fileId    =0xFFFFFFFF;
            data.file.fileData  = new byte[(int)file.length()];
            data.file.length    = (int)file.length();
            file.readFully(data.file.fileData);   
            file.close();

            // Transfer to activity
            reader=TomTomReader.getInstance();
            data.activity       =reader.readTtbinFile(data.file);
            data.ttbinSaved     =true;
            
            // Append the new data item to the list
            synchronized(this)
            {
                activities.add(data);
            }        
            theView.addListItem(data, "file : ");
            theView.selectLastListIndex();
        }
        catch (FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(theView, "Error loading file: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(theView, "Error loading file: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }

    
    /**
     * Erase all files of given file type
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    private boolean eraseFiles(WatchInterface watchInterface, WatchInterface.FileType type)
    {
        UsbFile             file;
        boolean             error;
        ArrayList<UsbFile>  files;
        Iterator<UsbFile>   it;
        
        error = false;
        if (!error)
        {
            // Enumerate all files of given type from the device
            files = watchInterface.getFileList(type);

            // If any found, download the data of each file
            if (files != null)
            {
                it = files.iterator();
                while (it.hasNext() && !error)
                {
                    file=it.next();
                    
                    // Just another check, double check
                    if (watchInterface.isFileType(file, type))  
                    {
                        error=watchInterface.deleteFile(file);
                        DebugLogger.info("ERASED "+String.format("0x%08x",file.fileId));                        
                    }
                    else
                    {
                        DebugLogger.error("Inconsistency while deleting file: incorrect file ID, file not deleted");
                    }
                }

            } 
            else
            {
                error = true;
            }
        }

        return error;
    }
    
    

    /*############################################################################################*\
     * THE THREAD COMMAND IMPLEMENTATIONS
    \*############################################################################################*/    
    /**
     * This method downloads the ttbin file list and each file from the watch
     */
    
    private boolean downloadActivityFiles(WatchInterface watchInterface)
    {
        UsbFile             file;
        boolean             error;
        ArrayList<UsbFile>  files;
        Iterator<UsbFile>   it;
        TomTomReader        reader;
        Activity            activity;
        ActivityData        data;
        TtbinFileWriter     writer;
        String              localDeviceName;
        String              fileName;
        boolean             fileSaveError;
        

        // Add progress listener, for file reading
        watchInterface.setProgressListener(this);
        
        
        theView.setStatus("Downloading... Please wait");
        reader = TomTomReader.getInstance();

        clear();

        error = false;
        if (!error)
        {
            // Enumerate all TTBIN files on the device
            files = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_TTBIN_DATA);

            // If any found, download the data of each file
            if (files != null)
            {            
                // The array list of USB files seems not to be sorted. So sort it
                sort(files);

                // Initialize the data for the progressbar
                bytesToDownload=0;
                bytesDownloaded=0;
                it = files.iterator();
                while (it.hasNext())
                {
                    file=it.next();
                    bytesToDownload+=file.length;
                }

                theView.setProgress(0);

                theView.setStatus("Downloading "+files.size()+" file... Please wait");
            

                fileSaveError   =false;
                it              = files.iterator();
                while (it.hasNext() && !error)
                {
                    file = it.next();
                    DebugLogger.info("File " + String.format("0x%08x", file.fileId) + " length " + file.length);

                    // Read the file data
                    error = watchInterface.readFile(file);
                    if (file.fileData != null)
                    {
                        // Convert the file data into an Activity
                        activity = reader.readTtbinFile(file);
                        activity.setDeviceName(this.deviceName);
                        
                        data = new ActivityData();
                        {
                            data.file       = file;
                            data.activity   = activity;
                            data.ttbinSaved = false;
                        }
                        synchronized (this)
                        {
                            activities.add(data);
                            localDeviceName = deviceName;
                        }

                        // Write the file as ttbin file to disk if required
                        if (theView.isAutoSaveTtbin() && !fileSaveError)
                        {
                            // Get the ttbin file writer
                            writer  = TtbinFileWriter.getInstance();
                            
                            // Get the full filename. Directories are created as side effect
                            fileName=writer.getFullFileName(theView.getTtbinPath(), 
                                                            localDeviceName, 
                                                            activity.getStartDateTime(), 
                                                            activity.getActivityDescription());
                            
                            if (fileName!=null)
                            {
                                // Write the ttbin file to disk
                                fileSaveError = writer.writeTtbinFile(fileName, file);
                                if (!fileSaveError)
                                {
                                    // Verify the file by reading it back
                                    fileSaveError=writer.verifyTtbinFile(fileName, file);
                                    if (!fileSaveError)
                                    {
                                        data.ttbinSaved = true;
                                    }
                                    else
                                    {
                                        JOptionPane.showMessageDialog(theView, "Error verifying TTBIN file. TTBIN file saving stopped.", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                else
                                {
                                    JOptionPane.showMessageDialog(theView, "Error saving TTBIN file. TTBIN file saving stopped.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                            else
                            {
                                JOptionPane.showMessageDialog(theView, "Error generating TTBIN filename. TTBIN file saving stopped.", "Error", JOptionPane.ERROR_MESSAGE);
                                fileSaveError=true;
                            }
                        }

                        // Add the activity info to the listbox
                        theView.addListItem(data, "watch: ");
                    }
                }
                theView.selectLastListIndex();
            } 
            else
            {
                error = true;
            }
        }
        theView.setStatus("Finished!");
        
        // remove progress listener to prevent unwanted effects
        watchInterface.setProgressListener(null);        

        return error;
    }
    
    
    /**
     * This method deletes the ttbin files from the watch
     * @return True if an error occurred, false if successful
     */
    private boolean deleteActivityFiles(WatchInterface watchInterface)
    {
        boolean                 error;
        UsbFile                 file;
        Iterator<ActivityData>  it;
        DefaultListModel        model;
        ActivityData           data;
        
        error=false;

        if (activities.size()>0)
        {
            synchronized(this)
            {
                it=activities.iterator();
                while (it.hasNext())
                {
                    data=it.next();
                    // Only delete ttbin files...
                    if (watchInterface.isFileType(data.file, WatchInterface.FileType.TTWATCH_FILE_TTBIN_DATA))
                    {
                        watchInterface.deleteFile(data.file);
                    }
                }
            }
            // Empty the list box
            clear();
            
            // Delete the activities
            this.activities.clear();
        }
        else
        {
            JOptionPane.showMessageDialog(theView, "No activities downloaded, first download activities", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        return error;
    }    
    
    /**
     * This method downloads the GPS QuickFix data from TomTom and uploads it to the
     * watch.
     * @return True if an error occurred, false if successful
     */
    private boolean uploadGpsData(WatchInterface watchInterface)
    {
        boolean         error;
        String          urlString;   
        URL             url;
        InputStream     stream;
        ByteArrayOutputStream outputStream;
        BufferedReader  in;
        String          fileString;
        String          inputLine;
        boolean         exit;
        JSONObject      jsonObject;
        byte[]          chunk;
        int             bytesRead;
        byte[]          quickFixFile;
        
        error=false;
        
        theView.setStatus("Uploading GPS Quickfix data\n");
        // Get the ConfigURL from the watch
        urlString=watchInterface.getPreference("ConfigURL").trim();

       
        DebugLogger.info("Write GPX Quickfix data: config url: "+urlString);
        theView.appendStatus("Configuration URL: "+urlString+"\n");
        
        
        if (urlString!=null)
        {
            // Read the config file from this url
            fileString=ToolBox.readStringFromUrl(urlString);

            jsonObject      =new JSONObject(fileString);
            urlString       =jsonObject.getString("service:ephemeris");


            urlString=urlString.replace("{DAYS}", "3");
            DebugLogger.info("Write GPS Quickfix data: data url: "+urlString);
            theView.appendStatus("Quickfix data URL: "+urlString+"\n");

            // Download the GPS quick fix file
            quickFixFile=ToolBox.readBytesFromUrl(urlString);

            error=watchInterface.writeGpxQuickFixFile(quickFixFile);

            if (!error)
            {
                JOptionPane.showMessageDialog(theView, "GPS Quickfix data sent", "Info", JOptionPane.PLAIN_MESSAGE);
            }
        }
        else
        {
            DebugLogger.error("Error reading preference from the Watch");
            error=true;
        }
        if (error)
        {
            JOptionPane.showMessageDialog(theView, "Error sending GPS Quickfix data sent", "Error", JOptionPane.ERROR_MESSAGE);
        }
        theView.appendStatus("Done\n");
        
        return error;
    }
    /**
     * Reads the watch preference file and displays the XML content in the
     * status pane
     * @param watchInterface USB Interface to use
     * @return True if an error occurred, false if not
     */
    private boolean getXmlPreferences(WatchInterface watchInterface)
    {
        UsbFile file;
        boolean error;
        
        error=false;
        
        file=watchInterface.readPreferences();
        
        if (file!=null)
        {
            theView.setStatus(new String(file.fileData));
        }
        else
        {
            error=true;
            DebugLogger.error("Error reading preferences");
        }
        return error;        
    }
    
    /**
     * Reads the file list and displays it in the status area
     * @param watchInterface THe interface to ue
     * @return True if an error occurred
     */
    private boolean listFiles(WatchInterface watchInterface)
    {
        ArrayList<UsbFile>  files;
        UsbFile             file;
        Iterator<UsbFile>   it;
        boolean             error;
        
        error   =false;
        files   =watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_ALL);
        
        if (files!=null)
        {
            theView.setStatus("File ID    File Size   \n"+
                              "__________ ____________\n");
            it=files.iterator();
            while (it.hasNext())
            {
                file=it.next();
                theView.appendStatus(String.format("0x%08x %8d\n", file.fileId, file.length));
            }
        }
        else
        {
            error=true;
        }
        
        return error;
    }
    
    /**
     * This method gets the device name from the watch and displays
     * it on the screen
     * @param watchInterface The USB interface to use
     * @return True if an error occurred, false if all went well
     */
    private boolean getDeviceName(WatchInterface watchInterface)
    {
        boolean error;
       
        error=false;

        synchronized(this)
        {
            deviceName=watchInterface.getPreference("watchName");
            
            if (deviceName!=null)
            {
                theView.setDeviceName(deviceName);
            }
            else
            {
                error=true;
            }
        }        
        return error;
    }
    
    
    /**
     * Sets the name of the TomTom Watch. Writes the name to the preference
     * file on the device
     * @param watchInterface USB Interface 
     * @return True if an error occurred, false if all went ok
     */
    private boolean setDeviceName(WatchInterface watchInterface)
    {
        boolean error;
        String  name;
        
        synchronized(this)
        {
            name=this.newDeviceName;
        }
        
        error=watchInterface.setPreference("watchName", name);
        
        if (!error)
        {
            
            synchronized(this)
            {
                commandQueue.addLast(ThreadCommand.THREADCOMMAND_GETNAME);
            }
            
        }
        
        return error;
    }
    
    /**
     * Reads a file from the watch and saves it to disk
     * @param watchInterface The USB interface for reading from the watch
     * @return True if an error occurred
     */
    private boolean saveDeviceFile(WatchInterface watchInterface)
    {
        ArrayList<UsbFile>  watchFiles;
        Iterator<UsbFile>   it;
        RandomAccessFile    diskFile;
        UsbFile             usbFile;
        int                 fileId;
        boolean             error;
        boolean             found;
        String              fileName;
        String              path;
        
        error=false;
        
        synchronized(this)
        {
           fileId   =this.fileIdToWrite;
           path     =this.debugFilePath;
        }
        
        
        // Get a list of all files
        watchFiles = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_ALL);
        
        // Check if the reading of the file list succeeded
        if (watchFiles!=null)
        {
            // Now check if the requested ID is present on the watch
            found       =false;
            it          =watchFiles.iterator();
            while (it.hasNext() && !found)
            {
                usbFile=it.next();
                if (usbFile.fileId==fileId)
                {
                    found=true;
                }
            }
            if (found)
            {
                usbFile        =new UsbFile();
                usbFile.fileId =fileId;
                error=watchInterface.readFile(usbFile);

                if (!error)
                {
/*                    
                    String f;
                    f="";
                    int i=0;
                    while (i<usbFile.length)
                    {
                        f+=String.format("%02x ", usbFile.fileData[i]);
                        i++;
                    }
                    theView.setStatus(f);
*/        
                    if (!path.endsWith("/") && !path.endsWith("\\"))
                    {
                        path+="/";
                    }
                    fileName=String.format("%s0x%08x.bin", path, fileId);
                    
                    if (!ToolBox.writeBytesToFile(fileName, usbFile.fileData))
                    {
                        theView.setStatus("File written to: "+fileName);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(theView, "Error writing file "+fileName);
                    }
                }        
            }
            else
            {
                JOptionPane.showMessageDialog(theView, String.format("File with ID 0x%08x does not exist on watch", fileId), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            error=true;
        }

        return error;
    }
    

    /**
     * Deletes the file indicated by the field fileIdToDelete.
     * @param watchInterface The USB interface for accessing from the watch
     * @return True if an error occurred
     */
    private boolean deleteDeviceFile(WatchInterface watchInterface)
    {
        ArrayList<UsbFile>  watchFiles;
        Iterator<UsbFile>   it;
        UsbFile             usbFile;
        int                 fileId;
        boolean             error;
        boolean             found;

        
        error=false;
        
        synchronized(this)
        {
           fileId   =this.fileIdToDelete;
        }
        
        
        // Get a list of all files
        watchFiles = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_ALL);
        
        // Check if the reading of the file list succeeded
        if ((watchFiles!=null) && (fileId!=WatchInterface.FILEID_INVALID))
        {
            // Now check if the requested ID is present on the watch
            found       =false;
            it          =watchFiles.iterator();
            while (it.hasNext() && !found)
            {
                usbFile=it.next();
                if (usbFile.fileId==fileId)
                {
                    found=true;
                }
            }
            if (found)
            {
                usbFile        =new UsbFile();
                usbFile.fileId =fileId;
                error=watchInterface.deleteFile(usbFile);

                if (!error)
                {
                    theView.setStatus(String.format("File 0x%08x deleted!", fileId));
                }        
            }
            else
            {
                JOptionPane.showMessageDialog(theView, String.format("File with ID 0x%08x does not exist on watch", fileId), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            error=true;
        }

        return error;
    }
    

    /**
     * Download and list the activity history
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    private boolean listHistorySummary(WatchInterface watchInterface)
    {
        UsbFile             file;
 //       String              fileString;
        boolean             error;
        ArrayList<UsbFile>  files;
        Iterator<UsbFile>   it;
        HistorySummary        entry;
        String              description;
        
        // Add progress listener, for file reading
        watchInterface.setProgressListener(this);

        theView.setStatus("Downloading history... Please wait");
        
        error = false;
        if (!error)
        {
            // Enumerate all TTBIN files on the device
            files = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_HISTORY_SUMMARY);

            // If any found, download the data of each file
            if (files != null)
            {
                // The array list of USB files seems not to be sorted. So sort it
                sort(files);

                // Initialize the data for the progressbar
                bytesToDownload=0;
                bytesDownloaded=0;
                it = files.iterator();
                while (it.hasNext())
                {
                    file=it.next();
                    bytesToDownload+=file.length;
                }

                theView.setProgress(0);

                theView.setStatus("Downloading "+files.size()+" files... Please wait");
            
                description     ="";
                it              = files.iterator();
                while (it.hasNext() && !error)
                {
                    file = it.next();
                    DebugLogger.info("File " + String.format("0x%08x", file.fileId) + " length " + file.length);

                    // Read the file data
                    error = watchInterface.readFile(file);
                    if (file.fileData != null)
                    {
                        entry=new HistorySummary(file);
                        
                        description+=entry.getDescription()+"\n";

                    }
                    else
                    {
                        error=true;
                    }
                }
                if (!error)
                {
                    theView.setStatus(description);
                }
            } 
            else
            {
                error = true;
            }
        }

        watchInterface.setProgressListener(null);
        
        return error;
    }
    
    
    /**
     * This method lists the device history
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    public boolean listHistory(WatchInterface watchInterface)
    {
        UsbFile             file;
 //       String              fileString;
        boolean             error;
        ArrayList<UsbFile>  files;
        Iterator<UsbFile>   it;
        HistorySummary      entry;
        String              description;
        History             history;
        
        
        history=new History();
        
        // Add progress listener, for file reading
        watchInterface.setProgressListener(this);

        theView.setStatus("Downloading history... Please wait");
        
        error = false;
        if (!error)
        {
            // Enumerate all history files
            files = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_HISTORY_DATA);

            // If any found, download the data of each file
            if (files != null)
            {
                // The array list of USB files seems not to be sorted. So sort it
                sort(files);

                // Initialize the data for the progressbar
                bytesToDownload=0;
                bytesDownloaded=0;
                it = files.iterator();
                while (it.hasNext())
                {
                    file=it.next();
                    bytesToDownload+=file.length;
                }

                theView.setProgress(0);

                theView.setStatus("Downloading "+files.size()+" files... Please wait");
            
                description     ="";
                it              = files.iterator();
                while (it.hasNext() && !error)
                {
                    file = it.next();
                    DebugLogger.info("File " + String.format("0x%08x", file.fileId) + " length " + file.length);

                    // Read the file data
                    error = watchInterface.readFile(file);
                    if (file.fileData != null)
                    {
                        history.addHistoryItemFromFile(file);
                    }
                    else
                    {
                        error=true;
                    }
                }
                if (!error)
                {
                    theView.setStatus(history.getDescription());
                }
            } 
            else
            {
                error = true;
            }
        }

        watchInterface.setProgressListener(null);
        
        return error;        
    }
    
    
    /**
     * Clear the activity and history data
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    private boolean clearData(WatchInterface watchInterface)
    {
        UsbFile             file;
 //       String              fileString;
        boolean             error;
        ArrayList<UsbFile>  files;
        Iterator<UsbFile>   it;
        HistorySummary      summary;
        
        theView.setStatus("Erasing data:\n");
        error = false;
        if (!error)
        {
            theView.appendStatus("Erasing activities\n");
            error=this.eraseFiles(watchInterface, WatchInterface.FileType.TTWATCH_FILE_TTBIN_DATA);
        }
        if (!error)
        {
            theView.appendStatus("Erasing activity history\n");
            error=this.eraseFiles(watchInterface, WatchInterface.FileType.TTWATCH_FILE_HISTORY_DATA);
        }
        if (!error)
        {
            theView.appendStatus("Erasing race history\n");
            error=this.eraseFiles(watchInterface, WatchInterface.FileType.TTWATCH_FILE_RACE_HISTORY_DATA);
        }
        if (!error)
        {
            theView.appendStatus("Erasing routes\n");
            error=this.eraseFiles(watchInterface, WatchInterface.FileType.TTWATCH_FILE_TRACKPLANNING);
        }

        theView.appendStatus("Clearing recent activities list...\n");
        if (!error)
        {
            // Enumerate all files from the device
            files = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_HISTORY_SUMMARY);

            // If any found, download the data of each file
            if (files != null)
            {
                it = files.iterator();
                while (it.hasNext() && !error)
                {
                    file=it.next();
                    

                    // The summary contains the last (up to) 10 summary records for the given activity
                    // Read the summary file, clear the enties and write it back.
                    if (watchInterface.isFileType(file, WatchInterface.FileType.TTWATCH_FILE_HISTORY_SUMMARY))
                    {
                        error=watchInterface.readFile(file);
                
                        if (!error)
                        {
                            summary =new HistorySummary(file);
                            summary.clearHistorySummary();
                            file    =summary.getHistorySummaryFile();
                            
                            error   =watchInterface.writeFile(file);
                        }
                            
                    }
                    
                }

            } 
            else
            {
                error = true;
            }
        }
        if (!error)
        {
            clear();
            theView.appendStatus("Done!\n");
        }
        else
        {
            theView.appendStatus("Error!\n");
        }
        
        return error;
    }
    
    
    /**
     * This method reads a GPX file, converts it to protobuf and uploads the file
     * to the watch
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    private boolean uploadRouteFile(WatchInterface watchInterface)
    {
        boolean                 error;
        String                  name;
        String                  file;
        GpxReader               reader;
        RouteTomTom             route;
        ArrayList<UsbFile>      files;
        Iterator<UsbFile>       it;
        int                     i;
        int                     fileId;
        UsbFile                 usbFile;
        boolean                 found;
        boolean                 exists;
        
        error       =false;
        usbFile     =null;
        
        theView.setStatus("Uploading GPX file to watch\n");
        if (!error)
        {
            // Enumerate all TTBIN files on the device
            files = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_TRACKPLANNING);
            if (files!=null)
            {
                // Find the first vacant file ID for storing the UsbFile
                i       =0x0000;
                found   =false;
                fileId  =0;
                while ((i<0x10000) && !found)
                {
                    // The file ID consist of the two MSB values=0x00b8 and the two LSB values=i
                    fileId=WatchInterface.FileType.TTWATCH_FILE_TRACKPLANNING.getValue()|i;
                    exists=false;
                    it=files.iterator();
                    while (it.hasNext() && !exists)
                    {
                        usbFile=it.next();
                        if (usbFile.fileId==fileId)
                        {
                            exists=true;
                        }
                    }
                    if (!exists)
                    {
                    usbFile=new UsbFile();
                    usbFile.fileId=fileId;                           
                    found=true;
                    }
                    i++;
                }
                
                // If all 0x10000 file IDs are occupied, show some error message
                if (found)
                {
                    theView.appendStatus(String.format("File ID: 0x%08x\n", fileId));
                }
                else
                {
                    error=true;
                    DebugLogger.error("Could not find vacant route file ID");
                }
            }
            else
            {
                error=true;
            }
        }

        
        // Now we've found an ID, read the GPX file and convert it to protobuf
        if (!error && usbFile!=null)
        {
            synchronized(this)
            {
                file        =this.uploadFile;
                name        =this.uploadName;
            }
            
            // Read the route
            reader=GpxReader.getInstance();

            // The log contains now the route read
            route=new RouteTomTom();

            error=reader.readRouteFromFile(file, route);
            
            
            // Just set the name
            route.setRouteName(name);
            
            // Convert it to serialized protobuf bytes. Add the bytes to the file
            usbFile.fileData=route.getTomTomRouteData();
            usbFile.length=usbFile.fileData.length;
            theView.appendStatus("File read and converted\n");

        }
        
        // The usbFile is ready for writing. Write it!
        if (!error && usbFile!=null)
        {
            error=watchInterface.writeVerifyFile(usbFile);
            if (!error)
            {
                theView.appendStatus("File written\n");
            }
            else
            {
                theView.appendStatus("Error writing file\n");
            }
        }
        
        return error;
    }

    /**
     * This method lists the route files
     * to the watch
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    private boolean listRouteFiles(WatchInterface watchInterface)
    {
        UsbFile             file;
 //       String              fileString;
        boolean             error;
        ArrayList<UsbFile>  files;
        Iterator<UsbFile>   it;
        HistorySummary      entry;
        String              description;
        RouteTomTom         route;
        
        // Add progress listener, for file reading
        watchInterface.setProgressListener(this);

        theView.setStatus("Downloading routes... Please wait");
        
        route=new RouteTomTom();
        
        error = false;
        if (!error)
        {
            // Enumerate all TTBIN files on the device
            files = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_TRACKPLANNING);

            // If any found, download the data of each file
            if (files != null)
            {
                // The array list of USB files seems not to be sorted. So sort it
                sort(files);

                // Initialize the data for the progressbar
                bytesToDownload=0;
                bytesDownloaded=0;
                it = files.iterator();
                while (it.hasNext())
                {
                    file=it.next();
                    bytesToDownload+=file.length;
                }

                theView.setProgress(0);

                theView.setStatus("Downloading "+files.size()+" files... Please wait");
            
                description     = "File ID    Name                         Distance (km) \n";
                description     +="__________ ____________________________ ______________\n";
                it              = files.iterator();
                while (it.hasNext() && !error)
                {
                    file = it.next();
                    DebugLogger.info("File " + String.format("0x%08x", file.fileId) + " length " + file.length);

                    // Read the file data
                    error = watchInterface.readFile(file);
                    if (!error && (file.fileData != null))
                    {
                        description+=String.format("0x%08x ", file.fileId);

                        error=route.loadLogFromTomTomRouteData(file.fileData);

                        
                        if (!error)
                        {
                            description+=String.format("%28s ", route.getRouteName());
                            description+=String.format("%8.1f", (route.getDistance()/1000.0))+"\n";
                        }
                        else
                        {
                            description+="error!\n";
                            // Since this is not a blocking error: reset error flag
                            error=false;
                        }
                            
                    }
                    else
                    {
                        error=true;
                    }
                }
                if (!error)
                {
                    theView.setStatus(description);
                }
            } 
            else
            {
                error = true;
            }
        }

        watchInterface.setProgressListener(null);
        
        return error;

    }
    
    /**
     * This method clears the route files from the watch
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    private boolean clearRouteFiles(WatchInterface watchInterface)
    {
        boolean error;

        theView.setStatus("Erasing routes\n");
        error=this.eraseFiles(watchInterface, WatchInterface.FileType.TTWATCH_FILE_TRACKPLANNING);
        theView.appendStatus("Done!\n");

        return error;
    }
    
    /**
     * Request the firmware version from the device
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    private boolean getFirmwareVersion(WatchInterface watchInterface)
    {
        boolean     error;
        String      version;
        String[]    parts;
        
        error=false;
        
        version=watchInterface.readFirmwareVersion();
        

        
        
        if (version!=null)
        {
            // Derive an integer reprenting the firmware version
            // 0x00HHMMLL
            parts=version.split("[.]");
            if (parts.length==3)
            {
                synchronized(this)
                {
                    this.currentFirmwareVersion=(Integer.parseInt(parts[0])<<16) |
                                                (Integer.parseInt(parts[1])<< 8) |
                                                (Integer.parseInt(parts[2])    );
                }
            }
            theView.setFirmwareVersion(version);
        }
        else
        {
            error=true;
        }
        
        return error;
    }
    
    /**
     * Request the firmware version from the device
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    private boolean getProductId(WatchInterface watchInterface)
    {
        boolean error;
        int     id;
        
        error=false;
        
        id=watchInterface.getProductId();
        
        if (id!=WatchInterface.PRODUCTID_UNKNOWN)
        {
            synchronized (this)
            {
                productId=id;
            }
            theView.setProductId(id);
        }
        else
        {
            error=true;
        }
        
        return error;
    }
    
    
    /**
     * This method updates the firmware
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    private boolean updateFirmware(WatchInterface watchInterface)
    {
        boolean     error;
        Firmware    firmware;
        int         id;
        int         firmwareVersion;
        
        error=false;

        synchronized(this)
        {
            id              =this.productId;
            firmwareVersion =this.currentFirmwareVersion;
        }
        
        firmware=Firmware.getInstance();
        
        // Check if updates are available and if so, update the firmware
        firmware.updateFirmware(watchInterface, id, firmwareVersion, theView);
        
        
        
        return error;
    }
    
    /**
     * This method save a 'simulation set'. The set consists of all files
     * on the watch and a JSON file containing the firmware versions.
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    public boolean saveSimulationSet(WatchInterface watchInterface)
    {
        UsbFile                     file;
        boolean                     error;
        boolean                     saveError;
        ArrayList<UsbFile>          files;
        Iterator<UsbFile>           it;
        String                      fileName;
        String                      path;
        UsbTestInterface.Versions   versions;
        String                      json;
                 
        synchronized(this)
        {
            path=simulationFilePath;
        }

        // Add progress listener, for file reading
        watchInterface.setProgressListener(this);

        theView.setStatus("Creating simulation set... Please wait\n");
        
        error = false;
        if (!error)
        {
            // Enumerate all history files
            files = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_ALL);

            // If any found, download the data of each file
            if (files != null)
            {
                // Initialize the data for the progressbar
                bytesToDownload=0;
                bytesDownloaded=0;
                it = files.iterator();
                while (it.hasNext())
                {
                    file=it.next();
                    bytesToDownload+=file.length;
                }

                theView.setProgress(0);

                theView.appendStatus("Downloading "+files.size()+" files... ("+bytesToDownload+" bytes) Please wait\n");

                saveError       =false;
                it              =files.iterator();
                while (it.hasNext() && !error && !saveError)
                {
                    file = it.next();

                    // Only read file when length>0; 0x00000000 with size 0 results in error....
                    if (file.length>0)
                    {
                        DebugLogger.info("File " + String.format("0x%08x", file.fileId) + " length " + file.length);

                        // Read the file data
                        error = watchInterface.readFile(file);
                        if (file.fileData != null)
                        {
                            fileName=String.format("%s0x%08x.bin", path, file.fileId);
                            saveError=ToolBox.writeBytesToFile(fileName, file.fileData);
                            if (!saveError)
                            {
                                theView.appendStatus(String.format("File 0x%08x written to %s\n", file.fileId, fileName));
                            }
                        }
                        else
                        {
                            error=true;
                        }
                    }
                }
                if (!error)
                {
                    theView.appendStatus("Done!\n");
                }
                else
                {
                    theView.appendStatus("Error reading files\n");
                }
                if (saveError)
                {
                    theView.appendStatus("Error writing file to disk\n");
                }
            } 
            else
            {
                error = true;
            }
        
            versions=new UsbTestInterface.Versions();
           
            if (!error)
            {
                versions.serialNumber=watchInterface.getDeviceSerialNumber();
                if (versions.serialNumber==null)
                {
                    error=true;
                }
            }
            if (!error)
            {
                versions.firmwareVersion=watchInterface.readFirmwareVersion();
                if (versions.firmwareVersion==null)
                {
                    error=true;
                }
            }
            if (!error)
            {
                versions.bleVersion=watchInterface.readBleVersion();
                if (versions.bleVersion==null)
                {
                    error=true;
                }
            }
            if (!error)
            {
                versions.productId=watchInterface.getProductId();
                if (versions.productId==WatchInterface.PRODUCTID_UNKNOWN)
                {
                    error=true;
                }
            }
            if (!error)
            {
                json=versions.serialize();
                ToolBox.writeStringToFile(path+"versions.json", json);
            }
        }

        watchInterface.setProgressListener(null);
        
        
        
        
        return error;
    }
    
    /**
     * This method displays the file with id 'fileIdToShow'
     * @param watchInterface The watch interface
     * @return True if an error occurred, false if successful
     */
    private boolean showFile(WatchInterface watchInterface)
    {
        boolean error;
        int     id;
        UsbFile file;
        String  log;
        
        error               =false;
        
        synchronized(this)
        {
            id              =this.fileIdToShow;
        }
        
        if (id!=WatchInterface.FILEID_INVALID)
        {
            file            =new UsbFile();
            file.fileId     =id;
            
            error           =watchInterface.readFile(file);
            
            if (!error)
            {
                log     =new String(file.fileData);
                theView.setStatus(log);
            }
            
        }
        return error;
    }
    
}
