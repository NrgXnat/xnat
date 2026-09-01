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

    /** The window is the highest tag asked for, not a fixed one: ordinary attributes stop well short. */
    @Test
    public final void readsOnlyAsFarAsTheAttributesAsked() {
        // SequenceName (0018,0024) is the highest of the three, and well below the pixel data.
        assertEquals(Tag.SequenceName,
                     DataSetAttrs.stopTagFor(Sets.newHashSet(SEQUENCE_NAME, ACQUISITION_DATE, OPERATORS_NAME)));
    }

    /**
     * An index that does not know its own bound reports the tag before the pixel data, which is where the read
     * always stopped -- so one such index in the set keeps the window open for all of them.
     */
    @Test
    public final void keepsTheOldWindowForAnIndexThatDoesNotKnowItsBound() {
        final DicomAttributeIndex unbounded = new DicomAttributeIndex() {
            public String getAttributeName(final org.dcm4che3.data.Attributes attributes) { return "unbounded"; }
            public String getColumnName() { return "unbounded"; }
            public Integer[] getPath(final org.dcm4che3.data.Attributes context) { return new Integer[0]; }
            public String getString(final org.dcm4che3.data.Attributes attributes) { return null; }
            public String getString(final org.dcm4che3.data.Attributes attributes, final String defaultValue) { return defaultValue; }
            public String[] getStrings(final org.dcm4che3.data.Attributes attributes) { return new String[0]; }
        };

        assertEquals(Tag.PixelData - 1, unbounded.getMaxTag());
        assertEquals(Tag.PixelData - 1, DataSetAttrs.stopTagFor(Sets.newHashSet(SEQUENCE_NAME, unbounded)));
    }

    /**
     * The caller adds one to get an exclusive stop tag, so a bound of 0xFFFFFFFF would wrap to 0 and dcm4che
     * would stop at the first element -- every file empty. A DICOM mapping can name that tag: parseDicomTag
     * accepts the whole unsigned range.
     */
    @Test
    public final void doesNotLetTheStopTagWrapToZero() {
        final DicomAttributeIndex highest = new FixedDicomAttributeIndex(0xFFFFFFFF);

        final int stopTag = DataSetAttrs.stopTagFor(Sets.newHashSet(highest));

        assertEquals("stepped back so the exclusive stop tag cannot wrap", 0xFFFFFFFE, stopTag);
        assertEquals("and that exclusive stop tag reads to the end", -1, stopTag + 1);
    }

    /** Asking for nothing reads as far as it always did rather than not at all. */
    @Test
    public final void readsTheOldWindowWhenNothingIsAskedFor() {
        assertEquals(Tag.PixelData - 1, DataSetAttrs.stopTagFor(Sets.newHashSet()));
    }

    /** ... and must move, unsigned, for one that reaches beyond it. */
    @Test
    public final void widensTheWindowForAnAttributeAfterThePixelData() {
        assertEquals(0xF2151050,
                     DataSetAttrs.stopTagFor(Sets.newHashSet(SEQUENCE_NAME, AFTER_PIXEL_DATA)));
    }
}
