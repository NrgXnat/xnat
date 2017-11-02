/*
 * web: org.nrg.xnat.event.listeners.methods.UpdateSecurityFilterHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.rest.aspects.XapiRequestMappingAspect;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.ConfigurableSecurityMetadataSourceFactory;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Handles changes to the {@link SiteConfigPreferences site configuration preferences} that affect the primary security filter. This
 * also doubles as the initial configuration processor for the security filter, replacing FilterSecurityInterceptorBeanPostProcessor.
 */
@Component
@Slf4j
public class UpdateSecurityFilterHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod implements BeanPostProcessor {
    @Autowired
    public UpdateSecurityFilterHandlerMethod(final XnatAppInfo appInfo, final ConfigurableSecurityMetadataSourceFactory factory) {
        _appInfo = appInfo;
        _factory = factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateSecurityFilter();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateSecurityFilter();
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
     *     <li>It sets the {@link ConfigurableSecurityMetadataSourceFactory security metadata source} on Spring's <b>FilterSecurityInterceptor</b></li>
     *     <li>
     *         It also sets the {@link XnatAppInfo#getOpenUrls() open (i.e. unrestricted)} and {@link XnatAppInfo#getAdminUrls() administrative URLs}
     *         on the {@link XapiRequestMappingAspect XAPI security manager object}.
     *     </li>
     * </ul>
     *
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
        if (log.isDebugEnabled()) {
            log.debug("Post-processing bean: " + name);
        }

        if (bean instanceof FilterSecurityInterceptor) {
            _interceptor = (FilterSecurityInterceptor) bean;
            updateSecurityFilter();
        } else if (bean instanceof XapiRequestMappingAspect) {
            final XapiRequestMappingAspect aspect = (XapiRequestMappingAspect) bean;
            aspect.setOpenUrls(_appInfo.getOpenUrls());
            aspect.setAdminUrls(_appInfo.getAdminUrls());
        }

        return bean;
    }

    private void updateSecurityFilter() {
        if (_interceptor != null) {
            final ExpressionBasedFilterInvocationSecurityMetadataSource metadataSource = _factory.getMetadataSource();
            if (log.isDebugEnabled()) {
                log.debug("Found a FilterSecurityInterceptor bean with the following metadata configuration:");
                displayMetadataSource(_interceptor.getSecurityMetadataSource());
                log.debug("Updating the bean with the following metadata configuration:");
                displayMetadataSource(metadataSource);
            }
            _interceptor.setSecurityMetadataSource(metadataSource);
        }
    }

    private void displayMetadataSource(final SecurityMetadataSource metadataSource) {
        if (metadataSource != null) {
            log.debug("Found metadata source configuration, now iterating.");
            for (final ConfigAttribute attribute : metadataSource.getAllConfigAttributes()) {
                log.debug("*** Attribute: " + attribute.getAttribute());
            }
        }
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Collections.singletonList("requireLogin"));

    private final XnatAppInfo                               _appInfo;
    private final ConfigurableSecurityMetadataSourceFactory _factory;

    private FilterSecurityInterceptor _interceptor;
}
