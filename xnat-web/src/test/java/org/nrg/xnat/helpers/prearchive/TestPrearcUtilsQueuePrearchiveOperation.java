/*
 * web: org.nrg.xnat.helpers.prearchive.TestPrearcUtilsQueuePrearchiveOperation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import java.io.File;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.nrg.xdat.XDAT;
import org.nrg.xnat.archive.Operation;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.ERROR;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_BUILDING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.READY;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus._BUILDING;

/**
 * Tests that {@link PrearcUtils#queuePrearchiveOperation} can override a session lock on behalf of a site
 * administrator, and that failing to queue the operation afterwards doesn't strand the session again. See XNAT-8767.
 */
public class TestPrearcUtilsQueuePrearchiveOperation {
    private MockedStatic<PrearcDatabase> mockedPrearcDatabase;
    private MockedStatic<XDAT>           mockedXDAT;

    @Before
    public void setUp() {
        // PrearcDatabase is a pure collaborator here -- the method under test lives on PrearcUtils -- so stubbing the
        // whole class is safe and keeps the test off the database entirely.
        mockedPrearcDatabase = Mockito.mockStatic(PrearcDatabase.class);
        mockedXDAT           = Mockito.mockStatic(XDAT.class);
    }

    @After
    public void tearDown() {
        mockedXDAT.closeOnDemand();
        mockedPrearcDatabase.closeOnDemand();
    }

    private static PrearchiveOperationRequest requestForSessionIn(final PrearcStatus status) {
        final SessionData sessionData = new SessionData().setFolderName("Sample_ID").setTimestamp("20260703_152142536").setProject("PROJ").setStatus(status);
        return new PrearchiveOperationRequest("admin", Operation.Rebuild, sessionData, new File("/data/prearchive/PROJ/20260703_152142536/Sample_ID"), new HashMap<>());
    }

    /**
     * The overrideLock flag has to reach PrearcDatabase.setStatus, or a locked session can never be queued no matter
     * what the caller asked for. This is the plumbing XNAT-8767 exists to connect.
     */
    @Test
    public void overrideLockReachesSetStatusSoALockedSessionCanBeQueued() throws Exception {
        mockedPrearcDatabase.when(() -> PrearcDatabase.setStatus(any(SessionData.class), any(PrearcStatus.class), eq(true))).thenReturn(true);

        assertThat(PrearcUtils.queuePrearchiveOperation(requestForSessionIn(_BUILDING), true)).isTrue();

        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(any(SessionData.class), eq(QUEUED_BUILDING), eq(true)));
        mockedXDAT.verify(() -> XDAT.sendJmsRequest(any()));
    }

    /**
     * Every existing caller goes through the single-argument entry point and must keep respecting the lock.
     */
    @Test
    public void existingCallersStillRespectTheLock() throws Exception {
        mockedPrearcDatabase.when(() -> PrearcDatabase.setStatus(any(SessionData.class), any(PrearcStatus.class), eq(false))).thenReturn(true);

        assertThat(PrearcUtils.queuePrearchiveOperation(requestForSessionIn(READY))).isTrue();

        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(any(SessionData.class), eq(QUEUED_BUILDING), eq(false)));
    }

    /**
     * A locked session that we can't queue must still report failure rather than pretending it worked.
     */
    @Test
    public void refusedStatusChangeReportsFailureAndDoesNotQueue() throws Exception {
        mockedPrearcDatabase.when(() -> PrearcDatabase.setStatus(any(SessionData.class), any(PrearcStatus.class), Mockito.anyBoolean())).thenReturn(false);

        assertThat(PrearcUtils.queuePrearchiveOperation(requestForSessionIn(_BUILDING), false)).isFalse();

        mockedXDAT.verifyNoInteractions();
    }

    /**
     * The rollback is the whole reason forcing a lock is delicate. Once we've forced _BUILDING to QUEUED_BUILDING the
     * session is no longer locked, so restoring the original status would succeed -- and put the session straight back
     * into the lock the administrator just asked us to clear. It has to land in ERROR instead.
     */
    @Test
    public void jmsFailureAfterForcingALockLeavesTheSessionInErrorNotBackInTheLock() {
        mockedPrearcDatabase.when(() -> PrearcDatabase.setStatus(any(SessionData.class), any(PrearcStatus.class), Mockito.anyBoolean())).thenReturn(true);
        mockedXDAT.when(() -> XDAT.sendJmsRequest(any())).thenThrow(new RuntimeException("broker down"));

        assertThatThrownBy(() -> PrearcUtils.queuePrearchiveOperation(requestForSessionIn(_BUILDING), true)).hasMessage("broker down");

        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(any(SessionData.class), eq(ERROR), eq(true)));
        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(any(SessionData.class), eq(_BUILDING), Mockito.anyBoolean()), Mockito.never());
    }

    /**
     * When we didn't force anything, the pre-existing rollback behaviour must not change -- including the fact that it
     * does not override a lock. Something else may have legitimately taken the row while the JMS send was failing, and
     * restoring the original status over that lock would clobber it. The flag is asserted with eq(false), not a
     * wildcard: it is the one thing this test exists to pin.
     */
    @Test
    public void jmsFailureWithoutForcingRestoresTheOriginalStatusWithoutOverridingAnyLock() {
        mockedPrearcDatabase.when(() -> PrearcDatabase.setStatus(any(SessionData.class), any(PrearcStatus.class), Mockito.anyBoolean())).thenReturn(true);
        mockedXDAT.when(() -> XDAT.sendJmsRequest(any())).thenThrow(new RuntimeException("broker down"));

        assertThatThrownBy(() -> PrearcUtils.queuePrearchiveOperation(requestForSessionIn(READY))).hasMessage("broker down");

        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(any(SessionData.class), eq(READY), eq(false)));
        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(any(SessionData.class), any(PrearcStatus.class), eq(true)), Mockito.never());
    }

    /**
     * Forcing past a lock that was never there is still not a reason to force the rollback: if the session was not
     * locked to begin with, the rollback restores its original status rather than dumping it in ERROR.
     */
    @Test
    public void forcingAnUnlockedSessionStillRollsBackToItsOriginalStatus() {
        mockedPrearcDatabase.when(() -> PrearcDatabase.setStatus(any(SessionData.class), any(PrearcStatus.class), Mockito.anyBoolean())).thenReturn(true);
        mockedXDAT.when(() -> XDAT.sendJmsRequest(any())).thenThrow(new RuntimeException("broker down"));

        assertThatThrownBy(() -> PrearcUtils.queuePrearchiveOperation(requestForSessionIn(READY), true)).hasMessage("broker down");

        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(any(SessionData.class), eq(READY), eq(true)));
        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(any(SessionData.class), eq(ERROR), Mockito.anyBoolean()), Mockito.never());
    }

    /**
     * Forcing a lock must not become a way to queue the same operation twice.
     */
    @Test
    public void alreadyQueuedSessionsAreNotRequeuedEvenWhenForcing() throws Exception {
        assertThat(PrearcUtils.queuePrearchiveOperation(requestForSessionIn(QUEUED_BUILDING), true)).isFalse();

        mockedPrearcDatabase.verifyNoInteractions();
        mockedXDAT.verifyNoInteractions();
    }
}
