package org.nrg.dicom.mizer.tags;

import junit.framework.TestCase;
import org.junit.Test;

public class TagPrivateCreatorTest extends TestCase {

    @Test
    public void test() {

        TagPrivateCreator tag = new TagPrivateCreator(0x00190011, "foo");

        assertEquals(0x0019, tag.getGroupAsInt());
        assertEquals(0x0011, tag.getElementAsInt());
        assertEquals( 0x00190011, tag.asInt());
        assertEquals(0x11, tag.getPrivateBlock());
        assertEquals(0x00190011, tag.getPvtCreatorIDTag());
        assertEquals("foo", tag.getPvtCreatorID());
    }

    @Test
    public void testInPrivateBlock() {
        TagPrivateCreator tagBlock = new TagPrivateCreator(0x00190020, "foo");

        assertTrue( new TagPrivateCreator(0x00192020, "foo").isInPrivateCreatorBlock( tagBlock));
//        assertTrue( new TagPrivateCreator(0x001920FF, null).isInPrivateCreatorBlock( tagBlock));
//        assertFalse( new TagPrivateCreator( 0x001910FF, "foo").isInPrivateCreatorBlock( tagBlock));
    }

    @Test
    public void testEquals() {

        assertTrue( new TagPrivate(0x001920FF, "foo").equals( new TagPrivate(0x001920FF, "foo")));
        assertFalse( new TagPrivate(0x001920FF, "foo").equals( new TagPrivate(0x001920FF, null)));
        assertFalse( new TagPrivate(0x001920FF, "foo").equals( new TagPrivate(0x001910FF, "foo")));

    }

}