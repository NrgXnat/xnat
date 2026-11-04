/*
 * web: org.nrg.xnat.turbine.modules.actions.NextActionDispatchTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.configuration.Configuration;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.services.assemblerbroker.TurbineAssemblerBroker;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.turbine.utils.TurbineUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * The post-registration and post-login redirects send the user on to a request-supplied {@code nextAction}. That
 * action must be run through Turbine's {@code perform}, which calls its {@code isAuthorized} check first, not through a
 * direct {@code doPerform} call, which skips the check.
 */
public class NextActionDispatchTest {
    private static final String TARGET = "TargetAction";

    @Test
    public void registrationRedirectChecksTheNextActionsAuthorization() throws Exception {
        try (final Fixture fixture = new Fixture()) {
            new XDATRegisterUser().directRequest(fixture.data, fixture.context, null);

            assertThat(fixture.target.authorizationChecked).isTrue();
            assertThat(fixture.target.performed).isFalse();
        }
    }

    @Test
    public void loginRedirectChecksTheNextActionsAuthorization() throws Exception {
        try (final Fixture fixture = new Fixture()) {
            new XDATLoginUser().doRedirect(fixture.data, fixture.context, null);

            assertThat(fixture.target.authorizationChecked).isTrue();
            assertThat(fixture.target.performed).isFalse();
        }
    }

    /** An action whose authorization check always refuses, recording whether it was checked and whether it ran. */
    private static final class UnauthorizedAction extends VelocitySecureAction {
        private boolean authorizationChecked;
        private boolean performed;

        @Override
        protected boolean isAuthorized(final RunData data) {
            authorizationChecked = true;
            return false;
        }

        @Override
        public void doPerform(final RunData data, final Context context) {
            performed = true;
        }
    }

    /**
     * Mocks the statics the redirects use. Turbine's configuration and assembler broker are mocked before the action
     * loader, since its static initializer reads both. The loader returns {@link UnauthorizedAction} for
     * {@link #TARGET} and runs Turbine's own {@code exec}.
     */
    private static final class Fixture implements AutoCloseable {
        private final MockedStatic<Turbine>                turbine;
        private final MockedStatic<TurbineAssemblerBroker> assemblerBroker;
        private final MockedStatic<ActionLoader>           actionLoader;
        private final MockedStatic<XDAT>                   xdat;
        private final MockedStatic<TurbineUtils>           turbineUtils;

        private final RunData            data    = mock(RunData.class);
        private final Context            context = mock(Context.class);
        private final UnauthorizedAction target;

        private Fixture() throws Exception {
            turbine = mockStatic(Turbine.class);
            final Configuration configuration = mock(Configuration.class);
            turbine.when(Turbine::getConfiguration).thenReturn(configuration);
            when(configuration.getBoolean(any(), anyBoolean())).thenReturn(false);
            assemblerBroker = mockStatic(TurbineAssemblerBroker.class);

            target = new UnauthorizedAction();
            final ActionLoader loader = mock(ActionLoader.class);
            doReturn(target).when(loader).getInstance(TARGET);
            doCallRealMethod().when(loader).exec(data, TARGET);
            actionLoader = mockStatic(ActionLoader.class);
            actionLoader.when(() -> ActionLoader.getInstance()).thenReturn(loader);

            // Registration is open with no email verification, so the registration redirect follows nextAction.
            final SiteConfigPreferences preferences = mock(SiteConfigPreferences.class);
            when(preferences.getUserRegistration()).thenReturn(true);
            when(preferences.getEmailVerification()).thenReturn(false);
            xdat = mockStatic(XDAT.class);
            xdat.when(XDAT::getSiteConfigPreferences).thenReturn(preferences);
            xdat.when(XDAT::getContextService).thenReturn(mock(ContextService.class));

            turbineUtils = mockStatic(TurbineUtils.class);
            turbineUtils.when(() -> TurbineUtils.GetPassedParameter("nextAction", data)).thenReturn(TARGET);
            // No par on the request or session, and no saved request, so the registration redirect skips the PAR.
            when(data.getSession()).thenReturn(mock(HttpSession.class));
            when(data.getRequest()).thenReturn(mock(HttpServletRequest.class));
            when(data.getScreenTemplate()).thenReturn("Index.vm");
        }

        @Override
        public void close() {
            turbineUtils.close();
            xdat.close();
            actionLoader.close();
            assemblerBroker.close();
            turbine.close();
        }
    }
}
