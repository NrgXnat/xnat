/*
 * web: org.nrg.xnat.security.ExternalClientAuthFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xft.security.UserI;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Supports browser-based login for external clients that handle the xnat:// and xnats:// protocols, such as the
 * XNAT Desktop Client. The client opens the system browser to any XNAT page
 * with <b>externalAuth=true</b> (and optionally <b>username</b>, which pre-populates the login form). This filter
 * records that request in a short-lived cookie. The first page the user loads once authenticated, by whatever
 * provider, is replaced with a page that issues an alias token and hands it back to the client through the
 * xnat(s):// protocol:
 *
 * <pre>xnat(s)://host[:port]/context/auth?a=alias&amp;s=secret</pre>
 *
 * This is the same alias/secret pair the "Download Via App" feature passes to the client. A user who is already
 * logged in is handed back immediately.
 * <p>
 * The flag is a cookie rather than a session attribute because some login flows discard the session: the OpenID
 * plugin invalidates it when the user first clicks a provider, before redirecting to that provider. SameSite=Lax
 * still sends the cookie when the provider redirects back to XNAT. The username is only needed on XNAT's own login
 * page, before any such redirect, so it stays in the session.
 */
@Slf4j
public class ExternalClientAuthFilter extends OncePerRequestFilter {
    public static final String PARAM_EXTERNAL_AUTH   = "externalAuth";
    public static final String PARAM_USERNAME        = "username";
    public static final String COOKIE_EXTERNAL_AUTH   = "XNAT_EXTERNAL_CLIENT_AUTH";
    public static final String ATTR_EXTERNAL_USERNAME = "xnat.externalClient.externalAuthUsername";
    public static final int    COOKIE_MAX_AGE_SECONDS = 15 * 60;

    public ExternalClientAuthFilter(final AliasTokenService aliasTokenService) {
        _aliasTokenService = aliasTokenService;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws ServletException, IOException {
        // Checked before reading any parameter: on a form-encoded POST, getParameter() parses and consumes the body,
        // leaving nothing for the resource behind this filter. YUI posts search XML that way, for one.
        if (!isPageRequest(request)) {
            chain.doFilter(request, response);
            return;
        }

        // The cookie set here isn't on this request, so a user who is already logged in is handed back on this one.
        final boolean isStart = StringUtils.equalsIgnoreCase(request.getParameter(PARAM_EXTERNAL_AUTH), "true");
        if (isStart) {
            setPendingCookie(request, response, COOKIE_MAX_AGE_SECONDS);
            final HttpSession session  = request.getSession(true);
            final String      username = StringUtils.trimToNull(request.getParameter(PARAM_USERNAME));
            if (username != null) {
                session.setAttribute(ATTR_EXTERNAL_USERNAME, username);
            } else {
                session.removeAttribute(ATTR_EXTERNAL_USERNAME);
            }
        }

        if (!(isStart || hasPendingCookie(request))) {
            chain.doFilter(request, response);
            return;
        }

        // Check for an anonymous login before asking XDAT, which would look up the guest user.
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            chain.doFilter(request, response);
            return;
        }

        final UserI user = XDAT.getUserDetails();
        if (user == null || user.isGuest()) {
            chain.doFilter(request, response);
            return;
        }

        setPendingCookie(request, response, 0);
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(ATTR_EXTERNAL_USERNAME);
        }

        final AliasToken token;
        try {
            token = _aliasTokenService.issueTokenForUser(user);
        } catch (Exception e) {
            log.error("Couldn't issue an alias token to hand user {} back to an external client", user.getUsername(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not issue a token for the external client.");
            return;
        }

        log.info("Handing user {} back to an external client", user.getUsername());
        writeHandoffPage(response, getCallbackUrl(request, token));
    }

    private static boolean hasPendingCookie(final HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if (COOKIE_EXTERNAL_AUTH.equals(cookie.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Written as a header because the servlet Cookie API can't set SameSite. A max age of 0 deletes the cookie.
    private static void setPendingCookie(final HttpServletRequest request, final HttpServletResponse response, final int maxAge) {
        final String path = StringUtils.defaultIfBlank(request.getContextPath(), "/");
        response.addHeader("Set-Cookie", COOKIE_EXTERNAL_AUTH + "=" + (maxAge > 0 ? "1" : "") + "; Path=" + path + "; Max-Age=" + maxAge
                                         + "; HttpOnly; SameSite=Lax" + (request.isSecure() ? "; Secure" : ""));
    }

    // Only intercept browser page loads, not XHR calls made by a page that loaded before the user logged in.
    private static boolean isPageRequest(final HttpServletRequest request) {
        return StringUtils.equalsIgnoreCase(request.getMethod(), "GET")
               && StringUtils.contains(request.getHeader("Accept"), "text/html")
               && !StringUtils.equalsIgnoreCase(request.getHeader("X-Requested-With"), "XMLHttpRequest");
    }

    private static String getCallbackUrl(final HttpServletRequest request, final AliasToken token) {
        final boolean secure      = StringUtils.equalsIgnoreCase(request.getScheme(), "https");
        final int     port        = request.getServerPort();
        final boolean defaultPort = port <= 0 || port == (secure ? 443 : 80);
        return (secure ? "xnats" : "xnat") + "://" + request.getServerName() + (defaultPort ? "" : ":" + port)
               + StringUtils.defaultString(request.getContextPath()) + "/auth"
               + "?a=" + URLEncoder.encode(token.getAlias(), StandardCharsets.UTF_8)
               + "&s=" + URLEncoder.encode(token.getSecret(), StandardCharsets.UTF_8);
    }

    private static void writeHandoffPage(final HttpServletResponse response, final String callbackUrl) throws IOException {
        final String htmlUrl = StringEscapeUtils.escapeHtml4(callbackUrl);
        final String jsUrl   = StringEscapeUtils.escapeEcmaScript(callbackUrl);

        // The page carries the token secret, so it must not be cached or leak through a referrer.
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.getWriter().write("<!DOCTYPE html>\n"
                                   + "<html><head><meta charset=\"UTF-8\"><title>Returning to your application</title>\n"
                                   + "<style>body{font-family:sans-serif;margin:4em auto;max-width:36em;text-align:center;color:#333}"
                                   + "a.btn{display:inline-block;margin-top:1em;padding:.6em 1.2em;background:#1a75bb;color:#fff;border-radius:4px;text-decoration:none}</style>\n"
                                   + "</head><body>\n"
                                   + "<h2>You're logged in</h2>\n"
                                   + "<p>Returning you to the application that started this login. If your browser asks, allow it to open the application.</p>\n"
                                   + "<p><a class=\"btn\" href=\"" + htmlUrl + "\">Open application</a></p>\n"
                                   + "<p>Once the app has opened, you can close this tab.</p>\n"
                                   + "<script>window.location.href = '" + jsUrl + "';</script>\n"
                                   + "</body></html>\n");
    }

    private final AliasTokenService _aliasTokenService;
}
