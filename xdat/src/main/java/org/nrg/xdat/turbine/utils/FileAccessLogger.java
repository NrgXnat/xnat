package org.nrg.xdat.turbine.utils;

import com.noelios.restlet.ext.servlet.ServletCall;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Request;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class FileAccessLogger extends AccessLogger{

    public static void LogFileResourceAccess(final String user, final HttpServletRequest request, final String service, final String message) {
        logFileAccess(user, request, service, message);
    }

    public static void LogFileResourceAccess(final String user, final Request request, final String service, final String message) {
        logFileAccess(user, ServletCall.getRequest(request), service, message);
    }

    private static void logFileAccess(final String username, final HttpServletRequest request, final String target, final String payload) {
        log.info("{} {} {} {} \"{}\" {}", username, GetRequestIp(request), request.getMethod(), target, getUserAgentHeader(request), StringUtils.defaultIfBlank(payload, ""));
    }
}
