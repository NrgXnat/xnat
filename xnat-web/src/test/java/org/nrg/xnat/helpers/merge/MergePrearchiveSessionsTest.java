package org.nrg.xnat.helpers.merge;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultReject;
import org.nrg.dicom.mizer.objects.AnonymizationResultSuccess;
import org.nrg.config.entities.Configuration;
import org.nrg.xdat.bean.XnatImagesessiondataBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Merging an upload into a prearchive session runs the site script on the upload's instances. An instance the script
 * rejects must not stay in the prearchive.
 */
public class MergePrearchiveSessionsTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void anInstanceTheSiteScriptRejectsIsDeleted() throws Exception {
        final File upload = folder.newFolder("upload");
        final File rejected = instance(upload, "1.dcm");
        final File kept = instance(upload, "2.dcm");

        final XnatImagesessiondataBean src = new XnatImagesessiondataBean();
        src.setProject("PROJECT");
        final MergePrearchiveSessions merge = new MergePrearchiveSessions(this, upload, src, upload.getParent(), folder.newFolder("existing"),
                                                                          null, folder.getRoot().getPath(), true, false, session -> { }, null);
        merge.setAnonymizer(new ResultsOf(Arrays.asList(result(new AnonymizationResultReject(null, "rejected by the site script"), rejected),
                                                        result(new AnonymizationResultSuccess(null), kept))));

        merge.getPostAnonSession();

        assertFalse("the instance the site script rejected should be deleted", rejected.exists());
        assertTrue("the instance the site script accepted should stay", kept.exists());
    }

    private static File instance(final File directory, final String name) throws IOException {
        final File file = new File(directory, name);
        Files.write(file.toPath(), new byte[] {1, 2, 3});
        return file;
    }

    private static AnonymizationResult result(final AnonymizationResult result, final File file) {
        result.setAbsolutePath(file.getAbsolutePath());
        return result;
    }

    /** An anonymizer that reports the results it is given. */
    private static final class ResultsOf extends AnonymizerA {
        ResultsOf(final List<AnonymizationResult> results) {
            _results = results;
        }

        @Override
        public List<AnonymizationResult> call() {
            return _results;
        }

        @Override
        String getSubject() {
            return null;
        }

        @Override
        String getLabel() {
            return null;
        }

        @Override
        Configuration getScript() {
            return null;
        }

        @Override
        boolean isEnabled() {
            return true;
        }

        @Override
        String getProjectName() {
            return null;
        }

        @Override
        List<File> getFilesToAnonymize() {
            return Collections.emptyList();
        }

        private final List<AnonymizationResult> _results;
    }
}
