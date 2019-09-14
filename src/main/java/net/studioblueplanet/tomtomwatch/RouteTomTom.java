/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import hirondelle.date4j.DateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.TimeZone;
import net.studioblueplanet.logger.DebugLogger;
import net.studioblueplanet.generics.ToolBox;

/**
 * This class represents the route as defined by the TomTom Watch for
 * track planning.
 * It extends the RouteLog with functionality to encode and decode the
 * Route to resp. from the serialized protobuf format as used by the TomTom
 * watch.
 * @author jorgen.van.der.velde
 */
public class RouteTomTom extends Route
{
    // Route files encode by TomTom Mysports appear to reduce the number of
    // points to 500 if more than 500 points are present in the GPX file
    private static final int MAX_ROUTEPOINTS=500;
    
    /**
     * Private constructor
     */
    public RouteTomTom()
    {
        super();
    }
    
    

    /**
     * Return the current time encoded according to the TomTom format in the 
     * route files (emperically found).
     * The time ticks are 3.45559 ticks per second. The apparent start
     * of the time is 2016-03-07 06:29:57. Don't ask why...
     * @return The time as byte array
     */
    private byte[] getTimeBytes()
    {
        DateTime    now;
        DateTime    start;
        long        diff;
        byte[]      bytes;
        
        start=new DateTime("2016-03-07 06:29:57");
        
        now=DateTime.now(TimeZone.getTimeZone("UTC"));
        
        diff=start.numSecondsFrom(now);
        
        diff=(long)((double)diff*3.45559);
        
        bytes=new byte[16];
        ToolBox.writeUnsignedLong(bytes, diff, 8, 8, false);
        
        return bytes;
    }
    
    /*############################################################################################*\
     * The Protobuf methods     
    \*############################################################################################*/    
    /**
     * Build the indicated protobuf message: 
     * LatLon: Enclosing message for a lat or lon value
     * @return The message
     */
    private RouteProto.LatLon buildLatLon(int latLonValue)
    {
        RouteProto.LatLon               value;
        RouteProto.LatLon.Builder       valueBuilder;

        valueBuilder=RouteProto.LatLon.newBuilder();

        valueBuilder.setValue(latLonValue);
        value=valueBuilder.build();       

        return value;
    }
    
    /**
     * Build the indicated protobuf message: 
     * Coordinate: Just a lat/lon pair
     * @return The message
     */
   private RouteProto.Coordinate buildCoordinate(int lat, int lon)
   {
       RouteProto.Coordinate           coordinate;
       RouteProto.Coordinate.Builder   coordinateBuilder;

       
       coordinateBuilder=RouteProto.Coordinate.newBuilder();

       coordinateBuilder.setLat(this.buildLatLon(lat));
       coordinateBuilder.setLon(this.buildLatLon(lon));

       coordinate=coordinateBuilder.build();
       
       return coordinate;
   }

    /**
     * Build the indicated protobuf message: 
     * CoordinateData: Enclosing message for a Coordinate value
     * @return The message
     */

   private RouteProto.CoordinateData buildCoordinateData(int lat, int lon)
   {
        RouteProto.CoordinateData          message;
        RouteProto.CoordinateData.Builder         builder;

        
        builder                 =RouteProto.CoordinateData.newBuilder();
        
        builder.setCoordinate(this.buildCoordinate(lat, lon));
        
        message=builder.build();
        
        return message;
       
   }
   
    /**
     * Build the indicated protobuf message: 
     * StartCoordinate: A coordinate with an index value. Used to indicate
     * the first and last coordinate of the first track segment.
     * @return The message
     */
   private RouteProto.StartCoordinate buildStartCoordinate(int lat, int lon, int index)
   {
       RouteProto.StartCoordinate              message;
       RouteProto.StartCoordinate.Builder      builder;
       
       builder=RouteProto.StartCoordinate.newBuilder();
       
       builder.setCoordinate(this.buildCoordinate(lat, lon));
       builder.setIndex(index);
       message=builder.build();
       
       return message;
   }
    
    /**
     * Build the indicated protobuf message: 
     * Header: The start of the file. Contains two fixed values. Probably 
     * versions or so...
     * @return The message
     */
    private RouteProto.RootContainer buildRootContainer()
    {
        RouteProto.RootContainer           rootContainer;
        RouteProto.RootContainer.Builder   rootContainerBuilder;
        RouteProto.MetaData                metadata;
        RouteProto.MetaData.Builder        metadataBuilder;

        metadataBuilder     =RouteProto.MetaData.newBuilder();
        
        // Set the values: appear to be the same always...
        metadataBuilder.setUnknown1(0x1234DAEB);
        metadataBuilder.setUnknown2(0x00070100);
        
        metadata            =metadataBuilder.build();

        rootContainerBuilder=RouteProto.RootContainer.newBuilder();
        rootContainerBuilder.setMetaData(metadata);
        rootContainer=rootContainerBuilder.build();
        
        return rootContainer;
    }
    
    
    /**
     * Build the indicated protobuf message: 
     * BoundingBox: Defines the lower right and upper left corner of the rout
     * @return The message
     */
    private RouteProto.BoundingBox   buildBoundingBox()
    {
        RouteProto.BoundingBox             message;
        RouteProto.BoundingBox.Builder     builder;
        int                     minLat;
        int                     maxLat;
        int                     minLon;
        int                     maxLon;
       
        
        builder=RouteProto.BoundingBox.newBuilder();
        
        minLat=this.getMinLatitude();
        maxLat=this.getMaxLatitude();
        minLon=this.getMinLongitude();
        maxLon=this.getMaxLongitude();
        
        builder.setLatDown (this.buildLatLon(minLat));
        builder.setLonLeft (this.buildLatLon(minLon));
        builder.setLatUp   (this.buildLatLon(maxLat));
        builder.setLonRight(this.buildLatLon(maxLon));

        message=builder.build();
        return message;
        
    }
    
    /**
     * Build the indicated protobuf message: 
     * TrackMetaData: Some data defining the route
     * @return The message
     */
    private RouteProto.TrackMetaData buildTrackMetadata()
    {
        RouteProto.TrackMetaData           metaData;
        RouteProto.TrackMetaData.Builder   builder;
        byte[]                  bytes;
        
        builder=RouteProto.TrackMetaData.newBuilder();
        
        // Name to display on the watch
        builder.setName(routeName);
        
        // Bouding box
        builder.setBox(buildBoundingBox());
        
        // Some funny timestamp of uploading to TomTom; here: upload to watch ;-)
        bytes=this.getTimeBytes();
        builder.setTime(ByteString.copyFrom(bytes));

        metaData=builder.build();
        
        
        
        return metaData;
    }
    
    
    /**
     * Build the indicated protobuf message: 
     * Segment: Route segment
     * @param routeSegment The route segment to encode
     * @param numberOfPoints The max number of points that may end up in the segment.
     * @return The message
     */
    private RouteProto.Segment buildSegment(RouteSegment routeSegment, int numberOfPoints)
    {
        RouteProto.Segment                  message;
        RouteProto.Segment.Builder          builder;
        Iterator<RoutePoint>                it;
        RoutePoint                          routePoint;
        ArrayList<RoutePoint>               points;
        float                               increment;
        int                                 index;
        int                                 i;

        
        
        builder                 =RouteProto.Segment.newBuilder();

        points=routeSegment.getRoutePoints();
        
        if (points.size()>numberOfPoints)
        {
            increment=(float)(points.size()-1)/(float)(numberOfPoints-1);
        }
        else
        {
            numberOfPoints=points.size();
            increment=1.0f;
        }
        
        builder.setNumberOfCoordinates(numberOfPoints);
        
        
        i=0;
        while (i<numberOfPoints)
        {
            index=Math.round(i*increment);
            
            routePoint=points.get(index);
            
            builder.addData(this.buildCoordinateData(routePoint.getLatitudeInt(), routePoint.getLongitudeInt()));

            i++;
        }        
        message=builder.build();
        
        return message;
        
    }
    
    /**
     * Build the indicated protobuf message: 
     * SegmentData: Message enclosing the segments. Contains the number of segments.
     * @return The message
     */
    private RouteProto.SegmentData buildSegmentData()
    {
        RouteProto.SegmentData              message;
        RouteProto.SegmentData.Builder      builder;
        Iterator<RouteSegment>              it;
        RouteSegment                        routeSegment;
        int                                 numberOfPoints;
        float                               increment;
        int                                 segmentCount;
        
        
        // Total number of points in the route
        numberOfPoints=this.getNumberOfPoints();
        
        // If the number exceeds the maximum, we have to skip points
        // Calculate the increment
        if (numberOfPoints<MAX_ROUTEPOINTS)
        {
            increment=1.0f;
        }
        else
        {
            increment=(float)numberOfPoints/(float)MAX_ROUTEPOINTS;
        }
        
        
        builder                 =RouteProto.SegmentData.newBuilder();

        
        segmentCount            =0;
        it=segments.iterator();
        while (it.hasNext())
        {
            routeSegment=it.next();
            
            numberOfPoints=Math.round((float)routeSegment.getNumberOfPoints()/increment);
            
            if (numberOfPoints>0)
            {
                builder.addData(this.buildSegment(routeSegment, numberOfPoints));
                segmentCount++;
            }
            else
            {
                DebugLogger.error("Skipping segment in route: to few points remaining after compression");
            }
        }        
        builder.setNumberOfSegments(segmentCount);

        message=builder.build();
        
        return message;
    }
    
    
    /**
     * Build the indicated protobuf message: 
     * TrackLevel2: the actual route data: metadata and segments
     * @return The message
     */
    private RouteProto.TrackLevel2 buildTrack()
    {
        RouteProto.TrackLevel2             message;
        RouteProto.TrackLevel2.Builder     builder;
        RoutePoint              first;
        RoutePoint              last;
        RouteSegment            segment;
        Iterator<RouteSegment>  it;
        
        builder=RouteProto.TrackLevel2.newBuilder();
        
        // Set the TrackMetadata (name, bounding box, timestamp)
        builder.setMetadata(this.buildTrackMetadata());
        
        // Set the first point of the first segment with index 1
        first=this.getFirstSegment().getFirstPoint();
        builder.addCoordinate(this.buildStartCoordinate(first.getLatitudeInt(), first.getLongitudeInt(), 1));

        // Set the last point of the first segment with index 2
        // Unclear why other segments are not processed...
        last=this.getFirstSegment().getLastPoint();
        builder.addCoordinate(this.buildStartCoordinate(last.getLatitudeInt() , last.getLongitudeInt(), 2));

        builder.setData(this.buildSegmentData());
        
        message=builder.build();

        
        return message;
    }
            
    
    /**
     * Build the indicated protobuf message: The outer track content levels
     * For some reason the track data is put three levels deep. Two 
     * levels don't add anything...
     * @return The message
     */
    private RouteProto.RootContainer buildTrackLevels()
    {
        RouteProto.RootContainer           rootContainer;
        RouteProto.RootContainer.Builder   rootContainerBuilder;

        RouteProto.TrackLevel1             level1;
        RouteProto.TrackLevel1.Builder     level1Builder;
        RouteProto.TrackLevel2             level2;
        
        level1Builder   =RouteProto.TrackLevel1.newBuilder();
        
        level2          =this.buildTrack();
        level1Builder.setLevel2(level2);
        level1          =level1Builder.build();

        rootContainerBuilder       =RouteProto.RootContainer.newBuilder();
        rootContainerBuilder.setLevel1(level1);
        rootContainer=rootContainerBuilder.build();
        
        return rootContainer;




/*
        RouteProto.TrackLevel0             level0;
        RouteProto.TrackLevel0.Builder     level0Builder;
        RouteProto.TrackLevel1             level1;
        RouteProto.TrackLevel1.Builder     level1Builder;
        RouteProto.TrackLevel2             level2;
        
        level1Builder   =RouteProto.TrackLevel1.newBuilder();
        
        level2          =this.buildTrack();
        level1Builder.setLevel2(level2);
        level1          =level1Builder.build();
        
        level0Builder   =RouteProto.TrackLevel0.newBuilder();
        level0Builder.setLevel1(level1);
        level0          =level0Builder.build();
        
        return level0;
*/        
    }
    
    
    
    /**
     * This method returns the protobuf serialized data representing the 
     * route. This data can be send to the watch.
     * @return Byte array to be send to the watch as USB File
     */
    public byte[] getTomTomRouteData()
    {
        byte[]                  bytes;
        RouteProto.Root                    root;
        RouteProto.Root.Builder            rootBuilder;
        
        bytes=null;

        rootBuilder         =RouteProto.Root.newBuilder();
        
        rootBuilder.addContainer(buildRootContainer());
        
        rootBuilder.addContainer(this.buildTrackLevels());

        root                =rootBuilder.build();

        // Generate the bytes
        bytes=root.toByteArray();
        
        return bytes;
    }
    

    /**
     * This method accepts the protobuf data from the watch and loads the route
     * from it.
     * @param data The protobuf encoded data
     * @return True if an error occurred, false if all went ok
     */
    public boolean loadLogFromTomTomRouteData(byte[] data)
    {
        RouteProto.Root                     root;
        RouteProto.RootContainer            container;
        List<RouteProto.RootContainer>      containers;
        Iterator<RouteProto.RootContainer>  containerIt;
        RouteProto.TrackLevel1              level1;
        RouteProto.TrackLevel2              level2;
        RouteProto.TrackMetaData            metadata;
        RouteProto.SegmentData              segmentData;
        List<RouteProto.Segment>            segments;
        Iterator<RouteProto.Segment>        segmentIt;
        RouteProto.Segment                  segment;
        List<RouteProto.CoordinateData>     coordDatas;
        RouteProto.CoordinateData           coordData;
        Iterator<RouteProto.CoordinateData> coordDataIt;
        RouteProto.Coordinate               coordinate;
        double                              lat;
        double                              lon;
        boolean                             exit;
        
        
        RouteSegment                        routeSegment;
        RoutePoint                          routePoint;
        
        
        boolean                             error;
        int                                 numberOfSegments;
        int                                 numberOfPoints;
        
        clear();
        error=false;
        try
        {
            root                =RouteProto.Root.parseFrom(data);
            
            exit                =false;
                
            
            containers                 =root.getContainerList();
            containerIt                =containers.iterator();
            while (containerIt.hasNext() && !exit)
            {
                container=containerIt.next();

                if (container.hasLevel1())
                {                
                    level1=container.getLevel1();
                    level2              =level1.getLevel2();
                    metadata            =level2.getMetadata();
                    this.routeName      =metadata.getName();

                    segmentData         =level2.getData();
                    numberOfSegments    =segmentData.getNumberOfSegments();
                    segments            =segmentData.getDataList();

                    // Consistency check
                    if (segments.size()!=numberOfSegments)
                    {
                        DebugLogger.error("Inconsistent protobuf data: number of segments incorrect");
                    }

                    segmentIt           =segments.iterator();
                    while (segmentIt.hasNext())
                    {
                        routeSegment=this.appendRouteSegment();

                        segment         =segmentIt.next();
                        numberOfPoints  =segment.getNumberOfCoordinates();
                        coordDatas      =segment.getDataList();

                        // Consistency check
                        if (coordDatas.size()!=numberOfPoints)
                        {
                            DebugLogger.error("Inconsistent protobuf data: number of points incorrect");
                        }


                        coordDataIt     =coordDatas.iterator();
                        while (coordDataIt.hasNext())
                        {
                            coordData   =coordDataIt.next();
                            coordinate  =coordData.getCoordinate();
                            lat         =(double)coordinate.getLat().getValue()/1e7;
                            lon         =(double)coordinate.getLon().getValue()/1e7;
                            routePoint  =new RoutePoint(lat, lon);
                            routeSegment.appendRoutePoint(routePoint);
                        }

                    }
                    exit=true;
                }
            }
                    
        }
        catch (InvalidProtocolBufferException e)
        {
            DebugLogger.error("Error parsing route file: "+e.getMessage());
            error               =true;
        }
        
        return error;
        
    }
    

    
}
