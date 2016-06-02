package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.security.FilterSecurityInterceptorBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class UpdateSecurityFilterHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
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
            _interceptor.setSecurityMetadataSource(_postProcessor.getMetadataSource(XDAT.getSiteConfigPreferences().getRequireLogin()));
        }
	}

    private static final Logger       _log        = LoggerFactory.getLogger(UpdateSecurityFilterHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("requireLogin"));

    @Inject
    private FilterSecurityInterceptor _interceptor;

    @Inject
    private FilterSecurityInterceptorBeanPostProcessor _postProcessor;

}
