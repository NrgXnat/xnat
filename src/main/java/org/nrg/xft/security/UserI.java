/*
 * core: org.nrg.xft.security.UserI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.security;

import com.google.common.base.Function;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xft.exception.MetaDataException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Tim
 *
 */
public interface UserI extends UserDetails,Serializable{
    String                 ANONYMOUS_AUTH_PROVIDER_KEY = "xnat-anonymous-auth-provider";
    SimpleGrantedAuthority AUTHORITY_ANONYMOUS         = new SimpleGrantedAuthority("ROLE_ANONYMOUS");
    List<GrantedAuthority> AUTHORITIES_ANONYMOUS       = Collections.<GrantedAuthority>singletonList(AUTHORITY_ANONYMOUS);
    SimpleGrantedAuthority AUTHORITY_ADMIN             = new SimpleGrantedAuthority("ROLE_ADMIN");
    SimpleGrantedAuthority AUTHORITY_USER              = new SimpleGrantedAuthority("ROLE_USER");

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

    Function<UserI, String> USERI_TO_USERNAME = new Function<UserI, String>() {
        @Nullable
        @Override
        public String apply(final UserI user) {
            return user.getUsername();
        }
    };
}


