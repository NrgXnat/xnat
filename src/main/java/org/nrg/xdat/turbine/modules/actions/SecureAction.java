/*
 * core: org.nrg.xdat.turbine.modules.actions.SecureAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.BrowserType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

import javax.annotation.Nonnull;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Enumeration;

import static org.springframework.http.HttpHeaders.USER_AGENT;

/**
 * @author Tim
 */
@Slf4j
public abstract class SecureAction extends VelocitySecureAction {
    @Nonnull
    protected UserI getUser() {
        try {
            return ObjectUtils.defaultIfNull(XDAT.getUserDetails(), Users.getGuest());
        } catch (UserNotFoundException | UserInitException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.UserServiceError, "An error occurred retrieving the user principal.", e);
        }
    }

    protected void preserveVariables(final RunData data, final Context context) {
        if (data.getParameters().containsKey("project")) {
            final String project = (String) TurbineUtils.GetPassedParameter("project", data);
            log.debug("{}: maintaining project '{}'", getClass(), project);
            context.put("project", TurbineUtils.escapeParam(project));
        }
    }

    protected void error(final Throwable e, final RunData data) {
        log.error("An error occurred", e);
        if (e instanceof InvalidPermissionException) {
            try {
                AdminUtils.sendAdminEmail(XDAT.getUserDetails(), "Possible Authorization Bypass Attempt", "User attempted to access or modify protected content at action: " + data.getAction() + "; " + e.getMessage());
                data.getResponse().sendError(403);
            } catch (IOException ignored) {
            }
        }
        data.setScreenTemplate("Error.vm");
        data.getParameters().setString("exception", e.toString());
    }

    final static String encoding = "ISO-8859-1";

    public void redirectToReportScreen(final String report, final ItemI item, final RunData data) {
        final RunData search = TurbineUtils.SetSearchProperties(data, item);
        try {
            final String popup   = (String) TurbineUtils.GetPassedParameter("popup", search);
            final String project = (String) TurbineUtils.GetPassedParameter("project", search);
            final String topTab  = (String) TurbineUtils.GetPassedParameter("topTab", search);
            final String params  = (String) TurbineUtils.GetPassedParameter("params", search);

            final StringBuilder path = new StringBuilder(TurbineUtils.GetRelativeServerPath(search) + "/app/template/" + URLEncoder.encode(report, encoding) + "/search_field/" + URLEncoder.encode(((String) TurbineUtils.GetPassedParameter("search_field", search)), encoding) + "/search_value/" + URLEncoder.encode(((String) TurbineUtils.GetPassedParameter("search_value", search)), encoding) + "/search_element/" + URLEncoder.encode(((String) TurbineUtils.GetPassedParameter("search_element", search)), encoding));
            if (StringUtils.isNotBlank(popup)) {
                path.append("/popup/").append(URLEncoder.encode(popup, encoding));
            }
            if (StringUtils.isNotBlank(project)) {
                path.append("/project/").append(URLEncoder.encode(project, encoding));
            }
            if (StringUtils.isNotBlank(topTab)) {
                path.append("/topTab/").append(URLEncoder.encode(topTab, encoding));
            }
            if (StringUtils.isNotBlank(params)) {
                path.append(URLEncoder.encode(params, encoding));
            }
            search.setRedirectURI(path.toString());
        } catch (UnsupportedEncodingException e) {
            try {
                log.error("An error occurred trying to redirect the user to the report screen '{}' for item {}", report, item.getItem().getIDValue(), e);
            } catch (XFTInitException | ElementNotFoundException ignored) {
                log.error("An error occurred trying to redirect the user to the report screen '{}' for an item of type {} (couldn't get item ID, that threw an exception as well)", report, item.getXSIType(), e);
            }
        }
    }

    @SuppressWarnings("unused")
    public void redirectToScreen(final String report, final RunData data) {
        try {
            final String popup = (String) TurbineUtils.GetPassedParameter("popup", data);
            final String project = (String) TurbineUtils.GetPassedParameter("project", data);

            final StringBuilder path = new StringBuilder(TurbineUtils.GetRelativeServerPath(data) + "/app/template/" + URLEncoder.encode(report, encoding));
            if (StringUtils.isNotBlank(popup)) {
                path.append("/popup/").append(URLEncoder.encode(popup, encoding));
            }
            if (StringUtils.isNotBlank(project)) {
                path.append("/project/").append(URLEncoder.encode(project, encoding));
            }
            data.setRedirectURI(path.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("An error occurred trying to redirect the user to the screen {}", report, e);
        }
    }

    @SuppressWarnings("unused")
    public void redirectToReportScreen(final ItemI item, final RunData data) {
        try {
            final SchemaElement schemaElement = SchemaElement.GetElement(item.getXSIType());
            redirectToReportScreen(DisplayItemAction.GetReportScreen(schemaElement), item, data);
        } catch (XFTInitException | ElementNotFoundException e) {
            log.error("An error occurred trying to redirect the user to the report screen", e);
        }
    }

    public static String csrfTokenErrorMessage(final HttpServletRequest request) {
        final StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(request.getMethod()).append(" on URL: ").append(request.getRequestURL()).append(" from ").append(AccessLogger.GetRequestIp(request)).append(" (").append(request.getRemotePort()).append(") user: ").append(request.getRemoteHost()).append("\n");
        errorMessage.append("Headers:\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hName = headerNames.nextElement();
            errorMessage.append(hName).append(": ").append(request.getHeader(hName)).append("\n");
        }
        errorMessage.append("\n Cookies:\n");

        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                errorMessage.append(cookie.getName()).append(" ").append(cookie.getValue()).append(" ").append(cookie.getMaxAge()).append(" ").append(cookie.getDomain()).append("\n");
            }
        }
        return errorMessage.toString();
    }

    //just a wrapper for isCsrfTokenOk(request, token)
    public static boolean isCsrfTokenOk(final RunData runData) throws Exception {
        //occasionally, (really, only on "actions" that inherit off secure screen instead of secure action like report issue)
        //the HTTPServletRequest parameters magically get cleared. that's why this method is here.
        String clientToken = TurbineUtils.escapeParam(runData.getParameters().get("XNAT_CSRF"));
        return isCsrfTokenOk(runData.getRequest(), clientToken, true);
    }

    //just a wrapper for isCsrfTokenOk(request, token)
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static boolean isCsrfTokenOk(final HttpServletRequest request, final boolean strict) throws Exception {
        return isCsrfTokenOk(request, request.getParameter("XNAT_CSRF"), strict);
    }

    //this is a little silly in that it either returns true or throws an exception...
    //if you change that behavior, look at every place this is used to be sure it actually
    //checks for true/false. I know for a fact it doesn't in XnatSecureGuard.	
    public static boolean isCsrfTokenOk(final HttpServletRequest request, final String clientToken, final boolean strict) throws Exception {
        final boolean csrfEmailEnabled = XDAT.getSiteConfigPreferences().getCsrfEmailAlert();

        if (!XDAT.getSiteConfigPreferences().getEnableCsrfToken()) {
            return true;
        }

        //let anyone using something other than a browser ignore the token.
        final String userAgent = request.getHeader(USER_AGENT);
        if (!strict) {
            if (StringUtils.isBlank(userAgent) || StringUtils.contains(userAgent, "XNATDesktopClient")) {
                return true;
            } else {
                final Browser browser = Browser.parseUserAgentString(userAgent);
                if ((!(browser.getBrowserType().equals(BrowserType.MOBILE_BROWSER) || browser.getBrowserType().equals(BrowserType.WEB_BROWSER))) || userAgent.toUpperCase().contains("JAVA")) {
                    return true;
                }
            }
        }

        final HttpSession session = request.getSession();
        final String serverToken = (String) session.getAttribute("XNAT_CSRF");

        if (serverToken == null) {
            final String errorMessage = csrfTokenErrorMessage(request);
            if (csrfEmailEnabled) {
                AdminUtils.sendAdminEmail("Possible CSRF Attempt", "XNAT_CSRF token was not properly set in the session.\n" + errorMessage);
            }
            throw new Exception("INVALID CSRF (" + errorMessage + ")");
        }

        final String method = request.getMethod();
        if (StringUtils.equalsAnyIgnoreCase(method, "POST", "PUT", "DELETE")) {
            //pull the token out of the parameter
            if (serverToken.equalsIgnoreCase(clientToken)) {
                return true;
            } else {
                final String errorMessage = csrfTokenErrorMessage(request);
                if (csrfEmailEnabled) {
                    AdminUtils.sendAdminEmail("Possible CSRF Attempt", errorMessage);
                }
                throw new Exception("INVALID CSRF (" + errorMessage + ")");
            }
        } else {
            return true;
        }
    }
    
    protected boolean isAuthorized(final RunData data) throws Exception {
        if (XDAT.getSiteConfigPreferences().getRequireLogin() || TurbineUtils.HasPassedParameter("par", data)) {
            TurbineVelocity.getContext(data).put("logout", "true");
            data.getParameters().setString("logout", "true");
            boolean isAuthorized = false;

            UserI user = XDAT.getUserDetails();
            if (user == null || user.isGuest()) {
                data.getParameters().add("nextPage", data.getTemplateInfo().getScreenTemplate());
                if (!data.getAction().equalsIgnoreCase("")) {
                    data.getParameters().add("nextAction", data.getAction());
                } else {
                    data.getParameters().add("nextAction", Turbine.getConfiguration().getString("action.login"));
                }
            } else {
                AccessLogger.LogActionAccess(data);
                isAuthorized = true;
            }
            return isAuthorized && isCsrfTokenOk(data);
        } else {
            final UserI user = XDAT.getUserDetails();
            final boolean isAuthorized = TurbineUtils.isAuthorized(data, user, allowGuestAccess());

            data.getParameters().add("new_session", "TRUE");
            AccessLogger.LogActionAccess(data);

            if (!isAuthorized) {
                String Destination = data.getTemplateInfo().getScreenTemplate();
                data.getParameters().add("nextPage", Destination);
                if (!data.getAction().equalsIgnoreCase("")) {
                    data.getParameters().add("nextAction", data.getAction());
                } else {
                    data.getParameters().add("nextAction", Turbine.getConfiguration().getString("action.login"));
                }
            }
            return isAuthorized && isCsrfTokenOk(data);
        }
    }

    public boolean allowGuestAccess() {
        return true;
    }

    public static EventUtils.TYPE getEventType(final RunData data) {
        final String id = (String) TurbineUtils.GetPassedParameter(EventUtils.EVENT_TYPE, data);
        if (id != null) {
            return EventUtils.getType(id, EventUtils.TYPE.WEB_FORM);
        } else {
            return EventUtils.TYPE.WEB_FORM;
        }
    }

    public static String getReason(RunData data) {
        return (String) TurbineUtils.GetPassedParameter(EventUtils.EVENT_REASON, data);
    }

    public static String getAction(RunData data) {
        return (String) TurbineUtils.GetPassedParameter(EventUtils.EVENT_ACTION, data);
    }

    public static String getComment(RunData data) {
        return (String) TurbineUtils.GetPassedParameter(EventUtils.EVENT_COMMENT, data);
    }

    public static EventDetails newEventInstance(RunData data, EventUtils.CATEGORY cat) {
        return EventUtils.newEventInstance(cat, getEventType(data), getAction(data), getReason(data), getComment(data));
    }

    public static EventDetails newEventInstance(RunData data, EventUtils.CATEGORY cat, String action) {
        return EventUtils.newEventInstance(cat, getEventType(data), (getAction(data) != null) ? getAction(data) : action, getReason(data), getComment(data));
    }

    public void handleException(RunData data, XFTItem first, Throwable error, String itemIdentifier) {
        log.error("", error);
        data.getSession().setAttribute(itemIdentifier, first);
        data.addMessage(error.getMessage());
        if (data.getParameters().getString("edit_screen") != null) {
            data.setScreenTemplate(data.getParameters().getString("edit_screen"));
        } else {
            data.setScreenTemplate("Index.vm");
        }
    }

    public void notifyAdmin(UserI authenticatedUser, RunData data, int code, String subject, String message) throws IOException {
        AdminUtils.sendAdminEmail(authenticatedUser, subject, message);
        data.getResponse().sendError(code);
    }

    protected boolean displayPopulatorErrors(final PopulateItem populator, final RunData data, final XFTItem item) {
        if (!populator.hasError()) {
            return false;
        }

        final InvalidValueException error =  populator.getError();
        data.addMessage(error.getMessage());
        TurbineUtils.SetEditItem(item, data);
        data.setScreenTemplate("XDATScreen_edit_projectData.vm");
        return true;
    }

    protected void displayProjectConflicts(final Collection<String> conflicts, final RunData data, final XFTItem item) {
        final StringBuilder message = new StringBuilder();
        for (final String conflict : conflicts) {
            message.append(conflict).append("<br/>");
        }
        displayProjectEditError(message.toString(), data, item);
    }

    @SuppressWarnings("unused")
    public void displayProjectEditError(RunData data, XFTItem item) {
        displayProjectEditError(null, data, item);
    }

    // Displays an error to the user.
    public void displayProjectEditError(String msg, RunData data, XFTItem item) {
        if (null != msg && !msg.isEmpty()) {
            data.addMessage(msg);
        }
        TurbineUtils.SetEditItem(item, data);
        if (TurbineUtils.GetPassedParameter("edit_screen", data) != null) {
            data.setScreenTemplate(((String) TurbineUtils.GetPassedParameter("edit_screen", data)));
        }
    }
}

