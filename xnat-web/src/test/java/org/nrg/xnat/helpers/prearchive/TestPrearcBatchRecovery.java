/*
 * web: org.nrg.xnat.helpers.prearchive.TestPrearcBatchRecovery
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import org.junit.Test;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction.FORCE_REBUILD;
import static org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction.LOCKED_REQUIRES_ADMIN;
import static org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction.PROCEED;
import static org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction.UNLOCK_TO_ERROR;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.READY;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._ARCHIVING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._BUILDING;

/**
 * Tests how a batch of prearchive sessions is recovered: what happens to the sessions around one that is refused or
 * blows up, and what the caller is told was done. See XNAT-8767.
 */
public class TestPrearcBatchRecovery {
    /** Records what it was asked to do and answers from a script, so the batch logic can be tested on its own. */
    private static final class FakeRecoveryOps implements PrearcBatchRecovery.RecoveryOps {
        private final Map<SessionDataTriple, SessionData> sessions   = new HashMap<>();
        private final Map<SessionDataTriple, Exception>   loadErrors = new HashMap<>();
        private final List<SessionDataTriple>             rebuilt    = new ArrayList<>();
        private final List<SessionDataTriple>             forced     = new ArrayList<>();
        private final List<SessionDataTriple>             unlocked   = new ArrayList<>();
        private final List<SessionDataTriple>             refuseQueue = new ArrayList<>();

        @Override
        public SessionData load(final SessionDataTriple triple) throws Exception {
            final Exception error = loadErrors.get(triple);
            if (error != null) {
                throw error;
            }
            return sessions.get(triple);
        }

        @Override
        public boolean queueRebuild(final SessionDataTriple triple, final SessionData sessionData, final boolean force) {
            rebuilt.add(triple);
            if (force) {
                forced.add(triple);
            }
            return !refuseQueue.contains(triple);
        }

        @Override
        public void unlockToError(final SessionDataTriple triple, final SessionData sessionData) {
            unlocked.add(triple);
        }
    }

    private static SessionDataTriple triple(final String name) {
        final SessionDataTriple triple = new SessionDataTriple();
        triple.setFolderName(name);
        triple.setTimestamp("20260715_120000");
        triple.setProject("PROJ");
        return triple;
    }

    private static SessionData session(final PrearcStatus status) {
        return new SessionData().setFolderName("ignored").setTimestamp("20260715_120000").setProject("PROJ").setStatus(status);
    }

    private static List<PrearcRecoveryAction> actionsOf(final List<PrearcRecoveryOutcome> outcomes) {
        final List<PrearcRecoveryAction> actions = new ArrayList<>();
        for (final PrearcRecoveryOutcome outcome : outcomes) {
            actions.add(outcome.action());
        }
        return actions;
    }

    /**
     * The batch is best-effort: a session that cannot even be loaded must not cost the sessions after it their turn.
     * An administrator clearing a backlog of stranded sessions relies on every selected session getting an attempt.
     */
    @Test
    public void oneSessionBlowingUpDoesNotCostTheOthersTheirTurn() {
        final SessionDataTriple first  = triple("first");
        final SessionDataTriple broken = triple("broken");
        final SessionDataTriple last   = triple("last");

        final FakeRecoveryOps ops = new FakeRecoveryOps();
        ops.sessions.put(first, session(_BUILDING));
        ops.loadErrors.put(broken, new IllegalStateException("session directory is gone"));
        ops.sessions.put(last, session(_BUILDING));

        final List<PrearcRecoveryOutcome> outcomes = PrearcBatchRecovery.run(Arrays.asList(first, broken, last), ops, true);

        assertThat(ops.forced).describedAs("both healthy sessions must still be forced").containsExactly(first, last);
        assertThat(outcomes).hasSize(3);
        assertThat(outcomes.get(1).succeeded()).isFalse();
        assertThat(outcomes.get(1).error()).hasMessage("session directory is gone");
        assertThat(outcomes.get(0).succeeded()).isTrue();
        assertThat(outcomes.get(2).succeeded()).isTrue();
    }

    /**
     * queuePrearchiveOperation reports a refusal through its return value, not an exception. Discarding that value
     * would report the session as rebuilt when nothing happened.
     */
    @Test
    public void aSessionTheQueueRefusedIsNotReportedAsDone() {
        final SessionDataTriple refused = triple("refused");
        final SessionDataTriple queued  = triple("queued");

        final FakeRecoveryOps ops = new FakeRecoveryOps();
        ops.sessions.put(refused, session(READY));
        ops.sessions.put(queued, session(READY));
        ops.refuseQueue.add(refused);

        final List<PrearcRecoveryOutcome> outcomes = PrearcBatchRecovery.run(Arrays.asList(refused, queued), ops, true);

        assertThat(outcomes.get(0).succeeded()).describedAs("refused session must not count as done").isFalse();
        assertThat(outcomes.get(0).action()).isEqualTo(PROCEED);
        assertThat(outcomes.get(1).succeeded()).isTrue();
    }

    /**
     * A session that is already queued must not be re-queued, but it is not a failure either: it must count as a
     * success, or an admin re-clicking Rebuild on a session that is already on its way gets a spurious error.
     */
    @Test
    public void alreadyQueuedSessionsAreReportedDoneWithoutBeingRequeued() {
        final SessionDataTriple queued  = triple("queued");
        final SessionDataTriple crashed = triple("crashed");

        final FakeRecoveryOps ops = new FakeRecoveryOps();
        ops.sessions.put(queued, session(PrearcStatus.QUEUED_BUILDING));
        ops.sessions.put(crashed, session(_BUILDING));

        final List<PrearcRecoveryOutcome> outcomes = PrearcBatchRecovery.run(Arrays.asList(queued, crashed), ops, true);

        assertThat(actionsOf(outcomes)).containsExactly(PrearcRecoveryAction.ALREADY_QUEUED, FORCE_REBUILD);
        assertThat(outcomes.get(0).succeeded()).describedAs("an already-queued session is not an error").isTrue();
        assertThat(ops.rebuilt).describedAs("it must not be queued a second time").containsExactly(crashed);
    }

    /** A non-admin gets nothing forced on their behalf, but unlocked sessions still go through. */
    @Test
    public void nonAdminHasLockedSessionsRefusedButUnlockedOnesProceed() {
        final SessionDataTriple locked   = triple("locked");
        final SessionDataTriple unlocked = triple("unlocked");

        final FakeRecoveryOps ops = new FakeRecoveryOps();
        ops.sessions.put(locked, session(_BUILDING));
        ops.sessions.put(unlocked, session(READY));

        final List<PrearcRecoveryOutcome> outcomes = PrearcBatchRecovery.run(Arrays.asList(locked, unlocked), ops, false);

        assertThat(actionsOf(outcomes)).containsExactly(LOCKED_REQUIRES_ADMIN, PROCEED);
        assertThat(ops.forced).isEmpty();
        assertThat(ops.rebuilt).containsExactly(unlocked);
    }

    /** Locks left by destructive operations are unlocked, never rebuilt -- even in the middle of a batch. */
    @Test
    public void destructiveLocksAreUnlockedWhileBuildingLocksAreRebuilt() {
        final SessionDataTriple archiving = triple("archiving");
        final SessionDataTriple building  = triple("building");

        final FakeRecoveryOps ops = new FakeRecoveryOps();
        ops.sessions.put(archiving, session(_ARCHIVING));
        ops.sessions.put(building, session(_BUILDING));

        final List<PrearcRecoveryOutcome> outcomes = PrearcBatchRecovery.run(Arrays.asList(archiving, building), ops, true);

        assertThat(actionsOf(outcomes)).containsExactly(UNLOCK_TO_ERROR, FORCE_REBUILD);
        assertThat(ops.unlocked).containsExactly(archiving);
        assertThat(ops.rebuilt).describedAs("an archiving session must never be sent to the rebuild queue").containsExactly(building);
    }

    @Test
    public void anEmptyBatchIsNotAnError() {
        assertThat(PrearcBatchRecovery.run(Collections.emptyList(), new FakeRecoveryOps(), true)).isEmpty();
    }
}
