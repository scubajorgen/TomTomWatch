/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.util.concurrent.Executor;
import net.studioblueplanet.generics.SerialExecutor;
import net.studioblueplanet.generics.ThreadExecutor;
import net.studioblueplanet.settings.ConfigSettings;
import net.studioblueplanet.ttbin.TomTomReader;
import net.studioblueplanet.usb.WatchInterface;
import net.studioblueplanet.usb.UsbInterface;
import net.studioblueplanet.usb.UsbTestInterface;
import net.studioblueplanet.usb.UsbConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 *
 * @author jorgen
 */
@Configuration
public class BeanConfiguration
{
    @Bean
    public WatchInterface getWatchInterface()
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
        return watchInterface;
    }

    @Bean
    public Executor getExecutor()
    {
        return new SerialExecutor(new ThreadExecutor());
    }
    
    @Bean
    public TomTomReader getTomTomReader()
    {
        return TomTomReader.getInstance();
    }
    
    @Bean
    public GpxReader getGpxReader()
    {
        return GpxReader.getInstance();
    }
    
    @Bean
    @Autowired
    public CommunicationProcess getCommunicationProcess(WatchInterface watchInterface, Executor executor, TomTomReader ttbinReader, GpxReader gpxReader)
    {
        return new CommunicationProcess(watchInterface, executor, ttbinReader, gpxReader);
    }
    
    @Bean
    @Autowired
    public TomTomWatchView getTomTomWatchView(CommunicationProcess commProcess)
    {
        return new TomTomWatchView(commProcess);
    }
    
    @Bean
    public TomTomWatch getTomTomWatch()
    {
        return new TomTomWatch();
    }
}
