/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.usb;

/**
 * Represents a file as on the TomTom watch
 * @author Jorgen
 */
public class UsbFile
{                                           
    /** ID of the file as on the watch */
    public int                  fileId      =-1;
    
    /** Length of the file data */
    public int                  length      =0;
    
    /** The file data */
    public byte[]               fileData    =null;
    
    /** Indicates in some occasions if this file is the last in the list */
    public boolean              endOfList   =false;
    
    /** Indicates whether a copy has been stored on disk. E.g. in case of ttbin files */
    public boolean              stored      =false;
    
    /**
     *  Default Consturctor
     */
    public UsbFile()
    {
    }
    
    /**
     *  Easy initialisation constructor
     *  @param fileId File ID
     *  @param length Data lenght (number of bytes)
     *  @param fileData File data, can be null
     */
    public UsbFile(int fileId, int length, byte[] fileData)
    {
        this.fileId=fileId;
        this.length=length;
        this.fileData=fileData;
    }
}
