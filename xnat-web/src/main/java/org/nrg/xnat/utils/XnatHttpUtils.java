/*
 * web: org.nrg.xnat.utils.XnatHttpUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xnat.security.alias.AliasTokenException;
import org.springframework.security.crypto.codec.Base64;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

@Slf4j
public class XnatHttpUtils {
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
        final String username = StringUtils.defaultIfBlank(request.getParameter("username"), request.getParameter("j_username"));
        final String password = StringUtils.defaultIfBlank(request.getParameter("password"), request.getParameter("j_password"));

        final MutablePair<String, String> credentials = new MutablePair<>();

        // If we found a username...
        if (StringUtils.isNotBlank(username)) {
            // Then we'll return that.
            credentials.setLeft(username);
            credentials.setRight(password);
            log.debug("Username parameter found for user '{}'", credentials.getLeft());
            return credentials;
        }

        // See if there's an authorization header.
        final String header = request.getHeader("Authorization");
        if (StringUtils.startsWith(header, "Basic ")) {
            try {
                final String encoding = StringUtils.defaultIfBlank(request.getCharacterEncoding(), "UTF-8");
                final String token    = new String(Base64.decode(header.substring(6).getBytes(encoding)), encoding);

                if (token.contains(":")) {
                    final String[] tokens = token.split(":", 2);
                    credentials.setLeft(tokens[0]);
                    credentials.setRight(tokens[1]);
                    log.debug("Basic authentication header found for user '{}'", credentials.getLeft());
                } else {
                    throw new ParseException("A basic authentication header was found but appears to be improperly formatted (no ':' delimiter found): " + token, 0);
                }
            } catch (UnsupportedEncodingException exception) {
                log.error("Encoding exception on authentication attempt", exception);
            }
        }

        if (service != null && StringUtils.isNotBlank(credentials.getLeft()) && AliasToken.isAliasFormat(credentials.getLeft())) {
            final AliasToken alias = service.locateToken(credentials.getLeft());
            if (alias == null) {
                throw new AliasTokenException(credentials.getLeft());
            }
            credentials.setLeft(alias.getXdatUserId());
        }

        if (StringUtils.isBlank(credentials.getLeft())) {
            log.info("No username found");
        }

        return credentials;
    }
}
