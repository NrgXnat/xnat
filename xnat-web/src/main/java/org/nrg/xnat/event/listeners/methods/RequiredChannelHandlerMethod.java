package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.security.TranslatingChannelProcessingFilter;
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
public class RequiredChannelHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
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
        if(PREFERENCES.contains(preference)){
            updateRequiredChannel();
        }
    }

    private void updateRequiredChannel(){
        _filter.setRequiredChannel(XDAT.getSiteConfigPreferences().getSecurityChannel());
	}

    private static final Logger       _log        = LoggerFactory.getLogger(RequiredChannelHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("security.channel"));

    @Autowired
    @Qualifier("channelProcessingFilter")
    private TranslatingChannelProcessingFilter _filter;
}
