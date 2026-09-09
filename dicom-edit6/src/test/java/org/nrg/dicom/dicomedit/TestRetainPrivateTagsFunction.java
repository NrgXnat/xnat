/*
 * DicomEdit: TestRetainPrivateTagsFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.*;

import static org.junit.Assert.*;
import static org.nrg.dicom.dicomedit.TestUtils.*;

/**
 * Run tests of retainPrivateTags function.
 *
 */
public class TestRetainPrivateTagsFunction {

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File de11 = _resourceManager.getTestResourceFile("dicom/IM_0001");
    private static final File FILE4 = _resourceManager.getTestResourceFile("dicom/1.2.840.113717.2.21733635.1-3-2-7b04bw.dcm");
    private static final File FILE0 = _resourceManager.getTestResourceFile("dicom/0.dcm");

    /**
     * 1. retain-tagPath arguments can be specified as a string.
     * 2. retain all tags in a specific private block in root DICOM object.
     *
     * <pre>
     * (0009,0012) LO #2 [p1] Private Creator Data Element    delete
     * (0009,1200) LO #4 [p1_0] ?                             delete
     * (0009,1201) LO #4 [p1_1] ?                             delete
     * (0029,0011) LO #4 [sm1] Private Creator Data Element   retain
     * (0029,1111) LO #6 [sm1_0] ?                            retain
     * (0029,11F1) LO #6 [sm1_1] ?                            retain
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testSimpleString() throws MizerException {
        TestTag p1 = new TestTag(0x00090010, "p1");
        TestTag p1_0 = new TestTag(0x00091000, "p1_0");
        TestTag p1_1 = new TestTag(0x00091001, "p1_1");
        TestTag sm1 = new TestTag(0x00290011, "sm1");
        TestTag sm1_0 = new TestTag(0x00291111, "sm1_0");
        TestTag sm1_1 = new TestTag(0x002911F1, "sm1_1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( p1.tag, p1.initialValue);
        dobj.putString( p1_0.tag, p1_0.initialValue);
        dobj.putString( p1_1.tag, p1_1.initialValue);
        dobj.putString( sm1.tag, sm1.initialValue);
        dobj.putString( sm1_0.tag, sm1_0.initialValue);
        dobj.putString( sm1_1.tag, sm1_1.initialValue);

        assertTrue( dobj.contains( p1.tag));
        assertTrue( dobj.contains( p1_0.tag));
        assertTrue( dobj.contains( p1_1.tag));
        assertTrue( dobj.contains( sm1.tag));
        assertTrue( dobj.contains( sm1_0.tag));
        assertTrue( dobj.contains( sm1_1.tag));

        String script = "retainPrivateTags[ \"(0029,{sm1}XX)\"]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply( dobj).getDicomObject();

        assertFalse( dobj.contains( p1.tag));
        assertFalse( dobj.contains( p1_0.tag));
        assertFalse( dobj.contains( p1_1.tag));
        assertTrue( dobj.contains( sm1.tag));
        assertTrue( dobj.contains( sm1_0.tag));
        assertTrue( dobj.contains( sm1_1.tag));
    }

    /**
     * Retain a block in a private group >= 0x8000, where the gggg+eeee composite exceeds Integer.MAX_VALUE.
     *
     * <pre>
     * (0009,0010) LO #2 [p1] Private Creator Data Element    delete
     * (0009,1000) LO #4 [p1_0] ?                             delete
     * (F215,0010) LO #4 [hi1] Private Creator Data Element   retain
     * (F215,1050) LO #6 [hi1_0] ?                            retain
     * (F215,1051) LO #6 [hi1_1] ?                            retain
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testHighPrivateGroup() throws MizerException {
        TestTag p1 = new TestTag(0x00090010, "p1");
        TestTag p1_0 = new TestTag(0x00091000, "p1_0");
        TestTag hi1 = new TestTag(0xF2150010, "hi1");
        TestTag hi1_0 = new TestTag(0xF2151050, "hi1_0");
        TestTag hi1_1 = new TestTag(0xF2151051, "hi1_1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString(p1.tag, p1.initialValue);
        dobj.putString(p1_0.tag, p1_0.initialValue);
        dobj.putString(hi1.tag, hi1.initialValue);
        dobj.putString(hi1_0.tag, hi1_0.initialValue);
        dobj.putString(hi1_1.tag, hi1_1.initialValue);

        String script = "retainPrivateTags[ (F215,{hi1}XX)]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertFalse(dobj.contains(p1.tag));
        assertFalse(dobj.contains(p1_0.tag));
        assertTrue(dobj.contains(hi1.tag));
        assertTrue(dobj.contains(hi1_0.tag));
        assertTrue(dobj.contains(hi1_1.tag));
    }

    /**
     * A private creator declaration in a group >= 0x8000 with no data elements of its own must not break the
     * sweep. This is the state a creator-aware delete of the block's data elements leaves behind.
     *
     * <pre>
     * (F215,0010) LO #4 [hi1] Private Creator Data Element   delete
     * (0029,0011) LO #4 [sm1] Private Creator Data Element   retain
     * (0029,1111) LO #6 [sm1_0] ?                            retain
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testHighPrivateGroupOrphanedCreatorId() throws MizerException {
        TestTag hi1 = new TestTag(0xF2150010, "hi1");
        TestTag sm1 = new TestTag(0x00290011, "sm1");
        TestTag sm1_0 = new TestTag(0x00291111, "sm1_0");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString(hi1.tag, hi1.initialValue);
        dobj.putString(sm1.tag, sm1.initialValue);
        dobj.putString(sm1_0.tag, sm1_0.initialValue);

        String script = "retainPrivateTags[ (0029,{sm1}XX)]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertFalse(dobj.contains(hi1.tag));
        assertTrue(dobj.contains(sm1.tag));
        assertTrue(dobj.contains(sm1_0.tag));
    }

    /**
     * 1. retain-tagPath arguments can be specified as a tagPath.
     * 2. retain all tags in a specific private block in root DICOM object.
     *
     * <pre>
     * (0009,0012) LO #2 [p1] Private Creator Data Element    delete
     * (0009,1200) LO #4 [p1_0] ?                             delete
     * (0009,1201) LO #4 [p1_1] ?                             delete
     * (0029,0011) LO #4 [sm1] Private Creator Data Element   retain
     * (0029,1111) LO #6 [sm1_0] ?                            retain
     * (0029,11F1) LO #6 [sm1_1] ?                            retain
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testSimpleTagPath() throws MizerException {
        TestTag p1 = new TestTag(0x00090010, "p1");
        TestTag p1_0 = new TestTag(0x00091000, "p1_0");
        TestTag p1_1 = new TestTag(0x00091001, "p1_1");
        TestTag sm1 = new TestTag(0x00290011, "sm1");
        TestTag sm1_0 = new TestTag(0x00291111, "sm1_0");
        TestTag sm1_1 = new TestTag(0x002911F1, "sm1_1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( p1.tag, p1.initialValue);
        dobj.putString( p1_0.tag, p1_0.initialValue);
        dobj.putString( p1_1.tag, p1_1.initialValue);
        dobj.putString( sm1.tag, sm1.initialValue);
        dobj.putString( sm1_0.tag, sm1_0.initialValue);
        dobj.putString( sm1_1.tag, sm1_1.initialValue);

        assertTrue( dobj.contains( p1.tag));
        assertTrue( dobj.contains( p1_0.tag));
        assertTrue( dobj.contains( p1_1.tag));
        assertTrue( dobj.contains( sm1.tag));
        assertTrue( dobj.contains( sm1_0.tag));
        assertTrue( dobj.contains( sm1_1.tag));

        String script = "retainPrivateTags[ (0029,{sm1}XX)]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply( dobj).getDicomObject();

        assertFalse( dobj.contains( p1.tag));
        assertFalse( dobj.contains( p1_0.tag));
        assertFalse( dobj.contains( p1_1.tag));
        assertTrue( dobj.contains( sm1.tag));
        assertTrue( dobj.contains( sm1_0.tag));
        assertTrue( dobj.contains( sm1_1.tag));
    }

    /**
     * An argument that is not a tagPath is an error.
     *
     * @throws MizerException
     */
    @Test
    public void nonTagPathArgumentsIsAnError() throws MizerException {
        TestTag p1 = new TestTag(0x00090010, "p1");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putString( p1.tag, p1.initialValue);

        assertTrue( dobj.contains( p1.tag));

        String script = "retainPrivateTags[ (0029,{sm1}XX), \"fubar\", 1]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue( result instanceof AnonymizationResultError);
    }

    /**
     * A tagPath argument that is not private is an error.
     *
     * @throws MizerException
     */
    @Test
    public void publicTagPathArgumentsIsAnError() throws MizerException {
        TestTag p1 = new TestTag(0x00090010, "p1");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putString( p1.tag, p1.initialValue);

        assertTrue( dobj.contains( p1.tag));

        String script = "retainPrivateTags[ (0028,0010)]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue( result instanceof AnonymizationResultError);
    }

    /**
     * Error to address a private tag directly. The creator-id syntax must be used.
     */
    @Test(expected = MizerException.class)
    public void testPvtTagSyntax() throws MizerException {
        TestTag pb1 =   new TestTag(0x00190010, "pb1");
        TestTag pb1_1 = new TestTag(0x001910F0, "pb1_1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( pb1.tag, pb1.initialValue);
        dobj.putString( pb1_1.tag, pb1_1.initialValue);

        assertTrue( dobj.contains( pb1.tag));
        assertTrue( dobj.contains( pb1_1.tag));

        String script = "retainPrivateTags[ (0019,10F0)]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        sa.apply(dobj);
    }

    /**
     * 1. Private tag in public sequence are properly removed
     * 2. Private tag in public sequence are properly retained.
     * 3. Sequence items have private creator IDs.
     *
     *  retainPrivateTags[ (0042,0010)/(0011,{p1c}3F)]
     * <pre>
     * (0010,0010) PN #2 [t1] Patient’s Name                   retain
     * (0042,0010) SQ #-1 [2 items] Document Title             retain
     * >ITEM #1:
     * >(0010,0010) PN #8 [t2_0_t1] Patient’s Name             retain
     * >(0011,0010) LO #4 [p1c] Private Creator Data Element   retain
     * >(0011,1033) UN #10 [t2_0_p1t1] ?                       delete
     * >(0011,103F) UN #10 [t2_0_p1t2] ?                       retain
     * >ITEM #2:
     * >(0010,0010) PN #8 [t2_1_t1] Patient’s Name             retain
     * >(0011,0010) LO #4 [p1c] Private Creator Data Element   retain
     * >(0011,1033) UN #10 [t2_1_p1t1] ?                       delete
     * >(0011,103F) UN #10 [t2_1_p1t2] ?                       retain
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testPvtTagsInPublicSeq() throws MizerException {
        String script = "retainPrivateTags[ (0042,0010)/(0011,{p1c}3F)]";

        TestTag t1           = new TestTag(0x00100010, "t1");
        TestTag t2           = new TestTag(0x00420010, "t2");
        TestSeqTag t2_0_t1   = new TestSeqTag(new int[]{0x00420010,0,0x00100010}, "t2_0_t1");
        TestSeqTag t2_0_p1c  = new TestSeqTag(new int[]{0x00420010,0,0x00110010}, "p1c");
        TestSeqTag t2_0_p1t1 = new TestSeqTag(new int[]{0x00420010,0,0x00111033}, "t2_0_p1t1");
        TestSeqTag t2_0_p1t2 = new TestSeqTag(new int[]{0x00420010,0,0x0011103F}, "t2_0_p1t2");
        TestSeqTag t2_1_t1   = new TestSeqTag(new int[]{0x00420010,1,0x00100010}, "t2_1_t1");
        TestSeqTag t2_1_p1c  = new TestSeqTag(new int[]{0x00420010,1,0x00110010}, "p1c");
        TestSeqTag t2_1_p1t1 = new TestSeqTag(new int[]{0x00420010,1,0x00111033}, "t2_1_p1t1");
        TestSeqTag t2_1_p1t2 = new TestSeqTag(new int[]{0x00420010,1,0x0011103F}, "t2_1_p1t2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, t2);
        put(dobj, t2_0_t1);
        put(dobj, t2_0_p1c);
        put(dobj, t2_0_p1t1);
        put(dobj, t2_0_p1t2);
        put(dobj, t2_1_t1);
        put(dobj, t2_1_p1c);
        put(dobj, t2_1_p1t1);
        put(dobj, t2_1_p1t2);

        assertTrue( dobj.contains( t1.tag));
        assertTrue( dobj.contains( t2.tag));
        assertTrue( dobj.contains( t2_0_t1.tag));
        assertTrue( dobj.contains( t2_0_p1c.tag));
        assertTrue( dobj.contains( t2_0_p1t1.tag));
        assertTrue( dobj.contains( t2_0_p1t2.tag));
        assertTrue( dobj.contains( t2_1_t1.tag));
        assertTrue( dobj.contains( t2_1_p1c.tag));
        assertTrue( dobj.contains( t2_1_p1t1.tag));
        assertTrue( dobj.contains( t2_1_p1t2.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( dobj.contains( t1.tag));
        assertTrue( dobj.contains( t2.tag));
        assertTrue( dobj.contains( t2_0_t1.tag));
        assertTrue( dobj.contains( t2_0_p1c.tag));
        assertFalse( dobj.contains( t2_0_p1t1.tag));
        assertTrue( dobj.contains( t2_0_p1t2.tag));
        assertTrue( dobj.contains( t2_1_t1.tag));
        assertTrue( dobj.contains( t2_1_p1c.tag));
        assertFalse( dobj.contains( t2_1_p1t1.tag));
        assertTrue( dobj.contains( t2_1_p1t2.tag));
    }

    /**
     * 1. Private tags in public sequence are completely removed
     * 2. Sequence items have private creator IDs.
     *
     *  retainPrivateTags[ (0042,0010)/(0011,{FICTION}3F)]
     * <pre>
     * (0010,0010) PN #2 [t1] Patient’s Name                   retain
     * (0042,0010) SQ #-1 [2 items] Document Title             retain
     * >ITEM #1:
     * >(0010,0010) PN #8 [t2_0_t1] Patient’s Name             retain
     * >(0011,0010) LO #4 [p1c] Private Creator Data Element   delete
     * >(0011,1033) UN #10 [t2_0_p1t1] ?                       delete
     * >(0011,103F) UN #10 [t2_0_p1t2] ?                       delete
     * >ITEM #2:
     * >(0010,0010) PN #8 [t2_1_t1] Patient’s Name             retain
     * >(0011,0010) LO #4 [p1c] Private Creator Data Element   delete
     * >(0011,1033) UN #10 [t2_1_p1t1] ?                       delete
     * >(0011,103F) UN #10 [t2_1_p1t2] ?                       delete
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testRemovePvtTagsInPublicSeq() throws MizerException {
        String script = "retainPrivateTags[ (0042,0010)/(0011,{FICTION}3F)]";

        TestTag t1           = new TestTag(0x00100010, "t1");
        TestTag t2           = new TestTag(0x00420010, "t2");
        TestSeqTag t2_0_t1   = new TestSeqTag(new int[]{0x00420010,0,0x00100010}, "t2_0_t1");
        TestSeqTag t2_0_p1c  = new TestSeqTag(new int[]{0x00420010,0,0x00110010}, "p1c");
        TestSeqTag t2_0_p1t1 = new TestSeqTag(new int[]{0x00420010,0,0x00111033}, "t2_0_p1t1");
        TestSeqTag t2_0_p1t2 = new TestSeqTag(new int[]{0x00420010,0,0x0011103F}, "t2_0_p1t2");
        TestSeqTag t2_1_t1   = new TestSeqTag(new int[]{0x00420010,1,0x00100010}, "t2_1_t1");
        TestSeqTag t2_1_p1c  = new TestSeqTag(new int[]{0x00420010,1,0x00110010}, "p1c");
        TestSeqTag t2_1_p1t1 = new TestSeqTag(new int[]{0x00420010,1,0x00111033}, "t2_1_p1t1");
        TestSeqTag t2_1_p1t2 = new TestSeqTag(new int[]{0x00420010,1,0x0011103F}, "t2_1_p1t2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, t1);
        put(dobj, t2);
        put(dobj, t2_0_t1);
        put(dobj, t2_0_p1c);
        put(dobj, t2_0_p1t1);
        put(dobj, t2_0_p1t2);
        put(dobj, t2_1_t1);
        put(dobj, t2_1_p1c);
        put(dobj, t2_1_p1t1);
        put(dobj, t2_1_p1t2);

        assertTrue( dobj.contains( t1.tag));
        assertTrue( dobj.contains( t2.tag));
        assertTrue( dobj.contains( t2_0_t1.tag));
        assertTrue( dobj.contains( t2_0_p1c.tag));
        assertTrue( dobj.contains( t2_0_p1t1.tag));
        assertTrue( dobj.contains( t2_0_p1t2.tag));
        assertTrue( dobj.contains( t2_1_t1.tag));
        assertTrue( dobj.contains( t2_1_p1c.tag));
        assertTrue( dobj.contains( t2_1_p1t1.tag));
        assertTrue( dobj.contains( t2_1_p1t2.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( dobj.contains( t1.tag));
        assertTrue( dobj.contains( t2.tag));
        assertTrue( dobj.contains( t2_0_t1.tag));
        assertFalse( dobj.contains( t2_0_p1c.tag));
        assertFalse( dobj.contains( t2_0_p1t1.tag));
        assertFalse( dobj.contains( t2_0_p1t2.tag));
        assertTrue( dobj.contains( t2_1_t1.tag));
        assertFalse( dobj.contains( t2_1_p1c.tag));
        assertFalse( dobj.contains( t2_1_p1t1.tag));
        assertFalse( dobj.contains( t2_1_p1t2.tag));
    }

    /**
     * Completely remove nested private sequences.
     *
     * retainPrivateTags[ (0029,{FICTION}XX)]
     * <pre>
     * (0009,0012) LO #4 [pb1] Private Creator Data Element       delete
     * (0009,1200) SQ #-1 [1 item] ?                              delete
     *   >ITEM #1:
     *   >(0023,0020) LO #4 [pb2] Private Creator Data Element    delete
     *   >(0023,2015) SQ #-1 [1 item] ?                           delete
     *     >>ITEM #1:
     *     >>(0023,0020) LO #4 [pb3] Private Creator Data Element delete
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testCompletelyRemoveEmptyPvtSeqs() throws MizerException {
        String script = "retainPrivateTags[ (0029,{FICTION}XX)]";
        TestTag pb1 = new TestTag(0x00090012, "pb1");
        TestTag pb1_1 = new TestTag(0x00091200, "pb1_1");
        TestSeqTag pb2 = new TestSeqTag( new int[] {0x00091200,0,0x00230020}, "pb2");
        TestSeqTag pb2_1 = new TestSeqTag( new int[] {0x00091200,0,0x00232015}, "pb2_1");
        TestSeqTag pb3 = new TestSeqTag( new int[] {0x00091200,0,0x00232015,0,0x00230020}, "pb3");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( pb1.tag, pb1.initialValue);
        dobj.putString( pb1_1.tag, pb1_1.initialValue);
        dobj.putString( pb2.tag, pb2.initialValue);
        dobj.putString( pb2_1.tag, pb2_1.initialValue);
        dobj.putString( pb3.tag, pb3.initialValue);

        assertTrue( dobj.contains( pb1.tag));
        assertTrue( dobj.contains( pb1_1.tag));
        assertTrue( dobj.contains( pb2.tag));
        assertTrue( dobj.contains( pb2_1.tag));
        assertTrue( dobj.contains( pb3.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertFalse( dobj.contains( pb1.tag));
        assertFalse( dobj.contains( pb1_1.tag));
        assertFalse( dobj.contains( pb2.tag));
        assertFalse( dobj.contains( pb2_1.tag));
        assertFalse( dobj.contains( pb3.tag));
    }

    /**
     * 1. complete removal of private tags in mixed sequences
     * 2. private creator IDs are present.
     *
     * retainPrivateTags[ (0029,{FICTION}XX)]
     * <pre>
     * (0010,0010) PN #2 [t1] Patient’s Name                   retain
     * (0019,0010) LO #4 [p1c] Private Creator Data Element    delete
     * (0019,1001) UN #4 [p1t1] ?                              delete
     * (0019,1002) SQ #-1 [1 item] ?                           delete
     * >ITEM #1:
     * >(0023,0020) LO #2 [p2] Private Creator Data Element    delete
     * >(0023,2001) UN #12 [p1s1_0_p2p1] ?                     delete
     * >(0023,2015) SQ #-1 [1 item] ?                          delete
     * >>ITEM #1:
     * >>(0023,0010) LO #18 [p1s1_0_p2p2_0_p3c] Private Creator Data Element  delete
     * >>(0023,10FF) UN #18 [p1s1_0_p2p2_0_p3p1] ?             delete
     * (0040,1200) SQ #-1 [1 item] ?                           retain
     * >ITEM #1:
     * >(0018,0021) CS #8 [t2_0_t1] Sequence Variant           retain
     * >(0018,0022) CS #8 [t2_0_t2] Scan Options               retain
     * >(0029,0010) LO #2 [p4] Private Creator Data Element    delete
     * >(0029,1000) SQ #-1 [1 item] ?                          delete
     * >>ITEM #1:
     * >>(0018,0010) LO #8 [pb4_1_t1] Contrast/Bolus Agent     delete
     * </pre>
     *
    * @throws MizerException unexpected exception
     */
    @Test
    public void testComplteRemovalInMixedSeqs() throws MizerException {
        String script = "retainPrivateTags[ (0029,{FICTION}XX)]";
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag p1c = new TestTag(0x00190010, "p1c");
        TestTag p1t1 = new TestTag(0x00191001, "p1t1");
        TestTag p1s1 = new TestTag(0x00191002, "p1s1");
        TestSeqTag p1s1_0_p2c = new TestSeqTag( new int[] {0x00191002,0,0x00230020}, "p2");
        TestSeqTag p1s1_0_p2p1 = new TestSeqTag( new int[] {0x00191002,0,0x00232001}, "p1s1_0_p2p1");
        TestSeqTag p1s1_0_p2p2 = new TestSeqTag( new int[] {0x00191002,0,0x00232015}, "p1s1_0_p2p2");
        TestSeqTag p1s1_0_p2p2_0_p3c = new TestSeqTag( new int[] {0x00191002,0,0x00232015,0,0x00230010}, "p3");
        TestSeqTag p1s1_0_p2p2_0_p3p1 = new TestSeqTag( new int[] {0x00191002,0,0x00232015,0,0x002310FF}, "p1s1_0_p2p2_0_p3p1");
        TestTag t2 = new TestTag(0x00401200, "t2");
        TestSeqTag t2_0_t1 = new TestSeqTag( new int[] {0x00401200,0,0x00180021}, "t2_0_t1");
        TestSeqTag t2_0_t2 = new TestSeqTag( new int[] {0x00401200,0,0x00180022}, "t2_0_t2");
        TestSeqTag t2_0_p4c = new TestSeqTag( new int[] {0x00401200,0,0x00290010}, "p4");
        TestSeqTag t2_0_p4p1 = new TestSeqTag( new int[] {0x00401200,0,0x00291000}, "t2_0_p4p1");
        TestSeqTag t2_0_p4p2 = new TestSeqTag( new int[] {0x00401200,0,0x00291000,0,0x00180010}, "t2_0_p4p2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( t1.tag, t1.initialValue);
        dobj.putString( p1c.tag, p1c.initialValue);
        dobj.putString( p1t1.tag, p1t1.initialValue);
        dobj.putString( p1s1.tag, p1s1.initialValue);
        dobj.putString( p1s1_0_p2c.tag, p1s1_0_p2c.initialValue);
        dobj.putString( p1s1_0_p2p1.tag, p1s1_0_p2p1.initialValue);
        dobj.putString( p1s1_0_p2p2.tag, p1s1_0_p2p2.initialValue);
        dobj.putString( p1s1_0_p2p2_0_p3c.tag, p1s1_0_p2p2_0_p3c.initialValue);
        dobj.putString( p1s1_0_p2p2_0_p3p1.tag, p1s1_0_p2p2_0_p3p1.initialValue);
        dobj.putString( t2.tag, t2.initialValue);
        dobj.putString( t2_0_t1.tag, t2_0_t1.initialValue);
        dobj.putString( t2_0_t2.tag, t2_0_t2.initialValue);
        dobj.putString( t2_0_p4c.tag, t2_0_p4c.initialValue);
        dobj.putString( t2_0_p4p1.tag, t2_0_p4p1.initialValue);
        dobj.putString( t2_0_p4p2.tag, t2_0_p4p2.initialValue);

        assertTrue( dobj.contains( t1.tag));
        assertTrue( dobj.contains( p1c.tag));
        assertTrue( dobj.contains( p1t1.tag));
        assertTrue( dobj.contains( p1s1.tag));
        assertTrue( dobj.contains( p1s1_0_p2c.tag));
        assertTrue( dobj.contains( p1s1_0_p2p1.tag));
        assertTrue( dobj.contains( p1s1_0_p2p2.tag));
        assertTrue( dobj.contains( p1s1_0_p2p2_0_p3c.tag));
        assertTrue( dobj.contains( p1s1_0_p2p2_0_p3p1.tag));
        assertTrue( dobj.contains( t2.tag));
        assertTrue( dobj.contains( t2_0_t1.tag));
        assertTrue( dobj.contains( t2_0_t2.tag));
        assertTrue( dobj.contains( t2_0_p4c.tag));
        assertTrue( dobj.contains( t2_0_p4p1.tag));
        assertTrue( dobj.contains( t2_0_p4p2.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( dobj.contains( t1.tag));
        assertFalse( dobj.contains( p1c.tag));
        assertFalse( dobj.contains( p1t1.tag));
        assertFalse( dobj.contains( p1s1.tag));
        assertFalse( dobj.contains( p1s1_0_p2c.tag));
        assertFalse( dobj.contains( p1s1_0_p2p1.tag));
        assertFalse( dobj.contains( p1s1_0_p2p2.tag));
        assertFalse( dobj.contains( p1s1_0_p2p2_0_p3c.tag));
        assertFalse( dobj.contains( p1s1_0_p2p2_0_p3p1.tag));
        assertTrue( dobj.contains( t2.tag));
        assertTrue( dobj.contains( t2_0_t1.tag));
        assertTrue( dobj.contains( t2_0_t2.tag));
        assertFalse( dobj.contains( t2_0_p4c.tag));
        assertFalse( dobj.contains( t2_0_p4p1.tag));
        assertFalse( dobj.contains( t2_0_p4p2.tag));
    }

    /**
     * retainPrivateTags[ (0019,{p1}02)/(0023,{p2}15)[%]/*]
     * <pre>
     * (0010,0010) PN #2 [t1] Patient’s Name                   retain
     * (0019,0010) LO #2 [p1] Private Creator Data Element     retain
     * (0019,1001) UN #4 [p1t1] ?                              delete
     * (0019,1002) SQ #-1 [1 item] ?                           retain
     * >ITEM #1:
     * >(0023,0020) LO #2 [p2] Private Creator Data Element    retain
     * >(0023,2001) UN #12 [p1s1_0_p2p1] ?                     delete
     * >(0023,2015) SQ #-1 [1 item] ?                          retain
     * >>ITEM #1:
     * >>(0023,0010) LO #2 [p3] Private Creator Data Element   retain
     * >>(0023,10FF) UN #18 [p1s1_0_p2p2_0_p3p1] ?             retain
     * (0040,1200) SQ #-1 [1 item] ?                           retain
     * >ITEM #1:
     * >(0018,0021) CS #8 [t2_0_t1] Sequence Variant           retain
     * >(0018,0022) CS #8 [t2_0_t2] Scan Options               retain
     * >(0029,0010) LO #2 [p4] Private Creator Data Element    delete
     * >(0029,1000) SQ #-1 [1 item] ?                          delete
     * >>ITEM #1:
     * >>(0018,0010) LO #10 [t2_0_p4p2] Contrast/Bolus Agent   delete
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testMixedSeqsRetain() throws MizerException {
        String script = "retainPrivateTags[ (0019,{p1}02)/(0023,{p2}15)]";
        TestTag t1 = new TestTag(0x00100010, "t1");
        TestTag p1c = new TestTag(0x00190010, "p1");
        TestTag p1t1 = new TestTag(0x00191001, "p1t1");
        TestTag p1s1 = new TestTag(0x00191002, "p1s1");
        TestSeqTag p1s1_0_p2c = new TestSeqTag( new int[] {0x00191002,0,0x00230010}, "p2");
        TestSeqTag p1s1_0_p2p1 = new TestSeqTag( new int[] {0x00191002,0,0x00231001}, "p1s1_0_p2p1");
        TestSeqTag p1s1_0_p2p2 = new TestSeqTag( new int[] {0x00191002,0,0x00231015}, "p1s1_0_p2p2");
        TestSeqTag p1s1_0_p2p2_0_p3c = new TestSeqTag( new int[] {0x00191002,0,0x00231015,0,0x00230010}, "p3");
        TestSeqTag p1s1_0_p2p2_0_p3p1 = new TestSeqTag( new int[] {0x00191002,0,0x00231015,0,0x002310FF}, "p1s1_0_p2p2_0_p3p1");
        TestTag t2 = new TestTag(0x00401200, "t2");
        TestSeqTag t2_0_t1 = new TestSeqTag( new int[] {0x00401200,0,0x00180021}, "t2_0_t1");
        TestSeqTag t2_0_t2 = new TestSeqTag( new int[] {0x00401200,0,0x00180022}, "t2_0_t2");
        TestSeqTag t2_0_p4c = new TestSeqTag( new int[] {0x00401200,0,0x00290010}, "p4");
        TestSeqTag t2_0_p4p1 = new TestSeqTag( new int[] {0x00401200,0,0x00291000}, "t2_0_p4p1");
        TestSeqTag t2_0_p4p2 = new TestSeqTag( new int[] {0x00401200,0,0x00291000,0,0x00180010}, "t2_0_p4p2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( t1.tag, t1.initialValue);
        dobj.putString( p1c.tag, p1c.initialValue);
        dobj.putString( p1t1.tag, p1t1.initialValue);
        dobj.putString( p1s1.tag, p1s1.initialValue);
        dobj.putString( p1s1_0_p2c.tag, p1s1_0_p2c.initialValue);
        dobj.putString( p1s1_0_p2p1.tag, p1s1_0_p2p1.initialValue);
        dobj.putString( p1s1_0_p2p2.tag, p1s1_0_p2p2.initialValue);
        dobj.putString( p1s1_0_p2p2_0_p3c.tag, p1s1_0_p2p2_0_p3c.initialValue);
        dobj.putString( p1s1_0_p2p2_0_p3p1.tag, p1s1_0_p2p2_0_p3p1.initialValue);
        dobj.putString( t2.tag, t2.initialValue);
        dobj.putString( t2_0_t1.tag, t2_0_t1.initialValue);
        dobj.putString( t2_0_t2.tag, t2_0_t2.initialValue);
        dobj.putString( t2_0_p4c.tag, t2_0_p4c.initialValue);
        dobj.putString( t2_0_p4p1.tag, t2_0_p4p1.initialValue);
        dobj.putString( t2_0_p4p2.tag, t2_0_p4p2.initialValue);

        assertTrue( dobj.contains( t1.tag));
        assertTrue( dobj.contains( p1c.tag));
        assertTrue( dobj.contains( p1t1.tag));
        assertTrue( dobj.contains( p1s1.tag));
        assertTrue( dobj.contains( p1s1_0_p2c.tag));
        assertTrue( dobj.contains( p1s1_0_p2p1.tag));
        assertTrue( dobj.contains( p1s1_0_p2p2.tag));
        assertTrue( dobj.contains( p1s1_0_p2p2_0_p3c.tag));
        assertTrue( dobj.contains( p1s1_0_p2p2_0_p3p1.tag));
        assertTrue( dobj.contains( t2.tag));
        assertTrue( dobj.contains( t2_0_t1.tag));
        assertTrue( dobj.contains( t2_0_t2.tag));
        assertTrue( dobj.contains( t2_0_p4c.tag));
        assertTrue( dobj.contains( t2_0_p4p1.tag));
        assertTrue( dobj.contains( t2_0_p4p2.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( dobj.contains( t1.tag));
        assertTrue( dobj.contains( p1c.tag));
        assertFalse( dobj.contains( p1t1.tag));
        assertTrue( dobj.contains( p1s1.tag));
        assertTrue( dobj.contains( p1s1_0_p2c.tag));
        assertFalse( dobj.contains( p1s1_0_p2p1.tag));
        assertTrue( dobj.contains( p1s1_0_p2p2.tag));
        assertTrue( dobj.contains( p1s1_0_p2p2_0_p3c.tag));
        assertTrue( dobj.contains( p1s1_0_p2p2_0_p3p1.tag));
        assertTrue( dobj.contains( t2.tag));
        assertTrue( dobj.contains( t2_0_t1.tag));
        assertTrue( dobj.contains( t2_0_t2.tag));
        assertFalse( dobj.contains( t2_0_p4c.tag));
        assertFalse( dobj.contains( t2_0_p4p1.tag));
        assertFalse( dobj.contains( t2_0_p4p2.tag));
    }

    /**
     * Show that tags are properly removed when creator IDs are present. See Part 2.
     *
     * (0019,0010) LO #4 [pb1] Private Creator Data Element    retain
     * (0019,10F0) SQ #-1 [1 item] ?                           retain
     * >ITEM #1:
     * >(0023,0010) LO #4 [pb2] Private Creator Data Element   retain
     * >(0023,1015) SQ #-1 [1 item] ?                          retain
     * >>ITEM #1:
     * >>(0023,0010) LO #4 [pb3] Private Creator Data Element  retain
     * >>(0023,1000) UN #6 [pb3_1] ?                           retain
     * >(0023,1022) SQ #-1 [1 item] ?                          delete
     * >>ITEM #1:
     * >>(0023,0010) LO #4 [pb4] Private Creator Data Element  delete
     * >>(0023,1000) UN #6 [pb4_1] ?                           delete
     *
     * @throws MizerException
     */
     @Test
    public void testOrphanPvtTagPart1() throws MizerException {
        String script = "retainPrivateTags[ (0019,{pb1}F0)/(0023,{pb2}15)]";
        TestTag pb1 =                new TestTag(0x00190010, "pb1");
        TestTag pb1_1 =              new TestTag(0x001910F0, "pb1_1");
        TestSeqTag pb2 =   new TestSeqTag( new int[] {0x001910F0,0,0x00230010}, "pb2");
        TestSeqTag pb2_1 = new TestSeqTag( new int[] {0x001910F0,0,0x00231015}, "pb2_1");
        TestSeqTag pb3 =   new TestSeqTag( new int[] {0x001910F0,0,0x00231015,0,0x00230010}, "pb3");
        TestSeqTag pb3_1 = new TestSeqTag( new int[] {0x001910F0,0,0x00231015,0,0x00231000}, "pb3_1");
        TestSeqTag pb2_2 = new TestSeqTag( new int[] {0x001910F0,0,0x00231022}, "pb2_2");
        TestSeqTag pb4 =   new TestSeqTag( new int[] {0x001910F0,0,0x00231022,0,0x00230010}, "pb4");
        TestSeqTag pb4_1 = new TestSeqTag( new int[] {0x001910F0,0,0x00231022,0,0x00231000}, "pb4_1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( pb1.tag, pb1.initialValue);
        dobj.putString( pb1_1.tag, pb1_1.initialValue);
        dobj.putString( pb2.tag, pb2.initialValue);
        dobj.putString( pb2_1.tag, pb2_1.initialValue);
        dobj.putString( pb3.tag, pb3.initialValue);
        dobj.putString( pb3_1.tag, pb3_1.initialValue);
        dobj.putString( pb2_2.tag, pb2_2.initialValue);
        dobj.putString( pb4.tag, pb4.initialValue);
        dobj.putString( pb4_1.tag, pb4_1.initialValue);

        assertTrue( dobj.contains( pb1.tag));
        assertTrue( dobj.contains( pb1_1.tag));
        assertTrue( dobj.contains( pb2.tag));
        assertTrue( dobj.contains( pb2_1.tag));
        assertTrue( dobj.contains( pb3.tag));
        assertTrue( dobj.contains( pb3_1.tag));
        assertTrue( dobj.contains( pb2_2.tag));
        assertTrue( dobj.contains( pb4.tag));
        assertTrue( dobj.contains( pb4_1.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( dobj.contains( pb1.tag));
        assertTrue( dobj.contains( pb1_1.tag));
        assertTrue( dobj.contains( pb2.tag));
        assertTrue( dobj.contains( pb2_1.tag));
        assertTrue( dobj.contains( pb3.tag));
        assertTrue( dobj.contains( pb3_1.tag));
        assertFalse( dobj.contains( pb2_2.tag));
        assertFalse( dobj.contains( pb4.tag));
        assertFalse( dobj.contains( pb4_1.tag));
    }

    /**
     * This is the DicomObject and script used in testOrphanPvtTagPart1 but with missing creator IDs for
     * (0019,12F0[%]/(0023,2015)[%]/(0023,2000)
     * (0019,12F0[%]/(0023,2022)[%]/(0023,2000)
     *
     * The structure remains but contents deleted.
     *
     * retainPrivateTags[ (0019,{pb1}F0)/(0023,{pb2}15)[%]/*]
     * <pre>
     * (0019,0010) LO #4 [pb1] Private Creator Data Element     retain
     * (0019,10F0) SQ #-1 [1 item] ?                            retain
     *   >ITEM #1:
     *   >(0023,0010) LO #4 [pb2] Private Creator Data Element  retain
     *   >(0023,1015) SQ #-1 [1 item] ?                         retain
     *     >>ITEM #1:
     *     >>(0023,1000) UN #6 [pb3_1] ?                        retain
     *   >(0023,1022) SQ #-1 [1 item] ?                         delete
     *     >>ITEM #1:
     *     >>(0023,1000) UN #6 [pb4_1] ?                        delete
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testOrphanPvtTagPart2() throws MizerException {
        String script = "retainPrivateTags[ (0019,{pb1}F0)/(0023,{pb2}15)]";
        TestTag pb1 =                new TestTag(0x00190010, "pb1");
        TestTag pb1_1 =              new TestTag(0x001910F0, "pb1_1");
        TestSeqTag pb2 =   new TestSeqTag( new int[] {0x001910F0,0,0x00230010}, "pb2");
        TestSeqTag pb2_1 = new TestSeqTag( new int[] {0x001910F0,0,0x00231015}, "pb2_1");
//        TestSeqTag pb3 =   new TestSeqTag( new int[] {0x001910F0,0,0x00231015,0,0x00230010}, "pb3");
        TestSeqTag pb3_1 = new TestSeqTag( new int[] {0x001910F0,0,0x00231015,0,0x00231000}, "pb3_1");
        TestSeqTag pb2_2 = new TestSeqTag( new int[] {0x001910F0,0,0x00231022}, "pb2_2");
//        TestSeqTag pb4 =   new TestSeqTag( new int[] {0x001910F0,0,0x00231022,0,0x00230010}, "pb4");
        TestSeqTag pb4_1 = new TestSeqTag( new int[] {0x001910F0,0,0x00231022,0,0x00231000}, "pb4_1");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( pb1.tag, pb1.initialValue);
        dobj.putString( pb1_1.tag, pb1_1.initialValue);
        dobj.putString( pb2.tag, pb2.initialValue);
        dobj.putString( pb2_1.tag, pb2_1.initialValue);
//        dobj.putString( pb3.tag, pb3.initialValue);
        dobj.putString( pb3_1.tag, pb3_1.initialValue);
        dobj.putString( pb2_2.tag, pb2_2.initialValue);
//        dobj.putString( pb4.tag, pb4.initialValue);
        dobj.putString( pb4_1.tag, pb4_1.initialValue);

        assertTrue( dobj.contains( pb1.tag));
        assertTrue( dobj.contains( pb1_1.tag));
        assertTrue( dobj.contains( pb2.tag));
        assertTrue( dobj.contains( pb2_1.tag));
//        assertTrue( dobj.contains( pb3.tag));
        assertTrue( dobj.contains( pb3_1.tag));
        assertTrue( dobj.contains( pb2_2.tag));
//        assertTrue( dobj.contains( pb4.tag));
        assertTrue( dobj.contains( pb4_1.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( dobj.contains( pb1.tag));
        assertTrue( dobj.contains( pb1_1.tag));
        assertTrue( dobj.contains( pb2.tag));
        assertTrue( dobj.contains( pb2_1.tag));
//        assertTrue( dobj.contains( pb3.tag));
        assertTrue( dobj.contains( pb3_1.tag));
        assertFalse( dobj.contains( pb2_2.tag));
//        assertFalse( dobj.contains( pb4.tag));
        assertFalse( dobj.contains( pb4_1.tag));
    }

    /**
     * This is the DicomObject and script used in testOrphanPvtTagsInRootPart2 but with the creator IDs present
     * just to verify that the script works as expected with good data.
     *
     * <pre>
     * (0019,0010) LO #4 [pb1] Private Creator Data Element    delete
     * (0019,0011) LO #4 [pb2] Private Creator Data Element    retain
     * (0019,0012) LO #4 [pb3] Private Creator Data Element    retain
     * (0019,10F0) LO #6 [pb1_1] ?                             delete
     * (0019,1100) LO #6 [pb2_1] ?                             retain
     * (0019,1200) LO #6 [pb3_1] ?                             delete
     * (0019,1201) LO #6 [pb3_2] ?                             retain
     * (0019,1202) LO #6 [pb3_3] ?                             delete
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testOrphanPvtTagsInRootPart1() throws MizerException {
        String script = "retainPrivateTags[ (0019,{pb2}00), (0019,{pb3}01)]";
        TestTag pb1 =   new TestTag(0x00190010, "pb1");
        TestTag pb1_1 = new TestTag(0x001910F0, "pb1_1");
        TestTag pb2 =   new TestTag(0x00190011, "pb2");
        TestTag pb2_1 = new TestTag(0x00191100, "pb2_1");
        TestTag pb3 =   new TestTag(0x00190012, "pb3");
        TestTag pb3_1 = new TestTag(0x00191200, "pb3_1");
        TestTag pb3_2 = new TestTag(0x00191201, "pb3_2");
        TestTag pb3_3 = new TestTag(0x00191202, "pb3_3");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( pb1.tag, pb1.initialValue);
        dobj.putString( pb1_1.tag, pb1_1.initialValue);
        dobj.putString( pb2.tag, pb2.initialValue);
        dobj.putString( pb2_1.tag, pb2_1.initialValue);
        dobj.putString( pb3.tag, pb3.initialValue);
        dobj.putString( pb3_1.tag, pb3_1.initialValue);
        dobj.putString( pb3_2.tag, pb3_2.initialValue);
        dobj.putString( pb3_3.tag, pb3_3.initialValue);

        assertTrue( dobj.contains( pb1.tag));
        assertTrue( dobj.contains( pb1_1.tag));
        assertTrue( dobj.contains( pb2.tag));
        assertTrue( dobj.contains( pb2_1.tag));
        assertTrue( dobj.contains( pb3.tag));
        assertTrue( dobj.contains( pb3_1.tag));
        assertTrue( dobj.contains( pb3_2.tag));
        assertTrue( dobj.contains( pb3_3.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertFalse( dobj.contains( pb1.tag));
        assertFalse( dobj.contains( pb1_1.tag));
        assertTrue( dobj.contains( pb2.tag));
        assertTrue( dobj.contains( pb2_1.tag));
        assertTrue( dobj.contains( pb3.tag));
        assertFalse( dobj.contains( pb3_1.tag));
        assertTrue( dobj.contains( pb3_2.tag));
        assertFalse( dobj.contains( pb3_3.tag));
    }

    /**
     * This is the DicomObject and script used in testOrphanPvtTagsInRootPart1 but with the creator IDs removed.
     * just to verify that the script works as expected with good data.
     *
     * No tags are retained because they can not be associated with creator IDs specified in retainPrivateTag
     * arguments.
     *
     * <pre>
     * (0019,10F0) LO #6 [pb1_1] ?      delete
     * (0019,1100) LO #6 [pb2_1] ?      can't retain
     * (0019,1200) LO #6 [pb3_1] ?      delete
     * (0019,1201) LO #6 [pb3_2] ?      can't retain
     * (0019,1202) LO #6 [pb3_3] ?      delete
     * </pre>
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testOrphanPvtTagsInRootPart2() throws MizerException {
        String script = "retainPrivateTags[ (0019,{pb2}00), (0019,{pb3}01)]";
        TestTag pb1_1 = new TestTag(0x001910F0, "pb1_1");
        TestTag pb2_1 = new TestTag(0x00191100, "pb2_1");
        TestTag pb3_1 = new TestTag(0x00191200, "pb3_1");
        TestTag pb3_2 = new TestTag(0x00191201, "pb3_2");
        TestTag pb3_3 = new TestTag(0x00191202, "pb3_3");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( pb1_1.tag, pb1_1.initialValue);
        dobj.putString( pb2_1.tag, pb2_1.initialValue);
        dobj.putString( pb3_1.tag, pb3_1.initialValue);
        dobj.putString( pb3_2.tag, pb3_2.initialValue);
        dobj.putString( pb3_3.tag, pb3_3.initialValue);

        assertTrue( dobj.contains( pb1_1.tag));
        assertTrue( dobj.contains( pb2_1.tag));
        assertTrue( dobj.contains( pb3_1.tag));
        assertTrue( dobj.contains( pb3_2.tag));
        assertTrue( dobj.contains( pb3_3.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertFalse( dobj.contains( pb1_1.tag));
        assertFalse( dobj.contains( pb2_1.tag));
        assertFalse( dobj.contains( pb3_1.tag));
        assertFalse( dobj.contains( pb3_2.tag));
        assertFalse( dobj.contains( pb3_3.tag));
    }

    /**
     * This data has multiple private blocks, including two blocks in the same group using the same creator id.
     * Remove all except the SIEMENS MEDCOM HEADER2 block.
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testSiemensDataMedcom() throws MizerException {

        String script = "retainPrivateTags[ (0029,{SIEMENS MEDCOM HEADER2}XX)]";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(FILE4);

        assertTrue( src_dobj.contains(geis_creator_id));
        assertTrue( src_dobj.contains(geis_data_12));

        assertTrue( src_dobj.contains(siemens_mr_header_creator_id_19));
        assertTrue( src_dobj.contains(siemens_mr_19_data_8));
        assertTrue( src_dobj.contains(siemens_mr_19_data_9));
        assertTrue( src_dobj.contains(siemens_mr_19_data_B));
        assertTrue( src_dobj.contains(siemens_mr_19_data_F));
        assertTrue( src_dobj.contains(siemens_mr_19_data_11));
        assertTrue( src_dobj.contains(siemens_mr_19_data_12));
        assertTrue( src_dobj.contains(siemens_mr_19_data_13));
        assertTrue( src_dobj.contains(siemens_mr_19_data_14));
        assertTrue( src_dobj.contains(siemens_mr_19_data_15));
        assertTrue( src_dobj.contains(siemens_mr_19_data_17));
        assertTrue( src_dobj.contains(siemens_mr_19_data_18));

        assertTrue( src_dobj.contains(siemens_csa_creator_id));
        assertTrue( src_dobj.contains(siemens_medcom_creator_id));
        assertTrue( src_dobj.contains(fuji_12_creator_id));
        assertTrue( src_dobj.contains(fuji_E1_creator_id));

        assertTrue( src_dobj.contains(csa_data_8));
        assertTrue( src_dobj.contains(csa_data_9));
        assertTrue( src_dobj.contains(csa_data_10));
        assertTrue( src_dobj.contains(csa_data_18));
        assertTrue( src_dobj.contains(csa_data_19));
        assertTrue( src_dobj.contains(csa_data_20));

        assertTrue( src_dobj.contains(medcom_data_60));

        assertTrue( src_dobj.contains(fuji_data_1231));
        assertTrue( src_dobj.contains(fuji_data_E131));

        assertTrue( src_dobj.contains(siemens_mr_header_creator_id_51));
        assertTrue( src_dobj.contains(siemens_mr_51_data_8));
        assertTrue( src_dobj.contains(siemens_mr_51_data_9));
        assertTrue( src_dobj.contains(siemens_mr_51_data_A));
        assertTrue( src_dobj.contains(siemens_mr_51_data_C));
        assertTrue( src_dobj.contains(siemens_mr_51_data_D));
        assertTrue( src_dobj.contains(siemens_mr_51_data_E));
        assertTrue( src_dobj.contains(siemens_mr_51_data_F));
        assertTrue( src_dobj.contains(siemens_mr_51_data_11));
        assertTrue( src_dobj.contains(siemens_mr_51_data_12));
        assertTrue( src_dobj.contains(siemens_mr_51_data_13));
        assertTrue( src_dobj.contains(siemens_mr_51_data_16));
        assertTrue( src_dobj.contains(siemens_mr_51_data_17));
        assertTrue( src_dobj.contains(siemens_mr_51_data_19));

        assertTrue( src_dobj.contains(geis_pacs_903_creator_id));
        assertTrue( src_dobj.contains(geis_pacs_903_data_10));
        assertTrue( src_dobj.contains(geis_pacs_903_data_11));
        assertTrue( src_dobj.contains(geis_pacs_903_data_12));

        assertTrue( src_dobj.contains(geis_905_creator_id));
        assertTrue( src_dobj.contains(geis_905_data_30));

        assertTrue( src_dobj.contains(geis_7FD1_creator_id));
        assertTrue( src_dobj.contains(geis_7FD1_data_30));
        assertTrue( src_dobj.contains(geis_7FD1_data_40));
        assertTrue( src_dobj.contains(geis_7FD1_data_50));
        assertTrue( src_dobj.contains(geis_7FD1_data_60));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertFalse( result_dobj.contains(geis_creator_id));
        assertFalse( result_dobj.contains(geis_data_12));

        assertFalse( result_dobj.contains(siemens_mr_header_creator_id_19));
        assertFalse( result_dobj.contains(siemens_mr_19_data_8));
        assertFalse( result_dobj.contains(siemens_mr_19_data_9));
        assertFalse( result_dobj.contains(siemens_mr_19_data_B));
        assertFalse( result_dobj.contains(siemens_mr_19_data_F));
        assertFalse( result_dobj.contains(siemens_mr_19_data_11));
        assertFalse( result_dobj.contains(siemens_mr_19_data_12));
        assertFalse( result_dobj.contains(siemens_mr_19_data_13));
        assertFalse( result_dobj.contains(siemens_mr_19_data_14));
        assertFalse( result_dobj.contains(siemens_mr_19_data_15));
        assertFalse( result_dobj.contains(siemens_mr_19_data_17));
        assertFalse( result_dobj.contains(siemens_mr_19_data_18));

        assertFalse( result_dobj.contains(siemens_csa_creator_id));
        assertTrue( result_dobj.contains(siemens_medcom_creator_id));
//        assertFalse( result_dobj.contains(fuji_12_creator_id));
//        assertFalse( result_dobj.contains(fuji_E1_creator_id));

        assertFalse( result_dobj.contains(csa_data_8));
        assertFalse( result_dobj.contains(csa_data_9));
        assertFalse( result_dobj.contains(csa_data_10));
        assertFalse( result_dobj.contains(csa_data_18));
        assertFalse( result_dobj.contains(csa_data_19));
        assertFalse( result_dobj.contains(csa_data_20));

        assertTrue( result_dobj.contains(medcom_data_60));

        assertFalse( result_dobj.contains(fuji_data_1231));
//        assertFalse( result_dobj.contains(fuji_data_E131));

        assertFalse( result_dobj.contains(siemens_mr_header_creator_id_51));
        assertFalse( result_dobj.contains(siemens_mr_51_data_8));
        assertFalse( result_dobj.contains(siemens_mr_51_data_9));
        assertFalse( result_dobj.contains(siemens_mr_51_data_A));
        assertFalse( result_dobj.contains(siemens_mr_51_data_C));
        assertFalse( result_dobj.contains(siemens_mr_51_data_D));
        assertFalse( result_dobj.contains(siemens_mr_51_data_E));
        assertFalse( result_dobj.contains(siemens_mr_51_data_F));
        assertFalse( result_dobj.contains(siemens_mr_51_data_11));
        assertFalse( result_dobj.contains(siemens_mr_51_data_12));
        assertFalse( result_dobj.contains(siemens_mr_51_data_13));
        assertFalse( result_dobj.contains(siemens_mr_51_data_16));
        assertFalse( result_dobj.contains(siemens_mr_51_data_17));
        assertFalse( result_dobj.contains(siemens_mr_51_data_19));

        assertFalse( result_dobj.contains(geis_pacs_903_creator_id));
        assertFalse( result_dobj.contains(geis_pacs_903_data_10));
        assertFalse( result_dobj.contains(geis_pacs_903_data_11));
        assertFalse( result_dobj.contains(geis_pacs_903_data_12));

        assertFalse( result_dobj.contains(geis_905_creator_id));
        assertFalse( result_dobj.contains(geis_905_data_30));

        assertFalse( result_dobj.contains(geis_7FD1_creator_id));
        assertFalse( result_dobj.contains(geis_7FD1_data_30));
        assertFalse( result_dobj.contains(geis_7FD1_data_40));
        assertFalse( result_dobj.contains(geis_7FD1_data_50));
        assertFalse( result_dobj.contains(geis_7FD1_data_60));
    }

    @Test
    public void testSiemensData() throws MizerException {

        String script = "retainPrivateTags[ (0029,{SIEMENS CSA HEADER}XX)]";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(FILE4);

        assertTrue( src_dobj.contains(geis_creator_id));
        assertTrue( src_dobj.contains(geis_data_12));

        assertTrue( src_dobj.contains(siemens_mr_header_creator_id_19));
        assertTrue( src_dobj.contains(siemens_mr_19_data_8));
        assertTrue( src_dobj.contains(siemens_mr_19_data_9));
        assertTrue( src_dobj.contains(siemens_mr_19_data_B));
        assertTrue( src_dobj.contains(siemens_mr_19_data_F));
        assertTrue( src_dobj.contains(siemens_mr_19_data_11));
        assertTrue( src_dobj.contains(siemens_mr_19_data_12));
        assertTrue( src_dobj.contains(siemens_mr_19_data_13));
        assertTrue( src_dobj.contains(siemens_mr_19_data_14));
        assertTrue( src_dobj.contains(siemens_mr_19_data_15));
        assertTrue( src_dobj.contains(siemens_mr_19_data_17));
        assertTrue( src_dobj.contains(siemens_mr_19_data_18));

        assertTrue( src_dobj.contains(siemens_csa_creator_id));
        assertTrue( src_dobj.contains(siemens_medcom_creator_id));
        assertTrue( src_dobj.contains(fuji_12_creator_id));
        assertTrue( src_dobj.contains(fuji_E1_creator_id));

        assertTrue( src_dobj.contains(csa_data_8));
        assertTrue( src_dobj.contains(csa_data_9));
        assertTrue( src_dobj.contains(csa_data_10));
        assertTrue( src_dobj.contains(csa_data_18));
        assertTrue( src_dobj.contains(csa_data_19));
        assertTrue( src_dobj.contains(csa_data_20));

        assertTrue( src_dobj.contains(medcom_data_60));

        assertTrue( src_dobj.contains(fuji_data_1231));
        assertTrue( src_dobj.contains(fuji_data_E131));

        assertTrue( src_dobj.contains(siemens_mr_header_creator_id_51));
        assertTrue( src_dobj.contains(siemens_mr_51_data_8));
        assertTrue( src_dobj.contains(siemens_mr_51_data_9));
        assertTrue( src_dobj.contains(siemens_mr_51_data_A));
        assertTrue( src_dobj.contains(siemens_mr_51_data_C));
        assertTrue( src_dobj.contains(siemens_mr_51_data_D));
        assertTrue( src_dobj.contains(siemens_mr_51_data_E));
        assertTrue( src_dobj.contains(siemens_mr_51_data_F));
        assertTrue( src_dobj.contains(siemens_mr_51_data_11));
        assertTrue( src_dobj.contains(siemens_mr_51_data_12));
        assertTrue( src_dobj.contains(siemens_mr_51_data_13));
        assertTrue( src_dobj.contains(siemens_mr_51_data_16));
        assertTrue( src_dobj.contains(siemens_mr_51_data_17));
        assertTrue( src_dobj.contains(siemens_mr_51_data_19));

        assertTrue( src_dobj.contains(geis_pacs_903_creator_id));
        assertTrue( src_dobj.contains(geis_pacs_903_data_10));
        assertTrue( src_dobj.contains(geis_pacs_903_data_11));
        assertTrue( src_dobj.contains(geis_pacs_903_data_12));

        assertTrue( src_dobj.contains(geis_905_creator_id));
        assertTrue( src_dobj.contains(geis_905_data_30));

        assertTrue( src_dobj.contains(geis_7FD1_creator_id));
        assertTrue( src_dobj.contains(geis_7FD1_data_30));
        assertTrue( src_dobj.contains(geis_7FD1_data_40));
        assertTrue( src_dobj.contains(geis_7FD1_data_50));
        assertTrue( src_dobj.contains(geis_7FD1_data_60));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertFalse( result_dobj.contains(geis_creator_id));
        assertFalse( result_dobj.contains(geis_data_12));

        assertFalse( result_dobj.contains(siemens_mr_header_creator_id_19));
        assertFalse( result_dobj.contains(siemens_mr_19_data_8));
        assertFalse( result_dobj.contains(siemens_mr_19_data_9));
        assertFalse( result_dobj.contains(siemens_mr_19_data_B));
        assertFalse( result_dobj.contains(siemens_mr_19_data_F));
        assertFalse( result_dobj.contains(siemens_mr_19_data_11));
        assertFalse( result_dobj.contains(siemens_mr_19_data_12));
        assertFalse( result_dobj.contains(siemens_mr_19_data_13));
        assertFalse( result_dobj.contains(siemens_mr_19_data_14));
        assertFalse( result_dobj.contains(siemens_mr_19_data_15));
        assertFalse( result_dobj.contains(siemens_mr_19_data_17));
        assertFalse( result_dobj.contains(siemens_mr_19_data_18));

        assertTrue( result_dobj.contains(siemens_csa_creator_id));
        assertFalse( result_dobj.contains(siemens_medcom_creator_id));
//        assertFalse( result_dobj.contains(fuji_12_creator_id));
//        assertFalse( result_dobj.contains(fuji_E1_creator_id));

        assertTrue( result_dobj.contains(csa_data_8));
        assertTrue( result_dobj.contains(csa_data_9));
        assertTrue( result_dobj.contains(csa_data_10));
        assertTrue( result_dobj.contains(csa_data_18));
        assertTrue( result_dobj.contains(csa_data_19));
        assertTrue( result_dobj.contains(csa_data_20));

        assertFalse( result_dobj.contains(medcom_data_60));

        assertFalse( result_dobj.contains(fuji_data_1231));
//        assertFalse( result_dobj.contains(fuji_data_E131));

        assertFalse( result_dobj.contains(siemens_mr_header_creator_id_51));
        assertFalse( result_dobj.contains(siemens_mr_51_data_8));
        assertFalse( result_dobj.contains(siemens_mr_51_data_9));
        assertFalse( result_dobj.contains(siemens_mr_51_data_A));
        assertFalse( result_dobj.contains(siemens_mr_51_data_C));
        assertFalse( result_dobj.contains(siemens_mr_51_data_D));
        assertFalse( result_dobj.contains(siemens_mr_51_data_E));
        assertFalse( result_dobj.contains(siemens_mr_51_data_F));
        assertFalse( result_dobj.contains(siemens_mr_51_data_11));
        assertFalse( result_dobj.contains(siemens_mr_51_data_12));
        assertFalse( result_dobj.contains(siemens_mr_51_data_13));
        assertFalse( result_dobj.contains(siemens_mr_51_data_16));
        assertFalse( result_dobj.contains(siemens_mr_51_data_17));
        assertFalse( result_dobj.contains(siemens_mr_51_data_19));

        assertFalse( result_dobj.contains(geis_pacs_903_creator_id));
        assertFalse( result_dobj.contains(geis_pacs_903_data_10));
        assertFalse( result_dobj.contains(geis_pacs_903_data_11));
        assertFalse( result_dobj.contains(geis_pacs_903_data_12));

        assertFalse( result_dobj.contains(geis_905_creator_id));
        assertFalse( result_dobj.contains(geis_905_data_30));

        assertFalse( result_dobj.contains(geis_7FD1_creator_id));
        assertFalse( result_dobj.contains(geis_7FD1_data_30));
        assertFalse( result_dobj.contains(geis_7FD1_data_40));
        assertFalse( result_dobj.contains(geis_7FD1_data_50));
        assertFalse( result_dobj.contains(geis_7FD1_data_60));
    }

    /**
     * This data from Philips has at least two private blocks and
     * a combination or normal private elements and private sequence elements.
     * Remove all except a fictional block should remove all of the Philips elements.
     * Note that we do not test each private element.
     *
     * @throws MizerException unexpected exception
     */
    @Test
    public void testPhilipsSequence() throws MizerException {

        String script = "retainPrivateTags[ (0029,{FICTION}XX)]";

        DicomObjectI dobj = DicomObjectFactory.newInstance(de11);

        assertTrue( dobj.contains(philips_mr_header_creator_id_2001));
        assertTrue( dobj.contains(philips_mr_header_creator_id_2005));
        assertTrue( dobj.contains(0x2001105F));

        assertTrue( dobj.contains(0x20051400));
        assertTrue( dobj.contains(0x20051401));
        assertTrue( dobj.contains(0x20051402));
        assertTrue( dobj.contains(0x20051403));
        assertTrue( dobj.contains(0x2005140E));
        assertTrue( dobj.contains(0x2005140F));
        assertTrue( dobj.contains(0x20051414));
        assertTrue( dobj.contains(0x20051415));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        String[] modTags = listAllTags(dobj);

        assertFalse( dobj.contains(0x2001105F));

        assertFalse( dobj.contains(0x20051400));
        assertFalse( dobj.contains(0x20051401));
        assertFalse( dobj.contains(0x20051402));
        assertFalse( dobj.contains(0x20051403));
        assertFalse( dobj.contains(0x2005140E));
        assertFalse( dobj.contains(0x2005140F));
        assertFalse( dobj.contains(0x20051414));
        assertFalse( dobj.contains(0x20051415));
    }

    @Test
    public void testDE11() throws MizerException, IOException {
        String script = "retainPrivateTags[ (0029,{FICTION}XX)]";

        DicomObjectI dobj = DicomObjectFactory.newInstance(de11);

        assertTrue( numberOfPvtTags(dobj) > 0);

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals(0, numberOfPvtTags(dobj));
    }


    private final int geis_creator_id = 0x00090010;
    private final int geis_data_12 = 0x00091012;

    private final int siemens_mr_header_creator_id_19 = 0x00190010;
    private final int siemens_mr_19_data_8 = 0x00191008;
    private final int siemens_mr_19_data_9 = 0x00191009;
    private final int siemens_mr_19_data_B = 0x0019100B;
    private final int siemens_mr_19_data_F = 0x0019100F;
    private final int siemens_mr_19_data_11 = 0x00191011;
    private final int siemens_mr_19_data_12 = 0x00191012;
    private final int siemens_mr_19_data_13 = 0x00191013;
    private final int siemens_mr_19_data_14 = 0x00191014;
    private final int siemens_mr_19_data_15 = 0x00191015;
    private final int siemens_mr_19_data_17 = 0x00191017;
    private final int siemens_mr_19_data_18 = 0x00191018;

    private final int siemens_csa_creator_id = 0x00290010;
    private final int siemens_medcom_creator_id = 0x00290011;
    private final int fuji_12_creator_id = 0x00290012;
    private final int fuji_E1_creator_id = 0x002900E1;

    private final int csa_data_8 = 0x00291008;
    private final int csa_data_9 = 0x00291009;
    private final int csa_data_10 = 0x00291010;
    private final int csa_data_18 = 0x00291018;
    private final int csa_data_19 = 0x00291019;
    private final int csa_data_20 = 0x00291020;

    private final int medcom_data_60 = 0x00291160;

    private final int fuji_data_1231 = 0x00291231;
    private final int fuji_data_E131 = 0x0029E131;

    private final int siemens_mr_header_creator_id_51 = 0x00510010;
    private final int siemens_mr_51_data_8 = 0x00511008;
    private final int siemens_mr_51_data_9 = 0x00511009;
    private final int siemens_mr_51_data_A = 0x0051100A;
    private final int siemens_mr_51_data_C = 0x0051100C;
    private final int siemens_mr_51_data_D = 0x0051100D;
    private final int siemens_mr_51_data_E = 0x0051100E;
    private final int siemens_mr_51_data_F = 0x0051100F;
    private final int siemens_mr_51_data_11 = 0x00511011;
    private final int siemens_mr_51_data_12 = 0x00511012;
    private final int siemens_mr_51_data_13 = 0x00511013;
    private final int siemens_mr_51_data_16 = 0x00511016;
    private final int siemens_mr_51_data_17 = 0x00511017;
    private final int siemens_mr_51_data_19 = 0x00511019;

    private final int geis_pacs_903_creator_id = 0x09030010;
    private final int geis_pacs_903_data_10 = 0x09031010;
    private final int geis_pacs_903_data_11 = 0x09031011;
    private final int geis_pacs_903_data_12 = 0x09031012;

    private final int geis_905_creator_id = 0x09050010;
    private final int geis_905_data_30 = 0x09051030;

    private final int geis_7FD1_creator_id = 0x7FD10010;
    private final int geis_7FD1_data_30 = 0x7FD11030;
    private final int geis_7FD1_data_40 = 0x7FD11040;
    private final int geis_7FD1_data_50 = 0x7FD11050;
    private final int geis_7FD1_data_60 = 0x7FD11060;

    private final int philips_mr_header_creator_id_2001 = 0x20010010;
    private final int philips_2001_data_5F = 0x2001105F;

    private final int philips_mr_header_creator_id_2005 = 0x20050010;

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    /**
     * This data is created to mimic actual CT data.
     *
     * Normal private tags with creator IDs
     * Private tags without creator IDs
     * Private sequence tag with creator ID containing private tags and private sequence tags without creator IDs.
     *
     */
    @Test
    public void testProdCT() {

        try {
            String script = "retainPrivateTags[ (0029,{SIEMENS_MEDCOM_HEADER_ID29}XX)]";

            DicomObjectI dobj = createTestObject();

            assertEquals(dobj.getString(MODALITY), "CT");

            assertTrue( dobj.contains(PATIENT_NAME));
            assertTrue( dobj.contains(SEQ1_1));
            assertTrue( dobj.contains(SEQ1_2));
            assertTrue( dobj.contains(SEQ1_SEQ1_1));
            assertTrue( dobj.contains(SEQ1_SEQ1_2));
            assertTrue( dobj.contains(SPI_RELEASE_1_ID9));
            assertTrue( dobj.contains(EMAGEON_STUDY_HOME_ID));
            assertTrue( dobj.contains(EMAGEON_JPEG2K_INFO_ID));
            assertTrue( dobj.contains(EMAGEON_JPEG2K_INFO_00));
            assertTrue( dobj.contains(EMAGEON_JPEG2K_INFO_00_13));
            assertTrue( dobj.contains(EMAGEON_JPEG2K_INFO_00_15));
            assertTrue( dobj.contains(EMAGEON_JPEG2K_INFO_00_15_16));
            assertTrue( dobj.contains(EMAGEON_JPEG2K_INFO_00_15_17));
            assertTrue( dobj.contains(EMAGEON_JPEG2K_INFO_00_15_18));
            assertTrue( dobj.contains(EMAGEON_JPEG2K_INFO_00_15_19));
            assertTrue( dobj.contains(EMAGEON_JPEG2K_INFO_01));
            assertTrue( dobj.contains(orphan0));
            assertTrue( dobj.contains(orphan2));
            assertTrue( dobj.contains(SPI_RELEASE_1_ID11));
            assertTrue( dobj.contains(SPI_RELEASE_1_ID11_10));
            assertTrue( dobj.contains(SIEMENS_MEDCOM_HEADER_ID29));
            assertTrue( dobj.contains(SIEMENS_MEDCOM_OOG_ID29));
            assertTrue( dobj.contains(SIEMENS_MEDCOM_OOG_ID29_08));
            assertTrue( dobj.contains(SIEMENS_MEDCOM_OOG_ID29_09));
            assertTrue( dobj.contains(SIEMENS_MEDCOM_OOG_ID29_10));
            assertTrue( dobj.contains(SIENET_ID91));
            assertTrue( dobj.contains(SIENET_ID91_20));
            assertTrue( dobj.contains(SIENET_ID95));
            assertTrue( dobj.contains(SIENET_ID95_10));
            assertTrue( dobj.contains(SIENET_ID97));
            assertTrue( dobj.contains(SIENET_ID97_03));
            assertTrue( dobj.contains(SIENET_ID99));
            assertTrue( dobj.contains(SIENET_ID99_02));
            assertTrue( dobj.contains(SIENET_ID99_05));

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
            dobj = sa.apply(dobj).getDicomObject();

            assertEquals(dobj.getString(MODALITY), "CT");

            assertTrue( dobj.contains(PATIENT_NAME));
            assertTrue( dobj.contains(SEQ1_1));
            assertTrue( dobj.contains(SEQ1_2));
            assertTrue( dobj.contains(SEQ1_SEQ1_1));
            assertTrue( dobj.contains(SEQ1_SEQ1_2));
            assertFalse( dobj.contains(SPI_RELEASE_1_ID9));
            assertFalse( dobj.contains(EMAGEON_STUDY_HOME_ID));
            assertFalse( dobj.contains(EMAGEON_JPEG2K_INFO_ID));
            assertFalse( dobj.contains(EMAGEON_JPEG2K_INFO_00));
            assertFalse( dobj.contains(EMAGEON_JPEG2K_INFO_00_13));
            assertFalse( dobj.contains(EMAGEON_JPEG2K_INFO_00_15));
            assertFalse( dobj.contains(EMAGEON_JPEG2K_INFO_00_15_16));
            assertFalse( dobj.contains(EMAGEON_JPEG2K_INFO_00_15_17));
            assertFalse( dobj.contains(EMAGEON_JPEG2K_INFO_00_15_18));
            assertFalse( dobj.contains(EMAGEON_JPEG2K_INFO_00_15_19));
            assertFalse( dobj.contains(EMAGEON_JPEG2K_INFO_01));
            assertFalse( dobj.contains(orphan0));
            assertFalse( dobj.contains(orphan2));
            assertFalse( dobj.contains(SPI_RELEASE_1_ID11));
            assertFalse( dobj.contains(SPI_RELEASE_1_ID11_10));
            assertTrue( dobj.contains(SIEMENS_MEDCOM_HEADER_ID29));
            assertFalse( dobj.contains(SIEMENS_MEDCOM_OOG_ID29));
            assertFalse( dobj.contains(SIEMENS_MEDCOM_OOG_ID29_08));
            assertFalse( dobj.contains(SIEMENS_MEDCOM_OOG_ID29_09));
            assertFalse( dobj.contains(SIEMENS_MEDCOM_OOG_ID29_10));
            assertFalse( dobj.contains(SIENET_ID91));
            assertFalse( dobj.contains(SIENET_ID91_20));
            assertFalse( dobj.contains(SIENET_ID95));
            assertFalse( dobj.contains(SIENET_ID95_10));
            assertFalse( dobj.contains(SIENET_ID97));
            assertFalse( dobj.contains(SIENET_ID97_03));
            assertFalse( dobj.contains(SIENET_ID99));
            assertFalse( dobj.contains(SIENET_ID99_02));
            assertFalse( dobj.contains(SIENET_ID99_05));

        }
        catch( Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    private DicomObjectI createTestObject() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString( MODALITY, "CT");
        src_dobj.putString( PATIENT_NAME, "PATIENT_NAME");
        src_dobj.putString( SEQ1_1, "SEQ1_1");
        src_dobj.putString( SEQ1_2, "SEQ1_2");
        src_dobj.putString( SEQ1_SEQ1_1, "SEQ1_SEQ1_1");
        src_dobj.putString( SEQ1_SEQ1_2, "SEQ1_SEQ1_2");

        src_dobj.putString( SPI_RELEASE_1_ID9, "SPI RELEASE 1");
        src_dobj.putString( EMAGEON_STUDY_HOME_ID, "EMAGEON STUDY HOME");
        src_dobj.putString( EMAGEON_JPEG2K_INFO_ID, "EMAGEON JPEG2K INFO");

        src_dobj.putString( EMAGEON_JPEG2K_INFO_00, "EMAGEON_JPEG2K_INFO_00");

        src_dobj.putString( EMAGEON_JPEG2K_INFO_00_13, "EMAGEON_JPEG2K_INFO_00_13");
        src_dobj.putString( EMAGEON_JPEG2K_INFO_00_15, "EMAGEON_JPEG2K_INFO_00_15");
        src_dobj.putString( EMAGEON_JPEG2K_INFO_00_15_16, "EMAGEON_JPEG2K_INFO_00_15_16");
        src_dobj.putString( EMAGEON_JPEG2K_INFO_00_15_17, "EMAGEON_JPEG2K_INFO_00_15_17");
        src_dobj.putString( EMAGEON_JPEG2K_INFO_00_15_18, "EMAGEON_JPEG2K_INFO_00_15_18");
        src_dobj.putString( EMAGEON_JPEG2K_INFO_00_15_19, "EMAGEON_JPEG2K_INFO_00_15_19");

        src_dobj.putString( EMAGEON_JPEG2K_INFO_01, "EMAGEON_JPEG2K_INFO_01");

        src_dobj.putString( orphan0, "orphan0");
        src_dobj.putString( orphan2, "orphan2");

        src_dobj.putString( SPI_RELEASE_1_ID11, "SPI_RELEASE_1_ID11");
        src_dobj.putString( SPI_RELEASE_1_ID11_10, "SPI_RELEASE_1_ID11_10");

        src_dobj.putString( SIEMENS_MEDCOM_HEADER_ID29, "SIEMENS_MEDCOM_HEADER_ID29");
        src_dobj.putString( SIEMENS_MEDCOM_OOG_ID29, "SIEMENS_MEDCOM_OOG_ID29");

        src_dobj.putString( SIEMENS_MEDCOM_HEADER_ID29_31, "SIEMENS_MEDCOM_HEADER_ID29_31");
        src_dobj.putString( SIEMENS_MEDCOM_HEADER_ID29_34, "SIEMENS_MEDCOM_HEADER_ID29_34");
        src_dobj.putString( SIEMENS_MEDCOM_OOG_ID29_08, "SIEMENS_MEDCOM_OOG_ID29_08");
        src_dobj.putString( SIEMENS_MEDCOM_OOG_ID29_09, "SIEMENS_MEDCOM_OOG_ID29_09");
        src_dobj.putString( SIEMENS_MEDCOM_OOG_ID29_10, "SIEMENS_MEDCOM_OOG_ID29_10");

        src_dobj.putString( SIENET_ID91, "SIENET_ID91");
        src_dobj.putString( SIENET_ID91_20, "SIENET_ID91_20");

        src_dobj.putString( SIENET_ID95, "SIENET_ID95");
        src_dobj.putString( SIENET_ID95_10, "SIENET_ID95_10");

        src_dobj.putString( SIENET_ID97, "SIENET_ID97");
        src_dobj.putString( SIENET_ID97_03, "SIEMENS_MEDCOM_OOG_ID29");

        src_dobj.putString( SIENET_ID99, "SIENET_ID99");
        src_dobj.putString( SIENET_ID99_02, "SIENET_ID99_02");
        src_dobj.putString( SIENET_ID99_05, "SIENET_ID99_05");

        return src_dobj;
    }

    private final static int[] MODALITY         = {0x00080060};

    private final static int[] PATIENT_NAME     = {0x00100010};

    // 0x00100017 must stay an element the DICOM standard has not assigned: these constants are
    // placeholders for "some element inside a sequence item", not real attributes. This was
    // 0x00100011 until dcm4che 5.35.0 picked up a dictionary in which the standard had assigned
    // it as PersonNamesToUseSequence, VR SQ, and setting a string value on it stopped working.
    private final static int[] SEQ1_1           = {0x00080066,0,0x00100010};
    private final static int[] SEQ1_2           = {0x00080066,0,0x00100017};
    private final static int[] SEQ1_SEQ1_1      = {0x00080066,0,0x00100020,0,0x00100010};
    private final static int[] SEQ1_SEQ1_2      = {0x00080066,0,0x00100020,0,0x00100017};


    private final static int[] SPI_RELEASE_1_ID9      = {0x00090010};
    private final static int[] EMAGEON_STUDY_HOME_ID  = {0x00090011};
    private final static int[] EMAGEON_JPEG2K_INFO_ID = {0x00090012};

    private final static int[] SPI_RELEASE_1_15      = {0x00091015};
    private final static int[] EMAGEON_STUDY_HOME_00 = {0x00091100};
    private final static int[] EMAGEON_STUDY_HOME_01 = {0x00091101};

    private final static int[] EMAGEON_JPEG2K_INFO_00 = {0x00091200};

    private final static int[] EMAGEON_JPEG2K_INFO_00_13    = {0x00091200,0,0x00232013};
    private final static int[] EMAGEON_JPEG2K_INFO_00_15    = {0x00091200,0,0x00232015};
    private final static int[] EMAGEON_JPEG2K_INFO_00_15_16 = {0x00091200,0,0x00232015,0,0x00232016};
    private final static int[] EMAGEON_JPEG2K_INFO_00_15_17 = {0x00091200,0,0x00232015,0,0x00232017};
    private final static int[] EMAGEON_JPEG2K_INFO_00_15_18 = {0x00091200,0,0x00232015,0,0x00232018};
    private final static int[] EMAGEON_JPEG2K_INFO_00_15_19 = {0x00091200,0,0x00232015,0,0x00232019};

    private final static int[] EMAGEON_JPEG2K_INFO_01 = {0x00091201};

    private final static int[] orphan0 = {0x00097700};
    private final static int[] orphan2 = {0x00097702};

    private final static int[] SPI_RELEASE_1_ID11    = {0x00110010};
    private final static int[] SPI_RELEASE_1_ID11_10 = {0x00111010};

    private final static int[] SIEMENS_MEDCOM_HEADER_ID29 = {0x00290010};
    private final static int[] SIEMENS_MEDCOM_OOG_ID29    = {0x00290011};

    private final static int[] SIEMENS_MEDCOM_HEADER_ID29_31 = {0x00291031};
    private final static int[] SIEMENS_MEDCOM_HEADER_ID29_34 = {0x00291034};
    private final static int[] SIEMENS_MEDCOM_OOG_ID29_08    = {0x00291108};
    private final static int[] SIEMENS_MEDCOM_OOG_ID29_09    = {0x00291109};
    private final static int[] SIEMENS_MEDCOM_OOG_ID29_10    = {0x00291110};

    private final static int[] SIENET_ID91    = {0x00910010};
    private final static int[] SIENET_ID91_20 = {0x00911020};

    private final static int[] SIENET_ID95    = {0x00950010};
    private final static int[] SIENET_ID95_10 = {0x00951010};

    private final static int[] SIENET_ID97    = {0x00970010};
    private final static int[] SIENET_ID97_03 = {0x00971003};

    private final static int[] SIENET_ID99    = {0x00990010};
    private final static int[] SIENET_ID99_02 = {0x00991002};
    private final static int[] SIENET_ID99_05 = {0x00991005};

    @Test
    public void testFile0() throws MizerException {

        final DicomObjectI dobj = DicomObjectFactory.newInstance(FILE0);

        String[] origPvtTags = listPvtTags(dobj);

        String script = "retainPrivateTags[{(0065,{AE 1}XX), */(0065,{AE 2}XX), (2005,{Philips MR Imaging DD 005}02)}]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        sa.apply(dobj);

        String[] modPvtTags = listPvtTags(dobj);
        TestTag p1c = new TestTag(0x00650010, "AE 1");
        TestTag p2c = new TestTag(0x00650011, "AE 2");
        TestTag p1 = new TestTag(0x00651010, "hello there");
        TestTag p2 = new TestTag(0x00651120, "hello again");
        TestSeqTag t1_0_p1c = new TestSeqTag(new int[]{0x00700311,0,0x00650011}, "AE 2");
        TestSeqTag t1_0_p1p1 = new TestSeqTag(new int[]{0x00700311,0,0x00651120}, "inner again");
        TestTag p3 = new TestTag(0x20050014, "Philips MR Imaging DD 005");
        TestSeqTag p1_0_t1 = new TestSeqTag(new int[]{0x20051402,0,0x00080100}, "");
        TestSeqTag p1_0_t2 = new TestSeqTag(new int[]{0x20051402,0,0x00080102}, "");
        TestSeqTag p1_0_t3 = new TestSeqTag(new int[]{0x20051402,0,0x00080103}, "");
        TestSeqTag p1_0_t4 = new TestSeqTag(new int[]{0x20051402,0,0x00080104}, "");
        TestSeqTag p1_0_t5 = new TestSeqTag(new int[]{0x20051402,0,0x0008010b}, "");
        TestSeqTag p1_0_t6 = new TestSeqTag(new int[]{0x20051402,0,0x0008010d}, "");

//        assertEquals(13, modPvtTags.length);
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p2c.tag));
        assertTrue(dobj.contains(p1.tag));
        assertTrue(dobj.contains(p2.tag));
        assertTrue(dobj.contains(t1_0_p1c.tag));
        assertTrue(dobj.contains(t1_0_p1p1.tag));

        assertTrue(dobj.contains(p3.tag));
        assertTrue(dobj.contains(p1_0_t1.tag));
        assertTrue(dobj.contains(p1_0_t2.tag));
        assertTrue(dobj.contains(p1_0_t3.tag));
        assertTrue(dobj.contains(p1_0_t4.tag));
        assertTrue(dobj.contains(p1_0_t5.tag));
        assertTrue(dobj.contains(p1_0_t6.tag));
    }

    public String[] listAllTags(DicomObjectI dobj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream os = new PrintStream( baos)) {
            DumpDicomTagVisitor visitor = new DumpDicomTagVisitor(os);
            visitor.visit(dobj);
        }
        return baos.toString().split("\\R");
    }

    public String[] listPvtTags(DicomObjectI dobj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream os = new PrintStream( baos)) {
            DicomObjectTagVisitor visitor = new DumpDicomPvtTagVisitor(os);
            visitor.visit(dobj);
        }
        return baos.toString().split("\\R");
    }

    public int numberOfPvtTags(DicomObjectI dobj) {
        String[] strings = listPvtTags( dobj);
        if( strings.length == 1) {
            return (strings[0].isEmpty())? 0: 1;
        }
        return strings.length;
    }

    /**
     * 1. Private sequence tag with items with only public tags. Retain all.
     * 2. List argument with single private tagPath.
     *
     * retainPrivateTags[{(2005,{Philips MR Imaging DD 005}02)[%]/*}]
     * <pre>
     * (2005,0010) LO #26 [Philips MR Imaging DD 005] Private Creator Data Element   retain
     * (2005,1001) UN #4 [gone] ?                                                    delete
     * (2005,1002) SQ #-1 [2 items] ?                                                retain
     * >ITEM #1:
     * >(0008,0100) SH #10 [CodeValue] Code Value                                    retain
     * >(0008,0102) SH #22 [CodingSchemeDesignator] Coding Scheme Designator         retain
     * >(0008,0104) LO #12 [CodeMeaning] Code Meaning                                retain
     * >ITEM #2:
     * >(0008,0100) SH #10 [CodeValue] Code Value                                    retain
     * >(0008,0102) SH #22 [CodingSchemeDesignator] Coding Scheme Designator         retain
     * >(0008,0104) LO #12 [CodeMeaning] Code Meaning                                retain
     * </pre>
     *
     * @throws MizerException
     */
    @Test
    public void publicInPvtSeq() throws MizerException {
        String script = "retainPrivateTags[{(2005,{Philips MR Imaging DD 005}02)}]\n";
        TestTag p1c = new TestTag(0x20050010, "Philips MR Imaging DD 005");
        TestTag p1 = new TestTag(0x20051001, "gone");
        TestSeqTag p1_0_t1 = new TestSeqTag(new int[]{0x20051002,0,0x00080100}, "CodeValue");
        TestSeqTag p1_0_t2 = new TestSeqTag(new int[]{0x20051002,0,0x00080102}, "CodingSchemeDesignator");
        TestSeqTag p1_0_t3 = new TestSeqTag(new int[]{0x20051002,0,0x00080104}, "CodeMeaning");
        TestSeqTag p1_1_t1 = new TestSeqTag(new int[]{0x20051002,1,0x00080100}, "CodeValue");
        TestSeqTag p1_1_t2 = new TestSeqTag(new int[]{0x20051002,1,0x00080102}, "CodingSchemeDesignator");
        TestSeqTag p1_1_t3 = new TestSeqTag(new int[]{0x20051002,1,0x00080104}, "CodeMeaning");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, p1c);
        put(dobj, p1);
        put(dobj, p1_0_t1);
        put(dobj, p1_0_t2);
        put(dobj, p1_0_t3);
        put(dobj, p1_1_t1);
        put(dobj, p1_1_t2);
        put(dobj, p1_1_t3);

        assertEquals(p1c.initialValue, dobj.getString(p1c.tag));
        assertEquals(p1.initialValue, dobj.getString(p1.tag));
        assertEquals(p1_0_t1.initialValue, dobj.getString(p1_0_t1.tag));
        assertEquals(p1_0_t2.initialValue, dobj.getString(p1_0_t2.tag));
        assertEquals(p1_0_t3.initialValue, dobj.getString(p1_0_t3.tag));
        assertEquals(p1_1_t1.initialValue, dobj.getString(p1_0_t1.tag));
        assertEquals(p1_1_t2.initialValue, dobj.getString(p1_0_t2.tag));
        assertEquals(p1_1_t3.initialValue, dobj.getString(p1_0_t3.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals(p1c.postValue, dobj.getString(p1c.tag));
        assertFalse( dobj.contains(p1.tag));
        assertEquals(p1_0_t1.postValue, dobj.getString(p1_0_t1.tag));
        assertEquals(p1_0_t2.postValue, dobj.getString(p1_0_t2.tag));
        assertEquals(p1_0_t3.postValue, dobj.getString(p1_0_t3.tag));
        assertEquals(p1_1_t1.postValue, dobj.getString(p1_1_t1.tag));
        assertEquals(p1_1_t2.postValue, dobj.getString(p1_1_t2.tag));
        assertEquals(p1_1_t3.postValue, dobj.getString(p1_1_t3.tag));
    }

    /**
     * 1. Private sequence tag with items with only public tags.
     * 2. Retain a specific item.
     * 3. List argument with single private tagPath.
     *
     * Note: Empty item not removed.
     *
     * retainPrivateTags[{(2005,{Philips MR Imaging DD 005}02)[1]/*}]
     * <pre>
     * (2005,0010) LO #26 [Philips MR Imaging DD 005] Private Creator Data Element   retain
     * (2005,1001) UN #4 [gone] ?                                                    delete
     * (2005,1002) SQ #-1 [2 items] ?                                                retain
     * >ITEM #1:
     * >(0008,0100) SH #10 [CodeValue] Code Value                                    delete
     * >(0008,0102) SH #22 [CodingSchemeDesignator] Coding Scheme Designator         delete
     * >(0008,0104) LO #12 [CodeMeaning] Code Meaning                                delete
     * >ITEM #2:
     * >(0008,0100) SH #10 [CodeValue] Code Value                                    retain
     * >(0008,0102) SH #22 [CodingSchemeDesignator] Coding Scheme Designator         retain
     * >(0008,0104) LO #12 [CodeMeaning] Code Meaning                                retain
     * </pre>
     *
     * @throws MizerException
     */
    @Test
    public void singleItemInPvtSeq() throws MizerException {
        String script = "retainPrivateTags[{(2005,{Philips MR Imaging DD 005}02)[1]}]\n";
        TestTag p1c = new TestTag(0x20050010, "Philips MR Imaging DD 005");
        TestTag p1 = new TestTag(0x20051001, "gone");
        TestSeqTag p1_0_t1 = new TestSeqTag(new int[]{0x20051002,0,0x00080100}, "CodeValue1");
        TestSeqTag p1_0_t2 = new TestSeqTag(new int[]{0x20051002,0,0x00080102}, "CodingSchemeDesignator1");
        TestSeqTag p1_0_t3 = new TestSeqTag(new int[]{0x20051002,0,0x00080104}, "CodeMeaning1");
        TestSeqTag p1_1_t1 = new TestSeqTag(new int[]{0x20051002,1,0x00080100}, "CodeValue2");
        TestSeqTag p1_1_t2 = new TestSeqTag(new int[]{0x20051002,1,0x00080102}, "CodingSchemeDesignator2");
        TestSeqTag p1_1_t3 = new TestSeqTag(new int[]{0x20051002,1,0x00080104}, "CodeMeaning2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, p1c);
        put(dobj, p1);
        put(dobj, p1_0_t1);
        put(dobj, p1_0_t2);
        put(dobj, p1_0_t3);
        put(dobj, p1_1_t1);
        put(dobj, p1_1_t2);
        put(dobj, p1_1_t3);

        assertTrue( dobj.contains(p1c.tag));
        assertTrue( dobj.contains(p1.tag));
        assertTrue( dobj.contains(p1_0_t1.tag));
        assertTrue( dobj.contains(p1_0_t2.tag));
        assertTrue( dobj.contains(p1_0_t3.tag));
        assertTrue( dobj.contains(p1_1_t1.tag));
        assertTrue( dobj.contains(p1_1_t2.tag));
        assertTrue( dobj.contains(p1_1_t3.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue( dobj.contains(p1c.tag));
        assertFalse( dobj.contains(p1.tag));
        assertFalse( dobj.contains(p1_0_t1.tag));
        assertFalse( dobj.contains(p1_0_t2.tag));
        assertFalse( dobj.contains(p1_0_t3.tag));
        assertTrue( dobj.contains(p1_1_t1.tag));
        assertTrue( dobj.contains(p1_1_t2.tag));
        assertTrue( dobj.contains(p1_1_t3.tag));
    }

    @Test
    public void pvtInPvtSeqImplicitSequence() throws MizerException {
        TestTag p1c = new TestTag(0x20050010, "Philips MR Imaging DD 005");
        TestTag p1 = new TestTag(0x20051001, "gone");
        TestSeqTag p1_0_p1c = new TestSeqTag(new int[]{0x20051002,0,0x20050013}, "Internal Pvt");
        TestSeqTag p1_0_p1t1 = new TestSeqTag(new int[]{0x20051002,0,0x20051301}, "p1_0_p1t1");
        TestSeqTag p1_0_p1t2 = new TestSeqTag(new int[]{0x20051002,0,0x20051302}, "p1_0_p1t2");
        TestSeqTag p1_0_p1t3 = new TestSeqTag(new int[]{0x20051002,0,0x20051303}, "p1_0_p1t3");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, p1c);
        put(dobj, p1);
        put(dobj, p1_0_p1c);
        put(dobj, p1_0_p1t1);
        put(dobj, p1_0_p1t2);
        put(dobj, p1_0_p1t3);

        assertEquals(p1c.initialValue, dobj.getString(p1c.tag));
        assertEquals(p1.initialValue, dobj.getString(p1.tag));
        assertEquals(p1_0_p1c.initialValue, dobj.getString(p1_0_p1c.tag));
        assertEquals(p1_0_p1t1.initialValue, dobj.getString(p1_0_p1t1.tag));
        assertEquals(p1_0_p1t2.initialValue, dobj.getString(p1_0_p1t2.tag));
        assertEquals(p1_0_p1t3.initialValue, dobj.getString(p1_0_p1t3.tag));

        String script = "retainPrivateTags[{\"(2005,{Philips MR Imaging DD 005}02)\"}]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals(p1c.postValue, dobj.getString(p1c.tag));
        assertFalse( dobj.contains(p1.tag));
        assertEquals(p1_0_p1c.postValue, dobj.getString(p1_0_p1c.tag));
        assertEquals(p1_0_p1t1.postValue, dobj.getString(p1_0_p1t1.tag));
        assertEquals(p1_0_p1t2.postValue, dobj.getString(p1_0_p1t2.tag));
        assertEquals(p1_0_p1t3.postValue, dobj.getString(p1_0_p1t3.tag));
    }

    @Test
    public void pvtInPvtSeqExplicitSequence() throws MizerException {
        TestTag p1c = new TestTag(0x20050010, "Philips MR Imaging DD 005");
        TestTag p1 = new TestTag(0x20051001, "gone");
        TestSeqTag p1_0_p1c = new TestSeqTag(new int[]{0x20051002,0,0x20050013}, "Internal Pvt");
        TestSeqTag p1_0_p1t1 = new TestSeqTag(new int[]{0x20051002,0,0x20051301}, "p1_0_p1t1");
        TestSeqTag p1_0_p1t2 = new TestSeqTag(new int[]{0x20051002,0,0x20051302}, "p1_0_p1t2");
        TestSeqTag p1_0_p1t3 = new TestSeqTag(new int[]{0x20051002,0,0x20051303}, "p1_0_p1t3");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, p1c);
        put(dobj, p1);
        put(dobj, p1_0_p1c);
        put(dobj, p1_0_p1t1);
        put(dobj, p1_0_p1t2);
        put(dobj, p1_0_p1t3);

        assertEquals(p1c.initialValue, dobj.getString(p1c.tag));
        assertEquals(p1.initialValue, dobj.getString(p1.tag));
        assertEquals(p1_0_p1c.initialValue, dobj.getString(p1_0_p1c.tag));
        assertEquals(p1_0_p1t1.initialValue, dobj.getString(p1_0_p1t1.tag));
        assertEquals(p1_0_p1t2.initialValue, dobj.getString(p1_0_p1t2.tag));
        assertEquals(p1_0_p1t3.initialValue, dobj.getString(p1_0_p1t3.tag));

        String script = "retainPrivateTags[{\"(2005,{Philips MR Imaging DD 005}02)\"}]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals(p1c.postValue, dobj.getString(p1c.tag));
        assertFalse( dobj.contains(p1.tag));
        assertEquals(p1_0_p1c.postValue, dobj.getString(p1_0_p1c.tag));
        assertEquals(p1_0_p1t1.postValue, dobj.getString(p1_0_p1t1.tag));
        assertEquals(p1_0_p1t2.postValue, dobj.getString(p1_0_p1t2.tag));
        assertEquals(p1_0_p1t3.postValue, dobj.getString(p1_0_p1t3.tag));
    }

    @Test
    public void goDeep() throws MizerException {
        TestTag p1c                              = new TestTag(0x20050010, "pc id");
        TestTag p1                               = new TestTag(0x20051001, "gone");
        TestSeqTag p1_0_p1c                      = new TestSeqTag(new int[]{0x20051002,0,0x20050013}, "Internal Pvt1");
        TestSeqTag p1_0_p1                       = new TestSeqTag(new int[]{0x20051002,0,0x20051301}, "gone");
        TestSeqTag p1_0_p1t1_0_p1c               = new TestSeqTag(new int[]{0x20051002,0,0x200513aa,0,0x20050013}, "Internal Pvt2");
        TestSeqTag p1_0_p1t1_0_p1                = new TestSeqTag(new int[]{0x20051002,0,0x200513aa,0,0x20051303}, "gone");
        TestSeqTag p1_0_p1t1_0_p1p1_0_p1c        = new TestSeqTag(new int[]{0x20051002,0,0x200513aa,0,0x200513aa,0,0x20050013}, "Internal Pvt3");
        TestSeqTag p1_0_p1t1_0_p1p1_0_p1         = new TestSeqTag(new int[]{0x20051002,0,0x200513aa,0,0x200513aa,0,0x20051304}, "bye bye");
        TestSeqTag p1_0_p1t1_0_p1p1_0_p1p1_0_p1c = new TestSeqTag(new int[]{0x20051002,0,0x200513aa,0,0x200513aa,0,0x200513aa,0,0x20050013}, "Internal Pvt4");
        TestSeqTag p1_0_p1t1_0_p1p1_0_p1p1_0_p1  = new TestSeqTag(new int[]{0x20051002,0,0x200513aa,0,0x200513aa,0,0x200513aa,0,0x20051305}, "no more");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, p1c);
        put(dobj, p1_0_p1c);
        put(dobj, p1_0_p1t1_0_p1c);
        put(dobj, p1_0_p1t1_0_p1p1_0_p1c);
        put(dobj, p1_0_p1t1_0_p1p1_0_p1p1_0_p1c);
        put(dobj, p1);
        put(dobj, p1_0_p1);
        put(dobj, p1_0_p1t1_0_p1);
        put(dobj, p1_0_p1t1_0_p1p1_0_p1);
        put(dobj, p1_0_p1t1_0_p1p1_0_p1p1_0_p1);

        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1_0_p1c.tag));
        assertTrue(dobj.contains(p1_0_p1t1_0_p1c.tag));
        assertTrue(dobj.contains(p1_0_p1t1_0_p1p1_0_p1c.tag));
        assertTrue(dobj.contains(p1_0_p1t1_0_p1p1_0_p1p1_0_p1c.tag));
        assertTrue(dobj.contains(p1.tag));
        assertTrue(dobj.contains(p1_0_p1.tag));
        assertTrue(dobj.contains(p1_0_p1t1_0_p1.tag));
        assertTrue(dobj.contains(p1_0_p1t1_0_p1p1_0_p1.tag));
        assertTrue(dobj.contains(p1_0_p1t1_0_p1p1_0_p1p1_0_p1.tag));

        String script = "retainPrivateTags[{\"(2005,{FICTION}02)\"}]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertFalse(dobj.contains(p1c.tag));
        assertFalse(dobj.contains(p1_0_p1c.tag));
        assertFalse(dobj.contains(p1_0_p1t1_0_p1c.tag));
        assertFalse(dobj.contains(p1_0_p1t1_0_p1p1_0_p1c.tag));
        assertFalse(dobj.contains(p1_0_p1t1_0_p1p1_0_p1p1_0_p1c.tag));
        assertFalse(dobj.contains(p1.tag));
        assertFalse(dobj.contains(p1_0_p1.tag));
        assertFalse(dobj.contains(p1_0_p1t1_0_p1.tag));
        assertFalse(dobj.contains(p1_0_p1t1_0_p1p1_0_p1.tag));
        assertFalse(dobj.contains(p1_0_p1t1_0_p1p1_0_p1p1_0_p1.tag));
    }

    /**
     *
     * (2005,0010) LO #4 [p1c] Private Creator Data Element    retain
     * (2005,10BB) UN #4 [p1p3] ?                              delete
     * (2005,10CC) UN #4 [p1p1] ?                              retain
     * (2005,10DD) SQ #-1 [1 item] ?                           retain
     * >ITEM #1:
     * >(2005,0010) LO #4 [p1c] Private Creator Data Element   retain
     * >(2005,10BB) UN #12 [p1p2_0_p1p3] ?                     retain
     * >(2005,10CC) UN #12 [p1p2_0_p1p1] ?                     retain
     * >(2005,10DD) SQ #-1 [1 item] ?                          retain
     * >>ITEM #1:
     * >>(2005,0010) LO #4 [p1c] Private Creator Data Element  retain
     * >>(2005,10BB) UN #18 [p1p2_0_p1p2_0_p1p2] ?             retain
     * >>(2005,10CC) UN #18 [p1p2_0_p1p2_0_p1p1] ?             retain
     * >>(2005,10DD) UN #18 [p1p2_0_p1p2_0_p1p2] ?             retain
     *
     * @throws MizerException
     */
    @Test
    public void testSequenceDot() throws MizerException {
        TestTag p1c = new TestTag(0x20050010, "p1c");
        TestTag p1p3 = new TestTag(0x200510BB, "p1p3");
        TestTag p1p1 = new TestTag(0x200510CC, "p1p1");
        TestTag p1p2 = new TestTag(0x200510DD, "p1p2");
        TestSeqTag p1p2_0_p1c = new TestSeqTag(new int[]{0x200510DD, 0, 0x20050010}, "p1c");
        TestSeqTag p1p2_0_p1p3 = new TestSeqTag(new int[]{0x200510DD, 0, 0x200510BB}, "p1p2_0_p1p3");
        TestSeqTag p1p2_0_p1p1 = new TestSeqTag(new int[]{0x200510DD, 0, 0x200510CC}, "p1p2_0_p1p1");
        TestSeqTag p1p2_0_p1p2 = new TestSeqTag(new int[]{0x200510DD, 0, 0x200510DD}, "p1p2_0_p1p2");
        TestSeqTag p1p2_0_p1p2_0_p1c = new TestSeqTag(new int[]{0x200510DD, 0, 0x200510DD, 0, 0x20050010}, "p1c");
        TestSeqTag p1p2_0_p1p2_0_p1p3 = new TestSeqTag(new int[]{0x200510DD, 0, 0x200510DD, 0, 0x200510BB}, "p1p2_0_p1p2_0_p1p2");
        TestSeqTag p1p2_0_p1p2_0_p1p1 = new TestSeqTag(new int[]{0x200510DD, 0, 0x200510DD, 0, 0x200510CC}, "p1p2_0_p1p2_0_p1p1");
        TestSeqTag p1p2_0_p1p2_0_p1p2 = new TestSeqTag(new int[]{0x200510DD, 0, 0x200510DD, 0, 0x200510DD}, "p1p2_0_p1p2_0_p1p2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, p1c);
        put(dobj, p1p1);
        put(dobj, p1p2);
        put(dobj, p1p3);
        put(dobj, p1p2_0_p1c);
        put(dobj, p1p2_0_p1p1);
        put(dobj, p1p2_0_p1p2);
        put(dobj, p1p2_0_p1p3);
        put(dobj, p1p2_0_p1p2_0_p1c);
        put(dobj, p1p2_0_p1p2_0_p1p1);
        put(dobj, p1p2_0_p1p2_0_p1p2);
        put(dobj, p1p2_0_p1p2_0_p1p3);

        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1p1.tag));
        assertTrue(dobj.contains(p1p2.tag));
        assertTrue(dobj.contains(p1p3.tag));
        assertTrue(dobj.contains(p1p2_0_p1c.tag));
        assertTrue(dobj.contains(p1p2_0_p1p1.tag));
        assertTrue(dobj.contains(p1p2_0_p1p2.tag));
        assertTrue(dobj.contains(p1p2_0_p1p3.tag));
        assertTrue(dobj.contains(p1p2_0_p1p2_0_p1c.tag));
        assertTrue(dobj.contains(p1p2_0_p1p2_0_p1p1.tag));
        assertTrue(dobj.contains(p1p2_0_p1p2_0_p1p2.tag));
        assertTrue(dobj.contains(p1p2_0_p1p2_0_p1p3.tag));

        String script = "retainPrivateTags[{\"./(2005,{p1c}BB)\"}]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue(dobj.contains(p1c.tag));
        assertFalse(dobj.contains(p1p1.tag));
        assertTrue(dobj.contains(p1p2.tag));
        assertFalse(dobj.contains(p1p3.tag));
        assertTrue(dobj.contains(p1p2_0_p1c.tag));
        assertFalse(dobj.contains(p1p2_0_p1p1.tag));
        assertFalse(dobj.contains(p1p2_0_p1p2.tag));
        assertTrue(dobj.contains(p1p2_0_p1p3.tag));
        assertFalse(dobj.contains(p1p2_0_p1p2_0_p1c.tag));
        assertFalse(dobj.contains(p1p2_0_p1p2_0_p1p1.tag));
        assertFalse(dobj.contains(p1p2_0_p1p2_0_p1p2.tag));
        assertFalse(dobj.contains(p1p2_0_p1p2_0_p1p3.tag));
    }

    @Test
    public void testSequencePlus() throws MizerException {
        TestTag p1id                        = new TestTag(0x20050010, "p1id");
        TestTag p1b                         = new TestTag(0x200510BB, "p1b");
        TestTag p1c                         = new TestTag(0x200510CC, "p1c");
        TestTag p1d                         = new TestTag(0x200510DD, "p1d");
        TestSeqTag p1d_0_p1id       = new TestSeqTag(new int[]{0x200510DD,0,0x20050010}, "p1id");
        TestSeqTag p1d_0_p1b        = new TestSeqTag(new int[]{0x200510DD,0,0x200510BB}, "p1d_0_p1b");
        TestSeqTag p1d_0_p1c        = new TestSeqTag(new int[]{0x200510DD,0,0x200510CC}, "p1d_0_p1c");
        TestSeqTag p1d_0_p1d        = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD}, "p1d_0_p1d");
        TestSeqTag p1d_0_p1d_0_p1id = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,0,0x20050010}, "p1id");
        TestSeqTag p1d_0_p1d_0_p1b  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,0,0x200510BB}, "p1d_0_p1d_0_p1b");
        TestSeqTag p1d_0_p1d_0_p1c  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,0,0x200510CC}, "p1d_0_p1d_0_p1c");
        TestSeqTag p1d_0_p1d_1_p1id = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,1,0x20050010}, "p1id");
        TestSeqTag p1d_0_p1d_1_p1b  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,1,0x200510BB}, "p1d_0_p1d_1_p1b");
        TestSeqTag p1d_0_p1d_1_p1c  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,1,0x200510CC}, "p1d_0_p1d_1_p1c");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, p1id);
        put(dobj, p1b);
        put(dobj, p1c);
        put(dobj, p1d);
        put(dobj, p1d_0_p1id);
        put(dobj, p1d_0_p1b);
        put(dobj, p1d_0_p1c);
        put(dobj, p1d_0_p1d);
        put(dobj, p1d_0_p1d_0_p1id);
        put(dobj, p1d_0_p1d_0_p1b);
        put(dobj, p1d_0_p1d_0_p1c);
        put(dobj, p1d_0_p1d_1_p1id);
        put(dobj, p1d_0_p1d_1_p1b);
        put(dobj, p1d_0_p1d_1_p1c);

        assertTrue(dobj.contains(p1id.tag));
        assertTrue(dobj.contains(p1b.tag));
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1c.tag));

        String script = "retainPrivateTags[{\"+/(2005,{p1id}BB)\"}]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue(dobj.contains(p1id.tag));
        assertFalse(dobj.contains(p1b.tag));
        assertFalse(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1d_1_p1c.tag));
    }

    @Test
    public void testSequenceTopLevelOnly() throws MizerException {
        TestTag p1id                        = new TestTag(0x20050010, "p1id");
        TestTag p1b                         = new TestTag(0x200510BB, "p1b");
        TestTag p1c                         = new TestTag(0x200510CC, "p1c");
        TestTag p1d                         = new TestTag(0x200510DD, "p1d");
        TestSeqTag p1d_0_p1id       = new TestSeqTag(new int[]{0x200510DD,0,0x20050010}, "p1id");
        TestSeqTag p1d_0_p1b        = new TestSeqTag(new int[]{0x200510DD,0,0x200510BB}, "p1d_0_p1b");
        TestSeqTag p1d_0_p1c        = new TestSeqTag(new int[]{0x200510DD,0,0x200510CC}, "p1d_0_p1c");
        TestSeqTag p1d_0_p1d        = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD}, "p1d_0_p1d");
        TestSeqTag p1d_0_p1d_0_p1id = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,0,0x20050010}, "p1id");
        TestSeqTag p1d_0_p1d_0_p1b  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,0,0x200510BB}, "p1d_0_p1d_0_p1b");
        TestSeqTag p1d_0_p1d_0_p1c  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,0,0x200510CC}, "p1d_0_p1d_0_p1c");
        TestSeqTag p1d_0_p1d_1_p1id = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,1,0x20050010}, "p1id");
        TestSeqTag p1d_0_p1d_1_p1b  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,1,0x200510BB}, "p1d_0_p1d_1_p1b");
        TestSeqTag p1d_0_p1d_1_p1c  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,1,0x200510CC}, "p1d_0_p1d_1_p1c");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, p1id);
        put(dobj, p1b);
        put(dobj, p1c);
        put(dobj, p1d);
        put(dobj, p1d_0_p1id);
        put(dobj, p1d_0_p1b);
        put(dobj, p1d_0_p1c);
        put(dobj, p1d_0_p1d);
        put(dobj, p1d_0_p1d_0_p1id);
        put(dobj, p1d_0_p1d_0_p1b);
        put(dobj, p1d_0_p1d_0_p1c);
        put(dobj, p1d_0_p1d_1_p1id);
        put(dobj, p1d_0_p1d_1_p1b);
        put(dobj, p1d_0_p1d_1_p1c);

        assertTrue(dobj.contains(p1id.tag));
        assertTrue(dobj.contains(p1b.tag));
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1c.tag));

        String script = "retainPrivateTags[{\"(2005,{p1id}BB)\"}]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue(dobj.contains(p1id.tag));
        assertTrue(dobj.contains(p1b.tag));
        assertFalse(dobj.contains(p1c.tag));
        assertFalse(dobj.contains(p1d.tag));
        assertFalse(dobj.contains(p1d_0_p1id.tag));
        assertFalse(dobj.contains(p1d_0_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1c.tag));
        assertFalse(dobj.contains(p1d_0_p1d.tag));
        assertFalse(dobj.contains(p1d_0_p1d_0_p1id.tag));
        assertFalse(dobj.contains(p1d_0_p1d_0_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1d_0_p1c.tag));
        assertFalse(dobj.contains(p1d_0_p1d_1_p1id.tag));
        assertFalse(dobj.contains(p1d_0_p1d_1_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1d_1_p1c.tag));
    }

    @Test
    public void testSequenceSplat() throws MizerException {
        TestTag p1id                        = new TestTag(0x20050010, "p1id");
        TestTag p1b                         = new TestTag(0x200510BB, "p1b");
        TestTag p1c                         = new TestTag(0x200510CC, "p1c");
        TestTag p1d                         = new TestTag(0x200510DD, "p1d");
        TestSeqTag p1d_0_p1id       = new TestSeqTag(new int[]{0x200510DD,0,0x20050010}, "p1id");
        TestSeqTag p1d_0_p1b        = new TestSeqTag(new int[]{0x200510DD,0,0x200510BB}, "p1d_0_p1b");
        TestSeqTag p1d_0_p1c        = new TestSeqTag(new int[]{0x200510DD,0,0x200510CC}, "p1d_0_p1c");
        TestSeqTag p1d_0_p1d        = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD}, "p1d_0_p1d");
        TestSeqTag p1d_0_p1d_0_p1id = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,0,0x20050010}, "p1id");
        TestSeqTag p1d_0_p1d_0_p1b  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,0,0x200510BB}, "p1d_0_p1d_0_p1b");
        TestSeqTag p1d_0_p1d_0_p1c  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,0,0x200510CC}, "p1d_0_p1d_0_p1c");
        TestSeqTag p1d_0_p1d_1_p1id = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,1,0x20050010}, "p1id");
        TestSeqTag p1d_0_p1d_1_p1b  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,1,0x200510BB}, "p1d_0_p1d_1_p1b");
        TestSeqTag p1d_0_p1d_1_p1c  = new TestSeqTag(new int[]{0x200510DD,0,0x200510DD,1,0x200510CC}, "p1d_0_p1d_1_p1c");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, p1id);
        put(dobj, p1b);
        put(dobj, p1c);
        put(dobj, p1d);
        put(dobj, p1d_0_p1id);
        put(dobj, p1d_0_p1b);
        put(dobj, p1d_0_p1c);
        put(dobj, p1d_0_p1d);
        put(dobj, p1d_0_p1d_0_p1id);
        put(dobj, p1d_0_p1d_0_p1b);
        put(dobj, p1d_0_p1d_0_p1c);
        put(dobj, p1d_0_p1d_1_p1id);
        put(dobj, p1d_0_p1d_1_p1b);
        put(dobj, p1d_0_p1d_1_p1c);

        assertTrue(dobj.contains(p1id.tag));
        assertTrue(dobj.contains(p1b.tag));
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1c.tag));

        String script = "retainPrivateTags[{\"*/(2005,{p1id}BB)\"}]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue(dobj.contains(p1id.tag));
        assertTrue(dobj.contains(p1b.tag));
        assertFalse(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1d_1_p1c.tag));
    }

    @Test
    public void testDontChangePrivateBLocks() throws MizerException {
        TestTag p1id                        = new TestTag(0x20050014, "p1id");
        TestTag p1b                         = new TestTag(0x200514BB, "p1b");
        TestTag p1c                         = new TestTag(0x200514CC, "p1c");
        TestTag p1d                         = new TestTag(0x200514DD, "p1d");
        TestSeqTag p1d_0_p1id       = new TestSeqTag(new int[]{0x200514DD,0,0x20050011}, "p1id");
        TestSeqTag p1d_0_p1b        = new TestSeqTag(new int[]{0x200514DD,0,0x200511BB}, "p1d_0_p1b");
        TestSeqTag p1d_0_p1c        = new TestSeqTag(new int[]{0x200514DD,0,0x200511CC}, "p1d_0_p1c");
        TestSeqTag p1d_0_p1d        = new TestSeqTag(new int[]{0x200514DD,0,0x200511DD}, "p1d_0_p1d");
        TestSeqTag p1d_0_p1d_0_p1id = new TestSeqTag(new int[]{0x200514DD,0,0x200511DD,0,0x20050013}, "p1id");
        TestSeqTag p1d_0_p1d_0_p1b  = new TestSeqTag(new int[]{0x200514DD,0,0x200511DD,0,0x200513BB}, "p1d_0_p1d_0_p1b");
        TestSeqTag p1d_0_p1d_0_p1c  = new TestSeqTag(new int[]{0x200514DD,0,0x200511DD,0,0x200513CC}, "p1d_0_p1d_0_p1c");
        TestSeqTag p1d_0_p1d_1_p1id = new TestSeqTag(new int[]{0x200514DD,0,0x200511DD,1,0x20050010}, "p1id");
        TestSeqTag p1d_0_p1d_1_p1b  = new TestSeqTag(new int[]{0x200514DD,0,0x200511DD,1,0x200510BB}, "p1d_0_p1d_1_p1b");
        TestSeqTag p1d_0_p1d_1_p1c  = new TestSeqTag(new int[]{0x200514DD,0,0x200511DD,1,0x200510CC}, "p1d_0_p1d_1_p1c");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj, p1id);
        put(dobj, p1b);
        put(dobj, p1c);
        put(dobj, p1d);
        put(dobj, p1d_0_p1id);
        put(dobj, p1d_0_p1b);
        put(dobj, p1d_0_p1c);
        put(dobj, p1d_0_p1d);
        put(dobj, p1d_0_p1d_0_p1id);
        put(dobj, p1d_0_p1d_0_p1b);
        put(dobj, p1d_0_p1d_0_p1c);
        put(dobj, p1d_0_p1d_1_p1id);
        put(dobj, p1d_0_p1d_1_p1b);
        put(dobj, p1d_0_p1d_1_p1c);

        assertTrue(dobj.contains(p1id.tag));
        assertTrue(dobj.contains(p1b.tag));
        assertTrue(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1b.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1c.tag));

        String script = "retainPrivateTags[{\"*/(2005,{p1id}BB)\"}]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        dobj = sa.apply(dobj).getDicomObject();

        assertTrue(dobj.contains(p1id.tag));
        assertTrue(dobj.contains(p1b.tag));
        assertFalse(dobj.contains(p1c.tag));
        assertTrue(dobj.contains(p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_0_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1d_0_p1c.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1id.tag));
        assertTrue(dobj.contains(p1d_0_p1d_1_p1b.tag));
        assertFalse(dobj.contains(p1d_0_p1d_1_p1c.tag));
    }

}
