package org.nrg.xnat.archive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.action.ClientException;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A compressed upload that fails marks every session it went into, including the session of an entry that failed:
 * that entry may have been the first in its session, so no entry returned it.
 */
public class DicomZipImporterTest {
    private static final String SESSION_A = "/prearchive/projects/PROJ/20261015_120000/A";
    private static final String SESSION_B = "/prearchive/projects/PROJ/20261015_120000/B";

    private MockedStatic<XDAT>                       mockedXDAT;
    private MockedStatic<ImportFailures>             mockedImportFailures;
    private MockedConstruction<GradualDicomImporter> importers;

    /**
     * Each entry's importer: a.dcm goes into session A, b.dcm fails after its session B was created, and c.dcm fails
     * before it has a session.
     */
    @Before
    public void setUp() {
        // ImporterHandlerA looks up importer packages in the Spring context when the class loads.
        final ContextService contextService = mock(ContextService.class);
        doReturn(Collections.emptyMap()).when(contextService).getBeansOfType(any());
        mockedXDAT = Mockito.mockStatic(XDAT.class);
        mockedXDAT.when(XDAT::getContextService).thenReturn(contextService);
        // The importer publishes its progress as events.
        mockedXDAT.when(XDAT::getEventService).thenReturn(mock(DataTypeAwareEventService.class));
        mockedImportFailures = Mockito.mockStatic(ImportFailures.class);
        importers = Mockito.mockConstruction(GradualDicomImporter.class, (importer, context) -> {
            final String entry = ((FileWriterWrapperI) context.arguments().get(2)).getName();
            switch (entry) {
                case "a.dcm":
                    when(importer.call()).thenReturn(Collections.singletonList(SESSION_A));
                    when(importer.getSessionUri()).thenReturn(SESSION_A);
                    break;
                case "b.dcm":
                    when(importer.call()).thenThrow(new ClientException("unable to read DICOM object b.dcm"));
                    when(importer.getSessionUri()).thenReturn(SESSION_B);
                    break;
                default:
                    when(importer.call()).thenThrow(new ClientException("unable to read DICOM object " + entry));
            }
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
        if (mockedXDAT != null) {
            mockedXDAT.closeOnDemand();
        }
    }

    @Test
    public void anEntrySkippedBeforeTheUploadIsCutOffIsMarkedWithTheRest() throws Exception {
        // Under Ignore-Unparsable, b.dcm and c.dcm are skipped; then the zip itself ends partway through d.dcm.
        final byte[] zip = zip("a.dcm", "c.dcm", "b.dcm", "d.dcm");
        final DicomZipImporter importer = importer(Arrays.copyOf(zip, zip.length - 300), true);

        assertThrows(ClientException.class, importer::call);

        assertThat(marked()).containsExactly(SESSION_A, SESSION_B);
    }

    @Test
    public void anEntryThatStopsTheUploadIsMarkedWithTheRest() throws Exception {
        assertThrows(ClientException.class, importer(zip("a.dcm", "b.dcm"), false)::call);

        assertThat(marked()).containsExactly(SESSION_A, SESSION_B);
    }

    @Test
    public void anUploadWithNoEntryImportedMarksTheSessionsOfTheEntriesThatFailed() throws Exception {
        assertThrows(ClientException.class, importer(zip("b.dcm", "c.dcm"), true)::call);

        assertThat(marked()).containsExactly(SESSION_B);
    }

    private Collection<String> marked() {
        @SuppressWarnings("unchecked") final ArgumentCaptor<Collection<String>> uris = ArgumentCaptor.forClass(Collection.class);
        mockedImportFailures.verify(() -> ImportFailures.markFailed(uris.capture(), any()));
        return uris.getValue();
    }

    private static DicomZipImporter importer(final byte[] zip, final boolean ignoreUnparsable) throws IOException {
        final FileWriterWrapperI upload = mock(FileWriterWrapperI.class);
        when(upload.getName()).thenReturn("upload.zip");
        when(upload.getInputStream()).thenReturn(new ByteArrayInputStream(zip));
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("Ignore-Unparsable", Boolean.toString(ignoreUnparsable));
        return new DicomZipImporter(null, mock(UserI.class), upload, parameters);
    }

    private static byte[] zip(final String... entries) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (final ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (final String entry : entries) {
                zip.putNextEntry(new ZipEntry(entry));
                final byte[] content = new byte[1000];
                new Random(entry.hashCode()).nextBytes(content);
                zip.write(content);
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
}
