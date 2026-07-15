/*
 * web: org.nrg.xnat.helpers.prearchive.PrearcBatchRecovery
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs {@link PrearcLockRecovery}'s decision over a batch of prearchive sessions and reports what happened to each.
 * See XNAT-8767.
 * <p>
 * The batch is best-effort: every session gets its turn no matter how the ones before it went, because an
 * administrator clearing a backlog of stranded sessions should not lose the whole batch to one bad row. Callers get an
 * outcome per session and decide what to tell the user.
 * <p>
 * The database and the queue are reached through {@link Executor} so this class stays free of both, which is what
 * makes the batch behaviour testable -- the REST resource it serves cannot be instantiated in a unit test.
 */
@Slf4j
public final class PrearcBatchRecovery {
    /** Everything {@link PrearcBatchRecovery} needs from the outside world. */
    public interface Executor {
        /**
         * @param triple The session to read.
         *
         * @return The session's current row.
         *
         * @throws Exception When the session can't be read.
         */
        SessionData load(SessionDataTriple triple) throws Exception;

        /**
         * @param triple      The session to rebuild.
         * @param sessionData The session's current row.
         * @param force       Whether to override the session's lock.
         *
         * @return True if the rebuild was queued, false if something else holds the session.
         *
         * @throws Exception When the rebuild can't be queued.
         */
        boolean queueRebuild(SessionDataTriple triple, SessionData sessionData, boolean force) throws Exception;

        /**
         * Clears an abandoned lock without rebuilding the session.
         *
         * @param triple      The session to unlock.
         * @param sessionData The session's current row.
         *
         * @throws Exception When the session can't be unlocked.
         */
        void unlockToError(SessionDataTriple triple, SessionData sessionData) throws Exception;
    }

    private PrearcBatchRecovery() {
        // Utility class.
    }

    /**
     * Decides and carries out the recovery for each session in turn.
     *
     * @param triples     The sessions the user asked to rebuild.
     * @param executor    Reaches the database and the queue.
     * @param isSiteAdmin Whether the requesting user holds the site administrator role.
     *
     * @return One outcome per session, in the order they were given.
     */
    public static List<PrearcRecoveryOutcome> run(final List<SessionDataTriple> triples, final Executor executor, final boolean isSiteAdmin) {
        final List<PrearcRecoveryOutcome> outcomes = new ArrayList<>();
        for (final SessionDataTriple triple : triples) {
            outcomes.add(recover(triple, executor, isSiteAdmin));
        }
        return outcomes;
    }

    private static PrearcRecoveryOutcome recover(final SessionDataTriple triple, final Executor executor, final boolean isSiteAdmin) {
        try {
            final SessionData          sessionData = executor.load(triple);
            final PrearcRecoveryAction action      = PrearcLockRecovery.decide(sessionData.getStatus(), isSiteAdmin);
            switch (action) {
                case ALREADY_QUEUED:
                    // The session is already waiting to be built, so the user has what they asked for.
                    return PrearcRecoveryOutcome.done(triple, action);

                case LOCKED_REQUIRES_ADMIN:
                    return PrearcRecoveryOutcome.refused(triple, action);

                case UNLOCK_TO_ERROR:
                    executor.unlockToError(triple, sessionData);
                    return PrearcRecoveryOutcome.done(triple, action);

                case FORCE_REBUILD:
                case PROCEED:
                    // A refused queue means the session was never rebuilt, so it must not be reported as done.
                    return executor.queueRebuild(triple, sessionData, action == PrearcRecoveryAction.FORCE_REBUILD)
                           ? PrearcRecoveryOutcome.done(triple, action)
                           : PrearcRecoveryOutcome.refused(triple, action);

                default:
                    throw new IllegalStateException("No recovery action defined for " + action + " on prearchive session " + triple);
            }
        } catch (Exception e) {
            // One unrecoverable session does not end the batch: the rest still get their turn.
            log.error("Unable to recover prearchive session {}", triple, e);
            return PrearcRecoveryOutcome.failed(triple, null, e);
        }
    }
}
