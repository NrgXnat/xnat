/*
 * web: org.nrg.xnat.utils.XnatHttpUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.om.XdatUserLogin;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xnat.security.alias.AliasTokenException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.codec.Base64;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

@Slf4j
public class XnatHttpUtils {
    private static final String PARAM_LOGIN_METHOD      = "login_method";
    private static final String PARAM_USERNAME          = "username";
    private static final String PARAM_PASSWORD          = "password";
    private static final String QUERY_UPGRADE_NEEDED    = "SELECT primary_password ~ '^[A-Fa-f0-9]{64}\\{[A-Za-z0-9]{64}}$' FROM xdat_user WHERE login = :" + PARAM_USERNAME;
    private static final String QUERY_UPGRADE_PASSWORD  = "UPDATE xdat_user SET primary_password = :" + PARAM_PASSWORD + ", salt = null, primary_password_encrypt = 1 WHERE login = :" + PARAM_USERNAME;
    private static final String QUERY_CLEAR_CACHE_ENTRY = "DELETE FROM xs_item_cache WHERE contents LIKE 'Item:(0(xdat:user)((login:string)=(%s)%%'";

    public static String getServerRoot(final HttpServletRequest request) {
        final String port        = request.getServerPort() == 80 ? "" : ":" + request.getServerPort();
        final String servletPath = StringUtils.defaultIfBlank(request.getContextPath(), "");
        return String.format("%s://%s%s%s", request.getScheme(), request.getServerName(), port, servletPath);
    }

    /**
     * Tries to get authentication credentials from the submitted request object. It tries first to get the
     * <b>username</b> and <b>password</b> parameters from the request. If those aren't found, it tries to get the
     * <b>username</b> and <b>password</b> parameters. If those aren't found, it looks for the <b>Authorization</b>
     * header, which is used to pass encoded authentication credentials for basic authentication operations.
     *
     * Note that this method always returns a value, even though it may not have found any credentials in the request!
     *
     * @param request The servlet request from which credentials should be extracted.
     *
     * @return Returns a pair object, with the username on the left and the password on the right.
     *
     * @throws ParseException When an improperly formatted basic authentication header is found.
     */
    public static Pair<String, String> getCredentials(final HttpServletRequest request) throws ParseException {
        try {
            return getCredentials(request, null);
        } catch (AliasTokenException ignored) {
            // This method doesn't check for an alias token, so this won't happen.
            return new MutablePair<>();
        }
    }

    private static Pair<String, String> getCredentialsNoExceptions(final HttpServletRequest request) {
        try {
            return getCredentials(request, null);
        } catch (AliasTokenException|ParseException ignored) {
            return new MutablePair<>();
        }
    }

    /**
     * Tries to get authentication credentials from the submitted request object. It tries first to get the
     * <b>username</b> and <b>password</b> parameters from the request. If those aren't found, it tries to get the
     * <b>username</b> and <b>password</b> parameters. If those aren't found, it looks for the <b>Authorization</b>
     * header, which is used to pass encoded authentication credentials for basic authentication operations.
     *
     * If the username and password are found in the basic authentication header and an instance of the {@link
     * AliasTokenService} is passed in, the username is tested to see if it matches the alias token format. If so, the
     * corresponding alias token is retrieved if it exists. If not, the {@link AliasTokenException} is thrown.
     *
     * Note that this method always returns a value, even though it may not have found any credentials in the request!
     *
     * @param request The servlet request from which credentials should be extracted.
     *
     * @return Returns a pair object, with the username on the left and the password on the right.
     *
     * @throws ParseException      When an improperly formatted basic authentication header is found.
     * @throws AliasTokenException When the requested alias token can't be found.
     */
    public static Pair<String, String> getCredentials(final HttpServletRequest request, final AliasTokenService service) throws ParseException, AliasTokenException {
        final Pair<String, String> credentials = ObjectUtils.defaultIfNull(getBasicAuthCredentials(request), getFormCredentials(request));

        if (credentials == null) {
            return new MutablePair<>();
        }

        if (service == null) {
            return credentials;
        }

        if (StringUtils.isNotBlank(credentials.getLeft()) && AliasToken.isAliasFormat(credentials.getLeft())) {
            final AliasToken alias = service.locateToken(credentials.getLeft());
            if (alias == null) {
                throw new AliasTokenException(credentials.getLeft());
            }
            return new ImmutablePair<>(alias.getXdatUserId(), credentials.getRight());
        }

        if (StringUtils.isBlank(credentials.getLeft())) {
            log.info("No username found");
        }

        return credentials;
    }

    @Nullable
    public static Pair<String, String> getBasicAuthCredentials(final HttpServletRequest request) throws ParseException {
        // See if there's an authorization header.
        final String header = request.getHeader("Authorization");
        if (StringUtils.startsWith(header, "Basic ")) {
            try {
                final String encoding = StringUtils.defaultIfBlank(request.getCharacterEncoding(), "UTF-8");
                final String token    = new String(Base64.decode(header.substring(6).getBytes(encoding)), encoding);

                if (token.contains(":")) {
                    final String[] tokens = token.split(":", 2);
                    log.debug("Basic authentication header found for user '{}'", tokens[0]);
                    return new ImmutablePair<>(tokens[0], tokens[1]);
                } else {
                    throw new ParseException("A basic authentication header was found but appears to be improperly formatted (no ':' delimiter found): " + token, 0);
                }
            } catch (UnsupportedEncodingException exception) {
                log.error("Encoding exception on authentication attempt", exception);
            }
        }
        return null;
    }

    @Nullable
    public static Pair<String, String> getFormCredentials(final HttpServletRequest request) {
        final String username = StringUtils.defaultIfBlank(request.getParameter("username"), request.getParameter("j_username"));
        final String password = StringUtils.defaultIfBlank(request.getParameter("password"), request.getParameter("j_password"));

        // If we found a username...
        if (StringUtils.isNotBlank(username)) {
            // Then we'll return that.
            log.debug("Username parameter found for user '{}'", username);
            return new ImmutablePair<>(username, password);
        }

        return null;
    }

    @Nonnull
    public static String buildArchiveEventId(final @Nullable String project, final @Nullable String timestamp, final @Nonnull String session) {
        // JAVA8: This should be a default method implementation on the ArchiveOperationListener interface, probably as toString().
        return StringUtils.isNotBlank(project) ? StringUtils.joinWith("/", project, timestamp, session) : session;
    }

    /**
     * Completes actions which happen after a user logs into the application.
     *
     * @param request
     * @param template
     * @throws IOException
     * @throws ServletException
     */
    public static void onAuthenticationSuccess(final HttpServletRequest request, final NamedParameterJdbcTemplate template) {
        try {
            Users.recordUserLogin(request);
        } catch (XFTInitException e) {
            log.error("An error occurred accessing XFT", e);
        } catch (ElementNotFoundException e) {
            log.error("Could not find the requested element {}", e.ELEMENT, e);
        } catch (InvalidValueException e) {
            log.error("An invalid value was submitted when creating the user login object", e);
        } catch (FieldNotFoundException e) {
            log.error("The field {} was not found when creating the user login object of type {}", e.FIELD, XdatUserLogin.SCHEMA_ELEMENT_NAME);
        } catch (Exception e) {
            log.error("An unknown error was found", e);
        }

        checkAccountUpgrades(request, template);
    }


    /**
     * Checks if any upgrades are required to the user's account, e.g. upgrading password encryption.
     *
     * @param request The request for user authentication.
     * @param template JDBC template for query executions
     */
    private static void checkAccountUpgrades(final HttpServletRequest request, final NamedParameterJdbcTemplate template) {
        // The login method may be null in some cases, specifically basic auth, but that indicates localdb or alias token auth.
        final String loginMethod = StringUtils.defaultIfBlank(request.getParameter(PARAM_LOGIN_METHOD), XdatUserAuthService.LOCALDB);
        final String username = getCredentialsNoExceptions(request).getLeft();

        // We have to have a username to upgrade a password (not having one may indicate e.g., oauth login)
        if (StringUtils.isNotBlank(username) && StringUtils.equals(XdatUserAuthService.LOCALDB, loginMethod)) {
            if (shouldUpgradePassword(username,template)) {
                log.debug("It seems I should upgrade the password for user {}", username);
                final String password = StringUtils.getIfBlank(request.getParameter(PARAM_PASSWORD), () -> {
                    log.debug("Couldn't find password on the authentication request, checking for basic auth credentials instead.");
                    try {
                        final Pair<String, String> credentials = XnatHttpUtils.getCredentials(request);
                        if (credentials == null) {
                            log.warn("Couldn't find password for user {} on the authentication request or in basic auth credentials, so I can't upgrade that password", username);
                        } else if (StringUtils.equals(username, credentials.getKey())) {
                            return credentials.getValue();
                        } else {
                            log.warn("I found credentials for user {} in the basic auth credentials, but that doesn't match the login username {}", credentials.getKey(), username);
                        }
                    } catch (ParseException e) {
                        log.error("An error occurred trying to parse the user credentials from basic auth header.", e);
                    }
                    return null;
                });
                if (StringUtils.isBlank(password)) {
                    log.warn("I'm trying to upgrade the password for user {} but I can't find a password to store", username);
                    return;
                }
                final int affected = template.update(QUERY_UPGRADE_PASSWORD, new MapSqlParameterSource(PARAM_USERNAME, username).addValue(PARAM_PASSWORD, Users.encode(password)));
                if (affected == 0) {
                    log.warn("Tried to upgrade password encoding for user {} but query said no rows were affected", username);
                } else {
                    if (affected > 1) {
                        log.warn("Tried to upgrade password encoding for user {} but query said {} rows were affected, which seems weird", username, affected);
                    } else {
                        log.info("I upgraded password encoding for user {}", username);
                    }
                    final int deleted = template.update(String.format(QUERY_CLEAR_CACHE_ENTRY, username), EmptySqlParameterSource.INSTANCE);
                    log.debug("Deleted {} cache entries for user {}", deleted, username);
                }
            }
        }
    }


    private static boolean shouldUpgradePassword(final String username,final NamedParameterJdbcTemplate template) {
        try {
            return template.queryForObject(QUERY_UPGRADE_NEEDED, new MapSqlParameterSource(PARAM_USERNAME, username), Boolean.class);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Checked for password encoding upgrade required, but couldn't find the username {}", username);
            return false;
        }
    }
}
