package org.nrg.xnat.archive.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * XNAT-7944: the guarded status transitions must be atomic. A delete claiming a session and the archive trigger
 * queueing it can run at the same moment on the same RECEIVING row; whichever commits second must see the other's
 * status and refuse, never overwrite it. Runs against the real ORM test database because a mocked DAO cannot show a
 * lost update.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DirectArchiveSessionOrmTestConfig.class)
public class DirectArchiveSessionStatusTransitionTest {
    private static final int ROUNDS = 300;

    @Autowired
    private DirectArchiveSessionHibernateService service;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @After
    public void shutDown() {
        executor.shutdownNow();
    }

    private SessionData receivingSession(final int round) throws ArchivingException {
        final SessionData incoming = new SessionData().setProject("PROJ").setTag("1.2.3." + round).setName("SESSION_" + round)
                                                      .setFolderName("SESSION_" + round).setTimestamp("20260923_120000")
                                                      .setUrl("/data/xnat/archive/PROJ/arc001/SESSION_" + round)
                                                      .setStatus(PrearcStatus.RECEIVING);
        return service.create(incoming);
    }

    /** Runs both transitions as close to simultaneously as two threads allow and reports which of them succeeded. */
    private boolean[] raceClaimAgainstQueue(final long id) throws Exception {
        final CyclicBarrier   start   = new CyclicBarrier(2);
        final Callable<Boolean> claim = () -> {
            start.await();
            try {
                service.setStatusToDeleting(id, false);
                return true;
            } catch (ArchivingException e) {
                return false;
            }
        };
        final Callable<Boolean> queue = () -> {
            start.await();
            return service.setStatusToQueuedBuilding(id);
        };
        final Future<Boolean> claimed = executor.submit(claim);
        final Future<Boolean> queued  = executor.submit(queue);
        return new boolean[]{claimed.get(), queued.get()};
    }

    @Test
    public void claimingAndQueueingTheSameRestingSessionAtOnceLetsExactlyOneWin() throws Exception {
        final List<String> lostUpdates = new ArrayList<>();
        for (int round = 0; round < ROUNDS; round++) {
            final long      id      = receivingSession(round).getId();
            final boolean[] outcome = raceClaimAgainstQueue(id);
            final PrearcStatus finalStatus = service.getSessionData(id).getStatus();

            final boolean claimed = outcome[0];
            final boolean queued  = outcome[1];
            if (claimed == queued) {
                lostUpdates.add("round " + round + ": claimed=" + claimed + " queued=" + queued + " final=" + finalStatus);
            } else if (claimed && finalStatus != PrearcStatus.DELETING || queued && finalStatus != PrearcStatus.QUEUED_BUILDING) {
                lostUpdates.add("round " + round + ": winner " + (claimed ? "delete" : "queue") + " but final=" + finalStatus);
            }
        }
        assertThat(lostUpdates).as("rounds where both transitions succeeded or the winner was overwritten").isEmpty();
    }

    /** The importer's per-file touch runs before it takes its file lock, so it races the claim the same way. */
    @Test
    public void touchingASessionWhileADeleteClaimsItNeverRevivesIt() throws Exception {
        final List<String> revived = new ArrayList<>();
        for (int round = 0; round < ROUNDS; round++) {
            final long          id    = receivingSession(1000 + round).getId();
            final CyclicBarrier start = new CyclicBarrier(2);
            final Future<?> touched = executor.submit(() -> {
                start.await();
                service.touch(id);
                return null;
            });
            final Future<?> claimed = executor.submit(() -> {
                start.await();
                service.setStatusToDeleting(id, false);
                return null;
            });
            touched.get();
            claimed.get();

            final PrearcStatus finalStatus = service.getSessionData(id).getStatus();
            if (finalStatus != PrearcStatus.DELETING) {
                revived.add("round " + round + ": final=" + finalStatus);
            }
        }
        assertThat(revived).as("rounds where a touch overwrote the DELETING claim").isEmpty();
    }

    @Test
    public void aSessionInADisallowedStatusIsLeftUntouched() throws Exception {
        final long id = receivingSession(ROUNDS + 1).getId();
        service.setStatusToQueuedBuilding(id);
        service.setStatusToBuildingAndReturn(id);

        boolean claimed;
        try {
            service.setStatusToDeleting(id, false);
            claimed = true;
        } catch (ArchivingException e) {
            claimed = false;
        }

        assertThat(claimed).isFalse();
        assertThat(service.getSessionData(id).getStatus()).isEqualTo(PrearcStatus.BUILDING);
    }

    @Test
    public void queueingForBuildFromAnInFlightStatusIsRefusedWithoutForce() throws Exception {
        final long id = receivingSession(ROUNDS + 2).getId();
        service.setStatusToQueuedBuilding(id);
        service.setStatusToBuildingAndReturn(id);

        assertThat(service.setStatusToQueuedBuilding(id, false)).isFalse();
        assertThat(service.getSessionData(id).getStatus()).isEqualTo(PrearcStatus.BUILDING);
    }

    @Test
    public void forcingTheQueueForBuildMovesAnArchivingSessionBackToQueuedBuilding() throws Exception {
        final long id = receivingSession(ROUNDS + 4).getId();
        service.setStatusToQueuedBuilding(id);
        service.setStatusToBuildingAndReturn(id);
        service.setStatusToQueuedArchiving(id);
        service.setStatusToArchivingAndReturn(id);

        assertThat(service.setStatusToQueuedBuilding(id, true)).isTrue();
        assertThat(service.getSessionData(id).getStatus()).isEqualTo(PrearcStatus.QUEUED_BUILDING);
    }

    @Test
    public void forcingTheQueueForBuildLeavesAClaimedDeleteAlone() throws Exception {
        final long id = receivingSession(ROUNDS + 5).getId();
        service.setStatusToDeleting(id, false);

        assertThat(service.setStatusToQueuedBuilding(id, true)).isFalse();
        assertThat(service.getSessionData(id).getStatus()).isEqualTo(PrearcStatus.DELETING);
    }

    @Test
    public void forcingTheQueueForBuildMovesAnInFlightSessionBackToQueuedBuilding() throws Exception {
        final long id = receivingSession(ROUNDS + 3).getId();
        service.setStatusToQueuedBuilding(id);
        service.setStatusToBuildingAndReturn(id);

        assertThat(service.setStatusToQueuedBuilding(id, true)).isTrue();
        assertThat(service.getSessionData(id).getStatus()).isEqualTo(PrearcStatus.QUEUED_BUILDING);
    }
}
