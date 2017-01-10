/*
 * web: org.nrg.xnat.turbine.modules.screens.GetFile
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Hashtable;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.turbine.utils.CatalogSet;
import org.nrg.xnat.turbine.utils.XNATUtils;

import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBFileInputStream;

public class GetFile extends RawScreen {
    static org.apache.log4j.Logger logger = Logger.getLogger(GetFile.class);

    /**
    * Set the content type to Xml. (see RawScreen)
    *
    * @param data Turbine information.
    * @return content type.
    */
    public String getContentType(RunData data)
    {
        return "application/octet-stream";
    };


    /**
    * Overrides & finalizes doOutput in RawScreen to serve the output stream
 created in buildPDF.
    *
    * @param data RunData
    * @exception Exception, any old generic exception.
    */
    protected final void doOutput(RunData data)
    {
        long startTime = Calendar.getInstance().getTimeInMillis();
             String search_element = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data));
             String search_field = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data));
             String search_value = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data));
             String key = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("key",data));
             Integer fileId = ((Integer)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedInteger("file",data));
             UserI user = null;
             String log = "";
             HttpSession httpSession = data.getRequest().getSession();

             HttpServletResponse response = data.getResponse();
             byte[] buf = new byte[FileUtils.LARGE_DOWNLOAD];
             try {
                 if (TurbineUtils.getUser(data)!=null){
                     user = TurbineUtils.getUser(data);
                 }

                 if (user==null){
                     try {
                         response.sendError(401);
                     } catch (IOException e1) {
                         logger.error("",e1);
                     }
                 }

                 AccessLogger.LogScreenAccess(data);

                 log +="," + (Calendar.getInstance().getTimeInMillis()-startTime);
                 startTime = Calendar.getInstance().getTimeInMillis();

                 if (search_element!=null && search_field!=null && search_value!=null && fileId!=null)
                 {
                     Object o = null;

                     String innerKey= "";
                     if (key!=null){
                         innerKey="/key/" + key;
                     }
                     innerKey+="/file/" + fileId;
                     final String identifier = search_element + ":"+ search_field + ":"+ search_value;
                     if (httpSession.getAttribute(identifier)!=null){
                         Hashtable<String,Object> hash = (Hashtable<String,Object>)httpSession.getAttribute(identifier);

                         o = hash.get(innerKey);
                     }else{
                         CatalogSet catalogSet= XNATUtils.getCatalogBean(data, TurbineUtils.GetItemBySearch(data));

                         CatCatalogBean catalog = catalogSet.catalog;
                         data.getSession().setAttribute(identifier, catalogSet.hash);


                         o = catalogSet.hash.get(innerKey);
                     }

                     if (o instanceof File){
                         File f = (File)o;
                         if (f.exists()){
                             TurbineUtils.setContentDisposition(data.getResponse(), f.getName(), false);
                             java.io.FileInputStream in = new java.io.FileInputStream(f);
                             logger.debug(f.getName() + log + "," + (Calendar.getInstance().getTimeInMillis()-startTime));
                             int len;
                             while ((len = in.read(buf)) > 0) {
                                 data.getResponse().getOutputStream().write(buf, 0, len);
                             }

                         }
                     }else if (o instanceof SRBFile){
                         SRBFile f = (SRBFile)o;
                         if (f.exists()){
                             TurbineUtils.setContentDisposition(data.getResponse(), f.getName(), false);

                             byte[] tempBUF = new byte[FileUtils.LARGE_DOWNLOAD];
                             SRBFileInputStream is = new SRBFileInputStream(f);
                             // Transfer bytes from the file to the ZIP file
                             int len;

                             startTime = Calendar.getInstance().getTimeInMillis();

                             while ((len = is.read(tempBUF)) > 0) {
                                 data.getResponse().getOutputStream().write(tempBUF, 0, len);
                                 data.getResponse().getOutputStream().flush();
                             }


                         }
                     }
                 }
             } catch (XFTInitException e) {
                 logger.error("",e);
                 try {
                     response.sendError(500);
                 } catch (IOException e1) {
                     logger.error("",e1);
                 }
             } catch (ElementNotFoundException e) {
                 logger.error("",e);
                 try {
                     response.sendError(500);
                 } catch (IOException e1) {
                     logger.error("",e1);
                 }
             } catch (DBPoolException e) {
                 logger.error("",e);
                 try {
                     response.sendError(500);
                 } catch (IOException e1) {
                     logger.error("",e1);
                 }
             } catch (SQLException e) {
                 logger.error("",e);
                 try {
                     response.sendError(500);
                 } catch (IOException e1) {
                     logger.error("",e1);
                 }
             } catch (FieldNotFoundException e) {
                 logger.error("",e);
                 try {
                     response.sendError(500);
                 } catch (IOException e1) {
                     logger.error("",e1);
                 }
             } catch (FailedLoginException e) {
                 logger.error("",e);
                 try {
                     response.sendError(401);
                 } catch (IOException e1) {
                     logger.error("",e1);
                 }
             } catch (Exception e) {
                 logger.error("",e);
                 try {
                     response.sendError(500);
                 } catch (IOException e1) {
                     logger.error("",e1);
                 }
             }


    }
 }
