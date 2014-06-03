/*
 * org.nrg.xft.security.UserI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 12:00 PM
 */


package org.nrg.xft.security;

import java.io.Serializable;
import java.util.Date;

import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xft.exception.MetaDataException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Tim
 *
 */
public interface UserI extends UserDetails,Serializable{
	public Integer getID();
	public String getUsername();
	public String getLogin();
    public boolean isGuest();
	
	/**
	 * @return
	 */
	public String getFirstname();

	/**
	 * @return
	 */
	public String getLastname();

	/**
	 * @return
	 */
	public String getEmail();
	public boolean checkRole(String roleSiteAdmin) throws Exception;

	public boolean isSiteAdmin();
	public String getDBName();
	public String getPassword();
	public boolean isEnabled();
	public Boolean isVerified();
	public String getSalt();
	public boolean isActive()throws MetaDataException;
	
	public Date getLastModified();
	
	public void setLogin(String login);
	public void setEmail(String e);
	public void setFirstname(String firstname);
	public void setLastname(String lastname);
	public void setPassword(String encodePassword);
	public void setSalt(String salt);
	public void setPrimaryPassword_encrypt(Object b);
	public void setEnabled(Object enabled);
	public void setVerified(Object verified);
	
	public Object getCustomField(String key);
	public void setCustomField(String key, Object value) throws Exception;
	
	public UserAuthI setAuthorization(UserAuthI newUserAuth);
	public UserAuthI getAuthorization();
}

