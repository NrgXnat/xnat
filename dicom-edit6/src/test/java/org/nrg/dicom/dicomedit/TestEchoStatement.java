/*
 * DicomEdit: TestAssignmentStatement
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * Run tests of echo statement.
 *
 */
public class TestEchoStatement {

    // Note the new line as part of the value.
    @Test
    public void testEchoingAttribute() {
        try {
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

            int[] s = {0x00080080};
            src_dobj.putString(s, "Some Institution");

            assertTrue(src_dobj.contains(0x00080080));
            assertEquals(src_dobj.getString(s), "Some Institution");

            PrintStream out = System.out;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                System.setOut(new PrintStream(baos));

                final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes("echo (0008,0080)"));
                final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

                assertEquals("Some Institution\n", baos.toString());
                System.setOut(out);
            }

        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void testEchoingFunction() {
        try {
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

            int[] s = {0x00080080};
            src_dobj.putString(s, "Some Institution");

            assertTrue(src_dobj.contains(0x00080080));
            assertEquals(src_dobj.getString(s), "Some Institution");

            PrintStream out = System.out;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes("echo format[\"{0} {1}\", (0008,0080),\"for the win.\"]"));
                System.setOut(new PrintStream(baos));
                final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

                // There may be other unflushed stuff in std out.
                assertTrue(baos.toString().endsWith( "Some Institution for the win.\n") );
                System.setOut( out);
            }

        }
        catch( Exception e) {
            fail( "Unexpected exception: " + e);
        }
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
