/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
     final Queue<Runnable> tasks;
     Runnable active;

     /**
      * Constructor
      */
     public SerialExecutor(/*Executor executor*/) 
     {
         tasks          = new ArrayDeque<>();
     }

     
     /**
      * Execute the next task
      */
     private synchronized void scheduleNext() 
     {
         active = tasks.poll();
         if (active != null) 
         {
             new Thread(active).start();
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
                            try {
                                r.run();
                            } finally {
                                scheduleNext();
                            }
                        });
         if (active == null) 
         {
             scheduleNext();
         }
     }
 
}
