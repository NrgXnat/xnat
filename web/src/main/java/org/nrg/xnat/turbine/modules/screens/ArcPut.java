/*
 * web: org.nrg.xnat.turbine.modules.screens.ArcPut
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.CatCatalogTagI;
import org.nrg.xdat.om.XnatAbstractresourceTag;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.zip.TarUtils;
import org.nrg.xft.utils.zip.ZipI;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.WorkflowUtils;

public class ArcPut extends RawScreen {
    private static final Logger logger = Logger.getLogger(ArcPut.class);
    @Override
    protected void doOutput(RunData data) throws Exception {
        String session = (String) TurbineUtils.GetPassedParameter("session",data);
        String mr_session_id = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("mr_session_id",data));
        HttpServletResponse response = data.getResponse();
        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter pw = response.getWriter();
        try {
            if (session==null){
                pw.println("<UploadResponse status=\"ERROR\" CODE=\"100\">");
                pw.println("<message>Unspecified User Session</message>");
                pw.println("</UploadResponse>");
                return;
            }
            UserI user = TurbineUtils.getUser(data);
            if (user==null){
                pw.println("<UploadResponse status=\"ERROR\" CODE=\"103\">");
                pw.println("<message>User Session Missing</message>");
                pw.println("</UploadResponse>");
                return;
            }
            XnatMrsessiondata tempMR =XnatMrsessiondata.getXnatMrsessiondatasById(mr_session_id, user, false);
            if(tempMR==null){
            	ArrayList<XnatMrsessiondata> al=XnatMrsessiondata.getXnatMrsessiondatasByField("xnat:mrSessionData/label",mr_session_id, user, false);
            	if(al.size()>0){
            		tempMR=al.get(0);
            	}
            }
            
            if (tempMR==null){
                pw.println("<UploadResponse status=\"ERROR\" CODE=\"101\">");
                pw.println("<message>Unknown MRSession ID: " + mr_session_id + "</message>");
                pw.println("</UploadResponse>");
                return;
            }
            ParameterParser params = data.getParameters();
            FileItem fi = params.getFileItem("archive");
            if (fi != null )
            {
                String filename = fi.getName();
                int index = filename.lastIndexOf('\\');
                if (index< filename.lastIndexOf('/'))index = filename.lastIndexOf('/');
                if(index>0)filename = filename.substring(index+1);
                String cachePath= ArcSpecManager.GetInstance().getGlobalCachePath();
                cachePath+="user_uploads/"+user.getID() + "/" + session + "/";
                File destination = new File(cachePath);
                if(!destination.exists()){
                    if (!destination.mkdirs()) {
                        logger.warn("It appears that I failed to create the directory: " + destination.getAbsolutePath() + ". If there's some error later, this may be why.");
                    }
                }
                String compressionMethod = ".zip";
                final String normalized = filename.toLowerCase();
                if (normalized.endsWith(".tar")) {
                    compressionMethod = ".tar";
                } else if (normalized.endsWith(".tgz") || normalized.endsWith(".tar.gz")) {
                    compressionMethod = ".tgz";
                } else if (filename.contains(".")) {
                    compressionMethod = filename.substring(filename.lastIndexOf("."));
                }

                if (compressionMethod.equalsIgnoreCase(".tar") ||
                    compressionMethod.equalsIgnoreCase(".gz") ||
                    compressionMethod.equalsIgnoreCase(".tgz") ||
                    compressionMethod.equalsIgnoreCase(".zip") ||
                    compressionMethod.equalsIgnoreCase(".zar")) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Extracting file: " + filename);
                    }

                    InputStream is = fi.getInputStream();

                    ZipI zipper;
                    if (compressionMethod.equalsIgnoreCase(".tar")) {
                        zipper = new TarUtils();
                    } else if (compressionMethod.equalsIgnoreCase(".tgz")) {
                        zipper = new TarUtils();
                        zipper.setCompressionMethod(ZipOutputStream.DEFLATED);
                    } else {
                        zipper = new ZipUtils();
                    }

                    try {
                        zipper.extract(is,cachePath);
                    } catch (Throwable e1) {
                        pw.println("<UploadResponse status=\"ERROR\" CODE=\"102\">");
                        pw.println("<message>" + e1.getMessage() + "</message>");
                        pw.println("</UploadResponse>");
                        return;
                    }
                } else {
                    //PLACE UPLOADED IMAGE INTO FOLDER
                    File uploaded = new File(cachePath + filename) ;
                    fi.write(uploaded);
                }
                fi.delete();
                int counter=0; 
                Date d = Calendar.getInstance().getTime();
                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyyMMdd_hhmmss");
                String uploadID = formatter.format(d);
                String arcPath= tempMR.getRelativeArchivePath();
                arcPath= FileUtils.AppendSlash(arcPath);
                String destinationPath=arcPath + "/UPLOADS/" + uploadID + "/" ;
                File[] listFiles = destination.listFiles();
                for (final File listFile : listFiles) {
                    if (!listFile.isDirectory()) {
                        if (listFile.getName().endsWith(".xml") || listFile.getName().endsWith(".xcat")) {
                            if (listFile.exists()) {
                                FileInputStream fis = new FileInputStream(listFile);
                                XDATXMLReader reader = new XDATXMLReader();
                                try {
                                    BaseElement base = reader.parse(fis);
                                    if (base instanceof CatCatalogBean) {
                                        CatCatalogI cBean = (CatCatalogBean) base;
                                        XnatResourcecatalog cat = new XnatResourcecatalog((UserI) TurbineUtils.getUser(data));
                                        if (cBean.getId() != null) {
                                            cat.setLabel(cBean.getId());
                                        } else {
                                            cat.setLabel(Calendar.getInstance().getTime().toString());
                                        }
                                        for (CatCatalogTagI tag : cBean.getTags_tag()) {
                                            XnatAbstractresourceTag t = new XnatAbstractresourceTag((UserI) TurbineUtils.getUser(data));
                                            t.setTag(tag.getTag());
                                            cat.setTags_tag(t);
                                        }
                                        cat.setUri(destinationPath + listFile.getName());
                                        tempMR.setResources_resource(cat);
                                        counter++;
                                    }
                                } catch (Throwable e) {
                                    logger.error("", e);
                                }
                            }
                        }
                    } else {
                        for (int j = 0; j < listFile.listFiles().length; j++) {
                            if (!listFile.listFiles()[j].isDirectory()) {
                                if (listFile.listFiles()[j].getName().endsWith(".xml") || listFile.getName().endsWith(".xcat")) {
                                    File xml = listFile.listFiles()[j];
                                    if (xml.exists()) {
                                        FileInputStream fis = new FileInputStream(xml);
                                        XDATXMLReader reader = new XDATXMLReader();
                                        try {
                                            BaseElement base = reader.parse(fis);
                                            if (base instanceof CatCatalogBean) {
                                                CatCatalogI cBean = (CatCatalogBean) base;
                                                XnatResourcecatalog cat = new XnatResourcecatalog((UserI) TurbineUtils.getUser(data));
                                                if (cBean.getId() != null) {
                                                    cat.setLabel(cBean.getId());
                                                } else {
                                                    cat.setLabel(Calendar.getInstance().getTime().toString());
                                                }
                                                for (CatCatalogTagI tag : cBean.getTags_tag()) {
                                                    XnatAbstractresourceTag t = new XnatAbstractresourceTag((UserI) TurbineUtils.getUser(data));
                                                    t.setTag(tag.getTag());
                                                    cat.setTags_tag(t);
                                                }
                                                cat.setUri(destinationPath + listFile.getName() + "/" + xml.getName());
                                                tempMR.setResources_resource(cat);
                                                counter++;
                                            }
                                        } catch (Throwable e) {
                                            logger.error("", e);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (counter>0){
                	PersistentWorkflowI workflow =WorkflowUtils.getOrCreateWorkflowData(null, user, tempMR.getXSIType(), tempMR.getId(), tempMR.getProject(),SecureAction.newEventInstance(data, EventUtils.CATEGORY.DATA, EventUtils.ARCPUT));
                	EventMetaI ci=workflow.buildEvent();
                	
                    File dest = new File(FileUtils.AppendRootPath(tempMR.getArchiveRootPath(),destinationPath));
                    FileUtils.MoveDir(destination, dest, true);
                    FileUtils.DeleteFile(destination);
                    try {
                    	SaveItemHelper.authorizedSave(tempMR,user,false,false,ci);
                    	
                        data.setMessage("Files successfully uploaded.");
                        PersistentWorkflowUtils.complete(workflow,ci);
                    } catch (Exception e) {
                        PersistentWorkflowUtils.fail(workflow,ci);
                    	logger.error("",e);
                        pw.println("<UploadResponse status=\"ERROR\" CODE=\"104\">");
                        pw.println("<message>Error updating MR Database Entries.</message>");
                        pw.println("</UploadResponse>");
                        return;
                    }
                }else{
                    pw.println("<UploadResponse status=\"ERROR\" CODE=\"104\">");
                    pw.println("<message>Missing catalog xml.</message>");
                    pw.println("</UploadResponse>");
                    return;
                }
                pw.println("<UploadResponse status=\"COMPLETE\" CODE=\"0\">");
                pw.println("<message>Upload Complete</message>");
                pw.println("</UploadResponse>");
            }
        } catch (FileNotFoundException e) {
            logger.error(e);
            pw.println("<UploadResponse status=\"ERROR\" CODE=\"105\">");
            pw.println("<message>" + e.getMessage() + "</message>");
            pw.println("</UploadResponse>");
        } catch (IOException e) {
            logger.error(e);
            pw.println("<UploadResponse status=\"ERROR\" CODE=\"106\">");
            pw.println("<message>" + e.getMessage() + "</message>");
            pw.println("</UploadResponse>");
        } catch (XFTInitException e) {
            logger.error(e);
            pw.println("<UploadResponse status=\"ERROR\" CODE=\"107\">");
            pw.println("<message>" + e.getMessage() + "</message>");
            pw.println("</UploadResponse>");
        } catch (ElementNotFoundException e) {
            logger.error(e);
            pw.println("<UploadResponse status=\"ERROR\" CODE=\"108\">");
            pw.println("<message>" + e.getMessage() + "</message>");
            pw.println("</UploadResponse>");
        } catch (DBPoolException e) {
            logger.error(e);
            pw.println("<UploadResponse status=\"ERROR\" CODE=\"109\">");
            pw.println("<message>" + e.getMessage() + "</message>");
            pw.println("</UploadResponse>");
        } catch (SQLException e) {
            logger.error(e);
            pw.println("<UploadResponse status=\"ERROR\" CODE=\"110\">");
            pw.println("<message>" + e.getMessage() + "</message>");
            pw.println("</UploadResponse>");
        } catch (FieldNotFoundException e) {
            logger.error(e);
            pw.println("<UploadResponse status=\"ERROR\" CODE=\"111\">");
            pw.println("<message>" + e.getMessage() + "</message>");
            pw.println("</UploadResponse>");
        } catch (FailedLoginException e) {
            logger.error(e);
            pw.println("<UploadResponse status=\"ERROR\" CODE=\"112\">");
            pw.println("<message>" + e.getMessage() + "</message>");
            pw.println("</UploadResponse>");
        } catch (Exception e) {
            logger.error(e);
            pw.println("<UploadResponse status=\"ERROR\" CODE=\"113\">");
            pw.println("<message>" + e.getMessage() + "</message>");
            pw.println("</UploadResponse>");
        }
    }
    @Override
    protected String getContentType(RunData data) {
        return "text/xml";
    }
}
