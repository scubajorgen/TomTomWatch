/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.generics;

/**
 * This class implements the Google Maps polyline encoding
 * Usage:
 * encoder=PolyLineEncoder.getInstance();
 * encoder.resetPointEncoding();
 * String encodedString="";
 * encodeString+=encoder.encodePoint(point1);
 * encodeString+=encoder.encodePoint(point2);
 * encodeString+=encoder.encodePoint(point3);
 * ...
 * @author Jorgen
 */
public class PolyLineEncoder
{
    private static PolyLineEncoder  theInstance;

    private boolean                 firstPoint=true;
    private double                  previousLat;
    private double                  previousLon;

    
    /**
     * Returns the one and only instance of this Singleton class
     * @return The instance
     */
    public static PolyLineEncoder getInstance()
    {
        if (theInstance==null)
        {
            theInstance=new PolyLineEncoder();
        }
        return theInstance;
    }

    /**
     *  This method resets the Google polyline encoding. Basically it
     *  sets the 1st point to null, indicating that the first point must be
     *  encoded fully (for subsequent points only the delta will be encoded)
     */
    public void resetPointEncoding()
    {
//        this.previousEncodedRecord=null;
        this.firstPoint=true;
    }
    
    /**
     * This method encodes a point according the Google encoded polyline
     * method.
     * @param lat The latitude of the point to encode
     * @param lon The longitude of the point to encode
     * @return String representing the encoded point (or delta with respect
     *                to previous point if it is not the 1st point)
     */
    public String encodePoint(double lat, double lon)
    {
        String              encodedPointString;
        double              deltaLat, deltaLon;
        
        encodedPointString="";
        
        
        if (firstPoint)
        {
            encodedPointString+=encodeValue(lat);
            encodedPointString+=encodeValue(lon);
            firstPoint=false;
        }
        else
        {
            deltaLat=lat-previousLat;
            deltaLon=lon-previousLon;
            encodedPointString+=encodeValue(deltaLat);
            encodedPointString+=encodeValue(deltaLon);
        }
        previousLat=lat;
        previousLon=lon;
        
        return encodedPointString;
        
    }
    
        
    
    
    /**
     * This method converts doubles to the Google encoded string format
     * @param value Value to convert
     * @return Encoded string part
     */
    public String encodeValue(double value)
    {
        String  conversion;
        int     binValue;
        int     charCode;
        int     nextCharCode;
        char    theChar;
        boolean isNegative;
        int     i;
        boolean finished;
        
        conversion="";
        finished=false;
        
        if (value<0)
        {
            value=-value;
            isNegative=true;
        }
        else
        {
            isNegative=false;
        }
        

        value=value*1e5;
        binValue=(int)Math.round(value);
        if (binValue==0)
        {
            isNegative=false;
        }
        
        if (isNegative)
        {
            binValue=~binValue;
            binValue+=1;
        }
        binValue<<=1;
        
        if (isNegative)
        {
            binValue=~binValue;
        }
        
        i=0;
        while (i<6 && !finished)
        {
            charCode=binValue & 0x1f;
            binValue>>=5;
            
            if (i<5)
            {
                nextCharCode=binValue>>((i+1)*5) & 0x1f;
                if (binValue>0)
                {
                    charCode |= 0x20;
                }
                else
                {
                    finished=true;
                }
            }
            charCode+=63;
            theChar=(char)charCode;
            conversion+=theChar;


            i++;
        }
        
        return conversion;
    }
    
}


