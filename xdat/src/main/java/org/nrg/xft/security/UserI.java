/*
 * core: org.nrg.xft.security.UserI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.security;

import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xft.exception.MetaDataException;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Tim
 *
 */
public interface UserI extends UserDetails,Serializable{
    Integer getID();
	String getUsername();
    String getLogin();
    boolean isGuest();

    String getFirstname();
    String getLastname();
    String getEmail();
    String getDBName();
    String getPassword();
    boolean isEnabled();
    Boolean isVerified();

    /**
     * The user's salt
     *
     * @return The user's salt
     *
     * @deprecated Passwords are automatically salted by the security framework.
     */
    @Deprecated
    String getSalt();
    boolean isActive()throws MetaDataException;

    Date getLastModified();

    void setLogin(String login);
    void setEmail(String e);
    void setFirstname(String firstname);
    void setLastname(String lastname);
    void setPassword(String encodePassword);
    /**
     * Sets the user's salt
     *
     * @param salt The value to set for the user's salt
     *
     * @deprecated Passwords are automatically salted by the security framework.
     */
    @Deprecated
    void setSalt(String salt);
    void setPrimaryPassword_encrypt(Object b);
    void setEnabled(Object enabled);
    void setVerified(Object verified);

    UserAuthI setAuthorization(UserAuthI newUserAuth);
    UserAuthI getAuthorization();
}


