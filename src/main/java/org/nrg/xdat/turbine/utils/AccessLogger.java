/*
 * core: org.nrg.xdat.turbine.utils.AccessLogger
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.utils;

import com.noelios.restlet.ext.servlet.ServletCall;
import lombok.extern.slf4j.Slf4j;
import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.services.InstantiationException;
import org.apache.turbine.services.session.TurbineSession;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.python.antlr.ast.arguments;
import org.restlet.data.Request;
import org.slf4j.helpers.MessageFormatter;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.springframework.http.HttpHeaders.USER_AGENT;

/**
 * @author Tim
 */
@Slf4j
public class AccessLogger {
    public static String GetRequestIp(final HttpServletRequest request) {
        if (request == null) {
            return NULL_ADDRESS;
        }

        final Enumeration<String> headers = request.getHeaders("x-forwarded-for");
        if (headers == null) {
            return NULL_ADDRESS;
        }

        while (headers.hasMoreElements()) {
            final String[] ips = headers.nextElement().split("\\s*,\\s*");
            for (final String ip : ips) {
                if (StringUtils.isNotBlank(ip) && !StringUtils.equalsIgnoreCase("unknown", ip)) {
                    try {
                        final InetAddress proxyAddy = InetAddress.getByName(ip);
                        return proxyAddy != null ? proxyAddy.getHostAddress() : NULL_ADDRESS;
                    } catch (UnknownHostException e) {
                        log.warn("Ignoring unknown host {}: {}", ip, e.getMessage());
                    }
                }
            }
        }

        return request.getRemoteAddr();
    }

    public static void LogScreenAccess(final RunData data) {
        if (StringUtils.isNotBlank(data.getScreen())) {
            logAccess(data, true);
        }
        trackSession(data);
    }

    public static void LogActionAccess(final RunData data) {
        if (StringUtils.isNotBlank(data.getAction())) {
            logAccess(data, false);
        }
        trackSession(data);
    }

    public static void LogScreenAccess(final RunData data, final String message) {
        if (StringUtils.isNotBlank(data.getScreen())) {
            logAccess(data, true, message);
        }
        trackSession(data);
    }

    public static void LogActionAccess(final RunData data, final String message) {
        if (StringUtils.isNotBlank(data.getAction())) {
            logAccess(data, false, message);
        }
        trackSession(data);
    }

    public static void LogResourceAccess(final String user, final Request request, final String service, final String message) {
        logAccess(user, ServletCall.getRequest(request), service, message);
    }

    public static void LogResourceAccess(final String user, final HttpServletRequest request, final String service) {
        logAccess(user, request, service, null);
    }

    public static void LogResourceAccess(final String user, final HttpServletRequest request, final String service, final String message) {
        logAccess(user, request, service, message);
    }

    public static void LogServiceAccess(final String user, final MessageContext context, final String service, final String message) {
        LogServiceAccess(user, getAxisRequest(context), service, message);
    }

    public static void LogAjaxServiceAccess(final String user, final HttpServletRequest request) {
        LogServiceAccess(user, request, getFullRequestUrl(request), null);
    }

    public static void LogAjaxServiceAccess(final String user, final HttpServletRequest request, final String message, final String... arguments) {
        LogServiceAccess(user, request, getFullRequestUrl(request), arguments.length == 0 ? message : MessageFormatter.arrayFormat(message, arguments).getMessage());
    }

    public static void LogServiceAccess(final String user, final HttpServletRequest request, final String service, final String message) {
        logAccess(user, request, service, message);

        if (isTrackingSessions()) {
            try {
                final Session      session = AxisEngine.getCurrentMessageContext().getSession();
                final List<String> history;
                if (session.get(REQUEST_HISTORY) == null) {
                    history = new ArrayList<>();
                    session.set(REQUEST_HISTORY, history);
                } else {
                    //noinspection unchecked
                    history = (List<String>) session.get(REQUEST_HISTORY);
                }
                history.add(service + " " + message);
            } catch (Throwable e) {
                log.error("An error occurred trying to record request history for user \"{}\", attempted request was: {}", user, service + " " + message, e);
            }
        }
    }

    public static String getFullRequestUrl(final HttpServletRequest request) {
        final String querystring = request.getQueryString();
        if (StringUtils.isBlank(querystring)) {
            return request.getRequestURL().toString();
        }
        return request.getRequestURL().append("?").append(querystring).toString();
    }

    private static void logAccess(final RunData data, final boolean isScreen) {
        logAccess(data, isScreen, null);
    }

    private static void logAccess(final RunData data, final boolean isScreen, final String message) {
        final UserI user = XDAT.getUserDetails();

        final List<String> parameters = new ArrayList<>();
        if (TurbineUtils.HasPassedParameter("xdataction", data)) {
            parameters.add("xdataction=" + TurbineUtils.GetPassedParameter("xdataction", data));
        }
        if (TurbineUtils.HasPassedParameter("search_element", data)) {
            parameters.add("search_element=" + TurbineUtils.GetPassedParameter("search_element", data));
        }
        if (TurbineUtils.HasPassedParameter("search_value", data)) {
            parameters.add("search_value=" + TurbineUtils.GetPassedParameter("search_value", data));
        }
        logAccess(user != null ? user.getUsername() : "Guest", data.getRequest(), (isScreen ? "SCREEN" : "ACTION") + ": " + (isScreen ? data.getScreen() : data.getAction()), (parameters.isEmpty() ? "" : StringUtils.join(parameters, " ")) + StringUtils.defaultIfBlank(message, ""));
    }

    private static void logAccess(final String username, final HttpServletRequest request, final String target, final String payload) {
        log.info("{} {} {} {} \"{}\" {}", username, GetRequestIp(request), request.getMethod(), target, getUserAgentHeader(request), StringUtils.defaultIfBlank(payload, ""));
    }

    private static void trackSession(final RunData data) {
        if (isTrackingSessions()) {
            try {
                if (data.getSession().getAttribute(REQUEST_HISTORY) == null) {
                    data.getSession().setAttribute(REQUEST_HISTORY, new ArrayList<String>());
                }

                //noinspection unchecked
                ((List<String>) data.getSession().getAttribute(REQUEST_HISTORY)).add(data.getRequest().getRequestURI());
            } catch (Throwable e) {
                log.error("", e);
            }
        }
    }

    private static Boolean isTrackingSessions() {
        if (TRACKING_SESSIONS == null) {
            try {
                TRACKING_SESSIONS = !TurbineSession.getActiveSessions().isEmpty();
            } catch (InstantiationException exception) {
                log.info("Got instantiation exception trying to track active sessions. Maybe it's too early to do this?");
            }
        }
        return TRACKING_SESSIONS != null && TRACKING_SESSIONS;
    }

    private static String getUserAgentHeader(final HttpServletRequest request) {
        return request == null ? "<null request>" : request.getHeader(USER_AGENT);
    }

    private static HttpServletRequest getAxisRequest(final MessageContext context) {
        return (HttpServletRequest) context.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
    }

    private static final String  NULL_ADDRESS      = "0.0.0.0";
    private static final String  REQUEST_HISTORY   = "request_history";
    private static       Boolean TRACKING_SESSIONS = null;
}
