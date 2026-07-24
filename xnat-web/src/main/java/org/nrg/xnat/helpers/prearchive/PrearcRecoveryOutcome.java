/*
 * web: org.nrg.xnat.helpers.prearchive.PrearcRecoveryOutcome
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import javax.annotation.Nullable;

/**
 * What happened to one session in a batch handled by {@link PrearcBatchRecovery}. See XNAT-8767.
 *
 * @param triple    The session.
 * @param action    What {@link PrearcLockRecovery#decide} decided to do with it, or null if its status could not even
 *                  be read.
 * @param succeeded Whether the session was actually acted on. Only these belong in the response: a session the queue
 *                  refused was not rebuilt, whatever the decision was.
 * @param error     The exception that stopped this session, or null if none did.
 */
public record PrearcRecoveryOutcome(SessionDataTriple triple, @Nullable PrearcRecoveryAction action, boolean succeeded, @Nullable Exception error) {
    static PrearcRecoveryOutcome done(final SessionDataTriple triple, final PrearcRecoveryAction action) {
        return new PrearcRecoveryOutcome(triple, action, true, null);
    }

    static PrearcRecoveryOutcome refused(final SessionDataTriple triple, final PrearcRecoveryAction action) {
        return new PrearcRecoveryOutcome(triple, action, false, null);
    }

    static PrearcRecoveryOutcome failed(final SessionDataTriple triple, @Nullable final PrearcRecoveryAction action, final Exception error) {
        return new PrearcRecoveryOutcome(triple, action, false, error);
    }
}
