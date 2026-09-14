package org.nrg.xnat.archive.services.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.restlet.data.Status;
import org.springframework.jms.core.JmsTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * XNAT-7944: deleting a direct archive session through the user-facing API must remove the session's files from the
 * archive directory, not just the tracking row. The delete must also refuse to touch a session the archiver is working
 * on, and must leave the directory alone when it belongs to something other than the session being deleted.
 */
public class DirectArchiveSessionServiceImplDeleteTest {
    private static final long   SESSION_ID = 42L;
    private static final String PROJECT    = "PROJ";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private DirectArchiveSessionHibernateService hibernateService;
    private DirectArchiveSessionServiceImpl      service;
    private UserI                                user;
    private MockedStatic<Permissions>            mockedPermissions;

    private File arc;
    private File sessionDirectory;
    private File sessionXml;

    @Before
    public void setUp() throws Exception {
        hibernateService = mock(DirectArchiveSessionHibernateService.class);
        user             = mock(UserI.class);
        service          = new DirectArchiveSessionServiceImpl(hibernateService,
                                                               mock(JmsTemplate.class),
                                                               mock(XnatUserProvider.class),
                                                               mock(GroupsAndPermissionsCache.class),
                                                               mock(PermissionsServiceI.class));

        arc = temporaryFolder.newFolder("archive", PROJECT, "arc001");
        sessionDirectory = new File(arc, "SESSION_01");
        assertThat(sessionDirectory.mkdir()).isTrue();
        Files.write(new File(sessionDirectory, "1.dcm").toPath(), "dicom".getBytes(StandardCharsets.UTF_8));
        sessionXml = new File(arc, "SESSION_01.xml");
        Files.write(sessionXml.toPath(), "<xml/>".getBytes(StandardCharsets.UTF_8));

        mockedPermissions = Mockito.mockStatic(Permissions.class);
        mockedPermissions.when(() -> Permissions.canDeleteProject(user, PROJECT)).thenReturn(true);
    }

    @After
    public void tearDown() {
        mockedPermissions.closeOnDemand();
        assertThat(arc.setWritable(true)).isTrue();
    }

    private SessionData sessionIn(final PrearcStatus status) {
        return new SessionData().setProject(PROJECT).setTag("1.2.3").setName("SESSION_01").setFolderName("SESSION_01")
                                .setUrl(sessionDirectory.getAbsolutePath()).setStatus(status);
    }

    private SessionData stubSession(final PrearcStatus status) throws NotFoundException {
        final SessionData session = sessionIn(status);
        session.setId(SESSION_ID);
        when(hibernateService.getSessionData(SESSION_ID)).thenReturn(session);
        when(hibernateService.getOverwriteMode(SESSION_ID)).thenReturn(null);
        when(hibernateService.findByLocation(sessionDirectory.getAbsolutePath())).thenReturn(Collections.singletonList(session));
        return session;
    }

    private void assertFilesIntact() {
        assertThat(sessionDirectory).isDirectory();
        assertThat(new File(sessionDirectory, "1.dcm")).isFile();
        assertThat(sessionXml).isFile();
    }

    private void assertFilesGone() {
        assertThat(sessionDirectory).doesNotExist();
        assertThat(sessionXml).doesNotExist();
    }

    @Test
    public void deletingAnErrorSessionRemovesDirectoryXmlAndRow() throws Exception {
        stubSession(PrearcStatus.ERROR);

        service.delete(SESSION_ID, user);

        assertFilesGone();
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void deletingAReceivingSessionRemovesDirectoryXmlAndRow() throws Exception {
        stubSession(PrearcStatus.RECEIVING);

        service.delete(SESSION_ID, user);

        assertFilesGone();
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void deletingASessionTheArchiverIsWorkingOnIsRefusedWithConflict() throws Exception {
        final List<PrearcStatus> inFlight = Arrays.asList(PrearcStatus.BUILDING, PrearcStatus.ARCHIVING,
                                                          PrearcStatus.QUEUED_BUILDING, PrearcStatus.QUEUED_ARCHIVING);
        for (final PrearcStatus status : inFlight) {
            stubSession(status);

            assertThatThrownBy(() -> service.delete(SESSION_ID, user))
                    .as("status %s", status)
                    .isInstanceOf(ClientException.class)
                    .extracting(e -> ((ClientException) e).getStatus())
                    .isEqualTo(Status.CLIENT_ERROR_CONFLICT);

            assertFilesIntact();
        }
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void deletingWithoutProjectDeletePermissionLeavesFilesAndRow() throws Exception {
        stubSession(PrearcStatus.ERROR);
        mockedPermissions.when(() -> Permissions.canDeleteProject(user, PROJECT)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(SESSION_ID, user)).isInstanceOf(InvalidPermissionException.class);

        assertFilesIntact();
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void deletingAMergeSessionKeepsTheArchivedDirectory() throws Exception {
        // With an overwrite mode set, the "location" is an already-archived session that this direct archive
        // session was appending to. Those files are not ours to delete.
        stubSession(PrearcStatus.ERROR);
        when(hibernateService.getOverwriteMode(SESSION_ID)).thenReturn("append");

        service.delete(SESSION_ID, user);

        assertFilesIntact();
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void deletingAnErrorSessionKeepsFilesWhenAnotherActiveSessionSharesTheLocation() throws Exception {
        // create() allows a new session at the same location once the old one has errored, so the directory may
        // now belong to a newer session that is still receiving.
        final SessionData stale = stubSession(PrearcStatus.ERROR);
        final SessionData newer = sessionIn(PrearcStatus.RECEIVING);
        newer.setId(43L);
        when(hibernateService.findByLocation(sessionDirectory.getAbsolutePath())).thenReturn(Arrays.asList(stale, newer));

        service.delete(SESSION_ID, user);

        assertFilesIntact();
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void deletingWhenTheDirectoryIsAlreadyGoneStillRemovesTheRow() throws Exception {
        stubSession(PrearcStatus.ERROR);
        FileUtils.deleteDirectory(sessionDirectory);
        assertThat(sessionXml.delete()).isTrue();

        service.delete(SESSION_ID, user);

        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void deletingAnUnknownIdThrowsNotFound() throws Exception {
        when(hibernateService.getSessionData(SESSION_ID)).thenThrow(new NotFoundException("nope"));

        assertThatThrownBy(() -> service.delete(SESSION_ID, user)).isInstanceOf(NotFoundException.class);

        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void aFailedFileDeleteLeavesTheRowSoTheUserCanRetry() throws Exception {
        stubSession(PrearcStatus.ERROR);
        // A read-only parent makes removing the session directory fail.
        assertThat(arc.setWritable(false)).isTrue();

        assertThatThrownBy(() -> service.delete(SESSION_ID, user)).isInstanceOf(ServerException.class);

        verify(hibernateService, never()).delete(eq(SESSION_ID));
    }
}
