//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jun 7, 2006
 *
 */
package org.nrg.xdat.services;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.axis.AxisEngine;
import org.apache.log4j.Logger;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XDATUser.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTool;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.utils.StringUtils;
/**
 * @author timo
 *
 */
public class GetIdentifiers {
	static org.apache.log4j.Logger logger = Logger.getLogger(GetIdentifiers.class);
    public ArrayList search(String _field,String _comparison,Object _value,String _dataType)  throws RemoteException
    {
        String _username= AxisEngine.getCurrentMessageContext().getUsername();
        String _password= AxisEngine.getCurrentMessageContext().getPassword();
        AccessLogger.LogServiceAccess(_username,"","GetIdentifiers",_field + " " +  _comparison + " " + _value);
        ArrayList al = new ArrayList();
        try {
            String elementName=null;
            if (_dataType==null){
                elementName = StringUtils.GetRootElementName(_field);
            }else{
                elementName = StringUtils.GetRootElementName(_dataType);
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
			rfield = org.nrg.xft.utils.StringUtils.StandardizeXMLPath(rfield);
			String sfield = (String)_field;
			sfield = org.nrg.xft.utils.StringUtils.StandardizeXMLPath(sfield);
			String sfieldElementName = StringUtils.GetRootElementName(_field);
			String svalidElementName = XFTTool.GetValidElementName(sfieldElementName);
			if (!svalidElementName.equals(sfieldElementName))
			{
			    sfield = svalidElementName + XFT.PATH_SEPERATOR + StringUtils.GetFieldText(sfield);
			}
			String comparison = "=";
			if (_comparison!=null)
			{
			    comparison = _comparison;
			}
			Object o = _value;
			XDATUser user = Authenticator.Authenticate(new Authenticator.Credentials(_username,_password));
            
            Authorizer.getInstance().authorizeRead(gwe, user);
            if (user == null)
            {
                throw new Exception("Invalid User.");
            }
            
            Authorizer.getInstance().authorizeRead(gwe, user);
            
			al =  FieldValues.GetValuesBySearchField(elementName,user,rfield,sfield,comparison,o,null);
			if(al.size()==0 && _field.endsWith(".ID")){
				_field= _field.substring(0,_field.length()-2) + "label";
				sfield = (String)_field;
				sfield = org.nrg.xft.utils.StringUtils.StandardizeXMLPath(sfield);
	
				sfieldElementName = StringUtils.GetRootElementName(_field);
				svalidElementName = XFTTool.GetValidElementName(sfieldElementName);
				if (!svalidElementName.equals(sfieldElementName))
				{
				    sfield = svalidElementName + XFT.PATH_SEPERATOR + StringUtils.GetFieldText(sfield);
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
        AccessLogger.LogServiceAccess(session_id,"","GetIdentifiers",_field + " " +  _comparison + " " + _value);
        ArrayList al = new ArrayList();
        try {
            String elementName=null;
            if (_dataType==null){
                elementName = StringUtils.GetRootElementName(_field);
            }else{
                elementName = StringUtils.GetRootElementName(_dataType);
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
			rfield = org.nrg.xft.utils.StringUtils.StandardizeXMLPath(rfield);
			String sfield = (String)_field;
			sfield = org.nrg.xft.utils.StringUtils.StandardizeXMLPath(sfield);
			String sfieldElementName = StringUtils.GetRootElementName(_field);
			String svalidElementName = XFTTool.GetValidElementName(sfieldElementName);
			if (!svalidElementName.equals(sfieldElementName))
			{
			    sfield = svalidElementName + XFT.PATH_SEPERATOR + StringUtils.GetFieldText(sfield);
			}
			String comparison = "=";
			if (_comparison!=null)
			{
			    comparison = _comparison;
			}
			Object o = _value;
			XDATUser user = (XDATUser)AxisEngine.getCurrentMessageContext().getSession().get("user");
            if (user == null)
            {
                throw new Exception("Invalid User.");
            }
            
            Authorizer.getInstance().authorizeRead(gwe, user);
            
			al =  FieldValues.GetValuesBySearchField(elementName,user,rfield,sfield,comparison,o,null);
	
			if(al.size()==0 && _field.endsWith(".ID")){
				_field= _field.substring(0,_field.length()-2) + "label";
				sfield = (String)_field;
				sfield = org.nrg.xft.utils.StringUtils.StandardizeXMLPath(sfield);
				sfieldElementName = StringUtils.GetRootElementName(_field);
				svalidElementName = XFTTool.GetValidElementName(sfieldElementName);
				if (!svalidElementName.equals(sfieldElementName))
				{
				    sfield = svalidElementName + XFT.PATH_SEPERATOR + StringUtils.GetFieldText(sfield);
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
}
