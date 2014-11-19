package org.nrg.xdat.entities;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;

public interface UserAuthI {

	public abstract String getXdatUsername();

	public abstract void setXdatUsername(String xdatUsername);

	public abstract String getAuthUser();

	public abstract void setAuthUser(String user);

	public abstract String getAuthMethod();

	public abstract void setAuthMethod(String means);

	public abstract String getAuthMethodId();

	public abstract void setAuthMethodId(String means);

	public abstract Integer getFailedLoginAttempts();

	public abstract void setFailedLoginAttempts(Integer count);

	public abstract Date getLastSuccessfulLogin();

	public abstract void setLastSuccessfulLogin(Date lastSuccessfulLogin);

	@Temporal(TemporalType.TIMESTAMP)
	public abstract Date getPasswordUpdated();

	@Temporal(TemporalType.TIMESTAMP)
	public abstract void setPasswordUpdated(Date timestamp);

	@Transient
	public abstract Collection<GrantedAuthority> getAuthorities();

	@Transient
	public abstract boolean isAccountNonExpired();

	@Transient
	public abstract boolean isAccountNonLocked();

	@Transient
	public abstract boolean isCredentialsNonExpired();

	public abstract boolean isEnabled();

}