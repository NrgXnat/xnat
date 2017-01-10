/*
 * web: org.nrg.xnat.turbine.modules.screens.GetFileCatalog
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Hashtable;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.turbine.utils.CatalogSet;
import org.nrg.xnat.turbine.utils.XNATUtils;

public class GetFileCatalog extends RawScreen {
    static org.apache.log4j.Logger logger = Logger.getLogger(GetFileCatalog.class);
    
    /**
    * Set the content type to Xml. (see RawScreen)
    *
    * @param data Turbine information.
    * @return content type.
    */
    public String getContentType(RunData data)
    {
        return "text/xml";
    };
    
    protected final void doOutput(RunData data) 
    {
        AccessLogger.LogScreenAccess(data);
        HttpServletResponse response = data.getResponse();
        long startTime = Calendar.getInstance().getTimeInMillis();
             UserI user = null;
             String log = "";
             byte[] buf = new byte[FileUtils.SMALL_DOWNLOAD];
             try {
                 Hashtable session = null;
                 if (TurbineUtils.getUser(data)!=null){
                     user = TurbineUtils.getUser(data);
                 }

                 if (user==null){
                     response.sendError(401);
                 }
                 
                 TurbineUtils.setUser(data, user);
                 
                 XFTItem item= TurbineUtils.GetItemBySearch(data);
                                  
                 if (item==null){
                     response.sendError(404);
                 }
                
                 CatalogSet catalogSet= XNATUtils.getCatalogBean(data, item);
                 
                 CatCatalogBean catalog = catalogSet.catalog;

                 catalog.setId(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data)));
                 
                 try {
                     final String identifier = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)) + ":"+ ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)) + ":"+ ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data));
                     
                     data.getSession().setAttribute(identifier, catalogSet.hash);
                     response.setContentType("text/xml");
                     ServletOutputStream out = response.getOutputStream();
                     
                     OutputStreamWriter sw = new OutputStreamWriter(out);
                     catalog.toXML(sw, false);
                 
                     sw.flush();
                     sw.close();
                     
                     
                 } catch (IOException e) {
                     logger.error("",e);
                     response.sendError(404);
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
//    
//    @Override
//    public void finalProcessing(RunData data, Context context) {
//        CatalogSet catalogSet= XNATUtils.getCatalogBean(data, (XFTItem)item);
//        
//        CatCatalogBean catalog = catalogSet.catalog;
//        
//        try {
//            final String identifier = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)) + ":"+ ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)) + ":"+ ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data));
//            
//            HttpServletResponse response = data.getResponse();
//            data.getSession().setAttribute(identifier, catalogSet.hash);
//            response.setContentType("text/xml");
//            ServletOutputStream out = response.getOutputStream();
//            
//            OutputStreamWriter sw = new OutputStreamWriter(out);
//            catalog.toXML(sw, false);
//        
//            sw.flush();
//            sw.close();
//            
//            
//        } catch (IOException e) {
//            logger.error("",e);
//        }
//        
//    }

}
