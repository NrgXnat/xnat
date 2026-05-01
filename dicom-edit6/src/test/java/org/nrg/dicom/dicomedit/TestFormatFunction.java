package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class TestFormatFunction {

    @Test
    public void testFormatFunction() {
        try {
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

            int[] s = {0x00080080};
            src_dobj.putString(s, "Some Institution");

            assertTrue(src_dobj.contains(0x00080080));
            assertEquals(src_dobj.getString(s), "Some Institution");

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes("(0008,0080) := format[\"{0} {1}\", (0008,0080),\"for the win.\"]"));
                final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

                assertEquals(  "Some Institution for the win.", result_dobj.getString( 0x00080080) );
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
