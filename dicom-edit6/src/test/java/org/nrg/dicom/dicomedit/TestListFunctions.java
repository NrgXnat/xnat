package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.junit.Assert.*;
import static org.nrg.dicom.dicomedit.TestUtils.*;

public class TestListFunctions {

    @Test
    public void testListsScript() throws MizerException {
        try {
            DicomObjectI dobj = DicomObjectFactory.newInstance();

            // removed
//            TestTag r1 = new TestTag(0x00400555, "AcquisitionContextSequence");
            TestTag r2 = new TestTag(0x001021B0, "AdditionalPatientHistory");
            TestTag r3 = new TestTag(0x00380010, "AdmissionID");
            TestTag r4 = new TestTag(0x00081080, "AdmittingDiagnosesDescription");

            TestTag u1 = new TestTag(0x00080014, "0.0.0", "2.25.179381058945121467305106941903336095433");     // InstanceCreatorUID
            TestTag u2 = new TestTag(0x00080018, "1.2.3.4", "2.25.12977335313771255011876057319581175462");    // SOPInstanceUID
            TestTag u3 = new TestTag(0x00081155, "1.3.1.1", "2.25.258602106693983065446800266265806686854");   // ReferencedSOPInstanceUID
            TestTag u4 = new TestTag(0x0020000E, "1.2.3", "2.25.148370922854076938159295778886134140470");     // SeriesInstanceUID
            TestTag u5 = new TestTag(0x0020000D, "1.2","2.25.171578026969864389535347314560684512280");        // StudyInstanceUID

            // dates, datetimes
            TestTag dt1 = new TestTag(0x00080020, "20230113", "20230112");   // StudyDate
            TestTag dt2 = new TestTag(0x00080012, "20230114", "20230113");   // InstanceCreationDate
            TestTag dt3 = new TestTag(0x00080021, "20230115", "20230114");   // SeriesDate
            TestTag dt4 = new TestTag(0x00080022, "20230116", "20230115");   // AcquisitionDate
            TestTag dt5 = new TestTag(0x00080023, "20230117", "20230116");   // ContentDate
            TestTag dt6 = new TestTag(0x0008002A, "20230118", "20230117");   // AcquisitionDateTime
            TestTag dt7 = new TestTag(0x00100030, "20230119", "20230118");   // PatientBirthDate

            // add some known PHI
            TestTag phi1 = new TestTag(0x00181003, "AdmissionID", "");

            TestTag t1 = new TestTag(0x00080050, "accession", "new accession");
            TestTag t2 = new TestTag(0x00100010, "patientName", "new patientID");
            TestTag t3 = new TestTag(0x00100020, "patientID", "new patientID");
            TestTag t4 = new TestTag(0x00080050, "accession", "new accession");
            TestTag t5 = new TestTag(0x00080090, "ReferringPhysicianName", "");
            TestTag t6 = new TestTag(0x00120062, "", "YES");  // PatientIdentityRemoved
            TestTag t7 = new TestTag(0x00200010, "1", "new accession");  // StudyID
            TestTag t8 = new TestTag(0x00080080, "Some Institution");

            TestTag pa = new TestTag(0x00101010, "090Y", "089Y");

            TestTag studyDescription = new TestTag(0x00081030, "St¨dy Deßcription", "St_dy De_cription");
            TestTag seriesDescription = new TestTag(0x0008103E, "S´riesDescriptiøn", "S_riesDescripti_n");

            TestTag p19_ge_pc = new TestTag(0x00190010, "GE_19");
            TestTag p19_sm_pc = new TestTag(0x00190011, "SM_19");
            TestTag p43_ge_pc = new TestTag(0x00430011, "GE_43");
            TestTag p43_sm_pc = new TestTag(0x00430010, "SM_43");

            TestTag p19_ge_cc = new TestTag(0x001910cc, "p19_ge_cc");
            TestTag p19_ge_e2 = new TestTag(0x001910e2, "p19_ge_e2");
            TestTag p19_ge_ff = new TestTag(0x001910ff, "p19_ge_ff");
            TestTag p43_ge_30 = new TestTag(0x00431130, "p43_ge_30");
            TestTag p43_ge_32 = new TestTag(0x00431132, "p43_ge_32");
            TestTag p43_ge_dd = new TestTag(0x004311dd, "p43_ge_dd");

//            put(dobj, r1);
            put(dobj, r2);
            put(dobj, r3);
            put(dobj, r4);

            put(dobj, u1);
            put(dobj, u2);
            put(dobj, u3);
            put(dobj, u4);
            put(dobj, u5);

            put(dobj, dt1);
            put(dobj, dt2);
            put(dobj, dt3);
            put(dobj, dt4);
            put(dobj, dt5);
            put(dobj, dt6);
            put(dobj, dt7);

            put(dobj, phi1);

            put(dobj, t1);
            put(dobj, t2);
            put(dobj, t3);
            put(dobj, t4);
            put(dobj, t5);
            // put(dobj, t6);  This gets added
            put(dobj, t7);

            put(dobj, pa);

            put(dobj, studyDescription);
            put(dobj, seriesDescription);

            put(dobj, p19_ge_pc);
            put(dobj, p19_sm_pc);
            put(dobj, p43_ge_pc);
            put(dobj, p43_sm_pc);
            put(dobj, p19_ge_cc);
            put(dobj, p19_ge_e2);
            put(dobj, p19_ge_ff);
            put(dobj, p43_ge_30);
            put(dobj, p43_ge_32);
            put(dobj, p43_ge_dd);

//            assertEquals(r1.initialValue, dobj.getString(r1.tag));
            assertEquals(r2.initialValue, dobj.getString(r2.tag));
            assertEquals(r3.initialValue, dobj.getString(r3.tag));
            assertEquals(r4.initialValue, dobj.getString(r4.tag));

            assertEquals(u1.initialValue, dobj.getString(u1.tag));
            assertEquals(u2.initialValue, dobj.getString(u2.tag));
            assertEquals(u3.initialValue, dobj.getString(u3.tag));
            assertEquals(u4.initialValue, dobj.getString(u4.tag));
            assertEquals(u5.initialValue, dobj.getString(u5.tag));

            assertEquals(dt1.initialValue, dobj.getString(dt1.tag));
            assertEquals(dt2.initialValue, dobj.getString(dt2.tag));
            assertEquals(dt3.initialValue, dobj.getString(dt3.tag));
            assertEquals(dt4.initialValue, dobj.getString(dt4.tag));
            assertEquals(dt5.initialValue, dobj.getString(dt5.tag));
            assertEquals(dt6.initialValue, dobj.getString(dt6.tag));
            assertEquals(dt7.initialValue, dobj.getString(dt7.tag));

            assertEquals(phi1.initialValue, dobj.getString(phi1.tag));

            assertEquals(t1.initialValue, dobj.getString(t1.tag));
            assertEquals(t2.initialValue, dobj.getString(t2.tag));
            assertEquals(t3.initialValue, dobj.getString(t3.tag));
            assertEquals(t4.initialValue, dobj.getString(t4.tag));
            assertEquals(t5.initialValue, dobj.getString(t5.tag));
            assertFalse( dobj.contains( t6.tag));
            assertEquals(t7.initialValue, dobj.getString(t7.tag));

            assertEquals(pa.initialValue, dobj.getString(pa.tag));

            assertEquals(studyDescription.initialValue, dobj.getString(studyDescription.tag));
            assertEquals(seriesDescription.initialValue, dobj.getString(seriesDescription.tag));

            assertEquals(p19_ge_pc.initialValue, dobj.getString(p19_ge_pc.tag));
            assertEquals(p19_sm_pc.initialValue, dobj.getString(p19_sm_pc.tag));
            assertEquals(p43_ge_pc.initialValue, dobj.getString(p43_ge_pc.tag));
            assertEquals(p43_sm_pc.initialValue, dobj.getString(p43_sm_pc.tag));
            assertEquals(p19_ge_cc.initialValue, dobj.getString(p19_ge_cc.tag));
            assertEquals(p19_ge_e2.initialValue, dobj.getString(p19_ge_e2.tag));
            assertEquals(p19_ge_ff.initialValue, dobj.getString(p19_ge_ff.tag));
            assertEquals(p43_ge_30.initialValue, dobj.getString(p43_ge_30.tag));
            assertEquals(p43_ge_32.initialValue, dobj.getString(p43_ge_32.tag));
            assertEquals(p43_ge_dd.initialValue, dobj.getString(p43_ge_dd.tag));

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(resourceFileInputStream("scripts/lists.das"));
            dobj = sa.apply(dobj).getDicomObject();

//            assertFalse( dobj.contains( r1.tag));
            assertFalse( dobj.contains( r2.tag));
            assertFalse( dobj.contains( r3.tag));
            assertFalse( dobj.contains( r4.tag));

            assertEquals(u1.postValue, dobj.getString(u1.tag));
            assertEquals(u2.postValue, dobj.getString(u2.tag));
            assertEquals(u3.postValue, dobj.getString(u3.tag));
            assertEquals(u4.postValue, dobj.getString(u4.tag));
            assertEquals(u5.postValue, dobj.getString(u5.tag));

            assertEquals(dt1.postValue, dobj.getString(dt1.tag));
            assertEquals(dt2.postValue, dobj.getString(dt2.tag));
            assertEquals(dt3.postValue, dobj.getString(dt3.tag));
            assertEquals(dt4.postValue, dobj.getString(dt4.tag));
            assertEquals(dt5.postValue, dobj.getString(dt5.tag));
            assertEquals(dt6.postValue, dobj.getString(dt6.tag));
            assertEquals(dt7.postValue, dobj.getString(dt7.tag));

            assertEquals(phi1.postValue, dobj.getString(phi1.tag));

            assertEquals(t1.postValue, dobj.getString(t1.tag));
            assertEquals(t2.postValue, dobj.getString(t2.tag));
            assertEquals(t3.postValue, dobj.getString(t3.tag));
            assertEquals(t4.postValue, dobj.getString(t4.tag));
            assertEquals(t5.postValue, dobj.getString(t5.tag));
            assertEquals(t6.postValue, dobj.getString(t6.tag));
            assertEquals(t7.postValue, dobj.getString(t7.tag));

            assertEquals(pa.postValue, dobj.getString(pa.tag));

            assertEquals(studyDescription.postValue, dobj.getString(studyDescription.tag));
            assertEquals(seriesDescription.postValue, dobj.getString(seriesDescription.tag));

            assertEquals(p19_ge_pc.postValue, dobj.getString(p19_ge_pc.tag));
            assertFalse( dobj.contains(p19_sm_pc.tag));
            assertEquals(p43_ge_pc.postValue, dobj.getString(p43_ge_pc.tag));
            assertFalse( dobj.contains(p43_sm_pc.tag));
            assertEquals(p19_ge_cc.postValue, dobj.getString(p19_ge_cc.tag));
            assertEquals(p19_ge_e2.postValue, dobj.getString(p19_ge_e2.tag));
            assertFalse( dobj.contains(p19_ge_ff.tag));
            assertEquals(p43_ge_30.postValue, dobj.getString(p43_ge_30.tag));
            assertEquals(p43_ge_32.postValue, dobj.getString(p43_ge_32.tag));
            assertFalse( dobj.contains(p43_ge_dd.tag));
        } catch( Exception e) {
            fail( "Unexpected exception: " + e);
        }
    }

}
