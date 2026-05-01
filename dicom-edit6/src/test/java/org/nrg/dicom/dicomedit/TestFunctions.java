/*
 * DicomEdit: TestFunctions
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Ignore;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

/**
 * Run tests of functions.
 *
 */
public class TestFunctions {
    public static final String ASSIGN_TO_PUBLIC_TAG_FROM_GETURL_FUNCTION = "(0008,0080) := getURL[\"http://www.mocky.io/v2/577eb33e100000c217f261ce\"] \n";

    /**
     * Test the getURL function.
     *
     * Charlie has set up a simple rest call at http://www.mocky.io/v2/577eb33e100000c217f261ce
     * that returns "text_from_URL" string.  We'll see how long mocky provides this service...
     *
     * @throws MizerException When an error occurs creating a DICOM object from the submitted file.
     */
    @Test
    @Ignore
    public void testGetURLFunction() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        int[] s = {0x00080080};

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(ASSIGN_TO_PUBLIC_TAG_FROM_GETURL_FUNCTION));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( result_dobj.getString(0x00080080), "text_from_URL");
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }


}
