/*
 * DicomEdit: TestVRs
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.dcm4che3.data.Tag;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPublic;
import org.nrg.dicom.mizer.values.ConstantValue;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Run tests on implementation of DicomObjectI interface.
 **
 */
public class TestDicomObject {

    @Test
    public void testEmptyConstructor() throws MizerException {
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance();
    }

    @Test
    public void testFileConstructor() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);
    }

    @Test
    public void testGetUIDfromIntTag() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);

        String s = dicomObjectI.getString(Tag.StudyInstanceUID);

        assertEquals( s, "1.3.12.2.1107.5.2.32.35177.30000006121218324675000000034");
    }

    @Test
    public void testGetFloatFromTagPath() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);

        TagPath tp = new TagPath();
        tp.addTag( new TagPublic(Tag.SAR));
        String s = dicomObjectI.getString( tp);

        assertEquals( s, "0.07360927401803");
    }

    @Test
    public void testGetMultivaluedFloatFromTagPath() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);

        TagPath tp = new TagPath();
        tp.addTag( new TagPublic( Tag.ImagePositionPatient));
        String s = dicomObjectI.getString( tp);

        assertEquals( s, "-67.184784952598\\-167.44676099425\\40.821525786976");
    }

    @Test
    public void testGetMissingValueFromTagPath() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);

        TagPath tp = new TagPath();
        tp.addTag( new TagPublic( Tag.PatientAddress));
        String s = dicomObjectI.getString( tp);

        assertNull( s);
    }

    @Test
    public void testGetEmptyValueFromTagPath() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);

        TagPath tp = new TagPath();
        tp.addTag( new TagPublic( Tag.ReferringPhysicianName));
        String s = dicomObjectI.getString( tp);

        assertTrue( s.isEmpty());
    }

    @Test(expected = NumberFormatException.class)
    public void testGetValueFromNonSingularTagPath() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);

        TagPath tp = new TagPath();
        tp.addTag( new TagPublic( "0010", "00xx" ));
        String s = dicomObjectI.getString( tp);
    }

    @Test
    public void testResolvePrivateTag_Normal() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);

        int tag = 0x00190008;
        int rt = dicomObjectI.resolvePrivateTag(tag, "SIEMENS MR HEADER", false);

        assertEquals( rt, 0x00191008);
    }

    @Test
    public void testResolvePrivateTag_NoPvtCreatorID() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);

        int tag = 0x00190008;
        int rt = dicomObjectI.resolvePrivateTag(tag, "FAKE", false);

        assertEquals( rt, -1);
    }

    @Test
    public void testResolvePrivateTag_createPreexistingTag() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);

        int tag = 0x00191008;
        String s = dicomObjectI.getString( tag);
        assertEquals( s, "IMAGE NUM 4");

        int rt = dicomObjectI.resolvePrivateTag(tag, "SIEMENS MR HEADER", true);
        s = dicomObjectI.getString( rt);
        assertEquals( s, "IMAGE NUM 4");
    }

    @Test
    public void testResolvePrivateTag_createMissingTag() throws MizerException {
        File f = new File("src/test/resources/dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
        final DicomObjectI dicomObjectI = DicomObjectFactory.newInstance(f);

        int tag = 0x00191007;
        String s = dicomObjectI.getString( tag);
        assertNull( s);

        int rt = dicomObjectI.resolvePrivateTag(tag, "SIEMENS MR HEADER", true);
        s = dicomObjectI.getString( rt);

        assertNull( s);

        dicomObjectI.assign( rt, new ConstantValue(""));
        s = dicomObjectI.getString( rt);
        assertEquals( s, "");
    }

}
