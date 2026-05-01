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