/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;


/**
 *
 * @author jorgen
 */
public class WatchTimerTest
{
    private final CommunicationProcess process;
    
    public WatchTimerTest()
    {
        process=mock(CommunicationProcess.class);
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
     * Test of run method, of class WatchTimer.
     */
    @Test
    public void testRun()
    {
        System.out.println("run");
        WatchTimer instance = new WatchTimer(process);
        when(process.isConnected()).thenReturn(true);
        instance.run();
        verify(process, times(1)).isConnected();
        verify(process, times(1)).pushCommand(ThreadCommand.THREADCOMMAND_GETTIME);

        Mockito.reset(process);
        when(process.isConnected()).thenReturn(false);
        instance.run();
        verify(process, times(1)).isConnected();
        verify(process, times(0)).pushCommand(ThreadCommand.THREADCOMMAND_GETTIME);
    }

    /**
     * Test of start method, of class WatchTimer.
     */
    @Test
    public void testStart()
    {
        System.out.println("start");
        WatchTimer instance = new WatchTimer(process);
        when(process.isConnected()).thenReturn(true);
        instance.start();
        verify(process, timeout(2500).times(2)).pushCommand(ThreadCommand.THREADCOMMAND_GETTIME);
        instance.stop();
    }

    /**
     * Test of stop method, of class WatchTimer.
     */
    @Test
    public void testStop()
    {
        System.out.println("stop");
        WatchTimer instance = new WatchTimer(process);
        when(process.isConnected()).thenReturn(true);
        instance.start();
        try
        {
            Thread.sleep(2500);
        }
        catch(Exception e)
        {
            
        }
        instance.stop();
        verify(process, timeout(4500).times(3)).pushCommand(ThreadCommand.THREADCOMMAND_GETTIME);
    }
    
}
