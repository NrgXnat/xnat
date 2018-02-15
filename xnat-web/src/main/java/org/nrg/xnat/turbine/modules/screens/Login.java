/*
 * web: org.nrg.xnat.turbine.modules.screens.Login
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.jetbrains.annotations.NotNull;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

import java.io.IOException;
import java.util.*;

import static org.nrg.xdat.services.XdatUserAuthService.LOCALDB;

@Slf4j
public class Login extends org.nrg.xdat.turbine.modules.screens.Login {
    @Override
    protected void doBuildTemplate(final RunData data, final Context context) throws Exception {
        context.put("login_methods", getVisibleEnabledProviders());

        final ThemeService themeService   = XDAT.getThemeService();
        final String       themedRedirect = themeService.getThemePage("Login");
        if (StringUtils.isNotBlank(themedRedirect)) {
            context.put("themedRedirect", themedRedirect);
            return;
        }
        String themedStyle = themeService.getThemePage("theme", "style");
        if (themedStyle != null) {
            context.put("themedStyle", themedStyle);
        }
        String themedScript = themeService.getThemePage("theme", "script");
        if (themedScript != null) {
            context.put("themedScript", themedScript);
        }
    }

    @NotNull
    private List<Pair<String, String>> getVisibleEnabledProviders() throws ConfigServiceException, IOException {
        final List<Pair<String, String>> visibleEnabledProviders = new ArrayList<>();
        for (final String providerId : getEnabledProviders()) {
            if (!getProviders().containsKey(providerId)) {
                log.warn("The provider ID {} is enabled, but there is no configured definition for that ID", providerId);
                continue;
            }
            final XnatAuthenticationProvider provider = getProviders().get(providerId);
            if (!provider.isVisible()) {
                log.warn("The provider ID {} is enabled, but the provider is not a visible provider", providerId);
                continue;
            }
            visibleEnabledProviders.add(new ImmutablePair<>(provider.getProviderId(), provider.getName()));
        }
        return visibleEnabledProviders;
    }

    private Map<String, XnatAuthenticationProvider> getProviders() {
        if (_providers.isEmpty()) {
            for (final AuthenticationProvider provider : XDAT.getContextService().getBean("authenticationManager", ProviderManager.class).getProviders()) {
                if (provider instanceof XnatAuthenticationProvider) {
                    final XnatAuthenticationProvider authProvider = (XnatAuthenticationProvider) provider;
                    _providers.put(authProvider.getProviderId(), authProvider);
                }
            }
        }
        return _providers;
    }

    private List<String> getEnabledProviders() throws ConfigServiceException, IOException {
        if (_enabled.isEmpty()) {
            _enabled.addAll(XDAT.getContextService().getBean(SerializerService.class).deserializeJson(XDAT.getSiteConfigurationProperty("enabledProviders", LOCALDB), SerializerService.TYPE_REF_LIST_STRING));
        }
        return _enabled;
    }

    private static final Map<String, XnatAuthenticationProvider> _providers = Collections.synchronizedMap(new HashMap<String, XnatAuthenticationProvider>());
    private static final List<String>                            _enabled   = Collections.synchronizedList(new ArrayList<String>());
}
