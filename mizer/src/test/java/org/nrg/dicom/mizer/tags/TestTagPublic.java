package org.nrg.dicom.mizer.tags;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTagPublic {

    @Test
    public void testAsInt() {
        TagPublic tag = new TagPublic( "fffa", "fffa");

        int i = (int) tag.asInt();
        assertEquals( -327686, i);

        tag = new TagPublic( 0x7fffffff);
        assertEquals( Integer.MAX_VALUE, tag.asInt());
        tag = new TagPublic( 0x80000000);
        assertEquals( Integer.MIN_VALUE, tag.asInt());
        tag = new TagPublic( 0xffffffff);
        assertEquals( -1, tag.asInt());
    }
}
