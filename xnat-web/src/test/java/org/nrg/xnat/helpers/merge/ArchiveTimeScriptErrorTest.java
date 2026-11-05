/*
 * web: org.nrg.xnat.helpers.merge.ArchiveTimeScriptErrorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import org.dcm4che3.data.Tag;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.config.entities.Configuration;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.framework.services.ContextService;
import org.nrg.test.workers.resources.ResourceManager;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.dicom.mizer.config.MizerServiceTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A script that fails at archive time. Project and site scripts run there through {@link AnonymizerA} and the batch
 * {@link MizerService#anonymize(List, String, String, String, long, String, boolean, boolean)}, whose exception becomes
 * the prearchive error an admin sees ({@code MergeSessionsA} prefixes it with "An error occurred when anonymizing the
 * data: "). The receive-time tests in {@code AnonymizationErrorSurfacingTest} do not reach this path.
 *
 * <p>The script fails the way XNAT-8499 did: it assigns the injected {@code subject} variable, which is null because
 * the session has no subject. It does so only for the second file, so the first has been anonymized into its staged
 * copy by the time the batch fails.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MizerServiceTestConfig.class)
public class ArchiveTimeScriptErrorTest {
    private static final File INSTANCE = ResourceManager.getInstance().getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm");

    private static final String SCRIPT = "version \"6.1\"\n"
                                         + "(0008,1030) := project\n"
                                         + "(0008,103E) = \"FAIL\" ? (0010,0010) := subject\n";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Autowired
    private MizerService service;

    private MockedStatic<XDAT> mockedXDAT;
    private File               first;
    private File               second;
    private byte[]             firstOriginal;
    private byte[]             secondOriginal;

    @Before
    public void setUp() throws Exception {
        final ContextService contextService = mock(ContextService.class);
        when(contextService.getBeanSafely(MizerService.class)).thenReturn(service);
        mockedXDAT = Mockito.mockStatic(XDAT.class);
        mockedXDAT.when(XDAT::getContextService).thenReturn(contextService);

        first = folder.newFile("1.dcm");
        second = folder.newFile("2.dcm");
        Files.copy(INSTANCE.toPath(), first.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(INSTANCE.toPath(), second.toPath(), StandardCopyOption.REPLACE_EXISTING);
        service.anonymize(second, "PROJECT", "SUBJECT", "SESSION", false, false, 0L, "version \"6.1\"\n(0008,103E) := \"FAIL\"\n");
        assertEquals("FAIL", DicomObjectFactory.newInstance(second).getString(Tag.SeriesDescription));
        assertNotEquals("FAIL", DicomObjectFactory.newInstance(first).getString(Tag.SeriesDescription));
        firstOriginal = Files.readAllBytes(first.toPath());
        secondOriginal = Files.readAllBytes(second.toPath());
    }

    @After
    public void tearDown() {
        mockedXDAT.close();
    }

    @Test
    public void theErrorNamesTheFileTheLineTheStatementAndTheVariable() {
        final MizerException e = assertThrows(MizerException.class, new ProjectScript(SCRIPT, first, second)::call);
        assertEquals("Unable to anonymize " + second + ", so none of the 2 files in this batch was changed: "
                     + "Failed to apply script: line 3; stmt=`(0008,103E)=\"FAIL\"?(0010,0010):=subject`: Variable 'subject' is null.",
                     e.getMessage());
    }

    @Test
    public void noFileIsChanged() {
        assertThrows(MizerException.class, new ProjectScript(SCRIPT, first, second)::call);
        assertUnchanged(first, firstOriginal);
        assertUnchanged(second, secondOriginal);
    }

    /**
     * Without this, the tests above would pass against a script or setup that fails for some other reason: the same
     * script and files anonymize when the subject is there.
     */
    @Test
    public void theSameScriptSucceedsWithASubject() throws Exception {
        final ProjectScript script = new ProjectScript(SCRIPT, first, second);
        script.subject = "SUBJECT";
        assertEquals(2, script.call().size());
    }

    private static void assertUnchanged(final File file, final byte[] original) {
        try {
            assertArrayEquals(file + " should not have changed", original, Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /** A project script over the given files, as {@link ProjectAnonymizer} would supply it from the session. */
    private static final class ProjectScript extends AnonymizerA {
        ProjectScript(final String contents, final File... files) {
            _script = mock(Configuration.class);
            when(_script.getId()).thenReturn(1L);
            when(_script.getContents()).thenReturn(contents);
            _files = Arrays.asList(files);
        }

        @Override
        String getSubject() {
            return subject;
        }

        @Override
        String getLabel() {
            return "SESSION";
        }

        @Override
        Configuration getScript() {
            return _script;
        }

        @Override
        boolean isEnabled() {
            return true;
        }

        @Override
        String getProjectName() {
            return "PROJECT";
        }

        @Override
        List<File> getFilesToAnonymize() {
            return _files;
        }

        private String subject;

        private final Configuration _script;
        private final List<File>    _files;
    }
}
