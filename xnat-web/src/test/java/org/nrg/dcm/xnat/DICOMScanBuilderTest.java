/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.DICOMScanBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.xnat;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.dcm.AttrDefs;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.xnat.exceptions.UnableToBuildScanException;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.bean.XnatResourcecatalogBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;
import static org.nrg.dcm.Attributes.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class DICOMScanBuilderTest extends Scan4TestCase {
    @Before
    public void setUp() throws IOException {
        scan4Dir = initializeScan4WorkingCopy();
    }

    @After
    public void tearDown() {
        tearDownWorkingCopy(scan4Dir);
    }

    /**
     * Test method for {@link org.nrg.dcm.xnat.DICOMScanBuilder#getNativeTypeAttrs(Map<Class<? extends XnatImagescandataBean>, AttrDefs >)}.
     */
    @Test
    public void testGetNativeTypeAttrs() {
        final ImmutableSet<DicomAttributeIndex> attrs =
                DICOMScanBuilder.getNativeTypeAttrs(DICOMScanBuilder.getDefaultScanTypeAttrs());
        assertTrue(attrs.contains(SeriesInstanceUID));
        assertTrue(attrs.contains(SeriesTime));
        assertTrue(attrs.contains(Modality));
        assertTrue(attrs.contains(SeriesDate));
        assertFalse(attrs.contains(StudyInstanceUID));
    }

    @Test
    public void testFromScanDirectoryWithRelativePaths() throws IOException, SQLException, UnableToBuildScanException {
        final XnatImagescandataBean   scan        = DICOMScanBuilder.buildScanFromDirectory(scan4Dir, scan4Dir, "4", true);
        final XnatResourcecatalogBean catResource = (XnatResourcecatalogBean) scan.getFile().get(0);
        assertEquals("DICOM", catResource.getFormat());
        assertEquals("DICOM", catResource.getLabel());
        assertEquals("RAW", catResource.getContent());
        final String catalogPath = catResource.getUri();
        assertFalse(catalogPath.startsWith("/"));
        final File catalogFile = new File(scan4Dir, catalogPath);
        assertTrue(catalogFile.exists());
    }

    @Test
    public void testFromScanDirectoryWithRelativePathsAndChangedId() throws IOException, SQLException, UnableToBuildScanException {
        // Make scan4Dir the session directory, so we can create XNAT's default file structure
        File tmpDir = new File(scan4Dir.getParent(), scan4Dir.getName() + ".1");
        FileUtils.moveDirectory(scan4Dir, tmpDir);
        String sessionDir = scan4Dir.getAbsolutePath();
        File scanDir    = Paths.get(sessionDir, "SCANS", "ID", "DICOM").toFile();
        File scanDirOut = Paths.get(sessionDir, "SCANS", "4", "DICOM").toFile();
        FileUtils.deleteDirectory(scanDir);
        FileUtils.deleteDirectory(scanDirOut);
        FileUtils.moveDirectory(tmpDir, scanDir);

        // Now, do the test
        final XnatImagescandataBean   scan        = DICOMScanBuilder.buildScanFromDirectory(scan4Dir, scanDir, "4", true);
        final XnatResourcecatalogBean catResource = (XnatResourcecatalogBean) scan.getFile().get(0);
        assertEquals("DICOM", catResource.getFormat());
        assertEquals("DICOM", catResource.getLabel());
        assertEquals("RAW", catResource.getContent());
        final String catalogPath = catResource.getUri();
        assertFalse(catalogPath.startsWith("/"));
        final File catalogFile = new File(sessionDir, catalogPath);
        assertTrue(catalogFile.exists());
        assertTrue(scanDirOut.exists() && scanDirOut.isDirectory());
    }

    @Test
    public void testFromScanDirectoryWithAbsolutePaths() throws IOException, SQLException, UnableToBuildScanException {
        final XnatImagescandataBean scan = DICOMScanBuilder.buildScanFromDirectory(scan4Dir, scan4Dir, "4", false);
        assertNotNull(scan);
        assertNotNull(scan.getModality());
        assertEquals("MR", scan.getModality());
        final Date startDate = scan.getStartDate();
        assertTrue(DateUtils.isSameDay(SCAN_DATE, startDate));

        final XnatResourcecatalogBean catResource = (XnatResourcecatalogBean) scan.getFile().get(0);
        assertEquals("DICOM", catResource.getFormat());
        assertEquals("DICOM", catResource.getLabel());
        assertEquals("RAW", catResource.getContent());

        final String catalogPath = catResource.getUri();
        assertTrue(catalogPath.matches("^([A-z]:[/\\\\\\\\].*)|(/.*)$"));
        final File catalogFile = new File(catalogPath);
        assertTrue(catalogFile.exists());
    }

    // This is the long representation of 14 December, 2006.
    private static Date SCAN_DATE = new Date(1166124948805L);

    private File scan4Dir = null;
}
