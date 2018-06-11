/*
 * web: org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.tokens.XnatAuthenticationToken;
import org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

import java.util.List;

@Slf4j
public class XnatDatabaseAuthenticationProvider extends DaoAuthenticationProvider implements XnatAuthenticationProvider {
    public XnatDatabaseAuthenticationProvider(final String displayName, final AliasTokenService aliasTokenService) {
        this(displayName, XdatUserAuthService.LOCALDB, aliasTokenService);
    }

    public XnatDatabaseAuthenticationProvider(final String displayName, final String providerId, final AliasTokenService aliasTokenService) {
        super();
        setPreAuthenticationChecks(new PreAuthenticationChecks());
        _displayName = StringUtils.defaultIfBlank(displayName, "XNAT");
        _providerId = StringUtils.defaultIfBlank(providerId, XdatUserAuthService.LOCALDB);
        _aliasTokenService = aliasTokenService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return _displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderId() {
        return _providerId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthMethod() {
        return XdatUserAuthService.LOCALDB;
    }

    /**
     * Indicates whether the provider should be visible to and selectable by users. <b>false</b> usually indicates an
     * internal authentication provider, e.g. token authentication.
     *
     * @return <b>true</b> if the provider should be visible to and usable by users.
     */
    @Override
    public boolean isVisible() {
        return _visible;
    }

    @Override
    public void setVisible(final boolean visible) {
        _visible = visible;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutoEnabled() {
        return _autoEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoEnabled(final boolean autoEnabled) {
        _autoEnabled = autoEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutoVerified() {
        return _autoVerified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoVerified(final boolean autoVerified) {
        _autoVerified = autoVerified;
    }

    /**
     * @deprecated Ordering of authentication providers is set through the {@link SiteConfigPreferences#getEnabledProviders()} property.
     */
    @Deprecated
    @Override
    public int getOrder() {
        log.info("The order property is deprecated and will be removed in a future version of XNAT.");
        return 0;
    }

    /**
     * @deprecated Ordering of authentication providers is set through the {@link SiteConfigPreferences#setEnabledProviders(List)} property.
     */
    @Deprecated
    @Override
    public void setOrder(final int order) {
        log.info("The order property is deprecated and will be removed in a future version of XNAT.");
    }

    @Override
    public XnatAuthenticationToken createToken(final String username, final String password) {
        return new XnatDatabaseUsernamePasswordAuthenticationToken(username, password);
    }

    @Override
    public boolean supports(final Authentication authentication) {
        if (!supports(authentication.getClass())) {
            return false;
        }

        final Class<? extends UsernamePasswordAuthenticationToken> clazz = authentication.getClass().asSubclass(UsernamePasswordAuthenticationToken.class);
        return clazz.equals(UsernamePasswordAuthenticationToken.class)
               || XnatDatabaseUsernamePasswordAuthenticationToken.class.isAssignableFrom(clazz)
                  && StringUtils.equals(getProviderId(), ((XnatAuthenticationToken) authentication).getProviderId());
    }

    /**
     * This class prefers the {@link XnatDatabaseUsernamePasswordAuthenticationToken} class, but supports the base
     * Spring <b>UsernamePasswordAuthenticationToken</b> class as well.
     *
     * @param authentication The authentication token to test.
     *
     * @return Returns <b>true</b> if the indicated authentication token class is supported by this provider.
     */
    @Override
    public boolean supports(final Class<?> authentication) {
        return XnatDatabaseUsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication) || UsernamePasswordAuthenticationToken.class.equals(authentication);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    protected void additionalAuthenticationChecks(final UserDetails userDetails, final UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (!UserI.class.isAssignableFrom(userDetails.getClass())) {
            throw new AuthenticationServiceException("User details class is not of a type I know how to handle: " + userDetails.getClass());
        }
        final UserI xdatUserDetails = (UserI) userDetails;
        if ((XDAT.getSiteConfigPreferences().getEmailVerification() && !xdatUserDetails.isVerified() && xdatUserDetails.isEnabled()) || !xdatUserDetails.isAccountNonLocked()) {
            throw new CredentialsExpiredException("Attempted login to unverified or locked account: " + xdatUserDetails.getUsername());
        }
        final String principal = authentication.getPrincipal().toString();
        if (!StringUtils.equals(userDetails.getUsername(), principal) && AliasToken.isAliasFormat(principal)) {
            // If we validate the token by alias and secret and that maps to the same user, then skip the super checks.
            final String username = _aliasTokenService.validateToken(principal, authentication.getCredentials().toString());
            if (StringUtils.equals(userDetails.getUsername(), username)) {
                return;
            }
        }
        super.additionalAuthenticationChecks(userDetails, authentication);
    }

    private class PreAuthenticationChecks implements UserDetailsChecker {
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                logger.debug("User account is locked");

                throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked",
                                                              "User account is locked: " + user.getUsername()));
            }

            if (!user.isEnabled() && !disabledDueToInactivity(user)) {
                logger.debug("User account is disabled");

                throw new DisabledException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled",
                                                                "User is disabled: " + user.getUsername()));
            }

            if (!user.isAccountNonExpired()) {
                logger.debug("User account is expired");

                throw new AccountExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired",
                                                                      "User account has expired: " + user.getUsername()));
            }
        }

        private boolean disabledDueToInactivity(UserDetails user) {
            try {
                UserI xdatUserDetails = (UserI) user;
                if (!xdatUserDetails.isEnabled()) {
                    String query = "SELECT COUNT(*) AS count " +
                                   "FROM xdat_user_history " +
                                   "WHERE xdat_user_id=" + xdatUserDetails.getID() + " " +
                                   "AND change_user=" + xdatUserDetails.getID() + " " +
                                   "AND change_date = (SELECT MAX(change_date) " +
                                   "FROM xdat_user_history " +
                                   "WHERE xdat_user_id=" + xdatUserDetails.getID() + " " +
                                   "AND enabled=1)";
                    Long result = (Long) PoolDBUtils.ReturnStatisticQuery(query, "count", xdatUserDetails.getDBName(), xdatUserDetails.getUsername());

                    return result > 0;
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            return false;
        }
    }

    private final String            _providerId;
    private final AliasTokenService _aliasTokenService;

    private String  _displayName;
    private boolean _autoEnabled;
    private boolean _autoVerified;

    private boolean _visible = true;
}
