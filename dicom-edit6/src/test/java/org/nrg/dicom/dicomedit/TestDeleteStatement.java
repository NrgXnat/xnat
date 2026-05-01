/*
 * DicomEdit: TestDeleteStatement
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.nrg.dicom.dicomedit.TestUtils.*;

/**
 * Run tests on Delete statements
 *
 * Attempts to create adequate coverage of the following features:
 *
 * 1. Public and Private Tags
 * 2. Sequences of Public and Private Tags nested to arbitrary levels.
 * 3. Singular Tag Paths. i.e. paths that map to a single element
 * 4. Plural Tag Paths.  i.e. paths with wildcards that map to multiple elements
 *
 * Created by drm on 8/2/16.
 */
public class TestDeleteStatement {

    private static final Logger logger = LoggerFactory.getLogger(TestDeleteStatement.class);

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();

    private static final File f1 = _resourceManager.getTestResourceFile("dicom/seq/000.dcm");
    private static final File de11 = _resourceManager.getTestResourceFile("dicom/IM_0001");

    private static final String S_DELETE = "-(0010,0020)\n";
    private static final String S_DELETE_W_INLINE_COMMENT = "-(0010,0020) // inline comment\n";
    private static final String S_DELETE_W_TERMINAL_COMMENT = "-(0010,0020)\n// inline comment\n";
    private static final String S_DELETE_PVT = "-(0013,{CTP}10)\n";
    private static final String S_DELETE_ENTIRE_SEQ = "-(0012,0064)\n";
    private static final String S_DELETE_SEQ_ITEM = "-(0012,0064)[0]\n";
    private static final String S_DELETE_ELEMENT_IN_SEQ_ITEM = "-(0012,0064)[1]/(0008,0104)\n";
    private static final String S_DELETE_ELEMENT_IN_ALL_SEQ_ITEM = "-(0012,0064)[%]/(0008,0104)\n";
    private static final String S_DELETE_ELEMENT_EVERYWHERE = "- * / (0010, 0010)\n";
    private static final String S_DELETE_ELEMENT_BELOW_FIRST_LEVEL = "- + / (0010, 0010)\n";
    private static final String S_DELETE_ELEMENT_ONLY_SECOND_LEVEL = "- . / (0010, 0010)\n";

    @Test
    public void testDeleteSingleTag() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(f1);
        assertTrue(src_dobj.contains(0x00100020));
        final BaseScriptApplicator s_delete    = BaseScriptApplicator.getInstance( bytes(S_DELETE));
        final DicomObjectI result_dobj = s_delete.apply(src_dobj).getDicomObject();
        assertFalse(result_dobj.contains(0x00100020));
    }

    @Test
    public void testDeleteSingleTagWithInlineComment() throws Exception {
        final DicomObjectI source = DicomObjectFactory.newInstance(f1);
        assertTrue(source.contains(0x00100020));

        final BaseScriptApplicator deleteWithInline    = BaseScriptApplicator.getInstance( bytes(S_DELETE_W_INLINE_COMMENT));
        final DicomObjectI inlineResult = deleteWithInline.apply(f1).getDicomObject();

        final BaseScriptApplicator deleteWithTerminal = BaseScriptApplicator.getInstance( bytes(S_DELETE_W_TERMINAL_COMMENT));
        final DicomObjectI terminalResult = deleteWithTerminal.apply(f1).getDicomObject();

        assertFalse(inlineResult.contains(0x00100020));
        assertFalse(terminalResult.contains(0x00100020));
    }

    @Test
    public void testDeleteSinglePvtTag() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(f1);
        assertTrue(src_dobj.contains(0x00131010));
        final BaseScriptApplicator s_delete = BaseScriptApplicator.getInstance( bytes(S_DELETE_PVT));
        final DicomObjectI result_dobj = s_delete.apply(src_dobj).getDicomObject();
        assertFalse(result_dobj.contains(0x00131110));
    }

    /**
     * deleting a sequence tag deletes the entire sequence.
     * <pre>
     * -(0012,0064)
     *
     * (0012,0064) SQ #-1 [2 items] De-identification Method Code Sequence   delete
     * >ITEM #1 @424:                                                        delete
     * >(0008,0100) SH #6 [item0] Code Value                                 delete
     * >(0008,0102) SH #4 [XNAT] Coding Scheme Designator                    delete
     * >(0008,0103) SH #4 [0.1] Coding Scheme Version                        delete
     * >(0008,0104) LO #16 [XNAT Edit Script] Code Meaning                   delete
     * >ITEM #2 @494:                                                        delete
     * >(0008,0100) SH #6 [item1] Code Value                                 delete
     * >(0008,0102) SH #4 [XNAT] Coding Scheme Designator                    delete
     * >(0008,0103) SH #4 [0.1] Coding Scheme Version                        delete
     * >(0008,0104) LO #16 [XNAT Edit Script] Code Meaning                   delete
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testDeleteSeqAtRoot() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(f1);
        assertTrue(src_dobj.contains(0x00120064));
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_ENTIRE_SEQ));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();
        assertFalse(result_dobj.contains(0x00120064));
    }

    /**
     * deleting an item deletes the contents of the item, but not the item itself.
     * This is DE-26
     *
     * @throws Exception
     */
    @Test
    public void testDeleteSeqItem1() throws Exception {
        final DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestSeqTag t1 = new TestSeqTag( new int[]{0x00189112,0,0x00100010}, "t1");
        TestUtils.put( dobj, t1);

        assertTrue( dobj.contains( 0x00189112));
        assertTrue( dobj.contains(t1.tag));

        String script = "- (0018,9112)[0]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(dobj).getDicomObject();

        assertTrue( dobj.contains( 0x00189112));
        assertFalse( dobj.contains(t1.tag));
    }

    @Test
    public void testDeleteSeqItem() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(f1);
        int[] s = {0x00120064};
        assertTrue( src_dobj.contains(s));
        int[] s00 = {0x00120064,0,0x00080100};
        assertTrue( src_dobj.contains(s00));
        int[] s01 = {0x00120064,0,0x00080102};
        assertTrue( src_dobj.contains(s01));
        int[] s02 = {0x00120064,0,0x00080103};
        assertTrue( src_dobj.contains(s02));
        int[] s03 = {0x00120064,0,0x00080104};
        assertTrue( src_dobj.contains(s03));
        int[] s10 = {0x00120064,1,0x00080100};
        assertTrue( src_dobj.contains(s10));
        int[] s11 = {0x00120064,1,0x00080102};
        assertTrue( src_dobj.contains(s11));
        int[] s12 = {0x00120064,1,0x00080103};
        assertTrue( src_dobj.contains(s12));
        int[] s13 = {0x00120064,1,0x00080104};
        assertTrue( src_dobj.contains(s13));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_SEQ_ITEM));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertTrue( result_dobj.contains(s));
        assertFalse( result_dobj.contains(s00));
        assertFalse( result_dobj.contains(s01));
        assertFalse( result_dobj.contains(s02));
        assertFalse( result_dobj.contains(s03));
        assertTrue( result_dobj.contains(s10));
        assertTrue( result_dobj.contains(s11));
        assertTrue( result_dobj.contains(s12));
        assertTrue( result_dobj.contains(s13));
    }

    /**
     * delete a tag in a sequence item.
     * <pre>
     * -(0012,0064)[1]/(0008,0104)
     *
     * (0012,0064) SQ #-1 [2 items] De-identification Method Code Sequence   retain
     * >ITEM #1 @424:                                                        retain
     * >(0008,0100) SH #6 [item0] Code Value                                 retain
     * >(0008,0102) SH #4 [XNAT] Coding Scheme Designator                    retain
     * >(0008,0103) SH #4 [0.1] Coding Scheme Version                        retain
     * >(0008,0104) LO #16 [XNAT Edit Script] Code Meaning                   retain
     * >ITEM #2 @494:                                                        retain
     * >(0008,0100) SH #6 [item1] Code Value                                 retain
     * >(0008,0102) SH #4 [XNAT] Coding Scheme Designator                    retain
     * >(0008,0103) SH #4 [0.1] Coding Scheme Version                        retain
     * >(0008,0104) LO #16 [XNAT Edit Script] Code Meaning                   delete
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testDeleteSeqItemElement() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(f1);

        int[] s = {0x00120064};
        assertTrue( src_dobj.contains(s));
        int[] s00 = {0x00120064,0,0x00080100};
        assertTrue( src_dobj.contains(s00));
        int[] s01 = {0x00120064,0,0x00080102};
        assertTrue( src_dobj.contains(s01));
        int[] s02 = {0x00120064,0,0x00080103};
        assertTrue( src_dobj.contains(s02));
        int[] s03 = {0x00120064,0,0x00080104};
        assertTrue( src_dobj.contains(s03));
        int[] s10 = {0x00120064,1,0x00080100};
        assertTrue( src_dobj.contains(s10));
        int[] s11 = {0x00120064,1,0x00080102};
        assertTrue( src_dobj.contains(s11));
        int[] s12 = {0x00120064,1,0x00080103};
        assertTrue( src_dobj.contains(s12));
        int[] s13 = {0x00120064,1,0x00080104};
        assertTrue( src_dobj.contains(s13));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_ELEMENT_IN_SEQ_ITEM));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertTrue( result_dobj.contains(s));
        assertTrue( result_dobj.contains(s00));
        assertTrue( result_dobj.contains(s01));
        assertTrue( result_dobj.contains(s02));
        assertTrue( result_dobj.contains(s03));
        assertTrue( result_dobj.contains(s10));
        assertTrue( result_dobj.contains(s11));
        assertTrue( result_dobj.contains(s12));
        assertFalse( result_dobj.contains(s13));
    }

    @Test
    public void testDE11() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(de11);
        int[] s = {0x00080100};
        assertTrue( src_dobj.contains(s));
        int[] s1 = {0x00180026,0,0x00180029,0,0x00080100};
        assertTrue( src_dobj.contains(s1));
        int[] s2 = {0x00400260,0,0x00080100};
        assertTrue( src_dobj.contains(s2));
        int[] s3 = {0x20051402,0,0x00080100};
        assertTrue( src_dobj.contains(s3));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes("- * / (0008, 0100)\n"));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertFalse( result_dobj.contains(s));
        assertFalse( result_dobj.contains(s1));
        assertFalse( result_dobj.contains(s2));
        assertFalse( result_dobj.contains(s3));
    }

    /**
     * delete a tag in all sequence items.
     * <pre>
     * -(0012,0064)[%]/(0008,0104)
     *
     * (0012,0064) SQ #-1 [2 items] De-identification Method Code Sequence   retain
     * >ITEM #1 @424:                                                        retain
     * >(0008,0100) SH #6 [item0] Code Value                                 retain
     * >(0008,0102) SH #4 [XNAT] Coding Scheme Designator                    retain
     * >(0008,0103) SH #4 [0.1] Coding Scheme Version                        retain
     * >(0008,0104) LO #16 [XNAT Edit Script] Code Meaning                   delete
     * >ITEM #2 @494:                                                        retain
     * >(0008,0100) SH #6 [item1] Code Value                                 retain
     * >(0008,0102) SH #4 [XNAT] Coding Scheme Designator                    retain
     * >(0008,0103) SH #4 [0.1] Coding Scheme Version                        retain
     * >(0008,0104) LO #16 [XNAT Edit Script] Code Meaning                   delete
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testDeleteSeqAllItemElement() throws Exception {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(f1);
        int[] s = {0x00120064};
        assertTrue( src_dobj.contains(s));
        int[] s00 = {0x00120064,0,0x00080100};
        assertTrue( src_dobj.contains(s00));
        int[] s01 = {0x00120064,0,0x00080102};
        assertTrue( src_dobj.contains(s01));
        int[] s02 = {0x00120064,0,0x00080103};
        assertTrue( src_dobj.contains(s02));
        int[] s03 = {0x00120064,0,0x00080104};
        assertTrue( src_dobj.contains(s03));
        int[] s10 = {0x00120064,1,0x00080100};
        assertTrue( src_dobj.contains(s10));
        int[] s11 = {0x00120064,1,0x00080102};
        assertTrue( src_dobj.contains(s11));
        int[] s12 = {0x00120064,1,0x00080103};
        assertTrue( src_dobj.contains(s12));
        int[] s13 = {0x00120064,1,0x00080104};
        assertTrue( src_dobj.contains(s13));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_ELEMENT_IN_ALL_SEQ_ITEM));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertTrue( result_dobj.contains(s));
        assertTrue( result_dobj.contains(s00));
        assertTrue( result_dobj.contains(s01));
        assertTrue( result_dobj.contains(s02));
        assertFalse( result_dobj.contains(s03));
        assertTrue( result_dobj.contains(s10));
        assertTrue( result_dobj.contains(s11));
        assertTrue( result_dobj.contains(s12));
        assertFalse( result_dobj.contains(s13));
    }

    @Test
    public void testDeleteElementEverywhere() throws Exception {
//        final DicomObjectI src_dobj = DicomObjectFactory.newInstance(f2);
        final DicomObjectI src_dobj = createSeqTestObject();

        assertTrue( src_dobj.contains(s));

        assertTrue( src_dobj.contains(s0a));
        assertTrue( src_dobj.contains(s1a));
        assertTrue( src_dobj.contains(s0b));
        assertTrue( src_dobj.contains(s1b));

        assertTrue( src_dobj.contains(s00));
        assertTrue( src_dobj.contains(s01));
        assertTrue( src_dobj.contains(s10));
        assertTrue( src_dobj.contains(s11));

        assertTrue( src_dobj.contains(s000));
        assertTrue( src_dobj.contains(s001));
        assertTrue( src_dobj.contains(s010));
        assertTrue( src_dobj.contains(s011));
        assertTrue( src_dobj.contains(s100));
        assertTrue( src_dobj.contains(s101));
        assertTrue( src_dobj.contains(s110));
        assertTrue( src_dobj.contains(s111));

        assertTrue( src_dobj.contains(p0a));
        assertTrue( src_dobj.contains(p0b));
        assertTrue( src_dobj.contains(p1a));
        assertTrue( src_dobj.contains(p1b));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_ELEMENT_EVERYWHERE));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertFalse( result_dobj.contains(s));

        assertFalse( result_dobj.contains(s0a));
        assertFalse( result_dobj.contains(s1a));
        assertTrue( src_dobj.contains(s0b));
        assertTrue( src_dobj.contains(s1b));

        assertFalse( result_dobj.contains(s00));
        assertFalse( result_dobj.contains(s01));
        assertFalse( result_dobj.contains(s10));
        assertFalse( result_dobj.contains(s11));

        assertFalse( result_dobj.contains(s000));
        assertFalse( result_dobj.contains(s001));
        assertFalse( result_dobj.contains(s010));
        assertFalse( result_dobj.contains(s011));
        assertFalse( result_dobj.contains(s100));
        assertFalse( result_dobj.contains(s101));
        assertFalse( result_dobj.contains(s110));
        assertFalse( result_dobj.contains(s111));

        assertFalse( src_dobj.contains(p0a));
        assertTrue( src_dobj.contains(p0b));
        assertFalse( src_dobj.contains(p1a));
        assertTrue( src_dobj.contains(p1b));

    }

    @Test
    public void testDeleteElementEverywhereBelowFirstLevel() throws Exception {
        final DicomObjectI src_dobj = createSeqTestObject();

        assertTrue( src_dobj.contains(s));

        assertTrue( src_dobj.contains(s0a));
        assertTrue( src_dobj.contains(s1a));

        assertTrue( src_dobj.contains(s00));
        assertTrue( src_dobj.contains(s01));
        assertTrue( src_dobj.contains(s10));
        assertTrue( src_dobj.contains(s11));

        assertTrue( src_dobj.contains(s000));
        assertTrue( src_dobj.contains(s001));
        assertTrue( src_dobj.contains(s010));
        assertTrue( src_dobj.contains(s011));
        assertTrue( src_dobj.contains(s100));
        assertTrue( src_dobj.contains(s101));
        assertTrue( src_dobj.contains(s110));
        assertTrue( src_dobj.contains(s111));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_ELEMENT_BELOW_FIRST_LEVEL));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertTrue( result_dobj.contains(s));

        assertFalse( result_dobj.contains(s0a));
        assertFalse( result_dobj.contains(s1a));

        assertFalse( result_dobj.contains(s00));
        assertFalse( result_dobj.contains(s01));
        assertFalse( result_dobj.contains(s10));
        assertFalse( result_dobj.contains(s11));

        assertFalse( result_dobj.contains(s000));
        assertFalse( result_dobj.contains(s001));
        assertFalse( result_dobj.contains(s010));
        assertFalse( result_dobj.contains(s011));
        assertFalse( result_dobj.contains(s100));
        assertFalse( result_dobj.contains(s101));
        assertFalse( result_dobj.contains(s110));
        assertFalse( result_dobj.contains(s111));

    }

    @Test
    public void testDeleteElementSecondLevelOnly() throws Exception {
        final DicomObjectI src_dobj = createSeqTestObject();

        assertTrue( src_dobj.contains(s));

        assertTrue( src_dobj.contains(s0a));
        assertTrue( src_dobj.contains(s1a));

        assertTrue( src_dobj.contains(s00));
        assertTrue( src_dobj.contains(s01));
        assertTrue( src_dobj.contains(s10));
        assertTrue( src_dobj.contains(s11));

        assertTrue( src_dobj.contains(s000));
        assertTrue( src_dobj.contains(s001));
        assertTrue( src_dobj.contains(s010));
        assertTrue( src_dobj.contains(s011));
        assertTrue( src_dobj.contains(s100));
        assertTrue( src_dobj.contains(s101));
        assertTrue( src_dobj.contains(s110));
        assertTrue( src_dobj.contains(s111));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_ELEMENT_ONLY_SECOND_LEVEL));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertTrue( result_dobj.contains(s));

        assertFalse( result_dobj.contains(s0a));
        assertFalse( result_dobj.contains(s1a));

        assertTrue( result_dobj.contains(s00));
        assertTrue( result_dobj.contains(s01));
        assertTrue( result_dobj.contains(s10));
        assertTrue( result_dobj.contains(s11));

        assertTrue( result_dobj.contains(s000));
        assertTrue( result_dobj.contains(s001));
        assertTrue( result_dobj.contains(s010));
        assertTrue( result_dobj.contains(s011));
        assertTrue( result_dobj.contains(s100));
        assertTrue( result_dobj.contains(s101));
        assertTrue( result_dobj.contains(s110));
        assertTrue( result_dobj.contains(s111));

    }

    /**
     * <pre>
     * (0018,0026) SQ #-1 [2 items] Intervention Drug Information Sequence
     * >ITEM #1 @1984:
     * >(0018,0028) DS #6 [150000] Intervention Drug Dose                     retain
     * >(0018,0029) SQ #-1 [1 item] Intervention Drug Code Sequence
     * >>ITEM #1 @2018:
     * >>(0008,0100) SH #8 [C-21047] Code Value                               retain
     * >>(0008,0104) LO #8 [Ethanol] Code Meaning                             retain
     * >(0018,0034) LO #10 [CHOCOLATE] Intervention Drug Name                 retain
     * >ITEM #2 @2076:                                                        retain!
     * >(0018,0028) DS #6 [100000] Intervention Drug Dose                     delete
     * >(0018,0034) LO #10 [CHOCOLATE] Intervention Drug Name                 delete
     *
     * (0020,9222) SQ #-1 [2 items] Dimension Index Sequence
     * >ITEM #1 @3520:
     * >(0020,9164) UI #50 [1.3.4...] Dimension Organization UID              retain
     * >(0020,9165) AT #4 [00209056] Dimension Index Pointer                  retain
     * >(0020,9167) AT #4 [00209111] Functional Group Pointer                 retain
     * >(0020,9421) LO #8 [Stack ID] Dimension Description Label              retain
     * >ITEM #2 @3626:   retain
     * >(0020,9164) UI #50 [1.3.4...] Dimension Organization UID              retain
     * >(0020,9165) AT #4 [00209057] Dimension Index Pointer                  retain
     * >(0020,9167) AT #4 [00209111] Functional Group Pointer                 retain
     * >(0020,9421) LO #24 [In-Stack Positi...] Dimension Description Label   retain
     *
     * (0040,0260) SQ #-1 [1 item] Performed Protocol Code Sequence
     * >ITEM #1 @4088:
     * >(0008,0100) SH #10 [UNDEFINED] Code Value                             retain
     * >(0008,0102) SH #10 [UNDEFINED] Coding Scheme Designator               retain
     * >(0008,0104) LO #10 [UNDEFINED] Code Meaning                           retain
     * >(0008,010B) CS #2 [N] Context Group Extension Flag                    delete
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testStandardSeqDelete() throws Exception {
        final DicomObjectI dobj = DicomObjectFactory.newInstance( );
        String script = "-(0018,0026)[1]\n" +
                "-(0040,100A)[0]/(0008,0100)\n" +
                "-(0020,9222)[2]/(0008,0100)\n" +
                "-(0040,0260)[0]/(0008,010b)";

        logger.info( dobj.toCompleteString());

        TestUtils.TestSeqTag t1_0_t1 = new TestUtils.TestSeqTag(new int[]{0x00180026,0,0x00180028}, "t1_0_t1");
        TestUtils.TestSeqTag t1_0_t1_0_t1 = new TestUtils.TestSeqTag(new int[]{0x00180026,0,0x00180029,0,0x00080100}, "t1_0_t1_0_t1");
        TestUtils.TestSeqTag t1_0_t1_0_t2 = new TestUtils.TestSeqTag(new int[]{0x00180026,0,0x00180029,0,0x00080104}, "t1_0_t1_0_t2");
        TestUtils.TestSeqTag t1_0_t2 = new TestUtils.TestSeqTag(new int[]{0x00180026,0,0x00180034}, "t1_0_t2");
        TestUtils.TestSeqTag t1_1_t1 = new TestUtils.TestSeqTag(new int[]{0x00180026,1,0x00180028}, "t1_1_t1");
        TestUtils.TestSeqTag t1_1_t2 = new TestUtils.TestSeqTag(new int[]{0x00180026,1,0x00180034}, "t1_1_t2");

        TestUtils.TestSeqTag t2_0_t1 = new TestUtils.TestSeqTag(new int[]{0x00209222,0,0x00209164}, "t2_0_t1");
        TestUtils.TestSeqTag t2_0_t2 = new TestUtils.TestSeqTag(new int[]{0x00209222,0,0x00209213}, "t2_0_t2");
        TestUtils.TestSeqTag t2_0_t3 = new TestUtils.TestSeqTag(new int[]{0x00209222,0,0x00209238}, "t2_0_t3");
        TestUtils.TestSeqTag t2_0_t4 = new TestUtils.TestSeqTag(new int[]{0x00209222,0,0x00209421}, "t2_0_t4");
        TestUtils.TestSeqTag t2_1_t1 = new TestUtils.TestSeqTag(new int[]{0x00209222,1,0x00209164}, "t2_1_t1");
        TestUtils.TestSeqTag t2_1_t2 = new TestUtils.TestSeqTag(new int[]{0x00209222,1,0x00209213}, "t2_1_t2");
        TestUtils.TestSeqTag t2_1_t3 = new TestUtils.TestSeqTag(new int[]{0x00209222,1,0x00209238}, "t2_1_t3");
        TestUtils.TestSeqTag t2_1_t4 = new TestUtils.TestSeqTag(new int[]{0x00209222,1,0x00209421}, "t2_1_t4");

        TestUtils.TestSeqTag t3_0_t1 = new TestUtils.TestSeqTag(new int[]{0x00400260,0,0x00080100}, "t3_0_t1");
        TestUtils.TestSeqTag t3_0_t2 = new TestUtils.TestSeqTag(new int[]{0x00400260,0,0x00080102}, "t3_0_t2");
        TestUtils.TestSeqTag t3_0_t3 = new TestUtils.TestSeqTag(new int[]{0x00400260,0,0x00080104}, "t3_0_t3");
        TestUtils.TestSeqTag t3_0_t4 = new TestUtils.TestSeqTag(new int[]{0x00400260,0,0x0008010B}, "t3_0_t4");

        TestUtils.put( dobj, t1_0_t1);
        TestUtils.put( dobj, t1_0_t1_0_t1);
        TestUtils.put( dobj, t1_0_t1_0_t2);
        TestUtils.put( dobj, t1_0_t2);
        TestUtils.put( dobj, t1_1_t1);
        TestUtils.put( dobj, t1_1_t2);

        TestUtils.put( dobj, t2_0_t1);
        TestUtils.put( dobj, t2_0_t2);
        TestUtils.put( dobj, t2_0_t3);
        TestUtils.put( dobj, t2_0_t4);
        TestUtils.put( dobj, t2_1_t1);
        TestUtils.put( dobj, t2_1_t2);
        TestUtils.put( dobj, t2_1_t3);
        TestUtils.put( dobj, t2_1_t4);

        TestUtils.put( dobj, t3_0_t1);
        TestUtils.put( dobj, t3_0_t2);
        TestUtils.put( dobj, t3_0_t3);
        TestUtils.put( dobj, t3_0_t4);

        assertTrue( dobj.contains(t1_0_t1.tag));
        assertTrue( dobj.contains(t1_0_t1_0_t1.tag));
        assertTrue( dobj.contains(t1_0_t1_0_t2.tag));
        assertTrue( dobj.contains(t1_0_t2.tag));
        assertTrue( dobj.contains(t1_1_t1.tag));
        assertTrue( dobj.contains(t1_1_t2.tag));

        assertTrue( dobj.contains(t2_0_t1.tag));
        assertTrue( dobj.contains(t2_0_t2.tag));
        assertTrue( dobj.contains(t2_0_t3.tag));
        assertTrue( dobj.contains(t2_0_t4.tag));
        assertTrue( dobj.contains(t2_1_t1.tag));
        assertTrue( dobj.contains(t2_1_t2.tag));
        assertTrue( dobj.contains(t2_1_t3.tag));
        assertTrue( dobj.contains(t2_1_t4.tag));

        assertTrue( dobj.contains(t3_0_t1.tag));
        assertTrue( dobj.contains(t3_0_t2.tag));
        assertTrue( dobj.contains(t3_0_t3.tag));
        assertTrue( dobj.contains(t3_0_t4.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        logger.info( dobj.toCompleteString());
        sa.apply(dobj);
        logger.info( dobj.toCompleteString());

        assertTrue( dobj.contains(t1_0_t1.tag));
        assertTrue( dobj.contains(t1_0_t1_0_t1.tag));
        assertTrue( dobj.contains(t1_0_t1_0_t2.tag));
        assertTrue( dobj.contains(t1_0_t2.tag));
        assertFalse( dobj.contains(t1_1_t1.tag));
        assertFalse( dobj.contains(t1_1_t2.tag));

        assertTrue( dobj.contains(t2_0_t1.tag));
        assertTrue( dobj.contains(t2_0_t2.tag));
        assertTrue( dobj.contains(t2_0_t3.tag));
        assertTrue( dobj.contains(t2_0_t4.tag));
        assertTrue( dobj.contains(t2_1_t1.tag));
        assertTrue( dobj.contains(t2_1_t2.tag));
        assertTrue( dobj.contains(t2_1_t3.tag));
        assertTrue( dobj.contains(t2_1_t4.tag));

        assertTrue( dobj.contains(t3_0_t1.tag));
        assertTrue( dobj.contains(t3_0_t2.tag));
        assertTrue( dobj.contains(t3_0_t3.tag));
        assertFalse( dobj.contains(t3_0_t4.tag));
    }

    /**
     * <pre>
     * (5200,9230) SQ #-1 [2 items] Per-frame Functional Groups Sequence    retain
     * >ITEM #1:                                                            retain
     * >(0018,9152) SQ #-1 [2 items] MR Metabolite Map Sequence             delete
     * >>ITEM #1:                                                           delete
     * >>(0010,0010) LO #12 [t1_0_t1_0_t1] Patient’s Name                   delete
     * >>(0010,0012) LO #12 [t1_0_t1_0_t2] ?                                delete
     * >>ITEM #2:                                                           delete
     * >>(0010,0010) LO #12 [t1_0_t1_1_t1] Patient’s Name                   delete
     * >>(0010,0012) LO #12 [t1_0_t1_1_t2] ?                                delete
     * >ITEM #2:                                                            retain
     * >(0018,9152) SQ #-1 [2 items] MR Metabolite Map Sequence             delete
     * >>ITEM #1:                                                           delete
     * >>(0010,0010) LO #12 [t1_1_t1_0_t1] Patient’s Name                   delete
     * >>(0010,0012) LO #12 [t1_1_t1_0_t2] ?                                delete
     * >>ITEM #2:                                                           delete
     * >>(0010,0010) LO #12 [t1_1_t1_1_t1] Patient’s Name                   delete
     * >>(0010,0012) LO #12 [t1_1_t1_1_t2] ?                                delete
     *
     *  results in
     *
     * (5200,9230) SQ #-1 [2 items] Per-frame Functional Groups Sequence
     * >ITEM #1:
     * >ITEM #2:
     *
     *  versus
     *
     * (5200,9230) SQ #-1 [2 items] Per-frame Functional Groups Sequence
     * >ITEM #1:
     * >(0018,9152) SQ #-1 [2 items] MR Metabolite Map Sequence
     * >>ITEM #1:
     * >>ITEM #2:
     * >ITEM #2:
     * >(0018,9152) SQ #-1 [2 items] MR Metabolite Map Sequence
     * >>ITEM #1:
     * >>ITEM #2:
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testSubSeqDelete() throws Exception {
        String script = "-(5200,9230)[%]/(0018,9152)";

        TestUtils.TestSeqTag t1_0_t1_0_t1 = new TestUtils.TestSeqTag(new int[]{0x52009230,0,0x00189152,0,0x00100010}, "t1_0_t1_0_t1");
        TestUtils.TestSeqTag t1_0_t1_0_t2 = new TestUtils.TestSeqTag(new int[]{0x52009230,0,0x00189152,0,0x00100012}, "t1_0_t1_0_t2");
        TestUtils.TestSeqTag t1_0_t1_1_t1 = new TestUtils.TestSeqTag(new int[]{0x52009230,0,0x00189152,1,0x00100010}, "t1_0_t1_1_t1");
        TestUtils.TestSeqTag t1_0_t1_1_t2 = new TestUtils.TestSeqTag(new int[]{0x52009230,0,0x00189152,1,0x00100012}, "t1_0_t1_1_t2");
        TestUtils.TestSeqTag t1_1_t1_0_t1 = new TestUtils.TestSeqTag(new int[]{0x52009230,1,0x00189152,0,0x00100010}, "t1_1_t1_0_t1");
        TestUtils.TestSeqTag t1_1_t1_0_t2 = new TestUtils.TestSeqTag(new int[]{0x52009230,1,0x00189152,0,0x00100012}, "t1_1_t1_0_t2");
        TestUtils.TestSeqTag t1_1_t1_1_t1 = new TestUtils.TestSeqTag(new int[]{0x52009230,1,0x00189152,1,0x00100010}, "t1_1_t1_1_t1");
        TestUtils.TestSeqTag t1_1_t1_1_t2 = new TestUtils.TestSeqTag(new int[]{0x52009230,1,0x00189152,1,0x00100012}, "t1_1_t1_1_t2");

        final DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.putString( t1_0_t1_0_t1.tag, t1_0_t1_0_t1.initialValue);
        dobj.putString( t1_0_t1_0_t2.tag, t1_0_t1_0_t2.initialValue);
        dobj.putString( t1_0_t1_1_t1.tag, t1_0_t1_1_t1.initialValue);
        dobj.putString( t1_0_t1_1_t2.tag, t1_0_t1_1_t2.initialValue);
        dobj.putString( t1_1_t1_0_t1.tag, t1_1_t1_0_t1.initialValue);
        dobj.putString( t1_1_t1_0_t2.tag, t1_1_t1_0_t2.initialValue);
        dobj.putString( t1_1_t1_1_t1.tag, t1_1_t1_1_t1.initialValue);
        dobj.putString( t1_1_t1_1_t2.tag, t1_1_t1_1_t2.initialValue);
        logger.info( dobj.toCompleteString());

        assertTrue( dobj.contains(t1_0_t1_0_t1.tag));
        assertTrue( dobj.contains(t1_0_t1_0_t2.tag));
        assertTrue( dobj.contains(t1_0_t1_1_t1.tag));
        assertTrue( dobj.contains(t1_0_t1_1_t2.tag));
        assertTrue( dobj.contains(t1_1_t1_0_t1.tag));
        assertTrue( dobj.contains(t1_1_t1_0_t2.tag));
        assertTrue( dobj.contains(t1_1_t1_1_t1.tag));
        assertTrue( dobj.contains(t1_1_t1_1_t2.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        sa.apply(dobj);

        logger.info( dobj.toCompleteString());

        // the tags are gone but the items 0x52009230,0 and 0x52009230,1 remain.
        assertFalse( dobj.contains(t1_0_t1_0_t1.tag));
        assertFalse( dobj.contains(t1_0_t1_0_t2.tag));
        assertFalse( dobj.contains(t1_0_t1_1_t1.tag));
        assertFalse( dobj.contains(t1_0_t1_1_t2.tag));
        assertFalse( dobj.contains(t1_1_t1_0_t1.tag));
        assertFalse( dobj.contains(t1_1_t1_0_t2.tag));
        assertFalse( dobj.contains(t1_1_t1_1_t1.tag));
        assertFalse( dobj.contains(t1_1_t1_1_t2.tag));
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createSeqTestObject() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString( s, "PatientName");
        int[] foo = {0x00200010};
        src_dobj.putString( foo, "foo");

        src_dobj.putString( s0a, "s0a");
        src_dobj.putString( s1a, "s1a");
        src_dobj.putString( s0b, "s0b");
        src_dobj.putString( s1b, "s1a");
        int[] foo2 = {0x00081032,1,0x00200010};
        src_dobj.putString( foo2, "foo");

        src_dobj.putString( s00, "s00");
        src_dobj.putString( s01, "s01");
        src_dobj.putString( s10, "s10");
        src_dobj.putString( s11, "s11");

        src_dobj.putString( s000, "s000");
        src_dobj.putString( s001, "s001");
        src_dobj.putString( s010, "s010");
        src_dobj.putString( s011, "s011");
        src_dobj.putString( s100, "s100");
        src_dobj.putString( s101, "s101");
        src_dobj.putString( s110, "s110");
        src_dobj.putString( s111, "s111");

        src_dobj.putString( pcreator, "pcreator");
        src_dobj.putString( p, "p");

        src_dobj.putString( p0a, "p0a");
        src_dobj.putString( p0b, "p0b");
        src_dobj.putString( p1a, "p1a");
        src_dobj.putString( p1b, "p1b");

        return src_dobj;
    }

    private static int[] s = {0x00100010};

    private static int[] s0a = {0x00081032,0,0x00100010};
    private static int[] s0b = {0x00081032,0,0x00100020};
    private static int[] s1a = {0x00081032,1,0x00100010};
    private static int[] s1b = {0x00081032,1,0x00100020};

    private static int[] s00 = {0x00081032,0,0x00201032,0,0x00100010};
    private static int[] s01 = {0x00081032,0,0x00201032,1,0x00100010};
    private static int[] s10 = {0x00081032,1,0x00201033,0,0x00100010};
    private static int[] s11 = {0x00081032,1,0x00201033,1,0x00100010};

    private static int[] s000 = {0x00081032,0,0x00201032,0,0x00201034,0,0x00100010};
    private static int[] s001 = {0x00081032,0,0x00201032,0,0x00201034,1,0x00100010};
    private static int[] s010 = {0x00081032,0,0x00201032,1,0x00201036,0,0x00100010};
    private static int[] s011 = {0x00081032,0,0x00201032,1,0x00201036,1,0x00100010};
    private static int[] s100 = {0x00081032,1,0x00201033,0,0x00201035,0,0x00100010};
    private static int[] s101 = {0x00081032,1,0x00201033,0,0x00201035,1,0x00100010};
    private static int[] s110 = {0x00081032,1,0x00201033,1,0x00201037,0,0x00100010};
    private static int[] s111 = {0x00081032,1,0x00201033,1,0x00201037,1,0x00100010};

    private static int[] pcreator = {0x00130010};
    private static int[] p = {0x00131000};

    private static int[] p0a = {0x00131010,0,0x00100010};
    private static int[] p0b = {0x00131010,0,0x00100011};
    private static int[] p1a = {0x00131010,1,0x00100010};
    private static int[] p1b = {0x00131010,1,0x0010001b};

}
