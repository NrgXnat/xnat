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

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

@Entity
public class UserRole extends AbstractHibernateEntity {
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

    private String _role;
    private String _username;
}
