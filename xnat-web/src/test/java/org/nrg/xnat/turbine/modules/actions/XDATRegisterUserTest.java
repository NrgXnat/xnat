/*
 * web: org.nrg.xnat.turbine.modules.actions.XDATRegisterUserTest
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
import org.junit.Test;
import org.mockito.MockedStatic;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the project-access-request checks in {@link XDATRegisterUser}. A PAR is looked up by its guid.
 * {@link XDATRegisterUser#isPendingParGuid} only needs a pending request for the guid and is used for the
 * external-user-par-disabled switch. {@link XDATRegisterUser#hasValidPar} also needs the invited email to match the
 * registering email; it decides enable and verify, and is used for the new-user switch.
 */
public class XDATRegisterUserTest {

    private static final String GUID  = "3f2504e0-4f89-41d3-9a0c-0305e82c3301";
    private static final String EMAIL = "invited@example.org";

    private interface Body { void run(XDATRegisterUser action, RunData data, MockedStatic<ProjectAccessRequest> par); }

    /** Builds the web action with the statics it needs mocked, exposes the PAR static mock, and runs {@code body}. */
    private void withAction(final String parParam, final String emailParam, final Body body) {
        try (final MockedStatic<Turbine> turbine = mockStatic(Turbine.class);
             final MockedStatic<XDAT> xdat = mockStatic(XDAT.class);
             final MockedStatic<Users> users = mockStatic(Users.class);
             final MockedStatic<TurbineUtils> turbineUtils = mockStatic(TurbineUtils.class);
             final MockedStatic<ProjectAccessRequest> par = mockStatic(ProjectAccessRequest.class)) {
            final Configuration configuration = mock(Configuration.class);
            turbine.when(Turbine::getConfiguration).thenReturn(configuration);
            when(configuration.getBoolean(any(), anyBoolean())).thenReturn(false);
            final ContextService contextService = mock(ContextService.class);
            xdat.when(XDAT::getContextService).thenReturn(contextService);
            when(contextService.getBean(UserRegistrationDataService.class)).thenReturn(mock(UserRegistrationDataService.class));
            xdat.when(XDAT::getSiteConfigPreferences).thenReturn(mock(SiteConfigPreferences.class));
            users.when(Users::getGuest).thenReturn(mock(UserI.class));

            final RunData data = mock(RunData.class);
            when(data.getSession()).thenReturn(mock(javax.servlet.http.HttpSession.class)); // getAttribute("par") -> null
            turbineUtils.when(() -> TurbineUtils.GetPassedParameter(eq("par"), eq(data))).thenReturn(parParam);
            turbineUtils.when(() -> TurbineUtils.GetPassedParameter(eq("xdat:user.email"), eq(data))).thenReturn(emailParam);
            // The registering email is read back un-escaped for the comparison; here it has no escaped characters.
            turbineUtils.when(() -> TurbineUtils.unescapeParam(emailParam)).thenReturn(emailParam);

            body.run(new XDATRegisterUser(), data, par);
        }
    }

    private static ProjectAccessRequest par(final String guid, final String email, final Date approvalDate) {
        final ProjectAccessRequest request = mock(ProjectAccessRequest.class);
        when(request.getGuid()).thenReturn(guid);
        when(request.getEmail()).thenReturn(email);
        when(request.getApprovalDate()).thenReturn(approvalDate);
        when(request.getApproved()).thenReturn(null);
        return request;
    }

    // --- hasValidPar: a pending PAR for the submitted guid whose invited email matches the registering email ---

    @Test
    public void hasValidParFalseWhenParAlreadyApproved() {
        withAction(GUID, EMAIL, (action, data, par) -> {
            final ProjectAccessRequest approved = par(GUID, EMAIL, new Date());
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(approved);
            assertThat(action.hasValidPar(data)).isFalse();
        });
    }

    @Test
    public void hasValidParFalseWhenNoParParameter() {
        withAction(null, EMAIL, (action, data, par) -> {
            assertThat(action.hasValidPar(data)).isFalse();
            par.verify(() -> ProjectAccessRequest.RequestPARByGUID(any(), any()), never());
        });
    }

    @Test
    public void hasValidParTrueWhenInvitedEmailMatchesIgnoringCase() {
        withAction(GUID, "invited@example.org", (action, data, par) -> {
            final ProjectAccessRequest pending = par(GUID, "Invited@Example.ORG", null);
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(pending);
            assertThat(action.hasValidPar(data)).isTrue();
        });
    }

    @Test
    public void hasValidParFalseWhenInvitedEmailDiffers() {
        withAction(GUID, "other@example.org", (action, data, par) -> {
            final ProjectAccessRequest pending = par(GUID, "invited@example.org", null);
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(pending);
            assertThat(action.hasValidPar(data)).isFalse();
        });
    }

    @Test
    public void hasValidParFalseWhenNoPendingPar() {
        withAction(GUID, EMAIL, (action, data, par) -> {
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(null);
            assertThat(action.hasValidPar(data)).isFalse();
        });
    }

    // --- isPendingParGuid: used only for the external-user-par-disabled switch, and ignores the email ---

    @Test
    public void isPendingParGuidTrueRegardlessOfEmail() {
        withAction(GUID, "other@example.org", (action, data, par) -> {
            final ProjectAccessRequest pending = par(GUID, "invited@example.org", null);
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(pending);
            assertThat(action.isPendingParGuid(data)).isTrue();
            assertThat(action.hasValidPar(data)).isFalse(); // the email doesn't match, so no enable or verify
        });
    }

    @Test
    public void isPendingParGuidFalseWhenApproved() {
        withAction(GUID, EMAIL, (action, data, par) -> {
            final ProjectAccessRequest approved = par(GUID, EMAIL, new Date());
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(approved);
            assertThat(action.isPendingParGuid(data)).isFalse();
        });
    }

    // --- registrationDisallowed: each switch has its own check ---
    //   par-disabled applies when isPendingParGuid is true (guid only).
    //   new-user-disabled applies unless hasValidPar is true (guid and email).

    private static SiteConfigPreferences siteConfig(final boolean parDisabled, final boolean newUserDisabled) {
        final SiteConfigPreferences siteConfig = mock(SiteConfigPreferences.class);
        when(siteConfig.getSecurityExternalUserParDisabled()).thenReturn(parDisabled);
        when(siteConfig.getSecurityNewUserRegistrationDisabled()).thenReturn(newUserDisabled);
        return siteConfig;
    }

    @Test
    public void gateBlocksPendingGuidWithDifferentEmailWhenNewUserRegistrationDisabled() {
        // A pending guid with a different email is an ordinary registration, so the new-user switch applies.
        withAction(GUID, "other@example.org", (action, data, par) -> {
            final ProjectAccessRequest pending = par(GUID, "invited@example.org", null);
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(pending);
            assertThat(action.registrationDisallowed(data, siteConfig(false, true))).isTrue();
        });
    }

    @Test
    public void gateAllowsInvitedEmailWhenNewUserRegistrationDisabledButParEnabled() {
        // The invitee (guid and matching email) is subject only to the external-user-par switch.
        withAction(GUID, EMAIL, (action, data, par) -> {
            final ProjectAccessRequest pending = par(GUID, EMAIL, null);
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(pending);
            assertThat(action.registrationDisallowed(data, siteConfig(false, true))).isFalse();
        });
    }

    @Test
    public void gateBlocksPendingGuidWithDifferentEmailWhenParDisabled() {
        // The par-disabled switch checks the guid alone, so it applies whatever the email.
        withAction(GUID, "other@example.org", (action, data, par) -> {
            final ProjectAccessRequest pending = par(GUID, "invited@example.org", null);
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(pending);
            assertThat(action.registrationDisallowed(data, siteConfig(true, false))).isTrue();
        });
    }

    @Test
    public void gateBlocksUnknownParWhenNewUserRegistrationDisabled() {
        withAction("not-a-guid", EMAIL, (action, data, par) -> {
            assertThat(action.registrationDisallowed(data, siteConfig(false, true))).isTrue();
        });
    }

    @Test
    public void gateAllowsOpenRegistration() {
        withAction(null, EMAIL, (action, data, par) -> {
            assertThat(action.registrationDisallowed(data, siteConfig(false, false))).isFalse();
        });
    }

    // --- registrationDisallowedMessage: an invitee registering with a different email is told to use the invited email,
    //     but only when that email would be accepted ---

    @Test
    public void disallowedMessageTellsWrongEmailInviteeToUseInvitedEmail() {
        // Pending invitation, different email, new-user disabled but par enabled: the invited email would be accepted.
        withAction(GUID, "other@example.org", (action, data, par) -> {
            final ProjectAccessRequest pending = par(GUID, "invited@example.org", null);
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(pending);
            final String message = action.registrationDisallowedMessage(data, siteConfig(false, true));
            assertThat(message).contains("email address the invitation was sent to");
            assertThat(message).doesNotContain("log in");
        });
    }

    @Test
    public void disallowedMessageGenericWhenParRegistrationItselfDisabled() {
        // With par registration disabled, the invited email wouldn't be accepted either, so show the generic message.
        withAction(GUID, "other@example.org", (action, data, par) -> {
            final ProjectAccessRequest pending = par(GUID, "invited@example.org", null);
            par.when(() -> ProjectAccessRequest.RequestPARByGUID(eq(GUID), any())).thenReturn(pending);
            assertThat(action.registrationDisallowedMessage(data, siteConfig(true, false))).contains("not allowed");
        });
    }

    @Test
    public void disallowedMessageGenericForUnknownPar() {
        withAction("not-a-guid", EMAIL, (action, data, par) -> {
            assertThat(action.registrationDisallowedMessage(data, siteConfig(false, true))).contains("not allowed");
        });
    }
}
