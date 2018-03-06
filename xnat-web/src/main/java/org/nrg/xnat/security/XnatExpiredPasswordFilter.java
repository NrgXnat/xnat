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
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection", "unused", "SameParameterValue"})
@Slf4j
public class XnatExpiredPasswordFilter extends OncePerRequestFilter {
    public static final String COOKIE_SESSION_EXPIRATION_TIME = "SESSION_EXPIRATION_TIME";

    @Autowired
    public XnatExpiredPasswordFilter(final SiteConfigPreferences preferences, final NamedParameterJdbcTemplate jdbcTemplate, final AliasTokenService aliasTokenService, final DateValidation dateValidation) {
        super();

        _preferences = preferences;
        _aliasTokenService = aliasTokenService;
        _jdbcTemplate = jdbcTemplate;
        _dateValidation = dateValidation;

        refreshFromSiteConfig();
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws ServletException, IOException {
        final HttpSession session = request.getSession();

        // Regardless of why you're here, we're going to do this.
        final Cookie cookie = new Cookie(COOKIE_SESSION_EXPIRATION_TIME, new Date().getTime() + "," + session.getMaxInactiveInterval() * 1000);
        cookie.setPath(request.getContextPath() + "/");
        response.addCookie(cookie);
        log.debug("Updated session expiration time cookie '{}' to value '{}'.", cookie.getName(), cookie.getValue());

        // MIGRATION: Need to remove arcspec.
        final ArcArchivespecification arcSpec         = ArcSpecManager.GetInstance();
        final String                  referer         = request.getHeader("Referer");
        final Object                  passwordExpired = session.getAttribute("expired");

        if (BooleanUtils.toBooleanDefaultIfNull((Boolean) session.getAttribute("forcePasswordChange"), false)) {
            try {
                final String refererPath      = !StringUtils.isBlank(referer) ? new URI(referer).getPath() : null;
                final String uri              = getRequestUriPath(request);
                final String shortUri         = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
                final String shortRefererPath = refererPath != null ? (refererPath.contains("?") ? refererPath.substring(0, refererPath.indexOf("?")) : refererPath) : null;

                if (shortUri.endsWith(changePasswordPath) || uri.endsWith(changePasswordDestination) || uri.endsWith(logoutDestination) || uri.endsWith(loginPath) || uri.endsWith(loginDestination)) {
                    //If you're already on the change password page, continue on without redirect.
                    chain.doFilter(request, response);
                } else if (!StringUtils.isBlank(refererPath) && (shortRefererPath.endsWith(changePasswordPath) || refererPath.endsWith(changePasswordDestination) || refererPath.endsWith(logoutDestination))) {
                    //If you're on a request within the change password page, continue on without redirect.
                    chain.doFilter(request, response);
                } else {
                    response.sendRedirect(TurbineUtils.GetFullServerPath() + changePasswordPath);
                }
            } catch (URISyntaxException ignored) {
                //
            }
        } else if (passwordExpired != null && !(Boolean) passwordExpired) {
            //If the date of password change was checked earlier in the session and found to be not expired, do not send them to the expired password page.
            chain.doFilter(request, response);
        } else if (arcSpec == null || !arcSpec.isComplete()) {
            //If the arc spec has not yet been set, have the user configure the arc spec before changing their password. This prevents a negative interaction with the arc spec filter.
            chain.doFilter(request, response);
        } else {
            final UserI user = XDAT.getUserDetails();
            if (user == null || user.isGuest()) {
                //If the user is not logged in, do not send them to the expired password page.
                final String header = request.getHeader("Authorization");
                if (header != null && header.startsWith("Basic ")) {
                    //For users that authenticated using basic authentication, check whether their password is expired, and if so give them a 403 and a message that they need to change their password.
                    final String[] atoms = new String(Base64.decode(header.substring(6).getBytes("UTF-8")), "UTF-8").split(":");
                    if (atoms.length != 2) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The authentication token is invalid. You must provide a username and password separated by the ':' character.");
                        return;
                    }

                    final String username;
                    if (AliasToken.isAliasFormat(atoms[0])) {
                        final AliasToken alias = _aliasTokenService.locateToken(atoms[0]);
                        if (alias == null) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your security token has expired or is invalid. Please try again after updating your session.");
                            return;
                        }
                        username = alias.getXdatUserId();
                    } else {
                        username = atoms[0];
                    }

                    // Check whether the user is connected to an active role for non_expiring.
                    try {
                        if (isUserNonExpiring(username)) {
                            chain.doFilter(request, response);
                        }
                    } catch (Exception e) {
                        log.error("An error occurred trying to check for non-expiring role for user " + username, e);
                    }

                    if (isPasswordExpirationDisabled()) {
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
                    chain.doFilter(request, response);
                }
            } else {
                try {
                    final String  refererPath      = !StringUtils.isBlank(referer) ? new URI(referer).getPath() : null;
                    final String  uri              = getRequestUriPath(request);
                    final String  shortUri         = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
                    final String  shortRefererPath = refererPath != null ? (refererPath.contains("?") ? refererPath.substring(0, refererPath.indexOf("?")) : refererPath) : null;
                    final boolean isEnabled        = user.isEnabled();

                    // Each of the following variables is exclusive: once one is found true, the remaining will be found false.
                    //If you're logging in or out, or going to the login page itself
                    final boolean isLoginLogoutRequest = StringUtils.endsWithAny(uri, logoutDestination, loginPath, loginDestination);
                    //If you're already on the change password page, continue on without redirect.
                    final boolean isChangePassword = !isLoginLogoutRequest && isEnabled && (shortUri.endsWith(changePasswordPath) || uri.endsWith(changePasswordDestination));
                    //If you're already on the inactive account page or reactivating an account, continue on without redirect.
                    final boolean isInactiveOrReactivating = !isLoginLogoutRequest && !isChangePassword && !isEnabled &&
                                                             (StringUtils.endsWithAny(uri, inactiveAccountPath, inactiveAccountDestination, emailVerificationPath, emailVerificationDestination) ||
                                                              StringUtils.endsWithAny(referer, inactiveAccountPath, inactiveAccountDestination));
                    //If you're on a request within the change password page, continue on without redirect.
                    final boolean isOnChangePassword = !isLoginLogoutRequest && !isChangePassword && !isInactiveOrReactivating &&
                                                       (StringUtils.endsWith(shortRefererPath, changePasswordPath) || StringUtils.endsWithAny(referer, changePasswordDestination, logoutDestination));

                    if (isLoginLogoutRequest || isChangePassword || isInactiveOrReactivating || isOnChangePassword) {
                        chain.doFilter(request, response);
                    } else {
                        if (user.getAuthorization() != null && user.getAuthorization().getAuthMethod().equals(XdatUserAuthService.LDAP)) {
                            // Shouldn't check for a localdb expired password if user is coming in through a non-localdb provider.
                            chain.doFilter(request, response);
                        } else if (isEnabled) {
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
                } catch (URISyntaxException ignored) {
                    // the URI
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

    public String getInactiveAccountPath() {
        return inactiveAccountPath;
    }

    public void setInactiveAccountDestination(String inactiveAccountDestination) {
        this.inactiveAccountDestination = inactiveAccountDestination;
    }

    public String getInactiveAccountDestination() {
        return inactiveAccountDestination;
    }

    public void setEmailVerificationDestination(String emailVerificationDestination) {
        this.emailVerificationDestination = emailVerificationDestination;
    }

    public String getEmailVerificationDestination() {
        return emailVerificationDestination;
    }

    public void setEmailVerificationPath(String emailVerificationPath) {
        this.emailVerificationPath = emailVerificationPath;
    }

    public String getEmailVerificationPath() {
        return emailVerificationPath;
    }

    public void refreshFromSiteConfig() {
        _expirationType = ExpirationType.value(_preferences.getPasswordExpirationType());
        switch (_expirationType) {
            case Interval:
                _expirationSetting = _preferences.getPasswordExpirationInterval();
                break;

            case Date:
                try {
                    _expirationSetting = _dateValidation.convertDateToLongString(_preferences.getPasswordExpirationDate());
                } catch (SiteConfigurationException e) {
                    log.error("A site configuration error was detected for the password expiration date. Please check the configured value and make sure it's using a properly formatted date.");
                }
                break;
        }
    }

    private boolean checkForExpiredPassword(final UserI user) {
        return checkForExpiredPassword(user.getUsername());
    }

    private boolean checkForExpiredPassword(final String username) {
        if (isPasswordExpirationDisabled()) {
            return false;
        }
        try {
            final MapSqlParameterSource parameters = new MapSqlParameterSource("username", username).addValue("authMethod", AUTH_DEFAULT);

            final String query;
            if (isPasswordExpirationInterval()) {
                query = QUERY_BY_INTERVAL;
                parameters.addValue("interval", _expirationSetting);
            } else {
                query = QUERY_BY_DATE;
                parameters.addValue("date", new SimpleDateFormat("MM/dd/yyyy").format(new Date(Long.parseLong(_expirationSetting))));
            }

            return _jdbcTemplate.queryForObject(query, parameters, Boolean.class);
        } catch (Throwable e) { // ldap authentication can throw an exception during these queries
            log.error("An error occurred while checking whether the password has expired for user " + username, e);
        }
        return false;
    }

    private boolean isPasswordExpirationDisabled() {
        return _expirationType == ExpirationType.Disabled;
    }

    private boolean isPasswordExpirationInterval() {
        return _expirationType == ExpirationType.Interval;
    }

    private boolean isUserNonExpiring(UserI user) {
        try {
            return Roles.checkRole(user, UserRole.ROLE_NON_EXPIRING);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isUserNonExpiring(final String username) {
        return _jdbcTemplate.queryForObject("SELECT COUNT(*) FROM xhbm_user_role WHERE username = :username AND role = :role AND enabled = 't'", new MapSqlParameterSource("username", username).addValue("role", UserRole.ROLE_NON_EXPIRING), Boolean.class);
    }

    private void checkUserChangePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String uri = getRequestUriPath(request);
        if (uri.endsWith("XDATScreen_UpdateUser.vm") && request.getParameterMap().isEmpty()) {
            response.sendRedirect(TurbineUtils.GetFullServerPath() + "/app/template/Login.vm");
        }
    }

    @Nonnull
    private static String getRequestUriPath(final HttpServletRequest request) {
        try {
            return new URI(request.getRequestURI()).getPath();
        } catch (URISyntaxException e) {
            // Because the URI is coming from an incoming request, it should be valid syntax. Log it,
            // return a null, and everything should fall apart quite nicely.
            log.warn("The request URI is invalid for some reason, which makes literally no sense so you figure it out: {}", request.getRequestURI());
            return "";
        }
    }

    private enum ExpirationType {
        Interval,
        Date,
        Disabled;

        static ExpirationType value(final String value) {
            switch (value) {
                case "Interval":
                    return Interval;

                case "Date":
                    return Date;

                default:
                    return Disabled;
            }
        }
    }

    private static final String AUTH_DEFAULT      = "localdb";
    private static final String QUERY_BY_INTERVAL = "SELECT now() - password_updated > :interval::INTERVAL AS expired FROM xhbm_xdat_user_auth WHERE auth_user = :username AND auth_method = :authMethod";
    private static final String QUERY_BY_DATE     = "SELECT to_date(:date, 'MM/DD/YYYY') BETWEEN password_updated AND now() AS expired FROM xhbm_xdat_user_auth WHERE auth_user = :username AND auth_method = :authMethod";

    private String changePasswordPath        = "";
    private String changePasswordDestination = "";
    private String logoutDestination         = "";
    private String loginPath                 = "";
    private String loginDestination          = "";

    private String inactiveAccountPath;
    private String inactiveAccountDestination;
    private String emailVerificationDestination;
    private String emailVerificationPath;

    private ExpirationType _expirationType;
    private String         _expirationSetting;

    private final SiteConfigPreferences      _preferences;
    private final NamedParameterJdbcTemplate _jdbcTemplate;
    private final AliasTokenService          _aliasTokenService;
    private final DateValidation             _dateValidation;
}
