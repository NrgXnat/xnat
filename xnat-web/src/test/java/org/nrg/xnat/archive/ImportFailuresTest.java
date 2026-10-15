package org.nrg.xnat.archive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * An import that fails partway, a compressed upload or an inbox folder, must leave the sessions it wrote into marked,
 * whichever route they took, so that nothing archives what may be only part of a study.
 */
public class ImportFailuresTest {
    private static final String PROJECT            = "PROJ";
    private static final String TIMESTAMP          = "20260930_120000";
    private static final String SESSION            = "SESSION_01";
    private static final String PREARCHIVE_URI     = "/prearchive/projects/" + PROJECT + "/" + TIMESTAMP + "/" + SESSION;
    private static final String DIRECT_ARCHIVE_URI = "/xapi/direct-archive/" + PROJECT + "/1.2.3/" + SESSION;

    private final Exception cause = new ServerException("unable to write the object");

    private MockedStatic<PrearcUtils>    mockedPrearcUtils;
    private MockedStatic<PrearcDatabase> mockedPrearcDatabase;
    private MockedStatic<XDAT>           mockedXDAT;
    private DirectArchiveSessionService  directArchive;

    @Before
    public void setUp() {
        mockedPrearcUtils    = Mockito.mockStatic(PrearcUtils.class);
        mockedPrearcUtils.when(() -> PrearcUtils.parseURI(anyString())).thenCallRealMethod();
        mockedPrearcDatabase = Mockito.mockStatic(PrearcDatabase.class);
        mockedPrearcDatabase.when(() -> PrearcDatabase.setStatus(anyString(), anyString(), anyString(), any(PrearcStatus.class))).thenReturn(true);
        // Still receiving, unless a test says otherwise.
        mockedPrearcDatabase.when(() -> PrearcDatabase.getSession(SESSION, TIMESTAMP, PROJECT)).thenReturn(new SessionData().setStatus(PrearcStatus.RECEIVING));
        directArchive = mock(DirectArchiveSessionService.class);
        final ContextService contextService = mock(ContextService.class);
        when(contextService.getBean(DirectArchiveSessionService.class)).thenReturn(directArchive);
        mockedXDAT = Mockito.mockStatic(XDAT.class);
        mockedXDAT.when(XDAT::getContextService).thenReturn(contextService);
    }

    @After
    public void tearDown() {
        mockedXDAT.closeOnDemand();
        mockedPrearcDatabase.closeOnDemand();
        mockedPrearcUtils.closeOnDemand();
    }

    @Test
    public void onlyAnObjectCutShortEndsEarly() {
        final ClientException cutShort = new ClientException("unable to read DICOM object", new ReceivedDicomObject.TruncatedObjectException("ends 10 bytes short"));
        assertTrue(ImportFailures.endsEarly(cutShort));
        assertFalse("an object that isn't DICOM", ImportFailures.endsEarly(new ClientException("unable to read DICOM object", new IOException("Not a DICOM stream"))));
        assertFalse("a stream that runs out with nothing to say it was DICOM", ImportFailures.endsEarly(new ClientException("unable to read DICOM object", new EOFException())));
    }

    @Test
    public void aPrearchiveSessionIsMarkedErrorWithTheReasonInItsLog() throws Exception {
        ImportFailures.markFailed(Collections.singletonList(PREARCHIVE_URI), cause);

        mockedPrearcUtils.verify(() -> PrearcUtils.log(eq(PROJECT), eq(TIMESTAMP), eq(SESSION), contains("failed partway")));
        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(SESSION, TIMESTAMP, PROJECT, PrearcStatus.ERROR));
        verify(directArchive, never()).handleFailedUpload(any(), any());
    }

    @Test
    public void aSessionQueuedForOrInTheMiddleOfAnOperationIsLeftToIt() throws Exception {
        // A zip that runs past the idle timeout can fail after an earlier study in it was queued for archiving. The
        // queued operation runs whatever the status says, so ERROR wouldn't keep it from running.
        final EnumSet<PrearcStatus> markable = EnumSet.of(PrearcStatus.RECEIVING, PrearcStatus.RECEIVING_INTERRUPT,
                                                          PrearcStatus.READY, PrearcStatus.CONFLICT, PrearcStatus.ERROR);
        for (final PrearcStatus status : EnumSet.complementOf(markable)) {
            mockedPrearcDatabase.when(() -> PrearcDatabase.getSession(SESSION, TIMESTAMP, PROJECT)).thenReturn(new SessionData().setStatus(status));

            ImportFailures.markFailed(Collections.singletonList(PREARCHIVE_URI), cause);
        }

        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(anyString(), anyString(), anyString(), any(PrearcStatus.class)), never());
        mockedPrearcUtils.verify(() -> PrearcUtils.log(anyString(), anyString(), anyString(), anyString()), never());
    }

    @Test
    public void aDirectArchiveSessionIsHandedToItsService() throws Exception {
        final SessionData session = new SessionData();
        when(directArchive.findByProjectTagName(PROJECT, "1.2.3", SESSION)).thenReturn(session);

        ImportFailures.markFailed(Collections.singletonList(DIRECT_ARCHIVE_URI), cause);

        verify(directArchive).handleFailedUpload(session, cause);
        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(anyString(), anyString(), anyString(), any(PrearcStatus.class)), never());
    }

    @Test
    public void aSessionThatCannotBeMarkedDoesNotStopTheRest() throws Exception {
        when(directArchive.findByProjectTagName(PROJECT, "1.2.3", SESSION)).thenThrow(new NotFoundException("already gone"));

        ImportFailures.markFailed(Arrays.asList(DIRECT_ARCHIVE_URI, PREARCHIVE_URI), cause);

        mockedPrearcDatabase.verify(() -> PrearcDatabase.setStatus(SESSION, TIMESTAMP, PROJECT, PrearcStatus.ERROR));
    }
}
