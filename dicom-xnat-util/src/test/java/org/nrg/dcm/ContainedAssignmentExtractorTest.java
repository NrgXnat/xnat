/*
 * dicom-xnat-util: org.nrg.dcm.ContainedAssignmentExtractorTest
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
public class ContainedAssignmentExtractorTest {

    /**
     * Test method for {@link org.nrg.dcm.ContainedAssignmentExtractor#ContainedAssignmentExtractor(int, java.lang.String, java.lang.String, java.lang.String, int)}.
     */
    @Test
    public void testContainedAssignmentExtractorIntStringStringStringInt() {
        final Extractor extractor = new ContainedAssignmentExtractor(Tag.PatientComments, "foo", "=", Pattern.CASE_INSENSITIVE);
        final Attributes a = new Attributes();
        assertNull(extractor.extract(a));
        a.setString(Tag.PatientComments, VR.LO, "baz");
        assertNull(extractor.extract(a));
        a.setString(Tag.PatientComments, VR.LO, "foo=baz");
        assertEquals("baz", extractor.extract(a));
        a.setString(Tag.PatientComments, VR.LO, "baz=foo;foo=bar;");
        assertEquals("bar", extractor.extract(a));
    }

}
