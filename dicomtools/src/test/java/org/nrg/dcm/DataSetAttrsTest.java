/*
 * DicomDB: org.nrg.dcm.DataSetAttrsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.collect.Sets;
import org.dcm4che3.data.Tag;
import org.junit.Test;
import org.nrg.attr.ConversionFailureException;
import org.nrg.util.FileURIOpener;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DataSetAttrsTest {
    // Tests based on the sample1 dataset, with each file gzipped
    private static final String DICOM_RESOURCE = "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz";

    private static final DicomAttributeIndex SEQUENCE_NAME = new FixedDicomAttributeIndex(Tag.SequenceName);
    private static final DicomAttributeIndex ACQUISITION_DATE = new FixedDicomAttributeIndex(Tag.AcquisitionDate);
    private static final DicomAttributeIndex ACQUISITION_TIME = new FixedDicomAttributeIndex(Tag.AcquisitionTime);
    private static final DicomAttributeIndex OPERATORS_NAME = new FixedDicomAttributeIndex(Tag.OperatorsName);

    private static final String sequenceName = "*tfl3d1_ns";

    /** Carries a private block on each side of the pixel data. */
    private static final String AFTER_PIXEL_DATA_RESOURCE = "dataSetAttrs/tagAfterPixelData.dcm";

    private static final DicomAttributeIndex BEFORE_PIXEL_DATA = new FixedDicomAttributeIndex(0x00191050);
    private static final DicomAttributeIndex AFTER_PIXEL_DATA  = new FixedDicomAttributeIndex(0xF2151050);

    @Test
    public final void testDataSetAttrsFileSetOfInteger() throws IOException,URISyntaxException {
        Set<DicomAttributeIndex> attrs = Sets.newHashSet();
        attrs.add(SEQUENCE_NAME);
        attrs.add(ACQUISITION_DATE);
        attrs.add(ACQUISITION_TIME);
        attrs.add(OPERATORS_NAME);

        final URL url = getClass().getClassLoader().getResource(DICOM_RESOURCE);
        assertNotNull(url);

        final DataSetAttrs dsa = DataSetAttrs.create(url.toURI(), attrs, FileURIOpener.getInstance());
        assertNotNull(dsa);
    }

    @Test
    public final void testGet() throws IOException,ConversionFailureException,URISyntaxException {
        Set<DicomAttributeIndex> attrs = Sets.newHashSet();
        DataSetAttrs dsa;

        attrs.add(SEQUENCE_NAME);

        // test some values without conversions,
        // and at least one value of each type requiring conversion
        final URL url = getClass().getClassLoader().getResource(DICOM_RESOURCE);
        assertNotNull(url);

        dsa = DataSetAttrs.create(url.toURI(), attrs, FileURIOpener.getInstance());
        assertNotNull(dsa);
        assertEquals(sequenceName, dsa.get(SEQUENCE_NAME));
    }

    /**
     * The read used to stop at the pixel data unconditionally, so an index pointing past it -- a private group
     * &gt;= 0x8000 sorts there -- found nothing and the attribute came back empty with no error.
     */
    @Test
    public final void readsAnAttributeThatSortsAfterThePixelData()
            throws IOException, ConversionFailureException, URISyntaxException {
        final Set<DicomAttributeIndex> attrs = Sets.newHashSet(BEFORE_PIXEL_DATA, AFTER_PIXEL_DATA);

        final URL url = getClass().getClassLoader().getResource(AFTER_PIXEL_DATA_RESOURCE);
        assertNotNull(url);
        final DataSetAttrs dsa = DataSetAttrs.create(url.toURI(), attrs, FileURIOpener.getInstance());

        assertEquals("before pixel data", dsa.get(BEFORE_PIXEL_DATA));
        assertEquals("after pixel data", dsa.get(AFTER_PIXEL_DATA));
    }

    /** Every standard index stops short of the pixel data, so the window must not move for them. */
    @Test
    public final void doesNotWidenTheWindowForOrdinaryAttributes() {
        assertEquals(Tag.PixelData - 1,
                     DataSetAttrs.stopTagFor(Sets.newHashSet(SEQUENCE_NAME, ACQUISITION_DATE, OPERATORS_NAME)));
        assertEquals(Tag.PixelData - 1, DataSetAttrs.stopTagFor(Sets.newHashSet()));
    }

    /** ... and must move, unsigned, for one that reaches beyond it. */
    @Test
    public final void widensTheWindowForAnAttributeAfterThePixelData() {
        assertEquals(0xF2151050,
                     DataSetAttrs.stopTagFor(Sets.newHashSet(SEQUENCE_NAME, AFTER_PIXEL_DATA)));
    }
}
