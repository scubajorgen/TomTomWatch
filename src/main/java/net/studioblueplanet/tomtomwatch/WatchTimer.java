/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import java.util.Timer;
import java.util.TimerTask;
import net.studioblueplanet.logger.DebugLogger;
/**
 *
 * @author jorgen
 */
public class WatchTimer extends TimerTask
{
    CommunicationProcess    communicationProcess;
    Timer                   timer;
    
    public WatchTimer(CommunicationProcess communicationProcess)
    {
        this.communicationProcess=communicationProcess;
    }
    
    @Override
    public void run() 
    {
        if (communicationProcess.isConnected())
        {
            communicationProcess.pushCommand(ThreadCommand.THREADCOMMAND_GETTIME);
        }
    }

   
    public void start()
    {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(this, 0, 1*1000);
        DebugLogger.info("Watch Timer started");
    }
    
    public void stop()
    {
        timer.cancel();
        DebugLogger.info("Watch Timer stopped");
    }

}
