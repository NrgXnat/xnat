package org.nrg.xnat.utils;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.test.workers.resources.ResourceManager;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.ClassMappingFactory;
import org.nrg.xnat.junit.ConcurrentJunitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(ConcurrentJunitRunner.class)
public class TestThreadAndProcessFileLock {

    private static final String catFilename = "catalogs/DEBUG_OUTPUT_catalog.xml";
    private static final String dcmFilename = "catalogs/scan_4_catalog.xml";
    private static File TEST_CATALOG_FILE;
    private static File TEST_DCMCATALOG;

    @BeforeClass
    public static void setup() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        File catFile = ResourceManager.getInstance().getTestResourceFile(catFilename);
        TEST_CATALOG_FILE = new File("/tmp/" + catFilename);
        TEST_CATALOG_FILE.mkdirs();
        Files.copy(catFile.toPath(), TEST_CATALOG_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);

        File dcmFile = ResourceManager.getInstance().getTestResourceFile(dcmFilename);
        TEST_DCMCATALOG = new File("/tmp/" + dcmFilename);
        TEST_DCMCATALOG.mkdirs();
        Files.copy(dcmFile.toPath(), TEST_DCMCATALOG.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Need to do this for the multithreading to work
        ClassMappingFactory.getInstance().getElements();
    }

    @AfterClass
    public static void cleanup() {
        TEST_CATALOG_FILE.delete();
        TEST_DCMCATALOG.delete();
    }

    @Test
    public void testCatalog() throws Exception {
        doReadWrite(TEST_CATALOG_FILE, "/tmp/tmp_catalog.xml");
    }

    @Test
    public void testCatalogRepeat() throws Exception {
        doReadWrite(TEST_CATALOG_FILE, "/tmp/tmp_catalog.xml");
    }

    @Test
    public void testDcm() throws Exception {
        doReadWrite(TEST_DCMCATALOG, "/tmp/tmp_dcm_catalog.xml");
    }

    @Test
    public void testDcmRepeat() throws Exception {
        doReadWrite(TEST_DCMCATALOG, "/tmp/tmp_dcm_catalog.xml");
    }

    private void doReadWrite(File file, String copiedFile) throws Exception {
        File copyLoc = new File(copiedFile.replace(".xml", Thread.currentThread().getName() + ".xml"));
        Files.copy(file.toPath(), copyLoc.toPath(), StandardCopyOption.REPLACE_EXISTING);
        CatCatalogBean cat = CatalogUtils.getCatalog(file);
        CatalogUtils.writeCatalogToFile(cat, file, false, new HashMap<String, Map<String,Integer>>());
        assertTrue(org.apache.commons.io.FileUtils.contentEquals(file, copyLoc));
        CatCatalogBean cat2 = CatalogUtils.getCatalog(file);
        assertEquals(cat.toString(), cat2.toString());
        copyLoc.delete();
    }
}
