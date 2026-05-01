/*
 * core: org.nrg.xdat.services.impl.hibernate.HibernateUserRegistrationDataService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.UserRegistrationDataDAO;
import org.nrg.xdat.entities.UserRegistrationData;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class HibernateUserRegistrationDataService extends AbstractHibernateEntityService<UserRegistrationData,UserRegistrationDataDAO> implements UserRegistrationDataService {
    private UserRegistrationDataDAO _dao;

    @Autowired
    public void setUserRegistrationDataDAO(final UserRegistrationDataDAO dao) {
        _dao = dao;
    }

    @Transactional
    @Override
    public UserRegistrationData cacheUserRegistrationData(final UserI user, final String phone, final String organization, final String comments) {
        log.debug("Creating user registration data for login: {}", user.getLogin());
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
        log.debug("Searching for user registration data by login: {}", login);
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
}
