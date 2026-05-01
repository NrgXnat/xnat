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
import org.nrg.dicom.dicomedit.TestUtils.TestTag;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nrg.dicom.dicomedit.TestUtils.put;
import static org.nrg.dicom.dicomedit.TestUtils.bytes;

/**
 * Run tests of shiftDateTimeListByIncrement function.
 *
 */
public class TestShiftDateTimeListByIncrementFunction {

    @Test
    public void testDTAttributes() throws MizerException {
        // shift -300 days
        String dY                = "2023";
        String dY_shifted        = "2022";
        String dYM               = "202305";
        String dYM_shifted       = "202207";
        String dYMD              = "20230519";
        String dYMD_shifted      = "20220723";
        String dYMDH             = "2023051913";
        String dYMDH_shifted     = "2022072313";
        String dYMDHM            = "202305191330";
        String dYMDHM_shifted    = "202207231330";
        String dYMDHMS           = "20230519133022";
        String dYMDHMS_shifted   = "20220723133022";
        String dYMDHMSF3         = "20230519133022.123";
        String dYMDHMSF3_shifted = "20220723133022.123";

        TestTag d1 = new TestTag( 0x0008002A, dY, dY_shifted);
        TestTag d2 = new TestTag( 0x0040A120, dYM, dYM_shifted);
        TestTag d3 = new TestTag( 0x0018A002, dYMD, dYMD_shifted);
        TestTag d4 = new TestTag( 0x00189151, dYMDH, dYMDH_shifted);
        TestTag d5 = new TestTag( 0x0040A13A, dYMDHM, dYMDHM_shifted);
        TestTag d6 = new TestTag( 0x00189074, dYMDHMS, dYMDHMS_shifted);
        TestTag d7 = new TestTag( 0x04000105, dYMDHMSF3, dYMDHMSF3_shifted);

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, d1);
        put( dobj, d2);
        put( dobj, d3);
        put( dobj, d4);
        put( dobj, d5);
        put( dobj, d6);
        put( dobj, d7);

        assertEquals( d1.initialValue, dobj.getString(d1.tag));
        assertEquals( d2.initialValue, dobj.getString(d2.tag));
        assertEquals( d3.initialValue, dobj.getString(d3.tag));
        assertEquals( d4.initialValue, dobj.getString(d4.tag));
        assertEquals( d5.initialValue, dobj.getString(d5.tag));
        assertEquals( d6.initialValue, dobj.getString(d6.tag));
        assertEquals( d7.initialValue, dobj.getString(d7.tag));

        String script = "shiftDateTimeListByIncrement[ {(0008,002A),(0018,9074),(0040,A120),(0018,A002),(0018,9151),(0040,A13A),(0400,0105)}, \"-300\", \"days\"]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( d1.postValue, dobj.getString(d1.tag));
        assertEquals( d2.postValue, dobj.getString(d2.tag));
        assertEquals( d3.postValue, dobj.getString(d3.tag));
        assertEquals( d4.postValue, dobj.getString(d4.tag));
        assertEquals( d5.postValue, dobj.getString(d5.tag));
        assertEquals( d6.postValue, dobj.getString(d6.tag));
        assertEquals( d7.postValue, dobj.getString(d7.tag));
    }

    @Test
    public void testDateAttributes() throws MizerException {
        // shift -300 days
        String dY                = "2023";
        String dY_shifted        = "2022";
        String dYM               = "202305";
        String dYM_shifted       = "202207";
        String dYMD              = "20230519";
        String dYMD_shifted      = "20220723";
        String dYMDH             = "2023051913";
        String dYMDH_shifted     = "2022072313";
        String dYMDHM            = "202305191330";
        String dYMDHM_shifted    = "202207231330";
        String dYMDHMS           = "20230519133022";
        String dYMDHMS_shifted   = "20220723133022";
        String dYMDHMSF3         = "20230519133022.123";
        String dYMDHMSF3_shifted = "20220723133022.123";

        TestTag d1 = new TestTag( 0x00080025, dY, dY_shifted);
        TestTag d2 = new TestTag( 0x0040A121, dYM, dYM_shifted);
        TestTag d3 = new TestTag( 0x00080022, dYMD, dYMD_shifted);
        TestTag d4 = new TestTag( 0x00080012, dYMDH, dYMDH_shifted);
        TestTag d5 = new TestTag( 0x00100030, dYMDHM, dYMDHM_shifted);
        TestTag d6 = new TestTag( 0x00080020, dYMDHMS, dYMDHMS_shifted);
        TestTag d7 = new TestTag( 0x00080021, dYMDHMSF3, dYMDHMSF3_shifted);

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, d1);
        put( dobj, d2);
        put( dobj, d3);
        put( dobj, d4);
        put( dobj, d5);
        put( dobj, d6);
        put( dobj, d7);

        assertEquals( d1.initialValue, dobj.getString(d1.tag));
        assertEquals( d2.initialValue, dobj.getString(d2.tag));
        assertEquals( d3.initialValue, dobj.getString(d3.tag));
        assertEquals( d4.initialValue, dobj.getString(d4.tag));
        assertEquals( d5.initialValue, dobj.getString(d5.tag));
        assertEquals( d6.initialValue, dobj.getString(d6.tag));
        assertEquals( d7.initialValue, dobj.getString(d7.tag));

        String script = "shiftDateTimeListByIncrement[ {(0008,0025),(0040,A121),(0008,0022),(0008,0012),(0010,0030),(0008,0020),(0008,0021)}, \"-300\", \"days\"]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( d1.postValue, dobj.getString(d1.tag));
        assertEquals( d2.postValue, dobj.getString(d2.tag));
        assertEquals( d3.postValue, dobj.getString(d3.tag));
        assertEquals( d4.postValue, dobj.getString(d4.tag));
        assertEquals( d5.postValue, dobj.getString(d5.tag));
        assertEquals( d6.postValue, dobj.getString(d6.tag));
        assertEquals( d7.postValue, dobj.getString(d7.tag));
    }

    @Test
    public void testTimeAttributes() throws MizerException {
        // 13 hrs, 13 min, 13 sec = 46813 sec
        // shift -46813 days
        String dY                = "2023";
        String dY_shifted        = "2023";
        String dYM               = "202305";
        String dYM_shifted       = "202305";
        String dYMD              = "20230519";
        String dYMD_shifted      = "20230518";
        String dYMDH             = "2023051913";
        String dYMDH_shifted     = "2023051900";
        String dYMDHM            = "202305191330";
        String dYMDHM_shifted    = "202305190030";
        String dYMDHMS           = "20230519133022";
        String dYMDHMS_shifted   = "20230519003009";
        String dYMDHMSF3         = "20230519133022.123";
        String dYMDHMSF3_shifted = "20230519003009.123";

        TestTag d1 = new TestTag( 0x00080033, dY, dY_shifted);
        TestTag d2 = new TestTag( 0x0008002A, dYM, dYM_shifted);
        TestTag d3 = new TestTag( 0x00700083, dYMD, dYMD_shifted);
        TestTag d4 = new TestTag( 0x00080013, dYMDH, dYMDH_shifted);
        TestTag d5 = new TestTag( 0x00100032, dYMDHM, dYMDHM_shifted);
        TestTag d6 = new TestTag( 0x00080030, dYMDHMS, dYMDHMS_shifted);
        TestTag d7 = new TestTag( 0x00080031, dYMDHMSF3, dYMDHMSF3_shifted);

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, d1);
        put( dobj, d2);
        put( dobj, d3);
        put( dobj, d4);
        put( dobj, d5);
        put( dobj, d6);
        put( dobj, d7);

        assertEquals( d1.initialValue, dobj.getString(d1.tag));
        assertEquals( d2.initialValue, dobj.getString(d2.tag));
        assertEquals( d3.initialValue, dobj.getString(d3.tag));
        assertEquals( d4.initialValue, dobj.getString(d4.tag));
        assertEquals( d5.initialValue, dobj.getString(d5.tag));
        assertEquals( d6.initialValue, dobj.getString(d6.tag));
        assertEquals( d7.initialValue, dobj.getString(d7.tag));

        String script = "shiftDateTimeListByIncrement[ {(0008,0033),(0008,002A),(0070,0083),(0008,0013),(0010,0032),(0008,0030),(0008,0031)}, \"-46813\", \"seconds\"]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( d1.postValue, dobj.getString(d1.tag));
        assertEquals( d2.postValue, dobj.getString(d2.tag));
        assertEquals( d3.postValue, dobj.getString(d3.tag));
        assertEquals( d4.postValue, dobj.getString(d4.tag));
        assertEquals( d5.postValue, dobj.getString(d5.tag));
        assertEquals( d6.postValue, dobj.getString(d6.tag));
        assertEquals( d7.postValue, dobj.getString(d7.tag));
    }

    /**
     * Will shift a non DA,TM,DT attribute as long as value is valid DT format.
     *
     * @throws MizerException
     */
    @Test
    public void testNotDateTimeAttribute() throws MizerException {
        // 13 hrs, 13 min, 13 sec = 46813 sec
        // shift -46813 days
        String dYMDH             = "2023051913";
        String dYMDH_shifted     = "2023051900";
        String dYMDHM            = "202305191330";
        String dYMDHM_shifted    = "202305190030";

        TestTag d1 = new TestTag( 0x00100010, dYMDH, dYMDH_shifted);
        TestTag d2 = new TestTag( 0x00100020, dYMDHM, dYMDHM_shifted);

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, d1);
        put( dobj, d2);

        assertEquals( d1.initialValue, dobj.getString(d1.tag));
        assertEquals( d2.initialValue, dobj.getString(d2.tag));

        String script = "shiftDateTimeListByIncrement[ {(0010,0010),(0010,0020)}, \"-46813\", \"seconds\"]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( d1.postValue, dobj.getString(d1.tag));
        assertEquals( d2.postValue, dobj.getString(d2.tag));
    }

    @Test
    public void testNotDateTimeFormat1() throws MizerException {
        // 13 hrs, 13 min, 13 sec = 46813 sec
        // shift -46813 days
        String dY = "1";

        TestTag d1 = new TestTag( 0x00080033, dY);

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, d1);

        assertEquals( d1.initialValue, dobj.getString(d1.tag));

        String script = "shiftDateTimeListByIncrement[ {(0008,0033)}, \"-46813\", \"seconds\"]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
        assertTrue(String.join("\n", result.getMessages()).contains("Failed to parse DICOM time string '1'"));
    }

    @Test
    public void testNotDateTimeFormat2() throws MizerException {
        // 13 hrs, 13 min, 13 sec = 46813 sec
        // shift -46813 days
        String dY = "Doe^John";

        TestTag d1 = new TestTag( 0x00080033, dY);

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, d1);

        assertEquals( d1.initialValue, dobj.getString(d1.tag));

        String script = "shiftDateTimeListByIncrement[ {(0008,0033)}, \"-46813\", \"seconds\"]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
        assertTrue(String.join("\n", result.getMessages()).contains("Failed to parse DICOM time string 'Doe^John'"));
    }

}
