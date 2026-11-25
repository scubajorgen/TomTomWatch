/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.ttbin;

import hirondelle.date4j.DateTime;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private boolean             useHeartRateRecords;    // Activity without GPS: write heart rate records

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
     * Formats the UTC date time as TCX/XSD dateTime string
     * @param dateTime The date time, UTC
     * @return The formatted string, like 2025-10-03T11:55:29Z
     */
    private String formatUtcDateTime(DateTime dateTime)
    {
        return dateTime.format("YYYY-MM-DD")+"T"+dateTime.format("hh:mm:ss")+"Z";
    }

    /**
     * Returns the UTC date time of the first record in the list that has a
     * date time stamp
     * @param records The records to search
     * @return The date time or null if none found
     */
    private DateTime getFirstRecordDateTime(List<ActivityRecord> records)
    {
        DateTime    dateTime;

        dateTime=null;
        for (ActivityRecord rec : records)
        {
            if (rec.getDateTime()!=null)
            {
                dateTime=rec.getDateTime();
                break;
            }
        }
        return dateTime;
    }

    /**
     * Returns the UTC date time of the first record in the activity that has
     * a date time stamp. The activity start time in the header is local time,
     * hence the first record is used.
     * @param track The activity
     * @return The date time or null if no record found
     */
    private DateTime getFirstRecordDateTime(Activity track)
    {
        int         i;
        DateTime    dateTime;

        dateTime=null;
        i=0;
        while (i<track.getNumberOfSegments() && dateTime==null)
        {
            dateTime=getFirstRecordDateTime(getRecords(track.getSegment(i)));
            i++;
        }
        return dateTime;
    }

    /**
     * Returns the records of the segment to use: the GPS based records or,
     * for activities without GPS, the heart rate records
     * @param segment The segment
     * @return The records
     */
    private List<ActivityRecord> getRecords(ActivitySegment segment)
    {
        List<ActivityRecord>    records;

        if (useHeartRateRecords)
        {
            records=segment.getHeartRateRecords();
        }
        else
        {
            records=segment.getRecords();
        }
        return records;
    }

    /**
     * Checks whether the activity contains records with a valid position
     * @param track The activity
     * @return True if the activity contains positions, false if not
     *         (indoor cycling, treadmill)
     */
    private boolean hasPositions(Activity track)
    {
        int         i;
        boolean     found;

        found=false;
        i=0;
        while (i<track.getNumberOfSegments() && !found)
        {
            found=!getPositionRecords(track.getSegment(i)).isEmpty();
            i++;
        }
        return found;
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
        Element segmentElement;
        Element element;
        Attr    attr;

        DateTime        startTime;
        String          trackName;
        String          trackDescription;
        
        numberOfSegments=track.getNumberOfSegments();

        // Activities without GPS: write the heart rate records without position
        useHeartRateRecords=!hasPositions(track);

        trackName          ="Track - "+track.getActivityDescription();
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
            case TtbinFileDefinition.ACTIVITY_TREADMILL:
                attr.setValue("Running");
                break;
            case TtbinFileDefinition.ACTIVITY_CYCLING:
            case TtbinFileDefinition.ACTIVITY_INDOORCYCLING:
                attr.setValue("Biking");
                break;
            default:
                attr.setValue("Other");
                break;                
        }
        activityElement.setAttributeNode(attr);
        activitiesElement.appendChild(activityElement);
        // The Id is the UTC time of the first record. The header start time
        // is local time and cannot be used.
        element=doc.createElement("Id");
        startTime=getFirstRecordDateTime(track);
        if (startTime!=null)
        {
            element.appendChild(doc.createTextNode(formatUtcDateTime(startTime)));
        }
        else
        {
            element.appendChild(doc.createTextNode(track.getStartDateTime().format("YYYY-MM-DDThh:mm:ssZ")));
        }
        activityElement.appendChild(element);

        // Add the laps
        if (track.getLaps().isEmpty())
        {
            // No manual laps marked: one lap per segment
            i=0;
            while (i<numberOfSegments)
            {
                addSegmentLap(doc, activityElement, track.getSegment(i));
                i++;
            }
        }
        else
        {
            // Manual laps marked: split the segments at the lap marks
            addManualLaps(doc, activityElement, track);
        }
    }

    /**
     * Returns the records of the segment that are written as Trackpoint:
     * the records with a time stamp and a valid position or, for activities
     * without GPS, the heart rate records with a time stamp
     * @param segment The segment
     * @return The list of records
     */
    private List<ActivityRecord> getTrackRecords(ActivitySegment segment)
    {
        List<ActivityRecord>    records;

        if (useHeartRateRecords)
        {
            records=new ArrayList<>();
            for (ActivityRecord rec : segment.getHeartRateRecords())
            {
                if (rec.getDateTime()!=null)
                {
                    records.add(rec);
                }
            }
        }
        else
        {
            records=getPositionRecords(segment);
        }
        return records;
    }

    /**
     * Returns the records of the segment with a time stamp and a valid position
     * @param segment The segment
     * @return The list of records
     */
    private List<ActivityRecord> getPositionRecords(ActivitySegment segment)
    {
        List<ActivityRecord>    records;
        DateTime                dateTime;
        double                  latitude;
        double                  longitude;

        records=new ArrayList<>();
        for (ActivityRecord rec : segment.getRecords())
        {
            dateTime        =rec.getDateTime();
            latitude        =rec.getLatitude();
            longitude       =rec.getLongitude();
            if ((dateTime!=null) &&
                    (latitude!=ActivityRecord.INVALID) && (longitude!=ActivityRecord.INVALID) &&
                    (latitude!=0.0) && (longitude!=0.0))
            {
                records.add(rec);
            }
        }
        return records;
    }

    /**
     * Adds a lap for the entire segment. This is used when no manual laps
     * were marked
     * @param doc XML document
     * @param activityElement The Activity element to add the Lap to
     * @param segment The segment
     */
    private void addSegmentLap(Document doc, Element activityElement, ActivitySegment segment)
    {
        Element                 lapElement;
        Element                 trackElement;
        DateTime                startTime;
        List<ActivityRecord>    records;
        long                    lapTime;

        records     =getTrackRecords(segment);
        lapElement  =doc.createElement("Lap");
        activityElement.appendChild(lapElement);

        // The lap starts at the first record of the segment (UTC). The
        // segment start time is local time.
        startTime=getFirstRecordDateTime(getRecords(segment));
        if (startTime==null)
        {
            startTime=segment.getStartTime();
        }
        lapElement.setAttribute("StartTime", formatUtcDateTime(startTime));

        lapTime=segment.getStartTime().numSecondsFrom(segment.getEndTime());
        addLapSummary(doc, lapElement, lapTime, segment.getDistance()*1000, 0, null);

        trackElement=doc.createElement("Track");
        lapElement.appendChild(trackElement);
        for(ActivityRecord rec : records)
        {
            addTrackpoint(doc, trackElement, rec);
        }
    }

    /**
     * Adds the laps based on the manual lap marks. Each segment is split
     * at the lap marks: a record belongs to the lap of the first lap mark
     * with a time equal to or later than the record time. The records after
     * the last lap mark form the last lap. Lap time, distance and calories
     * are the difference between the cumulative values of subsequent lap marks.
     * @param doc XML document
     * @param activityElement The Activity element to add the Laps to
     * @param track The activity
     */
    private void addManualLaps(Document doc, Element activityElement, Activity track)
    {
        List<ActivityLap>       laps;
        List<ActivityRecord>    records;
        List<ActivityRecord>    lapRecords;
        ActivityLap             lap;
        ActivityRecord          lastRecord;
        int                     lapIndex;
        int                     segmentIndex;
        int                     lastSegmentIndex;
        int                     numberOfSegments;
        long                    prevTime;
        double                  prevDistance;
        int                     prevCalories;
        long                    endTime;
        double                  endDistance;
        int                     endCalories;

        laps            =track.getLaps();
        numberOfSegments=track.getNumberOfSegments();

        // Find the last segment containing records
        lastSegmentIndex=-1;
        segmentIndex    =0;
        while (segmentIndex<numberOfSegments)
        {
            if (!getTrackRecords(track.getSegment(segmentIndex)).isEmpty())
            {
                lastSegmentIndex=segmentIndex;
            }
            segmentIndex++;
        }

        lapIndex        =0;
        prevTime        =0;
        prevDistance    =0.0;
        prevCalories    =0;
        segmentIndex    =0;
        while (segmentIndex<numberOfSegments)
        {
            records     =getTrackRecords(track.getSegment(segmentIndex));
            lapRecords  =new ArrayList<>();
            for (ActivityRecord rec : records)
            {
                // Close the lap(s) of the lap marks preceding this record
                while ((lapIndex<laps.size()) &&
                       ((laps.get(lapIndex).getDateTime()==null) || rec.getDateTime().gt(laps.get(lapIndex).getDateTime())))
                {
                    lap=laps.get(lapIndex);
                    if (!lapRecords.isEmpty())
                    {
                        addLap(doc, activityElement, lapRecords,
                               lap.getElapsedSeconds()-prevTime,
                               lap.getDistance()-prevDistance,
                               lap.getCalories()-prevCalories);
                        prevTime        =lap.getElapsedSeconds();
                        prevDistance    =lap.getDistance();
                        prevCalories    =lap.getCalories();
                        lapRecords      =new ArrayList<>();
                    }
                    // If no records belong to the lap, the values are added to the next lap
                    lapIndex++;
                }
                lapRecords.add(rec);
            }

            // End of segment: the remaining records form a lap
            if (!lapRecords.isEmpty())
            {
                lastRecord=lapRecords.get(lapRecords.size()-1);
                if ((lapIndex<laps.size()) && (segmentIndex==lastSegmentIndex))
                {
                    // Lap marks remaining after the last record: use the last one
                    lap         =laps.get(laps.size()-1);
                    endTime     =lap.getElapsedSeconds();
                    endDistance =lap.getDistance();
                    endCalories =lap.getCalories();
                    lapIndex    =laps.size();
                }
                else if ((segmentIndex==lastSegmentIndex) && (track.getSummaryDuration()>0))
                {
                    // Last lap of the activity: use the activity totals
                    endTime     =track.getSummaryDuration();
                    endDistance =track.getSummaryDistance();
                    endCalories =track.getSummaryCalories();
                }
                else
                {
                    // Lap interrupted by a pause: estimate the values from the records
                    endTime     =prevTime+lapRecords.get(0).getDateTime().numSecondsFrom(lastRecord.getDateTime());
                    endDistance =lastRecord.getDistance()!=ActivityRecord.INVALID ? lastRecord.getDistance() : prevDistance;
                    endCalories =lastRecord.getCalories()!=ActivityRecord.INVALID ? (int)lastRecord.getCalories() : prevCalories;
                }
                addLap(doc, activityElement, lapRecords,
                       endTime-prevTime, endDistance-prevDistance, endCalories-prevCalories);
                prevTime        =endTime;
                prevDistance    =endDistance;
                prevCalories    =endCalories;
            }
            segmentIndex++;
        }
    }

    /**
     * Adds a manual lap containing the records passed
     * @param doc XML document
     * @param activityElement The Activity element to add the Lap to
     * @param records The records in the lap; must contain at least one record
     * @param lapTime Lap duration in s
     * @param distance Lap distance in m
     * @param calories Lap calories
     */
    private void addLap(Document doc, Element activityElement, List<ActivityRecord> records,
                        long lapTime, double distance, int calories)
    {
        Element                 lapElement;
        Element                 trackElement;

        lapElement  =doc.createElement("Lap");
        activityElement.appendChild(lapElement);
        lapElement.setAttribute("StartTime", formatUtcDateTime(records.get(0).getDateTime()));

        addLapSummary(doc, lapElement, Math.max(lapTime, 0), Math.max(distance, 0.0), Math.max(calories, 0), records);

        trackElement=doc.createElement("Track");
        lapElement.appendChild(trackElement);
        for(ActivityRecord rec : records)
        {
            addTrackpoint(doc, trackElement, rec);
        }
    }

    /**
     * Adds the lap summary elements, in the order required by the XSD:
     * TotalTimeSeconds, DistanceMeters, Calories, AverageHeartRateBpm,
     * MaximumHeartRateBpm, Intensity, TriggerMethod
     * @param doc XML document
     * @param lapElement The Lap element
     * @param lapTime Lap duration in s
     * @param distance Lap distance in m
     * @param calories Lap calories
     * @param records Records for calculating average and maximum heart rate;
     *                null if heart rate must not be written
     */
    private void addLapSummary(Document doc, Element lapElement, long lapTime, double distance, int calories,
                               List<ActivityRecord> records)
    {
        Element     element;
        int         heartRate;
        int         maxHeartRate;
        long        sumHeartRate;
        int         count;

        element=doc.createElement("TotalTimeSeconds");
        element.appendChild(doc.createTextNode(String.format("%d", lapTime)));
        lapElement.appendChild(element);

        element=doc.createElement("DistanceMeters");
        element.appendChild(doc.createTextNode(String.format(Locale.US, "%.1f", distance)));
        lapElement.appendChild(element);

        element=doc.createElement("Calories");
        element.appendChild(doc.createTextNode(String.format("%d", Math.min(calories, 65535))));
        lapElement.appendChild(element);

        if (records!=null)
        {
            maxHeartRate    =0;
            sumHeartRate    =0;
            count           =0;
            for (ActivityRecord rec : records)
            {
                heartRate=rec.getHeartRate();
                if ((heartRate!=ActivityRecord.INVALID) && (heartRate>0))
                {
                    sumHeartRate+=heartRate;
                    maxHeartRate=Math.max(maxHeartRate, heartRate);
                    count++;
                }
            }
            if (count>0)
            {
                addHeartRateElement(doc, lapElement, "AverageHeartRateBpm", (int)Math.round((double)sumHeartRate/count));
                addHeartRateElement(doc, lapElement, "MaximumHeartRateBpm", maxHeartRate);
            }
        }

        element=doc.createElement("Intensity");
        element.appendChild(doc.createTextNode("Active"));
        lapElement.appendChild(element);
        element=doc.createElement("TriggerMethod");
        element.appendChild(doc.createTextNode("Manual"));
        lapElement.appendChild(element);
    }

    /**
     * Adds a heart rate element containing a Value element
     * @param doc XML document
     * @param parent Parent element
     * @param name Name of the element, like HeartRateBpm
     * @param heartRate The heart rate in bpm
     */
    private void addHeartRateElement(Document doc, Element parent, String name, int heartRate)
    {
        Element hrElement;
        Element valueElement;

        hrElement    =doc.createElement(name);
        parent.appendChild(hrElement);
        valueElement =doc.createElement("Value");
        valueElement.appendChild(doc.createTextNode(String.valueOf(heartRate)));
        hrElement.appendChild(valueElement);
    }

    /**
     * Adds the record as Trackpoint to the Track element
     * @param doc XML document
     * @param trackElement The Track element
     * @param rec The record to add
     */
    private void addTrackpoint(Document doc, Element trackElement, ActivityRecord rec)
    {
        Element     pointElement;
        Element     positionElement;
        Element     element;
        DateTime    dateTime;
        double      latitude;
        double      longitude;
        double      distance;
        double      elevation;
        int         heartRate;

        dateTime        =rec.getDateTime();
        latitude        =rec.getLatitude();
        longitude       =rec.getLongitude();
        distance        =rec.getDistance();
        elevation       =rec.getDerivedElevation();
        heartRate       =rec.getHeartRate();

        pointElement=doc.createElement("Trackpoint");
        trackElement.appendChild(pointElement);

        element     = doc.createElement("Time");
        element.appendChild(doc.createTextNode(formatUtcDateTime(dateTime)));
        pointElement.appendChild(element);

        if ((latitude!=ActivityRecord.INVALID) && (longitude!=ActivityRecord.INVALID))
        {
            positionElement=doc.createElement("Position");
            pointElement.appendChild(positionElement);
            element=doc.createElement("LatitudeDegrees");
            element.appendChild(doc.createTextNode((String.format("%.7f", latitude))));
            positionElement.appendChild(element);
            element=doc.createElement("LongitudeDegrees");
            element.appendChild(doc.createTextNode((String.format("%.7f", longitude))));
            positionElement.appendChild(element);
        }

        // The elevation.
        if (elevation!=ActivityRecord.INVALID)
        {
            element    = doc.createElement("AltitudeMeters");
            element.appendChild(doc.createTextNode(String.format("%.1f", elevation)));
            pointElement.appendChild(element);
        }

        if (distance!=ActivityRecord.INVALID)
        {
            element    = doc.createElement("DistanceMeters");
            element.appendChild(doc.createTextNode(String.format("%.1f", distance)));
            pointElement.appendChild(element);
        }

        // The Heartrate
        if ((heartRate!=ActivityRecord.INVALID) && (heartRate>0))
        {
            addHeartRateElement(doc, pointElement, "HeartRateBpm", heartRate);
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
