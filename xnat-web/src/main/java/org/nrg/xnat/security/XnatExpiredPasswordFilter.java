/*
 * web: org.nrg.xnat.security.XnatExpiredPasswordFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.codec.Base64;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection", "unused", "SameParameterValue"})
public class XnatExpiredPasswordFilter extends GenericFilterBean {
    @Autowired
    public XnatExpiredPasswordFilter(final SiteConfigPreferences preferences, final JdbcTemplate jdbcTemplate, final AliasTokenService aliasTokenService, final DateValidation dateValidation) {
        super();
        _preferences = preferences;
        _aliasTokenService = aliasTokenService;
        _jdbcTemplate = jdbcTemplate;
        _dateValidation = dateValidation;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;
        final UserI user = XDAT.getUserDetails();
        final HttpSession session = request.getSession();
        final Object passwordExpired = session.getAttribute("expired");
        // MIGRATION: Need to remove arcspec.
        final ArcArchivespecification _arcSpec = ArcSpecManager.GetInstance();

        final String referer = request.getHeader("Referer");
        if (BooleanUtils.toBooleanDefaultIfNull((Boolean) session.getAttribute("forcePasswordChange"), false)) {
            try {
                String refererPath = null;
                String uri = new URI(request.getRequestURI()).getPath();
                String shortUri = uri;
                try{
                    shortUri = uri.substring(0, uri.indexOf("?"));
                }
                catch(Exception ignored){

                }

                String shortRefererPath = null;
                if (!StringUtils.isBlank(referer)) {
                    refererPath = new URI(referer).getPath();
                    shortRefererPath = refererPath;
                    if(refererPath.contains("?")) {
                        shortRefererPath = refererPath.substring(0, refererPath.indexOf("?"));
                    }
                }
                if (shortUri.endsWith(changePasswordPath) || uri.endsWith(changePasswordDestination) || uri.endsWith(logoutDestination) || uri.endsWith(loginPath) || uri.endsWith(loginDestination)) {
                    //If you're already on the change password page, continue on without redirect.
                    chain.doFilter(req, res);
                } else if (!StringUtils.isBlank(refererPath) && (shortRefererPath.endsWith(changePasswordPath) || refererPath.endsWith(changePasswordDestination) || refererPath.endsWith(logoutDestination))) {
                    //If you're on a request within the change password page, continue on without redirect.
                    chain.doFilter(req, res);
                } else {
                    response.sendRedirect(TurbineUtils.GetFullServerPath() + changePasswordPath);
                }
            } catch (URISyntaxException ignored) {
                //
            }
        } else if (passwordExpired != null && !(Boolean) passwordExpired) {
            //If the date of password change was checked earlier in the session and found to be not expired, do not send them to the expired password page.
            chain.doFilter(request, response);
        } else if (_arcSpec == null || !_arcSpec.isComplete()) {
            //If the arc spec has not yet been set, have the user configure the arc spec before changing their password. This prevents a negative interaction with the arc spec filter.
            chain.doFilter(request, response);
        } else {
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
                        _log.error("An error occurred trying to check for non-expiring role for user " + username, e);
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
                    chain.doFilter(req, res);
                }
            } else {
                String uri = request.getRequestURI();
                String shortUri = uri;
                try{
                    shortUri = uri.substring(0, uri.indexOf("?"));
                }
                catch(Exception ignored){

                }
                String shortRefererPath = referer;
                if (!StringUtils.isBlank(referer) && referer.contains("?")) {
                    shortRefererPath = referer.substring(0, referer.indexOf("?"));
                }

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
                    if (
                            user.getAuthorization() != null && user.getAuthorization().getAuthMethod().equals(XdatUserAuthService.LDAP)
                            ) {
                        // Shouldn't check for a localdb expired password if user is coming in through LDAP
                        chain.doFilter(req, res);
                    } else if (user.isEnabled()) {
                        boolean isExpired = checkForExpiredPassword(user);
                        boolean requireSalted = _preferences.getRequireSaltedPasswords();
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
        final String type = _preferences.getPasswordExpirationType();
        if (StringUtils.equals("Interval", type)) {
            passwordExpirationInterval = true;
            passwordExpirationSetting = _preferences.getPasswordExpirationInterval();
            passwordExpirationDisabled = StringUtils.equals(passwordExpirationSetting, "0");
        } else if (StringUtils.equals("Date", type)) {
            try {
                passwordExpirationSetting = _dateValidation.convertDateToLongString(_preferences.getPasswordExpirationDate());
                passwordExpirationInterval = false;
                passwordExpirationDisabled = StringUtils.equals(passwordExpirationSetting, "0");
            } catch (SiteConfigurationException e) {
                _log.error("A site configuration error was detected for the password expiration date. Please check the configured value and make sure it's using a properly formatted date.");
            }
        } else {
            passwordExpirationDisabled = true;
        }
    }

    private boolean checkForExpiredPassword(final UserI user) {
        return checkForExpiredPassword(user.getUsername());
    }

    private boolean checkForExpiredPassword(final String username) {
        try {
            if (isPasswordExpirationDisabled()) {
                return false;
            }
            if (isPasswordExpirationInterval()) {
                List<Boolean> expired = _jdbcTemplate.query("SELECT ((now()-password_updated)> (Interval '" + passwordExpirationSetting + "')) AS expired FROM xhbm_xdat_user_auth WHERE auth_user = ? AND auth_method = 'localdb'", new String[]{username}, new RowMapper<Boolean>() {
                    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getBoolean(1);
                    }
                });
                return expired.get(0);
            } else {
                List<Boolean> expired = _jdbcTemplate.query("SELECT (to_date('" + new SimpleDateFormat("MM/dd/yyyy").format(new Date(Long.parseLong(passwordExpirationSetting))) + "', 'MM/DD/YYYY') BETWEEN password_updated AND now()) AS expired FROM xhbm_xdat_user_auth WHERE auth_user = ? AND auth_method = 'localdb'", new String[]{username}, new RowMapper<Boolean>() {
                    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getBoolean(1);
                    }
                });
                return expired.get(0);
            }
        } catch (Throwable e) { // ldap authentication can throw an exception during these queries
            _log.error(e.getMessage(), e);
        }
        return false;
    }

    private boolean isPasswordExpirationDisabled() {
        return passwordExpirationDisabled;
    }

    private boolean isPasswordExpirationInterval() {
        return passwordExpirationInterval;
    }

    private boolean isUserNonExpiring(UserI user) {
        try {
            return Roles.checkRole(user, UserRole.ROLE_NON_EXPIRING);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isUserNonExpiring(final String username) {
        return _jdbcTemplate.queryForObject("SELECT COUNT(*) FROM xhbm_user_role WHERE username = ? AND role = ? AND enabled = 't'", new String[]{username, UserRole.ROLE_NON_EXPIRING}, Boolean.class);
    }

    private void checkUserChangePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String uri = new URI(request.getRequestURI()).getPath();
            if (uri.endsWith("XDATScreen_UpdateUser.vm") && request.getParameterMap().isEmpty()) {
                response.sendRedirect(TurbineUtils.GetFullServerPath() + "/app/template/Login.vm");
            }
        } catch (URISyntaxException ignored) {
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatExpiredPasswordFilter.class);

    private String changePasswordPath        = "";
    private String changePasswordDestination = "";
    private String logoutDestination         = "";
    private String loginPath                 = "";
    private String loginDestination          = "";

    private String  inactiveAccountPath;
    private String  inactiveAccountDestination;
    private String  emailVerificationDestination;
    private String  emailVerificationPath;
    private boolean passwordExpirationDisabled;
    private boolean passwordExpirationInterval;
    private String  passwordExpirationSetting;

    private final SiteConfigPreferences _preferences;
    private final JdbcTemplate          _jdbcTemplate;
    private final AliasTokenService     _aliasTokenService;
    private final DateValidation        _dateValidation;
}
