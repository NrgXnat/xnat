package org.nrg.xdat.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.nrg.xdat.security.XDATUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public class XDATUserDetails extends XDATUser implements UserDetails {

    private XdatUserAuth authorization;

    public XDATUserDetails() {
        //
    }

    public XDATUserDetails(String user) throws Exception {
        super(user);
    }

    public XDATUserDetails(XDATUser user) throws Exception {
        super(user);
    }

    public XdatUserAuth getAuthorization() {
        return authorization;
    }

    public XdatUserAuth setAuthorization(XdatUserAuth auth) {
        return authorization = auth;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> list = new HashSet<GrantedAuthority>();
        list.add(new GrantedAuthorityImpl("ROLE_USER"));

        return list;
    }

    public String getPassword() {
        try {
            return this.getStringProperty("primary_password");
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
        return true;
    }
    
    
    //necessary for spring security session management
	public int hashCode() {
        return new HashCodeBuilder(691, 431). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
        	append(this.getUsername()).
            
            toHashCode();
    }

	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        XDATUserDetails rhs = (XDATUserDetails) obj;
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
        	append(this.getUsername(), rhs.getUsername()).
            isEquals();
    }

}
