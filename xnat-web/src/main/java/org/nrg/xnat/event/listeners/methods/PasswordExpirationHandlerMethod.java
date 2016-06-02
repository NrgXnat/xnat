package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xnat.security.XnatExpiredPasswordFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PasswordExpirationHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updatePasswordExpiration();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updatePasswordExpiration();
        }
    }

    private void updatePasswordExpiration() {
        _filter.refreshFromSiteConfig();
    }

    private static final Logger       _log        = LoggerFactory.getLogger(PasswordExpirationHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("passwordExpirationType", "passwordExpirationInterval", "passwordExpirationDate"));

    @Autowired
    @Qualifier("expiredPasswordFilter")
    private XnatExpiredPasswordFilter _filter;
}
