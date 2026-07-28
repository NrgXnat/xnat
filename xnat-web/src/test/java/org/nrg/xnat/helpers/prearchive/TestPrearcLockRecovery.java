/*
 * web: org.nrg.xnat.helpers.prearchive.TestPrearcLockRecovery
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import org.junit.Test;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction.ALREADY_QUEUED;
import static org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction.FORCE_REBUILD;
import static org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction.LOCKED_REQUIRES_ADMIN;
import static org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction.PROCEED;
import static org.nrg.xnat.helpers.prearchive.PrearcRecoveryAction.UNLOCK_TO_ERROR;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.BUILDING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.CONFLICT;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.ERROR;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_ARCHIVING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_BUILDING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_DELETING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_MOVING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_SEPARATING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.READY;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.RECEIVING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._ARCHIVING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._BUILDING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._CONFLICT;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._DELETING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._MOVING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._RECEIVING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._RECEIVING_INTERRUPT;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._SEPARATING;

/**
 * Tests the recovery strategy for prearchive sessions stranded in a locked ("_"-prefixed) status. See XNAT-8767.
 */
public class TestPrearcLockRecovery {

    /** Every locked status other than {@link PrearcStatus#_BUILDING}. */
    private static final Set<PrearcStatus> NON_REBUILDABLE_LOCKS = EnumSet.of(_ARCHIVING, _SEPARATING, _MOVING, _DELETING, _CONFLICT, _RECEIVING, _RECEIVING_INTERRUPT);

    /** The reason XNAT-8767 exists: a crash strands the session in _BUILDING and nothing can clear it. */
    @Test
    public void siteAdminForcesRebuildOfSessionStrandedInBuilding() {
        assertThat(PrearcLockRecovery.decide(_BUILDING, true)).isEqualTo(FORCE_REBUILD);
    }

    /**
     * _BUILDING is the only lock we rebuild. The others are left by operations that move or delete files before
     * writing any database row, so rebuilding them would silently produce a truncated session.
     */
    @Test
    public void nonRebuildableLocksAreOnlyUnlockedToErrorNeverRebuilt() {
        for (final PrearcStatus status : NON_REBUILDABLE_LOCKS) {
            assertThat(PrearcLockRecovery.decide(status, true)).describedAs("status %s must never be rebuilt", status).isEqualTo(UNLOCK_TO_ERROR);
        }
    }

    @Test
    public void nonAdminHittingALockedSessionIsRefusedRatherThanSilentlyIgnored() {
        assertThat(PrearcLockRecovery.decide(_BUILDING, false)).isEqualTo(LOCKED_REQUIRES_ADMIN);
        assertThat(PrearcLockRecovery.decide(_ARCHIVING, false)).isEqualTo(LOCKED_REQUIRES_ADMIN);
    }

    /**
     * Guards a real regression. PrearchiveOperationRequest reuses the overrideLock parameter as getPrearcSessionDir's
     * allowUnassigned flag, so a non-admin rebuilding a session in the Unassigned prearchive has to pass
     * overrideLock=true today -- that is the only way the flag gets through. An unlocked session must therefore never
     * be refused on account of that flag: the lock status alone decides.
     */
    @Test
    public void unlockedSessionsProceedForEveryoneRegardlessOfRole() {
        for (final PrearcStatus status : Arrays.asList(READY, ERROR, RECEIVING, CONFLICT, BUILDING)) {
            assertThat(PrearcLockRecovery.decide(status, true)).describedAs("admin, status %s", status).isEqualTo(PROCEED);
            assertThat(PrearcLockRecovery.decide(status, false)).describedAs("non-admin, status %s", status).isEqualTo(PROCEED);
        }
    }

    /**
     * A session that is already queued has nothing wrong with it -- asking for a rebuild is simply redundant and must
     * count as a success. Treating queuePrearchiveOperation's refusal to queue it twice as a failure would turn that
     * into an error for a user who did nothing wrong.
     */
    @Test
    public void alreadyQueuedSessionsAreANoOpForEveryone() {
        for (final PrearcStatus status : Arrays.asList(QUEUED_BUILDING, QUEUED_ARCHIVING, QUEUED_MOVING, QUEUED_DELETING, QUEUED_SEPARATING)) {
            assertThat(PrearcLockRecovery.decide(status, true)).describedAs("admin, status %s", status).isEqualTo(ALREADY_QUEUED);
            assertThat(PrearcLockRecovery.decide(status, false)).describedAs("non-admin, status %s", status).isEqualTo(ALREADY_QUEUED);
        }
    }

    /** isQueued must agree with the check queuePrearchiveOperation makes, or the two will disagree about what to do. */
    @Test
    public void isQueuedMatchesTheQueuedPrefixCheckInQueuePrearchiveOperation() {
        for (final PrearcStatus status : PrearcStatus.values()) {
            assertThat(PrearcLockRecovery.isQueued(status)).describedAs("status %s", status).isEqualTo(status.toString().startsWith(PrearcUtils.PREFIX_QUEUED));
        }
    }

    /** Guards the whitelist: a PrearcStatus added later must not silently become force-rebuildable. */
    @Test
    public void onlyBuildingIsEverForceRebuiltAndUnlockedStatusesAlwaysProceed() {
        for (final PrearcStatus status : PrearcStatus.values()) {
            final PrearcRecoveryAction action = PrearcLockRecovery.decide(status, true);
            assertThat(action).describedAs("status %s has no decision", status).isNotNull();
            if (PrearcLockRecovery.isLockedStatus(status)) {
                assertThat(action == FORCE_REBUILD).describedAs("only _BUILDING may be force-rebuilt, not %s", status).isEqualTo(status == _BUILDING);
            } else if (PrearcLockRecovery.isQueued(status)) {
                assertThat(action).describedAs("queued status %s", status).isEqualTo(ALREADY_QUEUED);
            } else {
                assertThat(action).describedAs("unlocked status %s", status).isEqualTo(PROCEED);
            }
        }
    }

    /**
     * The strategy must agree with PrearcDatabase.isLocked() about what "locked" means, or a session it declares
     * recoverable would still be refused by PrearcDatabase.setStatus().
     */
    @Test
    public void lockedStatusAgreesWithTheMapBackingPrearcDatabaseIsLocked() {
        for (final PrearcStatus status : PrearcStatus.values()) {
            assertThat(PrearcLockRecovery.isLockedStatus(status)).describedAs("status %s", status).isEqualTo(PrearcUtils.inProcessStatusMap.containsValue(status));
        }
    }
}
