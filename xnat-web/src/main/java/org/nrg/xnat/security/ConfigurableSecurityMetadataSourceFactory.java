package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;

@Component
@Slf4j
public class ConfigurableSecurityMetadataSourceFactory {
    @Autowired
    public ConfigurableSecurityMetadataSourceFactory(final SiteConfigPreferences preferences, final XnatAppInfo appInfo) {
        _preferences = preferences;
        _appInfo = appInfo;
    }

    public ExpressionBasedFilterInvocationSecurityMetadataSource getMetadataSource() {
        final LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<>();

        for (final String url : _appInfo.getOpenUrls()) {
            if (log.isDebugEnabled()) {
                log.debug("Setting permitAll on the open URL: " + url);
            }
            map.put(new AntPathRequestMatcher(url), SecurityConfig.createList(PERMIT_ALL));
        }

        for (final String adminUrl : _appInfo.getAdminUrls()) {
            if (log.isDebugEnabled()) {
                log.debug("Setting permissions on the admin URL: " + adminUrl);
            }
            String tempAdminUrl = adminUrl;
            if (tempAdminUrl.endsWith("/*")) {
                tempAdminUrl += "*";
            } else if (tempAdminUrl.endsWith("/")) {
                tempAdminUrl += "**";
            } else if (!tempAdminUrl.endsWith("/**")) {
                tempAdminUrl += "/**";
            }
            map.put(new AntPathRequestMatcher(tempAdminUrl), SecurityConfig.createList(ADMIN_EXPRESSION));
        }

        final String secure = _preferences.getRequireLogin() ? DEFAULT_EXPRESSION : PERMIT_ALL;
        if (log.isDebugEnabled()) {
            log.debug("Setting " + secure + " on the default pattern: " + DEFAULT_PATTERN);
        }
        map.put(new AntPathRequestMatcher(DEFAULT_PATTERN), SecurityConfig.createList(secure));
        return new ExpressionBasedFilterInvocationSecurityMetadataSource(map, new DefaultWebSecurityExpressionHandler());
    }

    private static final String PERMIT_ALL         = "permitAll";
    private static final String DEFAULT_PATTERN    = "/**";
    private static final String ADMIN_EXPRESSION   = "hasRole('ROLE_ADMIN')";
    private static final String DEFAULT_EXPRESSION = "hasRole('ROLE_USER')";

    private final SiteConfigPreferences _preferences;
    private final XnatAppInfo           _appInfo;
}
