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
import org.nrg.test.workers.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Run tests of remoeveAllPrivateTags command.
 *
 */
public class TestRemoveAllPrivateTags {

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File FILE4 = _resourceManager.getTestResourceFile("dicom/1.2.840.113717.2.21733635.1-3-2-7b04bw.dcm");

    /**
     * This data has multiple private blocks, including two blocks in the same group using the same creator id.
     * There are no private sequence tags for private tags in sequences.
     *
     * Remove them all.
     *
     * @throws MizerException
     */
    @Test
    public void testRemoveAllSimplePrivateTags() throws MizerException {

        String script = "removeAllPrivateTags";

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
        assertFalse( result_dobj.contains(siemens_medcom_creator_id));
        assertFalse( result_dobj.contains(fuji_12_creator_id));
        assertFalse( result_dobj.contains(fuji_E1_creator_id));

        assertFalse( result_dobj.contains(csa_data_8));
        assertFalse( result_dobj.contains(csa_data_9));
        assertFalse( result_dobj.contains(csa_data_10));
        assertFalse( result_dobj.contains(csa_data_18));
        assertFalse( result_dobj.contains(csa_data_19));
        assertFalse( result_dobj.contains(csa_data_20));

        assertFalse( result_dobj.contains(medcom_data_60));

        assertFalse( result_dobj.contains(fuji_data_1231));
        assertFalse( result_dobj.contains(fuji_data_E131));

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
    public void testRemoveSequencePrivateTags() throws MizerException {
        String script = "removeAllPrivateTags";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        // normal public tag
        int[] s = {0x00100010};

        // normal private tag
        int[] p_creator_id = {0x00210010};
        int[] p1 = {0x00211010};

        // public seq with private tag
        int[] spc = {0x00081032,0,0x00190010};
        int[] sp = {0x00081032,0,0x00191010};
        // public seq with public tag
        int[] sn = {0x00081032,0,0x00100010};

        // private sequence tag with public members
        int[] p1_s1 = {0x00211011,0,0x00100010};
        int[] p1_s2 = {0x00211011,1,0x00100010};

        src_dobj.putString( s, "PatientName");
        src_dobj.putString( p_creator_id, "PT Test Creator ID");
        src_dobj.putString( p1, "Simple PT");
        src_dobj.putString( spc, "Sequence PT Creator ID");
        src_dobj.putString( sp, "Private Tag in Public sequence");
        src_dobj.putString( sn, "Public Tag in Public sequence");
        src_dobj.putString( p1_s1, "Public Tag1 in Prjvate sequence");
        src_dobj.putString( p1_s2, "Public Tag2 in Private sequence");

        assertTrue( src_dobj.contains(s));
        assertTrue( src_dobj.contains(p_creator_id));
        assertTrue( src_dobj.contains(p1));
        assertTrue( src_dobj.contains(spc));
        assertTrue( src_dobj.contains(sp));
        assertTrue( src_dobj.contains(sn));
        assertTrue( src_dobj.contains(p1_s1));
        assertTrue( src_dobj.contains(p1_s2));


        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();


        assertTrue( src_dobj.contains(s));
        assertFalse( src_dobj.contains(p_creator_id));
        assertFalse( src_dobj.contains(p1));
        assertFalse( src_dobj.contains(spc));
        assertFalse( src_dobj.contains(sp));
        assertTrue( src_dobj.contains(sn));
        assertFalse( src_dobj.contains(p1_s1));
        assertFalse( src_dobj.contains(p1_s2));
    }

    private int geis_creator_id = 0x00090010;
    private int geis_data_12 = 0x00091012;

    private int siemens_mr_header_creator_id_19 = 0x00190010;
    private int siemens_mr_19_data_8 = 0x00191008;
    private int siemens_mr_19_data_9 = 0x00191009;
    private int siemens_mr_19_data_B = 0x0019100B;
    private int siemens_mr_19_data_F = 0x0019100F;
    private int siemens_mr_19_data_11 = 0x00191011;
    private int siemens_mr_19_data_12 = 0x00191012;
    private int siemens_mr_19_data_13 = 0x00191013;
    private int siemens_mr_19_data_14 = 0x00191014;
    private int siemens_mr_19_data_15 = 0x00191015;
    private int siemens_mr_19_data_17 = 0x00191017;
    private int siemens_mr_19_data_18 = 0x00191018;

    private int siemens_csa_creator_id = 0x00290010;
    private int siemens_medcom_creator_id = 0x00290011;
    private int fuji_12_creator_id = 0x00290012;
    private int fuji_E1_creator_id = 0x002900E1;

    private int csa_data_8 = 0x00291008;
    private int csa_data_9 = 0x00291009;
    private int csa_data_10 = 0x00291010;
    private int csa_data_18 = 0x00291018;
    private int csa_data_19 = 0x00291019;
    private int csa_data_20 = 0x00291020;

    private int medcom_data_60 = 0x00291160;

    private int fuji_data_1231 = 0x00291231;
    private int fuji_data_E131 = 0x0029E131;

    private int siemens_mr_header_creator_id_51 = 0x00510010;
    private int siemens_mr_51_data_8 = 0x00511008;
    private int siemens_mr_51_data_9 = 0x00511009;
    private int siemens_mr_51_data_A = 0x0051100A;
    private int siemens_mr_51_data_C = 0x0051100C;
    private int siemens_mr_51_data_D = 0x0051100D;
    private int siemens_mr_51_data_E = 0x0051100E;
    private int siemens_mr_51_data_F = 0x0051100F;
    private int siemens_mr_51_data_11 = 0x00511011;
    private int siemens_mr_51_data_12 = 0x00511012;
    private int siemens_mr_51_data_13 = 0x00511013;
    private int siemens_mr_51_data_16 = 0x00511016;
    private int siemens_mr_51_data_17 = 0x00511017;
    private int siemens_mr_51_data_19 = 0x00511019;

    private int geis_pacs_903_creator_id = 0x09030010;
    private int geis_pacs_903_data_10 = 0x09031010;
    private int geis_pacs_903_data_11 = 0x09031011;
    private int geis_pacs_903_data_12 = 0x09031012;

    private int geis_905_creator_id = 0x09050010;
    private int geis_905_data_30 = 0x09051030;

    private int geis_7FD1_creator_id = 0x7FD10010;
    private int geis_7FD1_data_30 = 0x7FD11030;
    private int geis_7FD1_data_40 = 0x7FD11040;
    private int geis_7FD1_data_50 = 0x7FD11050;
    private int geis_7FD1_data_60 = 0x7FD11060;


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}