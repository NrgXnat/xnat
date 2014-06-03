//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 10, 2006
 *
 */
package org.nrg.xdat.services;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.sql.SQLException;

import org.apache.axis.AxisEngine;
import org.apache.log4j.Logger;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFT;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.ValidationException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XMLValidator;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.xml.sax.InputSource;

/**
 * @author Tim
 *
 */
public class StoreXML {
    static org.apache.log4j.Logger logger = Logger.getLogger(StoreXML.class);
    
    public String store(String _file,Boolean _quarantine,Boolean _allowDataDeletion)  throws RemoteException
    {
        String _username= AxisEngine.getCurrentMessageContext().getUsername();
        String _password= AxisEngine.getCurrentMessageContext().getPassword();
        AccessLogger.LogServiceAccess(_username,"","StoreXML","Called");
        StringBuffer sb = new StringBuffer();

            try {
                UserI user = Authenticator.Authenticate(new Authenticator.Credentials(_username,_password));
                               
                //XERCES VALIDATION
                XMLValidator validator = new XMLValidator();
                validator.validateString(_file);

                //Document doc = XMLUtils.GetDOM(_file);
                //XFTItem item = XMLReader.TranslateDomToItem(doc,user);
                SAXReader reader = new SAXReader(user);
    			org.nrg.xft.XFTItem item = reader.parse(new StringReader(_file));
                if (XFT.VERBOSE)
                    System.out.println("Store XML Service Called By " + _username + " For " + item.getProperName());
                logger.info("Store XML Service Called By " + _username + " For " + item.getProperName());
                
                ValidationResults vr = XFTValidator.Validate(item);
                if (vr.isValid())
                {
                    logger.info("Validation: PASSED");
                	
                	boolean q;
                	boolean override;
                	if (_quarantine!=null)
                	{
                	    q = _quarantine.booleanValue();
                	    override = true;
                	}else{
                	    q = item.getGenericSchemaElement().isQuarantine();
                	    override = false;
                	}
                    
                    SaveItemHelper.unauthorizedSave(item, user, false,q,override,_allowDataDeletion.booleanValue(),EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.STORE_XML, "Stored XML", EventUtils.MODIFY_VIA_STORE_XML, null));
                    
                    
                    sb.append("Item Successfully Stored.");
                    logger.info("Item Successfully Stored.");	
                    AccessLogger.LogServiceAccess(_username,"","StoreXML",item.getProperName() + " Successfully Stored");		
                }else
                {
                	throw new ValidationException(vr);
                }
            } catch (FileNotFoundException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(_username,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (XFTInitException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(_username,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(_username,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (DBPoolException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(_username,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (SQLException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(_username,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (FieldNotFoundException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(_username,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (FailedLoginException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(_username,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (ValidationException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(_username,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (Exception e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(_username,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            }
        
        
        return sb.toString();
        
    }
    

    public String store(String session_id,String _file,Boolean _quarantine,Boolean _allowDataDeletion)  throws RemoteException
    {
        AccessLogger.LogServiceAccess(session_id,"","StoreXML","Called");
        StringBuffer sb = new StringBuffer();

            try {
            	UserI user = (UserI)AxisEngine.getCurrentMessageContext().getSession().get("user");
                               
                //XERCES VALIDATION
                XMLValidator validator = new XMLValidator();
                validator.validateString(_file);
                //Document doc = XMLUtils.GetDOM(_file);
                
                //XFTItem item = XMLReader.TranslateDomToItem(doc,user);
                SAXReader reader = new SAXReader(user);
                StringReader sr = new StringReader(_file);
                InputSource is = new InputSource(sr);
    			org.nrg.xft.XFTItem item = reader.parse(is);
                if (XFT.VERBOSE)
                    System.out.println("Store XML Service Called By " + session_id + " For " + item.getProperName());
                logger.info("Store XML Service Called By " + session_id + " For " + item.getProperName());
                
                ValidationResults vr = XFTValidator.Validate(item);
                if (vr.isValid())
                {
                    logger.info("Validation: PASSED");
                	
                	boolean q;
                	boolean override;
                	if (_quarantine!=null)
                	{
                	    q = _quarantine.booleanValue();
                	    override = true;
                	}else{
                	    q = item.getGenericSchemaElement().isQuarantine();
                	    override = false;
                	}
                    
                    SaveItemHelper.unauthorizedSave(item, user, false,q,override,_allowDataDeletion.booleanValue(),EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.STORE_XML, "Stored XML", EventUtils.MODIFY_VIA_STORE_XML, null));
                    
                	sb.append("Item Successfully Stored.");
                    logger.info("Item Successfully Stored.");	
                    AccessLogger.LogServiceAccess(session_id,"","StoreXML",item.getProperName() + " Successfully Stored");		
                }else
                {
                	throw new ValidationException(vr);
                }
            } catch (FileNotFoundException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(session_id,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (XFTInitException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(session_id,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(session_id,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (DBPoolException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(session_id,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (SQLException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(session_id,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (FieldNotFoundException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(session_id,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (FailedLoginException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(session_id,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (ValidationException e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(session_id,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            } catch (Exception e) {
                logger.error("",e);
                AccessLogger.LogServiceAccess(session_id,"","StoreXML","Store Failed");
                throw new RemoteException("",e);
            }
        
        
        return sb.toString();
        
    }
    
    public static String Store(String session_id,String _file,Boolean _quarantine,Boolean _allowDataDeletion) throws RemoteException
    {
        return (new StoreXML()).store(session_id,_file,_quarantine,_allowDataDeletion);
    }
    
    public static String Store(String _file,Boolean _quarantine,Boolean _allowDataDeletion) throws RemoteException
    {
        return (new StoreXML()).store(_file,_quarantine,_allowDataDeletion);
    }
    
    
}
