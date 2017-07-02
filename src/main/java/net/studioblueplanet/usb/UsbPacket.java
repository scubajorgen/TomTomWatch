/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.usb;

/**
 * This class represents a packet of data for exchange with the watch.
 * The packet consist of N bytes:
 * byte [0]    Start byte. Tx: 0x09, Rx: 0x01
 * byte [1]    Length of the remaining packet, hence N-2
 * byte [2]    Sequence number. Response sequence number equals the one of the request
 * byte [3]    Message type.
 * byte [4..N> Payload (byte N not included)
 * @author Jorgen
 */
public class UsbPacket
{
    /** Maximum packet data size. */
    public static final int     MAX_PACKET_SIZE=256;
    
    /** The length of the total packet, hence N */
    public byte                 length;
    
    /** The packet. Its size is 256, which is the maximum packet size that is exchanged */
    public byte[]               data=new byte[MAX_PACKET_SIZE];
}