/*
 * web: org.nrg.xnat.utils.BrowserDetector
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class DefaultInteractiveAgentDetector implements InteractiveAgentDetector {
    /**
     * Sets the data paths, i.e. those paths which require a user-agent interactivity test to determine whether the user
     * should be denied as unauthorized or redirected to the login page. Each data path should be a valid Ant-style
     * pattern matching the URL(s) to be designated as data paths.
     *
     * @param preferences A list of strings in Ant-style patterns indicating data paths.
     */
    public DefaultInteractiveAgentDetector(final SiteConfigPreferences preferences) {
        for (final String interactiveAgent: preferences.getInteractiveAgentIds()) {
            log.debug("Adding interactive agent specifier: {}", interactiveAgent);
            final Pattern pattern = Pattern.compile(interactiveAgent);
            _interactiveAgentIds.add(pattern);
        }

        for (final String dataPath: preferences.getDataPaths()) {
            log.debug("Adding data path: {}", dataPath);
            _dataPaths.add(new AntPathRequestMatcher(dataPath));
        }
    }

    @Override
    public boolean isInteractiveAgent(final HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        final String userAgent = getUserAgent(request);
        log.debug("Testing user agent as interactive: {}", userAgent);

        if (StringUtils.isBlank(userAgent) || StringUtils.contains(userAgent, "XNATDesktopClient")) {
            return false;
        }

        for (final Pattern interactiveAgent: _interactiveAgentIds) {
            if (interactiveAgent.matcher(userAgent).matches()) {
                log.debug("User agent {} is interactive, matched simple regex pattern: {}", userAgent, interactiveAgent);
                return true;
            }
        }
        log.debug("User agent {} is not interactive, failed to match any patterns", userAgent);
        return false;
    }

    @Override
    public boolean isDataPath(final HttpServletRequest request) {
        log.debug("Testing URI as data path: {}", request.getContextPath());
        for (final RequestMatcher dataPath: _dataPaths) {
            if (dataPath.matches(request)) {
                log.debug("URI {} is a data path.", request.getContextPath());
                return true;
            }
        }
        log.debug("URI {} is not a data path.", request.getContextPath());
        return false;
    }

    private String getUserAgent(final HttpServletRequest request) {
        return request != null ? request.getHeader(UA_HEADER) : null;
    }

    private static final String UA_HEADER = "User-Agent";

    private final List<Pattern>        _interactiveAgentIds = new ArrayList<>();
    private final List<RequestMatcher> _dataPaths           = new ArrayList<>();
}
