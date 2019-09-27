/*
 * This Excutor class queues and exectutes offered Runnables in a serialized
 * way
 */
package net.studioblueplanet.generics;

import java.util.concurrent.Executor;
import java.util.Queue;
import java.util.ArrayDeque;

/**
 *
 * @author jorgen
 */
public class SerialExecutor implements Executor
{
     final Queue<Runnable>  tasks;
     Runnable               active;
     Executor               executor;

     /**
      * Constructor
      * @param executor Secondary executor to do the actual execution
      */
     public SerialExecutor(Executor executor) 
     {
         tasks          = new ArrayDeque<>();
         this.executor  = executor;
     }

     
     /**
      * Execute the next task
      */
     private synchronized void scheduleNext() 
     {
         active = tasks.poll();
         if (active != null) 
         {
             executor.execute(active);
         }
     }
     
     /**
      * The method to schedule a Runnable task
      * @param r The task to run
      */
     @Override
     public synchronized void execute(final Runnable r) 
     {
         tasks.offer((Runnable)() -> 
                        {
                            try 
                            {
                                r.run();
                            } 
                            finally 
                            {
                                scheduleNext();
                            }
                        });
         if (active == null) 
         {
             scheduleNext();
         }
     }
 
}
