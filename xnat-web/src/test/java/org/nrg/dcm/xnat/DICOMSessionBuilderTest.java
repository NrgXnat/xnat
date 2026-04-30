/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.DICOMSessionBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.xdat.bean.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class DICOMSessionBuilderTest extends Scan4TestCase {
    private File sessionDir = null;

    @Before
    public void setUp() throws IOException {
        sessionDir = initializeScan4WorkingCopy("1.MR.head_DHead.*");
    }

    @After
    public void tearDown() {
        tearDownWorkingCopy(sessionDir);
    }

    /**
     * Test method for {@link org.nrg.dcm.xnat.DICOMSessionBuilder#call()}.
     */
    @Test
    public void testCall() throws Exception {
        try (final DICOMSessionBuilder builder = new DICOMSessionBuilder(sessionDir)) {

            final XnatImagesessiondataBean session = builder.call();
            assertTrue(session instanceof XnatMrsessiondataBean);

            final Date sessionDate = new SimpleDateFormat("MM/dd/yyyy").parse("12/14/2006");
            assertEquals(sessionDate, session.getDate());
            assertEquals("09:12:06", session.getTime());
            assertEquals("Hospital", session.getAcquisitionSite());
            assertEquals("SIEMENS", session.getScanner_manufacturer());
            assertEquals("TrioTim", session.getScanner_model());
            assertEquals("MEDPC", session.getScanner());
            assertEquals("1", session.getStudyId());

            final List<XnatImagescandataBean> scans = session.getScans_scan();
            assertEquals(3, scans.size());
            assertTrue(scans.getFirst() instanceof XnatMrscandataBean);
            final XnatMrscandataBean scan4 = (XnatMrscandataBean) scans.getFirst();
            assertEquals("4", scan4.getId());
            assertEquals("1.3.12.2.1107.5.2.32.35177.3.2006121409284535196417894.0.0.0", scan4.getUid());
            assertEquals("t1_mpr_1mm_p2_pos50", scan4.getSeriesDescription());
            assertEquals("MR", scan4.getModality());

            assertTrue(scan4.getFile().getFirst() instanceof XnatResourcecatalogBean);
            final XnatResourcecatalogBean s4_catalog = (XnatResourcecatalogBean) scan4.getFile().getFirst();
            assertEquals("RAW", s4_catalog.getContent());
            assertEquals("DICOM", s4_catalog.getFormat());
            assertEquals("DICOM", s4_catalog.getLabel());
            assertEquals(Double.valueOf(3.0), Double.valueOf(scan4.getFieldstrength()));
            assertEquals(Double.valueOf(1.0), scan4.getParameters_voxelres_x());
            assertEquals(Double.valueOf(1.0), scan4.getParameters_voxelres_y());
            assertEquals(Double.valueOf(1.0), scan4.getParameters_voxelres_z());
            assertEquals("Sag", scan4.getParameters_orientation());
            assertEquals(Integer.valueOf(256), scan4.getParameters_fov_x());
            assertEquals(Integer.valueOf(256), scan4.getParameters_fov_y());
            assertEquals(Double.valueOf(2400.0), scan4.getParameters_tr());
            assertEquals(Double.valueOf(3.08), scan4.getParameters_te());
            assertEquals(Double.valueOf(1000.0), scan4.getParameters_ti());
            assertEquals(Double.valueOf(8.0), scan4.getParameters_flip());
            assertEquals("ORIGINAL\\PRIMARY\\M\\ND\\NORM", scan4.getParameters_imagetype());
            assertEquals("GR\\IR", scan4.getParameters_scansequence());
            assertEquals(Integer.valueOf(9), scan4.getFrames());
            assertEquals("5", scans.get(1).getId());
            assertEquals("6", scans.get(2).getId());
        }
    }
}
