/*
 * dicom-xnat-util: org.nrg.dcm.MatchedPatternExtractorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MatchedPatternExtractorWithLengthLimitTest {

    /**
     * Test method for {@link MatchedPatternExtractor#extract(DicomObject)}.
     */
    @Test
    public void testExtract() {
        final Extractor extractor = new MatchedPatternExtractorWithLengthLimit(Tag.PatientComments,
                Pattern.compile(".*"), 0, 5);
        final DicomObject o = new BasicDicomObject();
        assertNull(extractor.extract(o));
        o.putString( Tag.PatientComments, VR.LO, "123456789");
        assertEquals("12345", extractor.extract(o));
        o.putString( Tag.PatientComments, VR.LO, "12345");
        assertEquals("12345", extractor.extract(o));
        o.putString( Tag.PatientComments, VR.LO, "123");
        assertEquals("123", extractor.extract(o));
        o.putString(Tag.PatientComments, VR.LO, "");
        assertNull( extractor.extract(o));
    }
}
