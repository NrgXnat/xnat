/*
 * web: org.nrg.xnat.security.XnatSessionEventPublisher
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static org.nrg.framework.orm.DatabaseHelper.convertPGIntervalToIntSeconds;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.web.context.support.WebApplicationContextUtils.findWebApplicationContext;

@Component
@Getter(AccessLevel.PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class XnatSessionEventPublisher implements HttpSessionListener, ServletContextListener {
    /**
     * Handles the HttpSessionEvent by publishing a {@link HttpSessionCreatedEvent} to the application
     * appContext.
     *
     * @param event HttpSessionEvent passed in by the container
     */
    @Override
    public void sessionCreated(final HttpSessionEvent event) {
        final HttpSession             session             = event.getSession();
        final HttpSessionCreatedEvent sessionCreatedEvent = new HttpSessionCreatedEvent(session);

        log.debug("Publishing event: {}", sessionCreatedEvent);

        session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
        session.setMaxInactiveInterval(getSessionTimeout()); // Preference is in PG Interval and setMaxInactiveInterval wants seconds.
        findWebApplicationContext(session.getServletContext()).publishEvent(sessionCreatedEvent);
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

        try {
            final Object contextCandidate = event.getSession().getAttribute(SPRING_SECURITY_CONTEXT_KEY);
            if (contextCandidate instanceof SecurityContext) {
                final SecurityContext context        = (SecurityContext) contextCandidate;
                final Authentication  authentication = context.getAuthentication();
                if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                    final Object userCandidate = authentication.getPrincipal();
                    if (userCandidate instanceof UserI) {
                        final Integer userId = ((UserI) userCandidate).getID();
                        if (userId == null) {
                            log.info("Got a session destroyed event for an empty user ID");
                        } else if (ObjectUtils.compare(getGuestUserId(), userId) == 0) {
                            log.debug("Got a session destroyed event for the guest user");
                        } else{
                            //sessionId's aren't guaranteed to be unique forever. But, the likelihood of sessionId and userId not forming a unique combo with a null logout_date is slim.
                            final int count = getTemplate().update(UPDATE_QUERY, new MapSqlParameterSource(PARAM_SESSION_ID, sessionId).addValue(PARAM_TIMESTAMP, new Timestamp(Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis())).addValue(PARAM_USER_ID, userId));
                            log.debug("Got a session destroyed event for user ID {}, updated {} rows in xdat_user_login to record this.", userId, count);
                        }
                    } else if (userCandidate instanceof String) {
                        log.info("Got a session destroyed event for string principal: {}", userCandidate);
                    }
                }
            }
        } catch (Exception e) {
            //remember, anonymous gets a session, too. Those won't be in the table. Fail silently.
        }

        final HttpSessionDestroyedEvent sessionDestroyedEvent = new HttpSessionDestroyedEvent(event.getSession());
        log.debug("Publishing event: {}", sessionDestroyedEvent);

        findWebApplicationContext(event.getSession().getServletContext()).publishEvent(sessionDestroyedEvent);
    }

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        log.debug("Servlet context initialized: {}", event.getServletContext().getContextPath());
        _preferences = WebApplicationContextUtils.getRequiredWebApplicationContext(event.getServletContext()).getBean(SiteConfigPreferences.class);
        _template = WebApplicationContextUtils.getRequiredWebApplicationContext(event.getServletContext()).getBean(NamedParameterJdbcTemplate.class);
        _cache = WebApplicationContextUtils.getRequiredWebApplicationContext(event.getServletContext()).getBean(GroupsAndPermissionsCache.class);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        log.debug("Servlet context destroyed: {}", event.getServletContext().getContextPath());

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        log.info("Preparing to de-register JDBC drivers for this application.");
        final Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            final Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == classLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                    log.info("De-registering JDBC driver '{}'", driver);
                } catch (SQLException e) {
                    log.error("Error de-registering JDBC driver '{}'", driver, e);
                }
            } else {
                // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                log.info("Not de-registering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
            }
        }
    }

    /**
     * In some weird circumstances, mainly when the server is still starting up or is shutting down, if a user attempts to connect to the server the preferences
     * object may be null. This provides a default timeout value of 900 seconds (15 minutes) in those cases to prevent NPEs.
     *
     * @return The configured session timeout value in seconds or a default value if the configured value isn't available.
     */
    private int getSessionTimeout() {
        return _preferences != null && StringUtils.isNotBlank(_preferences.getSessionTimeout()) ? convertPGIntervalToIntSeconds(_preferences.getSessionTimeout()) : 900;
    }

    private Integer getGuestUserId() {
        if (_guestUserId == null) {
            try {
                _guestUserId = Users.getGuest().getID();
            } catch (UserNotFoundException | UserInitException e) {
                log.warn("An error occurred retrieving the guest user", e);
            }
        }
        return _guestUserId;
    }

    private static final String UPDATE_QUERY     = "UPDATE xdat_user_login SET logout_date = :timestamp WHERE logout_date IS NULL AND session_id = :sessionId AND user_xdat_user_id = :userId";
    private static final String PARAM_SESSION_ID = "sessionId";
    private static final String PARAM_TIMESTAMP  = "timestamp";
    private static final String PARAM_USER_ID    = "userId";

    private SiteConfigPreferences      _preferences;
    private NamedParameterJdbcTemplate _template;
    private GroupsAndPermissionsCache  _cache;
    private Integer                    _guestUserId;
}
