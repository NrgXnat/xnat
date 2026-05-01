/*
 * dicom-xnat-util: org.nrg.dcm.ContainedAssignmentExtractorTest
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
public class ContainedAssignmentExtractorTest {

    /**
     * Test method for {@link org.nrg.dcm.ContainedAssignmentExtractor#ContainedAssignmentExtractor(int, java.lang.String, java.lang.String, java.lang.String, int)}.
     */
    @Test
    public void testContainedAssignmentExtractorIntStringStringStringInt() {
        final Extractor extractor = new ContainedAssignmentExtractor(Tag.PatientComments, "foo", "=", Pattern.CASE_INSENSITIVE);
        final DicomObject o = new BasicDicomObject();
        assertNull(extractor.extract(o));
        o.putString(Tag.PatientComments, VR.LO, "baz");
        assertNull(extractor.extract(o));
        o.putString(Tag.PatientComments, VR.LO, "foo=baz");
        assertEquals("baz", extractor.extract(o));
        o.putString(Tag.PatientComments, VR.LO, "baz=foo;foo=bar;");
        assertEquals("bar", extractor.extract(o));
    }

}
