/*
 * org.nrg.xft.commandPrompt.MaskingThread
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.commandPrompt;

/**
 * @author timo
 *
 */
public class MaskingThread extends Thread {
    private volatile boolean stop;
    private char echochar = '*';
    private int charCount = 0;
   /**
    *@param prompt The prompt displayed to the user
    */
    public MaskingThread(String prompt) {
       System.out.print(prompt);
    }

   /**
    * Begin masking until asked to stop.
    */
    public void run() {

       int priority = Thread.currentThread().getPriority();
       Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

       try {
          stop = true;
          while(stop) {
              System.out.print("\010" + echochar);
            try {
               // attempt masking at this rate
               Thread.currentThread().sleep(1);
            }catch (InterruptedException iex) {
               Thread.currentThread().interrupt();
               return;
            }
          }
       } finally { // restore the original priority
          Thread.currentThread().setPriority(priority);
       }
    }

   /**
    * Instruct the thread to stop masking.
    */
    public void stopMasking() {
       this.stop = false;
    }

}
