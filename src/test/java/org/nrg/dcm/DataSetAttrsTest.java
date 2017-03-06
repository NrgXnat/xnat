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
import org.dcm4che2.data.Tag;
import org.junit.Test;
import org.nrg.attr.ConversionFailureException;
import org.nrg.util.FileURIOpener;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class DataSetAttrsTest {
    // Tests based on the sample1 dataset, with each file gzipped
    private static final String DICOM_RESOURCE = "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz";

    private static final DicomAttributeIndex SEQUENCE_NAME = new FixedDicomAttributeIndex(Tag.SequenceName);
    private static final DicomAttributeIndex ACQUISITION_DATE = new FixedDicomAttributeIndex(Tag.AcquisitionDate);
    private static final DicomAttributeIndex ACQUISITION_TIME = new FixedDicomAttributeIndex(Tag.AcquisitionTime);
    private static final DicomAttributeIndex OPERATORS_NAME = new FixedDicomAttributeIndex(Tag.OperatorsName);

    private static final String sequenceName = "*tfl3d1_ns";

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
}
