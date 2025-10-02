/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import java.io.StringWriter;
import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.usb.UsbFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jorgen
 */
public class GpxWriterTest
{
    private final byte[] ttbinFileData2;
    
    public GpxWriterTest()
    {
        ttbinFileData2  =ToolBox.readBytesFromFile("src/test/resources/test_cycles.ttbin");
    }    

    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }


    /**
     * Test of writeTrackToFile method, of class GpxWriter.
     */
    @Test
    public void testWriteTrackToFile()
    {
        System.out.println("writeTrackToFile");
        // No compression, no smoothing
        TomTomReader instance   = TomTomReader.getInstance();
        instance.setTrackSmoothing(false, 0.0f);
        instance.setTrackCompression(false, 0.0);
        Activity track          =instance.readTtbinFile(new UsbFile(0xFFFFFFFF, ttbinFileData2.length, ttbinFileData2));
        StringWriter writer     =new StringWriter();
        GpxWriter gpxWriter     =GpxWriter.getInstance();
        gpxWriter.setGpxExtensions(false, true);
        gpxWriter.writeTrackToFile(writer, track, "testapp");
        String gpx              =writer.getBuffer().toString();
        String expected=new String(ToolBox.readBytesFromFile("src/test/resources/test6-none.gpx"));
        assertEquals(expected, gpx);

        // No compressiong, smoothing
        instance.setTrackSmoothing(true, 3.21f);
        instance.setTrackCompression(false, 0.0);
        track                   =instance.readTtbinFile(new UsbFile(0xFFFFFFFF, ttbinFileData2.length, ttbinFileData2));
        gpxWriter.writeTrackToFile(writer, track, "testapp");
        gpx                     =writer.getBuffer().toString();
        expected=new String(ToolBox.readBytesFromFile("src/test/resources/test6-sm.gpx"));
        assertEquals(expected, gpx);

        // Compression, smoothing
        instance.setTrackSmoothing(true, 3.21f);
        instance.setTrackCompression(true, 0.321);
        track                   =instance.readTtbinFile(new UsbFile(0xFFFFFFFF, ttbinFileData2.length, ttbinFileData2));
        gpxWriter.writeTrackToFile(writer, track, "testapp");
        gpx                     =writer.getBuffer().toString();
        expected=new String(ToolBox.readBytesFromFile("src/test/resources/test6-sm-cmp.gpx"));
        assertEquals(expected, gpx);
    }
}
