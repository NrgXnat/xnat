package org.nrg.xnat.security;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xft.security.UserI;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.Cookie;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TestExternalClientAuthFilter {
    @Before
    public void setUp() {
        _tokenService = mock(AliasTokenService.class);
        _filter = new ExternalClientAuthFilter(_tokenService);
        _session = new MockHttpSession();
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testAnonymousRequestCachesFlagAndUsername() throws Exception {
        setAnonymous();
        final MockFilterChain chain = new MockFilterChain();
        final MockHttpServletRequest request = pageRequest("/");
        request.setParameter("externalAuth", "true");
        request.setParameter("username", "jdoe");

        request.setSecure(true);
        final MockHttpServletResponse response = new MockHttpServletResponse();

        _filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(response.getHeader("Set-Cookie")).startsWith("XNAT_EXTERNAL_CLIENT_AUTH=1; Path=/; Max-Age=900;").contains("Secure", "HttpOnly", "SameSite=Lax");
        assertThat(_session.getAttribute(ExternalClientAuthFilter.ATTR_EXTERNAL_USERNAME)).isEqualTo("jdoe");
        verifyNoInteractions(_tokenService);
    }

    @Test
    public void testAuthenticatedPageLoadHandsOffToken() throws Exception {
        _session.setAttribute(ExternalClientAuthFilter.ATTR_EXTERNAL_USERNAME, "jdoe");
        final UserI user = setAuthenticated("jdoe");
        final AliasToken token = mock(AliasToken.class);
        when(token.getAlias()).thenReturn("alias-123");
        when(token.getSecret()).thenReturn("secret456");
        when(_tokenService.issueTokenForUser(user)).thenReturn(token);

        final MockFilterChain chain = new MockFilterChain();
        final MockHttpServletRequest request = pendingPageRequest("/app/template/Index.vm");
        request.setScheme("https");
        request.setServerName("xnat.example.org");
        request.setServerPort(443);
        request.setContextPath("/xnat");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        _filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getContentAsString()).contains("xnats://xnat.example.org/xnat/auth?a=alias-123&amp;s=secret456");
        assertThat(response.getContentAsString()).contains("'xnats:\\/\\/xnat.example.org\\/xnat\\/auth?a=alias-123&s=secret456'");
        assertThat(response.getHeader("Set-Cookie")).startsWith("XNAT_EXTERNAL_CLIENT_AUTH=; Path=/xnat; Max-Age=0;");
        assertThat(_session.getAttribute(ExternalClientAuthFilter.ATTR_EXTERNAL_USERNAME)).isNull();
    }

    @Test
    public void testNonDefaultPortAndHttp() throws Exception {
        final UserI user = setAuthenticated("jdoe");
        final AliasToken token = mock(AliasToken.class);
        when(token.getAlias()).thenReturn("a");
        when(token.getSecret()).thenReturn("s");
        when(_tokenService.issueTokenForUser(user)).thenReturn(token);

        final MockHttpServletRequest request = pendingPageRequest("/");
        request.setServerName("localhost");
        request.setServerPort(8080);
        final MockHttpServletResponse response = new MockHttpServletResponse();

        _filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getContentAsString()).contains("href=\"xnat://localhost:8080/auth?a=a&amp;s=s\"");
    }

    @Test
    public void testXhrRequestIsNotIntercepted() throws Exception {
        setAuthenticated("jdoe");

        final MockFilterChain chain = new MockFilterChain();
        final MockHttpServletRequest request = pendingPageRequest("/xapi/users");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        _filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(response.getHeader("Set-Cookie")).isNull();
        verifyNoInteractions(_tokenService);
    }

    @Test
    public void testAuthenticatedWithoutFlagPassesThrough() throws Exception {
        setAuthenticated("jdoe");
        final MockFilterChain chain = new MockFilterChain();

        _filter.doFilter(pageRequest("/"), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(_tokenService);
    }

    @Test
    public void testHandoffAfterSessionWasReplaced() throws Exception {
        // The OpenID plugin invalidates the session when the user picks a provider, so only the cookie remains.
        final UserI user = setAuthenticated("jdoe");
        final AliasToken token = mock(AliasToken.class);
        when(token.getAlias()).thenReturn("a");
        when(token.getSecret()).thenReturn("s");
        when(_tokenService.issueTokenForUser(user)).thenReturn(token);

        final MockFilterChain chain = new MockFilterChain();
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.addHeader("Accept", "text/html");
        request.setCookies(new Cookie(ExternalClientAuthFilter.COOKIE_EXTERNAL_AUTH, "1"));
        final MockHttpServletResponse response = new MockHttpServletResponse();

        _filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getContentAsString()).contains("href=\"xnat://localhost/auth?a=a&amp;s=s\"");
    }

    @Test
    public void testAlreadyLoggedInIsHandedBackOnStartRequest() throws Exception {
        final UserI user = setAuthenticated("jdoe");
        final AliasToken token = mock(AliasToken.class);
        when(token.getAlias()).thenReturn("a");
        when(token.getSecret()).thenReturn("s");
        when(_tokenService.issueTokenForUser(user)).thenReturn(token);

        final MockFilterChain chain = new MockFilterChain();
        final MockHttpServletRequest request = pageRequest("/");
        request.setParameter("externalAuth", "true");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        _filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getHeaders("Set-Cookie")).last().asString().contains("Max-Age=0;");
    }

    private MockHttpServletRequest pendingPageRequest(final String uri) {
        final MockHttpServletRequest request = pageRequest(uri);
        request.setCookies(new Cookie(ExternalClientAuthFilter.COOKIE_EXTERNAL_AUTH, "1"));
        return request;
    }

    private MockHttpServletRequest pageRequest(final String uri) {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setSession(_session);
        request.addHeader("Accept", "text/html,application/xhtml+xml");
        return request;
    }

    private void setAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key", "guest", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    }

    private UserI setAuthenticated(final String username) {
        final UserI user = mock(UserI.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getLogin()).thenReturn(username);
        when(user.isGuest()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
        return user;
    }

    private AliasTokenService      _tokenService;
    private ExternalClientAuthFilter _filter;
    private MockHttpSession        _session;
}
