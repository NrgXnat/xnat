/*
 * org.nrg.xnat.security.provider.XnatAuthenticationProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security.provider;

import org.springframework.security.authentication.AuthenticationProvider;

public interface XnatAuthenticationProvider extends AuthenticationProvider {
    /**
     * Gets the display name for the XNAT authentication provider. This is what's displayed to the user when selecting
     * the authentication method.
     * @return The display name for the XNAT authentication provider.
     */
    abstract public String getName();

    /**
     * Gets the provider ID for the XNAT authentication provider. This is used to map the properties associated with the
     * provider instance.
     * @return The provider ID for the XNAT authentication provider.
     */
    abstract public String getProviderId();

    /**
     * Indicates whether the provider should be visible to and selectable by users. <b>false</b> usually indicates an
     * internal authentication provider, e.g. token authentication.
     * @return <b>true</b> if the provider should be visible to and usable by users.
     */
    abstract public boolean isVisible();
    
    /**
     * Indicates the authentication method associated with this provider. This is used to locate the provider based on
     * the user's selected authentication method.
     * @return The authentication method for this provider.
     */
    abstract public String getAuthMethod();
}
