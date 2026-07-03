/*
 * core: org.nrg.xdat.entities.UserRole
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2018, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.entities;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;

@Auditable
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"role", "username", "disabled"}))
@Cacheable
@SuppressWarnings("deprecation")
public class UserRole extends AbstractHibernateEntity {
    public static String ROLE_NON_EXPIRING  = "non_expiring";
    public static String ROLE_ADMINISTRATOR = "Administrator";
    public static String ROLE_PRIVILEGED = "Privileged";


    @SuppressWarnings("unused")
    public UserRole() {
    }

    @SuppressWarnings("unused")
    public UserRole(final String username, final String role) {
        setUsername(username);
        setRole(role);
    }

    /**
     * Gets the role
     *
     * @return A value representing the role.
     */
    public String getRole() {
        return _role;
    }

    /**
     * Sets the role
     *
     * @param role A value representing the role.
     */
    public void setRole(final String role) {
        _role = role;
    }

    /**
     * Gets the username.
     *
     * @return A value representing the username.
     */
    public String getUsername() {
        return _username;
    }

    /**
     * Sets the username.
     *
     * @param username A value representing the username
     */
    public void setUsername(final String username) {
        _username = username;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserRole)) {
            return false;
        }
        final UserRole userRole = (UserRole) object;
        return Objects.equals(getRole(), userRole.getRole()) &&
               Objects.equals(getUsername(), userRole.getUsername()) &&
               Objects.equals(getDisabled(), userRole.getDisabled());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRole(), getUsername(), getDisabled());
    }

    private String _role;
    private String _username;
}
