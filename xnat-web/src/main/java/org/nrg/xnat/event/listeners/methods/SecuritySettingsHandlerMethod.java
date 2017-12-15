/*
 * web: org.nrg.xnat.event.listeners.methods.RequiredChannelHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.security.XnatLogoutSuccessHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.config.http.ChannelAttributeFactory;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class SecuritySettingsHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod implements ApplicationContextAware {
    @Autowired
    public SecuritySettingsHandlerMethod(final XnatLogoutSuccessHandler logoutSuccessHandler) {
        _logoutSuccessHandler = logoutSuccessHandler;
    }

    @Autowired
    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        _context = context;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            for (final String preference : values.keySet()) {
                handlePreference(preference, values.get(preference));
            }
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        switch (preference) {
            case "securityChannel":
                updateRequiredChannel(value);
                break;

            case "requireLogin":
                updateRequireLogin(value);
                break;

            default:
                log.info("Preference not handled by this method: {}", preference);
        }
    }

    private void updateRequiredChannel(final String requiredChannel) {
        final ChannelProcessingFilter filter = _context.getBean(ChannelProcessingFilter.class);
        log.debug("Setting the default pattern required channel to: {}", requiredChannel);
        final LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<>();
        map.put(new AntPathRequestMatcher("/**"), ChannelAttributeFactory.createChannelAttributes(requiredChannel));
        final FilterInvocationSecurityMetadataSource metadataSource = new DefaultFilterInvocationSecurityMetadataSource(map);
        filter.setSecurityMetadataSource(metadataSource);
    }

    private void updateRequireLogin(final String value) {
        _logoutSuccessHandler.setRequireLogin(Boolean.parseBoolean(value));
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("securityChannel", "requireLogin"));

    private final XnatLogoutSuccessHandler _logoutSuccessHandler;

    private ApplicationContext _context;
}
