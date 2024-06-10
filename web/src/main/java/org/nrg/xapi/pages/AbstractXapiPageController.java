package org.nrg.xapi.pages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public abstract class AbstractXapiPageController {
    protected String getWildCardPath(final HttpServletRequest request) {
        final String path             = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        final String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        log.info("Trying to get a match for the wild-card path '{}' with the pattern '{}'", path, bestMatchPattern);
        return _pathMatcher.extractPathWithinPattern(bestMatchPattern, path);
    }

    final AntPathMatcher _pathMatcher = new AntPathMatcher();
}
