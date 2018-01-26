/*
 * web: org.nrg.xnat.event.listeners.methods.UpdateSecurityFilterHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xnat.security.FilterSecurityInterceptorBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateSecurityFilterHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public UpdateSecurityFilterHandlerMethod(final FilterSecurityInterceptor interceptor, final FilterSecurityInterceptorBeanPostProcessor postProcessor) {
        super("requireLogin");
        _interceptor = interceptor;
        _postProcessor = postProcessor;
        if (_interceptor == null) {
            log.warn("Filter security interceptor is null initializing, changes to requireLogin setting won't propagate");
        }
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        if (StringUtils.equals("requireLogin", preference) && _interceptor != null) {
            _interceptor.setSecurityMetadataSource(_postProcessor.getMetadataSource(Boolean.parseBoolean(value)));
        }
    }

    private final FilterSecurityInterceptor                  _interceptor;
    private final FilterSecurityInterceptorBeanPostProcessor _postProcessor;
}
