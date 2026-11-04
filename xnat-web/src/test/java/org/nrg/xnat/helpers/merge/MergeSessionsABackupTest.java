package org.nrg.xnat.helpers.merge;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.nrg.action.ServerException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.preferences.SiteConfigPreferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A merge copies the existing session into the cache first, so it can put the session back if the merge fails. The copy
 * is only for that: a merge that succeeds deletes it, and one that fails keeps it.
 */
public class MergeSessionsABackupTest {
    private static final String PROJECT = "PROJECT";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private MockedStatic<XDAT> _xdat;
    private File               _backups;
    private File               _upload;
    private File               _existing;

    @Before
    public void setUp() throws IOException {
        final File cache = folder.newFolder("cache");
        final SiteConfigPreferences preferences = mock(SiteConfigPreferences.class);
        when(preferences.getCachePath()).thenReturn(cache.getPath());
        _xdat = Mockito.mockStatic(XDAT.class);
        _xdat.when(XDAT::getSiteConfigPreferences).thenReturn(preferences);

        _backups = new File(cache, PROJECT + "/merge");
        _upload = new File(folder.newFolder("prearchive"), "SESSION");
        _existing = new File(folder.newFolder("archive"), "SESSION");
        instance(_upload, "new.dcm");
        instance(_existing, "archived.dcm");
    }

    @After
    public void tearDown() {
        _xdat.close();
    }

    @Test
    public void aMergeThatSucceedsDeletesItsBackup() throws Exception {
        new Merge(false).call();

        assertTrue("the upload should be merged into the existing session", new File(_existing, "new.dcm").exists());
        assertArrayEquals("the merge should leave no backup in the cache", new String[0], _backups.list());
    }

    @Test
    public void aMergeThatFailsKeepsItsBackup() {
        assertThrows(ServerException.class, () -> new Merge(true).call());

        assertTrue("the existing session should be put back", new File(_existing, "archived.dcm").exists());
        assertEquals("the merge should keep its backup in the cache", 1, _backups.list().length);
    }

    private static void instance(final File directory, final String name) throws IOException {
        Files.createDirectories(directory.toPath());
        Files.write(new File(directory, name).toPath(), new byte[] {1, 2, 3});
    }

    /** A merge of the upload into the existing session's directory, with no session metadata to merge. */
    private final class Merge extends MergeSessionsA<XnatImagesessiondataBean> {
        Merge(final boolean fails) {
            super(MergeSessionsABackupTest.this, _upload, upload(), _upload.getParent(), _existing, null, _existing.getParent(), true, false, session -> { }, null, null);
            _fails = fails;
        }

        @Override
        protected XnatImagesessiondataBean getPostAnonSession() {
            return src;
        }

        @Override
        public Results<XnatImagesessiondataBean> mergeSessions(final XnatImagesessiondataBean src, final String srcRootPath, final XnatImagesessiondataBean dest, final String destRootPath, final File rootbackup) throws ServerException {
            if (_fails) {
                throw new ServerException("the metadata merge failed");
            }
            return new Results<>(src);
        }

        @Override
        public void finalize(final XnatImagesessiondataBean session) {
        }

        @Override
        public String getCacheBKDirName() {
            return "merge";
        }

        private final boolean _fails;
    }

    private static XnatImagesessiondataBean upload() {
        final XnatImagesessiondataBean session = new XnatImagesessiondataBean();
        session.setProject(PROJECT);
        return session;
    }
}
