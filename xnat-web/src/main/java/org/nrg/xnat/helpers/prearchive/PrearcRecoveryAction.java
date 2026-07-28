/*
 * web: org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

/**
 * What should be done with a prearchive session when a user asks to rebuild it. Determined by
 * {@link PrearcLockRecovery#decide}. See XNAT-8767.
 */
public enum PrearcRecoveryAction {
    /** The session isn't locked. Queue a rebuild the usual way, without overriding the lock. */
    PROCEED,

    /** The session is already queued, so the user's request is already satisfied. Nothing to do. */
    ALREADY_QUEUED,

    /**
     * The session is stranded in {@link PrearcUtils.PrearcStatus#_BUILDING}. Override the lock and rebuild it: a build
     * writes nothing to the database and rescans the session directory from scratch, so a second attempt is harmless.
     */
    FORCE_REBUILD,

    /**
     * The session is stranded in a lock left by an operation that modifies the session on disk. Clear the lock so the
     * session stops being untouchable, but do not rebuild it: leave it in {@link PrearcUtils.PrearcStatus#ERROR} for
     * an administrator to review.
     */
    UNLOCK_TO_ERROR,

    /** The session is locked and the requesting user isn't a site administrator. */
    LOCKED_REQUIRES_ADMIN
}
