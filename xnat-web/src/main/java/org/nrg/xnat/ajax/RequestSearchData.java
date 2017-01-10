/*
 * web: org.nrg.xnat.ajax.RequestSearchData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax;

import org.apache.log4j.Logger;
import org.apache.turbine.services.rundata.RunDataService;
import org.apache.turbine.services.rundata.TurbineRunDataFacade;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;
import org.apache.turbine.util.parser.CookieParser;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.exceptions.IllegalAccessException;
import org.nrg.xdat.presentation.HTMLPresenter;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.xml.sax.InputSource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.Hashtable;

public class RequestSearchData {
    static org.apache.log4j.Logger logger = Logger.getLogger(RequestSearchData.class);

    public void init(HttpServletRequest req, HttpServletResponse response,ServletConfig sc) throws IOException{
        String xmlString = req.getParameter("search");
        
        HttpSession session = req.getSession();
        UserI user = XDAT.getUserDetails();    

        if (user!=null){
            StringReader sr = new StringReader(xmlString);
            InputSource is = new InputSource(sr);
            
            RunDataService rundataService = null;
            rundataService = TurbineRunDataFacade.getService();
            try {
                if (rundataService == null)
                {
                    throw new TurbineException(
                            "No RunData Service configured!");
                }
                RunData data = rundataService.getRunData(req, response, sc);
                Context context = TurbineVelocity.getContext(data);

                String isNew =req.getParameter("isNew");

                boolean successful=false;
                SAXReader reader = new SAXReader(user);
                XFTItem item = reader.parse(is);
                XdatStoredSearch search = new XdatStoredSearch(item);
                
                if(!Permissions.canQuery(user, search.getRootElementName())){
                    return;
    			}
                
                DisplaySearch ds=null;
                if (isNew!=null)
                {
                    ds = search.getDisplaySearch(user);
                }else{
                    ds = (DisplaySearch)session.getAttribute(search.getId() + "DS");
                    if (ds==null){
                        ds = search.getDisplaySearch(user);
                    }
                }

                String sortBy = req.getParameter("sortBy");
                String sortOrder = req.getParameter("sortOrder");
                if (sortBy != null){
                    ds.setSortBy(sortBy);
                    if(sortOrder != null)
                    {
                        ds.setSortOrder(sortOrder);
                    }
                }
                
                CookieParser cp = data.getCookies();
                
                Integer numToDisplay = null;
                String num =req.getParameter("rows");
                if (num!=null){
                    numToDisplay=Integer.valueOf(num);
                    if (cp.getIntObject("numToDisplay")!=null && numToDisplay.equals(cp.getIntObject("numToDisplay"))){
                        numToDisplay=null;
                    }
                }                               
                
                if (numToDisplay != null)
                {
                    org.apache.turbine.util.uri.TurbineURI dui = new org.apache.turbine.util.uri.TurbineURI(data, "/");
                    dui.removePathInfo();
                    dui.setScriptName("/");
                    cp.setCookiePath(dui);
                    cp.set("numToDisplay", numToDisplay.toString(), 60*60*24*365);
                }else
                {
                    if (cp.containsKey("numToDisplay"))
                    {
                        numToDisplay = cp.getIntObject("numToDisplay");
                    }else{
                        numToDisplay = new Integer(40);
                    }
                }

                ds.setRowsPerPage(numToDisplay.intValue());
                ds.setPagingOn(true);
                
                try {
                    ds.execute(new HTMLPresenter(TurbineUtils.GetContext(),false),TurbineUtils.getUser(data).getLogin());
                    StringBuffer sb = new StringBuffer();
                    sb.append("<results ");
                    sb.append(" numPages=\"" + ds.getPages() +"\"");
                    sb.append(" currentPage=\"" + ds.getCurrentPageNum() +"\"");
                    sb.append(" totalRecords=\"" + ds.getNumRows() +"\"");
                    sb.append(" numToDisplay=\"" + ds.getRowsPerPage() +"\"");
                    sb.append(">");
                   
                    session.setAttribute(search.getId()+"DS", ds);
                    
                    sb.append("</results>");

                    response.setContentType("text/xml");
                    response.setHeader("Cache-Control", "no-cache");
                    response.getWriter().write(sb.toString());
                } catch (XFTInitException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                } catch (DBPoolException e) {
                    logger.error("",e);
                } catch (SQLException e) {
                    logger.error("",e);
                } catch (IllegalAccessException e) {
                    logger.error("",e);
                } catch (Exception e) {
                    logger.error("",e);
                }
            } catch (Throwable e) {
                logger.error("",e);
            }
        }
        
        //System.out.print("Monitor Progress " + uploadID + "... ");
    }
    

    public void loadPage(HttpServletRequest req, HttpServletResponse response,ServletConfig sc) throws IOException{
        String id = req.getParameter("search");
        String pageS = req.getParameter("page");
        
        Integer page = null;
        try {
            page = Integer.parseInt(pageS);
        } catch (NumberFormatException e1) {
            logger.error("",e1);
            page=new Integer(0);
        }
        
        
        HttpSession session = req.getSession();
        UserI user = XDAT.getUserDetails();
        
        if (user!=null){
            
            RunDataService rundataService = null;
            rundataService = TurbineRunDataFacade.getService();
            try {
                if (rundataService == null)
                {
                    throw new TurbineException(
                            "No RunData Service configured!");
                }
                RunData data = rundataService.getRunData(req, response, sc);
                Context context = TurbineVelocity.getContext(data);
                
                StringBuffer sb = new StringBuffer();
                DisplaySearch ds = (DisplaySearch)session.getAttribute(id + "DS");
                if (ds!=null){
                    XFTTableI table = ds.getPage(page, new HTMLPresenter(TurbineUtils.GetContext(),false), user.getLogin());

                    if (table.size()>0){
                        Hashtable tableProps = new Hashtable();
                        tableProps.put("class","dataTable"); 
//                        tableProps.put("bgColor","white"); 
//                        tableProps.put("border","0"); 
                        tableProps.put("cellPadding","0"); 
                        tableProps.put("cellSpacing","0"); 
//                        tableProps.put("width","95%"); 
                        String tableS = table.toHTML(false,null,null,tableProps,(ds.getCurrentPageNum() * ds.getRowsPerPage())+ 1);
                        sb.append(tableS);
                    }
                }
                
                response.setContentType("text/html");
                response.setHeader("Cache-Control", "no-cache");
                response.getWriter().write(sb.toString());

            } catch (Throwable e) {
                logger.error("",e);
            }
        }
        
        //System.out.print("Monitor Progress " + uploadID + "... ");
    }
}
