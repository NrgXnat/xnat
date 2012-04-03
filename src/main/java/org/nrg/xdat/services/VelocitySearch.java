//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 9, 2006
 *
 */
package org.nrg.xdat.services;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.axis.AxisEngine;
import org.apache.log4j.Logger;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XDATUser.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
/**
 * @author Tim
 *
 */
public class VelocitySearch{
	static org.apache.log4j.Logger logger = Logger.getLogger(VelocitySearch.class);
    public String search(String _field,String _comparison,Object _value,String _dataType)  throws RemoteException
    {
        String _username= AxisEngine.getCurrentMessageContext().getUsername();
        String _password= AxisEngine.getCurrentMessageContext().getPassword();
        AccessLogger.LogServiceAccess(_username,"","VelocitySearch",_field + " " +  _comparison + " " + _value);
        try {
            SearchService search = new SearchService();
            search.setUsername(_username);
            search.setPassword(_password);
            search.setField(_field);
            search.setComparison(_comparison);
            search.setValue(_value);
            search.setDataType(_dataType);
            ItemCollection items = search.execute();
            StringBuffer sb = new StringBuffer();
            if (items.size()>0)
            {
                sb.append(output(items,null,search.getUser()));
            }else{
                sb.append("No Matches Found.");
            }
            String s = sb.toString();
            return s;
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
    }
    public String search(String _session_id,String _field,String _comparison,Object _value,String _dataType,String templateName)  throws RemoteException
    {
        String _username= AxisEngine.getCurrentMessageContext().getUsername();
        String _password= AxisEngine.getCurrentMessageContext().getPassword();
        AccessLogger.LogServiceAccess(_session_id,"","VelocitySearch",_field + " " +  _comparison + " " + _value);
        try {
            SearchService search = new SearchService();
            search.setUsername(_username);
            search.setPassword(_password);
            search.setSession_id(_session_id);
            search.setField(_field);
            search.setComparison(_comparison);
            search.setValue(_value);
            search.setDataType(_dataType);
            ItemCollection items = search.execute();
            if(items.size()==0 && _field.endsWith(".ID")){
            	search.setField(_field.substring(0,_field.length()-2) + "label");
                items = search.execute();
            }
            StringBuffer sb = new StringBuffer();
            if (items.size()>0)
            {
                //sb.append(items.size()+ " Matching Items Found.\n\n");
                sb.append(output(items,templateName,search.getUser()));
            }else{
                sb.append("No Matches Found.");
            }
            return sb.toString();
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
    }
    public String search(String _session_id,String _field,String _comparison,Object _value,String _dataType)  throws RemoteException
    {
        String _username= AxisEngine.getCurrentMessageContext().getUsername();
        String _password= AxisEngine.getCurrentMessageContext().getPassword();
        AccessLogger.LogServiceAccess(_session_id,"","VelocitySearch",_field + " " +  _comparison + " " + _value);
        try {
            SearchService search = new SearchService();
            search.setSession_id(_session_id);
            search.setUsername(_username);
            search.setPassword(_password);
            search.setField(_field);
            search.setComparison(_comparison);
            search.setValue(_value);
            search.setDataType(_dataType);
            ItemCollection items = search.execute();
            StringBuffer sb = new StringBuffer();
            if (items.size()>0)
            {
                //sb.append(items.size()+ " Matching Items Found.\n\n");
                sb.append(output(items,null,search.getUser()));
            }else{
                sb.append("No Matches Found.");
            }
            return sb.toString();
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
    }
    
    private String output(ItemCollection items, String templateName,XDATUser user){
    	StringBuffer sb= new StringBuffer();
        Iterator iter = items.getItemIterator();
        while (iter.hasNext())
        {
            XFTItem item =(XFTItem)iter.next();
            try {
				Authorizer.getInstance().authorizeRead(item, user);
				ItemI bo = BaseElement.GetGeneratedItem(item);
				sb.append(bo.output(templateName));

				sb.append("\n");
			} catch (Exception e) {
			}
        }
        return sb.toString();
    }
    
    public static String Search(String _field,String _comparison,Object _value,String _dataType) throws RemoteException
    {
        return (new VelocitySearch()).search(_field,_comparison,_value,_dataType);
    }
    public static String Search(String _session_id,String _field,String _comparison,Object _value,String _dataType) throws RemoteException
    {
        return (new VelocitySearch()).search(_session_id,_field,_comparison,_value,_dataType);
    }
    public static String Search(String _session_id,String _field,String _comparison,Object _value,String _dataType,String templateName) throws RemoteException
    {
        return (new VelocitySearch()).search(_session_id,_field,_comparison,_value,_dataType,templateName);
    }
    public static void main(String[] args) {
    }
}
