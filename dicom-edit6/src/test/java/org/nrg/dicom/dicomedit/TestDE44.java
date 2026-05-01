package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;
import java.io.File;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * Compressed pixel data has Items like VR=SQ but not a sequence item.
 */
public class TestDE44 {

    @Test
    public void testRetainPrivateTags() {
        try {
            String script =
                    "retainPrivateTags[(0051,{PRIVATESTUFF}02), (0051,{PRIVATESTUFF}03)] \n";

            ClassLoader loader = TestDE44.class.getClassLoader();
            File file = new File( loader.getResource("dicom/DE44/horos_jpg2k.dcm").toURI());
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance( file);

            assertEquals("PRIVATESTUFF", src_dobj.getString(0x00510010));
            assertEquals("PRIVATE0", src_dobj.getString(0x00511000));
            assertEquals("PRIVATE1", src_dobj.getString(0x00511001));
            assertEquals("PRIVATE2", src_dobj.getString(0x00511002));
            assertEquals("PRIVATE3", src_dobj.getString(0x00511003));
            assertEquals("GEHC_CT_ADVAPP_001", src_dobj.getString(0x00530010));
            assertEquals("40", src_dobj.getString(0x00531043));

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
            final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

            assertEquals("PRIVATESTUFF", src_dobj.getString(0x00510010));
            assertNull( src_dobj.getString(0x00511000));
            assertNull( src_dobj.getString(0x00511001));
            assertEquals("PRIVATE2", src_dobj.getString(0x00511002));
            assertEquals("PRIVATE3", src_dobj.getString(0x00511003));
            assertNull( src_dobj.getString(0x00530010));
            assertNull( src_dobj.getString(0x00531043));
//            Path f = Files.createTempFile(Paths.get("/tmp"),"de6", ".dcm");
//            try (FileOutputStream os = new FileOutputStream(f.toFile())) {
//                result_dobj.write(os);
//            }
        }
        catch( Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void testRemoveAllPrivateTags() {
        try {
            String script =
                    "removeAllPrivateTags \n";

            ClassLoader loader = TestDE44.class.getClassLoader();
            File file = new File( loader.getResource("dicom/DE44/horos_jpg2k.dcm").toURI());
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance( file);

            assertEquals("PRIVATESTUFF", src_dobj.getString(0x00510010));
            assertEquals("PRIVATE0", src_dobj.getString(0x00511000));
            assertEquals("PRIVATE1", src_dobj.getString(0x00511001));
            assertEquals("PRIVATE2", src_dobj.getString(0x00511002));
            assertEquals("PRIVATE3", src_dobj.getString(0x00511003));
            assertEquals("GEHC_CT_ADVAPP_001", src_dobj.getString(0x00530010));
            assertEquals("40", src_dobj.getString(0x00531043));

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
            final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

            assertNull( src_dobj.getString(0x00510010));
            assertNull( src_dobj.getString(0x00511000));
            assertNull( src_dobj.getString(0x00511001));
            assertNull( src_dobj.getString(0x00511002));
            assertNull( src_dobj.getString(0x00511003));
            assertNull( src_dobj.getString(0x00530010));
            assertNull( src_dobj.getString(0x00531043));
        }
        catch( Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
