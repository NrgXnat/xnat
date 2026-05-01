/*
 * DicomEdit: TestFunctions
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Run test deleting invalid private tags.
 *
 */
public class TestInvalidDuplicatedPrivateTagRemoval {

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    /**
     * Same group with two blocks with the same creator ID.
     * Does not remove orphan creator IDs.
     *
     * <pre>
     *     -(0029,{NAUGHTY PERSON}XX)
     *     -(0049,{NAUGHTY PERSON}31)
     *
     * (0029,0010) LO #26 [REASONABLE PERSON HEADER1] PrivateCreatorID      retain
     * (0029,0011) LO #26 [REASONABLE PERSON HEADER2] PrivateCreatorID      retain
     * (0029,0012) LO #14 [NAUGHTY PERSON] PrivateCreatorID                 retain
     * (0029,00E1) LO #14 [NAUGHTY PERSON] PrivateCreatorID                 retain
     * (0029,1008) ST #4 [MLO]                                              retain
     * (0029,1009) CS #4 [YES]                                              retain
     * (0029,11AA) LT #4 [meow]                                             retain
     * (0029,1231) ST #4 [uhhh]                                             delete
     * (0029,E131) ST #4 [uhhh]                                             delete
     * (0049,0010) LO #26 [REASONABLE PERSON HEADER1] PrivateCreatorID      retain
     * (0049,0011) LO #26 [REASONABLE PERSON HEADER2] PrivateCreatorID      retain
     * (0049,0012) LO #14 [NAUGHTY PERSON] PrivateCreatorID                 delete
     * (0049,00E1) LO #14 [NAUGHTY PERSON] PrivateCreatorID                 delete
     * (0049,1008) ST #4 [MLO]                                              retain
     * (0049,1009) CS #4 [YES]                                              retain
     * (0049,11AA) LT #4 [meow]                                             retain
     * (0049,1231) ST #4 [uhhh]                                             delete
     * (0049,E131) ST #4 [uhhh]                                             delete
     * </pre>
     */
    @Test
    public void testSimple() throws MizerException {
        TestUtils.TestTag p1   = new TestUtils.TestTag(0x00290010, "REASONABLE PERSON HEADER1");
        TestUtils.TestTag p2   = new TestUtils.TestTag(0x00290011, "REASONABLE PERSON HEADER2");
        TestUtils.TestTag p3   = new TestUtils.TestTag(0x00290012, "NAUGHTY PERSON");
        TestUtils.TestTag p4   = new TestUtils.TestTag(0x002900E1, "NAUGHTY PERSON");
        TestUtils.TestTag p1_1 = new TestUtils.TestTag(0x00291008, "MLO");
        TestUtils.TestTag p1_2 = new TestUtils.TestTag(0x00291009, "YES");
        TestUtils.TestTag p2_1 = new TestUtils.TestTag(0x002911AA, "meow");
        TestUtils.TestTag p3_1 = new TestUtils.TestTag(0x00291231, "uhhh");
        TestUtils.TestTag p4_1 = new TestUtils.TestTag(0x0029E131, "uhhh");
        TestUtils.TestTag q1   = new TestUtils.TestTag(0x00490010, "REASONABLE PERSON HEADER1");
        TestUtils.TestTag q2   = new TestUtils.TestTag(0x00490011, "REASONABLE PERSON HEADER2");
        TestUtils.TestTag q3   = new TestUtils.TestTag(0x00490012, "NAUGHTY PERSON");
        TestUtils.TestTag q4   = new TestUtils.TestTag(0x004900E1, "NAUGHTY PERSON");
        TestUtils.TestTag q1_1 = new TestUtils.TestTag(0x00491008, "MLO");
        TestUtils.TestTag q1_2 = new TestUtils.TestTag(0x00491009, "YES");
        TestUtils.TestTag q2_1 = new TestUtils.TestTag(0x004911AA, "meow");
        TestUtils.TestTag q3_1 = new TestUtils.TestTag(0x00491231, "uhhh");
        TestUtils.TestTag q4_1 = new TestUtils.TestTag(0x0049E131, "uhhh");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( p1.tag, p1.initialValue);
        dobj.putString( p2.tag, p2.initialValue);
        dobj.putString( p3.tag, p3.initialValue);
        dobj.putString( p4.tag, p4.initialValue);
        dobj.putString( p1_1.tag, p1_1.initialValue);
        dobj.putString( p1_2.tag, p1_2.initialValue);
        dobj.putString( p2_1.tag, p2_1.initialValue);
        dobj.putString( p3_1.tag, p3_1.initialValue);
        dobj.putString( p4_1.tag, p4_1.initialValue);
        dobj.putString( q1.tag, q1.initialValue);
        dobj.putString( q2.tag, q2.initialValue);
        dobj.putString( q3.tag, q3.initialValue);
        dobj.putString( q4.tag, q4.initialValue);
        dobj.putString( q1_1.tag, q1_1.initialValue);
        dobj.putString( q1_2.tag, q1_2.initialValue);
        dobj.putString( q2_1.tag, q2_1.initialValue);
        dobj.putString( q3_1.tag, q3_1.initialValue);
        dobj.putString( q4_1.tag, q4_1.initialValue);

        assertTrue( dobj.contains( p1.tag));
        assertTrue( dobj.contains( p2.tag));
        assertTrue( dobj.contains( p3.tag));
        assertTrue( dobj.contains( p4.tag));
        assertTrue( dobj.contains( p1_1.tag));
        assertTrue( dobj.contains( p1_2.tag));
        assertTrue( dobj.contains( p2_1.tag));
        assertTrue( dobj.contains( p3_1.tag));
        assertTrue( dobj.contains( p4_1.tag));
        assertTrue( dobj.contains( q1.tag));
        assertTrue( dobj.contains( q2.tag));
        assertTrue( dobj.contains( q3.tag));
        assertTrue( dobj.contains( q4.tag));
        assertTrue( dobj.contains( q1_1.tag));
        assertTrue( dobj.contains( q1_2.tag));
        assertTrue( dobj.contains( q2_1.tag));
        assertTrue( dobj.contains( q3_1.tag));
        assertTrue( dobj.contains( q4_1.tag));

        String script = "-(0029,{NAUGHTY PERSON}XX)\n" +
                "-(0049,{NAUGHTY PERSON}31)";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply( dobj).getDicomObject();

        assertTrue( dobj.contains( p1.tag));
        assertTrue( dobj.contains( p2.tag));
        assertTrue( dobj.contains( p3.tag));
        assertTrue( dobj.contains( p4.tag));
        assertTrue( dobj.contains( p1_1.tag));
        assertTrue( dobj.contains( p1_2.tag));
        assertTrue( dobj.contains( p2_1.tag));
        assertFalse( dobj.contains( p3_1.tag));
        assertFalse( dobj.contains( p4_1.tag));
        assertTrue( dobj.contains( q1.tag));
        assertTrue( dobj.contains( q2.tag));
        assertTrue( dobj.contains( q3.tag));
        assertTrue( dobj.contains( q4.tag));
        assertTrue( dobj.contains( q1_1.tag));
        assertTrue( dobj.contains( q1_2.tag));
        assertTrue( dobj.contains( q2_1.tag));
        assertFalse( dobj.contains( q3_1.tag));
        assertFalse( dobj.contains( q4_1.tag));
    }

}
