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
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.jetbrains.annotations.NotNull;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

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
    private Set<String> getVisibleEnabledProviders() throws ConfigServiceException {
        final List<XnatAuthenticationProvider> providers        = getProviders();
        final List<String>                     enabledProviders = Arrays.asList(XDAT.getSiteConfigurationProperty("enabledProviders", LOCALDB).split("\\s*,\\s*"));
        final Set<String>                      providerNames    = new HashSet<>();
        for (final XnatAuthenticationProvider provider : providers) {
            if (provider.isVisible() && enabledProviders.contains(provider.getProviderId())) {
                providerNames.add(provider.getName());
            }
        }
        return providerNames;
    }

    private List<XnatAuthenticationProvider> getProviders() {
        if (_providers.isEmpty()) {
            for (final AuthenticationProvider provider : XDAT.getContextService().getBean("authenticationManager", ProviderManager.class).getProviders()) {
                if (provider instanceof XnatAuthenticationProvider) {
                    _providers.add((XnatAuthenticationProvider) provider);
                }
            }
        }
        return _providers;
    }

    private static final List<XnatAuthenticationProvider> _providers = Collections.synchronizedList(new ArrayList<XnatAuthenticationProvider>());
}
