package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.security.FilterSecurityInterceptorBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.stereotype.Component;

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
        FilterSecurityInterceptor interceptor = XDAT.getContextService().getBean(FilterSecurityInterceptor.class);
        FilterSecurityInterceptorBeanPostProcessor postProcessor = XDAT.getContextService().getBean(FilterSecurityInterceptorBeanPostProcessor.class);
        if(interceptor!=null && postProcessor!=null){
            interceptor.setSecurityMetadataSource(postProcessor.getMetadataSource(XDAT.getSiteConfigPreferences().getRequireLogin()));
        }
	}

    private static final Logger       _log        = LoggerFactory.getLogger(UpdateSecurityFilterHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("requireLogin"));

    @Autowired
    @Lazy
    private JdbcTemplate _template;
}
