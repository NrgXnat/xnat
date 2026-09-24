package org.nrg.xnat.archive.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.restlet.data.Status;
import org.springframework.jms.core.JmsTemplate;

import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * XNAT-7944: a manual archive trigger uses the guarded queue transition and answers 409 when the session is not in
 * a status that can be queued; a site admin may force the queue transition to retry a session left in flight.
 */
public class DirectArchiveSessionServiceImplTriggerTest {
    private static final long SESSION_ID = 42L;

    private DirectArchiveSessionHibernateService hibernateService;
    private DirectArchiveSessionServiceImpl      service;
    private UserI                                user;
    private SessionData                          session;
    private MockedStatic<Roles>                  mockedRoles;
    private MockedStatic<XDAT>                   mockedXDAT;
    private MockedStatic<PrearcUtils>            mockedPrearcUtils;
    private MockedStatic<BaseXnatExperimentdata> mockedExperiments;

    @Before
    public void setUp() {
        hibernateService = mock(DirectArchiveSessionHibernateService.class);
        user             = mock(UserI.class);
        service          = new DirectArchiveSessionServiceImpl(hibernateService, mock(JmsTemplate.class), mock(XnatUserProvider.class),
                                                               mock(GroupsAndPermissionsCache.class), mock(PermissionsServiceI.class));
        session = new SessionData().setProject("PROJ").setTag("1.2.3").setName("SESSION_1").setFolderName("SESSION_1")
                                   .setTimestamp("20260924_120000").setUrl("/data/xnat/archive/PROJ/arc001/SESSION_1")
                                   .setStatus(PrearcStatus.BUILDING);
        session.setId(SESSION_ID);
        mockedRoles = Mockito.mockStatic(Roles.class);
        mockedRoles.when(() -> Roles.isSiteAdmin(user)).thenReturn(false);
        mockedXDAT = Mockito.mockStatic(XDAT.class);
        mockedPrearcUtils = Mockito.mockStatic(PrearcUtils.class);
        mockedPrearcUtils.when(() -> PrearcUtils.isSessionReceiving(any())).thenReturn(false);
        // No archived experiment owns the session directory unless a test says so
        mockedExperiments = Mockito.mockStatic(BaseXnatExperimentdata.class);
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(any(), any(), any(), anyBoolean())).thenReturn(null);
    }

    @After
    public void tearDown() {
        mockedExperiments.closeOnDemand();
        mockedPrearcUtils.closeOnDemand();
        mockedXDAT.closeOnDemand();
        mockedRoles.closeOnDemand();
    }

    @Test
    public void forcingARetryRequiresASiteAdmin() throws Exception {
        assertThatThrownBy(() -> service.triggerArchive(session, user, true)).isInstanceOf(InvalidPermissionException.class);

        verify(hibernateService, never()).setStatusToQueuedBuilding(anyLong(), anyBoolean());
    }

    @Test
    public void aSiteAdminCanForceARetryOfAnInFlightSession() throws Exception {
        mockedRoles.when(() -> Roles.isSiteAdmin(user)).thenReturn(true);
        when(hibernateService.setStatusToQueuedBuilding(SESSION_ID, true)).thenReturn(true);

        assertThatCode(() -> service.triggerArchive(session, user, true)).doesNotThrowAnyException();

        verify(hibernateService).setStatusToQueuedBuilding(SESSION_ID, true);
        mockedXDAT.verify(() -> XDAT.sendJmsRequest(any(), any()));
    }

    @Test
    public void withoutForceTheGuardedQueueTransitionIsUsed() throws Exception {
        when(hibernateService.setStatusToQueuedBuilding(SESSION_ID, false)).thenReturn(true);

        assertThatCode(() -> service.triggerArchive(session, user, false)).doesNotThrowAnyException();

        verify(hibernateService).setStatusToQueuedBuilding(SESSION_ID, false);
    }

    @Test
    public void aManualRetryIsRefusedWhenAnArchivedExperimentOwnsTheDirectory() throws Exception {
        givenAnArchivedExperimentOwnsTheDirectory();

        assertThatThrownBy(() -> service.triggerArchive(session, user, false))
                .isInstanceOf(ClientException.class)
                .satisfies(e -> org.assertj.core.api.Assertions.assertThat(((ClientException) e).getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT));

        verify(hibernateService, never()).setStatusToQueuedBuilding(anyLong(), anyBoolean());
        mockedXDAT.verify(() -> XDAT.sendJmsRequest(any(), any()), never());
    }

    @Test
    public void aForcedRetryIsRefusedWhenAnArchivedExperimentOwnsTheDirectory() throws Exception {
        mockedRoles.when(() -> Roles.isSiteAdmin(user)).thenReturn(true);
        givenAnArchivedExperimentOwnsTheDirectory();

        assertThatThrownBy(() -> service.triggerArchive(session, user, true))
                .isInstanceOf(ClientException.class)
                .satisfies(e -> org.assertj.core.api.Assertions.assertThat(((ClientException) e).getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT));

        verify(hibernateService, never()).setStatusToQueuedBuilding(anyLong(), anyBoolean());
        mockedXDAT.verify(() -> XDAT.sendJmsRequest(any(), any()), never());
    }

    @Test
    public void aMergeSessionMayBeRetriedAlthoughAnArchivedExperimentOwnsTheDirectory() throws Exception {
        givenAnArchivedExperimentOwnsTheDirectory();
        when(hibernateService.getOverwriteMode(SESSION_ID)).thenReturn("append");
        when(hibernateService.setStatusToQueuedBuilding(SESSION_ID, false)).thenReturn(true);

        assertThatCode(() -> service.triggerArchive(session, user, false)).doesNotThrowAnyException();

        verify(hibernateService).setStatusToQueuedBuilding(SESSION_ID, false);
    }

    private void givenAnArchivedExperimentOwnsTheDirectory() throws Exception {
        final XnatExperimentdata archived = mock(XnatExperimentdata.class);
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(eq("PROJ"), eq("SESSION_1"), any(), anyBoolean())).thenReturn(archived);
        when(hibernateService.getOverwriteMode(SESSION_ID)).thenReturn(null);
    }

    @Test
    public void aRefusedQueueTransitionIsAConflict() throws Exception {
        when(hibernateService.setStatusToQueuedBuilding(SESSION_ID, false)).thenReturn(false);

        assertThatThrownBy(() -> service.triggerArchive(session, user, false))
                .isInstanceOf(ClientException.class)
                .satisfies(e -> org.assertj.core.api.Assertions.assertThat(((ClientException) e).getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT));
        mockedXDAT.verify(() -> XDAT.sendJmsRequest(any(), any()), never());
    }
}
