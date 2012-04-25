/**
 * H2AliasTokenService
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 4/17/12 by rherri01
 */
package org.nrg.xdat.services.impl.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.daos.AliasTokenDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.Override;
import java.lang.String;
import java.util.Set;

@Service
public class HibernateAliasTokenService extends AbstractHibernateEntityService<AliasToken> implements AliasTokenService {
    /**
     * @return A new empty entity object.
     * @see org.nrg.framework.orm.hibernate.BaseHibernateService#newEntity()
     */
    @Override
    public AliasToken newEntity() {
        return new AliasToken();
    }

    @Override
    @Transactional
    public AliasToken issueTokenForUser(final String xdatUserId) throws Exception {
        XDATUser user = new XDATUser(xdatUserId);
        return issueTokenForUser(user);
    }

    @Override
    @Transactional
    public AliasToken issueTokenForUser(final XdatUser xdatUser) {
        return issueTokenForUser(xdatUser, null);
    }

    @Override
    @Transactional
    public AliasToken issueTokenForUser(final XdatUser xdatUser, final boolean isSingleUse) {
        return issueTokenForUser(xdatUser, isSingleUse, null);
    }

    @Override
    @Transactional
    public AliasToken issueTokenForUser(final XdatUser xdatUser, Set<String> validIPAddresses) {
        return issueTokenForUser(xdatUser, false, validIPAddresses);
    }

    @Override
    @Transactional
    public AliasToken issueTokenForUser(final XdatUser xdatUser, boolean isSingleUse, Set<String> validIPAddresses) {
        AliasToken token = newEntity();
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
    public String validateToken(final String alias, final long secret) {
        return validateToken(alias, secret, null);
    }

    @Override
    @Transactional
    public String validateToken(final String alias, final long secret, final String address) {
        AliasToken token = getDao().findByAlias(alias);
        if (token == null) {
            if (_log.isInfoEnabled()) {
                _log.info("Token requested but not found for alias: " + alias);
            }
            return null;
        }
        try {
            return secret == token.getSecret() && token.isValidIPAddress(address) ? token.getXdatUserId() : null;
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
            if (_log.isDebugEnabled()) {
                _log.debug("Invalidating token: " + alias);
            }
            getDao().delete(token);
        }
    }

    /**
     * Gets the {@link AliasTokenDAO alias token DAO} instance for this service.
     * @return The {@link AliasTokenDAO alias token DAO} instance for this service.
     * @see AbstractHibernateEntityService#getDao()
     */
    @Override
    protected AliasTokenDAO getDao() {
        return _dao;
    }

    private static final Log _log = LogFactory.getLog(HibernateAliasTokenService.class);

    @Autowired
    private AliasTokenDAO _dao;
}
