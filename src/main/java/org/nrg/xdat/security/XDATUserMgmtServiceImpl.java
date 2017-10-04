/*
 * core: org.nrg.xdat.security.XDATUserMgmtServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.Authenticator.Credentials;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.security.validators.PasswordValidatorChain;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserAttributes;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
@Service
public class XDATUserMgmtServiceImpl implements UserManagementServiceI {
    private UserI _guest;
    private String _guestName;
    private Integer _guestId;

    @Override
    public UserI createUser() {
        return new XDATUser();
    }

    @Override
    public UserI getUser(String username) throws UserNotFoundException, UserInitException {
        if (StringUtils.equals(_guestName, username)) {
            return _guest;
        }
        return new XDATUser(username);
    }

    @Override
    public UserI getUser(Integer userId) throws UserNotFoundException, UserInitException {
        if (Objects.equals(_guestId, userId)) {
            return _guest;
        }
        XdatUser u = XdatUser.getXdatUsersByXdatUserId(userId, null, true);
        if (u != null) {
            return new XDATUser(u.getItem());
        } else {
            throw new UserNotFoundException(userId);
        }
    }

    @Override
    @Nonnull
    public UserI getGuestUser() throws UserNotFoundException, UserInitException {
        if (_guest == null) {
            try {
                _guest = getUser(XDAT.getSiteConfigurationProperty("security.user.guestName", "guest"));
            } catch (ConfigServiceException e) {
                logger.error("", e);
                _guest = getUser("guest");
            }
            if (_guest == null) {
                logger.error("Unable to create the guest user");
            } else {
                _guestId = _guest.getID();
                _guestName = _guest.getLogin();
            }
        }
        return _guest;
    }

    @Override
    public void invalidateGuest() {
        _guest = null;
        _guestId = null;
        _guestName = null;
    }

    @Override
    public List<UserI> getUsers() {
        return Lists.transform(XdatUser.getAllXdatUsers(null, false), XdatUserToUserITransform.getInstance());
    }

    @Override
    @Nonnull
    public List<UserI> getUsersByEmail(String email) {
        return Lists.transform(XdatUser.getXdatUsersByField("xdat:user.email", email, null, true), XdatUserToUserITransform.getInstance());
    }

    @Override
    public String getUserDataType() {
        return XdatUser.SCHEMA_ELEMENT_NAME;
    }

    @Override
    public UserI createUser(Map<String, ?> properties) throws UserFieldMappingException, UserInitException {
        try {
            PopulateItem populator = new PopulateItem(properties, null, org.nrg.xft.XFT.PREFIX + ":user", true);
            ItemI        found     = populator.getItem();
            return new XDATUser(found);
        } catch (Exception e) {
            throw new UserFieldMappingException(e);
        }
    }

    @Override
    public void clearCache(UserI user) {
        if (user instanceof XDATUser) {
            ((XDATUser) user).clearLocalCache();
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.services.UserManagementServiceI#save(org.nrg.xft.security.UserI, org.nrg.xft.security.UserI, boolean, org.nrg.xft.event.EventDetails)
     */
    @Override
    public void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventDetails event) throws Exception {
        //this calls the other save method, but also takes care of creating the workflow entry for this change.
        if (user.getLogin() == null) {
            throw new NullPointerException();
        }

        final Object id = user.getID() != null ? user.getID() : user.getLogin();

        PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, Users.getUserDataType(), id.toString(), PersistentWorkflowUtils.getExternalId(user), event);

        try {
            save(user, authenticatedUser, overrideSecurity, wrk.buildEvent());

            if (id.equals(user.getLogin())) {
                //this was a new user or didn't include the user's id.
                //before we save the workflow entry, we should replace the login with the ID.
                Integer userId = Users.getUserid(user.getLogin());
                if (userId != null) {
                    wrk.setId(user.getID().toString());
                } else {
                    throw new Exception("Couldn't find a user for the indicated login: " + user.getLogin());
                }
            }

            PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
        } catch (Exception e) {
            PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
            throw e;
        }
    }

    @Override
    public void save(final UserI user, final UserI authenticatedUser, final boolean overrideSecurity, final EventMetaI event) throws Exception {
        if (user.getLogin() == null) {
            throw new NullPointerException();
        }

        UserI existing = null;
        try {
            existing = Users.getUser(user.getLogin());
        } catch (Exception ignored) {
        }

        if (existing == null) {
            // NEW USER
            if (overrideSecurity || Roles.isSiteAdmin(authenticatedUser)) {
                final String password = user.getPassword();
                if (StringUtils.isNotBlank(password)) {
                    final String message = XDAT.getContextService().getBean(PasswordValidatorChain.class).isValid(password, null);
                    if (StringUtils.isNotBlank(message)) {
                        throw new PasswordComplexityException(message);
                    }
                    //this is set to null instead of authenticatedUser because new users should be able to use any password even those that have recently been used by other users.
                    final String salt = StringUtils.defaultIfBlank(user.getSalt(), Users.createNewSalt());
                    user.setSalt(salt);
                    if (user.getPassword() == null || user.getPassword().length() != 64) {
                        user.setPassword(Users.encode(password, salt));
                    }
                }

                SaveItemHelper.authorizedSave(((XDATUser) user), authenticatedUser, true, false, event);
                XdatUserAuth newUserAuth = new XdatUserAuth(user.getLogin(), XdatUserAuthService.LOCALDB);
                XDAT.getXdatUserAuthService().create(newUserAuth);
            } else {
                throw new InvalidPermissionException("Unauthorized user modification attempt");
            }
        } else {
            final Map<UserAttributes, String> passwordAndSalt = Users.getUpdatedPassword(existing, user);

            user.setPassword(passwordAndSalt.get(UserAttributes.password));
            user.setSalt(passwordAndSalt.get(UserAttributes.salt));

            if (overrideSecurity) {
                SaveItemHelper.authorizedSave(((XDATUser) user), authenticatedUser, true, false, event);
            } else if (Roles.isSiteAdmin(authenticatedUser)) {
                SaveItemHelper.authorizedSave(((XDATUser) user), authenticatedUser, false, false, event);
            } else if (user.getLogin().equals(authenticatedUser.getLogin())) {
                //not-admin user is modifying his own account.
                //we only allow him to modify specific fields.
                XDATUser toSave = (XDATUser) createUser();
                toSave.setLogin(authenticatedUser.getLogin());
                toSave.setPassword(user.getPassword());
                toSave.setSalt(user.getSalt());
                toSave.setEmail(user.getEmail());

                if (!user.isEnabled()) {
                    //allowed to disable his own account (but not enable)
                    toSave.setEnabled(false);
                }

                if (!user.isVerified()) {
                    //allowed to un-verify his own account (but not verify)
                    toSave.setVerified(false);
                }

                SaveItemHelper.authorizedSave(toSave, authenticatedUser, false, false, event);

                authenticatedUser.setPassword(user.getPassword());
                authenticatedUser.setSalt(user.getSalt());
                authenticatedUser.setEmail(user.getEmail());

                if (user.isVerified() != null) {
                    authenticatedUser.setVerified(user.isVerified());
                }

                if (user.isEnabled()) {
                    authenticatedUser.setEnabled(Boolean.TRUE);
                }
            } else {
                throw new InvalidPermissionException("Unauthorized user modification attempt");
            }

            // This means the password was actually updated, so reset the password updated date and failed login attempts.
            if (!StringUtils.equals(existing.getPassword(), user.getPassword())) {
                final XdatUserAuth auth = XDAT.getXdatUserAuthService().getUserByNameAndAuth(user.getLogin(), XdatUserAuthService.LOCALDB, "");
                if(auth!=null) {
                    auth.setPasswordUpdated(new Date());
                    auth.setFailedLoginAttempts(0);
                    XDAT.getXdatUserAuthService().update(auth);
                }
            }
        }
    }

    @Override
    public ValidationResultsI validate(UserI user) throws Exception {
        return ((XDATUser) user).validate();
    }

    @Override
    public void enableUser(UserI user, UserI authenticatedUser, EventDetails event) throws Exception {
        user.setEnabled(true);
        Users.save(user, authenticatedUser, false, event);
    }

    @Override
    public void disableUser(UserI user, UserI authenticatedUser, EventDetails event) throws Exception {
        user.setEnabled(false);
        Users.save(user, authenticatedUser, false, event);
    }

    @Override
    public boolean authenticate(UserI user, Credentials credentials) throws Exception {
        return ((XDATUser) user).login(credentials.password);
    }

    private static final Logger logger = LoggerFactory.getLogger(XDATUserMgmtServiceImpl.class);
}
