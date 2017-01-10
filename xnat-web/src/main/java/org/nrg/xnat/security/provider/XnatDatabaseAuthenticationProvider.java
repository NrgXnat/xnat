/*
 * web: org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.provider;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.tokens.XnatDatabaseUsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

public class XnatDatabaseAuthenticationProvider extends DaoAuthenticationProvider implements XnatAuthenticationProvider {

    public XnatDatabaseAuthenticationProvider() {
        super();
        setPreAuthenticationChecks(new PreAuthenticationChecks());
    }

    /**
     * Indicates whether the provider should be visible to and selectable by users. <b>false</b> usually indicates an
     * internal authentication provider, e.g. token authentication.
     *
     * @return <b>true</b> if the provider should be visible to and usable by users.
     */
    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return XnatDatabaseUsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getName() {
        return displayName;
    }

    public void setName(String newName) {
        displayName = newName;
    }

    @Override
    public String getProviderId() {
        return _providerId;
    }

    public void setProviderId(String providerId) {
        _providerId = providerId;
    }

    @Override
    public String getAuthMethod() {
        return XdatUserAuthService.LOCALDB;
    }

    @Override
    public int getOrder() {
        return _order;
    }

    @Override
    public void setOrder(int order) {
        _order = order;
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
        super.additionalAuthenticationChecks(userDetails, authentication);
    }

    public boolean isPlainText() {
        return (this.getPasswordEncoder().getClass() == plainText);
    }

    private String displayName = "";
    private String _providerId = "";
    private Class plainText = PlaintextPasswordEncoder.class;

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

    private int _order = -1;
}
