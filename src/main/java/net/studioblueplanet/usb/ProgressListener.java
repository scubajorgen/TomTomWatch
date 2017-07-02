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
public interface ProgressListener
{
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
