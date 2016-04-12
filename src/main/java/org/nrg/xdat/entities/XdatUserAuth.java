/*
 * org.nrg.xdat.entities.XdatUserAuth
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/4/13 4:37 PM
 */
package org.nrg.xdat.entities;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.util.*;

@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"authUser", "authMethodId"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class XdatUserAuth extends AbstractHibernateEntity implements UserAuthI{

    private static final long serialVersionUID = -1590002660142544162L;
    private String xdatUsername;
	private String authUser;
	private String authMethod;
	private String authMethodId;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private Date passwordUpdated;
	private Integer failedLoginAttempts;
	private Date lastSuccessfulLogin;
	
	public XdatUserAuth() {
	}
	
	public XdatUserAuth(String user, String method) {
		this(user,method,user,true,0);
	}

	public XdatUserAuth(String user, String method, String id) {
		this(user,method,id,user,true,0);
	}
	
	public XdatUserAuth(String user, String method, String xdat, boolean enabled,Integer failedLoginAttempts) {
		this.authUser = user;
		this.authMethod = method;
		setEnabled(enabled);
		accountNonExpired=true;
		accountNonLocked=true;
		credentialsNonExpired=true;
		this.xdatUsername = xdat;
		passwordUpdated = new Date();
		this.failedLoginAttempts=failedLoginAttempts;
	}

    @SuppressWarnings("unused")
	public XdatUserAuth(String user, String method, boolean enabled, boolean aNonExpired, boolean nonLocked, boolean cNonExpired, List<GrantedAuthority> auth, String xdatUsername,Integer failedLoginAttempts) {
		this.authUser = user;
		this.authMethod = method;
		setEnabled(enabled);
		accountNonExpired=true;
		accountNonLocked=true;
		credentialsNonExpired=true;
		this.xdatUsername = xdatUsername;
		passwordUpdated = new Date();
		this.failedLoginAttempts=failedLoginAttempts;
	}
	
	public XdatUserAuth(String user, String method, String methodId, String xdat, boolean enabled,Integer failedLoginAttempts) {
		this.authUser = user;
		this.authMethod = method;
		this.authMethodId = methodId;
		setEnabled(enabled);
		accountNonExpired=true;
		accountNonLocked=true;
		credentialsNonExpired=true;
		this.xdatUsername = xdat;
		passwordUpdated = new Date();
		this.failedLoginAttempts=failedLoginAttempts;
	}

    @SuppressWarnings("unused")
	public XdatUserAuth(String user, String method, String methodId, boolean enabled, boolean aNonExpired, boolean nonLocked, boolean cNonExpired, List<GrantedAuthority> auth, String xdatUsername,Integer failedLoginAttempts, Date lastSuccessfulLogin) {
		this.authUser = user;
		this.authMethod = method;
		this.authMethodId = methodId;
		setEnabled(enabled);
		accountNonExpired=true;
		accountNonLocked=true;
		credentialsNonExpired=true;
		this.xdatUsername = xdatUsername;
		passwordUpdated = new Date();
		this.failedLoginAttempts=failedLoginAttempts;
		this.lastSuccessfulLogin = lastSuccessfulLogin;
	}
	
	public XdatUserAuth(XdatUserAuth other)
	{
		this.authUser = other.authUser;
		this.authMethod = other.authMethod;
		this.authMethodId = other.authMethodId;
		setEnabled(other.isEnabled());
		accountNonExpired=other.accountNonExpired;
		accountNonLocked=other.accountNonLocked;
		credentialsNonExpired=other.credentialsNonExpired;
		this.xdatUsername = other.xdatUsername;
		passwordUpdated = other.passwordUpdated;
		this.failedLoginAttempts = other.failedLoginAttempts;
		this.lastSuccessfulLogin = other.lastSuccessfulLogin;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getXdatUsername()
	 */
	@Override
	public String getXdatUsername() {
		return xdatUsername;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setXdatUsername(java.lang.String)
	 */
	@Override
	public void setXdatUsername(String xdatUsername) {
		this.xdatUsername = xdatUsername;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getAuthUser()
	 */
	@Override
	public String getAuthUser() {
		return authUser;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setAuthUser(java.lang.String)
	 */
	@Override
	public void setAuthUser(String user) {
		this.authUser = user;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getAuthMethod()
	 */
	@Override
	public String getAuthMethod() {
		return authMethod;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setAuthMethod(java.lang.String)
	 */
	@Override
	public void setAuthMethod(String means) {
		this.authMethod = means;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getAuthMethodId()
	 */
	@Override
	public String getAuthMethodId() {
		return authMethodId;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setAuthMethodId(java.lang.String)
	 */
	@Override
	public void setAuthMethodId(String means) {
		this.authMethodId = means;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getFailedLoginAttempts()
	 */
	@Override
	public Integer getFailedLoginAttempts() {
		if(failedLoginAttempts==null){
			return 0;
		}else{
			return failedLoginAttempts;
		}
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setFailedLoginAttempts(java.lang.Integer)
	 */
	@Override
	public void setFailedLoginAttempts(Integer count) {
		this.failedLoginAttempts = count;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getLastSuccessfulLogin()
	 */
	@Override
	public Date getLastSuccessfulLogin() {
		return lastSuccessfulLogin;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setLastSuccessfulLogin(java.util.Date)
	 */
	@Override
	public void setLastSuccessfulLogin(Date lastSuccessfulLogin) {
		this.lastSuccessfulLogin = lastSuccessfulLogin;
	}

    /* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getPasswordUpdated()
	 */
    @Override
	@Temporal(TemporalType.TIMESTAMP)
    public Date getPasswordUpdated() {
        return passwordUpdated;
    }

    /* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setPasswordUpdated(java.util.Date)
	 */
    @Override
	@Temporal(TemporalType.TIMESTAMP)
    public void setPasswordUpdated(Date timestamp) {
    	passwordUpdated = timestamp;
    }
	
    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof XdatUserAuth)) {
            return false;
        }
        UserAuthI other = (UserAuthI) object;
        return           StringUtils.equals(getAuthUser(), other.getAuthUser()) &&
                         StringUtils.equals(getAuthMethod(), other.getAuthMethod()) &&
                         StringUtils.equals(getXdatUsername(), other.getXdatUsername());
    }
	

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getAuthorities()
	 */
	@Override
	@Transient
	public Collection<GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> list = new HashSet<>();
        list.add(new SimpleGrantedAuthority("ROLE_USER"));
        return list;
	}


	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#isAccountNonExpired()
	 */
	@Override
	@Transient
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}


	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#isAccountNonLocked()
	 */
	@Override
	@Transient
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}


	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#isCredentialsNonExpired()
	 */
	@Override
	@Transient
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}
}
