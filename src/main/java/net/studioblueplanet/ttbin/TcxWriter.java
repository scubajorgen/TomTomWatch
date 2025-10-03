/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import hirondelle.date4j.DateTime;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.studioblueplanet.logger.DebugLogger;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author jorgen
 */
public class TcxWriter implements TrackWriter
{
    private static TcxWriter    theInstance=null;
    private int                 trackPoints;
    private int                 wayPoints;
    private Document            doc;
    private Element             tcxElement;

    /**
     * Constructor
     */
    private TcxWriter()
    {
        
    }

    /**
     * This method returns the one and only instance of this singleton class
     * @return The instance
     */
    public static TcxWriter getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new TcxWriter();
        }
        return theInstance;
    }

    /**
     * This method creates the XML document, adds the GPX headers and
     * creates the <gpx> element. The variables doc and gpxElement will
     * be global variables in this class.
     * @param deviceType Indication of the logging device
     * @param track Track
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    private void createTcxDocument(String deviceType, Activity track) throws ParserConfigurationException
    {
        String      creator="";

        DocumentBuilderFactory docFactory   = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder          = docFactory.newDocumentBuilder();

        // The document
        doc                                 = docBuilder.newDocument();

        // TrainingCenterDatabase element
        tcxElement = doc.createElement("TrainingCenterDatabase");
        doc.appendChild(tcxElement);
        
        addHeader(doc, tcxElement, creator, track);
    }    

    /**
     * Add the GPX 1.0 header
     * @param doc The XML document
     * @param tcxElement The GPX element
     * @param creator
     * @param track
     */
    private void addHeader(Document doc, Element tcxElement, String creator, Activity track)
    {
        Attr        attr;

/*
        attr = doc.createAttribute("creator");        
        if (garminGpxExtensions && track.hasHeightValues())
        {
            creator = creator + " with barometer";  //add "with barometer" string to ensure that Strava use elevation data
        }
        attr.setValue(creator);
        tcxElement.setAttributeNode(attr);

        // GPX version
        attr = doc.createAttribute("version");
        attr.setValue("1.0");
        tcxElement.setAttributeNode(attr);
*/
        // TCX namespace
        attr = doc.createAttribute("xmlns");
        attr.setValue("http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2");
        tcxElement.setAttributeNode(attr);

        // XMLSchema namespace
        attr = doc.createAttribute("xmlns:xsi");
        attr.setValue("http://www.w3.org/2001/XMLSchema-instance");
        tcxElement.setAttributeNode(attr);

         // Schema locations
        attr = doc.createAttribute("xsi:schemaLocation");
        attr.setValue("http://www.garmin.com/xmlschemas/ActivityExtension/v2 "+
                      "https://www8.garmin.com/xmlschemas/ActivityExtensionv2.xsd "+
                      "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 " +
                      "https://www8.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd");
        tcxElement.setAttributeNode(attr);
    }


    /**
     * This method adds the track segments to the track.
     * @param doc XML document
     * @param tcxElement The GPX element
     * @param track The track to write
     * @param appName Application name
     */
    private void addTrack(Document doc, Element tcxElement, Activity track, String appName)
    {
        int     i;
        int     numberOfSegments;
        Element activitiesElement;
        Element activityElement;
        Element lapElement;
        Element segmentElement;
        Element element;
        Attr    attr;
        
        ActivitySegment segment;
        String          trackName;
        String          trackDescription;
        
        numberOfSegments=track.getNumberOfSegments();

        trackName           ="Track - "+track.getActivityDescription();
        trackDescription    ="Created by: "+appName+". Logged by: \'"+track.getDeviceName()+"\' (serial: "+track.getDeviceSerialNumber()+"). "+
                             "Logged as: "+track.getActivityDescription()+".";
        if (track.isSmoothed())
        {
            trackDescription+=" Smoothing ("+String.format("%.1f", track.getTrackSmoothingQFactor())+") applied.";
        }
        
        // The activities element
        activitiesElement = doc.createElement("Activities");
        tcxElement.appendChild(activitiesElement);

        activityElement = doc.createElement("Activity");
        // set obligatory 'Sports' to element
        attr = doc.createAttribute("Sport");
        switch(track.getActivityType())
        {
            case TtbinFileDefinition.ACTIVITY_RUNNING:
            case TtbinFileDefinition.ACTIVITY_TRAILRUNNING:
                attr.setValue("Running");
                break;
            case TtbinFileDefinition.ACTIVITY_CYCLING:
                attr.setValue("Biking");
                break;
            default:
                attr.setValue("Other");
                break;                
        }
        activityElement.setAttributeNode(attr);
        activitiesElement.appendChild(activityElement);
        element=doc.createElement("Id");
        element.appendChild(doc.createTextNode(track.getStartDateTime().format("YYYY-MM-DDThh:mm:ssZ")));
        activityElement.appendChild(element);
        
        // Add the track segments.
        i=0;
        while (i<numberOfSegments)
        {
            segment=track.getSegment(i);
            lapElement=doc.createElement("Lap");
            activityElement.appendChild(lapElement);
            DateTime startTime=segment.getStartTime();
            String startTimeString   =startTime.format("YYYY-MM-DD")+"T"+
                                     startTime.format("hh:mm:ss")+"Z";
            lapElement.setAttribute("StartTime", startTimeString);

            Element totalTimeElement=doc.createElement("TotalTimeSeconds");
            long lapTime=segment.getStartTime().numSecondsFrom(segment.getEndTime());
            totalTimeElement.appendChild(doc.createTextNode(String.format("%d", lapTime)));
            lapElement.appendChild(totalTimeElement);
            
            Element distanceElement=doc.createElement("DistanceMeters");
            distanceElement.appendChild(doc.createTextNode(String.format("%.1f", segment.getDistance()*1000)));
            lapElement.appendChild(distanceElement);

            // For XSD compliancy
            Element caloriesElement=doc.createElement("Calories");
            caloriesElement.appendChild(doc.createTextNode(String.format("%d", 0)));
            lapElement.appendChild(caloriesElement);            
            Element intensityElement=doc.createElement("Intensity");
            intensityElement.appendChild(doc.createTextNode("Active"));
            lapElement.appendChild(intensityElement);            
            Element triggerElement=doc.createElement("TriggerMethod");
            triggerElement.appendChild(doc.createTextNode("Manual"));
            lapElement.appendChild(triggerElement);            

            
            Element trackElement=doc.createElement("Track");
            lapElement.appendChild(trackElement);
            
            for(ActivityRecord rec : segment.getRecords())
            {
                DateTime dateTime      =rec.getDateTime();
                double latitude        =rec.getLatitude();
                double longitude       =rec.getLongitude();

                if ((dateTime!=null) && 
                        (latitude!=ActivityRecord.INVALID) && (longitude!=ActivityRecord.INVALID) && 
                        (latitude!=0.0) && (longitude!=0.0))
                {             
                    double heading         =rec.getHeading();
                    double calories        =rec.getCalories();
                    double ascend          =rec.getCumulativeAscend();
                    double descend         =rec.getCumulativeDecend();
                    double distance        =rec.getDistance();
                    double elevation       =rec.getDerivedElevation();
                    int    heartRate       =rec.getHeartRate();
                    int    temperature     =rec.getTemperature();
                    double speed           =rec.getSpeed();

                    Element pointElement=doc.createElement("Trackpoint");
                    trackElement.appendChild(pointElement);

                    Element timeElement     = doc.createElement("Time");
                    String dateTimeString   =dateTime.format("YYYY-MM-DD")+"T"+
                                             dateTime.format("hh:mm:ss")+"Z";
                    timeElement.appendChild(doc.createTextNode(dateTimeString));
                    pointElement.appendChild(timeElement);

                    Element positionElement=doc.createElement("Position");
                    pointElement.appendChild(positionElement);
                    element=doc.createElement("LatitudeDegrees");
                    element.appendChild(doc.createTextNode((String.format("%.7f", latitude))));
                    positionElement.appendChild(element);
                    element=doc.createElement("LongitudeDegrees");
                    element.appendChild(doc.createTextNode((String.format("%.7f", longitude))));
                    positionElement.appendChild(element);
                    
                    // The elevation.
                    if (elevation!=ActivityRecord.INVALID)
                    {
                        element    = doc.createElement("AltitudeMeters");
                        element.appendChild(doc.createTextNode(String.format("%.1f", elevation)));
                        pointElement.appendChild(element);
                    }                    
                    
                    // The Heartrate
                    if ((heartRate!=ActivityRecord.INVALID) && (heartRate>0))
                    {
                        Element hrElement    = doc.createElement("HeartRateBpm");
                        pointElement.appendChild(hrElement);
                        Element valueElement =doc.createElement("Value");
                        valueElement.appendChild(doc.createTextNode(String.valueOf(heartRate)));
                        hrElement.appendChild(valueElement);
                    }

                    if (distance!=ActivityRecord.INVALID)
                    {
                        element    = doc.createElement("DistanceMeters");
                        element.appendChild(doc.createTextNode(String.format("%.1f", distance)));
                        pointElement.appendChild(element);
                    }                    
                }
            }

            i++;
        }
    }
    

    /**
     * This method writes the GPX file
     * @param fileName Name of the file
     * @throws javax.xml.transform.TransformerException
     */
    private void writeTcxDocument(Writer writer) throws TransformerException
    {
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
    }
    
    /* ************************************************************************\
     * The interface functions
     * ************************************************************************/
    /**
     * Write the track to a TCX file
     * @param writer Writer to use for writing the file
     * @param track Track to write to the GPX file
     * @param appName Application description
     */
    public void writeTrackToFile(Writer writer, Activity track, String appName)
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
            createTcxDocument(appName, track);

            addTrack(doc, tcxElement, track, appName);

            // write the content into xml file
            writeTcxDocument(writer);

            DebugLogger.info("TcxWriter says: 'File saved!'");
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
