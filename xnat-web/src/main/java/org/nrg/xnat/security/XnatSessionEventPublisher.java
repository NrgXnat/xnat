/*
 * web: org.nrg.xnat.security.XnatSessionEventPublisher
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class XnatSessionEventPublisher implements HttpSessionListener, ServletContextListener {
    /**
     * Handles the HttpSessionEvent by publishing a {@link HttpSessionCreatedEvent} to the application
     * appContext.
     *
     * @param event HttpSessionEvent passed in by the container
     */
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession             session = event.getSession();
        HttpSessionCreatedEvent e       = new HttpSessionCreatedEvent(session);

        if (_log.isDebugEnabled()) {
            _log.debug("Publishing event: " + e);
        }

        session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
        try {
            session.setMaxInactiveInterval((new Long(SiteConfigPreferences.convertPGIntervalToSeconds(XDAT.getSiteConfigPreferences().getSessionTimeout()))).intValue());//Preference is in PG Interval and setMaxInactiveInterval wants seconds.
        } catch (SQLException e1) {
            _log.error("" + e);
        }
        getContext(session.getServletContext()).publishEvent(e);
    }

    /**
     * Handles the HttpSessionEvent by publishing a {@link HttpSessionDestroyedEvent} to the application
     * appContext.
     *
     * @param event The HttpSessionEvent pass in by the container
     */
    @Override
    public void sessionDestroyed(final HttpSessionEvent event) {
        final String sessionId = event.getSession().getId();
        final Date   today     = Calendar.getInstance(TimeZone.getDefault()).getTime();

        try {
            final Object contextCandidate = event.getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            if (contextCandidate != null && contextCandidate instanceof SecurityContext) {
                final SecurityContext context = (SecurityContext) contextCandidate;
                final Authentication authentication = context.getAuthentication();
                if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                    final Object userCandidate = authentication.getPrincipal();
                    if (userCandidate != null && userCandidate instanceof UserI) {
                        final String userId = ((UserI) userCandidate).getID().toString();
                        final Timestamp stamp = new Timestamp(today.getTime());
                        //sessionId's aren't guaranteed to be unique forever. But, the likelihood of sessionId and userId not forming a unique combo with a null logout_date is slim.
                        //noinspection SqlDialectInspection,SqlNoDataSourceInspection,SqlResolve
                        _template.execute("UPDATE xdat_user_login SET logout_date='" + stamp + "' WHERE logout_date is null and session_id='" + sessionId + "' and user_xdat_user_id='" + userId + "';");
                    }
                }
            }
        } catch (Exception e) {
            //remember, anonymous gets a session, too. Those won't be in the table. Fail silently.
        }
        HttpSessionDestroyedEvent e = new HttpSessionDestroyedEvent(event.getSession());
        if (_log.isDebugEnabled()) {
            _log.debug("Publishing event: " + e);
        }
        getContext(event.getSession().getServletContext()).publishEvent(e);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        if (_log.isDebugEnabled()) {
            final ServletContext context   = event.getServletContext();
            _log.debug("Context destroyed: {}", context.getContextPath());
        }
    }

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        if (_log.isDebugEnabled()) {
            final ServletContext context   = event.getServletContext();
            _log.debug("Context initialized: {}", context.getContextPath());
        }
    }

    @Autowired
    public void setJdbcTemplate(final JdbcTemplate template) {
        _template = template;
    }

    private ApplicationContext getContext(ServletContext servletContext) {
        return WebApplicationContextUtils.findWebApplicationContext(servletContext);  // contextAttribute in xnat's case will always be "org.springframework.web.servlet.FrameworkServlet.CONTEXT.spring-mvc");
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatSessionEventPublisher.class);

    private JdbcTemplate _template;
}
