package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;
import org.nrg.dicom.dicomedit.TestUtils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMissingPrivateCreatorID {
    private static final Logger logger = LoggerFactory.getLogger(TestMissingPrivateCreatorID.class);

    @Test
    public void testMissingCreatorID() {

        String script =
                "(0010,0010) := \"pn\" \n"
                        + "(0010,0020) := (0021,{PT Test Creator ID}10)";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        // normal private tag
        int[] p_creator_id = {0x00210010};
        int[] p1 = {0x00211010};
        int t1 = 0x00100020;

//        src_dobj.putString( p_creator_id, "PT Test Creator ID");
        src_dobj.putString( p1, "Simple PT");

        assertFalse( src_dobj.contains(p_creator_id));
        assertTrue( src_dobj.contains(p1));
        assertFalse( src_dobj.contains(t1));

        final BaseScriptApplicator sa;
        try {
            sa = BaseScriptApplicator.getInstance( bytes( script));
            sa.apply(src_dobj);

        } catch (MizerException e) {
            fail("Unexpected exception: " + e);
        }

        assertEquals( "pn", src_dobj.getString( 0x00100010));
//        assertFalse( src_dobj.contains(p_creator_id));
        assertTrue( src_dobj.contains(p1));
        assertTrue( src_dobj.contains(t1));
        assertEquals( "Simple PT", src_dobj.getString( t1));
    }

    @Test
    public void testAddCreatorID() {

        String script =
                "(0010,0010) := \"pn\" \n"
                        + "set[ \"(0021,0010)\", \"PT Test Creator ID\"] \n"
                        + "(0010,0020) := (0021,{PT Test Creator ID}10)";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        // normal private tag
        int[] p_creator_id = {0x00210010};
        int[] p1 = {0x00211010};
        int t1 = 0x00100020;

//        src_dobj.putString( p_creator_id, "PT Test Creator ID");
        src_dobj.putString( p1, "Simple PT");

        assertFalse( src_dobj.contains(p_creator_id));
        assertTrue( src_dobj.contains(p1));
        assertFalse( src_dobj.contains(t1));

        final BaseScriptApplicator sa;
        try {
            sa = BaseScriptApplicator.getInstance( bytes( script));
            sa.apply(src_dobj);

        } catch (MizerException e) {
            fail("Unexpected exception: " + e);
        }

        assertTrue( src_dobj.contains(p_creator_id));
        assertTrue( src_dobj.contains(p1));
        assertTrue( src_dobj.contains(t1));
        assertEquals( "Simple PT", src_dobj.getString( t1));
    }

    /**
     * Emageon data
     *
     * {0x00090012} = "EMAGEON JPEG2K INFO"
     *
     * {0x00091200,0,0x00232013};
     * {0x00091200,0,0x00232015};
     * {0x00091200,0,0x00232015,0,0x00232016};
     * {0x00091200,0,0x00232015,0,0x00232017};
     * {0x00091200,0,0x00232015,0,0x00232018};
     * {0x00091200,0,0x00232015,0,0x00232019};
     *
     * Missing pvt creator ids:
     * {0x00091200,0,0x00230020};
     * {0x00091200,0,0x00232015,0,0x00230020};
     *
     * Create them with enclosing {0x00090012} = "EMAGEON JPEG2K INFO"
     */
    @Test
    public void testDE_61() {
        String script =
                "- (0009,{EMAGEON JPEG2K INFO}00)";

        TestUtils.TestTag p1c = new TestUtils.TestTag( 0x00090012, "EMAGEON JPEG2K INFO");

        // Missing creator id for sequence item
        TestSeqTag p1_ps1c = new TestSeqTag( new int[] {0x00091200,0,0x00230020}, "p1_ps1");
        TestSeqTag p1_ps1_13 = new TestSeqTag( new int[] {0x00091200,0,0x00232013}, "p1_ps1_13");

        // Missing creator id for sequence item.
        TestSeqTag p1_ps1_ps1c = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00230020}, "p1_ps1_ps1c");
        TestSeqTag p1_ps1_ps1_16 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00232016}, "p1_ps1_ps1_16");
        TestSeqTag p1_ps1_ps1_17 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00232017}, "p1_ps1_ps1_17");
        TestSeqTag p1_ps1_ps1_18 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00232018}, "p1_ps1_ps1_18");
        TestSeqTag p1_ps1_ps1_19 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00232019}, "p1_ps1_ps1_19");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( p1c.tag, p1c.initialValue);
        // Don't add missing creator id
        // dobj.putString( p1_ps1c.tag, p1_ps1c.value);
        dobj.putString( p1_ps1_13.tag, p1_ps1_13.initialValue);
        // Don't add missing creator id
        // dobj.putString( p1_ps1_ps1c.tag, p1_ps1_ps1c.value);
        dobj.putString( p1_ps1_ps1_16.tag, p1_ps1_ps1_16.initialValue);
        dobj.putString( p1_ps1_ps1_17.tag, p1_ps1_ps1_17.initialValue);
        dobj.putString( p1_ps1_ps1_18.tag, p1_ps1_ps1_18.initialValue);
        dobj.putString( p1_ps1_ps1_19.tag, p1_ps1_ps1_19.initialValue);

        assertEquals( p1c.initialValue, dobj.getString( p1c.tag));
        assertFalse( dobj.contains( p1_ps1c.tag));
        assertEquals( p1_ps1_13.initialValue, dobj.getString( p1_ps1_13.tag));
        assertFalse( dobj.contains( p1_ps1_ps1c.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_16.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_17.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_18.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_19.tag));

        final BaseScriptApplicator sa;
        try {
            sa = BaseScriptApplicator.getInstance( bytes( script));
            logger.info( dobj.toCompleteString());
            sa.apply(dobj);
            logger.info( dobj.toCompleteString());
        } catch (MizerException e) {
            fail("Unexpected exception: " + e);
        }

        assertEquals( p1c.initialValue, dobj.getString( p1c.tag));
        assertFalse( dobj.contains( p1_ps1c.tag));
        assertFalse( dobj.contains( p1_ps1_13.tag));
        assertFalse( dobj.contains( p1_ps1_ps1c.tag));
        assertFalse( dobj.contains( p1_ps1_ps1_16.tag));
        assertFalse( dobj.contains( p1_ps1_ps1_17.tag));
        assertFalse( dobj.contains( p1_ps1_ps1_18.tag));
        assertFalse( dobj.contains( p1_ps1_ps1_19.tag));
    }

    /**
     * Emageon data
     *
     * {0x00090012} = "EMAGEON JPEG2K INFO"
     *
     * {0x00091200,0,0x00232013};
     * {0x00091200,0,0x00232015};
     * {0x00091200,0,0x00232015,0,0x00232016};
     * {0x00091200,0,0x00232015,0,0x00232017};
     * {0x00091200,0,0x00232015,0,0x00232018};
     * {0x00091200,0,0x00232015,0,0x00232019};
     *
     * Missing pvt creator ids:
     * {0x00091200,0,0x00230020};
     * {0x00091200,0,0x00232015,0,0x00230020};
     *
     * Create them with enclosing {0x00090012} = "EMAGEON JPEG2K INFO"
     */
    @Test
    public void testDE_61_2() {
        String script =
                "- (0009,{EMAGEON JPEG2K INFO}00)[%]/(0009,{EMAGEON JPEG2K INFO}13)";

        TestUtils.TestTag p1c = new TestUtils.TestTag( 0x00090012, "EMAGEON JPEG2K INFO");

        // Missing creator id for sequence item
        TestSeqTag p1_ps1c = new TestSeqTag( new int[] {0x00091200,0,0x00230020}, "p1_ps1");
        TestSeqTag p1_ps1_13 = new TestSeqTag( new int[] {0x00091200,0,0x00232013}, "p1_ps1_13");

        // Missing creator id for sequence item.
        TestSeqTag p1_ps1_ps1c = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00230020}, "p1_ps1_ps1c");
        TestSeqTag p1_ps1_ps1_16 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00232016}, "p1_ps1_ps1_16");
        TestSeqTag p1_ps1_ps1_17 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00232017}, "p1_ps1_ps1_17");
        TestSeqTag p1_ps1_ps1_18 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00232018}, "p1_ps1_ps1_18");
        TestSeqTag p1_ps1_ps1_19 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00232019}, "p1_ps1_ps1_19");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( p1c.tag, p1c.initialValue);
        // Don't add missing creator id
        // dobj.putString( p1_ps1c.tag, p1_ps1c.value);
        dobj.putString( p1_ps1_13.tag, p1_ps1_13.initialValue);
        // Don't add missing creator id
        // dobj.putString( p1_ps1_ps1c.tag, p1_ps1_ps1c.value);
        dobj.putString( p1_ps1_ps1_16.tag, p1_ps1_ps1_16.initialValue);
        dobj.putString( p1_ps1_ps1_17.tag, p1_ps1_ps1_17.initialValue);
        dobj.putString( p1_ps1_ps1_18.tag, p1_ps1_ps1_18.initialValue);
        dobj.putString( p1_ps1_ps1_19.tag, p1_ps1_ps1_19.initialValue);

        assertEquals( p1c.initialValue, dobj.getString( p1c.tag));
        assertFalse( dobj.contains( p1_ps1c.tag));
        assertEquals( p1_ps1_13.initialValue, dobj.getString( p1_ps1_13.tag));
        assertFalse( dobj.contains( p1_ps1_ps1c.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_16.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_17.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_18.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_19.tag));

        final BaseScriptApplicator sa;
        try {
            sa = BaseScriptApplicator.getInstance( bytes( script));
            logger.info( dobj.toCompleteString());
            sa.apply(dobj);
            logger.info( dobj.toCompleteString());
        } catch (MizerException e) {
            fail("Unexpected exception: " + e);
        }

        assertEquals( p1c.initialValue, dobj.getString( p1c.tag));
        assertFalse( dobj.contains( p1_ps1c.tag));
        assertTrue( dobj.contains( p1_ps1_13.tag));
        assertFalse( dobj.contains( p1_ps1_ps1c.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_16.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_17.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_18.tag));
        assertTrue( dobj.contains( p1_ps1_ps1_19.tag));
    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
