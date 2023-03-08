package org.nrg.xnat.security.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.nrg.xdat.preferences.SiteConfigPreferences;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public ProviderAttributes(final String providerId, final String authMethod, final String name, final Boolean visible, final Boolean autoEnabled, final Boolean autoVerified, final Properties properties) {
        _providerId = providerId;
        _authMethod = authMethod;
        _name       = name;

        setVisible(ObjectUtils.defaultIfNull(visible, true));
        setAutoEnabled(ObjectUtils.defaultIfNull(autoEnabled, false));
        setAutoVerified(ObjectUtils.defaultIfNull(autoVerified, false));

        _properties = getScrubbedProperties(properties);
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
        return _name;
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

    /**
     * Gets the value of the specified property. If the property is not present, this method returns <pre>null</pre>. To
     * specify a default value other than <pre>null</pre>, call {@link #getProperty(String, String)}. To specify a
     * supplier for the default value, call {@link #getProperty(String, Supplier)}.
     *
     * @param property The name of the property to retrieve.
     *
     * @return The value of the qualified property if present, <pre>null</pre> otherwise.
     */
    public String getProperty(final String property) {
        return getProperty(property, () -> null);
    }

    /**
     * Gets the value of the specified property. If the property is not present, this method returns the specified default
     * value. To have the default value be <pre>null</pre>, you can call {@link #getProperty(String)} instead. To
     * specify a supplier for the default value, call {@link #getProperty(String, Supplier)}.
     *
     * @param property     The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not present.
     *
     * @return The value of the property if present, the specified default value otherwise.
     */
    public String getProperty(final String property, final String defaultValue) {
        return getProperty(property, () -> defaultValue);
    }

    /**
     * Gets the value of the specified property. If the property is not present, this method returns the value from the
     * specified supplier. To have the default value be <pre>null</pre>, call {@link #getProperty(String)} instead. To
     * specify a default value directly, call {@link #getProperty(String, String)}.
     *
     * @param property The name of the property to retrieve.
     * @param supplier The supplier of a default value to return if the property is not present.
     *
     * @return The value of the property if present, the value returned from the specified supplier otherwise.
     */
    public String getProperty(final String property, final Supplier<String> supplier) {
        return StringUtils.getIfBlank(_properties.getProperty(property), supplier);
    }

    /**
     * Gets the value of the specified property qualified by the {@link #getAuthMethod() authentication method} and the
     * {@link #getProviderId() provider ID} in the format <pre>auth-method.provider-id.property</pre>. If the property
     * is not present, this method returns <pre>null</pre>. To specify a default value other than <pre>null</pre>, call
     * {@link #getQualifiedProperty(String, String)}. To specify a supplier for the default value, call
     * {@link #getQualifiedProperty(String, Supplier)}.
     *
     * @param property The name of the property to retrieve.
     *
     * @return The value of the qualified property if present, <pre>null</pre> otherwise.
     */
    public String getQualifiedProperty(final String property) {
        return getQualifiedProperty(property, () -> null);
    }

    /**
     * Gets the value of the specified property qualified by the {@link #getAuthMethod() authentication method} and the
     * {@link #getProviderId() provider ID} in the format <pre>auth-method.provider-id.property</pre>. If the property
     * is not present, this method returns the specified default value. To have the default value be <pre>null</pre>,
     * call {@link #getQualifiedProperty(String)} instead. To specify a supplier for the default value, call
     * {@link #getQualifiedProperty(String, Supplier)}.
     *
     * @param property     The name of the property to retrieve.
     * @param defaultValue The default value to return if the qualified property is not present.
     *
     * @return The value of the qualified property if present, the default value otherwise.
     */
    public String getQualifiedProperty(final String property, final String defaultValue) {
        return getQualifiedProperty(property, () -> defaultValue);
    }

    /**
     * Gets the value of the specified property qualified by the {@link #getAuthMethod() authentication method} and the
     * {@link #getProviderId() provider ID} in the format <pre>auth-method.provider-id.property</pre>. If the property
     * is not present, this method returns the value from the specified supplier. To have the default value be
     * <pre>null</pre>, call {@link #getQualifiedProperty(String)}. To specify a default value directly, call
     * {@link #getQualifiedProperty(String, String)}.
     *
     * @param property The name of the property to retrieve.
     * @param supplier The supplier of a default value to return if the qualified property is not present.
     *
     * @return The value of the qualified property if present, the value returned from the specified supplier otherwise.
     */
    public String getQualifiedProperty(final String property, final Supplier<String> supplier) {
        return getProperty(formatQualifiedProperty(property), supplier);
    }

    /**
     * Gets the boolean value of the specified property. If the property is not present, this method returns
     * <pre>null</pre>. To specify a default value other than <pre>null</pre>, call {@link #getBoolean(String, Boolean)}.
     * To specify a supplier for the default value, call {@link #getBoolean(String, Supplier)}.
     *
     * @param property The name of the property to retrieve.
     *
     * @return The boolean value of the qualified property if present, <pre>null</pre> otherwise.
     */
    public Boolean getBoolean(final String property) {
        return getBoolean(property, () -> null);
    }

    /**
     * Gets the boolean value of the specified property. If the property is not present, this method returns the value
     * from the specified supplier. To have the default value be <pre>null</pre>, call {@link #getBoolean(String)}
     * instead. To specify a supplier for the default value, call {@link #getBoolean(String, Supplier)}.
     *
     * @param property     The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not present.
     *
     * @return The value of the property if present, the default value otherwise.
     */
    public Boolean getBoolean(final String property, final Boolean defaultValue) {
        return getBoolean(property, () -> defaultValue);
    }

    /**
     * Gets the boolean value of the specified property. If the property is not present, this method returns the value
     * from the specified supplier. To have the default value be <pre>null</pre>, call {@link #getBoolean(String)}.
     * To specify a default value directly, call {@link #getBoolean(String, Boolean)}.
     *
     * @param property The name of the property to retrieve.
     * @param supplier The supplier of a default value to return if the property is not present.
     *
     * @return The value of the property if present, the value returned from the specified supplier otherwise.
     */
    public Boolean getBoolean(final String property, final Supplier<Boolean> supplier) {
        return Optional.ofNullable(BooleanUtils.toBooleanObject(getProperty(property, () -> null))).orElseGet(supplier);
    }

    /**
     * Gets the boolean value of the specified property qualified by the {@link #getAuthMethod() authentication method}
     * and the {@link #getProviderId() provider ID} in the format <pre>auth-method.provider-id.property</pre>. If the
     * property is not present, or if the value of the property doesn't represent a valid boolean value, this method
     * returns <pre>null</pre>. To specify a default value other than <pre>null</pre> call
     * {@link #getQualifiedBoolean(String, Boolean)}. To specify a supplier for the default value, call
     * {@link #getQualifiedBoolean(String, Supplier)}.
     *
     * @param property The name of the property to retrieve.
     *
     * @return The boolean value of the qualified property if present and valid, <pre>null</pre> otherwise.
     */
    public Boolean getQualifiedBoolean(final String property) {
        return getQualifiedBoolean(property, () -> null);
    }

    /**
     * Gets the boolean value of the specified property qualified by the {@link #getAuthMethod() authentication method}
     * and the {@link #getProviderId() provider ID} in the format <pre>auth-method.provider-id.property</pre>. If the
     * property is not present, or if the value of the property doesn't represent a valid boolean value, this method
     * returns the specified default value. To have the default value be <pre>null</pre>, call
     * {@link #getQualifiedBoolean(String)} instead.  To specify a supplier for the default value, call
     * {@link #getQualifiedBoolean(String, Supplier)}.
     *
     * @param property     The name of the property to retrieve.
     * @param defaultValue The default value to return if the property is not present.
     *
     * @return The boolean value of the qualified property if present and valid, the value from the specified supplier otherwise.
     */
    public Boolean getQualifiedBoolean(final String property, final Boolean defaultValue) {
        return getQualifiedBoolean(property, () -> defaultValue);
    }

    /**
     * Gets the boolean value of the specified property qualified by the {@link #getAuthMethod() authentication method}
     * and the {@link #getProviderId() provider ID} in the format <pre>auth-method.provider-id.property</pre>. If the
     * property is not present, or if the value of the property doesn't represent a valid boolean value, this method
     * returns the value from the specified supplier. To have the default value be <pre>null</pre>, call
     * {@link #getQualifiedBoolean(String)} instead. To specify a default value directly, call
     * {@link #getQualifiedBoolean(String, Boolean)}.
     *
     * @param property The name of the property to retrieve.
     * @param supplier The supplier of a default value to return if the qualified property is not present.
     *
     * @return The boolean value of the qualified property if present and valid, the value from the specified supplier otherwise.
     */
    public Boolean getQualifiedBoolean(final String property, final Supplier<Boolean> supplier) {
        return getBoolean(formatQualifiedProperty(property), supplier);
    }

    @NotNull
    private String formatQualifiedProperty(final String property) {
        return String.join(".", _authMethod, _providerId, property);
    }

    @Override
    public String toString() {
        return "Provider " + _name + " (" + _authMethod + ": " + _providerId + ") " + _properties;
    }

    private static Properties getScrubbedProperties(final Properties properties) {
        if (properties == null || properties.isEmpty()) {
            return new Properties();
        }
        final Properties scrubbed = new Properties();
        scrubbed.putAll(properties.stringPropertyNames().stream()
                                  .filter(name -> !EXCLUDED_PROPERTIES.contains(name))
                                  .collect(Collectors.toMap(Function.identity(), properties::getProperty)));
        return scrubbed;
    }

    private static final Set<String> EXCLUDED_PROPERTIES = Stream.of(PROVIDER_ID, PROVIDER_NAME, PROVIDER_AUTH_METHOD, PROVIDER_VISIBLE, PROVIDER_AUTO_ENABLED, PROVIDER_AUTO_VERIFIED, "order").collect(Collectors.toSet());

    private final String     _providerId;
    private final String     _authMethod;
    private final String     _name;
    private final Properties _properties;

    private boolean _visible;
    private boolean _autoEnabled;
    private boolean _autoVerified;
}
