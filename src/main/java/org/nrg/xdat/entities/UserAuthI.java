/*
 * core: org.nrg.xdat.entities.UserAuthI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.entities;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Date;

public interface UserAuthI {

	String getXdatUsername();

	void setXdatUsername(String xdatUsername);

	String getAuthUser();

	void setAuthUser(String user);

	String getAuthMethod();

	void setAuthMethod(String means);

	String getAuthMethodId();

	void setAuthMethodId(String means);

	Integer getFailedLoginAttempts();

	void setFailedLoginAttempts(Integer count);

	Date getLastSuccessfulLogin();

	void setLastSuccessfulLogin(Date lastSuccessfulLogin);

	Date getLastLoginAttempt();

	void setLastLoginAttempt(Date lastLoginAttempt);

	@Temporal(TemporalType.TIMESTAMP)
	Date getPasswordUpdated();

	@Temporal(TemporalType.TIMESTAMP)
	void setPasswordUpdated(Date timestamp);

	@Temporal(TemporalType.TIMESTAMP)
	Date getLockoutTime();

	@Temporal(TemporalType.TIMESTAMP)
	void setLockoutTime(Date timestamp);

	@Transient
	Collection<GrantedAuthority> getAuthorities();

	@Transient
	boolean isAccountNonExpired();

	@Transient
	boolean isAccountNonLocked();

	@Transient
	boolean isCredentialsNonExpired();

	boolean isEnabled();

	void setEnabled(boolean enabled);

}
