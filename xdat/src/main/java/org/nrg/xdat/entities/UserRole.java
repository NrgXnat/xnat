/*
 * core: org.nrg.xdat.entities.UserRole
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2018, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.entities;

import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;
import org.nrg.framework.orm.hibernate.audit.NrgRevisionEntity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;

/**
 * Associates a role, such as {@link #ROLE_ADMINISTRATOR}, with a username.
 *
 * <p>Role grants and revocations are audited through Hibernate Envers, with the username of the user making the change
 * recorded on the {@link NrgRevisionEntity revision}. Note that the deprecated {@link Auditable} annotation is not a
 * substitute and is retained deliberately: it drives the soft-delete behavior in
 * {@link org.nrg.framework.orm.hibernate.AbstractHibernateEntityService}, so revoking a role marks the row disabled
 * rather than removing it, and Envers records that as a modification rather than a deletion.
 *
 * <p>That soft delete is why the inherited "enabled" and "disabled" properties are explicitly audited below. Envers
 * does not audit {@link javax.persistence.MappedSuperclass} properties unless told to, and without these a revocation
 * would produce an audit row identical to the original grant — recording that something changed but not what.
 */
@Auditable
@Entity
@Audited
@AuditOverride(forClass = AbstractHibernateEntity.class, name = "enabled")
@AuditOverride(forClass = AbstractHibernateEntity.class, name = "disabled")
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
