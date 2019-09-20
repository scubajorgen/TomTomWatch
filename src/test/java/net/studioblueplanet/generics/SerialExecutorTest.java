/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.generics;

import net.studioblueplanet.usb.UsbPacket;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author jorgen
 */
public class SerialExecutorTest
{
    private Runnable r=mock(Runnable.class);
    
    public SerialExecutorTest()
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
     * Test of execute method, of class SerialExecutor.
     */
    @Test
    public void testExecute()
    {
        System.out.println("execute");

        SerialExecutor instance = new SerialExecutor();
        instance.execute(r);
        
        Mockito.verify(r, timeout(1000).times(1)).run();
    }

    
    /**
     * Test of execute method, of class SerialExecutor.
     */
    @Test
    public void testExecute2()
    {
        System.out.println("execute 2");

        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) 
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch(Exception e)
                {
                    
                }
                return null; // void method, so return null
            }
        }).when(r).run();   
        
        
        SerialExecutor instance = new SerialExecutor();
        instance.execute(r);
        instance.execute(r);
        
        Mockito.verify(r, timeout(500).times(0)).run();
        Mockito.verify(r, timeout(1500).times(1)).run();
        Mockito.verify(r, timeout(2500).times(2)).run();
        // Isn't this a creative solution to check serialized execution? :-)
    }    
    

}
