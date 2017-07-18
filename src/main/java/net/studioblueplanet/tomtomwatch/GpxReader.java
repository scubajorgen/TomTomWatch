/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.logger.DebugLogger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

/**
 *
 * @author UserXP
 */
public class GpxReader
{
    private static GpxReader theInstance=null;

    /**
     * Private constructor
     */
    private GpxReader()
    {

    }

    /**
     * This method returns the one and only instance of this singleton class
     * @return The instance
     */
    public static GpxReader getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new GpxReader();
        }
        return theInstance;
    }

    /**
     * This method returns an element
     * @param parent Parent from which the element is obtained
     * @param elementName Name of the element
     * @return The element or null if not found
     */
    private Element getChildElement(Element parent, String elementName)
    {
        Element     element;
        NodeList    nodeList;

        element=null;
        nodeList=parent.getElementsByTagName(elementName);

        if (nodeList.getLength()==1)
        {
            if (nodeList.item(0).getNodeType()==Node.ELEMENT_NODE)
            {
                element=(Element)nodeList.item(0);
            }
        }
        else
        {
            // We expect 0 or 1 elements in th list
            if (nodeList.getLength()>1)
            {
                DebugLogger.error("Number of nodes <"+ elementName+
                                  "> tags in GPX file not equal to one as expected...");
            }
        }

        return element;
    }

    /**
     * This method returns the value of a child element
     * @param parent The parent element
     * @param tagName The name of the child element
     * @return The value of the child element
     */
    private String getChildElementValue(Element parent, String tagName)
    {
        Element  element;
        String   value;
        NodeList list;
        Node     node;

        element=this.getChildElement(parent, tagName);

        if (element!=null)
        {
            list=element.getChildNodes();
            node=list.item(0);
            if (node!=null)
            {
                value=node.getNodeValue();
            }
            else
            {
                value="";
            }
        }
        else
        {
            value="";
            DebugLogger.error("Tag <"+tagName+"> not found in GPX file");
        }
        return value;
    }

    
    
    private void parseTrack(Route log, Element trackElement)
    {
        NodeList                segmentList;
        NodeList                nList;
        Element                 eElement;
        Node                    nNode;
        int                     i;        
        int                     j;
        double                  longitude;
        double                  latitude;
        RouteSegment            routeSegment;
        RoutePoint              routePoint;
        
        // If succeeded, get the segments from the track
        
        segmentList = trackElement.getElementsByTagName("trkseg");

        if ((segmentList!=null) && (segmentList.getLength()>0))
        {
            // Parse trkseg segments
            j=0;
            while (j<segmentList.getLength())
            {
                // If succeeded, get the points from the route
                nList = segmentList.item(j).getChildNodes();

                if ((nList!=null) && (nList.getLength()>0))
                {
                    routeSegment=log.appendRouteSegment();
                    i=0;
                    while (i<nList.getLength())
                    {
                       nNode = nList.item(i);
                       if ((nNode.getNodeType() == Node.ELEMENT_NODE) &&
                           (nNode.getNodeName() == "trkpt"))    
                       {
                          eElement = (Element) nNode;

                          longitude =Double.parseDouble(eElement.getAttribute("lon"));
                          latitude  =Double.parseDouble(eElement.getAttribute("lat"));


                          routePoint=new RoutePoint(latitude, longitude);
                          routeSegment.appendRoutePoint(routePoint);
                       }
                       i++;
                    }
                }
                else
                {
                    DebugLogger.error("No <trkpt> data in "+(j+1)+"th <trkseg> in GPX file");
                }        
                j++;
            }
        }
        else
        {
            DebugLogger.error("No <rte> data in GPX file");
        }        
        
    }
    
    private void parseRoute(Route log, Element routeElement)
    {
        NodeList                nList;
        Element                 eElement;
        Node                    nNode;
        int                     i;        
        double                  longitude;
        double                  latitude;
        RouteSegment            routeSegment;
        RoutePoint              routePoint;
        
        // If succeeded, get the points from the route
        nList = routeElement.getElementsByTagName("rtept");
        
        if ((nList!=null) && (nList.getLength()>0))
        {
            routeSegment=log.appendRouteSegment();
            i=0;
            while (i<nList.getLength())
            {
               nNode = nList.item(i);
               if (nNode.getNodeType() == Node.ELEMENT_NODE)
               {
                  eElement = (Element) nNode;

                  longitude =Double.parseDouble(eElement.getAttribute("lon"));
                  latitude  =Double.parseDouble(eElement.getAttribute("lat"));


                  routePoint=new RoutePoint(latitude, longitude);
                  routeSegment.appendRoutePoint(routePoint);
               }
               i++;
            }
        }
        else
        {
            DebugLogger.error("No <rte> data in GPX file");
        }        
    }
    
    
    
    
    /**
     * This method reads a route consisting of waypoints from file
     * @param fileName Name of the file to read from
     * @param route Route will contain the route read. Supply new, empty route
     * @return True if an error occurred
     */
    public boolean readRouteFromFile(String fileName, Route route)

    {
        File                    fXmlFile;
        DocumentBuilderFactory  dbFactory;
        DocumentBuilder         dBuilder;
        Document                doc;
        Element                 gpxElement;
        Element                 routeElement;
        Element                 trackElement;
        boolean                 error;

        
        error=false;

        route.clear();
        try
        {

            fXmlFile = new File(fileName);
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();


            gpxElement=doc.getDocumentElement();
            if (gpxElement.getNodeName().equals("gpx"))
            {
                // First try to to load points from a <rte>
                routeElement=this.getChildElement(gpxElement, "rte");
                
                if (routeElement!=null)
                {
                    this.parseRoute(route, routeElement);
                }
                else
                {
                    trackElement=this.getChildElement(gpxElement, "trk");
                    if (trackElement!=null)
                    {
                        this.parseTrack(route, trackElement);
                    }
                    else
                    {
                        DebugLogger.error("No <trk> or <rte> in GPX file");
                    }
                }



                route.dumpLog();
            }
            else
            {
                DebugLogger.error(fileName+" does not seem to be a GPX file");
            }
        }
        catch (Exception e)
        {
            error=true;
            DebugLogger.error("Error parsing gpx file: "+e.getMessage());
        }
        return error;
    }



}
