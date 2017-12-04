/*
 * web: org.nrg.xnat.security.provider.XnatAuthenticationProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.provider;

import org.nrg.xnat.security.tokens.XnatAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

/**
 * Defines the interface for authentication providers within XNAT. This expands on the base authentication provider interface
 * in Spring Security to add the ability to create authentication tokens for a specific provider and manage provider IDs and
 * authentication mechanisms.
 *
 * If you need to support multiple configurations for a single provider, e.g. an LDAP provider that supports multiple LDAP
 * repositories, consider extending the {@link XnatMulticonfigAuthenticationProvider} interface instead.
 */
public interface XnatAuthenticationProvider extends AuthenticationProvider {
    /**
     * Gets the provider ID for the XNAT authentication provider. This is used to map the properties associated with the
     * provider instance. Note that, if multiple provider configurations are defined for this instance, this method returns
     * null. You should then call {@link XnatMulticonfigAuthenticationProvider#getProviderIds()} to get the list of configured
     * provider IDs.
     *
     * @return The provider ID for the XNAT authentication provider or null if more than one provider is configured.
     */
    String getProviderId();

    /**
     * Indicates the authentication method associated with this provider, e.g. LDAP, OpenID, etc. This is used to locate
     * the provider based on the user's selected authentication method. Although a single provider can support multiple
     * configurations, it can only have a single authentication method.
     *
     * @return The authentication method for this provider.
     */
    String getAuthMethod();

    /**
     * Gets the display name for the XNAT authentication provider. This is what's displayed to the user when selecting
     * the authentication method. As with {@link #getProviderId()}, if multiple provider configurations are defined for this
     * instance, this method returns null.  You should then call {@link XnatMulticonfigAuthenticationProvider#getName(String)}
     * to get the name of a specified provider.
     *
     * @return The display name for the specified XNAT authentication provider.
     */
    String getName();

    /**
     * Indicates whether the provider should be visible to and selectable by users. <b>false</b> usually indicates an
     * internal authentication provider, e.g. token authentication. Note that, if multiple provider configurations are defined
     * for this instance, the return value for this method is meaningless. In that case, you should call {@link
     * XnatMulticonfigAuthenticationProvider#isVisible(String)}.
     *
     * @return <b>true</b> if the provider should be visible to and usable by users.
     */
    boolean isVisible();

    /**
     * Sets whether the provider should be visible to and selectable by users. <b>false</b> usually indicates an
     * internal authentication provider, e.g. token authentication.
     *
     * @param visible Whether the provider should be visible to and usable by users.
     */
    void setVisible(final boolean visible);

    /**
     * Indicates the order precedence associated with this provider. This is used to determine the order in which the providers
     * show up in the login drop-down list and the order in which they are checked when a login is attempted. Note that, if multiple
     * provider configurations are defined for this instance, this method returns 0. You should call {@link
     * XnatMulticonfigAuthenticationProvider#getOrder(String)} in that case.
     *
     * @return The order for this provider.
     */
    int getOrder();

    /**
     * Sets the order precedence associated with this provider. This is used to determine the order in which the providers
     * show up in the login drop-down list and the order in which they are checked when a login is attempted. Note that, if multiple
     * provider configurations are defined for this instance, this method has no effect. You should call {@link
     * XnatMulticonfigAuthenticationProvider#setOrder(String, int)} in that case.
     *
     * @param order The order to set for this provider.
     */
    void setOrder(int order);

    /**
     * Creates an authentication token suitable for use with the provider implementation.
     *
     * @param username The username (or principal) for the new token.
     * @param password The password (or credentials) for the new token.
     *
     * @return A new {@link XnatAuthenticationToken authentication token} suitable for use with the provider implementation.
     */
    XnatAuthenticationToken createToken(final String username, final String password);

    /**
     * Indicates whether this implementation supports the particular instance of the submitted {@link Authentication}
     * token. This extends the base {@link #supports(Class)} method, which only checks the type of the token. By also
     * checking the instance, we can test not only for the type of authentication but a particular configuration of that
     * authentication. For example, you could configure multiple LDAP instances against which a user could be authenticated.
     *
     * @param authentication The authentication token to be tested.
     *
     * @return Returns <b>true</b> if this instance of the provider supports the submitted authentication token.
     */
    boolean supports(final Authentication authentication);
}
