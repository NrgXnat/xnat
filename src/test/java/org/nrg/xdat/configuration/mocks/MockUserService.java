/*
 * core: org.nrg.xdat.configuration.mocks.MockUserService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration.mocks;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MockUserService extends AbstractHibernateEntityService<MockUser, MockUserRepository> implements UserManagementServiceI {
    @Override
    public UserI createUser() {
        return newEntity();
    }

    @Override
    public UserI getUser(final String username) throws UserInitException, UserNotFoundException {
        return getDao().findByUniqueProperty("username", username);
    }

    @Override
    public UserI getUser(final Integer userId) throws UserNotFoundException, UserInitException {
        return getDao().findByUniqueProperty("id", userId);
    }

    @Override
    public List<? extends UserI> getUsersByEmail(final String email) {
        return getDao().findByProperty("email", email);
    }

    @Override
    public UserI getGuestUser() throws UserNotFoundException, UserInitException {
        return getDao().findByUniqueProperty("username", "guest");
    }

    @Override
    public List<? extends UserI> getUsers() {
        return getDao().findAllEnabled();
    }

    @Override
    public String getUserDataType() {
        return MockUser.class.getName();
    }

    @Override
    public UserI createUser(final Map<String, ?> properties) throws UserFieldMappingException, UserInitException {
        try {
            final MockUser user = new MockUser();
            BeanUtils.populate(user, properties);
            return user;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearCache(final UserI user) {
        // Nothing to do here.
    }

    @Override
    public void save(final UserI user, final UserI authenticatedUser, final boolean overrideSecurity, final EventMetaI event) throws Exception {
        save(user, null, overrideSecurity, (EventDetails) null);
    }

    @Override
    public void save(final UserI user, final UserI authenticatedUser, final boolean overrideSecurity, final EventDetails event) throws Exception {
        if (user.getID() == 0) {
            create((MockUser)  user);
        } else {
            update((MockUser) user);
        }
    }

    @Override
    public ValidationResultsI validate(final UserI user) throws Exception {
        return null;
    }

    @Override
    public void enableUser(final UserI user, final UserI authenticatedUser, final EventDetails event) throws Exception {
        user.setEnabled(true);
        update((MockUser) user);
    }

    @Override
    public void disableUser(final UserI user, final UserI authenticatedUser, final EventDetails event) throws Exception {
        user.setEnabled(false);
        update((MockUser) user);
    }

    @Override
    public boolean authenticate(final UserI user, final Authenticator.Credentials credentials) throws Exception {
        return StringUtils.equals(user.getUsername(), credentials.getUsername()) && StringUtils.equals(user.getPassword(), credentials.getPassword());
    }
}
