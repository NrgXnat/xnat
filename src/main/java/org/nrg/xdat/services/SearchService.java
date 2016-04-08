/*
 * org.nrg.xdat.services.SearchService
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.services;

import java.sql.SQLException;

import org.apache.axis.AxisEngine;
import org.apache.log4j.Logger;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTool;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;

/**
 * @author Tim
 *
 */
public class SearchService {
	static org.apache.log4j.Logger logger = Logger.getLogger(SearchService.class);
    private String _username=null;
    private String _password=null;
    private String session_id=null;
    private String _field =null;
    private String _comparison = "=";
    private Object _value=null;
    private String _dataType = null;
    
    /**
     * 
     */
    public SearchService() {
        super();
    }

    UserI user=null;
    
    public ItemCollection execute() throws ElementNotFoundException,DBPoolException,SQLException,FieldNotFoundException,FailedLoginException,Exception
    {
        try {
            String elementName = XftStringUtils.GetRootElementName(getField());
            
            boolean valid = XFTTool.ValidateElementName(elementName);
            if (! valid)
            {
            	throw new Exception("Invalid field: " + getField());
            }

            
            if (getDataType()!=null)
            {
                elementName = getDataType();
            }
            
            valid = XFTTool.ValidateElementName(elementName);
            if (! valid)
            {
            	throw new Exception("Invalid Element Name: " + elementName);
            }

            String field = getField();

            String fieldElementName = XftStringUtils.GetRootElementName(getField());
            String validElementName = XFTTool.GetValidElementName(fieldElementName);
            if (!validElementName.equals(fieldElementName))
            {
                field = validElementName + XFT.PATH_SEPERATOR + XftStringUtils.GetFieldText(field);
            }
            
            Object o = getValue();

            if (getUsername()==null && getUsername()=="" && getPassword()==null && getPassword()=="" && this.getSession_id()==null && getSession_id()=="")
            {
                throw new Exception("Requires Username And Password.");
            }
            
            if (session_id== null)
            {
                user = Authenticator.Authenticate(new Authenticator.Credentials(_username,_password));
            }else{
                user = (UserI)AxisEngine.getCurrentMessageContext().getSession().get("user");
            }
            if (user == null)
            {
                throw new Exception("Invalid User.");
            }
            
            String xmlPath = XftStringUtils.StandardizeXMLPath(field);
            ItemSearch search = ItemSearch.GetItemSearch(elementName,user);
            search.setAllowMultiples(false);
            search.addCriteria(xmlPath,getValue(),getComparison());
            
            Authorizer.getInstance().authorizeRead(search.getElement(), user);
            
            ItemCollection items= search.exec(false);
            
            return items;
        } catch (XFTInitException e) {
            throw e;
        } catch (ElementNotFoundException e) {
            throw e;
        } catch (DBPoolException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } catch (FieldNotFoundException e) {
            throw e;
        } catch (FailedLoginException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }	
    }
    
//    public Document getXML() throws ElementNotFoundException,DBPoolException,SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        ItemCollection items = execute();
//        return items.toDOM();
//    }
//    
//    public File getResultsFile() throws ElementNotFoundException,DBPoolException,SQLException,FieldNotFoundException,FailedLoginException,XFTInitException,Exception
//    {
//        try {
//            ItemCollection items = execute();
//            
//            String directory = "./work/";
//            File f = new File(directory);
//            if (! f.exists())
//            {
//                f.mkdir();
//            }
//            
//            directory+= getUsername() + "_"+  Calendar.getInstance().getTimeInMillis() + "/";
//            if (! f.exists())
//            {
//                f.mkdir();
//            }
//            
//            XMLWriter.StoreXFTItemListToXMLFile(items.items(),directory);
//        } catch (ElementNotFoundException e) {
//            logger.error("",e);
//        } catch (DBPoolException e) {
//            logger.error("",e);
//        } catch (SQLException e) {
//            logger.error("",e);
//        } catch (FieldNotFoundException e) {
//            logger.error("",e);
//        } catch (FailedLoginException e) {
//            logger.error("",e);
//        } catch (XFTInitException e) {
//            logger.error("",e);
//        } catch (Exception e) {
//            logger.error("",e);
//        }
//        
//    }
    
    /**
     * @return Returns the _comparison.
     */
    public String getComparison() {
        return _comparison;
    }
    /**
     * @param _comparison The _comparison to set.
     */
    public void setComparison(String _comparison) {
        if (_comparison !=null)
            this._comparison = _comparison;
    }
    /**
     * @return Returns the _dataType.
     */
    public String getDataType() {
        return _dataType;
    }
    /**
     * @param type The _dataType to set.
     */
    public void setDataType(String type) {
        _dataType = type;
    }
    /**
     * @return Returns the _field.
     */
    public String getField() {
        return _field;
    }
    /**
     * @param _field The _field to set.
     */
    public void setField(String _field) {
        this._field = _field;
    }
    /**
     * @return Returns the _password.
     */
    public String getPassword() {
        return _password;
    }
    /**
     * @param _password The _password to set.
     */
    public void setPassword(String _password) {
        this._password = _password;
    }
    /**
     * @return Returns the _username.
     */
    public String getUsername() {
        return _username;
    }
    /**
     * @param _username The _username to set.
     */
    public void setUsername(String _username) {
        this._username = _username;
    }
    /**
     * @return Returns the _value.
     */
    public Object getValue() {
        return _value;
    }
    /**
     * @param _value The _value to set.
     */
    public void setValue(Object _value) {
        this._value = _value;
    }
    
    
    /**
     * @return Returns the session_id.
     */
    public String getSession_id() {
        return session_id;
    }
    /**
     * @param session_id The session_id to set.
     */
    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }
    
    public UserI getUser(){
    	return user;
    }
}
