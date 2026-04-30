/*
 * DicomEdit: TestFunctions
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.junit.Assert.assertEquals;

import static org.nrg.dicom.dicomedit.TestUtils.TestTag;
import static org.nrg.dicom.dicomedit.TestUtils.put;
import static org.nrg.dicom.dicomedit.TestUtils.bytes;

public class TestMagicCaret {

    @Test
    public void testHappy() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00100010, "Doe^John", "Handsome!");
        put( dobj, t1);

        String script =
                "sPatientName := (0010,0010)" +
                "sPatientName = \"Doe^John\" ? (0010,0010) := \"Handsome!\" : (0010,0010) := \"Ugly!!\"\n";

        assertEquals( "Doe^John", dobj.getString( t1.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals(  t1.postValue,  dobj.getString( t1.tag));
    }

}
