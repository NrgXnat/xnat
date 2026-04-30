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
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.test.workers.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.Assert.*;

public class TestMixedPrivateStandardSequence {

    private static final Logger logger = LoggerFactory.getLogger(TestMixedPrivateStandardSequence.class);
    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File mellon = _resourceManager.getTestResourceFile("dicom/mellon.dcm");

    @Test
    public void test() {
        try {
            String script = "-(2001,{Philips Imaging DD 001}5F)[%]/(2001,{Philips Imaging DD 001}32)\n" +
                    "-(2001,{Philips Imaging DD 001}5F)[%]/(2005,{Philips MR Imaging DD 005}3e)\n" +
                    "-(5200,9230)[1]/(2005,{Philips MR Imaging DD 005}XX)\n";

            DicomObjectI dobj = DicomObjectFactory.newInstance(mellon);

            TestUtils.TestTag    p1 = new TestUtils.TestTag( 0x20010010, "Philips Imaging DD 001");
            TestUtils.TestSeqTag p2 = new TestUtils.TestSeqTag(new int[]{0x2001105f, 0, 0x20010010},  "Philips Imaging DD 001");
            TestUtils.TestSeqTag p3 = new TestUtils.TestSeqTag(new int[]{0x2001105f, 0, 0x20011032},  "0");
            TestUtils.TestSeqTag p4 = new TestUtils.TestSeqTag(new int[]{0x2001105f, 0, 0x20050014},  "Philips MR Imaging DD 005");
            TestUtils.TestSeqTag p5 = new TestUtils.TestSeqTag(new int[]{0x2001105f, 0, 0x2005143e},  "1.7E38");
            TestUtils.TestSeqTag s0 = new TestUtils.TestSeqTag(new int[]{0x52009230, 0, 0x20050014},  "Philips MR Imaging DD 005");
            TestUtils.TestSeqTag s0_1 = new TestUtils.TestSeqTag(new int[]{0x52009230, 0, 0x2005140f},  "");
            TestUtils.TestSeqTag s0_2 = new TestUtils.TestSeqTag(new int[]{0x52009230, 0, 0x2005140f, 0, 0x00080008},  "ORIGINAL\\PRIMARY\\M_SE\\M\\SE");
            TestUtils.TestSeqTag s1 = new TestUtils.TestSeqTag(new int[]{0x52009230, 1, 0x20050014},  "Philips MR Imaging DD 005");
            TestUtils.TestSeqTag s1_1 = new TestUtils.TestSeqTag(new int[]{0x52009230, 1, 0x2005140f},  "");
            TestUtils.TestSeqTag s1_2 = new TestUtils.TestSeqTag(new int[]{0x52009230, 1, 0x2005140f, 0, 0x00080008},  "ORIGINAL\\PRIMARY\\M_SE\\M\\SE");
            TestUtils.TestSeqTag s2 = new TestUtils.TestSeqTag(new int[]{0x52009230, 2, 0x20050014},  "Philips MR Imaging DD 005");
            TestUtils.TestSeqTag s2_1 = new TestUtils.TestSeqTag(new int[]{0x52009230, 2, 0x2005140f},  "");
            TestUtils.TestSeqTag s2_2 = new TestUtils.TestSeqTag(new int[]{0x52009230, 2, 0x2005140f, 0, 0x00080008},  "ORIGINAL\\PRIMARY\\M_SE\\M\\SE");

            assertEquals( p1.initialValue, dobj.getString( p1.tag));
            assertEquals( p2.initialValue, dobj.getString( p2.tag));
            assertEquals( p3.initialValue, dobj.getString( p3.tag));
            assertEquals( p4.initialValue, dobj.getString( p4.tag));
            assertEquals( p5.initialValue, dobj.getString( p5.tag));
            assertEquals( s0.initialValue, dobj.getString( s0.tag));
            assertTrue( dobj.contains( s0_1.tag));
            assertEquals( s0_2.initialValue, dobj.getString( s0_2.tag));
            assertEquals( s1.initialValue, dobj.getString( s1.tag));
            assertTrue( dobj.contains( s1_1.tag));
            assertEquals( s1_2.initialValue, dobj.getString( s0_2.tag));
            assertEquals( s2.initialValue, dobj.getString( s2.tag));
            assertTrue( dobj.contains( s2_1.tag));
            assertEquals( s2_2.initialValue, dobj.getString( s0_2.tag));

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
            logger.info( dobj.toCompleteString());
            dobj = sa.apply( dobj).getDicomObject();
            logger.info( dobj.toCompleteString());

            assertEquals( p1.initialValue, dobj.getString( p1.tag));
            assertEquals( p2.initialValue, dobj.getString( p2.tag));
            assertFalse( dobj.contains( p3.tag));
            assertEquals( p4.initialValue, dobj.getString( p4.tag));
            assertFalse( dobj.contains( p5.tag));
            assertEquals( s0.initialValue, dobj.getString( s0.tag));
            assertTrue( dobj.contains( s0_1.tag));
            assertTrue( dobj.contains( s0_2.tag));
            assertEquals( s1.initialValue, dobj.getString( s1.tag));
            assertFalse( dobj.contains( s1_1.tag));
            assertFalse( dobj.contains( s1_2.tag));
            assertEquals( s2.initialValue, dobj.getString( s2.tag));
            assertTrue( dobj.contains( s2_1.tag));
            assertTrue( dobj.contains( s2_2.tag));

        }
        catch( Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * <pre>
     *      -(5200,9230)[1]/(2005,{Philips MR Imaging DD 005}XX)
     *
     * (5200,9230) SQ #-1 [3 items] Per-frame Functional Groups Sequence                retain
     *
     * >ITEM #1:                                                                        retain
     * >(2005,0014) LO #26 [Philips MR Imaging DD 005] Private Creator Data Element     retain
     * >(2005,140F) SQ #-1 [1 item] ?                                                   retain
     * >>ITEM #1:                                                                       retain
     * >>(0008,0008) LO #26 [ORIGINAL\PRIMARY\M_SE\M\SE] Image Type                     delete
     *
     * >ITEM #2:                                                                        retain
     * >(2005,0014) LO #26 [Philips MR Imaging DD 005] Private Creator Data Element     retain
     * >(2005,140F) SQ #-1 [1 item] ?                                                   delete
     * >>ITEM #1:                                                                       delete
     * >>(0008,0008) LO #26 [ORIGINAL\PRIMARY\M_SE\M\SE] Image Type                     delete
     *
     * >ITEM #3:                                                                        retain
     * >(2005,0014) LO #26 [Philips MR Imaging DD 005] Private Creator Data Element     retain
     * >(2005,140F) SQ #-1 [1 item] ?                                                   retain
     * >>ITEM #1:                                                                       retain
     * >>(0008,0008) LO #26 [ORIGINAL\PRIMARY\M_SE\M\SE] Image Type                     retain
     * </pre>
     */
    @Test
    public void test2() {
        try {
            DicomObjectI dobj = DicomObjectFactory.newInstance();

            TestUtils.TestSeqTag s0 = new TestUtils.TestSeqTag(new int[]{0x52009230, 0, 0x20050014},  "Philips MR Imaging DD 005");
            TestUtils.TestSeqTag s0_2 = new TestUtils.TestSeqTag(new int[]{0x52009230, 0, 0x2005140f, 0, 0x00080008},  "ORIGINAL\\PRIMARY\\M_SE\\M\\SE");
            TestUtils.TestSeqTag s1 = new TestUtils.TestSeqTag(new int[]{0x52009230, 1, 0x20050014},  "Philips MR Imaging DD 005");
            TestUtils.TestSeqTag s1_2 = new TestUtils.TestSeqTag(new int[]{0x52009230, 1, 0x2005140f, 0, 0x00080008},  "ORIGINAL\\PRIMARY\\M_SE\\M\\SE");
            TestUtils.TestSeqTag s2 = new TestUtils.TestSeqTag(new int[]{0x52009230, 2, 0x20050014},  "Philips MR Imaging DD 005");
            TestUtils.TestSeqTag s2_2 = new TestUtils.TestSeqTag(new int[]{0x52009230, 2, 0x2005140f, 0, 0x00080008},  "ORIGINAL\\PRIMARY\\M_SE\\M\\SE");

            dobj.putString( s0.tag, s0.initialValue);
            dobj.putString( s0_2.tag, s0_2.initialValue);
            dobj.putString( s1.tag, s1.initialValue);
            dobj.putString( s1_2.tag, s1_2.initialValue);
            dobj.putString( s2.tag, s2.initialValue);
            dobj.putString( s2_2.tag, s2_2.initialValue);

            assertEquals( 3, dobj.getElement( 0x52009230).countItems());
            assertEquals( s0.initialValue, dobj.getString( s0.tag));
            assertEquals( s0_2.initialValue, dobj.getString( s0_2.tag));
            assertEquals( s1.initialValue, dobj.getString( s1.tag));
            assertEquals( s1_2.initialValue, dobj.getString( s0_2.tag));
            assertEquals( s2.initialValue, dobj.getString( s2.tag));
            assertEquals( s2_2.initialValue, dobj.getString( s2_2.tag));

            String script = "-(2001,{Philips Imaging DD 001}5F)[%]/(2001,{Philips Imaging DD 001}32)\n" +
                    "-(2001,{Philips Imaging DD 001}5F)[%]/(2005,{Philips MR Imaging DD 005}3e)\n" +
                    "-(5200,9230)[1]/(2005,{Philips MR Imaging DD 005}XX)\n";

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
            logger.info( dobj.toCompleteString());
            dobj = sa.apply( dobj).getDicomObject();
            logger.info( dobj.toCompleteString());

            assertEquals( 3, dobj.getElement( 0x52009230).countItems());
            assertEquals( s0.initialValue, dobj.getString( s0.tag));
            assertTrue( dobj.contains( s0_2.tag));
            assertTrue( dobj.contains( s1.tag));
            assertFalse( dobj.contains( s1_2.tag));
            assertEquals( s2.initialValue, dobj.getString( s2.tag));
            assertTrue( dobj.contains( s2_2.tag));

        }
        catch( Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

}
