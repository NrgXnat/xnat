package org.nrg.xnat.security.provider;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractBaseXnatMulticonfigAuthenticationProvider implements XnatMulticonfigAuthenticationProvider {
    private final Map<String, ProviderAttributes>         _providerAttributes = new HashMap<>();
    private final Map<String, XnatAuthenticationProvider> _providers          = new HashMap<>();

    protected AbstractBaseXnatMulticonfigAuthenticationProvider(final AuthenticationProviderConfigurationLocator locator, final String authMethod) {
        this(locator.getProviderDefinitionsByAuthMethod(authMethod));
    }

    protected AbstractBaseXnatMulticonfigAuthenticationProvider(final Map<String, ProviderAttributes> definitions) {
        if (CollectionUtils.isEmpty(definitions)) {
            return;
        }
        new LinkedList<>(definitions.keySet()).stream().map(definitions::get).forEach(attributes -> {
            final String providerId = attributes.getProviderId();
            _providerAttributes.put(providerId, attributes);
            _providers.put(providerId, createAuthenticationProvider(attributes));
        });
    }

    protected abstract XnatAuthenticationProvider createAuthenticationProvider(final ProviderAttributes attributes);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getProviderIds() {
        return ImmutableList.copyOf(_providerAttributes.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<XnatAuthenticationProvider> getProviders() {
        return new ArrayList<>(_providers.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatAuthenticationProvider getProvider(final String providerId) {
        return _providers.get(providerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(final String providerId) {
        final XnatAuthenticationProvider provider = getProvider(providerId);
        return provider != null ? provider.getName() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisible(final String providerId) {
        final XnatAuthenticationProvider provider = getProvider(providerId);
        return provider != null && provider.isVisible();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(final String providerId, final boolean visible) {
        final XnatAuthenticationProvider provider = getProvider(providerId);
        if (provider != null) {
            provider.setVisible(visible);
            _providerAttributes.get(providerId).setVisible(visible);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + _providers.values().stream()
                                                             .map(XnatAuthenticationProvider::getName)
                                                             .collect(Collectors.joining(", "));
    }
}
