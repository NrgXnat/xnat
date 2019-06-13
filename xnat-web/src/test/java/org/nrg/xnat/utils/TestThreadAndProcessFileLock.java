package org.nrg.xnat.utils;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.nrg.test.workers.resources.ResourceManager;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.ClassMappingFactory;
import org.nrg.xnat.junit.ConcurrentJunitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(ConcurrentJunitRunner.class)
@PrepareForTest({CatalogUtils.class})
@PowerMockIgnore({"org.apache.*", "java.*", "javax.*", "org.w3c.*", "com.sun.*", "org.xml.sax.*"})
public class TestThreadAndProcessFileLock {

    private static final File TMPDIR = new File("/tmp/catalogs/");
    private static File TEST_CATALOG_FILE;
    private static File TEST_DCMCATALOG;
    private static File TEST_DCMCATALOG_PERM;

    @BeforeClass
    public static void setup() throws Exception {
        TMPDIR.mkdirs();

        String catFilename = "DEBUG_OUTPUT_catalog.xml";
        final String dcmFilename = "scan_4_catalog.xml";
        File catFile = ResourceManager.getInstance().getTestResourceFile("catalogs/" + catFilename);
        TEST_CATALOG_FILE = new File(TMPDIR, catFilename);
        TEST_CATALOG_FILE.mkdirs();
        Files.copy(catFile.toPath(), TEST_CATALOG_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);

        TEST_DCMCATALOG_PERM = ResourceManager.getInstance().getTestResourceFile("catalogs/" + dcmFilename);
        TEST_DCMCATALOG = new File(TMPDIR, dcmFilename);
        TEST_DCMCATALOG.mkdirs();
        Files.copy(TEST_DCMCATALOG_PERM.toPath(), TEST_DCMCATALOG.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Need to do this for the multithreading to work
        ClassMappingFactory.getInstance().getElements();

        // Stub external FS check
        PowerMockito.spy(CatalogUtils.class);
        //doReturn(false).when(CatalogUtils.class, "getChecksumConfiguration"); // doesn't work, not sure why
        Whitebox.setInternalState(CatalogUtils.class, "_checksumConfig", false);
    }

    @AfterClass
    public static void cleanup() throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(TMPDIR);
    }

    @Test
    public void testCatalog() throws Exception {
        doReadWrite(TEST_CATALOG_FILE, "tmp_catalog.xml");
    }

    @Test
    public void testCatalogRepeat() throws Exception {
        doReadWrite(TEST_CATALOG_FILE, "tmp_catalog.xml");
    }

    @Test
    public void testDcm() throws Exception {
        doReadWrite(TEST_DCMCATALOG, "tmp_dcm_catalog.xml");
    }

    @Test
    public void testDcmRepeat() throws Exception {
        doReadWrite(TEST_DCMCATALOG, "tmp_dcm_catalog.xml");
    }

    @Test
    public void testEntryDelete() throws Exception {
        // This operates on a separate file from testDcm/testDcmRepeat so it doesn't change their data mid-test
        File outfile = new File(TMPDIR,"tmp_dcm_catalog_alt.xml");
        Files.copy(TEST_DCMCATALOG_PERM.toPath(), outfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        CatalogUtils.CatalogData catalogData = CatalogUtils.getCatalogData(outfile);
        int size = catalogData.catBean.getEntries_entry().size();
        CatEntryBean entry = (CatEntryBean) CatalogUtils.getEntryByURI(catalogData.catBean, "TESTID.MR.999.4.53.20080618.133713.agkqek.dcm");
        assertNotNull(entry);
        CatalogUtils.removeEntry(catalogData.catBean, entry);
        CatalogUtils.writeCatalogToFile(catalogData);

        CatalogUtils.CatalogData catalogData2 = CatalogUtils.getCatalogData(outfile);
        assertThat(catalogData2.catBean.getEntries_entry().size(), is(size-1));
        entry = (CatEntryBean) CatalogUtils.getEntryByURI(catalogData2.catBean, "TESTID.MR.999.4.38.20080618.133713.1va8eb6.dcm");
        assertNotNull(entry);
        CatalogUtils.removeEntry(catalogData2.catBean, entry);
        CatalogUtils.writeCatalogToFile(catalogData2);
        CatCatalogBean cat2 = CatalogUtils.getCatalog(outfile);
        assertNotNull(cat2);
        assertThat(cat2.getEntries_entry().size(), is(size-2));
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testConcurrentWriteThrowsException() throws Exception {
        // This operates on a separate file from testDcm/testDcmRepeat so it doesn't change their data mid-test
        File outfile = new File(TMPDIR,"tmp_dcm_catalog_alt2.xml");
        Files.copy(TEST_DCMCATALOG_PERM.toPath(), outfile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Read in the catalog and do things
        CatalogUtils.CatalogData catalogData = CatalogUtils.getCatalogData(outfile);
        CatEntryBean entry = (CatEntryBean) CatalogUtils.getEntryByURI(catalogData.catBean, "TESTID.MR.999.4.53.20080618.133713.agkqek.dcm");
        assertNotNull(entry);
        CatalogUtils.removeEntry(catalogData.catBean, entry);

        // While one process/thread/user is doing things, another reads the unchanged catalog
        CatalogUtils.CatalogData catalogData2 = CatalogUtils.getCatalogData(outfile);

        // The original process/thread/user writes the modified catalog
        CatalogUtils.writeCatalogToFile(catalogData);

        // The second is still doing his thing, unaware that the catalog file has changed
        entry = (CatEntryBean) CatalogUtils.getEntryByURI(catalogData2.catBean, "TESTID.MR.999.4.38.20080618.133713.1va8eb6.dcm");
        assertNotNull(entry);
        CatalogUtils.removeEntry(catalogData2.catBean, entry);

        // And when the second process/thread/user tries to write his version of the catalog, an exception should be thrown
        exceptionRule.expect(ConcurrentModificationException.class);
        exceptionRule.expectMessage("Another thread or process modified " + catalogData2.catFile +
                " since I last read it. To avoid overwriting changes, I'm throwing an exception.");
        CatalogUtils.writeCatalogToFile(catalogData2);
    }

    private void doReadWrite(File file, String copiedFile) throws Exception {
        // Make pristine copy in private location (for this thread)
        File copyLoc = new File(TMPDIR, copiedFile.replace(".xml", Thread.currentThread().getName() + ".xml"));
        Files.copy(file.toPath(), copyLoc.toPath(), StandardCopyOption.REPLACE_EXISTING);
        assertTrue(org.apache.commons.io.FileUtils.contentEqualsIgnoreEOL(file, copyLoc, null)); // DUMMY CHECK

        // Read the shared file
        CatalogUtils.CatalogData catalogData = CatalogUtils.getCatalogData(file);

        // Write to the shared file
        CatalogUtils.writeCatalogToFile(catalogData, false, new HashMap<String, Map<String,Integer>>());

        // Read it again
        CatCatalogBean cat2 = CatalogUtils.getCatalog(file);
        assertNotNull(cat2);
        assertEquals(catalogData.catBean.toString(), cat2.toString());
    }
}
