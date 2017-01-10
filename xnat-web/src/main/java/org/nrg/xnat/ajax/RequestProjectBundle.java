/*
 * web: org.nrg.xnat.ajax.RequestProjectBundle
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.turbine.services.rundata.RunDataService;
import org.apache.turbine.services.rundata.TurbineRunDataFacade;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.exceptions.IllegalAccessException;
import org.nrg.xdat.om.XnatAbstractprotocol;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

public class RequestProjectBundle {
    private final Logger logger = Logger.getLogger(RequestProjectBundle.class);

    public void execute(HttpServletRequest req, HttpServletResponse response,ServletConfig sc) throws IOException{
        String projectID = req.getParameter("project");
        String protocol = req.getParameter("protocol");
        String bundle = req.getParameter("bundle");

        response.setContentType("text/html");
        response.setHeader("Cache-Control", "no-cache");
        
        HttpSession session = req.getSession();
        UserI user = XDAT.getUserDetails();
        
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
            
            DisplaySearch ds = null;
            try {
                if (bundle==null){
                    bundle = protocol;
                }
                
                if (bundle!=null)
                {
                    XdatStoredSearch xss = XdatStoredSearch.GetPreLoadedSearch(bundle,true);
                    ds = xss.getDisplaySearch(user);
                }
                
                if (ds==null){
                    ArrayList al = XnatAbstractprotocol.getXnatAbstractprotocolsByField("xnat:abstractProtocol.ID", protocol, user, false);
                    
                    XnatAbstractprotocol protocolOM = null;
                    if (al.size()>0)
                    {
                        protocolOM =(XnatAbstractprotocol) al.get(0);
                        
                        ds =UserHelper.getSearchHelperService().getSearchForUser(user,protocolOM.getDataType(), "listing");
                        CriteriaCollection cc = new CriteriaCollection("OR");
//                        DisplayCriteria dc = new DisplayCriteria();
//                        dc.setSearchFieldByDisplayField(protocolOM.getDataType(), "PROJECTS");
//                        dc.setComparisonType(" LIKE ");
//                        dc.setOverrideDataFormatting(true);
//                        dc.setValue( "'%<" + projectID + ">%'");
//                        cc.addClause(dc);
                        cc.addClause(protocolOM.getDataType()+"/sharing/share/project", "=", projectID);
                        cc.addClause(protocolOM.getDataType()+".PROJECT", "=", projectID);
                        ds.addCriteria(cc);
                        
                    }
                }
            } catch (Exception e) {
                logger.error("",e);
            }
            
            ds.setPagingOn(false);
            try {
                ds.execute(new org.nrg.xdat.presentation.HTMLPresenter(TurbineUtils.GetContext(),false),TurbineUtils.getUser(data).getLogin());
                
                TurbineUtils.setSearch(data,ds);
                
                XFTTableI table = ds.getPresentedTable();

                Hashtable tableProps = new Hashtable();
                tableProps.put("bgColor","white"); 
                tableProps.put("border","0"); 
                tableProps.put("cellPadding","0"); 
                tableProps.put("cellSpacing","0"); 
                tableProps.put("width","95%"); 
                table.toHTML(false,null,null,tableProps,(ds.getCurrentPageNum() * ds.getRowsPerPage())+ 1,response.getOutputStream());
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
        } catch (TurbineException e) {
            logger.error("",e);
        }
        
        //System.out.print("Monitor Progress " + uploadID + "... ");
    }
    
}
