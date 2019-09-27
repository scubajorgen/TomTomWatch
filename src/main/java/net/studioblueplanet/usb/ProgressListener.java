/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.usb;

/**
 * Progress listener. Progress when reading or writing files will
 * be reported. Typically, a file is read or written in blocks. Progress
 * is reported after reading or writing a block. The number of bytes 
 * read or written during each is reported. The user/implementer should
 * sum each report to get the total amount of bytes read or written
 * @author Jorgen
 */
public interface ProgressListener
{
    /**
     * Sets the number of expected bytes to be read
     * @param bytes Number of bytes
     */
    public void setReadExpectedBytes(int bytes);
    
    /**
     * Sets the number of expected bytes to be writen
     * @param bytes Number of bytes
     */
    public void setWriteExpectedBytes(int bytes); 
    
    /**
     * This method reports the progress file reading
     * @param bytesRead Number of bytes that have been written
     */
    public void reportReadProgress(int bytesRead);

    /**
     * This method reports the progress file writing
     * @param bytesWritten Number of bytes that have been written
     */
    public void reportWriteProgress(int bytesWritten);

}
