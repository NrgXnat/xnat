/*
 * core: org.nrg.xdat.turbine.modules.actions.SecureActionCsrfTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.InvalidCsrfException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SecureAction#isCsrfTokenOk(javax.servlet.http.HttpServletRequest, boolean)} for requests from a browser
 * user agent: a request authenticated by a bearer token with no HTTP session skips the check unless it is a write that
 * carries an Origin header, and every request with a session or without a bearer header is checked as before.
 */
public class SecureActionCsrfTest {
    private static final String BROWSER    = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";
    private static final String CSRF_TOKEN = "session-csrf-token";

    private MockedStatic<XDAT>    _xdat;
    private MockedStatic<Users>   _users;
    private SiteConfigPreferences _siteConfig;

    @Before
    public void setUp() {
        _siteConfig = mock(SiteConfigPreferences.class);
        when(_siteConfig.getEnableCsrfToken()).thenReturn(true);
        _xdat = mockStatic(XDAT.class);
        _xdat.when(XDAT::getSiteConfigPreferences).thenReturn(_siteConfig);
        _users = mockStatic(Users.class);
    }

    @After
    public void tearDown() {
        _users.close();
        _xdat.close();
    }

    @Test
    public void bearerWithoutSessionSkipsTheCheckAndCreatesNoSession() throws InvalidCsrfException {
        final MockHttpServletRequest request = browserRequest("GET", "Bearer abc.def.ghi");
        assertThat(SecureAction.isCsrfTokenOk(request, false)).isTrue();
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    public void bearerWithoutSessionSkipsTheCheckForAWriteWithoutOrigin() throws InvalidCsrfException {
        assertThat(SecureAction.isCsrfTokenOk(browserRequest("POST", "bearer abc.def.ghi"), false)).isTrue();
    }

    @Test
    public void bearerWithoutSessionIsCheckedForAWriteWithOrigin() {
        for (final String method : new String[]{"POST", "PUT", "DELETE"}) {
            final MockHttpServletRequest request = browserRequest(method, "Bearer abc.def.ghi");
            request.addHeader("Origin", "https://attacker.example");
            assertThatThrownBy(() -> SecureAction.isCsrfTokenOk(request, false)).as(method).isInstanceOf(InvalidCsrfException.class);
        }
    }

    @Test
    public void bearerWithoutSessionSkipsTheCheckForAReadWithOrigin() throws InvalidCsrfException {
        final MockHttpServletRequest request = browserRequest("GET", "Bearer abc.def.ghi");
        request.addHeader("Origin", "https://app.example");
        assertThat(SecureAction.isCsrfTokenOk(request, false)).isTrue();
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    public void bearerWithSessionIsChecked() {
        final MockHttpServletRequest request = withSession(browserRequest("POST", "Bearer abc.def.ghi"));
        assertThatThrownBy(() -> SecureAction.isCsrfTokenOk(request, false)).isInstanceOf(InvalidCsrfException.class);
    }

    @Test
    public void bearerWithSessionPassesWithTheSessionToken() throws InvalidCsrfException {
        final MockHttpServletRequest request = withSession(browserRequest("POST", "Bearer abc.def.ghi"));
        request.setParameter("XNAT_CSRF", CSRF_TOKEN);
        assertThat(SecureAction.isCsrfTokenOk(request, false)).isTrue();
    }

    @Test
    public void noBearerWithoutSessionIsChecked() {
        assertThatThrownBy(() -> SecureAction.isCsrfTokenOk(browserRequest("GET", null), false)).isInstanceOf(InvalidCsrfException.class);
    }

    @Test
    public void noBearerWithSessionIsChecked() {
        final MockHttpServletRequest request = withSession(browserRequest("POST", null));
        assertThatThrownBy(() -> SecureAction.isCsrfTokenOk(request, false)).isInstanceOf(InvalidCsrfException.class);
    }

    @Test
    public void basicWithoutSessionIsChecked() {
        assertThatThrownBy(() -> SecureAction.isCsrfTokenOk(browserRequest("GET", "Basic dXNlcjpwYXNz"), false)).isInstanceOf(InvalidCsrfException.class);
    }

    @Test
    public void disabledCsrfPassesEveryRequest() throws InvalidCsrfException {
        when(_siteConfig.getEnableCsrfToken()).thenReturn(false);
        assertThat(SecureAction.isCsrfTokenOk(withSession(browserRequest("POST", null)), false)).isTrue();
        assertThat(SecureAction.isCsrfTokenOk(browserRequest("POST", "Bearer abc.def.ghi"), false)).isTrue();
    }

    private static MockHttpServletRequest browserRequest(final String method, final String authorization) {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, "/data/projects");
        request.addHeader("User-Agent", BROWSER);
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        return request;
    }

    private static MockHttpServletRequest withSession(final MockHttpServletRequest request) {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute("XNAT_CSRF", CSRF_TOKEN);
        request.setSession(session);
        return request;
    }
}
