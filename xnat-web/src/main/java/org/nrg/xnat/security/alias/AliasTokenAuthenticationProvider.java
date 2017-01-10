/*
 * web: org.nrg.xnat.security.alias.AliasTokenAuthenticationProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.alias;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AliasTokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider implements XnatAuthenticationProvider {
    @Autowired
    public AliasTokenAuthenticationProvider(final AliasTokenService aliasTokenService, final XdatUserAuthService userAuthService) {
        super();
        _aliasTokenService = aliasTokenService;
        _userAuthService = userAuthService;
    }

    /**
     * Performs authentication with the same contract as {@link AuthenticationManager#authenticate(Authentication)}.
     *
     * @param authentication the authentication request object.
     * @return a fully authenticated object including credentials. May return <code>null</code> if the
     * <code>AuthenticationProvider</code> is unable to support authentication of the passed
     * <code>Authentication</code> object. In such a case, the next <code>AuthenticationProvider</code> that
     * supports the presented <code>Authentication</code> class will be tried.
     * @throws AuthenticationException if authentication fails.
     */
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String     alias = (String) authentication.getPrincipal();
        final AliasToken token = _aliasTokenService.locateToken(alias);
        if (token == null) {
            throw new BadCredentialsException("No valid alias token found for alias: " + alias);
        }
        // Translate the token into the actual user name and allow the DAO to retrieve the user object.
        return super.authenticate(authentication);
    }

    /**
     * Returns <code>true</code> if this <Code>AuthenticationProvider</code> supports the indicated
     * <Code>Authentication</code> object.
     * <p>
     * Returning <code>true</code> does not guarantee an <code>AuthenticationProvider</code> will be able to
     * authenticate the presented instance of the <code>Authentication</code> class. It simply indicates it can support
     * closer evaluation of it. An <code>AuthenticationProvider</code> can still return <code>null</code> from the
     * {@link #authenticate(org.springframework.security.core.Authentication)} method to indicate another <code>AuthenticationProvider</code> should be
     * tried.
     * </p>
     * <p>Selection of an <code>AuthenticationProvider</code> capable of performing authentication is
     * conducted at runtime the <code>ProviderManager</code>.</p>
     *
     * @param authentication DOCUMENT ME!
     * @return <code>true</code> if the implementation can more closely evaluate the <code>Authentication</code> class
     * presented
     */
    @Override
    public boolean supports(final Class<?> authentication) {
        return AliasTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * Indicates whether the provider should be visible to and selectable by users. <b>false</b> usually indicates an
     * internal authentication provider, e.g. token authentication.
     *
     * @return <b>true</b> if the provider should be visible to and usable by users.
     */
    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public String getName() {
        return XdatUserAuthService.TOKEN;
    }

    @Override
    public String getProviderId() {
        return XdatUserAuthService.TOKEN;
    }

    @Override
    public String getAuthMethod() {
        return XdatUserAuthService.TOKEN;
    }

    @Override
    public String toString() {
        return getName();
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
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException("The submitted alias token was empty.");
        }

        if (!UserI.class.isAssignableFrom(userDetails.getClass())) {
            throw new AuthenticationServiceException("User details class is not of a type I know how to handle: " + userDetails.getClass());
        }
        final UserI xdatUserDetails = (UserI) userDetails;
        Users.validateUserLogin(xdatUserDetails);

        String alias  = ((AliasTokenAuthenticationToken) authentication).getAlias();
        String secret = ((AliasTokenAuthenticationToken) authentication).getSecret();
        String userId = _aliasTokenService.validateToken(alias, secret);
        if (StringUtils.isBlank(userId) || !userId.equals(userDetails.getUsername())) {
            throw new BadCredentialsException("The submitted alias token was invalid: " + alias);
        }
    }

    /**
     * Allows subclasses to actually retrieve the <code>UserDetails</code> from an implementation-specific
     * location, with the option of throwing an <code>AuthenticationException</code> immediately if the presented
     * credentials are incorrect (this is especially useful if it is necessary to bind to a resource as the user in
     * order to obtain or generate a <code>UserDetails</code>).<p>Subclasses are not required to perform any
     * caching, as the <code>AbstractUserDetailsAuthenticationProvider</code> will by default cache the
     * <code>UserDetails</code>. The caching of <code>UserDetails</code> does present additional complexity as this
     * means subsequent requests that rely on the cache will need to still have their credentials validated, even if
     * the correctness of credentials was assured by subclasses adopting a binding-based strategy in this method.
     * Accordingly it is important that subclasses either disable caching (if they want to ensure that this method is
     * the only method that is capable of authenticating a request, as no <code>UserDetails</code> will ever be
     * cached) or ensure subclasses implement {@link #additionalAuthenticationChecks(org.springframework.security.core.userdetails.UserDetails,
     * org.springframework.security.authentication.UsernamePasswordAuthenticationToken)} to compare the credentials of a cached <code>UserDetails</code> with
     * subsequent authentication requests.</p>
     * <p>Most of the time subclasses will not perform credentials inspection in this method, instead
     * performing it in {@link #additionalAuthenticationChecks(org.springframework.security.core.userdetails.UserDetails, org.springframework.security.authentication.UsernamePasswordAuthenticationToken)} so
     * that code related to credentials validation need not be duplicated across two methods.</p>
     *
     * @param username       The username to retrieve
     * @param authentication The authentication request, which subclasses <em>may</em> need to perform a binding-based
     *                       retrieval of the <code>UserDetails</code>
     * @return the user information (never <code>null</code> - instead an exception should the thrown)
     * @throws AuthenticationException If the credentials could not be validated (generally a
     *                                 <code>BadCredentialsException</code>, an <code>AuthenticationServiceException</code> or
     *                                 <code>UsernameNotFoundException</code>)
     */
    @Override
    protected UserDetails retrieveUser(final String username, final UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        AliasToken token = _aliasTokenService.locateToken(username);
        if (token == null) {
            throw new UsernameNotFoundException("Unable to locate token with alias: " + username);
        }
        /*
         * We don't really know which provider the user was authenticated under when this token was created.
         * The hack is to return the user details for the most recent successful login of the user, as that is likely the provider that was used.
         * Not perfect, but better than just hard-coding to localdb provider (cause then it won't work for a token created by an LDAP-authenticated user).
         */
        return _userAuthService.getUserDetailsByUsernameAndMostRecentSuccessfulLogin(token.getXdatUserId());
    }

    private final AliasTokenService   _aliasTokenService;
    private final XdatUserAuthService _userAuthService;
    private int _order = -1;
}
