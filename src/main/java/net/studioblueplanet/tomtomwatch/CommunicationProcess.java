/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.usb.ProgressListener;
import net.studioblueplanet.usb.WatchInterface;
import net.studioblueplanet.usb.UsbTestInterface;
import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.TomTomReader;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.settings.ConfigSettings;
import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.generics.SerialExecutor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import hirondelle.date4j.DateTime;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.json.JSONObject;

/**
 * This class executes the commands involving communication to the device.
 * The commands are processed in a separate thread
 * @author jorgen.van.der.velde
 */
public class CommunicationProcess implements ProgressListener
{
    public static final int                     MAXROUTES=15;
    /** Parent view */
    private TomTomWatchView                     theView;
    
    private final WatchInterface                watchInterface;
    

    // Guarded data
    private final ArrayList<ActivityData>       activities;
    private final ArrayList<UsbFile>            newRouteFiles;
    private final ArrayList<UsbFile>            watchRouteFiles;
    private String                              newDeviceName;
    private int                                 fileIdToWrite;
    private String                              fileToUpload;
    private int                                 fileIdToDelete;
    private int                                 fileIdToShow;
    private String                              ttbinFileToLoad;
    private String                              deviceName;
    private String                              deviceSerial;
    private boolean                             isConnected;

    private String                              uploadGpxFile;
    private String                              uploadGpxName;
    private final String                        ttbinFilePath;
    private final String                        debugFilePath;
    private int                                 productId;
    private long                                currentFirmwareVersion;
    // End of guarded data

    // Progress listener data
    private long                                bytesToDownload;
    private long                                bytesDownloaded;
    
    private UsbFile                             preferenceFile;
    
    private final SerialExecutor                executor;
    
    private final WatchTimer                    timer;
    
    /*############################################################################################*\
     * PUBLIC METHODS
    \*############################################################################################*/    

    /**
     * Constructor. Initializes the instance 
     * @param watchInterface Interface to the watch to use
     */
    public CommunicationProcess(WatchInterface watchInterface, SerialExecutor executor)
    {
        ConfigSettings  settings;
        
        activities          =new ArrayList<>();
        newRouteFiles       =new ArrayList<>();
        watchRouteFiles     =new ArrayList<>();
        isConnected         =false;
        productId           =WatchInterface.PRODUCTID_UNKNOWN;
        preferenceFile      =null;
        
        fileIdToWrite       =WatchInterface.FILEID_INVALID;
        fileIdToDelete      =WatchInterface.FILEID_INVALID;
        
        settings            =ConfigSettings.getInstance();
        ttbinFilePath       =settings.getStringValue("ttbinFilePath");
        debugFilePath       =settings.getStringValue("debugFilePath");

        timer               =new WatchTimer(this);
        
        this.watchInterface=watchInterface;
        this.executor       =executor;
    }    

    
    /**
     * Set the view and start the processing
     * @param view TomTomWatchView instance
     */
    public void startProcess(TomTomWatchView view)
    {
        this.theView=view;
        pushCommand(ThreadCommand.THREADCOMMAND_CONNECT);
        timer.start();
    }

    /**
     * Stop the process.
     * The request is processed asynchronously by the thread.
     */
    public void requestStop()
    {
        timer.stop();
        watchInterface.closeConnection();
    }    
    
    /**
     * Push a command for executing on the command queue
     * @param command Command to push on the queue
     */
    public void pushCommand(ThreadCommand command)
    {
        Runnable r;
        
        // Execute command
        switch (command)
        {
            case THREADCOMMAND_CONNECT:
                r=() ->{connect(watchInterface);};
                break;
            case THREADCOMMAND_GETTIME:
                r=() ->{getWatchTime(watchInterface);};
                break;
            case THREADCOMMAND_GETDEVICESERIAL:
                r=() ->{getDeviceSerial(watchInterface);};
                break;
            case THREADCOMMAND_DOWNLOAD:
                r=() ->{downloadActivityFiles(watchInterface);};
                break;
            case THREADCOMMAND_DELETETTBINFILES:
                r=() ->{deleteActivityFiles(watchInterface);};
                break;
            case THREADCOMMAND_UPLOADGPSDATA:
                r=() ->{uploadGpsData(watchInterface);};
                break;
            case THREADCOMMAND_PREFERENCES:
                r=() ->{boolean error=getXmlPreferences(watchInterface);};
                break;
            case THREADCOMMAND_DELETEPREFERENCES:
                r=() ->{boolean error=deleteXmlPreferences(watchInterface);};
                break;
            case THREADCOMMAND_LISTFILES:
                r=() ->{boolean error=listFiles(watchInterface);};
                break;
            case THREADCOMMAND_GETNAME:
                r=() ->{getDeviceName(watchInterface);};
                break;
            case THREADCOMMAND_SETNAME:
                r=() ->{setDeviceName(watchInterface);};
                break;
            case THREADCOMMAND_SAVEFILE:
                r=() ->{boolean error=saveDeviceFile(watchInterface);};
                break;
            case THREADCOMMAND_UPLOADFILE:
                r=() ->{boolean error=uploadDeviceFile(watchInterface);};
                break;
            case THREADCOMMAND_DELETEFILE:
                r=() ->{boolean error=deleteDeviceFile(watchInterface);};
                break;
            case THREADCOMMAND_REBOOT:
                r=() ->{boolean error=reboot(watchInterface);};
                break;
            case THREADCOMMAND_LOADTTBINFILE:
                r=() ->{boolean error=loadActivityFromTtbinFile();};
                break;
            case THREADCOMMAND_LISTHISTORY:
                r=() ->{boolean error=listHistory(watchInterface);};
                break;
            case THREADCOMMAND_LISTHISTORYSUMMARY:
                r=() ->{boolean error=listHistorySummary(watchInterface);};
                break;
            case TRHEADCOMMAND_CLEARDATA:
                r=() ->{boolean error=clearData(watchInterface);};
                break;
            case THREADCOMMAND_UPLOADROUTE:
                r=() ->{boolean error=uploadRouteFile(watchInterface);};
                break;
            case THREADCOMMAND_UPLOADROUTES:
                r=() ->{boolean error=uploadRouteFiles(watchInterface);};
                break;
            case THREADCOMMAND_DOWNLOADROUTES:
                r=() ->{boolean error=listRouteFiles(watchInterface);};
                break;
            case THREADCOMMAND_CLEARROUTES:
                r=() ->{boolean error=this.clearRouteFiles(watchInterface);};
                break;
            case THREADCOMMAND_LISTRACES:
                r=() ->{boolean error=listRaces(watchInterface);};
                break;
            case THREADCOMMAND_GETPRODUCTID:
                r=() ->{boolean error=getProductId(watchInterface);};
                break;
            case THREADCOMMAND_GETFIRMWAREVERSION:
                r=() ->{boolean error=getFirmwareVersion(watchInterface);};
                break;
            case THREADCOMMAND_UPDATEFIRMWARE:
                r=() ->{boolean error=updateFirmware(watchInterface);};
                break;
            case THREADCOMMAND_SAVESIMULATIONSET:
                r=() ->{boolean error=saveSimulationSet(watchInterface);};
                break;
            case THREADCOMMAND_SHOWFILE:
                r=() ->{boolean error=showFile(watchInterface);};
                break;
            case THREADCOMMAND_LISTTRACKEDACTIVITY:
                r=() ->{boolean error=showTrackedActivity(watchInterface);};
                break;
            case THREADCOMMAND_DELETETRACKEDACTIVITY:
                r=() ->{boolean error=deleteTrackedActivity(watchInterface);};
                break;
            case THREADCOMMAND_SHOWWATCHSETTINGS:
                r=() ->{boolean error=showWatchSettings(watchInterface);};
                break;
            case THREADCOMMAND_SYNCTIME:
                r=() ->{boolean error=syncTime(watchInterface);};
                break;
            case THREADCOMMAND_FACTORYRESET:
                r=() ->{boolean error=factoryReset(watchInterface);};
                break;    
            default:
                r=null;
        }     
        if (r!=null)
        {
            executor.execute(r);
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
                this.pushCommand(ThreadCommand.THREADCOMMAND_SETNAME);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(theView, "Illegel Watch Name", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads the activity from a ttbin file on disk
     * @param fileName THe file to load
     */
    public void requestLoadActivityFromTtbinFile(String fileName)
    {
        synchronized(this)
        {
            this.ttbinFileToLoad=fileName;
            
            // Execute directly. Enables this function without a watch connected
            this.loadActivityFromTtbinFile();
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
     * Request the upload of a file from disk to the watch
     * @param fileName The file to upload
     */
    public void requestUploadFile(String fileName)
    {
        synchronized(this)
        {
            this.fileToUpload=fileName;
            this.pushCommand(ThreadCommand.THREADCOMMAND_UPLOADFILE);
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
            this.uploadGpxFile=file;
            this.uploadGpxName=name;
            this.pushCommand(ThreadCommand.THREADCOMMAND_UPLOADROUTE);
            this.pushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADROUTES);
        }
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
            this.pushCommand(ThreadCommand.THREADCOMMAND_SHOWFILE);
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
     * Indicates whether a watch is connected or not
     * @return True if connected, false if not
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
            reader=TomTomReader.getInstance();
            reader.setTrackSmoothing(enabled, qFactor);            
        }
    }
    
    /**
     * This method loads and adds a route file to the array with route files
     * @param name Name of the route
     * @param file File name of the route gpx file
     * @param index Location in the array to add the route file to
     */
    public void addRouteFile(String name, String file, int index)
    {
        GpxReader                   reader;
        RouteTomTom                 route;
        boolean                     error;
        UsbFile                     usbFile;

        reader=GpxReader.getInstance();
        // The log contains now the route read
        route=new RouteTomTom();
        error=reader.readRouteFromFile(file, route);
        // Just set the name
        route.setRouteName(name);

        // Convert it to serialized protobuf bytes. Add the bytes to the file
        usbFile         =new UsbFile();
        usbFile.fileData=route.getTomTomRouteData();
        usbFile.length  =usbFile.fileData.length;
        if (index>=0)
        {
            this.newRouteFiles.add(index, usbFile);
        }
        else
        {
            this.newRouteFiles.add(usbFile);
        }
        theView.appendStatus("File read and converted\n"); 
        theView.addRoutesToListBox(newRouteFiles, index);
    }

    /**
     * Remove all route files from the array
     */
    public void deleteAllRouteFiles()
    {
        this.newRouteFiles.clear();
        theView.addRoutesToListBox(newRouteFiles, -1);
    }

    /**
     * Remote selected route from the array
     * @param index Location of the route file
     */
    public void deleteRouteFile(int index)
    {
        if (index>=0 && index<newRouteFiles.size())
        {
            this.newRouteFiles.remove(index);
            theView.addRoutesToListBox(newRouteFiles, -1);
        }
        else
        {
            DebugLogger.error("Illegal route file array index while deleting route file");
            theView.appendStatus("Error while deleting route file");
        }
    }
    
    /**
     * Returns route from array at given index.
     * @param index Location of the route file in the array
     * @return The route file as UsbFile instance
     */
    public UsbFile getRouteFile(int index)
    {
        UsbFile file;
        
        file=null;
        if (index>=0 && index<newRouteFiles.size())
        {
            file=newRouteFiles.get(index);
        }
        else
        {
            DebugLogger.error("Illegal route file array index while returning route file");
            theView.appendStatus("Error while fetching route: "+index+"\n");
        }
        return file;
    }
    
    
    /*############################################################################################*\
     * HELPERS
    \*############################################################################################*/    
    /**
     * Removes any track data. Erase acitivities, clear the list
     */
    public void clear()
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
     * @return  True if an error occurred
     */
    public boolean loadActivityFromTtbinFile()
    {
        RandomAccessFile    file;
        ActivityData        data;
        TomTomReader        reader;
        String              fileName;
        boolean             error;
        
        error=false;
        
        synchronized(this)
        {
            fileName=this.ttbinFileToLoad;
        }
        
        theView.setStatus("Loading "+fileName+"\n");
        
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
            theView.addListItem(data, "file  ");
            theView.selectLastListIndex();
        }
        catch (FileNotFoundException e)
        {
            error=true;
            JOptionPane.showMessageDialog(theView, "Error loading file: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e)
        {
            error=true;
            JOptionPane.showMessageDialog(theView, "Error loading file: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        theView.appendStatus("Done!");
        
        return error;
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
                theView.appendStatus("Deleting "+files.size()+" files...\n");
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
    
    /**
     * Handle communication error
     */
    private void toErrorState()
    { 
        try
        {
            watchInterface.closeConnection();
        }
        catch(Exception e)
        {
            DebugLogger.error("Error closing watch connection: "+e.getMessage());
        }
        finally
        {
            synchronized(this)
            {
                // Forget all about the watch...
                this.productId  =WatchInterface.PRODUCTID_UNKNOWN;
                this.deviceName ="Unknown";
                isConnected=false;
            }
            clear();
            // reconnect
            pushCommand(ThreadCommand.THREADCOMMAND_CONNECT);
        }
    }    

    /*############################################################################################*\
     * THE THREAD COMMAND IMPLEMENTATIONS
    \*############################################################################################*/ 
    
    private void connect(WatchInterface watchInterface)
    {
        boolean error;
        boolean connected;
        boolean exit;
        
        error=false;
        exit =false;

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
                    this.pushCommand(ThreadCommand.THREADCOMMAND_GETDEVICESERIAL);
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
            }
        }        
    }
    
    /**
     * Get the watch time
     * @param watchInterface Watch interface to use
     */
    private void getWatchTime(WatchInterface watchInterface)
    {
        DateTime        time;

        time = watchInterface.getWatchTime();
        if (time != null)
        {
            theView.showTime(time);
        } 
        else
        {
            toErrorState();
        }
    }
    
    /**
     * Get the serial number of the device
     * @param watchInterface Watch interface to use
     */
    private void getDeviceSerial(WatchInterface watchInterface)
    {
        // Get device serial. Does not seem to work under windows :-(
        deviceSerial=watchInterface.getDeviceSerialNumber();
        if (deviceSerial!=null)
        {
            DebugLogger.info("Watch serial: "+deviceSerial);
            theView.setSerial(deviceSerial);
        }        
    }
    
    /**
     * This method downloads the ttbin file list and each file from the watch
     */
    private void downloadActivityFiles(WatchInterface watchInterface)
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
        int                 startIndex;
        int                 endIndex;
        int                 index;

        // Add progress listener, for file reading
        watchInterface.setProgressListener(this);
        
        theView.setStatus("Downloading... Please wait");
        reader = TomTomReader.getInstance();

        clear();

        error=false;

        // Enumerate all TTBIN files on the device
        files = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_TTBIN_DATA);

        // If any found, download the data of each file
        if (files != null)
        {            
            // The array list of USB files seems not to be sorted. So sort it
            sort(files);

            if (theView.isDownloadMostRecent())
            {
                startIndex  =Math.max(files.size()-3, 0);
                endIndex    =files.size();
                theView.setStatus("Downloading "+(endIndex-startIndex)+" most recent files... Please wait");
            }
            else
            {
                startIndex  =0;
                endIndex    =files.size();
                theView.setStatus("Downloading all "+(endIndex-startIndex)+" files... Please wait");
            }

            // Initialize the data for the progressbar
            bytesToDownload=0;
            bytesDownloaded=0;
            index=startIndex;
            while (index<endIndex)
            {
                file=files.get(index);
                bytesToDownload+=file.length;
                index++;
            }
            theView.setProgress(0);

            fileSaveError   =false;
            index           =endIndex-1;
            while ((index>=startIndex) && !error)
            {
                file = files.get(index);
                DebugLogger.info("File " + String.format("0x%08x", file.fileId) + " length " + file.length);

                // Read the file data
                error = watchInterface.readFile(file);
                if (file.fileData != null)
                {
                    // Convert the file data into an Activity
                    activity = reader.readTtbinFile(file);
                    activity.setDeviceName(this.deviceName);
                    activity.setDeviceSerialNumber(this.deviceSerial);

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
                        fileName=writer.getFullFileName(ttbinFilePath, 
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
                    theView.addListItem(data, "watch ");

                    if (index==endIndex-1)
                    {
                        theView.selectFirstListIndex();
                    }
                    index--;
                }
            }
        } 
        else
        {
            error = true;
        }
        if (error)
        {
            toErrorState();
        }
        
        theView.setStatus("Finished!");
        
        // remove progress listener to prevent unwanted effects
        watchInterface.setProgressListener(null);        
    }
    
    
    /**
     * This method deletes the ttbin files from the watch
     * @return True if an error occurred, false if successful
     */
    private void deleteActivityFiles(WatchInterface watchInterface)
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
                while (it.hasNext() && !error)
                {
                    data=it.next();
                    // Only delete ttbin files...
                    if (watchInterface.isFileType(data.file, WatchInterface.FileType.TTWATCH_FILE_TTBIN_DATA))
                    {
                        error=watchInterface.deleteFile(data.file);
                    }
                }
            }
            // Empty the list box
            clear();
            
            // Delete the activities
            this.activities.clear();
            
            if (error)
            {
                toErrorState();
            }
        }
        else
        {
            JOptionPane.showMessageDialog(theView, "No activities downloaded, first download activities", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }    
    
    /**
     * This method downloads the GPS QuickFix data from TomTom and uploads it to the
     * watch.
     */
    private void uploadGpsData(WatchInterface watchInterface)
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
        int             days;
        
        // Read the days to look ahead from the configuration.
        days=ConfigSettings.getInstance().getIntValue("quickFixDays");
        
        if (days!=3 && days!=7)
        {
            days=3;
        }
       
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


            urlString=urlString.replace("{DAYS}", Integer.toString(days));
            DebugLogger.info("Write GPS Quickfix data: data url: "+urlString);
            theView.appendStatus("Quickfix data URL: "+urlString+"\n");

            // Download the GPS quick fix file
            quickFixFile=ToolBox.readBytesFromUrl(urlString);

            if (quickFixFile!=null)
            {
                error=watchInterface.writeGpxQuickFixFile(quickFixFile);

                if (!error)
                {
                    JOptionPane.showMessageDialog(theView, "GPS Quickfix data sent", "Info", JOptionPane.PLAIN_MESSAGE);
                }
            }
            else
            {
                theView.appendStatus("Unable to read quickfix file from TomTom\n");
                DebugLogger.error("Unable to read quickfix file from TomTom");
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
            toErrorState();
        }
        theView.appendStatus("Done\n");
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
        
        this.preferenceFile=file;
        
        return error;        
    }

    /**
     * Delete the watch preference file. This is needed to clear the 
     * TomTom Mysports connectivity data.
     * @param watchInterface USB Interface to use
     * @return True if an error occurred, false if not
     */
    private boolean deleteXmlPreferences(WatchInterface watchInterface)
    {
        UsbFile file;
        boolean error;
        
        file=new UsbFile();
        file.fileId=WatchInterface.FILEID_PREFERENCES_XML;
        
        error=watchInterface.deleteFile(file);
        
        if (!error)
        {
            theView.setStatus("Preference file deleted.\n"); 
            theView.appendStatus("Reconnect watch to TomTomWatch to write default off-line preferences.\n");
            theView.appendStatus("Connect to TomTom Sports Connect to connect the watch to TomTom account.\n");
        
        }
        else
        {
            
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
    private void getDeviceName(WatchInterface watchInterface)
    {
        synchronized(this)
        {
            deviceName=watchInterface.getPreference("watchName");
            
            if (deviceName!=null)
            {
                theView.setDeviceName(deviceName);
            }
            else
            {
                toErrorState();
            }
        }        
    }
    
    
    /**
     * Sets the name of the TomTom Watch. Writes the name to the preference
     * file on the device
     * @param watchInterface USB Interface 
     */
    private void setDeviceName(WatchInterface watchInterface)
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
            pushCommand(ThreadCommand.THREADCOMMAND_GETNAME);
        }
        else
        {
            toErrorState();
        }
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
     * Reads a file from the watch and saves it to disk
     * @param watchInterface The USB interface for reading from the watch
     * @return True if an error occurred
     */
    private boolean uploadDeviceFile(WatchInterface watchInterface)
    {
        RandomAccessFile    diskFile;
        UsbFile             usbFile;

        int                 fileId;
        boolean             error;
        boolean             found;
        String              filePath;
        String              fileName;
        String              path;
        File                file;
        Pattern             p;
        Matcher             m;
        String              digits;

        
        error=false;

        synchronized(this)
        {
            filePath=this.fileToUpload;
        }
        
        file=new File(filePath);
        fileName=file.getName();
        
        p = Pattern.compile("^0x([0-9a-fA-F]{8})[.]bin$");
        m = p.matcher(fileName);
        
        if (m.matches())
        {
            digits          =m.group(1);
            
            usbFile         =new UsbFile();
            usbFile.fileId  =Integer.parseInt(digits, 16);
            
            try
            {
                diskFile            =new RandomAccessFile(filePath, "r");  
                usbFile.length      =(int)diskFile.length();
                usbFile.fileData    =new byte[(int)diskFile.length()];  
                diskFile.readFully(usbFile.fileData);
                
                theView.setStatus(String.format("Uploading %s to ID %08x\n", filePath, usbFile.fileId));
                error=watchInterface.writeVerifyFile(usbFile);
                
                if (!error)
                {
                    theView.appendStatus("Done!\n");
                }
                else
                {
                    theView.appendStatus("Failed!!\n");
                }
            }
            catch (FileNotFoundException e)
            {
                DebugLogger.error("File not found: "+filePath);
                theView.appendStatus("File not found\n");
            }
            catch (IOException e)
            {
                DebugLogger.error("Error reading file: "+filePath);
                theView.appendStatus("Error reading file\n");
            }
        } 
        else
        {
            theView.setStatus("The filename '"+fileName+"' does not fit the required format: 0xnnnnnnnn.bin");
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
        if (!error)
        {
            theView.appendStatus("Erasing tracked activity\n");
            error=this.eraseFiles(watchInterface, WatchInterface.FileType.TTWATCH_FILE_TRACKEDACTIVITY);
        }
        if (!error)
        {
            theView.appendStatus("Erasing tracked activity per day\n");
            error=this.eraseFiles(watchInterface, WatchInterface.FileType.TTWATCH_FILE_TRACKEDACTIVITYDAILY);
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
                file        =this.uploadGpxFile;
                name        =this.uploadGpxName;
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
     * This method uploads the new route file array to the watch. It is intelligent
     * which means route files are selectively erased and/or uploaded. Unmodified
     * files are kept in place.
     * @param watchInterface WatchInterface to use
     * @return True if an error, false if all went ok
     */
    private boolean uploadRouteFiles(WatchInterface watchInterface)
    {
        boolean             error;
        Iterator<UsbFile>   it;
        UsbFile             watchFile;
        UsbFile             newFile;
        int                 fileId;
        int                 watchFileId;
        int                 i;
        
        error   =false;
        if (newRouteFiles.size()<=MAXROUTES)
        {
            i=0;
            while (i<Math.max(newRouteFiles.size(), watchRouteFiles.size()) && !error)
            {
                if (i<newRouteFiles.size())
                {
                    // expected file ID
                    fileId      =WatchInterface.FileType.TTWATCH_FILE_TRACKPLANNING.getValue()|i;
                    newFile     =newRouteFiles.get(i);
                    
                    if (newFile.fileId!=fileId)
                    {
                        if (i<watchRouteFiles.size())
                        {
                            watchFile=watchRouteFiles.get(i);
                            error=watchInterface.deleteFile(watchFile);
                            DebugLogger.info("Erasing "+String.format("0x%8x", watchFile.fileId));
                        }
                        if (!error)
                        {
                            // The file ID consist of the two MSB values=0x00b8 and the two LSB values=i
                            newFile.fileId=fileId;
                            error=watchInterface.writeVerifyFile(newFile);
                            DebugLogger.info("Writing "+String.format("0x%8x", newFile.fileId));
                        }
                    }
                }
                else
                {
                    watchFile=watchRouteFiles.get(i);
                    error=watchInterface.deleteFile(watchFile);
                    DebugLogger.info("Erasing "+String.format("0x%8x", watchFile.fileId));
                }
                i++;
            }
            if (!error)
            {
                theView.appendStatus("Files updated to watch\n");
            }
            else
            {
                theView.appendStatus("Error updating files to watch\n");
            }
        }
        else
        {
            theView.appendStatus("To many routes to upload. Reduce to "+MAXROUTES);
        }
        theView.enableRouteButtons(true);
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
        UsbFile             watchFile;
        boolean             error;
        Iterator<UsbFile>   it;
        HistorySummary      entry;
        String              description="";
        
        // Add progress listener, for file reading
        watchInterface.setProgressListener(this);

        theView.setStatus("Downloading routes... Please wait\n");
        
        error = false;
        if (!error)
        {
            // Enumerate all TTBIN files on the device
            newRouteFiles.clear();
            newRouteFiles.addAll(watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_TRACKPLANNING));

            // If any found, download the data of each file
            if (newRouteFiles != null)
            {
                // The array list of USB files seems not to be sorted. So sort it
                sort(newRouteFiles);

                // Initialize the data for the progressbar
                bytesToDownload=0;
                bytesDownloaded=0;
                it = newRouteFiles.iterator();

                while (it.hasNext())
                {
                    file=it.next();
                    bytesToDownload+=file.length;
                }

                theView.setProgress(0);

                theView.appendStatus("Downloading "+newRouteFiles.size()+" files... Please wait\n");
            
                it              = newRouteFiles.iterator();
                watchRouteFiles.clear();
                while (it.hasNext() && !error)
                {
                    file = it.next();
                    DebugLogger.info("File " + String.format("0x%08x", file.fileId) + " length " + file.length);

                    // Read the file data
                    error = watchInterface.readFile(file);
                    if (file.fileData == null)
                    {
                        error=true;
                    }
                    
                    watchFile=new UsbFile();
                    watchFile.fileId=file.fileId;
                    watchRouteFiles.add(watchFile);
                }
                if (!error)
                {
                    theView.addRoutesToListBoxLater(newRouteFiles, 0);
                }
            } 
            else
            {
                error = true;
            }
        }
        watchInterface.setProgressListener(null);
        theView.enableRouteButtons(true);
        theView.appendStatus("Done downloading route files\n");
        return error;
    }
    

    /**
     * This method lists the races that are stored on the watch
     * @param watchInterface USB interface to use
     * @return True if an error occurred, false if successful
     */
    private boolean listRaces(WatchInterface watchInterface)
    {
        UsbFile             file;
 //       String              fileString;
        boolean             error;
        ArrayList<UsbFile>  files;
        Iterator<UsbFile>   it;
        HistorySummary      entry;
        String              description;
        Race                race;
        
        // Add progress listener, for file reading
        watchInterface.setProgressListener(this);

        theView.setStatus("Downloading races... Please wait");
        
        error = false;
        if (!error)
        {
            // Enumerate all TTBIN files on the device
            files = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_RACE_DATA);

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
            
                
                description     = "File ID    Activity       Name            Dist     Duration Checkpoints (m) \n";
                description     +="__________ ______________ _______________ ________ ________ ______________________________________\n";
                it              = files.iterator();
                while (it.hasNext() && !error)
                {
                    file = it.next();
                    DebugLogger.info("File " + String.format("0x%08x", file.fileId) + " length " + file.length);

                    // Read the file data
                    error = watchInterface.readFile(file);
                    if (!error && (file.fileData != null))
                    {
                            race=new Race(file);
                            
                            description+=race.getInfo()+"\n";
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
                    this.currentFirmwareVersion=(Long.parseLong(parts[0])<<32) |
                                                (Long.parseLong(parts[1])<<16) |
                                                (Long.parseLong(parts[2])    );
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
        long        firmwareVersion;
        
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
        String                      simulationFilePath;    
        
        synchronized(this)
        {
            simulationFilePath=ConfigSettings.getInstance().getStringValue("simulationPath");
            if (!simulationFilePath.endsWith("/") && !simulationFilePath.endsWith("\\"))
            {
                simulationFilePath+="/";
            }            
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
            if (watchInterface.fileExists(id))
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
            else
            {
                theView.setStatus("Requested file does not exist on the watch.");
            }
        }
        return error;
    }
    
    /**
     * This method displays the activity tracked by the watch. This info is 
     * stored in the 0x00b1nnnn files.
     * @param watchInterface The watch interface
     * @return True if an error occurred, false if successful
     */
    private boolean showTrackedActivity(WatchInterface watchInterface)
    {
        boolean error;
        Tracker tracker;

        theView.setStatus("Downloading tracked activity files... Please wait.");
        
        UsbFile             file;
        ArrayList<UsbFile>  files;
        Iterator<UsbFile>   it;
        
        error = false;

        tracker=new Tracker();
        
        // Enumerate all files of given type from the device
        files = watchInterface.getFileList(WatchInterface.FileType.TTWATCH_FILE_TRACKEDACTIVITY);

        // If any found, download the data of each file
        if (files != null)
        {
            this.sort(files);

            it = files.iterator();
            while (it.hasNext() && !error)
            {
                file=it.next();

                error=watchInterface.readFile(file);
                
                if (!error)
                {
                    DebugLogger.info("Appending "+String.format("0x%08x", file.fileId)+" length "+file.length);

                    // Just another check, double check
                    if (watchInterface.isFileType(file, WatchInterface.FileType.TTWATCH_FILE_TRACKEDACTIVITY))  
                    {
                        error=tracker.appendFromData(file.fileData);
                    }
                    else
                    {
                        DebugLogger.error("Inconsistency while requesting tracked activity files");
                    }
                }
                else
                {
                    DebugLogger.error("Error reading file "+String.format("0x%08x", file.fileId));
                }
            }
        
            if (!error)
            {
                tracker.convertToHourly();
                theView.setStatus(tracker.trackedActivityToString()+"\n"+
                                  tracker.heartRatesToString()+"\n"+
                                  tracker.sleepingPeriodsToString());
            }
        }
        return error;    
    }    
    
    /**
     * Delete the tracked activity files
     * @param watchInterface Watch interface to use.
     * @return True if an error occurred
     */
    private boolean deleteTrackedActivity(WatchInterface watchInterface)
    {
        boolean error;
        theView.setStatus("Erasing tracked activity\n");
        error=this.eraseFiles(watchInterface, WatchInterface.FileType.TTWATCH_FILE_TRACKEDACTIVITY);
        theView.appendStatus("Done\n");
        return error;
    }
    

    /**
     * This method reboots the watch
     * @param watchInterface The watch interface
     * @return True if an error occurred, false if successful
     */
    private boolean reboot(WatchInterface watchInterface)
    {
        boolean error;

        
        error = false;

        watchInterface.resetDevice();
        theView.setStatus("Rebooted...");

        return error;    
    }    

     /**
     * This method resets the watch to factory resets. All userdata is erased.
     * @param watchInterface The watch interface
     * @return True if an error occurred, false if successful
     */
    private boolean factoryReset(WatchInterface watchInterface)
    {
        boolean error;
        Firmware firmware;
        
        theView.setStatus("Factory reset proces started. Do not disconnect watch.\n");

        theView.appendStatus("Preparing firmware download...\n");

        firmware=Firmware.getInstance();
        
        error=firmware.prepareFirmware(watchInterface, productId);
        
        if (!error)
        {
            error=watchInterface.formatDevice();
            theView.appendStatus("Factory reset initialized.\n");
            if (!error)
            {
                error=firmware.forceUpdateFirmware(watchInterface, theView);
                theView.appendStatus("Reconnect watch to TomTomWatch to write default off-line preferences.\n");
                theView.appendStatus("Connect to TomTom Sports Connect to connect the watch to TomTom account.\n");
            }
            else
            {
                theView.appendStatus("Error formatting device.");
            }
        }
        else
        {
            theView.appendStatus("Unable to prepare download firmware update. Cannot factory reset.");
        }
        return error;    
    }    

    /**
     * This method downloads the settings from the Manifest File in the watch
     * and displays it based on the settings defintion
     * @param watchInterface The watch interface
     * @return True if an error occurred, false if successful
     */
    private boolean showWatchSettings(WatchInterface watchInterface)
    {
        boolean         error;
        UsbFile         settingsFile;
        WatchSettings   settings;
        
        theView.setStatus("Reading watch settings...");

        settingsFile=new UsbFile();
        settingsFile.fileId=WatchInterface.FILEID_MANIFEST1;
        
        error=watchInterface.readFile(settingsFile);

        if (!error)
        {
            settings=new WatchSettings(settingsFile.fileData, this.currentFirmwareVersion);
            theView.setStatus(settings.getSettingDescriptions());
        }
        
        return error;    
    }    
    
    /**
     * This method syncs the time to the local computer time
     * @param watchInterface The watch interface
     * @return True if an error occurred, false if successful
     */
    private boolean syncTime(WatchInterface watchInterface)
    {
        boolean         error;
        UsbFile         settingsFile;
        WatchSettings   settings;
        DateTime        utcTime;
        DateTime        utcWatchTime;
        DateTime        localTime;
        DateTime        localWatchTime;
        long            utcTimeSeconds;
        long            utcWatchSeconds;
        long            timeOffset;
        int             timeOffsetHours;
        int             timeOffsetMinutes;
        int             timeOffsetSeconds;
        long            newTimeOffset;
        TimeZone        utcTimeZone;
        int             response;
        
        theView.setStatus("Synchronizing time...");
        error = false;

        utcTimeZone     =TimeZone.getTimeZone("UTC");
        utcWatchTime    =watchInterface.getWatchTime();
        utcTime         =DateTime.now(utcTimeZone);

        if (utcWatchTime!=null)
        {
            utcTimeSeconds  =utcTime.getMilliseconds(utcTimeZone)/1000;
            utcWatchSeconds =utcWatchTime.getMilliseconds(utcTimeZone)/1000;

            theView.setStatus   ("Watch Time(UTC): "+utcWatchTime.format("DD-MM-YYYY hh:mm:ss.fff")+"\n");
            theView.appendStatus("PC Time   (UTC): "+utcTime.format("DD-MM-YYYY hh:mm:ss.fff")     +"\n");
            
            
            settingsFile=new UsbFile();
            settingsFile.fileId=WatchInterface.FILEID_MANIFEST1;

            error=watchInterface.readFile(settingsFile);

            if (!error)
            {
                settings    =new WatchSettings(settingsFile.fileData, this.currentFirmwareVersion);
                timeOffset  =settings.getSettingsValueInt("options/utc_offset");
                theView.appendStatus("Time offset with respect to UTC: "+timeOffset+"\n");      
                
                localTime           =utcTime.changeTimeZone(utcTimeZone, TimeZone.getDefault());
                timeOffsetHours     =(int)timeOffset/3600;
                timeOffsetMinutes   =(int)(timeOffset-timeOffsetHours*3600)/60;
                timeOffsetSeconds   =(int)timeOffset-3600*timeOffsetHours-60*timeOffsetMinutes;
                if (timeOffset>=0)
                {
                    localWatchTime=utcWatchTime.plus(0, 0, 0, timeOffsetHours, timeOffsetMinutes, timeOffsetSeconds, 0, DateTime.DayOverflow.Spillover);
                }
                else
                {
                    localWatchTime=utcWatchTime.minus(0, 0, 0, -timeOffsetHours, -timeOffsetMinutes, -timeOffsetSeconds, 0, DateTime.DayOverflow.Spillover);
                }
                
                theView.appendStatus   ("Watch Time(local): "+localWatchTime.format("DD-MM-YYYY hh:mm:ss.fff")+"\n");
                theView.appendStatus("PC Time   (local): "+localTime.format("DD-MM-YYYY hh:mm:ss.fff")     +"\n");

                if (Math.abs(utcTimeSeconds-utcWatchSeconds)>300)
                {
                    theView.appendStatus("Watch clock more than 5 minutes out of sync. Enable GPS to sync to GPS time\n");
                }
                else
                {
                    newTimeOffset=utcTime.numSecondsFrom(localTime);
                    theView.appendStatus("New time offset: "+newTimeOffset+"\n");

                    if (newTimeOffset!=timeOffset)
                    {
                        response = JOptionPane.showConfirmDialog(null, "Sync watch time offset to PC time?", "Confirm",
                                                                 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (response == JOptionPane.YES_OPTION)
                        {
                            settings.setSettingsValueInt("options/utc_offset", newTimeOffset);
                            settingsFile.fileData=settings.convertSettingsToData();
                            error=watchInterface.writeFile(settingsFile);
                            theView.appendStatus("Offset written to watch\n");
                        }
                        else
                        {
                            theView.appendStatus("No update written to watch");
                        }
                    }
                    else
                    {
                        theView.appendStatus("No need to sync the time");
                    }
                }

            }
            else
            {
                theView.appendStatus("Error syncing time!");
            }
        }
        else
        {
            error=true;
        }
        
        return error;    
    }    
}
