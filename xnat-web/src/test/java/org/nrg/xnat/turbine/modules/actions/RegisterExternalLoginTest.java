/*
 * web: org.nrg.xnat.turbine.modules.actions.RegisterExternalLoginTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.configuration.Configuration;
import org.apache.turbine.Turbine;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.exceptions.UsernameAuthMappingNotFoundException;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.InvalidCsrfException;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.security.XnatProviderManager;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RegisterExternalLogin}. The action needs the {@link UsernameAuthMappingNotFoundException} that
 * the login filter stores on the session, takes the external identity from it, removes it once an account is created
 * or linked, and applies the provider's auto-verify only when the submitted email matches its email.
 */
public class RegisterExternalLoginTest {

    private static final String ATTR = UsernameAuthMappingNotFoundException.class.getSimpleName();

    private static UsernameAuthMappingNotFoundException sampleException() {
        return new UsernameAuthMappingNotFoundException("extuser", "ldap", "ldap1", "e@x.org", "Last", "First");
    }

    // --- Session lookup: only a session attribute of the right type counts (the present case runs in every doPerform
    //     fixture test through withProof) ---

    @Test
    public void returnsNullWhenSessionHasNoAttribute() {
        final HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(ATTR)).thenReturn(null);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(session);

        assertThat(RegisterExternalLogin.getVerifiedExternalAuth(request)).isNull();
    }

    @Test
    public void returnsNullWhenAttributeIsWrongType() {
        final HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(ATTR)).thenReturn("not-an-exception");
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(session);

        assertThat(RegisterExternalLogin.getVerifiedExternalAuth(request)).isNull();
    }

    // --- Email matching for the provider's auto-verify (the doPerformRegister...AutoVerify... tests below cover the
    //     match and mismatch through doPerform) ---

    @Test
    public void emailMatchesProofIgnoringCaseAndWhitespace() {
        final UsernameAuthMappingNotFoundException proof =
                new UsernameAuthMappingNotFoundException("u", "ldap", "ldap1", "User@Example.ORG", "L", "F");
        assertThat(RegisterExternalLogin.submittedEmailMatchesProof("  user@example.org ", proof)).isTrue();
    }

    @Test
    public void emailDoesNotMatchWhenProofHasNoEmail() {
        final UsernameAuthMappingNotFoundException proof =
                new UsernameAuthMappingNotFoundException("u", "ldap", "ldap1", null, "L", "F");
        assertThat(RegisterExternalLogin.submittedEmailMatchesProof("whatever@example.org", proof)).isFalse();
    }

    // --- doPerform without an external login in the session ---

    @Test
    public void doPerformWithoutProofRedirectsToLoginAndDoesNotRegister() throws Exception {
        try (final MockedStatic<XDAT> xdat = mockStatic(XDAT.class);
             final MockedStatic<TurbineUtils> turbine = mockStatic(TurbineUtils.class);
             final MockedStatic<Turbine> turbineFramework = mockStatic(Turbine.class)) {
            // Satisfy the Turbine action base constructor's configuration lookup.
            final Configuration configuration = mock(Configuration.class);
            turbineFramework.when(Turbine::getConfiguration).thenReturn(configuration);
            when(configuration.getBoolean(any(), anyBoolean())).thenReturn(false);
            // Satisfy the constructor's bean/preference lookups.
            final ContextService contextService = mock(ContextService.class);
            xdat.when(XDAT::getContextService).thenReturn(contextService);
            when(contextService.getBean(XdatUserAuthService.class)).thenReturn(mock(XdatUserAuthService.class));
            when(contextService.getBean(XnatDatabaseAuthenticationProvider.class)).thenReturn(mock(XnatDatabaseAuthenticationProvider.class));
            when(contextService.getBean(XnatProviderManager.class)).thenReturn(mock(XnatProviderManager.class));
            xdat.when(XDAT::getSiteConfigPreferences).thenReturn(mock(SiteConfigPreferences.class));

            final RegisterExternalLogin action = new RegisterExternalLogin();

            final HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getSession(false)).thenReturn(null); // no external login in the session
            final RunData data = mock(RunData.class);
            when(data.getRequest()).thenReturn(request);
            final Context context = mock(Context.class);

            action.doPerform(data, context);

            // Redirected to login without reading the operation.
            turbine.verify(() -> TurbineUtils.redirectToLogin(data));
            turbine.verify(() -> TurbineUtils.GetPassedParameter(eq("operation"), any()), never());
        }
    }

    // --- doPerform with an external login in the session ---

    @Test
    public void doPerformMergeWithInvalidCsrfTokenIsRefusedBeforeLinkingOrLogin() throws Exception {
        try (final ActionFixture fixture = new ActionFixture()) {
            fixture.withProof(sampleException()).withParameter("operation", "merge").withParameter("username", "existing").withParameter("password", "secret");
            fixture.secureAction.when(() -> SecureAction.isCsrfTokenOk(fixture.data)).thenThrow(new InvalidCsrfException("bad token", null));

            fixture.action.doPerform(fixture.data, fixture.context);

            verify(fixture.databaseProvider, never()).authenticate(any());
            verify(fixture.userAuthService, never()).create(any(XdatUserAuth.class));
            fixture.xdat.verify(() -> XDAT.loginUser(any(RunData.class), any(), anyBoolean()), never());
            verify(fixture.data).setScreenTemplate("Login.vm");
        }
    }

    @Test
    public void doPerformMergeLinksTheSessionIdentityNotTheRequestAndConsumesProof() throws Exception {
        try (final ActionFixture fixture = new ActionFixture()) {
            // The request names a different identity; the linked auth record carries the session's (extuser / ldap /
            // ldap1).
            fixture.withProof(sampleException()).withParameter("operation", "merge").withParameter("username", "existing").withParameter("password", "secret")
                   .withParameter("authUsername", "other").withParameter("authMethod", "localdb").withParameter("authMethodId", "localdb");
            final Authentication authentication = mock(Authentication.class);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(fixture.databaseProvider.authenticate(any())).thenReturn(authentication);

            fixture.action.doPerform(fixture.data, fixture.context);

            verify(fixture.userAuthService).create(argThat((XdatUserAuth auth) ->
                    "extuser".equals(auth.getAuthUser()) && "ldap".equals(auth.getAuthMethod()) && "ldap1".equals(auth.getAuthMethodId())));
            verify(fixture.session).removeAttribute(ATTR);
        }
    }

    @Test
    public void doPerformRegisterConsumesProofOnceAccountIsCreated() throws Exception {
        try (final ActionFixture fixture = new ActionFixture()) {
            fixture.withProof(sampleException()).withParameter("operation", "register").withParameter("xdat:user.login", "newuser");
            fixture.users.when(() -> Users.getUser("newuser")).thenReturn(null, mock(UserI.class));

            fixture.action.doPerform(fixture.data, fixture.context);

            verify(fixture.session).removeAttribute(ATTR);
        }
    }

    @Test
    public void doPerformRegisterKeepsProofWhenNoAccountIsCreated() throws Exception {
        try (final ActionFixture fixture = new ActionFixture()) {
            fixture.withProof(sampleException()).withParameter("operation", "register").withParameter("xdat:user.login", "newuser");
            fixture.users.when(() -> Users.getUser("newuser")).thenReturn(null);

            fixture.action.doPerform(fixture.data, fixture.context);

            verify(fixture.session, never()).removeAttribute(ATTR);
        }
    }

    @Test
    public void doPerformRegisterDropsProviderAutoVerifyWhenEmailDiffersFromProof() throws Exception {
        try (final ActionFixture fixture = new ActionFixture()) {
            fixture.withProof(sampleException()).withAutoVerifyingProvider().withParameter("operation", "register").withParameter("xdat:user.email", "other@example.org");

            fixture.action.doPerform(fixture.data, fixture.context);

            verify(fixture.context).put("providerAutoVerified", false);
        }
    }

    @Test
    public void doPerformRegisterHonorsProviderAutoVerifyWhenEmailMatchesProof() throws Exception {
        try (final ActionFixture fixture = new ActionFixture()) {
            fixture.withProof(sampleException()).withAutoVerifyingProvider().withParameter("operation", "register").withParameter("xdat:user.email", "e@x.org");

            fixture.action.doPerform(fixture.data, fixture.context);

            verify(fixture.context).put("providerAutoVerified", true);
        }
    }

    @Test
    public void doPerformWithNonPostMethodIsRefusedBeforeLinkingOrLogin() throws Exception {
        try (final ActionFixture fixture = new ActionFixture()) {
            // The CSRF check validates the token only for POST, PUT and DELETE, so a GET is refused before linking.
            fixture.withProof(sampleException()).withParameter("operation", "merge").withParameter("username", "existing").withParameter("password", "secret");
            when(fixture.request.getMethod()).thenReturn("GET");

            fixture.action.doPerform(fixture.data, fixture.context);

            verify(fixture.databaseProvider, never()).authenticate(any());
            verify(fixture.userAuthService, never()).create(any(XdatUserAuth.class));
            fixture.xdat.verify(() -> XDAT.loginUser(any(RunData.class), any(), anyBoolean()), never());
        }
    }

    /**
     * Builds a {@link RegisterExternalLogin} with its static collaborators mocked. The base registration workflow is
     * stubbed out so each test observes only what RegisterExternalLogin itself decides.
     */
    private static final class ActionFixture implements AutoCloseable {
        private final MockedStatic<XDAT>         xdat             = mockStatic(XDAT.class);
        private final MockedStatic<TurbineUtils> turbine          = mockStatic(TurbineUtils.class);
        private final MockedStatic<Turbine>      turbineFramework = mockStatic(Turbine.class);
        private final MockedStatic<SecureAction> secureAction     = mockStatic(SecureAction.class);
        private final MockedStatic<Users>        users            = mockStatic(Users.class);

        private final XdatUserAuthService                userAuthService  = mock(XdatUserAuthService.class);
        private final XnatDatabaseAuthenticationProvider databaseProvider = mock(XnatDatabaseAuthenticationProvider.class);
        private final XnatProviderManager                providerManager  = mock(XnatProviderManager.class);

        private final HttpSession        session = mock(HttpSession.class);
        private final HttpServletRequest request = mock(HttpServletRequest.class);
        private final RunData            data    = mock(RunData.class);
        private final Context            context = mock(Context.class);

        private final RegisterExternalLogin action;

        private ActionFixture() throws Exception {
            final Configuration configuration = mock(Configuration.class);
            turbineFramework.when(Turbine::getConfiguration).thenReturn(configuration);
            when(configuration.getBoolean(any(), anyBoolean())).thenReturn(false);

            final ContextService contextService = mock(ContextService.class);
            xdat.when(XDAT::getContextService).thenReturn(contextService);
            when(contextService.getBean(XdatUserAuthService.class)).thenReturn(userAuthService);
            when(contextService.getBean(XnatDatabaseAuthenticationProvider.class)).thenReturn(databaseProvider);
            when(contextService.getBean(XnatProviderManager.class)).thenReturn(providerManager);
            xdat.when(XDAT::getSiteConfigPreferences).thenReturn(mock(SiteConfigPreferences.class));

            secureAction.when(() -> SecureAction.isCsrfTokenOk(any(RunData.class))).thenReturn(true);
            // The action un-escapes the submitted email before matching it; the fixture's values are not escaped.
            turbine.when(() -> TurbineUtils.unescapeParam(any())).thenAnswer(invocation -> invocation.getArgument(0));

            when(request.getSession(false)).thenReturn(session);
            when(request.getMethod()).thenReturn("POST");
            when(data.getRequest()).thenReturn(request);

            action = spy(new RegisterExternalLogin());
            doNothing().when(action).runRegistrationWorkflow(any(RunData.class), any(Context.class));
        }

        private ActionFixture withProof(final UsernameAuthMappingNotFoundException proof) {
            when(session.getAttribute(ATTR)).thenReturn(proof);
            return this;
        }

        private ActionFixture withParameter(final String name, final String value) {
            turbine.when(() -> TurbineUtils.GetPassedParameter(name, data)).thenReturn(value);
            return this;
        }

        private ActionFixture withAutoVerifyingProvider() {
            final XnatAuthenticationProvider provider = mock(XnatAuthenticationProvider.class);
            when(provider.isAutoVerified()).thenReturn(true);
            when(providerManager.getProvider("ldap", "ldap1")).thenReturn(provider);
            return this;
        }

        @Override
        public void close() {
            users.close();
            secureAction.close();
            turbineFramework.close();
            turbine.close();
            xdat.close();
        }
    }
}
