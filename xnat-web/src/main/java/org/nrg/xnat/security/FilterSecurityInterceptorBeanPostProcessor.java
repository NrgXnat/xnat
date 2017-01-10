/*
 * web: org.nrg.xnat.security.FilterSecurityInterceptorBeanPostProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Collection;
import java.util.LinkedHashMap;

public class FilterSecurityInterceptorBeanPostProcessor implements BeanPostProcessor {
    @Autowired
    public FilterSecurityInterceptorBeanPostProcessor(final SiteConfigPreferences preferences, final XnatAppInfo appInfo) {
        _preferences = preferences;
        _appInfo = appInfo;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String name) throws BeansException {
        if (_log.isDebugEnabled()) {
            _log.debug("Post-processing bean: " + name);
        }

        if (bean instanceof FilterSecurityInterceptor) {
            final FilterSecurityInterceptor interceptor = (FilterSecurityInterceptor) bean;
            final ExpressionBasedFilterInvocationSecurityMetadataSource metadataSource = getMetadataSource(_preferences.getRequireLogin());
            if (_log.isDebugEnabled()) {
                _log.debug("Found a FilterSecurityInterceptor bean with the following metadata configuration:");
                displayMetadataSource(interceptor.getSecurityMetadataSource());
                _log.debug("Updating the bean with the following metadata configuration:");
                displayMetadataSource(metadataSource);
            }
            interceptor.setSecurityMetadataSource(metadataSource);
        }

        return bean;
    }

    public ExpressionBasedFilterInvocationSecurityMetadataSource getMetadataSource(boolean requiredLogin) {
        final LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<>();

        for (final String url : _appInfo.getOpenUrls()) {
            if (_log.isDebugEnabled()) {
                _log.debug("Setting permitAll on the open URL: " + url);
            }
            map.put(new AntPathRequestMatcher(url), SecurityConfig.createList(PERMIT_ALL));
        }

        for (final String adminUrl: _appInfo.getAdminUrls()) {
            if (_log.isDebugEnabled()) {
                _log.debug("Setting permissions on the admin URL: " + adminUrl);
            }
            map.put(new AntPathRequestMatcher(adminUrl), SecurityConfig.createList(ADMIN_EXPRESSION));
        }

        final String secure = requiredLogin ? DEFAULT_EXPRESSION : PERMIT_ALL;
        if (_log.isDebugEnabled()) {
            _log.debug("Setting " + secure + " on the default pattern: " + DEFAULT_PATTERN);
        }
        map.put(new AntPathRequestMatcher(DEFAULT_PATTERN), SecurityConfig.createList(secure));
        return new ExpressionBasedFilterInvocationSecurityMetadataSource(map, new DefaultWebSecurityExpressionHandler());
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private void displayMetadataSource(final SecurityMetadataSource metadataSource) {
        if (metadataSource != null) {
            _log.debug("Found metadata source configuration, now iterating.");
            for (ConfigAttribute attribute : metadataSource.getAllConfigAttributes()) {
                _log.debug("*** Attribute: " + attribute.getAttribute());
            }
        }
    }

    private static final Log _log = LogFactory.getLog(FilterSecurityInterceptorBeanPostProcessor.class);
    private static final String PERMIT_ALL = "permitAll";
    private static final String DEFAULT_PATTERN = "/**";
    private static final String ADMIN_EXPRESSION = "hasRole('ROLE_ADMIN')";
    private static final String DEFAULT_EXPRESSION = "hasRole('ROLE_USER')";

    private final SiteConfigPreferences _preferences;
    private final XnatAppInfo           _appInfo;
}
