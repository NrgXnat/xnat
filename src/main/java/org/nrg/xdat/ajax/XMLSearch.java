//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Nov 26, 2007
 *
 */
package org.nrg.xdat.ajax;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;
import org.nrg.xft.search.ItemSearch;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLSearch {
    static org.apache.log4j.Logger logger = Logger.getLogger(XMLSearch.class);
    public void execute(HttpServletRequest req, HttpServletResponse response,ServletConfig sc) throws IOException{
        String xmlString = req.getParameter("search");
        String allowMultiples = req.getParameter("allowMultiples");
        boolean allowChildren = true;
        if (allowMultiples!=null){
            allowChildren=Boolean.valueOf(allowMultiples).booleanValue();
        }
        XDATUser user = (XDATUser)req.getSession().getAttribute("user");
        if (user!=null){
            StringReader sr = new StringReader(xmlString);
            InputSource is = new InputSource(sr);
            
            response.setContentType("text/plain");
            response.setHeader("Cache-Control", "no-cache");
            boolean successful=false;
            SAXReader reader = new SAXReader(user);
            try {

                XFTItem item = reader.parse(is);
                
                XdatStoredSearch xss = new XdatStoredSearch(item);
                ItemSearch search= xss.getItemSearch(user);
                ItemCollection items =search.exec(allowChildren);
                if (items.size()>1 || items.size()==0){
                    response.getWriter().write("<matchingResults xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
                    Iterator iter = items.iterator();
                    while(iter.hasNext())
                    {
                        XFTItem next = (XFTItem)iter.next();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        response.getWriter().write("<matchingResult>");
                        
                        try {
                            SAXWriter writer = new SAXWriter(baos,false);
                            writer.setWriteHiddenFields(true);
                            writer.write(next);
                        } catch (TransformerConfigurationException e) {
                            logger.error("",e);
                        } catch (TransformerFactoryConfigurationError e) {
                            logger.error("",e);
                        } catch (FieldNotFoundException e) {
                            logger.error("",e);
                        }
                        response.getWriter().write(baos.toString());
                        response.getWriter().flush();
                        
                        response.getWriter().write("</matchingResult>");
                    }
                    response.getWriter().write("</matchingResults>");
                }else{
                    XFTItem next = (XFTItem)items.first();
                        
                        try {
                            SAXWriter writer = new SAXWriter(response.getOutputStream(),false);
                            writer.setWriteHiddenFields(true);
                            
                            writer.write(next);
                        } catch (TransformerConfigurationException e) {
                            logger.error("",e);
                        } catch (TransformerFactoryConfigurationError e) {
                            logger.error("",e);
                        } catch (FieldNotFoundException e) {
                            logger.error("",e);
                        }
                        
                }

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
