package org.nrg.xnat.archive.services.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
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
 * XNAT-7944: the delete path claims a session by moving it to DELETING, which is only allowed from the resting
 * statuses (and DELETING itself, so an interrupted delete can be retried). The transitions the archive trigger makes
 * must not revive a claimed session. Directory ownership is decided by whether any other session at the same location
 * is still alive.
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
    public void aRestingOrAlreadyClaimedSessionCanBeClaimedForDeletion() throws Exception {
        // DELETING is included so a delete that died after claiming the session can be retried.
        for (final PrearcStatus status : Arrays.asList(PrearcStatus.RECEIVING, PrearcStatus.ERROR, PrearcStatus.DELETING)) {
            final DirectArchiveSession session = stubSessionIn(status);

            service.setStatusToDeleting(SESSION_ID);

            assertThat(session.getStatus()).as("from %s", status).isEqualTo(PrearcStatus.DELETING);
        }
        verify(dao, times(3)).update(any(DirectArchiveSession.class));
    }

    @Test
    public void aSessionInAnyOtherStatusCannotBeClaimed() {
        final EnumSet<PrearcStatus> notDeletable = EnumSet.complementOf(EnumSet.of(PrearcStatus.RECEIVING, PrearcStatus.ERROR, PrearcStatus.DELETING));
        for (final PrearcStatus status : notDeletable) {
            final DirectArchiveSession session = stubSessionIn(status);

            assertThatThrownBy(() -> service.setStatusToDeleting(SESSION_ID)).as("from %s", status).isInstanceOf(ArchivingException.class);

            assertThat(session.getStatus()).isEqualTo(status);
        }
        verify(dao, never()).update(any());
    }

    @Test
    public void queueingForBuildMovesARestingSessionAndSaysSo() throws Exception {
        // RECEIVING is the normal case; ERROR lets a user retry the archive of a failed session.
        for (final PrearcStatus status : Arrays.asList(PrearcStatus.RECEIVING, PrearcStatus.ERROR)) {
            final DirectArchiveSession session = stubSessionIn(status);

            assertThat(service.setStatusToQueuedBuilding(SESSION_ID)).as("from %s", status).isTrue();

            assertThat(session.getStatus()).isEqualTo(PrearcStatus.QUEUED_BUILDING);
        }
        verify(dao, times(2)).update(any(DirectArchiveSession.class));
    }

    @Test
    public void queueingForBuildLeavesAClaimedSessionAloneAndSaysSo() throws Exception {
        // The archive trigger must not overwrite a session that a delete has just claimed.
        final DirectArchiveSession claimed = stubSessionIn(PrearcStatus.DELETING);

        assertThat(service.setStatusToQueuedBuilding(SESSION_ID)).isFalse();

        assertThat(claimed.getStatus()).isEqualTo(PrearcStatus.DELETING);
        verify(dao, never()).update(any());
    }

    @Test
    public void settingBackToReceivingOnlyUndoesAQueuedForBuildingTransition() throws Throwable {
        // A claimed session must not be revived by the build path giving up on it.
        assertOnlyMovesFrom(PrearcStatus.QUEUED_BUILDING, PrearcStatus.RECEIVING, () -> service.setStatusBackToReceiving(SESSION_ID));
    }

    private void assertOnlyMovesFrom(final PrearcStatus from, final PrearcStatus to, final ThrowingCallable transition) throws Throwable {
        final DirectArchiveSession allowed = stubSessionIn(from);
        transition.call();
        assertThat(allowed.getStatus()).isEqualTo(to);

        final DirectArchiveSession claimed = stubSessionIn(PrearcStatus.DELETING);
        transition.call();
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
    }

    @Test
    public void anEmptyLocationIsNotActive() {
        when(dao.findByLocation(LOCATION)).thenReturn(Collections.emptyList());

        assertThat(service.hasActiveSessionAtLocation(LOCATION, null)).isFalse();
    }
}
