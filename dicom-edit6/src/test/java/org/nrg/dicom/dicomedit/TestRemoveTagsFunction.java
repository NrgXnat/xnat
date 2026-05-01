package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.nrg.dicom.dicomedit.TestUtils.*;

public class TestRemoveTagsFunction {
    @Test
    public void basicTagsList() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00080080, "Some Institution");
        TestTag t2 = new TestTag( 0x00100010, "PatientName");
        TestTag t3 = new TestTag( 0x0008103E, "Some Series Description");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));

        String script = "tagsToRemove := {(0008,0080), (0010,0010)}\n"
                + "removeTags[tagsToRemove]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertFalse( dobj.contains(t1.tag));
        assertFalse( dobj.contains(t2.tag));
        assertTrue( dobj.contains(t3.tag));
    }

    @Test
    public void basicTags() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00080080, "Some Institution");
        TestTag t2 = new TestTag( 0x00100010, "PatientName");
        TestTag t3 = new TestTag( 0x0008103E, "Some Series Description");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));

        String script = "tagsToRemove := {(0010,0010)}\n"
                + "removeTags[(0008,0080), tagsToRemove]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertFalse( dobj.contains(t1.tag));
        assertFalse( dobj.contains(t2.tag));
        assertTrue( dobj.contains(t3.tag));
    }

    @Test
    public void sequenceTags() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00080080, "Some Institution");
        TestTag t2 = new TestTag( 0x00100010, "PatientName");
        TestTag t3 = new TestTag( 0x0008103E, "Some Series Description");
        TestSeqTag s1_0_t1 = new TestSeqTag(new int[]{0x00120064,0,0x00100010}, "s1_0_t1");
        TestSeqTag s1_1_t1 = new TestSeqTag(new int[]{0x00120064,1,0x00100010}, "s1_1_t1");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);
        put( dobj, s1_0_t1);
        put( dobj, s1_1_t1);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));
        assertEquals( s1_0_t1.initialValue, dobj.getString( s1_0_t1.tag));
        assertEquals( s1_1_t1.initialValue, dobj.getString( s1_1_t1.tag));

        String script = "tagsToRemove := {(0008,0080), (0012,0064)}\n"
                + "removeTags[tagsToRemove]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertFalse( dobj.contains(t1.tag));
        assertTrue( dobj.contains(t2.tag));
        assertTrue( dobj.contains(t3.tag));
        TestTag s1 = new TestTag( 0x00120064, "Seq tags don't really have a value");

        assertFalse( dobj.contains(s1.tag));
    }

    @Test
    public void subSequenceTags() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00080080, "Some Institution");
        TestTag t2 = new TestTag( 0x00100010, "PatientName");
        TestTag t3 = new TestTag( 0x0008103E, "Some Series Description");
        TestSeqTag s1_0_t1 = new TestSeqTag(new int[]{0x00120064,0,0x00100010}, "s1_0_t1");
        TestSeqTag s1_0_t2 = new TestSeqTag(new int[]{0x00120064,0,0x00100020}, "s1_0_t2");
        TestSeqTag s1_1_t1 = new TestSeqTag(new int[]{0x00120064,1,0x00100010}, "s1_1_t1");
        TestSeqTag s1_1_t2 = new TestSeqTag(new int[]{0x00120064,1,0x00100020}, "s1_1_t2");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);
        put( dobj, s1_0_t1);
        put( dobj, s1_1_t1);
        put( dobj, s1_0_t2);
        put( dobj, s1_1_t2);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));
        assertEquals( s1_0_t1.initialValue, dobj.getString( s1_0_t1.tag));
        assertEquals( s1_1_t1.initialValue, dobj.getString( s1_1_t1.tag));
        assertEquals( s1_0_t2.initialValue, dobj.getString( s1_0_t2.tag));
        assertEquals( s1_1_t2.initialValue, dobj.getString( s1_1_t2.tag));

        String script = "tagsToRemove := {(0008,0080), (0012,0064)/(0010,0020)}\n"
                + "removeTags[tagsToRemove]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertFalse( dobj.contains(t1.tag));
        assertTrue( dobj.contains(t2.tag));
        assertTrue( dobj.contains(t3.tag));
        assertTrue( dobj.contains(s1_0_t1.tag));
        assertTrue( dobj.contains(s1_1_t1.tag));
        assertFalse( dobj.contains(s1_0_t2.tag));
        assertFalse( dobj.contains(s1_1_t2.tag));
    }

    /**
     * Sequence contains 3 items. The entire contents of the second item are removed. The empty item remains.
     * @throws MizerException
     */
    @Test
    public void sequenceItemContents() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestSeqTag s1_0_t1 = new TestSeqTag(new int[]{0x00120064,0,0x00100010}, "s1_0_t1");
        TestSeqTag s1_0_t2 = new TestSeqTag(new int[]{0x00120064,0,0x00100020}, "s1_0_t2");
        TestSeqTag s1_1_t1 = new TestSeqTag(new int[]{0x00120064,1,0x00100010}, "s1_1_t1");
        TestSeqTag s1_1_t2 = new TestSeqTag(new int[]{0x00120064,1,0x00100020}, "s1_1_t2");
        TestSeqTag s1_2_t1 = new TestSeqTag(new int[]{0x00120064,2,0x00100010}, "s1_2_t1");
        TestSeqTag s1_2_t2 = new TestSeqTag(new int[]{0x00120064,2,0x00100020}, "s1_2_t2");

        put( dobj, s1_0_t1);
        put( dobj, s1_1_t1);
        put( dobj, s1_2_t1);
        put( dobj, s1_0_t2);
        put( dobj, s1_1_t2);
        put( dobj, s1_2_t2);

        assertEquals( s1_0_t1.initialValue, dobj.getString( s1_0_t1.tag));
        assertEquals( s1_1_t1.initialValue, dobj.getString( s1_1_t1.tag));
        assertEquals( s1_2_t1.initialValue, dobj.getString( s1_2_t1.tag));
        assertEquals( s1_0_t2.initialValue, dobj.getString( s1_0_t2.tag));
        assertEquals( s1_1_t2.initialValue, dobj.getString( s1_1_t2.tag));
        assertEquals( s1_2_t2.initialValue, dobj.getString( s1_2_t2.tag));

        String script = "tagsToRemove := {(0012,0064)[1]/(0010,00XX)}\n"
                + "removeTags[tagsToRemove]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertTrue(  dobj.contains(s1_0_t1.tag));
        assertTrue(  dobj.contains(s1_0_t2.tag));
        assertFalse( dobj.contains(s1_1_t1.tag));
        assertFalse( dobj.contains(s1_1_t2.tag));
        assertTrue(  dobj.contains(s1_2_t1.tag));
        assertTrue(  dobj.contains(s1_2_t2.tag));
        assertTrue(  dobj.getItem(new int[]{0x00120064,1}).isEmpty());
    }

    /**
     * Sequence contains 3 items. The entire contents of the first item are removed. The empty item remains.
     * @throws MizerException
     */
    @Test
    public void sequenceItem() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestSeqTag s1_0_t1 = new TestSeqTag(new int[]{0x00120064,0,0x00100010}, "s1_0_t1");
        TestSeqTag s1_0_t2 = new TestSeqTag(new int[]{0x00120064,0,0x00100020}, "s1_0_t2");
        TestSeqTag s1_0_s1_0_t1 = new TestSeqTag(new int[]{0x00120064,0,0x00120064,0,0x00100020}, "s1_0_s1_0_t1");
        TestSeqTag s1_1_t1 = new TestSeqTag(new int[]{0x00120064,1,0x00100010}, "s1_1_t1");
        TestSeqTag s1_1_t2 = new TestSeqTag(new int[]{0x00120064,1,0x00100020}, "s1_1_t2");
        TestSeqTag s1_2_t1 = new TestSeqTag(new int[]{0x00120064,2,0x00100010}, "s1_2_t1");
        TestSeqTag s1_2_t2 = new TestSeqTag(new int[]{0x00120064,2,0x00100020}, "s1_2_t2");

        put( dobj, s1_0_t1);
        put( dobj, s1_0_t2);
        put( dobj, s1_0_s1_0_t1);
        put( dobj, s1_1_t1);
        put( dobj, s1_1_t2);
        put( dobj, s1_2_t1);
        put( dobj, s1_2_t2);

        assertEquals( s1_0_t1.initialValue, dobj.getString( s1_0_t1.tag));
        assertEquals( s1_1_t1.initialValue, dobj.getString( s1_1_t1.tag));
        assertEquals( s1_2_t1.initialValue, dobj.getString( s1_2_t1.tag));
        assertEquals( s1_0_t2.initialValue, dobj.getString( s1_0_t2.tag));
        assertEquals( s1_1_t2.initialValue, dobj.getString( s1_1_t2.tag));
        assertEquals( s1_2_t2.initialValue, dobj.getString( s1_2_t2.tag));
        assertEquals( s1_0_s1_0_t1.initialValue, dobj.getString( s1_0_s1_0_t1.tag));

        String script = "tagsToRemove := {(0012,0064)[0]}\n"
                + "removeTags[tagsToRemove]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertFalse( dobj.contains(s1_0_t1.tag));
        assertFalse( dobj.contains(s1_0_t2.tag));
        assertTrue(  dobj.contains(s1_1_t1.tag));
        assertTrue(  dobj.contains(s1_1_t2.tag));
        assertTrue(  dobj.contains(s1_2_t1.tag));
        assertTrue(  dobj.contains(s1_2_t2.tag));
        assertTrue(  dobj.getItem(new int[]{0x00120064,0}).isEmpty());
    }

    @Test
    public void privateTags() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag(0x00080080, "Some Institution");
        TestTag t2 = new TestTag(0x00100010, "PatientName");
        TestTag t3 = new TestTag(0x0008103E, "Some Series Description");
        TestTag p1_c = new TestTag(0x00190010, "P1 creator ID");
        TestTag p1_t1 = new TestTag(0x00191011, "p1_t1");
        TestTag p1_t2 = new TestTag(0x001910AA, "p1_t2");
        TestTag p2_c = new TestTag(0x00190011, "P2 creator ID");
        TestTag p2_t1 = new TestTag(0x00191111, "p2_t1");
        TestTag p2_t2 = new TestTag(0x001911AA, "p2_t2");

        put(dobj, t1);
        put(dobj, t2);
        put(dobj, t3);
        put(dobj, p1_c);
        put(dobj, p1_t1);
        put(dobj, p1_t2);
        put(dobj, p2_c);
        put(dobj, p2_t1);
        put(dobj, p2_t2);

        assertEquals(t1.initialValue, dobj.getString(t1.tag));
        assertEquals(t2.initialValue, dobj.getString(t2.tag));
        assertEquals(t3.initialValue, dobj.getString(t3.tag));
        assertEquals(p1_c.initialValue, dobj.getString(p1_c.tag));
        assertEquals(p1_t1.initialValue, dobj.getString(p1_t1.tag));
        assertEquals(p1_t2.initialValue, dobj.getString(p1_t2.tag));
        assertEquals(p2_c.initialValue, dobj.getString(p2_c.tag));
        assertEquals(p2_t1.initialValue, dobj.getString(p2_t1.tag));
        assertEquals(p2_t2.initialValue, dobj.getString(p2_t2.tag));

        String script = "tagsToRemove := {(0008,0080), (0019,{P2 creator ID}xx)}\n"
                + "removeTags[tagsToRemove]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertFalse(dobj.contains(t1.tag));
        assertTrue(dobj.contains(t2.tag));
        assertTrue(dobj.contains(t3.tag));
        assertTrue(dobj.contains(p1_c.tag));
        assertTrue(dobj.contains(p1_t1.tag));
        assertTrue(dobj.contains(p1_t2.tag));
        // It is policy that creator-id tag of 'empty' private block is not removed.
        assertTrue(dobj.contains(p2_c.tag));
        assertFalse(dobj.contains(p2_t1.tag));
        assertFalse(dobj.contains(p2_t2.tag));
    }

    @Test
    public void wildCards() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00080080, "Some Institution");
        TestTag t2 = new TestTag( 0x00100010, "PatientName");
        TestTag t3 = new TestTag( 0x0008103E, "Some Series Description");
        TestTag t4 = new TestTag( 0x0008008E, "Something");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);
        put( dobj, t4);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));
        assertEquals( t4.initialValue, dobj.getString( t4.tag));

        String script = "tagsToRemove := {(0008,008X)}\n"
                + "removeTags[tagsToRemove]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertFalse( dobj.contains(t1.tag));
        assertTrue( dobj.contains(t2.tag));
        assertTrue( dobj.contains(t3.tag));
        assertFalse( dobj.contains(t4.tag));
    }

    @Test
    public void flatten() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00080080, "Some Institution");
        TestTag t2 = new TestTag( 0x00100010, "PatientName");
        TestTag t3 = new TestTag( 0x0008103E, "Some Series Description");
        TestTag t4 = new TestTag( 0x0008008E, "Something");

        put( dobj, t1);
        put( dobj, t2);
        put( dobj, t3);
        put( dobj, t4);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t2.initialValue, dobj.getString( t2.tag));
        assertEquals( t3.initialValue, dobj.getString( t3.tag));
        assertEquals( t4.initialValue, dobj.getString( t4.tag));

        String script = "tagsToRemove := {(0008,008X)}\n"
                + "removeTags[tagsToRemove, (0010,0010)]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply( dobj).getDicomObject();

        assertFalse( dobj.contains(t1.tag));
        assertFalse( dobj.contains(t2.tag));
        assertTrue( dobj.contains(t3.tag));
        assertFalse( dobj.contains(t4.tag));
    }

}
