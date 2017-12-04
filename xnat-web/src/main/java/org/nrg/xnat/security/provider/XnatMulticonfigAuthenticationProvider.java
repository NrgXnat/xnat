/*
 * web: org.nrg.xnat.security.provider.XnatAuthenticationProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.provider;

import java.util.List;

/**
 * Defines the interface for authentication providers within XNAT. This expands on the {@link XnatAuthenticationProvider base
 * authentication provider interface} to add the ability to support multiple providers within a single instance of a provider
 * implementation. For example, an LDAP provider may actually support multiple LDAP repositories. For this purpose, the XNAT
 * multi-configuration interface supports multiple IDs, names, and other properties.
 */
public interface XnatMulticonfigAuthenticationProvider extends XnatAuthenticationProvider {
    /**
     * Gets the provider IDs for the XNAT authentication provider. This is used to map the properties associated with the
     * provider instance. If only one provider is configured, then a list is returned with just that single ID.
     *
     * @return The provider IDs for the XNAT authentication provider.
     */
    List<String> getProviderIds();

    /**
     * Gets the {@link XnatAuthenticationProvider authentication provider instance} with the indicated provider ID.
     *
     * @param providerId The ID of the provider to retrieve.
     *
     * @return The requested provider or null if the ID doesn't exist in this provider configuration.
     */
    XnatAuthenticationProvider getProvider(final String providerId);

    /**
     * Gets the display name for the specified XNAT authentication provider. This is what's displayed to the user when selecting
     * the authentication method.
     *
     * @param providerId The provider to be retrieved.
     *
     * @return The display name for the specified XNAT authentication provider.
     */
    String getName(final String providerId);

    /**
     * Indicates whether the specified provider should be visible to and selectable by users. <b>false</b> usually indicates an
     * internal authentication provider, e.g. token authentication.
     *
     * @param providerId The provider to be retrieved.
     *
     * @return <b>true</b> if the provider should be visible to and usable by users.
     */
    boolean isVisible(final String providerId);

    /**
     * Sets whether the provider should be visible to and selectable by users. <b>false</b> usually indicates an internal
     * authentication provider, e.g. token authentication.
     *
     * @param providerId The provider to be set.
     * @param visible    Whether the provider should be visible to and usable by users.
     */
    void setVisible(final String providerId, final boolean visible);

    /**
     * Indicates the order precedence associated with this provider. This is used to determine the order in which the providers
     * show up in the login drop-down list and the order in which they are checked when a login is attempted.
     *
     * @param providerId The provider to be retrieved.
     *
     * @return The order for the specified provider.
     */
    int getOrder(final String providerId);

    /**
     * Sets the order precedence associated with this provider. This is used to determine the order in which the providers
     * show up in the login drop-down list and the order in which they are checked when a login is attempted. Note that, if multiple
     * provider configurations are defined for this instance, this method has no effect. You should call {@link #setOrder(int)} in that case.
     *
     * @param providerId The provider to be set.
     * @param order      The order to set for this provider.
     */
    void setOrder(final String providerId, final int order);
}
