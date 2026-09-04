package org.nrg.xnat.helpers.merge.anonymize;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.mizer.service.StagingDirectoryResolver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SameVolumeStagingDirectoryResolverTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File archive;
    private File prearchive;
    private File elsewhere;
    private final StagingDirectoryResolver fallback = dicomFile -> elsewhere;

    private SameVolumeStagingDirectoryResolver resolver(final String... roots) throws Exception {
        archive    = folder.newFolder("archive");
        prearchive = folder.newFolder("prearchive");
        elsewhere  = folder.newFolder("fallback");
        final List<String> configured = roots.length == 0
                                        ? Arrays.asList(archive.getPath(), prearchive.getPath())
                                        : Arrays.asList(roots);
        return new SameVolumeStagingDirectoryResolver(() -> configured, fallback);
    }

    @Test
    public void stagesAFileUnderTheArchiveAtTheArchiveRoot() throws Exception {
        final SameVolumeStagingDirectoryResolver resolver = resolver();
        final File file = fileUnder(archive, "proj/arc001/SESSION/SCANS/1/DICOM/1.dcm");

        final File staging = resolver.resolve(file);

        assertEquals(new File(archive, SameVolumeStagingDirectoryResolver.STAGING_DIRECTORY_NAME), staging);
        assertTrue("the staging directory should have been created", staging.isDirectory());
    }

    @Test
    public void stagesAFileUnderThePrearchiveAtThePrearchiveRoot() throws Exception {
        final SameVolumeStagingDirectoryResolver resolver = resolver();
        final File file = fileUnder(prearchive, "proj/20260904_120000000/SESSION/SCANS/1/DICOM/1.dcm");

        assertEquals(new File(prearchive, SameVolumeStagingDirectoryResolver.STAGING_DIRECTORY_NAME), resolver.resolve(file));
    }

    @Test
    public void fallsBackForAFileUnderNoDataRoot() throws Exception {
        final SameVolumeStagingDirectoryResolver resolver = resolver();
        final File file = fileUnder(folder.newFolder("inbox"), "1.dcm");

        assertEquals(elsewhere, resolver.resolve(file));
        assertTrue("no staging directory should appear at a root the file is not under",
                   !new File(archive, SameVolumeStagingDirectoryResolver.STAGING_DIRECTORY_NAME).exists());
    }

    @Test
    public void ignoresBlankRoots() throws Exception {
        final SameVolumeStagingDirectoryResolver resolver = resolver();
        final SameVolumeStagingDirectoryResolver withBlanks = new SameVolumeStagingDirectoryResolver(
                () -> Arrays.asList("", null, "   ", prearchive.getPath()), fallback);
        final File file = fileUnder(prearchive, "proj/20260904_120000000/SESSION/SCANS/1/DICOM/1.dcm");

        assertEquals(new File(prearchive, SameVolumeStagingDirectoryResolver.STAGING_DIRECTORY_NAME), withBlanks.resolve(file));
        assertEquals(resolver.resolve(file), withBlanks.resolve(file));
    }

    @Test
    public void recognisesARootConfiguredThroughASymlink() throws Exception {
        resolver();
        final Path link = folder.getRoot().toPath().resolve("archive-link");
        Files.createSymbolicLink(link, archive.toPath());
        final SameVolumeStagingDirectoryResolver throughLink = new SameVolumeStagingDirectoryResolver(
                () -> Arrays.asList(link.toString()), fallback);
        // The file is addressed through the real directory, the root through the link.
        final File file = fileUnder(archive, "proj/arc001/SESSION/SCANS/1/DICOM/1.dcm");

        assertEquals(link.resolve(SameVolumeStagingDirectoryResolver.STAGING_DIRECTORY_NAME).toFile(), throughLink.resolve(file));
    }

    private static File fileUnder(final File root, final String relative) throws Exception {
        final File file = new File(root, relative);
        Files.createDirectories(file.getParentFile().toPath());
        Files.write(file.toPath(), new byte[]{1, 2, 3});
        return file;
    }
}
