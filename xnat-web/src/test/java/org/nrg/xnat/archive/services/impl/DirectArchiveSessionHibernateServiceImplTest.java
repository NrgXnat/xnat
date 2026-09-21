package org.nrg.xnat.archive.services.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.daos.DirectArchiveSessionDao;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * XNAT-7944: the delete path claims a session by moving it to DELETING, which is only allowed from the two resting
 * statuses (plus DELETING itself, so an interrupted delete can be retried). The transitions the importer and the
 * archive trigger make must not revive a claimed session. Directory ownership is decided by whether any other session
 * at the same location is still alive.
 */
public class DirectArchiveSessionHibernateServiceImplTest {
    private static final long   SESSION_ID = 42L;
    private static final String LOCATION   = "/data/xnat/archive/PROJ/arc001/SESSION_01";

    private DirectArchiveSessionDao                 dao;
    private DirectArchiveSessionHibernateServiceImpl service;

    @Before
    public void setUp() {
        dao     = mock(DirectArchiveSessionDao.class);
        service = new DirectArchiveSessionHibernateServiceImpl();
        service.setDao(dao);
    }

    private static DirectArchiveSession sessionIn(final long id, final PrearcStatus status) {
        final DirectArchiveSession session = new DirectArchiveSession();
        session.setId(id);
        session.setStatus(status);
        session.setLocation(LOCATION);
        return session;
    }

    private DirectArchiveSession stubSessionIn(final PrearcStatus status) {
        final DirectArchiveSession session = sessionIn(SESSION_ID, status);
        when(dao.retrieve(SESSION_ID)).thenReturn(session);
        return session;
    }

    @Test
    public void aRestingSessionCanBeClaimedForDeletion() throws Exception {
        for (final PrearcStatus status : Arrays.asList(PrearcStatus.RECEIVING, PrearcStatus.ERROR)) {
            final DirectArchiveSession session = stubSessionIn(status);

            assertThat(service.setStatusToDeletingAndReturn(SESSION_ID).getStatus()).as("from %s", status).isEqualTo(PrearcStatus.DELETING);

            assertThat(session.getStatus()).isEqualTo(PrearcStatus.DELETING);
        }
        verify(dao, times(2)).update(any(DirectArchiveSession.class));
    }

    @Test
    public void aSessionLeftInDeletingByAnInterruptedDeleteCanBeClaimedAgain() throws Exception {
        final DirectArchiveSession session = stubSessionIn(PrearcStatus.DELETING);

        assertThat(service.setStatusToDeletingAndReturn(SESSION_ID).getStatus()).isEqualTo(PrearcStatus.DELETING);

        assertThat(session.getStatus()).isEqualTo(PrearcStatus.DELETING);
    }

    @Test
    public void aSessionInAnyOtherStatusCannotBeClaimed() {
        final EnumSet<PrearcStatus> notDeletable = EnumSet.complementOf(EnumSet.of(PrearcStatus.RECEIVING, PrearcStatus.ERROR, PrearcStatus.DELETING));
        for (final PrearcStatus status : notDeletable) {
            final DirectArchiveSession session = stubSessionIn(status);

            assertThatThrownBy(() -> service.setStatusToDeletingAndReturn(SESSION_ID)).as("from %s", status).isInstanceOf(ArchivingException.class);

            assertThat(session.getStatus()).isEqualTo(status);
        }
        verify(dao, never()).update(any());
    }

    @Test
    public void queueingForBuildOnlyMovesAReceivingSession() throws Exception {
        final DirectArchiveSession receiving = stubSessionIn(PrearcStatus.RECEIVING);
        service.setStatusToQueuedBuilding(SESSION_ID);
        assertThat(receiving.getStatus()).isEqualTo(PrearcStatus.QUEUED_BUILDING);

        // The archive trigger must not overwrite a session that a delete has just claimed.
        final DirectArchiveSession claimed = stubSessionIn(PrearcStatus.DELETING);
        service.setStatusToQueuedBuilding(SESSION_ID);
        assertThat(claimed.getStatus()).isEqualTo(PrearcStatus.DELETING);
        verify(dao, times(1)).update(any(DirectArchiveSession.class));
    }

    @Test
    public void settingBackToReceivingOnlyUndoesAQueuedForBuildingTransition() {
        final DirectArchiveSession queued = stubSessionIn(PrearcStatus.QUEUED_BUILDING);
        service.setStatusBackToReceiving(SESSION_ID);
        assertThat(queued.getStatus()).isEqualTo(PrearcStatus.RECEIVING);

        // A claimed session must not be revived by the build path giving up on it.
        final DirectArchiveSession claimed = stubSessionIn(PrearcStatus.DELETING);
        service.setStatusBackToReceiving(SESSION_ID);
        assertThat(claimed.getStatus()).isEqualTo(PrearcStatus.DELETING);
        verify(dao, times(1)).update(any(DirectArchiveSession.class));
    }

    @Test
    public void deletingARowThatIsAlreadyGoneIsANoOp() {
        when(dao.retrieve(SESSION_ID)).thenReturn(null);

        service.delete(SESSION_ID);

        verify(dao, never()).delete(any());
    }

    @Test
    public void deletingAnExistingRowRemovesIt() {
        final DirectArchiveSession session = stubSessionIn(PrearcStatus.DELETING);

        service.delete(SESSION_ID);

        verify(dao).delete(session);
    }

    @Test
    public void locationIsActiveWhenAnotherSessionThereIsNotInError() {
        when(dao.findByLocation(LOCATION)).thenReturn(Arrays.asList(sessionIn(SESSION_ID, PrearcStatus.ERROR),
                                                                     sessionIn(43L, PrearcStatus.RECEIVING)));

        assertThat(service.hasActiveSessionAtLocation(LOCATION, SESSION_ID)).isTrue();
    }

    @Test
    public void locationIsNotActiveWhenEveryOtherSessionThereIsInError() {
        when(dao.findByLocation(LOCATION)).thenReturn(Arrays.asList(sessionIn(SESSION_ID, PrearcStatus.RECEIVING),
                                                                     sessionIn(43L, PrearcStatus.ERROR)));

        assertThat(service.hasActiveSessionAtLocation(LOCATION, SESSION_ID)).isFalse();
    }

    @Test
    public void withoutAnExclusionEverySessionAtTheLocationCounts() {
        when(dao.findByLocation(LOCATION)).thenReturn(Collections.singletonList(sessionIn(SESSION_ID, PrearcStatus.RECEIVING)));

        assertThat(service.hasActiveSessionAtLocation(LOCATION, null)).isTrue();
        assertThat(service.hasActiveSessionAtLocation(LOCATION, SESSION_ID)).isFalse();
    }

    @Test
    public void anEmptyLocationIsNotActive() {
        when(dao.findByLocation(LOCATION)).thenReturn(Collections.emptyList());

        assertThat(service.hasActiveSessionAtLocation(LOCATION, null)).isFalse();
    }
}
