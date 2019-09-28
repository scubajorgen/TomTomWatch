/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;


import net.studioblueplanet.generics.GitBuildInfo;
import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.ttbin.TomTomReader;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.TtbinFileDefinition;
import net.studioblueplanet.ttbin.GpxWriter;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.settings.ConfigSettings;

import hirondelle.date4j.DateTime;
import java.io.File;

import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.Iterator;


import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.studioblueplanet.usb.WatchInterface;
import org.jdesktop.application.ResourceMap;

/**
 * View for the TomTomWatch application. Supports Multisport,
 * Spark/Runner and Adventurer
 *
 * @author Jorgen
 */
public class TomTomWatchView extends javax.swing.JFrame
{
    private static final int                    MAXNAME=15;
    
    private final CommunicationProcess          communicationProcess;
 
    private final ConfigSettings                settings;
    private final Map                           map;
    
    private TomTomWatchAbout                    aboutBox;
    
    private float                               trackSmoothingQFactor;
   
    /**
     * Constructor. Creates new form TomTomWatchView
     * @param communicationProcess The process for communication to the watch
     */
    @SuppressWarnings("unchecked")
    public TomTomWatchView(CommunicationProcess communicationProcess)
    {
        DefaultListModel<String>    model;
        boolean                     trackSmoothing;
        
        TomTomReader                reader;
        
        // Get the application settings
        settings = ConfigSettings.getInstance();

        // Set the DebugLogger log level
        DebugLogger.setDebugLevel(settings.getStringValue("debugLevel"));

        // Initialize the widgents and components
        initComponents();
        
        // Initialize the listbox
        model = new DefaultListModel<>();
        this.jListActivities.setModel(model);
        this.jListActivities.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Initialize the listbox
        model = new DefaultListModel<>();
        this.jListRoutes.setModel(model);
        model.addElement("Press Load to download routes, Add/Delete and Save the result to the watch.");
        this.jListRoutes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set the autosave option
        this.jCheckBoxAutoSave.setSelected(settings.getBooleanValue("autoSaveTtbin"));

        // Initialize the map
        this.jPanelMap.setLayout(new BoxLayout(this.jPanelMap, BoxLayout.Y_AXIS));

        if (settings.getStringValue("mapService").equals("google"))
        {
            map = new MapGoogle(this.jPanelMap);
        }
        else if (settings.getStringValue("mapService").equals("osm"))
        {
            map = new MapOsm(this.jPanelMap);
        }
        else
        {
            map=null;
        }

        // Fixed progress bar from 0-1000 promile
        this.jProgressBar.setMinimum(0);
        this.jProgressBar.setMaximum(1000);
        this.jProgressBar.setValue(0);

        if (!settings.getBooleanValue("debuggingMenu"))
        {
            this.jMenuDebugging.setVisible(false);
        }
        
        // Get the communication process. This starts the process
        this.communicationProcess=communicationProcess;
        communicationProcess.startProcess(this);

        // Set the track smoothing to the TomTom TTBIN reader
        trackSmoothing          =settings.getBooleanValue("trackSmoothingEnabled");
        trackSmoothingQFactor   =(float)settings.getDoubleValue("trackSmoothingQFactor");
        this.jCheckBoxSmooth.setSelected(trackSmoothing);
        
        // If simulation data is used, disable the menu item to save simulation data
        if (settings.getBooleanValue("simulation"))
        {
            this.jMenuItemSaveSimSet.setEnabled(false);
        }

        this.jCheckBoxDownloadMostRecent.setSelected(!settings.getBooleanValue("downloadAll"));
    }
    
    /**
     * This method redefines the fonts on the UI. It replaces the fonts by
     * fonts incorporated in the application.
     */
    public void setFont()
    {
        Font monospace11pt;
        Font monospace14pt;
        Font proportional11pt;
        Font proportional12pt;
        Font proportional14pt;
        Font proportional14ptBold;
        
        proportional11pt=new Font("Raleway", Font.PLAIN, 11);
        proportional12pt=new Font("Raleway", Font.PLAIN, 12);
        proportional14pt=new Font("Raleway", Font.PLAIN, 14);
        proportional14ptBold=new Font("Raleway", Font.BOLD, 14);
        monospace11pt   =new Font("Bitstream Vera Sans Mono", Font.PLAIN, 11);
        monospace14pt   =new Font("Bitstream Vera Sans Mono", Font.PLAIN, 14);
        
        jLabel4.setFont(proportional14pt);
        jLabel8.setFont(proportional14pt);
        jLabel9.setFont(proportional14pt);
        jLabel10.setFont(proportional14pt);
        jLabel11.setFont(proportional14pt);
        
        jTextFieldWatch.setFont(proportional14pt);
        jTextFieldSerial.setFont(proportional14pt);
        jTextFieldProductId.setFont(proportional14pt);
        jTextFieldFirmware.setFont(proportional14pt);
        jTextFieldTime.setFont(proportional14pt);
        
        jListActivities.setFont(monospace14pt);
        
        jLabel7.setFont(proportional14ptBold);
        jButtonDownload.setFont(proportional12pt);
        jButtonErase.setFont(proportional12pt);
        jButtonUploadGps.setFont(proportional12pt);
        jButtonLoadTtbin.setFont(proportional12pt);
        jButtonSaveTtbin.setFont(proportional12pt);
        jLabel2.setFont(proportional14pt);
        jTextFieldGpxFile.setFont(proportional14pt);
        jButtonSaveGpx.setFont(proportional12pt);
        jRadioButtonRunning.setFont(proportional11pt);
        jRadioButtonCycling.setFont(proportional11pt);
        jRadioButtonHiking.setFont(proportional11pt);
        jRadioButtonSwimming.setFont(proportional11pt);
        jRadioButtonDriving.setFont(proportional11pt);
        jRadioButtonSkating.setFont(proportional11pt);
        jRadioButtonFlying.setFont(proportional11pt);
        jRadioButtonMulti.setFont(proportional11pt);
        jCheckBoxDownloadMostRecent.setFont(proportional11pt);
        jCheckBoxAutoSave.setFont(proportional11pt);
        jCheckBoxSmooth.setFont(proportional11pt);
        jProgressBar.setFont(proportional11pt);
        
        jLabel6.setFont(proportional14ptBold);
        jLabel3.setFont(proportional14pt);
        jLabel5.setFont(proportional14pt);
        jTextFieldRouteGpx.setFont(proportional14pt);
        jTextFieldRouteName.setFont(proportional14pt);
        jButtonChooseRoute.setFont(proportional12pt);
        jButtonDeleteAllRoutes.setFont(proportional12pt);
        jButtonAddRoute.setFont(proportional12pt);
        jButtonListRoutes.setFont(proportional12pt);
        
        jTextAreaStatus.setFont(monospace14pt);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaStatus = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListActivities = new javax.swing.JList();
        jPanelMap = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldWatch = new javax.swing.JTextField();
        jTextFieldTime = new javax.swing.JTextField();
        jTextFieldFirmware = new javax.swing.JTextField();
        jTextFieldProductId = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextFieldSerial = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jProgressBar = new javax.swing.JProgressBar();
        jButtonDownload = new javax.swing.JButton();
        jButtonErase = new javax.swing.JButton();
        jButtonUploadGps = new javax.swing.JButton();
        jButtonLoadTtbin = new javax.swing.JButton();
        jButtonSaveTtbin = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldGpxFile = new javax.swing.JTextField();
        jButtonSaveGpx = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jRadioButtonRunning = new javax.swing.JRadioButton();
        jRadioButtonCycling = new javax.swing.JRadioButton();
        jRadioButtonHiking = new javax.swing.JRadioButton();
        jRadioButtonSwimming = new javax.swing.JRadioButton();
        jRadioButtonSkating = new javax.swing.JRadioButton();
        jRadioButtonDriving = new javax.swing.JRadioButton();
        jRadioButtonFlying = new javax.swing.JRadioButton();
        jRadioButtonMulti = new javax.swing.JRadioButton();
        jCheckBoxDownloadMostRecent = new javax.swing.JCheckBox();
        jCheckBoxAutoSave = new javax.swing.JCheckBox();
        jCheckBoxSmooth = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldRouteGpx = new javax.swing.JTextField();
        jTextFieldRouteName = new javax.swing.JTextField();
        jButtonChooseRoute = new javax.swing.JButton();
        jButtonAddRoute = new javax.swing.JButton();
        jButtonListRoutes = new javax.swing.JButton();
        jButtonDeleteAllRoutes = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListRoutes = new javax.swing.JList();
        jButtonDeleteRoute = new javax.swing.JButton();
        jButtonSaveRoutes = new javax.swing.JButton();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemQuit = new javax.swing.JMenuItem();
        jMenuDevice = new javax.swing.JMenu();
        jMenuItemListRoutes = new javax.swing.JMenuItem();
        jMenuItemSetName = new javax.swing.JMenuItem();
        jMenuItemListHistorySummary = new javax.swing.JMenuItem();
        jMenuItemListHistory = new javax.swing.JMenuItem();
        jMenuItemEraseData = new javax.swing.JMenuItem();
        jMenuItemShowTrackedActivity = new javax.swing.JMenuItem();
        jMenuDeleteTrackedActivity = new javax.swing.JMenuItem();
        jMenuItemPlaylists = new javax.swing.JMenuItem();
        jMenuItemUpdateFirmware = new javax.swing.JMenuItem();
        jMenuItemListRaces = new javax.swing.JMenuItem();
        jMenuItemSyncTime = new javax.swing.JMenuItem();
        jMenuDebugging = new javax.swing.JMenu();
        jMenuItemListFiles = new javax.swing.JMenuItem();
        jMenuDownloadFile = new javax.swing.JMenuItem();
        jMenuItemUploadFile = new javax.swing.JMenuItem();
        jMenuItemDeleteFile = new javax.swing.JMenuItem();
        jMenuItemReboot = new javax.swing.JMenuItem();
        jMenuItemFactoryReset = new javax.swing.JMenuItem();
        jMenuItemPreferences = new javax.swing.JMenuItem();
        jMenuItemDeletePreferences = new javax.swing.JMenuItem();
        jMenuItemShowUpdateLog = new javax.swing.JMenuItem();
        jMenuItemShowLog = new javax.swing.JMenuItem();
        jMenuItemShowSettings = new javax.swing.JMenuItem();
        jMenuItemSaveSimSet = new javax.swing.JMenuItem();
        jMenuAbout = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        jMenu1.setText("jMenu1");

        jMenuItem2.setText("jMenuItem2");

        jMenu2.setText("jMenu2");

        jMenu3.setText("jMenu3");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setFocusTraversalPolicyProvider(true);
        setResizable(false);

        jTextAreaStatus.setEditable(false);
        jTextAreaStatus.setColumns(20);
        jTextAreaStatus.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 14)); // NOI18N
        jTextAreaStatus.setRows(5);
        jScrollPane1.setViewportView(jTextAreaStatus);

        jListActivities.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 14)); // NOI18N
        jListActivities.setModel(new javax.swing.AbstractListModel()
        {
            String[] strings = { "x" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListActivities.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jListActivitiesValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListActivities);

        jPanelMap.setBackground(new java.awt.Color(230, 230, 230));

        javax.swing.GroupLayout jPanelMapLayout = new javax.swing.GroupLayout(jPanelMap);
        jPanelMap.setLayout(jPanelMapLayout);
        jPanelMapLayout.setHorizontalGroup(
            jPanelMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelMapLayout.setVerticalGroup(
            jPanelMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 388, Short.MAX_VALUE)
        );

        jLabel4.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jLabel4.setText("TomTomWatch: ");

        jTextFieldWatch.setEditable(false);
        jTextFieldWatch.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N

        jTextFieldTime.setEditable(false);
        jTextFieldTime.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N

        jTextFieldFirmware.setEditable(false);
        jTextFieldFirmware.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N

        jTextFieldProductId.setEditable(false);
        jTextFieldProductId.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jLabel8.setText("Product ID: ");

        jLabel9.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jLabel9.setText("Firmware: ");

        jLabel10.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jLabel10.setText("Time:");

        jLabel11.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jLabel11.setText("Serial: ");

        jTextFieldSerial.setEditable(false);
        jTextFieldSerial.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setFont(new java.awt.Font("Lucida Sans", 1, 14)); // NOI18N
        jLabel7.setText("Tracks & GPS Quickfix");

        jProgressBar.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N

        jButtonDownload.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonDownload.setText("Download");
        jButtonDownload.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonDownloadActionPerformed(evt);
            }
        });

        jButtonErase.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonErase.setText("Erase");
        jButtonErase.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonEraseActionPerformed(evt);
            }
        });

        jButtonUploadGps.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonUploadGps.setText("GPS Quickfix");
        jButtonUploadGps.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonUploadGpsActionPerformed(evt);
            }
        });

        jButtonLoadTtbin.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonLoadTtbin.setText("Load TTBIN");
        jButtonLoadTtbin.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonLoadTtbinActionPerformed(evt);
            }
        });

        jButtonSaveTtbin.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonSaveTtbin.setText("Save TTBIN");
        jButtonSaveTtbin.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSaveTtbinActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jLabel2.setText("GPX File");

        jTextFieldGpxFile.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N

        jButtonSaveGpx.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonSaveGpx.setText("Save");
        jButtonSaveGpx.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSaveGpxActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonRunning);
        jRadioButtonRunning.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jRadioButtonRunning.setText("Running");
        jRadioButtonRunning.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonRunningActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonCycling);
        jRadioButtonCycling.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jRadioButtonCycling.setText("Cycling");
        jRadioButtonCycling.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonCyclingActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonHiking);
        jRadioButtonHiking.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jRadioButtonHiking.setText("Hiking");
        jRadioButtonHiking.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonHikingActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonSwimming);
        jRadioButtonSwimming.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jRadioButtonSwimming.setText("Swimming");
        jRadioButtonSwimming.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonSwimmingActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonSkating);
        jRadioButtonSkating.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jRadioButtonSkating.setText("Skating");
        jRadioButtonSkating.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonSkatingActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonDriving);
        jRadioButtonDriving.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jRadioButtonDriving.setText("Driving");
        jRadioButtonDriving.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonDrivingActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonFlying);
        jRadioButtonFlying.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jRadioButtonFlying.setText("Flying");
        jRadioButtonFlying.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonFlyingActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonMulti);
        jRadioButtonMulti.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jRadioButtonMulti.setText("Multi");
        jRadioButtonMulti.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonMultiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonRunning)
                    .addComponent(jRadioButtonCycling))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonSwimming)
                    .addComponent(jRadioButtonHiking))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonSkating)
                    .addComponent(jRadioButtonDriving))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonMulti, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonFlying, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jRadioButtonCycling, jRadioButtonDriving, jRadioButtonFlying, jRadioButtonHiking, jRadioButtonMulti, jRadioButtonRunning, jRadioButtonSkating, jRadioButtonSwimming});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 6, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonHiking)
                    .addComponent(jRadioButtonRunning)
                    .addComponent(jRadioButtonDriving)
                    .addComponent(jRadioButtonFlying))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonSwimming)
                    .addComponent(jRadioButtonCycling)
                    .addComponent(jRadioButtonSkating)
                    .addComponent(jRadioButtonMulti)))
        );

        jCheckBoxDownloadMostRecent.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jCheckBoxDownloadMostRecent.setText("Download most recent");

        jCheckBoxAutoSave.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jCheckBoxAutoSave.setText("Autosave ttbin");

        jCheckBoxSmooth.setFont(new java.awt.Font("Lucida Sans", 0, 11)); // NOI18N
        jCheckBoxSmooth.setText("Smooth");
        jCheckBoxSmooth.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxSmoothActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jCheckBoxDownloadMostRecent)
                                .addComponent(jCheckBoxAutoSave)
                                .addComponent(jCheckBoxSmooth)))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addGap(18, 18, 18)
                            .addComponent(jTextFieldGpxFile)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonSaveGpx, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                            .addComponent(jButtonDownload, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonErase, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonUploadGps, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonLoadTtbin, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonSaveTtbin, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonDownload, jButtonErase, jButtonLoadTtbin, jButtonSaveTtbin, jButtonUploadGps});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonDownload)
                    .addComponent(jButtonErase)
                    .addComponent(jButtonUploadGps)
                    .addComponent(jButtonLoadTtbin)
                    .addComponent(jButtonSaveTtbin))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonSaveGpx, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jTextFieldGpxFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(1, 1, 1)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jCheckBoxDownloadMostRecent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBoxAutoSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxSmooth)))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setFont(new java.awt.Font("Lucida Sans", 1, 14)); // NOI18N
        jLabel6.setText("Upload Route");

        jLabel3.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jLabel3.setText("Route GPX");

        jLabel5.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jLabel5.setText("Route name");

        jTextFieldRouteGpx.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N
        jTextFieldRouteGpx.setMaximumSize(new java.awt.Dimension(6, 20));

        jTextFieldRouteName.setColumns(30);
        jTextFieldRouteName.setFont(new java.awt.Font("Lucida Sans", 0, 14)); // NOI18N

        jButtonChooseRoute.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonChooseRoute.setText("Choose");
        jButtonChooseRoute.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonChooseRouteActionPerformed(evt);
            }
        });

        jButtonAddRoute.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonAddRoute.setText("Add");
        jButtonAddRoute.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonAddRouteActionPerformed(evt);
            }
        });

        jButtonListRoutes.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonListRoutes.setText("Load");
        jButtonListRoutes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonListRoutesActionPerformed(evt);
            }
        });

        jButtonDeleteAllRoutes.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonDeleteAllRoutes.setText("Delete all");
        jButtonDeleteAllRoutes.setEnabled(false);
        jButtonDeleteAllRoutes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonDeleteAllRoutesActionPerformed(evt);
            }
        });

        jListRoutes.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 14)); // NOI18N
        jListRoutes.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jListRoutesValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jListRoutes);

        jButtonDeleteRoute.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonDeleteRoute.setText("Delete");
        jButtonDeleteRoute.setActionCommand("jButtonDeleteRoute");
        jButtonDeleteRoute.setEnabled(false);
        jButtonDeleteRoute.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonDeleteRouteActionPerformed(evt);
            }
        });

        jButtonSaveRoutes.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jButtonSaveRoutes.setText("Save");
        jButtonSaveRoutes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSaveRoutesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(133, 133, 133))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel3))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(jTextFieldRouteName, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jButtonAddRoute, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButtonDeleteRoute, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jTextFieldRouteGpx, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButtonChooseRoute, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButtonDeleteAllRoutes, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonSaveRoutes)
                            .addComponent(jButtonListRoutes, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonAddRoute, jButtonChooseRoute, jButtonDeleteAllRoutes, jButtonDeleteRoute, jButtonListRoutes, jButtonSaveRoutes});

        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldRouteGpx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jButtonListRoutes)
                            .addComponent(jButtonChooseRoute))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jButtonDeleteAllRoutes)
                            .addComponent(jButtonAddRoute)
                            .addComponent(jButtonDeleteRoute)
                            .addComponent(jButtonSaveRoutes)
                            .addComponent(jTextFieldRouteName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                .addContainerGap())
        );

        jMenuFile.setText("File");

        jMenuItemQuit.setText("Quit");
        jMenuItemQuit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemQuitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemQuit);

        jMenuBar.add(jMenuFile);

        jMenuDevice.setText("Device");

        jMenuItemListRoutes.setText("List Routes");
        jMenuItemListRoutes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemListRoutesActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuItemListRoutes);

        jMenuItemSetName.setText("Set Device Name");
        jMenuItemSetName.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemSetNameActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuItemSetName);

        jMenuItemListHistorySummary.setText("Activity Summary");
        jMenuItemListHistorySummary.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemListHistorySummaryActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuItemListHistorySummary);

        jMenuItemListHistory.setText("Activity History");
        jMenuItemListHistory.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemListHistoryActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuItemListHistory);

        jMenuItemEraseData.setText("Erase History");
        jMenuItemEraseData.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemEraseDataActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuItemEraseData);

        jMenuItemShowTrackedActivity.setText("Show Tracked Activity");
        jMenuItemShowTrackedActivity.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemShowTrackedActivityActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuItemShowTrackedActivity);

        jMenuDeleteTrackedActivity.setText("Delete Tracked Activity");
        jMenuDeleteTrackedActivity.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuDeleteTrackedActivityActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuDeleteTrackedActivity);

        jMenuItemPlaylists.setText("Playlists");
        jMenuItemPlaylists.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemPlaylistsActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuItemPlaylists);

        jMenuItemUpdateFirmware.setText("Update Firmware");
        jMenuItemUpdateFirmware.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemUpdateFirmwareActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuItemUpdateFirmware);

        jMenuItemListRaces.setText("List Races");
        jMenuItemListRaces.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemListRacesActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuItemListRaces);

        jMenuItemSyncTime.setText("Synchronize Time");
        jMenuItemSyncTime.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemSyncTimeActionPerformed(evt);
            }
        });
        jMenuDevice.add(jMenuItemSyncTime);

        jMenuBar.add(jMenuDevice);

        jMenuDebugging.setText("Debugging");

        jMenuItemListFiles.setText("List All Files");
        jMenuItemListFiles.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemListFilesActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemListFiles);

        jMenuDownloadFile.setText("Download File");
        jMenuDownloadFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuDownloadFileActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuDownloadFile);

        jMenuItemUploadFile.setText("Upload File");
        jMenuItemUploadFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemUploadFileActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemUploadFile);

        jMenuItemDeleteFile.setText("Delete File");
        jMenuItemDeleteFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemDeleteFileActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemDeleteFile);

        jMenuItemReboot.setText("Reboot Watch");
        jMenuItemReboot.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemRebootActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemReboot);

        jMenuItemFactoryReset.setText("Factory Reset");
        jMenuItemFactoryReset.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemFactoryResetActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemFactoryReset);

        jMenuItemPreferences.setText("Preferences");
        jMenuItemPreferences.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemPreferencesActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemPreferences);

        jMenuItemDeletePreferences.setText("Delete Preferences");
        jMenuItemDeletePreferences.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemDeletePreferencesActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemDeletePreferences);

        jMenuItemShowUpdateLog.setText("Show Update Log");
        jMenuItemShowUpdateLog.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemShowUpdateLogActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemShowUpdateLog);

        jMenuItemShowLog.setText("Show Device Log");
        jMenuItemShowLog.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemShowLogActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemShowLog);

        jMenuItemShowSettings.setText("Show Settings");
        jMenuItemShowSettings.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemShowSettingsActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemShowSettings);

        jMenuItemSaveSimSet.setText("Save Simulation Set");
        jMenuItemSaveSimSet.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemSaveSimSetActionPerformed(evt);
            }
        });
        jMenuDebugging.add(jMenuItemSaveSimSet);

        jMenuBar.add(jMenuDebugging);

        jMenuAbout.setText("Help");
        jMenuAbout.setToolTipText("");
        jMenuAbout.setActionCommand("");

        jMenuItemAbout.setText("About");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuAbout.add(jMenuItemAbout);

        jMenuBar.add(jMenuAbout);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextFieldWatch, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldSerial)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldProductId, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldTime, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldFirmware))
                            .addComponent(jPanelMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldWatch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldFirmware, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jTextFieldSerial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jTextFieldProductId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                    .addComponent(jPanelMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonDownloadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonDownloadActionPerformed
    {//GEN-HEADEREND:event_jButtonDownloadActionPerformed
        boolean trackSmoothingEnabled;
        
        // Set the tracksmoothing
        trackSmoothingEnabled=this.jCheckBoxSmooth.isSelected();
        communicationProcess.setTrackSmoothing(trackSmoothingEnabled, trackSmoothingQFactor);
        
        // Do the download
        checkAndPushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADACTIVITIES);
    }//GEN-LAST:event_jButtonDownloadActionPerformed

    private void jRadioButtonRunningActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonRunningActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonRunningActionPerformed
        updateActivityInfo(true);
    }//GEN-LAST:event_jRadioButtonRunningActionPerformed

    private void jListActivitiesValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListActivitiesValueChanged
    {//GEN-HEADEREND:event_jListActivitiesValueChanged
        if (!evt.getValueIsAdjusting())
        {
            updateActivityInfo(false);
        }
    }//GEN-LAST:event_jListActivitiesValueChanged

    private void jRadioButtonCyclingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonCyclingActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonCyclingActionPerformed
        updateActivityInfo(true);
    }//GEN-LAST:event_jRadioButtonCyclingActionPerformed

    private void jRadioButtonHikingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonHikingActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonHikingActionPerformed
        updateActivityInfo(true);
    }//GEN-LAST:event_jRadioButtonHikingActionPerformed

    private void jRadioButtonSwimmingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonSwimmingActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonSwimmingActionPerformed
        updateActivityInfo(true);
    }//GEN-LAST:event_jRadioButtonSwimmingActionPerformed

    private void jRadioButtonSkatingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonSkatingActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonSkatingActionPerformed
        updateActivityInfo(true);
    }//GEN-LAST:event_jRadioButtonSkatingActionPerformed

    private void jRadioButtonDrivingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonDrivingActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonDrivingActionPerformed
        updateActivityInfo(true);
    }//GEN-LAST:event_jRadioButtonDrivingActionPerformed

    private void jRadioButtonFlyingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonFlyingActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonFlyingActionPerformed
        updateActivityInfo(true);
    }//GEN-LAST:event_jRadioButtonFlyingActionPerformed

    private void jRadioButtonMultiActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonMultiActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonMultiActionPerformed
        updateActivityInfo(true);
    }//GEN-LAST:event_jRadioButtonMultiActionPerformed

    private void jMenuItemQuitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemQuitActionPerformed
    {//GEN-HEADEREND:event_jMenuItemQuitActionPerformed
        // Signal the thread to bail out
        communicationProcess.requestStop();
        // Remove the JFrame
        this.dispose();
    }//GEN-LAST:event_jMenuItemQuitActionPerformed

    private void jButtonEraseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonEraseActionPerformed
    {//GEN-HEADEREND:event_jButtonEraseActionPerformed
        int response;
        
        if (communicationProcess.isConnected())
        {
            response = JOptionPane.showConfirmDialog(null, "Do you want to erase TTBIN files from the watch?", "Confirm",
                                                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) 
            {
                // Signal the thread to erase the files
                communicationProcess.pushCommand(ThreadCommand.THREADCOMMAND_DELETETTBINFILES);
            } 
            else 
            {

            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jButtonEraseActionPerformed

    private void jButtonUploadGpsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonUploadGpsActionPerformed
    {//GEN-HEADEREND:event_jButtonUploadGpsActionPerformed
        // Signal the thread to upload GPS data for quick GPS fix
        checkAndPushCommand(ThreadCommand.THREADCOMMAND_UPLOADGPSDATA);
    }//GEN-LAST:event_jButtonUploadGpsActionPerformed

    private void jButtonSaveGpxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSaveGpxActionPerformed
    {//GEN-HEADEREND:event_jButtonSaveGpxActionPerformed
        int                         index;
        ActivityData                data;
        Activity                    activity;
        GpxWriter                   writer;
        String                      fileName;
        String                      path;
        String                      appName;
        String                      appVersion;
        GitBuildInfo                build;

        build=GitBuildInfo.getInstance();
        appName="TomTomWatch "+build.getGitCommitDescription()+" ("+build.getBuildTime()+")";
        
        index = this.jListActivities.getSelectedIndex();
        
        data=communicationProcess.getActivityData(index);
        
        if (data!=null)
        {
            activity=data.activity;
            
            fileName=this.jTextFieldGpxFile.getText();
            path    =settings.getStringValue("gpxFilePath");
            
            fileName=this.fileChooser(fileName, path, "Save", "GPX files (*.gpx)", "gpx");

            if (fileName!=null)
            {
                writer=GpxWriter.getInstance();
                writer.writeTrackToFile(fileName, activity, appName);                
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No track selected", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jButtonSaveGpxActionPerformed

    /**
     * Saves the selected TTBIN file
     * @param evt 
     */
    @SuppressWarnings("unchecked")
    private void jButtonSaveTtbinActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSaveTtbinActionPerformed
    {//GEN-HEADEREND:event_jButtonSaveTtbinActionPerformed
        int                         index;
        ActivityData                data;
        Activity                    activity;
        UsbFile                     file;
        TtbinFileWriter             writer;
        String                      ttbinPath;
        String                      description;
        DefaultListModel<String>    model;
        String                      fileName;
        boolean                     fileSaveError;
        
        index   = this.jListActivities.getSelectedIndex();

        data=communicationProcess.getActivityData(index);
        
        if (data!=null)
        {
            activity        =data.activity;
            file            =data.file;
            writer          =TtbinFileWriter.getInstance();
            // Get the ttbin root path
            ttbinPath=settings.getStringValue("ttbinFilePath");
            
            // Create the subpath (if it does not already exist) in tomtom style:
            // <watchname>/<date>/ and generate the filename: 
            // <watchname>/<date>/<activity>_<time>.ttbin
            // TODO: handle illegal ttbinpath value, e.g. ""
            fileName        =writer.getFullFileName(ttbinPath, 
                                                    communicationProcess.getDeviceName(), 
                                                    activity.getFirstActiveRecordTime(), 
                                                    activity.getActivityDescription());
         
            if (fileName!=null)
            {
                fileName=this.fileChooser(fileName, null, "Save", "TTBIN files (*.ttbin)", "ttbin");
                if (fileName!=null)
                {
                    // Write the file
                    fileSaveError=writer.writeTtbinFile(fileName, file);
                    
                    // If succeeded, check the file on disk
                    if (!fileSaveError)
                    {
                        fileSaveError=writer.verifyTtbinFile(fileName, file);
                        
                        // If validated, update the listbox
                        if (!fileSaveError)
                        {
                            data.ttbinSaved=true;
                            // Add the activity info to the listbox
                            description = getActivityDescription(data, "watch: ");
                            model       = (DefaultListModel<String>)this.jListActivities.getModel();
                            model.set(index, description);

//                            fileSaveError=writer.writeTtbinMetadataFile(fileName, data);
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(this, "Verification of TTBIN file failed", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }                    
                }
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Error writing TTBIN file", "Warning", JOptionPane.ERROR_MESSAGE);
            }
            
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No track selected", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jButtonSaveTtbinActionPerformed

    private void jMenuItemPreferencesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemPreferencesActionPerformed
    {//GEN-HEADEREND:event_jMenuItemPreferencesActionPerformed
        checkAndPushCommand(ThreadCommand.THREADCOMMAND_PREFERENCES);
    }//GEN-LAST:event_jMenuItemPreferencesActionPerformed

    private void jMenuItemListFilesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemListFilesActionPerformed
    {//GEN-HEADEREND:event_jMenuItemListFilesActionPerformed
        checkAndPushCommand(ThreadCommand.THREADCOMMAND_LISTFILES);
    }//GEN-LAST:event_jMenuItemListFilesActionPerformed

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemAboutActionPerformed
    {//GEN-HEADEREND:event_jMenuItemAboutActionPerformed
        TomTomWatch     app;
        ResourceMap     appResourceMap;
        GitBuildInfo build;
        
        app=TomTomWatch.getApplication();
        build=GitBuildInfo.getInstance();

        appResourceMap=app.getContext().getResourceMap();

        if (aboutBox == null)
        {
            aboutBox = new TomTomWatchAbout(this, true);
            aboutBox.setLocationRelativeTo(this);

            aboutBox.setVersion(build.getGitCommitDescription()+" ("+build.getBuildTime()+")");
            aboutBox.setAuthor(appResourceMap.getString("Application.author"));
            aboutBox.setHomePage(appResourceMap.getString("Application.homepage"));
        }
        TomTomWatch.getApplication().show(aboutBox);
    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    private void jMenuItemSetNameActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemSetNameActionPerformed
    {//GEN-HEADEREND:event_jMenuItemSetNameActionPerformed
        String name;
        
        if (communicationProcess.isConnected())
        {
            name = JOptionPane.showInputDialog(this, "Give new Device Name", communicationProcess.getDeviceName());
            if (name!=null)
            {
               name=name.trim();
               if (!name.equals(""))
               {
                    communicationProcess.requestSetNewDeviceName(name);
               }
               else
               {
                    JOptionPane.showMessageDialog(this, "No name entered", "Warning", JOptionPane.WARNING_MESSAGE);
               }
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);            
        }
    }//GEN-LAST:event_jMenuItemSetNameActionPerformed

    private void jButtonLoadTtbinActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLoadTtbinActionPerformed
    {//GEN-HEADEREND:event_jButtonLoadTtbinActionPerformed
        String              fileName;
        String              ttbinPath;
        int                 listItems;
        boolean trackSmoothingEnabled;
        
        // Set the tracksmoothing
        trackSmoothingEnabled=this.jCheckBoxSmooth.isSelected();
        communicationProcess.setTrackSmoothing(trackSmoothingEnabled, trackSmoothingQFactor);        

        ttbinPath=settings.getStringValue("ttbinFilePath");
        
        fileName=this.fileChooser(ttbinPath, null, "Load", "TTBIN files (*.ttbin)", "ttbin");
        
        if (fileName!=null)
        {
            communicationProcess.requestLoadActivityFromTtbinFile(fileName);
        }
    }//GEN-LAST:event_jButtonLoadTtbinActionPerformed

    /**
     * This method handles the menu item requesting saving 
     * a watch file to disk
     */
    private void jMenuDownloadFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuDownloadFileActionPerformed
    {//GEN-HEADEREND:event_jMenuDownloadFileActionPerformed
        String  hex;
        int     fileId;
        
        if (communicationProcess.isConnected())
        {
            hex = JOptionPane.showInputDialog(this, "Give new file ID (hex)", "");
            if (hex!=null)
            {
               hex=hex.trim();
               hex=hex.toLowerCase();
               if (!hex.equals(""))
               {
                   hex=hex.replace("0x", "");
                   
                   try
                   {
                       fileId=Integer.parseInt(hex, 16);
                       communicationProcess.requestWriteDeviceFileToDisk(fileId);
                   }
                   catch (NumberFormatException e)
                   {
                       JOptionPane.showMessageDialog(this, "Cannot parse file ID", "Error", JOptionPane.ERROR_MESSAGE);
                   }
               }
               else
               {
                    JOptionPane.showMessageDialog(this, "No ID entered", "Warning", JOptionPane.WARNING_MESSAGE);
               }
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);            
        }
        
        
    }//GEN-LAST:event_jMenuDownloadFileActionPerformed

    private void jMenuItemListHistorySummaryActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemListHistorySummaryActionPerformed
    {//GEN-HEADEREND:event_jMenuItemListHistorySummaryActionPerformed
        checkAndPushCommand(ThreadCommand.THREADCOMMAND_LISTHISTORYSUMMARY);
    }//GEN-LAST:event_jMenuItemListHistorySummaryActionPerformed

    private void jButtonChooseRouteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonChooseRouteActionPerformed
    {//GEN-HEADEREND:event_jButtonChooseRouteActionPerformed
        String              fileName;
        String              path;
        GpxReader           reader;
        Route               route;
        File                file;
        String              name;
        boolean             error;
        
        fileName=this.jTextFieldRouteGpx.getText();
        path    =settings.getStringValue("routeFilePath");
        
        fileName=this.fileChooser(fileName, path, "Load", "GPX files (*.gpx)", "gpx");
        
        if (fileName!=null)
        {
            this.jTextFieldRouteGpx.setColumns(5); // prevents textfield from resizing...
            this.jTextFieldRouteGpx.setText(fileName);
            
            // Set the route name:take the filename without the extension as name
            file=new File(fileName);
            name=file.getName();
            name=name.replace(".gpx", "");
            name=name.replace(".GPX", "");
            this.jTextFieldRouteName.setText(name);

            // Read the route
            reader=GpxReader.getInstance();

            // The log contains now the route read
            route=new RouteTomTom();

            error=reader.readRouteFromFile(fileName, route);
            
            if (map!=null)
            {
                map.showTrack(route);
            }
            this.jListActivities.clearSelection();

        }
    }//GEN-LAST:event_jButtonChooseRouteActionPerformed

    private void jButtonAddRouteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddRouteActionPerformed
    {//GEN-HEADEREND:event_jButtonAddRouteActionPerformed
        String                      name;
        String                      file;
        DefaultListModel<String>    model;
        int                         index;
        
        if (communicationProcess.isConnected())
        {
            name=this.jTextFieldRouteName.getText().trim();
            file=this.jTextFieldRouteGpx.getText().trim();

            if (!name.equals(""))
            {
                if (name.length()<=MAXNAME)
                {
                    if (!file.equals(""))
                    {
                        index=this.jListRoutes.getSelectedIndex();
                        if (index>=0)
                        {
                            communicationProcess.addRouteFile(name, file, index+1);
                        }
                        else
                        {
                            communicationProcess.addRouteFile(name, file, -1);
                        }
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(this, "No gpx file entered", "Warning", JOptionPane.WARNING_MESSAGE); 
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "Name to long. Set to max "+MAXNAME+" chars", "Warning", JOptionPane.WARNING_MESSAGE); 
                }
            }
            else
            {
                JOptionPane.showMessageDialog(this, "No name entered", "Warning", JOptionPane.WARNING_MESSAGE); 
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);            
        }
    }//GEN-LAST:event_jButtonAddRouteActionPerformed

    /**
     * List routes button event handler
     * @param evt Event
     */
    private void jMenuItemListRoutesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemListRoutesActionPerformed
    {//GEN-HEADEREND:event_jMenuItemListRoutesActionPerformed
        checkAndPushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADROUTES);
    }//GEN-LAST:event_jMenuItemListRoutesActionPerformed

    /**
     * Erase route button event handler
     * @param evt Event
     */
    private void jButtonDeleteAllRoutesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonDeleteAllRoutesActionPerformed
    {//GEN-HEADEREND:event_jButtonDeleteAllRoutesActionPerformed
        communicationProcess.deleteAllRouteFiles();
        if (map!=null)
        {
            map.hideTrack();
        }
    }//GEN-LAST:event_jButtonDeleteAllRoutesActionPerformed

    /**
     * Menu item: erase all track and history data
     * @param evt Event
     */
    private void jMenuItemEraseDataActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemEraseDataActionPerformed
    {//GEN-HEADEREND:event_jMenuItemEraseDataActionPerformed
        int response;
        
        if (communicationProcess.isConnected())
        {
            response = JOptionPane.showConfirmDialog(null, "Do you want to erase all track, route and history data?", "Confirm",
                                                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) 
            {
                 communicationProcess.pushCommand(ThreadCommand.TRHEADCOMMAND_CLEARDATA);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);
        }

    }//GEN-LAST:event_jMenuItemEraseDataActionPerformed

    private void jMenuItemListHistoryActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemListHistoryActionPerformed
    {//GEN-HEADEREND:event_jMenuItemListHistoryActionPerformed
        this.checkAndPushCommand(ThreadCommand.THREADCOMMAND_LISTHISTORY);
    }//GEN-LAST:event_jMenuItemListHistoryActionPerformed

    private void jMenuItemUpdateFirmwareActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemUpdateFirmwareActionPerformed
    {//GEN-HEADEREND:event_jMenuItemUpdateFirmwareActionPerformed
        this.checkAndPushCommand(ThreadCommand.THREADCOMMAND_UPDATEFIRMWARE);
    }//GEN-LAST:event_jMenuItemUpdateFirmwareActionPerformed

    private void jMenuItemDeleteFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemDeleteFileActionPerformed
    {//GEN-HEADEREND:event_jMenuItemDeleteFileActionPerformed
        String  hex;
        int     fileId;
        
        if (communicationProcess.isConnected())
        {
            hex = JOptionPane.showInputDialog(this, "Give new file ID (hex)", "");
            if (hex!=null)
            {
               hex=hex.trim();
               hex=hex.toLowerCase();
               if (!hex.equals(""))
               {
                   hex=hex.replace("0x", "");
                   
                   try
                   {
                       fileId=Integer.parseInt(hex, 16);
                       communicationProcess.requestDeleteDeviceFileFromWatch(fileId);
                   }
                   catch (NumberFormatException e)
                   {
                       JOptionPane.showMessageDialog(this, "Cannot parse file ID", "Error", JOptionPane.ERROR_MESSAGE);
                   }
               }
               else
               {
                    JOptionPane.showMessageDialog(this, "No ID entered", "Warning", JOptionPane.WARNING_MESSAGE);
               }
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);            
        }
        
    }//GEN-LAST:event_jMenuItemDeleteFileActionPerformed

    private void jMenuItemSaveSimSetActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemSaveSimSetActionPerformed
    {//GEN-HEADEREND:event_jMenuItemSaveSimSetActionPerformed
        this.checkAndPushCommand(ThreadCommand.THREADCOMMAND_SAVESIMULATIONSET);
    }//GEN-LAST:event_jMenuItemSaveSimSetActionPerformed

    private void jMenuItemShowUpdateLogActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemShowUpdateLogActionPerformed
    {//GEN-HEADEREND:event_jMenuItemShowUpdateLogActionPerformed
        communicationProcess.requestShowFile(WatchInterface.FILEID_UPDATE_LOG);
    }//GEN-LAST:event_jMenuItemShowUpdateLogActionPerformed

    private void jMenuItemShowLogActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemShowLogActionPerformed
    {//GEN-HEADEREND:event_jMenuItemShowLogActionPerformed
        communicationProcess.requestShowFile(WatchInterface.FILEID_LOG);
    }//GEN-LAST:event_jMenuItemShowLogActionPerformed

    private void jMenuItemPlaylistsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemPlaylistsActionPerformed
    {//GEN-HEADEREND:event_jMenuItemPlaylistsActionPerformed
        communicationProcess.requestShowFile(WatchInterface.FILEID_PLAYLIST);
    }//GEN-LAST:event_jMenuItemPlaylistsActionPerformed

    private void jMenuItemListRacesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemListRacesActionPerformed
    {//GEN-HEADEREND:event_jMenuItemListRacesActionPerformed
        this.checkAndPushCommand(ThreadCommand.THREADCOMMAND_LISTRACES);
    }//GEN-LAST:event_jMenuItemListRacesActionPerformed

    private void jMenuItemShowTrackedActivityActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemShowTrackedActivityActionPerformed
    {//GEN-HEADEREND:event_jMenuItemShowTrackedActivityActionPerformed
        this.checkAndPushCommand(ThreadCommand.THREADCOMMAND_LISTTRACKEDACTIVITY);
    }//GEN-LAST:event_jMenuItemShowTrackedActivityActionPerformed

    private void jMenuDeleteTrackedActivityActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuDeleteTrackedActivityActionPerformed
    {//GEN-HEADEREND:event_jMenuDeleteTrackedActivityActionPerformed
        int response;
        
        if (communicationProcess.isConnected())
        {
            response = JOptionPane.showConfirmDialog(null, "Do you want to erase the tracked activity from the watch?", "Confirm",
                                                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) 
            {
                // Signal the thread to erase the files
                communicationProcess.pushCommand(ThreadCommand.THREADCOMMAND_DELETETRACKEDACTIVITY);
            } 
            else 
            {

            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jMenuDeleteTrackedActivityActionPerformed

    private void jMenuItemUploadFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemUploadFileActionPerformed
    {//GEN-HEADEREND:event_jMenuItemUploadFileActionPerformed
        String fileName;
        
        if (communicationProcess.isConnected())
        {
            fileName="";

            fileName=this.fileChooser(fileName, null, "Upload", "bin (*.bin)", "bin");
            if (fileName!=null)
            {
                communicationProcess.requestUploadFile(fileName);
            }
        }        
    }//GEN-LAST:event_jMenuItemUploadFileActionPerformed

    private void jMenuItemRebootActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemRebootActionPerformed
    {//GEN-HEADEREND:event_jMenuItemRebootActionPerformed
        int response;
        
        if (communicationProcess.isConnected())
        {
            response = JOptionPane.showConfirmDialog(null, "Do you want to reboot the watch?", "Confirm",
                                                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) 
            {
                // Signal the thread to erase the files
                communicationProcess.pushCommand(ThreadCommand.THREADCOMMAND_REBOOT);
            } 
            else 
            {

            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemRebootActionPerformed

    private void jMenuItemShowSettingsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemShowSettingsActionPerformed
    {//GEN-HEADEREND:event_jMenuItemShowSettingsActionPerformed
        this.checkAndPushCommand(ThreadCommand.THREADCOMMAND_SHOWWATCHSETTINGS);
    }//GEN-LAST:event_jMenuItemShowSettingsActionPerformed

    private void jMenuItemSyncTimeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemSyncTimeActionPerformed
    {//GEN-HEADEREND:event_jMenuItemSyncTimeActionPerformed
        this.checkAndPushCommand(ThreadCommand.THREADCOMMAND_SYNCTIME);
    }//GEN-LAST:event_jMenuItemSyncTimeActionPerformed

    private void jMenuItemFactoryResetActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemFactoryResetActionPerformed
    {//GEN-HEADEREND:event_jMenuItemFactoryResetActionPerformed
        int response;
        
        if (communicationProcess.isConnected())
        {
            response = JOptionPane.showConfirmDialog(null, "Do you want to factory reset the watch? All data will be lost", "Confirm",
                                                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) 
            {
                // Signal the thread to reset the watch
                communicationProcess.pushCommand(ThreadCommand.THREADCOMMAND_FACTORYRESET);
            } 
            else 
            {

            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemFactoryResetActionPerformed

    private void jMenuItemDeletePreferencesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemDeletePreferencesActionPerformed
    {//GEN-HEADEREND:event_jMenuItemDeletePreferencesActionPerformed
        int response;
        
        if (communicationProcess.isConnected())
        {
            response = JOptionPane.showConfirmDialog(null, "Do you want to delete the preference file from the watch? Connectivity data will be lost", "Confirm",
                                                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) 
            {
                // Signal the thread to reset the watch
                communicationProcess.pushCommand(ThreadCommand.THREADCOMMAND_DELETEPREFERENCES);
            } 
            else 
            {

            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemDeletePreferencesActionPerformed

    private void jButtonListRoutesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonListRoutesActionPerformed
    {//GEN-HEADEREND:event_jButtonListRoutesActionPerformed
        enableRouteButtons(false);
        checkAndPushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADROUTES);
    }//GEN-LAST:event_jButtonListRoutesActionPerformed

    private void jCheckBoxSmoothActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxSmoothActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxSmoothActionPerformed
        // Clear the list of activities as maintained by the communication
        // process. En passant the listbox in this view is erased.
        communicationProcess.clear();
    }//GEN-LAST:event_jCheckBoxSmoothActionPerformed

    private void jButtonDeleteRouteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonDeleteRouteActionPerformed
    {//GEN-HEADEREND:event_jButtonDeleteRouteActionPerformed
        int index;
        index=this.jListRoutes.getSelectedIndex();
        if (index>=0)
        {
            communicationProcess.deleteRouteFile(index);
            if (map!=null)
            {
                map.hideTrack();
            }
        }
        else
        {
            this.appendStatus("First select route to delete\n");
        }
    }//GEN-LAST:event_jButtonDeleteRouteActionPerformed

    private void jButtonSaveRoutesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSaveRoutesActionPerformed
    {//GEN-HEADEREND:event_jButtonSaveRoutesActionPerformed

        int response;
        
        if (communicationProcess.isConnected())
        {
            response = JOptionPane.showConfirmDialog(null, "This will upload the routes to the watch. Existing routes will be erased.", "Confirm",
                                                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) 
            {
                enableRouteButtons(false);
                // Signal the thread to erase and upload the files
                communicationProcess.pushCommand(ThreadCommand.THREADCOMMAND_UPLOADROUTES);
                communicationProcess.pushCommand(ThreadCommand.THREADCOMMAND_DOWNLOADROUTES);
            } 
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);
        }

    }//GEN-LAST:event_jButtonSaveRoutesActionPerformed

    private void jListRoutesValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListRoutesValueChanged
    {//GEN-HEADEREND:event_jListRoutesValueChanged
        int         index;
        RouteTomTom route;
        UsbFile     file;
        if (!evt.getValueIsAdjusting())
        {
            index=this.jListRoutes.getSelectedIndex();
            if (index>=0)
            {
                file=communicationProcess.getRouteFile(index);

                if (file!=null)
                {
                    route=new RouteTomTom();
                    route.loadLogFromTomTomRouteData(file.fileData);
                    if (map!=null)
                    {
                        map.showTrack(route);
                    }
                }
                else
                {
                    if (map!=null)
                    {
                        map.hideTrack();
                    }                
                }
            }
        }
    }//GEN-LAST:event_jListRoutesValueChanged

    /*############################################################################################*\
     * HELPER FUNCTIONS     
    \*############################################################################################*/    

    /**
     * This method pushes a command on the command stack. First however a check 
     * is made whether a watch is connected
     * @param command The command to push.
     */
    private void checkAndPushCommand(ThreadCommand command)
    {
        if (communicationProcess.isConnected())
        {
            communicationProcess.pushCommand(command);
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No watch connected", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        
    }
    
    /**
     * Returns the activity description to be shown in the list box. Format is:
     * [v] - [start datetime] [id] - [type] [activity]
     * [v]              - 'v': File has been saved, 'x': file has not been saved
     * [start datetime] - Start of the activity
     * [id]             - 0x99999999, file ID
     * [type]           - 'file' for loaded from file, 'watch' for downloaded from watch
     * [activity]       - the activity: 'run', 'swim' etc
     * @param data Data defining the activity
     * @param prefix Prefix indicating the source of the activity ('file' or 'watch')
     * @return A string with the description
     */
    public String getActivityDescription(ActivityData data, String prefix)
    {
        String      description;
        String      dateTime;
        int         fitnessPoints;
        String      fitnessPointsString;

        if (data.ttbinSaved)
        {
            description = "v ";
        } else
        {
            description = "x ";
        }
        dateTime        = data.activity.getStartDateTime().format("YYYY-MM-DD hh:mm");
        fitnessPoints   =data.activity.getFitnessPoints();
        if (fitnessPoints>=0)
        {
            fitnessPointsString=Integer.toString(fitnessPoints);
        }
        else
        {
            fitnessPointsString="-";
        }
        description += dateTime
                + " - " + prefix
                + String.format("- %-13s ", data.activity.getActivityDescription())
                + String.format("- %5.1f km ", data.activity.getDistance())
                + " - " + fitnessPointsString;

        return description;
    }

    /**
     * Based on the information in the activity, set the corresponding
     * radio button
     * @param activity The activity for which the radio button is set
     */
    private void setRadioButton(Activity activity)
    {
        int activityType;

        activityType = activity.getActivityType();

        switch (activityType)
        {
            case TtbinFileDefinition.ACTIVITY_HIKING:
                this.jRadioButtonHiking.setSelected(true);
                break;
            case TtbinFileDefinition.ACTIVITY_CYCLING:
                this.jRadioButtonCycling.setSelected(true);
                break;
            case TtbinFileDefinition.ACTIVITY_RUNNING:
            case TtbinFileDefinition.ACTIVITY_TRAILRUNNING:
                this.jRadioButtonRunning.setSelected(true);
                break;
            default:
                this.jRadioButtonMulti.setSelected(true);
                break;
        }
    }

    /**
     * Generate the output file name based on the activity and selected activity
     * type (radio button)
     *
     * @param activity The activity to base the filename on
     */
    private void generateGpxFileName(Activity activity)
    {
        String      fileName;
        String      currentFileName;
        String      activityPrefix;
        String      path;
        String      file;
        String      fileTail;
        DateTime    timeStamp;

        if (this.jRadioButtonRunning.isSelected())
        {
            activityPrefix = "run";
        } else if (this.jRadioButtonCycling.isSelected())
        {
            activityPrefix = "cycle";
        } else if (this.jRadioButtonHiking.isSelected())
        {
            activityPrefix = "walk";
        } else if (this.jRadioButtonSwimming.isSelected())
        {
            activityPrefix = "swim";
        } else if (this.jRadioButtonDriving.isSelected())
        {
            activityPrefix = "drive";
        } else if (this.jRadioButtonFlying.isSelected())
        {
            activityPrefix = "flight";
        } else if (this.jRadioButtonSkating.isSelected())
        {
            activityPrefix = "skate";
        } else if (this.jRadioButtonMulti.isSelected())
        {
            activityPrefix = "multi";
        } else
        {
            activityPrefix = "unknown";
        }

        // Get the currently entered filename
        currentFileName = this.jTextFieldGpxFile.getText();

        // If the filename has not been defined yet, fill in the defaults
        if (currentFileName.equals(""))
        {

            fileName = settings.getStringValue("gpxFilePath");

            if ((!fileName.endsWith("/")) && (!fileName.endsWith("\\")))
            {
                fileName += "/";
            }
            fileName += activityPrefix + "_";

            timeStamp = activity.getStartDateTime();
            fileName += timeStamp.format("YYYYMMDD") + "_description.gpx";

            this.jTextFieldGpxFile.setText(fileName);
        } 
        else
        {
            file = currentFileName.replaceFirst("(^.*[/\\\\])?([^/\\\\]*)$", "$2");
            path = currentFileName.replaceFirst("(^.*[/\\\\])?([^/\\\\]*)$", "$1");

            fileTail = file.replaceFirst("^([a-z]*)([_]\\d{8})([_]\\S+)", "$3");

            timeStamp = activity.getStartDateTime();

            if (!fileTail.equals(file))
            {

                fileName = path + activityPrefix + "_" + timeStamp.format("YYYYMMDD") + fileTail;
                this.jTextFieldGpxFile.setText(fileName);
            }
        }
    }
    
    /**
     * Updates the GPX filename and optionally the map and radiobuttons
     * @param fileNameOnly True if only the GPX filename needs to be updated
     */
    private void updateActivityInfo(boolean fileNameOnly)
    {
        int             index;
        ActivityData    data;

        index   = this.jListActivities.getSelectedIndex();
        data    = communicationProcess.getActivityData(index);
        if (data != null)
        {
            if (!fileNameOnly)
            {
                if (map!=null)
                {
                    map.showTrack(data);                // Show the map
                }
                setRadioButton(data.activity);          // Set the appropriate radiobutton
            }
            generateGpxFileName(data.activity);         // Propose GPX file name
        }
    }

    /**
     * File Chooser functionality
     * @param initialFileName
     * @param buttonText
     * @param filterDescription
     * @param filterExtension
     * @return 
     */
    private String fileChooser(String initialFileName, String initialDirectory, String buttonText, String filterDescription, String filterExtension)
    {
        JFileChooser                fc;
        FileNameExtensionFilter     fileFilter;
        int                         returnValue;

        String                      path;
        String                      fileName;
        File                        theFile;
        
        fileName=null;
        
        fc= new JFileChooser();

        if (initialFileName.equals("") && initialDirectory!=null)
        {
            fc.setCurrentDirectory(new File(initialDirectory));
        }
        else
        {
            theFile=new File(initialFileName);
            path=theFile.getAbsolutePath();
            fc.setSelectedFile(new File(path));
        }

        fileFilter=new FileNameExtensionFilter(filterDescription, filterExtension);

        // Set file extension filters
        fc.addChoosableFileFilter(fileFilter);
        fc.setFileFilter(fileFilter);

        returnValue=fc.showDialog(null, buttonText);

        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            path=fc.getCurrentDirectory().toString();
            fileName=path+"/"+fc.getSelectedFile().getName();

            // Make sure the file has the right extension
            if(!fileName.toLowerCase().endsWith("."+filterExtension))
            {
                fileName +="."+filterExtension;
            }

        }
        if (returnValue == JFileChooser.CANCEL_OPTION)
        {
        }    
        
        return fileName;
    }
    
    /**
     * Returns whether the user has selected only to download most recent
     * activities.
     * @return True when selected
     */
    public boolean isDownloadMostRecent()
    {
        return this.jCheckBoxDownloadMostRecent.isSelected();
    }
    
    
    /* ******************************************************************************************* *\
     * PUBLIC WIDGET FUNCTIONS
    \* ******************************************************************************************* */

    /**
     * Clears existing data and the UI
     */
    public void clear()
    {
        DefaultListModel model;

        // Clear the list 
        model = (DefaultListModel) this.jListActivities.getModel();
        model.clear();

        // Hide the map
        if (map!=null)
        {
            map.hideTrack();
        }
    }
    
    /**
     * Show error in dialog box
     * @param message Error message
     */
    public void showErrorDialog(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);        
    }
    
    /**
     * Show warning in dialog box
     * @param message Warning message
     */
    public void showWarningDialog(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);        
    }
    
    /**
     * Show info in dialog box
     * @param message Info message
     */
    public void showInfoDialog(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.PLAIN_MESSAGE);        
    }
    
    /**
     * Show confirm dialog.
     * @param message Message to show
     * @return True if confirmed, false if canceled.
     */
    public boolean showConfirmDialog(String message)
    {
        int     response;
        boolean yesPressed;
        
        yesPressed=false;
        response = JOptionPane.showConfirmDialog(null, message, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION)
        {
            yesPressed=true;
        }
        return yesPressed;
    }
    
    /**
     * Clear the route list
     */
    public void clearRoutes()
    {
        DefaultListModel model;

        // Clear the list 
        model = (DefaultListModel)this.jListRoutes.getModel();
        model.clear();
        DebugLogger.info("Routes cleared");
    }
    
    /**
     * Displays the time in the time text box
     * @param time DateTime to show
     */
    public void showTime(DateTime time)
    {
        this.jTextFieldTime.setText(time.format("hh:mm:ss"));
    }

    /**
     * Set the progress bar
     * @param promile Progress in the range of 0-1000 
     */
    public void setProgress(int promile)
    {
        this.jProgressBar.setValue(promile);
    }
    
    /**
     * Set the status in the status area
     * @param status String to display
     */
    public void setStatus(String status)
    {
        this.jTextAreaStatus.setText(status);
    }
    
    /**
     * This method appends the line of status text to the status area, 
     * leaving any existing text in tact
     * @param status The line to append
     */
    public void appendStatus(String status)
    {
        this.jTextAreaStatus.append(status);
    }
    
    /**
     * Returns whether the ttbin autosave checkbox is checked
     * @return True when autosave checked, false if no
     */
    public boolean isAutoSaveTtbin()
    {
        return this.jCheckBoxAutoSave.isSelected();
    }
    /**
     * Returns the ttbin path
     * @return The TTBIN path
     */

    /**
     * Adds item to the tracklist
     * @param data The item to add
     * @param prefix Prefix indicating the source of the data: 'file' or 'watch'
     */
    @SuppressWarnings("unchecked")
    public void addListItem(ActivityData data, String prefix)
    {
        DefaultListModel    model;
        String              description;
        
        // Add the activity info to the listbox
        description = getActivityDescription(data, prefix);
        model       = (DefaultListModel) this.jListActivities.getModel();
        model.addElement(description);        
    }

    /**
     * Selects the last list item. 
     */
    public void selectFirstListIndex()
    {
        jListActivities.setSelectedIndex(0);
    }

    /**
     * Selects the last list item. 
     */
    public void selectLastListIndex()
    {
        int listItems;
        
        listItems=jListActivities.getModel().getSize();
        jListActivities.setSelectedIndex(listItems-1);
    }
    
    /**
     * Sets the device name
     * @param deviceName Name of the device 
     */
    public void setDeviceName(String deviceName)
    {
        this.jTextFieldWatch.setText(deviceName);
    }
    
    /**
     * Sets the firmware version in the appropriate text filed
     * @param version String defining the firmware
     */
    public void setFirmwareVersion(String version)
    {
        this.jTextFieldFirmware.setText(version);
    }
    
    /**
     * Sets the serial number to the textbox
     * @param serial Serial number
     */
    public void setSerial(String serial)
    {
        this.jTextFieldSerial.setText(serial);
    }
    
    public void setProductId(int id)
    {
        this.jTextFieldProductId.setText(String.format("0x%08x", id));
    }

    /**
     * This method displays the route info in the Route list box
     * @param routes Array with routes, as UsbFile
     * @param index Index to highlight
     */
    @SuppressWarnings("unchecked")
    public void addRoutesToListBox(ArrayList<UsbFile> routes, int index)
    {
        DefaultListModel<String>    model;
        Iterator<UsbFile>           it;
        UsbFile                     file;
        RouteTomTom                 route;
        String                      description;
        String                      name;
        boolean                     error;
        
        route=new RouteTomTom();
        
        model=(DefaultListModel<String>)jListRoutes.getModel();
        model.removeAllElements();
        
        it=routes.iterator();
        while (it.hasNext())
        {
            file=it.next();
            description=String.format("0x%08x ", file.fileId);

            error=route.loadLogFromTomTomRouteData(file.fileData);

            if (!error)
            {
                name=route.getRouteName();
                if (name.length()>=30)
                {
                    name=name.substring(0,29);
                }
                description+=String.format("%-30s ", name);
                description+=String.format("%5.1f km ", (route.getDistance()/1000.0));
                description+=String.format("%5d segm", route.getNumberOfSegments());
                description+=String.format("%5d pts", route.getNumberOfPoints());
            }
            else
            {
                description+="Error, file data appears corrupt!\n";
                // Since this is not a blocking error: reset error flag
                error=false;
            }
            model.addElement(description);
        }
        if (index>=0)
        {
            jListRoutes.setSelectedIndex(index);
        }
        jListRoutes.ensureIndexIsVisible(index);
        DebugLogger.info("Updated route list");
    }    
    
    /**
     * Call the update of the route list box in the EDT of Swing
     * @param routes Routes to display in the listbox
     * @param index Index to highlight
     */
    public void addRoutesToListBoxLater(ArrayList<UsbFile> routes, int index)
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            // Direct call
            addRoutesToListBox(routes, index);
        }
        else
        {
            // Invoke call in Swing EDT 
            SwingUtilities.invokeLater(() ->
            {
                addRoutesToListBox(routes, index);
            });
        }
    }
    
    /** 
     * Enable/disable the route buttons that should not be used during
     * upload or download of routes
     * @param enabled Enable/disable
     */
    public void enableRouteButtons(boolean enabled)
    {
        this.jButtonDeleteAllRoutes.setEnabled(enabled);
        this.jButtonDeleteRoute.setEnabled(enabled);
        this.jButtonAddRoute.setEnabled(enabled);
        this.jButtonSaveRoutes.setEnabled(enabled);
        this.jButtonListRoutes.setEnabled(enabled);
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonAddRoute;
    private javax.swing.JButton jButtonChooseRoute;
    private javax.swing.JButton jButtonDeleteAllRoutes;
    private javax.swing.JButton jButtonDeleteRoute;
    private javax.swing.JButton jButtonDownload;
    private javax.swing.JButton jButtonErase;
    private javax.swing.JButton jButtonListRoutes;
    private javax.swing.JButton jButtonLoadTtbin;
    private javax.swing.JButton jButtonSaveGpx;
    private javax.swing.JButton jButtonSaveRoutes;
    private javax.swing.JButton jButtonSaveTtbin;
    private javax.swing.JButton jButtonUploadGps;
    private javax.swing.JCheckBox jCheckBoxAutoSave;
    private javax.swing.JCheckBox jCheckBoxDownloadMostRecent;
    private javax.swing.JCheckBox jCheckBoxSmooth;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jListActivities;
    private javax.swing.JList jListRoutes;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenuAbout;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuDebugging;
    private javax.swing.JMenuItem jMenuDeleteTrackedActivity;
    private javax.swing.JMenu jMenuDevice;
    private javax.swing.JMenuItem jMenuDownloadFile;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemDeleteFile;
    private javax.swing.JMenuItem jMenuItemDeletePreferences;
    private javax.swing.JMenuItem jMenuItemEraseData;
    private javax.swing.JMenuItem jMenuItemFactoryReset;
    private javax.swing.JMenuItem jMenuItemListFiles;
    private javax.swing.JMenuItem jMenuItemListHistory;
    private javax.swing.JMenuItem jMenuItemListHistorySummary;
    private javax.swing.JMenuItem jMenuItemListRaces;
    private javax.swing.JMenuItem jMenuItemListRoutes;
    private javax.swing.JMenuItem jMenuItemPlaylists;
    private javax.swing.JMenuItem jMenuItemPreferences;
    private javax.swing.JMenuItem jMenuItemQuit;
    private javax.swing.JMenuItem jMenuItemReboot;
    private javax.swing.JMenuItem jMenuItemSaveSimSet;
    private javax.swing.JMenuItem jMenuItemSetName;
    private javax.swing.JMenuItem jMenuItemShowLog;
    private javax.swing.JMenuItem jMenuItemShowSettings;
    private javax.swing.JMenuItem jMenuItemShowTrackedActivity;
    private javax.swing.JMenuItem jMenuItemShowUpdateLog;
    private javax.swing.JMenuItem jMenuItemSyncTime;
    private javax.swing.JMenuItem jMenuItemUpdateFirmware;
    private javax.swing.JMenuItem jMenuItemUploadFile;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanelMap;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JRadioButton jRadioButtonCycling;
    private javax.swing.JRadioButton jRadioButtonDriving;
    private javax.swing.JRadioButton jRadioButtonFlying;
    private javax.swing.JRadioButton jRadioButtonHiking;
    private javax.swing.JRadioButton jRadioButtonMulti;
    private javax.swing.JRadioButton jRadioButtonRunning;
    private javax.swing.JRadioButton jRadioButtonSkating;
    private javax.swing.JRadioButton jRadioButtonSwimming;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextAreaStatus;
    private javax.swing.JTextField jTextFieldFirmware;
    private javax.swing.JTextField jTextFieldGpxFile;
    private javax.swing.JTextField jTextFieldProductId;
    private javax.swing.JTextField jTextFieldRouteGpx;
    private javax.swing.JTextField jTextFieldRouteName;
    private javax.swing.JTextField jTextFieldSerial;
    private javax.swing.JTextField jTextFieldTime;
    private javax.swing.JTextField jTextFieldWatch;
    // End of variables declaration//GEN-END:variables
}
