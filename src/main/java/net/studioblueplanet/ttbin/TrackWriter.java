/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import java.io.Writer;

/**
 *
 * @author jorgen
 */
public interface TrackWriter
{
    /**
     * Write the track to a an export file
     * @param writer Writer to use for writing the file; might be file writer or string writer for example
     * @param track Track to write
     * @param appName Description of the application that writes the file
     */
    public void writeTrackToFile(Writer writer, Activity track, String appName);    
}
