/*
 * org.nrg.xdat.entities.XDATUserDetails
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.entities;

import com.google.common.base.Joiner;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.UserGroup;
import org.nrg.xdat.security.XDATUser;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.nrg.xdat.XDAT;

import java.util.*;

@SuppressWarnings("unchecked")
public class XDATUserDetails extends XDATUser implements UserDetails {

    public XDATUserDetails() {
        _log.debug("Called default constructor");
    }

    public XDATUserDetails(String user) throws Exception {
        super(user);
        if (_log.isDebugEnabled()) {
            _log.debug("Called constructor with string arg: " + user);
        }
    }

    public XDATUserDetails(XdatUser user) throws Exception {
        super(user);
        if (_log.isDebugEnabled()) {
            _log.debug("Called constructor with XDATUser arg: " + user.getLogin());
        }
    }

    public XdatUserAuth getAuthorization() {
        return _authorization;
    }

    public XdatUserAuth setAuthorization(XdatUserAuth auth) {
        return _authorization = auth;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        if (_authorities == null) {
            _authorities = new ArrayList<GrantedAuthority>();
            _authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
            if (isSiteAdmin()) {
                _authorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
            }
            Map<String,UserGroup> groups = getGroups();
            if (groups != null && groups.size() > 0) {
                for (String group : groups.keySet()) {
                    _authorities.add(new GrantedAuthorityImpl(group));
                }
            }
            if (_log.isDebugEnabled()) {
                _log.debug("Created granted authorities list for user " + getLogin() + ": " + Joiner.on(", ").join(_authorities));
            }
        }
        return _authorities;
    }

    public String getPassword() {
        try {
            return getStringProperty("primary_password");
        } catch (Exception e) {
            return null;
        }
    }

    public String getUsername() {
        return super.getUsername();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return super.isEnabled();
    }
    
    public void validateUserLogin() {
        if (!isEnabled()) {
            throw new DisabledException("Attempted login to disabled account: " + getUsername(), this);
        }
        if ( (XDAT.verificationOn() && !isVerified()) || !isAccountNonLocked()) {
            throw new CredentialsExpiredException("Attempted login to unverified or locked account: " + getUsername(), this);
        }
    }
    
    //necessary for spring security session management
	public int hashCode() {
        return new HashCodeBuilder(691, 431).append(getUsername()).toHashCode();
    }

	public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        XDATUserDetails rhs = (XDATUserDetails) obj;
        return new EqualsBuilder().append(getUsername(), rhs.getUsername()).isEquals();
    }

    private static final Log _log = LogFactory.getLog(XDATUserDetails.class);

    private XdatUserAuth _authorization;
    private List<GrantedAuthority> _authorities;
}
