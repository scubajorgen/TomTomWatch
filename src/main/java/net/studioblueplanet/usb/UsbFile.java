/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.usb;

/**
 *
 * @author Jorgen
 */
public class UsbFile
{
    public int                  fileId      =-1;
    public int                  length      =0;
    public byte[]               fileData    =null;
    public boolean              endOfList   =false;
    public boolean              stored      =false;
}
