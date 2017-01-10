/*
 * web: org.nrg.xnat.archive.UploadManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.nrg.xft.security.UserI;

public class UploadManager extends Thread{
    static Logger logger = Logger.getLogger(UploadManager.class);
    private String projectID = null;
    private ArrayList<File> files = new ArrayList<File>();
    private UserI user = null;
    private String outputDir = null;
    private String server = null;

    public UploadManager(UserI user, String project, String outputPath, String server)
    {
        this.user=user;
        projectID= project;
        outputDir=outputPath;
        if (server.trim().endsWith(":80"))
        {
            server = server.trim().substring(0,server.trim().indexOf(":80"));
        }
        this.server=server;
    }
    
    public void addFile(File file)
    {
        files.add(file);
    }
    
    public int size(){
        return files.size();
    }

    public void execute(){
        Date d = Calendar.getInstance().getTime();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        super.run();
        try {
            execute();
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}
