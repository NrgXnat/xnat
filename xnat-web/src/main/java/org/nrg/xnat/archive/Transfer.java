/*
 * web: org.nrg.xnat.archive.Transfer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.exceptions.UndefinedArchive;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class Transfer {
    static Logger logger = Logger.getLogger(Transfer.class);
    private org.nrg.xdat.om.XnatImagesessiondata mr = null;
    private String imageType=null;
    private boolean overwrite=true;
    private boolean cache = true;
    private boolean placeInRaw = false;
    private String prearc = null;
    
    private boolean waitForTransfer= false;
    
    private boolean clearExistingWorkflows=false;
    
    private boolean build = true;

    private String server = null;
    private String system = null;
    private String admin_email = null;
    private boolean notifies = true;

    
    public Transfer(String server,String system,String admin_email)
    {
        this.server=server;
        this.system=system;
        this.admin_email=admin_email;
    }

    public int getNotifies() {
    	return notifies?1:0;
    }
    
    public void setNotifies(boolean val) {
    	notifies = val;
    }

    
    public boolean execute() {
        boolean _successful = false;
        UserI user = mr.getUser();

        try {
            if (prearc==null)
                prearc = mr.getPrearchivepath();
            File prearcF;
            try {
                prearcF = new File(prearc);
                if (!prearcF.exists()) throw new FileNotFoundException(prearc);
            }catch(FileNotFoundException fne) {
                logger.error("",fne);
                throw fne;
            }

            String currentarc =null;
            try {
                currentarc = mr.getCurrentArchiveFolder();
                if(XFT.VERBOSE)System.out.println("CURRENT ARC 1: " + currentarc);
            } catch (InvalidArchiveStructure e) {
                logger.error("",e);
                throw e;
            }
            String arc = mr.getArchiveRootPath();
            if (currentarc!=null){
                arc += currentarc;
            }
            if(XFT.VERBOSE)System.out.println("ARC: " + arc);
            arc += mr.getArchiveDirectoryName() + File.separator;
            if (this.isPlaceInRaw()){
                arc += "SCANS" + File.separator;
            }

            XnatPipelineLauncher xnatPipelineLauncher = new XnatPipelineLauncher((UserI)user);
           // Modified by MR - 2010/03/11 AdminEmail is set in pipeline setup setup
            // xnatPipelineLauncher.setAdmin_email(XDAT.getSiteConfigPreferences().getAdminEmail());
           //Modified bt MR - 2010/03/11 There is a setting for Site Admin to set email notification for Transfer pipeline
           // xnatPipelineLauncher.setAlwaysEmailAdmin(ArcSpecManager.GetInstance().getEmailspecifications_pipeline());
            String pipelineName = "xnat_tools/Transfer.xml";
            xnatPipelineLauncher.setPipelineName(pipelineName);
            xnatPipelineLauncher.setNeedsBuildDir(false);
           
            if(ArcSpecManager.allowTransferEmail()){
                xnatPipelineLauncher.setParameter("notifyAdmin","1");
            }else{
                xnatPipelineLauncher.setParameter("notifyAdmin","0");
            }
            
            //Launcher will not send an email. 
            //The pipeline has a notify step which will CC to Admin if 
            //the parameter notify is set to 1.
            xnatPipelineLauncher.setSupressNotification(true);
            xnatPipelineLauncher.setId(mr.getId());
            xnatPipelineLauncher.setLabel(mr.getLabel());
            xnatPipelineLauncher.setDataType(mr.getXSIType());
            xnatPipelineLauncher.setExternalId(mr.getProject());
            xnatPipelineLauncher.setParameter("sourceDir", prearc);
            xnatPipelineLauncher.setParameter("destinationDir", arc);
            xnatPipelineLauncher.setParameter("session", mr.getId());
            xnatPipelineLauncher.setParameter("sessionLabel", mr.getLabel());
            xnatPipelineLauncher.setParameter("useremail", user.getEmail());
            xnatPipelineLauncher.setParameter("userfullname", XnatPipelineLauncher.getUserName(user));
            xnatPipelineLauncher.setParameter("adminemail", admin_email);
            xnatPipelineLauncher.setParameter("xnatserver", system);
            xnatPipelineLauncher.setParameter("mailhost", XDAT.getNotificationsPreferences().getSmtpServer().getHostname());
            xnatPipelineLauncher.setParameter("sessionType", mr.getXSIType());
            xnatPipelineLauncher.setParameter("xnat_project", mr.getProject());
            xnatPipelineLauncher.setParameter("logDir", XDAT.getSiteConfigPreferences().getCachePath() + "logs" + "/" + "transfer");
           // xnatPipelineLauncher.setParameter("notify","" +getNotifies());
 
            xnatPipelineLauncher.setWaitFor(waitForTransfer);            
            
            if (cache){
            	if(XFT.VERBOSE)System.out.print("Caching Uploaded Files...");
                String cachePath = mr.getCachePath();
                if (!cachePath.endsWith(File.separator)){
                    cachePath+=File.separator;
                }
                File parent = prearcF.getParentFile();
                cachePath +="transfer_bk" + File.separator + parent.getName() + File.separator + prearcF.getName();
                xnatPipelineLauncher.setParameter("cachepath", cachePath);
            }

        /*    if (mr.getXSIType().equals("xnat:mrSessionData")) {
                xnatPipelineLauncher.setParameter("createQc", "1");
                
                xnatPipelineLauncher.setParameter("tbpath", QCImageCreator.getQCThumbnailPathForSession(mr.getProject()));
                xnatPipelineLauncher.setParameter("cpath", QCImageCreator.getQCCachePathForSession(mr.getProject()));
            } */
            
          
            _successful = xnatPipelineLauncher.launch(null);
            if (!_successful) {
                throw new Exception("Unable to complete transfer");
            }

            _successful = true;
        } catch (FileNotFoundException e) {
            logger.error("",e);
            StringBuffer sb = new StringBuffer();
            sb.append("Archiving Failed.<BR>"+ mr.getId() + "<BR>");
            sb.append(e.toString());
            try {
                AdminUtils.sendAdminEmail(user,"Archiving Failed: " + mr.getId(),sb.toString());
            } catch (RuntimeException e1) {
                logger.error("",e1);
            }
        } catch (UndefinedArchive e) {
            logger.error("",e);
            StringBuffer sb = new StringBuffer();
            sb.append("Archiving Failed.<BR>"+ mr.getId() + "<BR>");
            sb.append(e.toString());
            try {
                AdminUtils.sendAdminEmail(user,"Archiving Failed: " + mr.getId(),sb.toString());
            } catch (RuntimeException e1) {
                logger.error("",e1);
            }
        } catch (InvalidArchiveStructure e) {
            logger.error("",e);
            StringBuffer sb = new StringBuffer();
            sb.append("Archiving Failed.<BR>"+ mr.getId() + "<BR>");
            sb.append(e.toString());
            try {
                AdminUtils.sendAdminEmail(user,"Archiving Failed: " + mr.getId(),sb.toString());
            } catch (RuntimeException e1) {
                logger.error("",e1);
            }
        } catch (IOException e) {
            logger.error("",e);
            StringBuffer sb = new StringBuffer();
            sb.append("Archiving Failed.<BR>"+ mr.getId() + "<BR>");
            sb.append(e.toString());
            try {
                AdminUtils.sendAdminEmail(user,"Archiving Failed: " + mr.getId(),sb.toString());
            } catch (RuntimeException e1) {
                logger.error("",e1);
            }
        } catch (Throwable e) {
            logger.error("",e);
            StringBuffer sb = new StringBuffer();
            sb.append("Archiving Failed.<BR>"+ mr.getId() + "<BR>");
            sb.append(e.toString());
            try {
                AdminUtils.sendAdminEmail(user,"Archiving Failed: " + mr.getId(),sb.toString());
            } catch (RuntimeException e1) {
                logger.error("",e1);
            }
        }
        return _successful;
    }
    
    /*public void run() {
        super.run();
        execute();
    }*/
  
    
    public String getEmailCompletionMessage(UserI user, String message, String system, String admin_email) throws Exception{
        VelocityContext context = new VelocityContext();
        context.put("user",user);
        context.put("server",server);
        context.put("siteLogoPath", XDAT.getSiteLogoPath());
        context.put("process","Transfer to the archive.");
        context.put("message",message);
        context.put("system",system);
        context.put("admin_email",admin_email);
        TurbineUtils.SetSearchProperties(context, mr);
        StringWriter sw = new StringWriter();
        Template template =Velocity.getTemplate("/screens/WorkflowCompleteEmail.vm");
        template.merge(context,sw);
        return sw.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */




    /**
     * @return the imageType
     */
    public String getImageType() {
        return imageType;
    }
    /**
     * @param imageType the imageType to set
     */
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
    /**
     * @return the mr
     */
    public org.nrg.xdat.om.XnatImagesessiondata getImageSession() {
        return mr;
    }
    /**
     * @param mr the mr to set
     */
    public void setImageSession(org.nrg.xdat.om.XnatImagesessiondata mr) {
        this.mr = mr;
    }

    /**
     * @return the overwrite
     */
    public boolean overwrite() {
        return overwrite;
    }

    /**
     * @param overwrite the overwrite to set
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * @return the cache
     */
    public boolean cache() {
        return cache;
    }

    /**
     * @param cache the cache to set
     */
    public void setCache(boolean cache) {
        this.cache = cache;
    }

    /**
     * @return the prearc
     */
    public String getPrearc() {
        return prearc;
    }

    /**
     * @param prearc the prearc to set
     */
    public void setPrearc(String prearc) {
        this.prearc = prearc;
    }



    /**
     * @return the placeInRaw
     */
    public boolean isPlaceInRaw() {
        return placeInRaw;
    }



    /**
     * @param placeInRaw the placeInRaw to set
     */
    public void setPlaceInRaw(boolean placeInRaw) {
        this.placeInRaw = placeInRaw;
    }

    /**
     * @return the build
     */
    public boolean isBuild() {
        return build;
    }

    /**
     * @param build the build to set
     */
    public void setBuild(boolean build) {
        this.build = build;
    }

    /**
     * @return the clearExistingWorkflows
     */
    public boolean isClearExistingWorkflows() {
        return clearExistingWorkflows;
    }

    /**
     * @param clearExistingWorkflows the clearExistingWorkflows to set
     */
    public void setClearExistingWorkflows(boolean clearExistingWorkflows) {
        this.clearExistingWorkflows = clearExistingWorkflows;
    }

    /**
     * @return the waitForTransfer
     */
    public boolean isWaitForTransfer() {
        return waitForTransfer;
    }

    /**
     * @param waitForTransfer the waitForTransfer to set
     */
    public void setWaitForTransfer(boolean waitForTransfer) {
        this.waitForTransfer = waitForTransfer;
    }
 
}
