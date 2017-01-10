/*
 * web: org.nrg.xnat.event.listeners.methods.PasswordExpirationHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xnat.security.XnatExpiredPasswordFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PasswordExpirationHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public PasswordExpirationHandlerMethod(final XnatExpiredPasswordFilter filter) {
        _filter = filter;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            _log.debug("Found updated password expiration preferences, updating expired password filter");
            updatePasswordExpiration();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            _log.debug("Found updated password expiration preference {}, updating expired password filter", preference);
            updatePasswordExpiration();
        }
    }

    private void updatePasswordExpiration() {
        _filter.refreshFromSiteConfig();
    }

    private static final Logger       _log        = LoggerFactory.getLogger(PasswordExpirationHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("passwordExpirationType", "passwordExpirationInterval", "passwordExpirationDate"));

    private final XnatExpiredPasswordFilter _filter;
}
