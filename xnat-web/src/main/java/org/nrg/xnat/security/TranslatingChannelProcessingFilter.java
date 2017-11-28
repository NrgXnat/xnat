/*
 * web: org.nrg.xnat.security.TranslatingChannelProcessingFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.config.http.ChannelAttributeFactory;
import org.springframework.security.web.access.channel.ChannelDecisionManagerImpl;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Collection;
import java.util.LinkedHashMap;

@Slf4j
public class TranslatingChannelProcessingFilter extends ChannelProcessingFilter {
    public TranslatingChannelProcessingFilter(final ChannelDecisionManagerImpl decisionManager, final String securityChannel) {
        setChannelDecisionManager(decisionManager);
        setRequiredChannel(securityChannel);
    }

    public void setRequiredChannel(String requiredChannel) {
        log.debug("Setting the default pattern required channel to: {}", requiredChannel);

        final LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<>();
        map.put(new AntPathRequestMatcher("/**"), ChannelAttributeFactory.createChannelAttributes(requiredChannel));
        final FilterInvocationSecurityMetadataSource metadataSource = new DefaultFilterInvocationSecurityMetadataSource(map);
        setSecurityMetadataSource(metadataSource);
    }
}
