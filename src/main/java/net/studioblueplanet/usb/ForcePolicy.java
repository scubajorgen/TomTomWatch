/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.usb;

import javax.usb.UsbInterfacePolicy;

/**
 * New USB Interface policy that claims the interface if it aready claimed  
 * @author Jorgen
 */
public class ForcePolicy implements UsbInterfacePolicy
{
    @Override 
    public boolean forceClaim(javax.usb.UsbInterface usbInterface) 
    { 
        return true; 
    }     
}
