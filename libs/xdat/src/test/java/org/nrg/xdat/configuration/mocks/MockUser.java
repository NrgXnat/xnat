/*
 * core: org.nrg.xdat.configuration.mocks.MockUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration.mocks;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class MockUser extends AbstractHibernateEntity implements UserI {
    private static final long serialVersionUID = 651358928252294412L;

    private String       _username;
    private String       _firstname;
    private String       _lastname;
    private String       _email;
    private String       _password;
    private Boolean      _verified;
    private Boolean      _encrypt;
    private String       _salt;
    private UserAuthI    _authorization;
    private boolean      _accountNonExpired;
    private boolean      _active;
    private boolean      _accountNonLocked;
    private boolean      _credentialsNonExpired;
    private List<String> _authorities = new ArrayList<>();

    @Transient
    @Override
    public Integer getID() {
        return (int) getId();
    }

    @Column(nullable = false, unique = true, updatable = false)
    @Override
    public String getUsername() {
        return _username;
    }

    public void setUsername(final String username) {
        _username = username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return _accountNonExpired;
    }

    public void setAccountNonExpired(final boolean accountNonExpired) {
        _accountNonExpired = accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return _accountNonLocked;
    }

    public void setAccountNonLocked(final boolean accountNonLocked) {
        _accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return _credentialsNonExpired;
    }

    public void setCredentialsNonExpired(final boolean credentialsNonExpired) {
        _credentialsNonExpired = credentialsNonExpired;
    }

    @Transient
    @Override
    public String getLogin() {
        return _username;
    }

    @Override
    public void setLogin(final String login) {
        _username = login;
    }

    @Transient
    @Override
    public boolean isGuest() {
        return Users.isGuest(_username);
    }

    @Override
    public String getFirstname() {
        return _firstname;
    }

    @Override
    public void setFirstname(final String firstname) {
        _firstname = firstname;
    }

    @Override
    public String getLastname() {
        return _lastname;
    }

    @Override
    public void setLastname(final String lastname) {
        _lastname = lastname;
    }

    @Override
    public String getEmail() {
        return _email;
    }

    @Override
    public void setEmail(final String email) {
        _email = email;
    }

    @Transient
    @Override
    public String getDBName() {
        return null;
    }

    @Transient
    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return getPlainAuthorities().stream().map(Users::getGrantedAuthority).collect(Collectors.toList());
    }

    public void setAuthorities(final List<? extends GrantedAuthority> authorities) {
        setPlainAuthorities(authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    }

    @ElementCollection(targetClass = String.class)
    public List<? extends String> getPlainAuthorities() {
        return _authorities;
    }

    public void setPlainAuthorities(final List<? extends String> authorities) {
        _authorities.clear();
        _authorities.addAll(authorities);
    }

    @Override
    public String getPassword() {
        return _password;
    }

    @Override
    public void setPassword(final String password) {
        _password = password;
    }

    public Boolean getPrimaryPassword_encrypt() {
        return _encrypt;
    }

    @Override
    public void setPrimaryPassword_encrypt(final Object b) {
        _encrypt = (Boolean) b;
    }

    @Transient
    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override
    public void setEnabled(final Object enabled) {
        super.setEnabled(convertObjectToBoolean(enabled));
    }

    @Override
    public Boolean isVerified() {
        return _verified;
    }

    @Override
    public void setVerified(final Object verified) {
        _verified = convertObjectToBoolean(verified);
    }

    @Override
    public String getSalt() {
        return _salt;
    }

    @Override
    public void setSalt(final String salt) {
        _salt = salt;
    }

    @Override
    public boolean isActive() {
        return _active;
    }

    public void setActive(final boolean active) {
        _active = active;
    }

    @Transient
    @Override
    public Date getLastModified() {
        return getTimestamp();
    }

    @SuppressWarnings("JpaAttributeMemberSignatureInspection")
    @OneToOne(targetEntity = XdatUserAuth.class, cascade = CascadeType.ALL)
    @Override
    public UserAuthI getAuthorization() {
        return _authorization;
    }

    @Override
    public UserAuthI setAuthorization(final UserAuthI authorization) {
        final UserAuthI old = _authorization;
        _authorization = authorization;
        return old;
    }

    private boolean convertObjectToBoolean(final Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (Boolean) object;
        }
        return Boolean.parseBoolean(object.toString());
    }
}
