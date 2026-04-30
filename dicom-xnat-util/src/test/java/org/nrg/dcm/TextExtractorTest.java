/*
 * dicom-xnat-util: org.nrg.dcm.TextExtractorTest
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class TextExtractorTest {

    /**
     * Test method for {@link org.nrg.dcm.TextExtractor#extract(Attributes)}.
     */
    @Test
    public void testExtract() {
        final TextExtractor extractor = new TextExtractor(Tag.StudyComments);
        final Attributes a = new Attributes();
        assertNull(extractor.extract(a));
        a.setString(Tag.StudyComments, VR.LO, "test");
        assertEquals("test", extractor.extract(a));
    }
}
