package org.nrg.xnat.utils;


import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.test.workers.resources.ResourceManager;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.ClassMappingFactory;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.junit.ConcurrentJunitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(ConcurrentJunitRunner.class)
@PowerMockIgnore({"org.apache.*", "java.*", "javax.*", "org.w3c.*", "com.sun.*", "org.xml.sax.*"})
@Slf4j
public class TestThreadAndProcessFileLock {

    private static final File TMPDIR = new File("/tmp/catalogs/");
    private static File TEST_CATALOG_FILE;
    private static File TEST_DCMCATALOG;
    private static File TEST_DCMCATALOG_PERM;

    @BeforeClass
    public static void setup() throws Exception {
        TMPDIR.mkdirs();

        final String catFilename = "DEBUG_OUTPUT_catalog.xml";
        final String dcmFilename = "scan_4_catalog.xml";
        final String subdir = "catalogs";

        File permFile = ResourceManager.getInstance().getTestResourceFile(
                Paths.get(subdir, catFilename).toString());
        TEST_CATALOG_FILE = new File(TMPDIR, catFilename);
        rewriteFileWithCatalogUtils(permFile);
        copyCatalog(TEST_CATALOG_FILE, permFile);

        TEST_DCMCATALOG_PERM = ResourceManager.getInstance().getTestResourceFile(
                Paths.get(subdir, dcmFilename).toString());
        TEST_DCMCATALOG = new File(TMPDIR, dcmFilename);

        rewriteFileWithCatalogUtils(TEST_DCMCATALOG_PERM);
        copyCatalog(TEST_DCMCATALOG, TEST_DCMCATALOG_PERM);

        // Need to do this for the multithreading to work
        ClassMappingFactory.getInstance().getElements();

        // Stub getChecksumConfiguration check
        PowerMockito.spy(CatalogUtils.class);
        //doReturn(false).when(CatalogUtils.class, "getChecksumConfiguration"); // doesn't work, not sure why
        Whitebox.setInternalState(CatalogUtils.class, "_checksumConfig", new AtomicBoolean(false));
    }

    private static void rewriteFileWithCatalogUtils(File catFile) throws Exception {
        // We have to read & write the file once to ensure the checksum is constant for our tests
        // (the way the test resources are "compiled" adds a newline at end of file, which writeCatalogToFile strips)
        CatalogUtils.CatalogData catalogData = new CatalogUtils.CatalogData(catFile, false);
        CatalogUtils.writeCatalogToFile(catalogData, false,
                new HashMap<String, Map<String, Integer>>());
    }

    private static void copyCatalog(File catFile, File permFile) throws Exception {
        // Copy perm file to test location
        catFile.mkdirs();
        Files.copy(permFile.toPath(), catFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterClass
    public static void cleanup() throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(TMPDIR);
    }

    @Test
    public void testCatalog() throws Exception {
        doReadWrite(TEST_CATALOG_FILE);
    }

    @Test
    public void testCatalogRepeat() throws Exception {
        doReadWrite(TEST_CATALOG_FILE);
    }

    @Test
    public void testCatalogRepeat2() throws Exception {
        doReadWrite(TEST_CATALOG_FILE);
    }

    @Test
    public void testDcm() throws Exception {
        doReadWrite(TEST_DCMCATALOG);
    }

    @Test
    public void testDcmRepeat() throws Exception {
        doReadWrite(TEST_DCMCATALOG);
    }

    @Test
    public void testDcmRepeat2() throws Exception {
        doReadWrite(TEST_DCMCATALOG);
    }

    @Test
    public void testEntryDelete() throws Exception {
        // This operates on a separate file from testDcm/testDcmRepeat so it doesn't change their data mid-test
        File outfile = new File(TMPDIR,"testEntryDelete_tmp_dcm_catalog.xml");
        Files.copy(TEST_DCMCATALOG_PERM.toPath(), outfile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        CatalogUtils.CatalogData catalogData = new CatalogUtils.CatalogData(outfile, false);
        int size = catalogData.catBean.getEntries_entry().size();
        CatEntryBean entry = (CatEntryBean) CatalogUtils.getEntryByURI(catalogData.catBean, "TESTID.MR.999.4.53.20080618.133713.agkqek.dcm");
        assertNotNull(entry);
        CatalogUtils.removeEntry(catalogData.catBean, entry);
        CatalogUtils.writeCatalogToFile(catalogData);

        CatalogUtils.CatalogData catalogData2 = new CatalogUtils.CatalogData(outfile, false);
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
        File outfile = new File(TMPDIR,"testConcurrentWriteThrowsException_tmp_dcm_catalog.xml");
        Files.copy(TEST_DCMCATALOG_PERM.toPath(), outfile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Read in the catalog and do things
        CatalogUtils.CatalogData catalogData = new CatalogUtils.CatalogData(outfile, false);
        CatEntryBean entry = (CatEntryBean) CatalogUtils.getEntryByURI(catalogData.catBean,
                "TESTID.MR.999.4.53.20080618.133713.agkqek.dcm");
        assertNotNull(entry);
        CatalogUtils.removeEntry(catalogData.catBean, entry);

        // While one process/thread/user is doing things, another reads the unchanged catalog
        CatalogUtils.CatalogData catalogData2 = new CatalogUtils.CatalogData(outfile, false);

        // The original process/thread/user writes the modified catalog
        CatalogUtils.writeCatalogToFile(catalogData);

        // The second is still doing his thing, unaware that the catalog file has changed
        entry = (CatEntryBean) CatalogUtils.getEntryByURI(catalogData2.catBean,
                "TESTID.MR.999.4.38.20080618.133713.1va8eb6.dcm");
        assertNotNull(entry);
        CatalogUtils.removeEntry(catalogData2.catBean, entry);

        // And when the second process/thread/user tries to write his version of the catalog, an exception should be thrown
        exceptionRule.expect(ConcurrentModificationException.class);
        exceptionRule.expectMessage(    "Another thread or process modified " + catalogData2.catFile +
                " since I last read it. To avoid overwriting changes, I'm throwing an exception.");
        CatalogUtils.writeCatalogToFile(catalogData2);
    }

    @Test
    public void testRewriteOnSameObject() throws Exception {
        // This operates on a separate file from testDcm/testDcmRepeat so it doesn't change their data mid-test
        File outfile = new File(TMPDIR,"testRewriteOnSameObject_catalog.xml");
        String fakeName = "testRewriteOnSameObject.txt";
        File fakeFile = new File(TMPDIR, fakeName);
        fakeFile.createNewFile();

        CatalogUtils.CatalogData catalogData = new CatalogUtils.CatalogData(outfile); //creates catalog
        CatalogUtils.writeCatalogToFile(catalogData); //saves new & empty catalog
        XnatResourceInfo mockInfo = Mockito.mock(XnatResourceInfo.class);
        CatalogUtils.addOrUpdateEntry(catalogData, null, fakeName, fakeName,
                fakeFile, mockInfo, null);
        // main test is to ensure that no exception is thrown here
        CatalogUtils.writeCatalogToFile(catalogData);

        // but then also, let's check that we added the item
        CatalogUtils.CatalogData catalogData2 = new CatalogUtils.CatalogData(outfile);
        assertThat(catalogData2.catBean.getEntries_entry(), Matchers.<CatEntryI>hasSize(1));
    }

    private void doReadWrite(File file) throws Exception {
        // Read the shared file
        CatalogUtils.CatalogData catalogData = new CatalogUtils.CatalogData(file, false);

        // Write to the shared file (without changing anything)
        CatalogUtils.writeCatalogToFile(catalogData, false,
                new HashMap<String, Map<String, Integer>>());

        // Read it again - since we didn't actually mod anything, it better match
        CatCatalogBean cat2 = CatalogUtils.getCatalog(file);
        assertNotNull(cat2);
        assertEquals(catalogData.catBean.toString(), cat2.toString());
    }
}
