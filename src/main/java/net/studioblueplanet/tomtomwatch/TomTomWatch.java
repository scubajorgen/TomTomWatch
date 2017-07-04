/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.ApplicationContext;


import javax.swing.ImageIcon;
import java.awt.Image;
import java.util.ArrayList;

import hirondelle.date4j.DateTime;
import java.util.TimeZone;

/**
 *
 * @author Jorgen
 */
public class TomTomWatch extends SingleFrameApplication 
{
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() 
    {
        TomTomWatchView     view;
        ArrayList<Image>    iconList;
        ImageIcon           icon;  
        ResourceMap         resourceMap;
        ApplicationContext  context;
        
 
        view=new TomTomWatchView();
        view.setVisible(true);
        
        // Set the icons...

        context     =this.getContext();
        resourceMap =context.getResourceMap();
        
        iconList=new ArrayList();

        icon=resourceMap.getImageIcon("Application.icon16");
        iconList.add(icon.getImage());
        icon=resourceMap.getImageIcon("Application.icon24");
        iconList.add(icon.getImage());
        icon=resourceMap.getImageIcon("Application.icon32");
        iconList.add(icon.getImage());
        icon=resourceMap.getImageIcon("Application.icon42");
        iconList.add(icon.getImage());


        view.setIconImages(iconList);       
    }
    
    
    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) 
    {
    }
    

    /**
     * Returns the application instance
     * @return The instance
     */
    public static TomTomWatch getApplication()
    {
        return Application.getInstance(TomTomWatch.class);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
         launch(TomTomWatch.class, args);
    }
    
    
}
