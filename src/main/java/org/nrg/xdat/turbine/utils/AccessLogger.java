/*
 * org.nrg.xdat.turbine.utils.AccessLogger
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.turbine.utils;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.turbine.services.InstantiationException;
import org.apache.turbine.services.session.TurbineSession;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Tim
 *
 */
public class AccessLogger {
    private static final String  REQUEST_HISTORY   = "request_history";
    private static final Logger  logger            = Logger.getLogger(AccessLogger.class);
    private static       Boolean TRACKING_SESSIONS = null;

    private static Boolean isTrackingSessions() {
        if (TRACKING_SESSIONS == null) {
            try {
                final Collection<?> col = TurbineSession.getActiveSessions();
                TRACKING_SESSIONS = Boolean.FALSE;
                if (col.size() > 0) {
                    TRACKING_SESSIONS = Boolean.TRUE;
                }
            } catch (InstantiationException exception) {
                logger.info("Got InstantiationException, maybe it's too early to do this?");
            }
        }
        return TRACKING_SESSIONS;
    }

    public static String GetRequestIp(HttpServletRequest request){
    	@SuppressWarnings("unchecked")
		final Enumeration<String> headers = request.getHeaders("x-forwarded-for");

    	final String nullAddy = "0.0.0.0";
        if (headers == null) {
        	return nullAddy;
        } else {
            while (headers.hasMoreElements()) {
                final String[] ips = headers.nextElement().split(",");
                for (final String ip : ips) {
                    final String proxy = ip.trim();
                    if (!"unknown".equals(proxy) && !proxy.isEmpty()) {
                        try {
                            InetAddress proxyAddy = InetAddress.getByName(proxy);
                            if (proxyAddy != null) {
                                return proxyAddy.toString();
                            } else {
                                return nullAddy;
                            }
                        } catch (UnknownHostException e) {
                            logger.warn("ignoring host " + proxy + ": " + e.getClass().getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }

        return request.getRemoteAddr();
    }
    
    @SuppressWarnings("unchecked")
    public static void LogScreenAccess(RunData data) {
        if (!data.getScreen().equalsIgnoreCase("")) {
            final UserI user = XDAT.getUserDetails();
            final StringBuilder buffer = new StringBuilder(user != null ? user.getUsername() : "Guest");
            buffer.append(" ").append(GetRequestIp(data.getRequest())).append(" SCREEN: ").append(data.getScreen());

            if (TurbineUtils.HasPassedParameter("search_element", data)) {
                buffer.append(" ").append(TurbineUtils.GetPassedParameter("search_element", data));
            }
            if (TurbineUtils.HasPassedParameter("search_value", data)) {
                buffer.append(" ").append(TurbineUtils.GetPassedParameter("search_value", data));
            }
            logger.error(buffer.toString());
        }

        trackSession(data);
    }
    
    @SuppressWarnings("unchecked")
    public static void LogActionAccess(RunData data)
	{
        if (!data.getAction().equalsIgnoreCase(""))
        {
            final UserI  user = XDAT.getUserDetails();
            final StringBuilder buffer = new StringBuilder(user != null ? user.getUsername() : "Guest");
            buffer.append(" ").append(GetRequestIp(data.getRequest())).append(" ACTION: ").append(data.getAction());

		    if(TurbineUtils.HasPassedParameter("xdataction", data)){
		    	buffer.append(" ").append(TurbineUtils.GetPassedParameter("xdataction", data));
		    }
		    if(TurbineUtils.HasPassedParameter("search_element", data)){
                buffer.append(" ").append(TurbineUtils.GetPassedParameter("search_element", data));
		    }
		    if(TurbineUtils.HasPassedParameter("search_value", data)){
                buffer.append(" ").append(TurbineUtils.GetPassedParameter("search_value", data));
		    }

            logger.error(buffer.toString());
        }

        trackSession(data);
    }

    @SuppressWarnings("unchecked")
    public static void LogScreenAccess(RunData data, String message) {
        if (!data.getScreen().equalsIgnoreCase("")) {
            final UserI user = XDAT.getUserDetails();
            logger.error(((user != null) ? user.getUsername() : "Guest") + " " + GetRequestIp(data.getRequest()) + " SCREEN: " + data.getScreen() + " " + message);
        }

        trackSession(data);
    }

    @SuppressWarnings("unchecked")
    public static void LogActionAccess(RunData data, String message) {
        if (!data.getAction().equalsIgnoreCase("")) {
            final UserI user = XDAT.getUserDetails();
            logger.error(((user != null) ? user.getUsername() : "Guest") + " " + GetRequestIp(data.getRequest()) + " ACTION: " + data.getAction() + " " + message);
        }

        trackSession(data);
    }

    public static void LogServiceAccess(String user, String address, String service, String message) {
        logger.error(user + " " + address + " " + service + " " + message);

        final Boolean trackingSessions = isTrackingSessions();
        if (trackingSessions != null && trackingSessions) {
            try {
                MessageContext mc      = AxisEngine.getCurrentMessageContext();
                Session        session = mc.getSession();

                if (session.get(REQUEST_HISTORY) == null) {
                    session.set(REQUEST_HISTORY, new ArrayList<String>());
                }

                //noinspection unchecked
                ((List<String>) session.get(REQUEST_HISTORY)).add(service + " " + message);
            } catch (Throwable e) {
                logger.error("", e);
            }
        }
    }

    public static String getAccessLogDirectory() {
        final Enumeration<?> appenders = logger.getAllAppenders();
        while (appenders.hasMoreElements()) {
            final Appender appender = (Appender) appenders.nextElement();
            if (appender instanceof FileAppender) {
                final String location = ((FileAppender) appender).getFile();
                if (StringUtils.isNotBlank(location)) {
                    return FileUtils.AppendSlash(new File(location).getParentFile().getAbsolutePath());
                }
            }
        }
        return null;
    }

    private static void trackSession(final RunData data) {
        final Boolean trackingSessions = isTrackingSessions();
        if (trackingSessions != null && trackingSessions) {
            try {
                if (data.getSession().getAttribute(REQUEST_HISTORY) == null) {
                    data.getSession().setAttribute(REQUEST_HISTORY, new ArrayList<String>());
                }

                //noinspection unchecked
                ((List<String>) data.getSession().getAttribute(REQUEST_HISTORY)).add(data.getRequest().getRequestURI());
            } catch (Throwable e) {
                logger.error("", e);
            }
        }
    }
}
