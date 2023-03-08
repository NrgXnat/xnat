/*
 * web: org.nrg.xnat.security.provider.XnatAuthenticationProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.provider;

import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.tokens.XnatAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import java.util.List;

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
     * Indicates whether users who authenticate using this provider definition and then create a new XNAT account should
     * be enabled immediately (in which case this property is true) or requires administrator review and manual enabling.
     *
     * @return Returns true if users should be enabled automatically, false otherwise.
     */
    boolean isAutoEnabled();

    /**
     * Sets whether users who authenticate using this provider definition and then create a new XNAT account should
     * be enabled immediately (in which case this property is true) or requires administrator review and manual enabling.
     *
     * @param autoEnabled Set to true if users should be enabled automatically, false otherwise.
     */
    void setAutoEnabled(final boolean autoEnabled);

    /**
     * Indicates whether users who authenticate using this provider definition and then create a new XNAT account should
     * be verified immediately (in which case this property is true) or requires administrator review and manual verification.
     *
     * @return Returns true if users should be verified automatically, false otherwise.
     */
    boolean isAutoVerified();

    /**
     * Sets whether users who authenticate using this provider definition and then create a new XNAT account should
     * be verified immediately (in which case this property is true) or requires administrator review and manual verification.
     *
     * @param autoVerified Set to true if users should be verified automatically, false otherwise.
     */
    void setAutoVerified(final boolean autoVerified);

    /**
     * Indicates whether the provider has HTML or text to display on the login page.
     *
     * @return Returns <pre>true</pre> if this provider has HTML or text to display on the login page.
     * @see #getLink()
     */
    default boolean hasLink() {
        return false;
    }

    /**
     * Provides the HTML or text to display on the login page for this provider. This may return <pre>null</pre> or a
     * blank string when {@link  #hasLink()} returns <pre>false</pre>.
     *
     * @return The HTML or text to display on the login page.
     * @see #hasLink()
     */
    default String getLink() {
        return null;
    }

    /**
     * @deprecated Ordering of authentication providers is set through the {@link SiteConfigPreferences#getEnabledProviders()} property.
     */
    @Deprecated
    int getOrder();

    /**
     * @deprecated Ordering of authentication providers is set through the {@link SiteConfigPreferences#setEnabledProviders(List)} property.
     */
    @Deprecated
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
