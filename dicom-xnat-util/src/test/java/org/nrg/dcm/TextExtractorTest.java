/*
 * dicom-xnat-util: org.nrg.dcm.TextExtractorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import static org.junit.Assert.*;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.junit.Test;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class TextExtractorTest {

    /**
     * Test method for {@link org.nrg.dcm.TextExtractor#extract(org.dcm4che2.data.DicomObject)}.
     */
    @Test
    public void testExtract() {
        final TextExtractor extractor = new TextExtractor(Tag.StudyComments);
        final DicomObject o = new BasicDicomObject();
        assertNull(extractor.extract(o));
        o.putString(Tag.StudyComments, VR.LO, "test");
        assertEquals("test", extractor.extract(o));
    }
}
