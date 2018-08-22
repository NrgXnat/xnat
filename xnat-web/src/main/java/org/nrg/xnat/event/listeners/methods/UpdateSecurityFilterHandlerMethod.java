/*
 * web: org.nrg.xnat.event.listeners.methods.UpdateSecurityFilterHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.nrg.xapi.rest.aspects.XapiRequestMappingAspect;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.XnatLogoutSuccessHandler;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.config.http.ChannelAttributeFactory;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static org.nrg.xdat.security.helpers.Users.ROLE_ADMIN;
import static org.nrg.xdat.security.helpers.Users.ROLE_USER;

/**
 * Handles changes to the {@link SiteConfigPreferences site configuration preferences} that affect the primary security filter. This
 * also doubles as the initial configuration processor for the security filter, replacing FilterSecurityInterceptorBeanPostProcessor.
 */
@Component
@Slf4j
public class UpdateSecurityFilterHandlerMethod extends AbstractXnatPreferenceHandlerMethod implements BeanPostProcessor {
    @Autowired
    public UpdateSecurityFilterHandlerMethod(final SiteConfigPreferences preferences, final XnatAppInfo appInfo, final XnatLogoutSuccessHandler logoutSuccessHandler) {
        super(SECURITY_CHANNEL, REQUIRE_LOGIN);
        _openUrls = appInfo.getOpenUrls();
        _adminUrls = appInfo.getAdminUrls();
        _logoutSuccessHandler = logoutSuccessHandler;
        _requireLogin = preferences.getRequireLogin();
        _securityChannel = preferences.getSecurityChannel();
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        switch (preference) {
            case REQUIRE_LOGIN:
                _requireLogin = Boolean.parseBoolean(value);
                updateRequireLogin();
                break;

            case SECURITY_CHANNEL:
                _securityChannel = value;
                updateSecurityChannel();
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String name) throws BeansException {
        return bean;
    }

    /**
     * Processes the submitted bean. This implementation is only interested in two particular beans:
     *
     * <ul>
     * <li>It updates the security metadata source on Spring's <b>FilterSecurityInterceptor</b></li>
     * <li>
     * It also sets the {@link XnatAppInfo#getOpenUrls() open (i.e. unrestricted)} and {@link XnatAppInfo#getAdminUrls() administrative URLs}
     * on the {@link XapiRequestMappingAspect XAPI security manager object}.
     * </li>
     * </ul>
     * <p>
     * The interceptor is also stored for use in later operations in this class's capacity handling changes to the site configuration preferences
     * that require changes in the security filter.
     *
     * @param bean The bean to be processed.
     * @param name The name of the bean to be processed.
     *
     * @return The processed bean.
     */
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String name) {
        log.debug("Post-processing bean: {}", name);

        if (bean instanceof FilterSecurityInterceptor) {
            _interceptor = (FilterSecurityInterceptor) bean;
            updateSecurityFilter();
        } else if (bean instanceof XapiRequestMappingAspect) {
            final XapiRequestMappingAspect aspect = (XapiRequestMappingAspect) bean;
            aspect.setOpenUrls(_openUrls);
            aspect.setAdminUrls(_adminUrls);
        } else if (bean instanceof ChannelProcessingFilter) {
            _channelProcessingFilter = (ChannelProcessingFilter) bean;

        }

        return bean;
    }

    private void updateSecurityFilter() {
        if (_interceptor != null) {
            log.info("Building a security metadata map from the system configuration and settings.");
            final LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<>();

            if (_openUrls.isEmpty()) {
                log.warn("No open URLs found in configuration. This may be OK, but isn't normal.");
            } else {
                log.info(" * Found {} open URLs to configure, setting to 'permitAll': {}", _openUrls.size(), StringUtils.join(_openUrls, ", "));
                for (final String url : _openUrls) {
                    map.put(new AntPathRequestMatcher(url), SecurityConfig.createList(PERMIT_ALL));
                }
            }

            if (_openUrls.isEmpty()) {
                log.warn("No admin URLs found in configuration. This may be OK, but isn't normal.");
            } else {
                log.info(" * Found {} admin URLs to configure, setting to '{}': {}", _adminUrls.size(), ADMIN_EXPRESSION, StringUtils.join(_adminUrls, ", "));
                for (final String adminUrl : _adminUrls) {
                    final String fullUrl;
                    if (adminUrl.endsWith("/*")) {
                        fullUrl = adminUrl + "*";
                    } else if (adminUrl.endsWith("/")) {
                        fullUrl = adminUrl + "**";
                    } else if (!adminUrl.endsWith("/**")) {
                        fullUrl = adminUrl + "/**";
                    } else {
                        fullUrl = adminUrl;
                    }
                    map.put(new AntPathRequestMatcher(fullUrl), SecurityConfig.createList(ADMIN_EXPRESSION));
                }
            }

            final String secure = _requireLogin ? DEFAULT_EXPRESSION : PERMIT_ALL;
            log.info(" * All non-open and non-admin URLs match the default pattern '{}', system {} login, setting to '{}'", DEFAULT_PATTERN, _requireLogin ? "requires" : "does not require", secure);
            map.put(new AntPathRequestMatcher(DEFAULT_PATTERN), SecurityConfig.createList(secure));
            _interceptor.setSecurityMetadataSource(new ExpressionBasedFilterInvocationSecurityMetadataSource(map, new DefaultWebSecurityExpressionHandler()));
        }
    }

    private void updateRequireLogin() {
        _logoutSuccessHandler.setRequireLogin(_requireLogin);
        updateSecurityFilter();
    }

    private void updateSecurityChannel() {
        if (_channelProcessingFilter != null) {
            log.debug("Setting the default pattern required channel to: {}", _securityChannel);
            final LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<>();
            map.put(new AntPathRequestMatcher("/**"), ChannelAttributeFactory.createChannelAttributes(_securityChannel));
            final FilterInvocationSecurityMetadataSource metadataSource = new DefaultFilterInvocationSecurityMetadataSource(map);
            _channelProcessingFilter.setSecurityMetadataSource(metadataSource);
        }
    }

    private static final String PERMIT_ALL          = "permitAll";
    private static final String DEFAULT_PATTERN     = "/**";
    private static final String SECURITY_EXPRESSION = "hasRole('${ROLE}')";
    private static final String ADMIN_EXPRESSION    = StringSubstitutor.replace(SECURITY_EXPRESSION, ImmutableMap.of("ROLE", ROLE_ADMIN));
    private static final String DEFAULT_EXPRESSION  = StringSubstitutor.replace(SECURITY_EXPRESSION, ImmutableMap.of("ROLE", ROLE_USER));
    private static final String SECURITY_CHANNEL    = "securityChannel";
    private static final String REQUIRE_LOGIN       = "requireLogin";

    private final XnatLogoutSuccessHandler _logoutSuccessHandler;
    private final List<String>             _openUrls;
    private final List<String>             _adminUrls;

    private FilterSecurityInterceptor _interceptor;
    private ChannelProcessingFilter   _channelProcessingFilter;
    private boolean                   _requireLogin;
    private String                    _securityChannel;
}
