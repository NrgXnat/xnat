/*
 * core: org.nrg.xdat.services.impl.hibernate.HibernateUserRegistrationDataService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * HibernateUserRegistrationDataService
 * (C) 2013 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 6/26/13 by rherri01
 */
package org.nrg.xdat.services.impl.hibernate;

import javax.inject.Inject;

import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.UserRegistrationDataDAO;
import org.nrg.xdat.entities.UserRegistrationData;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HibernateUserRegistrationDataService extends AbstractHibernateEntityService<UserRegistrationData,UserRegistrationDataDAO> implements UserRegistrationDataService {

    @Transactional
    @Override
    public UserRegistrationData cacheUserRegistrationData(final UserI user, final String phone, final String organization, final String comments) throws NrgServiceException{
        if (_log.isDebugEnabled()) {
            _log.debug("Creating user registration data for login: " + user.getLogin());
        }
        UserRegistrationData data = newEntity();
        data.setLogin(user.getLogin());
        data.setPhone(phone);
        data.setOrganization(organization);
        data.setComments(comments);
        _dao.create(data);
        return data;
    }

    @Transactional
    @Override
    public UserRegistrationData getUserRegistrationData(final UserI user) {
        return getUserRegistrationData(user.getLogin());
    }

    @Transactional
    @Override
    public UserRegistrationData getUserRegistrationData(final String login) {
        if (_log.isDebugEnabled()) {
            _log.debug("Searching for user registration data by login: " + login);
        }
        return _dao.findByLogin(login);
    }

    @Transactional
    @Override
    public void clearUserRegistrationData(final UserI user) {
        final UserRegistrationData data = _dao.findByLogin(user.getLogin());
        if (data != null) {
            _dao.delete(data);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(HibernateUserRegistrationDataService.class);

    @Inject
    private UserRegistrationDataDAO _dao;
}
