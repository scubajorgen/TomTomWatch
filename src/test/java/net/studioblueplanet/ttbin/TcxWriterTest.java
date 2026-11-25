/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Assume;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
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

    /**
     * Read the activity and convert it to TCX DOM document
     * @param file The ttbin file
     * @return The TCX document
     */
    private Document writeTcx(UsbFile file) throws Exception
    {
        StringWriter writer     = new StringWriter();
        TomTomReader instance   = TomTomReader.getInstance();
        instance.setTrackSmoothing(false, 0.0f);
        instance.setTrackCompression(false, 0.0);
        Activity track          = instance.readTtbinFile(file);
        TcxWriter.getInstance().writeTrackToFile(writer, track, "test app");
        return parseTcx(writer.toString());
    }

    /**
     * Parse TCX string as DOM document; checks it is well formed
     * @param tcx The TCX
     * @return The document
     */
    private Document parseTcx(String tcx) throws Exception
    {
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(tcx)));
    }

    /**
     * Returns the text of the first descendant element with given name
     * @param parent Parent element
     * @param name Name of the child
     * @return The text or null if not found
     */
    private String getChildText(Element parent, String name)
    {
        NodeList nodes=parent.getElementsByTagName(name);
        return nodes.getLength()>0?nodes.item(0).getTextContent().trim():null;
    }

    /**
     * Checks the order of the child elements of the Lap as required by the
     * TrainingCenterDatabase v2 XSD
     * @param lap The Lap element
     */
    private void assertLapElementOrder(Element lap)
    {
        List<String> xsdOrder=Arrays.asList("TotalTimeSeconds", "DistanceMeters", "MaximumSpeed", "Calories",
                                            "AverageHeartRateBpm", "MaximumHeartRateBpm", "Intensity", "Cadence",
                                            "TriggerMethod", "Track", "Notes", "Extensions");
        int     previous=-1;
        Node    node=lap.getFirstChild();
        while (node!=null)
        {
            if (node.getNodeType()==Node.ELEMENT_NODE)
            {
                int index=xsdOrder.indexOf(node.getLocalName());
                assertTrue("Unexpected element "+node.getLocalName(), index>=0);
                assertTrue("Element out of order "+node.getLocalName(), index>previous);
                previous=index;
            }
            node=node.getNextSibling();
        }
        assertTrue("Track must contain Trackpoints", lap.getElementsByTagName("Trackpoint").getLength()>0);
    }

    /**
     * Checks a Lap element
     */
    private void assertLap(Element lap, String startTime, String time, String distance, String calories,
                           String avgHr, String maxHr, int trackpoints)
    {
        assertEquals(startTime, lap.getAttribute("StartTime"));
        assertEquals(time     , getChildText(lap, "TotalTimeSeconds"));
        assertEquals(distance , getChildText(lap, "DistanceMeters"));
        assertEquals(calories , getChildText(lap, "Calories"));
        assertEquals(avgHr    , getChildText((Element)lap.getElementsByTagName("AverageHeartRateBpm").item(0), "Value"));
        assertEquals(maxHr    , getChildText((Element)lap.getElementsByTagName("MaximumHeartRateBpm").item(0), "Value"));
        assertEquals("Manual" , getChildText(lap, "TriggerMethod"));
        assertEquals(trackpoints, lap.getElementsByTagName("Trackpoint").getLength());
        assertLapElementOrder(lap);
    }

    /**
     * Test of writeTrackToFile method, of class TcxWriter: Id and lap start
     * time are UTC and equal to the first Trackpoint time
     */
    @Test
    public void testWriteTrackToFileStartTime() throws Exception
    {
        System.out.println("writeTrackToFile start time");
        Document doc            = writeTcx(new UsbFile(0xFFFFFFFF, ttbinFileData.length, ttbinFileData));
        Element  lap            = (Element)doc.getElementsByTagName("Lap").item(0);
        String   firstPoint     = getChildText((Element)doc.getElementsByTagName("Trackpoint").item(0), "Time");
        assertEquals("2019-09-20T13:16:39Z", firstPoint);
        assertEquals(firstPoint, getChildText(doc.getDocumentElement(), "Id"));
        assertEquals(firstPoint, lap.getAttribute("StartTime"));
    }

    /**
     * Test of writeTrackToFile method, of class TcxWriter: activity with
     * manual laps. 3 lap marks result in 4 laps.
     */
    @Test
    public void testWriteTrackToFileWithLaps() throws Exception
    {
        System.out.println("writeTrackToFile with laps");
        Document doc            = writeTcx(TtbinTestFileBuilder.createRunWithLaps(false));
        NodeList laps           = doc.getElementsByTagName("Lap");

        assertEquals("Running", ((Element)doc.getElementsByTagName("Activity").item(0)).getAttribute("Sport"));
        assertEquals("2026-09-21T14:13:20Z", getChildText(doc.getDocumentElement(), "Id"));
        assertEquals(4, laps.getLength());
        // hr=100+i/10: lap 1 records 0-199: avg 109.5, max 119
        assertLap((Element)laps.item(0), "2026-09-21T14:13:20Z", "200", "600.0", "40", "110", "119", 200);
        assertLap((Element)laps.item(1), "2026-09-21T14:16:40Z", "200", "600.0", "40", "130", "139", 200);
        assertLap((Element)laps.item(2), "2026-09-21T14:20:00Z", "100", "300.0", "20", "145", "149", 100);
        // Last lap: remainder up to the summary totals
        assertLap((Element)laps.item(3), "2026-09-21T14:21:40Z", "100", "300.0", "20", "155", "159", 100);
    }

    /**
     * Test of writeTrackToFile method, of class TcxWriter: activity with
     * manual laps and a pause in the second lap. The second lap is split
     * in two at the pause.
     */
    @Test
    public void testWriteTrackToFileWithLapsAndPause() throws Exception
    {
        System.out.println("writeTrackToFile with laps and pause");
        Document doc            = writeTcx(TtbinTestFileBuilder.createRunWithLaps(true));
        NodeList laps           = doc.getElementsByTagName("Lap");

        assertEquals(5, laps.getLength());
        assertLap((Element)laps.item(0), "2026-09-21T14:13:20Z", "200", "600.0", "40", "110", "119", 200);
        // Values at the pause estimated from the last record before the pause (record 299)
        assertLap((Element)laps.item(1), "2026-09-21T14:16:40Z",  "99", "297.0", "19", "125", "129", 100);
        assertLap((Element)laps.item(2), "2026-09-21T14:19:20Z", "101", "303.0", "21", "135", "139", 100);
        assertLap((Element)laps.item(3), "2026-09-21T14:21:00Z", "100", "300.0", "20", "145", "149", 100);
        assertLap((Element)laps.item(4), "2026-09-21T14:22:40Z", "100", "300.0", "20", "155", "159", 100);
    }

    /**
     * Test of writeTrackToFile method with a real file of a TomTom watch
     * containing 18 manual laps. The file contains private GPS data and is
     * not part of the repository; the test is skipped if it is not present.
     * See TomTomReaderTest.testReadLapsLocalFile().
     */
    @Test
    public void testWriteTrackToFileWithLapsLocalFile() throws Exception
    {
        System.out.println("writeTrackToFile with laps, local file");
        File file=new File(System.getProperty("lapTestFile", TomTomReaderTest.LAPTESTFILE));
        Assume.assumeTrue("Local lap test file not present", file.exists());

        byte[]   data           = ToolBox.readBytesFromFile(file.getPath());
        Document doc            = writeTcx(new UsbFile(0xFFFFFFFF, data.length, data));
        NodeList laps           = doc.getElementsByTagName("Lap");
        int[][]  values         = TomTomReaderTest.LAPTESTVALUES;

        assertTrue(laps.getLength()>=values.length);
        int prevTime=0;
        int prevCalories=0;
        for (int i=0; i<values.length; i++)
        {
            Element lap=(Element)laps.item(i);
            assertEquals("Lap "+(i+1), String.valueOf(values[i][0]-prevTime), getChildText(lap, "TotalTimeSeconds"));
            assertEquals("Lap "+(i+1), String.valueOf(values[i][2]-prevCalories), getChildText(lap, "Calories"));
            assertLapElementOrder(lap);
            prevTime    =values[i][0];
            prevCalories=values[i][2];
        }
    }

    /**
     * Test of writeTrackToFile method, of class TcxWriter: indoor cycling,
     * no GPS. Trackpoints contain time and heart rate, no position.
     */
    @Test
    public void testWriteTrackToFileIndoorCycling() throws Exception
    {
        System.out.println("writeTrackToFile indoor cycling");
        Document doc            = writeTcx(TtbinTestFileBuilder.createIndoorActivity(TtbinFileDefinition.ACTIVITY_INDOORCYCLING, false));
        NodeList points         = doc.getElementsByTagName("Trackpoint");

        assertEquals("Biking", ((Element)doc.getElementsByTagName("Activity").item(0)).getAttribute("Sport"));
        // Heart rate records contain local time: converted to UTC
        assertEquals("2026-09-21T14:13:20Z", getChildText(doc.getDocumentElement(), "Id"));
        assertEquals(1, doc.getElementsByTagName("Lap").getLength());
        assertEquals("2026-09-21T14:13:20Z", ((Element)doc.getElementsByTagName("Lap").item(0)).getAttribute("StartTime"));
        assertEquals(300, points.getLength());
        assertEquals(0, doc.getElementsByTagName("Position").getLength());
        Element point=(Element)points.item(299);
        assertEquals("2026-09-21T14:18:19Z", getChildText(point, "Time"));
        assertEquals("119", getChildText(point, "Value"));
        assertLapElementOrder((Element)doc.getElementsByTagName("Lap").item(0));
    }

    /**
     * Test of writeTrackToFile method, of class TcxWriter: treadmill, no GPS,
     * with a lap mark
     */
    @Test
    public void testWriteTrackToFileTreadmillWithLap() throws Exception
    {
        System.out.println("writeTrackToFile treadmill with lap");
        Document doc            = writeTcx(TtbinTestFileBuilder.createIndoorActivity(TtbinFileDefinition.ACTIVITY_TREADMILL, true));
        NodeList laps           = doc.getElementsByTagName("Lap");

        assertEquals("Running", ((Element)doc.getElementsByTagName("Activity").item(0)).getAttribute("Sport"));
        assertEquals(2, laps.getLength());
        assertLap((Element)laps.item(0), "2026-09-21T14:13:20Z", "100", "0.0", "10",  "95",  "99", 100);
        assertLap((Element)laps.item(1), "2026-09-21T14:15:00Z", "200", "0.0", "20", "110", "119", 200);
        assertEquals(0, doc.getElementsByTagName("Position").getLength());
    }

    /**
     * Test of writeTrackToFile method, of class TcxWriter: activities with
     * GPS do not write the heart rate records without position
     */
    @Test
    public void testWriteTrackToFileGpsNoHeartRateRecords() throws Exception
    {
        System.out.println("writeTrackToFile GPS activity");
        Document doc            = writeTcx(TtbinTestFileBuilder.createRunWithLaps(false));
        assertEquals(600, doc.getElementsByTagName("Trackpoint").getLength());
        assertEquals(600, doc.getElementsByTagName("Position").getLength());
    }
}
