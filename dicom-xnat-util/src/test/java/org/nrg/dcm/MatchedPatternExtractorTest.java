/*
 * dicom-xnat-util: org.nrg.dcm.MatchedPatternExtractorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.junit.Test;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MatchedPatternExtractorTest {

    /**
     * Test method for {@link org.nrg.dcm.MatchedPatternExtractor#extract(org.dcm4che2.data.DicomObject)}.
     */
    @Test
    public void testExtract() {
        final Extractor extractor = new MatchedPatternExtractor(Tag.PatientComments,
                Pattern.compile("\\s*(\\w+)\\s*"), 1);
        final DicomObject o = new BasicDicomObject();
        assertNull(extractor.extract(o));
        o.putString(Tag.PatientComments, VR.LO, "a b");
        assertNull(extractor.extract(o));
        o.putString(Tag.PatientComments, VR.LO, " ab");
        assertEquals("ab", extractor.extract(o));
    }
}
