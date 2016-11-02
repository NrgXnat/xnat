/*
 * web: org.nrg.xnat.event.listeners.methods.ReceivedFileUserPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xnat.utils.XnatUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class XnatUserProviderPreferenceHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public XnatUserProviderPreferenceHandlerMethod(final List<XnatUserProvider> providers) {
        for (final XnatUserProvider provider : providers) {
            _providers.put(provider.getUserKey(), provider);
        }
    }

    @Override
    public List<String> getHandledPreferences() {
        return new ArrayList<>(_providers.keySet());
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        for (final String key : values.keySet()) {
            handlePreference(key, values.get(key));
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (_providers.containsKey(preference)) {
            final XnatUserProvider provider = _providers.get(preference);
            if (!StringUtils.equals(value, provider.getLogin())) {
                provider.setLogin(value);
            }
        }
    }

    private final Map<String, XnatUserProvider> _providers = Maps.newHashMap();
}
