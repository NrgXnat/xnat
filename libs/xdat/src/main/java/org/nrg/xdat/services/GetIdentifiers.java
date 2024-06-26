/*
 * core: org.nrg.xdat.services.GetIdentifiers
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
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTool;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author timo
 *
 */
public class GetIdentifiers {
	static org.apache.log4j.Logger logger = Logger.getLogger(GetIdentifiers.class);
    public ArrayList search(String _field,String _comparison,Object _value,String _dataType)  throws RemoteException
    {
        final MessageContext messageContext = AxisEngine.getCurrentMessageContext();
        final String         _username      = messageContext.getUsername();
        final String         _password      = messageContext.getPassword();
        final UserI user = authenticate(_username, _password);
        if (user == null) {
            throw new RemoteException("Invalid User.");
        }

        AccessLogger.LogServiceAccess(_username, messageContext, "GetIdentifiers", _field + " " + _comparison + " " + _value);
        ArrayList al = new ArrayList();
        try {
            String elementName=null;
            if (_dataType==null){
                elementName = XftStringUtils.GetRootElementName(_field);
            }else{
                elementName = XftStringUtils.GetRootElementName(_dataType);
            }
			boolean valid = XFTTool.ValidateElementName(elementName);
			if (! valid)
			{
			    throw new Exception("Invalid Element Name: " + elementName);
			}
			String rfield = null;
			GenericWrapperElement gwe = GenericWrapperElement.GetElement(elementName);
			Iterator iter = gwe.getAllPrimaryKeys().iterator();
			while(iter.hasNext())
			{
			    GenericWrapperField f = (GenericWrapperField)iter.next();
			    rfield = f.getXMLPathString(gwe.getXSIType());
			}
			rfield = XftStringUtils.StandardizeXMLPath(rfield);
			String sfield = (String)_field;
			sfield = XftStringUtils.StandardizeXMLPath(sfield);
			String sfieldElementName = XftStringUtils.GetRootElementName(_field);
			String svalidElementName = XFTTool.GetValidElementName(sfieldElementName);
			if (!svalidElementName.equals(sfieldElementName))
			{
			    sfield = svalidElementName + XFT.PATH_SEPARATOR + XftStringUtils.GetFieldText(sfield);
			}
			String comparison = "=";
			if (_comparison!=null)
			{
			    comparison = _comparison;
			}
			Object o = _value;
            Authorizer.getInstance().authorizeRead(gwe, user);
			al =  FieldValues.GetValuesBySearchField(elementName,user,rfield,sfield,comparison,o,null);
			if(al.size()==0 && _field.endsWith(".ID")){
				_field= _field.substring(0,_field.length()-2) + "label";
				sfield = (String)_field;
				sfield = XftStringUtils.StandardizeXMLPath(sfield);
	
				sfieldElementName = XftStringUtils.GetRootElementName(_field);
				svalidElementName = XFTTool.GetValidElementName(sfieldElementName);
				if (!svalidElementName.equals(sfieldElementName))
				{
				    sfield = svalidElementName + XFT.PATH_SEPARATOR + XftStringUtils.GetFieldText(sfield);
				}
			al =  FieldValues.GetValuesBySearchField(elementName,user,rfield,sfield,comparison,o,null);
			}
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

    public ArrayList search(String session_id,String _field,String _comparison,Object _value,String _dataType)  throws RemoteException
    {
        final MessageContext messageContext = AxisEngine.getCurrentMessageContext();
        final UserI          user           = (UserI) messageContext.getSession().get("user");
        if (user == null) {
            throw new RemoteException("Invalid User for session ID: " + session_id);
        }
        AccessLogger.LogServiceAccess(user.getUsername(), messageContext, "GetIdentifiers", _field + " " + _comparison + " " + _value);
        ArrayList al = new ArrayList();
        try {
            String elementName=null;
            if (_dataType==null){
                elementName = XftStringUtils.GetRootElementName(_field);
            }else{
                elementName = XftStringUtils.GetRootElementName(_dataType);
            }
			boolean valid = XFTTool.ValidateElementName(elementName);
			if (! valid)
			{
			    throw new Exception("Invalid Element Name: " + elementName);
			}
			String rfield = null;
			GenericWrapperElement gwe = GenericWrapperElement.GetElement(elementName);
			Iterator iter = gwe.getAllPrimaryKeys().iterator();
			while(iter.hasNext())
			{
			    GenericWrapperField f = (GenericWrapperField)iter.next();
			    rfield = f.getXMLPathString(gwe.getXSIType());
			}
			rfield = XftStringUtils.StandardizeXMLPath(rfield);
			String sfield = (String)_field;
			sfield = XftStringUtils.StandardizeXMLPath(sfield);
			String sfieldElementName = XftStringUtils.GetRootElementName(_field);
			String svalidElementName = XFTTool.GetValidElementName(sfieldElementName);
			if (!svalidElementName.equals(sfieldElementName))
			{
			    sfield = svalidElementName + XFT.PATH_SEPARATOR + XftStringUtils.GetFieldText(sfield);
			}
			String comparison = "=";
			if (_comparison!=null)
			{
			    comparison = _comparison;
			}
			Object o = _value;

            Authorizer.getInstance().authorizeRead(gwe, user);

			al =  FieldValues.GetValuesBySearchField(elementName,user,rfield,sfield,comparison,o,null);

			if(al.size()==0 && _field.endsWith(".ID")){
				_field= _field.substring(0,_field.length()-2) + "label";
				sfield = (String)_field;
				sfield = XftStringUtils.StandardizeXMLPath(sfield);
				sfieldElementName = XftStringUtils.GetRootElementName(_field);
				svalidElementName = XFTTool.GetValidElementName(sfieldElementName);
				if (!svalidElementName.equals(sfieldElementName))
				{
				    sfield = svalidElementName + XFT.PATH_SEPARATOR + XftStringUtils.GetFieldText(sfield);
				}
			al =  FieldValues.GetValuesBySearchField(elementName,user,rfield,sfield,comparison,o,null);
			}
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

    public static ArrayList Search(String session_id,String _field,String _comparison,Object _value,String _dataType) throws RemoteException
    {
        return (new GetIdentifiers()).search(session_id,_field,_comparison,_value,_dataType);
    }

    public static ArrayList Search(String _field,String _comparison,Object _value,String _dataType) throws RemoteException
    {
        return (new GetIdentifiers()).search(_field,_comparison,_value,_dataType);
    }

    private UserI authenticate(final String _username, final String _password) {
        try {
            return Authenticator.Authenticate(new Authenticator.Credentials(_username, _password));
        } catch (Exception e) {
            logger.warn("Failed to authenticate user with username " + _password, e);
            return null;
        }
    }
}
