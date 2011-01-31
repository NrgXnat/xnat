//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 15, 2006
 *
 */
package org.nrg.xdat.services;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.axis.AxisEngine;
import org.apache.log4j.Logger;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XDATUser.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.XFTTool;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;
/**
 * @author timo
 *
 */
public class FieldValues {
    static org.apache.log4j.Logger logger = Logger.getLogger(FieldValues.class);
    public ArrayList search(String _field,String _comparison,Object _value,String _rfield, String _order) throws RemoteException
    {
        String _username= AxisEngine.getCurrentMessageContext().getUsername();
        String _password= AxisEngine.getCurrentMessageContext().getPassword();
        AccessLogger.LogServiceAccess(_username,"","FieldValues",_field + " " +  _comparison + " " + _value + " : " + _rfield);
        ArrayList al = new ArrayList();
        try {
            String elementName = StringUtils.GetRootElementName(_rfield);
			boolean valid = XFTTool.ValidateElementName(elementName);
			if (! valid)
			{
			    throw new Exception("Invalid Element Name: " + elementName);
			}
			valid = XFTTool.ValidateElementName(elementName);
			if (! valid)
			{
			    throw new Exception("Invalid Element Name: " + elementName);
			}
			String rfield = _rfield;
			rfield = org.nrg.xft.utils.StringUtils.StandardizeXMLPath(rfield);
			String rfieldElementName = StringUtils.GetRootElementName(_rfield);
			String rvalidElementName = XFTTool.GetValidElementName(rfieldElementName);
			if (!rvalidElementName.equals(rfieldElementName))
			{
			    rfield = rvalidElementName + XFT.PATH_SEPERATOR + StringUtils.GetFieldText(rfield);
			}
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
            if (user == null)
            {
                throw new Exception("Invalid User.");
            }
            
            Authorizer.getInstance().authorizeRead(GenericWrapperElement.GetElement(elementName), user);
            
			al =  GetValuesBySearchField(elementName,user,rfield,sfield,comparison,o,_order);
	
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
			al =  GetValuesBySearchField(elementName,user,rfield,sfield,comparison,o,_order);
			}
			//TEST ATTACHMENTS
//			String fileName = "C:\\jakarta-tomcat-5.0.28\\webapps\\ROOT\\images\\washu.gif";
//			
//			//explicitly set format to DIME, default is MIME
//			Message rspmsg =AxisEngine.getCurrentMessageContext().getResponseMessage();
//				
//			rspmsg.getAttachmentsImpl().setSendType(org.apache.axis.attachments.Attachments.SEND_TYPE_DIME);
//			
//			DataHandler dh = new DataHandler(new FileDataSource(fileName));
//			if (dh == null ) System.err.println("dhSource is null");
//			
//					
//			AttachmentPart ap = new AttachmentPart(dh);
//			ap.setContentId("Filename1");
//			
//			MessageContext context=MessageContext.getCurrentContext();
//			Message responseMessage=context.getResponseMessage();
//			
//			responseMessage.addAttachmentPart(ap);
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
    public ArrayList search(String session_id,String _field,String _comparison,Object _value,String _rfield, String _order) throws RemoteException
    {
        AccessLogger.LogServiceAccess(session_id,"","FieldValues",_field + " " +  _comparison + " " + _value + " : " + _rfield);
        ArrayList al = new ArrayList();
        try {
            String elementName = StringUtils.GetRootElementName(_rfield);
			boolean valid = XFTTool.ValidateElementName(elementName);
			if (! valid)
			{
			    throw new Exception("Invalid Element Name: " + elementName);
			}
			valid = XFTTool.ValidateElementName(elementName);
			if (! valid)
			{
			    throw new Exception("Invalid Element Name: " + elementName);
			}
			String rfield = _rfield;
			rfield = org.nrg.xft.utils.StringUtils.StandardizeXMLPath(rfield);
			String rfieldElementName = StringUtils.GetRootElementName(_rfield);
			String rvalidElementName = XFTTool.GetValidElementName(rfieldElementName);
			if (!rvalidElementName.equals(rfieldElementName))
			{
			    rfield = rvalidElementName + XFT.PATH_SEPERATOR + StringUtils.GetFieldText(rfield);
			}
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
            Authorizer.getInstance().authorizeRead(GenericWrapperElement.GetElement(elementName), user);
            
			al =  GetValuesBySearchField(elementName,user,rfield,sfield,comparison,o,_order);
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
			al =  GetValuesBySearchField(elementName,user,rfield,sfield,comparison,o,_order);
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
    public static ArrayList GetValuesBySearchField(String elementName, UserI user, String rfield,String sfield,String comparison, Object o, String _order) throws Exception
    {
        QueryOrganizer qo = new QueryOrganizer(elementName,user,ViewManager.ALL);
		qo.addField(rfield);
		qo.addField(sfield);
		CriteriaCollection cc =new CriteriaCollection("AND");
		cc.addClause(sfield,comparison,o);
		String query = "SELECT * FROM (" + qo.buildQuery() + ") SEARCH";
		query += " WHERE " + cc.getSQLClause(qo);
		query += ";";
		String login = null;
		if (user!= null)
		{
		    login = user.getUsername();
		}
		XFTTable table = XFTTable.Execute(query,SchemaElement.GetElement(elementName).getDbName(),login);
		
		String colname= qo.translateXMLPath(rfield);
		if (_order!=null)
		{
		    if ((_order).equalsIgnoreCase("DESC"))
		    {
		        table.sort(colname,"DESC");
		    }else if ((_order).equalsIgnoreCase("ASC"))
		    {
		        table.sort(colname,"ASC");
		    }else{
		        throw new Exception("-order value must be 'ASC' or 'DESC'");
		    }
		}else{
			table.sort(colname,"ASC");
		}
		ArrayList al= new ArrayList();
		table.resetRowCursor();
		while (table.hasMoreRows())
		{
		    Hashtable row = table.nextRowHash();
		    al.add(row.get(colname.toLowerCase()));
		}
        al.trimToSize();
        return al;
    }
    public static ArrayList Search(String session_id,String _field,String _comparison,Object _value, String _rfield, String _order) throws RemoteException
    {
        return (new FieldValues()).search(session_id,_field,_comparison,_value,_rfield,_order);
    }
    public static ArrayList Search(String _field,String _comparison,Object _value, String _rfield, String _order) throws RemoteException
    {
        return (new FieldValues()).search(_field,_comparison,_value,_rfield,_order);
    }
}
