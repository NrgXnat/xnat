/*
 * core: org.nrg.xdat.configuration.mocks.MockUserService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration.mocks;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
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
    public boolean exists(final String username) {
        try {
            getUser(username);
            return true;
        } catch (UserNotFoundException ignored) {
            return false;
        }
    }

    @Nonnull
    @Override
    public UserI getUser(final String username) throws UserNotFoundException {
        final UserI user = getDao().findByUniqueProperty("username", username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        return user;
    }

    @Override
    public UserI getUser(final Integer userId) throws UserNotFoundException {
        final UserI user = getDao().findByUniqueProperty("id", userId);
        if (user == null) {
            throw new UserNotFoundException(Integer.toString(userId));
        }
        return user;
    }

    @Override
    public List<? extends UserI> getUsersByEmail(final String email) {
        return getDao().findByProperty("email", email);
    }

    @Override
    public UserI getGuestUser() {
        return getDao().findByUniqueProperty(Users.USERNAME_PROPERTY, Users.DEFAULT_GUEST_USERNAME);
    }

    @Override
    public void invalidateGuest() {
        // Nothing here.
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
    public UserI createUser(final Map<String, ?> properties) {
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
    public void save(final UserI user, final UserI authenticatedUser, final boolean overrideSecurity, final EventMetaI event) {
        save(user, null, overrideSecurity, (EventDetails) null);
    }

    @Override
    public void save(final UserI user, final UserI authenticatedUser, final boolean overrideSecurity, final EventDetails event) {
        if (user.getID() == 0) {
            create((MockUser)  user);
        } else {
            update((MockUser) user);
        }
    }

    @Override
    public void save(final UserI user, final UserI authenticatedUser, final boolean overrideSecurity, final EventMetaI event, XdatUserAuth auth) {
        save(user, null, overrideSecurity, (EventDetails) null,auth);
    }

    @Override
    public void save(final UserI user, final UserI authenticatedUser, final boolean overrideSecurity, final EventDetails event, XdatUserAuth auth) {
        if (user.getID() == 0) {
            create((MockUser)  user);
        } else {
            update((MockUser) user);
        }
    }

    @Override
    public ValidationResultsI validate(final UserI user) {
        return null;
    }

    @Override
    public void enableUser(final UserI user, final UserI authenticatedUser, final EventDetails event) {
        user.setEnabled(true);
        update((MockUser) user);
    }

    @Override
    public void disableUser(final UserI user, final UserI authenticatedUser, final EventDetails event) {
        user.setEnabled(false);
        update((MockUser) user);
    }

    @Override
    public boolean authenticate(final UserI user, final Authenticator.Credentials credentials) {
        return StringUtils.equals(user.getUsername(), credentials.getUsername()) && StringUtils.equals(user.getPassword(), credentials.getPassword());
    }
}
