/*
 * This executor executes the offered runnable in its own thread
 */
package net.studioblueplanet.generics;

import java.util.concurrent.Executor;
/**
 *
 * @author jorgen
 */
public class ThreadExecutor implements Executor
{
     /**
      * The method to schedule a Runnable task
      * @param r The task to run
      */
     @Override
     public synchronized void execute(final Runnable r) 
     {
         new Thread(r).start();
     }    
}
