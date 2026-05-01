package org.nrg.dicom.mizer.objects;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPrivate;
import org.nrg.dicom.mizer.tags.TagPublic;
import org.nrg.dicom.mizer.tags.TagSequence;

import static org.junit.Assert.*;
import static org.nrg.dicom.mizer.TestUtils.put;

import static org.nrg.dicom.mizer.TestUtils.*;

/**
 * It is not possible to specify the private block when adding a private tag with dcm4che2. It will always use
 * the first available. This limits the variety of Dicom objects that can be created programmatically.
 */
public class TestDicomObjectI {

    /**
     * DicomObjectI.getString( int tag)
     * returns multivalued tags as single string with sub-values separated by '\'
     * returns missing tags as null.
     */
    @Test
    public void testGetString() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag t2 = new TestTag(0x0010002, "t1\\t2");
        TestTag empty = new TestTag(0x00100021, "");
        TestTag missing = new TestTag(0x00101001, "");
        TestTag unknownVR = new TestTag(0x00300102, "unknown");
        TestTag missing_unknownVR = new TestTag(0x00300103, "");
        TestTag empty_unknownVR = new TestTag(0x00300104, "");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, t2);
        put(dobj, empty);
        put(dobj, unknownVR);
        put(dobj, empty_unknownVR);

        assertTrue( dobj.contains( t1.tag));
        assertTrue( dobj.contains( t2.tag));
        assertTrue( dobj.contains( empty.tag));
        assertFalse( dobj.contains( missing.tag));
        assertTrue( dobj.contains( unknownVR.tag));
        assertFalse( dobj.contains( missing_unknownVR.tag));
        assertTrue( dobj.contains( empty_unknownVR.tag));

        String s = dobj.getString(empty.tag);
        String s2 = dobj.getString(missing.tag);
        String un = dobj.getString(unknownVR.tag);
        String missing_un = dobj.getString(missing_unknownVR.tag);
        String empty_un = dobj.getString(empty_unknownVR.tag);

        assertEquals( t1.initialValue, dobj.getString(t1.tag));
        assertEquals( t2.initialValue, dobj.getString(t2.tag));
        assertEquals( empty.initialValue, dobj.getString(empty.tag));
        assertNull( dobj.getString(missing.tag));
        assertTrue(StringUtils.isEmpty( dobj.getString(empty.tag)));
        assertTrue(StringUtils.isBlank( dobj.getString(empty.tag)));
        assertTrue(StringUtils.isEmpty( dobj.getString(missing.tag)));
        assertTrue(StringUtils.isBlank( dobj.getString(missing.tag)));
        assertEquals( unknownVR.initialValue, dobj.getString(unknownVR.tag));
        assertNull( dobj.getString(missing_unknownVR.tag));
    }

    @Test
    public void resolvePublic() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag p1c = new TestTag(0x20230010, "p1c");
        TestTag p1t1 = new TestTag(0x202310EF, "p1t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, p1c);
        put(dobj, p1t1);

        assertTrue(dobj.contains(t1.tag));
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1t1.tag));

        TagPath tp = new TagPath(new TagPublic(t1.tag));
        assertArrayEquals(new int[]{t1.tag}, dobj.resolveTagPath(tp).get());
    }

    @Test
    public void resolveMissingPublic() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag t2 = new TestTag(0x00100020, "t2");
        TestTag p1c = new TestTag(0x20230010, "p1c");
        TestTag p1t1 = new TestTag(0x202310EF, "p1t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, p1c);
        put(dobj, p1t1);

        assertTrue(dobj.contains(t1.tag));
        assertFalse(dobj.contains(t2.tag));
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1t1.tag));

        TagPath tp = new TagPath(new TagPublic(t2.tag));
        assertFalse( dobj.resolveTagPath(tp).isPresent());
    }

    @Test
    public void resolvePrivate() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag p1c = new TestTag(0x20230010, "p1c");
        TestTag p1t1 = new TestTag(0x202310EF, "p1t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, p1c);
        put(dobj, p1t1);

        assertTrue(dobj.contains(t1.tag));
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1t1.tag));

        TagPath tp = new TagPath(new TagPrivate(p1t1.tag, "p1c"));
        assertArrayEquals(new int[]{p1t1.tag}, dobj.resolveTagPath(tp).get());
    }

    @Test
    public void resolveMissingPrivate() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag p1c = new TestTag(0x20230010, "p1c");
        TestTag p1t1 = new TestTag(0x202310EF, "p1t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, p1c);
//        put(dobj, p1t1);

        assertTrue(dobj.contains(t1.tag));
        assertTrue(dobj.contains(p1c.tag));
//        assertTrue(dobj.contains(p1t1.tag));

        TagPath tp = new TagPath(new TagPrivate(p1t1.tag, "p1c"));
        assertFalse( dobj.resolveTagPath(tp).isPresent());
    }
    @Test
    public void resolveMissingPrivateWithMissingCreator() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag p1c = new TestTag(0x20230010, "p1c");
        TestTag p1t1 = new TestTag(0x202310EF, "p1t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
//        put(dobj, p1c);
//        put(dobj, p1t1);

        assertTrue(dobj.contains(t1.tag));
        assertFalse(dobj.contains(p1c.tag));
        assertFalse(dobj.contains(p1t1.tag));

        TagPath tp = new TagPath(new TagPrivate(p1t1.tag, "p1c"));
        assertFalse( dobj.resolveTagPath(tp).isPresent());
    }

    @Test
    public void resolvePrivateInSeq() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag p1c = new TestTag(0x20230010, "p1c");
        TestTag p1t1 = new TestTag(0x202313EF, "p1t1");
        TestSeqTag p1s1_0_p1c = new TestSeqTag(new int[]{0x202310EF,0,0x20050010}, "p1s1_0_p1c");
        TestSeqTag p1s1_0_p1t1 = new TestSeqTag(new int[]{0x202310EF,0,0x20051011}, "p1s1_0_p1t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, p1c);
        put(dobj, p1t1);
        put(dobj, p1s1_0_p1c);
        put(dobj, p1s1_0_p1t1);

        assertTrue(dobj.contains(t1.tag));
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1t1.tag));
        assertTrue(dobj.contains(p1s1_0_p1c.tag));
        assertTrue(dobj.contains(p1s1_0_p1t1.tag));

        TagPath tp = new TagPath();
        tp.addTag(new TagSequence( new TagPrivate(p1t1.tag, "p1c"), 0));
        tp.addTag(new TagPrivate(p1s1_0_p1t1.tag[2], "p1s1_0_p1c"));
        assertArrayEquals(new int[]{0x202310EF,0,0x20051011}, dobj.resolveTagPath(tp).get());
    }

    @Test
    public void resolvePrivateInMultiItemSeq() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag p1c = new TestTag(0x20230010, "p1c");
        TestTag p1t1 = new TestTag(0x202313EF, "p1t1");
        TestSeqTag p1s1_0_p1c = new TestSeqTag(new int[]{0x202310EF,0,0x20050010}, "p1s1_0_p1c");
        TestSeqTag p1s1_0_p1t1 = new TestSeqTag(new int[]{0x202310EF,0,0x20051011}, "p1s1_0_p1t1");
        TestSeqTag p1s1_1_p1c = new TestSeqTag(new int[]{0x202310EF,1,0x20050010}, "p1s1_1_p1c");
        TestSeqTag p1s1_1_p1t1 = new TestSeqTag(new int[]{0x202310EF,1,0x20051012}, "p1s1_1_p1t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, p1c);
        put(dobj, p1t1);
        put(dobj, p1s1_0_p1c);
        put(dobj, p1s1_0_p1t1);
        put(dobj, p1s1_1_p1c);
        put(dobj, p1s1_1_p1t1);

        assertTrue(dobj.contains(t1.tag));
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1t1.tag));
        assertTrue(dobj.contains(p1s1_0_p1c.tag));
        assertTrue(dobj.contains(p1s1_0_p1t1.tag));
        assertTrue(dobj.contains(p1s1_1_p1c.tag));
        assertTrue(dobj.contains(p1s1_1_p1t1.tag));

        TagPath tp1 = new TagPath();
        tp1.addTag(new TagSequence( new TagPrivate(p1t1.tag, "p1c"), 0));
        tp1.addTag(new TagPrivate(p1s1_0_p1t1.tag[2], "p1s1_0_p1c"));
        assertArrayEquals(new int[]{0x202310EF,0,0x20051011}, dobj.resolveTagPath(tp1).get());

        TagPath tp2 = new TagPath();
        tp2.addTag(new TagSequence( new TagPrivate(p1t1.tag, "p1c"), 1));
        tp2.addTag(new TagPrivate(p1s1_1_p1t1.tag[2], "p1s1_1_p1c"));
        assertArrayEquals(new int[]{0x202310EF,1,0x20051012}, dobj.resolveTagPath(tp2).get());
    }

    @Test
    public void resolveMultiItemSeqMissingItem() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestSeqTag t2_0_t1 = new TestSeqTag(new int[]{0x00209221,0,0x00209164}, "t2_0_t1");
        TestSeqTag t2_1_t1 = new TestSeqTag(new int[]{0x00209221,1,0x00209164}, "t2_1_t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, t2_0_t1);
//        put(dobj, t2_1_t1);

        assertTrue(dobj.contains(t1.tag));
        assertTrue(dobj.contains(t2_0_t1.tag));

        TagPath tp2_0_1 = new TagPath();
        tp2_0_1.addTag(new TagSequence( new TagPublic(t2_0_t1.tag[0]), 0));
        tp2_0_1.addTag(new TagPublic(t2_0_t1.tag[2]));
        assertArrayEquals(new int[]{0x00209221,0,0x00209164}, dobj.resolveTagPath(tp2_0_1).get());

        TagPath tp2_1_1 = new TagPath();
        tp2_1_1.addTag(new TagSequence( new TagPublic(t2_0_t1.tag[1]), 1));
        tp2_1_1.addTag(new TagPublic(t2_0_t1.tag[2]));
        assertFalse( dobj.resolveTagPath(tp2_1_1).isPresent());
    }

    @Test
    public void resolveCreateMissingItem() {
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestSeqTag t2_0_t1 = new TestSeqTag(new int[]{0x00209221,0,0x00209164}, "t2_0_t1");
        TestSeqTag t2_1_t1 = new TestSeqTag(new int[]{0x00209221,1,0x00209164}, "t2_1_t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, t2_0_t1);
        // This tag is missing
        // put(dobj, t2_1_t1);

        assertTrue(dobj.contains(t1.tag));
        assertTrue(dobj.contains(t2_0_t1.tag));

        TagPath tp2_0_1 = new TagPath();
        tp2_0_1.addTag(new TagSequence( new TagPublic(t2_0_t1.tag[0]), 0));
        tp2_0_1.addTag(new TagPublic(t2_0_t1.tag[2]));
        assertArrayEquals(new int[]{0x00209221,0,0x00209164}, dobj.resolveTagPath(tp2_0_1).get());

        TagPath tp2_1_1 = new TagPath();
        tp2_1_1.addTag(new TagSequence( new TagPublic(t2_1_t1.tag[1]), 1));
        tp2_1_1.addTag(new TagPublic(t2_1_t1.tag[2]));
        assertFalse( dobj.resolveTagPath(tp2_1_1).isPresent());
    }

    @Test
    public void testMixedSeqsRetain() throws MizerException {
        String script = "retainPrivateTags[ (0019,{pb1}02)/(0023,{pb2}15)[%]/*]";
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag pb1 = new TestTag(0x00190010, "pb1");
        TestTag pb1_1 = new TestTag(0x00191001, "pb1_1");
        TestTag pb1_2 = new TestTag(0x00191002, "pb1_2");
        TestSeqTag pb2 = new TestSeqTag( new int[] {0x00191002,0,0x00230010}, "pb2");
        TestSeqTag pb2_1 = new TestSeqTag( new int[] {0x00191002,0,0x00231001}, "pb2_1");
        TestSeqTag pb2_2 = new TestSeqTag( new int[] {0x00191002,0,0x00231015}, "pb2_2");
        TestSeqTag pb3 = new TestSeqTag( new int[] {0x00191002,0,0x00231015,0,0x00230010}, "pb3");
        TestSeqTag pb3_1 = new TestSeqTag( new int[] {0x00191002,0,0x00231015,0,0x002310FF}, "pb3_1");
        TestTag t2 = new TestTag(0x00401200, "t2");
        TestSeqTag t2_1 = new TestSeqTag( new int[] {0x00401200,0,0x00180021}, "t2_1");
        TestSeqTag t2_2 = new TestSeqTag( new int[] {0x00401200,0,0x00180022}, "t2_2");
        TestSeqTag pb4 = new TestSeqTag( new int[] {0x00401200,0,0x00290010}, "pb4");
        TestSeqTag pb4_1 = new TestSeqTag( new int[] {0x00401200,0,0x00291000}, "pb4_1");
        TestSeqTag pb4_1_t1 = new TestSeqTag( new int[] {0x00401200,0,0x00291000,0,0x00180010}, "pb4_1_t1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putString( t1.tag, t1.initialValue);
        dobj.putString( pb1.tag, pb1.initialValue);
        dobj.putString( pb1_1.tag, pb1_1.initialValue);
        dobj.putString( pb1_2.tag, pb1_2.initialValue);
        dobj.putString( pb2.tag, pb2.initialValue);
        dobj.putString( pb2_1.tag, pb2_1.initialValue);
        dobj.putString( pb2_2.tag, pb2_2.initialValue);
        dobj.putString( pb3.tag, pb3.initialValue);
        dobj.putString( pb3_1.tag, pb3_1.initialValue);
        dobj.putString( t2.tag, t2.initialValue);
        dobj.putString( t2_1.tag, t2_1.initialValue);
        dobj.putString( t2_2.tag, t2_2.initialValue);
        dobj.putString( pb4.tag, pb4.initialValue);
        dobj.putString( pb4_1.tag, pb4_1.initialValue);
        dobj.putString( pb4_1_t1.tag, pb4_1_t1.initialValue);

        assertTrue( dobj.contains( t1.tag));
        assertTrue( dobj.contains( pb1.tag));
        assertTrue( dobj.contains( pb1_1.tag));
        assertTrue( dobj.contains( pb1_2.tag));
        assertTrue( dobj.contains( pb2.tag));
        assertTrue( dobj.contains( pb2_1.tag));
        assertTrue( dobj.contains( pb2_2.tag));
        assertTrue( dobj.contains( pb3.tag));
        assertTrue( dobj.contains( pb3_1.tag));
        assertTrue( dobj.contains( t2.tag));
        assertTrue( dobj.contains( t2_1.tag));
        assertTrue( dobj.contains( t2_2.tag));
        assertTrue( dobj.contains( pb4.tag));
        assertTrue( dobj.contains( pb4_1.tag));
        assertTrue( dobj.contains( pb4_1_t1.tag));

        TagPath tp = new TagPath();
        tp.addTag(new TagSequence( new TagPublic(pb4_1_t1.tag[0]), pb4_1_t1.tag[1]));
        tp.addTag(new TagSequence( new TagPrivate(pb4_1_t1.tag[2], "pb4"), pb4_1_t1.tag[3]));
        tp.addTag(new TagPublic(pb4_1_t1.tag[4]));
        assertArrayEquals(new int[]{pb4_1_t1.tag[0],pb4_1_t1.tag[1],pb4_1_t1.tag[2],pb4_1_t1.tag[3],pb4_1_t1.tag[4]},
                dobj.resolveTagPath(tp).get());

        tp = new TagPath();
//        TestSeqTag pb3_1 = new TestSeqTag( new int[] {0x00191002,0,0x00231015,0,0x002310FF}, "pb3_1");
        tp.addTag(new TagSequence( new TagPrivate(pb3_1.tag[0],"pb1"), pb3_1.tag[1]));
        tp.addTag(new TagSequence( new TagPrivate(pb3_1.tag[2], "pb2"), pb3_1.tag[3]));
        tp.addTag(new TagPrivate(pb3_1.tag[4],"pb3"));
        assertArrayEquals(new int[]{pb3_1.tag[0],pb3_1.tag[1],pb3_1.tag[2],pb3_1.tag[3],pb3_1.tag[4]},
                dobj.resolveTagPath(tp).get());

    }

}
