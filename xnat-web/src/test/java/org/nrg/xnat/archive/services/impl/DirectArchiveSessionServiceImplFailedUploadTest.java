package org.nrg.xnat.archive.services.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xnat.archive.ImportFailures;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * An upload that fails partway leaves its direct-archive session holding only part of a study. The session is marked
 * and keeps receiving, since another upload may still be sending into it. Once it goes idle, the archive trigger moves
 * it to the prearchive in ERROR, as a session whose build or archive failed does, unless its files are not its own to
 * move.
 */
public class DirectArchiveSessionServiceImplFailedUploadTest {
    private static final long   SESSION_ID = 42L;
    private static final String PROJECT    = "PROJ";
    private static final String SESSION    = "SESSION_01";
    private static final String TIMESTAMP  = "20260930_120000";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private DirectArchiveSessionHibernateService hibernateService;
    private DirectArchiveSessionServiceImpl      service;
    private MockedStatic<PrearcUtils>            mockedPrearcUtils;
    private MockedStatic<PrearcDatabase>         mockedPrearcDatabase;
    private MockedStatic<BaseXnatProjectdata>    mockedProjects;
    private MockedStatic<BaseXnatExperimentdata> mockedExperiments;
    private MockedStatic<XDAT>                   mockedXDAT;

    private File sessionDirectory;
    private File prearchiveDirectory;

    /** The session the move recorded in the prearchive. */
    private final AtomicReference<SessionData> recorded = new AtomicReference<>();

    private final Exception cause = new Exception("unable to read data from file");

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        hibernateService = mock(DirectArchiveSessionHibernateService.class);
        service          = new DirectArchiveSessionServiceImpl(hibernateService,
                                                               mock(JmsTemplate.class),
                                                               mock(XnatUserProvider.class),
                                                               mock(GroupsAndPermissionsCache.class),
                                                               mock(PermissionsServiceI.class));

        sessionDirectory    = temporaryFolder.newFolder("archive", PROJECT, "arc001", SESSION);
        prearchiveDirectory = new File(temporaryFolder.getRoot(), String.join(File.separator, "prearchive", PROJECT, TIMESTAMP, SESSION));
        Files.writeString(new File(sessionDirectory, "1.dcm").toPath(), "dicom");

        // Nothing is still landing and no archived experiment owns the directory unless a test says so.
        mockedPrearcUtils = Mockito.mockStatic(PrearcUtils.class);
        mockedPrearcUtils.when(() -> PrearcUtils.isSessionReceiving(any())).thenReturn(false);
        mockedPrearcUtils.when(() -> PrearcUtils.getPrearcSessionDir(any(), eq(PROJECT), eq(TIMESTAMP), eq(SESSION), eq(true))).thenReturn(prearchiveDirectory);
        mockedExperiments = Mockito.mockStatic(BaseXnatExperimentdata.class);
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(any(), any(), any(), anyBoolean())).thenReturn(null);
        final XnatProjectdata project = mock(XnatProjectdata.class);
        when(project.getRootArchivePath()).thenReturn(sessionDirectory.getParentFile().getParent());
        mockedProjects = Mockito.mockStatic(BaseXnatProjectdata.class);
        mockedProjects.when(() -> BaseXnatProjectdata.getProjectByIDorAlias(eq(PROJECT), any(), anyBoolean())).thenReturn(project);

        // No prearchive row at the move's folder yet, unless a test says so.
        mockedPrearcDatabase = Mockito.mockStatic(PrearcDatabase.class);
        mockedPrearcDatabase.when(() -> PrearcDatabase.countOf(any(), any(), any())).thenReturn(0);
        mockedPrearcDatabase.when(() -> PrearcDatabase.addSession(any())).thenAnswer(invocation -> {
            recorded.set(invocation.getArgument(0));
            return null;
        });
        mockedPrearcDatabase.when(() -> PrearcDatabase.getSession(any(), any(), any())).thenAnswer(invocation -> recorded.get());

        mockedXDAT = Mockito.mockStatic(XDAT.class);

        // The session has gone idle still marked, unless a test says otherwise.
        when(hibernateService.markFailedUpload(SESSION_ID, cause)).thenReturn(true);
        when(hibernateService.findReadyForArchive()).thenAnswer(invocation -> List.of(idleSession()));
        when(hibernateService.hasFailedUpload(SESSION_ID)).thenReturn(true);
        when(hibernateService.claimIdleFailedUpload(eq(SESSION_ID), any())).thenReturn(true);
    }

    @After
    public void tearDown() {
        mockedXDAT.closeOnDemand();
        mockedPrearcDatabase.closeOnDemand();
        mockedProjects.closeOnDemand();
        mockedExperiments.closeOnDemand();
        mockedPrearcUtils.closeOnDemand();
    }

    private SessionData receivingSession() {
        final SessionData session = new SessionData().setProject(PROJECT).setTag("1.2.3").setName(SESSION).setFolderName(SESSION)
                                                     .setTimestamp(TIMESTAMP).setUrl(sessionDirectory.getAbsolutePath())
                                                     .setStatus(PrearcStatus.RECEIVING);
        session.setId(SESSION_ID);
        return session;
    }

    /** The session as the archive trigger finds it, with the failure the mark recorded. */
    private SessionData idleSession() {
        final SessionData session = receivingSession();
        session.setMessage(cause.getMessage());
        return session;
    }

    private void assertLeftInPlace() throws Exception {
        assertThat(new File(sessionDirectory, "1.dcm")).isFile();
        assertThat(prearchiveDirectory).doesNotExist();
        mockedPrearcDatabase.verify(() -> PrearcDatabase.addSession(any()), never());
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void aFailedUploadMarksItsSessionAndLeavesItReceivingWhereItIs() throws Exception {
        // Another upload may still be sending into it.
        service.handleFailedUpload(receivingSession(), cause);

        verify(hibernateService).markFailedUpload(SESSION_ID, cause);
        verify(hibernateService, never()).claimIdleFailedUpload(anyLong(), any());
        assertLeftInPlace();
    }

    @Test
    public void anIdleSessionStillMarkedMovesToThePrearchiveInErrorWithANoteInItsLog() throws Exception {
        service.triggerArchive();

        assertThat(sessionDirectory).doesNotExist();
        assertThat(new File(prearchiveDirectory, "1.dcm")).isFile();
        assertThat(Files.readString(new File(prearchiveDirectory, "logs/directArchive" + SESSION_ID + ".log").toPath()))
                .contains(ImportFailures.FAILED_PARTWAY)
                // The note only: the reason can name the uploader's file.
                .doesNotContain(cause.getMessage());
        assertThat(recorded.get().getStatus()).isEqualTo(PrearcStatus.ERROR);
        assertThat(recorded.get().getUrl()).isEqualTo(prearchiveDirectory.getAbsolutePath());
        // Looked up by study instead, it would join any other prearchive session of the study and leave these files
        // without a row.
        mockedPrearcDatabase.verify(() -> PrearcDatabase.eitherGetOrCreateSession(any(), any(), any()), never());
        // Queued for a rebuild instead, the partial session would be archived after all.
        mockedPrearcUtils.verify(() -> PrearcUtils.queuePrearchiveOperation(any()), never());
        verify(hibernateService, never()).setStatusToQueuedBuilding(anyLong(), anyBoolean());
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void theLogGivesEachReasonDownTheCauseChainWithoutStackFrames() {
        final Exception failure = new Exception("Unable to build the session", new IOException("No space left on device"));

        assertThat(DirectArchiveSessionServiceImpl.reasonOf(failure))
                .isEqualTo("Unable to build the session\nCaused by: No space left on device\n");
        assertThat(DirectArchiveSessionServiceImpl.reasonOf(new NullPointerException())).isEqualTo("NullPointerException\n");
    }

    @Test
    public void aRowAlreadyAtTheMovesFolderGivesTheMoveANewTimestamp() throws Exception {
        final File daDirectory = new File(temporaryFolder.getRoot(), String.join(File.separator, "prearchive", PROJECT, TIMESTAMP + "_DA", SESSION));
        mockedPrearcUtils.when(() -> PrearcUtils.getPrearcSessionDir(any(), eq(PROJECT), eq(TIMESTAMP + "_DA"), eq(SESSION), eq(true))).thenReturn(daDirectory);
        mockedPrearcDatabase.when(() -> PrearcDatabase.countOf(SESSION, TIMESTAMP, PROJECT)).thenReturn(1);

        service.triggerArchive();

        assertThat(new File(daDirectory, "1.dcm")).isFile();
        assertThat(recorded.get().getTimestamp()).isEqualTo(TIMESTAMP + "_DA");
        assertThat(recorded.get().getUrl()).isEqualTo(daDirectory.getAbsolutePath());
    }

    @Test
    public void anEmptySessionXmlLeftByABuildDoesNotStopTheMove() throws Exception {
        // A build that produced no session, such as one of a session that holds no instances, leaves the XML empty.
        // Parsed after the files had moved, it failed the move and left them in the prearchive with no row.
        Files.createFile(new File(sessionDirectory.getPath() + ".xml").toPath());

        service.triggerArchive();

        assertThat(new File(prearchiveDirectory, "1.dcm")).isFile();
        assertThat(recorded.get().getStatus()).isEqualTo(PrearcStatus.ERROR);
        assertThat(recorded.get().getUrl()).isEqualTo(prearchiveDirectory.getAbsolutePath());
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void aLaterFileLeavesTheMarkInPlace() throws Exception {
        // The later file may fail too, and C-STORE and single-file uploads never mark a session.
        when(hibernateService.getSessionData(SESSION_ID)).thenReturn(receivingSession());

        service.requireReceiving(receivingSession());

        verify(hibernateService).getSessionData(SESSION_ID);
        verifyNoMoreInteractions(hibernateService);
    }

    @Test
    public void anIdleSessionWithNoFailedUploadIsArchivedAsUsual() throws Exception {
        when(hibernateService.hasFailedUpload(SESSION_ID)).thenReturn(false);
        when(hibernateService.setStatusToQueuedBuilding(SESSION_ID, false)).thenReturn(true);

        service.triggerArchive();

        verify(hibernateService).setStatusToQueuedBuilding(SESSION_ID, false);
        mockedXDAT.verify(() -> XDAT.sendJmsRequest(any(), any()));
        verify(hibernateService, never()).claimIdleFailedUpload(anyLong(), any());
        assertLeftInPlace();
    }

    @Test
    public void aSessionThatHasMovedOnIsLeftAlone() throws Exception {
        // Claimed by a delete, or already failed: not the trigger's to move.
        when(hibernateService.claimIdleFailedUpload(eq(SESSION_ID), any())).thenReturn(false);

        service.triggerArchive();

        assertLeftInPlace();
    }

    @Test
    public void aMergeIntoAnArchivedExperimentStaysInErrorWhereItIs() throws Exception {
        // Its directory is the experiment's, so moving it would take the archived files to the prearchive too.
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(eq(PROJECT), eq(SESSION), any(), anyBoolean()))
                         .thenReturn(mock(XnatExperimentdata.class));

        service.triggerArchive();

        verify(hibernateService).claimIdleFailedUpload(eq(SESSION_ID), any());
        assertLeftInPlace();
    }

    @Test
    public void aSessionAFileIsStillLandingInStaysInErrorWhereItIs() throws Exception {
        // That file passed the importer's check before the claim and is still being written.
        mockedPrearcUtils.when(() -> PrearcUtils.isSessionReceiving(any())).thenReturn(true);

        service.triggerArchive();

        verify(hibernateService).claimIdleFailedUpload(eq(SESSION_ID), any());
        assertLeftInPlace();
    }
}
