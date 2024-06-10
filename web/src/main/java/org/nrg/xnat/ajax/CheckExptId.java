/*
 * web: org.nrg.xnat.ajax.CheckExptId
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Hashtable;
public class CheckExptId {
    static org.apache.log4j.Logger logger = Logger.getLogger(StoreSubject.class);
    public void execute(HttpServletRequest req, HttpServletResponse response) throws IOException{
        String expt_id = req.getParameter("id");
        StringBuffer sb = new StringBuffer();
        UserI user = XDAT.getUserDetails();
        if (user!=null){
        sb.append("<matchingExperiments>");
        if (expt_id !=null && !expt_id.trim().equals("") && !(expt_id.contains("\\") || expt_id.contains("'")))
        {
            try {
                GenericWrapperElement e = GenericWrapperElement.GetElement("xnat:experimentData");
                
                XFTTable table = XFTTable.Execute("SELECT id, insert_date, login, me.element_name FROM xnat_experimentData ed " +
                        " LEFT JOIN xnat_experimentData_meta_data edm ON ed.experimentData_info=edm.meta_data_id" +
                        " LEFT JOIN xdat_user u ON edm.insert_user_xdat_user_id=u.xdat_user_id " +
                        " LEFT JOIN xdat_meta_element me ON ed.extension=me.xdat_meta_element_id" +
                        " WHERE ID='" + expt_id +"';", e.getDbName(), "system");
                System.out.println(table.size() + " Matches Found.");
                table.resetRowCursor();
                while(table.hasMoreRows())
                {
                    Hashtable hash = table.nextRowHash();
                    String id = (String)hash.get("id");
                    Object date = hash.get("insert_date");
                    String login = (String)hash.get("login");
                    String element = (String)hash.get("element_name");
                    
                    sb.append("<Experiment" +
                            " id=\"" + id + "\"" +
                            " create_date=\"" + date + "\"" +
                            " create_user=\"" + login + "\"" +
                            " element=\"" + element + "\"" +
                            "/>");
                }
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (SQLException e) {
                logger.error("",e);
            } catch (DBPoolException e) {
                logger.error("",e);
            }
        }else{
            
        }
        sb.append("</matchingExperiments>");
        }
        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(sb.toString());
    }
}
