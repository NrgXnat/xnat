/*
 * web: org.nrg.xnat.ajax.RequestSearchXML
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;
import org.nrg.xft.security.UserI;

public class RequestSearchXML {


    static org.apache.log4j.Logger logger = Logger.getLogger(RequestSearchXML.class);

    public void execute(HttpServletRequest req, HttpServletResponse response,ServletConfig sc) throws IOException{
        String bundle = req.getParameter("bundleID");

        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "no-cache");
        
        HttpSession session = req.getSession();
        UserI user = XDAT.getUserDetails();
        

        if (user!=null){
            XdatStoredSearch xss = XdatStoredSearch.GetPreLoadedSearch(bundle,true);
            if (xss!=null){
                try {
                    SAXWriter writer = new SAXWriter(response.getOutputStream(),false);
                    writer.setAllowSchemaLocation(false);
                    writer.write(xss.getItem(),false);
                } catch (Throwable e) {
                    logger.error("",e);
                }
            }
        }
        
        //System.out.print("Monitor Progress " + uploadID + "... ");
    }
}
