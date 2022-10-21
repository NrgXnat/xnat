package org.nrg.xnat.security.provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;

import java.util.List;

@Getter
@Setter
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractBaseXnatAuthenticationProvider implements XnatAuthenticationProvider {
    public static final String ATTRIBUTE_LINK = "link";

    private final String _authMethod;
    private final String _providerId;
    private final String _name;

    private boolean _visible;
    private boolean _autoEnabled;
    private boolean _autoVerified;
    private String  _link;

    @SuppressWarnings("unused")
    protected AbstractBaseXnatAuthenticationProvider(final ProviderAttributes attributes) {
        this(attributes.getProviderId(), attributes);
    }

    protected AbstractBaseXnatAuthenticationProvider(final String providerId, final ProviderAttributes attributes) {
        _authMethod   = attributes.getAuthMethod();
        _providerId   = providerId;
        _name         = attributes.getName();
        _visible      = attributes.getQualifiedBoolean(ProviderAttributes.PROVIDER_VISIBLE, attributes::isVisible);
        _autoEnabled  = attributes.getQualifiedBoolean(ProviderAttributes.PROVIDER_AUTO_ENABLED, attributes::isAutoEnabled);
        _autoVerified = attributes.getQualifiedBoolean(ProviderAttributes.PROVIDER_AUTO_VERIFIED, attributes::isAutoVerified);
        _link         = attributes.getProperty(ATTRIBUTE_LINK, () -> attributes.getQualifiedProperty(ATTRIBUTE_LINK));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLink() {
        return StringUtils.isNotBlank(_link);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLink() {
        return _link;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Ordering of authentication providers is set through the {@link SiteConfigPreferences#getEnabledProviders()} property.
     */
    @Deprecated
    public int getOrder() {
        log.warn("The order property is deprecated and will be removed in a future version of XNAT.");
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Ordering of authentication providers is set through the {@link SiteConfigPreferences#setEnabledProviders(List)} property.
     */
    @Deprecated
    public void setOrder(int order) {
        log.warn("The order property is deprecated and will be removed in a future version of XNAT.");
    }

}
