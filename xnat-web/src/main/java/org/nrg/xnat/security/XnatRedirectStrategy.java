package org.nrg.xnat.security;

import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.utils.InteractiveAgentDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

@Component
public class XnatRedirectStrategy extends DefaultRedirectStrategy {
    @Autowired
    public XnatRedirectStrategy(final SiteConfigPreferences preferences, final InteractiveAgentDetector detector) {
        _detector = detector;
        _realm = "Basic realm=\"" + preferences.getSiteId() + "\"";
    }

    @Override
    public void sendRedirect(final HttpServletRequest request, final HttpServletResponse response, final String url) throws IOException {
        if (_detector.isDataPath(request) && !_detector.isInteractiveAgent(request)) {
            response.setHeader(HEADER_WWW_AUTHENTICATE, _realm);
            response.sendError(SC_UNAUTHORIZED);
        } else {
            super.sendRedirect(request, response, url);
        }
    }

    public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

    private final String                   _realm;
    private final InteractiveAgentDetector _detector;
}
