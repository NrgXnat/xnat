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
	Integer getID();
	String getUsername();
    String getLogin();
    boolean isGuest();
	
	/**
	 * @return
	 */
    String getFirstname();

	/**
	 * @return
	 */
    String getLastname();

	/**
	 * @return
	 */
    String getEmail();

    String getDBName();
    String getPassword();
    boolean isEnabled();
    Boolean isVerified();
    String getSalt();
    boolean isActive()throws MetaDataException;
	
    Date getLastModified();
	
    void setLogin(String login);
    void setEmail(String e);
    void setFirstname(String firstname);
    void setLastname(String lastname);
    void setPassword(String encodePassword);
    void setSalt(String salt);
    void setPrimaryPassword_encrypt(Object b);
    void setEnabled(Object enabled);
    void setVerified(Object verified);
	
    UserAuthI setAuthorization(UserAuthI newUserAuth);
    UserAuthI getAuthorization();
}


