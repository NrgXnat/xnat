package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.TranslatingChannelProcessingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class RequiredChannelHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public RequiredChannelHandlerMethod(final SiteConfigPreferences preferences, final TranslatingChannelProcessingFilter filter) {
        _preferences = preferences;
        _filter = filter;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateRequiredChannel();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateRequiredChannel();
        }
    }

    private void updateRequiredChannel() {
        _filter.setRequiredChannel(_preferences.getSecurityChannel());
    }

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Collections.singletonList("security.channel"));

    private final SiteConfigPreferences              _preferences;
    private final TranslatingChannelProcessingFilter _filter;
}
