/*
 * web: org.nrg.xnat.helpers.prearchive.PrearcLockRecovery
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;

/**
 * Decides how a prearchive session stranded in a locked status should be recovered when a user asks to rebuild it.
 * See XNAT-8767.
 * <p>
 * A session is locked when its status carries the "_" prefix, which {@link PrearcDatabase.LockAndSync} applies for the
 * duration of an operation and removes when the operation finishes. A crash leaves the prefix behind forever, and
 * {@link PrearcDatabase#setStatus(String, String, String, PrearcStatus, boolean)} then refuses every subsequent status
 * change, so the session can only be freed by overriding the lock. That is what a site administrator is doing when
 * they click Rebuild on a session that has been sitting in {@code _BUILDING} since the server crashed.
 * <p>
 * Only one lock is safe to rebuild. Each {@link PrearcDatabase.LockAndSync} runs from exactly one status, so a lock
 * identifies the operation that crashed: {@code _BUILDING} can only have come from
 * {@link PrearcDatabase#buildSession}, which writes nothing to the database and rescans the session directory from
 * scratch, making a second attempt harmless. Every other lock comes from an operation that moves or deletes files
 * before writing any database row -- see the moveScans calls in {@code PrearcDatabase._separatePetMrSession} -- so
 * rebuilding one of those would quietly produce a session missing whatever had already been moved. Those are unlocked
 * to {@link PrearcStatus#ERROR} instead, which is also the one status {@link SessionXMLRebuilder} never picks up, so
 * nothing rebuilds them behind the administrator's back.
 * <p>
 * <b>Known limitation:</b> nothing here distinguishes a crashed operation from a slow one. A large session sits in
 * {@code _BUILDING} for as long as its build takes, and an administrator who overrides that lock turns a second build
 * loose on a directory the first is still writing to. The prearchive carries no signal that would settle it --
 * {@code lastmod} is stamped once when the lock is taken and never refreshed while the operation runs, so it measures
 * how long the operation has been going, not how long it has been silent. Closing that hole needs a heartbeat, which
 * is tracked separately; today the confirmation dialog warning the administrator that the session "may be in the
 * middle of processing" is the only guard.
 * <p>
 * This class is pure: no I/O, no collaborators, no state. All inputs are arguments.
 */
public final class PrearcLockRecovery {
    private PrearcLockRecovery() {
        // Utility class.
    }

    /**
     * Decides what to do with a session the user has asked to rebuild.
     * <p>
     * Note that the caller's overrideLock parameter deliberately plays no part here. It cannot be read as "this user
     * wants to break a lock": {@code PrearchiveOperationRequest} reuses the same parameter as the allowUnassigned flag
     * for {@link PrearcUtils#getPrearcSessionDir}, so a non-administrator rebuilding a session in the Unassigned
     * prearchive has to pass it. Whether a lock may be broken is decided by the lock itself and the user's role.
     *
     * @param status      The session's current status.
     * @param isSiteAdmin Whether the requesting user holds the site administrator role.
     *
     * @return The action to take.
     */
    public static PrearcRecoveryAction decide(final PrearcStatus status, final boolean isSiteAdmin) {
        if (isQueued(status)) {
            return PrearcRecoveryAction.ALREADY_QUEUED;
        }
        if (!isLockedStatus(status)) {
            return PrearcRecoveryAction.PROCEED;
        }
        if (!isSiteAdmin) {
            return PrearcRecoveryAction.LOCKED_REQUIRES_ADMIN;
        }
        // Whitelist, not blacklist: a status is rebuildable only where we can show the crashed operation left the
        // session on disk untouched. Anything added to PrearcStatus later gets the safe answer by default.
        return status == PrearcStatus._BUILDING ? PrearcRecoveryAction.FORCE_REBUILD : PrearcRecoveryAction.UNLOCK_TO_ERROR;
    }

    /**
     * Whether the session is already waiting for an operation to pick it up, in which case asking for a rebuild is a
     * no-op rather than a failure. Mirrors the check {@link PrearcUtils#queuePrearchiveOperation} makes before it
     * refuses to queue a second operation.
     *
     * @param status The status to check.
     *
     * @return True if the session is already queued.
     */
    public static boolean isQueued(final PrearcStatus status) {
        return status != null && StringUtils.startsWith(status.name(), PrearcUtils.PREFIX_QUEUED);
    }

    /**
     * Whether a session in this status is locked. Reads the same map as
     * {@link PrearcDatabase#isLocked(String, String, String)} so the two cannot drift apart.
     *
     * @param status The status to check.
     *
     * @return True if a session in this status is locked.
     */
    public static boolean isLockedStatus(final PrearcStatus status) {
        return PrearcUtils.inProcessStatusMap.containsValue(status);
    }
}
