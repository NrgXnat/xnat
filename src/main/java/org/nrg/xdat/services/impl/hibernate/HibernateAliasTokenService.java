/*
 * core: org.nrg.xdat.services.impl.hibernate.HibernateAliasTokenService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * HibernateAliasTokenService
 * (C) 2016 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 */
package org.nrg.xdat.services.impl.hibernate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.AliasTokenDAO;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

@Service
public class HibernateAliasTokenService extends AbstractHibernateEntityService<AliasToken, AliasTokenDAO> implements AliasTokenService {
    @Autowired
    public HibernateAliasTokenService(final AliasTokenDAO dao, final SiteConfigPreferences preferences, final UserManagementServiceI userService) {
        _dao = dao;
        _preferences = preferences;
        _userService = userService;
    }

    /**
     * Finds all active tokens for a particular user.
     *
     * @param xdatUserId The user ID from the XdatUser table.
     *
     * @return An list of the {@link AliasToken alias tokens} issued to the indicated user.
     */
    @Override
    @Transactional
    public List<AliasToken> findTokensForUser(String xdatUserId) {
        return getDao().findByXdatUserId(xdatUserId);

    }

    /**
     * Finds and deactivates all active tokens for a particular user.
     *
     * @param username The username of the user to deactivate.
     */
    @Override
    @Transactional
    public void deactivateAllTokensForUser(String username) {
        List<AliasToken> tokens = findTokensForUser(username);
        if (tokens != null) {
            for (AliasToken token : tokens) {
                token.setEnabled(false);
            }
        }
    }

    @Override
    @Transactional
    public AliasToken issueTokenForUser(final String username) throws Exception {
        return issueTokenForUser(_userService.getUser(username));
    }

    @Override
    @Transactional
    public AliasToken issueTokenForUser(final UserI xdatUser) {
        return issueTokenForUser(xdatUser, null);
    }

    @Override
    @Transactional
    public AliasToken issueTokenForUser(final UserI xdatUser, final boolean isSingleUse) {
        return issueTokenForUser(xdatUser, isSingleUse, null);
    }

    @Override
    @Transactional
    public AliasToken issueTokenForUser(final UserI xdatUser, Set<String> validIPAddresses) {
        return issueTokenForUser(xdatUser, false, validIPAddresses);
    }

    @Override
    @Transactional
    public AliasToken issueTokenForUser(final UserI xdatUser, boolean isSingleUse, Set<String> validIPAddresses) {
        AliasToken token = newEntity();
        final Calendar calendar = Calendar.getInstance();
        try {
            // Try to get the configuration setting for the alias token timeout.
            final long seconds = SiteConfigPreferences.convertPGIntervalToSeconds(_preferences.getAliasTokenTimeout());
            calendar.add(Calendar.SECOND, ((Long) seconds).intValue());
        } catch (SQLException e) {
            // If that fails--always means number format exception--use two days as the default.
            calendar.add(Calendar.DATE, 2);
        }
        token.setEstimatedExpirationTime(calendar.getTime());
        token.setXdatUserId(xdatUser.getLogin());
        token.setSingleUse(isSingleUse);
        token.setValidIPAddresses(validIPAddresses);
        getDao().create(token);
        if (_log.isDebugEnabled()) {
            _log.debug("Created new token " + token.getAlias() + " for user: " + xdatUser.getLogin());
        }
        return token;
    }

    /**
     * Locates and returns the token indicated by the alias string. The returned token should not be considered fully
     * validated until the {@link org.nrg.xdat.entities.AliasToken#getSecret() token secret} and {@link org.nrg.xdat.entities.AliasToken#getValidIPAddresses() IP
     * addresses} have also been checked and validated against the requesting client.
     *
     * @param alias The alias for the requested token.
     *
     * @return The token matching the indicated alias if one exists; otherwise this returns null.
     */
    @Override
    @Transactional
    public AliasToken locateToken(final String alias) {
        AliasToken token = getDao().findByAlias(alias);
        if (token == null) {
            if (_log.isInfoEnabled()) {
                _log.info("Token location requested but not found for alias: " + alias);
            }
            return null;
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Token located for alias: " + alias);
        }

        return token;
    }

    @Override
    @Transactional
    public String validateToken(final String alias, final String secret) {
        return validateToken(alias, secret, null);
    }

    @Override
    @Transactional
    public String validateToken(final String alias, final String secret, final String address) {
        AliasToken token = getDao().findByAlias(alias);
        if (token == null) {
            if (_log.isInfoEnabled()) {
                _log.info("Token requested but not found for alias: " + alias);
            }
            return null;
        }
        try {
            return StringUtils.equals(secret, token.getSecret()) && token.isValidIPAddress(address) ? token.getXdatUserId() : null;
        } finally {
            if (token.isSingleUse()) {
                getDao().delete(token);
            }
        }
    }

    @Override
    @Transactional
    public void invalidateToken(final String alias) {
        AliasToken token = getDao().findByAlias(alias);
        if (token == null) {
            if (_log.isInfoEnabled()) {
                _log.info("Token requested to be invalidated but not found for alias: " + alias);
            }
        } else {
            invalidateToken(token);
        }
    }

    @Override
    @Transactional
    public void invalidateToken(final AliasToken token) {
        if (token != null) {
            if (_log.isDebugEnabled()) {
                _log.debug("Invalidating token: " + token.getAlias());
            }
            getDao().delete(token);
        }
    }

    @Override
    @Transactional
    public void invalidateExpiredTokens(String interval) {
        final List<AliasToken> tokensToExpire = _dao.findByExpired(interval);
        if (tokensToExpire != null) {
            for (final AliasToken token : tokensToExpire) {
                if (token != null) {
                    invalidateToken(token);
                }
            }
        }
    }

    /**
     * Gets the {@link AliasTokenDAO alias token DAO} instance for this service.
     *
     * @return The {@link AliasTokenDAO alias token DAO} instance for this service.
     *
     * @see AbstractHibernateEntityService#getDao()
     */
    @Override
    protected AliasTokenDAO getDao() {
        return _dao;
    }

    private static final Log _log = LogFactory.getLog(HibernateAliasTokenService.class);

    private final AliasTokenDAO          _dao;
    private final SiteConfigPreferences  _preferences;
    private final UserManagementServiceI _userService;
}
