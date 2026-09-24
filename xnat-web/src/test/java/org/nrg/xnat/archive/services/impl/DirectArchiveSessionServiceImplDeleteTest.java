package org.nrg.xnat.archive.services.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.restlet.data.Status;
import org.springframework.jms.core.JmsTemplate;

import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.messaging.archive.DirectArchiveRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * XNAT-7944: deleting a direct archive session through the user-facing API must remove the session's files from the
 * archive directory, not just the tracking row. The session is claimed (moved to DELETING) before any file is touched,
 * so a session the archiver is working on is refused unless a site admin forces it, and the directory is left alone
 * when it belongs to something other than the session being deleted. Files still landing are checked both before and
 * after the claim, because the importer decides the session is RECEIVING before it takes its file lock. The archive
 * trigger, in turn, must not queue a session a delete has claimed, and the importer must not write into one.
 */
public class DirectArchiveSessionServiceImplDeleteTest {
    private static final long   SESSION_ID = 42L;
    private static final String PROJECT    = "PROJ";
    private static final String SESSION    = "SESSION_01";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private DirectArchiveSessionHibernateService hibernateService;
    private DirectArchiveSessionServiceImpl      service;
    private UserI                                user;
    private MockedStatic<Permissions>            mockedPermissions;
    private MockedStatic<Roles>                  mockedRoles;
    private MockedStatic<XDAT>                   mockedXDAT;
    private MockedStatic<PrearcUtils>            mockedPrearcUtils;
    private MockedStatic<BaseXnatExperimentdata> mockedExperiments;

    private File archiveDirectory;
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

        sessionDirectory = temporaryFolder.newFolder("archive", PROJECT, "arc001", SESSION);
        archiveDirectory = sessionDirectory.getParentFile();
        sessionXml       = new File(archiveDirectory, SESSION + ".xml");
        Files.writeString(new File(sessionDirectory, "1.dcm").toPath(), "dicom");
        Files.writeString(sessionXml.toPath(), "<xml/>");

        mockedPermissions = Mockito.mockStatic(Permissions.class);
        mockedPermissions.when(() -> Permissions.canDeleteProject(user, PROJECT)).thenReturn(true);
        mockedRoles = Mockito.mockStatic(Roles.class);
        mockedRoles.when(() -> Roles.isSiteAdmin(user)).thenReturn(false);
        // No backup-to-cache: MoveToCache deletes outright.
        mockedXDAT = Mockito.mockStatic(XDAT.class);
        mockedXDAT.when(() -> XDAT.getBoolSiteConfigurationProperty("backupDeletedToCache", false)).thenReturn(false);
        // Nothing is still landing in the session directory and no archived experiment owns it unless a test says so.
        mockedPrearcUtils = Mockito.mockStatic(PrearcUtils.class);
        mockedPrearcUtils.when(() -> PrearcUtils.isSessionReceiving(any())).thenReturn(false);
        mockedExperiments = Mockito.mockStatic(BaseXnatExperimentdata.class);
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(any(), any(), any(), anyBoolean())).thenReturn(null);
    }

    @After
    public void tearDown() {
        mockedExperiments.closeOnDemand();
        mockedPrearcUtils.closeOnDemand();
        mockedXDAT.closeOnDemand();
        mockedRoles.closeOnDemand();
        mockedPermissions.closeOnDemand();
        archiveDirectory.setWritable(true);
    }

    private SessionData sessionIn(final PrearcStatus status) {
        final SessionData session = new SessionData().setProject(PROJECT).setTag("1.2.3").setName(SESSION).setFolderName(SESSION)
                                                     .setTimestamp("20260914_120000").setUrl(sessionDirectory.getAbsolutePath()).setStatus(status);
        session.setId(SESSION_ID);
        return session;
    }

    private void stubDeletableSession() throws Exception {
        stubDeletableSession(sessionIn(PrearcStatus.ERROR));
    }

    private void stubDeletableSession(final SessionData session) throws Exception {
        when(hibernateService.getSessionData(SESSION_ID)).thenReturn(session);
    }

    private void stubBackupToCache(final File cacheRoot) {
        final SiteConfigPreferences preferences = mock(SiteConfigPreferences.class);
        when(preferences.getCachePath()).thenReturn(cacheRoot.getAbsolutePath());
        mockedXDAT.when(() -> XDAT.getBoolSiteConfigurationProperty("backupDeletedToCache", false)).thenReturn(true);
        mockedXDAT.when(XDAT::getSiteConfigPreferences).thenReturn(preferences);
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

    /** The row goes but the directory is not this session's to remove. */
    private void assertDeleteKeepsFilesAndRemovesRow() throws Exception {
        service.delete(SESSION_ID, user);

        assertFilesIntact();
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void deletingAClaimedSessionRemovesDirectoryXmlAndRow() throws Exception {
        stubDeletableSession();

        service.delete(SESSION_ID, user);

        assertFilesGone();
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void aSessionThatCannotBeClaimedIsRefusedWithConflict() throws Exception {
        when(hibernateService.getSessionData(SESSION_ID)).thenReturn(sessionIn(PrearcStatus.ARCHIVING));
        doThrow(new ArchivingException("not deletable")).when(hibernateService).setStatusToDeleting(SESSION_ID, false);

        assertThatThrownBy(() -> service.delete(SESSION_ID, user))
                .isInstanceOfSatisfying(ClientException.class,
                                        e -> assertThat(e.getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT));

        assertFilesIntact();
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void aSessionStillReceivingFilesIsRefusedBeforeItIsClaimed() throws Exception {
        // Files still landing in the directory would recreate it behind the delete, the same reason triggerArchive
        // leaves such a session alone.
        stubDeletableSession();
        mockedPrearcUtils.when(() -> PrearcUtils.isSessionReceiving(any())).thenReturn(true);

        assertThatThrownBy(() -> service.delete(SESSION_ID, user))
                .isInstanceOfSatisfying(ClientException.class,
                                        e -> assertThat(e.getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT));

        assertFilesIntact();
        verify(hibernateService, never()).setStatusToDeleting(anyLong(), anyBoolean());
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void aSessionThatStartsReceivingAfterItIsClaimedIsRefusedAndLeftClaimed() throws Exception {
        // The importer checks the status before it takes its file lock, so a file can be on its way in when the
        // claim lands. The lock check is repeated after the claim; the claim stands so the next delete can retry.
        stubDeletableSession();
        mockedPrearcUtils.when(() -> PrearcUtils.isSessionReceiving(any())).thenReturn(false, true);

        assertThatThrownBy(() -> service.delete(SESSION_ID, user))
                .isInstanceOfSatisfying(ClientException.class,
                                        e -> assertThat(e.getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT));

        assertFilesIntact();
        verify(hibernateService).setStatusToDeleting(SESSION_ID, false);
        verify(hibernateService, never()).setStatusToError(anyLong(), any());
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void forcingADeleteRequiresASiteAdmin() throws Exception {
        stubDeletableSession(sessionIn(PrearcStatus.ARCHIVING));

        assertThatThrownBy(() -> service.delete(SESSION_ID, user, true)).isInstanceOf(InvalidPermissionException.class);

        assertFilesIntact();
        verify(hibernateService, never()).setStatusToDeleting(anyLong(), anyBoolean());
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void forcingDoesNotBypassTheReceivingGuard() throws Exception {
        // Files still landing would recreate the directory whatever the row's status says.
        stubDeletableSession(sessionIn(PrearcStatus.ARCHIVING));
        mockedRoles.when(() -> Roles.isSiteAdmin(user)).thenReturn(true);
        mockedPrearcUtils.when(() -> PrearcUtils.isSessionReceiving(any())).thenReturn(true);

        assertThatThrownBy(() -> service.delete(SESSION_ID, user, true))
                .isInstanceOfSatisfying(ClientException.class,
                                        e -> assertThat(e.getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT));

        assertFilesIntact();
        verify(hibernateService, never()).setStatusToDeleting(anyLong(), anyBoolean());
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void aSiteAdminCanForceDeleteASessionTheArchiverLeftBehind() throws Exception {
        // Rows stuck in a queued, building or archiving status have no other way out.
        stubDeletableSession(sessionIn(PrearcStatus.ARCHIVING));
        mockedRoles.when(() -> Roles.isSiteAdmin(user)).thenReturn(true);

        service.delete(SESSION_ID, user, true);

        assertFilesGone();
        verify(hibernateService).setStatusToDeleting(SESSION_ID, true);
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void aFileMayLandWhileTheSessionIsStillReceiving() throws Exception {
        stubDeletableSession(sessionIn(PrearcStatus.RECEIVING));

        service.requireReceiving(sessionIn(PrearcStatus.RECEIVING));
    }

    @Test
    public void aFileIsRefusedOnceADeleteHasClaimedTheSession() throws Exception {
        // The importer's status check ran before its file lock; under the lock the row now says DELETING.
        stubDeletableSession(sessionIn(PrearcStatus.DELETING));

        assertThatThrownBy(() -> service.requireReceiving(sessionIn(PrearcStatus.RECEIVING)))
                .isInstanceOfSatisfying(ClientException.class,
                                        e -> assertThat(e.getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT));
    }

    @Test
    public void aFileIsRefusedWhenTheSessionRowIsAlreadyGone() throws Exception {
        when(hibernateService.getSessionData(SESSION_ID)).thenThrow(new NotFoundException("gone"));

        assertThatThrownBy(() -> service.requireReceiving(sessionIn(PrearcStatus.RECEIVING)))
                .isInstanceOfSatisfying(ClientException.class,
                                        e -> assertThat(e.getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT));
    }

    @Test
    public void deletingWithoutProjectDeletePermissionClaimsNothingAndLeavesFilesAndRow() throws Exception {
        stubDeletableSession();
        mockedPermissions.when(() -> Permissions.canDeleteProject(user, PROJECT)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(SESSION_ID, user)).isInstanceOf(InvalidPermissionException.class);

        assertFilesIntact();
        verify(hibernateService, never()).setStatusToDeleting(anyLong(), anyBoolean());
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void deletingKeepsFilesWhenAnArchivedExperimentOwnsTheDirectory() throws Exception {
        // archive() saves the experiment before its final cleanup, and an overwrite-mode session appends straight
        // into an archived experiment's directory: in both cases the directory is real archive data.
        stubDeletableSession();
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(eq(PROJECT), eq(SESSION), any(), anyBoolean()))
                         .thenReturn(mock(XnatExperimentdata.class));

        assertDeleteKeepsFilesAndRemovesRow();
    }

    @Test
    public void theFolderNameIsCheckedAsAnExperimentLabelWhenItDiffersFromTheSessionName() throws Exception {
        stubDeletableSession(sessionIn(PrearcStatus.ERROR).setFolderName("SESSION_01_RENAMED"));
        mockedExperiments.when(() -> BaseXnatExperimentdata.GetExptByProjectIdentifier(eq(PROJECT), eq("SESSION_01_RENAMED"), any(), anyBoolean()))
                         .thenReturn(mock(XnatExperimentdata.class));

        assertDeleteKeepsFilesAndRemovesRow();
    }

    @Test
    public void deletingKeepsFilesWhenAnotherActiveSessionSharesTheLocation() throws Exception {
        // create() allows a new session at the same location once the old one has errored, so the directory may
        // now belong to a newer session that is still receiving.
        stubDeletableSession();
        when(hibernateService.hasActiveSessionAtLocation(sessionDirectory.getAbsolutePath(), SESSION_ID)).thenReturn(true);

        assertDeleteKeepsFilesAndRemovesRow();
    }

    @Test
    public void deletingWhenTheDirectoryIsAlreadyGoneStillRemovesTheRow() throws Exception {
        stubDeletableSession();
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
    public void aFailedFileDeleteMarksTheSessionErrorAndKeepsTheRowForRetry() throws Exception {
        stubDeletableSession();
        // A read-only parent makes removing the session XML (and the directory) fail.
        assertThat(archiveDirectory.setWritable(false)).isTrue();

        assertThatThrownBy(() -> service.delete(SESSION_ID, user)).isInstanceOf(ServerException.class);

        assertFilesIntact();
        verify(hibernateService).setStatusToError(eq(SESSION_ID), any(IOException.class));
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void anUnexpectedFailureWhileDecidingOwnershipAlsoMarksTheSessionErrorInsteadOfLeavingItDeleting() throws Exception {
        stubDeletableSession();
        when(hibernateService.hasActiveSessionAtLocation(sessionDirectory.getAbsolutePath(), SESSION_ID)).thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> service.delete(SESSION_ID, user)).isInstanceOf(ServerException.class);

        assertFilesIntact();
        verify(hibernateService).setStatusToError(eq(SESSION_ID), any(IllegalStateException.class));
        verify(hibernateService, never()).delete(anyLong());
    }

    @Test
    public void backedUpXmlAndDirectoryLandUnderTheSameTimestampSoTheyCanBeRestoredTogether() throws Exception {
        stubDeletableSession();
        final File cacheRoot = temporaryFolder.newFolder("cache");
        stubBackupToCache(cacheRoot);

        service.delete(SESSION_ID, user);

        assertFilesGone();
        final Path deleted = cacheRoot.toPath().resolve("DELETED");
        assertThat(backupStamp(deleted, findOnly(deleted, SESSION + ".xml"), sessionXml))
                .isEqualTo(backupStamp(deleted, findOnly(deleted, "1.dcm"), new File(sessionDirectory, "1.dcm")));
        verify(hibernateService).delete(SESSION_ID);
    }

    @Test
    public void triggerArchiveQueuesTheSessionAndSendsTheBuildRequest() throws Exception {
        when(hibernateService.setStatusToQueuedBuilding(SESSION_ID, false)).thenReturn(true);

        service.triggerArchive(sessionIn(PrearcStatus.RECEIVING));

        mockedXDAT.verify(() -> XDAT.sendJmsRequest(any(JmsTemplate.class), any(DirectArchiveRequest.class)));
    }

    @Test
    public void triggerArchiveRefusesASessionItCouldNotQueueAndSendsNothing() throws Exception {
        // A session a delete has claimed (or that is otherwise not queueable) must not reach the archiver.
        when(hibernateService.setStatusToQueuedBuilding(SESSION_ID, false)).thenReturn(false);

        assertThatThrownBy(() -> service.triggerArchive(sessionIn(PrearcStatus.DELETING)))
                .isInstanceOfSatisfying(ClientException.class,
                                        e -> assertThat(e.getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT));

        mockedXDAT.verify(() -> XDAT.sendJmsRequest(any(), any()), never());
        verify(hibernateService, never()).setStatusBackToReceiving(anyLong());
    }

    private static Path findOnly(final Path root, final String fileName) {
        final Collection<File> matches = FileUtils.listFiles(root.toFile(), new NameFileFilter(fileName), TrueFileFilter.INSTANCE);
        assertThat(matches).as("backups of %s under %s", fileName, root).hasSize(1);
        return matches.iterator().next().toPath();
    }

    /**
     * MoveToCache lays a backup out as {@code <cache>/DELETED/<timestamp dirs>/<original absolute path>}; strip the
     * original path to get the timestamp part.
     */
    private static Path backupStamp(final Path deleted, final Path backup, final File original) {
        final Path relative = deleted.relativize(backup);
        return relative.subpath(0, relative.getNameCount() - Path.of(original.getAbsolutePath()).getNameCount());
    }
}
