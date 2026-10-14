package org.nrg.xnat.archive.services.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
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
import static org.mockito.Mockito.when;

/**
 * An upload that fails partway leaves its direct-archive session holding only part of a study. Left receiving, the
 * archive trigger would archive it once it had been idle long enough. Instead it goes to the prearchive in ERROR, as a
 * session whose build or archive failed does, unless its files are not its own to move.
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

        final PrearcDatabase.Either<SessionData, SessionData> created = mock(PrearcDatabase.Either.class);
        when(created.isLeft()).thenReturn(true);
        when(created.getLeft()).thenAnswer(invocation -> recorded.get());
        mockedPrearcDatabase = Mockito.mockStatic(PrearcDatabase.class);
        mockedPrearcDatabase.when(() -> PrearcDatabase.eitherGetOrCreateSession(any(), any(), any())).thenAnswer(invocation -> {
            recorded.set(invocation.getArgument(0));
            return created;
        });

        when(hibernateService.setStatusToErrorIfReceiving(SESSION_ID, cause)).thenReturn(true);
    }

    @After
    public void tearDown() {
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

    private void assertLeftInPlace() throws Exception {
        assertThat(new File(sessionDirectory, "1.dcm")).isFile();
        assertThat(prearchiveDirectory).doesNotExist();
        mockedPrearcDatabase.verify(() -> PrearcDatabase.eitherGetOrCreateSession(any(), any(), any()), never());
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void aFailedUploadMovesItsSessionToThePrearchiveInErrorWithTheReasonInItsLog() throws Exception {
        service.handleFailedUpload(receivingSession(), cause);

        assertThat(sessionDirectory).doesNotExist();
        assertThat(new File(prearchiveDirectory, "1.dcm")).isFile();
        assertThat(Files.readString(new File(prearchiveDirectory, "logs/directArchive" + SESSION_ID + ".log").toPath()))
                .contains("An import into this session failed partway").contains("unable to read data from file");
        assertThat(recorded.get().getStatus()).isEqualTo(PrearcStatus.ERROR);
        assertThat(recorded.get().getUrl()).isEqualTo(prearchiveDirectory.getAbsolutePath());
        // Queued for a rebuild instead, the partial session would be archived after all.
        mockedPrearcUtils.verify(() -> PrearcUtils.queuePrearchiveOperation(any()), never());
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void aSessionThatHasMovedOnIsLeftAlone() throws Exception {
        // Queued for building, claimed by a delete, or already failed: not the importer's to move.
        when(hibernateService.setStatusToErrorIfReceiving(SESSION_ID, cause)).thenReturn(false);

        service.handleFailedUpload(receivingSession(), cause);

        assertLeftInPlace();
    }

    @Test
    public void aMergeIntoAnArchivedExperimentStaysInErrorWhereItIs() throws Exception {
        // Its directory is the experiment's, so moving it would take the archived files to the prearchive too.
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(eq(PROJECT), eq(SESSION), any(), anyBoolean()))
                         .thenReturn(mock(XnatExperimentdata.class));

        service.handleFailedUpload(receivingSession(), cause);

        verify(hibernateService).setStatusToErrorIfReceiving(SESSION_ID, cause);
        assertLeftInPlace();
    }

    @Test
    public void aSessionStillReceivingAnotherUploadsFileStaysInErrorWhereItIs() throws Exception {
        // That file passed the importer's check before the claim and is still being written.
        mockedPrearcUtils.when(() -> PrearcUtils.isSessionReceiving(any())).thenReturn(true);

        service.handleFailedUpload(receivingSession(), cause);

        verify(hibernateService).setStatusToErrorIfReceiving(SESSION_ID, cause);
        assertLeftInPlace();
    }
}
