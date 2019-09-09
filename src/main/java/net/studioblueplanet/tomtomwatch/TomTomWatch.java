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
import java.util.Locale;

import java.io.InputStream;
import java.io.IOException;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import net.studioblueplanet.logger.DebugLogger;


/**
 *
 * @author Jorgen
 */
public class TomTomWatch extends SingleFrameApplication 
{
//    private final String FONT1="net/studioblueplanet/tomtomwatch/resources/BAUHS93.TTF";
    private final String FONT1="net/studioblueplanet/tomtomwatch/resources/Raleway-Regular.ttf";
    private final String FONT2="net/studioblueplanet/tomtomwatch/resources/VeraMono.ttf";
    
    private TomTomWatchView     view;
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() 
    {
        
        ArrayList<Image>    iconList;
        DependencyInjector  injector;
 
        // Prevent comma as decimal separator
        Locale.setDefault(new Locale("en", "US"));       

        
        injector=DependencyInjector.getInstance();
        
        injector.inject(this);
        
        // Create the application view
        view.setVisible(true);

        // Add custom application icon
        iconList=loadIcons();
        view.setIconImages(iconList);
        
        // Register new fonts and apply them to the UI
        loadFonts(); 
        view.setFont();
    }
    
    /**
     * Inject the TomTomWatchView
     * @param view The view
     */
    public void injectView(TomTomWatchView view)
    {
        this.view=view;
    }
    
    /**
     * This method loads the application icon resources
     * @return List of icons
     */
    private ArrayList<Image> loadIcons()
    {
        ArrayList<Image>    iconList;
        ImageIcon           icon;  
        ResourceMap         resourceMap;

        ApplicationContext  context;        // Set the icons...

        context     =this.getContext();
        resourceMap =context.getResourceMap();
        
        iconList=new ArrayList<>();
        
        icon=resourceMap.getImageIcon("Application.icon16");
        iconList.add(icon.getImage());
        icon=resourceMap.getImageIcon("Application.icon24");
        iconList.add(icon.getImage());
        icon=resourceMap.getImageIcon("Application.icon32");
        iconList.add(icon.getImage());
        icon=resourceMap.getImageIcon("Application.icon42");
        iconList.add(icon.getImage());

        return iconList;
    }
    
    /**
     * This method loads and registers the fonts used by the UI. Fonts are
     * incorporated as resources in the application. By using incorporated fonts
     * the application will look the same independent of the Java platform
     */
    public void loadFonts() 
    {
        InputStream mainFontIn;
        Font    font;
        boolean registered;
        GraphicsEnvironment ge;
        
        font=null;
        try 
        {
            ge          = GraphicsEnvironment.getLocalGraphicsEnvironment();

            mainFontIn  = TomTomWatch.class.getClassLoader().getResourceAsStream(FONT1);
            font        = Font.createFont(Font.TRUETYPE_FONT, mainFontIn);
            
            registered              =ge.registerFont(font);
            if (registered)
            {
                DebugLogger.info("Registered font "+FONT1+": "+font.getFontName());
            }
            mainFontIn  = TomTomWatch.class.getClassLoader().getResourceAsStream(FONT2);
            font        = Font.createFont(Font.TRUETYPE_FONT, mainFontIn);
            
            registered              =ge.registerFont(font);
            if (registered)
            {
                DebugLogger.info("Registered font "+FONT2+": "+font.getFontName());
            }
	} 
        catch (IOException | FontFormatException e) 
        {
            DebugLogger.error("Error loading font: "+e.getMessage());
            System.exit(-1);
        }
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
