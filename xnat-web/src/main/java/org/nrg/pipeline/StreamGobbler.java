/*
 * org.nrg.pipeline.StreamGobbler
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

package org.nrg.pipeline;

import org.apache.log4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StreamGobbler extends Thread {


    public StreamGobbler(InputStream is, String type) {
        this.type = type;
        isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        out = null;
        log = new StringBuffer();
        stringBuffer = new StringBuffer();
    }

   public StreamGobbler(InputStream is, String type, boolean returnString) {
        this.type = type;
        isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        if (returnString)
            stringBuffer = new StringBuffer();
        log = new StringBuffer();
        out = null;
    }
    
    public void setFile(String fileName) throws IOException {
        out = new BufferedWriter(new FileWriter(fileName, true));
        out.write("\n--------------------------------------------\n");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        out.write( dateFormat.format(Calendar.getInstance().getTime()) + "\n");
        out.write(log + "\n");
        out.write("\n--------------------------------------------\n");
    }
    
    public void log(String msg) {
        log.append(msg);
    }
    
    
    /**
     * @param instantUpdate The instantUpdate to set.
     */
    public void setInstantUpdate(boolean instantUpdate) {
        this.instantUpdate = instantUpdate;
    }
    
    public  void run() {
        String line="";
        if (!stop) {
            try  {
                while ( (line = br.readLine()) != null) {
                    //logger.info(type + ">" + line);
                    if (out != null) { 
                        out.write(line); out.write("\n");
                    }
                    appendLine(line);
                }
             } catch (IOException ioe) {
                 logger.debug(ioe.getLocalizedMessage());  
                 try {
                     if (out != null) out.close();
                 }catch (IOException ioe1) {
                     logger.debug("Unable to close file " + ioe1.getLocalizedMessage());
                 }
             }
        }
    }
    
    public String getString() {
        return stringBuffer.toString();
    }
    
    public String getFirstLine() {
        String rtn =null;
        int indexOfNewLine = stringBuffer.indexOf("\n");
        if (indexOfNewLine != -1)
            rtn = stringBuffer.substring(0,indexOfNewLine);
        return rtn;
    }
    
    public  void appendLine(String line) {
        if (instantUpdate) {
            System.out.println(line);
        }else {
            if (stringBuffer != null) {
                stringBuffer.append(line); 
                stringBuffer.append("\n");
            }
        }
    }
    
    public void finish() {
        stop = true;
        if (out != null ) {
            try {
                isr.close();
                out.close();
                
            }catch(IOException ioe) {
                logger.debug("Unable to close file " + ioe.getLocalizedMessage());
            }
        }
    }

    
    InputStreamReader isr;
    BufferedReader br ;
    String type;
    BufferedWriter out;
    StringBuffer stringBuffer;
    boolean stop = false;
    StringBuffer log;
    boolean instantUpdate = false;

    static Logger logger = Logger.getLogger(StreamGobbler.class);
}


