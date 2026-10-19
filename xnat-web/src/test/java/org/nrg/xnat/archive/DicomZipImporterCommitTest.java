/*
 * web: org.nrg.xnat.archive.DicomZipImporterCommitTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.restlet.data.Status;

import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase.SyncFailedException;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.handlers.PrearchiveOperationHandlerResolver;
import org.nrg.xnat.helpers.prearchive.handlers.PrearchiveRebuildHandler;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.ARCHIVING;

/**
 * A zip upload with action=commit builds, and with auto-archive=true archives, every session it wrote into. A session
 * that fails doesn't stop the sessions after it; the upload still fails, naming each session that failed. It fails
 * with 500 if any session failed on the server, with no client error behind it. Otherwise it fails with the status of
 * the client errors when they agree, such as 409 for a session already archived under the same label, and with 400
 * when they don't.
 */
public class DicomZipImporterCommitTest {
    private static final String PROJECT   = "PROJ";
    private static final String TIMESTAMP = "20261014_120000000";
    private static final String FIRST     = "/prearchive/projects/" + PROJECT + "/" + TIMESTAMP + "/FIRST";
    private static final String SECOND    = "/prearchive/projects/" + PROJECT + "/" + TIMESTAMP + "/SECOND";

    private MockedStatic<XDAT>                          mockedXDAT;
    private MockedStatic<PrearcDatabase>                mockedPrearcDatabase;
    private MockedConstruction<GradualDicomImporter>    importers;
    private MockedConstruction<PrearcSession>           prearcSessions;
    private Map<String, PrearchiveRebuildHandler>       handlers;
    private UserI                                       user;

    @Before
    public void setUp() throws Exception {
        user = mock(UserI.class);
        when(user.getUsername()).thenReturn("admin");

        // One handler per session, so a test can fail one session's build and see whether the next is built
        handlers = new HashMap<>();
        for (final String folder : Arrays.asList("FIRST", "SECOND")) {
            final PrearchiveRebuildHandler handler = mock(PrearchiveRebuildHandler.class);
            when(handler.rebuild()).thenReturn(true);
            handlers.put(folder, handler);
        }
        final PrearchiveOperationHandlerResolver resolver = mock(PrearchiveOperationHandlerResolver.class);
        when(resolver.getHandler(any())).thenAnswer(invocation -> handlers.get(invocation.<PrearchiveOperationRequest>getArgument(0).getSessionData().getFolderName()));
        final ContextService contextService = mock(ContextService.class);
        when(contextService.getBean(PrearchiveOperationHandlerResolver.class)).thenReturn(resolver);
        mockedXDAT = Mockito.mockStatic(XDAT.class);
        mockedXDAT.when(XDAT::getContextService).thenReturn(contextService);
        mockedXDAT.when(XDAT::getEventService).thenReturn(mock(DataTypeAwareEventService.class));

        mockedPrearcDatabase = Mockito.mockStatic(PrearcDatabase.class);
        for (final String folder : Arrays.asList("FIRST", "SECOND")) {
            mockedPrearcDatabase.when(() -> PrearcDatabase.getSession(folder, TIMESTAMP, PROJECT))
                                .thenReturn(new SessionData().setFolderName(folder).setTimestamp(TIMESTAMP).setProject(PROJECT).setUrl("/prearchive/" + folder));
        }
        mockedPrearcDatabase.when(() -> PrearcDatabase.setStatus(any(), any(), any(), eq(ARCHIVING))).thenReturn(true);
        mockedPrearcDatabase.when(() -> PrearcDatabase.archive(any(), any(PrearcSession.class), anyBoolean(), anyBoolean(), anyBoolean(), any(), any()))
                            .thenAnswer(invocation -> "/archive/" + invocation.<PrearcSession>getArgument(1).getFolderName());

        // Each zip entry writes into the next session, and each archive is of the session its request names
        final List<String> uris = Arrays.asList(FIRST, SECOND);
        importers = Mockito.mockConstruction(GradualDicomImporter.class, (importer, context) -> when(importer.call()).thenReturn(uris.subList(context.getCount() - 1, context.getCount())));
        prearcSessions = Mockito.mockConstruction(PrearcSession.class, (session, context) -> when(session.getFolderName()).thenReturn(((PrearchiveOperationRequest) context.arguments().get(0)).getSessionData().getFolderName()));
    }

    @After
    public void tearDown() {
        prearcSessions.closeOnDemand();
        importers.closeOnDemand();
        mockedPrearcDatabase.closeOnDemand();
        mockedXDAT.closeOnDemand();
    }

    @Test
    public void aFailedArchiveDoesNotStopTheNextSession() {
        final ClientException conflict = new ClientException(Status.CLIENT_ERROR_CONFLICT, PrearcSessionArchiver.PRE_EXISTS, new Exception());
        mockedPrearcDatabase.when(() -> PrearcDatabase.archive(any(), sessionNamed("FIRST"), anyBoolean(), anyBoolean(), anyBoolean(), any(), any()))
                            .thenThrow(new SyncFailedException("Operation Failed: " + conflict.getMessage(), conflict));

        final Throwable thrown = catchThrowable(() -> committedUpload(true).call());

        mockedPrearcDatabase.verify(() -> PrearcDatabase.archive(any(), sessionNamed("SECOND"), anyBoolean(), anyBoolean(), anyBoolean(), any(), any()));
        assertThat(thrown).isInstanceOf(ClientException.class)
                          .hasMessage("unable to build or archive 1 of 2 Prearchive sessions: " + FIRST + " (" + PrearcSessionArchiver.PRE_EXISTS + ")");
        assertThat(((ClientException) thrown).getStatus()).as("a conflict keeps the archiver's status").isEqualTo(Status.CLIENT_ERROR_CONFLICT);
    }

    @Test
    public void aFailedBuildDoesNotStopTheNextSessionAndHidesItsMessage() throws Exception {
        when(handlers.get("FIRST").rebuild()).thenThrow(new RuntimeException(new IOException("/data/xnat/prearchive/" + PROJECT + "/" + TIMESTAMP + "/FIRST.xml")));

        final Throwable thrown = catchThrowable(() -> committedUpload(false).call());

        verify(handlers.get("SECOND")).postBuild();
        assertThat(thrown).isInstanceOf(ServerException.class)
                          .hasMessage("unable to build or archive 1 of 2 Prearchive sessions: " + FIRST + " (server error)");
        assertThat(((ServerException) thrown).getStatus()).isEqualTo(Status.SERVER_ERROR_INTERNAL);
    }

    @Test
    public void aConflictAlongsideAServerFailureIs500() throws Exception {
        final ClientException conflict = new ClientException(Status.CLIENT_ERROR_CONFLICT, PrearcSessionArchiver.PRE_EXISTS, new Exception());
        mockedPrearcDatabase.when(() -> PrearcDatabase.archive(any(), sessionNamed("FIRST"), anyBoolean(), anyBoolean(), anyBoolean(), any(), any()))
                            .thenThrow(new SyncFailedException("Operation Failed: " + conflict.getMessage(), conflict));
        doThrow(new IllegalStateException("second")).when(handlers.get("SECOND")).postBuild();

        final Throwable thrown = catchThrowable(() -> committedUpload(true).call());

        assertThat(thrown).isInstanceOf(ServerException.class)
                          .hasMessage("unable to build or archive 2 of 2 Prearchive sessions: " + FIRST + " (" + PrearcSessionArchiver.PRE_EXISTS + "); " + SECOND + " (server error)");
        assertThat(((ServerException) thrown).getStatus()).isEqualTo(Status.SERVER_ERROR_INTERNAL);
    }

    @Test
    public void clientErrorsThatDisagreeAre400() {
        final ClientException conflict = new ClientException(Status.CLIENT_ERROR_CONFLICT, PrearcSessionArchiver.PRE_EXISTS, new Exception());
        final ClientException relabel  = new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, PrearcSessionArchiver.LABEL_MOD, new Exception());
        mockedPrearcDatabase.when(() -> PrearcDatabase.archive(any(), sessionNamed("FIRST"), anyBoolean(), anyBoolean(), anyBoolean(), any(), any()))
                            .thenThrow(new SyncFailedException("Operation Failed: " + conflict.getMessage(), conflict));
        mockedPrearcDatabase.when(() -> PrearcDatabase.archive(any(), sessionNamed("SECOND"), anyBoolean(), anyBoolean(), anyBoolean(), any(), any()))
                            .thenThrow(new SyncFailedException("Operation Failed: " + relabel.getMessage(), relabel));

        final Throwable thrown = catchThrowable(() -> committedUpload(true).call());

        assertThat(thrown).isInstanceOf(ClientException.class)
                          .hasMessage("unable to build or archive 2 of 2 Prearchive sessions: " + FIRST + " (" + PrearcSessionArchiver.PRE_EXISTS + "); " + SECOND + " (" + PrearcSessionArchiver.LABEL_MOD + ")");
        assertThat(((ClientException) thrown).getStatus()).isEqualTo(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    @Test
    public void everySessionThatFailsIsNamed() throws Exception {
        doThrow(new IllegalStateException("first")).when(handlers.get("FIRST")).postBuild();
        doThrow(new IllegalStateException("second")).when(handlers.get("SECOND")).postBuild();

        final Throwable thrown = catchThrowable(() -> committedUpload(false).call());

        verify(handlers.get("SECOND")).postBuild();
        assertThat(thrown).isInstanceOf(ServerException.class)
                          .hasMessage("unable to build or archive 2 of 2 Prearchive sessions: " + FIRST + " (server error); " + SECOND + " (server error)");
        assertThat(thrown.getSuppressed()).hasSize(1);
    }

    private static PrearcSession sessionNamed(final String folder) {
        return Mockito.argThat(session -> folder.equals(session.getFolderName()));
    }

    /**
     * An importer for a zip of two entries, each of which the mocked {@link GradualDicomImporter} files into its own
     * session, uploaded with action=commit and, if asked, auto-archive=true.
     */
    private DicomZipImporter committedUpload(final boolean autoArchive) throws IOException {
        final ByteArrayOutputStream zip = new ByteArrayOutputStream();
        try (final ZipOutputStream out = new ZipOutputStream(zip)) {
            for (final String name : Arrays.asList("first.dcm", "second.dcm")) {
                out.putNextEntry(new ZipEntry(name));
                out.write(name.getBytes());
                out.closeEntry();
            }
        }
        final FileWriterWrapperI upload = mock(FileWriterWrapperI.class);
        when(upload.getName()).thenReturn("upload.zip");
        when(upload.getInputStream()).thenReturn(new ByteArrayInputStream(zip.toByteArray()));

        final Map<String, Object> params = new HashMap<>();
        params.put("action", "commit");
        if (autoArchive) {
            params.put(RequestUtil.AUTO_ARCHIVE, RequestUtil.TRUE);
        }
        return new DicomZipImporter(null, user, upload, params);
    }
}
