/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.generics;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jorgen
 */
public class ToolBoxTest
{
    
    public ToolBoxTest()
    {
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
     * Test of readUnsignedInt method, of class ToolBox.
     */
    @Test
    public void testReadUnsignedInt()
    {
        System.out.println("readUnsignedInt");
        byte[]  data;
        int     offset;
        int     bytes;
        boolean isLE;
        int     result;
        int     expResult;
        
        data        =new byte[6];
        data[0]     =(byte)0x01;
        data[1]     =(byte)0xff;
        data[2]     =(byte)0x01;
        data[3]     =(byte)0x02;
        data[4]     =(byte)0xff;
        data[5]     =(byte)0xfe;
        
        
        offset      =0;
        bytes       =1;
        isLE        =true;
        expResult   =0x01;
        result      =ToolBox.readUnsignedInt(data, offset, bytes, isLE);
        assertEquals(result, expResult);
        
        offset      =1;
        bytes       =4;
        isLE        =true;
        expResult   =0xff0201ff;
        result      =ToolBox.readUnsignedInt(data, offset, bytes, isLE);
        assertEquals(result, expResult);

        offset      =1;
        bytes       =4;
        isLE        =false;
        expResult   =0xff0102ff;
        result      =ToolBox.readUnsignedInt(data, offset, bytes, isLE);
        assertEquals(result, expResult);

    }

    /**
     * Test of readUnsignedLong method, of class ToolBox.
     */
    @Test
    public void testReadUnsignedLong()
    {
        System.out.println("readUnsignedLong");
        byte[]  data;
        int     offset;
        int     bytes;
        boolean isLE;
        long    result;
        long    expResult;
        
        data        =new byte[9];
        data[0]     =(byte)0x00;
        data[1]     =(byte)0x01;
        data[2]     =(byte)0x02;
        data[3]     =(byte)0x03;
        data[4]     =(byte)0x04;
        data[5]     =(byte)0x05;
        data[6]     =(byte)0x06;
        data[7]     =(byte)0x07;
        data[8]     =(byte)0xff;
        
        
        offset      =1;
        bytes       =8;
        isLE        =false;
        expResult   =0x01020304050607ffL;
        result      =ToolBox.readUnsignedLong(data, offset, bytes, isLE);
        assertEquals(result, expResult);

        offset      =1;
        bytes       =8;
        isLE        =true;
        expResult   =-70080650589044223L;
        result      =ToolBox.readUnsignedLong(data, offset, bytes, isLE);
        assertEquals(result, expResult);

        offset      =8;
        bytes       =1;
        isLE        =true;
        expResult   =0xffL;
        result      =ToolBox.readUnsignedLong(data, offset, bytes, isLE);
        assertEquals(result, expResult);

        offset      =1;
        bytes       =1;
        isLE        =false;
        expResult   =+1L;
        result      =ToolBox.readUnsignedLong(data, offset, bytes, isLE);
        assertEquals(result, expResult);

    }

    /**
     * Test of readInt method, of class ToolBox.
     */
    @Test
    public void testReadInt()
    {
        System.out.println("readInt");
        byte[]  data;
        int     offset;
        int     bytes;
        boolean isLE;
        int     result;
        int     expResult;
        
        data        =new byte[6];
        data[0]     =(byte)0x01;
        data[1]     =(byte)0xff;
        data[2]     =(byte)0x01;
        data[3]     =(byte)0x02;
        data[4]     =(byte)0xff;
        data[5]     =(byte)0xfe;
        
        
        offset      =0;
        bytes       =1;
        isLE        =true;
        expResult   =0x01;
        result      =ToolBox.readInt(data, offset, bytes, isLE);
        assertEquals(result, expResult);
        
        offset      =1;
        bytes       =1;
        isLE        =true;
        expResult   =-0x01;
        result      =ToolBox.readInt(data, offset, bytes, isLE);
        assertEquals(result, expResult);
        
        offset      =2;
        bytes       =2;
        isLE        =true;
        expResult   =0x0201;
        result      =ToolBox.readInt(data, offset, bytes, isLE);
        assertEquals(result, expResult);
        
        offset      =4;
        bytes       =2;
        isLE        =true;
        expResult   =-257;
        result      =ToolBox.readInt(data, offset, bytes, isLE);
        assertEquals(result, expResult);
        
        offset      =2;
        bytes       =2;
        isLE        =false;
        expResult   =0x0102;
        result      =ToolBox.readInt(data, offset, bytes, isLE);
        assertEquals(result, expResult);
        
        offset      =4;
        bytes       =2;
        isLE        =false;
        expResult   =-2;
        result      =ToolBox.readInt(data, offset, bytes, isLE);
        assertEquals(result, expResult);
        
    }

    /**
     * Test of readFloat method, of class ToolBox.
     */
    @Test
    public void testReadFloat()
    {
        System.out.println("readFloat");
        byte[]  data;
        int     offset;
        boolean isLittleEndian;
        float   expResult;
        float   result; 
        float   expectedResult;
        
        data            =new byte[16];
        expectedResult  =12.34f;
        offset          =2;
        isLittleEndian  =false;
        ToolBox.writeFloat(data, expectedResult, offset, isLittleEndian);
        result = ToolBox.readFloat(data, offset, isLittleEndian);
        assertEquals(expectedResult, result, 0.0);

        data            =new byte[16];
        expectedResult  =-12.34f;
        offset          =2;
        isLittleEndian  =true;
        ToolBox.writeFloat(data, expectedResult, offset, isLittleEndian);
        result = ToolBox.readFloat(data, offset, isLittleEndian);
        assertEquals(expectedResult, result, 0.0);

    }

    /**
     * Test of readString method, of class ToolBox.
     */
    @Test
    public void testReadString()
    {
        byte[]  data;
        int     offset;
        int     chars;
        String  result;
        String  expResult;
        
        System.out.println("readString");

        data=new byte[5];
        data[0]=(byte)0x00;
        data[1]=(byte)'h';
        data[2]=(byte)'o';
        data[3]=(byte)'i';
        data[4]=(byte)'x';
        
        offset      =1;
        chars       =3;
        expResult   = "hoi";
        result = ToolBox.readString(data, offset, chars);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeUnsignedInt method, of class ToolBox.
     */
    @Test
    public void testWriteUnsignedInt()
    {
        System.out.println("writeUnsignedInt");
        byte[] data;
        int value;
        int expectedValue;
        int offset;
        int bytes;
        boolean isLittleEndian;
        
        data=new byte[16];
        
        value=0x1234567;
        offset=0x01;
        bytes=4;
        isLittleEndian=false;
        ToolBox.writeUnsignedInt(data, value, offset, bytes, isLittleEndian);
        expectedValue=ToolBox.readUnsignedInt(data, offset, bytes, isLittleEndian);
        assertEquals(expectedValue, value);
        
        value=0x1234;
        offset=0x01;
        bytes=2;
        isLittleEndian=true;
        ToolBox.writeUnsignedInt(data, value, offset, bytes, isLittleEndian);
        expectedValue=ToolBox.readUnsignedInt(data, offset, bytes, isLittleEndian);
        assertEquals(expectedValue, value);
        
    }

    /**
     * Test of writeUnsignedLong method, of class ToolBox.
     */
    @Test
    public void testWriteUnsignedLong()
    {
        System.out.println("writeUnsignedLong");
        byte[]  data;
        long    value;
        long    expectedValue;
        int     offset;
        int     bytes;
        boolean isLittleEndian;
        
        data=new byte[24];
        
        value=0x12345678fffefdfcL;
        offset=0x01;
        bytes=8;
        isLittleEndian=false;
        ToolBox.writeUnsignedLong(data, value, offset, bytes, isLittleEndian);
        expectedValue=ToolBox.readUnsignedLong(data, offset, bytes, isLittleEndian);
        assertEquals(expectedValue, value);
        
        value=0x1234;
        offset=0x02;
        bytes=2;
        isLittleEndian=true;
        ToolBox.writeUnsignedLong(data, value, offset, bytes, isLittleEndian);
        expectedValue=ToolBox.readUnsignedLong(data, offset, bytes, isLittleEndian);
        assertEquals(expectedValue, value);
    }

    /**
     * Test of secondsToHours method, of class ToolBox.
     */
    @Test
    public void testSecondsToHours()
    {
        int     seconds;
        String  expResult;
        String  result;

        System.out.println("secondsToHours");

        seconds         = 0;
        expResult       = "0h00'00\"";
        result          = ToolBox.secondsToHours(seconds);
        assertEquals(expResult, result);

        seconds         = 3661;
        expResult       = "1h01'01\"";
        result          = ToolBox.secondsToHours(seconds);
        assertEquals(expResult, result);

        seconds         = 36610;
        expResult       = "10h10'10\"";
        result          = ToolBox.secondsToHours(seconds);
        assertEquals(expResult, result);

    }
    
}
