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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Run test to delete from multiple blocks in the same private group.
 * Inspired by issue DE-21
 *
 *
 * Created by drm on 2018-03-09.
 */
public class TestDeleteTwoBlocksInSamePrivateGroup {

    private static final Logger logger = LoggerFactory.getLogger(TestDeleteTwoBlocksInSamePrivateGroup.class);

    private static final String S_DELETE_FUJI = "version \"6.1\"\n- (0029, {FujiFILM TM}XX)\n";
    private static final String S_DELETE_FUJI_31 = "version \"6.1\"\n- (0029, {FujiFILM TM}31)\n";

    /**
     * Deleting all private elements in a block does not remove orphan private creator ID tag.
     * <pre>
     * - (0029, {FujiFILM TM}XX)
     *
     * (0029,0010) LO #18 [SIEMENS CSA HEADER] Private Creator Data Element       retain
     * (0029,0011) LO #22 [SIEMENS MEDCOM HEADER] Private Creator Data Element    retain
     * (0029,0012) LO #12 [FujiFILM TM] Private Creator Data Element              retain
     * (0029,00E1) LO #12 [FujiFILM TM] Private Creator Data Element              retain
     * (0029,1008) LO #14 [some CSA data] ?                                       retain
     * (0029,1009) LO #14 [some CSA data] ?                                       retain
     * (0029,1010) LO #14 [some CSA data] ?                                       retain
     * (0029,1110) LO #16 [some MEDCOM data] ?                                    retain
     * (0029,1160) LO #16 [some MEDCOM data] ?                                    retain
     * (0029,1203) LO #18 [some Fuji 12 data] ?                                   delete
     * (0029,1231) LO #18 [some Fuji 12 data] ?                                   delete
     * (0029,E101) LO #18 [some Fuji E1 data] ?                                   delete
     * (0029,E131) LO #18 [some Fuji E1 data] ?                                   delete
     * </pre>
     * @throws Exception
     */
    @Test
    public void testDeleteTwoBlocks() throws Exception {
        final DicomObjectI src_dobj = createTestObject();

        logger.info(src_dobj.toString());

        assertTrue( src_dobj.contains(siemens_csa_creator_id));
        assertTrue( src_dobj.contains(siemens_medcom_creator_id));
        assertTrue( src_dobj.contains(fuji_12_creator_id));
        assertTrue( src_dobj.contains(fuji_E1_creator_id));

        assertTrue( src_dobj.contains(csa_data_8));
        assertTrue( src_dobj.contains(csa_data_9));
        assertTrue( src_dobj.contains(csa_data_10));

        assertTrue( src_dobj.contains(medcom_data_60));
        assertTrue( src_dobj.contains(medcom_data_10));

        assertTrue( src_dobj.contains(fuji_data_1231));
        assertTrue( src_dobj.contains(fuji_data_1203));
        assertTrue( src_dobj.contains(fuji_data_E131));
        assertTrue( src_dobj.contains(fuji_data_E101));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_FUJI));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertTrue( result_dobj.contains(siemens_csa_creator_id));
        assertTrue( result_dobj.contains(siemens_medcom_creator_id));
        assertTrue( result_dobj.contains(fuji_12_creator_id));
        assertTrue( result_dobj.contains(fuji_E1_creator_id));

        assertTrue( result_dobj.contains(csa_data_8));
        assertTrue( result_dobj.contains(csa_data_9));
        assertTrue( result_dobj.contains(csa_data_10));

        assertTrue( result_dobj.contains(medcom_data_60));
        assertTrue( result_dobj.contains(medcom_data_10));

        assertFalse( result_dobj.contains(fuji_data_1231));
        assertFalse( result_dobj.contains(fuji_data_1203));
        assertFalse( result_dobj.contains(fuji_data_E131));
        assertFalse( result_dobj.contains(fuji_data_E101));

//        logger.info(result_dobj.toString());
//        DumpVisitor dv = new DumpVisitor(result_dobj, new PrintStream(new File("/tmp/foo.txt")));
//        dv.visit( result_dobj);
    }

    /**
     * Deleting all private elements in a block does not remove orphan private creator ID tag.
     * <pre>
     * - (0029, {FujiFILM TM}31)
     *
     * (0029,0010) LO #18 [SIEMENS CSA HEADER] Private Creator Data Element       retain
     * (0029,0011) LO #22 [SIEMENS MEDCOM HEADER] Private Creator Data Element    retain
     * (0029,0012) LO #12 [FujiFILM TM] Private Creator Data Element              retain
     * (0029,00E1) LO #12 [FujiFILM TM] Private Creator Data Element              retain
     * (0029,1008) LO #14 [some CSA data] ?                                       retain
     * (0029,1009) LO #14 [some CSA data] ?                                       retain
     * (0029,1010) LO #14 [some CSA data] ?                                       retain
     * (0029,1110) LO #16 [some MEDCOM data] ?                                    retain
     * (0029,1160) LO #16 [some MEDCOM data] ?                                    retain
     * (0029,1203) LO #18 [some Fuji 12 data] ?                                   retain
     * (0029,1231) LO #18 [some Fuji 12 data] ?                                   delete
     * (0029,E101) LO #18 [some Fuji E1 data] ?                                   retain
     * (0029,E131) LO #18 [some Fuji E1 data] ?                                   delete
     * </pre>
     * @throws Exception
     */
    @Test
    public void testDeleteFuji31() throws Exception {
        final DicomObjectI src_dobj = createTestObject();

        logger.info(src_dobj.toString());

        assertTrue( src_dobj.contains(siemens_csa_creator_id));
        assertTrue( src_dobj.contains(siemens_medcom_creator_id));
        assertTrue( src_dobj.contains(fuji_12_creator_id));
        assertTrue( src_dobj.contains(fuji_E1_creator_id));

        assertTrue( src_dobj.contains(csa_data_8));
        assertTrue( src_dobj.contains(csa_data_9));
        assertTrue( src_dobj.contains(csa_data_10));

        assertTrue( src_dobj.contains(medcom_data_60));
        assertTrue( src_dobj.contains(medcom_data_10));

        assertTrue( src_dobj.contains(fuji_data_1231));
        assertTrue( src_dobj.contains(fuji_data_1203));
        assertTrue( src_dobj.contains(fuji_data_E131));
        assertTrue( src_dobj.contains(fuji_data_E101));

//        DupPrivateBlockTerminator terminator = new DupPrivateBlockTerminator();
//        terminator.visit( src_dobj);
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(S_DELETE_FUJI_31));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertTrue( result_dobj.contains(siemens_csa_creator_id));
        assertTrue( result_dobj.contains(siemens_medcom_creator_id));
        assertTrue( result_dobj.contains(fuji_12_creator_id));
        assertTrue( result_dobj.contains(fuji_E1_creator_id));

        assertTrue( result_dobj.contains(csa_data_8));
        assertTrue( result_dobj.contains(csa_data_9));
        assertTrue( result_dobj.contains(csa_data_10));

        assertTrue( result_dobj.contains(medcom_data_60));
        assertTrue( result_dobj.contains(medcom_data_10));

        assertFalse( result_dobj.contains(fuji_data_1231));
        assertTrue( result_dobj.contains(fuji_data_1203));
        assertFalse( result_dobj.contains(fuji_data_E131));
        assertTrue( result_dobj.contains(fuji_data_E101));

//        logger.info(result_dobj.toString());
//        DumpVisitor dv = new DumpVisitor(result_dobj, new PrintStream(new File("/tmp/foo.txt")));
//        dv.visit( result_dobj);
    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createTestObject() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString( siemens_csa_creator_id, "SIEMENS CSA HEADER");
        src_dobj.putString( siemens_medcom_creator_id, "SIEMENS MEDCOM HEADER");
        src_dobj.putString( fuji_12_creator_id, "FujiFILM TM");
        src_dobj.putString( fuji_E1_creator_id, "FujiFILM TM");

        src_dobj.putString( csa_data_8, "some CSA data");
        src_dobj.putString( csa_data_9, "some CSA data");
        src_dobj.putString( csa_data_10, "some CSA data");

        src_dobj.putString( medcom_data_60, "some MEDCOM data");
        src_dobj.putString( medcom_data_10, "some MEDCOM data");

        src_dobj.putString( fuji_data_1231, "some Fuji 12 data");
        src_dobj.putString( fuji_data_1203, "some Fuji 12 data");
        src_dobj.putString( fuji_data_E131, "some Fuji E1 data");
        src_dobj.putString( fuji_data_E101, "some Fuji E1 data");

        return src_dobj;
    }

    private static int siemens_csa_creator_id = 0x00290010;
    private static int siemens_medcom_creator_id = 0x00290011;
    private static int fuji_12_creator_id = 0x00290012;
    private static int fuji_E1_creator_id = 0x002900E1;

    private static int csa_data_8 = 0x00291008;
    private static int csa_data_9 = 0x00291009;
    private static int csa_data_10 = 0x00291010;

    private static int medcom_data_60 = 0x00291160;
    private static int medcom_data_10 = 0x00291110;

    private static int fuji_data_1231 = 0x00291231;
    private static int fuji_data_1203 = 0x00291203;
    private static int fuji_data_E131 = 0x0029E131;
    private static int fuji_data_E101 = 0x0029E101;

}
