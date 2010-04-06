//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Aug 16, 2006
 *
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
