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
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.security.FilterSecurityInterceptorBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class UpdateSecurityFilterHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public UpdateSecurityFilterHandlerMethod(final SiteConfigPreferences preferences, @SuppressWarnings("SpringJavaAutowiringInspection") final FilterSecurityInterceptor interceptor, final FilterSecurityInterceptorBeanPostProcessor postProcessor) {
        _preferences = preferences;
        _interceptor = interceptor;
        _postProcessor = postProcessor;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateSecurityFilter();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if(PREFERENCES.contains(preference)){
            updateSecurityFilter();
        }
    }

    private void updateSecurityFilter(){
        if(_interceptor!=null && _postProcessor!=null){
            _interceptor.setSecurityMetadataSource(_postProcessor.getMetadataSource(_preferences.getRequireLogin()));
        }
	}

    private static final List<String> PREFERENCES = ImmutableList.copyOf(Collections.singletonList("requireLogin"));

    private final SiteConfigPreferences _preferences;
    private final FilterSecurityInterceptor _interceptor;
    private final FilterSecurityInterceptorBeanPostProcessor _postProcessor;

}
