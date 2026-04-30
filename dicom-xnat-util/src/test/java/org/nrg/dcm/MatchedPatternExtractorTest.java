/*
 * dicom-xnat-util: org.nrg.dcm.MatchedPatternExtractorTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class MatchedPatternExtractorTest {

    /**
     * Test method for {@link org.nrg.dcm.MatchedPatternExtractor#extract(Attributes)}.
     */
    @Test
    public void testExtract() {
        final Extractor extractor = new MatchedPatternExtractor(Tag.PatientComments,
                Pattern.compile("\\s*(\\w+)\\s*"), 1);
        final Attributes a = new Attributes();
        assertNull(extractor.extract(a));
        a.setString(Tag.PatientComments, VR.LO, "a b");
        assertNull(extractor.extract(a));
        a.setString(Tag.PatientComments, VR.LO, " ab");
        assertEquals("ab", extractor.extract(a));
    }
}
