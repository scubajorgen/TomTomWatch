/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.ttbin;

import net.studioblueplanet.logger.DebugLogger;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;

import hirondelle.date4j.DateTime;


/**
 * This class writes tracks and waypoints to GPX file.
 * The major difference between GPX 1.0 and GPX 1.1 is the &lt;extensions&gt; element.
 * This element is available in GPX 1.1 and not in GPX 1.0.
 * Minor difference is that &lt;speed&gt; and &lt;course&gt; no longer exist in GPX 1.1.
 * Therefore, tracks writen in GPX 1.1 use the extensions element. Private
 * fields are used in the &lt;extensions&gt; element in the u-gotMe namespace
 * @author Jorgen
 */
public class GpxWriter
{
    private static GpxWriter    theInstance=null;


    private int                 trackPoints;
    private int                 wayPoints;
    private String              gpxVersion;


    Document                    doc;
    Element                     gpxElement;


    /**
     * Constructor
     */
    private GpxWriter()
    {
        gpxVersion      =new String("1.1");
    }

    /**
     * This method returns the one and only instance of this singleton class
     * @return The instance
     */
    public static GpxWriter getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new GpxWriter();
        }

        return theInstance;
    }

    /**
     * This method sets the GPX version
     * @param newVersion The new version "1.0" and "1.1" allowed.
     */
    public void setGpxVersion(String newVersion)
    {
        if(newVersion.equals("1.0") || newVersion.equals("1.1"))
        {
            this.gpxVersion=newVersion;
        }
        else
        {
            DebugLogger.error("Illegal GPX version "+newVersion+
                              ". Version left to "+gpxVersion);
        }
    }


    /**
     * This method creates the XML document, adds the GPX headers and
     * creates the <gpx> element. The variables doc and gpxElement will
     * be global variables in this class.
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    private void createGpxDocument(String deviceType) throws ParserConfigurationException
    {
        String      creator;


        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // The document
        doc = docBuilder.newDocument();


        // gpx elements
        gpxElement = doc.createElement("gpx");
        doc.appendChild(gpxElement);


        if (gpxVersion.equals("1.0"))
        {
            this.addGpx1_0Header(doc, gpxElement, deviceType);
        }
        else if (gpxVersion.equals("1.1"))
        {
            this.addGpx1_1Header(doc, gpxElement, deviceType);
        }
    }

    /**
     * Add the GPX 1.0 header
     * @param doc The XML document
     * @param gpxElement The GPX element
     */
    private void addGpx1_0Header(Document doc, Element gpxElement, String creator)
    {
        Attr        attr;

        // GPX version 1.0

        attr = doc.createAttribute("creator");
        attr.setValue(creator);
        gpxElement.setAttributeNode(attr);

        attr = doc.createAttribute("version");
        attr.setValue("1.0");
        gpxElement.setAttributeNode(attr);

        // GPX namespace
        attr = doc.createAttribute("xmlns");
        attr.setValue("http://www.topografix.com/GPX/1/0");
        gpxElement.setAttributeNode(attr);

        // XMLSchema namespace
        attr = doc.createAttribute("xmlns:xsi");
        attr.setValue("http://www.w3.org/2001/XMLSchema-instance");
        gpxElement.setAttributeNode(attr);

         // Schema locations - just the GPX location
        attr = doc.createAttribute("xsi:schemaLocation");
        attr.setValue("http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd ");
        gpxElement.setAttributeNode(attr);

    }

    /**
     * Add the GPX 1.1 header
     * @param doc The XML document
     * @param gpxElement The GPX element
     */
    private void addGpx1_1Header(Document doc, Element gpxElement, String creator)
    {
        Comment     comment;
        Attr        attr;

        // GPX version 1.1
        attr = doc.createAttribute("creator");
        attr.setValue(creator);
        gpxElement.setAttributeNode(attr);

        attr = doc.createAttribute("version");
        attr.setValue("1.1");
        gpxElement.setAttributeNode(attr);

        // XMLSchema namespace
        attr = doc.createAttribute("xmlns:xsi");
        attr.setValue("http://www.w3.org/2001/XMLSchema-instance");
        gpxElement.setAttributeNode(attr);

        // GPX namespace
        attr = doc.createAttribute("xmlns");
        attr.setValue("http://www.topografix.com/GPX/1/1");
        gpxElement.setAttributeNode(attr);

        // u-gotMe namespace
        attr = doc.createAttribute("xmlns:u-gotMe");
        attr.setValue("http://tracklog.studioblueplanet.net/gpxextensions/v1");
        gpxElement.setAttributeNode(attr);

        // Schema locations
        attr = doc.createAttribute("xsi:schemaLocation");
        attr.setValue("http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd "+
                      "http://tracklog.studioblueplanet.net/gpxextensions/v1 http://tracklog.studioblueplanet.net/gpxextensions/v1/ugotme-gpx.xsd");
        gpxElement.setAttributeNode(attr);

    }

    /**
     * This method writes the GPX file
     * @param fileName Name of the file
     * @throws javax.xml.transform.TransformerException
     */
    void writeGpxDocument(String fileName) throws TransformerException
    {
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");


            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileName));


            transformer.transform(source, result);
    }

    /**
     * Appends the track points to the segment under GPX 1.0. GPX 1.0 supports
     * the elements 'course' and 'speed'. Under 'hdop' the ehpe value is
     * stored. In fact this is not correct, since ehpe is not hdop.
     * @param doc
     * @param segmentElement Document element representing the track segment
     * @param trackNo The track
     * @param segmentNo The segment
     */
    private void appendTrackSegmentGpx1_0(Document doc, Element segmentElement, ActivitySegment segment)
    {
        ArrayList <ActivityRecord>  points;
        Iterator<ActivityRecord>    iterator;
        ActivityRecord              point;
        Element                     pointElement;
        Element                     element;
        Attr                        attr;
        DateTime                    dateTime;
        String                      dateTimeString;
        double                      latitude;
        double                      longitude;
        double                      elevation;


        points=segment.getRecords();

        iterator=points.iterator();

        while (iterator.hasNext())
        {
            point           =iterator.next();
            
            dateTime        =point.getDateTime();
            latitude        =((ActivityRecordGps)point).getLatitude();
            longitude       =((ActivityRecordGps)point).getLongitude();
            elevation       =((ActivityRecordGps)point).getDerivedElevation();
            
            if ((dateTime!=null) && 
                (latitude!=ActivityRecord.INVALID) && (longitude!=ActivityRecord.INVALID) && 
                (latitude!=0.0) && (longitude!=0.0) &&
                (elevation!=ActivityRecord.INVALID))
            {            
                pointElement    = doc.createElement("trkpt");
                segmentElement.appendChild(pointElement);

                element    = doc.createElement("ele");
                element.appendChild(doc.createTextNode(String.format("%.1f", elevation)));
                pointElement.appendChild(element);

                element    = doc.createElement("time");
                
                dateTimeString=dateTime.format("YYYY-MM-DD")+"T"+
                               dateTime.format("hh:mm:ss")+"Z";
                element.appendChild(doc.createTextNode(dateTimeString));
                pointElement.appendChild(element);


                // Extensions: speed
                element    = doc.createElement("speed");
                element.appendChild(doc.createTextNode(String.valueOf(((ActivityRecordGps)point).getSpeed())));
                pointElement.appendChild(element);

                // TO DO ADD DISTANCE

                // set attribute 'lat' to element
                attr = doc.createAttribute("lat");
                attr.setValue(String.format("%.7f", latitude));
                pointElement.setAttributeNode(attr);

                // set attribute 'lon' to element
                attr = doc.createAttribute("lon");
                attr.setValue(String.format("%.7f", longitude));
                pointElement.setAttributeNode(attr);

                trackPoints++;
            }
        }
    }

    /**
     * Appends the track points to the segment under GPX 1.1. GPX 1.1
     * does not support the elements 'course' and 'speed'.
     * The elements 'course', 'speed' and 'ehpe' are stored under the
     * 'extensions' element.
     * @param doc
     * @param segmentElement Document element representing the track segment
     * @param trackNo The track
     * @param segmentNo The segment
     */
    private void appendTrackSegmentGpx1_1(Document doc, Element segmentElement, ActivitySegment segment)
    {
        ArrayList <ActivityRecord>  points;
        Iterator<ActivityRecord>    iterator;
        ActivityRecord              point;
        Element                     pointElement;
        Element                     element;
        Element                     extensionsElement;
        Attr                        attr;
        DateTime                    dateTime;
        String                      dateTimeString;
        int                         temperature;
        int                         heartRate;
        double                      latitude;
        double                      longitude;
        double                      elevation;
        double                      heading;
        double                      ascend;
        double                      descend;
        double                      calories;
        double                      distance;
        double                      speed;
        int                         ehpe;
        int                         evpe;
        int                         hdop;
        

        points=segment.getRecords();

        iterator=points.iterator();

        while (iterator.hasNext())
        {
            point           =iterator.next();
            
            
            dateTime        =point.getDateTime();
            latitude        =((ActivityRecordGps)point).getLatitude();
            longitude       =((ActivityRecordGps)point).getLongitude();
            elevation       =((ActivityRecordGps)point).getDerivedElevation();
            heartRate       =((ActivityRecordGps)point).getHeartRate();
            temperature     =((ActivityRecordGps)point).getTemperature();
            speed           =((ActivityRecordGps)point).getSpeed();
            ehpe            =((ActivityRecordGps)point).getEhpe();
            evpe            =((ActivityRecordGps)point).getEvpe();
            hdop            =((ActivityRecordGps)point).getHdop();
            
            
            // TO DO: add or not add. Can be derived...
            heading         =((ActivityRecordGps)point).getHeading();
            calories        =((ActivityRecordGps)point).getCalories();
            ascend          =((ActivityRecordGps)point).getCumulativeAscend();
            descend         =((ActivityRecordGps)point).getCumulativeDecend();
            distance        =((ActivityRecordGps)point).getDistance();
            
            if ((dateTime!=null) && 
                    (latitude!=ActivityRecord.INVALID) && (longitude!=ActivityRecord.INVALID) && 
                    (elevation!=ActivityRecord.INVALID) &&
                    (latitude!=0.0) && (longitude!=0.0))
            {             
                pointElement    = doc.createElement("trkpt");
                segmentElement.appendChild(pointElement);

                // The elevation.
                element    = doc.createElement("ele");
                element.appendChild(doc.createTextNode(String.format("%.1f", elevation)));
                pointElement.appendChild(element);

                element    = doc.createElement("time");
                dateTimeString=dateTime.format("YYYY-MM-DD")+"T"+
                               dateTime.format("hh:mm:ss")+"Z";
                element.appendChild(doc.createTextNode(dateTimeString));
                pointElement.appendChild(element);

                extensionsElement    = doc.createElement("extensions");
                pointElement.appendChild(extensionsElement);

                // Extensions: speed
                element    = doc.createElement("u-gotMe:speed");
                element.appendChild(doc.createTextNode(String.format("%.2f", speed)));
                extensionsElement.appendChild(element);

                // Extensions: course
                element    = doc.createElement("u-gotMe:course");
                element.appendChild(doc.createTextNode(String.format("%.2f", heading)));
                extensionsElement.appendChild(element);

                // Extensions: temperature
                if (temperature!=ActivityRecord.INVALID)
                {
                    element    = doc.createElement("u-gotMe:temp");
                    element.appendChild(doc.createTextNode(String.valueOf(temperature)));
                    extensionsElement.appendChild(element);
                }

                // Extension: heartrate
                if ((heartRate!=ActivityRecord.INVALID) && (heartRate>0))
                {
                    element    = doc.createElement("u-gotMe:hr");
                    element.appendChild(doc.createTextNode(String.valueOf(heartRate)));
                    extensionsElement.appendChild(element);
                }

                // Extension: ehpe
                if (ehpe!=ActivityRecord.INVALID)
                {
                    element    = doc.createElement("u-gotMe:ehpe");
                    element.appendChild(doc.createTextNode(String.valueOf(ehpe)));
                    extensionsElement.appendChild(element);
                }

                // Extension: evpe
                if (evpe!=ActivityRecord.INVALID)
                {
                    element    = doc.createElement("u-gotMe:evpe");
                    element.appendChild(doc.createTextNode(String.valueOf(evpe)));
                    extensionsElement.appendChild(element);
                }

                // Extension: hdop
                if (hdop!=ActivityRecord.INVALID)
                {
                    element    = doc.createElement("u-gotMe:hdop");
                    element.appendChild(doc.createTextNode(String.valueOf(hdop)));
                    extensionsElement.appendChild(element);
                }

                // Extension: ascend
                if (evpe!=ActivityRecord.INVALID)
                {
                    element    = doc.createElement("u-gotMe:ascend");
                    element.appendChild(doc.createTextNode(String.format("%.1f", ascend)));
                    extensionsElement.appendChild(element);
                }

                // Extension: descend
                if (evpe!=ActivityRecord.INVALID)
                {
                    element    = doc.createElement("u-gotMe:descend");
                    element.appendChild(doc.createTextNode(String.format("%.1f", descend)));
                    extensionsElement.appendChild(element);
                }

                // set attribute 'lat' to element
                attr = doc.createAttribute("lat");
                attr.setValue(String.format("%.7f", latitude));
                pointElement.setAttributeNode(attr);

                // set attribute 'lon' to element
                attr = doc.createAttribute("lon");
                attr.setValue(String.format("%.7f", longitude));
                pointElement.setAttributeNode(attr);

                trackPoints++;
            }
        }
    }

    /**
     * Appends the waypoints belonging to the given track segment.
     * Assume GPX 1.0 or 1.1
     * @param doc The XML document
     * @param trackElement The element representing the track
     * @param trackNo The track identification
     * @param segmentNo The segment identification
     */
    private void appendWaypointsGpx(Document doc, Element trackElement, Activity track)
    {
        ArrayList <ActivityRecord>  points;
        Iterator<ActivityRecord>    iterator;
        ActivityRecord              point;
        Element                     pointElement;
        Element                     element;
        Element                     extensionElement;
        Attr                        attr;
        DateTime                    dateTime;
        String                      dateTimeString;


        // Retrieve the list of waypoints
        points=track.getWaypoints();

        // Parse the points
        iterator=points.iterator();

        while (iterator.hasNext())
        {
            point           =iterator.next();
            pointElement    = doc.createElement("wpt");
            trackElement.appendChild(pointElement);

            element         = doc.createElement("time");
            dateTime        =point.getDateTime();
            dateTimeString  =dateTime.format("YYYY-MM-DD")+"T"+
                             dateTime.format("hh:mm:ss")+"Z";
            element.appendChild(doc.createTextNode(dateTimeString));
            pointElement.appendChild(element);
            
            element         = doc.createElement("name");
            element.appendChild(doc.createTextNode(String.format("waypoint%02d",wayPoints)));
            pointElement.appendChild(element);
           
            element         = doc.createElement("desc");
            element.appendChild(doc.createTextNode(String.format("waypoint%02d",wayPoints)));
            pointElement.appendChild(element);
           
            element         = doc.createElement("sym");
            element.appendChild(doc.createTextNode("Waypoint"));
            pointElement.appendChild(element);
           
            // set attribute 'lat' to element
            attr = doc.createAttribute("lat");
            attr.setValue(String.valueOf(((ActivityRecordGps)point).getLatitude()));
            pointElement.setAttributeNode(attr);

            // set attribute 'lon' to element
            attr = doc.createAttribute("lon");
            attr.setValue(String.valueOf(((ActivityRecordGps)point).getLongitude()));
            pointElement.setAttributeNode(attr);

            element    = doc.createElement("ele");
            element.appendChild(doc.createTextNode(String.valueOf(((ActivityRecordGps)point).getElevation1())));
            pointElement.appendChild(element);


            wayPoints++;
        }

    }



    /**
     * This method adds the track segments to the track.
     * @param doc XML document
     * @param gpxElement The GPX element
     * @param track The track to write
     * @param appName Application name
     */
    private void addTrack(Document doc, Element gpxElement, Activity track, String appName)
    {
        int     i;
        int     numberOfSegments;
        Element trackElement;
        Element segmentElement;
        Element extensionsElement;
        Element element;
        
        ActivitySegment segment;
        String          trackName;
        String          trackDescription;
        
        numberOfSegments=track.getNumberOfSegments();

        if (gpxVersion.equals("1.0"))
        {
            appendWaypointsGpx(doc, gpxElement, track);
        }
        else if (gpxVersion.equals("1.1"))
        {
            appendWaypointsGpx(doc, gpxElement, track);
        }
        
        trackName           ="Track - "+track.getActivityDescription();
        trackDescription    ="Created by: "+appName+". Logged by: \'"+track.getDeviceName()+"\'. Logged as: "+track.getActivityDescription()+".";
        if (track.isSmoothed())
        {
            trackDescription+=" Smoothing ("+String.format("%.1f", track.getTrackSmoothingQFactor())+") applied.";
        }
        
        // The track element
        trackElement = doc.createElement("trk");
        gpxElement.appendChild(trackElement);

        element = doc.createElement("name");
        element.appendChild(doc.createTextNode(trackName));
        trackElement.appendChild(element);

//        description=track.getDeviceName() +" logged track";
        element = doc.createElement("desc");
        element.appendChild(doc.createTextNode(trackDescription));
        trackElement.appendChild(element);        
        
        // Add the track segments.
        i=0;
        while (i<numberOfSegments)
        {
            segment=track.getSegment(i);
            
            // segment
            segmentElement          = doc.createElement("trkseg");
            trackElement.appendChild(segmentElement);
            
            if (gpxVersion.equals("1.0"))
            {
                appendTrackSegmentGpx1_0(doc, segmentElement, segment);
            }
            else if (gpxVersion.equals("1.1"))
            {
                appendTrackSegmentGpx1_1(doc, segmentElement, segment);
            }

            this.addTrackSegmentExtensions(segment, segmentElement);
            
            i++;
        }
        
        // Add the track extensions, if required
        this.addTrackExtensions(track, gpxElement);
        
    }

    /**
     * Adds a &lt;extensions&gt; section to the trkseg, if needed. It 
     * contains the heart rate recovery.
     * @param segment Segment containing the segment data
     * @param segmentElement XML Element describing the segment.
     */
    private void addTrackSegmentExtensions(ActivitySegment segment, Element segmentElement)
    {
        int             heartRateRecovery;
        int             heartRateRecoveryScore;
        Element         extensionsElement;
        Element         element;

        
        // Extensions
        heartRateRecovery       =segment.getHeartRateRecovery();
        heartRateRecoveryScore  =segment.getHeartRateRecoveryScore();

        if (heartRateRecovery!=ActivitySegment.HRRECOVERY_UNDEFINED || heartRateRecoveryScore>=0)
        {
            extensionsElement       = doc.createElement("extensions");
            segmentElement.appendChild(extensionsElement);


            // Extensions: hr Recovery
            if (heartRateRecovery!=ActivitySegment.HRRECOVERY_UNDEFINED)
            {
                element    = doc.createElement("u-gotMe:hrRecovery");
                element.appendChild(doc.createTextNode(String.valueOf(heartRateRecovery)));
                extensionsElement.appendChild(element);
            }

            // Extensions: hr Recovery score
            if (heartRateRecoveryScore>=0)
            {
                element    = doc.createElement("u-gotMe:hrRecoveryScore");
                element.appendChild(doc.createTextNode(String.valueOf(heartRateRecoveryScore)));
                extensionsElement.appendChild(element);
            }
        }        
    }
    
    /**
     * Add the &lt;extensions&gt; section to the track
     * @param track Track containing the track information
     * @param trackElement XML Track element
     */
    private void addTrackExtensions(Activity track, Element trackElement)
    {
        int     fitnessPoints;
        float   trackSmoothing;
        boolean isSmoothed;
        Element extensionsElement;
        Element element;
        

        extensionsElement       = doc.createElement("extensions");
        trackElement.appendChild(extensionsElement);


        element    = doc.createElement("u-gotMe:device");
        element.appendChild(doc.createTextNode(track.getDeviceName()));
        extensionsElement.appendChild(element);

        element    = doc.createElement("u-gotMe:activity");
        element.appendChild(doc.createTextNode(track.getActivityDescription()));
        extensionsElement.appendChild(element);

        // Extensions: fitnesspoints
        fitnessPoints       =track.getFitnessPoints();
        if (fitnessPoints!=Activity.FITNESSPOINTS_UNDEFINED)
        {
            element    = doc.createElement("u-gotMe:tomtomFitnessPoints");
            element.appendChild(doc.createTextNode(String.valueOf(fitnessPoints)));
            extensionsElement.appendChild(element);
        }

        isSmoothed          =track.isSmoothed();
        if (isSmoothed)
        {
            trackSmoothing      =track.getTrackSmoothingQFactor();
            element    = doc.createElement("u-gotMe:smoothingFactor");
            element.appendChild(doc.createTextNode(String.valueOf(trackSmoothing)));
            extensionsElement.appendChild(element);
        }
    }





    /* ************************************************************************\
     * The interface functions
     * ************************************************************************/
    /**
     * Write the track to a GPX file
     * @param fileName Name of the file to write to
     * @param track Track to write to the GPX file
     * @param appName Application description
     */
    public void writeTrackToFile(String fileName, Activity track, String appName)
    {
        Element     trackElement;
        Element     element;
        Comment     comment;
        Attr        attr;
        String      creator;


        wayPoints=0;
        trackPoints=0;

        try
        {
            // create the GPX file
            createGpxDocument(appName);


            addTrack(doc, gpxElement, track, appName);

            // write the content into xml file
            writeGpxDocument(fileName);

            DebugLogger.info("GpxWriter says: 'File saved to " + fileName + "!'");
            DebugLogger.info("Track: "+track.getActivityDescription()+", track points: "+trackPoints+
                             ", wayPoints: "+wayPoints);

        }
        catch (ParserConfigurationException pce)
        {
            pce.printStackTrace();
        }
        catch (TransformerException tfe)
        {
            tfe.printStackTrace();
        }

    }

}
 