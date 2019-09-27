/*
 * This class directly executes the Runnable passed
 */
package net.studioblueplanet.generics;

import java.util.concurrent.Executor;
/**
 *
 * @author jorgen
 */
public class DirectExecutor implements Executor
{
     /**
      * The method to schedule a Runnable task
      * @param r The task to run
      */
     @Override
     public synchronized void execute(final Runnable r) 
     {
         r.run();
     }
    
}
