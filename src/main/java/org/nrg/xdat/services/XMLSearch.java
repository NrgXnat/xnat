/*
 * org.nrg.xdat.services.XMLSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.services;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.axis.AxisEngine;
import org.apache.log4j.Logger;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.XDATUser.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;

/**
 * @author Tim
 *
 */
public class XMLSearch {
	static org.apache.log4j.Logger logger = Logger.getLogger(XMLSearch.class);
    public ArrayList search(String _field,String _comparison,Object _value,String _dataType,Boolean limited) throws RemoteException
    {
        String _username= AxisEngine.getCurrentMessageContext().getUsername();
        String _password= AxisEngine.getCurrentMessageContext().getPassword();
        AccessLogger.LogServiceAccess(_username,"","XMLSearch",_field + " " +  _comparison + " " + _value);
        ArrayList al = new ArrayList();
        try {
            SearchService search = new SearchService();
            search.setUsername(_username);
            search.setPassword(_password);
            search.setField(_field);
            search.setComparison(_comparison);
            search.setValue(_value);
            search.setDataType(_dataType);
            ItemCollection items = search.execute();
            if (items.size()>0)
            {
                Iterator iter = items.getItemIterator();
                while (iter.hasNext())
                {
                    XFTItem item =(XFTItem)iter.next();
                    
                    try {
						Authorizer.getInstance().authorizeRead(item, search.getUser());
						StringWriter sw = new StringWriter();
						BufferedWriter bw = new BufferedWriter(sw);
						SAXWriter writer = new SAXWriter(bw,true);
						writer.setAllowSchemaLocation(true);
						writer.setLocation("schemas/");
						writer.write(item);
						bw.flush();
						bw.close();
						ArrayList child = new ArrayList();
						child.add(item.getUniqueFileName());
						child.add(sw.toString());
						al.add(child);
					} catch (Exception e) {
					}
                }
            }else{
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
            throw new RemoteException(e.getMessage(),e);
        } catch (Exception e) {
            logger.error("",e);
            throw new RemoteException("",e);
        }
        return al;
    }
    
    
    public static ArrayList Search(String _field,String _comparison,Object _value,String _dataType,Boolean limited) throws RemoteException
    {
        return (new XMLSearch()).search(_field,_comparison,_value,_dataType,limited);
    }
}
