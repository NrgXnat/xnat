package org.nrg.xnat.security.provider;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Properties;

/**
 * Provides a convenient container for the attributes of an authentication provider stored in an instance of the {@link
 * XnatMulticonfigAuthenticationProvider} class.
 */
public class ProviderAttributes {
    public ProviderAttributes(final String providerId, final String authMethod, final String displayName, final Boolean visible, final Integer order, final Properties properties) {
        _providerId = providerId;
        _authMethod = authMethod;
        _displayName = displayName;
        _visible = ObjectUtils.defaultIfNull(visible, true);
        _order = ObjectUtils.defaultIfNull(order, -1);
        _properties = properties;
    }

    public ProviderAttributes(final XnatAuthenticationProvider provider) {
        _providerId = provider.getProviderId();
        _authMethod = provider.getAuthMethod();
        _displayName = provider.getName();
        _visible = provider.isVisible();
        _order = provider.getOrder();
        _properties = new Properties();
    }

    public ProviderAttributes(final Properties properties) {
        this(properties.getProperty("id"), properties.getProperty("type"), properties.getProperty("name"), Boolean.parseBoolean(properties.getProperty("visible", "true")), Integer.parseInt(properties.getProperty("order", "-1")), properties);
    }

    /**
     * Gets the provider ID for the XNAT authentication provider. This is used to map the properties associated with the
     * provider instance. Note that, if multiple provider configurations are defined for this instance, this method returns
     * null. You should then call {@link XnatMulticonfigAuthenticationProvider#getProviderIds()} to get the list of configured
     * provider IDs.
     *
     * @return The provider ID for the XNAT authentication provider or null if more than one provider is configured.
     */
    public String getProviderId() {
        return _providerId;
    }

    /**
     * Indicates the authentication method associated with this provider, e.g. LDAP, OpenID, etc. This is used to locate
     * the provider based on the user's selected authentication method. Although a single provider can support multiple
     * configurations, it can only have a single authentication method.
     *
     * @return The authentication method for this provider.
     */
    public String getAuthMethod() {
        return _authMethod;
    }

    /**
     * Gets the display name for the XNAT authentication provider. This is what's displayed to the user when selecting
     * the authentication method. As with {@link #getProviderId()}, if multiple provider configurations are defined for this
     * instance, this method returns null.  You should then call {@link XnatMulticonfigAuthenticationProvider#getName(String)}
     * to get the name of a specified provider.
     *
     * @return The display name for the specified XNAT authentication provider.
     */
    public String getName() {
        return _displayName;
    }

    /**
     * Indicates whether the provider should be visible to and selectable by users. <b>false</b> usually indicates an
     * internal authentication provider, e.g. token authentication. Note that, if multiple provider configurations are defined
     * for this instance, the return value for this method is meaningless. In that case, you should call {@link
     * XnatMulticonfigAuthenticationProvider#isVisible(String)}.
     *
     * @return <b>true</b> if the provider should be visible to and usable by users.
     */
    public boolean isVisible() {
        return _visible;
    }

    public void setVisible(final boolean visible) {
        _visible = visible;
    }

    /**
     * Indicates the order precedence associated with this provider. This is used to determine the order in which the providers
     * show up in the login drop-down list and the order in which they are checked when a login is attempted. Note that, if multiple
     * provider configurations are defined for this instance, this method returns 0. You should call {@link
     * XnatMulticonfigAuthenticationProvider#getOrder(String)} in that case.
     *
     * @return The order for this provider.
     */
    public int getOrder() {
        return _order;
    }

    /**
     * Sets the order precedence associated with this provider. This is used to determine the order in which the providers
     * show up in the login drop-down list and the order in which they are checked when a login is attempted. Note that, if multiple
     * provider configurations are defined for this instance, this method has no effect. You should call {@link
     * XnatMulticonfigAuthenticationProvider#setOrder(String, int)} in that case.
     *
     * @param order The order to set for this provider.
     */
    public void setOrder(int order) {
        _order = order;
    }

    public Properties getProperties() {
        return _properties;
    }

    private final String     _providerId;
    private final String     _authMethod;
    private final String     _displayName;
    private final Properties _properties;

    private boolean _visible;
    private int     _order;
}
