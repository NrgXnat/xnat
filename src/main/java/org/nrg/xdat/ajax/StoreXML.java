/*
 * core: org.nrg.xdat.ajax.StoreXML
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.ajax;

import java.io.IOException;
import java.io.StringReader;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StoreXML {
    static org.apache.log4j.Logger logger = Logger.getLogger(StoreXML.class);
    public void execute(HttpServletRequest req, HttpServletResponse response,ServletConfig sc) throws IOException{
        String xmlString = req.getParameter("xml");
        String allowDString = req.getParameter("allowDataDeletion");
        boolean allowDataDeletion = true;
        if (allowDString!=null){
            allowDataDeletion=Boolean.valueOf(allowDString).booleanValue();
        }
        response.setContentType("text/plain");
        response.setHeader("Cache-Control", "no-cache");
        UserI user = XDAT.getUserDetails();
        if (user!=null){
            StringReader sr = new StringReader(xmlString);
            InputSource is = new InputSource(sr);
            
            SAXReader reader = new SAXReader(user);
            try {

                XFTItem item = reader.parse(is);
                
				SaveItemHelper.unauthorizedSave(item, user, false, allowDataDeletion,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.SOAP, "Stored XML", req.getParameter(EventUtils.EVENT_REASON), req.getParameter(EventUtils.EVENT_COMMENT)));
                
                SAXWriter writer = new SAXWriter(response.getOutputStream(),false);
                writer.setWriteHiddenFields(true);
                writer.write(item);
                return;
            } catch (SAXException e) {
                logger.error("",e);
                response.getWriter().write("<error msg=\"" + e.getMessage() + "\"/>");
            } catch (XFTInitException e) {
                logger.error("",e);
                response.getWriter().write("<error msg=\"" + e.getMessage() + "\"/>");
            } catch (ElementNotFoundException e) {
                logger.error("",e);
                response.getWriter().write("<error msg=\"" + e.getMessage() + "\"/>");
            } catch (FieldNotFoundException e) {
                logger.error("",e);
                response.getWriter().write("<error msg=\"" + e.getMessage() + "\"/>");
            } catch (Exception e) {
                logger.error("",e);
                response.getWriter().write("<error msg=\"" + e.getMessage() + "\"/>");
            }

        }else{

            response.getWriter().write("<error msg=\"Invalid User\"/>");
        }
    }
}
