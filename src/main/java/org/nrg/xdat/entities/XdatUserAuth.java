/*
 * core: org.nrg.xdat.entities.XdatUserAuth
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
    private String  _xdatUsername;
	private String  _authUser;
	private String  _authMethod;
	private String  _authMethodId;
	private boolean _accountNonExpired;
	private boolean _accountNonLocked;
	private boolean _credentialsNonExpired;
	private Date    _passwordUpdated;
	private Integer _failedLoginAttempts;
	private Date    _lastLoginAttempt;
	private Date    _lastSuccessfulLogin;
	private Date    _lockoutTime;
	
	public XdatUserAuth() {
	}
	
	public XdatUserAuth(String user, String method) {
		this(user,method,user,true,0);
	}

	public XdatUserAuth(String user, String method, String id) {
		this(user,method,id,user,true,0);
	}
	
	public XdatUserAuth(String user, String method, String xdat, boolean enabled,Integer failedLoginAttempts) {
		_authUser = user;
		_authMethod = method;
		setEnabled(enabled);
		_accountNonExpired =true;
		_accountNonLocked =true;
		_credentialsNonExpired =true;
		_xdatUsername = xdat;
		_passwordUpdated = new Date();
		_failedLoginAttempts =failedLoginAttempts;
		_lockoutTime = null;
	}

    @SuppressWarnings("unused")
	public XdatUserAuth(String user, String method, boolean enabled, boolean aNonExpired, boolean nonLocked, boolean cNonExpired, List<GrantedAuthority> auth, String xdatUsername,Integer failedLoginAttempts) {
		_authUser = user;
		_authMethod = method;
		setEnabled(enabled);
		_accountNonExpired =true;
		_accountNonLocked =true;
		_credentialsNonExpired =true;
		_xdatUsername = xdatUsername;
		_passwordUpdated = new Date();
		_failedLoginAttempts =failedLoginAttempts;
		_lockoutTime = null;
	}
	
	public XdatUserAuth(String user, String method, String methodId, String xdat, boolean enabled,Integer failedLoginAttempts) {
		_authUser = user;
		_authMethod = method;
		_authMethodId = methodId;
		setEnabled(enabled);
		_accountNonExpired =true;
		_accountNonLocked =true;
		_credentialsNonExpired =true;
		_xdatUsername = xdat;
		_passwordUpdated = new Date();
		_failedLoginAttempts =failedLoginAttempts;
		_lockoutTime = null;
	}


	public XdatUserAuth(String user, String method, String methodId, boolean enabled, boolean aNonExpired, boolean nonLocked, boolean cNonExpired, List<GrantedAuthority> auth, String xdatUsername,Integer failedLoginAttempts, Date lastSuccessfulLogin, Date lockoutTime) {
		_authUser = user;
		_authMethod = method;
		_authMethodId = methodId;
		setEnabled(enabled);
		_accountNonExpired=true;
		_accountNonLocked=true;
		_credentialsNonExpired=true;
		_xdatUsername = xdatUsername;
		_passwordUpdated = new Date();
		_failedLoginAttempts=failedLoginAttempts;
		_lastSuccessfulLogin = lastSuccessfulLogin;
		_lockoutTime = lockoutTime;
	}

	public XdatUserAuth(XdatUserAuth other)
	{
		_authUser = other._authUser;
		_authMethod = other._authMethod;
		_authMethodId = other._authMethodId;
		setEnabled(other.isEnabled());
		_accountNonExpired =other._accountNonExpired;
		_accountNonLocked =other._accountNonLocked;
		_credentialsNonExpired =other._credentialsNonExpired;
		_xdatUsername = other._xdatUsername;
		_passwordUpdated = other._passwordUpdated;
		_failedLoginAttempts = other._failedLoginAttempts;
		_lastSuccessfulLogin = other._lastSuccessfulLogin;
		_lockoutTime = other._lockoutTime;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getXdatUsername()
	 */
	@Override
	public String getXdatUsername() {
		return _xdatUsername;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setXdatUsername(java.lang.String)
	 */
	@Override
	public void setXdatUsername(String xdatUsername) {
		_xdatUsername = xdatUsername;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getAuthUser()
	 */
	@Override
	public String getAuthUser() {
		return _authUser;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setAuthUser(java.lang.String)
	 */
	@Override
	public void setAuthUser(String user) {
		_authUser = user;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getAuthMethod()
	 */
	@Override
	public String getAuthMethod() {
		return _authMethod;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setAuthMethod(java.lang.String)
	 */
	@Override
	public void setAuthMethod(String means) {
		_authMethod = means;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getAuthMethodId()
	 */
	@Override
	public String getAuthMethodId() {
		return _authMethodId;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setAuthMethodId(java.lang.String)
	 */
	@Override
	public void setAuthMethodId(String means) {
		_authMethodId = means;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getFailedLoginAttempts()
	 */
	@Override
	public Integer getFailedLoginAttempts() {
		if(_failedLoginAttempts == null){
			return 0;
		}else{
			return _failedLoginAttempts;
		}
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setFailedLoginAttempts(java.lang.Integer)
	 */
	@Override
	public void setFailedLoginAttempts(Integer count) {
		_failedLoginAttempts = count;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getLastSuccessfulLogin()
	 */
	@Override
	public Date getLastSuccessfulLogin() {
		return _lastSuccessfulLogin;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setLastSuccessfulLogin(java.util.Date)
	 */
	@Override
	public void setLastSuccessfulLogin(Date lastSuccessfulLogin) {
		_lastSuccessfulLogin = lastSuccessfulLogin;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getLastLoginAttempt()
	 */
	@Override
	public Date getLastLoginAttempt() {
		return _lastLoginAttempt;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setLastLoginAttempt(java.util.Date)
	 */
	@Override
	public void setLastLoginAttempt(Date lastLoginAttempt) {
		_lastLoginAttempt = lastLoginAttempt;
	}

    /* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#getPasswordUpdated()
	 */
    @Override
	@Temporal(TemporalType.TIMESTAMP)
    public Date getPasswordUpdated() {
        return _passwordUpdated;
    }

    /* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#setPasswordUpdated(java.util.Date)
	 */
    @Override
	@Temporal(TemporalType.TIMESTAMP)
    public void setPasswordUpdated(Date timestamp) {
		_passwordUpdated = timestamp;
    }

	/* (non-Javadoc)
     * @see org.nrg.xdat.entities.UserAuthI#getLockoutTime()
     */
	@Override
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLockoutTime() {
		return _lockoutTime;
	}

	/* (non-Javadoc)
     * @see org.nrg.xdat.entities.UserAuthI#setLockoutTime(java.util.Date)
     */
	@Override
	@Temporal(TemporalType.TIMESTAMP)
	public void setLockoutTime(Date timestamp) {
		_lockoutTime = timestamp;
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
		return _accountNonExpired;
	}


	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#isAccountNonLocked()
	 */
	@Override
	@Transient
	public boolean isAccountNonLocked() {
		return _accountNonLocked;
	}


	/* (non-Javadoc)
	 * @see org.nrg.xdat.entities.UserAuthI#isCredentialsNonExpired()
	 */
	@Override
	@Transient
	public boolean isCredentialsNonExpired() {
		return _credentialsNonExpired;
	}
}
