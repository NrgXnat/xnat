/*
 * core: org.nrg.xdat.services.Browse
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.services;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.log4j.Logger;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.XFTTool;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * @author timo
 *
 */
public class Browse {
	static org.apache.log4j.Logger logger = Logger.getLogger(Browse.class);
    public ArrayList search(String _dataType,Date _date) throws RemoteException
    {
        final MessageContext messageContext = AxisEngine.getCurrentMessageContext();
        String               _username             = messageContext.getUsername();
        String               _password             = messageContext.getPassword();
        AccessLogger.LogServiceAccess(_username, messageContext,"Browse",_dataType + " since " + _date);
        ArrayList al = new ArrayList();
        try {
            String elementName = XFTTool.GetValidElementName(_dataType);

			boolean valid = XFTTool.ValidateElementName(elementName);
			if (! valid)
			{
			    throw new Exception("Invalid Element Name: " + elementName);
			}
			
			UserI user = Authenticator.Authenticate(new Authenticator.Credentials(_username,_password));
            if (user == null)
            {
                throw new Exception("Invalid User.");
            }
			QueryOrganizer qo = new QueryOrganizer(elementName,user,ViewManager.ALL);
			
            SchemaElement se = SchemaElement.GetElement(elementName);
            ArrayList keys = se.getAllPrimaryKeys();
            Iterator keyIter = keys.iterator();
            String pk = null;
            while (keyIter.hasNext())
            {
                SchemaField sf = (SchemaField)keyIter.next();
                pk = sf.getXMLPathString(elementName);
                qo.addField(pk);
            }
            
            String insert_date = elementName + XFT.PATH_SEPARATOR + "meta" + XFT.PATH_SEPARATOR + "insert_date";
            String mod_date = elementName + XFT.PATH_SEPARATOR + "meta" + XFT.PATH_SEPARATOR + "last_modified";
            qo.addField(insert_date);
            qo.addField(mod_date); 
			
            String query =null;
            if (_date != null)
            {
    			CriteriaCollection cc =new CriteriaCollection("OR");
    			cc.addClause(insert_date,">=",_date);
    			cc.addClause(mod_date,">=",_date);
    			
    			query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH";
    			query += " WHERE " + cc.getSQLClause(qo);
    			query += ";";
            }else{
    			query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH";
    			query += ";";                
            }

			
			XFTTable table = XFTTable.Execute(query,se.getDbName(),user.getLogin());
			
			
			String colname= qo.translateXMLPath(pk);

		    al.add(pk);
						
		    ArrayList child = new ArrayList();
			table.resetRowCursor();
			while (table.hasMoreRows())
			{
			    Hashtable row = table.nextRowHash();
			    child.add(row.get(colname.toLowerCase()));
			}
			child.trimToSize();
			al.add(child);
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

    public ArrayList search(String session_id,String _dataType,Date _date) throws RemoteException
    {
        final MessageContext messageContext = AxisEngine.getCurrentMessageContext();
        final UserI          user           = (UserI) messageContext.getSession().get("user");
        if (user == null) {
            throw new RemoteException("Invalid User for session ID: " + session_id);
        }
        AccessLogger.LogServiceAccess(user.getUsername(), messageContext, "Browse", _dataType + " since " + _date);
        ArrayList al = new ArrayList();
        try {

            String elementName = XFTTool.GetValidElementName(_dataType);

			boolean valid = XFTTool.ValidateElementName(elementName);
			if (! valid)
			{
			    throw new Exception("Invalid Element Name: " + elementName);
			}
			QueryOrganizer qo = new QueryOrganizer(elementName,user,ViewManager.ALL);
			
            SchemaElement se = SchemaElement.GetElement(elementName);
            ArrayList keys = se.getAllPrimaryKeys();
            Iterator keyIter = keys.iterator();
            String pk = null;
            while (keyIter.hasNext())
            {
                SchemaField sf = (SchemaField)keyIter.next();
                pk = sf.getXMLPathString(elementName);
                qo.addField(pk);
            }
            
            String insert_date = elementName + XFT.PATH_SEPARATOR + "meta" + XFT.PATH_SEPARATOR + "insert_date";
            String mod_date = elementName + XFT.PATH_SEPARATOR + "meta" + XFT.PATH_SEPARATOR + "last_modified";
            qo.addField(insert_date);
            qo.addField(mod_date); 
			
            String query =null;
            if (_date != null)
            {
    			CriteriaCollection cc =new CriteriaCollection("OR");
    			cc.addClause(insert_date,">=",_date);
    			cc.addClause(mod_date,">=",_date);
    			
    			query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH";
    			query += " WHERE " + cc.getSQLClause(qo);
    			query += ";";
            }else{
    			query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH";
    			query += ";";                
            }

			
			XFTTable table = XFTTable.Execute(query,se.getDbName(),user.getLogin());
			
			
			String colname= qo.translateXMLPath(pk);

		    al.add(pk);
						
		    ArrayList child = new ArrayList();
			table.resetRowCursor();
			while (table.hasMoreRows())
			{
			    Hashtable row = table.nextRowHash();
			    child.add(row.get(colname.toLowerCase()));
			}
			child.trimToSize();
			al.add(child);
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
    
    public static ArrayList Search(String _dataType,Date _date) throws RemoteException
    {
        return (new Browse()).search(_dataType,_date);
    }
    
    public static ArrayList Search(String session_id,String _dataType,Date _date) throws RemoteException
    {
        return (new Browse()).search(session_id,_dataType,_date);
    }
}
