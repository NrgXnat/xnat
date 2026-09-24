package org.nrg.xnat.archive.services.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.daos.DirectArchiveSessionDao;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * XNAT-7944: the delete path claims a session by moving it to DELETING, which is only allowed from the resting
 * statuses (and DELETING itself, so an interrupted delete can be retried), or from anywhere when a site admin forces
 * it. The transitions the archive trigger makes must not revive a claimed session. Every guarded transition goes
 * through the DAO's single conditional update rather than a read-check-write on the entity, so the DAO is stood in
 * for by a fake that applies the same rule the SQL does; {@link DirectArchiveSessionStatusTransitionTest} shows the
 * real statement is atomic.
 */
public class DirectArchiveSessionHibernateServiceImplTest {
    private static final long   SESSION_ID = 42L;
    private static final String LOCATION   = "/data/xnat/archive/PROJ/arc001/SESSION_01";

    private DirectArchiveSessionDao                 dao;
    private DirectArchiveSessionHibernateServiceImpl service;

    /** The row's status as the database holds it; null once the row is gone. */
    private PrearcStatus status;

    @Before
    public void setUp() {
        dao     = mock(DirectArchiveSessionDao.class);
        service = new DirectArchiveSessionHibernateServiceImpl();
        service.setDao(dao);

        when(dao.transitionStatus(eq(SESSION_ID), any(), any())).thenAnswer(invocation -> {
            final Set<PrearcStatus> allowed = invocation.getArgument(2);
            if (status == null || !allowed.contains(status)) {
                return 0;
            }
            status = invocation.getArgument(1);
            return 1;
        });
        when(dao.retrieve(SESSION_ID)).thenAnswer(invocation -> status == null ? null : sessionIn(SESSION_ID, status));
    }

    private static DirectArchiveSession sessionIn(final long id, final PrearcStatus status) {
        final DirectArchiveSession session = new DirectArchiveSession();
        session.setId(id);
        session.setStatus(status);
        session.setLocation(LOCATION);
        return session;
    }

    @Test
    public void aRestingOrAlreadyClaimedSessionCanBeClaimedForDeletion() throws Exception {
        // DELETING is included so a delete that died after claiming the session can be retried.
        for (final PrearcStatus from : Arrays.asList(PrearcStatus.RECEIVING, PrearcStatus.ERROR, PrearcStatus.DELETING)) {
            status = from;

            service.setStatusToDeleting(SESSION_ID, false);

            assertThat(status).as("from %s", from).isEqualTo(PrearcStatus.DELETING);
        }
        verify(dao, never()).update(any());
    }

    @Test
    public void aSessionInAnyOtherStatusCannotBeClaimed() {
        final EnumSet<PrearcStatus> notDeletable = EnumSet.complementOf(EnumSet.of(PrearcStatus.RECEIVING, PrearcStatus.ERROR, PrearcStatus.DELETING));
        for (final PrearcStatus from : notDeletable) {
            status = from;

            assertThatThrownBy(() -> service.setStatusToDeleting(SESSION_ID, false)).as("from %s", from)
                    .isInstanceOf(ArchivingException.class).hasMessageContaining(from.name());

            assertThat(status).isEqualTo(from);
        }
        verify(dao, never()).update(any());
    }

    @Test
    public void aForcedClaimMovesASessionInAnyStatusToDeleting() throws Exception {
        // A site admin's way out for rows stuck in QUEUED_BUILDING, BUILDING, QUEUED_ARCHIVING or ARCHIVING.
        for (final PrearcStatus from : PrearcStatus.values()) {
            status = from;

            service.setStatusToDeleting(SESSION_ID, true);

            assertThat(status).as("from %s", from).isEqualTo(PrearcStatus.DELETING);
        }
    }

    @Test
    public void aForcedQueueForBuildMovesEveryStatusButDeletingToQueuedBuilding() throws Exception {
        for (final PrearcStatus from : EnumSet.complementOf(EnumSet.of(PrearcStatus.DELETING))) {
            status = from;

            assertThat(service.setStatusToQueuedBuilding(SESSION_ID, true)).as("from %s", from).isTrue();
            assertThat(status).as("from %s", from).isEqualTo(PrearcStatus.QUEUED_BUILDING);
        }
    }

    @Test
    public void aForcedQueueForBuildLeavesADeletingRowAlone() throws Exception {
        status = PrearcStatus.DELETING;

        assertThat(service.setStatusToQueuedBuilding(SESSION_ID, true)).isFalse();
        assertThat(status).isEqualTo(PrearcStatus.DELETING);
    }

    @Test
    public void claimingARowThatIsGoneThrowsNotFound() {
        status = null;

        assertThatThrownBy(() -> service.setStatusToDeleting(SESSION_ID, false)).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void queueingARowThatIsGoneThrowsNotFound() {
        status = null;

        assertThatThrownBy(() -> service.setStatusToQueuedBuilding(SESSION_ID)).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void touchingOnlyUpdatesTheLastBuiltDateAndNeverWritesTheEntityBack() throws Exception {
        // The importer touches the row before it takes its file lock; a whole-entity update would write the status
        // it read a moment ago over a delete's claim.
        when(dao.touch(SESSION_ID)).thenReturn(1);

        service.touch(SESSION_ID);

        verify(dao).touch(SESSION_ID);
        verify(dao, never()).update(any());
    }

    @Test
    public void touchingARowThatIsGoneThrowsNotFound() {
        when(dao.touch(SESSION_ID)).thenReturn(0);

        assertThatThrownBy(() -> service.touch(SESSION_ID)).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void queueingForBuildMovesARestingSessionAndSaysSo() throws Exception {
        // RECEIVING is the normal case; ERROR lets a user retry the archive of a failed session.
        for (final PrearcStatus from : Arrays.asList(PrearcStatus.RECEIVING, PrearcStatus.ERROR)) {
            status = from;

            assertThat(service.setStatusToQueuedBuilding(SESSION_ID)).as("from %s", from).isTrue();

            assertThat(status).isEqualTo(PrearcStatus.QUEUED_BUILDING);
        }
    }

    @Test
    public void queueingForBuildLeavesAClaimedSessionAloneAndSaysSo() throws Exception {
        // The archive trigger must not overwrite a session that a delete has just claimed.
        status = PrearcStatus.DELETING;

        assertThat(service.setStatusToQueuedBuilding(SESSION_ID)).isFalse();

        assertThat(status).isEqualTo(PrearcStatus.DELETING);
        verify(dao, never()).update(any());
    }

    @Test
    public void settingBackToReceivingOnlyUndoesAQueuedForBuildingTransition() throws Throwable {
        // A claimed session must not be revived by the build path giving up on it.
        assertOnlyMovesFrom(PrearcStatus.QUEUED_BUILDING, PrearcStatus.RECEIVING, () -> service.setStatusBackToReceiving(SESSION_ID));
    }

    @Test
    public void buildingAndArchivingFollowTheQueueAndReturnTheMovedRow() throws Exception {
        status = PrearcStatus.QUEUED_BUILDING;
        assertThat(service.setStatusToBuildingAndReturn(SESSION_ID).getStatus()).isEqualTo(PrearcStatus.BUILDING);

        status = PrearcStatus.QUEUED_ARCHIVING;
        assertThat(service.setStatusToArchivingAndReturn(SESSION_ID).getStatus()).isEqualTo(PrearcStatus.ARCHIVING);

        status = PrearcStatus.RECEIVING;
        assertThatThrownBy(() -> service.setStatusToBuildingAndReturn(SESSION_ID)).isInstanceOf(ArchivingException.class);
        assertThat(status).isEqualTo(PrearcStatus.RECEIVING);
    }

    private void assertOnlyMovesFrom(final PrearcStatus from, final PrearcStatus to, final ThrowingCallable transition) throws Throwable {
        status = from;
        transition.call();
        assertThat(status).isEqualTo(to);

        status = PrearcStatus.DELETING;
        transition.call();
        assertThat(status).isEqualTo(PrearcStatus.DELETING);
    }

    @Test
    public void deletingARowThatIsAlreadyGoneIsANoOp() {
        status = null;

        service.delete(SESSION_ID);

        verify(dao, never()).delete(any());
    }

    @Test
    public void deletingAnExistingRowRemovesIt() {
        status = PrearcStatus.DELETING;

        service.delete(SESSION_ID);

        verify(dao).delete(any(DirectArchiveSession.class));
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
    public void anotherSessionInErrorAtTheLocationStillSharesTheDirectory() {
        when(dao.findByLocation(LOCATION)).thenReturn(Arrays.asList(sessionIn(SESSION_ID, PrearcStatus.ERROR),
                                                                    sessionIn(43L, PrearcStatus.ERROR)));

        assertThat(service.hasOtherSessionAtLocation(LOCATION, SESSION_ID)).isTrue();
    }

    @Test
    public void aSessionAloneAtTheLocationSharesItWithNoOne() {
        when(dao.findByLocation(LOCATION)).thenReturn(Collections.singletonList(sessionIn(SESSION_ID, PrearcStatus.ERROR)));

        assertThat(service.hasOtherSessionAtLocation(LOCATION, SESSION_ID)).isFalse();
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
