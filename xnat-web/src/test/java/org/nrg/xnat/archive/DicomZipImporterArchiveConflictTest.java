/*
 * web: org.nrg.xnat.archive.DicomZipImporterArchiveConflictTest
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
import java.util.Collections;
import java.util.HashMap;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.ARCHIVING;

/**
 * A zip upload with action=commit and auto-archive=true reports an archive failure the request caused with the
 * archiver's status and message: a session already in the archive under the same label is a 409, as it is through the
 * session importer.
 */
public class DicomZipImporterArchiveConflictTest {
    private static final String PROJECT   = "PROJ";
    private static final String TIMESTAMP = "20261016_120000000";
    private static final String FOLDER    = "S1";
    private static final String URI       = "/prearchive/projects/" + PROJECT + "/" + TIMESTAMP + "/" + FOLDER;

    private MockedStatic<XDAT>                       mockedXDAT;
    private MockedStatic<PrearcDatabase>             mockedPrearcDatabase;
    private MockedConstruction<GradualDicomImporter> importers;
    private MockedConstruction<PrearcSession>        prearcSessions;
    private UserI                                    user;

    @Before
    public void setUp() throws Exception {
        user = mock(UserI.class);
        when(user.getUsername()).thenReturn("admin");

        final PrearchiveRebuildHandler handler = mock(PrearchiveRebuildHandler.class);
        when(handler.rebuild()).thenReturn(true);
        final PrearchiveOperationHandlerResolver resolver = mock(PrearchiveOperationHandlerResolver.class);
        when(resolver.getHandler(any())).thenReturn(handler);
        final ContextService contextService = mock(ContextService.class);
        when(contextService.getBean(PrearchiveOperationHandlerResolver.class)).thenReturn(resolver);
        mockedXDAT = Mockito.mockStatic(XDAT.class);
        mockedXDAT.when(XDAT::getContextService).thenReturn(contextService);
        mockedXDAT.when(XDAT::getEventService).thenReturn(mock(DataTypeAwareEventService.class));

        mockedPrearcDatabase = Mockito.mockStatic(PrearcDatabase.class);
        mockedPrearcDatabase.when(() -> PrearcDatabase.getSession(FOLDER, TIMESTAMP, PROJECT))
                            .thenReturn(new SessionData().setFolderName(FOLDER).setTimestamp(TIMESTAMP).setProject(PROJECT).setUrl("/prearchive/" + FOLDER));
        mockedPrearcDatabase.when(() -> PrearcDatabase.setStatus(any(), any(), any(), eq(ARCHIVING))).thenReturn(true);

        importers = Mockito.mockConstruction(GradualDicomImporter.class, (importer, context) -> when(importer.call()).thenReturn(Collections.singletonList(URI)));
        prearcSessions = Mockito.mockConstruction(PrearcSession.class, (session, context) -> when(session.getFolderName()).thenReturn(FOLDER));
    }

    @After
    public void tearDown() {
        prearcSessions.closeOnDemand();
        importers.closeOnDemand();
        mockedPrearcDatabase.closeOnDemand();
        mockedXDAT.closeOnDemand();
    }

    @Test
    public void aSessionAlreadyArchivedUnderTheSameLabelIsAConflict() {
        final ClientException conflict = new ClientException(Status.CLIENT_ERROR_CONFLICT, PrearcSessionArchiver.PRE_EXISTS, new Exception());
        failArchiveWith(new SyncFailedException("Operation Failed: " + conflict.getMessage(), conflict));

        final Throwable thrown = catchThrowable(() -> committedUpload().call());

        assertThat(thrown).isInstanceOf(ClientException.class)
                          .hasMessage("unable to archive for the Prearchive session: " + PrearcSessionArchiver.PRE_EXISTS);
        assertThat(((ClientException) thrown).getStatus()).isEqualTo(Status.CLIENT_ERROR_CONFLICT);
    }

    @Test
    public void anyOtherArchiveFailureIsReportedAsBefore() {
        failArchiveWith(new IllegalStateException("/data/xnat/prearchive/" + PROJECT + "/" + TIMESTAMP + "/" + FOLDER));

        final Throwable thrown = catchThrowable(() -> committedUpload().call());

        assertThat(thrown).isInstanceOf(ClientException.class).hasMessage("unable to archive for the Prearchive session");
        assertThat(((ClientException) thrown).getStatus()).isEqualTo(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    private void failArchiveWith(final Exception failure) {
        mockedPrearcDatabase.when(() -> PrearcDatabase.archive(any(), any(PrearcSession.class), anyBoolean(), anyBoolean(), anyBoolean(), any(), any()))
                            .thenThrow(failure);
    }

    /** An importer for a zip of one entry, which the mocked {@link GradualDicomImporter} files into S1, uploaded with action=commit and auto-archive=true. */
    private DicomZipImporter committedUpload() throws IOException {
        final ByteArrayOutputStream zip = new ByteArrayOutputStream();
        try (final ZipOutputStream out = new ZipOutputStream(zip)) {
            out.putNextEntry(new ZipEntry("one.dcm"));
            out.write("one".getBytes());
            out.closeEntry();
        }
        final FileWriterWrapperI upload = mock(FileWriterWrapperI.class);
        when(upload.getName()).thenReturn("upload.zip");
        when(upload.getInputStream()).thenReturn(new ByteArrayInputStream(zip.toByteArray()));

        final Map<String, Object> params = new HashMap<>();
        params.put("action", "commit");
        params.put(RequestUtil.AUTO_ARCHIVE, RequestUtil.TRUE);
        return new DicomZipImporter(null, user, upload, params);
    }
}
