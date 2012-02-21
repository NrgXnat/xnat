package org.nrg.xdat.entities;

import java.util.Collection;
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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"authUser", "authMethod"}))
public class XdatUserAuth extends AbstractHibernateEntity{

	private String xdatUsername;
	private String authUser;
	private String authMethod;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	
	private static final long serialVersionUID = 1L;
	
	public XdatUserAuth(String user, String method) {
		this(user,method,user,true);
	}
	
	public XdatUserAuth(String user, String method, boolean enabled) {
		this(user,method,user,enabled);
		
	}
	
	public XdatUserAuth(String user, String method, String xdat, boolean enabled) {
		this.authUser = user;
		this.authMethod = method;
		setEnabled(enabled);
		accountNonExpired=true;
		accountNonLocked=true;
		credentialsNonExpired=true;
		this.xdatUsername = xdat;
		
	}
	
	public XdatUserAuth(String user, String method, boolean enabled, boolean aNonExpired, boolean nonLocked, boolean cNonExpired, List<GrantedAuthority> auth) {
		this.authUser = user;
		this.authMethod = method;
		setEnabled(enabled);
		accountNonExpired=true;
		accountNonLocked=true;
		credentialsNonExpired=true;
		
	}
	
	public XdatUserAuth(String user, String method, boolean enabled, boolean aNonExpired, boolean nonLocked, boolean cNonExpired, List<GrantedAuthority> auth, String xdatUsername) {
		this.authUser = user;
		this.authMethod = method;
		setEnabled(enabled);
		accountNonExpired=true;
		accountNonLocked=true;
		credentialsNonExpired=true;
		this.xdatUsername = xdatUsername;
	}
	
	public XdatUserAuth() {
		// TODO Auto-generated constructor stub
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
