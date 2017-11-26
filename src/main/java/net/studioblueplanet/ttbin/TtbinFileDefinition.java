/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;


import java.util.ArrayList;

/**
 *
 * @author jorgen.van.der.velde
 */
public class TtbinFileDefinition
{
    // Tag definitions in the ttbin file. Each recrod in the ttbin file starts
    // with a tag byte, defining the type of record
    public static final    byte                        TAG_FILE_HEADER         =0x20;
    public static final    byte                        TAG_STATUS              =0x21;
    public static final    byte                        TAG_GPS                 =0x22;
    public static final    byte                        TAG_PRECISION           =0x23; // Some measured data? Accelerators??
    public static final    byte                        TAG_HEART_RATE          =0x25;
    public static final    byte                        TAG_SUMMARY             =0x27;
    public static final    byte                        TAG_POOL_SIZE           =0x2a;
    public static final    byte                        TAG_WHEEL_SIZE          =0x2b;
    public static final    byte                        TAG_TRAINING_SETUP      =0x2d;
    public static final    byte                        TAG_LAP                 =0x2f;
    public static final    byte                        TAG_30                  =0x30;
    public static final    byte                        TAG_CYCLING_CADENCE     =0x31;
    public static final    byte                        TAG_TREADMILL           =0x32;
    public static final    byte                        TAG_SWIM                =0x34;
    public static final    byte                        TAG_GOAL_PROGRESS       =0x35;
    public static final    byte                        TAG_37                  =0x37;
    public static final    byte                        TAG_INTERVAL_SETUP      =0x39;
    public static final    byte                        TAG_INTERVAL_START      =0x3a;
    public static final    byte                        TAG_INTERVAL_FINISH     =0x3b;
    public static final    byte                        TAG_RACE_SETUP          =0x3c;
    public static final    byte                        TAG_RACE_RESULT         =0x3d;
    public static final    byte                        TAG_ALTITUDE_UPDATE     =0x3e;
    public static final    byte                        TAG_HEART_RATE_RECOVERY =0x3f;  // Heart rate recovery 1 minute after set to pause
    public static final    byte                        TAG_INDOOR_CYCLING      =0x40;
    public static final    byte                        TAG_GYM                 =0x41;
    public static final    byte                        TAG_MOVEMENT            =0x42; 
    public static final    byte                        TAG_ROUTEDESCRIPTION    =0x44;
    public static final    byte                        TAG_ELEVATION           =0x47;
    public static final    byte                        TAG_48                  =0x48; 
    public static final    byte                        TAG_49                  =0x49; 
    public static final    byte                        TAG_FITNESSPOINTS       =0x4a; 
    public static final    byte                        TAG_4B                  =0x4b; // Variable lenght!!
  
    
    public static final    byte                        STATUS_READY            =0x00;
    public static final    byte                        STATUS_ACTIVE           =0x01;
    public static final    byte                        STATUS_PAUSED           =0x02;
    public static final    byte                        STATUS_STOPPED          =0x03;

    public static final    byte                        ACTIVITY_RUNNING        =0;
    public static final    byte                        ACTIVITY_CYCLING        =1;
    public static final    byte                        ACTIVITY_SWIMMING       =2;
    public static final    byte                        ACTIVITY_TREADMILL      =7;
    public static final    byte                        ACTIVITY_FREESTYLE      =8;
    public static final    byte                        ACTIVITY_GYM            =9;
    public static final    byte                        ACTIVITY_HIKING         =10;
    public static final    byte                        ACTIVITY_INDOORCYCLING  =11;
    public static final    byte                        ACTIVITY_TRAILRUNNING   =14;
    public static final    byte                        ACTIVITY_SKIING         =15;
    public static final    byte                        ACTIVITY_SNOWBOARDING   =16;    
}
