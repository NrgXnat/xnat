/*
 * web: org.nrg.xnat.security.OnXnatLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.om.XdatUserLogin;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class OnXnatLogin extends SavedRequestAwareAuthenticationSuccessHandler {
    private static final String DEFAULT_LANDING         = "/app/template/Index.vm?login=true";
    private static final String PARAM_LOGIN_METHOD      = "login_method";
    private static final String PARAM_USERNAME          = "username";
    private static final String PARAM_PASSWORD          = "password";
    private static final String QUERY_UPGRADE_NEEDED    = "SELECT primary_password ~ '^[A-Fa-f0-9]{64}\\{[A-Za-z0-9]{64}}$' FROM xdat_user WHERE login = :" + PARAM_USERNAME;
    private static final String QUERY_UPGRADE_PASSWORD  = "UPDATE xdat_user SET primary_password = :" + PARAM_PASSWORD + ", salt = null, primary_password_encrypt = 1 WHERE login = :" + PARAM_USERNAME;
    private static final String QUERY_CLEAR_CACHE_ENTRY = "DELETE FROM xs_item_cache WHERE contents LIKE 'Item:(0(xdat:user)((login:string)=(%s)%%'";

    private final NamedParameterJdbcTemplate _template;

    public OnXnatLogin(final NamedParameterJdbcTemplate template) {
        _template = template;
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException, ServletException {
        log.debug("Request is to process authentication");

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

        if (StringUtils.equals(XdatUserAuthService.LOCALDB, request.getParameter(PARAM_LOGIN_METHOD))) {
            final String username = request.getParameter(PARAM_USERNAME);
            if (shouldUpgradePassword(username)) {
                log.debug("It seems I should upgrade the password for user {}", username);
                final int affected = _template.update(QUERY_UPGRADE_PASSWORD, new MapSqlParameterSource(PARAM_USERNAME, username).addValue(PARAM_PASSWORD, Users.encode(request.getParameter(PARAM_PASSWORD))));
                if (affected == 0) {
                    log.warn("Tried to upgrade password encoding for user {} but query said no rows were affected", username);
                } else {
                    if (affected > 1) {
                        log.warn("Tried to upgrade password encoding for user {} but query said {} rows were affected, which seems weird", username, affected);
                    } else {
                        log.info("I upgraded password encoding for user {}", username);
                    }
                    final int deleted = _template.update(String.format(QUERY_CLEAR_CACHE_ENTRY, username), EmptySqlParameterSource.INSTANCE);
                    log.debug("Deleted {} cache entries for user {}", deleted, username);
                }
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(final HttpServletRequest request, final HttpServletResponse response) {
        // TODO: Isn't there an override for the default landing page?
        final String  url           = getDefaultTargetUrl();
        final boolean isRootDefault = "/".equals(url);

        if (isRootDefault) {
            setDefaultTargetUrl(DEFAULT_LANDING);
        }

        // If the app root is the default, go to the default landing, otherwise figure it out.
        return isRootDefault ? DEFAULT_LANDING : super.determineTargetUrl(request, response);
    }

    private boolean shouldUpgradePassword(final String username) {
        return _template.queryForObject(QUERY_UPGRADE_NEEDED, new MapSqlParameterSource(PARAM_USERNAME, username), Boolean.class);
    }
}
