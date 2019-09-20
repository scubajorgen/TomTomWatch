/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.generics.SerialExecutor;
import net.studioblueplanet.settings.ConfigSettings;
import net.studioblueplanet.usb.WatchInterface;
import net.studioblueplanet.usb.UsbInterface;
import net.studioblueplanet.usb.UsbTestInterface;
import net.studioblueplanet.usb.UsbConnection;

/**
 *
 * @author jorgen
 */
public class DependencyInjector
{
    private static DependencyInjector theInstance=null;
    
    /**
     * Constructor
     */
    private DependencyInjector()
    {
    
    }
    
    /**
     * Return the one and only instance of this class (Singleton)
     * @return The singleton instance
     */
    public static DependencyInjector getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new DependencyInjector();
        }
        return theInstance;
    }
    
    /**
     * Do the injection
     * @param theApplication The main application 
     */
    public void inject(TomTomWatch theApplication)
    {
        ConfigSettings  settings;
        
        settings=ConfigSettings.getInstance();

        
        
        WatchInterface    watchInterface;
        if (settings.getBooleanValue("simulation"))
        {
            String simulationFilePath=settings.getStringValue("simulationPath");
            if (!simulationFilePath.endsWith("/") && !simulationFilePath.endsWith("\\"))
            {
                simulationFilePath+="/";
            }
            watchInterface  = new UsbTestInterface(simulationFilePath);
        }
        else
        {
            UsbConnection usbConnection=new UsbConnection();
            watchInterface  = new UsbInterface(usbConnection);
        }
        SerialExecutor          executor   =new SerialExecutor();
        CommunicationProcess    commProcess=new CommunicationProcess(watchInterface, executor);
        TomTomWatchView         view=new TomTomWatchView(commProcess);
        theApplication.injectView(view);
    }
}
