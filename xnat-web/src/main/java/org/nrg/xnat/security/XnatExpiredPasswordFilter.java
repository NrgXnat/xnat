/*
 * web: org.nrg.xnat.security.XnatExpiredPasswordFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.UserRole;
import org.nrg.xdat.om.ArcArchivespecification;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.validation.DateValidation;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static org.nrg.xnat.utils.XnatHttpUtils.getCredentials;

@SuppressWarnings("SqlResolve")
@Component
@Slf4j
public class XnatExpiredPasswordFilter extends GenericFilterBean {
    @Autowired
    public XnatExpiredPasswordFilter(final SiteConfigPreferences preferences, final AliasTokenService aliasTokenService, final DateValidation dateValidation, final NamedParameterJdbcTemplate template) {
        super();
        _preferences = preferences;
        _aliasTokenService = aliasTokenService;
        _template = template;
        _dateValidation = dateValidation;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest  request         = (HttpServletRequest) req;
        final HttpServletResponse response        = (HttpServletResponse) res;
        final UserI               user            = XDAT.getUserDetails();
        final HttpSession         session         = request.getSession();
        final boolean             passwordExpired = BooleanUtils.toBooleanDefaultIfNull((Boolean) session.getAttribute("expired"), false);

        final ArcArchivespecification arcSpec = ArcSpecManager.GetInstance();

        final String referer = request.getHeader("Referer");
        if (BooleanUtils.toBooleanDefaultIfNull((Boolean) session.getAttribute("forcePasswordChange"), false)) {
            try {
                final String uri      = new URI(request.getRequestURI()).getPath();
                final String shortUri = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;

                final String refererPath;
                final String shortRefererPath;
                if (StringUtils.isNotBlank(referer)) {
                    refererPath = new URI(referer).getPath();
                    shortRefererPath = refererPath.contains("?") ? refererPath.substring(0, refererPath.indexOf("?")) : refererPath;
                } else {
                    refererPath = null;
                    shortRefererPath = null;
                }
                if (shortUri.endsWith(changePasswordPath) || uri.endsWith(changePasswordDestination) || uri.endsWith(logoutDestination) || uri.endsWith(loginPath) || uri.endsWith(loginDestination)) {
                    //If you're already on the change password page, continue on without redirect.
                    chain.doFilter(req, res);
                } else if (StringUtils.isNotBlank(refererPath) && (shortRefererPath.endsWith(changePasswordPath) || refererPath.endsWith(changePasswordDestination) || refererPath.endsWith(logoutDestination))) {
                    //If you're on a request within the change password page, continue on without redirect.
                    chain.doFilter(req, res);
                } else {
                    response.sendRedirect(TurbineUtils.GetFullServerPath() + changePasswordPath);
                }
            } catch (URISyntaxException ignored) {
                //
            }
        } else if (!passwordExpired) {
            //If the date of password change was checked earlier in the session and found to be not expired, do not send them to the expired password page.
            chain.doFilter(request, response);
        } else if (arcSpec == null || !arcSpec.isComplete()) {
            //If the arc spec has not yet been set, have the user configure the arc spec before changing their password. This prevents a negative interaction with the arc spec filter.
            chain.doFilter(request, response);
        } else {
            if (user == null || user.isGuest()) {
                //If the user is not logged in, do not send them to the expired password page.
                final Pair<String, String> credentials;
                try {
                    credentials = getCredentials(request);
                } catch (ParseException e) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
                    return;
                }
                if (StringUtils.isNotBlank(credentials.getLeft())) {
                    final String username;
                    if (AliasToken.isAliasFormat(credentials.getLeft())) {
                        final AliasToken alias = _aliasTokenService.locateToken(credentials.getLeft());
                        if (alias == null) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your security token has expired or is invalid. Please try again after updating your session.");
                            return;
                        }
                        username = alias.getXdatUserId();
                    } else {
                        username = credentials.getLeft();
                    }

                    // Check whether the user is connected to an active role for non_expiring.
                    try {
                        if (isUserNonExpiring(username)) {
                            chain.doFilter(request, response);
                        }
                    } catch (Exception e) {
                        log.error("An error occurred trying to check for non-expiring role for user " + username, e);
                    }

                    if (passwordExpirationSetting == PasswordExpirationSetting.Disabled) {
                        chain.doFilter(request, response);
                    } else {
                        final boolean isExpired = checkForExpiredPassword(username);
                        session.setAttribute("expired", isExpired);
                        if (username != null && isExpired && !username.equals("guest")) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your password has expired. Please try again after changing your password.");
                        } else {
                            chain.doFilter(request, response);
                        }
                    }
                } else {
                    checkUserChangePassword(request, response);
                    //User is not authenticated through basic authentication either.
                    chain.doFilter(req, res);
                }
            } else {
                final String uri              = request.getRequestURI();
                final String shortUri         = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
                final String shortRefererPath = StringUtils.isNotBlank(referer) && referer.contains("?") ? referer.substring(0, referer.indexOf("?")) : referer;

                if (user.isGuest()) {
                    //If you're a guest and you try to access the change password page, you get sent to the login page since there's no password on the guest account to change.
                    checkUserChangePassword(request, response);
                }

                if (user.isGuest() ||
                    //If you're logging in or out, or going to the login page itself
                    (uri.endsWith(logoutDestination) || uri.endsWith(loginPath) || uri.endsWith(loginDestination)) ||
                    //If you're already on the change password page, continue on without redirect.
                    (user.isEnabled() && (shortUri.endsWith(changePasswordPath) || uri.endsWith(changePasswordDestination))) ||
                    //If you're already on the inactive account page or reactivating an account, continue on without redirect.
                    (!user.isEnabled() && (uri.endsWith(inactiveAccountPath) || uri.endsWith(inactiveAccountDestination) ||
                                           uri.endsWith(emailVerificationPath) || uri.endsWith(emailVerificationDestination) ||
                                           (referer != null && (referer.endsWith(inactiveAccountPath) || referer.endsWith(inactiveAccountDestination))))) ||
                    //If you're on a request within the change password page, continue on without redirect.
                    (referer != null && (shortRefererPath.endsWith(changePasswordPath) || referer.endsWith(changePasswordDestination) ||
                                         referer.endsWith(logoutDestination)))) {
                    chain.doFilter(req, res);
                } else {
                    final UserAuthI authorization = user.getAuthorization();
                    if (authorization != null && !StringUtils.equals(authorization.getAuthMethod(), XdatUserAuthService.LOCALDB)) {
                        // Shouldn't check for a localdb expired password if user is coming in through another validation method.
                        chain.doFilter(req, res);
                    } else if (user.isEnabled()) {
                        final boolean isExpired     = checkForExpiredPassword(user);
                        final boolean requireSalted = _preferences.getRequireSaltedPasswords();
                        if ((!isUserNonExpiring(user) && isExpired) || (requireSalted && user.getSalt() == null)) {
                            session.setAttribute("expired", true);
                            response.sendRedirect(TurbineUtils.GetFullServerPath() + changePasswordPath);
                        } else {
                            chain.doFilter(request, response);
                        }
                    } else {
                        response.sendRedirect(TurbineUtils.GetFullServerPath() + inactiveAccountPath);
                    }
                }
            }
        }
    }

    public void setChangePasswordPath(String path) {
        this.changePasswordPath = path;
    }

    public void setChangePasswordDestination(String path) {
        this.changePasswordDestination = path;
    }

    public void setLogoutDestination(String path) {
        this.logoutDestination = path;
    }

    public void setLoginPath(String path) {
        this.loginPath = path;
    }

    public void setLoginDestination(String loginDestination) {
        this.loginDestination = loginDestination;
    }

    public void setInactiveAccountPath(String inactiveAccountPath) {
        this.inactiveAccountPath = inactiveAccountPath;
    }

    @SuppressWarnings("unused")
    public String getInactiveAccountPath() {
        return inactiveAccountPath;
    }

    public void setInactiveAccountDestination(String inactiveAccountDestination) {
        this.inactiveAccountDestination = inactiveAccountDestination;
    }

    @SuppressWarnings("unused")
    public String getInactiveAccountDestination() {
        return inactiveAccountDestination;
    }

    public void setEmailVerificationDestination(String emailVerificationDestination) {
        this.emailVerificationDestination = emailVerificationDestination;
    }

    @SuppressWarnings("unused")
    public String getEmailVerificationDestination() {
        return emailVerificationDestination;
    }

    public void setEmailVerificationPath(String emailVerificationPath) {
        this.emailVerificationPath = emailVerificationPath;
    }

    @SuppressWarnings("unused")
    public String getEmailVerificationPath() {
        return emailVerificationPath;
    }

    public void refreshFromSiteConfig() {
        passwordExpirationSetting = PasswordExpirationSetting.valueOf(_preferences.getPasswordExpirationType());
        switch (passwordExpirationSetting) {
            case Interval:
                passwordExpirationQuery = QUERY_PASSWORD_EXPIRATION_INTERVAL;
                passwordExpirationKey = KEY_PASSWORD_EXPIRATION_INTERVAL;
                passwordExpirationValue = _preferences.getPasswordExpirationInterval();
                break;

            case Date:
                try {
                    passwordExpirationQuery = QUERY_BY_EXPIRATION_DATE;
                    passwordExpirationKey = KEY_EXPIRATION_DATE;
                    passwordExpirationValue = DATE_FORMAT.format(_dateValidation.parseDate(_preferences.getPasswordExpirationDate()));
                } catch (SiteConfigurationException e) {
                    log.error("A site configuration error was detected for the password expiration date. Please check the configured value and make sure it's using a properly formatted date.");
                }
            case Disabled:
                passwordExpirationQuery = null;
                passwordExpirationKey = null;
                passwordExpirationValue = null;
        }
    }

    private boolean checkForExpiredPassword(final UserI user) {
        return checkForExpiredPassword(user.getUsername());
    }

    private boolean checkForExpiredPassword(final String username) {
        try {
            if (passwordExpirationSetting == PasswordExpirationSetting.Disabled) {
                return false;
            }
            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("username", username);
            parameters.put(passwordExpirationKey, passwordExpirationValue);
            return _template.queryForObject(passwordExpirationQuery, parameters, Boolean.class);
        } catch (Throwable e) { // Some authentication methods can throw an exception during these queries
            log.error(e.getMessage(), e);
        }
        return false;
    }

    private boolean isUserNonExpiring(final UserI user) {
        try {
            return Roles.checkRole(user, UserRole.ROLE_NON_EXPIRING);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isUserNonExpiring(final String username) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("role", UserRole.ROLE_NON_EXPIRING);
        return _template.queryForObject("SELECT COUNT(*) FROM xhbm_user_role WHERE username = :username AND role = :role AND enabled = 't'", parameters, Boolean.class);
    }

    private void checkUserChangePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            final String uri = new URI(request.getRequestURI()).getPath();
            if (uri.endsWith("XDATScreen_UpdateUser.vm") && request.getParameterMap().isEmpty()) {
                response.sendRedirect(TurbineUtils.GetFullServerPath() + "/app/template/Login.vm");
            }
        } catch (URISyntaxException ignored) {
        }
    }

    private enum PasswordExpirationSetting {
        Interval,
        Date,
        Disabled
    }

    private static final SimpleDateFormat DATE_FORMAT                        = new SimpleDateFormat("MM/dd/yyyy");
    private static final String           KEY_EXPIRATION_DATE                = "expirationDate";
    private static final String           KEY_PASSWORD_EXPIRATION_INTERVAL   = "passwordExpirationInterval";
    private static final String           QUERY_BY_EXPIRATION_DATE           = "SELECT (to_date(:" + KEY_EXPIRATION_DATE + ", 'MM/DD/YYYY') BETWEEN password_updated AND now()) AS expired FROM xhbm_xdat_user_auth WHERE auth_user = :username AND auth_method = 'localdb'";
    private static final String           QUERY_PASSWORD_EXPIRATION_INTERVAL = "SELECT ((now() - password_updated) > (INTERVAL :" + KEY_PASSWORD_EXPIRATION_INTERVAL + ")) AS expired FROM xhbm_xdat_user_auth WHERE auth_user = :username AND auth_method = 'localdb'";

    private String changePasswordPath        = "";
    private String changePasswordDestination = "";
    private String logoutDestination         = "";
    private String loginPath                 = "";
    private String loginDestination          = "";

    private String                    inactiveAccountPath;
    private String                    inactiveAccountDestination;
    private String                    emailVerificationDestination;
    private String                    emailVerificationPath;
    private String                    passwordExpirationQuery;
    private String                    passwordExpirationKey;
    private String                    passwordExpirationValue;
    private PasswordExpirationSetting passwordExpirationSetting;

    private final SiteConfigPreferences      _preferences;
    private final NamedParameterJdbcTemplate _template;
    private final AliasTokenService          _aliasTokenService;
    private final DateValidation             _dateValidation;
}
