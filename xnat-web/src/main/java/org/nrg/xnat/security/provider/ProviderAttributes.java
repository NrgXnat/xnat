package org.nrg.xnat.security.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Provides a convenient container for the attributes of an authentication provider.
 */
@Slf4j
public class ProviderAttributes {
    public static final String PROVIDER_NAME          = "name";
    public static final String PROVIDER_ID            = "provider.id";
    public static final String PROVIDER_AUTH_METHOD   = "auth.method";
    public static final String PROVIDER_VISIBLE       = "visible";
    public static final String PROVIDER_AUTO_ENABLED  = "auto.enabled";
    public static final String PROVIDER_AUTO_VERIFIED = "auto.verified";

    public ProviderAttributes(final String providerId, final String authMethod, final String displayName, final Boolean visible, final Boolean autoEnabled, final Boolean autoVerified, final Properties properties) {
        _providerId = providerId;
        _authMethod = authMethod;
        _displayName = displayName;

        setVisible(ObjectUtils.defaultIfNull(visible, true));
        setAutoEnabled(ObjectUtils.defaultIfNull(autoEnabled, false));
        setAutoVerified(ObjectUtils.defaultIfNull(autoVerified, false));

        _properties = properties;
    }

    public ProviderAttributes(final Properties properties) {
        this(properties.getProperty(PROVIDER_ID),
             properties.getProperty(PROVIDER_AUTH_METHOD),
             properties.getProperty(PROVIDER_NAME),
             Boolean.parseBoolean(properties.getProperty(PROVIDER_VISIBLE, "true")),
             Boolean.parseBoolean(properties.getProperty(PROVIDER_AUTO_ENABLED, "false")),
             Boolean.parseBoolean(properties.getProperty(PROVIDER_AUTO_VERIFIED, "false")),
             getScrubbedProperties(properties));
    }

    /**
     * Gets the provider ID for the XNAT authentication provider. This is used to map the properties associated with the
     * provider instance. Note that, if multiple provider configurations are defined for this instance, this method returns
     * null.
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
     * instance, this method returns null.
     *
     * @return The display name for the specified XNAT authentication provider.
     */
    public String getName() {
        return _displayName;
    }

    /**
     * Indicates whether the provider should be visible to and selectable by users. <b>false</b> usually indicates an
     * internal authentication provider, e.g. token authentication.
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
     * {@inheritDoc}
     */
    public boolean isAutoEnabled() {
        return _autoEnabled;
    }

    /**
     * {@inheritDoc}
     */
    public void setAutoEnabled(final boolean autoEnabled) {
        _autoEnabled = autoEnabled;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAutoVerified() {
        return _autoVerified;
    }

    /**
     * {@inheritDoc}
     */
    public void setAutoVerified(final boolean autoVerified) {
        _autoVerified = autoVerified;
    }

    /**
     * @deprecated Ordering of authentication providers is set through the {@link SiteConfigPreferences#getEnabledProviders()} property.
     */
    @Deprecated
    public int getOrder() {
        log.info("The order property is deprecated and will be removed in a future version of XNAT.");
        return 0;
    }

    /**
     * @deprecated Ordering of authentication providers is set through the {@link SiteConfigPreferences#setEnabledProviders(List)} property.
     */
    @Deprecated
    public void setOrder(@SuppressWarnings("unused") final int order) {
        log.info("The order property is deprecated and will be removed in a future version of XNAT.");
    }

    public Properties getProperties() {
        return _properties;
    }

    public String getProperty(final String property) {
        return getProperty(property, null);
    }

    public String getProperty(final String property, final String defaultValue) {
        return _properties.getProperty(property, defaultValue);
    }

    @Override
    public String toString() {
        return "Provider " + _displayName + " (" + _authMethod + ": " + _providerId + ") " + _properties;
    }

    private static Properties getScrubbedProperties(final Properties properties) {
        final Properties  scrubbed      = new Properties();
        final Set<String> propertyNames = properties.stringPropertyNames();
        propertyNames.removeAll(EXCLUDED_PROPERTIES);
        for (final String property : propertyNames) {
            scrubbed.setProperty(property, properties.getProperty(property));
        }
        return scrubbed;
    }

    private static final List<String> EXCLUDED_PROPERTIES = Arrays.asList(PROVIDER_ID, PROVIDER_NAME, PROVIDER_AUTH_METHOD, PROVIDER_VISIBLE, PROVIDER_AUTO_ENABLED, PROVIDER_AUTO_VERIFIED, "order");

    private final String     _providerId;
    private final String     _authMethod;
    private final String     _displayName;
    private final Properties _properties;

    private boolean _visible;
    private boolean _autoEnabled;
    private boolean _autoVerified;
}


