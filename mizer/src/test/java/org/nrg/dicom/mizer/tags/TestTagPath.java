package org.nrg.dicom.mizer.tags;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestTagPath {

    @Test
    public void testCompareTo() {
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagPublic( 0x00100010));

        TagPath testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x00100010));
        assertTrue( tagPath.compareTo( testPath) == 0);

        testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x00080010));
        assertTrue( tagPath.compareTo( testPath) > 0);

        testPath = new TagPath();
        testPath.addTag( new TagPublic( 0x00180010));
        assertTrue( tagPath.compareTo( testPath) < 0);

        testPath = new TagPath();
        testPath.addTag( new TagPrivate( 0x00130010, "CTP"));
        assertTrue( tagPath.compareTo( testPath) < 0);

        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00100010), null));
        testPath.addTag( new TagPublic(0x00080010));
        assertTrue( tagPath.compareTo( testPath) < 0);

        testPath = new TagPath();
        testPath.addTag( new TagPublic(0x000F0010));
        assertTrue( tagPath.compareTo( testPath) > 0);

        testPath = new TagPath();
        testPath.addTag( new TagPublic(0x001A0010));
        assertTrue( tagPath.compareTo( testPath) < 0);
    }

    @Test
    public void testSequenceCompareTo() {
        // (0010,0010) [1] / (0008,0010)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagSequence( new TagPublic(0x00100010), 1));
        tagPath.addTag( new TagPublic(0x00080010));

        // (0010,0010) [1] / (0008,0010)
        TagPath testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00100010), 1));
        testPath.addTag( new TagPublic(0x00080010));
        assertTrue( tagPath.compareTo( testPath) == 0);

        // (0010,0010) [0] / (0008,0010)
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00100010), 0));
        testPath.addTag( new TagPublic(0x00080010));
        assertTrue( tagPath.compareTo( testPath) > 0);

        // (0010,0010) [2] / (0008,0010)
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00100010), 2));
        testPath.addTag( new TagPublic(0x00080010));
        assertTrue( tagPath.compareTo( testPath) < 0);

        // (0010,0010) / (0008,0010)
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00100010), 2));
        testPath.addTag( new TagPublic(0x00080010));
        assertTrue( tagPath.compareTo( testPath) < 0);
    }

    @Test
    public void testSequenceWildcard() {
        // (0010,0010) / (0008,0010)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagSequence( new TagPublic(0x00100010), null));
        tagPath.addTag( new TagPublic(0x00080010));

        // (0010,0010) [1] / (0008,0010)
        TagPath testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00100010), 1));
        testPath.addTag( new TagPublic(0x00080010));
        assertTrue( tagPath.compareTo( testPath) > 0);

        // (0010,0010) [0] / (0008,0010)
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00100010), 0));
        testPath.addTag( new TagPublic(0x00080010));
        assertTrue( tagPath.compareTo( testPath) > 0);

        // (0010,0010) / (0008,0010)
        testPath = new TagPath();
        testPath.addTag( new TagSequence( new TagPublic(0x00100010), null));
        testPath.addTag( new TagPublic(0x00080010));
        assertTrue( tagPath.compareTo( testPath) == 0);
    }

    @Test
    public void testEvenDigitWildcard() {
        // (0010,00@0)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagPublic("0010", "00@0"));

        // (0010,0010)
        TagPath testPath = new TagPath();
        testPath.addTag( new TagPublic(0x00100010));
        assertTrue( tagPath.compareTo( testPath) > 0);

        // (0010,00F0)
        testPath = new TagPath();
        testPath.addTag( new TagPublic(0x001000F0));
        assertTrue( tagPath.compareTo( testPath) < 0);

        // (0010,00E0)
        testPath = new TagPath();
        testPath.addTag( new TagPublic(0x001000E0));
        assertTrue( tagPath.compareTo( testPath) == 0);
    }

    @Test
    public void testOddDigitWildcard() {
        // (0010,00#0)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagPublic("0010", "00#0"));

        // (0010,0010)
        TagPath testPath = new TagPath();
        testPath.addTag( new TagPublic(0x00100010));
        assertTrue( tagPath.compareTo( testPath) > 0);

        // (0010,00F0)
        testPath = new TagPath();
        testPath.addTag( new TagPublic(0x001000F0));
        assertTrue( tagPath.compareTo( testPath) == 0);

        // (0010,00E0)
        testPath = new TagPath();
        testPath.addTag( new TagPublic(0x001000F1));
        assertTrue( tagPath.compareTo( testPath) < 0);
    }

    @Test
    public void testHexDigitWildcard() {
        // (0010,00x0)
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagPublic("0010", "00x0"));

        // (0010,0010)
        TagPath testPath = new TagPath();
        testPath.addTag( new TagPublic(0x00100010));
        assertTrue( tagPath.compareTo( testPath) > 0);

        // (0010,00F0)
        testPath = new TagPath();
        testPath.addTag( new TagPublic(0x001000F0));
        assertTrue( tagPath.compareTo( testPath) == 0);

        // (0010,00E0)
        testPath = new TagPath();
        testPath.addTag( new TagPublic(0x001000F1));
        assertTrue( tagPath.compareTo( testPath) < 0);
    }

    @Test
    public void testParentTagPath() {
        TagPath tagPath = new TagPath();
        TagPath parent = tagPath.getParentTagPath();
        assertEquals( 0, parent.size());

        tagPath = new TagPath();
        tagPath.addTag( new TagPublic(0x00100010));
        parent = tagPath.getParentTagPath();
        assertEquals( 0, parent.size());

        tagPath = new TagPath();
        tagPath.addTag( new TagSequence( new TagPublic(0x00100010), null));
        parent = tagPath.getParentTagPath();
        assertEquals( 0, parent.size());

        tagPath = new TagPath();
        tagPath.addTag( new TagSequence( new TagPublic(0x00100010), 0));
        parent = tagPath.getParentTagPath();
        assertEquals( 0, parent.size());

        tagPath = new TagPath();
        tagPath.addTag( new TagSequence( new TagPublic(0x00120010), 0));
        tagPath.addTag( new TagPublic( 0x00100010));
        parent = tagPath.getParentTagPath();
        assertEquals( 1, parent.size());

    }

    @Test
    public void testExpectedPvtCreatorID() {
        TagPath tagPath = new TagPath();
        tagPath.addTag( new TagSequence( new TagPublic(0x00120010), 0));
        tagPath.addTag( new TagPrivate( 0x00211113, null));
        TagPath tpc = tagPath.getExpectedPvtCreatorID();
        assertEquals( 0x00210011, tpc.getLastTag().asInt());
    }

    @Test
    public void testContains() {
        // the tags we are testing are singular
        Tag t1 = new TagPublic(0x00100010);
        Tag t2 = new TagPublic(0x00100020);
        Tag p1 = new TagPrivate(0x20050014, "pc1" );
        Tag p2 = new TagPrivate(0x20051314, "pc1" );
        Tag p3 = new TagPrivate(0x20051314, "fubar" );
        Tag pc1 = new TagPrivate(0x20050014, "pc1" );
        Tag pc2 = new TagPrivate(0x20051314, "pc1" );
        Tag pc3 = new TagPrivate(0x20051314, "fubar" );
        Tag s1 = new TagSequence(t1, 0);
        Tag s2 = new TagSequence(p1, 0);
        Tag s3 = new TagSequence(p2, 0);
        Tag s4 = new TagSequence(p3, 0);
        Tag s5 = new TagSequence(pc1, 0);
        Tag s6 = new TagSequence(pc2, 0);
        Tag s7 = new TagSequence(pc3, 0);
        Tag sw1 = new TagSequenceWildcard("*");
        Tag sw2 = new TagSequenceWildcard("+");
        Tag sw3 = new TagSequenceWildcard(".");

        TagPath tagPath = new TagPath().addTag(new TagPublic(0x00100010));
        assertTrue( tagPath.contains(t1));
        assertFalse( tagPath.contains(t2));
        assertFalse( tagPath.contains(p1));
        assertFalse( tagPath.contains(p2));
        assertFalse( tagPath.contains(p3));
        assertFalse( tagPath.contains(pc1));
        assertFalse( tagPath.contains(pc2));
        assertFalse( tagPath.contains(pc3));
        assertFalse( tagPath.contains(s1));
        assertFalse( tagPath.contains(s2));
        assertFalse( tagPath.contains(s3));
        assertFalse( tagPath.contains(s4));
        assertFalse( tagPath.contains(s5));
        assertFalse( tagPath.contains(s6));
        assertFalse( tagPath.contains(s7));
        assertFalse( tagPath.contains(sw1));
        assertFalse( tagPath.contains(sw2));
        assertFalse( tagPath.contains(sw3));

        tagPath = new TagPath().addTag(new TagPrivate(0x20050014,"pc1"));
        assertFalse( tagPath.contains(t1));
        assertFalse( tagPath.contains(t2));
        assertTrue( tagPath.contains(p1));
        assertTrue( tagPath.contains(p2));
        assertFalse( tagPath.contains(p3));
        assertTrue( tagPath.contains(pc1));
        assertTrue( tagPath.contains(pc2));
        assertFalse( tagPath.contains(pc3));
        assertFalse( tagPath.contains(s1));
        assertFalse( tagPath.contains(s2));
        assertFalse( tagPath.contains(s3));
        assertFalse( tagPath.contains(s4));
        assertFalse( tagPath.contains(s5));
        assertFalse( tagPath.contains(s6));
        assertFalse( tagPath.contains(s7));
        assertFalse( tagPath.contains(sw1));
        assertFalse( tagPath.contains(sw2));
        assertFalse( tagPath.contains(sw3));

        tagPath = new TagPath().addTag(new TagPrivate("2005","pc1","10XX"));
        assertFalse( tagPath.contains(t1));
        assertFalse( tagPath.contains(t2));
        assertTrue( tagPath.contains(p1));
        assertTrue( tagPath.contains(p2));
        assertFalse( tagPath.contains(p3));
        assertFalse( tagPath.contains(s1));
        assertFalse( tagPath.contains(s2));
        assertFalse( tagPath.contains(s3));
        assertFalse( tagPath.contains(s4));
        assertFalse( tagPath.contains(s5));
        assertFalse( tagPath.contains(s6));
        assertFalse( tagPath.contains(s7));
        assertFalse( tagPath.contains(sw1));
        assertFalse( tagPath.contains(sw2));
        assertFalse( tagPath.contains(sw3));

        tagPath = new TagPath().addTag(new TagPrivate("2005","pc1","14XX"));
        assertFalse( tagPath.contains(t1));
        assertFalse( tagPath.contains(t2));
        assertTrue( tagPath.contains(p1));
        assertTrue( tagPath.contains(p2));
        assertFalse( tagPath.contains(p3));
        assertFalse( tagPath.contains(s1));
        assertFalse( tagPath.contains(s2));
        assertFalse( tagPath.contains(s3));
        assertFalse( tagPath.contains(s4));
        assertFalse( tagPath.contains(s5));
        assertFalse( tagPath.contains(s6));
        assertFalse( tagPath.contains(s7));
        assertFalse( tagPath.contains(sw1));
        assertFalse( tagPath.contains(sw2));
        assertFalse( tagPath.contains(sw3));

    }

}
