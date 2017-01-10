/*
 * web: org.nrg.xnat.ajax.QuickSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class QuickSearch {

    static org.apache.log4j.Logger logger = Logger.getLogger(StoreSubject.class);
    public void execute(HttpServletRequest req, HttpServletResponse response) throws IOException{
        String xmlString = req.getParameter("search");
        UserI user = XDAT.getUserDetails();
        if (user!=null){
            StringReader sr = new StringReader(xmlString);
            InputSource is = new InputSource(sr);
            XnatSubjectdata subject=null;
            
            boolean successful=false;
            SAXReader reader = new SAXReader(user);
            try {
                
                StringBuffer sb = new StringBuffer();

                sb.append("<matchingExperiments xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");

                XFTItem item = reader.parse(is);
                
                XdatStoredSearch xss = new XdatStoredSearch(item);
                ItemSearch search= xss.getItemSearch(user);
                ItemCollection items =search.exec(false);
                Iterator iter = items.iterator();
                while(iter.hasNext())
                {
                    XFTItem hash = (XFTItem)iter.next();
                    XnatExperimentdata expt = (XnatExperimentdata)BaseElement.GetGeneratedItem(hash);
                    
                    sb.append("<Experiment" +
                            " ID=\"" + expt.getId() + "\"" +
                            " project=\"" + expt.getProject() + "\"" +
                            " label=\"" + expt.getLabel() + "\"" +
                            " insert_date=\"" + expt.getInsertDate() + "\"" +
                            " insert_user=\"" + expt.getInsertUser().getLogin() + "\"" +
                            " xsi:type=\"" + expt.getXSIType() + "\"" + 
                            ">");

                    sb.append("<projects>");
                    List<XnatExperimentdataShareI> projects = expt.getSharing_share();
                    for (XnatExperimentdataShareI project : projects){
                        sb.append("<project label=\"" + project.getLabel()+"\">");
                        sb.append(project.getProject());
                        sb.append("</project>");
                    }
                    sb.append("</projects>");
                    sb.append("</Experiment>");
                }
                sb.append("</matchingExperiments>");

                response.setContentType("text/xml");
                response.setHeader("Cache-Control", "no-cache");
                response.getWriter().write(sb.toString());
            } catch (SAXException e) {
                logger.error("",e);
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            } catch (Exception e) {
                logger.error("",e);
            }
        }
    }
}
