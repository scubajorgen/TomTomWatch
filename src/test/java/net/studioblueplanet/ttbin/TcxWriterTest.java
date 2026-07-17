/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.usb.UsbFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
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

        assertTrue("Outdoor activity is expected to have GPS data", track.hasGpsData());

        tcxWriter.writeTrackToFile(writer, track, appName);
        String tcx              = writer.getBuffer().toString();
        System.out.println(tcx);
        String expected=new String(ToolBox.readBytesFromFile("src/test/resources/test7.tcx"));
        assertEquals(expected, tcx);
    }

    /**
     * An indoor activity (e.g. indoor cycling) has no GPS fix, only heart
     * rate. The writer must still emit those trackpoints (with Time and
     * HeartRateBpm, but no Position) instead of dropping them.
     */
    @Test
    public void testWriteTrackToFile_indoorActivityWithoutGps() throws Exception
    {
        System.out.println("writeTrackToFile (indoor, no GPS)");
        byte[] indoorTtbinData  = ToolBox.readBytesFromFile("src/test/resources/test_indoorcycling.ttbin");
        StringWriter writer     = new StringWriter();
        TomTomReader instance   = TomTomReader.getInstance();
        instance.setTrackSmoothing(false, 0.0f);
        instance.setTrackCompression(false, 0.0);
        Activity track          = instance.readTtbinFile(new UsbFile(0xFFFFFFFF, indoorTtbinData.length, indoorTtbinData));
        String appName          = "test app";
        TcxWriter tcxWriter     = TcxWriter.getInstance();

        assertFalse("Indoor activity is expected to have no GPS data", track.hasGpsData());

        tcxWriter.writeTrackToFile(writer, track, appName);
        String tcx              = writer.getBuffer().toString();

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document        doc     = builder.parse(new InputSource(new StringReader(tcx)));
        NodeList        points  = doc.getElementsByTagName("Trackpoint");

        assertTrue("Expected at least one Trackpoint", points.getLength()>0);

        boolean foundHeartRateWithoutPosition=false;
        for (int i=0; i<points.getLength(); i++)
        {
            Element trackpoint         =(Element)points.item(i);
            boolean hasHeartRate        =trackpoint.getElementsByTagName("HeartRateBpm").getLength()>0;
            boolean hasPosition         =trackpoint.getElementsByTagName("Position").getLength()>0;
            if (hasHeartRate && !hasPosition)
            {
                foundHeartRateWithoutPosition=true;
            }
        }
        assertTrue("Expected a Trackpoint with heart rate but no GPS position (indoor activity)",
                   foundHeartRateWithoutPosition);
    }

}
