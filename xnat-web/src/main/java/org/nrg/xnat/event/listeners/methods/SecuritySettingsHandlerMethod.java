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
import org.nrg.xnat.security.TranslatingChannelProcessingFilter;
import org.nrg.xnat.security.XnatLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SecuritySettingsHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public SecuritySettingsHandlerMethod(final TranslatingChannelProcessingFilter filter, final XnatLogoutSuccessHandler logoutSuccessHandler) {
        _filter = filter;
        _logoutSuccessHandler = logoutSuccessHandler;
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

    private void updateRequiredChannel(final String value) {
        _filter.setRequiredChannel(value);
    }

    private void updateRequireLogin(final String value) {
        _logoutSuccessHandler.setRequireLogin(Boolean.parseBoolean(value));
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("securityChannel", "requireLogin"));

    private final TranslatingChannelProcessingFilter _filter;
    private final XnatLogoutSuccessHandler           _logoutSuccessHandler;
}
