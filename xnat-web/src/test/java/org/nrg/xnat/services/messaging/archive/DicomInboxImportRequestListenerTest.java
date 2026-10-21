package org.nrg.xnat.services.messaging.archive;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.action.ServerException;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.GradualDicomImporter;
import org.nrg.xnat.archive.ImportFailures;
import org.nrg.xnat.services.archive.DicomInboxImportRequestService;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * An inbox import that stops marks every session it went into, including the session of the file it stopped at: that
 * file may have been the first in its session, so no file returned it.
 */
public class DicomInboxImportRequestListenerTest {
    private static final String SESSION_B = "/prearchive/projects/PROJ/20261015_120000/B";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private MockedStatic<Users>                      mockedUsers;
    private MockedStatic<XDAT>                       mockedXDAT;
    private MockedStatic<ImportFailures>             mockedImportFailures;
    private MockedConstruction<GradualDicomImporter> importers;

    @Before
    public void setUp() {
        // ImporterHandlerA looks up importer packages in the Spring context when the class loads.
        final ContextService contextService = mock(ContextService.class);
        doReturn(Collections.emptyMap()).when(contextService).getBeansOfType(any());
        mockedXDAT = Mockito.mockStatic(XDAT.class);
        mockedXDAT.when(XDAT::getContextService).thenReturn(contextService);
        mockedUsers = Mockito.mockStatic(Users.class);
        mockedUsers.when(() -> Users.getUser("admin")).thenReturn(mock(UserI.class));
        mockedImportFailures = Mockito.mockStatic(ImportFailures.class);
        // The write fails on the server after the file's session was created.
        importers = Mockito.mockConstruction(GradualDicomImporter.class, (importer, context) -> {
            when(importer.call()).thenThrow(new ServerException("unable to write DICOM object b.dcm"));
            when(importer.getSessionUri()).thenReturn(SESSION_B);
        });
    }

    @After
    public void tearDown() {
        if (importers != null) {
            importers.closeOnDemand();
        }
        if (mockedImportFailures != null) {
            mockedImportFailures.closeOnDemand();
        }
        if (mockedUsers != null) {
            mockedUsers.closeOnDemand();
        }
        if (mockedXDAT != null) {
            mockedXDAT.closeOnDemand();
        }
    }

    @Test
    public void theSessionOfTheFileTheImportStoppedAtIsMarked() throws Exception {
        final File folder = temporaryFolder.newFolder("inbox", "study");
        Files.writeString(new File(folder, "b.dcm").toPath(), "dicom");
        final DicomInboxImportRequest request = DicomInboxImportRequest.builder().username("admin").sessionPath(folder.getAbsolutePath())
                                                                       .parameters(Collections.emptyMap()).build();

        new DicomInboxImportRequestListener(mock(DicomInboxImportRequestService.class), Collections.emptyMap(), Collections.emptyMap()).onRequest(request);

        @SuppressWarnings("unchecked") final ArgumentCaptor<Collection<String>> uris = ArgumentCaptor.forClass(Collection.class);
        mockedImportFailures.verify(() -> ImportFailures.markFailed(uris.capture(), any()));
        assertThat(uris.getValue()).containsExactly(SESSION_B);
    }
}
