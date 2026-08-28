package org.nrg.dicom.mizer.tags;

import junit.framework.TestCase;
import org.junit.Test;

public class TagPrivateTest extends TestCase {

    @Test
    public void test() {
        TagPrivate tagPrivate = new TagPrivate(0x001920FF, "foo");

        assertEquals( 0x0019, tagPrivate.getGroupAsInt());
        assertEquals( 0x20FF, tagPrivate.getElementAsInt());
        assertEquals( 0x20, tagPrivate.getPrivateBlock());
        assertEquals( 0x00190010, tagPrivate.getPvtCreatorIDTag());
        assertEquals( "foo", tagPrivate.getPvtCreatorID());
    }

    /**
     * A private group >= 0x8000 makes the gggg+eeee composite exceed Integer.MAX_VALUE, so it has to be
     * parsed as unsigned rather than as a signed int.
     */
    @Test
    public void testHighPrivateGroup() {
        TagPrivate tagPrivate = new TagPrivate("F215", "foo", "1050");

        assertEquals(0xF215, tagPrivate.getGroupAsInt());
        assertEquals(0x1050, tagPrivate.getElementAsInt());
        assertEquals(0x10, tagPrivate.getPrivateBlock());
        assertEquals(0xF2151050, tagPrivate.asInt());
        assertEquals(0xF2150010, tagPrivate.getPvtCreatorIDTag());
        assertEquals("foo", tagPrivate.getPvtCreatorID());
    }

    /**
     * 0x7FFF is the last group whose composite parses as a signed int, 0x8000 the first that does not.
     */
    @Test
    public void testPrivateGroupSignBoundary() {
        assertEquals(0x7FFF1050, new TagPrivate("7FFF", "foo", "1050").asInt());
        assertEquals(0x80001050, new TagPrivate("8000", "foo", "1050").asInt());
        assertEquals(0x97531050, new TagPrivate("9753", "foo", "1050").asInt());
        assertEquals(0xFFFF1050, new TagPrivate("FFFF", "foo", "1050").asInt());
    }

    /**
     * The int constructor round-trips the tag through Integer.toHexString, so it has to survive the same range.
     */
    @Test
    public void testHighPrivateGroupFromInt() {
        TagPrivate tagPrivate = new TagPrivate(0xF2151050, "foo");

        assertEquals(0xF215, tagPrivate.getGroupAsInt());
        assertEquals(0xF2151050, tagPrivate.asInt());
        assertEquals(0xF2150010, tagPrivate.getPvtCreatorIDTag());
    }

    @Test
    public void testInPrivateBlock() {
        TagPrivateCreator tagBlock = new TagPrivateCreator(0x00190020, "foo");

        assertTrue( new TagPrivate(0x001920FF, "foo").isInPrivateCreatorBlock( tagBlock));
        assertTrue( new TagPrivate(0x001920FF, null).isInPrivateCreatorBlock( tagBlock));
        assertFalse( new TagPrivate( 0x001910FF, "foo").isInPrivateCreatorBlock( tagBlock));
    }

    @Test
    public void testEquals() {

        assertTrue( new TagPrivate(0x001920FF, "foo").equals( new TagPrivate(0x001920FF, "foo")));
        assertFalse( new TagPrivate(0x001920FF, "foo").equals( new TagPrivate(0x001920FF, null)));
        assertFalse( new TagPrivate(0x001920FF, "foo").equals( new TagPrivate(0x001910FF, "foo")));

    }

    @Test
    public void testExpectedPvtCreatorID() {
        TagPrivate tp = new TagPrivate( 0x00211113, "foo");
        TagPrivateCreator tpc = tp.getExpectedCreatorIDTag();
        assertEquals( 0x0021, tpc.getGroupAsInt());
    }

}