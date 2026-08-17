package org.nrg.dicom.mizer.objects;

import org.junit.Test;
import org.nrg.dicom.mizer.tags.*;

import java.util.Arrays;

import static org.junit.Assert.*;

import static org.nrg.dicom.mizer.TestUtils.*;

public class PrivateBlockTest {

    @Test
    public void testHash() {
        PrivateBlock pb1 = new PrivateBlock( new int[]{1,2,3}, 0x10, "foo");
        PrivateBlock pb2 = new PrivateBlock( new int[]{3,2,1}, 0x10, "foo");
        assertNotEquals( pb1.hashCode(), pb2.hashCode());
    }

    /**
     * PrivateBlock holds the group in a short, so a group >= 0x8000 sign-extends when it is compared against
     * the unsigned group taken from a tag.
     */
    @Test
    public void testHighGroupPvtCreatorForTag() {
        PrivateBlock pb = new PrivateBlock(new int[]{0x00091200, 0}, 0xF2150010, "foo");

        assertEquals(0xF215, pb.getGroupAsInt());
        assertEquals(0x10, pb.getBlockTag());
        assertTrue(pb.isPvtCreatorForTag(new int[]{0x00091200, 0, 0xF2151050}));
        assertFalse(pb.isPvtCreatorForTag(new int[]{0x00091200, 0, 0x12151050}));
    }

    @Test
    public void testBlocks() {
        // private creator ID
        PrivateBlock pb1 = new PrivateBlock( new int[]{0x20010010}, 0x20010010, "foo");
        // tag in above block.
        PrivateBlock pb2 = new PrivateBlock( new int[]{0x20011000}, 0x20011000, "foo");
        assertEquals( pb1.getBlockTag(), pb2.getBlockTag());
    }

    /**
     * <pre>
     * 0009,0012) LO #2 [p1] Private Creator Data Element
     * (0009,1200) SQ #-1 [1 item] ?
     * >ITEM #1:
     * >(0023,0020) LO #2 [s1] Private Creator Data Element
     * >(0023,2015) SQ #-1 [1 item] ?
     * >>ITEM #1:
     * >>(0025,0011) LO #6 [s1_s1] Private Creator Data Element
     * </pre>
     */
    @Test
    public void testPvtSeqItems() {
        TestTag p1 = new TestTag(0x00090012, "p1");
        TestSeqTag s1 = new TestSeqTag( new int[] {0x00091200,0,0x00230020}, "s1");
        TestSeqTag s1_s1 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00250011}, "s1_s1");

        final DicomObjectI dobj = DicomObjectFactory.newInstance();

        put( dobj, p1);
        put( dobj, s1);
        put( dobj, s1_s1);

        TagPath tp1 = new TagPath();
        tp1.addTag( new TagPrivateCreator( p1.tag, p1.initialValue));
        PrivateBlock pb1 = tp1.getPrivateBlock().orElse(null);
        assertTrue(Arrays.equals(new int[0], pb1.getItem() ));
        assertEquals( 0x12, pb1.getBlockTag());
        assertEquals(0x0009, pb1.getGroupAsInt());
        assertEquals( p1.initialValue, pb1.getCreatorID());

        TagPath tp2 = new TagPath();
        tp2.addTag( new TagSequence(new TagPrivate(0x00091200, "p1"), 0)).addTag(new TagPrivateCreator(0x00230020, "s1"));
        PrivateBlock pb2 = tp2.getPrivateBlock().orElse(null);
        assertTrue(Arrays.equals( new int[]{0x00091200,0}, pb2.getItem() ));
        assertEquals( 0x20, pb2.getBlockTag());
        assertEquals(0x0023, pb2.getGroupAsInt());
        assertEquals( s1.initialValue, pb2.getCreatorID());

        TagPath tp3 = new TagPath();
        tp3.addTag( new TagSequence(new TagPrivate(0x00091200, "p1"), 0))
                .addTag( new TagSequence( new TagPrivate(0x00232015, "s1"), 0))
                .addTag( new TagPrivateCreator(0x00250011, "s1_s1"));
        PrivateBlock pb3 = tp3.getPrivateBlock().orElse(null);
        assertTrue(Arrays.equals(new int[]{0x00091200,0,0x00232015,0}, pb3.getItem() ));
        assertEquals( 0x11, pb3.getBlockTag());
        assertEquals(0x0025, pb3.getGroupAsInt());
        assertEquals( s1_s1.initialValue, pb3.getCreatorID());
    }
}