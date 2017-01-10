/*
 * web: org.nrg.xnat.services.SessionUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.log4j.Logger;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;

/**
 * @author timo
 *
 */
public class SessionUtils {

    static org.apache.log4j.Logger logger = Logger.getLogger(SessionUtils.class);
    public ArrayList getSessionScanIds(String _id) throws RemoteException
    {
        String _username= AxisEngine.getCurrentMessageContext().getUsername();
        String _password= AxisEngine.getCurrentMessageContext().getPassword();
        AccessLogger.LogServiceAccess(_username,"","SessionUtils:getSessionScanIds",_id);
        ArrayList al = new ArrayList();
        try {
            MessageContext mc = AxisEngine.getCurrentMessageContext();
            UserI user =(UserI) mc.getSession().get("user");
            if(user!=null){
                if (_username != null && _password !=null)
                {
                    user = Authenticator.Authenticate(new Authenticator.Credentials(_username,_password));
                    mc.getSession().invalidate();
                }
            }
            
            if (user == null)
            {
                throw new Exception("Invalid User.");
            }
            
            QueryOrganizer qo = new QueryOrganizer("xnat:mrScanData",user,ViewManager.ALL);
			qo.addField("xnat:mrScanData/id");
			qo.addField("xnat:mrScanData/image_session_id");
			qo.addField("xnat:mrScanData/type");
			
			CriteriaCollection cc =new CriteriaCollection("AND");
			cc.addClause("xnat:mrScanData/image_session_id","=",_id);
			
			String query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH";
			query += " WHERE " + cc.getSQLClause(qo);
			query += ";";
			
			XFTTable table = XFTTable.Execute(query,SchemaElement.GetElement("xnat:mrScanData").getDbName(),user.getLogin());
			
			
			String scan_idN= qo.translateXMLPath("xnat:mrScanData/id");
			String typeN= qo.translateXMLPath("xnat:mrScanData/type");
			
			Hashtable hash = new Hashtable();
						
			table.resetRowCursor();
			while (table.hasMoreRows())
			{
			    Hashtable row = table.nextRowHash();
			    
			    String scan_id = (String)row.get(scan_idN.toLowerCase());
			    String type = (String)row.get(typeN.toLowerCase());
			    
			    if (hash.get(type)==null)
			    {
			        hash.put(type,new ArrayList());
			    }
			    
			    ((ArrayList)hash.get(type)).add(scan_id);
			}
			
			Enumeration e = hash.keys();
			while (e.hasMoreElements())
			{
			    String type = (String) e.nextElement();
			    ArrayList ids = (ArrayList)hash.get(type);
			    
			    ArrayList child = new ArrayList();
			    child.add(type);
			    child.add(new Integer(ids.size()));
			    child.add(ids);
			    al.add(child);
			}
			
            al.trimToSize();
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (DBPoolException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (SQLException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (FailedLoginException e) {
            logger.error("",e);
            throw new RemoteException("",e);
        } catch (Exception e) {
            logger.error("",e);
            throw new RemoteException("",e);
        }
        return al;
    }
    
    
    public static ArrayList GetSessionScanIds(String _id) throws RemoteException
    {
        return (new SessionUtils()).getSessionScanIds(_id);
    }
}
