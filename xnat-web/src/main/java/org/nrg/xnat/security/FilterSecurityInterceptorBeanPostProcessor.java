/*
 * org.nrg.xnat.security.FilterSecurityInterceptorBeanPostProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/5/13 3:29 PM
 */
package org.nrg.xnat.security;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.constants.Scope;
import org.nrg.prefs.entities.Preference;
import org.nrg.prefs.services.PreferenceService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class FilterSecurityInterceptorBeanPostProcessor implements BeanPostProcessor {
    public void setOpenUrls(List<String> openUrls) {
        _openUrls.clear();
        _openUrls.addAll(openUrls);
    }

    public void setAdminUrls(List<String> adminUrls) {
        _adminUrls.clear();
        _adminUrls.addAll(adminUrls);
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String name) throws BeansException {
        if (_log.isDebugEnabled()) {
            _log.debug("Post-processing bean: " + name);
        }

        if (bean instanceof FilterSecurityInterceptor) {
            FilterSecurityInterceptor interceptor = (FilterSecurityInterceptor) bean;
            final ExpressionBasedFilterInvocationSecurityMetadataSource metadataSource = getMetadataSource(isRequiredLogin());
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

        for (final String openUrl : _openUrls) {
            if (_log.isDebugEnabled()) {
                _log.debug("Setting permitAll on the open URL: " + openUrl);
            }

            map.put(new AntPathRequestMatcher(openUrl), SecurityConfig.createList(PERMIT_ALL));
        }

        for (String adminUrl : _adminUrls) {
            if (_log.isDebugEnabled()) {
                _log.debug("Setting permissions on the admin URL: " + adminUrl);
            }

            map.put(new AntPathRequestMatcher(adminUrl), SecurityConfig.createList(ADMIN_EXPRESSION));
        }

        final String nonopen = requiredLogin ? DEFAULT_EXPRESSION : PERMIT_ALL;
        if (_log.isDebugEnabled()) {
            _log.debug("Setting " + nonopen + " on the default pattern: " + DEFAULT_PATTERN);
        }
        map.put(new AntPathRequestMatcher(DEFAULT_PATTERN), SecurityConfig.createList(nonopen));
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

    private boolean isRequiredLogin() {
        final Preference preference = _service.getPreference("system", "require_login", Scope.Site, null);
        if (preference == null) {
            _log.warn("There is no preference set for the require_login setting.");
            return true;
        }
        final String value = preference.getValue();
        if (StringUtils.isBlank(value)) {
            _log.warn("There is no preference value set for the require_login setting.");
            return true;
        }
        return Boolean.parseBoolean(value);
    }

    private static final Log _log = LogFactory.getLog(FilterSecurityInterceptorBeanPostProcessor.class);
    private static final String PERMIT_ALL = "permitAll";
    private static final String DEFAULT_PATTERN = "/**";
    private static final String ADMIN_EXPRESSION = "hasRole('ROLE_ADMIN')";
    private static final String DEFAULT_EXPRESSION = "hasRole('ROLE_USER')";

    @Inject
    private PreferenceService _service;

    private final List<String> _openUrls = new ArrayList<>();
    private final List<String> _adminUrls = new ArrayList<>();
}
