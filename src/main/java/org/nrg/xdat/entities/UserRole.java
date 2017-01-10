/*
 * core: org.nrg.xdat.entities.UserRole
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * UserRole
 * (C) 2013 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 6/20/13 by Tim Olsen
 */
package org.nrg.xdat.entities;


import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"role", "username","disabled"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class UserRole extends AbstractHibernateEntity {
    public static String ROLE_NON_EXPIRING = "non_expiring";

    public UserRole() {
    }

    /**
     * Gets the role
     * @return A value representing the role.
     */
    public String getRole() {
        return _role;
    }

    /**
     * Sets the role
     * @param role    A value representing the role.
     */
    public void setRole(final String role) {
        _role = role;
    }

    /**
     * Gets the username.
     * @return A value representing the username.
     */
    public String getUsername() {
        return _username;
    }

    /**
     * Sets the username.
     * @param username    A value representing the username
     */
    public void setUsername(final String username) {
    	_username = username;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserRole)) {
            return false;
        }

        final UserRole userRole = (UserRole) o;

        return !(_role != null ? !_role.equals(userRole._role) : userRole._role != null) &&
               !(_username != null ? !_username.equals(userRole._username) : userRole._username != null);
    }

    @Override
    public int hashCode() {
        int result = _role != null ? _role.hashCode() : 0;
        result = 31 * result + (_username != null ? _username.hashCode() : 0);
        return result;
    }

    private String _role;
    private String _username;
}
