/*
 * core: org.nrg.xdat.security.XDATUserMgmtServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserAttributes;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@Service
@Slf4j
public class XDATUserMgmtServiceImpl implements UserManagementServiceI {
    @Autowired
    public XDATUserMgmtServiceImpl(final NamedParameterJdbcTemplate template) {
        _template = template;
    }

    @Override
    public UserI createUser() {
        return new XDATUser();
    }

    @Override
    public boolean exists(final String username) {
        return _template.queryForObject(QUERY_CHECK_USER_EXISTS, new MapSqlParameterSource("username", username), Boolean.class);
    }

    @Nonnull
    @Override
    public UserI getUser(final String username) throws UserNotFoundException, UserInitException {
        if (Users.isGuest(username)) {
            return getGuestUser();
        }
        return new XDATUser(username);
    }

    @Override
    public UserI getUser(final Integer userId) throws UserNotFoundException, UserInitException {
        if (Objects.equals(getGuestUserId(), userId)) {
            return _guest;
        }
        return new XDATUser(Optional.ofNullable(XdatUser.getXdatUsersByXdatUserId(userId, null, true)).orElseThrow(() -> new UserNotFoundException(userId)).getItem());
    }

    @Override
    @Nonnull
    public UserI getGuestUser() throws UserNotFoundException, UserInitException {
        if (_guest == null) {
            final XdatUser user = XdatUser.getXdatUsersByLogin(Users.DEFAULT_GUEST_USERNAME, null, false);
            if (user == null) {
                throw new UserNotFoundException(Users.DEFAULT_GUEST_USERNAME);
            }
            _guest = new XDATUser(user.getItem());
            _guestId = _guest.getID();
        }
        return _guest;
    }

    @Override
    public void invalidateGuest() {
        _guest = null;
        _guestId = null;
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
    public UserI createUser(final Map<String, ?> properties) throws UserFieldMappingException {
        try {
            return new XDATUser(new PopulateItem(properties, null, org.nrg.xft.XFT.PREFIX + ":user", true).getItem());
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
    public void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventDetails event, XdatUserAuth newUserAuth) throws Exception {
        //this calls the other save method, but also takes care of creating the workflow entry for this change.
        if (user.getLogin() == null) {
            throw new NullPointerException();
        }

        final Object id = user.getID() != null ? user.getID() : user.getLogin();

        PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, authenticatedUser, Users.getUserDataType(), id.toString(), PersistentWorkflowUtils.getExternalId(user), event);

        try {
            save(user, authenticatedUser, overrideSecurity, wrk.buildEvent(), newUserAuth);

            if (id.equals(user.getLogin())) {
                //this was a new user or didn't include the user's id.
                //before we save the workflow entry, we should replace the login with the ID.
                Integer userId = Users.getUserId(user.getLogin());
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

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.services.UserManagementServiceI#save(org.nrg.xft.security.UserI, org.nrg.xft.security.UserI, boolean, org.nrg.xft.event.EventDetails)
     */
    @Override
    public void save(UserI user, UserI authenticatedUser, boolean overrideSecurity, EventDetails event) throws Exception {
        save(user, authenticatedUser, overrideSecurity, event, null);
    }

    @Override
    public void save(final UserI user, final UserI authenticatedUser, final boolean overrideSecurity, final EventMetaI event, XdatUserAuth newUserAuth) throws Exception {
        if (user.getLogin() == null) {
            throw new NullPointerException();
        }

        UserI existing = null;
        try {
            existing = Users.getUser(user.getLogin());
        } catch (Exception ignored) {
        }

        final XdatUserAuthService userAuthService = XDAT.getXdatUserAuthService();
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
                if (newUserAuth == null) {
                    newUserAuth = new XdatUserAuth(user.getLogin(), XdatUserAuthService.LOCALDB);
                }
                userAuthService.create(newUserAuth);
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
                final XdatUserAuth auth = userAuthService.getUserByNameAndAuth(user.getLogin(), XdatUserAuthService.LOCALDB, "");
                if (auth != null) {
                    auth.setPasswordUpdated(new Date());
                    userAuthService.resetFailedLogins(auth);
                }
            }
        }
    }

    @Override
    public void save(final UserI user, final UserI authenticatedUser, final boolean overrideSecurity, final EventMetaI event) throws Exception {
        save(user, authenticatedUser, overrideSecurity, event, null);
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

    @Nonnull
    protected Integer getGuestUserId() throws UserNotFoundException, UserInitException {
        if (_guestId == null) {
            getGuestUser();
        }
        return _guestId;
    }

    private static final String QUERY_CHECK_USER_EXISTS = "SELECT EXISTS(SELECT TRUE FROM xdat_user WHERE login = :username) AS exists";

    private final NamedParameterJdbcTemplate _template;

    private UserI   _guest;
    private Integer _guestId;
}
