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
import org.nrg.xnat.security.XnatLogoutSuccessHandler;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.SecurityMetadataSource;
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

import java.util.*;

/**
 * Handles changes to the {@link SiteConfigPreferences site configuration preferences} that affect the primary security filter. This
 * also doubles as the initial configuration processor for the security filter, replacing FilterSecurityInterceptorBeanPostProcessor.
 */
@Component
@Slf4j
public class UpdateSecurityFilterHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod implements BeanPostProcessor, ApplicationContextAware {
    @Autowired
    public UpdateSecurityFilterHandlerMethod(final SiteConfigPreferences preferences, final XnatAppInfo appInfo, final XnatLogoutSuccessHandler logoutSuccessHandler) {
        _openUrls = appInfo.getOpenUrls();
        _adminUrls = appInfo.getAdminUrls();
        _logoutSuccessHandler = logoutSuccessHandler;
        _requireLogin = preferences.getRequireLogin();
        _securityChannel = preferences.getSecurityChannel();
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
        final Set<String> preferences = values.keySet();
        if (!Collections.disjoint(PREFERENCES, preferences)) {
            for (final String preference : preferences) {
                handlePreference(preference, values.get(preference));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePreference(final String preference, final String value) {
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
     * <p>
     * <ul>
     * <li>It sets the {@link ConfigurableSecurityMetadataSourceFactory security metadata source} on Spring's <b>FilterSecurityInterceptor</b></li>
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
        if (log.isDebugEnabled()) {
            log.debug("Post-processing bean: " + name);
        }

        if (bean instanceof FilterSecurityInterceptor) {
            _interceptor = (FilterSecurityInterceptor) bean;
            updateSecurityFilter();
        } else if (bean instanceof XapiRequestMappingAspect) {
            final XapiRequestMappingAspect aspect = (XapiRequestMappingAspect) bean;
            aspect.setOpenUrls(_openUrls);
            aspect.setAdminUrls(_adminUrls);
        } else if (bean instanceof ChannelProcessingFilter) {

        }

        return bean;
    }

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        _context = context;
    }

    private void updateSecurityFilter() {
        if (_interceptor != null) {
            final ExpressionBasedFilterInvocationSecurityMetadataSource metadataSource = getMetadataSource();
            if (log.isDebugEnabled()) {
                log.debug("Found a FilterSecurityInterceptor bean with the following metadata configuration:\n{}", displayMetadataSource(_interceptor.getSecurityMetadataSource()));
                log.debug("Updating the bean with the following metadata configuration:\n{}", displayMetadataSource(metadataSource));
            }
            _interceptor.setSecurityMetadataSource(metadataSource);
        }
    }

    private void updateRequireLogin() {
        _logoutSuccessHandler.setRequireLogin(_requireLogin);
        _interceptor.setSecurityMetadataSource(getMetadataSource());
    }

    private void updateSecurityChannel() {
        final ChannelProcessingFilter filter = _context.getBean(ChannelProcessingFilter.class);
        log.debug("Setting the default pattern required channel to: {}", _securityChannel);
        final LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<>();
        map.put(new AntPathRequestMatcher("/**"), ChannelAttributeFactory.createChannelAttributes(_securityChannel));
        final FilterInvocationSecurityMetadataSource metadataSource = new DefaultFilterInvocationSecurityMetadataSource(map);
        filter.setSecurityMetadataSource(metadataSource);
    }

    private ExpressionBasedFilterInvocationSecurityMetadataSource getMetadataSource() {
        final LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<>();

        for (final String url : _openUrls) {
            if (log.isDebugEnabled()) {
                log.debug("Setting permitAll on the open URL: " + url);
            }
            map.put(new AntPathRequestMatcher(url), SecurityConfig.createList(PERMIT_ALL));
        }

        for (final String adminUrl : _adminUrls) {
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

        final String secure = _requireLogin ? DEFAULT_EXPRESSION : PERMIT_ALL;
        if (log.isDebugEnabled()) {
            log.debug("Setting " + secure + " on the default pattern: " + DEFAULT_PATTERN);
        }
        map.put(new AntPathRequestMatcher(DEFAULT_PATTERN), SecurityConfig.createList(secure));
        return new ExpressionBasedFilterInvocationSecurityMetadataSource(map, new DefaultWebSecurityExpressionHandler());
    }

    private static String displayMetadataSource(final SecurityMetadataSource metadataSource) {
        if (metadataSource == null) {
            return "*** No metadata source found";
        }

        final StringBuilder builder = new StringBuilder();
        for (final ConfigAttribute attribute : metadataSource.getAllConfigAttributes()) {
            builder.append("*** Attribute: ").append(attribute.getAttribute());
        }
        return builder.toString();
    }

    private static final String       PERMIT_ALL         = "permitAll";
    private static final String       DEFAULT_PATTERN    = "/**";
    private static final String       ADMIN_EXPRESSION   = "hasRole('ROLE_ADMIN')";
    private static final String       DEFAULT_EXPRESSION = "hasRole('ROLE_USER')";
    private static final String       SECURITY_CHANNEL   = "securityChannel";
    private static final String       REQUIRE_LOGIN      = "requireLogin";
    private static final List<String> PREFERENCES        = ImmutableList.copyOf(Arrays.asList(SECURITY_CHANNEL, REQUIRE_LOGIN));

    private final XnatLogoutSuccessHandler _logoutSuccessHandler;
    private final List<String>             _openUrls;
    private final List<String>             _adminUrls;

    private FilterSecurityInterceptor _interceptor;
    private ApplicationContext        _context;

    private boolean _requireLogin;
    private String  _securityChannel;
}
