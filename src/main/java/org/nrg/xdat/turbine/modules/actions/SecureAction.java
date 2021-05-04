/*
 * core: org.nrg.xdat.turbine.modules.actions.SecureAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import static org.springframework.http.HttpHeaders.USER_AGENT;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.BrowserType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.StreamUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.InvalidCsrfException;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

            final StringBuilder path = new StringBuilder(XDAT.getSiteConfigurationProperty("siteUrl", TurbineUtils.GetRelativeServerPath(search))).append("/app/template/").append(URLEncoder.encode(report, encoding)).append("/search_field/").append(URLEncoder.encode(((String) TurbineUtils.GetPassedParameter("search_field", search)), encoding)).append("/search_value/").append(URLEncoder.encode(((String) TurbineUtils.GetPassedParameter("search_value", search)), encoding)).append("/search_element/").append(URLEncoder.encode(((String) TurbineUtils.GetPassedParameter("search_element", search)), encoding));
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
        } catch (ConfigServiceException e) {
            log.error("An error occurred trying to retrieve the siteUrl property from the system", e);
        }
    }

    @SuppressWarnings("unused")
    public void redirectToScreen(final String report, final RunData data) {
        try {
            final String popup   = (String) TurbineUtils.GetPassedParameter("popup", data);
            final String project = (String) TurbineUtils.GetPassedParameter("project", data);

            final StringBuilder path = new StringBuilder(XDAT.getSiteConfigurationProperty("siteUrl", TurbineUtils.GetRelativeServerPath(data))).append("/app/template/").append(URLEncoder.encode(report, encoding));
            if (StringUtils.isNotBlank(popup)) {
                path.append("/popup/").append(URLEncoder.encode(popup, encoding));
            }
            if (StringUtils.isNotBlank(project)) {
                path.append("/project/").append(URLEncoder.encode(project, encoding));
            }
            data.setRedirectURI(path.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("An error occurred trying to redirect the user to the screen {}", report, e);
        } catch (ConfigServiceException e) {
            log.error("An error occurred trying to retrieve the siteUrl property from the system", e);
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

    //just a wrapper for isCsrfTokenOk(request, token)
    public static boolean isCsrfTokenOk(final RunData runData) throws InvalidCsrfException {
        //occasionally, (really, only on "actions" that inherit off secure screen instead of secure action like report issue)
        //the HTTPServletRequest parameters magically get cleared. that's why this method is here.
        String clientToken = TurbineUtils.escapeParam(runData.getParameters().get("XNAT_CSRF"));
        return isCsrfTokenOk(runData.getRequest(), clientToken, true);
    }

    //just a wrapper for isCsrfTokenOk(request, token)
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static boolean isCsrfTokenOk(final HttpServletRequest request, final boolean strict) throws InvalidCsrfException {
        return isCsrfTokenOk(request, request.getParameter("XNAT_CSRF"), strict);
    }

    //this is a little silly in that it either returns true or throws an exception...
    //if you change that behavior, look at every place this is used to be sure it actually
    //checks for true/false. I know for a fact it doesn't in XnatSecureGuard.	
    public static boolean isCsrfTokenOk(final HttpServletRequest request, final String clientToken, final boolean strict) throws InvalidCsrfException {
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

        final HttpSession session     = request.getSession();
        final String      serverToken = (String) session.getAttribute("XNAT_CSRF");

        if (serverToken == null) {
            handleCsrfTokenError(request);
        }
        assert serverToken != null;

        final String method = request.getMethod();
        if (StringUtils.equalsAnyIgnoreCase(method, "POST", "PUT", "DELETE")) {
            //pull the token out of the parameter
            if (serverToken.equalsIgnoreCase(clientToken)) {
                return true;
            }
            handleCsrfTokenError(request);
        }
        return true;
    }

    public static void handleCsrfTokenError(final HttpServletRequest request) throws InvalidCsrfException {
        final String errorMessage = csrfTokenErrorMessage(request);
        if (XDAT.getSiteConfigPreferences().getCsrfEmailAlert()) {
            AdminUtils.sendAdminEmail("Possible CSRF Attempt", "XNAT_CSRF token was not properly set in the session.\n" + errorMessage);
        }
        final UserI user = Users.getUserPrincipal(request.getUserPrincipal());
        throw new InvalidCsrfException(errorMessage, user != null ? user.getUsername() : Users.DEFAULT_GUEST_USERNAME);
    }

    protected boolean isAuthorized(final RunData data) throws Exception {
        if (XDAT.getSiteConfigPreferences().getRequireLogin() || TurbineUtils.HasPassedParameter("par", data)) {
            TurbineVelocity.getContext(data).put("logout", "true");
            data.getParameters().setString("logout", "true");
        } else {
            data.getParameters().add("new_session", "TRUE");
        }

        AccessLogger.LogActionAccess(data);
        if (!TurbineUtils.isAuthorized(data, XDAT.getUserDetails(), allowGuestAccess())) {
            data.getParameters().add("nextPage", data.getTemplateInfo().getScreenTemplate());
            data.getParameters().add("nextAction", StringUtils.defaultIfBlank(data.getAction(), Turbine.getConfiguration().getString("action.login")));
            return false;
        }
        return isCsrfTokenOk(data);
    }

    public boolean allowGuestAccess() {
        return true;
    }

    public static EventUtils.TYPE getEventType(final RunData data) {
        final String id = (String) TurbineUtils.GetPassedParameter(EventUtils.EVENT_TYPE, data);
        return StringUtils.isNotBlank(id) ? EventUtils.getType(id, EventUtils.TYPE.WEB_FORM) : EventUtils.TYPE.WEB_FORM;
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

    public void handleException(final RunData data, final XFTItem first, final Throwable error, final String itemIdentifier) {
        log.error("An error occurred", error);
        data.getSession().setAttribute(itemIdentifier, first);
        data.addMessage(error.getMessage());
        data.setScreenTemplate(StringUtils.defaultIfBlank(data.getParameters().getString("edit_screen"), "Index.vm"));
    }

    public void notifyAdmin(UserI authenticatedUser, RunData data, int code, String subject, String message) throws IOException {
        AdminUtils.sendAdminEmail(authenticatedUser, subject, message);
        data.getResponse().sendError(code);
    }

    @SuppressWarnings("unused")
    protected boolean displayPopulatorErrors(final PopulateItem populator, final RunData data, final XFTItem item) {
        if (!populator.hasError()) {
            return false;
        }

        final InvalidValueException error = populator.getError();
        data.addMessage(error.getMessage());
        TurbineUtils.SetEditItem(item, data);
        data.setScreenTemplate("XDATScreen_edit_projectData.vm");
        return true;
    }

    @SuppressWarnings("unused")
    protected void displayProjectConflicts(final Collection<String> conflicts, final RunData data, final XFTItem item) {
        displayProjectEditError(String.join("<br/>", conflicts), data, item);
    }

    @SuppressWarnings("unused")
    public void displayProjectEditError(RunData data, XFTItem item) {
        displayProjectEditError(null, data, item);
    }

    // Displays an error to the user.
    public void displayProjectEditError(final String message, final RunData data, final XFTItem item) {
        if (StringUtils.isNotBlank(message)) {
            data.addMessage(message);
        }
        TurbineUtils.SetEditItem(item, data);
        final String editScreen = (String) TurbineUtils.GetPassedParameter("edit_screen", data);
        if (StringUtils.isNotBlank(editScreen)) {
            data.setScreenTemplate(editScreen);
        }
    }

    private static String csrfTokenErrorMessage(final HttpServletRequest request) {
        final Enumeration<String> headerNames = request.getHeaderNames();
        final String              headerText  = headerNames != null && headerNames.hasMoreElements() ? StreamUtils.asStream(headerNames).map(name -> " * " + name + ": " + formatHeaderValues(name, request)).collect(Collectors.joining("\n")) : NO_HEADERS_OR_COOKIES;
        final Cookie[]            cookies     = request.getCookies();
        final String              cookieText  = cookies != null && cookies.length > 0 ? Arrays.stream(cookies).map(cookie -> " * " + String.join(" ", cookie.getName(), cookie.getValue(), Integer.toString(cookie.getMaxAge()), cookie.getDomain())).collect(Collectors.joining("\n")) : NO_HEADERS_OR_COOKIES;
        return String.format(CSRF_MESSAGE_FORMAT, request.getMethod(), request.getRequestURL(), AccessLogger.GetRequestIp(request), headerText, cookieText);
    }

    private static String formatHeaderValues(final String name, final HttpServletRequest request) {
        if (StringUtils.equalsIgnoreCase(AUTH_HEADER_NAME, name)) {
            final String value = request.getHeader(AUTH_HEADER_NAME);
            return StringUtils.containsWhitespace(value) ? RegExUtils.removePattern(value, "\\s+.*$") + " " + AUTH_HEADER_MASK_VALUE : AUTH_HEADER_MASK_VALUE;
        }
        return StreamUtils.asStream(request.getHeaders(name)).collect(Collectors.joining(", "));
    }

    private static final String CSRF_MESSAGE_FORMAT    = "%s on URL: %s from %s:\nHeaders:\n%s\nCookies:\n%s";
    private static final String AUTH_HEADER_NAME       = "Authorization";
    private static final String AUTH_HEADER_MASK_VALUE = "XXXXXXXXX";
    private static final String NO_HEADERS_OR_COOKIES  = " * <none>";
}

