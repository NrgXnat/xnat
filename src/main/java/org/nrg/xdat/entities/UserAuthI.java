package org.nrg.xdat.entities;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;

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

	@Transient
	Collection<GrantedAuthority> getAuthorities();

	@Transient
	boolean isAccountNonExpired();

	@Transient
	boolean isAccountNonLocked();

	@Transient
	boolean isCredentialsNonExpired();

	boolean isEnabled();

}