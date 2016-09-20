/*
 * core: org.nrg.xdat.entities.UserRegistrationData
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * UserRegistrationData
 * (C) 2013 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 6/26/13 by rherrick
 */
package org.nrg.xdat.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class UserRegistrationData extends AbstractHibernateEntity {

    private static final long serialVersionUID = 6837523294464366339L;

    /**
     * The login name of the user. This maps directly to the {@link org.nrg.xft.security.UserI#getLogin()} property.
     * @return The login name for the registered user with which this registration data is associated.
     */
    @Column(unique = true, nullable = false)
    public String getLogin() {
        return _login;
    }

    public void setLogin(final String login) {
        if (_log.isDebugEnabled()) {
            _log.debug("Setting login name of user registration data to: " + login);
        }
        _login = login;
    }

    public String getPhone() {
        return _phone;
    }

    public void setPhone(final String phone) {
        if (_log.isDebugEnabled()) {
            _log.debug("Setting phone of user registration data to: " + phone);
        }
        _phone = phone;
    }

    public String getOrganization() {
        return _organization;
    }

    public void setOrganization(final String organization) {
        if (_log.isDebugEnabled()) {
            _log.debug("Setting organization of user registration data to: " + organization);
        }
        _organization = organization;
    }

    public String getComments() {
        return _comments;
    }

    public void setComments(final String comments) {
        if (_log.isDebugEnabled()) {
            _log.debug("Setting comments of user registration data to: " + comments);
        }
        _comments = comments;
    }

    private static final Logger _log = LoggerFactory.getLogger(UserRegistrationData.class);

    private String _login;
    private String _phone;
    private String _organization;
    private String _comments;
}
