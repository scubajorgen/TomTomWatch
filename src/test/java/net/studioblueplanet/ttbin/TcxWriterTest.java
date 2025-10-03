/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import java.io.StringWriter;
import java.io.Writer;
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
public class TcxWriterTest
{
    private final byte[] ttbinFileData;
    
    public TcxWriterTest()
    {
        ttbinFileData  =ToolBox.readBytesFromFile("src/test/resources/test.ttbin");
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
     * Test of getInstance method, of class TcxWriter.
     */
    @Test
    public void testGetInstance()
    {
        System.out.println("getInstance");
        TcxWriter result = TcxWriter.getInstance();
        assertNotNull(result);
    }

    /**
     * Test of writeTrackToFile method, of class TcxWriter.
     */
    @Test
    public void testWriteTrackToFile()
    {
        System.out.println("writeTrackToFile");
        StringWriter writer     = new StringWriter();
        // No compression, no smoothing
        TomTomReader instance   = TomTomReader.getInstance();
        instance.setTrackSmoothing(false, 0.0f);
        instance.setTrackCompression(false, 0.0);
        Activity track          = instance.readTtbinFile(new UsbFile(0xFFFFFFFF, ttbinFileData.length, ttbinFileData));
        String appName          = "test app";
        TcxWriter tcxWriter     = TcxWriter.getInstance();

        tcxWriter.writeTrackToFile(writer, track, appName);
        String tcx              = writer.getBuffer().toString();
        System.out.println(tcx);
        String expected=new String(ToolBox.readBytesFromFile("src/test/resources/test7.tcx"));
        assertEquals(expected, tcx);
    }
    
}
