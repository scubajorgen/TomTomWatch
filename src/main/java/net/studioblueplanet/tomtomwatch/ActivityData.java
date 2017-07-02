/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.usb.UsbFile;

import java.awt.image.BufferedImage;

/**
 * Collection of the raw data and accompanying activity
 */
public class ActivityData
{
    /** The ttbin file read from the device */
    public UsbFile          file;
    
    /** The activity as derived from the ttbin file */
    public Activity         activity;
    
    /** Indicates whether the ttbin file has been saved to disk */
    public boolean          ttbinSaved = false;
    
    /** Google map image, if the file has been displayed */
    public BufferedImage    mapImage=null;
}