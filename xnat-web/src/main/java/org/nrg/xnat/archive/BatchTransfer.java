/*
 * web: org.nrg.xnat.archive.BatchTransfer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.nrg.viewer.QCImageCreator;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;

import javax.mail.MessagingException;
import java.io.*;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BatchTransfer extends Thread{
    final private Logger logger = Logger.getLogger(BatchTransfer.class);
    final private List<XnatImagesessiondata> sessions = new LinkedList<>();
    final private List<File> dirs = new LinkedList<>();
    private UserI user = null;
    
    final private String server;
    final private String system;
    final private String admin_email;
    
    
    public BatchTransfer(final String server, final String system, final String admin_email)
    {
        this.server=server;
        this.system=system;
        this.admin_email=admin_email;
    }
    
    public void addSession(XnatImagesessiondata session, File dir){
        sessions.add(session);
        dirs.add(dir);
    }
    
    public int count() {
        return sessions.size();
    }
    
    public void execute(){
		final List<String> messages = new LinkedList<>();
		final List<List<String>> errors = new LinkedList<>();
        
        
        for(int i=0, nSessions = sessions.size(); i<nSessions; i++){
            boolean _successful;
            WrkWorkflowdata wkdata;
            try {
                XnatImagesessiondata partialMR = sessions.get(i);
                final File dir = dirs.get(i);
                final File xml = new File(dir.getAbsolutePath() + ".xml");
                final File txt = new File(dir.getAbsolutePath() + ".txt");
                
                try {
                     CriteriaCollection cc= new CriteriaCollection("AND");
                    cc.addClause("wrk:workFlowData.ID",partialMR.getId());
                    cc.addClause("wrk:workFlowData.pipeline_name","Transfer");
                    CriteriaCollection subCC = new CriteriaCollection("OR");
                    subCC.addClause("wrk:workFlowData.status","In Progress");
                    subCC.addClause("wrk:workFlowData.status","Queued");
                    cc.addClause(subCC);
					List<WrkWorkflowdata> al = WrkWorkflowdata.getWrkWorkflowdatasByField(cc, user, false);
                    if (al.size()>0){
                        wkdata= al.get(al.size() - 1);
						if (XFT.VERBOSE)
							System.out.println("FOUND Transfer Workflow:" + wkdata.getWrkWorkflowdataId());
                    }else{
						if (XFT.VERBOSE)
							System.out.println("MISSING TRANSFER WORKFLOWDATA");
                        wkdata = new WrkWorkflowdata(user);
                        try {
                            wkdata.setId(partialMR.getId());
                            wkdata.setDataType("xnat:mrSessionData");
                            wkdata.setCurrentStepId("Store");
                            wkdata.setLaunchTime(java.util.Calendar.getInstance().getTime());
                            wkdata.setPipelineName("Transfer");
                        } catch (Exception e) {
                            logger.error("",e);
                        }
                    }
                } catch (Exception e1) {
                    logger.error("",e1);
                    wkdata = new WrkWorkflowdata(user);
                    try {
                        wkdata.setId(partialMR.getId());
                        wkdata.setDataType(partialMR.getXSIType());
                        wkdata.setCurrentStepId("Store");
                        wkdata.setLaunchTime(java.util.Calendar.getInstance().getTime());
                        wkdata.setPipelineName("Transfer");
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                }
                
                try {
                    wkdata.setStatus("In Progress");
                    PersistentWorkflowUtils.save(wkdata,wkdata.buildEvent());
                } catch (Throwable e) {
                    logger.error("",e);
                }
                
				if (XFT.VERBOSE)
					System.out.print("Reading " + xml.getAbsolutePath() + "...");
                SAXReader reader = new SAXReader(null);
                long last = System.currentTimeMillis();
                XFTItem item = reader.parse(xml.getAbsolutePath());
				if (XFT.VERBOSE)
					System.out.println("done. (" + (System.currentTimeMillis() - last) + ")");
                
                final XnatImagesessiondata session = (XnatImagesessiondata)BaseElement.GetGeneratedItem(item);
                final XnatImagesessiondata tempMR = partialMR;
                if (null != partialMR.getId())
                    session.setId(partialMR.getId());
                
                session.copyValuesFrom(tempMR);
                session.fixScanTypes();
                session.correctArchivePaths();
                
                SaveItemHelper.authorizedSave(session,user, false, false,wkdata.buildEvent());
                

//                        logger.error("",e2);
//                        ArrayList error = new ArrayList();
//                        error.add(mr.getId());
//                        error.add("Error storing session xml. Please consult with your site administrator.");
//                        errors.add(error);
//                        _successful=false;
//                        continue;
                    
                    partialMR = (XnatImagesessiondata)BaseElement.GetGeneratedItem(session.getCurrentDBVersion());

                try {
                    wkdata.setCurrentStepId("Copy To Arc");
                    wkdata.setCurrentStepLaunchTime(java.util.Calendar.getInstance().getTime());
                    wkdata.setNextStepId("Verify Copy");
                    PersistentWorkflowUtils.save(wkdata,wkdata.buildEvent());
                } catch (Exception e) {
                    logger.error("",e);
                }

                if (xml.exists()) {                    
                    FileOutputStream fos=new FileOutputStream(xml);
                    OutputStreamWriter fw;
        			try {
        				FileLock fl=fos.getChannel().lock();
        				try{
        					fw = new OutputStreamWriter(fos);
                            partialMR.toXML(fos, false);
        					fw.flush();
        				}finally{
        					fl.release();
        				}
        			}finally{
        				fos.close();
        			}
//                    } catch (Exception e) {
//                	logger.error("",e);
//                    }
                }else{
                    logger.error("Error updating prearchive xml file.\n"+ xml.getAbsolutePath() + " does not exist.");
                }

                if (null == partialMR.getUser()) {
                    partialMR.getItem().setUser(user);
                }
                  
                String currentarc;
                try {
                    currentarc = partialMR.getCurrentArchiveFolder();
					if (XFT.VERBOSE)
						System.out.println("CURRENT ARC 1: " + currentarc);
                } catch (Throwable e) {
                    logger.error("Unable to identify current archive location",e);

					final List<String> error = new ArrayList<>(2);
                    error.add(partialMR.getId());
                    error.add("Error transfering files.  Unable to identify current archive location.");
                    errors.add(error);
                    _successful=false;
                    continue;
                }
                  
                final StringBuilder arcPath = new StringBuilder(partialMR.getArchiveRootPath());
                if (null != currentarc)
                    arcPath.append(currentarc);
				if (XFT.VERBOSE)
					System.out.println("ARC: " + arcPath);

                arcPath.append(partialMR.getArchiveDirectoryName());
                arcPath.append(File.separator);
                final File arcF = new File(arcPath.toString());
                if (!arcF.exists()){
                    arcF.mkdirs();
                }
                  
				if (XFT.VERBOSE)
					System.out.print("Copying from " + dir.getAbsolutePath() + " to " + arcF.getAbsolutePath() + "...");
                try {
                    org.nrg.xft.utils.FileUtils.CopyDir(dir, arcF, true);
					if (XFT.VERBOSE)
						System.out.println("done.");
                } catch (FileNotFoundException e) {
					if (XFT.VERBOSE)
						System.out.println("failed.");
                    logger.error("",e);
					List<String> error = new ArrayList<String>(2);
                    error.add(partialMR.getId());
                    error.add("Error transfering files.  Failed to create archive directory.");
                    errors.add(error);
                    _successful=false;
                    continue;
                }
                  
                try {
                    wkdata.setCurrentStepId("Verify Copy");
                    wkdata.setCurrentStepLaunchTime(java.util.Calendar.getInstance().getTime());
                    wkdata.setNextStepId("Zip");
                    PersistentWorkflowUtils.save(wkdata,wkdata.buildEvent());
                } catch (Exception e) {
                    logger.error("",e);
                }

				if (XFT.VERBOSE)
					System.out.print("Verifying copy...");
				List<String> fileerrors = FileUtils.CompareFile(dir, arcF);
				if (XFT.VERBOSE)
					System.out.println("done.");
                if (fileerrors.size()>0){
					if (XFT.VERBOSE)
						System.out.println("failed.");
                    try {
                	wkdata.setStatus("Failed");
                	wkdata.setCurrentStepLaunchTime(java.util.Calendar.getInstance().getTime());
                    PersistentWorkflowUtils.save(wkdata,wkdata.buildEvent());
                    } catch (Exception e) {
                	logger.error("",e);
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("Archiving Failed.<BR>File Comparison Failed.<BR>");
					for (String error : fileerrors) {
						sb.append(error).append("<BR>");
                    }
					List<String> error = new ArrayList<>(2);
                    error.add(partialMR.getId());
                    error.add(sb.toString());
                    errors.add(error);
                    _successful=false;
                    continue;
                }

//                    try {
//                        wkdata.setCurrentStepId("Session Validation");
//                        wkdata.setCurrentStepLaunchTime(java.util.Calendar.getInstance().getTime());
//                        wkdata.save(mr.getUser(), false, false);
//                    } catch (Exception e) {
//                        logger.error("",e);
//                    }
                  
                  if (partialMR instanceof XnatMrsessiondata) {
					if (XFT.VERBOSE)
						System.out.print("Creating QC Images...");
                      try {
                          QCImageCreator qcImageCreator = new QCImageCreator((XnatMrsessiondata)partialMR, user);
                          boolean _success = qcImageCreator.createQCImagesForScans();
                          if (!_success) {
                              AdminUtils.sendAdminEmail(user, "Archiving Failed", "Couldnt complete creation of Quality Control Images Successfully");
                          }
                      }catch (Exception e) {
                          final String sb = "Archiving Failed.<BR>Quality Control Image Creation Failed.<BR>" +
                                            e.getMessage();
                          AdminUtils.sendAdminEmail(user, "Archiving Failed", sb);
                      }
                      if(XFT.VERBOSE)System.out.print("Done");
                  }
                 /* try {
                      wkdata.setCurrentStepId("Zipping");
                      wkdata.setCurrentStepLaunchTime(java.util.Calendar.getInstance().getTime());
                      wkdata.setNextStepId("Cache Upload");
                      wkdata.save(user, false, false);
                  } catch (Exception e) {
                      logger.error("",e);
                  }
                  
                  try {
                      System.out.print("Zipping archive files...");
                      FileUtils.GZIPFiles(arcF);
                      System.out.println("done.");
                  } catch (RuntimeException e1) {
                      System.out.println("failed.");
                      logger.error("zip failed", e1);
                      try {
                          wkdata.setStatus("Failed");
                          wkdata.setCurrentStepLaunchTime(java.util.Calendar.getInstance().getTime());
                          wkdata.save(user, false, false);
                      } catch (Exception e) {
                          logger.error("",e);
                      }
                      Collection<String> error = new ArrayList<String>(2);
                      error.add(partialMR.getId());
                      error.add("Error transfering files.  Failed to zip archive files.");
                      errors.add(error);
                      _successful=false;
                      continue;
                  }*/

                  try {
                      wkdata.setStatus("Complete");
                      wkdata.setCurrentStepLaunchTime(java.util.Calendar.getInstance().getTime());
                      PersistentWorkflowUtils.save(wkdata,wkdata.buildEvent());
                  } catch (Exception e) {
                      logger.error("",e);
                  }

                  _successful = true;
                  messages.add(partialMR.getId());
                  
                  try {
					if (XFT.VERBOSE)
						System.out.print("Caching Uploaded Files...");
                      String cachePath = partialMR.getCachePath();
                      if (!cachePath.endsWith(File.separator)){
                          cachePath+=File.separator;
                      }
                      cachePath += dir.getName();
                      File cacheF = new File(cachePath);
                      org.nrg.xft.utils.FileUtils.MoveDir(dir, cacheF, true);
                      
                      String prearcpath = dir.getAbsolutePath();
                      while (prearcpath.endsWith("/")){
                          prearcpath= prearcpath.substring(0,prearcpath.length()-1);
                      }
                      while (prearcpath.endsWith("\\")){
                          prearcpath= prearcpath.substring(0,prearcpath.length()-1);
                      }
                      
                      prearcpath+=".xml";
                      File prearcXML = new File(prearcpath);
                      cachePath+= File.separator + dir.getName() + ".xml";
                      
                      File cacheXML=new File(cachePath);
                      org.nrg.xft.utils.FileUtils.MoveFile(prearcXML, cacheXML, true);
                      
					if (XFT.VERBOSE)
						System.out.println("done.");
                  } catch (FileNotFoundException e) {
                      logger.error("",e);
					if (XFT.VERBOSE)
						System.out.println("failed.");
                  } catch (Throwable e) {
                      logger.error("",e);
					if (XFT.VERBOSE)
						System.out.println("failed.");
                  }
                  
                  if (_successful){
                      try {
                          wkdata.setStatus("Complete");
                          wkdata.setCurrentStepLaunchTime(java.util.Calendar.getInstance().getTime());
                          PersistentWorkflowUtils.save(wkdata,wkdata.buildEvent());
                      } catch (Exception e) {
                          logger.error("",e);
                      }
                  }else{
                      try {
                          wkdata.setStatus("Failed");
                          wkdata.setCurrentStepLaunchTime(java.util.Calendar.getInstance().getTime());
                          PersistentWorkflowUtils.save(wkdata,wkdata.buildEvent());
                      } catch (Exception e) {
                          logger.error("",e);
                      }
                  }
               
                  txt.delete();
            } catch (Throwable e) {
                logger.error("",e);
            }
        }
        
		String[] to = new String[] { XDAT.getSiteConfigPreferences().getAdminEmail(), user.getEmail() };
            String from = XDAT.getSiteConfigPreferences().getAdminEmail();
            String subject = system + " update: Archiving Complete.";
            if (errors.size()>0)
                subject += errors.size() + " errors";
            String message = getEmailCompletionMessage(user, messages,errors, system,admin_email);
		try {
			XDAT.getMailService().sendHtmlMessage(from, to, subject, message);
		} catch (MessagingException exception) {
			logger.error("Error sending email", exception);
        }
        
		if (XFT.VERBOSE)
			System.out.println("Ending Batch Transfer Thread");
    }

    public String getEmailCompletionMessage(final UserI user, final List<String> messages, final List<List<String>> errors, final String system, final String adminEmail) {
        final VelocityContext context = new VelocityContext() {{
            put("user", user);
            put("server", server);
            put("siteLogoPath", XDAT.getSiteLogoPath());
            put("process", "Transfer to the archive.");
            put("messages", messages);
            put("errors", errors);
            put("system", system);
            put("admin_email", adminEmail);
        }};
        StringWriter sw = new StringWriter();
        Template template = Velocity.getTemplate("/screens/BatchWorkflowCompleteEmail.vm");
        template.merge(context, sw);
        return sw.toString();
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

    /**
     * @return the user
     */
    public UserI getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(UserI user) {
        this.user = user;
    }
}
