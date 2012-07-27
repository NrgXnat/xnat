package org.nrg.xdat.entities;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.apache.commons.lang.StringUtils;

@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"authUser", "authMethodId"}))
public class XdatUserAuth extends AbstractHibernateEntity{

	private String xdatUsername;
	private String authUser;
	private String authMethod;
	private String authMethodId;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private Date passwordUpdated;
	private Integer failedLoginAttempts;
	
	private static final long serialVersionUID = 1L;
	
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
	
	public XdatUserAuth(String user, String method, String methodId, boolean enabled, boolean aNonExpired, boolean nonLocked, boolean cNonExpired, List<GrantedAuthority> auth, String xdatUsername,Integer failedLoginAttempts) {
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
	}

	public String getXdatUsername() {
		return xdatUsername;
	}

	public void setXdatUsername(String xdatUsername) {
		this.xdatUsername = xdatUsername;
	}

	public String getAuthUser() {
		return authUser;
	}

	public void setAuthUser(String user) {
		this.authUser = user;
	}
	
	public String getAuthMethod() {
		return authMethod;
	}

	public void setAuthMethod(String means) {
		this.authMethod = means;
	}
	
	public String getAuthMethodId() {
		return authMethodId;
	}

	public void setAuthMethodId(String means) {
		this.authMethodId = means;
	}

	public Integer getFailedLoginAttempts() {
		if(failedLoginAttempts==null){
			return 0;
		}else{
			return failedLoginAttempts;
		}
	}

	public void setFailedLoginAttempts(Integer count) {
		this.failedLoginAttempts = count;
	}

    @Temporal(TemporalType.TIMESTAMP)
    public Date getPasswordUpdated() {
        return passwordUpdated;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public void setPasswordUpdated(Date timestamp) {
    	passwordUpdated = timestamp;
    }
	
    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof XdatUserAuth)) {
            return false;
        }
        XdatUserAuth other = (XdatUserAuth) object;
        return           StringUtils.equals(getAuthUser(), other.getAuthUser()) &&
                         StringUtils.equals(getAuthMethod(), other.getAuthMethod()) &&
                         StringUtils.equals(getXdatUsername(), other.getXdatUsername());
    }
	

	@Transient
	public Collection<GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> list = new HashSet<GrantedAuthority>();
        list.add(new GrantedAuthorityImpl("ROLE_USER"));
        return list;
	}


	@Transient
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}


	@Transient
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}


	@Transient
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

}
